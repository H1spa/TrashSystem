module com.example.trash {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.google.gson;
    requires java.sql;
    requires kernel;
    requires layout;
    requires spark.core;
    requires io;
    requires okhttp3;

    // Открываем пакеты для JavaFX рефлексии
    opens com.example.trash to javafx.fxml;
    opens com.example.trash.controllers to javafx.fxml;

    // ВАЖНО: открываем пакет model для javafx.base
    opens com.example.trash.model to javafx.base, javafx.fxml;

    // Если есть DAO, которые используются в TableView
    opens com.example.trash.dao to javafx.base;

    // Экспортируем пакеты
    exports com.example.trash;
    exports com.example.trash.controllers;
    exports com.example.trash.model;
    exports com.example.trash.dao;
    exports com.example.trash.db;
}