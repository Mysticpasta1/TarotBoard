package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.network.NetworkMessage.PlayerInfo;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Multi-client TCP server that accepts connections, assigns player IDs, and relays messages
 * between clients. The host player (playerId=0) is created at startup.
 * It also handles UPnP port forwarding for easier global access.
 */
public class GameServer {
    /** How many consecutive external ports to try when the router rejects the preferred one. */
    private static final int PORT_MAPPING_ATTEMPTS = 10;

    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<PlayerInfo> players = new CopyOnWriteArrayList<>();
    private final int hostPlayerId;
    private volatile boolean running;
    private Consumer<NetworkMessage> onMessage;
    private GatewayDevice activeGateway;
    private volatile int externalPort = -1;
    private volatile Consumer<Integer> onPortForwarded;
    private Predicate<Integer> isOperatorCheck = playerId -> false; // Default: no one is operator

    private final double[][] playerColors = {
            {1.0, 0.2, 0.2}, {0.2, 1.0, 0.2}, {0.2, 0.5, 1.0}, {1.0, 1.0, 0.2},
            {1.0, 0.2, 1.0}, {0.2, 1.0, 1.0}, {1.0, 0.6, 0.2}, {1.0, 0.3, 0.7},
            {0.5, 0.8, 0.2}, {0.2, 0.8, 0.8}, {0.8, 0.2, 0.5}, {0.9, 0.7, 0.1}
    };

