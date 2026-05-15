package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.utils.LogWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * A JavaFX application that serves as the entry point for the self-contained headless server.
 * <p>
 * This class initializes the JavaFX toolkit, displays a {@link LogWindow} to capture
 * all console output, and then starts the {@link GameServer} in a background thread.
 * This provides a user-friendly way to run the server and monitor its activity without
 * requiring a command-line interface.
 */
public class HeadlessServerLauncher extends Application {

    private GameServer gameServer;

    /**
     * The main entry point for the JavaFX application.
     * <p>
     * This method is called after the JavaFX runtime has been initialized. It creates
     * and displays the {@link LogWindow} and then starts the {@link GameServer} in a
     * background thread to keep the UI responsive.
     *
     * @param primaryStage The primary stage for this application, though it is not
     *                     used directly as a new LogWindow stage is created.
     */
    @Override
    public void start(Stage primaryStage) {
        LogWindow logWindow = new LogWindow();
        logWindow.show();

        new Thread(() -> {
            try {
                int port = 5555;
                String hostName = "Headless Server";
                double r = 1.0, g = 1.0, b = 1.0;

                gameServer = new GameServer(port, hostName, r, g, b);
                gameServer.start();
                System.out.println("Headless GameServer started on port " + port);
            } catch (IOException e) {
                System.err.println("Failed to start Headless GameServer: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * This method is called when the application should stop, and is used
     * to gracefully shut down the running {@link GameServer}.
     */
    @Override
    public void stop() {
        if (gameServer != null) {
            gameServer.stop();
            System.out.println("Headless GameServer stopped.");
        }
    }

    /**
     * The main entry point for the application. This method is responsible for
     * launching the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
