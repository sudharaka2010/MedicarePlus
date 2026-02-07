package com.medicareplus.ui;

import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Patient;

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

public class PatientManagementFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private final PatientDAO patientDAO = new PatientDAO();

    // ✅ Search
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;

    public PatientManagementFrame() {

        setTitle("MediCare Plus - Manage Patients");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        // -------- Header + Search --------
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);

        JLabel title = new JLabel("Patient Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 25));

        JLabel subtitle = new JLabel("Manage patients and advance payments");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(80, 80, 80));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        // Search box (right)
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtSearch.setToolTipText("Search by ID, name, NIC, phone or email");

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 235)));
        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(searchBox);

        header.add(titleBox, BorderLayout.WEST);
        header.add(searchWrap, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // -------- Card --------
        RoundedPanel card = new RoundedPanel(22);
        card.setLayout(new BorderLayout(12, 12));
        card.setBackground(new Color(255, 255, 255, 210));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Buttons top
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);

        ModernButton btnAdd = new ModernButton("Add Patient");
        ModernButton btnEdit = new ModernButton("Edit Patient");
        ModernButton btnDelete = new ModernButton("Delete Patient");
        ModernButton btnRefresh = new ModernButton("Refresh");

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);

        card.add(actions, BorderLayout.NORTH);

        // Table model
        model = new DefaultTableModel(
                new Object[]{"ID", "Full Name", "NIC", "Phone", "Email", "Address", "Medical History", "Advance Paid"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 232, 255));
        table.setSelectionForeground(new Color(20, 20, 20));
        table.setGridColor(new Color(230, 233, 240));
        table.setShowVerticalLines(false);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(245, 246, 250));
        th.setForeground(new Color(40, 40, 40));

        // ✅ Row sorter for search
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        card.add(sp, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        // -------- Actions --------
        btnAdd.addActionListener(e -> addPatient());
        btnEdit.addActionListener(e -> editPatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnRefresh.addActionListener(e -> loadPatients());

        // ✅ Live Search
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void search() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { search(); }
            @Override public void removeUpdate(DocumentEvent e) { search(); }
            @Override public void changedUpdate(DocumentEvent e) { search(); }
        });

        loadPatients();
    }

    private void loadPatients() {
        model.setRowCount(0);
        List<Patient> patients = patientDAO.getAllPatients();

        for (Patient p : patients) {
            double advance = patientDAO.getAdvanceByPatientId(p.getPatientId());

            model.addRow(new Object[]{
                    p.getPatientId(),
                    p.getFullName(),
                    p.getNic(),
                    p.getPhone(),
                    p.getEmail(),
                    p.getAddress(),
                    p.getMedicalHistory(),
                    String.format("%.2f", advance)
            });
        }
    }

    private void addPatient() {

        JTextField fullName = new JTextField();
        JTextField nic = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField address = new JTextField();
        JTextField medicalHistory = new JTextField();
        JTextField advanceField = new JTextField("0");

        Object[] message = {
                "Full Name:", fullName,
                "NIC:", nic,
                "Phone:", phone,
                "Email:", email,
                "Address:", address,
                "Medical History:", medicalHistory,
                "Advance Payment:", advanceField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Add Patient", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            if (fullName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full Name is required!");
                return;
            }

            double advance = parseDoubleSafe(advanceField.getText());

            Patient p = new Patient(
                    fullName.getText().trim(),
                    nic.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    address.getText().trim(),
                    medicalHistory.getText().trim()
            );

            boolean success = patientDAO.addPatient(p);

            if (success) {
                List<Patient> list = patientDAO.getAllPatients();
                int lastId = list.get(list.size() - 1).getPatientId();
                patientDAO.updateAdvanceByPatientId(lastId, advance);
            }

            JOptionPane.showMessageDialog(this, success ? "Patient added!" : "Failed to add patient!");
            if (success) loadPatients();
        }
    }

    private void editPatient() {

        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        JTextField fullName = new JTextField(model.getValueAt(row, 1).toString());
        JTextField nic = new JTextField(model.getValueAt(row, 2).toString());
        JTextField phone = new JTextField(model.getValueAt(row, 3).toString());
        JTextField email = new JTextField(model.getValueAt(row, 4).toString());
        JTextField address = new JTextField(model.getValueAt(row, 5).toString());
        JTextField medicalHistory = new JTextField(model.getValueAt(row, 6).toString());
        JTextField advanceField = new JTextField(model.getValueAt(row, 7).toString());

        Object[] message = {
                "Full Name:", fullName,
                "NIC:", nic,
                "Phone:", phone,
                "Email:", email,
                "Address:", address,
                "Medical History:", medicalHistory,
                "Advance Payment:", advanceField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Edit Patient", JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {

            double advance = parseDoubleSafe(advanceField.getText());

            Patient updated = new Patient(
                    id,
                    fullName.getText().trim(),
                    nic.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    address.getText().trim(),
                    medicalHistory.getText().trim()
            );

            boolean success = patientDAO.updatePatient(updated);
            if (success) patientDAO.updateAdvanceByPatientId(id, advance);

            JOptionPane.showMessageDialog(this, success ? "Patient updated!" : "Update failed!");
            if (success) loadPatients();
        }
    }

    private void deletePatient() {

        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String name = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete patient: " + name + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = patientDAO.deletePatient(id);
            JOptionPane.showMessageDialog(this, success ? "Patient deleted!" : "Delete failed!");
            if (success) loadPatients();
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            if (value == null) return 0;
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    // ---------- UI Helpers ----------

    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(245, 246, 250),
                    0, getHeight(), new Color(235, 238, 245)
            );
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
            setPreferredSize(new Dimension(160, 40));

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
