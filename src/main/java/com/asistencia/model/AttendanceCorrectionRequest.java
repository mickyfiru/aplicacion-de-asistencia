package com.asistencia.model;

import java.time.LocalTime;
import java.util.Optional;

public class AttendanceCorrectionRequest {
    private final String recordId;
    private final LocalTime correctedEntryTime;
    private final LocalTime correctedExitTime;
    private final String reason;

    public AttendanceCorrectionRequest(
            String recordId,
            LocalTime correctedEntryTime,
            LocalTime correctedExitTime,
            String reason
    ) {
        if (recordId == null || recordId.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el registro a corregir");
        }
        if (correctedEntryTime == null && correctedExitTime == null) {
            throw new IllegalArgumentException("Debe indicar al menos un valor corregido");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar un motivo de correccion");
        }
        this.recordId = recordId.trim();
        this.correctedEntryTime = correctedEntryTime;
        this.correctedExitTime = correctedExitTime;
        this.reason = reason.trim();
    }

    public String getRecordId() {
        return recordId;
    }

    public Optional<LocalTime> getCorrectedEntryTime() {
        return Optional.ofNullable(correctedEntryTime);
    }

    public Optional<LocalTime> getCorrectedExitTime() {
        return Optional.ofNullable(correctedExitTime);
    }

    public String getReason() {
        return reason;
    }
}
