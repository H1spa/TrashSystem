package com.example.trash.controllers;

import com.example.trash.dao.*;
import com.example.trash.model.*;
import com.example.trash.util.Base64Generator;
import com.example.trash.util.PDFGenerator;
import com.example.trash.util.TextQRParser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class WasteAcceptanceController extends BaseLabController {

    @FXML
    private TextField caseCodeField;
    @FXML
    private TextField clientSearchField;
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, String> colClientName;
    @FXML
    private TableColumn<Client, String> colClientPhone;
    @FXML
    private TextField serviceSearchField;
    @FXML
    private TableView<Service> serviceTable;
    @FXML
    private TableColumn<Service, String> colServiceName;
    @FXML
    private TableColumn<Service, Double> colServiceCost;
    @FXML
    private TableView<Service> selectedServicesTable;
    @FXML
    private Button scanFromFileButton;
    @FXML
    private Button addServiceButton;
    @FXML
    private Button removeServiceButton;
    @FXML
    private Button createOrderButton;
    @FXML
    private Button scanQRButton;
    @FXML
    private Label orderNumberLabel;
    @FXML
    private Label totalCostLabel;
    @FXML
    private Button logoutButton;

    private ObservableList<Client> clients = FXCollections.observableArrayList();
    private ObservableList<Service> services = FXCollections.observableArrayList();
    private ObservableList<Service> selectedServices = FXCollections.observableArrayList();
    private Client selectedClient;
    private String qrCodeData;

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        // Установка подсказки для номера кейса (последний заказ + 1)
        int lastOrderNumber = OrderDAO.getLastOrderNumber();
        caseCodeField.setPromptText("Предлагаемый код: " + (lastOrderNumber + 1));

        // Настройка таблицы клиентов
        colClientName.setCellValueFactory(cellData -> cellData.getValue().fioProperty());
        colClientPhone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        clientTable.setItems(clients);

        // Настройка таблицы услуг
        colServiceName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colServiceCost.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
        serviceTable.setItems(services);

        // Настройка таблицы выбранных услуг
        TableColumn<Service, String> selectedNameCol = new TableColumn<>("Наименование услуги");
        selectedNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        selectedNameCol.setPrefWidth(250);

        TableColumn<Service, Double> selectedCostCol = new TableColumn<>("Стоимость");
        selectedCostCol.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
        selectedCostCol.setPrefWidth(150);

        selectedServicesTable.getColumns().clear();
        selectedServicesTable.getColumns().addAll(selectedNameCol, selectedCostCol);
        selectedServicesTable.setItems(selectedServices);

        // Настройка поиска клиентов (с 5 символов)
        clientSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 5) {
                searchClients(newValue);
            } else {
                clients.clear();
            }
        });

        // Настройка поиска услуг (с 5 символов)
        serviceSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 5) {
                searchServices(newValue);
            } else {
                services.clear();
            }
        });

        // Обработка выбора клиента
        clientTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedClient = newValue;
                });

        // Обновление общей стоимости при изменении выбранных услуг
        selectedServices.addListener((javafx.collections.ListChangeListener.Change<? extends Service> change) -> {
            updateTotalCost();
        });

        // Обновление номера заказа
        orderNumberLabel.setText("Следующий номер заказа: " + (lastOrderNumber + 1));

        // Обработка Enter в поле кода кейса
        caseCodeField.setOnAction(event -> {
            handleCreateOrder();
        });
        scanFromFileButton.setOnAction(e -> handleScanFromFile());
    }

    private void searchClients(String query) {
        // Нечеткий поиск клиентов с 5 символов
        List<Client> foundClients = ClientDAO.fuzzySearchClients(query);
        Platform.runLater(() -> {
            clients.setAll(foundClients);
            if (!foundClients.isEmpty()) {
                clientTable.getSelectionModel().selectFirst();
            }
        });
    }

    private void searchServices(String query) {
        // Нечеткий поиск услуг с 5 символов
        List<Service> foundServices = ServiceDAO.fuzzySearchServices(query);
        Platform.runLater(() -> {
            services.setAll(foundServices);
        });
    }
    @FXML
    private void handleAddClient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/add_client.fxml"));
            AnchorPane root = loader.load(); // <-- ПРАВИЛЬНО: AnchorPane, а не DialogPane

            AddClientController controller = loader.getController();

            // Создаем обычное окно вместо диалога
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Добавление клиента");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(caseCodeField.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            controller.setDialogStage(dialogStage);

            // Показываем и ждем закрытия
            dialogStage.showAndWait();

            // После закрытия окна проверяем результат
            if (controller.isOkClicked()) {
                // Обновить список клиентов
                String currentSearch = clientSearchField.getText();
                if (currentSearch.length() >= 5) {
                    searchClients(currentSearch);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно добавления клиента: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddService() {
        Service selectedService = serviceTable.getSelectionModel().getSelectedItem();
        if (selectedService != null && !selectedServices.contains(selectedService)) {
            selectedServices.add(selectedService);
            updateTotalCost();
        } else if (selectedService == null) {
            showAlert("Внимание", "Выберите услугу из списка");
        }
    }

    @FXML
    private void handleRemoveService() {
        Service selectedService = selectedServicesTable.getSelectionModel().getSelectedItem();
        if (selectedService != null) {
            selectedServices.remove(selectedService);
            updateTotalCost();
        }
    }

    @FXML
    private void handleScanQR() {
        try {
            // Здесь будет интеграция с мобильным приложением через API
            // Для демонстрации используем тестовые данные

            // Открываем диалог для ввода данных QR кода
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Сканирование QR-кода");
            dialog.setHeaderText("Введите данные из QR-кода");
            dialog.setContentText("JSON данные:");

            dialog.showAndWait().ifPresent(jsonData -> {
                try {
                    processQRData(jsonData);
                } catch (Exception e) {
                    showAlert("Ошибка", "Неверный формат QR-кода: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при обработке QR-кода: " + e.getMessage());
        }
    }

    private void processQRData(String qrData) {
        System.out.println("\n=== НАЧАЛО ОБРАБОТКИ QR ДАННЫХ ===");

        try {
            // 1. Пытаемся распарсить как JSON (для совместимости со старым форматом)
            boolean isJsonParsed = false;

            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                QRData jsonData = gson.fromJson(qrData, QRData.class);

                if (jsonData != null && jsonData.getClientData() != null &&
                        jsonData.getClientData().getFio() != null) {
                    System.out.println("Данные распознаны как JSON формат");
                    fillFormFromQRData(jsonData);
                    isJsonParsed = true;
                }
            } catch (Exception e) {
                System.out.println("Не JSON формат: " + e.getMessage());
            }

            // 2. Если JSON не распарсился, используем текстовый парсер
            if (!isJsonParsed) {
                System.out.println("Используем текстовый парсер...");
                QRData parsedData = TextQRParser.parseTextFormat(qrData);
                fillFormFromQRData(parsedData);
            }

            showAlert("Данные загружены", "Данные из QR-кода успешно загружены в форму");

        } catch (Exception e) {
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА при обработке QR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось обработать QR-код. Проверьте формат данных.");
        }

        System.out.println("=== ЗАВЕРШЕНИЕ ОБРАБОТКИ QR ДАННЫХ ===\n");
    }

    /**
     * Заполнение полей формы из распарсенных данных
     */
    private void fillFormFromQRData(QRData qrData) {
        Platform.runLater(() -> {
            // 1. Код кейса
            if (qrData.getCaseCode() != null && !qrData.getCaseCode().isEmpty()) {
                caseCodeField.setText(qrData.getCaseCode());
                System.out.println("Установлен код кейса: " + qrData.getCaseCode());
            } else {
                // Автопредложение кода
                int lastOrderNumber = OrderDAO.getLastOrderNumber();
                caseCodeField.setText(String.valueOf(lastOrderNumber + 1));
                System.out.println("Код кейса не указан, предложен: " + (lastOrderNumber + 1));
            }

            // 2. Клиент
            if (qrData.getClientData() != null) {
                QRData.ClientData client = qrData.getClientData();

                // Поиск клиента в базе
                if (client.getFio() != null && !client.getFio().isEmpty()) {
                    clientSearchField.setText(client.getFio());
                    System.out.println("Поиск клиента: " + client.getFio());
                    searchClients(client.getFio());
                }

                // 3. Услуги
                if (qrData.getServices() != null && !qrData.getServices().isEmpty()) {
                    System.out.println("Добавление услуг: " + qrData.getServices());

                    for (String serviceName : qrData.getServices()) {
                        // Ищем услугу по коду или названию
                        Service service = ServiceDAO.getServiceByCode(serviceName);
                        if (service == null) {
                            service = ServiceDAO.getServiceByName(serviceName);
                        }

                        if (service != null && !selectedServices.contains(service)) {
                            selectedServices.add(service);
                            System.out.println("  Добавлена услуга: " + service.getName());
                        } else if (service == null) {
                            System.out.println("  Услуга не найдена: " + serviceName);
                        }
                    }
                    updateTotalCost();
                }
            }
        });
    }

    @FXML
    private void handleCreateOrder() {
        // Проверка данных
        if (selectedClient == null) {
            showAlert("Ошибка", "Выберите клиента");
            return;
        }

        if (selectedServices.isEmpty()) {
            showAlert("Ошибка", "Добавьте хотя бы одну услугу");
            return;
        }

        String caseCode = caseCodeField.getText();
        if (caseCode.isEmpty()) {
            // Используем предложенный код
            int lastOrderNumber = OrderDAO.getLastOrderNumber();
            caseCode = String.valueOf(lastOrderNumber + 1);
            caseCodeField.setText(caseCode);
        }

        // Проверка уникальности кода кейса
        if (!OrderDAO.isCaseCodeUnique(caseCode)) {
            showAlert("Ошибка", "Код кейса уже существует. Введите другой код.");
            caseCodeField.requestFocus();
            return;
        }

        try {
            // Создание заказа
            List<Integer> serviceIds = selectedServices.stream()
                    .map(Service::getId)
                    .collect(Collectors.toList());

            Order order = new Order();
            order.setCaseCode(caseCode);
            order.setClientId(selectedClient.getId());
            order.setStatus("Создан");
            order.setServices(serviceIds);
            order.setTotalCost(calculateTotal());

            // Сохранение заказа в базу данных
            int orderId = OrderDAO.createOrder(order);

            if (orderId > 0) {
                order.setId(orderId);

                // Генерация PDF
                generateOrderPDF(order);

                // Генерация Base64 ссылки
                generateBase64Link(order);

                showSuccessAlert("Заказ успешно создан",
                        "Номер заказа: " + order.getOrderNumber() + "\n" +
                                "Код кейса: " + order.getCaseCode() + "\n" +
                                "Стоимость: " + String.format("%.2f", calculateTotal()) + " руб.");

                // Очистка формы
                clearForm();
            } else {
                showAlert("Ошибка", "Не удалось создать заказ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при создании заказа: " + e.getMessage());
        }
    }

    private void updateTotalCost() {
        double total = calculateTotal();
        totalCostLabel.setText(String.format("Итого: %.2f руб.", total));
    }

    private double calculateTotal() {
        return selectedServices.stream()
                .mapToDouble(Service::getCost)
                .sum();
    }

    private void generateOrderPDF(Order order) {
        try {
            // Получаем полные данные
            Client client = ClientDAO.getClientById(order.getClientId());
            List<Service> orderServices = ServiceDAO.getServicesByIds(order.getServices());

            // Генерация PDF с выбором пути
            File pdfFile = PDFGenerator.generateOrderPDF(order, client, orderServices, getCurrentStage());

            if (pdfFile != null) {
                showAlert("PDF создан", "Документ заказа сохранен в файл:\n" + pdfFile.getAbsolutePath());
            } else {
                showAlert("Информация", "Сохранение PDF отменено");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать PDF: " + e.getMessage());
        }
    }

    private void generateBase64Link(Order order) {
        try {
            // Получаем полные данные
            Client client = ClientDAO.getClientById(order.getClientId());
            List<Service> orderServices = ServiceDAO.getServicesByIds(order.getServices());

            // Генерация Base64 с выбором пути
            File base64File = Base64Generator.generateOrderLinkWithSaveDialog(order, client, orderServices, getCurrentStage());

            if (base64File != null) {
                showAlert("Ссылка создана", "Base64 ссылка сохранена в файл:\n" + base64File.getAbsolutePath());
            } else {
                showAlert("Информация", "Сохранение Base64 ссылки отменено");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать ссылку: " + e.getMessage());
        }
    }

    private void clearForm() {
        caseCodeField.clear();
        clientSearchField.clear();
        serviceSearchField.clear();
        clients.clear();
        services.clear();
        selectedServices.clear();
        selectedClient = null;

        // Обновление подсказки и номера заказа
        int lastOrderNumber = OrderDAO.getLastOrderNumber();
        caseCodeField.setPromptText("Предлагаемый код: " + (lastOrderNumber + 1));
        orderNumberLabel.setText("Следующий номер заказа: " + (lastOrderNumber + 1));
        totalCostLabel.setText("Итого: 0.00 руб.");
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void showSuccessAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText("Успешно!");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @FXML
    public void handleLogout() {
        logoutToLoginScreen();
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
    @FXML
    private void handleScanFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл QR-кода");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения QR", "*.jpg", "*.jpeg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(getCurrentStage());
        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName().toLowerCase();

                // Пока просто тестовое заполнение по имени файла
                // В реальной системе здесь было бы декодирование QR
                if (fileName.contains("qr1")) {
                    simulateQRScan("Jann Dark", new String[]{"323", "501"});
                } else if (fileName.contains("qr2")) {
                    simulateQRScan("Glund Dunkerly", new String[]{"1619", "996"});
                } else if (fileName.contains("qr3")) {
                    simulateQRScan("Glun Dunkerly", new String[]{"619", "836"});
                } else {
                    showAlert("Информация", "Формат файла не распознан. Используйте тестовые файлы: qr1.jpg, qr2.jpeg, qr3.jpg");
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось обработать файл: " + e.getMessage());
            }
        }
    }
    private void simulateQRScan(String clientName, String[] serviceCodes) {
        // 1. Поиск клиента
        clientSearchField.setText(clientName);
        searchClients(clientName);

        // 2. Добавление услуг
        Platform.runLater(() -> {
            for (String code : serviceCodes) {
                Service service = ServiceDAO.getServiceByCode(code);
                if (service != null && !selectedServices.contains(service)) {
                    selectedServices.add(service);
                }
            }
            updateTotalCost();
        });

        // 3. Установка кода кейса
        int lastOrderNumber = OrderDAO.getLastOrderNumber();
        caseCodeField.setText(String.valueOf(lastOrderNumber + 1));

        showAlert("Успех", "Данные из QR-кода загружены:\nКлиент: " + clientName);
    }
}