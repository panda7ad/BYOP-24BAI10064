package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // Insert or update (upsert) attendance for a course on a given date
    public boolean upsert(int courseId, LocalDate date, String status) {
        String sql = "INSERT INTO attendance(course_id, date, status) VALUES(?,?,?) "
                   + "ON CONFLICT(course_id, date) DO UPDATE SET status=excluded.status";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            p.setString(2, date.toString());
            p.setString(3, status);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Attendance upsert error: " + e.getMessage());
            return false;
        }
    }

    // Get all attendance records for a course
    public List<AttendanceRecord> getByCourseId(int courseId) {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE course_id=? ORDER BY date ASC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                list.add(new AttendanceRecord(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get attendance error: " + e.getMessage());
        }
        return list;
    }

    // Get status for a specific course + date (returns null if not marked yet)
    public String getStatus(int courseId, LocalDate date) {
        String sql = "SELECT status FROM attendance WHERE course_id=? AND date=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            p.setString(2, date.toString());
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (SQLException e) {
            System.out.println("Get status error: " + e.getMessage());
        }
        return null;
    }

    // Count OD entries for a course in a given month (for OD limit check)
    public int countODInMonth(int courseId, int year, int month) {
        String monthStr = String.format("%d-%02d", year, month);
        String sql = "SELECT COUNT(*) FROM attendance "
                   + "WHERE status='OD' AND strftime('%Y-%m', date)=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, monthStr);
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count OD error: " + e.getMessage());
        }
        return 0;
    }

    // Count total OD entries across all courses in a given month
    public int countTotalODInMonth(int year, int month) {
        String monthStr = String.format("%d-%02d", year, month);
        String sql = "SELECT COUNT(*) FROM attendance "
                   + "WHERE status='OD' AND strftime('%Y-%m', date)=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, monthStr);
            ResultSet rs = p.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Count total OD error: " + e.getMessage());
        }
        return 0;
    }

    public void deleteForCourse(int courseId) {
        String sql = "DELETE FROM attendance WHERE course_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete attendance error: " + e.getMessage());
        }
    }
}