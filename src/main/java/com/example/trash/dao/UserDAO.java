package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.LoginHistory;
import com.example.trash.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {

    public static void updateLastActivity(int userId) {
        String query = "UPDATE users SET last_activity = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для логирования попытки входа
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

            // Если вход успешный, обновляем last_login в таблице users
            if (success && userId > 0) {
                updateLastLogin(userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Обновление времени последнего входа
    public static void updateLastLogin(int userId) {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Получение истории входа с фильтрацией
    public static ObservableList<LoginHistory> getLoginHistory(String filterUsername) {
        ObservableList<LoginHistory> history = FXCollections.observableArrayList();

        StringBuilder query = new StringBuilder(
                "SELECT lh.*, u.login as username FROM login_history lh " +
                        "LEFT JOIN users u ON lh.user_id = u.id WHERE 1=1"
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
                        rs.getString("username"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("login_time").toLocalDateTime(),
                        rs.getBoolean("success")
                );
                history.add(loginHistory);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    // Улучшенный метод для получения онлайн пользователей с мониторингом
    public static ObservableList<User> getOnlineUsersWithMonitoring() {
        ObservableList<User> users = FXCollections.observableArrayList();

        String query = "SELECT u.id, u.fio, u.login, u.last_login, " +
                "(SELECT ip_address FROM login_history " +
                "WHERE user_id = u.id AND success = 1 " +
                "ORDER BY login_time DESC LIMIT 1) as last_ip " +
                "FROM users u " +
                "WHERE u.last_login >= DATE_SUB(NOW(), INTERVAL 5 MINUTE) " +
                "AND u.archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fio"),
                        rs.getString("login"),
                        rs.getTimestamp("last_login") != null ?
                                rs.getTimestamp("last_login").toLocalDateTime() : null,
                        rs.getString("last_ip"),
                        rs.getTimestamp("last_login") != null ?
                                rs.getTimestamp("last_login").toLocalDateTime() : null,
                        getDailyWorkTime(rs.getInt("id"))
                );
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    // Метод для получения общего времени работы за день
    private static int getDailyWorkTime(int userId) {
        String query = "SELECT SUM(TIMESTAMPDIFF(SECOND, login_time, " +
                "COALESCE(lead_time, NOW()))) as total_seconds " +
                "FROM (SELECT login_time, " +
                "LEAD(login_time) OVER (ORDER BY login_time) as lead_time " +
                "FROM login_history " +
                "WHERE user_id = ? AND success = 1 " +
                "AND DATE(login_time) = CURDATE()) as sessions";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_seconds");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Методы для действий администратора
    public static boolean disconnectUser(int userId) {
        String query = "UPDATE users SET last_login = DATE_SUB(NOW(), INTERVAL 10 MINUTE) WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean blockUser(int userId) {
        String query = "UPDATE users SET archived = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByLoginAndPassword(String login, String password) {
        String sql = "SELECT id, fio, login, password, type_user_id FROM users " +
                "WHERE login = ? AND password = ? AND archived = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("fio"),
                        rs.getString("login"),
                        rs.getString("password"),
                        String.valueOf(rs.getInt("type_user_id"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}