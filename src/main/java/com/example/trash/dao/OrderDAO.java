package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Order;
import com.example.trash.model.Service;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class OrderDAO {

    public static List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE archived = 0 ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public static List<Order> getTodayOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders " +
                "WHERE DATE(created_at) = CURDATE() AND archived = 0 " +
                "ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении сегодняшних заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public static List<Order> getMonthOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders " +
                "WHERE MONTH(created_at) = MONTH(CURDATE()) " +
                "AND YEAR(created_at) = YEAR(CURDATE()) " +
                "AND archived = 0 " +
                "ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении месячных заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    public static Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении заказа по ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static Order getOrderByNumber(int orderNumber) {
        String sql = "SELECT * FROM orders WHERE order_number = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOrder(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении заказа по номеру: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static List<Service> getOrderServices(int orderId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.* FROM services s " +
                "JOIN order_services os ON s.id = os.service_id " +
                "WHERE os.order_id = ? AND s.archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
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
            System.err.println("Ошибка при получении услуг заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    public static int createOrder(Order order, List<Integer> serviceIds) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Создаем заказ
            String orderSql = "INSERT INTO orders (client_id, status, order_number, case_code) " +
                    "VALUES (?, ?, ?, ?)";

            int orderNumber = generateOrderNumber();
            String caseCode = generateCaseCode();

            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getClientId());
                orderStmt.setString(2, "Создан");
                orderStmt.setInt(3, orderNumber);
                orderStmt.setString(4, caseCode);

                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);

                    // Добавляем услуги
                    String serviceSql = "INSERT INTO order_services (order_id, service_id) VALUES (?, ?)";
                    try (PreparedStatement serviceStmt = conn.prepareStatement(serviceSql)) {
                        for (Integer serviceId : serviceIds) {
                            serviceStmt.setInt(1, orderId);
                            serviceStmt.setInt(2, serviceId);
                            serviceStmt.addBatch();
                        }
                        serviceStmt.executeBatch();
                    }

                    conn.commit();
                    return orderId;
                }
            }

            conn.rollback();
            return -1;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Ошибка при откате транзакции: " + ex.getMessage());
                }
            }
            System.err.println("Ошибка при создании заказа: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
                }
            }
        }
    }

    public static boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении статуса заказа: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static double calculateOrderTotal(int orderId) {
        double total = 0.0;
        String sql = "SELECT SUM(s.cost) as total " +
                "FROM services s " +
                "JOIN order_services os ON s.id = os.service_id " +
                "WHERE os.order_id = ? AND s.archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при расчете суммы заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return total;
    }

    public static Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        String sql = "SELECT " +
                "COUNT(*) as total_orders, " +
                "SUM(CASE WHEN status = 'Создан' THEN 1 ELSE 0 END) as created, " +
                "SUM(CASE WHEN status = 'Выполняется' THEN 1 ELSE 0 END) as in_progress, " +
                "SUM(CASE WHEN status = 'Завершен' THEN 1 ELSE 0 END) as completed, " +
                "SUM(CASE WHEN status = 'Отменен' THEN 1 ELSE 0 END) as cancelled, " +
                "MIN(created_at) as first_order_date, " +
                "MAX(created_at) as last_order_date " +
                "FROM orders WHERE archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.put("total_orders", rs.getInt("total_orders"));
                stats.put("created", rs.getInt("created"));
                stats.put("in_progress", rs.getInt("in_progress"));
                stats.put("completed", rs.getInt("completed"));
                stats.put("cancelled", rs.getInt("cancelled"));
                stats.put("first_order_date", rs.getTimestamp("first_order_date"));
                stats.put("last_order_date", rs.getTimestamp("last_order_date"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return stats;
    }

    private static Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setClientId(rs.getInt("client_id"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getTimestamp("created_at"));

        if (columnExists(rs, "order_number")) {
            order.setOrderNumber(rs.getString("order_number"));
        }
        if (columnExists(rs, "case_code")) {
            order.setCaseCode(rs.getString("case_code"));
        }
        if (columnExists(rs, "duration_days")) {
            order.setDurationDays(rs.getInt("duration_days"));
        }

        return order;
    }

    private static boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static int generateOrderNumber() {
        String sql = "SELECT COALESCE(MAX(order_number), 0) + 1 as next_number FROM orders";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("next_number");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при генерации номера заказа: " + e.getMessage());
            e.printStackTrace();
        }

        return 1;
    }
    public static int getLastOrderNumber() {
        String sql = "SELECT COALESCE(MAX(order_number), 0) as last_number FROM orders";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("last_number");
            }
        } catch (SQLException e) {
            System.err.println("Error getting last order number: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public static boolean isCaseCodeUnique(String caseCode) {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE case_code = ? AND archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caseCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking case code uniqueness: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    public static int createOrder(Order order) {
        // Get the next order number
        int nextOrderNumber = getLastOrderNumber() + 1;

        String sql = "INSERT INTO orders (client_id, status, order_number, case_code, created_at) " +
                "VALUES (?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getClientId());
            stmt.setString(2, order.getStatus() != null ? order.getStatus() : "Создан");
            stmt.setInt(3, nextOrderNumber);
            stmt.setString(4, order.getCaseCode());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);

                    // Insert services if available
                    if (order.getServices() != null && !order.getServices().isEmpty()) {
                        String serviceSql = "INSERT INTO order_services (order_id, service_id) VALUES (?, ?)";
                        try (PreparedStatement serviceStmt = conn.prepareStatement(serviceSql)) {
                            for (Integer serviceId : order.getServices()) {
                                serviceStmt.setInt(1, orderId);
                                serviceStmt.setInt(2, serviceId);
                                serviceStmt.addBatch();
                            }
                            serviceStmt.executeBatch();
                        }
                    }

                    return orderId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    private static String generateCaseCode() {
        String prefix = "CASE";
        String sql = "SELECT COUNT(*) as count FROM orders WHERE DATE(created_at) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int count = rs.getInt("count") + 1;
                LocalDate today = LocalDate.now();
                return String.format("%s-%s-%03d",
                        prefix,
                        today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                        count);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при генерации кода кейса: " + e.getMessage());
            e.printStackTrace();
        }

        return prefix + "-" + LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-001";
    }

    // Получение заказов для утилизатора
    public static List<Order> getOrdersForUtilizer(String utilizerName) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT DISTINCT o.* FROM orders o " +
                "JOIN order_services os ON o.id = os.order_id " +
                "JOIN services s ON os.service_id = s.id " +
                "JOIN service_utilizers su ON s.code = su.service_code " +
                "WHERE su.utilizer_name = ? " +
                "AND o.status IN ('Создан', 'Выполняется') " +
                "AND o.archived = 0 " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении заказов для утилизатора: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    // Поиск заказов по различным критериям
    public static List<Order> searchOrders(String searchTerm, LocalDate startDate, LocalDate endDate, String status) {
        List<Order> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT o.* FROM orders o " +
                        "LEFT JOIN clients c ON o.client_id = c.id " +
                        "WHERE o.archived = 0 "
        );

        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (o.order_number LIKE ? OR o.case_code LIKE ? OR c.fio LIKE ?) ");
            String likeTerm = "%" + searchTerm + "%";
            params.add(likeTerm);
            params.add(likeTerm);
            params.add(likeTerm);
        }

        if (startDate != null) {
            sql.append("AND DATE(o.created_at) >= ? ");
            params.add(java.sql.Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append("AND DATE(o.created_at) <= ? ");
            params.add(java.sql.Date.valueOf(endDate));
        }

        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND o.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY o.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске заказов: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }
}