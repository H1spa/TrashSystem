package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    // Нечеткий поиск клиентов (с 5 символов)
    public static List<Client> fuzzySearchClients(String query) {
        List<Client> clients = new ArrayList<>();

        if (query.length() < 5) {
            return clients;
        }

        // Используем алгоритм Elasticsearch-подобного поиска
        String sql = "SELECT c.*, co.name as company_name FROM clients c " +
                "LEFT JOIN compani co ON c.company_id = co.id " +
                "WHERE c.archived = 0 AND (" +
                "  c.fio LIKE ? OR " +
                "  c.fio LIKE ? OR " +
                "  c.phone LIKE ? OR " +
                "  c.email LIKE ? OR " +
                "  co.name LIKE ?" +
                ") " +
                "ORDER BY " +
                "  CASE WHEN c.fio LIKE ? THEN 1 " +
                "       WHEN c.fio LIKE ? THEN 2 " +
                "       WHEN c.phone LIKE ? THEN 3 " +
                "       WHEN c.email LIKE ? THEN 4 " +
                "       WHEN co.name LIKE ? THEN 5 " +
                "       ELSE 6 " +
                "  END " +
                "LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String exactStart = query + "%";
            String contains = "%" + query + "%";

            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, contains);
            }
            for (int i = 6; i <= 10; i++) {
                if (i == 6) stmt.setString(i, exactStart);
                else stmt.setString(i, contains);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setFio(rs.getString("fio"));

                Date birthDate = rs.getDate("birth_date");
                if (birthDate != null) {
                    client.setBirthDate(birthDate.toLocalDate());
                }

                client.setPassportSeries(rs.getString("passport_series"));
                client.setPassportNumber(rs.getString("passport_number"));
                client.setPhone(rs.getString("phone"));
                client.setEmail(rs.getString("email"));
                client.setCompanyId(rs.getInt("company_id"));
                client.setTypeClientId(rs.getInt("type_client_id"));
                client.setArchived(rs.getBoolean("archived"));
                client.setCompanyName(rs.getString("company_name"));

                clients.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    // Добавление нового клиента
    public static boolean addClient(Client client) {
        String sql = "INSERT INTO clients (fio, birth_date, passport_series, passport_number, phone, email, company_id, type_client_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.getFio());

            if (client.getBirthDate() != null) {
                stmt.setDate(2, Date.valueOf(client.getBirthDate()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setString(3, client.getPassportSeries());
            stmt.setString(4, client.getPassportNumber());
            stmt.setString(5, client.getPhone());
            stmt.setString(6, client.getEmail());

            if (client.getCompanyId() > 0) {
                stmt.setInt(7, client.getCompanyId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setInt(8, client.getTypeClientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение клиента по ID
    public static Client getClientById(int clientId) {
        String sql = "SELECT c.*, co.name as company_name FROM clients c " +
                "LEFT JOIN compani co ON c.company_id = co.id " +
                "WHERE c.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setFio(rs.getString("fio"));

                Date birthDate = rs.getDate("birth_date");
                if (birthDate != null) {
                    client.setBirthDate(birthDate.toLocalDate());
                }

                client.setPassportSeries(rs.getString("passport_series"));
                client.setPassportNumber(rs.getString("passport_number"));
                client.setPhone(rs.getString("phone"));
                client.setEmail(rs.getString("email"));
                client.setCompanyId(rs.getInt("company_id"));
                client.setTypeClientId(rs.getInt("type_client_id"));
                client.setArchived(rs.getBoolean("archived"));
                client.setCompanyName(rs.getString("company_name"));

                return client;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Проверка существования клиента
    public static boolean clientExists(String fio, String phone) {
        String sql = "SELECT COUNT(*) as count FROM clients WHERE fio = ? AND phone = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fio);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Получение всех компаний
    public static ObservableList<String> getAllCompanies() {
        ObservableList<String> companies = FXCollections.observableArrayList();
        String sql = "SELECT name FROM compani WHERE archived = 0 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                companies.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return companies;
    }

    // Получение ID компании по названию
    public static int getCompanyIdByName(String companyName) {
        String sql = "SELECT id FROM compani WHERE name = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Создание новой компании
    public static int createCompany(String companyName, String address, String inn) {
        String sql = "INSERT INTO compani (name, address, inn) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, companyName);
            stmt.setString(2, address);
            stmt.setString(3, inn);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}