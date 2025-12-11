package com.example.trash.api;

import com.example.trash.model.QRData;
import com.example.trash.util.Base64Generator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobileAPI {

    private static final Map<String, QRData> qrDataStorage = new ConcurrentHashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    // Метод для обработки QR-данных от мобильного приложения
    public static String processQRData(String jsonData) {
        try {
            // Парсим JSON
            QRData qrData = gson.fromJson(jsonData, QRData.class);

            // Генерируем уникальный код для доступа
            String accessCode = UUID.randomUUID().toString().substring(0, 8);

            // Сохраняем данные
            qrDataStorage.put(accessCode, qrData);

            // Создаем ответ
            Map<String, String> response = new HashMap<>();
            response.put("access_code", accessCode);
            response.put("message", "QR данные получены успешно");

            return gson.toJson(new ApiResponse(true, "Success", response));
        } catch (Exception e) {
            return gson.toJson(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    // Метод для получения данных по коду доступа
    public static String getQRData(String accessCode) {
        QRData qrData = qrDataStorage.get(accessCode);
        if (qrData != null) {
            // Удаляем из хранилища после получения
            qrDataStorage.remove(accessCode);
            return gson.toJson(new ApiResponse(true, "Data found", qrData));
        } else {
            return gson.toJson(new ApiResponse(false, "Data not found or expired", null));
        }
    }

    // Метод для генерации Base64 ссылки
    public static String generateOrderLink(String orderJson) {
        try {
            String base64Data = Base64.getEncoder().encodeToString(orderJson.getBytes());
            return gson.toJson(new ApiResponse(true, "Link generated", base64Data));
        } catch (Exception e) {
            return gson.toJson(new ApiResponse(false, "Error: " + e.getMessage(), null));
        }
    }
}