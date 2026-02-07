package com.medicareplus.ui;

import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.model.Doctor;

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
import java.util.List;

public class DoctorManagementFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public DoctorManagementFrame() {
        setTitle("MediCare Plus - Manage Doctors");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root background (soft gradient like dashboard)
        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        // ---------- Header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Doctor Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 25));

        JLabel subtitle = new JLabel("Manage doctors, specialties and availability");
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
        txtSearch.setPreferredSize(new Dimension(280, 36));
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

        // Buttons row (top of card)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        ModernButton btnAdd = new ModernButton("Add Doctor");
        ModernButton btnEdit = new ModernButton("Edit Doctor");
        ModernButton btnDelete = new ModernButton("Delete Doctor");
        ModernButton btnRefresh = new ModernButton("Refresh");

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);

        card.add(actions, BorderLayout.NORTH);

        // Table model
        model = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "Specialty", "Phone", "Email", "Available Days", "Available Time"},
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

        // sorter (search filter)
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Actions
        btnAdd.addActionListener(e -> addDoctor());
        btnEdit.addActionListener(e -> editSelectedDoctor());
        btnDelete.addActionListener(e -> deleteSelectedDoctor());
        btnRefresh.addActionListener(e -> loadDoctors());

        // Load data
        loadDoctors();
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadDoctors() {
        model.setRowCount(0);
        List<Doctor> doctors = doctorDAO.getAllDoctors();

        for (Doctor d : doctors) {
            model.addRow(new Object[]{
                    d.getDoctorId(),
                    d.getFullName(),
                    d.getSpecialty(),
                    d.getPhone(),
                    d.getEmail(),
                    d.getAvailableDays(),
                    d.getAvailableTime()
            });
        }
    }

    private void addDoctor() {

        JTextField fullName = new JTextField();
        JTextField specialty = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField availableDays = new JTextField();   // example: Mon,Wed,Fri
        JTextField availableTime = new JTextField();   // example: 09:00-12:00

        Object[] message = {
                "Full Name:", fullName,
                "Specialty:", specialty,
                "Phone:", phone,
                "Email:", email,
                "Available Days (ex: Mon,Wed,Fri):", availableDays,
                "Available Time (ex: 09:00-12:00):", availableTime
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Add Doctor", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            if (fullName.getText().trim().isEmpty() || specialty.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name and Specialty are required!");
                return;
            }

            Doctor d = new Doctor(
                    fullName.getText().trim(),
                    specialty.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    availableDays.getText().trim(),
                    availableTime.getText().trim()
            );

            boolean success = doctorDAO.addDoctor(d);

            JOptionPane.showMessageDialog(this,
                    success ? "Doctor added successfully!" : "Failed to add doctor!");

            if (success) loadDoctors();
        }
    }

    private void editSelectedDoctor() {

        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor row to edit!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        JTextField fullName = new JTextField(model.getValueAt(row, 1).toString());
        JTextField specialty = new JTextField(model.getValueAt(row, 2).toString());
        JTextField phone = new JTextField(model.getValueAt(row, 3).toString());
        JTextField email = new JTextField(model.getValueAt(row, 4).toString());
        JTextField availableDays = new JTextField(model.getValueAt(row, 5).toString());
        JTextField availableTime = new JTextField(model.getValueAt(row, 6).toString());

        Object[] message = {
                "Full Name:", fullName,
                "Specialty:", specialty,
                "Phone:", phone,
                "Email:", email,
                "Available Days:", availableDays,
                "Available Time:", availableTime
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Edit Doctor", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            if (fullName.getText().trim().isEmpty() || specialty.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name and Specialty are required!");
                return;
            }

            Doctor updated = new Doctor(
                    id,
                    fullName.getText().trim(),
                    specialty.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    availableDays.getText().trim(),
                    availableTime.getText().trim()
            );

            boolean success = doctorDAO.updateDoctor(updated);

            JOptionPane.showMessageDialog(this,
                    success ? "Doctor updated successfully!" : "Failed to update doctor!");

            if (success) loadDoctors();
        }
    }

    private void deleteSelectedDoctor() {

        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor row to delete!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete doctor: " + name + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = doctorDAO.deleteDoctor(id);

            JOptionPane.showMessageDialog(this,
                    success ? "Doctor deleted successfully!" : "Failed to delete doctor!");

            if (success) loadDoctors();
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
            setPreferredSize(new Dimension(140, 38));

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
