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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BillingFrame extends JFrame {

    private final PatientDAO patientDAO = new PatientDAO();
    private final BillDAO billDAO = new BillDAO();
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
    private final Map<Integer, String> patientNames = new HashMap<>();

    private JComboBox<PatientItem> patientBox;
    private JLabel advanceBalanceValue;
    private JTextField totalField;
    private JCheckBox useAdvanceCheck;
    private JLabel advanceUsedValue;
    private JLabel payableValue;
    private JTextArea notesArea;

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private UITheme.SearchField searchField;
    private JLabel recordCount;
    private UITheme.Button previewButton;
    private UITheme.Button printButton;
    private UITheme.TableView tableView;

    public BillingFrame() {
        UITheme.configureFrame(this, "Billing & payments", 1340, 800);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(23, 25, 24, 25));

        root.add(UITheme.createHeader(
                "Billing & payments",
                "Create accurate statements, apply patient advances, and review billing history.",
                createHeaderStatus()
        ), BorderLayout.NORTH);
        root.add(UITheme.pageScroll(createWorkspace()), BorderLayout.CENTER);
        setContentPane(root);

        loadPatientsToCombo();
        loadBills();
        wireShortcuts();
    }

    private JComponent createHeaderStatus() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        status.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(UITheme.SUCCESS);
        dot.setFont(UITheme.font(Font.BOLD, 11));
        JLabel text = UITheme.mutedLabel("Local billing records");
        status.add(dot);
        status.add(text);
        return status;
    }

    private JComponent createWorkspace() {
        ResponsiveWorkspace workspace = new ResponsiveWorkspace();
        workspace.setOpaque(false);

        JComponent billForm = createBillForm();
        JComponent billTable = createBillTable();
        boolean[] stacked = {false};
        arrangeWorkspace(workspace, billForm, billTable, false);
        workspace.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                boolean shouldStack = workspace.getWidth() < 1120;
                if (shouldStack == stacked[0]) {
                    return;
                }
                stacked[0] = shouldStack;
                arrangeWorkspace(workspace, billForm, billTable, shouldStack);
            }
        });
        return workspace;
    }

    private void arrangeWorkspace(JPanel workspace, JComponent billForm,
                                  JComponent billTable, boolean stacked) {
        workspace.removeAll();

        GridBagConstraints formConstraints = new GridBagConstraints();
        formConstraints.gridx = 0;
        formConstraints.gridy = 0;
        formConstraints.weightx = stacked ? 1 : 0.34;
        formConstraints.weighty = stacked ? 0 : 1;
        formConstraints.fill = GridBagConstraints.BOTH;
        formConstraints.insets = stacked
                ? new Insets(0, 0, 14, 0)
                : new Insets(0, 0, 0, 14);
        workspace.add(billForm, formConstraints);

        GridBagConstraints tableConstraints = new GridBagConstraints();
        tableConstraints.gridx = stacked ? 0 : 1;
        tableConstraints.gridy = stacked ? 1 : 0;
        tableConstraints.weightx = stacked ? 1 : 0.66;
        tableConstraints.weighty = stacked ? 0 : 1;
        tableConstraints.fill = GridBagConstraints.BOTH;
        workspace.add(billTable, tableConstraints);
        workspace.revalidate();
        workspace.repaint();
    }

    private JComponent createBillForm() {
        UITheme.CardPanel card = new UITheme.CardPanel(20);
        card.setLayout(new BorderLayout(0, 15));
        card.setPreferredSize(new Dimension(410, 570));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.sectionLabel("Create a new bill"));
        heading.add(Box.createVerticalStrut(3));
        heading.add(UITheme.mutedLabel("Amounts recalculate automatically."));
        card.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        patientBox = new JComboBox<>();
        patientBox.setToolTipText("Select the patient receiving this bill");
        UITheme.styleField(patientBox);
        addField(form, c, 0, "Patient", patientBox);

        JPanel balancePanel = new JPanel(new BorderLayout(10, 0));
        balancePanel.setBackground(UITheme.PRIMARY_SOFT);
        balancePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 223, 219)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel balanceLabel = new JLabel("Available advance");
        balanceLabel.setFont(UITheme.font(Font.BOLD, 12));
        balanceLabel.setForeground(UITheme.PRIMARY_DARK);
        advanceBalanceValue = new JLabel("0.00");
        advanceBalanceValue.setFont(UITheme.font(Font.BOLD, 18));
        advanceBalanceValue.setForeground(UITheme.PRIMARY_DARK);
        balancePanel.add(balanceLabel, BorderLayout.WEST);
        balancePanel.add(advanceBalanceValue, BorderLayout.EAST);
        c.gridy = 2;
        c.insets = new Insets(2, 0, 9, 0);
        form.add(balancePanel, c);

        totalField = UITheme.textField();
        totalField.setToolTipText("Enter the full amount before advance payment");
        addField(form, c, 3, "Total amount", totalField);

        useAdvanceCheck = new JCheckBox("Apply the available advance to this bill");
        useAdvanceCheck.setOpaque(false);
        useAdvanceCheck.setFont(UITheme.font(Font.PLAIN, 13));
        useAdvanceCheck.setForeground(UITheme.TEXT);
        useAdvanceCheck.setToolTipText("The applied amount will never exceed the bill total");
        c.gridy = 5;
        c.insets = new Insets(1, 0, 8, 0);
        form.add(useAdvanceCheck, c);

        JPanel totals = new JPanel(new GridLayout(1, 2, 9, 0));
        totals.setOpaque(false);
        advanceUsedValue = new JLabel("0.00");
        payableValue = new JLabel("0.00");
        totals.add(amountTile("Advance used", advanceUsedValue, UITheme.INFO_SOFT, UITheme.INFO));
        totals.add(amountTile(
                "Amount payable", payableValue, UITheme.PRIMARY_SOFT, UITheme.PRIMARY_DARK
        ));
        c.gridy = 6;
        c.insets = new Insets(2, 0, 9, 0);
        form.add(totals, c);

        notesArea = new JTextArea(3, 20);
        notesArea.setToolTipText("Optional billing notes");
        JScrollPane notesScroll = UITheme.textAreaScroll(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 88));
        notesScroll.setMinimumSize(new Dimension(0, 72));
        addField(form, c, 7, "Notes (optional)", notesScroll);

        c.gridy = 9;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 0, 0);
        form.add(Box.createGlue(), c);
        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.setOpaque(false);
        UITheme.Button save = UITheme.button(
                "Save bill", UITheme.IconType.CHECK, UITheme.ButtonStyle.PRIMARY
        );
        UITheme.Button clear = UITheme.button(
                "Clear", UITheme.ButtonStyle.GHOST
        );
        save.setToolTipText("Save this bill (Ctrl+S)");
        clear.setToolTipText("Reset the billing form");
        save.addActionListener(e -> saveBill());
        clear.addActionListener(e -> clearForm());
        actions.add(save);
        actions.add(clear);
        card.add(actions, BorderLayout.SOUTH);

        patientBox.addActionListener(e -> refreshAdvanceLabel());
        useAdvanceCheck.addActionListener(e -> recalculate());
        totalField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                recalculate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                recalculate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                recalculate();
            }
        });
        return card;
    }

    private void addField(JPanel panel, GridBagConstraints c, int row,
                          String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.font(Font.BOLD, 12));
        label.setForeground(UITheme.TEXT_MUTED);
        Component labelTarget = field;
        if (field instanceof JScrollPane scrollPane
                && scrollPane.getViewport().getView() != null) {
            labelTarget = scrollPane.getViewport().getView();
        }
        label.setLabelFor(labelTarget);
        if (labelTarget instanceof JComponent target) {
            target.getAccessibleContext().setAccessibleName(labelText);
        }
        c.gridy = row;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(row == 0 ? 0 : 4, 1, 5, 1);
        panel.add(label, c);

        c.gridy = row + 1;
        c.insets = new Insets(0, 0, 8, 0);
        panel.add(field, c);
    }

    private JComponent amountTile(String label, JLabel value, Color background, Color accent) {
        JPanel tile = new JPanel();
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBackground(background);
        tile.setBorder(new EmptyBorder(9, 11, 9, 11));

        JLabel name = new JLabel(label);
        name.setFont(UITheme.font(Font.BOLD, 11));
        name.setForeground(UITheme.TEXT_MUTED);
        value.setFont(UITheme.font(Font.BOLD, 18));
        value.setForeground(accent);
        tile.add(name);
        tile.add(Box.createVerticalStrut(3));
        tile.add(value);
        return tile;
    }

    private JComponent createBillTable() {
        UITheme.CardPanel card = new UITheme.CardPanel(20);
        card.setLayout(new BorderLayout(0, 13));
        card.setPreferredSize(new Dimension(700, 570));

        JPanel heading = new JPanel(new BorderLayout(18, 0));
        heading.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(UITheme.sectionLabel("Billing history"));
        copy.add(Box.createVerticalStrut(3));
        copy.add(UITheme.mutedLabel("Most recent statements appear first."));
        heading.add(copy, BorderLayout.WEST);

        searchField = UITheme.searchField("Search bills or patients");
        searchField.setToolTipText("Search by bill, patient, date, amount, or notes (Ctrl+F)");
        heading.add(searchField, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Bill #", "Patient", "Date", "Total", "Advance", "Payable", "Notes"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Integer.class;
                    case 3, 4, 5 -> Double.class;
                    default -> String.class;
                };
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);
        UITheme.setColumnWidths(table, 65, 180, 110, 95, 95, 100, 160);
        table.setToolTipText("Double-click a bill to preview it");
        table.getColumnModel().getColumn(3).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new MoneyRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new MoneyRenderer());
        table.removeColumn(table.getColumnModel().getColumn(6));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.BILLING,
                "No bills yet",
                "Create a bill and it will appear in this history."
        );
        card.add(tableView, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(14, 0));
        footer.setOpaque(false);
        recordCount = UITheme.recordCountLabel();
        footer.add(recordCount, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        UITheme.Button refresh = UITheme.button(
                "Refresh", UITheme.IconType.REFRESH, UITheme.ButtonStyle.GHOST
        );
        previewButton = UITheme.button(
                "Preview", UITheme.IconType.REPORTS, UITheme.ButtonStyle.SECONDARY
        );
        printButton = UITheme.button(
                "Print", UITheme.IconType.PRINT, UITheme.ButtonStyle.SECONDARY
        );
        refresh.setToolTipText("Reload billing history (Ctrl+R)");
        previewButton.setToolTipText("Preview the selected bill");
        printButton.setToolTipText("Print the selected bill");
        previewButton.setEnabled(false);
        printButton.setEnabled(false);
        refresh.addActionListener(e -> loadBills());
        previewButton.addActionListener(e -> previewSelectedBill());
        printButton.addActionListener(e -> printSelectedBill());
        actions.add(refresh);
        actions.add(previewButton);
        actions.add(printButton);
        footer.add(actions, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> updateSelectionActions());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    previewSelectedBill();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        sorter.addRowSorterListener(e -> updateRecordCount());
        return card;
    }

    private void loadPatientsToCombo() {
        PatientItem previous = (PatientItem) patientBox.getSelectedItem();
        Integer previousId = previous == null ? null : previous.id;
        patientNames.clear();
        patientBox.removeAllItems();
        List<Patient> patients = patientDAO.getAllPatients();

        for (Patient patient : patients) {
            patientNames.put(patient.getPatientId(), patient.getFullName());
            patientBox.addItem(new PatientItem(patient.getPatientId(), patient.getFullName()));
        }

        if (previousId != null) {
            for (int i = 0; i < patientBox.getItemCount(); i++) {
                if (patientBox.getItemAt(i).id == previousId) {
                    patientBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (patientBox.getSelectedIndex() < 0 && patientBox.getItemCount() > 0) {
            patientBox.setSelectedIndex(0);
        }
        patientBox.setEnabled(patientBox.getItemCount() > 0);
        refreshAdvanceLabel();
        recalculate();
    }

    private void loadBills() {
        patientNames.clear();
        for (Patient patient : patientDAO.getAllPatients()) {
            patientNames.put(patient.getPatientId(), patient.getFullName());
        }

        model.setRowCount(0);
        for (Bill bill : billDAO.getAllBills()) {
            String patientName = patientNames.getOrDefault(
                    bill.getPatientId(), "Patient #" + bill.getPatientId()
            );
            model.addRow(new Object[]{
                    bill.getBillId(),
                    patientName + "  ·  #" + bill.getPatientId(),
                    bill.getBillDate(),
                    bill.getTotalAmount(),
                    bill.getAdvanceUsed(),
                    bill.getPayableAmount(),
                    bill.getNotes()
            });
        }
        table.clearSelection();
        updateSelectionActions();
        updateRecordCount();
    }

    private void refreshAdvanceLabel() {
        PatientItem item = (PatientItem) patientBox.getSelectedItem();
        if (item == null) {
            advanceBalanceValue.setText("0.00");
        } else {
            advanceBalanceValue.setText(
                    amountFormat.format(patientDAO.getAdvanceByPatientId(item.id))
            );
        }
        recalculate();
    }

    private void recalculate() {
        double total = parseAmount(totalField.getText());
        double advance = parseAmount(advanceBalanceValue.getText());
        double used = useAdvanceCheck.isSelected() ? Math.min(advance, total) : 0;
        double payable = Math.max(0, total - used);
        advanceUsedValue.setText(amountFormat.format(used));
        payableValue.setText(amountFormat.format(payable));
    }

    private void clearForm() {
        totalField.setText("");
        useAdvanceCheck.setSelected(false);
        notesArea.setText("");
        recalculate();
        totalField.requestFocusInWindow();
    }

    private void saveBill() {
        PatientItem patient = (PatientItem) patientBox.getSelectedItem();
        if (patient == null) {
            UITheme.showError(this, "Add or select a patient before creating a bill.");
            return;
        }

        double total = parseAmount(totalField.getText());
        if (!Double.isFinite(total) || total <= 0) {
            UITheme.showError(this, "Enter a total amount greater than zero.");
            totalField.requestFocusInWindow();
            return;
        }

        double advanceBalance = patientDAO.getAdvanceByPatientId(patient.id);
        double advanceUsed = useAdvanceCheck.isSelected()
                ? Math.min(advanceBalance, total)
                : 0;
        double payable = total - advanceUsed;

        Bill bill = new Bill(
                patient.id,
                total,
                advanceUsed,
                payable,
                notesArea.getText().trim()
        );
        boolean saved = billDAO.addBillAndApplyAdvance(
                bill,
                advanceBalance - advanceUsed
        );
        if (!saved) {
            UITheme.showError(this, "The bill could not be saved. Please try again.");
            return;
        }

        refreshAdvanceLabel();
        loadBills();
        clearForm();
        UITheme.showSuccess(this, "Bill saved successfully.");
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        updateRecordCount();
    }

    private void updateRecordCount() {
        int visible = table == null ? 0 : table.getRowCount();
        int total = model == null ? 0 : model.getRowCount();
        if (visible == total) {
            UITheme.setRecordCount(recordCount, total, "bill", "bills");
        } else {
            recordCount.setText(visible + " of " + total + " bills");
        }
        tableView.updateState(total, visible);
    }

    private void updateSelectionActions() {
        boolean selected = table.getSelectedRow() >= 0;
        previewButton.setEnabled(selected);
        printButton.setEnabled(selected);
    }

    private void previewSelectedBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        JScrollPane previewScroll = UITheme.pageScroll(createBillPreviewPanel(row));
        previewScroll.setPreferredSize(UITheme.dialogSize(630, 430));
        JOptionPane.showMessageDialog(
                this,
                previewScroll,
                "Bill preview",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private JComponent createBillPreviewPanel(int modelRow) {
        String billId = String.valueOf(model.getValueAt(modelRow, 0));
        String patient = String.valueOf(model.getValueAt(modelRow, 1));
        String date = String.valueOf(model.getValueAt(modelRow, 2));
        String total = formatModelAmount(model.getValueAt(modelRow, 3));
        String advance = formatModelAmount(model.getValueAt(modelRow, 4));
        String payable = formatModelAmount(model.getValueAt(modelRow, 5));
        String notes = String.valueOf(model.getValueAt(modelRow, 6));

        JPanel preview = new JPanel(new BorderLayout(0, 18));
        preview.setBackground(UITheme.SURFACE);
        preview.setBorder(new EmptyBorder(12, 14, 12, 14));
        preview.setPreferredSize(new Dimension(610, 410));

        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        brand.setOpaque(false);
        brand.add(new JLabel(new UITheme.BrandIcon(38)));
        JPanel brandCopy = new JPanel();
        brandCopy.setOpaque(false);
        brandCopy.setLayout(new BoxLayout(brandCopy, BoxLayout.Y_AXIS));
        JLabel brandName = new JLabel("MedicarePlus");
        brandName.setFont(UITheme.font(Font.BOLD, 19));
        brandName.setForeground(UITheme.NAVY);
        brandCopy.add(brandName);
        brandCopy.add(UITheme.mutedLabel("Patient billing statement"));
        brand.add(brandCopy);
        header.add(brand, BorderLayout.WEST);

        JPanel reference = new JPanel();
        reference.setOpaque(false);
        reference.setLayout(new BoxLayout(reference, BoxLayout.Y_AXIS));
        JLabel billNumber = new JLabel("Bill #" + billId);
        billNumber.setFont(UITheme.font(Font.BOLD, 14));
        billNumber.setForeground(UITheme.NAVY);
        billNumber.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel billDate = UITheme.mutedLabel(date);
        billDate.setAlignmentX(Component.RIGHT_ALIGNMENT);
        reference.add(billNumber);
        reference.add(Box.createVerticalStrut(3));
        reference.add(billDate);
        header.add(reference, BorderLayout.EAST);
        preview.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 13));
        body.setOpaque(false);
        JPanel patientPanel = new JPanel(new BorderLayout());
        patientPanel.setBackground(UITheme.SURFACE_ALT);
        patientPanel.setBorder(new EmptyBorder(11, 13, 11, 13));
        JLabel patientLabel = new JLabel("Billed to");
        patientLabel.setFont(UITheme.font(Font.BOLD, 11));
        patientLabel.setForeground(UITheme.TEXT_MUTED);
        JLabel patientValue = new JLabel(patient);
        patientValue.setFont(UITheme.font(Font.BOLD, 14));
        patientValue.setForeground(UITheme.TEXT);
        patientPanel.add(patientLabel, BorderLayout.WEST);
        patientPanel.add(patientValue, BorderLayout.EAST);
        body.add(patientPanel, BorderLayout.NORTH);

        JPanel amounts = new JPanel();
        amounts.setOpaque(false);
        amounts.setLayout(new BoxLayout(amounts, BoxLayout.Y_AXIS));
        amounts.add(statementRow("Total amount", total, false));
        amounts.add(Box.createVerticalStrut(5));
        amounts.add(statementRow("Advance applied", advance, false));
        amounts.add(Box.createVerticalStrut(8));
        amounts.add(statementRow("Amount payable", payable, true));
        body.add(amounts, BorderLayout.CENTER);

        JPanel notesPanel = new JPanel(new BorderLayout(0, 5));
        notesPanel.setOpaque(false);
        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setFont(UITheme.font(Font.BOLD, 11));
        notesLabel.setForeground(UITheme.TEXT_MUTED);
        JTextArea notesValue = new JTextArea(
                notes == null || notes.equals("null") || notes.isBlank() ? "—" : notes
        );
        notesValue.setEditable(false);
        notesValue.setFocusable(true);
        notesValue.setRows(2);
        UITheme.styleTextArea(notesValue);
        notesValue.setBackground(UITheme.SURFACE_ALT);
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesValue, BorderLayout.CENTER);
        body.add(notesPanel, BorderLayout.SOUTH);
        preview.add(body, BorderLayout.CENTER);

        JLabel footer = UITheme.mutedLabel(
                "Thank you for choosing MedicarePlus for your care."
        );
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        preview.add(footer, BorderLayout.SOUTH);
        return preview;
    }

    private JComponent statementRow(String label, String value, boolean emphasized) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(emphasized ? UITheme.PRIMARY_SOFT : UITheme.SURFACE);
        row.setBorder(new EmptyBorder(
                emphasized ? 12 : 8,
                emphasized ? 13 : 4,
                emphasized ? 12 : 8,
                emphasized ? 13 : 4
        ));

        JLabel name = new JLabel(label);
        name.setFont(UITheme.font(emphasized ? Font.BOLD : Font.PLAIN, 13));
        name.setForeground(emphasized ? UITheme.PRIMARY_DARK : UITheme.TEXT_MUTED);
        JLabel amount = new JLabel(value);
        amount.setFont(UITheme.font(Font.BOLD, emphasized ? 20 : 14));
        amount.setForeground(emphasized ? UITheme.PRIMARY_DARK : UITheme.TEXT);
        row.add(name, BorderLayout.WEST);
        row.add(amount, BorderLayout.EAST);
        return row;
    }

    private void printSelectedBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        JTextArea area = new JTextArea(buildBillText(row));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setEditable(false);
        try {
            if (!area.print()) {
                UITheme.showInfo(this, "Print cancelled", "No bill was printed.");
            }
        } catch (PrinterException exception) {
            UITheme.showError(this, "Printing failed: " + exception.getMessage());
        }
    }

    private String buildBillText(int modelRow) {
        String billId = model.getValueAt(modelRow, 0).toString();
        String patient = model.getValueAt(modelRow, 1).toString();
        String date = model.getValueAt(modelRow, 2).toString();
        String total = formatModelAmount(model.getValueAt(modelRow, 3));
        String advance = formatModelAmount(model.getValueAt(modelRow, 4));
        String payable = formatModelAmount(model.getValueAt(modelRow, 5));
        String notes = String.valueOf(model.getValueAt(modelRow, 6));

        return """
                MEDICAREPLUS
                Patient billing statement
                ==================================================
                Bill number       %s
                Patient           %s
                Statement date    %s
                --------------------------------------------------
                Total amount      %s
                Advance applied   %s
                AMOUNT PAYABLE    %s
                --------------------------------------------------
                Notes
                %s
                ==================================================
                Thank you for choosing MedicarePlus.
                """.formatted(
                billId,
                patient,
                date,
                total,
                advance,
                payable,
                notes == null || notes.equals("null") || notes.isBlank() ? "—" : notes
        );
    }

    private void wireShortcuts() {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusSearch",
                () -> searchField.requestFocusInWindow()
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshBills",
                this::loadBills
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask),
                "saveBill",
                this::saveBill
        );
    }

    private double parseAmount(String value) {
        try {
            if (value == null || value.isBlank()) {
                return 0;
            }
            double parsed = Double.parseDouble(value.replace(",", "").trim());
            return Double.isFinite(parsed) ? parsed : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String formatModelAmount(Object value) {
        return value instanceof Number number
                ? amountFormat.format(number.doubleValue())
                : String.valueOf(value);
    }

    private static final class ResponsiveWorkspace extends JPanel implements Scrollable {
        private ResponsiveWorkspace() {
            super(new GridBagLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
            return Math.max(18, visibleRect.height - 36);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class PatientItem {
        private final int id;
        private final String name;

        private PatientItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + "  ·  #" + id;
        }
    }

    private final class MoneyRenderer extends UITheme.TableCellRenderer {
        private MoneyRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );
            if (value instanceof Number number) {
                setText(amountFormat.format(number.doubleValue()));
            }
            return component;
        }
    }
}
