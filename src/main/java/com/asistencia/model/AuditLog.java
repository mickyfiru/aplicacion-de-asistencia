package com.asistencia.model;

import java.time.LocalDateTime;

public class AuditLog {
    private String auditId;
    private final String modifiedRecordId;
    private final String previousValue;
    private final String newValue;
    private final int administratorId;
    private final String administratorName;
    private final LocalDateTime dateTime;
    private final String reason;

    public AuditLog(
            String auditId,
            String modifiedRecordId,
            String previousValue,
            String newValue,
            int administratorId,
            String administratorName,
            LocalDateTime dateTime,
            String reason
    ) {
        this.auditId = auditId;
        this.modifiedRecordId = modifiedRecordId;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.administratorId = administratorId;
        this.administratorName = administratorName;
        this.dateTime = dateTime;
        this.reason = reason;
    }

    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    public String getModifiedRecordId() {
        return modifiedRecordId;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public int getAdministratorId() {
        return administratorId;
    }

    public String getAdministratorName() {
        return administratorName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getReason() {
        return reason;
    }
}
