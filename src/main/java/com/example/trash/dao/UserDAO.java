package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.LoginHistory;
import com.example.trash.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {

    // Добавьте этот метод для получения пользователя по ID
    public static User getUserById(int userId) {
        String sql = "SELECT id, fio, login, type_user_id, archived FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fio"),
                        rs.getString("login"),
                        "", // пароль не возвращаем
                        String.valueOf(rs.getInt("type_user_id"))
                );
                user.setArchived(rs.getBoolean("archived"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Метод для получения онлайн пользователей с мониторингом
    // Метод для получения онлайн пользователей с мониторингом
    public static ObservableList<User> getOnlineUsersWithMonitoring() {
        ObservableList<User> users = FXCollections.observableArrayList();

        // Исправленный запрос - используем правильные условия
        String query = "SELECT " +
                "u.id, " +
                "u.fio as name, " +
                "u.login, " +
                "u.type_user_id, " +
                "u.archived, " +
                "s.ip_address as last_ip, " +
                "s.login_time as session_start, " +
                "s.last_activity as last_activity " +
                "FROM users u " +
                "INNER JOIN user_sessions s ON u.id = s.user_id " + // Используем INNER JOIN
                "WHERE u.archived = 0 " +
                "AND s.is_active = 1 " + // Используем 1 вместо true
                "AND s.last_activity >= DATE_SUB(NOW(), INTERVAL 5 MINUTE) " + // Только активные за последние 5 минут
                "ORDER BY s.last_activity DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("id");
                String name = rs.getString("name");
                String login = rs.getString("login");
                Timestamp lastActivityTs = rs.getTimestamp("last_activity");
                Timestamp sessionStartTs = rs.getTimestamp("session_start");
                String type = String.valueOf(rs.getInt("type_user_id"));
                boolean archived = rs.getBoolean("archived");
                String lastIp = rs.getString("last_ip");

                LocalDateTime lastActivity = lastActivityTs != null ? lastActivityTs.toLocalDateTime() : null;
                LocalDateTime sessionStart = sessionStartTs != null ? sessionStartTs.toLocalDateTime() : null;

                int dailyWorkTimeSeconds = getDailyWorkTime(userId);
                int totalWorkTimeSeconds = getTotalWorkTime(userId);

                User user = new User(
                        userId,
                        name,
                        login,
                        lastActivity,
                        lastIp,
                        sessionStart,
                        dailyWorkTimeSeconds,
                        totalWorkTimeSeconds,
                        archived,
                        null, // blockReason
                        true  // sessionActive = true
                );

                user.setType(type);
                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при получении онлайн пользователей: " + e.getMessage());
        }
        return users;
    }

    private static int getDailyWorkTime(int userId) {
        String query = "SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, login_time, " +
                "COALESCE(logout_time, NOW()))), 0) as total_seconds " +
                "FROM user_sessions " +
                "WHERE user_id = ? " +
                "AND DATE(login_time) = CURDATE()";

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

    private static int getTotalWorkTime(int userId) {
        String query = "SELECT COALESCE(SUM(TIMESTAMPDIFF(SECOND, login_time, " +
                "COALESCE(logout_time, NOW()))), 0) as total_seconds " +
                "FROM user_sessions " +
                "WHERE user_id = ?";

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

    // Метод для получения общего времени работы за все время


    // Обновите метод findByLoginAndPassword для использования хеширования
    // Уберите хеширование из этого метода:
    public User findByLoginAndPassword(String login, String password) {
        String sql = "SELECT id, fio, login, password, type_user_id FROM users " +
                "WHERE login = ? AND password = ? AND archived = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, password);  // Пароль без хеширования
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

    // Хеширование пароля SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для создания пользователя с хешированием пароля
    public static boolean createUser(String fio, String login, String password, String type) {
        String query = "INSERT INTO users (fio, login, password, type_user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fio);
            stmt.setString(2, login);
            stmt.setString(3, password);  // Без хеширования
            stmt.setInt(4, Integer.parseInt(type));
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Обновить пользователя с возможностью смены пароля
    public static boolean updateUserWithPassword(int id, String fio, String login,
                                                 String password, String type, boolean archived) {
        String query;
        PreparedStatement stmt;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (password != null && !password.isEmpty()) {
                String hashedPassword = new UserDAO().hashPassword(password);
                query = "UPDATE users SET fio = ?, login = ?, password = ?, type_user_id = ?, archived = ? WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, fio);
                stmt.setString(2, login);
                stmt.setString(3, hashedPassword);
                stmt.setInt(4, Integer.parseInt(type));
                stmt.setBoolean(5, archived);
                stmt.setInt(6, id);
            } else {
                query = "UPDATE users SET fio = ?, login = ?, type_user_id = ?, archived = ? WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, fio);
                stmt.setString(2, login);
                stmt.setInt(3, Integer.parseInt(type));
                stmt.setBoolean(4, archived);
                stmt.setInt(5, id);
            }
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Обновление времени последней активности
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

    // Блокировка пользователя с причиной
    public static boolean blockUser(int userId, String reason) {
        String query = "UPDATE users SET archived = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            boolean success = stmt.executeUpdate() > 0;

            // Если нужно сохранить причину блокировки, можно добавить в отдельную таблицу
            if (success && reason != null && !reason.trim().isEmpty()) {
                saveBlockReason(userId, reason);
            }

            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Перегруженный метод без причины (для обратной совместимости)
    public static boolean blockUser(int userId) {
        return blockUser(userId, null);
    }

    // Сохранение причины блокировки
    private static void saveBlockReason(int userId, String reason) {
        String query = "INSERT INTO block_history (user_id, reason, blocked_at) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, reason);
            stmt.executeUpdate();
        } catch (Exception e) {
            // Если таблицы нет, просто игнорируем
            System.out.println("Таблица block_history не найдена, причина блокировки не сохранена.");
        }
    }

    public static boolean disconnectUser(int userId) {
        // Завершаем все активные сессии пользователя
        String sessionQuery = "UPDATE user_sessions SET logout_time = NOW(), is_active = 0 " +
                "WHERE user_id = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Завершаем сессии
            PreparedStatement sessionStmt = conn.prepareStatement(sessionQuery);
            sessionStmt.setInt(1, userId);
            sessionStmt.executeUpdate();

            // Обновляем last_login для совместимости
            String userQuery = "UPDATE users SET last_login = DATE_SUB(NOW(), INTERVAL 10 MINUTE) WHERE id = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setInt(1, userId);

            return userStmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение всех пользователей (для управления пользователями)
    public static ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();

        String query = "SELECT id, fio, login, type_user_id, archived, last_login FROM users ORDER BY fio";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fio"),
                        rs.getString("login"),
                        "", // пароль не получаем для безопасности
                        String.valueOf(rs.getInt("type_user_id"))
                );
                user.setArchived(rs.getBoolean("archived"));
                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    // Обновление пользователя (без пароля)
    public static boolean updateUser(int id, String fio, String login, String type, boolean archived) {
        String query = "UPDATE users SET fio = ?, login = ?, type_user_id = ?, archived = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fio);
            stmt.setString(2, login);
            stmt.setInt(3, Integer.parseInt(type));
            stmt.setBoolean(4, archived);
            stmt.setInt(5, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Удаление пользователя
    public static boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // UserDAO.java - добавьте этот метод, если его нет
    public static boolean isUserBlocked(int userId) {
        String sql = "SELECT archived FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("archived");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}