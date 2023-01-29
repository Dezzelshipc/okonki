module com.dezzelshipc.image_redactor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires opencv;
    requires java.desktop;
    requires com.google.gson;

    opens com.dezzelshipc.image_redactor to javafx.fxml, com.google.gson;
    exports com.dezzelshipc.image_redactor;
}