package com.example.trash.model;

import java.time.LocalDateTime;

public class LoginHistory {
    private int id;
    private int userId;  // Add this field
    private String username;
    private String ipAddress;
    private LocalDateTime loginTime;
    private boolean success;

    public LoginHistory() {}

    // Updated constructor with 6 parameters including userId
    public LoginHistory(int id, int userId, String ipAddress,
                        LocalDateTime loginTime, boolean success, String username) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
        this.success = success;
        this.username = username;
    }

    // Original constructor for backward compatibility
    public LoginHistory(int id, String username, String ipAddress,
                        LocalDateTime loginTime, boolean success) {
        this.id = id;
        this.username = username;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
        this.success = success;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getStatus() {
        return success ? "Успешно" : "Ошибка";
    }

    public String getFormattedTime() {
        if (loginTime == null) return "";
        return loginTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}