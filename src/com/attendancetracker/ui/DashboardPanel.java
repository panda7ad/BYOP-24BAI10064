package com.attendancetracker.ui;

import com.attendancetracker.dao.CourseDAO;
import com.attendancetracker.model.Course;
import com.attendancetracker.service.AttendanceService;
import com.attendancetracker.service.AttendanceSummary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final CourseDAO courseDAO             = new CourseDAO();
    private final AttendanceService attendanceService = new AttendanceService();

    private JPanel statsArea;
    private JLabel overallLabel;

    public DashboardPanel() {
        setBackground(UIConstants.BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(24, 32, 24, 32));
        build();
        refresh();
    }

    private void build() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(UIConstants.BG);

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(UIConstants.BG);

        JLabel title = UIConstants.label("Dashboard", UIConstants.FONT_TITLE, UIConstants.TEXT);
        overallLabel = UIConstants.label("", UIConstants.FONT_BODY, UIConstants.TEXT_MUTED);

        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(overallLabel, BorderLayout.EAST);
        content.add(headerRow, BorderLayout.NORTH);

        // Stats area (rebuilt on every refresh)
        statsArea = new JPanel();
        statsArea.setLayout(new BoxLayout(statsArea, BoxLayout.Y_AXIS));
        statsArea.setBackground(UIConstants.BG);
        content.add(statsArea, BorderLayout.CENTER);

        add(content, BorderLayout.NORTH);
    }

    public void refresh() {
        statsArea.removeAll();

        List<Course> courses = courseDAO.getAllCourses();
        if (courses.isEmpty()) {
            statsArea.add(Box.createVerticalStrut(40));
            JLabel empty = UIConstants.label("No courses yet — add them in the Courses tab.",
                    UIConstants.FONT_HEADING, UIConstants.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            statsArea.add(empty);
            statsArea.revalidate();
            statsArea.repaint();
            return;
        }

        List<AttendanceSummary> summaries = attendanceService.getAllSummaries(courses);

        // Overall danger count
        long dangerCount  = summaries.stream().filter(s -> s.getStatusLabel().equals("DANGER")).count();
        long warningCount = summaries.stream().filter(s -> s.getStatusLabel().equals("WARNING")).count();

        if (dangerCount > 0) {
            overallLabel.setForeground(UIConstants.DANGER);
            overallLabel.setText("⚠  " + dangerCount + " course(s) in DANGER zone");
        } else if (warningCount > 0) {
            overallLabel.setForeground(UIConstants.WARNING);
            overallLabel.setText("⚡  " + warningCount + " course(s) need attention");
        } else {
            overallLabel.setForeground(UIConstants.SUCCESS);
            overallLabel.setText("✓  All courses above 75%");
        }

        // Legend
        statsArea.add(buildLegend());
        statsArea.add(Box.createVerticalStrut(14));

        // One card per course
        for (AttendanceSummary summary : summaries) {
            statsArea.add(buildCourseCard(summary));
            statsArea.add(Box.createVerticalStrut(10));
        }

        statsArea.revalidate();
        statsArea.repaint();
    }

    private JPanel buildLegend() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setBackground(UIConstants.BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(UIConstants.badge("SAFE ≥75%",    UIConstants.SUCCESS_LIGHT, UIConstants.SUCCESS));
        row.add(UIConstants.badge("WARNING 65-74%", UIConstants.WARNING_LIGHT, UIConstants.WARNING));
        row.add(UIConstants.badge("DANGER <65%",  UIConstants.DANGER_LIGHT,  UIConstants.DANGER));
        return row;
    }

    private JPanel buildCourseCard(AttendanceSummary s) {
        // Pick colors based on status
        Color borderColor, accentColor, bgColor;
        switch (s.getStatusLabel()) {
            case "DANGER"  -> { borderColor = UIConstants.DANGER;  accentColor = UIConstants.DANGER;  bgColor = new Color(255, 245, 245); }
            case "WARNING" -> { borderColor = UIConstants.WARNING; accentColor = UIConstants.WARNING; bgColor = new Color(255, 253, 235); }
            default        -> { borderColor = UIConstants.BORDER_COLOR; accentColor = UIConstants.SUCCESS; bgColor = UIConstants.SURFACE; }
        }

        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(14, 16, 14, 16)
            )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: course name + day info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(bgColor);

        JLabel nameLbl = UIConstants.label(s.getCourse().getName(), UIConstants.FONT_BOLD, UIConstants.TEXT);
        JLabel daysLbl = UIConstants.label(
                s.getCourse().getDaysAsString() + "  ·  " + s.getCourse().getCredit() + " credits",
                UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);

        // Status badge
        Color badgeBg = switch (s.getStatusLabel()) {
            case "DANGER"  -> UIConstants.DANGER_LIGHT;
            case "WARNING" -> UIConstants.WARNING_LIGHT;
            default        -> UIConstants.SUCCESS_LIGHT;
        };
        Color badgeFg = switch (s.getStatusLabel()) {
            case "DANGER"  -> UIConstants.DANGER;
            case "WARNING" -> UIConstants.WARNING;
            default        -> UIConstants.SUCCESS;
        };
        JLabel badge = UIConstants.badge(s.getStatusLabel(), badgeBg, badgeFg);

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nameRow.setBackground(bgColor);
        nameRow.add(nameLbl);
        nameRow.add(badge);

        leftPanel.add(nameRow);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(daysLbl);

        // Action hint
        String hint;
        if (s.getStatusLabel().equals("DANGER") || s.getStatusLabel().equals("WARNING")) {
            hint = "Need to attend " + s.getNeedToAttend() + " more class(es) to reach 75%";
        } else {
            int skip = s.getSafeToSkip();
            hint = skip > 0
                ? "Can skip " + skip + " more class(es) this semester"
                : "Attend all remaining classes to stay safe";
        }
        JLabel hintLbl = UIConstants.label(hint, UIConstants.FONT_SMALL, accentColor);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(hintLbl);
        card.add(leftPanel, BorderLayout.CENTER);

        // Right: stats grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 4));
        statsGrid.setBackground(bgColor);

        statsGrid.add(buildStat("Attended",    String.valueOf(s.getAttended()),    UIConstants.TEXT));
        statsGrid.add(buildStat("Held So Far", String.valueOf(s.getHeldSoFar()),  UIConstants.TEXT));
        statsGrid.add(buildStat("Total (Sem)", String.valueOf(s.getTotalClasses()), UIConstants.TEXT_MUTED));
        statsGrid.add(buildStat("Current %",
                String.format("%.1f%%", s.getCurrentPercent()), accentColor));
        statsGrid.add(buildStat("Safe to Skip",
                s.getSafeToSkip() >= 0 ? String.valueOf(s.getSafeToSkip()) : "0",
                s.getSafeToSkip() > 0 ? UIConstants.SUCCESS : UIConstants.DANGER));
        statsGrid.add(buildStat("Need More",
                s.getStatusLabel().equals("SAFE") ? "—" : String.valueOf(s.getNeedToAttend()),
                UIConstants.TEXT_MUTED));

        card.add(statsGrid, BorderLayout.EAST);
        return card;
    }

    private JPanel buildStat(String label, String value, Color valueColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(null); // inherit parent
        p.setOpaque(false);

        JLabel val = UIConstants.label(value, UIConstants.FONT_HEADING, valueColor);
        JLabel lbl = UIConstants.label(label, UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        val.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(val);
        p.add(lbl);
        return p;
    }
}