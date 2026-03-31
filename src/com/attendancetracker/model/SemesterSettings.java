package com.attendancetracker.model;

import java.time.LocalDate;

public class SemesterSettings {
    private String studentName;
    private String branch;
    private LocalDate semStart;
    private LocalDate semEnd;
    private LocalDate midtermStart;
    private LocalDate midtermEnd;
    private LocalDate endtermStart;
    private LocalDate endtermEnd;

    public SemesterSettings() {}

    // Getters
    public String getStudentName()      { return studentName; }
    public String getBranch()           { return branch; }
    public LocalDate getSemStart()      { return semStart; }
    public LocalDate getSemEnd()        { return semEnd; }
    public LocalDate getMidtermStart()  { return midtermStart; }
    public LocalDate getMidtermEnd()    { return midtermEnd; }
    public LocalDate getEndtermStart()  { return endtermStart; }
    public LocalDate getEndtermEnd()    { return endtermEnd; }

    // Setters
    public void setStudentName(String studentName)      { this.studentName = studentName; }
    public void setBranch(String branch)                { this.branch = branch; }
    public void setSemStart(LocalDate semStart)         { this.semStart = semStart; }
    public void setSemEnd(LocalDate semEnd)             { this.semEnd = semEnd; }
    public void setMidtermStart(LocalDate midtermStart) { this.midtermStart = midtermStart; }
    public void setMidtermEnd(LocalDate midtermEnd)     { this.midtermEnd = midtermEnd; }
    public void setEndtermStart(LocalDate endtermStart) { this.endtermStart = endtermStart; }
    public void setEndtermEnd(LocalDate endtermEnd)     { this.endtermEnd = endtermEnd; }

    public boolean isComplete() {
        return studentName != null && !studentName.isEmpty()
            && semStart != null && semEnd != null
            && midtermStart != null && midtermEnd != null
            && endtermStart != null && endtermEnd != null;
    }
}