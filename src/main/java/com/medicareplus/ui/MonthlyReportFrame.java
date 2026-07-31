package com.medicareplus.ui;

import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.model.Doctor;
import com.medicareplus.service.ReportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MonthlyReportFrame extends JFrame {

    private static final DateTimeFormatter PERIOD_LABEL =
            DateTimeFormatter.ofPattern("MMMM yyyy");

    private final ReportService reportService = new ReportService();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTextField monthField;
    private UITheme.SearchField searchField;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel totalValue;
    private JLabel doctorsValue;
    private JLabel averageValue;
    private JLabel topDoctorValue;
    private JLabel reportPeriodLabel;
    private JLabel recordCountLabel;
    private UITheme.TableView tableView;

    public MonthlyReportFrame() {
        UITheme.configureFrame(this, "Monthly Reports", 1060, 720);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(0, 20));
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        searchField = UITheme.searchField("Search doctors");
        root.add(UITheme.createHeader(
                "Monthly reports",
                "Review appointment volume and care-team workload for any month.",
                searchField
        ), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        UITheme.CardPanel periodCard = new UITheme.CardPanel();
        periodCard.setLayout(new BorderLayout(18, 0));

        JPanel periodIdentity = new JPanel();
        periodIdentity.setOpaque(false);
        periodIdentity.setLayout(new BoxLayout(periodIdentity, BoxLayout.Y_AXIS));
        periodIdentity.add(UITheme.sectionLabel("Reporting period"));
        JLabel periodHint = UITheme.mutedLabel(
                "Choose a month in YYYY-MM format to refresh every metric.");
        periodHint.setBorder(new EmptyBorder(3, 0, 0, 0));
        periodIdentity.add(periodHint);
        periodCard.add(periodIdentity, BorderLayout.WEST);

        monthField = UITheme.textField();
        monthField.setText(YearMonth.now().toString());
        monthField.setPreferredSize(new Dimension(150, 40));
        monthField.setToolTipText("Reporting month in YYYY-MM format");
        monthField.getAccessibleContext().setAccessibleName("Reporting month");

        JLabel monthLabel = new JLabel("Month");
        monthLabel.setFont(UITheme.font(Font.BOLD, 12));
        monthLabel.setForeground(UITheme.TEXT_MUTED);
        monthLabel.setLabelFor(monthField);

        UITheme.Button generateButton = UITheme.button(
                "Generate report", UITheme.IconType.REPORTS, UITheme.ButtonStyle.PRIMARY);
        UITheme.Button currentMonthButton = UITheme.button(
                "Current month", UITheme.IconType.CALENDAR, UITheme.ButtonStyle.GHOST);
        generateButton.setToolTipText("Generate the report (Enter)");
        currentMonthButton.setToolTipText("Return to the current month");

        JPanel periodControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        periodControls.setOpaque(false);
        periodControls.add(monthLabel);
        periodControls.add(monthField);
        periodControls.add(currentMonthButton);
        periodControls.add(generateButton);
        periodCard.add(periodControls, BorderLayout.EAST);
        body.add(periodCard, BorderLayout.NORTH);

        JPanel results = new JPanel(new BorderLayout(0, 16));
        results.setOpaque(false);

        totalValue = metricValue();
        doctorsValue = metricValue();
        averageValue = metricValue();
        topDoctorValue = metricValue();
        topDoctorValue.setFont(UITheme.font(Font.BOLD, 18));

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(createMetricCard(
                "Appointments", totalValue, "Total visits in period", UITheme.PRIMARY));
        metrics.add(createMetricCard(
                "Active doctors", doctorsValue, "Doctors with appointments", UITheme.INFO));
        metrics.add(createMetricCard(
                "Average load", averageValue, "Appointments per doctor", UITheme.WARNING));
        metrics.add(createMetricCard(
                "Most booked", topDoctorValue, "Highest appointment volume", UITheme.SUCCESS));
        results.add(metrics, BorderLayout.NORTH);

        UITheme.CardPanel performanceCard = new UITheme.CardPanel();
        performanceCard.setLayout(new BorderLayout(0, 12));

        reportPeriodLabel = UITheme.mutedLabel("Appointment volume by doctor");
        recordCountLabel = UITheme.recordCountLabel();

        JPanel tableHeadingText = new JPanel();
        tableHeadingText.setOpaque(false);
        tableHeadingText.setLayout(new BoxLayout(tableHeadingText, BoxLayout.Y_AXIS));
        tableHeadingText.add(UITheme.sectionLabel("Appointment volume by doctor"));
        tableHeadingText.add(reportPeriodLabel);

        JPanel tableHeading = new JPanel(new BorderLayout(12, 0));
        tableHeading.setOpaque(false);
        tableHeading.add(tableHeadingText, BorderLayout.WEST);
        tableHeading.add(recordCountLabel, BorderLayout.EAST);
        performanceCard.add(tableHeading, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Rank", "Doctor", "Appointments", "Share of activity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 2 -> Integer.class;
                    case 3 -> Double.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        UITheme.setColumnWidths(table, 70, 260, 150, 190);
        table.getColumnModel().getColumn(3).setCellRenderer(new PercentageRenderer());
        table.setAutoCreateRowSorter(false);
        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.REPORTS,
                "No appointment activity",
                "Choose another month or schedule appointments for this period."
        );
        performanceCard.add(tableView, BorderLayout.CENTER);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        results.add(performanceCard, BorderLayout.CENTER);
        body.add(results, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
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

        generateButton.addActionListener(e -> generateReport());
        currentMonthButton.addActionListener(e -> {
            monthField.setText(YearMonth.now().toString());
            generateReport();
        });
        getRootPane().setDefaultButton(generateButton);

        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusReportSearch", () -> {
                    searchField.requestFocusInWindow();
                    searchField.selectAll();
                });
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshReport", this::generateReport);

        generateReport();
    }

    private JLabel metricValue() {
        JLabel label = new JLabel("0");
        label.setFont(UITheme.font(Font.BOLD, 28));
        label.setForeground(UITheme.NAVY);
        return label;
    }

    private JPanel createMetricCard(
            String title, JLabel value, String supportingText, Color accent) {
        UITheme.CardPanel card = new UITheme.CardPanel(16);
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLabel = new JLabel(title.toUpperCase(Locale.ROOT));
        titleLabel.setFont(UITheme.font(Font.BOLD, 11));
        titleLabel.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(UITheme.mutedLabel(supportingText), BorderLayout.SOUTH);
        return card;
    }

    private void generateReport() {
        String month = monthField.getText().trim();
        YearMonth period;
        try {
            period = YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            UITheme.showError(this, "Enter a valid reporting month in YYYY-MM format.");
            monthField.requestFocusInWindow();
            monthField.selectAll();
            return;
        }

        int total = reportService.countAppointmentsForMonth(month);
        Map<Integer, Integer> performance =
                reportService.doctorPerformanceForMonth(month);
        Map<Integer, String> doctorNames = new HashMap<>();
        for (Doctor doctor : doctorDAO.getAllDoctors()) {
            doctorNames.put(doctor.getDoctorId(), doctor.getFullName());
        }

        totalValue.setText(String.valueOf(total));
        doctorsValue.setText(String.valueOf(performance.size()));
        averageValue.setText(performance.isEmpty()
                ? "0.0"
                : String.format(Locale.ROOT, "%.1f", (double) total / performance.size()));

        Map.Entry<Integer, Integer> top = performance.entrySet().stream()
                .findFirst()
                .orElse(null);
        topDoctorValue.setText(top == null
                ? "—"
                : doctorNames.getOrDefault(top.getKey(), "Doctor #" + top.getKey()));
        topDoctorValue.setToolTipText(topDoctorValue.getText());

        model.setRowCount(0);
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : performance.entrySet()) {
            double share = total == 0 ? 0 : entry.getValue() * 100.0 / total;
            model.addRow(new Object[]{
                    rank++,
                    doctorNames.getOrDefault(entry.getKey(), "Doctor")
                            + "  ·  #" + entry.getKey(),
                    entry.getValue(),
                    share
            });
        }

        reportPeriodLabel.setText("Results for " + period.format(PERIOD_LABEL));
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        updateRecordCount();
    }

    private void updateRecordCount() {
        int total = model.getRowCount();
        int visible = table.getRowCount();
        if (visible == total) {
            UITheme.setRecordCount(recordCountLabel, total, "doctor", "doctors");
        } else {
            recordCountLabel.setText(visible + " shown  ·  " + total + " total");
        }
        tableView.updateState(total, visible);
    }

    private static final class PercentageRenderer extends UITheme.TableCellRenderer {
        private PercentageRenderer() {
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
                setText(String.format(Locale.ROOT, "%.1f%%", number.doubleValue()));
            }
            return component;
        }
    }
}
