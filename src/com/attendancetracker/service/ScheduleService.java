package com.attendancetracker.service;

import com.attendancetracker.dao.*;
import com.attendancetracker.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ScheduleService answers: "On which dates does a given course have class?"
 * It accounts for:
 *   - Semester start/end boundaries
 *   - Sundays (always off)
 *   - Regular Saturdays (off unless in special_saturdays table)
 *   - National holidays
 *   - Midterm exam period (no classes)
 *   - Endterm exam period (no classes)
 *   - Special Saturdays (follow another day's timetable)
 */
public class ScheduleService {

    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final HolidayDAO holidayDAO = new HolidayDAO();
    private final SpecialSaturdayDAO specialSaturdayDAO = new SpecialSaturdayDAO();

    /**
     * Returns all dates in the semester where this course has a scheduled class.
     * Does NOT factor in cancellations — that's done in AttendanceService.
     */
    public List<LocalDate> getScheduledDates(Course course) {
        SemesterSettings settings = settingsDAO.getSettings();
        if (settings == null || settings.getSemStart() == null || settings.getSemEnd() == null) {
            return new ArrayList<>();
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = settings.getSemStart();
        LocalDate end = settings.getSemEnd();

        while (!current.isAfter(end)) {
            String effectiveDay = getEffectiveDay(current, settings);
            if (effectiveDay != null && course.getDays().contains(effectiveDay)) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }
        return dates;
    }

    /**
     * Returns courses that have class on a specific date (used in MarkAttendancePanel).
     */
    public List<Course> getCoursesForDate(LocalDate date, List<Course> allCourses) {
        SemesterSettings settings = settingsDAO.getSettings();
        if (settings == null) return new ArrayList<>();

        String effectiveDay = getEffectiveDay(date, settings);
        if (effectiveDay == null) return new ArrayList<>();

        List<Course> result = new ArrayList<>();
        for (Course course : allCourses) {
            if (course.getDays().contains(effectiveDay)) {
                result.add(course);
            }
        }
        return result;
    }

    /**
     * Returns the effective day-of-week label for a given date, or null if no classes.
     *
     * Rules:
     *  - Sunday      → null (always off)
     *  - Saturday    → check special_saturdays; if found return its follows_day; else null
     *  - Holiday     → null
     *  - Exam period → null
     *  - Otherwise   → MON / TUE / WED / THU / FRI
     */
    public String getEffectiveDay(LocalDate date, SemesterSettings settings) {
        DayOfWeek dow = date.getDayOfWeek();

        // Sunday is always off
        if (dow == DayOfWeek.SUNDAY) return null;

        // Saturday: only on if it's a special saturday
        if (dow == DayOfWeek.SATURDAY) {
            SpecialSaturday ss = specialSaturdayDAO.getByDate(date);
            return ss != null ? ss.getFollowsDay() : null;
        }

        // National holiday
        if (holidayDAO.isHoliday(date)) return null;

        // Midterm exam period
        if (settings.getMidtermStart() != null && settings.getMidtermEnd() != null) {
            if (!date.isBefore(settings.getMidtermStart())
             && !date.isAfter(settings.getMidtermEnd())) return null;
        }

        // Endterm exam period
        if (settings.getEndtermStart() != null && settings.getEndtermEnd() != null) {
            if (!date.isBefore(settings.getEndtermStart())
             && !date.isAfter(settings.getEndtermEnd())) return null;
        }

        // Regular weekday
        return switch (dow) {
            case MONDAY    -> "MON";
            case TUESDAY   -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY  -> "THU";
            case FRIDAY    -> "FRI";
            default        -> null;
        };
    }

    /**
     * Checks if a given date is a no-class day (holiday, exam, Sunday, off-Saturday).
     */
    public boolean isNoClassDay(LocalDate date) {
        SemesterSettings settings = settingsDAO.getSettings();
        if (settings == null) return true;
        return getEffectiveDay(date, settings) == null;
    }

    public SemesterSettings getSettings() {
        return settingsDAO.getSettings();
    }
}