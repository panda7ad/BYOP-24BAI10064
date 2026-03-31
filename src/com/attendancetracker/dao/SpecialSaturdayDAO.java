package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.SpecialSaturday;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SpecialSaturdayDAO {

    public boolean add(SpecialSaturday ss) {
        String sql = "INSERT OR IGNORE INTO special_saturdays(date, follows_day) VALUES(?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, ss.getDate().toString());
            p.setString(2, ss.getFollowsDay());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add special saturday error: " + e.getMessage());
            return false;
        }
    }

    public List<SpecialSaturday> getAll() {
        List<SpecialSaturday> list = new ArrayList<>();
        String sql = "SELECT * FROM special_saturdays ORDER BY date ASC";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SpecialSaturday(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("follows_day")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get special saturdays error: " + e.getMessage());
        }
        return list;
    }

    // Returns the special saturday entry for a date, or null if it's a regular off Saturday
    public SpecialSaturday getByDate(LocalDate date) {
        String sql = "SELECT * FROM special_saturdays WHERE date=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, date.toString());
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return new SpecialSaturday(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("follows_day")
                );
            }
        } catch (SQLException e) {
            System.out.println("Get special saturday error: " + e.getMessage());
        }
        return null;
    }

    public void delete(int id) {
        String sql = "DELETE FROM special_saturdays WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete special saturday error: " + e.getMessage());
        }
    }
}