package com.example.trash.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.trash.dao.OrderDAO;
import com.example.trash.dao.ServiceDAO;
import com.example.trash.dao.ClientDAO;
import com.example.trash.model.Order;
import com.example.trash.model.Client;
import com.example.trash.model.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        try {
            // Создаем меню выбора типа отчета
            ContextMenu reportMenu = new ContextMenu();

            MenuItem dailyReport = new MenuItem("Ежедневный отчет (CSV)");
            MenuItem ordersReport = new MenuItem("Отчет по заказам (CSV)");
            MenuItem clientsReport = new MenuItem("Отчет по клиентам (CSV)");
            MenuItem exportAll = new MenuItem("Полный отчет (CSV)");
            MenuItem exportPDF = new MenuItem("Отчет в PDF");

            dailyReport.setOnAction(e -> generateDailyReport());
            ordersReport.setOnAction(e -> generateOrdersReport());
            clientsReport.setOnAction(e -> generateClientsReport());
            exportAll.setOnAction(e -> generateFullReport());
            exportPDF.setOnAction(e -> generatePDFReport());

            reportMenu.getItems().addAll(dailyReport, ordersReport, clientsReport,
                    new SeparatorMenuItem(), exportAll, exportPDF);

            // Показываем меню рядом с кнопкой
            reportMenu.show(reportButton,
                    reportButton.localToScreen(reportButton.getBoundsInLocal()).getMinX(),
                    reportButton.localToScreen(reportButton.getBoundsInLocal()).getMaxY());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть меню отчетов: " + e.getMessage());
        }
    }

    private void generateDailyReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить ежедневный отчет");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "ежедневный_отчет_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Получаем данные за сегодня
                List<Order> todayOrders = OrderDAO.getTodayOrders();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("Дата;Номер заказа;Код кейса;Клиент;Услуги;Стоимость;Статус\n");

                double total = 0;
                for (Order order : todayOrders) {
                    Client client = ClientDAO.getClientById(order.getClientId());
                    List<Service> services = ServiceDAO.getServicesByIds(order.getServices());

                    StringBuilder servicesNames = new StringBuilder();
                    double orderTotal = 0;
                    for (Service service : services) {
                        if (servicesNames.length() > 0) servicesNames.append(", ");
                        servicesNames.append(service.getName());
                        orderTotal += service.getCost();
                    }
                    total += orderTotal;

                    csv.append(String.format("%s;%d;%s;%s;%s;%.2f;%s\n",
                            order.getFormattedDate(),
                            order.getOrderNumber(),
                            order.getCaseCode(),
                            client != null ? client.getFio() : "Неизвестно",
                            servicesNames.toString(),
                            orderTotal,
                            order.getStatus()
                    ));
                }

                csv.append(String.format("\nИтого за день:;%.2f руб.", total));

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Ежедневный отчет сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateOrdersReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по заказам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_заказы_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Получаем все заказы
                List<Order> allOrders = OrderDAO.getAllOrders();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("ID;Номер заказа;Дата;Код кейса;ID Клиента;Услуги;Статус\n");

                for (Order order : allOrders) {
                    StringBuilder servicesList = new StringBuilder();
                    for (Integer serviceId : order.getServices()) {
                        if (servicesList.length() > 0) servicesList.append(",");
                        servicesList.append(serviceId);
                    }

                    csv.append(String.format("%d;%d;%s;%s;%d;%s;%s\n",
                            order.getId(),
                            order.getOrderNumber(),
                            order.getFormattedDate(),
                            order.getCaseCode(),
                            order.getClientId(),
                            servicesList.toString(),
                            order.getStatus()
                    ));
                }

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по заказам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateClientsReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить отчет по клиентам");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "отчет_клиенты_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Получаем всех клиентов
                List<Client> allClients = ClientDAO.getAllClients();

                // Формируем CSV
                StringBuilder csv = new StringBuilder();
                csv.append("ID;ФИО;Дата рождения;Телефон;Email;Тип;Компания;Статус\n");

                for (Client client : allClients) {
                    String clientType = client.getTypeClientId() == 2 ? "Юр. лицо" : "Физ. лицо";
                    String status = client.isArchived() ? "Архив" : "Активен";

                    csv.append(String.format("%d;%s;%s;%s;%s;%s;%s;%s\n",
                            client.getId(),
                            client.getFio(),
                            client.getBirthDate() != null ?
                                    client.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "",
                            client.getPhone(),
                            client.getEmail(),
                            clientType,
                            client.getCompanyName() != null ? client.getCompanyName() : "",
                            status
                    ));
                }

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Отчет по клиентам сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generateFullReport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить полный отчет");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            String defaultFileName = "полный_отчет_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Формируем CSV с несколькими разделами
                StringBuilder csv = new StringBuilder();

                // 1. Статистика
                csv.append("=== ОТЧЕТ ЛАБОРАТОРИИ ===\n");
                csv.append("Дата формирования: " +
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "\n");
                csv.append("Пользователь: " + currentUser.getName() + "\n\n");

                // 2. Клиенты
                csv.append("=== КЛИЕНТЫ ===\n");
                csv.append("ID;ФИО;Телефон;Email;Тип\n");
                List<Client> clients = ClientDAO.getAllClients();
                for (Client client : clients) {
                    String type = client.getTypeClientId() == 2 ? "Юр. лицо" : "Физ. лицо";
                    csv.append(String.format("%d;%s;%s;%s;%s\n",
                            client.getId(), client.getFio(), client.getPhone(),
                            client.getEmail(), type));
                }
                csv.append("\n");

                // 3. Заказы за месяц
                csv.append("=== ЗАКАЗЫ ЗА МЕСЯЦ ===\n");
                csv.append("Номер заказа;Дата;Клиент;Статус;Кол-во услуг\n");
                List<Order> monthOrders = OrderDAO.getMonthOrders();
                for (Order order : monthOrders) {
                    Client client = ClientDAO.getClientById(order.getClientId());
                    csv.append(String.format("%d;%s;%s;%s;%d\n",
                            order.getOrderNumber(),
                            order.getFormattedDate(),
                            client != null ? client.getFio() : "Неизвестно",
                            order.getStatus(),
                            order.getServices().size()));
                }

                // Сохраняем файл
                try (PrintWriter writer = new PrintWriter(
                        new FileWriter(file.getAbsolutePath(), StandardCharsets.UTF_8))) {
                    writer.write(csv.toString());
                }

                showAlert("Успех", "Полный отчет сохранен:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать отчет: " + e.getMessage());
        }
    }

    private void generatePDFReport() {
        try {
            // Здесь можно реализовать генерацию PDF отчета
            // Пока просто покажем сообщение
            showAlert("В разработке", "Генерация PDF отчетов будет доступна в следующем обновлении");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось сгенерировать PDF отчет: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    protected Stage getCurrentStage() {
        return (Stage) logoutButton.getScene().getWindow();
    }
}