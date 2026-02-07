package com.medicareplus.dao;

import com.medicareplus.db.DBConnection;
import com.medicareplus.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // 1) Add Doctor
    public boolean addDoctor(Doctor doctor) {

        String sql = "INSERT INTO doctors(full_name, specialty, phone, email, available_days, available_time) " +
                "VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialty());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getAvailableDays());
            ps.setString(6, doctor.getAvailableTime());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2) Get All Doctors
    public List<Doctor> getAllDoctors() {

        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT doctor_id, full_name, specialty, phone, email, available_days, available_time FROM doctors";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Doctor d = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("specialty"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("available_days"),
                        rs.getString("available_time")
                );
                list.add(d);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 3) Update Doctor
    public boolean updateDoctor(Doctor doctor) {

        String sql = "UPDATE doctors SET full_name=?, specialty=?, phone=?, email=?, " +
                "available_days=?, available_time=? WHERE doctor_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialty());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setString(5, doctor.getAvailableDays());
            ps.setString(6, doctor.getAvailableTime());
            ps.setInt(7, doctor.getDoctorId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4) Delete Doctor
    public boolean deleteDoctor(int doctorId) {

        String sql = "DELETE FROM doctors WHERE doctor_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
