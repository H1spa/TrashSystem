package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AccountantController {

    @FXML
    private Button logoutButton;

    @FXML
    private void handleViewReports() {
        System.out.println("Просмотр отчётов...");
        // Логика просмотра отчётов
    }

    @FXML
    private void handleCreateInvoice() {
        System.out.println("Формирование счёта предприятию...");
        // Логика формирования счёта
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/login.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Авторизация");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

