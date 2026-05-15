module tarotboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires weupnp;
    requires java.net.http;

    opens com.mystic.tarotboard to javafx.fxml;
    opens com.mystic.tarotboard.theming.configs to com.google.gson;
    opens com.mystic.tarotboard.theming to com.google.gson;
    opens com.mystic.tarotboard.utils to javafx.graphics; // For LogWindow
    opens com.mystic.tarotboard.network.server to javafx.graphics; // For HeadlessServerLauncher

    exports com.mystic.tarotboard;
    exports com.mystic.tarotboard.network;
    exports com.mystic.tarotboard.network.client;
    exports com.mystic.tarotboard.network.server;
    exports com.mystic.tarotboard.scenes;
    exports com.mystic.tarotboard.items;
    exports com.mystic.tarotboard.theming;
    exports com.mystic.tarotboard.utils;
}
