package com.asistencia.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

public class AttendanceFilter {
    private final Integer workerId;
    private final LocalDate date;
    private final YearMonth month;

    private AttendanceFilter(Integer workerId, LocalDate date, YearMonth month) {
        if (date != null && month != null) {
            throw new IllegalArgumentException("No se puede filtrar por fecha exacta y mes al mismo tiempo");
        }
        this.workerId = workerId;
        this.date = date;
        this.month = month;
    }

    public static AttendanceFilter empty() {
        return new AttendanceFilter(null, null, null);
    }

    public static AttendanceFilter byWorker(Integer workerId) {
        return new AttendanceFilter(workerId, null, null);
    }

    public static AttendanceFilter byDate(LocalDate date) {
        return new AttendanceFilter(null, date, null);
    }

    public static AttendanceFilter byMonth(YearMonth month) {
        return new AttendanceFilter(null, null, month);
    }

    public static AttendanceFilter of(Integer workerId, LocalDate date, YearMonth month) {
        return new AttendanceFilter(workerId, date, month);
    }

    public Optional<Integer> getWorkerId() {
        return Optional.ofNullable(workerId);
    }

    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<YearMonth> getMonth() {
        return Optional.ofNullable(month);
    }

    public boolean matches(AttendanceRecord record) {
        if (workerId != null && record.getWorker().getWorkerId() != workerId) {
            return false;
        }
        if (date != null && !record.getDate().equals(date)) {
            return false;
        }
        return month == null || YearMonth.from(record.getDate()).equals(month);
    }
}
