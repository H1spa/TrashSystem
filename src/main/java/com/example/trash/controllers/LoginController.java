package com.example.trash.controllers;

import com.example.trash.dao.UserDAO;
import com.example.trash.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetAddress;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> roleBox;
    @FXML private Label blockLabel;

    private final UserDAO userDAO = new UserDAO();

    // === Статические переменные для глобальной блокировки ===
    private static boolean isBlocked = false;
    private static int blockSeconds = 0;
    private static Timeline blockTimer;
    private static int failedAttempts = 0;
    private boolean captchaRequired = false;
    private boolean captchaCooldown = false;
    private int captchaCooldownSeconds = 0;
    private Timeline captchaCooldownTimer;

    // === Статические методы для управления блокировкой ===
    public static void startGlobalBlock(int seconds) {
        if (isBlocked) return;

        isBlocked = true;
        blockSeconds = seconds;

        // Создаем таймер для снятия блокировки
        blockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            blockSeconds--;
            if (blockSeconds <= 0) {
                blockTimer.stop();
                isBlocked = false;
            }
        }));

        blockTimer.setCycleCount(seconds);
        blockTimer.play();
    }

    public static boolean isLoginBlocked() {
        return isBlocked;
    }

    public static int getBlockSeconds() {
        return blockSeconds;
    }

    public static void resetFailedAttempts() {
        failedAttempts = 0;
    }

    public static void incrementFailedAttempts() {
        failedAttempts++;
    }

    public static int getFailedAttempts() {
        return failedAttempts;
    }

    @FXML
    public void initialize() {
        // Инициализация выпадающего списка ролей
        roleBox.getItems().addAll(
                "Администратор",
                "Лаборант",
                "Лаборант-исследователь",
                "Бухгалтер"
        );

        // Проверяем статус блокировки при инициализации
        checkBlockStatus();

        // Настройка обработчика для блока Label
        setupBlockLabelUpdater();
    }

    private void checkBlockStatus() {
        if (isBlocked) {
            disableLogin();
            if (blockLabel != null) {
                blockLabel.setText("Вход заблокирован на " + blockSeconds + " сек.");
                updateBlockLabelTimer();
            }
        } else {
            enableLogin();
            if (blockLabel != null) {
                blockLabel.setText("");
            }
        }
    }

    private void setupBlockLabelUpdater() {
        // Если блокировка активна, обновляем Label каждую секунду
        if (isBlocked && blockTimer == null) {
            // Если таймер не запущен, но блокировка активна
            updateBlockLabelTimer();
        }
    }

    private void updateBlockLabelTimer() {
        if (blockTimer == null && isBlocked && blockSeconds > 0) {
            blockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (blockSeconds > 0) {
                    blockSeconds--;
                    if (blockLabel != null) {
                        blockLabel.setText("Вход заблокирован на " + blockSeconds + " сек.");
                    }
                    if (blockSeconds <= 0) {
                        isBlocked = false;
                        enableLogin();
                        if (blockLabel != null) {
                            blockLabel.setText("Можно снова войти.");
                        }
                        if (blockTimer != null) {
                            blockTimer.stop();
                        }
                    }
                }
            }));
            blockTimer.setCycleCount(Timeline.INDEFINITE);
            blockTimer.play();
        }
    }

    private void disableLogin() {
        loginField.setDisable(true);
        passwordField.setDisable(true);
        roleBox.setDisable(true);
    }

    private void enableLogin() {
        loginField.setDisable(false);
        passwordField.setDisable(false);
        roleBox.setDisable(false);
    }

    @FXML
    public void handleLogin() {
        // Проверка глобальной блокировки входа
        if (isBlocked) {
            messageLabel.setText("Вход заблокирован. Осталось: " + blockSeconds + " сек.");
            if (blockLabel != null) {
                blockLabel.setText("Блокировка: " + blockSeconds + " сек.");
            }
            disableLogin();
            return;
        }

        // Проверка заполнения всех полей
        String login = loginField.getText();
        String password = passwordField.getText();
        String selectedRole = roleBox.getValue();

        if (login.isEmpty() || password.isEmpty() || selectedRole == null) {
            messageLabel.setText("Заполните все поля!");
            return;
        }

        // Получаем IP-адрес клиента
        String ipAddress = getClientIp();

        // Проверяем пользователя в базе данных
        User user = userDAO.findByLoginAndPassword(login, password);

        if (user != null) {
            // Проверяем соответствие выбранной роли типу пользователя
            if (isRoleMatching(user.getType(), selectedRole)) {
                // Логируем успешный вход
                UserDAO.logLoginAttempt(user.getId(), ipAddress, true);

                // Сбрасываем счетчик неудачных попыток
                resetFailedAttempts();

                // Переходим на главное окно системы
                openMainWindow(user, selectedRole);

            } else {
                // Неправильная роль для данного пользователя
                messageLabel.setText("Неверная роль для данного пользователя!");
                UserDAO.logLoginAttempt(user.getId(), ipAddress, false);
                handleFailedAttempt();
            }
        } else {
            // Неправильный логин или пароль
            messageLabel.setText("Неверный логин или пароль!");
            UserDAO.logLoginAttempt(0, ipAddress, false);
            handleFailedAttempt();
        }
    }

    // Метод для проверки соответствия типа пользователя выбранной роли
    private boolean isRoleMatching(String userType, String selectedRole) {
        if (userType == null || selectedRole == null) {
            return false;
        }

        // Маппинг типов пользователей (type_user_id) на роли в интерфейсе
        switch (selectedRole) {
            case "Администратор":
                return userType.equals("1");
            case "Лаборант":
                return userType.equals("2");
            case "Лаборант-исследователь":
                return userType.equals("3");
            case "Бухгалтер":
                return userType.equals("4");
            default:
                return false;
        }
    }

    // Обработка неудачной попытки входа
    private void handleFailedAttempt() {
        incrementFailedAttempts();
        int attempts = getFailedAttempts();

        if (attempts >= 3) {
            // После 3 неудачных попыток блокируем вход на 30 секунд
            startGlobalBlock(30);
            messageLabel.setText("Слишком много попыток. Блокировка на 30 секунд.");
            disableLogin();
        } else if (attempts >= 2) {
            // После 2 неудачных попыток показываем капчу
            showCaptcha();
        }
    }

    // Метод для показа капчи и обработки результата
    private void showCaptcha() {
        // Если капча уже показана и не пройдена - блокируем на 10 секунд
        if (captchaRequired) {
            startCaptchaCooldown();
            return;
        }

        captchaRequired = true;
        if (showCaptchaWindow()) {
            // Капча пройдена успешно
            resetFailedAttempts();
            captchaRequired = false;
            messageLabel.setText("Капча пройдена. Попробуйте снова.");
        } else {
            // Капча не пройдена - блокировка на 10 секунд
            startCaptchaCooldown();
        }
    }

    // Метод для перехода на главное окно системы
    private void openMainWindow(User user, String role) {
        try {
            Stage currentStage = (Stage) loginField.getScene().getWindow();
            String fxmlPath = "";
            String title = "";

            // Определяем путь к FXML файлу в зависимости от роли
            switch (role) {
                case "Администратор":
                    fxmlPath = "/fxml_file/admin.fxml";
                    title = "Панель администратора";
                    break;
                case "Лаборант":
                    fxmlPath = "/fxml_file/lab.fxml";
                    title = "Панель лаборанта";
                    break;
                case "Лаборант-исследователь":
                    fxmlPath = "/fxml_file/lab_research.fxml";
                    title = "Панель лаборанта-исследователя";
                    break;
                case "Бухгалтер":
                    fxmlPath = "/fxml_file/accountant.fxml";
                    title = "Панель бухгалтера";
                    break;
                default:
                    messageLabel.setText("Неизвестная роль!");
                    return;
            }

            // Загружаем FXML и создаем новую сцену
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Передаем информацию о пользователе, если необходимо
            // (можно передавать через конструктор или сеттеры)
            if (role.equals("Администратор")) {
                // Можно передать данные администратору при необходимости
                AdminController adminController = loader.getController();
                // Например: adminController.setCurrentUser(user);
            }

            // Устанавливаем новую сцену на текущем окне
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Ошибка при загрузке интерфейса: " + e.getMessage());
        }
    }

    // Метод для получения IP-адреса клиента
    private String getClientIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    // Метод для отображения окна с капчей
    private boolean showCaptchaWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/captcha.fxml"));
            Scene scene = new Scene(loader.load());

            Stage captchaStage = new Stage();
            captchaStage.setTitle("Подтвердите что вы не робот");
            captchaStage.setScene(scene);
            captchaStage.setResizable(false);

            // Получаем контроллер капчи и проверяем результат
            CaptchaController controller = loader.getController();
            captchaStage.showAndWait();

            return controller.isPassed();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Метод для запуска таймера блокировки после неудачной капчи
    private void startCaptchaCooldown() {
        captchaCooldown = true;
        captchaCooldownSeconds = 10;

        loginField.setDisable(true);
        passwordField.setDisable(true);
        roleBox.setDisable(true);

        messageLabel.setText("Капча не пройдена. Блокировка на " + captchaCooldownSeconds + " сек.");

        captchaCooldownTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    captchaCooldownSeconds--;
                    if (captchaCooldownSeconds > 0) {
                        messageLabel.setText("Капча не пройдена. Блокировка на " + captchaCooldownSeconds + " сек.");
                    } else {
                        captchaCooldownTimer.stop();
                        captchaCooldown = false;
                        captchaRequired = false;
                        messageLabel.setText("Блокировка снята. Попробуйте снова.");

                        loginField.setDisable(false);
                        passwordField.setDisable(false);
                        roleBox.setDisable(false);
                    }
                })
        );

        captchaCooldownTimer.setCycleCount(captchaCooldownSeconds);
        captchaCooldownTimer.play();
    }
}