package com.example.trash.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int fromUserId;
    private String message;
    private String type;
    private LocalDateTime createdAt;
    private boolean isRead;

    // Дополнительные поля для отображения
    private String userLogin;
    private String fromUserLogin;

    // Конструкторы
    public Notification() {}

    public Notification(int userId, int fromUserId, String message, String type) {
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.message = message;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getFromUserId() { return fromUserId; }
    public void setFromUserId(int fromUserId) { this.fromUserId = fromUserId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public String getFromUserLogin() { return fromUserLogin; }
    public void setFromUserLogin(String fromUserLogin) { this.fromUserLogin = fromUserLogin; }

    // Вспомогательные методы
    public String getTypeDisplay() {
        switch (type) {
            case "INFO": return "Информация";
            case "WARNING": return "Предупреждение";
            case "DISCONNECT": return "Отключение";
            case "BLOCK": return "Блокировка";
            default: return type;
        }
    }

    public String getCreatedAtFormatted() {
        return createdAt != null ?
                createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")) :
                "";
    }

    public String getFromUserDisplay() {
        return fromUserLogin != null ? fromUserLogin : (fromUserId > 0 ? "ID: " + fromUserId : "Система");
    }
}