package com.example.trash.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public abstract class BaseLabController {

    @FXML
    protected Label timerLabel;

    protected int totalSeconds = 120;
    protected Timeline timeline;

    @FXML
    public void initialize() {
        startTimer();
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void tick() {
        totalSeconds--;

        if (totalSeconds <= 0) {
            timeline.stop();
            timerLabel.setText("00:00");
            timerLabel.setStyle("-fx-text-fill: red;");

            showSessionExpired();
            logoutForced();
            return;
        }

        if (totalSeconds <= 60) {
            timerLabel.setStyle("-fx-text-fill: orange;");
        }

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void showSessionExpired() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Сеанс завершён");
            alert.setContentText("Необходимо выполнить кварцевание помещения.");
            alert.showAndWait();

            // ИСПРАВЛЕНО: передаем количество секунд (60), а не Label
            LoginController.startGlobalBlock(60);
        });
    }

    protected void logoutForced() {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) timerLabel.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/login.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Авторизация");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    protected void handleLogout() {
        logoutForced();
    }
}