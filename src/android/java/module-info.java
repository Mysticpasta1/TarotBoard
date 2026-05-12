module com.mystic.tarotboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.net.http;

    requires com.google.gson;

    requires flexmark;
    requires flexmark.util.data;
    requires flexmark.ext.tables;

    opens com.mystic.tarotboard to javafx.fxml, com.google.gson;

    exports com.mystic.tarotboard;
    exports com.mystic.tarotboard.network;
    exports com.mystic.tarotboard.network.client;
    exports com.mystic.tarotboard.network.server;
    exports com.mystic.tarotboard.theming;
}
