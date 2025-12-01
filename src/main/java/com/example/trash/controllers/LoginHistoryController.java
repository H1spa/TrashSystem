package com.example.trash.controllers;

import com.example.trash.dao.LoginHistoryDAO;
import com.example.trash.model.LoginHistory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Экспорт данных");
        alert.setHeaderText("Экспорт истории входа");
        alert.setContentText("Функция экспорта будет реализована в следующем обновлении");
        alert.showAndWait();
    }
}