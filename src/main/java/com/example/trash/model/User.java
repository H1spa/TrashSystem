package com.example.trash.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class User {
    private int id;
    private String name;
    private String login;
    private String password;
    private String type;
    private LocalDateTime lastActivity;
    private String lastIp;
    private LocalDateTime sessionStart;
    private int dailyWorkTimeSeconds;

    // Конструкторы
    public User(int id, String name, LocalDateTime lastActivity) {
        this.id = id;
        this.name = name;
        this.lastActivity = lastActivity;
    }

    public User(int id, String name, String login, String password, String type) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.password = password;
        this.type = type;
    }

    // Полный конструктор для мониторинга
    public User(int id, String name, String login, LocalDateTime lastActivity,
                String lastIp, LocalDateTime sessionStart, int dailyWorkTimeSeconds) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.lastActivity = lastActivity;
        this.lastIp = lastIp;
        this.sessionStart = sessionStart;
        this.dailyWorkTimeSeconds = dailyWorkTimeSeconds;
    }

    // Геттеры
    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getType() { return type; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public String getLastIp() { return lastIp; }
    public LocalDateTime getSessionStart() { return sessionStart; }
    public int getDailyWorkTimeSeconds() { return dailyWorkTimeSeconds; }

    // Сеттеры
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogin(String login) { this.login = login; }
    public void setPassword(String password) { this.password = password; }
    public void setType(String type) { this.type = type; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }
    public void setSessionStart(LocalDateTime sessionStart) { this.sessionStart = sessionStart; }
    public void setDailyWorkTimeSeconds(int dailyWorkTimeSeconds) {
        this.dailyWorkTimeSeconds = dailyWorkTimeSeconds;
    }

    // Форматированные методы для отображения
    public String getLastActivityFormatted() {
        if (lastActivity == null) return "-";
        return lastActivity.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String getCurrentSessionTime() {
        if (sessionStart == null) return "00:00:00";
        Duration duration = Duration.between(sessionStart, LocalDateTime.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getDailyWorkTimeFormatted() {
        int hours = dailyWorkTimeSeconds / 3600;
        int minutes = (dailyWorkTimeSeconds % 3600) / 60;
        int seconds = dailyWorkTimeSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Для отображения IP-адреса
    public String getDisplayIp() {
        return lastIp != null ? lastIp : "-";
    }

    // Для отображения имени пользователя (login)
    public String getDisplayName() {
        return name != null ? name : "-";
    }
}