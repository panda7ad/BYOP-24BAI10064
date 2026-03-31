package com.attendancetracker.ui;

import com.attendancetracker.dao.SettingsDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane tabs;
    private SetupPanel setupPanel;
    private CoursesPanel coursesPanel;
    private SpecialDaysPanel specialDaysPanel;
    private MarkAttendancePanel markAttendancePanel;
    private DashboardPanel dashboardPanel;
    private final SettingsDAO settingsDAO = new SettingsDAO();

    public MainFrame() {
        setTitle("FFCS Attendance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setPreferredSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        initUI();
        pack();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(59, 130, 246));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 56));
        header.add(UIConstants.label("📅  FFCS Attendance Tracker",
                UIConstants.FONT_TITLE, Color.WHITE), BorderLayout.WEST);

        String name = settingsDAO.getSettings().getStudentName();
        if (name != null && !name.isBlank()) {
            JLabel nameLabel = UIConstants.label(name, UIConstants.FONT_BODY,
                    new Color(186, 230, 253));
            nameLabel.setBorder(new EmptyBorder(0, 0, 0, 4));
            header.add(nameLabel, BorderLayout.EAST);
        }
        root.add(header, BorderLayout.NORTH);

        // Tabs
        tabs = new JTabbedPane();
        tabs.setFont(UIConstants.FONT_BODY);

        setupPanel          = new SetupPanel(this::onSetupSaved);
        coursesPanel        = new CoursesPanel();
        specialDaysPanel    = new SpecialDaysPanel();
        dashboardPanel      = new DashboardPanel();
        markAttendancePanel = new MarkAttendancePanel(this::onAttendanceMarked);

        tabs.addTab("⚙  Setup",          wrap(setupPanel));
        tabs.addTab("📚  Courses",         wrap(coursesPanel));
        tabs.addTab("📆  Special Days",    wrap(specialDaysPanel));
        tabs.addTab("✅  Mark Attendance", markAttendancePanel);
        tabs.addTab("📊  Dashboard",       wrap(dashboardPanel));

        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 3) markAttendancePanel.refresh();
            if (i == 4) dashboardPanel.refresh();
        });

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void onSetupSaved() {
        SwingUtilities.invokeLater(() -> { getContentPane().removeAll(); initUI(); revalidate(); repaint(); });
    }

    public void onAttendanceMarked() { dashboardPanel.refresh(); }

    private JScrollPane wrap(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }
}