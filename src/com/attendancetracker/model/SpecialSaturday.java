package com.attendancetracker.model;

import java.time.LocalDate;

public class SpecialSaturday {
    private int id;
    private LocalDate date;
    private String followsDay; // "MON", "TUE", etc.

    public SpecialSaturday(int id, LocalDate date, String followsDay) {
        this.id = id;
        this.date = date;
        this.followsDay = followsDay;
    }

    public SpecialSaturday(LocalDate date, String followsDay) {
        this.date = date;
        this.followsDay = followsDay;
    }

    public int getId()           { return id; }
    public LocalDate getDate()   { return date; }
    public String getFollowsDay(){ return followsDay; }
}