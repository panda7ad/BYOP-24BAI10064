package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.CancelledClass;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CancelledClassDAO {

    public boolean add(CancelledClass cc) {
        String sql = "INSERT OR IGNORE INTO cancelled_classes(course_id, date, has_substitute) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, cc.getCourseId());
            p.setString(2, cc.getDate().toString());
            p.setInt(3, cc.isHasSubstitute() ? 1 : 0);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add cancelled class error: " + e.getMessage());
            return false;
        }
    }

    public List<CancelledClass> getByCourseId(int courseId) {
        List<CancelledClass> list = new ArrayList<>();
        String sql = "SELECT * FROM cancelled_classes WHERE course_id=? ORDER BY date ASC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                list.add(new CancelledClass(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getInt("has_substitute") == 1
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get cancelled classes error: " + e.getMessage());
        }
        return list;
    }

    // Check if a specific class is cancelled
    public CancelledClass getByCoursAndDate(int courseId, LocalDate date) {
        String sql = "SELECT * FROM cancelled_classes WHERE course_id=? AND date=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            p.setString(2, date.toString());
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return new CancelledClass(
                    rs.getInt("id"),
                    rs.getInt("course_id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getInt("has_substitute") == 1
                );
            }
        } catch (SQLException e) {
            System.out.println("Get cancelled class error: " + e.getMessage());
        }
        return null;
    }

    public void delete(int id) {
        String sql = "DELETE FROM cancelled_classes WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete cancelled class error: " + e.getMessage());
        }
    }

    public void deleteForCourse(int courseId) {
        String sql = "DELETE FROM cancelled_classes WHERE course_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, courseId);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete cancelled classes for course error: " + e.getMessage());
        }
    }
}