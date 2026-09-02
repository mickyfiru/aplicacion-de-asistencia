package com.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class LateArrivalReportItem {
    private final int workerId;
    private final String workerName;
    private final String workerIdentifier;
    private final LocalDate date;
    private final LocalTime entryTime;
    private final int lateMinutes;
    private final AttendanceStatus status;

    public LateArrivalReportItem(AttendanceRecord record) {
        this.workerId = record.getWorker().getWorkerId();
        this.workerName = record.getWorker().getName();
        this.workerIdentifier = record.getWorker().getIdentifier();
        this.date = record.getDate();
        this.entryTime = record.getEntryTime();
        this.lateMinutes = record.getLateMinutes();
        this.status = record.getAttendanceStatus();
    }

    public int getWorkerId() {
        return workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public String getWorkerIdentifier() {
        return workerIdentifier;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}
