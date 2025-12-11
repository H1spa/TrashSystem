package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Order;
import com.example.trash.model.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Создание нового заказа
    public static int createOrder(Order order) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement serviceStmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Получаем последний номер заказа
            int lastOrderNumber = getLastOrderNumber();
            int newOrderNumber = lastOrderNumber + 1;

            // Вставка заказа
            String orderSql = "INSERT INTO orders (order_number, client_id, case_code, created_at, status) " +
                    "VALUES (?, ?, ?, NOW(), ?)";

            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, newOrderNumber);
            orderStmt.setInt(2, order.getClientId());
            orderStmt.setString(3, order.getCaseCode());
            orderStmt.setString(4, order.getStatus());

            int affectedRows = orderStmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Создание заказа не удалось");
            }

            // Получаем ID созданного заказа
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Не удалось получить ID заказа");
            }

            // Вставка услуг заказа в таблицу order_services
            if (order.getServices() != null && !order.getServices().isEmpty()) {
                // Создаем таблицу, если ее нет
                createOrderServicesTableIfNotExists(conn);

                String serviceSql = "INSERT INTO order_services (order_id, service_id) VALUES (?, ?)";
                serviceStmt = conn.prepareStatement(serviceSql);

                for (Integer serviceId : order.getServices()) {
                    serviceStmt.setInt(1, orderId);
                    serviceStmt.setInt(2, serviceId);
                    serviceStmt.addBatch();
                }

                serviceStmt.executeBatch();
            }

            conn.commit();

            // Устанавливаем номер заказа
            order.setOrderNumber(newOrderNumber);
            order.setId(orderId);

            return orderId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            try {
                if (serviceStmt != null) serviceStmt.close();
                if (orderStmt != null) orderStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void createOrderServicesTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS order_services (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "order_id INT NOT NULL, " +
                "service_id INT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // Получение последнего номера заказа
    public static int getLastOrderNumber() {
        String sql = "SELECT MAX(order_number) as last_number FROM orders";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("last_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Проверка уникальности кода кейса
    public static boolean isCaseCodeUnique(String caseCode) {
        String sql = "SELECT COUNT(*) as count FROM orders WHERE case_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caseCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    // Получение заказа по ID
    public static Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setOrderNumber(rs.getInt("order_number"));
                order.setClientId(rs.getInt("client_id"));
                order.setCaseCode(rs.getString("case_code"));
                order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                order.setStatus(rs.getString("status"));

                // Получаем услуги заказа
                List<Integer> services = getOrderServices(orderId);
                order.setServices(services);

                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Получение услуг заказа
    private static List<Integer> getOrderServices(int orderId) {
        List<Integer> services = new ArrayList<>();

        try {
            // Сначала пробуем получить из таблицы order_services
            String sql = "SELECT service_id FROM order_services WHERE order_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, orderId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    services.add(rs.getInt("service_id"));
                }
            }
        } catch (SQLException e) {
            // Если таблицы нет, игнорируем
            System.out.println("Таблица order_services не найдена");
        }

        return services;
    }

    // Обновление статуса заказа
    public static boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}