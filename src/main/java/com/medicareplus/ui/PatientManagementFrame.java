package com.medicareplus.ui;

import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Patient;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class PatientManagementFrame extends JFrame {

    private static final DecimalFormat AMOUNT_FORMAT =
            new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9 ()-]{6,19}$");
    private static final Pattern NIC_PATTERN = Pattern.compile("^(?:[0-9]{9}[VvXx]|[0-9]{12})$");

    private final PatientDAO patientDAO = new PatientDAO();

    private JTable table;
    private DefaultTableModel model;
    private UITheme.SearchField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;
    private UITheme.Button btnEdit;
    private UITheme.Button btnDelete;
    private JLabel recordCount;
    private UITheme.TableView tableView;

    public PatientManagementFrame() {
        UITheme.configureFrame(this, "Patients", 1200, 720);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(24, 26, 24, 26));

        txtSearch = UITheme.searchField("Search patients");
        txtSearch.setPreferredSize(new Dimension(330, 40));
        txtSearch.setToolTipText("Search by patient ID, name, NIC, phone, email, address or history (Ctrl/Cmd+F)");
        txtSearch.getAccessibleContext().setAccessibleName("Search patients");
        txtSearch.getAccessibleContext().setAccessibleDescription(
                "Filters the patient table as you type");
        installTextFocus(txtSearch);

        JPanel searchControl = new JPanel(new BorderLayout(8, 0));
        searchControl.setOpaque(false);
        JLabel searchIcon = new JLabel(UITheme.icon(
                UITheme.IconType.SEARCH, 18, UITheme.TEXT_MUTED));
        searchIcon.setLabelFor(txtSearch);
        searchIcon.setToolTipText("Search patients");
        searchControl.add(searchIcon, BorderLayout.WEST);
        searchControl.add(txtSearch, BorderLayout.CENTER);

        root.add(UITheme.createHeader(
                "Patient management",
                "Keep clinical details, contacts and advance balances accurate.",
                searchControl
        ), BorderLayout.NORTH);

        UITheme.CardPanel card = new UITheme.CardPanel();
        card.setLayout(new BorderLayout(0, 14));

        UITheme.Button btnAdd = UITheme.button(
                "Add patient", UITheme.IconType.ADD, UITheme.ButtonStyle.PRIMARY);
        btnEdit = UITheme.button(
                "Edit", UITheme.IconType.EDIT, UITheme.ButtonStyle.SECONDARY);
        btnDelete = UITheme.button(
                "Delete", UITheme.IconType.DELETE, UITheme.ButtonStyle.DANGER);
        UITheme.Button btnRefresh = UITheme.button(
                "Refresh", UITheme.IconType.REFRESH, UITheme.ButtonStyle.GHOST);

        btnAdd.setMnemonic(KeyEvent.VK_A);
        btnEdit.setMnemonic(KeyEvent.VK_E);
        btnDelete.setMnemonic(KeyEvent.VK_D);
        btnRefresh.setMnemonic(KeyEvent.VK_R);

        btnAdd.setToolTipText("Register a new patient (Insert)");
        btnEdit.setToolTipText("Edit the selected patient (double-click a row)");
        btnDelete.setToolTipText("Delete the selected patient (Delete)");
        btnRefresh.setToolTipText("Reload patient records (Ctrl/Cmd+R)");

        btnAdd.getAccessibleContext().setAccessibleDescription(
                "Opens the form for registering a patient");
        btnEdit.getAccessibleContext().setAccessibleDescription(
                "Opens the selected patient record for editing");
        btnDelete.getAccessibleContext().setAccessibleDescription(
                "Permanently deletes the selected patient after confirmation");
        btnRefresh.getAccessibleContext().setAccessibleDescription(
                "Reloads patient records from the database");

        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        JPanel primaryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        primaryActions.setOpaque(false);
        primaryActions.add(btnAdd);
        primaryActions.add(btnEdit);
        primaryActions.add(btnDelete);

        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.add(primaryActions, BorderLayout.WEST);
        toolbar.add(btnRefresh, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{
                        "ID", "Full name", "NIC", "Phone", "Email", "Address",
                        "Medical history", "Advance paid"
                },
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
                    case 7 -> Double.class;
                    default -> String.class;
                };
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setToolTipText("Select a patient to edit or delete. Double-click to edit.");
        table.getAccessibleContext().setAccessibleName("Patient records");
        table.getAccessibleContext().setAccessibleDescription(
                "Sortable patient records table. Select one row for record actions.");

        DefaultTableCellRenderer idRenderer = new UITheme.TableCellRenderer();
        idRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(createAmountRenderer());

        UITheme.setColumnWidths(table, 58, 180, 125, 125, 190, 180, 225, 115);
        table.getColumnModel().getColumn(0).setMinWidth(48);
        table.getColumnModel().getColumn(0).setMaxWidth(72);
        table.getColumnModel().getColumn(7).setMinWidth(105);
        // Keep the main list scannable; full address and clinical history remain
        // available in the selected patient's edit dialog and searchable model.
        table.removeColumn(table.getColumnModel().getColumn(6));
        table.removeColumn(table.getColumnModel().getColumn(5));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.addRowSorterListener(e -> updateRecordCount());

        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.PATIENTS,
                "No patients yet",
                "Add a patient to begin building the care record."
        );
        card.add(tableView, BorderLayout.CENTER);

        recordCount = UITheme.recordCountLabel();
        JLabel tableHint = UITheme.mutedLabel("Double-click a row to edit");
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(recordCount, BorderLayout.WEST);
        footer.add(tableHint, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

        btnAdd.addActionListener(e -> addPatient());
        btnEdit.addActionListener(e -> editPatient());
        btnDelete.addActionListener(e -> deletePatient());
        btnRefresh.addActionListener(e -> loadPatients());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionActions();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    if (viewRow >= 0) {
                        table.setRowSelectionInterval(viewRow, viewRow);
                        editPatient();
                    }
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
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

        installShortcuts(btnAdd, btnRefresh);
        loadPatients();
    }

    private void installShortcuts(JButton btnAdd, JButton btnRefresh) {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusPatientSearch",
                () -> {
                    txtSearch.requestFocusInWindow();
                    txtSearch.selectAll();
                }
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshPatients",
                btnRefresh::doClick
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
                "addPatient",
                btnAdd::doClick
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "deletePatient",
                () -> {
                    if (btnDelete.isEnabled()) {
                        btnDelete.doClick();
                    }
                }
        );
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter(
                    "(?iu)" + Pattern.quote(text)));
        }
        updateRecordCount();
        updateSelectionActions();
    }

    private void loadPatients() {
        model.setRowCount(0);
        List<Patient> patients = patientDAO.getAllPatients();

        for (Patient patient : patients) {
            model.addRow(new Object[]{
                    patient.getPatientId(),
                    patient.getFullName(),
                    patient.getNic(),
                    patient.getPhone(),
                    patient.getEmail(),
                    patient.getAddress(),
                    patient.getMedicalHistory(),
                    patient.getAdvancePaid()
            });
        }

        updateRecordCount();
        updateSelectionActions();
    }

    private void updateRecordCount() {
        if (recordCount == null || table == null || model == null) {
            return;
        }

        int visible = table.getRowCount();
        int total = model.getRowCount();
        if (visible == total) {
            UITheme.setRecordCount(recordCount, total, "patient", "patients");
        } else {
            recordCount.setText(visible + " of " + total + " patients");
        }
        tableView.updateState(total, visible);
    }

    private void updateSelectionActions() {
        boolean selected = table != null && table.getSelectedRow() >= 0;
        btnEdit.setEnabled(selected);
        btnDelete.setEnabled(selected);
    }

    private void addPatient() {
        PatientForm form = new PatientForm(
                "", "", "", "", "", "", 0.0);

        if (!showPatientForm(form, "Add patient")) {
            return;
        }

        Patient patient = new Patient(
                form.fullName(),
                form.nic(),
                form.phone(),
                form.email(),
                form.address(),
                form.medicalHistory()
        );

        boolean success = patientDAO.addPatient(patient, form.advanceAmount());
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record was added successfully.",
                    "Patient added",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadPatients();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record could not be added. Please try again.",
                    "Unable to add patient",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editPatient() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showNoSelection();
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        double advance = ((Number) model.getValueAt(row, 7)).doubleValue();

        PatientForm form = new PatientForm(
                cellText(row, 1),
                cellText(row, 2),
                cellText(row, 3),
                cellText(row, 4),
                cellText(row, 5),
                cellText(row, 6),
                advance
        );

        if (!showPatientForm(form, "Edit patient")) {
            return;
        }

        Patient updated = new Patient(
                id,
                form.fullName(),
                form.nic(),
                form.phone(),
                form.email(),
                form.address(),
                form.medicalHistory()
        );

        boolean success = patientDAO.updatePatient(updated, form.advanceAmount());
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record was updated successfully.",
                    "Patient updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadPatients();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record could not be updated. Please try again.",
                    "Unable to update patient",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deletePatient() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showNoSelection();
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        String name = cellText(row, 1);

        if (!UITheme.confirmDelete(this, "patient \"" + name + "\"")) {
            return;
        }

        boolean success = patientDAO.deletePatient(id);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record was deleted.",
                    "Patient deleted",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadPatients();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The patient record could not be deleted. It may be linked to other records.",
                    "Unable to delete patient",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean showPatientForm(PatientForm form, String title) {
        while (true) {
            SwingUtilities.invokeLater(() -> {
                form.fullNameField.requestFocusInWindow();
                form.fullNameField.selectAll();
            });

            int option = JOptionPane.showConfirmDialog(
                    this,
                    form.panel,
                    title,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (option != JOptionPane.OK_OPTION) {
                return false;
            }

            if (form.fullName().isEmpty()) {
                showValidation(
                        form.fullNameField,
                        "Enter the patient's full name.",
                        "Full name is required"
                );
                continue;
            }

            if (!form.nic().isEmpty() && !NIC_PATTERN.matcher(form.nic()).matches()) {
                showValidation(
                        form.nicField,
                        "Enter a valid NIC using 12 digits or 9 digits followed by V or X.",
                        "Check NIC"
                );
                continue;
            }

            if (!form.phone().isEmpty() && !PHONE_PATTERN.matcher(form.phone()).matches()) {
                showValidation(
                        form.phoneField,
                        "Enter a valid phone number with 7 to 20 characters.",
                        "Check phone number"
                );
                continue;
            }

            if (!form.email().isEmpty() && !EMAIL_PATTERN.matcher(form.email()).matches()) {
                showValidation(
                        form.emailField,
                        "Enter a valid email address, for example patient@example.com.",
                        "Check email address"
                );
                continue;
            }

            try {
                form.advanceAmount();
            } catch (IllegalArgumentException ex) {
                showValidation(
                        form.advanceField,
                        ex.getMessage(),
                        "Check advance payment"
                );
                continue;
            }
            return true;
        }
    }

    private void showValidation(JComponent component, String message, String title) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
        component.requestFocusInWindow();
        if (component instanceof JTextComponent textComponent) {
            textComponent.selectAll();
        }
    }

    private void showNoSelection() {
        JOptionPane.showMessageDialog(
                this,
                "Select a patient record first.",
                "No patient selected",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private String cellText(int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private DefaultTableCellRenderer createAmountRenderer() {
        return new UITheme.TableCellRenderer() {
            {
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
                        table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number number) {
                    setText(AMOUNT_FORMAT.format(number.doubleValue()));
                }
                return component;
            }
        };
    }

    private JTextField createField(String value, String accessibleName, String tooltip) {
        JTextField field = UITheme.textField();
        field.setText(value);
        field.setToolTipText(tooltip);
        field.getAccessibleContext().setAccessibleName(accessibleName);
        field.getAccessibleContext().setAccessibleDescription(tooltip);
        installTextFocus(field);
        return field;
    }

    private void installTextFocus(JTextComponent component) {
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_STRONG),
                new EmptyBorder(8, 11, 8, 11)
        );
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 2),
                new EmptyBorder(7, 10, 7, 10)
        );
        component.setBorder(normal);
        component.setCaretColor(UITheme.PRIMARY);
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                component.setBorder(focused);
            }

            @Override
            public void focusLost(FocusEvent e) {
                component.setBorder(normal);
            }
        });
    }

    private void installTextAreaFocus(JTextArea area, JScrollPane scrollPane) {
        Border normal = BorderFactory.createLineBorder(UITheme.BORDER_STRONG);
        Border focused = BorderFactory.createLineBorder(UITheme.PRIMARY, 2);
        scrollPane.setBorder(normal);
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                scrollPane.setBorder(focused);
            }

            @Override
            public void focusLost(FocusEvent e) {
                scrollPane.setBorder(normal);
            }
        });
    }

    private double parseAdvanceAmount(String value) {
        String normalized = value == null ? "" : value.trim().replace(",", "");
        if (normalized.isEmpty()) {
            return 0.0;
        }

        try {
            double amount = Double.parseDouble(normalized);
            if (!Double.isFinite(amount) || amount < 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Enter a non-negative amount, for example 2,500.00.");
        }
    }

    private final class PatientForm {
        private final JPanel panel;
        private final JTextField fullNameField;
        private final JTextField nicField;
        private final JTextField phoneField;
        private final JTextField emailField;
        private final JTextArea addressArea;
        private final JTextArea medicalHistoryArea;
        private final JTextField advanceField;

        private PatientForm(
                String fullName,
                String nic,
                String phone,
                String email,
                String address,
                String medicalHistory,
                double advance
        ) {
            fullNameField = createField(
                    fullName,
                    "Full name, required",
                    "Patient's full legal or preferred name");
            nicField = createField(
                    nic,
                    "NIC",
                    "National identity card number, for example 199012345678");
            phoneField = createField(
                    phone,
                    "Phone number",
                    "Contact number, for example +94 77 123 4567");
            emailField = createField(
                    email,
                    "Email address",
                    "Email address, for example patient@example.com");
            advanceField = createField(
                    AMOUNT_FORMAT.format(advance),
                    "Advance payment",
                    "Available advance balance; enter 0.00 when there is none");

            addressArea = new JTextArea(address, 2, 24);
            addressArea.getAccessibleContext().setAccessibleName("Address");
            addressArea.getAccessibleContext().setAccessibleDescription(
                    "Patient's current residential address");
            JScrollPane addressScroll = UITheme.textAreaScroll(addressArea);
            addressScroll.setPreferredSize(new Dimension(390, 64));
            installTextAreaFocus(addressArea, addressScroll);

            medicalHistoryArea = new JTextArea(medicalHistory, 3, 24);
            medicalHistoryArea.getAccessibleContext().setAccessibleName("Medical history");
            medicalHistoryArea.getAccessibleContext().setAccessibleDescription(
                    "Relevant medical history, conditions or allergies");
            JScrollPane historyScroll = UITheme.textAreaScroll(medicalHistoryArea);
            historyScroll.setPreferredSize(new Dimension(390, 76));
            installTextAreaFocus(medicalHistoryArea, historyScroll);

            UITheme.FormBuilder form = new UITheme.FormBuilder();
            form.addField("Full name *", fullNameField);
            form.addField("NIC", nicField, "Example: 199012345678");
            form.addField("Phone", phoneField, "Example: +94 77 123 4567");
            form.addField("Email", emailField, "Example: patient@example.com");
            form.addField("Address", addressScroll);
            addressScroll.setPreferredSize(new Dimension(390, 64));
            form.addField(
                    "Medical history",
                    historyScroll,
                    "Include conditions or allergies relevant to care.");
            historyScroll.setPreferredSize(new Dimension(390, 76));
            form.addField(
                    "Advance payment",
                    advanceField,
                    "Use 0.00 when no advance has been received.");

            panel = new JPanel(new BorderLayout(0, 12));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(4, 4, 4, 4));
            panel.add(UITheme.mutedLabel(
                    "Fields marked with * are required. Review clinical details before saving."),
                    BorderLayout.NORTH);
            JScrollPane formScroll = UITheme.pageScroll(form);
            formScroll.setPreferredSize(new Dimension(570, 390));
            panel.add(formScroll, BorderLayout.CENTER);
            panel.setPreferredSize(UITheme.dialogSize(610, 440));
        }

        private String fullName() {
            return fullNameField.getText().trim();
        }

        private String nic() {
            return nicField.getText().trim();
        }

        private String phone() {
            return phoneField.getText().trim();
        }

        private String email() {
            return emailField.getText().trim();
        }

        private String address() {
            return addressArea.getText().trim();
        }

        private String medicalHistory() {
            return medicalHistoryArea.getText().trim();
        }

        private double advanceAmount() {
            return parseAdvanceAmount(advanceField.getText());
        }
    }
}
