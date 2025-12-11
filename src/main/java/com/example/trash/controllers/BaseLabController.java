package com.example.trash.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.util.Duration;

public abstract class BaseLabController extends BaseController {

    @FXML
    protected Label timerLabel;

    protected int totalSeconds = 3600;
    protected Timeline quartzTimer;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        startQuartzTimer();
    }

    private void startQuartzTimer() {
        // Добавьте небольшую задержку перед запуском таймера
        Platform.runLater(() -> {
            quartzTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            quartzTimer.setCycleCount(Timeline.INDEFINITE);
            quartzTimer.play();
        });
    }

    private void tick() {
        totalSeconds--;

        if (totalSeconds <= 0) {
            quartzTimer.stop();
            timerLabel.setText("00:00");
            timerLabel.setStyle("-fx-text-fill: red;");

            showSessionExpired();
            logoutToLoginScreen();
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
            alert.setTitle("Сеанс завершён");
            alert.setHeaderText("Время сеанса истекло");
            alert.setContentText("Необходимо выполнить кварцевание помещения.");
            alert.showAndWait();

            LoginController.startGlobalBlock(60);
        });
    }

    @Override
    protected void stopAllTimers() {
        super.stopAllTimers();
        if (quartzTimer != null) {
            quartzTimer.stop();
        }
    }
}