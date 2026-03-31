package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.SemesterSettings;

import java.sql.*;
import java.time.LocalDate;

public class SettingsDAO {

    private void set(String key, String value) {
        String sql = "INSERT INTO settings(key, value) VALUES(?,?) "
                   + "ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, key);
            p.setString(2, value);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Settings set error: " + e.getMessage());
        }
    }

    private String get(String key) {
        String sql = "SELECT value FROM settings WHERE key=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, key);
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) {
            System.out.println("Settings get error: " + e.getMessage());
        }
        return null;
    }

    private LocalDate getDate(String key) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? LocalDate.parse(val) : null;
    }

    public SemesterSettings getSettings() {
        SemesterSettings s = new SemesterSettings();
        s.setStudentName(get("student_name"));
        s.setBranch(get("branch"));
        s.setSemStart(getDate("sem_start"));
        s.setSemEnd(getDate("sem_end"));
        s.setMidtermStart(getDate("midterm_start"));
        s.setMidtermEnd(getDate("midterm_end"));
        s.setEndtermStart(getDate("endterm_start"));
        s.setEndtermEnd(getDate("endterm_end"));
        return s;
    }

    public void saveSettings(SemesterSettings s) {
        set("student_name",   s.getStudentName() != null ? s.getStudentName() : "");
        set("branch",         s.getBranch() != null ? s.getBranch() : "");
        set("sem_start",      s.getSemStart() != null ? s.getSemStart().toString() : "");
        set("sem_end",        s.getSemEnd() != null ? s.getSemEnd().toString() : "");
        set("midterm_start",  s.getMidtermStart() != null ? s.getMidtermStart().toString() : "");
        set("midterm_end",    s.getMidtermEnd() != null ? s.getMidtermEnd().toString() : "");
        set("endterm_start",  s.getEndtermStart() != null ? s.getEndtermStart().toString() : "");
        set("endterm_end",    s.getEndtermEnd() != null ? s.getEndtermEnd().toString() : "");
    }

    public boolean isSetupComplete() {
        SemesterSettings s = getSettings();
        return s.isComplete();
    }
}
