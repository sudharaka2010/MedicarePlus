package com.medicareplus.dao;

import com.medicareplus.db.DBConnection;
import com.medicareplus.model.Bill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    public boolean addBill(Bill b) {
        String sql = """
                INSERT INTO bills (patient_id, total_amount, advance_used, payable_amount, notes)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, b.getPatientId());
            ps.setDouble(2, b.getTotalAmount());
            ps.setDouble(3, b.getAdvanceUsed());
            ps.setDouble(4, b.getPayableAmount());
            ps.setString(5, b.getNotes());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBillAndApplyAdvance(Bill bill, double remainingAdvance) {
        String insertBill = """
                INSERT INTO bills (patient_id, total_amount, advance_used, payable_amount, notes)
                VALUES (?, ?, ?, ?, ?)
                """;
        String updateAdvance = "UPDATE patients SET advance_paid = ? WHERE patient_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(insertBill)) {
                    ps.setInt(1, bill.getPatientId());
                    ps.setDouble(2, bill.getTotalAmount());
                    ps.setDouble(3, bill.getAdvanceUsed());
                    ps.setDouble(4, bill.getPayableAmount());
                    ps.setString(5, bill.getNotes());
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                if (bill.getAdvanceUsed() > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(updateAdvance)) {
                        ps.setDouble(1, remainingAdvance);
                        ps.setInt(2, bill.getPatientId());
                        if (ps.executeUpdate() != 1) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY bill_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Bill(
                        rs.getInt("bill_id"),
                        rs.getInt("patient_id"),
                        rs.getString("bill_date"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("advance_used"),
                        rs.getDouble("payable_amount"),
                        rs.getString("notes")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
