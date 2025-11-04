package com.example.trash.controllers;

import com.example.trash.dao.UserDAO;
import com.example.trash.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label blockLabel; // Добавь в FXML

    private final UserDAO userDAO = new UserDAO();

    // === Глобальные переменные блокировки ===
    private static boolean isBlocked = false;
    private static int blockSeconds = 0;
    private static Timeline blockTimer;
    private static int failedAttempts = 0;

    @FXML
    public void initialize() {
        roleBox.getItems().addAll(
                "Администратор",
                "Лаборант",
                "Лаборант-исследователь"
        );
        checkBlockStatus();
    }

    private void checkBlockStatus() {
        if (isBlocked) {
            disableLogin();
        }
    }

    private void disableLogin() {
        loginField.setDisable(true);
        passwordField.setDisable(true);
        roleBox.setDisable(true);
        if (blockLabel != null)
            blockLabel.setText("Вход заблокирован на " + blockSeconds + " сек.");
    }

    // === Статический метод, доступный из других классов ===
    public static void startBlock(Label labelToUpdate) {
        if (isBlocked) return;

        isBlocked = true;
        blockSeconds = 60; // 1 минута

        labelToUpdate.setText("Блокировка: " + blockSeconds + " сек.");
        blockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            blockSeconds--;
            if (blockSeconds > 0) {
                labelToUpdate.setText("Блокировка: " + blockSeconds + " сек.");
            } else {
                blockTimer.stop();
                isBlocked = false;
                labelToUpdate.setText("Можно снова войти.");
            }
        }));
        blockTimer.setCycleCount(blockSeconds);
        blockTimer.play();
    }

    @FXML
    protected void handleLogin() {
        if (isBlocked) {
            messageLabel.setText("Попробуйте позже: блокировка активна");
            return;
        }

        String login = loginField.getText();
        String password = passwordField.getText();
        String selectedRole = roleBox.getValue();

        if (selectedRole == null) {
            messageLabel.setText("Выберите роль");
            return;
        }

        User user = userDAO.findByLoginAndPassword(login, password);
        if (user == null) {
            messageLabel.setText("Неверный логин или пароль");
            failedAttempts++;
            if (failedAttempts >= 3) {
                startBlock(messageLabel); // Запуск блокировки
                failedAttempts = 0;
            }
            return;
        }

        String roleText = switch (user.getType()) {
            case "1" -> "Администратор";
            case "2" -> "Лаборант";
            case "3" -> "Лаборант-исследователь";
            default -> "Неизвестно";
        };

        if (!selectedRole.equals(roleText)) {
            messageLabel.setText("Роль выбрана неверно");
            return;
        }

        // ✅ Успешный вход
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    switch (user.getType()) {
                        case "1" -> "/fxml_file/admin.fxml";
                        case "2" -> "/fxml_file/lab.fxml";
                        case "3" -> "/fxml_file/lab_research.fxml";
                        default -> null;
                    }
            ));

            if (loader == null) {
                messageLabel.setText("Не найдена форма для роли");
                return;
            }

            Stage stage = (Stage) loginField.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(selectedRole);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Ошибка загрузки окна");
        }
    }
}
