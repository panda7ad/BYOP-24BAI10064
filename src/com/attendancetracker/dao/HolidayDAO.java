package com.attendancetracker.dao;

import com.attendancetracker.db.DBConnection;
import com.attendancetracker.model.Holiday;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HolidayDAO {

    public boolean add(Holiday holiday) {
        String sql = "INSERT OR IGNORE INTO holidays(date, reason) VALUES(?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, holiday.getDate().toString());
            p.setString(2, holiday.getReason());
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add holiday error: " + e.getMessage());
            return false;
        }
    }

    public List<Holiday> getAll() {
        List<Holiday> list = new ArrayList<>();
        String sql = "SELECT * FROM holidays ORDER BY date ASC";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Holiday(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("reason")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Get holidays error: " + e.getMessage());
        }
        return list;
    }

    public boolean isHoliday(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM holidays WHERE date=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, date.toString());
            ResultSet rs = p.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM holidays WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            p.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete holiday error: " + e.getMessage());
        }
    }
}