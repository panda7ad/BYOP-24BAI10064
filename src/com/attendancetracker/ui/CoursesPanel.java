package com.attendancetracker.ui;

import com.attendancetracker.dao.AttendanceDAO;
import com.attendancetracker.dao.CancelledClassDAO;
import com.attendancetracker.dao.CourseDAO;
import com.attendancetracker.model.Course;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesPanel extends JPanel {

    private final CourseDAO courseDAO = new CourseDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final CancelledClassDAO cancelledClassDAO = new CancelledClassDAO();

    private JTextField courseNameField;
    private JComboBox<String> creditBox;
    private JCheckBox[] dayBoxes;
    private final String[] DAYS = {"MON", "TUE", "WED", "THU", "FRI"};

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    public CoursesPanel() {
        setBackground(UIConstants.BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(24, 32, 24, 32));
        build();
        refreshTable();
    }

    private void build() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(UIConstants.BG);

        // Page title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(UIConstants.BG);
        titleRow.add(UIConstants.label("Your Courses", UIConstants.FONT_TITLE, UIConstants.TEXT),
                BorderLayout.WEST);
        titleRow.add(UIConstants.label("Add each course from your FFCS timetable",
                UIConstants.FONT_BODY, UIConstants.TEXT_MUTED), BorderLayout.SOUTH);
        content.add(titleRow, BorderLayout.NORTH);

        // --- Add Course Form ---
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UIConstants.SURFACE);
        formCard.setBorder(UIConstants.sectionBorder("Add New Course"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Row 0: Course name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formCard.add(UIConstants.label("Course Name", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        courseNameField = UIConstants.textField(22);
        formCard.add(courseNameField, gbc);

        // Row 1: Credits
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formCard.add(UIConstants.label("Credits", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        creditBox = new JComboBox<>(new String[]{"2 credits (1 day/week)", "3 credits (2 days/week)", "4 credits (3 days/week)"});
        creditBox.setFont(UIConstants.FONT_BODY);
        formCard.add(creditBox, gbc);

        // Row 2: Days
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formCard.add(UIConstants.label("Class Days", UIConstants.FONT_BODY, UIConstants.TEXT), gbc);

        gbc.gridx = 1;
        JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        daysPanel.setBackground(UIConstants.SURFACE);
        dayBoxes = new JCheckBox[DAYS.length];
        for (int i = 0; i < DAYS.length; i++) {
            dayBoxes[i] = new JCheckBox(DAYS[i]);
            dayBoxes[i].setFont(UIConstants.FONT_BODY);
            dayBoxes[i].setBackground(UIConstants.SURFACE);
            dayBoxes[i].setForeground(UIConstants.TEXT);
            daysPanel.add(dayBoxes[i]);
        }
        formCard.add(daysPanel, gbc);

        // Row 3: Add button + status
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(UIConstants.SURFACE);
        JButton addBtn = UIConstants.primaryButton("  Add Course  ");
        addBtn.addActionListener(e -> addCourse());
        btnRow.add(addBtn);
        btnRow.add(Box.createHorizontalStrut(16));
        statusLabel = UIConstants.label("", UIConstants.FONT_BODY, UIConstants.SUCCESS);
        btnRow.add(statusLabel);
        formCard.add(btnRow, gbc);

        content.add(formCard, BorderLayout.CENTER);

        // --- Courses Table ---
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(UIConstants.SURFACE);
        tableCard.setBorder(UIConstants.sectionBorder("Enrolled Courses"));

        String[] cols = {"ID", "Course Name", "Credits", "Class Days", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        table = new JTable(tableModel);
        styleTable();

        // Delete button in last column
        table.getColumn("Action").setCellRenderer(new ButtonRenderer("Delete"));
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this::deleteCourse));

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.SOUTH);
        add(content, BorderLayout.NORTH);
    }

    private void addCourse() {
        String name = courseNameField.getText().trim();
        if (name.isEmpty()) {
            showStatus("Course name cannot be empty.", false);
            return;
        }
        List<String> selectedDays = new ArrayList<>();
        for (JCheckBox cb : dayBoxes) {
            if (cb.isSelected()) selectedDays.add(cb.getText());
        }
        if (selectedDays.isEmpty()) {
            showStatus("Select at least one class day.", false);
            return;
        }
        int credit = creditBox.getSelectedIndex() + 2;
        Course course = new Course(name, credit, selectedDays);
        if (courseDAO.addCourse(course)) {
            showStatus("✓  " + name + " added.", true);
            courseNameField.setText("");
            for (JCheckBox cb : dayBoxes) cb.setSelected(false);
            refreshTable();
        } else {
            showStatus("Failed to add course.", false);
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + courseName + "\" and all its attendance records?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            attendanceDAO.deleteForCourse(id);
            cancelledClassDAO.deleteForCourse(id);
            courseDAO.deleteCourse(id);
            showStatus("✓  " + courseName + " deleted.", true);
            refreshTable();
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Course c : courseDAO.getAllCourses()) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getName(), c.getCredit() + " cr", c.getDaysAsString(), "Delete"
            });
        }
    }

    private void styleTable() {
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getTableHeader().setBackground(UIConstants.PRIMARY_LIGHT);
        table.getTableHeader().setForeground(UIConstants.TEXT);
        table.setSelectionBackground(UIConstants.PRIMARY_LIGHT);
        table.setBackground(UIConstants.SURFACE);
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setForeground(success ? UIConstants.SUCCESS : UIConstants.DANGER);
        statusLabel.setText(msg);
    }

    // --- Button Renderer/Editor for table delete button ---

    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
            setFont(UIConstants.FONT_SMALL);
            setForeground(Color.WHITE);
            setBackground(UIConstants.DANGER);
            setFocusPainted(false);
            setBorderPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final Runnable action;

        public ButtonEditor(JCheckBox checkBox, Runnable action) {
            super(checkBox);
            this.action = action;
            button = new JButton("Delete");
            button.setFont(UIConstants.FONT_SMALL);
            button.setForeground(Color.WHITE);
            button.setBackground(UIConstants.DANGER);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.addActionListener(e -> {
                fireEditingStopped();
                this.action.run();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean sel, int r, int c) {
            return button;
        }

        @Override public Object getCellEditorValue() { return "Delete"; }
    }
}