// Session.java
package com.example.trash.model;

import java.time.LocalDateTime;

public class Session {
    private int id;
    private int userId;
    private String ipAddress;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private LocalDateTime logoutTime;
    private boolean isActive;

    // Конструкторы
    public Session() {
    }

    public Session(int userId, String ipAddress) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isActive = true;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Дополнительные методы
    public boolean isExpired() {
        return isActive && lastActivity.isBefore(LocalDateTime.now().minusMinutes(5));
    }
}