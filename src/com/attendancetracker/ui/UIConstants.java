package com.attendancetracker.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class UIConstants {
    public static final Color PRIMARY       = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    public static final Color BG            = new Color(248, 250, 252);
    public static final Color SURFACE       = Color.WHITE;
    public static final Color BORDER_COLOR  = new Color(226, 232, 240);
    public static final Color TEXT          = new Color(15, 23, 42);
    public static final Color TEXT_MUTED    = new Color(100, 116, 139);
    public static final Color SUCCESS       = new Color(34, 197, 94);
    public static final Color SUCCESS_LIGHT = new Color(220, 252, 231);
    public static final Color WARNING       = new Color(234, 179, 8);
    public static final Color WARNING_LIGHT = new Color(254, 249, 195);
    public static final Color DANGER        = new Color(239, 68, 68);
    public static final Color DANGER_LIGHT  = new Color(254, 226, 226);
    public static final Color OD_COLOR      = new Color(168, 85, 247);
    public static final Color OD_LIGHT      = new Color(243, 232, 255);

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16));
    }

    public static Border sectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_BOLD, TEXT_MUTED),
            new EmptyBorder(8, 8, 8, 8));
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD); btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY); btn.setForeground(PRIMARY); btn.setBackground(SURFACE);
        btn.setFocusPainted(false); btn.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        return btn;
    }

    public static JButton attendanceButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD); btn.setForeground(fg); btn.setBackground(bg);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));
        return btn;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text); lbl.setFont(font); lbl.setForeground(color);
        return lbl;
    }

    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(FONT_SMALL); lbl.setForeground(fg); lbl.setBackground(bg);
        lbl.setOpaque(true); lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
        return lbl;
    }
}