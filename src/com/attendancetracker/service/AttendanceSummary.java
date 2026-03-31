package com.attendancetracker.service;

import com.attendancetracker.model.Course;

public class AttendanceSummary {
    private Course course;
    private int totalClasses;    // full semester projected (after removing cancelled-no-sub)
    private int heldSoFar;       // classes that have already happened (up to today)
    private int attended;        // PRESENT + OD
    private double currentPercent;
    private int safeToSkip;      // positive = can skip this many more; negative = already in danger
    private int needToAttend;    // how many more to attend to reach 75% (only when below 75%)

    public AttendanceSummary(Course course, int totalClasses, int heldSoFar,
                              int attended, double currentPercent,
                              int safeToSkip, int needToAttend) {
        this.course = course;
        this.totalClasses = totalClasses;
        this.heldSoFar = heldSoFar;
        this.attended = attended;
        this.currentPercent = currentPercent;
        this.safeToSkip = safeToSkip;
        this.needToAttend = needToAttend;
    }

    public Course getCourse()        { return course; }
    public int getTotalClasses()     { return totalClasses; }
    public int getHeldSoFar()        { return heldSoFar; }
    public int getAttended()         { return attended; }
    public double getCurrentPercent(){ return currentPercent; }
    public int getSafeToSkip()       { return safeToSkip; }
    public int getNeedToAttend()     { return needToAttend; }

    // Status tag shown in dashboard
    public String getStatusLabel() {
        if (currentPercent >= 75) return "SAFE";
        else if (currentPercent >= 65) return "WARNING";
        else return "DANGER";
    }
}
