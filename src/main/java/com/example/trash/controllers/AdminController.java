package com.example.trash.controllers;

import com.example.trash.dao.UserDAO;
import com.example.trash.dao.NotificationDAO;
import com.example.trash.dao.SessionDAO;
import com.example.trash.model.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class AdminController extends BaseController {

    @FXML
    private TableView<User> onlineUsersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUser;
    @FXML private TableColumn<User, String> colTime;
    @FXML private TableColumn<User, String> colIp;
    @FXML private TableColumn<User, String> colSessionTime;
    @FXML private TableColumn<User, String> colDailyTime;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnViewHistory;
    @FXML private Button btnManageUsers;
    @FXML private Button btnExportReport;
    @FXML private Button btnLogout;
    @FXML private Button btnBroadcast;
    @FXML private Label userStatusLabel;
    @FXML private Label welcomeLabel;
    @FXML private ImageView adminAvatar;

    private Timeline refreshTimer;

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        if (welcomeLabel != null) {
            welcomeLabel.setText("Панель администратора");
        }

        // Очищаем устаревшие сессии при запуске
        SessionDAO.cleanupStaleSessions(5);

        setupTableColumns();
        setupContextMenu();
        refreshOnlineUsers();
        setupRefreshTimer();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("lastActivityFormatted"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("displayIp"));
        colSessionTime.setCellValueFactory(new PropertyValueFactory<>("currentSessionTime"));
        colDailyTime.setCellValueFactory(new PropertyValueFactory<>("dailyWorkTimeFormatted"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem sendNotificationItem = new MenuItem("Отправить уведомление");
        MenuItem disconnectItem = new MenuItem("Отключить пользователя");
        MenuItem blockItem = new MenuItem("Заблокировать учетную запись");
        MenuItem viewDetailsItem = new MenuItem("Просмотр деталей");

        sendNotificationItem.setOnAction(e -> sendNotification());
        disconnectItem.setOnAction(e -> disconnectUser());
        blockItem.setOnAction(e -> blockUser());
        viewDetailsItem.setOnAction(e -> showUserDetails());

        contextMenu.getItems().addAll(sendNotificationItem, disconnectItem, blockItem,
                new SeparatorMenuItem(), viewDetailsItem);

        onlineUsersTable.setContextMenu(contextMenu);

        onlineUsersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showUserDetails();
                }
            });
            return row;
        });
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> refreshOnlineUsers())
        );
        refreshTimer.setCycleCount(Animation.INDEFINITE);
        refreshTimer.play();
    }

    @FXML
    private void handleRefresh() {
        refreshOnlineUsers();
        showAlert("Информация", "Данные обновлены", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewHistory() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/login_history.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("История входа в систему");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть историю входа: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleManageUsers() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/manage_users.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Управление пользователями");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть управление пользователями: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExportReport() {
        try {
            exportToCSV();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при экспорте: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBroadcast() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Рассылка уведомления");
        dialog.setHeaderText("Отправить уведомление всем онлайн пользователям");
        dialog.setContentText("Введите текст уведомления:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            ChoiceDialog<String> typeDialog = new ChoiceDialog<>("INFO", "INFO", "WARNING");
            typeDialog.setTitle("Тип уведомления");
            typeDialog.setHeaderText("Выберите тип уведомления");
            typeDialog.setContentText("Тип:");

            Optional<String> typeResult = typeDialog.showAndWait();
            typeResult.ifPresent(type -> {
                boolean success = NotificationDAO.broadcastNotification(currentUserId, message, type);

                if (success) {
                    showAlert("Успешно",
                            "Уведомление отправлено всем онлайн пользователям",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Ошибка", "Не удалось отправить уведомления", Alert.AlertType.ERROR);
                }
            });
        });
    }

    @FXML
    public void handleLogout() {
        stopAllTimers();
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        logoutToLoginScreen();
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) onlineUsersTable.getScene().getWindow();
    }

    private void refreshOnlineUsers() {
        ObservableList<User> users = UserDAO.getOnlineUsersWithMonitoring();
        onlineUsersTable.setItems(users);
    }

    private User getSelectedUser() {
        return onlineUsersTable.getSelectionModel().getSelectedItem();
    }

    private void sendNotification() {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Отправка уведомления");
            dialog.setHeaderText("Уведомление для: " + selectedUser.getName());
            dialog.setContentText("Введите текст уведомления:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(message -> {
                boolean success = NotificationDAO.createNotification(
                        selectedUser.getId(),
                        currentUserId,
                        message,
                        "INFO"
                );

                if (success) {
                    showAlert("Успешно",
                            "Уведомление отправлено пользователю: " + selectedUser.getName(),
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Ошибка", "Не удалось отправить уведомление", Alert.AlertType.ERROR);
                }
            });
        } else {
            showAlert("Предупреждение", "Выберите пользователя из таблицы", Alert.AlertType.WARNING);
        }
    }

    private void disconnectUser() {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Отключение пользователя");
            reasonDialog.setHeaderText("Отключение пользователя " + selectedUser.getName());
            reasonDialog.setContentText("Укажите причину отключения:");

            Optional<String> reasonResult = reasonDialog.showAndWait();
            reasonResult.ifPresent(reason -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Подтверждение отключения");
                confirm.setHeaderText("Отключить пользователя " + selectedUser.getName() + "?");
                confirm.setContentText("Причина: " + reason);

                Optional<ButtonType> confirmResult = confirm.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    boolean success = SessionDAO.forceEndSession(
                            selectedUser.getId(),
                            currentUserId,
                            reason
                    );

                    if (success) {
                        refreshOnlineUsers();
                        showAlert("Успешно",
                                "Пользователь " + selectedUser.getName() + " отключен\nПричина: " + reason,
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Ошибка", "Не удалось отключить пользователя", Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Предупреждение", "Выберите пользователя для отключения", Alert.AlertType.WARNING);
        }
    }

    private void blockUser() {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            TextInputDialog reasonDialog = new TextInputDialog();
            reasonDialog.setTitle("Блокировка пользователя");
            reasonDialog.setHeaderText("Блокировка учетной записи " + selectedUser.getName());
            reasonDialog.setContentText("Укажите причину блокировки:");

            Optional<String> reasonResult = reasonDialog.showAndWait();
            reasonResult.ifPresent(reason -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Подтверждение блокировки");
                confirm.setHeaderText("Заблокировать учетную запись " + selectedUser.getName() + "?");
                confirm.setContentText("Причина: " + reason + "\n\nПользователь не сможет войти в систему.");

                Optional<ButtonType> confirmResult = confirm.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    boolean success = UserDAO.blockUser(selectedUser.getId(), reason);

                    if (success) {
                        SessionDAO.forceEndSession(selectedUser.getId(), currentUserId,
                                "Учетная запись заблокирована. Причина: " + reason);

                        NotificationDAO.createNotification(
                                selectedUser.getId(),
                                currentUserId,
                                "Ваша учетная запись заблокирована. Причина: " + reason,
                                "BLOCK"
                        );

                        refreshOnlineUsers();

                        showAlert("Успешно",
                                "Пользователь " + selectedUser.getName() + " заблокирован\nПричина: " + reason,
                                Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Ошибка", "Не удалось заблокировать пользователя", Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Предупреждение", "Выберите пользователя для блокировки", Alert.AlertType.WARNING);
        }
    }

    private void showUserDetails() {
        User selectedUser = getSelectedUser();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Детальная информация");
            alert.setHeaderText("Информация о пользователе: " + selectedUser.getName());

            String details = String.format(
                    "ID: %d\n" +
                            "ФИО: %s\n" +
                            "Логин: %s\n" +
                            "IP-адрес: %s\n" +
                            "Последняя активность: %s\n" +
                            "Текущая сессия: %s\n" +
                            "Время работы за день: %s\n" +
                            "Общее время работы: %s\n" +
                            "Статус: %s\n" +
                            "Заблокирован: %s",
                    selectedUser.getId(),
                    selectedUser.getName(),
                    selectedUser.getLogin(),
                    selectedUser.getDisplayIp(),
                    selectedUser.getLastActivityFormatted(),
                    selectedUser.getCurrentSessionTime(),
                    selectedUser.getDailyWorkTimeFormatted(),
                    selectedUser.getTotalWorkTimeFormatted(),
                    selectedUser.getStatus(),
                    selectedUser.getArchived() ? "Да" : "Нет"
            );

            alert.setContentText(details);
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(400, 300);
            alert.showAndWait();
        } else {
            showAlert("Предупреждение", "Выберите пользователя для просмотра деталей", Alert.AlertType.WARNING);
        }
    }

    private void exportToCSV() {
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID;Имя пользователя;Логин;IP-адрес;Текущая сессия;Время за день;Статус\n");

        for (User user : onlineUsersTable.getItems()) {
            csvContent.append(String.format("%d;%s;%s;%s;%s;%s;%s\n",
                    user.getId(),
                    user.getName(),
                    user.getLogin(),
                    user.getDisplayIp(),
                    user.getCurrentSessionTime(),
                    user.getDailyWorkTimeFormatted(),
                    user.getStatus()
            ));
        }

        TextArea textArea = new TextArea(csvContent.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(600, 400);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Экспорт данных");
        alert.setHeaderText("Содержимое отчета (можно скопировать в CSV файл)");
        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    protected void stopAllTimers() {
        super.stopAllTimers();
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}