package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LabResearchController extends BaseLabController {

    @FXML
    private TableView<?> experimentsTable;
    @FXML
    private Button logoutButton;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button exportButton;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView userAvatar;

    @FXML
    @Override
    public void initialize() {
        super.initialize();
        welcomeLabel.setText("Добро пожаловать, Лаборант-исследователь!");

    }

    @FXML
    private void handleAdd() {
        // TODO: Реализовать добавление эксперимента
        System.out.println("Добавление эксперимента...");
    }

    @FXML
    private void handleEdit() {
        // TODO: Реализовать редактирование эксперимента
        System.out.println("Редактирование эксперимента...");
    }

    @FXML
    private void handleDelete() {
        // TODO: Реализовать удаление эксперимента
        System.out.println("Удаление эксперимента...");
    }

    @FXML
    private void handleExport() {
        // TODO: Реализовать экспорт в Excel
        System.out.println("Экспорт в Excel...");
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
}