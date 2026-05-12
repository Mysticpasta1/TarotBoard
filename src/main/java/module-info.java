/**
 * Core module for the TarotBoard virtual tabletop application.
 * Provides JavaFX-based board game controls, networking, theming, and persistence.
 */
module com.mystic.tarotboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;

    requires com.google.gson;

    requires flexmark;
    requires flexmark.util.data;
    requires flexmark.ext.tables;
    requires flexmark.util.ast;
    requires java.desktop;

    opens com.mystic.tarotboard to javafx.fxml, com.google.gson;
    opens com.mystic.tarotboard.theming to com.google.gson;
    opens com.mystic.tarotboard.theming.configs to com.google.gson;
    opens com.mystic.tarotboard.network to com.google.gson;
    opens com.mystic.tarotboard.utils to com.google.gson;

    exports com.mystic.tarotboard;
    exports com.mystic.tarotboard.network;
    exports com.mystic.tarotboard.network.client;
    exports com.mystic.tarotboard.network.server;
    exports com.mystic.tarotboard.theming;
    exports com.mystic.tarotboard.items;
    exports com.mystic.tarotboard.scenes;
    opens com.mystic.tarotboard.scenes to com.google.gson, javafx.fxml;
}
