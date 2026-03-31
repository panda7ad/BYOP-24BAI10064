package com.attendancetracker.model;

import java.time.LocalDate;

public class Holiday {
    private int id;
    private LocalDate date;
    private String reason;

    public Holiday(int id, LocalDate date, String reason) {
        this.id = id;
        this.date = date;
        this.reason = reason;
    }

    public Holiday(LocalDate date, String reason) {
        this.date = date;
        this.reason = reason;
    }

    public int getId()         { return id; }
    public LocalDate getDate() { return date; }
    public String getReason()  { return reason; }
}