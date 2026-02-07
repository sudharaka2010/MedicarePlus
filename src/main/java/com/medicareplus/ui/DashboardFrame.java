package com.medicareplus.ui;

import com.medicareplus.model.Doctor;
import com.medicareplus.service.AssignmentService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class DashboardFrame extends JFrame {

    public DashboardFrame() {
        setTitle("MediCare Plus - Dashboard");
        setSize(980, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main background panel (with transparent image)
        BackgroundPanel root = new BackgroundPanel("/images/dashboard_bg.png", 0.18f);
        root.setLayout(new BorderLayout(20, 20));
        root.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ---------- HEADER (Title LEFT + Logo RIGHT) ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("MediCare Plus", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(25, 25, 25));

        JLabel subtitle = new JLabel("Hospital Management Dashboard");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(70, 70, 70));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);

        // RIGHT side logo
        JLabel logoLabel = new JLabel();
        logoLabel.setOpaque(false);
        logoLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
            Image scaledLogo = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception e) {
            System.out.println("Logo not found! Put it in: src/main/resources/images/logo.png");
        }

        header.add(logoLabel, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Center: card panel (semi-transparent white)
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);

        // ✅ CHANGED: 4 rows x 2 columns (can show up to 8 buttons)
        JPanel glass = new JPanel(new GridLayout(4, 2, 16, 16));
        glass.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        glass.setBackground(new Color(255, 255, 255, 210));
        glass.setOpaque(true);

        // Buttons (use PNG icons from resources/icons/)
        ModernButton btnPatients = new ModernButton("Manage Patients", "/icons/patients.png");
        ModernButton btnDoctors = new ModernButton("Manage Doctors", "/icons/doctors.png");
        ModernButton btnAppointments = new ModernButton("Appointments", "/icons/appointment.png");
        ModernButton btnReports = new ModernButton("Monthly Reports", "/icons/report.png");
        ModernButton btnBilling = new ModernButton("Billing", "/icons/billing.png"); // ✅ NEW
        ModernButton btnAutoAssign = new ModernButton("Auto Assign Doctor", "/icons/assign.png");
        ModernButton btnNotifications = new ModernButton("Notifications", "/icons/notification.png");

        // Actions
        btnPatients.addActionListener(e -> new PatientManagementFrame().setVisible(true));
        btnDoctors.addActionListener(e -> new DoctorManagementFrame().setVisible(true));
        btnAppointments.addActionListener(e -> new AppointmentManagementFrame().setVisible(true));
        btnReports.addActionListener(e -> new MonthlyReportFrame().setVisible(true));
        btnBilling.addActionListener(e -> new BillingFrame().setVisible(true)); // ✅ NEW
        btnAutoAssign.addActionListener(e -> autoAssignDoctor());
        btnNotifications.addActionListener(e -> new NotificationFrame().setVisible(true));

        // Add buttons to grid
        glass.add(btnPatients);
        glass.add(btnDoctors);
        glass.add(btnAppointments);
        glass.add(btnReports);
        glass.add(btnBilling);        // ✅ NEW
        glass.add(btnAutoAssign);
        glass.add(btnNotifications);

        // Optional: filler to keep grid balanced (since 7 buttons in 8 slots)
        glass.add(new JLabel(""));

        // Wrap glass in rounded panel
        RoundedPanel rounded = new RoundedPanel(22);
        rounded.setLayout(new BorderLayout());
        rounded.setBackground(new Color(255, 255, 255, 200));
        rounded.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        rounded.add(glass, BorderLayout.CENTER);

        card.add(rounded, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void autoAssignDoctor() {
        String specialty = JOptionPane.showInputDialog(
                this,
                "Enter required specialty (example: Cardiologist):",
                "Auto Assign Doctor",
                JOptionPane.QUESTION_MESSAGE
        );

        if (specialty == null || specialty.trim().isEmpty()) return;

        AssignmentService service = new AssignmentService();
        Doctor doctor = service.autoAssignDoctor(specialty);

        if (doctor == null) {
            JOptionPane.showMessageDialog(this, "No doctor found for specialty: " + specialty);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Assigned Doctor:\n" +
                            "ID: " + doctor.getDoctorId() + "\n" +
                            "Name: " + doctor.getFullName() + "\n" +
                            "Specialty: " + doctor.getSpecialty()
            );
        }
    }

    // ---------- UI Helpers ----------

    static class BackgroundPanel extends JPanel {
        private final Image bg;
        private final float alpha;

        public BackgroundPanel(String resourcePath, float alpha) {
            this.alpha = alpha;
            Image temp = null;
            try {
                URL url = getClass().getResource(resourcePath);
                if (url != null) temp = new ImageIcon(url).getImage();
            } catch (Exception ignored) {}
            this.bg = temp;
            setBackground(new Color(245, 246, 250));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            GradientPaint gp = new GradientPaint(0, 0, new Color(245, 246, 250),
                    0, getHeight(), new Color(235, 238, 245));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (bg != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                int iw = bg.getWidth(null);
                int ih = bg.getHeight(null);

                double sx = (double) getWidth() / iw;
                double sy = (double) getHeight() / ih;
                double s = Math.max(sx, sy);

                int w = (int) (iw * s);
                int h = (int) (ih * s);
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;

                g2.drawImage(bg, x, y, w, h, null);
            }
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

        public ModernButton(String text, String iconPath) {
            super(text);

            try {
                if (iconPath != null) {
                    ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                    Image scaled = icon.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
                    setIcon(new ImageIcon(scaled));
                    setHorizontalTextPosition(SwingConstants.RIGHT);
                    setIconTextGap(14);
                }
            } catch (Exception ignored) {}

            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(240, 72));

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

            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 10, 18, 18);

            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
