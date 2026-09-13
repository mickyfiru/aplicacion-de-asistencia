package com.asistencia.model;

import java.time.LocalDate;

public class AbsenceReportItem {
    private final int workerId;
    private final String workerName;
    private final String workerIdentifier;
    private final LocalDate date;
    private final String status;

    public AbsenceReportItem(WorkerReference worker, LocalDate date) {
        this.workerId = worker.getWorkerId();
        this.workerName = worker.getName();
        this.workerIdentifier = worker.getIdentifier();
        this.date = date;
        this.status = "INASISTENCIA";
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

    public String getStatus() {
        return status;
    }
}
