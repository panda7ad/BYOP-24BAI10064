package com.attendancetracker.ui;

import com.attendancetracker.dao.CourseDAO;
import com.attendancetracker.model.Course;
import com.attendancetracker.service.AttendanceService;
import com.attendancetracker.service.ScheduleService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MarkAttendancePanel extends JPanel {

    private final CourseDAO courseDAO           = new CourseDAO();
    private final ScheduleService scheduleService = new ScheduleService();
    private final AttendanceService attendanceService = new AttendanceService();

    private final Runnable onAttendanceMarked;

    private JPanel coursesArea;
    private JLabel dateLabel, infoLabel, odCountLabel;
    private LocalDate selectedDate;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    public MarkAttendancePanel(Runnable onAttendanceMarked) {
        this.onAttendanceMarked = onAttendanceMarked;
        this.selectedDate = LocalDate.now();
        setBackground(UIConstants.BG);
        setLayout(new BorderLayout(0, 0));
        build();
        refresh();
    }

    private void build() {
        // Top bar: date navigation
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(UIConstants.SURFACE);
        topBar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JButton prevBtn = UIConstants.outlineButton("◀  Previous");
        JButton nextBtn = UIConstants.outlineButton("Next  ▶");
        JButton todayBtn = UIConstants.primaryButton("Today");

        dateLabel = UIConstants.label("", UIConstants.FONT_HEADING, UIConstants.TEXT);
        infoLabel = UIConstants.label("", UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        odCountLabel = UIConstants.label("", UIConstants.FONT_SMALL, UIConstants.OD_COLOR);

        JPanel centerLabels = new JPanel();
        centerLabels.setLayout(new BoxLayout(centerLabels, BoxLayout.Y_AXIS));
        centerLabels.setBackground(UIConstants.SURFACE);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        odCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerLabels.add(dateLabel);
        centerLabels.add(Box.createVerticalStrut(2));
        centerLabels.add(infoLabel);
        centerLabels.add(odCountLabel);

        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navBtns.setBackground(UIConstants.SURFACE);
        navBtns.add(prevBtn);
        navBtns.add(todayBtn);
        navBtns.add(nextBtn);

        topBar.add(new JPanel() {{ setBackground(UIConstants.SURFACE); }}, BorderLayout.WEST);
        topBar.add(centerLabels, BorderLayout.CENTER);
        topBar.add(navBtns, BorderLayout.EAST);

        prevBtn.addActionListener(e -> { selectedDate = selectedDate.minusDays(1); refresh(); });
        nextBtn.addActionListener(e -> { selectedDate = selectedDate.plusDays(1); refresh(); });
        todayBtn.addActionListener(e -> { selectedDate = LocalDate.now(); refresh(); });

        add(topBar, BorderLayout.NORTH);

        // Scrollable courses area
        coursesArea = new JPanel();
        coursesArea.setLayout(new BoxLayout(coursesArea, BoxLayout.Y_AXIS));
        coursesArea.setBackground(UIConstants.BG);
        coursesArea.setBorder(new EmptyBorder(16, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(coursesArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        dateLabel.setText(selectedDate.format(DISPLAY_FMT));

        int odThisMonth = attendanceService.getODCountThisMonth(selectedDate);
        odCountLabel.setText("OD used this month: " + odThisMonth + " / 2");
        odCountLabel.setForeground(odThisMonth >= 2 ? UIConstants.DANGER : UIConstants.OD_COLOR);

        coursesArea.removeAll();

        if (scheduleService.isNoClassDay(selectedDate)) {
            infoLabel.setText("No classes scheduled");
            JLabel noClass = UIConstants.label("🎉  No classes on this day — holiday, exam period, or weekend.",
                    UIConstants.FONT_HEADING, UIConstants.TEXT_MUTED);
            noClass.setAlignmentX(Component.CENTER_ALIGNMENT);
            coursesArea.add(Box.createVerticalStrut(60));
            coursesArea.add(noClass);
        } else {
            infoLabel.setText("Mark attendance for each course below");
            List<Course> courses = courseDAO.getAllCourses();
            List<Course> todayCourses = scheduleService.getCoursesForDate(selectedDate, courses);

            if (todayCourses.isEmpty()) {
                infoLabel.setText("No courses configured — go to Courses tab first");
            } else {
                for (Course course : todayCourses) {
                    coursesArea.add(buildCourseRow(course));
                    coursesArea.add(Box.createVerticalStrut(10));
                }
            }
        }

        coursesArea.revalidate();
        coursesArea.repaint();
        revalidate();
        repaint();
    }

    private JPanel buildCourseRow(Course course) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setBackground(UIConstants.SURFACE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1, true),
            new EmptyBorder(12, 18, 12, 18)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: course info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(UIConstants.SURFACE);
        JLabel name = UIConstants.label(course.getName(), UIConstants.FONT_BOLD, UIConstants.TEXT);
        JLabel days = UIConstants.label(course.getDaysAsString() + "  ·  " + course.getCredit() + " credits",
                UIConstants.FONT_SMALL, UIConstants.TEXT_MUTED);
        infoPanel.add(name);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(days);
        row.add(infoPanel, BorderLayout.WEST);

        // Right: action buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UIConstants.SURFACE);

        // Check if already marked or cancelled
        String currentStatus = attendanceService.getStatusForDate(course.getId(), selectedDate);
        boolean isCancelled  = attendanceService.getCancellation(course.getId(), selectedDate) != null;

        if (isCancelled) {
            JLabel cancelledTag = UIConstants.badge("CANCELLED", UIConstants.BORDER_COLOR, UIConstants.TEXT_MUTED);
            btnPanel.add(cancelledTag);
        } else {
            JButton presentBtn  = UIConstants.attendanceButton("Present", UIConstants.SUCCESS_LIGHT, UIConstants.SUCCESS);
            JButton absentBtn   = UIConstants.attendanceButton("Absent",  UIConstants.DANGER_LIGHT,  UIConstants.DANGER);
            JButton odBtn       = UIConstants.attendanceButton("OD",      UIConstants.OD_LIGHT,      UIConstants.OD_COLOR);
            JButton cancelBtn   = UIConstants.attendanceButton("Cancelled", UIConstants.WARNING_LIGHT, UIConstants.WARNING);

            // Highlight current status
            highlightButton(presentBtn, "PRESENT", currentStatus, UIConstants.SUCCESS);
            highlightButton(absentBtn,  "ABSENT",  currentStatus, UIConstants.DANGER);
            highlightButton(odBtn,      "OD",      currentStatus, UIConstants.OD_COLOR);

            presentBtn.addActionListener(e -> mark(course, "PRESENT", row));
            absentBtn.addActionListener(e  -> mark(course, "ABSENT",  row));
            odBtn.addActionListener(e      -> markOD(course, row));
            cancelBtn.addActionListener(e  -> markCancelled(course, row));

            btnPanel.add(presentBtn);
            btnPanel.add(absentBtn);
            btnPanel.add(odBtn);
            btnPanel.add(cancelBtn);

            // Show current status badge
            if (currentStatus != null) {
                btnPanel.add(Box.createHorizontalStrut(8));
                Color bgC = switch (currentStatus) {
                    case "PRESENT" -> UIConstants.SUCCESS_LIGHT;
                    case "ABSENT"  -> UIConstants.DANGER_LIGHT;
                    case "OD"      -> UIConstants.OD_LIGHT;
                    default        -> UIConstants.BORDER_COLOR;
                };
                Color fgC = switch (currentStatus) {
                    case "PRESENT" -> UIConstants.SUCCESS;
                    case "ABSENT"  -> UIConstants.DANGER;
                    case "OD"      -> UIConstants.OD_COLOR;
                    default        -> UIConstants.TEXT_MUTED;
                };
                btnPanel.add(UIConstants.badge(currentStatus, bgC, fgC));
            }
        }

        row.add(btnPanel, BorderLayout.EAST);
        return row;
    }

    private void highlightButton(JButton btn, String status, String current, Color color) {
        if (status.equals(current)) {
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
        }
    }

    private void mark(Course course, String status, JPanel row) {
        attendanceService.markAttendance(course.getId(), selectedDate, status);
        refresh();
        if (onAttendanceMarked != null) onAttendanceMarked.run();
    }

    private void markOD(Course course, JPanel row) {
        int odCount = attendanceService.getODCountThisMonth(selectedDate);
        if (odCount >= 2) {
            JOptionPane.showMessageDialog(this,
                "OD limit reached for " + selectedDate.getMonth().toString()
                + " (max 2 per month).\nYou've already used " + odCount + " ODs this month.",
                "OD Limit Reached", JOptionPane.WARNING_MESSAGE);
            return;
        }
        attendanceService.markAttendance(course.getId(), selectedDate, "OD");
        refresh();
        if (onAttendanceMarked != null) onAttendanceMarked.run();
    }

    private void markCancelled(Course course, JPanel row) {
        int choice = JOptionPane.showOptionDialog(this,
                "Was a substitute class or assignment given for \"" + course.getName() + "\"?",
                "Class Cancelled",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Yes — class still counts", "No — just cancelled", "Back"},
                "Back");

        if (choice == 0) {
            // Has substitute → class still counts in total, but no attendance impact today
            attendanceService.recordCancellation(course.getId(), selectedDate, true);
        } else if (choice == 1) {
            // No substitute → removes this class from total count
            attendanceService.recordCancellation(course.getId(), selectedDate, false);
        }
        // choice == 2 or closed → do nothing

        if (choice == 0 || choice == 1) {
            refresh();
            if (onAttendanceMarked != null) onAttendanceMarked.run();
        }
    }
}