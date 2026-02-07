package com.medicareplus.ui;

import com.medicareplus.dao.AppointmentDAO;
import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.dao.PatientDAO;
import com.medicareplus.dao.NotificationDAO;
import com.medicareplus.model.Appointment;
import com.medicareplus.model.Doctor;
import com.medicareplus.model.Patient;
import com.medicareplus.model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AppointmentManagementFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public AppointmentManagementFrame() {

        setTitle("MediCare Plus - Appointments");
        setSize(1150, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root background (soft gradient like dashboard)
        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ---------- Header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Appointment Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 25));

        JLabel subtitle = new JLabel("Schedule appointments, update status, and manage records");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(80, 80, 80));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);

        // Search box (right)
        JPanel searchBox = new JPanel(new BorderLayout(8, 8));
        searchBox.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 222)),
                new EmptyBorder(6, 10, 6, 10)
        ));

        searchBox.add(searchLbl, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        header.add(searchBox, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ---------- Center glass card ----------
        RoundedPanel card = new RoundedPanel(22);
        card.setLayout(new BorderLayout(14, 14));
        card.setBackground(new Color(255, 255, 255, 210));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Buttons row
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        ModernButton btnAdd = new ModernButton("Schedule Appointment");
        ModernButton btnStatus = new ModernButton("Update Status");
        ModernButton btnDelete = new ModernButton("Delete Appointment");
        ModernButton btnRefresh = new ModernButton("Refresh");

        actions.add(btnAdd);
        actions.add(btnStatus);
        actions.add(btnDelete);
        actions.add(btnRefresh);

        card.add(actions, BorderLayout.NORTH);

        // Table model (non-editable)
        model = new DefaultTableModel(
                new Object[]{"ID", "Patient ID", "Doctor ID", "Date", "Time", "Status", "Notes"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 232, 255));
        table.setSelectionForeground(new Color(20, 20, 20));
        table.setGridColor(new Color(230, 233, 240));
        table.setShowVerticalLines(false);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(245, 246, 250));
        th.setForeground(new Color(40, 40, 40));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        card.add(sp, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        // Search filter
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Actions
        btnAdd.addActionListener(e -> addAppointment());
        btnStatus.addActionListener(e -> updateStatus());
        btnDelete.addActionListener(e -> deleteAppointment());
        btnRefresh.addActionListener(e -> loadAppointments());

        loadAppointments();
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadAppointments() {
        model.setRowCount(0);

        for (Appointment a : appointmentDAO.getAllAppointments()) {
            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getPatientId(),
                    a.getDoctorId(),
                    a.getAppointmentDate(),
                    a.getAppointmentTime(),
                    a.getStatus(),
                    a.getNotes()
            });
        }
    }

    private void addAppointment() {

        JComboBox<Integer> patientBox = new JComboBox<>();
        JComboBox<Integer> doctorBox = new JComboBox<>();

        for (Patient p : patientDAO.getAllPatients()) {
            patientBox.addItem(p.getPatientId());
        }
        for (Doctor d : doctorDAO.getAllDoctors()) {
            doctorBox.addItem(d.getDoctorId());
        }

        JTextField date = new JTextField("2025-12-12");
        JTextField time = new JTextField("10:30");
        JTextField notes = new JTextField();

        Object[] message = {
                "Patient ID:", patientBox,
                "Doctor ID:", doctorBox,
                "Date (YYYY-MM-DD):", date,
                "Time (HH:MM):", time,
                "Notes:", notes
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Schedule Appointment",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            Appointment a = new Appointment(
                    (int) patientBox.getSelectedItem(),
                    (int) doctorBox.getSelectedItem(),
                    date.getText().trim(),
                    time.getText().trim(),
                    "Scheduled",
                    notes.getText().trim()
            );

            boolean success = appointmentDAO.addAppointment(a);

            JOptionPane.showMessageDialog(
                    this,
                    success ? "Appointment scheduled!" : "Failed to schedule appointment!"
            );

            if (success) {

                // 🔔 Notify Patient
                notificationDAO.addNotification(
                        new Notification(
                                "Patient",
                                a.getPatientId(),
                                "Your appointment is scheduled on " +
                                        a.getAppointmentDate() + " at " +
                                        a.getAppointmentTime() +
                                        " (Doctor ID: " + a.getDoctorId() + ")"
                        )
                );

                // 🔔 Notify Doctor
                notificationDAO.addNotification(
                        new Notification(
                                "Doctor",
                                a.getDoctorId(),
                                "New appointment scheduled on " +
                                        a.getAppointmentDate() + " at " +
                                        a.getAppointmentTime() +
                                        " (Patient ID: " + a.getPatientId() + ")"
                        )
                );

                loadAppointments();
            }
        }
    }

    private void updateStatus() {

        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        String[] statuses = {"Scheduled", "Completed", "Canceled", "Delayed"};
        String status = (String) JOptionPane.showInputDialog(
                this,
                "Select Status:",
                "Update Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                statuses,
                statuses[0]
        );

        if (status != null) {
            boolean success = appointmentDAO.updateStatus(id, status);

            JOptionPane.showMessageDialog(
                    this,
                    success ? "Status updated!" : "Failed to update status!"
            );

            if (success) loadAppointments();
        }
    }

    private void deleteAppointment() {

        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete this appointment?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = appointmentDAO.deleteAppointment(id);

            JOptionPane.showMessageDialog(
                    this,
                    success ? "Appointment deleted!" : "Failed to delete appointment!"
            );

            if (success) loadAppointments();
        }
    }

    // ---------- UI Helpers ----------

    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            GradientPaint gp = new GradientPaint(0, 0, new Color(245, 246, 250),
                    0, getHeight(), new Color(235, 238, 245));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ModernButton extends JButton {
        private final Color base = new Color(32, 84, 240);
        private final Color hover = new Color(22, 68, 215);
        private final Color pressed = new Color(18, 55, 175);

        public ModernButton(String text) {
            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(170, 38));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean isHover = getModel().isRollover();
            boolean isPressed = getModel().isArmed();

            Color c = isPressed ? pressed : (isHover ? hover : base);

            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(3, 4, getWidth() - 6, getHeight() - 6, 14, 14);

            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 14, 14);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
