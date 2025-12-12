package com.example.trash.controllers;

import com.example.trash.dao.ClientDAO;
import com.example.trash.model.Client;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class AddClientController {

    @FXML
    private TextField fioField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private TextField passportSeriesField;
    @FXML
    private TextField passportNumberField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> clientTypeComboBox;
    @FXML
    private ComboBox<String> companyComboBox;
    @FXML
    private TextField newCompanyField;
    @FXML
    private TextField companyAddressField;
    @FXML
    private TextField companyINNField;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private Stage dialogStage;
    private Client client;
    private boolean okClicked = false;
    private ObservableList<String> companies = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Заполнение типов клиентов
        clientTypeComboBox.getItems().addAll("Физическое лицо", "Юридическое лицо");
        clientTypeComboBox.setValue("Физическое лицо");

        // Загрузка компаний из базы данных
        loadCompanies();

        // Обработчик изменения типа клиента
        clientTypeComboBox.setOnAction(event -> {
            boolean isLegalEntity = "Юридическое лицо".equals(clientTypeComboBox.getValue());
            companyComboBox.setDisable(!isLegalEntity);
            newCompanyField.setDisable(!isLegalEntity);
            companyAddressField.setDisable(!isLegalEntity);
            companyINNField.setDisable(!isLegalEntity);
        });

        // Изначально скрываем поля для юрлиц
        companyComboBox.setDisable(true);
        newCompanyField.setDisable(true);
        companyAddressField.setDisable(true);
        companyINNField.setDisable(true);

        // Ограничение длины полей
        passportSeriesField.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 4 ? change : null));
        passportNumberField.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 6 ? change : null));

        // Валидация email
        emailField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return change;
            }
            return null;
        }));
    }

    private void loadCompanies() {
        companies.clear();
        companies.addAll(ClientDAO.getAllCompanies());
        companyComboBox.setItems(companies);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setClient(Client client) {
        this.client = client;

        if (client != null) {
            fioField.setText(client.getFio());

            if (client.getBirthDate() != null) {
                birthDatePicker.setValue(client.getBirthDate());
            }

            passportSeriesField.setText(client.getPassportSeries());
            passportNumberField.setText(client.getPassportNumber());
            phoneField.setText(client.getPhone());
            emailField.setText(client.getEmail());

            if (client.getTypeClientId() == 2) { // Юрлицо
                clientTypeComboBox.setValue("Юридическое лицо");
                if (client.getCompanyName() != null) {
                    companyComboBox.setValue(client.getCompanyName());
                }
            }
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        System.out.println("=== DEBUG: Начало handleOk() ===");

        if (isInputValid()) {
            System.out.println("Валидация пройдена");

            client = new Client();
            client.setFio(fioField.getText());
            System.out.println("ФИО: " + fioField.getText());

            client.setBirthDate(birthDatePicker.getValue());
            System.out.println("Дата рождения: " + birthDatePicker.getValue());

            client.setPassportSeries(passportSeriesField.getText());
            client.setPassportNumber(passportNumberField.getText());
            client.setPhone(phoneField.getText());
            client.setEmail(emailField.getText());

            // Определение типа клиента
            if ("Юридическое лицо".equals(clientTypeComboBox.getValue())) {
                System.out.println("Тип: Юридическое лицо");
                client.setTypeClientId(2);

                // Получение или создание компании
                int companyId = -1;
                if (!newCompanyField.getText().isEmpty()) {
                    System.out.println("Создание новой компании: " + newCompanyField.getText());
                    // Создание новой компании
                    companyId = ClientDAO.createCompany(
                            newCompanyField.getText(),
                            companyAddressField.getText(),
                            companyINNField.getText()
                    );
                    System.out.println("ID новой компании: " + companyId);
                } else if (companyComboBox.getValue() != null) {
                    System.out.println("Использование существующей компании: " + companyComboBox.getValue());
                    // Использование существующей компании
                    companyId = ClientDAO.getCompanyIdByName(companyComboBox.getValue());
                    System.out.println("ID существующей компании: " + companyId);
                }

                if (companyId > 0) {
                    client.setCompanyId(companyId);
                    System.out.println("ID компании установлен: " + companyId);
                } else {
                    System.out.println("ОШИБКА: Не удалось получить ID компании");
                }
            } else {
                System.out.println("Тип: Физическое лицо");
                client.setTypeClientId(1);
            }

            // Сохранение клиента
            System.out.println("Вызов ClientDAO.addClient()...");
            boolean success = ClientDAO.addClient(client);
            System.out.println("Результат сохранения: " + success);

            if (success) {
                okClicked = true;
                System.out.println("Клиент успешно сохранен");
                dialogStage.close();
            } else {
                System.out.println("ОШИБКА: Не удалось сохранить клиента");
                showAlert("Ошибка", "Не удалось сохранить клиента. Проверьте логи.");
            }
        } else {
            System.out.println("Валидация не пройдена");
        }

        System.out.println("=== DEBUG: Конец handleOk() ===");
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (fioField.getText() == null || fioField.getText().length() < 5) {
            errorMessage += "ФИО должно содержать минимум 5 символов!\n";
        }

        if (passportSeriesField.getText() == null || passportSeriesField.getText().length() != 4) {
            errorMessage += "Серия паспорта должна содержать 4 цифры!\n";
        }

        if (passportNumberField.getText() == null || passportNumberField.getText().length() != 6) {
            errorMessage += "Номер паспорта должен содержать 6 цифр!\n";
        }

        if (phoneField.getText() == null || phoneField.getText().length() < 10) {
            errorMessage += "Телефон должен содержать минимум 10 цифр!\n";
        }

        // Проверка для юрлиц
        if ("Юридическое лицо".equals(clientTypeComboBox.getValue())) {
            if (newCompanyField.getText().isEmpty() &&
                    (companyComboBox.getValue() == null || companyComboBox.getValue().isEmpty())) {
                errorMessage += "Выберите существующую компанию или введите новую!\n";
            }

            if (!newCompanyField.getText().isEmpty() &&
                    (companyINNField.getText() == null || companyINNField.getText().length() != 12)) {
                errorMessage += "ИНН должен содержать 12 цифр!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert("Ошибка валидации", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText("Пожалуйста, исправьте ошибки");
        alert.setContentText(content);
        alert.showAndWait();
    }
}