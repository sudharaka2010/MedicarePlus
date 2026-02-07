package com.medicareplus.service;

import com.medicareplus.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportService {

    // Count appointments in a month (YYYY-MM)
    public int countAppointmentsForMonth(String yearMonth) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Appointments per doctor (doctor_id -> count) for a month
    public Map<Integer, Integer> doctorPerformanceForMonth(String yearMonth) {
        Map<Integer, Integer> map = new LinkedHashMap<>();

        String sql = """
                SELECT doctor_id, COUNT(*) AS total
                FROM appointments
                WHERE appointment_date LIKE ?
                GROUP BY doctor_id
                ORDER BY total DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, yearMonth + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                map.put(rs.getInt("doctor_id"), rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
