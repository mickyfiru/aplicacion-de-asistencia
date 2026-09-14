package com.asistencia.service;

import com.asistencia.exception.DuplicateAttendanceException;
import com.asistencia.exception.MissingScheduleException;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.repository.dataconnect.DataConnectAttendanceGateway;
import com.asistencia.repository.dataconnect.DataConnectScheduleGateway;
import com.asistencia.time.ChileBusinessCalendar;
import com.asistencia.time.OfficialTimeProvider;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DataConnectAttendanceService {
    public static final String ENTRY_ON_TIME = "A_TIEMPO";
    public static final String ENTRY_LATE = "ATRASO";
    public static final String EXIT_NORMAL = "NORMAL";
    public static final String EXIT_EARLY = "ANTICIPADA";

    private final DataConnectAttendanceGateway attendanceGateway;
    private final DataConnectScheduleGateway scheduleGateway;
    private final OfficialTimeProvider officialTimeProvider;
    private final ChileBusinessCalendar chileCalendar;

    public DataConnectAttendanceService(
            DataConnectAttendanceGateway attendanceGateway,
            DataConnectScheduleGateway scheduleGateway,
            OfficialTimeProvider officialTimeProvider
    ) {
        this(attendanceGateway, scheduleGateway, officialTimeProvider, new ChileBusinessCalendar());
    }

    public DataConnectAttendanceService(
            DataConnectAttendanceGateway attendanceGateway,
            DataConnectScheduleGateway scheduleGateway,
            OfficialTimeProvider officialTimeProvider,
            ChileBusinessCalendar chileCalendar
    ) {
        this.attendanceGateway = attendanceGateway;
        this.scheduleGateway = scheduleGateway;
        this.officialTimeProvider = officialTimeProvider;
        this.chileCalendar = chileCalendar;
    }

    public DataConnectAttendance registerEntry(String usuarioId) {
        LocalDate fecha = chileCalendar.currentDate(officialTimeProvider);
        LocalTime horaEntradaReferencia = chileCalendar.currentMinute(officialTimeProvider);
        DataConnectSchedule horario = requireSchedule(usuarioId, fecha);

        if (attendanceGateway.findByUserAndDate(usuarioId, fecha).isPresent()) {
            throw new DuplicateAttendanceException("Ya existe una entrada para este usuario en la fecha actual.");
        }

        int minutosAtraso = Math.max(0, (int) ChronoUnit.MINUTES.between(horario.getHoraEntrada(), horaEntradaReferencia));
        String estadoEntrada = minutosAtraso == 0 ? ENTRY_ON_TIME : ENTRY_LATE;
        return attendanceGateway.createEntry(usuarioId, fecha, estadoEntrada, minutosAtraso);
    }

    public DataConnectAttendance registerExit(String usuarioId) {
        LocalDate fecha = chileCalendar.currentDate(officialTimeProvider);
        LocalTime horaSalidaReferencia = chileCalendar.currentMinute(officialTimeProvider);
        DataConnectSchedule horario = requireSchedule(usuarioId, fecha);
        DataConnectAttendance asistencia = attendanceGateway.findByUserAndDate(usuarioId, fecha)
                .orElseThrow(() -> new IllegalStateException("No puede registrar salida sin una entrada previa."));

        if (asistencia.getHoraSalida() != null) {
            throw new DuplicateAttendanceException("Ya existe una salida para este usuario en la fecha actual.");
        }

        int minutosSalidaAnticipada = Math.max(0, (int) ChronoUnit.MINUTES.between(horaSalidaReferencia, horario.getHoraSalida()));
        String estadoSalida = minutosSalidaAnticipada == 0 ? EXIT_NORMAL : EXIT_EARLY;
        return attendanceGateway.markExit(usuarioId, fecha, estadoSalida, minutosSalidaAnticipada);
    }

    public DataConnectAttendance findByUserAndDate(String usuarioId, LocalDate fecha) {
        return attendanceGateway.findByUserAndDate(usuarioId, fecha).orElse(null);
    }

    public List<DataConnectAttendance> findByUserAndDateRange(String usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return attendanceGateway.findByUserAndDateRange(usuarioId, fechaInicio, fechaFin);
    }

    public List<DataConnectAttendance> findLateArrivals(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
        return attendanceGateway.findLateArrivals(fechaInicio, fechaFin, usuarioId);
    }

    public List<DataConnectAttendance> findEarlyDepartures(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
        return attendanceGateway.findEarlyDepartures(fechaInicio, fechaFin, usuarioId);
    }

    private DataConnectSchedule requireSchedule(String usuarioId, LocalDate fecha) {
        int diaSemana = chileCalendar.dayOfWeek(fecha);
        return scheduleGateway.findActiveByUserAndDay(usuarioId, diaSemana)
                .orElseThrow(() -> new MissingScheduleException("El usuario no tiene horario activo para la fecha actual."));
    }
}
