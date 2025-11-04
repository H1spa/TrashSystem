package com.example.trash.controllers;

import javafx.fxml.FXML;

public class LabController extends BaseLabController {

    @FXML
    private void handleTakeWaste() {
        System.out.println("Приём отходов...");
    }

    @FXML
    private void handleReport() {
        System.out.println("Формирование отчёта...");
    }
}
