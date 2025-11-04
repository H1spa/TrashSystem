package com.example.trash.controllers;

import com.example.trash.dao.UserDAO;
import com.example.trash.model.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AdminController {

    @FXML private TableView<User> onlineUsersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUser;
    @FXML private TableColumn<User, String> colTime;

    private Timeline refreshTimer;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("lastActivityFormatted"));

        refreshOnlineUsers();

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> refreshOnlineUsers()));
        refreshTimer.setCycleCount(Animation.INDEFINITE);
        refreshTimer.play();
    }

    @FXML
    private void handleRefresh() {
        refreshOnlineUsers();
    }

    private void refreshOnlineUsers() {
        ObservableList<User> users = UserDAO.getOnlineUsers();
        onlineUsersTable.setItems(users);
    }

    @FXML
    private void handleLogout() {
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
}
