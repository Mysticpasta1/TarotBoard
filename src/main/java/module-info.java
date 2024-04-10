module com.mystic.playingcardgame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.mystic.playingcardgame to javafx.fxml;
    exports com.mystic.playingcardgame;
}