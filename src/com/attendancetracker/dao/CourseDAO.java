package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses(name, credit, days) VALUES(?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, course.getName());
            p.setInt(2, course.getCredit());
            p.setString(3, course.getDaysAsString());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add course error: " + e.getMessage());
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY name ASC";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Course(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("credit"),
                    rs.getString("days")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get courses error: " + e.getMessage());
        }
        return list;
    }

    public Course getById(int id) {
        String sql = "SELECT * FROM courses WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return new Course(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("credit"),
                    rs.getString("days")
                );
            }
        } catch (SQLException e) {
            System.out.println("Get course by id error: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteCourse(int id) {
        String sql = "DELETE FROM courses WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Delete course error: " + e.getMessage());
            return false;
        }
    }
}
