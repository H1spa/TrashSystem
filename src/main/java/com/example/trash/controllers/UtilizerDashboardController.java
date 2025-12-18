package com.example.trash.controllers;

import com.example.trash.dao.UtilizerDAO;
import com.example.trash.dao.UtilizerProcessDAO;
import com.example.trash.dao.ServiceDAO;
import com.example.trash.dao.OrderDAO;
import com.example.trash.model.Utilizer;
import com.example.trash.model.UtilizerProcess;
import com.example.trash.model.Service;
import com.example.trash.model.Order;
import com.example.trash.service.UtilizerServiceClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UtilizerDashboardController extends BaseLabController {

    @FXML private GridPane utilizersGrid;
    @FXML private Label totalUtilizersLabel;
    @FXML private Label availableUtilizersLabel;
    @FXML private Label busyUtilizersLabel;
    @FXML private Label fullUtilizersLabel;
    @FXML private TableView<UtilizerProcess> activeProcessesTable;
    @FXML private TableColumn<UtilizerProcess, String> processIdColumn;
    @FXML private TableColumn<UtilizerProcess, String> serviceColumn;
    @FXML private TableColumn<UtilizerProcess, String> utilizerColumn;
    @FXML private TableColumn<UtilizerProcess, Integer> progressColumn;
    @FXML private TableColumn<UtilizerProcess, String> statusColumn;
    @FXML private TableColumn<UtilizerProcess, String> startTimeColumn;
    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Label timestampLabel;
    @FXML private Label activeProcessesCount;
    @FXML private Button refreshButton;
    @FXML private Button releaseButton;
    @FXML private Button historyButton;
    @FXML private Button reportsButton;
    @FXML private Button logoutButton;
    @FXML private Label emulatorStatusLabel;

    private Timeline statusUpdateTimer;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private ObservableList<UtilizerProcess> processList = javafx.collections.FXCollections.observableArrayList();
    private List<Utilizer> allUtilizers = new ArrayList<>();
    private Map<String, UtilizerCard> utilizerCards = new HashMap<>();

    @Override
    protected Stage getCurrentStage() {
        if (utilizersGrid != null && utilizersGrid.getScene() != null) {
            return (Stage) utilizersGrid.getScene().getWindow();
        }
        return null;
    }

    @Override
    public void initialize() {
        super.initialize();

        if (currentUser != null) {
            userLabel.setText("Пользователь: " + currentUser.getName());
            String roleName = "Неизвестно";
            String userType = currentUser.getType();

            if (userType != null) {
                try {
                    int typeId = Integer.parseInt(userType);
                    switch (typeId) {
                        case 2: roleName = "Лаборант"; break;
                        case 1: roleName = "Администратор"; break;
                        case 3: roleName = "Исследователь"; break;
                        case 4: roleName = "Бухгалтер"; break;
                        default: roleName = "Неизвестно";
                    }
                } catch (NumberFormatException e) {
                    roleName = userType;
                }
            }
            roleLabel.setText("Роль: " + roleName);
        }

        setupTableColumns();
        loadUtilizers();
        loadActiveProcesses();
        updateTimestamp();
        setupStatusUpdateTimer();

        checkEmulatorHealth();
    }

    private void setupTableColumns() {
        processIdColumn.setCellValueFactory(cellData -> {
            String uuid = cellData.getValue().getProcessUuid();
            return new javafx.beans.property.SimpleStringProperty(
                    uuid != null && uuid.length() > 8 ?
                            uuid.substring(0, 8) + "..." : "N/A");
        });

        serviceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getServiceName()));

        utilizerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getUtilizerName()));

        progressColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(
                        cellData.getValue().getProgress()).asObject());

        statusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStatus()));

        startTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStartTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartTime().toString().substring(0, 16));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        progressColumn.setCellFactory(column -> new TableCell<UtilizerProcess, Integer>() {
            private final ProgressBar progressBar = new ProgressBar();
            private final Label label = new Label();

            @Override
            protected void updateItem(Integer progress, boolean empty) {
                super.updateItem(progress, empty);

                if (empty || progress == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress / 100.0);
                    label.setText(progress + "%");

                    if (progress < 30) {
                        progressBar.setStyle("-fx-accent: red;");
                    } else if (progress < 70) {
                        progressBar.setStyle("-fx-accent: orange;");
                    } else {
                        progressBar.setStyle("-fx-accent: green;");
                    }

                    HBox container = new HBox(10, progressBar, label);
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setGraphic(container);
                }
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<UtilizerProcess, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);

                    switch (status.toLowerCase()) {
                        case "running":
                            setTextFill(Color.BLUE);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "completed":
                            setTextFill(Color.GREEN);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "failed":
                            setTextFill(Color.RED);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "pending":
                            setTextFill(Color.ORANGE);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        default:
                            setTextFill(Color.BLACK);
                            setStyle("");
                    }
                }
            }
        });

        activeProcessesTable.setItems(processList);

        ContextMenu tableContextMenu = new ContextMenu();

        MenuItem viewDetails = new MenuItem("Просмотреть детали");
        MenuItem approveResults = new MenuItem("Одобрить результаты");
        MenuItem rejectResults = new MenuItem("Отклонить результаты");
        MenuItem cancelProcess = new MenuItem("Отменить процесс");

        viewDetails.setOnAction(e -> viewProcessDetails());
        approveResults.setOnAction(e -> approveProcessResults());
        rejectResults.setOnAction(e -> rejectProcessResults());
        cancelProcess.setOnAction(e -> cancelSelectedProcess());

        tableContextMenu.getItems().addAll(viewDetails,
                new SeparatorMenuItem(),
                approveResults,
                rejectResults,
                new SeparatorMenuItem(),
                cancelProcess);

        activeProcessesTable.setContextMenu(tableContextMenu);
    }

    private void loadUtilizers() {
        utilizersGrid.getChildren().clear();
        utilizerCards.clear();
        allUtilizers.clear();

        List<Utilizer> utilizers = UtilizerDAO.getAllUtilizers();
        allUtilizers.addAll(utilizers);

        int row = 0;
        int col = 0;
        int maxCols = 3;

        for (Utilizer utilizer : utilizers) {
            VBox card = createUtilizerCard(utilizer);
            utilizerCards.put(utilizer.getName(), new UtilizerCard(card, utilizer));

            GridPane.setMargin(card, new Insets(10));
            utilizersGrid.add(card, col, row);

            col++;
            if (col >= maxCols) {
                col = 0;
                row++;
            }
        }

        updateStatistics();
    }

    private class UtilizerCard {
        VBox card;
        Utilizer utilizer;
        Button startButton;
        ProgressIndicator progressIndicator;

        UtilizerCard(VBox card, Utilizer utilizer) {
            this.card = card;
            this.utilizer = utilizer;
        }
    }

    private VBox createUtilizerCard(Utilizer utilizer) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(utilizer.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Circle statusCircle = new Circle(6);
        updateStatusCircle(statusCircle, utilizer);

        header.getChildren().addAll(statusCircle, nameLabel);

        Label descriptionLabel = new Label(utilizer.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        descriptionLabel.setMaxHeight(40);

        VBox infoPanel = new VBox(5);

        Label loadLabel = new Label("Загрузка: " + utilizer.getCurrentLoad() + "/" + utilizer.getCapacity());
        loadLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label statusLabel = new Label();
        updateStatusLabel(statusLabel, utilizer);

        Label ipLabel = new Label("IP: " + utilizer.getIpAddress() + ":" + utilizer.getPort());
        ipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        infoPanel.getChildren().addAll(loadLabel, statusLabel, ipLabel);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button detailsButton = new Button("Информация");
        detailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        detailsButton.setOnAction(e -> openUtilizerDetails(utilizer));

        Button startButton = new Button("Запустить процесс");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px;");
        startButton.setOnAction(e -> startUtilizerProcess(utilizer));

        boolean isUtilizerAvailable = checkUtilizerAvailability(utilizer);
        startButton.setDisable(!isUtilizerAvailable);

        if (!isUtilizerAvailable) {
            String reason = getUnavailabilityReason(utilizer);
            Tooltip tooltip = new Tooltip(reason);
            startButton.setTooltip(tooltip);
        }

        buttonBox.getChildren().addAll(detailsButton, startButton);

        card.getChildren().addAll(header, descriptionLabel, infoPanel, buttonBox);

        UtilizerCard utilizerCard = utilizerCards.get(utilizer.getName());
        if (utilizerCard != null) {
            utilizerCard.startButton = startButton;
        }

        return card;
    }

    private void updateStatusCircle(Circle circle, Utilizer utilizer) {
        String status = utilizer.getStatus();

        switch (status) {
            case "available":
                circle.setFill(Color.GREEN);
                break;
            case "busy":
                circle.setFill(Color.ORANGE);
                break;
            case "full":
                circle.setFill(Color.RED);
                break;
            case "maintenance":
                circle.setFill(Color.GRAY);
                break;
            default:
                circle.setFill(Color.LIGHTGRAY);
        }
    }

    private void updateStatusLabel(Label label, Utilizer utilizer) {
        String status = utilizer.getStatus();
        String statusText = "";
        String color = "";

        switch (status) {
            case "available":
                statusText = "🟢 Доступен";
                color = "#4CAF50";
                break;
            case "busy":
                statusText = "🟡 Частично занят";
                color = "#FF9800";
                break;
            case "full":
                statusText = "🔴 Полностью занят";
                color = "#F44336";
                break;
            case "maintenance":
                statusText = "⚪ На обслуживании";
                color = "#9E9E9E";
                break;
            default:
                statusText = "❓ Неизвестно";
                color = "#607D8B";
        }

        label.setText(statusText);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 12px;");
    }

    private boolean checkUtilizerAvailability(Utilizer utilizer) {
        String status = utilizer.getStatus();
        int currentLoad = utilizer.getCurrentLoad();
        int capacity = utilizer.getCapacity();

        if ("maintenance".equals(status)) {
            return false;
        }

        if ("full".equals(status) || currentLoad >= capacity) {
            return false;
        }

        // Пробуем проверить через эмулятор
        try {
            System.out.println("Проверка доступности утилизатора: " + utilizer.getName());

            String url = "http://localhost:5000/api/utilizer/" + utilizer.getName() + "/capacity";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    System.out.println("Ответ от эмулятора: " + json);

                    Gson gson = new Gson();
                    Map<String, Object> capacityInfo = gson.fromJson(json,
                            new TypeToken<Map<String, Object>>(){}.getType());

                    if (capacityInfo != null && capacityInfo.containsKey("availableSlots")) {
                        int availableSlots = ((Number) capacityInfo.get("availableSlots")).intValue();
                        System.out.println("Утилизатор " + utilizer.getName() +
                                ": доступно слотов - " + availableSlots);
                        return availableSlots > 0;
                    }
                } else {
                    System.out.println("Эмулятор вернул ошибку: " + response.code() +
                            ", тело: " + response.body().string());
                    // Если эмулятор не доступен или ошибка, используем локальные данные
                    return currentLoad < capacity;
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при проверке доступности через эмулятор: " + e.getMessage());
            System.out.println("Используем локальную проверку для: " + utilizer.getName());
            // Если эмулятор не отвечает, используем локальные данные
            return currentLoad < capacity;
        }

        return currentLoad < capacity;
    }

    private String getUnavailabilityReason(Utilizer utilizer) {
        String status = utilizer.getStatus();
        int currentLoad = utilizer.getCurrentLoad();
        int capacity = utilizer.getCapacity();

        if ("maintenance".equals(status)) {
            return "На обслуживании";
        }

        if ("full".equals(status) || currentLoad >= capacity) {
            return "Полностью занят. Текущая загрузка: " + currentLoad + "/" + capacity;
        }

        return "Неизвестная причина";
    }

    private void openUtilizerDetails(Utilizer utilizer) {
        try {
            Map<String, Object> capacity = UtilizerServiceClient.getUtilizerCapacity(utilizer.getName());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация об утилизаторе");
            alert.setHeaderText(utilizer.getName());

            String content = "Модель: " + (utilizer.getModel() != null ? utilizer.getModel() : "Не указана") + "\n" +
                    "Производитель: " + (utilizer.getManufacturer() != null ? utilizer.getManufacturer() : "Не указан") + "\n" +
                    "Описание: " + utilizer.getDescription() + "\n" +
                    "Адрес: " + utilizer.getIpAddress() + ":" + utilizer.getPort() + "\n" +
                    "Вместимость: " + utilizer.getCapacity() + " процессов\n" +
                    "Текущая загрузка: " + utilizer.getCurrentLoad() + " процессов\n" +
                    "Статус: " + utilizer.getStatus();

            // Проверяем доступность утилизатора
            boolean isAvailable = checkUtilizerAvailability(utilizer);
            String availabilityStatus = isAvailable ? "✅ Доступен для запуска процессов" :
                    "❌ Недоступен для запуска процессов";

            if (!isAvailable) {
                String reason = getUnavailabilityReason(utilizer);
                availabilityStatus += "\nПричина: " + reason;
            }

            content += "\n\nСтатус доступности: " + availabilityStatus;

            if (capacity != null) {
                content += "\n\nСтатус эмулятора: ✅ Работает";
                content += "\nДоступные слоты: " + capacity.get("availableSlots");
                content += "\nЦвет статуса: " + capacity.get("statusColor");
            } else {
                content += "\n\nСтатус эмулятора: ❌ Не доступен";
            }

            List<UtilizerProcess> processes = UtilizerProcessDAO.getProcessesByUtilizer(utilizer.getName());
            if (!processes.isEmpty()) {
                content += "\n\nИстория процессов (" + processes.size() + "):";
                int completed = 0;
                for (UtilizerProcess process : processes) {
                    if ("completed".equals(process.getStatus())) completed++;
                }
                content += "\nЗавершено: " + completed + " из " + processes.size();
            }

            alert.setContentText(content);

            ButtonType startProcessButton = new ButtonType("Запустить процесс", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(startProcessButton, cancelButton);

            // Отключаем кнопку запуска, если утилизатор недоступен
            DialogPane dialogPane = alert.getDialogPane();
            Button startButton = (Button) dialogPane.lookupButton(startProcessButton);
            startButton.setDisable(!isAvailable);

            if (!isAvailable) {
                String reason = getUnavailabilityReason(utilizer);
                Tooltip tooltip = new Tooltip(reason);
                startButton.setTooltip(tooltip);
            }

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == startProcessButton) {
                // Проверяем еще раз перед запуском
                if (!checkUtilizerAvailability(utilizer)) {
                    showAlert("Ошибка", "Утилизатор стал недоступен для запуска процесса:\n" +
                            getUnavailabilityReason(utilizer));
                    return;
                }
                startUtilizerProcess(utilizer);
            }

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось получить информацию: " + e.getMessage());
        }
    }

    private void startUtilizerProcess(Utilizer utilizer) {
        try {
            Dialog<Service> dialog = new Dialog<>();
            dialog.setTitle("Выбор услуги");
            dialog.setHeaderText("Выберите услугу для запуска на утилизаторе: " + utilizer.getName());

            List<Service> availableServices = ServiceDAO.getServicesByUtilizer(utilizer.getName());
            if (availableServices.isEmpty()) {
                showAlert("Внимание", "Для данного утилизатора нет доступных услуг");
                return;
            }

            ComboBox<Service> serviceComboBox = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(availableServices));
            serviceComboBox.setCellFactory(listView -> new ListCell<Service>() {
                @Override
                protected void updateItem(Service service, boolean empty) {
                    super.updateItem(service, empty);
                    if (empty || service == null) {
                        setText(null);
                    } else {
                        setText(service.getCode() + " - " + service.getName() + " (" + service.getCost() + " руб.)");
                    }
                }
            });
            serviceComboBox.setButtonCell(new ListCell<Service>() {
                @Override
                protected void updateItem(Service service, boolean empty) {
                    super.updateItem(service, empty);
                    if (empty || service == null) {
                        setText(null);
                    } else {
                        setText(service.getCode() + " - " + service.getName());
                    }
                }
            });

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.getChildren().addAll(
                    new Label("Доступные услуги:"),
                    serviceComboBox
            );

            dialog.getDialogPane().setContent(content);

            ButtonType startButton = new ButtonType("Запустить", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(startButton, cancelButton);

            dialog.setResultConverter(buttonType -> {
                if (buttonType == startButton) {
                    return serviceComboBox.getValue();
                }
                return null;
            });

            Optional<Service> result = dialog.showAndWait();
            if (result.isPresent()) {
                Service selectedService = result.get();

                List<Order> orders = OrderDAO.getAllOrders();
                Order order = orders.isEmpty() ? createTestOrder() : orders.get(0);

                UtilizerProcess process = new UtilizerProcess();
                process.setOrderId(order.getId());
                process.setOrderNumber(order.getOrderNumber() != null ? order.getOrderNumber() : "ORDER-" + System.currentTimeMillis());
                process.setServiceId(selectedService.getId());
                process.setServiceCode(selectedService.getCode());
                process.setServiceName(selectedService.getName());
                process.setUtilizerId(utilizer.getId());
                process.setUtilizerName(utilizer.getName());
                process.setResearcherId(currentUser.getId());
                process.setResearcherName(currentUser.getName());

                String processId = UtilizerServiceClient.startUtilizerProcess(process);

                if (processId != null) {
                    process.setProcessUuid(processId);
                    String savedId = UtilizerProcessDAO.createProcess(process);

                    if (savedId != null) {
                        showAlert("Успех",
                                "Процесс запущен!\n" +
                                        "ID процесса: " + processId.substring(0, 8) + "...\n" +
                                        "Утилизатор: " + utilizer.getName() + "\n" +
                                        "Услуга: " + process.getServiceName() + "\n" +
                                        "Ожидаемое время выполнения: 30 секунд");

                        loadUtilizers();
                        loadActiveProcesses();

                        startMonitoringProcess(processId, utilizer.getName(), process);
                    } else {
                        showAlert("Ошибка", "Процесс запущен в эмуляторе, но не сохранен в БД");
                    }
                } else {
                    showAlert("Ошибка", "Не удалось запустить процесс в эмуляторе");
                }
            }

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось запустить процесс: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setId(999);
        order.setOrderNumber("TEST-001");
        order.setClientId(1);
        order.setStatus("Создан");
        return order;
    }

    private void startMonitoringProcess(String processId, String utilizerName, UtilizerProcess process) {
        new Thread(() -> {
            try {
                int maxChecks = 60;
                int checkCount = 0;

                while (checkCount < maxChecks) {
                    Thread.sleep(500);
                    checkCount++;

                    Map<String, Object> status = UtilizerServiceClient.getProcessStatus(processId, utilizerName);

                    if (status != null) {
                        int progress = getIntFromMap(status, "progress", 0);
                        String currentStatus = getStringFromMap(status, "status", "running");
                        String currentStep = getStringFromMap(status, "currentStep", "Выполняется...");

                        Platform.runLater(() -> {
                            updateProcessProgress(processId, progress, currentStatus, currentStep);
                        });

                        if ("completed".equals(currentStatus) || progress >= 100) {
                            Map<String, Object> results = UtilizerServiceClient.getProcessResults(processId, utilizerName);
                            if (results != null) {
                                Platform.runLater(() -> {
                                    showProcessResults(processId, results);
                                });
                            }
                            break;
                        }
                    } else {
                        int estimatedProgress = Math.min(100, (checkCount * 100) / maxChecks);
                        Platform.runLater(() -> {
                            updateProcessProgress(processId, estimatedProgress, "running", "Ожидание ответа от эмулятора...");
                        });
                    }
                }

                if (checkCount >= maxChecks) {
                    Platform.runLater(() -> {
                        showAlert("Внимание",
                                "Процесс " + processId.substring(0, 8) + "...\n" +
                                        "Превышено время ожидания. Проверьте эмулятор.");
                    });
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int getIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private boolean getBooleanFromMap(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return defaultValue;
    }

    private void updateProcessProgress(String processId, int progress, String status, String currentStep) {
        for (UtilizerProcess process : processList) {
            if (processId.equals(process.getProcessUuid())) {
                process.setProgress(progress);
                process.setStatus(status);

                UtilizerProcessDAO.updateProcessProgress(processId, progress, status);
                break;
            }
        }
        activeProcessesTable.refresh();
        updateActiveProcessesCount();
    }

    private void showProcessResults(String processId, Map<String, Object> results) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результаты процесса");
        alert.setHeaderText("Процесс завершен: " + processId.substring(0, 8) + "...");

        StringBuilder content = new StringBuilder();
        content.append("Услуга: ").append(getStringFromMap(results, "serviceName", "Неизвестно")).append("\n");
        content.append("Код услуги: ").append(getStringFromMap(results, "serviceCode", "Неизвестно")).append("\n");
        content.append("Тип процесса: ").append(getStringFromMap(results, "processType", "Неизвестно")).append("\n");
        content.append("Исследователь: ").append(getStringFromMap(results, "researcherName", "Неизвестно")).append("\n");

        if (results.containsKey("density")) {
            content.append("\nРезультаты измерений:\n");
            content.append("Плотность: ").append(results.get("density")).append("%\n");
            content.append("Дисперсность: ").append(results.get("dispersion")).append("%\n");
            content.append("Ртуть: ").append(results.get("mercuryConcentration")).append("%\n");
            content.append("Креозол: ").append(results.get("creosolConcentration")).append("%\n");
            content.append("Едкий калий: ").append(results.get("potassiumHydroxideConcentration")).append("%\n");
            content.append("Тяжелые металлы: ").append(results.get("heavyMetalsConcentration")).append("%\n");
        }

        boolean hasDeviationWarning = getBooleanFromMap(results, "hasDeviationWarning", false);
        if (hasDeviationWarning) {
            content.append("\n⚠️ ВНИМАНИЕ: Обнаружены аномальные показатели!\n");
            content.append(getStringFromMap(results, "anomalyDescription", "Неизвестная аномалия")).append("\n");
        }

        boolean withinNormalRange = getBooleanFromMap(results, "withinNormalRange", false);
        content.append("\nСтатус: ").append(withinNormalRange ? "✅ В пределах нормы" : "❌ Требует проверки");

        alert.setContentText(content.toString());

        ButtonType approveButton = new ButtonType("Одобрить", ButtonBar.ButtonData.OK_DONE);
        ButtonType rejectButton = new ButtonType("Отклонить", ButtonBar.ButtonData.NO);
        ButtonType reviewLaterButton = new ButtonType("Проверить позже", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(approveButton, rejectButton, reviewLaterButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == approveButton) {
                approveProcess(processId, results);
            } else if (result.get() == rejectButton) {
                rejectProcess(processId, results);
            }
        }
    }

    private void approveProcess(String processId, Map<String, Object> results) {
        try {
            boolean success = UtilizerServiceClient.approveResults(
                    processId,
                    getUtilizerNameFromProcessId(processId),
                    currentUser.getName(),
                    "Результаты одобрены"
            );

            if (success) {
                showAlert("Успех", "Результаты процесса успешно одобрены");
                UtilizerProcessDAO.updateProcessStatus(processId, "approved", currentUser.getName());
                loadActiveProcesses();
            } else {
                showAlert("Ошибка", "Не удалось одобрить результаты");
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Ошибка при одобрении результатов: " + e.getMessage());
        }
    }

    private void rejectProcess(String processId, Map<String, Object> results) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Отклонение результатов");
        dialog.setHeaderText("Процесс: " + processId.substring(0, 8) + "...");
        dialog.setContentText("Укажите причину отклонения:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                boolean success = UtilizerServiceClient.rejectResults(
                        processId,
                        getUtilizerNameFromProcessId(processId),
                        result.get(),
                        currentUser.getName()
                );

                if (success) {
                    showAlert("Успех", "Результаты процесса отклонены");
                    UtilizerProcessDAO.updateProcessStatus(processId, "rejected", currentUser.getName());
                    loadActiveProcesses();
                } else {
                    showAlert("Ошибка", "Не удалось отклонить результаты");
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Ошибка при отклонении результатов: " + e.getMessage());
            }
        }
    }

    private String getUtilizerNameFromProcessId(String processId) {
        for (UtilizerProcess process : processList) {
            if (processId.equals(process.getProcessUuid())) {
                return process.getUtilizerName();
            }
        }
        return "unknown";
    }

    private void viewProcessDetails() {
        UtilizerProcess selectedProcess = activeProcessesTable.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            try {
                Map<String, Object> results = UtilizerServiceClient.getProcessResults(
                        selectedProcess.getProcessUuid(),
                        selectedProcess.getUtilizerName()
                );

                if (results != null) {
                    showProcessResults(selectedProcess.getProcessUuid(), results);
                } else {
                    showAlert("Информация",
                            "Процесс: " + selectedProcess.getProcessUuid().substring(0, 8) + "...\n" +
                                    "Услуга: " + selectedProcess.getServiceName() + "\n" +
                                    "Утилизатор: " + selectedProcess.getUtilizerName() + "\n" +
                                    "Прогресс: " + selectedProcess.getProgress() + "%\n" +
                                    "Статус: " + selectedProcess.getStatus());
                }
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось получить детали процесса: " + e.getMessage());
            }
        } else {
            showAlert("Внимание", "Выберите процесс для просмотра деталей");
        }
    }

    private void approveProcessResults() {
        UtilizerProcess selectedProcess = activeProcessesTable.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            if ("completed".equals(selectedProcess.getStatus())) {
                approveProcess(selectedProcess.getProcessUuid(), null);
            } else {
                showAlert("Внимание", "Можно одобрять только завершенные процессы");
            }
        } else {
            showAlert("Внимание", "Выберите процесс для одобрения");
        }
    }

    private void rejectProcessResults() {
        UtilizerProcess selectedProcess = activeProcessesTable.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            if ("completed".equals(selectedProcess.getStatus())) {
                rejectProcess(selectedProcess.getProcessUuid(), null);
            } else {
                showAlert("Внимание", "Можно отклонять только завершенные процессы");
            }
        } else {
            showAlert("Внимание", "Выберите процесс для отклонения");
        }
    }

    private void cancelSelectedProcess() {
        UtilizerProcess selectedProcess = activeProcessesTable.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Отмена процесса");
            confirmAlert.setHeaderText("Вы уверены, что хотите отменить процесс?");
            confirmAlert.setContentText("Процесс: " + selectedProcess.getProcessUuid().substring(0, 8) + "...\n" +
                    "Услуга: " + selectedProcess.getServiceName());

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                UtilizerProcessDAO.updateProcessStatus(selectedProcess.getProcessUuid(), "failed", currentUser.getName());
                showAlert("Успех", "Процесс отменен");
                loadActiveProcesses();
            }
        } else {
            showAlert("Внимание", "Выберите процесс для отмены");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateStatistics() {
        Map<String, Integer> stats = UtilizerDAO.getUtilizerStatistics();

        totalUtilizersLabel.setText(String.valueOf(stats.getOrDefault("total", 0)));
        availableUtilizersLabel.setText(String.valueOf(stats.getOrDefault("available", 0)));
        busyUtilizersLabel.setText(String.valueOf(stats.getOrDefault("busy", 0)));
        fullUtilizersLabel.setText(String.valueOf(stats.getOrDefault("full", 0)));
    }

    private void loadActiveProcesses() {
        List<UtilizerProcess> processes = UtilizerProcessDAO.getActiveProcesses();
        processList.setAll(processes);
        updateActiveProcessesCount();
    }

    private void updateActiveProcessesCount() {
        int activeCount = (int) processList.stream()
                .filter(p -> "running".equals(p.getStatus()) || "pending".equals(p.getStatus()))
                .count();
        int totalCount = processList.size();

        activeProcessesCount.setText("Активных процессов: " + activeCount + " из " + totalCount);
    }

    private void updateTimestamp() {
        timestampLabel.setText("Обновлено: " + LocalDateTime.now().format(timeFormatter));
    }

    private void setupStatusUpdateTimer() {
        statusUpdateTimer = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> {
                    loadUtilizers();
                    loadActiveProcesses();
                    updateTimestamp();
                })
        );
        statusUpdateTimer.setCycleCount(Timeline.INDEFINITE);
        statusUpdateTimer.play();
    }

    private void checkEmulatorHealth() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                boolean isHealthy = UtilizerServiceClient.checkHealth();

                Platform.runLater(() -> {
                    if (isHealthy) {
                        emulatorStatusLabel.setText("Статус эмулятора: ✅ Работает");
                        emulatorStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        System.out.println("✅ Эмулятор доступен");
                    } else {
                        emulatorStatusLabel.setText("Статус эмулятора: ❌ Не запущен");
                        emulatorStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        showAlert("Внимание",
                                "Эмулятор утилизатора не запущен на порту 5000.\n" +
                                        "Запустите эмулятор для полной функциональности.");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadUtilizers();
        loadActiveProcesses();
        updateTimestamp();
        showAlert("Обновлено", "Данные успешно обновлены");
    }

    @FXML
    private void handleReleaseUtilizers() {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Освобождение утилизаторов");
            confirmAlert.setHeaderText("Вы уверены?");
            confirmAlert.setContentText("Это действие освободит все утилизаторы и сбросит текущие процессы.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                UtilizerDAO.releaseAllUtilizers();

                loadUtilizers();
                loadActiveProcesses();

                showAlert("Успех", "Все утилизаторы освобождены");
            }
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось освободить утилизаторы: " + e.getMessage());
        }
    }

    @FXML
    private void handleProcessHistory() {
        try {
            Stage currentStage = (Stage) utilizersGrid.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_file/process_history.fxml"));
            Scene scene = new Scene(loader.load());
            currentStage.setScene(scene);
            currentStage.setTitle("История процессов утилизации");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть историю процессов: " + e.getMessage());
        }
    }

    @FXML
    private void handleReports() {
        try {
            ContextMenu reportMenu = new ContextMenu();

            MenuItem utilizerReport = new MenuItem("Отчет по утилизаторам (CSV)");
            MenuItem processReport = new MenuItem("Отчет по процессам (CSV)");
            MenuItem efficiencyReport = new MenuItem("Отчет по эффективности (CSV)");
            MenuItem exportPDF = new MenuItem("Полный отчет (PDF)");

            utilizerReport.setOnAction(e -> generateUtilizerReport());
            processReport.setOnAction(e -> generateProcessReport());
            efficiencyReport.setOnAction(e -> generateEfficiencyReport());
            exportPDF.setOnAction(e -> generatePDFReport());

            reportMenu.getItems().addAll(utilizerReport, processReport, efficiencyReport,
                    new SeparatorMenuItem(), exportPDF);

            if (reportsButton != null) {
                reportMenu.show(reportsButton,
                        reportsButton.localToScreen(reportsButton.getBoundsInLocal()).getMinX(),
                        reportsButton.localToScreen(reportsButton.getBoundsInLocal()).getMaxY());
            } else {
                Stage stage = (Stage) utilizersGrid.getScene().getWindow();
                reportMenu.show(stage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть меню отчетов: " + e.getMessage());
        }
    }

    private void generateUtilizerReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по утилизаторам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_утилизаторы_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) utilizersGrid.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<Utilizer> utilizers = UtilizerDAO.getAllUtilizers();

                StringBuilder csv = new StringBuilder();
                csv.append("Название;Модель;Производитель;Вместимость;Текущая загрузка;Статус;IP адрес\n");

                for (Utilizer utilizer : utilizers) {
                    csv.append(String.format("%s;%s;%s;%d;%d;%s;%s:%d\n",
                            utilizer.getName(),
                            utilizer.getModel() != null ? utilizer.getModel() : "",
                            utilizer.getManufacturer() != null ? utilizer.getManufacturer() : "",
                            utilizer.getCapacity(),
                            utilizer.getCurrentLoad(),
                            utilizer.getStatus(),
                            utilizer.getIpAddress(),
                            utilizer.getPort()
                    ));
                }

                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по утилизаторам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateProcessReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по процессам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_процессы_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) utilizersGrid.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<UtilizerProcess> processes = UtilizerProcessDAO.getAllProcesses();

                StringBuilder csv = new StringBuilder();
                csv.append("ID процесса;Номер заказа;Услуга;Утилизатор;Исследователь;Статус;Прогресс;Время начала\n");

                for (UtilizerProcess process : processes) {
                    csv.append(String.format("%s;%s;%s;%s;%s;%s;%d%%;%s\n",
                            process.getProcessUuid() != null ?
                                    process.getProcessUuid().substring(0, 8) + "..." : "N/A",
                            process.getOrderNumber(),
                            process.getServiceName(),
                            process.getUtilizerName(),
                            process.getResearcherName(),
                            process.getStatus(),
                            process.getProgress(),
                            process.getStartTime() != null ?
                                    process.getStartTime().toString() : "N/A"
                    ));
                }

                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по процессам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateEfficiencyReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по эффективности");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_эффективность_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) utilizersGrid.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<Utilizer> utilizers = UtilizerDAO.getAllUtilizers();

                StringBuilder csv = new StringBuilder();
                csv.append("=== ОТЧЕТ ПО ЭФФЕКТИВНОСТИ РАБОТЫ УТИЛИЗАТОРОВ ===\n");
                csv.append("Дата формирования: " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n\n");

                csv.append("Утилизатор;Загрузка (%)\n");

                for (Utilizer utilizer : utilizers) {
                    double loadPercentage = (double) utilizer.getCurrentLoad() / utilizer.getCapacity() * 100;

                    csv.append(String.format("\n%s;%.2f%%",
                            utilizer.getName(),
                            loadPercentage
                    ));
                }

                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по эффективности сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generatePDFReport() {
        showAlert("В разработке", "Генерация PDF отчетов будет доступна в следующем обновлении");
    }

    @FXML
    private void handleNewUtilizer() {
        showAlert("Информация", "Функция добавления новых утилизаторов в разработке");
    }

    @FXML
    private void handleSettings() {
        showAlert("Настройки", "Настройки системы утилизаторов");
    }

    @Override
    protected void stopAllTimers() {
        super.stopAllTimers();
        if (statusUpdateTimer != null) {
            statusUpdateTimer.stop();
        }
    }
}