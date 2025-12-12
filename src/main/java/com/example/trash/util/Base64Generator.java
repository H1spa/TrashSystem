package com.example.trash.util;

import com.example.trash.model.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

public class Base64Generator {

    public static File generateOrderLinkWithSaveDialog(Order order, Client client, List<Service> services, Stage stage) {
        try {
            // Создаем FileChooser для выбора пути сохранения
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить Base64 ссылку");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"),
                    new FileChooser.ExtensionFilter("Все файлы", "*.*")
            );

            // Устанавливаем имя файла по умолчанию
            String defaultFileName = String.format("Ссылка_заказ_%d_%s.txt",
                    order.getOrderNumber(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            fileChooser.setInitialFileName(defaultFileName);

            // Показываем диалог сохранения
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Генерируем Base64 данные
                String base64Data = generateOrderLink(order, client, services);

                // Сохраняем в выбранный файл
                saveToFile(file, base64Data);
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateOrderLink(Order order, Client client, List<Service> services) {
        // Формируем JSON строку с информацией о заказе
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"order\": {");
        json.append("\"id\":").append(order.getId()).append(",");
        json.append("\"orderNumber\":").append(order.getOrderNumber()).append(",");
        json.append("\"date\":\"").append(order.getFormattedDate()).append("\",");
        json.append("\"caseCode\":\"").append(order.getCaseCode()).append("\",");
        json.append("\"status\":\"").append(order.getStatus()).append("\"");
        json.append("},");
        json.append("\"client\": {");
        json.append("\"id\":").append(client.getId()).append(",");
        json.append("\"fio\":\"").append(client.getFio()).append("\",");
        json.append("\"phone\":\"").append(client.getPhone()).append("\"");

        if (client.getEmail() != null && !client.getEmail().isEmpty()) {
            json.append(",\"email\":\"").append(client.getEmail()).append("\"");
        }

        if (client.getCompanyName() != null && !client.getCompanyName().isEmpty()) {
            json.append(",\"company\":\"").append(client.getCompanyName()).append("\"");
        }

        json.append("},");
        json.append("\"services\": [");

        double total = 0;
        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            json.append("{");
            json.append("\"id\":").append(service.getId()).append(",");
            json.append("\"name\":\"").append(service.getName()).append("\",");
            json.append("\"code\":\"").append(service.getCode() != null ? service.getCode() : "").append("\",");
            json.append("\"cost\":").append(service.getCost());
            json.append("}");

            if (i < services.size() - 1) {
                json.append(",");
            }

            total += service.getCost();
        }

        json.append("],");
        json.append("\"total\":").append(total);
        json.append("}");

        // Кодируем в Base64
        String jsonString = json.toString();
        String base64Data = Base64.getEncoder().encodeToString(
                jsonString.getBytes(StandardCharsets.UTF_8));

        // Формируем ссылку
        String link = "data:application/json;base64," + base64Data;

        return link;
    }

    public static void saveToFile(File file, String data) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("Base64 ссылка сохранена в файл: " + file.getAbsolutePath());
    }

    public static String decodeBase64(String base64Data) {
        try {
            // Убираем префикс data:application/json;base64, если есть
            if (base64Data.startsWith("data:application/json;base64,")) {
                base64Data = base64Data.substring("data:application/json;base64,".length());
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка декодирования Base64: " + e.getMessage());
        }
    }
}