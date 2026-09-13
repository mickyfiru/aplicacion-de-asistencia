package com.asistencia.service;

import com.asistencia.exception.DuplicateAttendanceException;
import com.asistencia.exception.MissingScheduleException;
import com.asistencia.model.AbsenceReportItem;
import com.asistencia.model.AttendanceCorrectionRequest;
import com.asistencia.model.AttendanceFilter;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.AttendanceStatus;
import com.asistencia.model.AuditLog;
import com.asistencia.model.EarlyDepartureReportItem;
import com.asistencia.model.ExitStatus;
import com.asistencia.model.LateArrivalReportItem;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.model.WorkerReference;
import com.asistencia.model.WorkSchedule;
import com.asistencia.repository.AttendanceRepository;
import com.asistencia.repository.DefaultScheduleRepository;
import com.asistencia.repository.ScheduleRepository;
import com.asistencia.time.OfficialTimeProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceService {
    public static final LocalTime OFFICIAL_ENTRY_TIME = LocalTime.of(8, 0);
    public static final LocalTime OFFICIAL_EXIT_TIME = LocalTime.of(17, 24);
    public static final int DAILY_LUNCH_MINUTES = 60;
    public static final int WEEKLY_WORK_MINUTES = 42 * 60;

    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final OfficialTimeProvider officialTimeProvider;

    public AttendanceService(AttendanceRepository attendanceRepository, OfficialTimeProvider officialTimeProvider) {
        this(attendanceRepository, officialTimeProvider, new DefaultScheduleRepository());
    }

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            OfficialTimeProvider officialTimeProvider,
            ScheduleRepository scheduleRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.officialTimeProvider = officialTimeProvider;
        this.scheduleRepository = scheduleRepository;
    }

    public AttendanceRecord registerEntry(WorkerReference worker) {
        LocalDateTime officialNow = officialTimeProvider.now();
        LocalDate date = officialNow.toLocalDate();
        LocalTime entryTime = officialNow.toLocalTime().withSecond(0).withNano(0);
        WorkSchedule schedule = requireSchedule(worker, date);

        if (attendanceRepository.hasEntryForDate(worker.getWorkerId(), date)) {
            throw new DuplicateAttendanceException("Ya existe un registro de entrada para este trabajador en la fecha actual.");
        }

        AttendanceRecord record = attendanceRepository.findByWorkerAndDate(worker.getWorkerId(), date)
                .orElseGet(() -> new AttendanceRecord(null, worker, date));
        applyEntryCalculation(record, entryTime, schedule.getEntryTime());
        return attendanceRepository.save(record);
    }

    public AttendanceRecord registerExit(WorkerReference worker) {
        LocalDateTime officialNow = officialTimeProvider.now();
        LocalDate date = officialNow.toLocalDate();
        LocalTime exitTime = officialNow.toLocalTime().withSecond(0).withNano(0);
        WorkSchedule schedule = requireSchedule(worker, date);

        if (!attendanceRepository.hasEntryForDate(worker.getWorkerId(), date)) {
            throw new IllegalStateException("No puede registrar salida sin una entrada previa.");
        }
        if (attendanceRepository.hasExitForDate(worker.getWorkerId(), date)) {
            throw new DuplicateAttendanceException("Ya existe un registro de salida para este trabajador en la fecha actual.");
        }

        AttendanceRecord record = attendanceRepository.findByWorkerAndDate(worker.getWorkerId(), date)
                .orElseThrow(() -> new IllegalStateException("No se encontro el registro de entrada del dia."));
        applyExitCalculation(record, exitTime, schedule.getExitTime());
        return attendanceRepository.save(record);
    }

    public List<LateArrivalReportItem> findLateArrivals(AttendanceFilter filter) {
        return attendanceRepository.findLateArrivals(filter).stream()
                .map(LateArrivalReportItem::new)
                .collect(Collectors.toList());
    }

    public List<EarlyDepartureReportItem> findEarlyDepartures(AttendanceFilter filter) {
        return attendanceRepository.findEarlyDepartures(filter).stream()
                .map(EarlyDepartureReportItem::new)
                .collect(Collectors.toList());
    }

    public List<AbsenceReportItem> findAbsences(List<WorkerReference> workers, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Debe indicar una fecha para consultar inasistencias");
        }
        return workers.stream()
                .filter(worker -> scheduleRepository.findByWorkerAndDate(worker.getWorkerId(), date).isPresent())
                .filter(worker -> attendanceRepository.findByWorkerAndDate(worker.getWorkerId(), date).isEmpty())
                .map(worker -> new AbsenceReportItem(worker, date))
                .collect(Collectors.toList());
    }

    public AttendanceRecord correctRecord(AttendanceCorrectionRequest request, Usuario administrator) {
        validateAdministrator(administrator);
        AttendanceRecord record = attendanceRepository.findById(request.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el registro a corregir"));

        String previousValue = describeRecord(record);
        WorkSchedule schedule = requireSchedule(record.getWorker(), record.getDate());
        request.getCorrectedEntryTime().ifPresent(time -> applyEntryCalculation(record, time, schedule.getEntryTime()));
        request.getCorrectedExitTime().ifPresent(time -> applyExitCalculation(record, time, schedule.getExitTime()));
        AttendanceRecord saved = attendanceRepository.save(record);

        AuditLog auditLog = new AuditLog(
                null,
                saved.getRecordId(),
                previousValue,
                describeRecord(saved),
                administrator.getId(),
                administrator.getNombre(),
                officialTimeProvider.now(),
                request.getReason()
        );
        attendanceRepository.saveAuditLog(auditLog);
        return saved;
    }

    private WorkSchedule requireSchedule(WorkerReference worker, LocalDate date) {
        return scheduleRepository.findByWorkerAndDate(worker.getWorkerId(), date)
                .orElseThrow(() -> new MissingScheduleException("El trabajador no tiene horario asignado para la fecha indicada."));
    }

    private void applyEntryCalculation(AttendanceRecord record, LocalTime entryTime, LocalTime scheduledEntryTime) {
        long lateMinutes = Math.max(0, ChronoUnit.MINUTES.between(scheduledEntryTime, entryTime));
        record.setEntryTime(entryTime);
        record.setLateMinutes((int) lateMinutes);
        record.setAttendanceStatus(lateMinutes == 0 ? AttendanceStatus.A_TIEMPO : AttendanceStatus.ATRASO);
    }

    private void applyExitCalculation(AttendanceRecord record, LocalTime exitTime, LocalTime scheduledExitTime) {
        long missingMinutes = Math.max(0, ChronoUnit.MINUTES.between(exitTime, scheduledExitTime));
        record.setExitTime(exitTime);
        record.setMissingMinutes((int) missingMinutes);
        record.setExitStatus(missingMinutes == 0 ? ExitStatus.SALIDA_NORMAL : ExitStatus.SALIDA_ANTICIPADA);
    }

    private void validateAdministrator(Usuario administrator) {
        if (administrator == null || administrator.getId() == null || administrator.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException("Solo un administrador puede corregir registros de asistencia.");
        }
    }

    private String describeRecord(AttendanceRecord record) {
        return "entrada=" + record.getEntryTime()
                + ", estadoEntrada=" + record.getAttendanceStatus()
                + ", minutosAtraso=" + record.getLateMinutes()
                + ", salida=" + record.getExitTime()
                + ", estadoSalida=" + record.getExitStatus()
                + ", minutosFaltantes=" + record.getMissingMinutes();
    }
}
