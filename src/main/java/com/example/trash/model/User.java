package com.example.trash.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class User {
    private int id;
    private String name;
    private String login;
    private String password;
    private String type;
    private LocalDateTime lastActivity;

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

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getType() { return type; }
    public LocalDateTime getLastActivity() { return lastActivity; }

    public String getLastActivityFormatted() {
        if (lastActivity == null) return "-";
        return lastActivity.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}

