package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class AccountantController extends BaseController {

    @FXML
    private Button logoutButton;
    @FXML
    private Button viewReportsButton;
    @FXML
    private Button createInvoiceButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        welcomeLabel.setText("Добро пожаловать, Бухгалтер!");
    }

    @FXML
    private void handleViewReports() {
        // TODO: Реализовать просмотр отчетов
        System.out.println("Просмотр отчётов...");
    }

    @FXML
    private void handleCreateInvoice() {
        // TODO: Реализовать формирование счета
        System.out.println("Формирование счёта предприятию...");
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
}