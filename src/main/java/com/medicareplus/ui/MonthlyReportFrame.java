package com.medicareplus.ui;

import com.medicareplus.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MonthlyReportFrame extends JFrame {

    private final ReportService reportService = new ReportService();

    public MonthlyReportFrame() {
        setTitle("MediCare Plus - Monthly Reports");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel("Month (YYYY-MM):");
        JTextField txtMonth = new JTextField(10);
        txtMonth.setText("2025-12");
        JButton btnGenerate = new JButton("Generate");

        top.add(lbl);
        top.add(txtMonth);
        top.add(btnGenerate);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            String month = txtMonth.getText().trim();

            if (!month.matches("\\d{4}-\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Enter month like: 2025-12");
                return;
            }

            int total = reportService.countAppointmentsForMonth(month);
            Map<Integer, Integer> perf = reportService.doctorPerformanceForMonth(month);

            StringBuilder sb = new StringBuilder();
            sb.append("MONTHLY REPORT - ").append(month).append("\n");
            sb.append("---------------------------------\n");
            sb.append("Total Appointments: ").append(total).append("\n\n");

            sb.append("Doctor Performance (Doctor ID -> Appointments)\n");
            sb.append("---------------------------------\n");

            if (perf.isEmpty()) {
                sb.append("No data for this month.\n");
            } else {
                for (Map.Entry<Integer, Integer> entry : perf.entrySet()) {
                    sb.append("Doctor ").append(entry.getKey())
                            .append(" -> ").append(entry.getValue()).append("\n");
                }
            }

            area.setText(sb.toString());
        });
    }
}
