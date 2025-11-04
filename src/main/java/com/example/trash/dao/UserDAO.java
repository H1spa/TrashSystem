package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static ObservableList<User> getOnlineUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String query = "SELECT user_id, name, last_activity FROM users WHERE last_activity >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getTimestamp("last_activity").toLocalDateTime()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }


    public User findByLoginAndPassword(String login, String password) {
        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"), // или "id", если у вас так называется
                        rs.getString("name"),
                        rs.getString("login"),
                        rs.getString("password"),
                        rs.getString("type") // ИЗМЕНИЛ: position -> type
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}