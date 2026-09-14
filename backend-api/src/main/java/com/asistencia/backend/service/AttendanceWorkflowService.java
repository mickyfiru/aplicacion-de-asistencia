package com.asistencia.backend.service;

import com.asistencia.backend.dataconnect.AttendanceDataGateway;
import com.asistencia.backend.model.BackendAttendance;
import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.BackendUser;
import com.asistencia.backend.model.EntryResult;
import com.asistencia.backend.model.ExitResult;
import com.asistencia.backend.model.TodayAttendanceResult;
import com.asistencia.backend.security.AuthenticatedUser;
import com.asistencia.backend.web.ConflictException;
import com.asistencia.backend.web.ForbiddenException;
import com.asistencia.backend.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class AttendanceWorkflowService {
    public static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");
    public static final String ENTRY_PENDING = "PENDIENTE";
    public static final String ENTRY_ON_TIME = "A_TIEMPO";
    public static final String ENTRY_LATE = "ATRASO";
    public static final String EXIT_NORMAL = "NORMAL";
    public static final String EXIT_EARLY = "ANTICIPADA";

    private final AttendanceDataGateway dataGateway;
    private final Clock backendClock;

    public AttendanceWorkflowService(AttendanceDataGateway dataGateway, Clock backendClock) {
        this.dataGateway = dataGateway;
        this.backendClock = backendClock;
    }

    public EntryResult markEntry(AuthenticatedUser authUser) {
        BackendUser user = requireActiveUser(authUser);
        LocalDate businessDate = businessDate();
        BackendSchedule schedule = requireSchedule(user.id(), businessDate);
        if (dataGateway.findAttendance(user.id(), businessDate).isPresent()) {
            throw new ConflictException("ASISTENCIA_YA_REGISTRADA", "Ya existe una entrada registrada para hoy.");
        }

        BackendAttendance created = dataGateway.createServerEntry(user.id(), businessDate);
        LocalTime serverEntryTime = toChileTime(created.horaEntrada());
        int lateMinutes = Math.max(0, (int) ChronoUnit.MINUTES.between(schedule.horaEntrada(), serverEntryTime));
        String state = lateMinutes == 0 ? ENTRY_ON_TIME : ENTRY_LATE;
        BackendAttendance updated = dataGateway.updateEntryResult(user.id(), businessDate, state, lateMinutes);
        return new EntryResult(updated.fecha(), toChileTime(updated.horaEntrada()), updated.estadoEntrada(), updated.minutosAtraso());
    }

    public ExitResult markExit(AuthenticatedUser authUser) {
        BackendUser user = requireActiveUser(authUser);
        LocalDate businessDate = businessDate();
        BackendSchedule schedule = requireSchedule(user.id(), businessDate);
        BackendAttendance existing = dataGateway.findAttendance(user.id(), businessDate)
                .orElseThrow(() -> new ConflictException("SALIDA_SIN_ENTRADA", "No puede registrar salida sin una entrada previa."));
        if (existing.horaSalida() != null) {
            throw new ConflictException("SALIDA_YA_REGISTRADA", "La salida de hoy ya fue registrada.");
        }

        BackendAttendance withServerExit = dataGateway.setServerExit(user.id(), businessDate);
        LocalTime serverExitTime = toChileTime(withServerExit.horaSalida());
        int earlyMinutes = Math.max(0, (int) ChronoUnit.MINUTES.between(serverExitTime, schedule.horaSalida()));
        String state = earlyMinutes == 0 ? EXIT_NORMAL : EXIT_EARLY;
        BackendAttendance updated = dataGateway.updateExitResult(user.id(), businessDate, state, earlyMinutes);
        return new ExitResult(updated.fecha(), toChileTime(updated.horaSalida()), updated.estadoSalida(), updated.minutosSalidaAnticipada());
    }

    public TodayAttendanceResult today(AuthenticatedUser authUser) {
        BackendUser user = requireActiveUser(authUser);
        LocalDate businessDate = businessDate();
        return dataGateway.findAttendance(user.id(), businessDate)
                .map(this::toTodayResult)
                .orElse(null);
    }

    public BackendSchedule mySchedule(AuthenticatedUser authUser) {
        BackendUser user = requireActiveUser(authUser);
        LocalDate businessDate = businessDate();
        return requireSchedule(user.id(), businessDate);
    }

    private BackendUser requireActiveUser(AuthenticatedUser authUser) {
        BackendUser user = dataGateway.findUserByAuthUid(authUser.uid())
                .orElseThrow(() -> new NotFoundException("El usuario autenticado no está registrado en el sistema de asistencia."));
        if (!user.activo()) {
            throw new ForbiddenException("El usuario no esta activo.");
        }
        return user;
    }

    private BackendSchedule requireSchedule(String userId, LocalDate businessDate) {
        int chileDay = businessDate.getDayOfWeek().getValue();
        return dataGateway.findActiveSchedule(userId, chileDay)
                .orElseThrow(() -> new NotFoundException("No existe horario activo para hoy."));
    }

    private LocalDate businessDate() {
        return backendClock.instant().atZone(CHILE_ZONE).toLocalDate();
    }

    private LocalTime toChileTime(java.time.OffsetDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atZone(CHILE_ZONE).toLocalTime().truncatedTo(ChronoUnit.SECONDS);
    }

    private TodayAttendanceResult toTodayResult(BackendAttendance attendance) {
        return new TodayAttendanceResult(
                attendance.fecha(),
                toChileTime(attendance.horaEntrada()),
                attendance.estadoEntrada(),
                attendance.minutosAtraso(),
                toChileTime(attendance.horaSalida()),
                attendance.estadoSalida(),
                attendance.minutosSalidaAnticipada()
        );
    }
}
