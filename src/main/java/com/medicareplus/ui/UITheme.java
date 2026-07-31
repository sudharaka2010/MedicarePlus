package com.medicareplus.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shared, dependency-free visual system for the MedicarePlus Swing application.
 */
public final class UITheme {

    public static final Color CANVAS = new Color(244, 248, 249);
    public static final Color CANVAS_DEEP = new Color(235, 244, 244);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SURFACE_ALT = new Color(248, 250, 252);
    public static final Color PRIMARY = new Color(13, 116, 110);
    public static final Color PRIMARY_DARK = new Color(8, 88, 84);
    public static final Color PRIMARY_HOVER = new Color(11, 101, 96);
    public static final Color PRIMARY_SOFT = new Color(226, 245, 242);
    public static final Color NAVY = new Color(20, 45, 67);
    public static final Color TEXT = new Color(25, 43, 57);
    public static final Color TEXT_MUTED = new Color(99, 116, 130);
    public static final Color BORDER = new Color(218, 228, 232);
    public static final Color BORDER_STRONG = new Color(192, 208, 214);
    public static final Color SUCCESS = new Color(22, 128, 91);
    public static final Color SUCCESS_SOFT = new Color(225, 246, 237);
    public static final Color WARNING = new Color(187, 111, 18);
    public static final Color WARNING_SOFT = new Color(255, 244, 221);
    public static final Color DANGER = new Color(190, 57, 70);
    public static final Color DANGER_DARK = new Color(158, 42, 54);
    public static final Color DANGER_SOFT = new Color(253, 233, 236);
    public static final Color INFO = new Color(45, 101, 170);
    public static final Color INFO_SOFT = new Color(231, 240, 250);
    public static final Color SELECTION = new Color(218, 241, 238);
    public static final Color FOCUS = new Color(9, 139, 133);

    // Semantic aliases keep screen code readable.
    public static final Color TEAL = PRIMARY;
    public static final Color TEAL_DARK = PRIMARY_DARK;
    public static final Color TEXT_PRIMARY = TEXT;
    public static final Color TEXT_SECONDARY = TEXT_MUTED;
    public static final Color MUTED = TEXT_MUTED;

    // 8px spacing system.
    public static final int UNIT = 8;
    public static final int SPACE_1 = 8;
    public static final int SPACE_2 = 16;
    public static final int SPACE_3 = 24;
    public static final int SPACE_4 = 32;
    public static final int SPACE_5 = 40;

    public static final String FONT_FAMILY = findFontFamily();
    private static final String FIELD_FOCUS_INSTALLED = "medicareplus.fieldFocusInstalled";

    private UITheme() {
    }

