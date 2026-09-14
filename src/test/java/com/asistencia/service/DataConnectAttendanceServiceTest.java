package com.asistencia.service;

import com.asistencia.exception.DuplicateAttendanceException;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.repository.dataconnect.DataConnectAttendanceGateway;
import com.asistencia.repository.dataconnect.DataConnectScheduleGateway;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataConnectAttendanceServiceTest {
    private static final String USER_ID = "00000000-0000-0000-0000-000000000001";

    @Test
    void entradaATiempo() {
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:00:00", new FakeDataConnectAttendanceGateway());

        DataConnectAttendance attendance = service.registerEntry(USER_ID);

        assertEquals(DataConnectAttendanceService.ENTRY_ON_TIME, attendance.getEstadoEntrada());
        assertEquals(0, attendance.getMinutosAtraso());
    }

    @Test
    void atraso() {
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:12:00", new FakeDataConnectAttendanceGateway());

        DataConnectAttendance attendance = service.registerEntry(USER_ID);

        assertEquals(DataConnectAttendanceService.ENTRY_LATE, attendance.getEstadoEntrada());
        assertEquals(12, attendance.getMinutosAtraso());
    }

    @Test
    void salidaNormal() {
        FakeDataConnectAttendanceGateway attendanceGateway = new FakeDataConnectAttendanceGateway();
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:00:00", attendanceGateway);
        service.registerEntry(USER_ID);
        service = serviceAt("2026-09-02T17:30:00", attendanceGateway);

        DataConnectAttendance attendance = service.registerExit(USER_ID);

        assertEquals(DataConnectAttendanceService.EXIT_NORMAL, attendance.getEstadoSalida());
        assertEquals(0, attendance.getMinutosSalidaAnticipada());
    }

    @Test
    void salidaAnticipada() {
        FakeDataConnectAttendanceGateway attendanceGateway = new FakeDataConnectAttendanceGateway();
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:00:00", attendanceGateway);
        service.registerEntry(USER_ID);
        service = serviceAt("2026-09-02T17:12:00", attendanceGateway);

        DataConnectAttendance attendance = service.registerExit(USER_ID);

        assertEquals(DataConnectAttendanceService.EXIT_EARLY, attendance.getEstadoSalida());
        assertEquals(18, attendance.getMinutosSalidaAnticipada());
    }

    @Test
    void intentoDeDobleEntradaEsRechazado() {
        FakeDataConnectAttendanceGateway attendanceGateway = new FakeDataConnectAttendanceGateway();
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:00:00", attendanceGateway);
        service.registerEntry(USER_ID);

        DuplicateAttendanceException exception = assertThrows(DuplicateAttendanceException.class, () -> service.registerEntry(USER_ID));

        assertEquals("Ya existe una entrada para este usuario en la fecha actual.", exception.getMessage());
    }

    @Test
    void intentoDeDobleSalidaEsRechazado() {
        FakeDataConnectAttendanceGateway attendanceGateway = new FakeDataConnectAttendanceGateway();
        DataConnectAttendanceService service = serviceAt("2026-09-02T08:00:00", attendanceGateway);
        service.registerEntry(USER_ID);
        service = serviceAt("2026-09-02T17:30:00", attendanceGateway);
        service.registerExit(USER_ID);
        DataConnectAttendanceService exitService = service;

        DuplicateAttendanceException exception = assertThrows(DuplicateAttendanceException.class, () -> exitService.registerExit(USER_ID));

        assertEquals("Ya existe una salida para este usuario en la fecha actual.", exception.getMessage());
    }

    @Test
    void salidaSinEntradaEsRechazada() {
        DataConnectAttendanceService service = serviceAt("2026-09-02T17:30:00", new FakeDataConnectAttendanceGateway());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.registerExit(USER_ID));

        assertEquals("No puede registrar salida sin una entrada previa.", exception.getMessage());
    }

    private DataConnectAttendanceService serviceAt(String dateTime, FakeDataConnectAttendanceGateway attendanceGateway) {
        return new DataConnectAttendanceService(
                attendanceGateway,
                new FakeDataConnectScheduleGateway(),
                new FakeOfficialTimeProvider(LocalDateTime.parse(dateTime))
        );
    }

    private static class FakeDataConnectScheduleGateway implements DataConnectScheduleGateway {
        @Override
        public Optional<DataConnectSchedule> findActiveByUserAndDay(String usuarioId, int diaSemana) {
            return Optional.of(new DataConnectSchedule(
                    "schedule-1",
                    usuarioId,
                    List.of(1, 2, 3, 4, 5),
                    LocalTime.of(8, 0),
                    LocalTime.of(17, 30),
                    true
            ));
        }
    }

    private static class FakeDataConnectAttendanceGateway implements DataConnectAttendanceGateway {
        private final Map<String, DataConnectAttendance> attendances = new HashMap<>();

        @Override
        public DataConnectAttendance createEntry(String usuarioId, LocalDate fecha, String estadoEntrada, int minutosAtraso) {
            DataConnectAttendance attendance = new DataConnectAttendance(
                    usuarioId,
                    fecha,
                    OffsetDateTime.parse(fecha + "T12:00:00Z"),
                    estadoEntrada,
                    minutosAtraso,
                    null,
                    null,
                    null
            );
            attendances.put(key(usuarioId, fecha), attendance);
            return attendance;
        }

        @Override
        public Optional<DataConnectAttendance> findByUserAndDate(String usuarioId, LocalDate fecha) {
            return Optional.ofNullable(attendances.get(key(usuarioId, fecha)));
        }

        @Override
        public DataConnectAttendance markExit(String usuarioId, LocalDate fecha, String estadoSalida, int minutosSalidaAnticipada) {
            DataConnectAttendance existing = attendances.get(key(usuarioId, fecha));
            DataConnectAttendance updated = new DataConnectAttendance(
                    usuarioId,
                    fecha,
                    existing.getHoraEntrada(),
                    existing.getEstadoEntrada(),
                    existing.getMinutosAtraso(),
                    OffsetDateTime.parse(fecha + "T21:00:00Z"),
                    estadoSalida,
                    minutosSalidaAnticipada
            );
            attendances.put(key(usuarioId, fecha), updated);
            return updated;
        }

        @Override
        public List<DataConnectAttendance> findByUserAndDateRange(String usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
            return attendances.values().stream()
                    .filter(attendance -> attendance.getUsuarioId().equals(usuarioId))
                    .filter(attendance -> !attendance.getFecha().isBefore(fechaInicio))
                    .filter(attendance -> !attendance.getFecha().isAfter(fechaFin))
                    .toList();
        }

        @Override
        public List<DataConnectAttendance> findLateArrivals(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
            return attendances.values().stream()
                    .filter(attendance -> DataConnectAttendanceService.ENTRY_LATE.equals(attendance.getEstadoEntrada()))
                    .toList();
        }

        @Override
        public List<DataConnectAttendance> findEarlyDepartures(LocalDate fechaInicio, LocalDate fechaFin, String usuarioId) {
            return attendances.values().stream()
                    .filter(attendance -> DataConnectAttendanceService.EXIT_EARLY.equals(attendance.getEstadoSalida()))
                    .toList();
        }

        private String key(String usuarioId, LocalDate fecha) {
            return usuarioId + ":" + fecha;
        }
    }
}
