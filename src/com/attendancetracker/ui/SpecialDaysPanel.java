package com.attendancetracker.ui;

import com.attendancetracker.dao.HolidayDAO;
import com.attendancetracker.dao.SpecialSaturdayDAO;
import com.attendancetracker.model.Holiday;
import com.attendancetracker.model.SpecialSaturday;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SpecialDaysPanel extends JPanel {

    private final HolidayDAO holidayDAO           = new HolidayDAO();
    private final SpecialSaturdayDAO saturdayDAO  = new SpecialSaturdayDAO();

    // Holiday form
    private JTextField holidayDateField, holidayReasonField;
    private DefaultTableModel holidayTableModel;
    private JLabel holidayStatus;

    // Special Saturday form
    private JTextField satDateField;
    private JComboBox<String> followsDayBox;
    private DefaultTableModel satTableModel;
    private JLabel satStatus;

    public SpecialDaysPanel() {
        setBackground(UIConstants.BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(24, 32, 24, 32));
        build();
    }

    private void build() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG);

        JLabel pageTitle = UIConstants.label("Special Days", UIConstants.FONT_TITLE, UIConstants.TEXT);
        JLabel subtitle  = UIConstants.label(
                "Add national holidays and special working Saturdays from your semester schedule.",
                UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(pageTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        // Holidays section
        content.add(buildHolidaySection());
        content.add(Box.createVerticalStrut(24));

        // Special Saturdays section
        content.add(buildSaturdaySection());

        add(content, BorderLayout.NORTH);
    }

    // ===================== HOLIDAYS =====================

    private JPanel buildHolidaySection() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(UIConstants.BG);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add form
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UIConstants.SURFACE);
        formCard.setBorder(UIConstants.sectionBorder("National Holidays / College Off Days"));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = makeGBC();

        gbc.gridy = 0; gbc.gridx = 0;
        formCard.add(UIConstants.label("Date (YYYY-MM-DD)", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        holidayDateField = UIConstants.textField(14);
        formCard.add(holidayDateField, gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        formCard.add(UIConstants.label("Reason / Name", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        holidayReasonField = UIConstants.textField(22);
        formCard.add(holidayReasonField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(UIConstants.SURFACE);
        JButton addBtn = UIConstants.primaryButton("  Add Holiday  ");
        addBtn.addActionListener(e -> addHoliday());
        btnRow.add(addBtn);
        btnRow.add(Box.createHorizontalStrut(12));
        holidayStatus = UIConstants.label("", UIConstants.FONT_BODY, UIConstants.SUCCESS);
        btnRow.add(holidayStatus);
        formCard.add(btnRow, gbc);
        outer.add(formCard);
        outer.add(Box.createVerticalStrut(12));

        // Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(UIConstants.SURFACE);
        tableCard.setBorder(UIConstants.sectionBorder("Added Holidays"));
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        holidayTableModel = new DefaultTableModel(new String[]{"ID", "Date", "Reason", "Remove"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable holidayTable = buildTable(holidayTableModel,
                new int[]{40, 120, 260, 80},
                () -> deleteSelectedHoliday(holidayTable()));

        // Store reference via array trick
        JTable[] ref = {holidayTable};
        holidayTable.getColumn("Remove").setCellRenderer(new CoursesPanel.ButtonRenderer("Remove"));
        holidayTable.getColumn("Remove").setCellEditor(
            new CoursesPanel.ButtonEditor(new JCheckBox(), () -> deleteSelectedHoliday(ref[0])));

        JScrollPane sp = new JScrollPane(holidayTable);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        sp.setPreferredSize(new Dimension(0, 160));
        tableCard.add(sp);
        outer.add(tableCard);

        refreshHolidayTable();
        return outer;
    }

    private JTable holidayTableRef;

    private JTable holidayTable() { return holidayTableRef; }

    private JTable buildTable(DefaultTableModel model, int[] widths, Runnable deleteAction) {
        JTable table = new JTable(model);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_LIGHT);
        table.getTableHeader().setForeground(UIConstants.TEXT);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setBackground(UIConstants.SURFACE);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return table;
    }

    private void addHoliday() {
        try {
            LocalDate date = LocalDate.parse(holidayDateField.getText().trim());
            String reason  = holidayReasonField.getText().trim();
            if (reason.isEmpty()) reason = "Holiday";
            holidayDAO.add(new Holiday(date, reason));
            holidayStatus.setForeground(UIConstants.SUCCESS);
            holidayStatus.setText("✓  Added " + date);
            holidayDateField.setText("");
            holidayReasonField.setText("");
            refreshHolidayTable();
        } catch (DateTimeParseException e) {
            holidayStatus.setForeground(UIConstants.DANGER);
            holidayStatus.setText("✗  Invalid date. Use YYYY-MM-DD");
        }
    }

    private void deleteSelectedHoliday(JTable table) {
        if (table == null) return;
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) holidayTableModel.getValueAt(row, 0);
        holidayDAO.delete(id);
        refreshHolidayTable();
    }

    private void refreshHolidayTable() {
        holidayTableModel.setRowCount(0);
        for (Holiday h : holidayDAO.getAll()) {
            holidayTableModel.addRow(new Object[]{h.getId(), h.getDate(), h.getReason(), "Remove"});
        }
    }

    // ===================== SPECIAL SATURDAYS =====================

    private JPanel buildSaturdaySection() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(UIConstants.BG);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UIConstants.SURFACE);
        formCard.setBorder(UIConstants.sectionBorder("Special Working Saturdays"));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Explanation label
        GridBagConstraints gbc = makeGBC();
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel info = UIConstants.label(
            "e.g. Saturday 02-Nov follows Monday → all courses that meet on Monday will have class that Saturday",
            UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        formCard.add(info, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0;
        formCard.add(UIConstants.label("Saturday Date", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        satDateField = UIConstants.textField(14);
        formCard.add(satDateField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0;
        formCard.add(UIConstants.label("Follows Day", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1;
        followsDayBox = new JComboBox<>(new String[]{"MON", "TUE", "WED", "THU", "FRI"});
        followsDayBox.setFont(UIConstants.FONT_BODY);
        formCard.add(followsDayBox, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(UIConstants.SURFACE);
        JButton addBtn = UIConstants.primaryButton("  Add Saturday  ");
        addBtn.addActionListener(e -> addSaturday());
        btnRow.add(addBtn);
        btnRow.add(Box.createHorizontalStrut(12));
        satStatus = UIConstants.label("", UIConstants.FONT_BODY, UIConstants.SUCCESS);
        btnRow.add(satStatus);
        formCard.add(btnRow, gbc);
        outer.add(formCard);
        outer.add(Box.createVerticalStrut(12));

        // Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(UIConstants.SURFACE);
        tableCard.setBorder(UIConstants.sectionBorder("Added Special Saturdays"));
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        satTableModel = new DefaultTableModel(new String[]{"ID", "Date", "Follows Day", "Remove"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable satTable = buildTable(satTableModel, new int[]{40, 120, 140, 80}, null);
        satTable.getColumn("Remove").setCellRenderer(new CoursesPanel.ButtonRenderer("Remove"));
        satTable.getColumn("Remove").setCellEditor(
            new CoursesPanel.ButtonEditor(new JCheckBox(), () -> deleteSelectedSaturday(satTable)));

        JScrollPane sp = new JScrollPane(satTable);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        sp.setPreferredSize(new Dimension(0, 140));
        tableCard.add(sp);
        outer.add(tableCard);

        refreshSaturdayTable();
        return outer;
    }

    private void addSaturday() {
        try {
            LocalDate date = LocalDate.parse(satDateField.getText().trim());
            String followsDay = (String) followsDayBox.getSelectedItem();
            saturdayDAO.add(new SpecialSaturday(date, followsDay));
            satStatus.setForeground(UIConstants.SUCCESS);
            satStatus.setText("✓  Added Saturday " + date + " → " + followsDay);
            satDateField.setText("");
            refreshSaturdayTable();
        } catch (DateTimeParseException e) {
            satStatus.setForeground(UIConstants.DANGER);
            satStatus.setText("✗  Invalid date. Use YYYY-MM-DD");
        }
    }

    private void deleteSelectedSaturday(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) satTableModel.getValueAt(row, 0);
        saturdayDAO.delete(id);
        refreshSaturdayTable();
    }

    private void refreshSaturdayTable() {
        satTableModel.setRowCount(0);
        for (SpecialSaturday ss : saturdayDAO.getAll()) {
            satTableModel.addRow(new Object[]{ss.getId(), ss.getDate(), ss.getFollowsDay(), "Remove"});
        }
    }

    private GridBagConstraints makeGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 8, 6, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        return gbc;
    }
}