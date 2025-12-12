package com.example.trash.dao;

import com.example.trash.db.DatabaseConnection;
import com.example.trash.model.Experiment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExperimentDAO {

    public static List<Experiment> getAllExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        String sql = "SELECT * FROM experiments WHERE archived = 0 ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Experiment exp = new Experiment();
                exp.setId(rs.getInt("id"));
                exp.setName(rs.getString("name"));
                exp.setStartDate(rs.getDate("start_date").toLocalDate());

                Date endDate = rs.getDate("end_date");
                if (endDate != null) {
                    exp.setEndDate(endDate.toLocalDate());
                }

                exp.setStatus(rs.getString("status"));
                exp.setResult(rs.getString("result"));
                exp.setNotes(rs.getString("notes"));
                exp.setResearcherName(rs.getString("researcher_name"));
                exp.setConclusions(rs.getString("conclusions"));

                experiments.add(exp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return experiments;
    }

    public static List<Experiment> getCompletedExperiments() {
        List<Experiment> experiments = new ArrayList<>();
        String sql = "SELECT * FROM experiments WHERE archived = 0 AND status = 'Завершен' ORDER BY end_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Experiment exp = new Experiment();
                exp.setId(rs.getInt("id"));
                exp.setName(rs.getString("name"));
                exp.setStartDate(rs.getDate("start_date").toLocalDate());
                exp.setEndDate(rs.getDate("end_date").toLocalDate());
                exp.setStatus(rs.getString("status"));
                exp.setResult(rs.getString("result"));
                exp.setResearcherName(rs.getString("researcher_name"));
                exp.setConclusions(rs.getString("conclusions"));

                experiments.add(exp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return experiments;
    }

    public static int getTotalExperimentsCount() {
        String sql = "SELECT COUNT(*) as count FROM experiments WHERE archived = 0";
        return getCount(sql);
    }

    public static int getActiveExperimentsCount() {
        String sql = "SELECT COUNT(*) as count FROM experiments WHERE archived = 0 AND status = 'Активен'";
        return getCount(sql);
    }

    public static int getCompletedExperimentsCount() {
        String sql = "SELECT COUNT(*) as count FROM experiments WHERE archived = 0 AND status = 'Завершен'";
        return getCount(sql);
    }

    public static double getSuccessRate() {
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN result = 'Успешно' THEN 1 ELSE 0 END) as successful " +
                "FROM experiments WHERE archived = 0 AND status = 'Завершен'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int total = rs.getInt("total");
                int successful = rs.getInt("successful");

                if (total > 0) {
                    return (successful * 100.0) / total;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public static int getAverageDuration() {
        String sql = "SELECT AVG(DATEDIFF(end_date, start_date)) as avg_days " +
                "FROM experiments WHERE archived = 0 AND status = 'Завершен' " +
                "AND end_date IS NOT NULL AND start_date IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("avg_days");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getResearchersCount() {
        String sql = "SELECT COUNT(DISTINCT researcher_name) as count FROM experiments WHERE archived = 0";
        return getCount(sql);
    }

    private static int getCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}