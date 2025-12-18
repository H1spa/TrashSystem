package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.UtilizerProcess;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilizerProcessDAO {

    // Создание нового процесса
    public static String createProcess(UtilizerProcess process) {
        String sql = "INSERT INTO utilizer_processes (" +
                "process_uuid, order_id, order_number, service_id, service_code, " +
                "service_name, utilizer_name, researcher_id, researcher_name, " +
                "status, progress, start_time, utilizer_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";

        String uuid = java.util.UUID.randomUUID().toString();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, uuid);
            stmt.setInt(2, process.getOrderId());
            stmt.setString(3, process.getOrderNumber());
            stmt.setInt(4, process.getServiceId());
            stmt.setString(5, process.getServiceCode());
            stmt.setString(6, process.getServiceName());
            stmt.setString(7, process.getUtilizerName());
            stmt.setInt(8, process.getResearcherId());
            stmt.setString(9, process.getResearcherName());
            stmt.setString(10, "running");
            stmt.setInt(11, 0);
            stmt.setInt(12, process.getUtilizerId());

            stmt.executeUpdate();

            // Обновляем загрузку утилизатора
            updateUtilizerLoad(process.getUtilizerId(), 1);

            return uuid;

        } catch (SQLException e) {
            System.err.println("Ошибка при создании процесса: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Получение активных процессов
    public static List<UtilizerProcess> getActiveProcesses() {
        List<UtilizerProcess> processes = new ArrayList<>();
        String sql = "SELECT * FROM utilizer_processes " +
                "WHERE status IN ('pending', 'running') AND archived = 0 " +
                "ORDER BY start_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                processes.add(mapResultSetToProcess(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении активных процессов: " + e.getMessage());
            e.printStackTrace();
        }
        return processes;
    }

    // Получение всех процессов
    public static List<UtilizerProcess> getAllProcesses() {
        List<UtilizerProcess> processes = new ArrayList<>();
        String sql = "SELECT * FROM utilizer_processes WHERE archived = 0 ORDER BY start_time DESC LIMIT 100";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                processes.add(mapResultSetToProcess(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении всех процессов: " + e.getMessage());
            e.printStackTrace();
        }
        return processes;
    }

    // Получение процессов по утилизатору
    public static List<UtilizerProcess> getProcessesByUtilizer(String utilizerName) {
        List<UtilizerProcess> processes = new ArrayList<>();
        String sql = "SELECT * FROM utilizer_processes " +
                "WHERE utilizer_name = ? AND archived = 0 " +
                "ORDER BY start_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                processes.add(mapResultSetToProcess(rs));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении процессов утилизатора: " + e.getMessage());
            e.printStackTrace();
        }
        return processes;
    }

    // Обновление прогресса процесса
    public static boolean updateProcessProgress(String processUuid, int progress, String status) {
        String sql = "UPDATE utilizer_processes " +
                "SET progress = ?, status = ?, updated_at = NOW() " +
                "WHERE process_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, progress);
            stmt.setString(2, status);
            stmt.setString(3, processUuid);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении прогресса: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Обновление статуса процесса (одобрение/отклонение)
    public static boolean updateProcessStatus(String processUuid, String status, String approverName) {
        String sql = "UPDATE utilizer_processes " +
                "SET status = ?, approver_name = ?, approval_time = NOW(), updated_at = NOW() " +
                "WHERE process_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, approverName);
            stmt.setString(3, processUuid);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении статуса: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Завершение процесса
    public static boolean completeProcess(String processUuid, String utilizerName) {
        String sql = "UPDATE utilizer_processes " +
                "SET status = 'completed', end_time = NOW(), progress = 100, updated_at = NOW() " +
                "WHERE process_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, processUuid);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Получаем ID утилизатора
                Integer utilizerId = getUtilizerIdByName(utilizerName);
                if (utilizerId != null) {
                    // Уменьшаем загрузку утилизатора
                    updateUtilizerLoad(utilizerId, -1);
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Ошибка при завершении процесса: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Получение процесса по UUID
    public static UtilizerProcess getProcessByUuid(String processUuid) {
        String sql = "SELECT * FROM utilizer_processes WHERE process_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, processUuid);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProcess(rs);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении процесса: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Обновление результатов процесса
    public static boolean updateProcessResults(String processUuid,
                                               double density, double dispersion,
                                               double mercuryConcentration, double creosolConcentration,
                                               double potassiumHydroxideConcentration, double heavyMetalsConcentration,
                                               boolean hasDeviationWarning, String anomalyDescription) {

        // Сначала обновляем основную таблицу
        String sql1 = "UPDATE utilizer_processes " +
                "SET density = ?, dispersion = ?, mercury_concentration = ?, " +
                "creosol_concentration = ?, potassium_hydroxide_concentration = ?, " +
                "heavy_metals_concentration = ?, has_deviation_warning = ?, " +
                "anomaly_description = ?, updated_at = NOW() " +
                "WHERE process_uuid = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Обновляем основную таблицу
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1)) {
                stmt1.setDouble(1, density);
                stmt1.setDouble(2, dispersion);
                stmt1.setDouble(3, mercuryConcentration);
                stmt1.setDouble(4, creosolConcentration);
                stmt1.setDouble(5, potassiumHydroxideConcentration);
                stmt1.setDouble(6, heavyMetalsConcentration);
                stmt1.setBoolean(7, hasDeviationWarning);
                stmt1.setString(8, anomalyDescription);
                stmt1.setString(9, processUuid);

                stmt1.executeUpdate();
            }

            // Также добавляем запись в таблицу результатов
            String sql2 = "INSERT INTO utilizer_results (" +
                    "process_uuid, density, dispersion, mercury_concentration, " +
                    "creosol_concentration, potassium_hydroxide_concentration, " +
                    "heavy_metals_concentration, has_deviation_warning, anomaly_description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "density = VALUES(density), dispersion = VALUES(dispersion), " +
                    "mercury_concentration = VALUES(mercury_concentration), " +
                    "creosol_concentration = VALUES(creosol_concentration), " +
                    "potassium_hydroxide_concentration = VALUES(potassium_hydroxide_concentration), " +
                    "heavy_metals_concentration = VALUES(heavy_metals_concentration), " +
                    "has_deviation_warning = VALUES(has_deviation_warning), " +
                    "anomaly_description = VALUES(anomaly_description)";

            try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                stmt2.setString(1, processUuid);
                stmt2.setDouble(2, density);
                stmt2.setDouble(3, dispersion);
                stmt2.setDouble(4, mercuryConcentration);
                stmt2.setDouble(5, creosolConcentration);
                stmt2.setDouble(6, potassiumHydroxideConcentration);
                stmt2.setDouble(7, heavyMetalsConcentration);
                stmt2.setBoolean(8, hasDeviationWarning);
                stmt2.setString(9, anomalyDescription);

                stmt2.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении результатов: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Вспомогательные методы
    private static void updateUtilizerLoad(int utilizerId, int change) {
        String sql = "UPDATE utilizator SET current_load = current_load + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, change);
            stmt.setInt(2, utilizerId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении загрузки утилизатора: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Integer getUtilizerIdByName(String utilizerName) {
        String sql = "SELECT id FROM utilizator WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilizerName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID утилизатора: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static UtilizerProcess mapResultSetToProcess(ResultSet rs) throws SQLException {
        UtilizerProcess process = new UtilizerProcess();
        process.setId(rs.getInt("id"));
        process.setProcessUuid(rs.getString("process_uuid"));
        process.setOrderId(rs.getInt("order_id"));
        process.setOrderNumber(rs.getString("order_number"));
        process.setServiceId(rs.getInt("service_id"));
        process.setServiceCode(rs.getString("service_code"));
        process.setServiceName(rs.getString("service_name"));
        process.setUtilizerName(rs.getString("utilizer_name"));
        process.setResearcherName(rs.getString("researcher_name"));
        process.setProgress(rs.getInt("progress"));
        process.setStatus(rs.getString("status"));
        process.setStartTime(rs.getTimestamp("start_time"));

        // Дополнительные поля, если они есть
        if (columnExists(rs, "end_time")) {
            process.setActualCompletionTime(rs.getTimestamp("end_time"));
        }
        if (columnExists(rs, "utilizer_id")) {
            process.setUtilizerId(rs.getInt("utilizer_id"));
        }

        return process;
    }

    private static boolean columnExists(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Получение статистики по процессам
    public static ProcessStatistics getProcessStatistics() {
        ProcessStatistics stats = new ProcessStatistics();
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'running' THEN 1 ELSE 0 END) as running, " +
                "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed, " +
                "SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as failed, " +
                "SUM(CASE WHEN has_deviation_warning = 1 THEN 1 ELSE 0 END) as with_warnings " +
                "FROM utilizer_processes WHERE archived = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.setTotal(rs.getInt("total"));
                stats.setRunning(rs.getInt("running"));
                stats.setCompleted(rs.getInt("completed"));
                stats.setFailed(rs.getInt("failed"));
                stats.setWithWarnings(rs.getInt("with_warnings"));
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении статистики процессов: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    // Класс для статистики процессов
    public static class ProcessStatistics {
        private int total;
        private int running;
        private int completed;
        private int failed;
        private int withWarnings;

        // Геттеры и сеттеры
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getRunning() { return running; }
        public void setRunning(int running) { this.running = running; }

        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public int getWithWarnings() { return withWarnings; }
        public void setWithWarnings(int withWarnings) { this.withWarnings = withWarnings; }
    }
}