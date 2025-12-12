package com.example.trash;

import com.example.trash.api.MobileAPI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Запускаем Mobile API в отдельном потоке
        new Thread(() -> {
            try {
                MobileAPI.setup();
            } catch (Exception e) {
                System.err.println("Ошибка запуска API: " + e.getMessage());
            }
        }).start();

        // Запускаем JavaFX приложение
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml_file/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Авторизация");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}