package com.example.trash.controllers;

import com.example.trash.dao.UserDAO;
import com.example.trash.model.User;
import com.example.trash.dao.SessionDAO;
import com.example.trash.dao.NotificationDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    // === Метод для обработки предыдущих сессий ===
    private void handlePreviousSessions(int userId, String currentIp) {
        try {
            // Получаем все активные сессии пользователя
            List<com.example.trash.model.Session> activeSessions = SessionDAO.getActiveSessionsByUser(userId);
            StringBuilder previousIPs = new StringBuilder();

            for (com.example.trash.model.Session session : activeSessions) {
                // Если сессия активна и IP отличается от текущего
                if (session.isActive() && !session.getIpAddress().equals(currentIp)) {
                    if (previousIPs.length() > 0) {
                        previousIPs.append(", ");
                    }
                    previousIPs.append(session.getIpAddress());

                    // Создаем уведомление для предыдущей сессии
                    String disconnectMessage = "Произведен новый вход в учетную запись с компьютера IP: " +
                            currentIp + ". Текущий сеанс завершен.";
                    NotificationDAO.createNotification(
                            userId,
                            0, // от системы
                            disconnectMessage,
                            "DISCONNECT"
                    );

                    // Завершаем предыдущую сессию
                    SessionDAO.endSessionById(session.getId());
                }
            }

            // Если были обнаружены предыдущие сессии, создаем уведомление для текущего пользователя
            if (previousIPs.length() > 0) {
                String infoMessage = "Обнаружены незавершенные сеансы на компьютерах с IP: " +
                        previousIPs.toString() + ". Эти сеансы завершены.";
                NotificationDAO.createNotification(userId, 0, infoMessage, "INFO");

                // Показываем предупреждение текущему пользователю
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Предыдущие сеансы");
                    alert.setHeaderText("Обнаружены незавершенные сеансы");
                    alert.setContentText("На компьютерах с IP " + previousIPs.toString() +
                            " были обнаружены незавершенные сеансы. Они были завершены.");
                    alert.show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        // Автофокус на поле логина
        Platform.runLater(() -> loginField.requestFocus());
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
            // Проверяем, не заблокирован ли пользователь
            if (UserDAO.isUserBlocked(user.getId())) {
                messageLabel.setText("Учетная запись заблокирована администратором!");
                UserDAO.logLoginAttempt(user.getId(), ipAddress, false);
                return;
            }

            // Проверяем соответствие выбранной роли типу пользователя
            if (isRoleMatching(String.valueOf(user.getType()), selectedRole)) {
                // Проверяем и завершаем предыдущие сессии пользователя
                handlePreviousSessions(user.getId(), ipAddress);

                // Сохраняем текущего пользователя в SessionManager
                SessionManager.setCurrentUserId(user.getId());
                SessionManager.setCurrentUserRole(selectedRole);

                // Создаем новую сессию
                int sessionId = SessionDAO.createSession(user.getId(), ipAddress);

                if (sessionId > 0) {
                    // Сохраняем ID сессии в SessionManager
                    SessionManager.setCurrentSessionId(String.valueOf(sessionId));

                    // Логируем успешный вход
                    UserDAO.logLoginAttempt(user.getId(), ipAddress, true);

                    // Сбрасываем счетчик неудачных попыток
                    resetFailedAttempts();

                    // Переходим на главное окно системы
                    openMainWindow(user, selectedRole);
                } else {
                    messageLabel.setText("Ошибка создания сессии!");
                }

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

    private boolean isRoleMatching(String userType, String selectedRole) {
        if (userType == null || selectedRole == null) {
            return false;
        }


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

    // Проверка блокировки пользователя
    private boolean isUserBlocked(int userId) {
        String sql = "SELECT archived FROM users WHERE id = ?";
        try (Connection conn = com.example.trash.db.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("archived");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Обработка нажатия клавиши Enter в полях ввода
    @FXML
    private void handleEnterPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            handleLogin();
        }
    }

    public static class SessionManager {
        private static int currentUserId;
        private static String currentSessionId;
        private static String currentUserRole;

        public static void setCurrentUserId(int userId) {
            currentUserId = userId;
        }

        public static int getCurrentUserId() {
            return currentUserId;
        }

        public static void setCurrentUserRole(String userRole) {
            currentUserRole = userRole;
        }

        public static String getCurrentUserRole() {
            return currentUserRole;
        }

        public static void setCurrentSessionId(String sessionId) {
            currentSessionId = sessionId;
        }

        public static String getCurrentSessionId() {
            return currentSessionId;
        }

        public static void logout() {
            // Завершаем сессию пользователя
            if (currentUserId > 0) {
                SessionDAO.endSession(currentUserId);
                UserDAO.disconnectUser(currentUserId);
            }

            // Сбрасываем все данные
            currentUserId = 0;
            currentSessionId = null;
            currentUserRole = null;
        }
    }

    
}