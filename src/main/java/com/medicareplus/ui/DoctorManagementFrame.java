package com.medicareplus.ui;

import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.model.Doctor;

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
import java.util.List;
import java.util.regex.Pattern;

public class DoctorManagementFrame extends JFrame {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTable table;
    private DefaultTableModel model;
    private UITheme.SearchField txtSearch;
    private TableRowSorter<DefaultTableModel> sorter;
    private UITheme.Button btnEdit;
    private UITheme.Button btnDelete;
    private JLabel recordCount;
    private UITheme.TableView tableView;

    public DoctorManagementFrame() {
        UITheme.configureFrame(this, "Doctors", 1140, 690);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(24, 26, 24, 26));

        txtSearch = UITheme.searchField("Search doctors");
        txtSearch.setPreferredSize(new Dimension(330, 40));
        txtSearch.setToolTipText(
                "Search by doctor ID, name, specialty, contact or availability (Ctrl/Cmd+F)");
        txtSearch.getAccessibleContext().setAccessibleName("Search doctors");
        txtSearch.getAccessibleContext().setAccessibleDescription(
                "Filters the doctor table as you type");
        installTextFocus(txtSearch);

        JPanel searchControl = new JPanel(new BorderLayout(8, 0));
        searchControl.setOpaque(false);
        JLabel searchIcon = new JLabel(UITheme.icon(
                UITheme.IconType.SEARCH, 18, UITheme.TEXT_MUTED));
        searchIcon.setLabelFor(txtSearch);
        searchIcon.setToolTipText("Search doctors");
        searchControl.add(searchIcon, BorderLayout.WEST);
        searchControl.add(txtSearch, BorderLayout.CENTER);

        root.add(UITheme.createHeader(
                "Doctor management",
                "Maintain specialties, contact details and clinical availability.",
                searchControl
        ), BorderLayout.NORTH);

        UITheme.CardPanel card = new UITheme.CardPanel();
        card.setLayout(new BorderLayout(0, 14));

        UITheme.Button btnAdd = UITheme.button(
                "Add doctor", UITheme.IconType.ADD, UITheme.ButtonStyle.PRIMARY);
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

        btnAdd.setToolTipText("Register a new doctor (Insert)");
        btnEdit.setToolTipText("Edit the selected doctor (double-click a row)");
        btnDelete.setToolTipText("Delete the selected doctor (Delete)");
        btnRefresh.setToolTipText("Reload doctor records (Ctrl/Cmd+R)");

        btnAdd.getAccessibleContext().setAccessibleDescription(
                "Opens the form for registering a doctor");
        btnEdit.getAccessibleContext().setAccessibleDescription(
                "Opens the selected doctor record for editing");
        btnDelete.getAccessibleContext().setAccessibleDescription(
                "Permanently deletes the selected doctor after confirmation");
        btnRefresh.getAccessibleContext().setAccessibleDescription(
                "Reloads doctor records from the database");

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
                        "ID", "Full name", "Specialty", "Phone", "Email",
                        "Available days", "Available time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setToolTipText("Select a doctor to edit or delete. Double-click to edit.");
        table.getAccessibleContext().setAccessibleName("Doctor records");
        table.getAccessibleContext().setAccessibleDescription(
                "Sortable doctor records table. Select one row for record actions.");

