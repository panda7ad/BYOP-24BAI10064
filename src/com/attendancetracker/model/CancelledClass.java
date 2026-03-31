package com.attendancetracker.model;

import java.time.LocalDate;

public class CancelledClass {
    private int id;
    private int courseId;
    private LocalDate date;
    private boolean hasSubstitute; // true = class still counts in total

    public CancelledClass(int id, int courseId, LocalDate date, boolean hasSubstitute) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.hasSubstitute = hasSubstitute;
    }

    public CancelledClass(int courseId, LocalDate date, boolean hasSubstitute) {
        this.courseId = courseId;
        this.date = date;
        this.hasSubstitute = hasSubstitute;
    }

    public int getId()            { return id; }
    public int getCourseId()      { return courseId; }
    public LocalDate getDate()    { return date; }
    public boolean isHasSubstitute() { return hasSubstitute; }
}