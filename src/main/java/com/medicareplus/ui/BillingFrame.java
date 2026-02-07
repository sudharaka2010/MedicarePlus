package com.medicareplus.ui;

import com.medicareplus.dao.BillDAO;
import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Bill;
import com.medicareplus.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.List;

public class BillingFrame extends JFrame {

    private final PatientDAO patientDAO = new PatientDAO();
    private final BillDAO billDAO = new BillDAO();

    private JComboBox<PatientItem> patientBox;
    private JLabel lblAdvance;
    private JTextField txtTotal;
    private JCheckBox chkUseAdvance;
    private JLabel lblAdvanceUsed;
    private JLabel lblPayable;
    private JTextArea txtNotes;

    private JTable table;
    private DefaultTableModel model;

    private final DecimalFormat df = new DecimalFormat("#0.00");

    public BillingFrame() {
        setTitle("MediCare Plus - Billing");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        // -------- Header --------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Billing");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 25));

        JLabel subtitle = new JLabel("Create bills, use advance payments, preview and print");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(80, 80, 80));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // -------- Left: Create Bill Panel --------
        RoundedPanel formCard = new RoundedPanel(22);
        formCard.setLayout(new BorderLayout(12, 12));
        formCard.setBackground(new Color(255, 255, 255, 210));
        formCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel formTitle = new JLabel("Create New Bill");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        // Patient dropdown
        form.add(new JLabel("Patient:"), c);
        c.gridx = 1;

        patientBox = new JComboBox<>();
        patientBox.setPreferredSize(new Dimension(320, 34));
        patientBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(patientBox, c);

        // Advance label
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Advance Balance:"), c);
        c.gridx = 1;

        lblAdvance = new JLabel("0.00");
        lblAdvance.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAdvance.setForeground(new Color(22, 120, 60));
        form.add(lblAdvance, c);

        // Total amount
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Total Amount:"), c);
        c.gridx = 1;