    public static void install() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The custom components below still provide a consistent appearance.
        }

        Font regular = font(Font.PLAIN, 14);
        Font medium = font(Font.BOLD, 14);

        UIManager.put("Panel.background", CANVAS);
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("Label.font", regular);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.font", medium);
        UIManager.put("TextField.font", regular);
        UIManager.put("TextArea.font", regular);
        UIManager.put("FormattedTextField.font", regular);
        UIManager.put("PasswordField.font", regular);
        UIManager.put("ComboBox.font", regular);
        UIManager.put("CheckBox.font", regular);
        UIManager.put("RadioButton.font", regular);
        UIManager.put("Table.font", regular);
        UIManager.put("TableHeader.font", medium);
        UIManager.put("OptionPane.font", regular);
        UIManager.put("OptionPane.messageFont", regular);
        UIManager.put("OptionPane.buttonFont", medium);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("control", SURFACE);
        UIManager.put("text", TEXT);
        UIManager.put("nimbusSelectionBackground", PRIMARY);
        UIManager.put("TextField.selectionBackground", PRIMARY);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextArea.selectionBackground", PRIMARY);
        UIManager.put("TextArea.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_SOFT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.focusCellHighlightBorder",
                BorderFactory.createLineBorder(FOCUS, 2));
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
        UIManager.put("ToolTip.background", NAVY);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", new EmptyBorder(7, 9, 7, 9));
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    public static void configureFrame(JFrame frame, String title, int width, int height) {
        frame.setTitle("MedicarePlus \u00B7 " + title);
        Rectangle workArea = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int safeWidth = Math.min(width, Math.max(480, workArea.width - 24));
        int safeHeight = Math.min(height, Math.max(360, workArea.height - 24));
        frame.setSize(safeWidth, safeHeight);
        frame.setMinimumSize(new Dimension(
                Math.min(safeWidth, 860),
                Math.min(safeHeight, 560)
        ));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(appIconImage(64));
        frame.getRootPane().putClientProperty("apple.awt.fullWindowContent", true);
        if (!"Dashboard".equalsIgnoreCase(title)) {
            bindShortcut(
                    frame.getRootPane(),
                    KeyStroke.getKeyStroke("ESCAPE"),
                    "returnToDashboard",
                    frame::dispose
            );
        }
    }

    public static Dimension dialogSize(int preferredWidth, int preferredHeight) {
        Rectangle workArea = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int width = Math.max(320, Math.min(preferredWidth, workArea.width - 120));
        int height = Math.max(220, Math.min(preferredHeight, workArea.height - 170));
        return new Dimension(width, height);
    }

    public static JPanel createHeader(String title, String subtitle, JComponent trailing) {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));

        JLabel titleLabel = titleLabel(title, 28);
        JLabel subtitleLabel = mutedLabel(subtitle);

        identity.add(titleLabel);
        identity.add(Box.createVerticalStrut(3));
        identity.add(subtitleLabel);
        header.add(identity, BorderLayout.WEST);

        if (trailing != null) {
            JPanel trailingWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            trailingWrap.setOpaque(false);
            trailingWrap.add(trailing);
            header.add(trailingWrap, BorderLayout.EAST);
        }

        return header;
    }

    public static JLabel titleLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, size));
        label.setForeground(NAVY);
        label.setBorder(new EmptyBorder(0, 0, 0, 10));
        return label;
    }

    public static JLabel titleLabel(String text) {
        return titleLabel(text, 28);
    }

    public static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 17));
        label.setForeground(NAVY);
        label.setBorder(new EmptyBorder(0, 0, 0, 10));
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 13));
        label.setForeground(TEXT_MUTED);
        label.setBorder(new EmptyBorder(0, 0, 0, 16));
        return label;
    }

    public static JLabel recordCountLabel() {
        JLabel label = mutedLabel("0 records");
        label.setBorder(new EmptyBorder(2, 2, 0, 8));
        return label;
    }

    public static void setRecordCount(JLabel label, int count, String singular, String plural) {
        label.setText(count + " " + (count == 1 ? singular : plural));
    }

    public static void styleField(JComponent component) {
        component.setFont(font(Font.PLAIN, 14));
        component.setForeground(TEXT);
        component.setBackground(SURFACE);
        component.setPreferredSize(new Dimension(
                Math.max(component.getPreferredSize().width, 180), 40
        ));

        Border normalBorder = fieldBorder(component, BORDER_STRONG, 1);
        Border focusBorder = fieldBorder(component, FOCUS, 2);
        component.setBorder(component.hasFocus() ? focusBorder : normalBorder);

        if (!Boolean.TRUE.equals(component.getClientProperty(FIELD_FOCUS_INSTALLED))) {
            component.putClientProperty(FIELD_FOCUS_INSTALLED, Boolean.TRUE);
            component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent event) {
                    component.setBorder(focusBorder);
                    component.repaint();
                }

                @Override
                public void focusLost(FocusEvent event) {
                    component.setBorder(normalBorder);
                    component.repaint();
                }
            });
        }

        if (component instanceof JTextComponent textComponent) {
            textComponent.setCaretColor(PRIMARY_DARK);
            textComponent.setSelectionColor(SELECTION);
            textComponent.setSelectedTextColor(TEXT);
        }
    }

    private static Border fieldBorder(JComponent component, Color color, int thickness) {
        Border outline = BorderFactory.createLineBorder(color, thickness);
        if (component instanceof JTextComponent) {
            int vertical = thickness > 1 ? 7 : 8;
            int horizontal = thickness > 1 ? 10 : 11;
            return BorderFactory.createCompoundBorder(
                    outline,
                    new EmptyBorder(vertical, horizontal, vertical, horizontal)
            );
        }
        return outline;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static SearchField searchField(String placeholder) {
        SearchField field = new SearchField(placeholder);
        styleField(field);
        field.setPreferredSize(new Dimension(280, 40));
        return field;
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(font(Font.PLAIN, 14));
        area.setForeground(TEXT);
        area.setBackground(SURFACE);
        area.setCaretColor(PRIMARY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        styleField(area);
        area.setPreferredSize(new Dimension(
                Math.max(area.getPreferredSize().width, 180),
                Math.max(area.getPreferredSize().height, 96)
        ));
    }

    public static JScrollPane textAreaScroll(JTextArea area) {
        styleTextArea(area);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_STRONG));
        scroll.getViewport().setBackground(SURFACE);
        return scroll;
    }

    public static Button button(String text, ButtonStyle style) {
        return new Button(text, null, style);
    }

    public static Button button(String text, IconType icon, ButtonStyle style) {
        return new Button(text, icon, style);
    }

    public static void styleTable(JTable table) {
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT);
        table.setGridColor(BORDER);
        table.setRowHeight(42);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new TableCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 12));
        header.setForeground(TEXT_MUTED);
        header.setBackground(SURFACE_ALT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setReorderingAllowed(false);
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    public static JScrollPane pageScroll(JComponent content) {
        JScrollPane scroll = new JScrollPane(
                content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    public static void setColumnWidths(JTable table, int... widths) {
        int count = Math.min(widths.length, table.getColumnModel().getColumnCount());
        for (int i = 0; i < count; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static class TableView extends JPanel {
        private static final String TABLE_CARD = "table";
        private static final String EMPTY_CARD = "empty";

        private final CardLayout cards = new CardLayout();
        private final String emptyTitle;
        private final String emptyDescription;
        private final JLabel stateTitle;
        private final JLabel stateDescription;

        public TableView(JTable table, IconType iconType,
                         String emptyTitle, String emptyDescription) {
            this.emptyTitle = emptyTitle;
            this.emptyDescription = emptyDescription;
            setLayout(cards);
            setOpaque(false);
            add(tableScroll(table), TABLE_CARD);

            JPanel state = new JPanel(new GridBagLayout());
            state.setBackground(SURFACE);
            state.setBorder(BorderFactory.createLineBorder(BORDER));

            JPanel copy = new JPanel();
            copy.setOpaque(false);
            copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

            JLabel stateIcon = new JLabel(UITheme.icon(iconType, 30, PRIMARY));
            stateIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateIcon.setBorder(new EmptyBorder(0, 0, 12, 0));

            stateTitle = new JLabel(emptyTitle);
            stateTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateTitle.setFont(font(Font.BOLD, 16));
            stateTitle.setForeground(NAVY);

            stateDescription = mutedLabel(emptyDescription);
            stateDescription.setAlignmentX(Component.CENTER_ALIGNMENT);
            stateDescription.setBorder(new EmptyBorder(5, 0, 0, 16));

            copy.add(stateIcon);
            copy.add(stateTitle);
            copy.add(stateDescription);
            state.add(copy);
            add(state, EMPTY_CARD);
            cards.show(this, EMPTY_CARD);
        }

        public void updateState(int totalRows, int visibleRows) {
            if (totalRows == 0) {
                stateTitle.setText(emptyTitle);
                stateDescription.setText(emptyDescription);
                cards.show(this, EMPTY_CARD);
            } else if (visibleRows == 0) {
                stateTitle.setText("No matching results");
                stateDescription.setText("Try a different search or filter.");
                cards.show(this, EMPTY_CARD);
            } else {
                cards.show(this, TABLE_CARD);
            }
        }
    }

    public static void bindShortcut(JComponent component, KeyStroke keyStroke,
                                    String actionName, Runnable action) {
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(keyStroke, actionName);
        component.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Unable to continue",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmDelete(Component parent, String subject) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                "Delete " + subject + "?\nThis action cannot be undone.",
                "Confirm deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    public static Icon icon(IconType type, int size, Color color) {
        return new LineIcon(type, size, color);
    }

    public static Image appIconImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        new BrandIcon(size).paintIcon(null, g2, 0, 0);
        g2.dispose();
        return image;
    }

    public enum ButtonStyle {
        PRIMARY, SECONDARY, GHOST, DANGER
    }

    public enum IconType {
        PATIENTS, DOCTORS, APPOINTMENTS, BILLING, REPORTS, NOTIFICATIONS,
        ASSIGN, SEARCH, REFRESH, ADD, EDIT, DELETE, PRINT, CHECK, CALENDAR,
        DASHBOARD
    }

    public static class BackgroundPanel extends JPanel {
        public BackgroundPanel() {
            setOpaque(true);
            setBackground(CANVAS);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(
                    0, 0, CANVAS,
                    getWidth(), getHeight(), CANVAS_DEEP
            ));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 13));
            int large = Math.max(260, Math.min(getWidth(), getHeight()) / 2);
            g2.fillOval(getWidth() - large / 2, -large / 2, large, large);

            g2.setColor(new Color(INFO.getRed(), INFO.getGreen(), INFO.getBlue(), 9));
            g2.fillOval(-large / 2, getHeight() - large / 2, large, large);
            g2.dispose();
        }
    }

    public static class CardPanel extends JPanel {
        private final int radius;

        public CardPanel() {
            this(18);
        }

        public CardPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBackground(SURFACE);
            setBorder(new EmptyBorder(18, 18, 18, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(19, 51, 62, 10));
            g2.fillRoundRect(1, 3, Math.max(0, getWidth() - 2),
                    Math.max(0, getHeight() - 4), radius, radius);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 2), radius, radius);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1),
                    Math.max(0, getHeight() - 3), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class SearchField extends JTextField {
        private final String placeholder;

        public SearchField(String placeholder) {
            this.placeholder = placeholder == null ? "" : placeholder;
            setToolTipText(this.placeholder);
            getAccessibleContext().setAccessibleName(
                    this.placeholder.isBlank() ? "Search" : this.placeholder
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont());
                g2.setColor(new Color(139, 153, 164));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, insets.left, y);
                g2.dispose();
            }
        }
    }

    public static class Button extends JButton {
        private final ButtonStyle style;

        public Button(String text, IconType iconType, ButtonStyle style) {
            super(text);
            this.style = style;
            setFont(font(Font.BOLD, 13));
            setForeground(foregroundFor(style));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(8);
            setRolloverEnabled(true);
            if (iconType != null) {
                setIcon(UITheme.icon(iconType, 17, foregroundFor(style)));
            }
            Dimension preferred = super.getPreferredSize();
            setPreferredSize(new Dimension(Math.max(112, preferred.width + 12), 40));
            setMinimumSize(new Dimension(Math.min(112, preferred.width), 40));
        }

        private static Color foregroundFor(ButtonStyle style) {
            return switch (style) {
                case PRIMARY, DANGER -> Color.WHITE;
                case SECONDARY -> PRIMARY_DARK;
                case GHOST -> TEXT_MUTED;
            };
        }

        private Color backgroundFor() {
            boolean rollover = getModel().isRollover();
            boolean pressed = getModel().isPressed();
            return switch (style) {
                case PRIMARY -> pressed ? PRIMARY_DARK : (rollover ? PRIMARY_HOVER : PRIMARY);
                case DANGER -> pressed ? DANGER_DARK : (rollover ? DANGER_DARK : DANGER);
                case SECONDARY -> pressed
                        ? new Color(205, 233, 230)
                        : (rollover ? new Color(216, 240, 237) : PRIMARY_SOFT);
                case GHOST -> pressed
                        ? new Color(226, 232, 236)
                        : (rollover ? new Color(237, 242, 244) : SURFACE);
            };
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            setCursor(enabled
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.48f));
            }

            if (hasFocus()) {
                g2.setColor(FOCUS);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 13, 13);
            }

            int inset = hasFocus() ? 3 : 1;
            g2.setColor(backgroundFor());
            g2.fillRoundRect(inset, inset, getWidth() - inset * 2,
                    getHeight() - inset * 2, 11, 11);

            if (style == ButtonStyle.SECONDARY || style == ButtonStyle.GHOST) {
                g2.setColor(style == ButtonStyle.SECONDARY ? new Color(185, 220, 216) : BORDER);
                g2.drawRoundRect(inset, inset, getWidth() - inset * 2 - 1,
                        getHeight() - inset * 2 - 1, 11, 11);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class FormBuilder extends JPanel {
        private int row;

        public FormBuilder() {
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(6, 4, 6, 4));
        }

        public FormBuilder addField(String labelText, JComponent component) {
            return addField(labelText, component, null);
        }

        public FormBuilder addField(String labelText, JComponent component, String hint) {
            styleField(component);

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = row;
            labelConstraints.weightx = 0;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(7, 3, hint == null ? 7 : 2, 16);

            JLabel label = new JLabel(labelText);
            label.setFont(font(Font.BOLD, 12));
            label.setForeground(TEXT_MUTED);
            label.setLabelFor(component);
            add(label, labelConstraints);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = row;
            fieldConstraints.weightx = 1;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(5, 0, hint == null ? 5 : 0, 3);
            add(component, fieldConstraints);
            row++;

            if (hint != null && !hint.isBlank()) {
                GridBagConstraints hintConstraints = new GridBagConstraints();
                hintConstraints.gridx = 1;
                hintConstraints.gridy = row;
                hintConstraints.weightx = 1;
                hintConstraints.anchor = GridBagConstraints.WEST;
                hintConstraints.insets = new Insets(0, 2, 5, 3);
                add(mutedLabel(hint), hintConstraints);
                row++;
            }
            return this;
        }

        public FormBuilder addFullWidth(JComponent component) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = row++;
            constraints.gridwidth = 2;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(6, 3, 6, 3);
            add(component, constraints);
            return this;
        }
    }

    public static class TableCellRenderer extends DefaultTableCellRenderer {
        public TableCellRenderer() {
            setBorder(new EmptyBorder(0, 11, 0, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(font(Font.PLAIN, 13));
            setForeground(TEXT);
            setBackground(isSelected
                    ? SELECTION
                    : (row % 2 == 0 ? SURFACE : SURFACE_ALT));
            setBorder(hasFocus
                    ? BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FOCUS, 2),
                    new EmptyBorder(0, 9, 0, 9))
                    : new EmptyBorder(0, 11, 0, 11));
            setText(value == null || value.toString().isBlank() ? "\u2014" : value.toString());
            setToolTipText(value == null ? null : value.toString());
            return this;
        }
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private String label = "";
        private Color rowBackground = SURFACE;
        private Color pillBackground = INFO_SOFT;
        private Color pillForeground = INFO;
        private boolean cellFocused;

        public StatusBadgeRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            label = value == null ? "Unknown" : value.toString();
            setText(label);
            cellFocused = hasFocus;
            rowBackground = isSelected
                    ? SELECTION
                    : (row % 2 == 0 ? SURFACE : SURFACE_ALT);
            chooseBadgeColors(label);
            setToolTipText(label);
            return this;
        }

        private void chooseBadgeColors(String value) {
            String normalized = value.toLowerCase();
            if (normalized.contains("complete") || normalized.equals("read")
                    || normalized.equals("yes") || normalized.contains("paid")) {
                pillBackground = SUCCESS_SOFT;
                pillForeground = SUCCESS;
            } else if (normalized.contains("cancel") || normalized.equals("unread")
                    || normalized.equals("no") || normalized.contains("failed")) {
                pillBackground = DANGER_SOFT;
                pillForeground = DANGER;
            } else if (normalized.contains("delay") || normalized.contains("pending")) {
                pillBackground = WARNING_SOFT;
                pillForeground = WARNING;
            } else {
                pillBackground = INFO_SOFT;
                pillForeground = INFO;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(rowBackground);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setFont(font(Font.BOLD, 11));
            FontMetrics metrics = g2.getFontMetrics();
            int pillWidth = Math.min(getWidth() - 12, Math.max(62, metrics.stringWidth(label) + 22));
            int pillHeight = 24;
            int x = (getWidth() - pillWidth) / 2;
            int y = (getHeight() - pillHeight) / 2;
            g2.setColor(pillBackground);
            g2.fillRoundRect(x, y, pillWidth, pillHeight, 18, 18);
            g2.setColor(pillForeground);
            int textX = x + (pillWidth - metrics.stringWidth(label)) / 2;
            int textY = y + (pillHeight - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(label, textX, textY);
            if (cellFocused) {
                g2.setColor(FOCUS);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(1, 1, Math.max(0, getWidth() - 3),
                        Math.max(0, getHeight() - 3));
            }
            g2.dispose();
        }
    }

    public static class BrandIcon implements Icon {
        private final int size;

        public BrandIcon(int size) {
            this.size = size;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setPaint(new GradientPaint(0, 0, new Color(21, 148, 139),
                    size, size, PRIMARY_DARK));
            g2.fill(new RoundRectangle2D.Float(0, 0, size, size,
                    size * .3f, size * .3f));

            float bar = size * .19f;
            float start = size * .22f;
            float length = size * .56f;
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(
                    size / 2f - bar / 2f, start, bar, length, bar, bar));
            g2.fill(new RoundRectangle2D.Float(
                    start, size / 2f - bar / 2f, length, bar, bar, bar));
            g2.dispose();
        }
    }

    public static class LineIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color color;

        public LineIcon(IconType type, int size, Color color) {
            this.type = type;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = size / 24.0;
            g2.translate(x, y);
            g2.scale(scale, scale);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));

            switch (type) {
                case PATIENTS -> drawPeople(g2, false);
                case DOCTORS -> drawPeople(g2, true);
                case APPOINTMENTS, CALENDAR -> drawCalendar(g2);
                case BILLING -> drawReceipt(g2);
                case REPORTS -> drawReport(g2);
                case NOTIFICATIONS -> drawBell(g2);
                case ASSIGN -> drawAssign(g2);
                case SEARCH -> {
                    g2.draw(new Ellipse2D.Float(4, 4, 11, 11));
                    g2.drawLine(14, 14, 20, 20);
                }
                case REFRESH -> {
                    g2.draw(new Arc2D.Float(4, 4, 16, 16, 35, 285, Arc2D.OPEN));
                    g2.drawLine(18, 4, 20, 9);
                    g2.drawLine(20, 9, 15, 8);
                }
                case ADD -> {
                    g2.drawLine(12, 5, 12, 19);
                    g2.drawLine(5, 12, 19, 12);
                }
                case EDIT -> {
                    g2.drawLine(5, 19, 9, 18);
                    g2.drawLine(6, 15, 15, 6);
                    g2.drawLine(9, 18, 18, 9);
                    g2.drawLine(15, 6, 18, 9);
                }
                case DELETE -> {
                    g2.drawRoundRect(7, 7, 10, 13, 2, 2);
                    g2.drawLine(5, 6, 19, 6);
                    g2.drawLine(10, 3, 14, 3);
                    g2.drawLine(10, 10, 10, 17);
                    g2.drawLine(14, 10, 14, 17);
                }
                case PRINT -> {
                    g2.drawRoundRect(4, 8, 16, 9, 2, 2);
                    g2.drawRect(7, 3, 10, 6);
                    g2.drawRect(7, 14, 10, 7);
                    g2.fillOval(16, 11, 2, 2);
                }
                case CHECK -> {
                    Path2D path = new Path2D.Float();
                    path.moveTo(4, 12);
                    path.lineTo(9, 17);
                    path.lineTo(20, 6);
                    g2.draw(path);
                }
                case DASHBOARD -> {
                    g2.drawRoundRect(3, 3, 7, 7, 2, 2);
                    g2.drawRoundRect(14, 3, 7, 7, 2, 2);
                    g2.drawRoundRect(3, 14, 7, 7, 2, 2);
                    g2.drawRoundRect(14, 14, 7, 7, 2, 2);
                }
            }
            g2.dispose();
        }

        private static void drawPeople(Graphics2D g2, boolean doctor) {
            g2.drawOval(8, 3, 8, 8);
            g2.drawArc(4, 11, 16, 11, 15, 150);
            if (doctor) {
                g2.drawArc(8, 12, 8, 7, 180, 180);
                g2.drawLine(8, 13, 6, 17);
                g2.drawLine(16, 13, 18, 17);
                g2.drawOval(17, 16, 3, 3);
            } else {
                g2.drawOval(2, 7, 5, 5);
                g2.drawOval(17, 7, 5, 5);
            }
        }

        private static void drawCalendar(Graphics2D g2) {
            g2.drawRoundRect(3, 5, 18, 16, 3, 3);
            g2.drawLine(3, 10, 21, 10);
            g2.drawLine(8, 3, 8, 7);
            g2.drawLine(16, 3, 16, 7);
            g2.drawLine(8, 14, 11, 14);
            g2.drawLine(14, 14, 17, 14);
            g2.drawLine(8, 18, 11, 18);
        }

        private static void drawReceipt(Graphics2D g2) {
            Path2D receipt = new Path2D.Float();
            receipt.moveTo(5, 3);
            receipt.lineTo(19, 3);
            receipt.lineTo(19, 21);
            receipt.lineTo(16, 19);
            receipt.lineTo(13, 21);
            receipt.lineTo(10, 19);
            receipt.lineTo(7, 21);
            receipt.lineTo(5, 19);
            receipt.closePath();
            g2.draw(receipt);
            g2.drawLine(9, 8, 15, 8);
            g2.drawLine(9, 12, 15, 12);
            g2.drawLine(9, 16, 13, 16);
        }

        private static void drawReport(Graphics2D g2) {
            g2.drawRoundRect(4, 3, 16, 18, 3, 3);
            g2.drawLine(8, 17, 8, 13);
            g2.drawLine(12, 17, 12, 9);
            g2.drawLine(16, 17, 16, 6);
        }

        private static void drawBell(Graphics2D g2) {
            Path2D bell = new Path2D.Float();
            bell.moveTo(5, 17);
            bell.curveTo(7, 14, 7, 11, 7, 9);
            bell.curveTo(7, 3, 17, 3, 17, 9);
            bell.curveTo(17, 12, 17, 14, 19, 17);
            bell.closePath();
            g2.draw(bell);
            g2.drawArc(9, 17, 6, 4, 180, 180);
        }

        private static void drawAssign(Graphics2D g2) {
            g2.drawOval(3, 4, 7, 7);
            g2.drawArc(1, 12, 11, 9, 10, 160);
            g2.drawLine(13, 9, 21, 9);
            g2.drawLine(18, 6, 21, 9);
            g2.drawLine(18, 12, 21, 9);
        }
    }

    private static String findFontFamily() {
        String[] preferred = {"Dialog", "Segoe UI", "Inter"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String candidate : preferred) {
            for (String family : available) {
                if (family.equalsIgnoreCase(candidate)) {
                    return family;
                }
            }
        }
        return Font.SANS_SERIF;
    }
}
