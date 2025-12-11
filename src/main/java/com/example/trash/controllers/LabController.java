package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LabController extends BaseLabController {

    @FXML
    private Button logoutButton;
    @FXML
    private Button takeWasteButton;
    @FXML
    private Button reportButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        welcomeLabel.setText("Добро пожаловать, Лаборант!");
    }

    @FXML
    private void handleTakeWaste() {
        // Переход в панель приема отходов
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/waste_acceptance.fxml"));
            Scene scene = new Scene(loader.load());
            currentStage.setScene(scene);
            currentStage.setTitle("Прием отходов");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleReport() {
        // TODO: Реализовать формирование отчета
        System.out.println("Формирование отчёта...");
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
}