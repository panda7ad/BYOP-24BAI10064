package com.attendancetracker.model;

import java.time.LocalDate;

public class AttendanceRecord {
    private int id;
    private int courseId;
    private LocalDate date;
    private String status; // PRESENT, ABSENT, OD

    public AttendanceRecord(int id, int courseId, LocalDate date, String status) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.status = status;
    }

    public AttendanceRecord(int courseId, LocalDate date, String status) {
        this.courseId = courseId;
        this.date = date;
        this.status = status;
    }

    public int getId()        { return id; }
    public int getCourseId()  { return courseId; }
    public LocalDate getDate(){ return date; }
    public String getStatus() { return status; }
}