package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.network.NetworkMessage;
import com.mystic.tarotboard.network.NetworkMessage.Msg;
import com.mystic.tarotboard.network.NetworkMessage.PlayerInfo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Multi-client TCP server that accepts connections, assigns player IDs, and relays messages
 * between clients. The host player (playerId=0) is created at startup.
 */
public class GameServer {
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<PlayerInfo> players = new CopyOnWriteArrayList<>();
    private final int hostPlayerId;
    private volatile boolean running;
    private Consumer<NetworkMessage> onMessage;

    private final double[][] playerColors = {
            {1.0, 0.2, 0.2}, {0.2, 1.0, 0.2}, {0.2, 0.5, 1.0}, {1.0, 1.0, 0.2},
            {1.0, 0.2, 1.0}, {0.2, 1.0, 1.0}, {1.0, 0.6, 0.2}, {1.0, 0.3, 0.7},
            {0.5, 0.8, 0.2}, {0.2, 0.8, 0.8}, {0.8, 0.2, 0.5}, {0.9, 0.7, 0.1}
    };

    /**
     * Creates a new game server listening on the given port.
     *
     * @param port     the TCP port to listen on
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
     * Returns the port the server is listening on.
     *
     * @return the local port
     */
    public int getPort() {
        return serverSocket.getLocalPort();
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
     * Starts the server's connection acceptance loop in a daemon thread.
     */
    public void start() {
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
     * Stops the server, closes all client connections and the server socket.
     */
    public void stop() {
        running = false;
        for (var client : clients) client.close();
        clients.clear();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    private class ClientHandler {
        private final Socket socket;
        private final int playerId;
        private PlayerInfo info;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private volatile boolean active = true;

        ClientHandler(Socket socket, int playerId, PlayerInfo info) {
            this.socket = socket;
            this.playerId = playerId;
            this.info = info;
        }

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

        void send(NetworkMessage msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                if (active) System.err.println("Send error to player " + playerId + ": " + e.getMessage());
            }
        }

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
                case Msg.PlayerLeave _ -> disconnect();
                case Msg.CursorMove c ->
                        broadcast(NetworkMessage.of(new Msg.CursorMove(playerId, c.x(), c.y())), playerId);
                case Msg.CursorImage c ->
                        broadcast(NetworkMessage.of(new Msg.CursorImage(playerId, c.imageData())), playerId);
                case Msg.SendState _ -> {
                    if (onMessage != null) {
                        onMessage.accept(NetworkMessage.of(new Msg.SendState(playerId)));
                    }
                    return;
                }
                case Msg.CardNamesSync _ -> {
                    broadcast(msg, playerId);
                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                    return;
                }
                case Msg.RequestOperator _ -> {
                    if (onMessage != null) {
                        onMessage.accept(msg);
                    }
                    return;
                }
                default -> broadcast(msg, playerId);
            }
            if (onMessage != null) {
                onMessage.accept(msg);
            }
        }

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

        void close() {
            active = false;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
