package com.example.trash.controllers;

import com.example.trash.dao.LoginHistoryDAO;
import com.example.trash.dao.UserDAO;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class AdminController {

    @FXML
    private TableView<User> onlineUsersTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colUser;

    @FXML
    private TableColumn<User, String> colTime;

    @FXML
    private TableColumn<User, String> colIp;

    @FXML
    private TableColumn<User, String> colSessionTime;

    @FXML
    private TableColumn<User, String> colDailyTime;

    @FXML
    private Button btnViewHistory;

    private Timeline refreshTimer;

    @FXML
    public void initialize() {
        // Настройка колонок таблицы
        setupTableColumns();

        // Настройка контекстного меню
        setupContextMenu();

        // Загрузка данных
        refreshOnlineUsers();

        // Настройка таймера автообновления
        setupRefreshTimer();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("lastActivityFormatted"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("displayIp"));
        colSessionTime.setCellValueFactory(new PropertyValueFactory<>("currentSessionTime"));
        colDailyTime.setCellValueFactory(new PropertyValueFactory<>("dailyWorkTimeFormatted"));
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem sendNotificationItem = new MenuItem("Отправить уведомление");
        MenuItem disconnectItem = new MenuItem("Отключить пользователя");
        MenuItem blockItem = new MenuItem("Заблокировать учетную запись");

        sendNotificationItem.setOnAction(e -> sendNotification());
        disconnectItem.setOnAction(e -> disconnectUser());
        blockItem.setOnAction(e -> blockUser());

        contextMenu.getItems().addAll(sendNotificationItem, disconnectItem, blockItem);

        onlineUsersTable.setContextMenu(contextMenu);

        // Двойной клик по строке
        onlineUsersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    User user = row.getItem();
                    showUserDetails(user);
                }
            });
            return row;
        });
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> refreshOnlineUsers())
        );
        refreshTimer.setCycleCount(Animation.INDEFINITE);
        refreshTimer.play();
    }

    @FXML
    private void handleRefresh() {
        refreshOnlineUsers();
    }

    private void refreshOnlineUsers() {
        ObservableList<User> users = UserDAO.getOnlineUsersWithMonitoring();
        onlineUsersTable.setItems(users);
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
            showAlert("Ошибка", "Не удалось открыть историю входа", Alert.AlertType.ERROR);
        }
    }

    private void sendNotification() {
        User selectedUser = onlineUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Отправка уведомления");
            dialog.setHeaderText("Уведомление для: " + selectedUser.getName());
            dialog.setContentText("Введите текст уведомления:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(message -> {
                // Здесь можно реализовать отправку уведомления
                // Например, через WebSocket или сохранение в БД
                showAlert("Уведомление отправлено",
                        "Пользователю " + selectedUser.getName() + " отправлено уведомление:\n" + message,
                        Alert.AlertType.INFORMATION);
            });
        } else {
            showAlert("Предупреждение", "Выберите пользователя", Alert.AlertType.WARNING);
        }
    }

    private void disconnectUser() {
        User selectedUser = onlineUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Отключение пользователя");
            confirm.setHeaderText("Отключить пользователя " + selectedUser.getName() + "?");
            confirm.setContentText("Пользователь будет отключен от системы.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = UserDAO.disconnectUser(selectedUser.getId());
                if (success) {
                    refreshOnlineUsers();
                    showAlert("Успешно", "Пользователь отключен", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Ошибка", "Не удалось отключить пользователя", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Предупреждение", "Выберите пользователя", Alert.AlertType.WARNING);
        }
    }

    private void blockUser() {
        User selectedUser = onlineUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Блокировка пользователя");
            confirm.setHeaderText("Заблокировать учетную запись " + selectedUser.getName() + "?");
            confirm.setContentText("Пользователь не сможет войти в систему.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = UserDAO.blockUser(selectedUser.getId());
                if (success) {
                    refreshOnlineUsers();
                    showAlert("Успешно", "Пользователь заблокирован", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Ошибка", "Не удалось заблокировать пользователя", Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Предупреждение", "Выберите пользователя", Alert.AlertType.WARNING);
        }
    }

    private void showUserDetails(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о пользователе");
        alert.setHeaderText("Детальная информация");
        alert.setContentText(
                "ID: " + user.getId() + "\n" +
                        "ФИО: " + user.getName() + "\n" +
                        "Логин: " + user.getLogin() + "\n" +
                        "IP-адрес: " + user.getDisplayIp() + "\n" +
                        "Текущая сессия: " + user.getCurrentSessionTime() + "\n" +
                        "Время работы за день: " + user.getDailyWorkTimeFormatted()
        );
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        try {
            Stage stage = (Stage) onlineUsersTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/login.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Авторизация");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageUsers() {
        try {
            Stage stage = (Stage) onlineUsersTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/manage_users.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Управление пользователями");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Остановка таймера при закрытии окна
    public void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
}