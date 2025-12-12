package com.example.trash.api;

import com.example.trash.dao.ClientDAO;
import com.example.trash.dao.OrderDAO;
import com.example.trash.model.Client;
import com.example.trash.model.Order;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDateTime;
import java.util.List;

import static spark.Spark.*;

public class MobileAPI {

    public static void setup() {
        // Разрешаем CORS для мобильного приложения
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "*");
            response.type("application/json; charset=utf-8");
        });

        // API endpoint для приема данных от мобильного приложения
        post("/api/scan", (request, response) -> {
            try {
                Gson gson = new Gson();
                QRRequest qrRequest = gson.fromJson(request.body(), QRRequest.class);

                // Проверяем клиента
                Client client = findOrCreateClient(qrRequest);

                // Создаем заказ
                Order order = new Order();
                order.setCaseCode(qrRequest.caseCode);
                order.setClientId(client.getId());
                order.setStatus("Принят через мобильное приложение");
                order.setServices(List.of(1)); // ID базовой услуги (настройте под свою БД)

                int orderId = OrderDAO.createOrder(order);

                if (orderId > 0) {
                    QRResponse qrResponse = new QRResponse();
                    qrResponse.success = true;
                    qrResponse.orderId = orderId;
                    qrResponse.message = "Заказ успешно создан. Номер: " + orderId;

                    return gson.toJson(qrResponse);
                } else {
                    response.status(500);
                    return "{\"error\": \"Не удалось создать заказ\"}";
                }
            } catch (Exception e) {
                response.status(400);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        });

        // Тестовый endpoint
        get("/api/test", (req, res) -> "{\"status\": \"API работает\", \"timestamp\": \"" + LocalDateTime.now() + "\"}");

        System.out.println("✅ Mobile API запущен на порту 4567");
        System.out.println("📱 Тестовый endpoint: GET http://localhost:4567/api/test");
        System.out.println("📱 Основной endpoint: POST http://localhost:4567/api/scan");
    }

    private static Client findOrCreateClient(QRRequest qrRequest) {
        // Ищем клиента по телефону
        List<Client> clients = ClientDAO.fuzzySearchClients(qrRequest.clientPhone);

        if (!clients.isEmpty()) {
            return clients.get(0);
        }

        // Создаем нового клиента
        Client client = new Client();
        client.setFio(qrRequest.clientFio);
        client.setPhone(qrRequest.clientPhone);
        client.setEmail(qrRequest.clientEmail);
        client.setTypeClientId(1); // Физ. лицо

        ClientDAO.addClient(client);
        return client;
    }

    // Вспомогательные классы
    static class QRRequest {
        String caseCode;
        String clientFio;
        String clientPhone;
        String clientEmail;
    }

    static class QRResponse {
        boolean success;
        int orderId;
        String message;
    }
}