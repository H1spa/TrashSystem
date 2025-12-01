package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.LoginHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class LoginHistoryDAO {

    // Запись попытки входа в историю
    public static void logLoginAttempt(int userId, String ipAddress, boolean success) {
        String query = "INSERT INTO login_history (user_id, ip_address, login_time, success) VALUES (?, ?, NOW(), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (userId > 0) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, ipAddress);
            stmt.setBoolean(3, success);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Получение всей истории с фильтрацией по логину
    public static ObservableList<LoginHistory> getLoginHistory(String filterUsername) {
        ObservableList<LoginHistory> history = FXCollections.observableArrayList();

        StringBuilder query = new StringBuilder(
                "SELECT lh.*, u.login as username " +
                        "FROM login_history lh " +
                        "LEFT JOIN users u ON lh.user_id = u.id " +
                        "WHERE 1=1"
        );

        if (filterUsername != null && !filterUsername.isEmpty()) {
            query.append(" AND u.login LIKE ?");
        }

        query.append(" ORDER BY lh.login_time DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            if (filterUsername != null && !filterUsername.isEmpty()) {
                stmt.setString(1, "%" + filterUsername + "%");
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LoginHistory loginHistory = new LoginHistory(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("login_time").toLocalDateTime(),
                        rs.getBoolean("success"),
                        rs.getString("username")
                );
                history.add(loginHistory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    // Получение истории за период (для статистики)
    public static ObservableList<LoginHistory> getLoginHistoryByPeriod(LocalDateTime start, LocalDateTime end) {
        ObservableList<LoginHistory> history = FXCollections.observableArrayList();

        String query = "SELECT lh.*, u.login as username " +
                "FROM login_history lh " +
                "LEFT JOIN users u ON lh.user_id = u.id " +
                "WHERE lh.login_time BETWEEN ? AND ? " +
                "ORDER BY lh.login_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LoginHistory loginHistory = new LoginHistory(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("login_time").toLocalDateTime(),
                        rs.getBoolean("success"),
                        rs.getString("username")
                );
                history.add(loginHistory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }
}