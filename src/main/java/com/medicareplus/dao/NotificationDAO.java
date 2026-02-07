package com.medicareplus.dao;

import com.medicareplus.db.DBConnection;
import com.medicareplus.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // add notification
    public boolean addNotification(Notification n) {

        String sql = "INSERT INTO notifications(receiver_type, receiver_id, message, is_read) VALUES(?,?,?,0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, n.getReceiverType());
            ps.setInt(2, n.getReceiverId());
            ps.setString(3, n.getMessage());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // read all notifications
    public List<Notification> getAllNotifications() {

        List<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, receiver_type, receiver_id, message, is_read FROM notifications ORDER BY notification_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Notification n = new Notification(
                        rs.getInt("notification_id"),
                        rs.getString("receiver_type"),
                        rs.getInt("receiver_id"),
                        rs.getString("message"),
                        rs.getInt("is_read")
                );
                list.add(n);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // mark as read
    public boolean markAsRead(int notificationId) {

        String sql = "UPDATE notifications SET is_read=1 WHERE notification_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
