package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Нечеткий поиск услуг (с 5 символов)
    public static List<Service> fuzzySearchServices(String query) {
        List<Service> services = new ArrayList<>();

        if (query.length() < 5) {
            return services;
        }

        String sql = "SELECT * FROM services WHERE archived = 0 AND (" +
                "  name LIKE ? OR " +
                "  name LIKE ? OR " +
                "  code LIKE ?" +
                ") " +
                "ORDER BY " +
                "  CASE WHEN name LIKE ? THEN 1 " +
                "       WHEN name LIKE ? THEN 2 " +
                "       WHEN code LIKE ? THEN 3 " +
                "       ELSE 4 " +
                "  END " +
                "LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String exactStart = query + "%";
            String contains = "%" + query + "%";

            stmt.setString(1, exactStart);
            stmt.setString(2, contains);
            stmt.setString(3, contains);
            stmt.setString(4, exactStart);
            stmt.setString(5, contains);
            stmt.setString(6, contains);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }

    // Получение услуги по ID
    public static Service getServiceById(int serviceId) {
        String sql = "SELECT * FROM services WHERE id = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                return service;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Получение услуги по названию
    public static Service getServiceByName(String serviceName) {
        String sql = "SELECT * FROM services WHERE name = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serviceName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                return service;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Получение всех услуг
    public static ObservableList<Service> getAllServices() {
        ObservableList<Service> services = FXCollections.observableArrayList();
        String sql = "SELECT * FROM services WHERE archived = 0 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }

    // Получение услуг по списку ID
    public static List<Service> getServicesByIds(List<Integer> serviceIds) {
        List<Service> services = new ArrayList<>();

        if (serviceIds.isEmpty()) {
            return services;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM services WHERE id IN (");
        for (int i = 0; i < serviceIds.size(); i++) {
            sql.append("?");
            if (i < serviceIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") AND archived = 0");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < serviceIds.size(); i++) {
                stmt.setInt(i + 1, serviceIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }


    public static Service getServiceByCode(String code) {
        String sql = "SELECT * FROM services WHERE code = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCost(rs.getDouble("cost"));
                service.setCode(rs.getString("code"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));
                return service;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Service> getServicesByUtilizer(String utilizerName) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.* FROM services s " +
                "JOIN service_utilizers su ON s.code = su.service_code " +
                "WHERE su.utilizer_name = ? AND s.archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setCode(rs.getString("code"));
                service.setCost(rs.getDouble("cost"));
                service.setDurationDays(rs.getInt("duration_days"));
                service.setDeviationAvg(rs.getDouble("deviation_avg"));
                service.setArchived(rs.getBoolean("archived"));

                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return services;
    }
}