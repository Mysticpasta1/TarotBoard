module com.mystic.pcg {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.mystic.pcg to javafx.fxml;
    exports com.mystic.pcg;
}