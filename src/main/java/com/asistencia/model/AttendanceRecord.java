package com.asistencia.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceRecord {
    private String recordId;
    private final WorkerReference worker;
    private final LocalDate date;
    private LocalTime entryTime;
    private AttendanceStatus attendanceStatus;
    private int lateMinutes;
    private LocalTime exitTime;
    private ExitStatus exitStatus;
    private int missingMinutes;

    public AttendanceRecord(String recordId, WorkerReference worker, LocalDate date) {
        this.recordId = recordId;
        this.worker = worker;
        this.date = date;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public WorkerReference getWorker() {
        return worker;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalTime entryTime) {
        this.entryTime = entryTime;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(int lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalTime exitTime) {
        this.exitTime = exitTime;
    }

    public ExitStatus getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(ExitStatus exitStatus) {
        this.exitStatus = exitStatus;
    }

    public int getMissingMinutes() {
        return missingMinutes;
    }

    public void setMissingMinutes(int missingMinutes) {
        this.missingMinutes = missingMinutes;
    }

    public boolean hasEntry() {
        return entryTime != null;
    }

    public boolean hasExit() {
        return exitTime != null;
    }
}
