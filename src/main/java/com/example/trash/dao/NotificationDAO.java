package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationDAO {

    // Создание таблицы уведомлений, если она не существует
    private static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id INT NOT NULL, " +
                "from_user_id INT NULL, " +
                "message TEXT NOT NULL, " +
                "type ENUM('INFO', 'WARNING', 'DISCONNECT', 'BLOCK') DEFAULT 'INFO', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE SET NULL" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Создание уведомления
    public static boolean createNotification(int userId, int fromUserId, String message, String type) {
        createTableIfNotExists();

        String sql = "INSERT INTO notifications (user_id, from_user_id, message, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, fromUserId);
            stmt.setString(3, message);
            stmt.setString(4, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение непрочитанных уведомлений пользователя
    public static ObservableList<Notification> getUnreadNotifications(int userId) {
        ObservableList<Notification> notifications = FXCollections.observableArrayList();
        createTableIfNotExists();

        String sql = "SELECT n.*, u1.login as user_login, u2.login as from_user_login " +
                "FROM notifications n " +
                "LEFT JOIN users u1 ON n.user_id = u1.id " +
                "LEFT JOIN users u2 ON n.from_user_id = u2.id " +
                "WHERE n.user_id = ? AND n.is_read = false " +
                "ORDER BY n.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setUserId(rs.getInt("user_id"));
                notification.setFromUserId(rs.getInt("from_user_id"));
                notification.setMessage(rs.getString("message"));
                notification.setType(rs.getString("type"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notification.setRead(rs.getBoolean("is_read"));

                // Дополнительная информация (опционально)
                notification.setUserLogin(rs.getString("user_login"));
                notification.setFromUserLogin(rs.getString("from_user_login"));

                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    // Получение всех уведомлений пользователя (с историей)
    public static ObservableList<Notification> getAllNotifications(int userId) {
        ObservableList<Notification> notifications = FXCollections.observableArrayList();
        createTableIfNotExists();

        String sql = "SELECT n.*, u1.login as user_login, u2.login as from_user_login " +
                "FROM notifications n " +
                "LEFT JOIN users u1 ON n.user_id = u1.id " +
                "LEFT JOIN users u2 ON n.from_user_id = u2.id " +
                "WHERE n.user_id = ? " +
                "ORDER BY n.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setUserId(rs.getInt("user_id"));
                notification.setFromUserId(rs.getInt("from_user_id"));
                notification.setMessage(rs.getString("message"));
                notification.setType(rs.getString("type"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notification.setRead(rs.getBoolean("is_read"));
                notification.setUserLogin(rs.getString("user_login"));
                notification.setFromUserLogin(rs.getString("from_user_login"));

                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    // Пометить уведомление как прочитанное
    public static boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Пометить все уведомления пользователя как прочитанные
    public static boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Отправка уведомления всем онлайн пользователям (рассылка)
    public static boolean broadcastNotification(int fromUserId, String message, String type) {
        List<Integer> activeUsers = SessionDAO.getActiveUsers();
        boolean allSuccess = true;

        for (Integer userId : activeUsers) {
            if (userId != fromUserId) { // Не отправляем себе
                boolean success = createNotification(userId, fromUserId, message, type);
                if (!success) {
                    allSuccess = false;
                }
            }
        }

        return allSuccess;
    }

    // Удаление уведомления
    public static boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удаление всех уведомлений пользователя
    public static boolean deleteAllNotifications(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение количества непрочитанных уведомлений
    public static int getUnreadCount(int userId) {
        createTableIfNotExists();

        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Получение уведомления по ID
    public static Notification getNotificationById(int notificationId) {
        createTableIfNotExists();

        String sql = "SELECT n.*, u1.login as user_login, u2.login as from_user_login " +
                "FROM notifications n " +
                "LEFT JOIN users u1 ON n.user_id = u1.id " +
                "LEFT JOIN users u2 ON n.from_user_id = u2.id " +
                "WHERE n.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setUserId(rs.getInt("user_id"));
                notification.setFromUserId(rs.getInt("from_user_id"));
                notification.setMessage(rs.getString("message"));
                notification.setType(rs.getString("type"));
                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                notification.setRead(rs.getBoolean("is_read"));
                notification.setUserLogin(rs.getString("user_login"));
                notification.setFromUserLogin(rs.getString("from_user_login"));

                return notification;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Обновление существующего уведомления
    public static boolean updateNotification(int notificationId, String message, String type) {
        String sql = "UPDATE notifications SET message = ?, type = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message);
            stmt.setString(2, type);
            stmt.setInt(3, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Создание таблицы сессий (если используется SessionDAO)
    public static void createSessionsTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS user_sessions (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id INT NOT NULL, " +
                "ip_address VARCHAR(45), " +
                "login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "last_activity DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "logout_time DATETIME NULL, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}