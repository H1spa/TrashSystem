package com.example.trash.controllers;

import com.example.trash.dao.NotificationDAO;
import com.example.trash.dao.SessionDAO;
import com.example.trash.dao.UserDAO;
import com.example.trash.model.Notification;
import com.example.trash.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseController {

    protected Timeline sessionCheckTimer;
    protected ScheduledExecutorService notificationScheduler;
    protected int currentUserId;
    protected String currentUserRole;
    protected User currentUser;

    @FXML
    protected Label userStatusLabel;
    @FXML
    protected Label userNameLabel;
    @FXML
    protected Label userRoleLabel;
    @FXML
    protected ImageView userAvatar;

    @FXML
    public void initialize() {
        currentUserId = com.example.trash.controllers.LoginController.SessionManager.getCurrentUserId();
        currentUserRole = com.example.trash.controllers.LoginController.SessionManager.getCurrentUserRole();
        currentUser = UserDAO.getUserById(currentUserId);

        if (currentUserId > 0 && currentUser != null) {
            updateUserInterface();
            startSessionCheckTimer();
            startNotificationChecker();
        }
    }

    private void startSessionCheckTimer() {
        sessionCheckTimer = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> checkSession())
        );
        sessionCheckTimer.setCycleCount(Timeline.INDEFINITE);
        sessionCheckTimer.play();
    }

    private void startNotificationChecker() {
        notificationScheduler = Executors.newSingleThreadScheduledExecutor();
        notificationScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::checkNotifications);
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void checkSession() {
        Platform.runLater(() -> {
            boolean isSessionActive = SessionDAO.isSessionActive(currentUserId);

            if (!isSessionActive) {
                showAlertAndLogout("Сессия завершена",
                        "Ваша сессия была завершена администратором.", Alert.AlertType.WARNING);
            } else {
                SessionDAO.updateLastActivity(currentUserId);
            }
        });
    }

    private void checkNotifications() {
        Platform.runLater(() -> {
            var notifications = NotificationDAO.getUnreadNotifications(currentUserId);

            for (Notification notification : notifications) {
                handleNotification(notification);
                NotificationDAO.markAsRead(notification.getId());
            }
        });
    }

    private void handleNotification(Notification notification) {
        String type = notification.getType();
        String message = notification.getMessage();

        switch (type) {
            case "DISCONNECT":
                showAlertAndLogout("Отключение от системы", message, Alert.AlertType.WARNING);
                break;
            case "BLOCK":
                showAlertAndLogout("Блокировка учетной записи", message, Alert.AlertType.ERROR);
                break;
            case "INFO":
            case "WARNING":
                showNotificationAlert(notification);
                break;
        }
    }

    private void showNotificationAlert(Notification notification) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Уведомление от администратора");
        alert.setHeaderText("Новое сообщение");
        alert.setContentText(notification.getMessage());
        alert.showAndWait();
    }

    private void showAlertAndLogout(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            stopAllTimers();

            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            logoutToLoginScreen();
        });
    }

    protected void updateUserInterface() {
        if (currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUserRole);
            }
            if (userStatusLabel != null) {
                userStatusLabel.setText("Статус: онлайн");
                userStatusLabel.setStyle("-fx-text-fill: green;");
            }
            if (userAvatar != null) {
                userAvatar.setImage(loadUserAvatar());
            }
        }
    }

    private Image loadUserAvatar() {
        String imagePath;
        switch(currentUserRole) {
            case "Администратор":
                imagePath = "/import/photo/administrator.png";
                break;
            case "Бухгалтер":
                imagePath = "/import/photo/accountant.jpeg";
                break;
            case "Лаборант":
                imagePath = "/import/photo/laborant_1.jpeg";
                break;
            case "Лаборант-исследователь":
                imagePath = "/import/photo/laborant_2.jpeg";
                break;
            default:
                imagePath = "/import/photo/Логотип.png";
        }
        try {
            return new Image(getClass().getResourceAsStream(imagePath));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/import/photo/Логотип.png"));
        }
    }

    // Изменено с protected на public
    @FXML
    public void handleLogout() {
        logoutToLoginScreen();
    }

    protected void logoutToLoginScreen() {
        stopAllTimers();

        if (currentUserId > 0) {
            // Завершаем сессию
            SessionDAO.endSession(currentUserId);

            // Также обновляем статус в UserDAO
            UserDAO.disconnectUser(currentUserId);
        }

        try {
            Stage stage = getCurrentStage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/login.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Авторизация");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract Stage getCurrentStage();

    protected void stopAllTimers() {
        if (sessionCheckTimer != null) {
            sessionCheckTimer.stop();
        }
        if (notificationScheduler != null) {
            notificationScheduler.shutdownNow();
        }
    }
}