module com.dezzelshipc.image_redactor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires opencv;
    requires java.desktop;

    opens com.dezzelshipc.image_redactor to javafx.fxml;
    exports com.dezzelshipc.image_redactor;
}