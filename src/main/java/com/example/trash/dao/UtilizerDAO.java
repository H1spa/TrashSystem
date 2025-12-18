package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Utilizer;
import java.sql.*;
import java.util.*;

public class UtilizerDAO {

    public static List<Utilizer> getAllUtilizers() {
        List<Utilizer> utilizers = new ArrayList<>();
        String sql = "SELECT * FROM utilizator WHERE archived = 0 ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilizers.add(mapResultSetToUtilizer(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении утилизаторов: " + e.getMessage());
            e.printStackTrace();
        }

        return utilizers;
    }

    public static Utilizer getUtilizerById(int id) {
        String sql = "SELECT * FROM utilizator WHERE id = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilizer(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении утилизатора по ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static Utilizer getUtilizerByName(String name) {
        String sql = "SELECT * FROM utilizator WHERE name = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilizer(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении утилизатора по имени: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateUtilizerStatus(int utilizerId, String status, int currentLoad) {
        String sql = "UPDATE utilizator SET status = ?, current_load = ?, last_status_update = NOW() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, currentLoad);
            stmt.setInt(3, utilizerId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении статуса утилизатора: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, Integer> getUtilizerStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'available' THEN 1 ELSE 0 END) as available, " +
                "SUM(CASE WHEN status = 'busy' THEN 1 ELSE 0 END) as busy, " +
                "SUM(CASE WHEN status = 'full' THEN 1 ELSE 0 END) as full, " +
                "SUM(CASE WHEN status = 'maintenance' THEN 1 ELSE 0 END) as maintenance " +
                "FROM utilizator WHERE archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("available", rs.getInt("available"));
                stats.put("busy", rs.getInt("busy"));
                stats.put("full", rs.getInt("full"));
                stats.put("maintenance", rs.getInt("maintenance"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики утилизаторов: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    public static boolean releaseAllUtilizers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Обновляем статус всех утилизаторов
            String sql1 = "UPDATE utilizator " +
                    "SET status = 'available', " +
                    "    current_load = 0, " +
                    "    last_status_update = NOW() " +
                    "WHERE archived = 0";

            try (Statement stmt1 = conn.createStatement()) {
                int rowsUpdated = stmt1.executeUpdate(sql1);
                System.out.println("Освобождено утилизаторов: " + rowsUpdated);
            }

            // Архивируем все активные процессы
            String sql2 = "UPDATE utilizer_processes " +
                    "SET status = 'completed', " +
                    "    end_time = NOW(), " +
                    "    progress = 100, " +
                    "    archived = 1 " +
                    "WHERE status IN ('running', 'pending') " +
                    "AND archived = 0";

            try (Statement stmt2 = conn.createStatement()) {
                int processesArchived = stmt2.executeUpdate(sql2);
                System.out.println("Архивировано процессов: " + processesArchived);
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Ошибка при освобождении утилизаторов: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static List<Utilizer> getAvailableUtilizers() {
        List<Utilizer> utilizers = new ArrayList<>();
        String sql = "SELECT * FROM utilizator " +
                "WHERE archived = 0 AND current_load < capacity " +
                "AND status != 'maintenance' " +
                "ORDER BY current_load ASC, name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilizers.add(mapResultSetToUtilizer(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении доступных утилизаторов: " + e.getMessage());
            e.printStackTrace();
        }

        return utilizers;
    }

    public static List<String> getServicesForUtilizer(String utilizerName) {
        List<String> services = new ArrayList<>();
        String sql = "SELECT service_code FROM service_utilizers " +
                "WHERE utilizer_name = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                services.add(rs.getString("service_code"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении услуг для утилизатора: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    public static boolean canHandleService(String utilizerName, String serviceCode) {
        String sql = "SELECT COUNT(*) as count FROM service_utilizers " +
                "WHERE utilizer_name = ? AND service_code = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            stmt.setString(2, serviceCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке возможности обработки услуги: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private static Utilizer mapResultSetToUtilizer(ResultSet rs) throws SQLException {
        Utilizer utilizer = new Utilizer();
        utilizer.setId(rs.getInt("id"));
        utilizer.setName(rs.getString("name"));
        utilizer.setDescription(rs.getString("description"));
        utilizer.setCapacity(rs.getInt("capacity"));
        utilizer.setCurrentLoad(rs.getInt("current_load"));
        utilizer.setStatus(rs.getString("status"));
        utilizer.setIpAddress(rs.getString("ip_address"));
        utilizer.setPort(rs.getInt("port"));
        utilizer.setModel(rs.getString("model"));
        utilizer.setManufacturer(rs.getString("manufacturer"));

        // Дополнительные поля
        if (columnExists(rs, "last_maintenance")) {
            utilizer.setLastMaintenance(rs.getDate("last_maintenance"));
        }
        if (columnExists(rs, "next_maintenance")) {
            utilizer.setNextMaintenance(rs.getDate("next_maintenance"));
        }
        if (columnExists(rs, "total_processes")) {
            utilizer.setTotalProcesses(rs.getInt("total_processes"));
        }
        if (columnExists(rs, "successful_processes")) {
            utilizer.setSuccessfulProcesses(rs.getInt("successful_processes"));
        }

        return utilizer;
    }

    private static boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Получение эффективности утилизаторов
    public static List<UtilizerEfficiency> getUtilizerEfficiency() {
        List<UtilizerEfficiency> efficiencyList = new ArrayList<>();

        String sql = "SELECT " +
                "u.name, " +
                "u.capacity, " +
                "u.current_load, " +
                "COUNT(p.id) as total_processes, " +
                "SUM(CASE WHEN p.status = 'completed' THEN 1 ELSE 0 END) as completed_processes, " +
                "AVG(p.progress) as avg_progress " +
                "FROM utilizator u " +
                "LEFT JOIN utilizer_processes p ON u.name = p.utilizer_name AND p.archived = 0 " +
                "WHERE u.archived = 0 " +
                "GROUP BY u.id, u.name, u.capacity, u.current_load " +
                "ORDER BY u.name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UtilizerEfficiency efficiency = new UtilizerEfficiency();
                efficiency.setName(rs.getString("name"));
                efficiency.setCapacity(rs.getInt("capacity"));
                efficiency.setCurrentLoad(rs.getInt("current_load"));
                efficiency.setTotalProcesses(rs.getInt("total_processes"));
                efficiency.setCompletedProcesses(rs.getInt("completed_processes"));
                efficiency.setAvgProgress(rs.getDouble("avg_progress"));
                efficiencyList.add(efficiency);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении эффективности утилизаторов: " + e.getMessage());
            e.printStackTrace();
        }

        return efficiencyList;
    }

    public static class UtilizerEfficiency {
        private String name;
        private int capacity;
        private int currentLoad;
        private int totalProcesses;
        private int completedProcesses;
        private double avgProgress;

        // Геттеры и сеттеры
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getCurrentLoad() { return currentLoad; }
        public void setCurrentLoad(int currentLoad) { this.currentLoad = currentLoad; }

        public int getTotalProcesses() { return totalProcesses; }
        public void setTotalProcesses(int totalProcesses) { this.totalProcesses = totalProcesses; }

        public int getCompletedProcesses() { return completedProcesses; }
        public void setCompletedProcesses(int completedProcesses) { this.completedProcesses = completedProcesses; }

        public double getAvgProgress() { return avgProgress; }
        public void setAvgProgress(double avgProgress) { this.avgProgress = avgProgress; }

        public double getLoadPercentage() {
            return capacity > 0 ? (double) currentLoad / capacity * 100 : 0;
        }

        public double getSuccessRate() {
            return totalProcesses > 0 ? (double) completedProcesses / totalProcesses * 100 : 0;
        }
    }
}