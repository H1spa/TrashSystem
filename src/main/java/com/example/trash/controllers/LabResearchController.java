package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class LabResearchController extends BaseLabController {

    @FXML
    private TableView<?> experimentsTable; // Можно заменить на конкретный тип эксперимента

    @FXML
    private void handleAdd() {
        System.out.println("Добавление эксперимента...");
        // Здесь логика добавления
    }

    @FXML
    private void handleEdit() {
        System.out.println("Редактирование эксперимента...");
        // Здесь логика редактирования
    }

    @FXML
    private void handleDelete() {
        System.out.println("Удаление эксперимента...");
        // Здесь логика удаления
    }

    @FXML
    private void handleExport() {
        System.out.println("Экспорт в Excel...");
        // Здесь логика экспорта
    }

    @FXML
    public void handleLogout() {
        logoutForced(); // метод из BaseLabController
    }
}
