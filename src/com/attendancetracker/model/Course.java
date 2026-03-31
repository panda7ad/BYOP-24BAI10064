package com.attendancetracker.model;

import java.util.Arrays;
import java.util.List;

public class Course {
    private int id;
    private String name;
    private int credit;
    private List<String> days; // ["MON", "WED", "FRI"]

    public Course(int id, String name, int credit, String daysStr) {
        this.id = id;
        this.name = name;
        this.credit = credit;
        this.days = Arrays.asList(daysStr.split(","));
    }

    public Course(String name, int credit, List<String> days) {
        this.name = name;
        this.credit = credit;
        this.days = days;
    }

    public int getId()          { return id; }
    public String getName()     { return name; }
    public int getCredit()      { return credit; }
    public List<String> getDays() { return days; }

    // Returns days as comma-separated string for DB storage
    public String getDaysAsString() { return String.join(",", days); }

    @Override
    public String toString() { return name; }
}