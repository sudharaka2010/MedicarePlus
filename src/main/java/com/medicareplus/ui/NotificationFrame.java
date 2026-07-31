package com.medicareplus.ui;

import com.medicareplus.dao.NotificationDAO;
import com.medicareplus.dao.DoctorDAO;
import com.medicareplus.dao.PatientDAO;
import com.medicareplus.model.Doctor;
import com.medicareplus.model.Notification;
import com.medicareplus.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NotificationFrame extends JFrame {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private JTable table;
    private DefaultTableModel model;
    private UITheme.SearchField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private UITheme.Button markReadButton;
    private JCheckBox unreadOnlyCheck;
    private JLabel recordCountLabel;
    private UITheme.TableView tableView;

    public NotificationFrame() {
        UITheme.configureFrame(this, "Notifications", 1080, 680);

        UITheme.BackgroundPanel root = new UITheme.BackgroundPanel();
        root.setLayout(new BorderLayout(0, 20));
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        searchField = UITheme.searchField("Search notifications");
        root.add(UITheme.createHeader(
                "Notifications",
                "Review patient and clinician updates in one focused inbox.",
                searchField
        ), BorderLayout.NORTH);

        UITheme.CardPanel card = new UITheme.CardPanel();
        card.setLayout(new BorderLayout(0, 14));

        markReadButton = UITheme.button(
                "Mark as read", UITheme.IconType.CHECK, UITheme.ButtonStyle.PRIMARY);
        UITheme.Button refreshButton = UITheme.button(
                "Refresh", UITheme.IconType.REFRESH, UITheme.ButtonStyle.GHOST);
        markReadButton.setEnabled(false);
        markReadButton.setToolTipText("Select an unread notification");
        refreshButton.setToolTipText("Reload notifications (Ctrl/Cmd+R)");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(markReadButton);
        actions.add(refreshButton);

        unreadOnlyCheck = new JCheckBox("Unread only");
        unreadOnlyCheck.setOpaque(false);
        unreadOnlyCheck.setFont(UITheme.font(Font.BOLD, 12));
        unreadOnlyCheck.setForeground(UITheme.TEXT_MUTED);
        unreadOnlyCheck.setToolTipText("Show only notifications that still need attention");

        recordCountLabel = UITheme.recordCountLabel();
        JPanel statusControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 7));
        statusControls.setOpaque(false);
        statusControls.add(unreadOnlyCheck);
        statusControls.add(recordCountLabel);

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);
        toolbar.add(actions, BorderLayout.WEST);
        toolbar.add(statusControls, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"ID", "Recipient", "Message", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int modelRow = convertRowIndexToModel(row);
                boolean unread = "Unread".equals(model.getValueAt(modelRow, 3));
                if (!isRowSelected(row) && column != 3) {
                    component.setBackground(unread
                            ? UITheme.PRIMARY_SOFT
                            : (row % 2 == 0 ? UITheme.SURFACE : UITheme.SURFACE_ALT));
                    component.setForeground(UITheme.TEXT);
                    component.setFont(UITheme.font(unread ? Font.BOLD : Font.PLAIN, 13));
                }
                return component;
            }
        };
        UITheme.styleTable(table);
        UITheme.setColumnWidths(table, 58, 220, 570, 100);
        table.getColumnModel().getColumn(3).setCellRenderer(new UITheme.StatusBadgeRenderer());
        table.setAutoCreateRowSorter(false);
        tableView = new UITheme.TableView(
                table,
                UITheme.IconType.NOTIFICATIONS,
                "No notifications",
                "Patient and clinician updates will appear here."
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
        unreadOnlyCheck.addActionListener(e -> applyFilter());
        sorter.addRowSorterListener(e -> updateRecordCount());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionAction();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
                        && table.getSelectedRow() >= 0) {
                    showSelectedNotification();
                }
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "openNotification"
        );
        table.getActionMap().put("openNotification", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSelectedNotification();
            }
        });

        markReadButton.addActionListener(e -> markSelectedAsRead());
        refreshButton.addActionListener(e -> loadNotifications());

        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask),
                "focusNotificationSearch", () -> {
                    searchField.requestFocusInWindow();
                    searchField.selectAll();
                });
        UITheme.bindShortcut(getRootPane(),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, menuMask),
                "refreshNotifications", this::loadNotifications);

        loadNotifications();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        String text = searchField.getText().trim();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
        if (unreadOnlyCheck.isSelected()) {
            filters.add(RowFilter.regexFilter("^Unread$", 3));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
        updateRecordCount();
    }

    private void loadNotifications() {
        Map<Integer, String> patientNames = new HashMap<>();
        for (Patient patient : patientDAO.getAllPatients()) {
            patientNames.put(patient.getPatientId(), patient.getFullName());
        }
        Map<Integer, String> doctorNames = new HashMap<>();
        for (Doctor doctor : doctorDAO.getAllDoctors()) {
            doctorNames.put(doctor.getDoctorId(), doctor.getFullName());
        }

        model.setRowCount(0);
        for (Notification notification : notificationDAO.getAllNotifications()) {
            model.addRow(new Object[]{
                    notification.getNotificationId(),
                    recipientLabel(notification, patientNames, doctorNames),
                    notification.getMessage(),
                    notification.getIsRead() == 1 ? "Read" : "Unread"
            });
        }

        table.clearSelection();
        updateSelectionAction();
        applyFilter();
    }

    private void updateRecordCount() {
        int unread = 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            if ("Unread".equals(model.getValueAt(row, 3))) {
                unread++;
            }
        }
        recordCountLabel.setText(
                table.getRowCount() + " shown  ·  " + unread + " unread  ·  "
                        + model.getRowCount() + " total"
        );
        tableView.updateState(model.getRowCount(), table.getRowCount());
    }

    private void updateSelectionAction() {
        markReadButton.setEnabled(isSelectedNotificationUnread());
    }

    private boolean isSelectedNotificationUnread() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return false;
        }
        int row = table.convertRowIndexToModel(viewRow);
        return "Unread".equals(model.getValueAt(row, 3));
    }

    private void markSelectedAsRead() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        if (!"Unread".equals(model.getValueAt(row, 3))) {
            return;
        }
        int id = ((Number) model.getValueAt(row, 0)).intValue();

        if (notificationDAO.markAsRead(id)) {
            UITheme.showSuccess(this, "Notification marked as read.");
            loadNotifications();
        } else {
            UITheme.showError(this, "The notification could not be updated.");
        }
    }

    private String recipientLabel(
            Notification notification,
            Map<Integer, String> patientNames,
            Map<Integer, String> doctorNames
    ) {
        boolean patient = "Patient".equalsIgnoreCase(notification.getReceiverType());
        Map<Integer, String> names = patient ? patientNames : doctorNames;
        String role = patient ? "Patient" : "Doctor";
        String name = names.get(notification.getReceiverId());
        return (name == null || name.isBlank() ? role : name)
                + "  ·  " + role + " #" + notification.getReceiverId();
    }

    private void showSelectedNotification() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        String recipient = String.valueOf(model.getValueAt(row, 1));
        String message = String.valueOf(model.getValueAt(row, 2));
        boolean unread = "Unread".equals(model.getValueAt(row, 3));

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        JLabel recipientLabel = UITheme.sectionLabel(recipient);
        JTextArea messageArea = new JTextArea(message, 6, 38);
        messageArea.setEditable(false);
        messageArea.setFocusable(true);
        JScrollPane messageScroll = UITheme.textAreaScroll(messageArea);
        messageScroll.setPreferredSize(new Dimension(520, 170));
        content.add(recipientLabel, BorderLayout.NORTH);
        content.add(messageScroll, BorderLayout.CENTER);

        Object[] options = unread
                ? new Object[]{"Mark as read", "Close"}
                : new Object[]{"Close"};
        int result = JOptionPane.showOptionDialog(
                this,
                content,
                "Notification details",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[options.length - 1]
        );
        if (unread && result == 0) {
            markSelectedAsRead();
        }
    }
}