    /**
     * Creates a new game server listening on the given port.
     *
     * @param port     the TCP port to listen on, or 0 to bind a port allocated by the OS
     *                 (see {@link #getPort()} for the port actually bound)
     * @param hostName the display name for the host player
     * @param hostR    the red component of the host player color
     * @param hostG    the green component of the host player color
     * @param hostB    the blue component of the host player color
     * @throws IOException if the server socket cannot be opened
     */
    public GameServer(int port, String hostName, double hostR, double hostG, double hostB) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.hostPlayerId = 0;
        players.add(new PlayerInfo(0, hostName, hostR, hostG, hostB));
        this.running = true;
    }

    /**
     * Sets the predicate used to check if a player is an operator.
     * This is used to restrict certain actions to operators only.
     *
     * @param isOperatorCheck The predicate to use for checking operator status.
     */
    public void setIsOperatorCheck(Predicate<Integer> isOperatorCheck) {
        this.isOperatorCheck = isOperatorCheck;
    }

    /**
     * Returns the player ID of the host (always 0).
     *
     * @return the host player ID
     */
    public int getHostPlayerId() {
        return hostPlayerId;
    }

    /**
     * Returns an immutable copy of the current player list.
     *
     * @return the list of connected players
     */
    public List<PlayerInfo> getPlayers() {
        return List.copyOf(players);
    }

    /**
     * Returns the port the server is actually listening on. When the server was constructed with
     * port 0 this is the port the OS allocated, so it is the only reliable port to show or save.
     *
     * @return the local port
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Returns the port clients outside the network must connect to. This is the external port of
     * the UPnP mapping, which is not always {@link #getPort()} — if the router already had that
     * port mapped, forwarding falls back to a different external port.
     *
     * @return the forwarded external port, or -1 if forwarding has not succeeded (yet)
     */
    public int getExternalPort() {
        return externalPort;
    }

    /**
     * Sets a callback invoked once port forwarding finishes, with the external port that was
     * mapped, or -1 if no mapping could be made. Forwarding runs in the background, so this may
     * fire seconds after {@link #start()} returns, and it is invoked on that background thread.
     *
     * @param onPortForwarded the callback
     */
    public void setOnPortForwarded(Consumer<Integer> onPortForwarded) {
        this.onPortForwarded = onPortForwarded;
    }

    /**
     * Sets the callback for incoming messages from any client.
     *
     * @param onMessage the message consumer
     */
    public void setOnMessage(Consumer<NetworkMessage> onMessage) {
        this.onMessage = onMessage;
    }

    /**
     * Starts the server's connection acceptance loop in a daemon thread and attempts to set up port forwarding.
     */
    public void start() {
        // UPnP discovery blocks for seconds, so keep it off the caller's thread.
        Thread forwardThread = new Thread(() -> setupPortForwarding(true));
        forwardThread.setDaemon(true);
        forwardThread.start();
        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    int playerId = assignPlayerId();
                    if (playerId < 0) {
                        socket.close();
                        continue;
                    }
                    var color = playerColors[playerId % playerColors.length];
                    var info = new PlayerInfo(playerId, "Player" + playerId,
                            color[0], color[1], color[2]);
                    players.add(info);
                    var handler = new ClientHandler(socket, playerId, info);
                    clients.add(handler);
                    handler.start();

                    broadcast(NetworkMessage.of(new Msg.PlayerList(new ArrayList<>(players))), -1);

                    System.out.println("Player " + playerId + " connected");
                } catch (IOException e) {
                    if (running) System.err.println("Accept error: " + e.getMessage());
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    /**
     * Assigns a unique player ID to a new client.
     *
     * @return A unique player ID, or -1 if no ID could be assigned.
     */
    private synchronized int assignPlayerId() {
        for (int i = 1; i < 100; i++) {
            int id = i;
            if (players.stream().noneMatch(p -> p.id() == id)) return id;
        }
        return -1;
    }

    /**
     * Broadcasts a message to all connected clients except the one with the given ID.
     *
     * @param msg       the message to broadcast
     * @param excludeId the player ID to exclude, or -1 to broadcast to everyone
     */
    public void broadcast(NetworkMessage msg, int excludeId) {
        for (var client : clients) {
            if (client.playerId != excludeId) {
                client.send(msg);
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param msg the message to broadcast
     */
    public void broadcastToAll(NetworkMessage msg) {
        broadcast(msg, -1);
    }

    /**
     * Sends a message to a specific player.
     *
     * @param playerId the target player ID
     * @param msg      the message to send
     */
    public void sendTo(int playerId, NetworkMessage msg) {
        for (var client : clients) {
            if (client.playerId == playerId) {
                client.send(msg);
                return;
            }
        }
    }

    /**
     * Stops the server, closes all client connections, removes port forwarding, and closes the server socket.
     */
    public void stop() {
        running = false;
        setupPortForwarding(false);
        for (var client : clients) client.close();
        clients.clear();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Attempts to automatically configure port forwarding using UPnP.
     * <p>
     * The external port is mapped to the port the server actually bound. If the router already
     * has that external port mapped to something else, nearby ports are tried instead, and the
     * one that succeeded becomes {@link #getExternalPort()} — that is the port remote players
     * must use, so it is reported through {@link #setOnPortForwarded}.
     *
     * @param enable true to add a port mapping, false to remove it.
     */
    private void setupPortForwarding(boolean enable) {
        try {
            if (enable) {
                GatewayDiscover discover = new GatewayDiscover();
                discover.discover();
                activeGateway = discover.getValidGateway();

                if (activeGateway == null) {
                    System.err.println("UPnP: No gateway device found.");
                } else {
                    String localAddress = activeGateway.getLocalAddress().getHostAddress();
                    int localPort = getPort();
                    for (int attempt = 0; attempt < PORT_MAPPING_ATTEMPTS; attempt++) {
                        int candidate = localPort + attempt;
                        if (candidate > 65535) break;
                        if (activeGateway.addPortMapping(candidate, localPort, localAddress, "TCP", "TarotBoard Server")) {
                            externalPort = candidate;
                            break;
                        }
                    }
                    if (externalPort < 0) {
                        System.err.println("UPnP: Port forwarding failed. Please configure manually.");
                    } else if (externalPort == localPort) {
                        System.out.println("UPnP: Port forwarding enabled for port " + externalPort);
                    } else {
                        System.out.println("UPnP: Port " + localPort + " was unavailable on the router; "
                                + "forwarded external port " + externalPort + " to local port " + localPort);
                    }
                }
                var callback = onPortForwarded;
                if (callback != null) callback.accept(externalPort);
            } else {
                if (activeGateway != null && externalPort > 0) {
                    if (activeGateway.deletePortMapping(externalPort, "TCP")) {
                        System.out.println("UPnP: Port forwarding disabled for port " + externalPort);
                    } else {
                        System.err.println("UPnP: Failed to remove port forwarding.");
                    }
                    externalPort = -1;
                }
            }
        } catch (Exception e) {
            System.err.println("UPnP: Could not configure port forwarding: " + e.getMessage());
            if (enable) {
                var callback = onPortForwarded;
                if (callback != null) callback.accept(externalPort);
            }
        }
    }

    /**
     * Handles communication with a single client connected to the server.
     */
    private class ClientHandler {
        private final Socket socket;
        private final int playerId;
        private PlayerInfo info;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private volatile boolean active = true;

        /**
         * Constructs a new ClientHandler.
         *
         * @param socket   The client's socket.
         * @param playerId The ID assigned to this client.
         * @param info     PlayerInfo object for this client.
         */
        ClientHandler(Socket socket, int playerId, PlayerInfo info) {
            this.socket = socket;
            this.playerId = playerId;
            this.info = info;
        }

        /**
         * Starts the client handler, setting up input/output streams and a message reading thread.
         */
        void start() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                out.writeObject(NetworkMessage.of(new Msg.YourId(playerId)));
                out.flush();
                out.writeObject(NetworkMessage.of(new Msg.PlayerList(new ArrayList<>(players))));
                out.flush();

                Thread readThread = new Thread(() -> {
                    while (active) {
                        try {
                            var msg = (NetworkMessage) in.readObject();
                            handleMessage(msg);
                        } catch (EOFException | SocketException e) {
                            break;
                        } catch (Exception e) {
                            if (active)
                                System.err.println("Read error from player " + playerId + ": " + e.getMessage());
                            break;
                        }
                    }
                    disconnect();
                });
                readThread.setDaemon(true);
                readThread.start();

            } catch (IOException e) {
                System.err.println("Failed to setup handler for player " + playerId + ": " + e.getMessage());
                disconnect();
            }
        }

        /**
         * Sends a NetworkMessage to this client.
         *
         * @param msg The message to send.
         */
        void send(NetworkMessage msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                if (active) System.err.println("Send error to player " + playerId + ": " + e.getMessage());
            }
        }

        /**
         * Handles an incoming NetworkMessage from the client, processing it or broadcasting it to others.
         *
         * @param msg The incoming NetworkMessage.
         */
        private void handleMessage(NetworkMessage msg) {
            switch (msg.data()) {
                case Msg.PlayerJoin join -> {
                    PlayerInfo updated = new PlayerInfo(playerId, join.name(), join.r(), join.g(), join.b());
                    players.remove(info);
                    players.add(updated);
                    this.info = updated;
                    var listMsg = NetworkMessage.of(new Msg.PlayerList(new ArrayList<>(players)));
                    broadcast(listMsg, -1);
                    if (onMessage != null) onMessage.accept(listMsg);
                }
                case Msg.PlayerLeave ignored -> disconnect();
                case Msg.CursorMove c ->
                        broadcast(NetworkMessage.of(new Msg.CursorMove(playerId, c.x(), c.y())), playerId);
                case Msg.CursorImage c ->
                        broadcast(NetworkMessage.of(new Msg.CursorImage(playerId, c.imageData())), playerId);
                case Msg.SendState ignored -> {
                    if (onMessage != null) {
                        onMessage.accept(NetworkMessage.of(new Msg.SendState(playerId)));
                    }
                    return;
                }
                case Msg.CardNamesSync ignored -> {
                    broadcast(msg, playerId);
                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                    return;
                }
                case Msg.RequestOperator ignored -> {
                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                    return;
                }
                case Msg.ReshuffleCards ignored -> {
                    if (isOperatorCheck.test(playerId)) {
                        broadcast(msg, playerId);
                    } else {
                        System.out.println("Denied ReshuffleCards from non-operator player " + playerId);
                    }
                }
                case Msg.ResetDice ignored -> {
                    if (isOperatorCheck.test(playerId)) {
                        broadcast(msg, playerId);
                    } else {
                        System.out.println("Denied ResetDice from non-operator player " + playerId);
                    }
                }
                case Msg.ResetChips ignored -> {
                    if (isOperatorCheck.test(playerId)) {
                        broadcast(msg, playerId);
                    } else {
                        System.out.println("Denied ResetChips from non-operator player " + playerId);
                    }
                }
                case Msg.NewGame ignored -> {
                    if (isOperatorCheck.test(playerId)) {
                        broadcast(msg, playerId);
                    } else {
                        System.out.println("Denied NewGame from non-operator player " + playerId);
                    }
                }
                default -> broadcast(msg, playerId);
            }
            if (onMessage != null) {
                onMessage.accept(msg);
            }
        }

        /**
         * Disconnects the client, removes it from the server's active lists, and notifies other players.
         */
        void disconnect() {
            if (!active) return;
            active = false;
            clients.remove(this);
            players.remove(info);
            broadcast(NetworkMessage.of(new Msg.PlayerLeave(playerId)), -1);
            var listMsg = NetworkMessage.of(new Msg.PlayerList(new ArrayList<>(players)));
            broadcast(listMsg, -1);
            if (onMessage != null) onMessage.accept(listMsg);
            System.out.println("Player " + playerId + " disconnected");
            close();
        }

        /**
         * Closes the client's socket connection.
         */
        void close() {
            active = false;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
