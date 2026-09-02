package com.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class EarlyDepartureReportItem {
    private final int workerId;
    private final String workerName;
    private final String workerIdentifier;
    private final LocalDate date;
    private final LocalTime exitTime;
    private final int missingMinutes;
    private final ExitStatus status;

    public EarlyDepartureReportItem(AttendanceRecord record) {
        this.workerId = record.getWorker().getWorkerId();
        this.workerName = record.getWorker().getName();
        this.workerIdentifier = record.getWorker().getIdentifier();
        this.date = record.getDate();
        this.exitTime = record.getExitTime();
        this.missingMinutes = record.getMissingMinutes();
        this.status = record.getExitStatus();
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

    public LocalTime getExitTime() {
        return exitTime;
    }

    public int getMissingMinutes() {
        return missingMinutes;
    }

    public ExitStatus getStatus() {
        return status;
    }
}
