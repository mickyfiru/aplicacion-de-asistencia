package com.asistencia.repository;

import com.asistencia.model.AttendanceFilter;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.AttendanceStatus;
import com.asistencia.model.AuditLog;
import com.asistencia.model.ExitStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryAttendanceRepository implements AttendanceRepository {
    private final Map<String, AttendanceRecord> records = new LinkedHashMap<>();
    private final Map<String, AuditLog> auditLogs = new LinkedHashMap<>();

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        if (record.getRecordId() == null || record.getRecordId().isBlank()) {
            record.setRecordId(UUID.randomUUID().toString());
        }
        records.put(record.getRecordId(), record);
        return record;
    }

    @Override
    public Optional<AttendanceRecord> findById(String recordId) {
        return Optional.ofNullable(records.get(recordId));
    }

    @Override
    public Optional<AttendanceRecord> findByWorkerAndDate(int workerId, LocalDate date) {
        return records.values().stream()
                .filter(record -> record.getWorker().getWorkerId() == workerId)
                .filter(record -> record.getDate().equals(date))
                .findFirst();
    }

    @Override
    public boolean hasEntryForDate(int workerId, LocalDate date) {
        return findByWorkerAndDate(workerId, date)
                .map(AttendanceRecord::hasEntry)
                .orElse(false);
    }

    @Override
    public boolean hasExitForDate(int workerId, LocalDate date) {
        return findByWorkerAndDate(workerId, date)
                .map(AttendanceRecord::hasExit)
                .orElse(false);
    }

    @Override
    public List<AttendanceRecord> findLateArrivals(AttendanceFilter filter) {
        return records.values().stream()
                .filter(filter::matches)
                .filter(record -> record.getAttendanceStatus() == AttendanceStatus.ATRASO)
                .sorted(Comparator.comparing(AttendanceRecord::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecord> findEarlyDepartures(AttendanceFilter filter) {
        return records.values().stream()
                .filter(filter::matches)
                .filter(record -> record.getExitStatus() == ExitStatus.SALIDA_ANTICIPADA)
                .sorted(Comparator.comparing(AttendanceRecord::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public AuditLog saveAuditLog(AuditLog auditLog) {
        if (auditLog.getAuditId() == null || auditLog.getAuditId().isBlank()) {
            auditLog.setAuditId(UUID.randomUUID().toString());
        }
        auditLogs.put(auditLog.getAuditId(), auditLog);
        return auditLog;
    }

    @Override
    public List<AuditLog> findAuditLogsByRecord(String recordId) {
        return auditLogs.values().stream()
                .filter(auditLog -> auditLog.getModifiedRecordId().equals(recordId))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
