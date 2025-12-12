package com.example.trash.controllers;

import com.example.trash.dao.LoginHistoryDAO;
import com.example.trash.model.LoginHistory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class LoginHistoryController {

    @FXML
    private TableView<LoginHistory> historyTable;

    @FXML
    private TableColumn<LoginHistory, String> colUsername;

    @FXML
    private TableColumn<LoginHistory, String> colIpAddress;

    @FXML
    private TableColumn<LoginHistory, String> colLoginTime;

    @FXML
    private TableColumn<LoginHistory, String> colStatus;

    @FXML
    private TextField filterField;

    @FXML
    private Button filterButton;

    @FXML
    private Button clearFilterButton;

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSortComboBox();
        loadHistory();
    }

    private void setupTableColumns() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colIpAddress.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colLoginTime.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupSortComboBox() {
        sortComboBox.getItems().addAll("Сначала новые", "Сначала старые");
        sortComboBox.setValue("Сначала новые");
        sortComboBox.setOnAction(e -> applySorting());
    }

    private void loadHistory() {
        String filter = filterField.getText();
        ObservableList<LoginHistory> history = LoginHistoryDAO.getLoginHistory(filter);
        historyTable.setItems(history);
        applySorting();
    }

    private void applySorting() {
        if (sortComboBox.getValue().equals("Сначала старые")) {
            historyTable.getSortOrder().clear();
            colLoginTime.setSortType(TableColumn.SortType.ASCENDING);
            historyTable.getSortOrder().add(colLoginTime);
        } else {
            historyTable.getSortOrder().clear();
            colLoginTime.setSortType(TableColumn.SortType.DESCENDING);
            historyTable.getSortOrder().add(colLoginTime);
        }
        historyTable.sort();
    }

    @FXML
    private void handleFilter() {
        loadHistory();
    }

    @FXML
    private void handleClearFilter() {
        filterField.clear();
        loadHistory();
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) historyTable.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExport() {
        try {
            // Создаем FileChooser для выбора пути сохранения
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить историю входа как CSV");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            // Устанавливаем имя файла по умолчанию
            String defaultFileName = "история_входа_" +
                    java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            // Показываем диалог сохранения
            Stage stage = (Stage) historyTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Формируем CSV содержимое
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Логин пользователя;IP-адрес;Время попытки входа;Статус\n");

                for (LoginHistory history : historyTable.getItems()) {
                    csvContent.append(String.format("%s;%s;%s;%s\n",
                            history.getUsername() != null ? history.getUsername() : "",
                            history.getIpAddress(),
                            history.getFormattedTime(),
                            history.getStatus()
                    ));
                }

                // Сохраняем в файл
                try (java.io.PrintWriter writer = new java.io.PrintWriter(
                        new java.io.FileWriter(file.getAbsolutePath(), java.nio.charset.StandardCharsets.UTF_8))) {
                    writer.write(csvContent.toString());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Экспорт данных");
                alert.setHeaderText("История входа экспортирована");
                alert.setContentText("Файл сохранен: " + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка при экспорте");
            alert.setContentText("Не удалось экспортировать данные: " + e.getMessage());
            alert.showAndWait();
        }
    }
}