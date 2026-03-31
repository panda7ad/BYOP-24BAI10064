package com.attendancetracker.ui;

import com.attendancetracker.dao.SettingsDAO;
import com.attendancetracker.model.SemesterSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class SetupPanel extends JPanel {

    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final Runnable onSaved;

    private JTextField nameField, branchField;
    private JTextField semStartField, semEndField;
    private JTextField midStartField, midEndField;
    private JTextField endStartField, endEndField;
    private JLabel statusLabel;

    public SetupPanel(Runnable onSaved) {
        this.onSaved = onSaved;
        setBackground(UIConstants.BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 32, 24, 32));
        build();
        loadExisting();
    }

    private void build() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BG);

        JLabel pageTitle = UIConstants.label("Semester Setup", UIConstants.FONT_TITLE, UIConstants.TEXT);
        JLabel subtitle  = UIConstants.label("Fill this once per semester. You can always edit and re-save.",
                UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(pageTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        // Student info
        JPanel studentCard = buildCard("Student Info");
        nameField   = UIConstants.textField(24);
        branchField = UIConstants.textField(24);
        addRow(studentCard, 0, "Your Name",        nameField);
        addRow(studentCard, 1, "Branch / Program", branchField);
        studentCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(studentCard);
        content.add(Box.createVerticalStrut(16));

        // Semester dates
        JPanel semCard = buildCard("Semester Dates");
        semStartField = UIConstants.textField(14);
        semEndField   = UIConstants.textField(14);
        addRow(semCard, 0, "Semester Start Date",      semStartField);
        addRow(semCard, 1, "Last Instructional Date",  semEndField);
        JLabel hint = UIConstants.label("  Format: YYYY-MM-DD   e.g.  2024-08-01",
                UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        semCard.add(hint, makeGBC(2, 1));
        semCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(semCard);
        content.add(Box.createVerticalStrut(16));

        // Exam periods
        JPanel examCard = buildCard("Exam Periods  (no classes during these dates)");
        midStartField = UIConstants.textField(14);
        midEndField   = UIConstants.textField(14);
        endStartField = UIConstants.textField(14);
        endEndField   = UIConstants.textField(14);
        addRow(examCard, 0, "Mid-term Start", midStartField);
        addRow(examCard, 1, "Mid-term End",   midEndField);
        addRow(examCard, 2, "End-term Start", endStartField);
        addRow(examCard, 3, "End-term End",   endEndField);
        examCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(examCard);
        content.add(Box.createVerticalStrut(24));

        // Save
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(UIConstants.BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn = UIConstants.primaryButton("  Save Settings  ");
        saveBtn.addActionListener(e -> save());
        btnRow.add(saveBtn);
        content.add(btnRow);
        content.add(Box.createVerticalStrut(10));

        statusLabel = UIConstants.label("", UIConstants.FONT_BODY, UIConstants.SUCCESS);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);

        add(content, BorderLayout.NORTH);
    }

    private void save() {
        try {
            SemesterSettings s = new SemesterSettings();
            s.setStudentName(nameField.getText().trim());
            s.setBranch(branchField.getText().trim());
            if (s.getStudentName().isEmpty()) { showError("Name cannot be empty."); return; }
            s.setSemStart(parseDate(semStartField.getText(),  "Semester Start"));
            s.setSemEnd(parseDate(semEndField.getText(),      "Last Instructional Date"));
            s.setMidtermStart(parseDate(midStartField.getText(), "Mid-term Start"));
            s.setMidtermEnd(parseDate(midEndField.getText(),     "Mid-term End"));
            s.setEndtermStart(parseDate(endStartField.getText(), "End-term Start"));
            s.setEndtermEnd(parseDate(endEndField.getText(),     "End-term End"));
            if (s.getSemEnd().isBefore(s.getSemStart())) {
                showError("Last instructional date cannot be before semester start."); return;
            }
            settingsDAO.saveSettings(s);
            statusLabel.setForeground(UIConstants.SUCCESS);
            statusLabel.setText("✓  Settings saved successfully!");
            if (onSaved != null) onSaved.run();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private LocalDate parseDate(String text, String fieldName) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date for " + fieldName
                    + ": use format YYYY-MM-DD");
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(UIConstants.DANGER);
        statusLabel.setText("✗  " + msg);
    }

    private void loadExisting() {
        SemesterSettings s = settingsDAO.getSettings();
        if (s.getStudentName()  != null) nameField.setText(s.getStudentName());
        if (s.getBranch()       != null) branchField.setText(s.getBranch());
        if (s.getSemStart()     != null) semStartField.setText(s.getSemStart().toString());
        if (s.getSemEnd()       != null) semEndField.setText(s.getSemEnd().toString());
        if (s.getMidtermStart() != null) midStartField.setText(s.getMidtermStart().toString());
        if (s.getMidtermEnd()   != null) midEndField.setText(s.getMidtermEnd().toString());
        if (s.getEndtermStart() != null) endStartField.setText(s.getEndtermStart().toString());
        if (s.getEndtermEnd()   != null) endEndField.setText(s.getEndtermEnd().toString());
    }

    private JPanel buildCard(String title) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIConstants.SURFACE);
        card.setBorder(UIConstants.sectionBorder(title));
        return card;
    }

    private void addRow(JPanel panel, int row, String labelText, JComponent field) {
        GridBagConstraints lc = makeGBC(row, 0);
        lc.weightx = 0;
        JLabel lbl = UIConstants.label(labelText, UIConstants.FONT_BODY, UIConstants.TEXT);
        lbl.setPreferredSize(new Dimension(220, 28));
        panel.add(lbl, lc);
        GridBagConstraints fc = makeGBC(row, 1);
        fc.weightx = 1;
        panel.add(field, fc);
    }

    private GridBagConstraints makeGBC(int row, int col) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = col; gbc.gridy = row;
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        return gbc;
    }
}