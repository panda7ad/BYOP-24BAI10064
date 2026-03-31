package com.attendancetracker.service;

import com.attendancetracker.dao.*;
import com.attendancetracker.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AttendanceService computes per-course attendance statistics.
 *
 * KEY FORMULA:
 *   totalClasses   = scheduled dates - cancelled-without-substitute dates
 *   heldSoFar      = class dates up to today (not counting future)
 *   attended       = PRESENT + OD records
 *   currentPercent = attended / heldSoFar * 100
 *   safeToSkip     = classes you can still miss and stay ≥75% overall
 *   needToAttend   = classes you must attend to recover to 75% (shown when below 75%)
 */
public class AttendanceService {

    private final ScheduleService scheduleService = new ScheduleService();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final CancelledClassDAO cancelledClassDAO = new CancelledClassDAO();

    public AttendanceSummary getSummary(Course course) {
        LocalDate today = LocalDate.now();

        // --- Step 1: All dates this course was scheduled ---
        List<LocalDate> allScheduled = scheduleService.getScheduledDates(course);

        // --- Step 2: Cancelled classes for this course ---
        List<CancelledClass> cancelled = cancelledClassDAO.getByCourseId(course.getId());

        // Dates cancelled WITHOUT substitute (class simply didn't happen, remove from total)
        Set<LocalDate> cancelledNoSub = cancelled.stream()
            .filter(cc -> !cc.isHasSubstitute())
            .map(CancelledClass::getDate)
            .collect(Collectors.toSet());

        // --- Step 3: Effective class dates (remove cancelled-no-sub) ---
        List<LocalDate> effectiveDates = allScheduled.stream()
            .filter(d -> !cancelledNoSub.contains(d))
            .collect(Collectors.toList());

        int totalClasses = effectiveDates.size();

        // --- Step 4: Classes held so far (up to and including today) ---
        int heldSoFar = (int) effectiveDates.stream()
            .filter(d -> !d.isAfter(today))
            .count();

        // --- Step 5: Attended = PRESENT + OD records ---
        List<AttendanceRecord> records = attendanceDAO.getByCourseId(course.getId());
        int attended = (int) records.stream()
            .filter(r -> r.getStatus().equals("PRESENT") || r.getStatus().equals("OD"))
            .count();

        // --- Step 6: Current percentage ---
        double currentPercent = heldSoFar > 0
            ? Math.round((attended * 100.0 / heldSoFar) * 10.0) / 10.0
            : 0.0;

        // --- Step 7: Safe to skip (future classes you can afford to miss) ---
        // We need: (attended + futureAttend) / totalClasses >= 0.75
        // Min total attended needed = ceil(0.75 * totalClasses)
        int minNeeded = (int) Math.ceil(0.75 * totalClasses);
        int remaining = totalClasses - heldSoFar;
        int alreadyShort = Math.max(0, minNeeded - attended);
        // safeToSkip = remaining future classes - how many you MUST attend
        int safeToSkip = remaining - alreadyShort;

        // --- Step 8: How many to attend to recover (only when below 75%) ---
        // (attended + x) / (heldSoFar + x) >= 0.75
        // attended + x >= 0.75 * heldSoFar + 0.75x
        // 0.25x >= 0.75*heldSoFar - attended
        // x >= (0.75*heldSoFar - attended) / 0.25
        int needToAttend = 0;
        if (currentPercent < 75 && heldSoFar > 0) {
            double shortfall = (0.75 * heldSoFar) - attended;
            needToAttend = shortfall > 0 ? (int) Math.ceil(shortfall / 0.25) : 0;
        }

        return new AttendanceSummary(course, totalClasses, heldSoFar,
                                      attended, currentPercent, safeToSkip, needToAttend);
    }

    public List<AttendanceSummary> getAllSummaries(List<Course> courses) {
        List<AttendanceSummary> summaries = new ArrayList<>();
        for (Course c : courses) {
            summaries.add(getSummary(c));
        }
        return summaries;
    }

    /**
     * Mark attendance for a course on a given date.
     * If OD, first checks the monthly OD count limit (2 per month).
     * Returns true if marked successfully, false if OD limit exceeded.
     */
    public boolean markAttendance(int courseId, LocalDate date, String status) {
        if (status.equals("OD")) {
            int odThisMonth = attendanceDAO.countTotalODInMonth(date.getYear(), date.getMonthValue());
            if (odThisMonth >= 2) {
                return false; // OD limit reached for this month
            }
        }
        return attendanceDAO.upsert(courseId, date, status);
    }

    public String getStatusForDate(int courseId, LocalDate date) {
        return attendanceDAO.getStatus(courseId, date);
    }

    public int getODCountThisMonth(LocalDate date) {
        return attendanceDAO.countTotalODInMonth(date.getYear(), date.getMonthValue());
    }

    /**
     * Records a class cancellation.
     * hasSub = true  → substitute/assignment given; class still counts in total
     * hasSub = false → class simply cancelled; reduces total count
     */
    public boolean recordCancellation(int courseId, LocalDate date, boolean hasSub) {
        CancelledClass cc = new CancelledClass(courseId, date, hasSub);
        return cancelledClassDAO.add(cc);
    }

    public CancelledClass getCancellation(int courseId, LocalDate date) {
        return cancelledClassDAO.getByCoursAndDate(courseId, date);
    }
}