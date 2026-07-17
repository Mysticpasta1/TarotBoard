package com.mystic.tarotboard.network.client;

import com.mystic.tarotboard.network.NetworkMessage;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * TCP client that connects to a TarotBoard server and exchanges serialised {@link NetworkMessage} objects.
 */
public class GameClient {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private volatile boolean connected;
    private Consumer<NetworkMessage> onMessage;
    /** Guards {@link #out}: ObjectOutputStream is not thread-safe and sends are not confined to one thread. */
    private final Object writeLock = new Object();

    /**
     * Connects to the TarotBoard server at the given host and port.
     *
     * @param host the server hostname or IP address
     * @param port the server port
     * @throws IOException if the connection cannot be established
     */
    public GameClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;
    }

    /**
     * Sets the callback for incoming messages from the server.
     *
     * @param onMessage the message consumer
     */
    public void setOnMessage(Consumer<NetworkMessage> onMessage) {
        this.onMessage = onMessage;
    }

    /**
     * Starts the background reader thread that processes incoming messages.
     */
    public void start() {
        Thread readThread = new Thread(() -> {
            while (connected) {
                try {
                    var msg = (NetworkMessage) in.readObject();
                    if (onMessage != null) onMessage.accept(msg);
                } catch (EOFException | SocketException e) {
                    break;
                } catch (Exception e) {
                    if (connected) System.err.println("Client read error: " + e.getMessage());
                    break;
                }
            }
            disconnect();
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    /**
     * Sends a message to the server.
     *
     * @param msg the message to send
     */
    public void send(NetworkMessage msg) {
        try {
            synchronized (writeLock) {
                out.writeObject(msg);
                // Without this the stream's back-reference table pins every message ever sent.
                out.reset();
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Client send error: " + e.getMessage());
        }
    }

    /**
     * Returns whether the client is currently connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Disconnects from the server and releases resources.
     */
    public void disconnect() {
        connected = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
