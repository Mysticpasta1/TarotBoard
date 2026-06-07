package com.mystic.tarotboard.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A JavaFX Stage that acts as a console window, redirecting System.out and System.err
 * to a TextArea within the window. This is particularly useful for capturing log output
 * from packaged applications that would otherwise run without a visible console.
 */
public class LogWindow extends Stage {

    private final TextArea logTextArea;

    /**
     * Constructs a new LogWindow.
     * <p>
     * This constructor initializes the JavaFX components for the logging window,
     * sets up the scene, and immediately redirects the standard output and error streams
     * to the window's text area.
     */
    public LogWindow() {
        setTitle("Headless Server Log");
        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        logTextArea.setStyle("-fx-control-inner-background:#2b2b2b; -fx-font-family: 'monospace'; -fx-font-size: 12px; -fx-text-fill: #cccccc;");

        VBox root = new VBox(logTextArea);
        Scene scene = new Scene(root, 700, 500);
        setScene(scene);

        redirectSystemStreams();
    }

    /**
     * Redirects the standard output and standard error streams to the log window's TextArea.
     * <p>
     * This is achieved by creating a custom {@link OutputStream} that appends any written
     * data to the TextArea on the JavaFX Application Thread. Both {@code System.out} and
     * {@code System.err} are then set to new {@link PrintStream} instances that use this
     * custom output stream.
     */
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                Platform.runLater(() -> logTextArea.appendText(String.valueOf((char) b)));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                Platform.runLater(() -> {
                    logTextArea.appendText(text);
                    logTextArea.setScrollTop(Double.MAX_VALUE);
                });
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
