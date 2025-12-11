package com.example.trash.model;

import java.util.List;

public class QRData {
    private ClientData clientData;
    private List<String> services;
    private String caseCode;

    // Вложенный класс для данных клиента
    public static class ClientData {
        private String fio;
        private String phone;
        private String email;
        private String companyName;

        public ClientData() {}

        public ClientData(String fio, String phone, String email, String companyName) {
            this.fio = fio;
            this.phone = phone;
            this.email = email;
            this.companyName = companyName;
        }

        // Геттеры и сеттеры
        public String getFio() { return fio; }
        public void setFio(String fio) { this.fio = fio; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
    }

    // Конструкторы
    public QRData() {}

    public QRData(ClientData clientData, List<String> services, String caseCode) {
        this.clientData = clientData;
        this.services = services;
        this.caseCode = caseCode;
    }

    // Геттеры и сеттеры
    public ClientData getClientData() { return clientData; }
    public void setClientData(ClientData clientData) { this.clientData = clientData; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public String getCaseCode() { return caseCode; }
    public void setCaseCode(String caseCode) { this.caseCode = caseCode; }

    // Метод для создания из JSON
    public static QRData fromJson(String json) {
        // Простая реализация парсинга JSON
        // В реальном проекте используйте Gson или Jackson
        try {
            // Упрощенный парсинг для демонстрации
            QRData qrData = new QRData();
            QRData.ClientData clientData = new QRData.ClientData();

            // Пример формата: {"client":{"fio":"Иванов Иван","phone":"+79991112233"},"services":["Утилизация"]}
            if (json.contains("fio")) {
                int fioStart = json.indexOf("\"fio\":\"") + 7;
                int fioEnd = json.indexOf("\"", fioStart);
                clientData.setFio(json.substring(fioStart, fioEnd));
            }

            if (json.contains("phone")) {
                int phoneStart = json.indexOf("\"phone\":\"") + 9;
                int phoneEnd = json.indexOf("\"", phoneStart);
                clientData.setPhone(json.substring(phoneStart, phoneEnd));
            }

            qrData.setClientData(clientData);
            return qrData;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга QR данных: " + e.getMessage());
        }
    }

    // Метод для преобразования в JSON
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"client\":{");
        json.append("\"fio\":\"").append(clientData.getFio()).append("\",");
        json.append("\"phone\":\"").append(clientData.getPhone()).append("\"");
        if (clientData.getEmail() != null) {
            json.append(",\"email\":\"").append(clientData.getEmail()).append("\"");
        }
        if (clientData.getCompanyName() != null) {
            json.append(",\"companyName\":\"").append(clientData.getCompanyName()).append("\"");
        }
        json.append("},");
        json.append("\"services\":[");
        if (services != null) {
            for (int i = 0; i < services.size(); i++) {
                json.append("\"").append(services.get(i)).append("\"");
                if (i < services.size() - 1) {
                    json.append(",");
                }
            }
        }
        json.append("],");
        json.append("\"caseCode\":\"").append(caseCode).append("\"");
        json.append("}");
        return json.toString();
    }
}