package com.medicareplus.ui;

import com.medicareplus.dao.AppointmentDAO;
import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.dao.NotificationDAO;
import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Appointment;
import com.medicareplus.model.Doctor;
import com.medicareplus.model.Notification;
import com.medicareplus.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AppointmentManagementFrame extends JFrame {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    private final Map<Integer, String> patientNames = new HashMap<>();
    private final Map<Integer, String> doctorNames = new HashMap<>();

    private JTable table;
    private DefaultTableModel model;
    private UITheme.SearchField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel recordCountLabel;
    private UITheme.Button statusButton;
    private UITheme.Button deleteButton;
    private UITheme.TableView tableView;

    public AppointmentManagementFrame() {
        UITheme.configureFrame(this, "Appointments", 1180, 720);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(0, 20));
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        searchField = UITheme.searchField("Search appointments");
        root.add(UITheme.createHeader(
                "Appointments",
                "Schedule visits, coordinate clinicians, and track every status.",
                searchField
        ), BorderLayout.NORTH);

        UITheme.CardPanel card = new UITheme.CardPanel();
        card.setLayout(new BorderLayout(0, 14));

        UITheme.Button addButton = UITheme.button(
                "Schedule appointment", UITheme.IconType.ADD, UITheme.ButtonStyle.PRIMARY);
        statusButton = UITheme.button(
                "Update status", UITheme.IconType.EDIT, UITheme.ButtonStyle.SECONDARY);
        deleteButton = UITheme.button(
                "Delete", UITheme.IconType.DELETE, UITheme.ButtonStyle.DANGER);
        UITheme.Button refreshButton = UITheme.button(
                "Refresh", UITheme.IconType.REFRESH, UITheme.ButtonStyle.GHOST);

        statusButton.setEnabled(false);
        deleteButton.setEnabled(false);
        addButton.setToolTipText("Schedule a new patient appointment");
        statusButton.setToolTipText("Select an appointment to update its status");
        deleteButton.setToolTipText("Select an appointment to delete it");
        refreshButton.setToolTipText("Reload appointments (Ctrl/Cmd+R)");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(addButton);
        actions.add(statusButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        recordCountLabel = UITheme.recordCountLabel();
        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.add(actions, BorderLayout.WEST);
        toolbar.add(recordCountLabel, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"ID", "Patient", "Doctor", "Date", "Time", "Status", "Notes"}, 0) {
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
        UITheme.setColumnWidths(table, 58, 170, 190, 105, 80, 105, 260);
        table.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());
        table.setAutoCreateRowSorter(false);
        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.APPOINTMENTS,
                "No appointments yet",
                "Schedule an appointment to start the care calendar."
        );
        card.add(tableView, BorderLayout.CENTER);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);

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
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionActions();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && table.getSelectedRow() >= 0) {
                    updateStatus();
                }
            }
        });

        addButton.addActionListener(e -> addAppointment());
        statusButton.addActionListener(e -> updateStatus());
        deleteButton.addActionListener(e -> deleteAppointment());
        refreshButton.addActionListener(e -> loadAppointments());

        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusAppointmentSearch", () -> {
                    searchField.requestFocusInWindow();
                    searchField.selectAll();
                });
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshAppointments", this::loadAppointments);

        loadAppointments();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        updateRecordCount();
    }

    private void loadAppointments() {
        patientNames.clear();
        doctorNames.clear();
        for (Patient patient : patientDAO.getAllPatients()) {
            patientNames.put(patient.getPatientId(), patient.getFullName());
        }
        for (Doctor doctor : doctorDAO.getAllDoctors()) {
            doctorNames.put(doctor.getDoctorId(), doctor.getFullName());
        }

        model.setRowCount(0);
        for (Appointment appointment : appointmentDAO.getAllAppointments()) {
            model.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    displayName(patientNames, appointment.getPatientId(), "Patient"),
                    displayName(doctorNames, appointment.getDoctorId(), "Doctor"),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus(),
                    appointment.getNotes()
            });
        }

        table.clearSelection();
        updateSelectionActions();
        updateRecordCount();
    }

    private String displayName(Map<Integer, String> names, int id, String fallback) {
        String name = names.get(id);
        return name == null || name.isBlank()
                ? fallback + " #" + id
                : name + "  ·  #" + id;
    }

    private void updateRecordCount() {
        int total = model.getRowCount();
        int visible = table.getRowCount();
        if (visible == total) {
            UITheme.setRecordCount(recordCountLabel, total, "appointment", "appointments");
        } else {
            recordCountLabel.setText(visible + " shown  ·  " + total + " total");
        }
        tableView.updateState(total, visible);
    }

    private void updateSelectionActions() {
        boolean selected = table.getSelectedRow() >= 0;
        statusButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private void addAppointment() {
        List<Patient> patients = patientDAO.getAllPatients();
        List<Doctor> doctors = doctorDAO.getAllDoctors();

        if (patients.isEmpty()) {
            UITheme.showError(this,
                    "Add at least one patient before scheduling an appointment.");
            return;
        }
        if (doctors.isEmpty()) {
            UITheme.showError(this,
                    "Add at least one doctor before scheduling an appointment.");
            return;
        }

        JComboBox<Patient> patientBox = new JComboBox<>(patients.toArray(new Patient[0]));
        JComboBox<Doctor> doctorBox = new JComboBox<>(doctors.toArray(new Doctor[0]));
        patientBox.setRenderer(personRenderer(true));
        doctorBox.setRenderer(personRenderer(false));

        JTextField dateField = UITheme.textField();
        dateField.setText(LocalDate.now().toString());
        JTextField timeField = UITheme.textField();
        timeField.setText(LocalTime.now().truncatedTo(ChronoUnit.MINUTES).format(TIME_FORMAT));
        JTextField notesField = UITheme.textField();

        UITheme.FormBuilder form = new UITheme.FormBuilder()
                .addField("Patient", patientBox)
                .addField("Doctor", doctorBox)
                .addField("Date", dateField, "Use YYYY-MM-DD")
                .addField("Time", timeField, "Use 24-hour HH:mm")
                .addField("Notes", notesField, "Optional context for the care team");
        JScrollPane formScroll = UITheme.pageScroll(form);
        formScroll.setPreferredSize(UITheme.dialogSize(520, 360));

        Patient patient;
        Doctor doctor;
        String dateText;
        String timeText;
        while (true) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    formScroll,
                    "Schedule appointment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            patient = (Patient) patientBox.getSelectedItem();
            doctor = (Doctor) doctorBox.getSelectedItem();
            dateText = dateField.getText().trim();
            timeText = timeField.getText().trim();

            if (patient == null || doctor == null) {
                UITheme.showError(this, "Choose both a patient and a doctor.");
                continue;
            }
            if (!isValidDate(dateText)) {
                UITheme.showError(this, "Enter a valid date in YYYY-MM-DD format.");
                dateField.requestFocusInWindow();
                dateField.selectAll();
                continue;
            }
            if (!isValidTime(timeText)) {
                UITheme.showError(this, "Enter a valid time in 24-hour HH:mm format.");
                timeField.requestFocusInWindow();
                timeField.selectAll();
                continue;
            }
            break;
        }

        Appointment appointment = new Appointment(
                patient.getPatientId(),
                doctor.getDoctorId(),
                dateText,
                timeText,
                "Scheduled",
                notesField.getText().trim()
        );

        if (!appointmentDAO.addAppointment(appointment)) {
            UITheme.showError(this, "The appointment could not be scheduled.");
            return;
        }

        notificationDAO.addNotification(new Notification(
                "Patient",
                appointment.getPatientId(),
                "Your appointment is scheduled on " + appointment.getAppointmentDate()
                        + " at " + appointment.getAppointmentTime()
                        + " with Dr. " + doctor.getFullName() + "."
        ));
        notificationDAO.addNotification(new Notification(
                "Doctor",
                appointment.getDoctorId(),
                "New appointment scheduled on " + appointment.getAppointmentDate()
                        + " at " + appointment.getAppointmentTime()
                        + " for " + patient.getFullName() + "."
        ));

        UITheme.showSuccess(this, "Appointment scheduled successfully.");
        loadAppointments();
    }

    private ListCellRenderer<Object> personRenderer(boolean patientRenderer) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (patientRenderer && value instanceof Patient patient) {
                    label.setText(patient.getFullName() + "  ·  Patient #" + patient.getPatientId());
                } else if (!patientRenderer && value instanceof Doctor doctor) {
                    label.setText(doctor.getFullName() + "  ·  " + doctor.getSpecialty()
                            + "  ·  #" + doctor.getDoctorId());
                }
                label.setBorder(new EmptyBorder(7, 9, 7, 9));
                return label;
            }
        };
    }

    private boolean isValidDate(String value) {
        if (value.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private boolean isValidTime(String value) {
        if (!value.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        try {
            LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void updateStatus() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        String currentStatus = String.valueOf(model.getValueAt(row, 5));
        String[] statuses = {"Scheduled", "Completed", "Canceled", "Delayed"};
        String status = (String) JOptionPane.showInputDialog(
                this,
                "Choose the new appointment status.",
                "Update appointment status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                statuses,
                currentStatus
        );
        if (status == null || status.equals(currentStatus)) {
            return;
        }

        if (appointmentDAO.updateStatus(id, status)) {
            UITheme.showSuccess(this, "Appointment status updated.");
            loadAppointments();
        } else {
            UITheme.showError(this, "The appointment status could not be updated.");
        }
    }

    private void deleteAppointment() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        int id = ((Number) model.getValueAt(row, 0)).intValue();
        if (!UITheme.confirmDelete(this, "appointment #" + id)) {
            return;
        }

        if (appointmentDAO.deleteAppointment(id)) {
            UITheme.showSuccess(this, "Appointment deleted.");
            loadAppointments();
        } else {
            UITheme.showError(this, "The appointment could not be deleted.");
        }
    }
}
