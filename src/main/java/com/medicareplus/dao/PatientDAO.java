package com.medicareplus.dao;

import com.medicareplus.db.DBConnection;
import com.medicareplus.model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // 1) Add Patient
    public boolean addPatient(Patient patient) {

        String sql = "INSERT INTO patients(full_name, nic, phone, email, address, medical_history) " +
                "VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFullName());
            ps.setString(2, patient.getNic());
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getMedicalHistory());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2) Get All Patients
    public List<Patient> getAllPatients() {

        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, full_name, nic, phone, email, address, medical_history FROM patients";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient p = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("full_name"),
                        rs.getString("nic"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("medical_history")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // 3) Update Patient
    public boolean updatePatient(Patient patient) {

        String sql = "UPDATE patients SET full_name=?, nic=?, phone=?, email=?, address=?, medical_history=? " +
                "WHERE patient_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getFullName());
            ps.setString(2, patient.getNic());
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getAddress());
            ps.setString(6, patient.getMedicalHistory());
            ps.setInt(7, patient.getPatientId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4) Delete Patient
    public boolean deletePatient(int patientId) {

        String sql = "DELETE FROM patients WHERE patient_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------
    // ✅ Billing Upgrade: Advance Pay
    // -------------------------------

    // Get advance amount by patient ID
    public double getAdvanceByPatientId(int patientId) {

        String sql = "SELECT advance_paid FROM patients WHERE patient_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("advance_paid");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Update advance amount (after using it for bill)
    public boolean updateAdvanceByPatientId(int patientId, double newAdvance) {

        String sql = "UPDATE patients SET advance_paid = ? WHERE patient_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newAdvance);
            ps.setInt(2, patientId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
