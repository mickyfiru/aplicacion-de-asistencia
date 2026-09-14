package com.asistencia.service;

import com.asistencia.exception.DuplicateAttendanceException;
import com.asistencia.exception.MissingScheduleException;
import com.asistencia.model.Asistencia;
import com.asistencia.model.Usuario;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectGateway;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

public class FirebaseWorkerAttendanceService implements WorkerAttendanceOperations {
    private final CurrentUserDataConnectGateway gateway;
    private final AttendancePresentationCalculator calculator;
    private final Clock uiClock;

    public FirebaseWorkerAttendanceService(CurrentUserDataConnectGateway gateway) {
        this(gateway, new AttendancePresentationCalculator(), Clock.system(AttendancePresentationCalculator.CHILE_ZONE));
    }

    public FirebaseWorkerAttendanceService(
            CurrentUserDataConnectGateway gateway,
            AttendancePresentationCalculator calculator,
            Clock uiClock
    ) {
        this.gateway = gateway;
        this.calculator = calculator;
        this.uiClock = uiClock;
    }

    @Override
    public Asistencia registrarEntrada(Usuario usuario) {
        requireActiveCurrentUser();
        blockDuplicateEntryForVisibleChileDay();
        DataConnectAttendance attendance = gateway.markMyEntry();
        DataConnectSchedule schedule = requireSchedule(attendance);
        calculator.entryState(attendance, schedule);
        calculator.lateMinutes(attendance, schedule);
        return null;
    }

    @Override
    public Asistencia registrarSalida(Usuario usuario) {
        requireActiveCurrentUser();
        DataConnectAttendance attendance = gateway.markMyExit();
        DataConnectSchedule schedule = requireSchedule(attendance);
        calculator.exitState(attendance, schedule);
        calculator.earlyExitMinutes(attendance, schedule);
        return null;
    }

    @Override
    public String obtenerEstadoActual(Usuario usuario) {
        Optional<DataConnectAttendance> openAttendance = gateway.findOpenAttendance();
        if (openAttendance.isPresent()) {
            DataConnectAttendance attendance = openAttendance.get();
            DataConnectSchedule schedule = requireSchedule(attendance);
            return "Entrada registrada: "
                    + calculator.chileTime(attendance.getHoraEntrada())
                    + " "
                    + calculator.entryState(attendance, schedule)
                    + " (" + calculator.lateMinutes(attendance, schedule) + " min)";
        }
        return gateway.findLatestAttendance()
                .filter(attendance -> attendance.getHoraSalida() != null)
                .map(attendance -> {
                    DataConnectSchedule schedule = requireSchedule(attendance);
                    return "Salida registrada: "
                            + calculator.chileTime(attendance.getHoraSalida())
                            + " "
                            + calculator.exitState(attendance, schedule)
                            + " (" + calculator.earlyExitMinutes(attendance, schedule) + " min)";
                })
                .orElse("Sin registro");
    }

    private DataConnectUser requireActiveCurrentUser() {
        DataConnectUser user = gateway.findCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Tu cuenta está autenticada, pero no está registrada en el sistema de asistencia."));
        if (!user.isActivo()) {
            throw new IllegalStateException("Tu usuario no está activo en el sistema de asistencia.");
        }
        return user;
    }

    private void blockDuplicateEntryForVisibleChileDay() {
        LocalDate visibleChileDate = LocalDate.now(uiClock);
        gateway.findLatestAttendance()
                .filter(attendance -> calculator.chileDate(attendance.getHoraEntrada()).equals(visibleChileDate))
                .ifPresent(attendance -> {
                    throw new DuplicateAttendanceException("Ya existe una entrada registrada para el día Chile visible.");
                });
    }

    private DataConnectSchedule requireSchedule(DataConnectAttendance attendance) {
        int chileDay = calculator.chileDate(attendance.getHoraEntrada()).getDayOfWeek().getValue();
        return gateway.findCurrentSchedule(chileDay)
                .orElseThrow(() -> new MissingScheduleException("No existe horario activo para el día de la asistencia."));
    }
}
