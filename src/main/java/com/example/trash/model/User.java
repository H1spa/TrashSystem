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
    private int totalWorkTimeSeconds;
    private boolean archived;
    private String blockReason;
    private boolean sessionActive; // Добавленное поле

    // Конструкторы
    public User(int id, String name, LocalDateTime lastActivity) {
        this.id = id;
        this.name = name;
        this.lastActivity = lastActivity;
        this.sessionActive = false;
    }

    public User(int id, String name, String login, String password, String type) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.password = password;
        this.type = type;
        this.sessionActive = false;
    }

    // Полный конструктор для мониторинга (10 параметров)
    public User(int id, String name, String login, LocalDateTime lastActivity,
                String lastIp, LocalDateTime sessionStart,
                int dailyWorkTimeSeconds, int totalWorkTimeSeconds,
                boolean archived, String blockReason) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.lastActivity = lastActivity;
        this.lastIp = lastIp;
        this.sessionStart = sessionStart;
        this.dailyWorkTimeSeconds = dailyWorkTimeSeconds;
        this.totalWorkTimeSeconds = totalWorkTimeSeconds;
        this.archived = archived;
        this.blockReason = blockReason;
        this.sessionActive = false;
    }

    // Дополнительный конструктор для совместимости (7 параметров)
    public User(int id, String name, String login, LocalDateTime lastActivity,
                String lastIp, LocalDateTime sessionStart, int dailyWorkTimeSeconds) {
        this(id, name, login, lastActivity, lastIp, sessionStart,
                dailyWorkTimeSeconds, 0, false, null);
        this.sessionActive = false;
    }

    // Новый конструктор с sessionActive (11 параметров)
    public User(int id, String name, String login, LocalDateTime lastActivity,
                String lastIp, LocalDateTime sessionStart,
                int dailyWorkTimeSeconds, int totalWorkTimeSeconds,
                boolean archived, String blockReason, boolean sessionActive) {
        this.id = id;
        this.name = name;
        this.login = login;
        this.lastActivity = lastActivity;
        this.lastIp = lastIp;
        this.sessionStart = sessionStart;
        this.dailyWorkTimeSeconds = dailyWorkTimeSeconds;
        this.totalWorkTimeSeconds = totalWorkTimeSeconds;
        this.archived = archived;
        this.blockReason = blockReason;
        this.sessionActive = sessionActive;
    }

    // Геттеры и сеттеры для sessionActive
    public boolean isSessionActive() {
        return sessionActive;
    }

    public void setSessionActive(boolean sessionActive) {
        this.sessionActive = sessionActive;
    }

    // Геттеры и сеттеры (остальные остаются без изменений)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    public LocalDateTime getSessionStart() { return sessionStart; }
    public void setSessionStart(LocalDateTime sessionStart) { this.sessionStart = sessionStart; }

    public int getDailyWorkTimeSeconds() { return dailyWorkTimeSeconds; }
    public void setDailyWorkTimeSeconds(int dailyWorkTimeSeconds) {
        this.dailyWorkTimeSeconds = dailyWorkTimeSeconds;
    }

    public int getTotalWorkTimeSeconds() { return totalWorkTimeSeconds; }
    public void setTotalWorkTimeSeconds(int totalWorkTimeSeconds) {
        this.totalWorkTimeSeconds = totalWorkTimeSeconds;
    }

    public boolean getArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    // Обновленный метод getStatus(), который использует sessionActive
    public String getStatus() {
        if (archived) return "Заблокирован";

        // Если сессия не активна, сразу возвращаем "Оффлайн"
        if (!sessionActive) {
            return "Оффлайн";
        }

        // Проверяем последнюю активность только если сессия активна
        if (lastActivity != null) {
            long minutesDiff = Duration.between(lastActivity, LocalDateTime.now()).toMinutes();
            if (minutesDiff < 1) {
                return "Онлайн";
            } else if (minutesDiff < 5) {
                return "Отошел";
            } else {
                return "Оффлайн";
            }
        }

        return "Оффлайн";
    }

    // Форматированные методы для отображения
    public String getLastActivityFormatted() {
        if (lastActivity == null) return "-";
        return lastActivity.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }

    public String getCurrentSessionTime() {
        if (sessionStart == null || !sessionActive) return "00:00:00";
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

    public String getTotalWorkTimeFormatted() {
        int hours = totalWorkTimeSeconds / 3600;
        int minutes = (totalWorkTimeSeconds % 3600) / 60;
        int seconds = totalWorkTimeSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    // Для отображения IP-адреса
    public String getDisplayIp() {
        return lastIp != null ? lastIp : "-";
    }

    // Для отображения имени пользователя
    public String getDisplayName() {
        return name != null ? name : login != null ? login : "-";
    }
}