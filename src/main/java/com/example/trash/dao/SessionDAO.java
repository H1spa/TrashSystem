package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Session;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    // === ОБНОВЛЕННЫЙ МЕТОД: Теперь возвращает ID созданной сессии ===
    public static int createSession(int userId, String ipAddress) {
        String sql = "INSERT INTO user_sessions (user_id, ip_address, login_time, last_activity, is_active) " +
                "VALUES (?, ?, NOW(), NOW(), 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();

            // Получаем ID созданной сессии
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // === НОВЫЙ МЕТОД: Получение всех активных сессий пользователя ===
    public static List<Session> getActiveSessionsByUser(int userId) {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Session session = new Session();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setIpAddress(rs.getString("ip_address"));
                session.setLoginTime(rs.getTimestamp("login_time").toLocalDateTime());
                session.setLastActivity(rs.getTimestamp("last_activity").toLocalDateTime());
                session.setLogoutTime(rs.getTimestamp("logout_time") != null ?
                        rs.getTimestamp("logout_time").toLocalDateTime() : null);
                session.setActive(rs.getBoolean("is_active"));
                sessions.add(session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessions;
    }

    // === НОВЫЙ МЕТОД: Завершение конкретной сессии по ID ===
    public static boolean endSessionById(int sessionId) {
        String sql = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // === СТАРЫЕ МЕТОДЫ (остаются для совместимости) ===

    public static Session getActiveSession(int userId) {
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND is_active = 1 ORDER BY login_time DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Session session = new Session();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setIpAddress(rs.getString("ip_address"));
                session.setLoginTime(rs.getTimestamp("login_time").toLocalDateTime());
                session.setLastActivity(rs.getTimestamp("last_activity").toLocalDateTime());
                session.setLogoutTime(rs.getTimestamp("logout_time") != null ?
                        rs.getTimestamp("logout_time").toLocalDateTime() : null);
                session.setActive(rs.getBoolean("is_active"));
                return session;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isSessionActive(int userId) {
        Session session = getActiveSession(userId);
        return session != null && session.isActive() && !session.isExpired();
    }

    public static boolean endSession(int userId) {
        String sql = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean forceEndSession(int userId, int adminId, String reason) {
        boolean ended = endSession(userId);

        if (ended) {
            String message = "Вы были отключены от системы администратором. Причина: " + reason;
            NotificationDAO.createNotification(userId, adminId, message, "DISCONNECT");
        }

        return ended;
    }

    public static boolean updateLastActivity(int userId) {
        String sql = "UPDATE user_sessions SET last_activity = NOW() WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Integer> getActiveUsers() {
        List<Integer> activeUsers = new ArrayList<>();
        String sql = "SELECT DISTINCT user_id FROM user_sessions WHERE is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                activeUsers.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activeUsers;
    }

    public static List<Session> getUserSessions(int userId) {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? ORDER BY login_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Session session = new Session();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setIpAddress(rs.getString("ip_address"));
                session.setLoginTime(rs.getTimestamp("login_time").toLocalDateTime());
                session.setLastActivity(rs.getTimestamp("last_activity").toLocalDateTime());
                session.setLogoutTime(rs.getTimestamp("logout_time") != null ?
                        rs.getTimestamp("logout_time").toLocalDateTime() : null);
                session.setActive(rs.getBoolean("is_active"));
                sessions.add(session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    public static boolean isUserSessionActive(int userId) {
        String query = "SELECT COUNT(*) as active_sessions FROM user_sessions WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("active_sessions") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void cleanupStaleSessions(int timeoutMinutes) {
        String query = "UPDATE user_sessions SET is_active = 0, logout_time = NOW() WHERE is_active = 1 AND last_activity < DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, timeoutMinutes);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}