module com.mystic.tarotboard {
    // Required JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.web;
    requires jdk.jsobject;

    // Required External Modules
    requires com.google.gson; // Standard module name for Gson

    requires flexmark;
    requires flexmark.util.data;
    requires flexmark.ext.tables;

    // Open packages for JavaFX FXML reflection
    opens com.mystic.tarotboard to javafx.fxml, com.google.gson;

    // Export your main package
    exports com.mystic.tarotboard;
}