        DefaultTableCellRenderer idRenderer = new UITheme.TableCellRenderer();
        idRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer);

        UITheme.setColumnWidths(table, 58, 190, 165, 135, 210, 190, 155);
        table.getColumnModel().getColumn(0).setMinWidth(48);
        table.getColumnModel().getColumn(0).setMaxWidth(72);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.addRowSorterListener(e -> updateRecordCount());

        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.DOCTORS,
                "No doctors yet",
                "Add a doctor to begin managing the care team."
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

        btnAdd.addActionListener(e -> addDoctor());
        btnEdit.addActionListener(e -> editSelectedDoctor());
        btnDelete.addActionListener(e -> deleteSelectedDoctor());
        btnRefresh.addActionListener(e -> loadDoctors());

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
                        editSelectedDoctor();
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
        loadDoctors();
    }

    private void installShortcuts(JButton btnAdd, JButton btnRefresh) {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusDoctorSearch",
                () -> {
                    txtSearch.requestFocusInWindow();
                    txtSearch.selectAll();
                }
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshDoctors",
                btnRefresh::doClick
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0),
                "addDoctor",
                btnAdd::doClick
        );
        UITheme.bindShortcut(
                getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "deleteDoctor",
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

    private void loadDoctors() {
        model.setRowCount(0);
        List<Doctor> doctors = doctorDAO.getAllDoctors();

        for (Doctor doctor : doctors) {
            model.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getFullName(),
                    doctor.getSpecialty(),
                    doctor.getPhone(),
                    doctor.getEmail(),
                    doctor.getAvailableDays(),
                    doctor.getAvailableTime()
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
            UITheme.setRecordCount(recordCount, total, "doctor", "doctors");
        } else {
            recordCount.setText(visible + " of " + total + " doctors");
        }
        tableView.updateState(total, visible);
    }

    private void updateSelectionActions() {
        boolean selected = table != null && table.getSelectedRow() >= 0;
        btnEdit.setEnabled(selected);
        btnDelete.setEnabled(selected);
    }

    private void addDoctor() {
        DoctorForm form = new DoctorForm("", "", "", "", "", "");
        if (!showDoctorForm(form, "Add doctor")) {
            return;
        }

        Doctor doctor = new Doctor(
                form.fullName(),
                form.specialty(),
                form.phone(),
                form.email(),
                form.availableDays(),
                form.availableTime()
        );

        boolean success = doctorDAO.addDoctor(doctor);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record was added successfully.",
                    "Doctor added",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadDoctors();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record could not be added. Please try again.",
                    "Unable to add doctor",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editSelectedDoctor() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showNoSelection();
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();

        DoctorForm form = new DoctorForm(
                cellText(row, 1),
                cellText(row, 2),
                cellText(row, 3),
                cellText(row, 4),
                cellText(row, 5),
                cellText(row, 6)
        );

        if (!showDoctorForm(form, "Edit doctor")) {
            return;
        }

        Doctor updated = new Doctor(
                id,
                form.fullName(),
                form.specialty(),
                form.phone(),
                form.email(),
                form.availableDays(),
                form.availableTime()
        );

        boolean success = doctorDAO.updateDoctor(updated);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record was updated successfully.",
                    "Doctor updated",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadDoctors();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record could not be updated. Please try again.",
                    "Unable to update doctor",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteSelectedDoctor() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showNoSelection();
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        String name = cellText(row, 1);

        if (!UITheme.confirmDelete(this, "doctor \"" + name + "\"")) {
            return;
        }

        boolean success = doctorDAO.deleteDoctor(id);
        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record was deleted.",
                    "Doctor deleted",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadDoctors();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The doctor record could not be deleted. It may be linked to appointments.",
                    "Unable to delete doctor",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean showDoctorForm(DoctorForm form, String title) {
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
                        "Enter the doctor's full name.",
                        "Full name is required"
                );
                continue;
            }
            if (form.specialty().isEmpty()) {
                showValidation(
                        form.specialtyField,
                        "Enter the doctor's clinical specialty.",
                        "Specialty is required"
                );
                continue;
            }
            return true;
        }
    }

    private void showValidation(JTextField field, String message, String title) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE
        );
        field.requestFocusInWindow();
        field.selectAll();
    }

    private void showNoSelection() {
        JOptionPane.showMessageDialog(
                this,
                "Select a doctor record first.",
                "No doctor selected",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private String cellText(int row, int column) {
        Object value = model.getValueAt(row, column);
        return value == null ? "" : value.toString();
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

    private final class DoctorForm {
        private final JPanel panel;
        private final JTextField fullNameField;
        private final JTextField specialtyField;
        private final JTextField phoneField;
        private final JTextField emailField;
        private final JTextField availableDaysField;
        private final JTextField availableTimeField;

        private DoctorForm(
                String fullName,
                String specialty,
                String phone,
                String email,
                String availableDays,
                String availableTime
        ) {
            fullNameField = createField(
                    fullName,
                    "Full name, required",
                    "Doctor's full professional name");
            specialtyField = createField(
                    specialty,
                    "Specialty, required",
                    "Clinical specialty, for example Cardiology");
            phoneField = createField(
                    phone,
                    "Phone number",
                    "Contact number, for example +94 77 123 4567");
            emailField = createField(
                    email,
                    "Email address",
                    "Professional email, for example doctor@example.com");
            availableDaysField = createField(
                    availableDays,
                    "Available days",
                    "Comma-separated days, for example Mon, Wed, Fri");
            availableTimeField = createField(
                    availableTime,
                    "Available time",
                    "Time range, for example 09:00 - 13:00");

            UITheme.FormBuilder form = new UITheme.FormBuilder();
            form.addField("Full name *", fullNameField);
            form.addField(
                    "Specialty *",
                    specialtyField,
                    "Example: Cardiology, Paediatrics or General Medicine");
            form.addField("Phone", phoneField, "Example: +94 77 123 4567");
            form.addField("Email", emailField, "Example: doctor@example.com");
            form.addField(
                    "Available days",
                    availableDaysField,
                    "Example: Mon, Wed, Fri");
            form.addField(
                    "Available time",
                    availableTimeField,
                    "Example: 09:00 - 13:00");

            panel = new JPanel(new BorderLayout(0, 12));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(4, 4, 4, 4));
            panel.add(UITheme.mutedLabel(
                    "Fields marked with * are required. Use consistent availability formats."),
                    BorderLayout.NORTH);
            JScrollPane formScroll = UITheme.pageScroll(form);
            formScroll.setPreferredSize(new Dimension(550, 360));
            panel.add(formScroll, BorderLayout.CENTER);
            panel.setPreferredSize(UITheme.dialogSize(590, 430));
        }

        private String fullName() {
            return fullNameField.getText().trim();
        }

        private String specialty() {
            return specialtyField.getText().trim();
        }

        private String phone() {
            return phoneField.getText().trim();
        }

        private String email() {
            return emailField.getText().trim();
        }

        private String availableDays() {
            return availableDaysField.getText().trim();
        }

        private String availableTime() {
            return availableTimeField.getText().trim();
        }
    }
}
