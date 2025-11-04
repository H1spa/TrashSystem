module com.example.trash {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;

    opens com.example.trash to javafx.fxml;
    exports com.example.trash;
    exports com.example.trash.controllers;
    opens com.example.trash.controllers to javafx.fxml;
}