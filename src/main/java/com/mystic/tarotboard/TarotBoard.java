package com.mystic.tarotboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class TarotBoard extends Application {
    private ClientNetwork clientNetwork;
    private GameUI gameUI;
    private PokerServer server;

    @Override
    public void start(Stage primaryStage) {
        String playerName = askPlayerName();
        if (playerName == null || playerName.isBlank()) {
            playerName = "Player" + (int) (Math.random() * 10000);
        }

        primaryStage.setTitle("TarotBoard Poker - " + playerName);
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Create Server", Arrays.asList("Create Server", "Join Server"));
        choiceDialog.setTitle("TarotBoard Poker");
        choiceDialog.setHeaderText("Choose an option");
        choiceDialog.setContentText("Create or Join:");

        Optional<String> choiceOpt = choiceDialog.showAndWait();
        if (choiceOpt.isEmpty()) {
            Platform.exit();
            return;
        }
        String choice = choiceOpt.get();

        if (choice.equals("Create Server")) {
            createServerFlow(primaryStage, playerName);
        } else {
            joinServerFlow(primaryStage, playerName);
        }
    }

    private void createServerFlow(Stage primaryStage, String playerName) {
        TextInputDialog ipDialog = new TextInputDialog("");
        ipDialog.setTitle("Create Server");
        ipDialog.setHeaderText("Enter IP to bind (leave empty for all interfaces):");
        ipDialog.setContentText("IP:");
        String host = ipDialog.showAndWait().orElse("");
        if (host.isBlank()) host = null;

        int port = promptPort("Create Server", 55555);

        String finalHost = host;
        new Thread(() -> {
            server = new PokerServer();
            try {
                server.start(finalHost, port);
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showError("Failed to start server: " + e.getMessage());
                    Platform.exit();
                });
            }
        }, "PokerServer-Thread").start();

        joinServer(primaryStage, "localhost", port, playerName);
    }

    private void joinServerFlow(Stage primaryStage, String playerName) {
        TextInputDialog ipDialog = new TextInputDialog("localhost");
        ipDialog.setTitle("Join Server");
        ipDialog.setHeaderText("Enter Server IP:");
        ipDialog.setContentText("IP:");
        String host = ipDialog.showAndWait().orElse("localhost");

        int port = promptPort("Join Server", 55555);

        joinServer(primaryStage, host, port, playerName);
    }


    private int promptPort(String title, int defaultPort) {
        TextInputDialog portDialog = new TextInputDialog(Integer.toString(defaultPort));
        portDialog.setTitle(title);
        portDialog.setHeaderText("Enter Port Number:");
        portDialog.setContentText("Port:");
        int port = defaultPort;
        try {
            port = Integer.parseInt(portDialog.showAndWait().orElse(Integer.toString(defaultPort)));
        } catch (NumberFormatException e) {
            // ignore and use default
        }
        return port;
    }

    private void joinServer(Stage primaryStage, String host, int port, String playerName) {
        gameUI = new GameUI(primaryStage);

        try {
            clientNetwork = new ClientNetwork(host, port, playerName, gameUI);
            gameUI.setClientNetwork(clientNetwork);
        } catch (IOException e) {
            e.printStackTrace();
            gameUI.showMessage("Failed to connect to server at " + host + ":" + port);
        }

        primaryStage.setTitle("TarotBoard Poker - " + playerName);
        primaryStage.setScene(gameUI.getScene());
        primaryStage.show();
    }

    private void showError(String message) {
        // You can implement a popup or dialog here, or simply print
        System.err.println(message);
    }

    private String askPlayerName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Player Name");
        dialog.setHeaderText("Welcome to TarotBoard Poker");
        dialog.setContentText("Please enter your player name:");

        Optional<String> result = dialog.showAndWait();
        return result.map(String::trim).orElse(null);
    }

    @Override
    public void stop() throws Exception {
        if (clientNetwork != null) {
            clientNetwork.close();
        }
        if (server != null) {
            server.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