        txtTotal = new JTextField();
        txtTotal.setPreferredSize(new Dimension(320, 34));
        txtTotal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 214, 222)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        form.add(txtTotal, c);

        // Use advance checkbox
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Use Advance:"), c);
        c.gridx = 1;

        chkUseAdvance = new JCheckBox("Apply advance to reduce payable");
        chkUseAdvance.setOpaque(false);
        chkUseAdvance.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(chkUseAdvance, c);

        // Advance used
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Advance Used:"), c);
        c.gridx = 1;

        lblAdvanceUsed = new JLabel("0.00");
        lblAdvanceUsed.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAdvanceUsed.setForeground(new Color(32, 84, 240));
        form.add(lblAdvanceUsed, c);

        // Payable
        c.gridy++;
        c.gridx = 0;
        form.add(new JLabel("Payable Amount:"), c);
        c.gridx = 1;

        lblPayable = new JLabel("0.00");
        lblPayable.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPayable.setForeground(new Color(180, 40, 40));
        form.add(lblPayable, c);

        // Notes
        c.gridy++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Notes:"), c);
        c.gridx = 1;

        txtNotes = new JTextArea(4, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        form.add(notesScroll, c);

        formCard.add(form, BorderLayout.CENTER);

        // Buttons bottom
        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formButtons.setOpaque(false);

        ModernButton btnSave = new ModernButton("Save Bill");
        ModernButton btnClear = new ModernButton("Clear");
        formButtons.add(btnSave);
        formButtons.add(btnClear);

        formCard.add(formButtons, BorderLayout.SOUTH);

        // -------- Right: Bills table + actions --------
        RoundedPanel tableCard = new RoundedPanel(22);
        tableCard.setLayout(new BorderLayout(12, 12));
        tableCard.setBackground(new Color(255, 255, 255, 210));
        tableCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel listTitle = new JLabel("Bills");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableCard.add(listTitle, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Bill ID", "Patient ID", "Date", "Total", "Advance Used", "Payable", "Notes"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 240)));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tableButtons.setOpaque(false);

        ModernButton btnRefresh = new ModernButton("Refresh");
        ModernButton btnPreview = new ModernButton("Preview");
        ModernButton btnPrint = new ModernButton("Print");

        tableButtons.add(btnRefresh);
        tableButtons.add(btnPreview);
        tableButtons.add(btnPrint);

        tableCard.add(tableButtons, BorderLayout.SOUTH);

        // -------- Layout split --------
        JPanel center = new JPanel(new GridLayout(1, 2, 18, 18));
        center.setOpaque(false);
        center.add(formCard);
        center.add(tableCard);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);

        // -------- Events --------
        loadPatientsToCombo();
        loadBills();

        patientBox.addActionListener(e -> refreshAdvanceLabel());
        chkUseAdvance.addActionListener(e -> recalc());
        txtTotal.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalc(); }
            @Override public void removeUpdate(DocumentEvent e) { recalc(); }
            @Override public void changedUpdate(DocumentEvent e) { recalc(); }
        });

        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadBills());

        btnSave.addActionListener(e -> saveBill());
        btnPreview.addActionListener(e -> previewSelectedBill());
        btnPrint.addActionListener(e -> printSelectedBill());
    }

    // -----------------------------
    // Data Loading
    // -----------------------------

    private void loadPatientsToCombo() {
        patientBox.removeAllItems();
        List<Patient> patients = patientDAO.getAllPatients();

        for (Patient p : patients) {
            patientBox.addItem(new PatientItem(p.getPatientId(), p.getFullName()));
        }

        if (patientBox.getItemCount() > 0) {
            patientBox.setSelectedIndex(0);
        }
        refreshAdvanceLabel();
        recalc();
    }

    private void loadBills() {
        model.setRowCount(0);
        for (Bill b : billDAO.getAllBills()) {
            model.addRow(new Object[]{
                    b.getBillId(),
                    b.getPatientId(),
                    b.getBillDate(),
                    df.format(b.getTotalAmount()),
                    df.format(b.getAdvanceUsed()),
                    df.format(b.getPayableAmount()),
                    b.getNotes()
            });
        }
    }

    private void refreshAdvanceLabel() {
        PatientItem item = (PatientItem) patientBox.getSelectedItem();
        if (item == null) {
            lblAdvance.setText("0.00");
            return;
        }
        double adv = patientDAO.getAdvanceByPatientId(item.id);
        lblAdvance.setText(df.format(adv));
    }

    // -----------------------------
    // Calculations
    // -----------------------------

    private void recalc() {
        double total = parseDoubleSafe(txtTotal.getText());
        double advance = parseDoubleSafe(lblAdvance.getText());

        double used = 0;
        if (chkUseAdvance.isSelected()) {
            used = Math.min(advance, total);
        }
        double payable = total - used;

        lblAdvanceUsed.setText(df.format(used));
        lblPayable.setText(df.format(payable));
    }

    // -----------------------------
    // Actions
    // -----------------------------

    private void clearForm() {
        txtTotal.setText("");
        chkUseAdvance.setSelected(false);
        txtNotes.setText("");
        recalc();
    }

    private void saveBill() {
        PatientItem item = (PatientItem) patientBox.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No patient selected!");
            return;
        }

        double total = parseDoubleSafe(txtTotal.getText());
        if (total <= 0) {
            JOptionPane.showMessageDialog(this, "Enter a valid total amount!");
            return;
        }

        double advanceBalance = patientDAO.getAdvanceByPatientId(item.id);
        double advanceUsed = 0;

        if (chkUseAdvance.isSelected()) {
            advanceUsed = Math.min(advanceBalance, total);
        }

        double payable = total - advanceUsed;

        Bill bill = new Bill(
                item.id,
                total,
                advanceUsed,
                payable,
                txtNotes.getText().trim()
        );

        boolean success = billDAO.addBill(bill);

        if (success) {
            // reduce patient advance if used
            if (advanceUsed > 0) {
                double newAdvance = advanceBalance - advanceUsed;
                patientDAO.updateAdvanceByPatientId(item.id, newAdvance);
            }

            JOptionPane.showMessageDialog(this, "Bill saved successfully!");
            refreshAdvanceLabel();
            recalc();
            loadBills();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save bill!");
        }
    }

    private void previewSelectedBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        String billId = model.getValueAt(row, 0).toString();
        String patientId = model.getValueAt(row, 1).toString();
        String date = model.getValueAt(row, 2).toString();
        String total = model.getValueAt(row, 3).toString();
        String advUsed = model.getValueAt(row, 4).toString();
        String payable = model.getValueAt(row, 5).toString();
        String notes = String.valueOf(model.getValueAt(row, 6));

        String text = buildBillText(billId, patientId, date, total, advUsed, payable, notes);

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(650, 420));

        JOptionPane.showMessageDialog(this, sp, "Bill Preview", JOptionPane.INFORMATION_MESSAGE);
    }

    private void printSelectedBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a bill first!");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        String billId = model.getValueAt(row, 0).toString();
        String patientId = model.getValueAt(row, 1).toString();
        String date = model.getValueAt(row, 2).toString();
        String total = model.getValueAt(row, 3).toString();
        String advUsed = model.getValueAt(row, 4).toString();
        String payable = model.getValueAt(row, 5).toString();
        String notes = String.valueOf(model.getValueAt(row, 6));

        String text = buildBillText(billId, patientId, date, total, advUsed, payable, notes);

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);

        try {
            boolean ok = area.print(); // shows print dialog
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Print cancelled.");
            }
        } catch (PrinterException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage());
        }
    }

    private String buildBillText(String billId, String patientId, String date,
                                 String total, String advUsed, String payable, String notes) {

        return "==============================\n" +
                "        MEDICARE PLUS\n" +
                "           BILL\n" +
                "==============================\n" +
                "Bill ID      : " + billId + "\n" +
                "Patient ID   : " + patientId + "\n" +
                "Bill Date    : " + date + "\n" +
                "------------------------------\n" +
                "Total Amount : " + total + "\n" +
                "Advance Used : " + advUsed + "\n" +
                "Payable      : " + payable + "\n" +
                "------------------------------\n" +
                "Notes:\n" + (notes == null ? "" : notes) + "\n" +
                "==============================\n" +
                "Thank you!\n";
    }

    private double parseDoubleSafe(String value) {
        try {
            if (value == null) return 0;
            String v = value.trim();
            if (v.isEmpty()) return 0;
            return Double.parseDouble(v);
        } catch (Exception e) {
            return 0;
        }
    }

    // -----------------------------
    // Helper classes
    // -----------------------------

    static class PatientItem {
        int id;
        String name;

        PatientItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    // -----------------------------
    // UI Helpers (same style)
    // -----------------------------

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
            setPreferredSize(new Dimension(160, 38));

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
