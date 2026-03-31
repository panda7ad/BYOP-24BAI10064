package com.attendancetracker.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    public static void initialize() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            // Settings table (key-value store for semester config)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    key   TEXT PRIMARY KEY,
                    value TEXT
                )
            """);

            // Courses table — days stored as comma-separated string e.g. "MON,WED"
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    name   TEXT NOT NULL,
                    credit INTEGER NOT NULL,
                    days   TEXT NOT NULL
                )
            """);

            // National holidays / college-declared off days
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS holidays (
                    id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    date   TEXT NOT NULL UNIQUE,
                    reason TEXT
                )
            """);

            // Special Saturdays (e.g. "2024-11-02 follows MONDAY")
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS special_saturdays (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    date        TEXT NOT NULL UNIQUE,
                    follows_day TEXT NOT NULL
                )
            """);

            // Cancelled classes — has_substitute: 1 = still counts in total, 0 = removed from total
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cancelled_classes (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id      INTEGER NOT NULL,
                    date           TEXT NOT NULL,
                    has_substitute INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (course_id) REFERENCES courses(id),
                    UNIQUE(course_id, date)
                )
            """);

            // Daily attendance records per course
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id INTEGER NOT NULL,
                    date      TEXT NOT NULL,
                    status    TEXT NOT NULL,
                    FOREIGN KEY (course_id) REFERENCES courses(id),
                    UNIQUE(course_id, date)
                )
            """);

            System.out.println("Database initialized.");

        } catch (SQLException e) {
            System.out.println("DB init error: " + e.getMessage());
        }
    }
}