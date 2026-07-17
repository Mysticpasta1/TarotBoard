package com.mystic.tarotboard.network.server;

import com.mystic.tarotboard.network.ServerAddress;
import com.mystic.tarotboard.utils.LogWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * A JavaFX application that serves as the entry point for the self-contained headless server.
 * <p>
 * It opens a small panel for configuring the listening port and the operator password before the
 * server starts, since a packaged server has no command line to pass those on. The fields are
 * pre-filled from any arguments or environment variables that were supplied, so the panel shows
 * what a hosting provider already configured rather than overriding it.
 * <p>
 * Once started, a {@link LogWindow} captures all console output so the server can be monitored
 * without a terminal.
 */
public class HeadlessServerLauncher extends Application {

    private HeadlessServer server;
    private LogWindow logWindow;

    private TextField portField;
    private PasswordField passwordField;
    private TextField passwordPlainField;
    private Label statusLabel;
    private Button startButton;

    /**
     * Builds and shows the server configuration panel.
     *
     * @param primaryStage the primary stage, used for the configuration panel
     */
    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();

        Label title = new Label("TarotBoard Dedicated Server");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dddddd;");

        portField = new TextField(String.valueOf(ServerAddress.resolvePort(args)));
        portField.setTooltip(new Tooltip(
                "Port to listen on. Use 0 to let the system allocate a free port."));

        String password = HeadlessServer.resolvePassword(args);
        passwordField = new PasswordField();
        passwordField.setText(password);
        passwordPlainField = new TextField(password);
        passwordPlainField.setManaged(false);
        passwordPlainField.setVisible(false);
        passwordPlainField.textProperty().bindBidirectional(passwordField.textProperty());
        Tooltip passwordTip = new Tooltip(
                "Password players enter to gain operator access. Leave blank to disable operator auth.");
        passwordField.setTooltip(passwordTip);
        passwordPlainField.setTooltip(passwordTip);

        CheckBox showPassword = new CheckBox("Show");
        showPassword.setStyle("-fx-text-fill: #cccccc;");
        showPassword.selectedProperty().addListener((obs, wasShown, shown) -> {
            passwordPlainField.setVisible(shown);
            passwordPlainField.setManaged(shown);
            passwordField.setVisible(!shown);
            passwordField.setManaged(!shown);
        });

        startButton = new Button("Start Server");
        startButton.setDefaultButton(true);
        startButton.setOnAction(event -> startServer());

        statusLabel = new Label("Ready to start.");
        statusLabel.setStyle("-fx-text-fill: #cccccc;");
        statusLabel.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        Label portLabel = new Label("Port:");
        portLabel.setStyle("-fx-text-fill: #cccccc;");
        Label passwordLabel = new Label("Operator password:");
        passwordLabel.setStyle("-fx-text-fill: #cccccc;");
        form.addRow(0, portLabel, portField);
        form.addRow(1, passwordLabel, passwordField, showPassword);
        form.add(passwordPlainField, 1, 1);

        VBox root = new VBox(14, title, form, startButton, statusLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: #2b2b2b;");

        primaryStage.setTitle("TarotBoard Dedicated Server");
        primaryStage.setScene(new Scene(root, 460, 240));
        primaryStage.show();
    }

    /**
     * Validates the panel's fields and starts the server on a background thread, leaving the
     * JavaFX thread free while the socket opens and UPnP forwarding is negotiated.
     */
    private void startServer() {
        if (server != null) return;

        String portText = portField.getText().trim();
        int port;
        if (portText.equals("0")) {
            port = 0;
        } else {
            try {
                port = ServerAddress.parsePort(portText);
            } catch (IllegalArgumentException e) {
                statusLabel.setText(e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #e06c6c;");
                return;
            }
        }

        String password = passwordField.getText();
        startButton.setDisable(true);
        portField.setDisable(true);
        passwordField.setDisable(true);
        passwordPlainField.setDisable(true);
        statusLabel.setText("Starting...");
        statusLabel.setStyle("-fx-text-fill: #cccccc;");

        // The log window must exist before the server starts, or its startup output is lost.
        logWindow = new LogWindow();
        logWindow.show();

        Thread startThread = new Thread(() -> {
            try {
                HeadlessServer started = new HeadlessServer(port, password);
                server = started;
                Platform.runLater(() -> onStarted(started.getPort(), password));
            } catch (IOException e) {
                Platform.runLater(() -> onStartFailed(e));
            }
        });
        startThread.setDaemon(true);
        startThread.start();
    }

    private void onStarted(int boundPort, String password) {
        statusLabel.setText("Running. Players connect to "
                + new ServerAddress(ServerAddress.PUBLIC_SERVER.host(), boundPort)
                + (password.isEmpty() ? "\nOperator auth disabled." : "\nOperator auth enabled."));
        statusLabel.setStyle("-fx-text-fill: #6cc06c;");
        startButton.setText("Running");
    }

    private void onStartFailed(IOException e) {
        statusLabel.setText("Failed to start: " + e.getMessage());
        statusLabel.setStyle("-fx-text-fill: #e06c6c;");
        startButton.setDisable(false);
        startButton.setText("Start Server");
        portField.setDisable(false);
        passwordField.setDisable(false);
        passwordPlainField.setDisable(false);
        if (logWindow != null) {
            logWindow.close();
            logWindow = null;
        }
    }

    /**
     * This method is called when the application should stop, and is used
     * to gracefully shut down the running {@link HeadlessServer}.
     */
    @Override
    public void stop() {
        if (server != null) {
            server.stop();
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
