package com.medicareplus.ui;

import com.medicareplus.dao.AppointmentDAO;
import com.medicareplus.dao.BillDAO;
import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.dao.NotificationDAO;
import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Appointment;
import com.medicareplus.model.Doctor;
import com.medicareplus.model.Notification;
import com.medicareplus.service.AssignmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class DashboardFrame extends JFrame {

    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final BillDAO billDAO = new BillDAO();

    private MetricCard patientMetric;
    private MetricCard doctorMetric;
    private MetricCard appointmentMetric;
    private MetricCard notificationMetric;
    private JLabel scheduledValue;
    private JLabel completedValue;
    private JLabel billValue;
    private JLabel unreadValue;
    private SwingWorker<DashboardSnapshot, Void> metricWorker;

    public DashboardFrame() {
        UITheme.configureFrame(this, "Dashboard", 1260, 790);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(0, 22));
        root.setBorder(new EmptyBorder(24, 28, 26, 28));
        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(UITheme.pageScroll(createDashboardBody()), BorderLayout.CENTER);
        setContentPane(root);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refreshMetrics();
            }
        });

        refreshMetrics();
    }

    private JComponent createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(24, 0));
        topBar.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel mark = new JLabel(new UITheme.BrandIcon(46));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("MedicarePlus");
        name.setFont(UITheme.font(Font.BOLD, 22));
        name.setForeground(UITheme.NAVY);
        JLabel descriptor = UITheme.mutedLabel("Clinical operations workspace");
        descriptor.setFont(UITheme.font(Font.PLAIN, 12));
        brandText.add(name);
        brandText.add(Box.createVerticalStrut(2));
        brandText.add(descriptor);

        brand.add(mark);
        brand.add(brandText);
        topBar.add(brand, BorderLayout.WEST);

        JPanel datePanel = new JPanel();
        datePanel.setOpaque(false);
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));

        JLabel date = new JLabel(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        ));
        date.setAlignmentX(Component.RIGHT_ALIGNMENT);
        date.setFont(UITheme.font(Font.BOLD, 13));
        date.setForeground(UITheme.TEXT);
        date.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        status.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(UITheme.PRIMARY);
        dot.setFont(UITheme.font(Font.BOLD, 11));
        JLabel statusText = UITheme.mutedLabel("Local workspace");
        status.add(dot);
        status.add(statusText);
        status.setAlignmentX(Component.RIGHT_ALIGNMENT);

        datePanel.add(date);
        datePanel.add(Box.createVerticalStrut(4));
        datePanel.add(status);
        topBar.add(datePanel, BorderLayout.EAST);

        return topBar;
    }

    private JComponent createDashboardBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        JPanel overviewHeader = new JPanel(new BorderLayout(20, 0));
        overviewHeader.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.setPreferredSize(new Dimension(540, 58));
        copy.add(UITheme.titleLabel("Care operations, at a glance", 30));
        copy.add(Box.createVerticalStrut(4));
        copy.add(UITheme.mutedLabel(
                "Review activity and move quickly to the work that needs attention."
        ));

        UITheme.Button newAppointment = UITheme.button(
                "Open appointments",
                UITheme.IconType.ADD,
                UITheme.ButtonStyle.PRIMARY
        );
        newAppointment.setToolTipText("Schedule a new patient appointment");
        newAppointment.addActionListener(e ->
                openModule(AppointmentManagementFrame::new)
        );
        overviewHeader.add(copy, BorderLayout.WEST);
        overviewHeader.add(newAppointment, BorderLayout.EAST);
        body.add(overviewHeader, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.add(createMetrics(), BorderLayout.NORTH);
        content.add(createWorkspace(), BorderLayout.CENTER);
        body.add(content, BorderLayout.CENTER);
        return body;
    }

    private JComponent createMetrics() {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        metrics.setPreferredSize(new Dimension(0, 104));

        patientMetric = new MetricCard(
                UITheme.IconType.PATIENTS, "Patients", "Registered records", UITheme.PRIMARY
        );
        doctorMetric = new MetricCard(
                UITheme.IconType.DOCTORS, "Care team", "Registered clinicians", UITheme.INFO
        );
        appointmentMetric = new MetricCard(
                UITheme.IconType.APPOINTMENTS, "Appointments", "Scheduled records",
                UITheme.WARNING
        );
        notificationMetric = new MetricCard(
                UITheme.IconType.NOTIFICATIONS, "Alerts", "Waiting to be read", UITheme.DANGER
        );

        metrics.add(patientMetric);
        metrics.add(doctorMetric);
        metrics.add(appointmentMetric);
        metrics.add(notificationMetric);
        return metrics;
    }

    private JComponent createWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 0.68;
        left.weighty = 1;
        left.fill = GridBagConstraints.BOTH;
        left.insets = new Insets(0, 0, 0, 14);
        workspace.add(createQuickActions(), left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.weightx = 0.32;
        right.weighty = 1;
        right.fill = GridBagConstraints.BOTH;
        workspace.add(createOperationalSnapshot(), right);
        return workspace;
    }

    private JComponent createQuickActions() {
        UITheme.CardPanel card = new UITheme.CardPanel(20);
        card.setLayout(new BorderLayout(0, 16));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(UITheme.sectionLabel("Workspace"), BorderLayout.WEST);
        heading.add(UITheme.mutedLabel("Choose a workspace"), BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 3, 12, 12));
        actions.setOpaque(false);

        actions.add(moduleButton(
                "Patients", "Clinical and contact records",
                UITheme.IconType.PATIENTS, PatientManagementFrame::new
        ));
        actions.add(moduleButton(
                "Doctors", "Care team availability",
                UITheme.IconType.DOCTORS, DoctorManagementFrame::new
        ));
        actions.add(moduleButton(
                "Appointments", "Schedules and status",
                UITheme.IconType.APPOINTMENTS, AppointmentManagementFrame::new
        ));
        actions.add(moduleButton(
                "Billing", "Payments and balances",
                UITheme.IconType.BILLING, BillingFrame::new
        ));
        actions.add(moduleButton(
                "Notifications", "Patient and doctor alerts",
                UITheme.IconType.NOTIFICATIONS, NotificationFrame::new
        ));
        actions.add(moduleButton(
                "Reports", "Monthly activity insights",
                UITheme.IconType.REPORTS, MonthlyReportFrame::new
        ));

        card.add(actions, BorderLayout.CENTER);
        return card;
    }

    private ModuleButton moduleButton(String title, String description,
                                      UITheme.IconType icon,
                                      Supplier<? extends JFrame> frameSupplier) {
        ModuleButton button = new ModuleButton(title, description, icon);
        button.addActionListener(e -> openModule(frameSupplier));
        return button;
    }

    private JComponent createOperationalSnapshot() {
        UITheme.CardPanel card = new UITheme.CardPanel(20);
        card.setLayout(new BorderLayout(0, 16));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UITheme.sectionLabel("Operational snapshot"));
        heading.add(Box.createVerticalStrut(3));
        heading.add(UITheme.mutedLabel("Totals from local records"));
        card.add(heading, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        scheduledValue = addSnapshotRow(
                rows, UITheme.IconType.CALENDAR, "Scheduled", UITheme.INFO
        );
        completedValue = addSnapshotRow(
                rows, UITheme.IconType.CHECK, "Completed", UITheme.SUCCESS
        );
        unreadValue = addSnapshotRow(
                rows, UITheme.IconType.NOTIFICATIONS, "Unread alerts", UITheme.DANGER
        );
        billValue = addSnapshotRow(
                rows, UITheme.IconType.BILLING, "Bills created", UITheme.WARNING
        );
        card.add(rows, BorderLayout.CENTER);

        JPanel assist = new JPanel(new BorderLayout(0, 10));
        assist.setOpaque(false);
        assist.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setBorder(new EmptyBorder(9, 0, 0, 0));
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Need the right specialist?");
        title.setFont(UITheme.font(Font.BOLD, 13));
        title.setForeground(UITheme.NAVY);
        title.setBorder(new EmptyBorder(0, 0, 0, 8));
        copy.add(title);
        copy.add(Box.createVerticalStrut(3));
        copy.add(UITheme.mutedLabel("Find a doctor by medical specialty."));
        assist.add(copy, BorderLayout.NORTH);

        UITheme.Button assign = UITheme.button(
                "Find specialist",
                UITheme.IconType.ASSIGN,
                UITheme.ButtonStyle.SECONDARY
        );
        assign.addActionListener(e -> autoAssignDoctor());
        assist.add(assign, BorderLayout.SOUTH);
        card.add(assist, BorderLayout.SOUTH);
        return card;
    }

    private JLabel addSnapshotRow(JPanel parent, UITheme.IconType icon,
                                  String label, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel iconLabel = new JLabel(UITheme.icon(icon, 18, color));
        iconLabel.setPreferredSize(new Dimension(28, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(iconLabel, BorderLayout.WEST);

        JLabel name = new JLabel(label);
        name.setFont(UITheme.font(Font.PLAIN, 13));
        name.setForeground(UITheme.TEXT_MUTED);
        row.add(name, BorderLayout.CENTER);

        JLabel value = new JLabel("…");
        value.setFont(UITheme.font(Font.BOLD, 15));
        value.setForeground(UITheme.NAVY);
        row.add(value, BorderLayout.EAST);
        parent.add(row);
        return value;
    }

    private void refreshMetrics() {
        if (metricWorker != null && !metricWorker.isDone()) {
            return;
        }

        metricWorker = new SwingWorker<>() {
            @Override
            protected DashboardSnapshot doInBackground() {
                int patients = patientDAO.getAllPatients().size();
                int doctors = doctorDAO.getAllDoctors().size();
                List<Appointment> appointments = appointmentDAO.getAllAppointments();
                List<Notification> notifications = notificationDAO.getAllNotifications();

                int scheduled = 0;
                int completed = 0;
                for (Appointment appointment : appointments) {
                    if ("Scheduled".equalsIgnoreCase(appointment.getStatus())) {
                        scheduled++;
                    } else if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
                        completed++;
                    }
                }

                int unread = 0;
                for (Notification notification : notifications) {
                    if (notification.getIsRead() == 0) {
                        unread++;
                    }
                }
                return new DashboardSnapshot(
                        patients,
                        doctors,
                        scheduled,
                        completed,
                        unread,
                        billDAO.getAllBills().size()
                );
            }

            @Override
            protected void done() {
                try {
                    DashboardSnapshot snapshot = get();
                    patientMetric.setValue(snapshot.patients());
                    doctorMetric.setValue(snapshot.doctors());
                    appointmentMetric.setValue(snapshot.scheduled());
                    notificationMetric.setValue(snapshot.unread());
                    scheduledValue.setText(String.valueOf(snapshot.scheduled()));
                    completedValue.setText(String.valueOf(snapshot.completed()));
                    unreadValue.setText(String.valueOf(snapshot.unread()));
                    billValue.setText(String.valueOf(snapshot.bills()));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showMetricsUnavailable();
                } catch (ExecutionException exception) {
                    showMetricsUnavailable();
                }
            }
        };
        metricWorker.execute();
    }

    private void showMetricsUnavailable() {
        patientMetric.setUnavailable();
        doctorMetric.setUnavailable();
        appointmentMetric.setUnavailable();
        notificationMetric.setUnavailable();
        scheduledValue.setText("—");
        completedValue.setText("—");
        unreadValue.setText("—");
        billValue.setText("—");
    }

    private void openModule(Supplier<? extends JFrame> frameSupplier) {
        JFrame module = frameSupplier.get();
        module.setLocationRelativeTo(this);
        setVisible(false);
        module.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refreshMetrics();
                setVisible(true);
                toFront();
            }
        });
        module.setVisible(true);
    }

    private void autoAssignDoctor() {
        JTextField specialty = UITheme.textField();
        specialty.setToolTipText("For example: Cardiology");
        UITheme.FormBuilder form = new UITheme.FormBuilder()
                .addField("Required specialty", specialty,
                        "Enter the specialty needed for this patient.");

        int option = JOptionPane.showConfirmDialog(
                this,
                form,
                "Find doctor by specialty",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String value = specialty.getText().trim();
        if (value.isEmpty()) {
            UITheme.showError(this, "Please enter a medical specialty.");
            return;
        }

        Doctor doctor = new AssignmentService().autoAssignDoctor(value);
        if (doctor == null) {
            UITheme.showInfo(
                    this,
                    "No match found",
                    "No available doctor currently matches “" + value + "”."
            );
            return;
        }

        UITheme.showSuccess(
                this,
                "Matched Dr. " + doctor.getFullName() + "\n"
                        + doctor.getSpecialty() + " · Doctor #" + doctor.getDoctorId()
        );
    }

    private static class MetricCard extends UITheme.CardPanel {
        private final JLabel value = new JLabel("…");

        MetricCard(UITheme.IconType icon, String label, String caption, Color accent) {
            super(18);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(16, 17, 16, 17));

            JLabel iconLabel = new JLabel(UITheme.icon(icon, 23, accent));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(42, 42));
            add(iconLabel, BorderLayout.WEST);

            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

            value.setFont(UITheme.font(Font.BOLD, 24));
            value.setForeground(UITheme.NAVY);
            JLabel name = new JLabel(label);
            name.setFont(UITheme.font(Font.BOLD, 12));
            name.setForeground(UITheme.TEXT);
            JLabel supporting = UITheme.mutedLabel(caption);
            supporting.setFont(UITheme.font(Font.PLAIN, 11));

            copy.add(value);
            copy.add(name);
            copy.add(Box.createVerticalStrut(1));
            copy.add(supporting);
            add(copy, BorderLayout.CENTER);
        }

        void setValue(int number) {
            value.setText(String.valueOf(number));
            value.setForeground(UITheme.NAVY);
        }

        void setUnavailable() {
            value.setText("—");
            value.setForeground(UITheme.TEXT_MUTED);
        }
    }

    private record DashboardSnapshot(
            int patients,
            int doctors,
            int scheduled,
            int completed,
            int unread,
            int bills
    ) {
    }

    private static class ModuleButton extends JButton {
        private final String title;
        private final String description;
        private final UITheme.IconType icon;

        ModuleButton(String title, String description, UITheme.IconType icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);
            setToolTipText(description);
            getAccessibleContext().setAccessibleName(title);
            getAccessibleContext().setAccessibleDescription(description);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = getModel().isPressed()
                    ? new Color(224, 240, 238)
                    : (getModel().isRollover() ? UITheme.PRIMARY_SOFT : UITheme.SURFACE_ALT);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(getModel().isRollover()
                    ? new Color(174, 215, 211)
                    : UITheme.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 16, 16);

            if (hasFocus()) {
                g2.setColor(UITheme.FOCUS);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 14, 14);
            }

            boolean compact = getWidth() < 190;
            int iconX = compact ? 16 : 17;
            int iconY = compact ? 14 : 17;
            g2.setColor(UITheme.PRIMARY_SOFT);
            g2.fillRoundRect(iconX, iconY, 38, 38, 12, 12);
            UITheme.icon(icon, 21, UITheme.PRIMARY).paintIcon(this, g2, iconX + 8, iconY + 8);

            g2.setFont(UITheme.font(Font.BOLD, 14));
            g2.setColor(UITheme.NAVY);
            int textX = compact ? 16 : 68;
            int titleY = compact ? 74 : 31;
            int descriptionY = compact ? 94 : 50;
            int textWidth = compact ? getWidth() - 44 : getWidth() - 96;
            drawFitted(g2, title, textX, titleY, textWidth);
            g2.setFont(UITheme.font(Font.PLAIN, 11));
            g2.setColor(UITheme.TEXT_MUTED);
            drawFitted(g2, description, textX, descriptionY, textWidth);

            g2.setFont(UITheme.font(Font.BOLD, 18));
            g2.setColor(getModel().isRollover() ? UITheme.PRIMARY : UITheme.BORDER_STRONG);
            int arrowY = compact ? getHeight() - 13 : getHeight() / 2 + 6;
            g2.drawString("›", getWidth() - 22, arrowY);
            g2.dispose();
        }

        private void drawFitted(Graphics2D g2, String value, int x, int y, int maxWidth) {
            FontMetrics metrics = g2.getFontMetrics();
            if (metrics.stringWidth(value) <= maxWidth) {
                g2.drawString(value, x, y);
                return;
            }

            String ellipsis = "…";
            int available = Math.max(0, maxWidth - metrics.stringWidth(ellipsis));
            String clipped = value;
            while (!clipped.isEmpty() && metrics.stringWidth(clipped) > available) {
                clipped = clipped.substring(0, clipped.length() - 1);
            }
            g2.drawString(clipped.stripTrailing() + ellipsis, x, y);
        }
    }
}
