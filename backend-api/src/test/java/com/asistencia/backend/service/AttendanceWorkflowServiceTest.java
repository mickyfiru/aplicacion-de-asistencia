package com.asistencia.backend.service;

import com.asistencia.backend.dataconnect.AttendanceDataGateway;
import com.asistencia.backend.model.BackendAttendance;
import com.asistencia.backend.model.BackendSchedule;
import com.asistencia.backend.model.BackendUser;
import com.asistencia.backend.model.EntryResult;
import com.asistencia.backend.model.ExitResult;
import com.asistencia.backend.security.AuthenticatedUser;
import com.asistencia.backend.web.ConflictException;
import com.asistencia.backend.web.ForbiddenException;
import com.asistencia.backend.web.NotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceWorkflowServiceTest {
    private static final AuthenticatedUser AUTH_USER = new AuthenticatedUser("firebase-uid-worker", "worker@example.com", Map.of(), false);
    private static final String USER_ID = "user-1";

    @Test
    void entradaATiempoUsaHoraRegistradaPorServidor() {
        FakeGateway gateway = new FakeGateway();
        gateway.entryTimestamp = OffsetDateTime.parse("2026-09-02T08:00:00-04:00");

        EntryResult result = service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER);

        assertEquals(LocalDate.parse("2026-09-02"), result.fecha());
        assertEquals(LocalTime.parse("08:00"), result.horaEntrada());
        assertEquals(AttendanceWorkflowService.ENTRY_ON_TIME, result.estado());
        assertEquals(0, result.minutosAtraso());
    }

    @Test
    void atrasoUsaHoraRegistradaPorServidor() {
        FakeGateway gateway = new FakeGateway();
        gateway.entryTimestamp = OffsetDateTime.parse("2026-09-02T08:12:34-04:00");

        EntryResult result = service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER);

        assertEquals(LocalTime.parse("08:12:34"), result.horaEntrada());
        assertEquals(AttendanceWorkflowService.ENTRY_LATE, result.estado());
        assertEquals(12, result.minutosAtraso());
    }

    @Test
    void dobleEntradaConcurrenteDejaUnaSolaEntrada() throws Exception {
        FakeGateway gateway = new FakeGateway();
        AttendanceWorkflowService service = service(gateway, "2026-09-02T12:00:00Z");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> action = () -> {
                try {
                    service.markEntry(AUTH_USER);
                    return true;
                } catch (ConflictException exception) {
                    return false;
                }
            };
            Future<Boolean> first = executor.submit(action);
            Future<Boolean> second = executor.submit(action);

            int successes = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);

            assertEquals(1, successes);
            assertEquals(1, gateway.attendances.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void salidaNormalUsaHoraRegistradaPorServidor() {
        FakeGateway gateway = new FakeGateway();
        AttendanceWorkflowService service = service(gateway, "2026-09-02T12:00:00Z");
        service.markEntry(AUTH_USER);
        gateway.exitTimestamp = OffsetDateTime.parse("2026-09-02T17:30:00-04:00");

        ExitResult result = service.markExit(AUTH_USER);

        assertEquals(LocalTime.parse("17:30"), result.horaSalida());
        assertEquals(AttendanceWorkflowService.EXIT_NORMAL, result.estado());
        assertEquals(0, result.minutosAnticipada());
    }

    @Test
    void salidaAnticipadaUsaHoraRegistradaPorServidor() {
        FakeGateway gateway = new FakeGateway();
        AttendanceWorkflowService service = service(gateway, "2026-09-02T12:00:00Z");
        service.markEntry(AUTH_USER);
        gateway.exitTimestamp = OffsetDateTime.parse("2026-09-02T17:25:10-04:00");

        ExitResult result = service.markExit(AUTH_USER);

        assertEquals(LocalTime.parse("17:25:10"), result.horaSalida());
        assertEquals(AttendanceWorkflowService.EXIT_EARLY, result.estado());
        assertEquals(4, result.minutosAnticipada());
    }

    @Test
    void salidaSinEntradaEsRechazada() {
        FakeGateway gateway = new FakeGateway();

        ConflictException exception = assertThrows(ConflictException.class, () -> service(gateway, "2026-09-02T12:00:00Z").markExit(AUTH_USER));

        assertEquals("No puede registrar salida sin una entrada previa.", exception.getMessage());
    }

    @Test
    void dobleSalidaEsRechazada() {
        FakeGateway gateway = new FakeGateway();
        AttendanceWorkflowService service = service(gateway, "2026-09-02T12:00:00Z");
        service.markEntry(AUTH_USER);
        service.markExit(AUTH_USER);

        ConflictException exception = assertThrows(ConflictException.class, () -> service.markExit(AUTH_USER));

        assertEquals("SALIDA_YA_REGISTRADA", exception.getErrorCode());
        assertEquals("La salida de hoy ya fue registrada.", exception.getMessage());
    }

    @Test
    void cambioDeHoraDelPcClienteNoAfectaResultados() {
        FakeGateway gateway = new FakeGateway();
        gateway.entryTimestamp = OffsetDateTime.parse("2026-09-02T08:30:00-04:00");
        LocalTime clientFakeTime = LocalTime.parse("07:10");

        EntryResult result = service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER);

        assertEquals(LocalTime.parse("07:10"), clientFakeTime);
        assertEquals(LocalTime.parse("08:30"), result.horaEntrada());
        assertEquals(30, result.minutosAtraso());
    }

    @Test
    void fechaChileSeCalculaDesdeInstantDelBackend() {
        FakeGateway gateway = new FakeGateway();
        gateway.entryTimestamp = OffsetDateTime.parse("2026-09-12T23:30:00-03:00");

        EntryResult result = service(gateway, "2026-09-13T02:30:00Z").markEntry(AUTH_USER);

        assertEquals(LocalDate.parse("2026-09-12"), result.fecha());
    }

    @Test
    void usuarioSinRegistroDataConnectEsRechazado() {
        FakeGateway gateway = new FakeGateway();
        gateway.users.clear();

        assertThrows(NotFoundException.class, () -> service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER));
    }

    @Test
    void usuarioNoPuedeMarcarAsistenciaDeOtroUsuario() {
        FakeGateway gateway = new FakeGateway();

        service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER);

        assertEquals(USER_ID, gateway.lastCreatedUserId);
    }

    @Test
    void trabajadorNoActivoEsRechazado() {
        FakeGateway gateway = new FakeGateway();
        gateway.users.put("firebase-uid-worker", new BackendUser(USER_ID, "firebase-uid-worker", "Ana", "Perez", "TRABAJADOR", false));

        assertThrows(ForbiddenException.class, () -> service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER));
    }

    @Test
    void horarioInexistenteEsRechazado() {
        FakeGateway gateway = new FakeGateway();
        gateway.schedule = Optional.empty();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service(gateway, "2026-09-02T12:00:00Z").markEntry(AUTH_USER));

        assertEquals("No existe horario activo para hoy.", exception.getMessage());
    }

    private AttendanceWorkflowService service(FakeGateway gateway, String instant) {
        return new AttendanceWorkflowService(gateway, Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    private static class FakeGateway implements AttendanceDataGateway {
        private final Map<String, BackendUser> users = new HashMap<>();
        private final Map<String, BackendAttendance> attendances = new HashMap<>();
        private OffsetDateTime entryTimestamp = OffsetDateTime.parse("2026-09-02T08:00:00-04:00");
        private OffsetDateTime exitTimestamp = OffsetDateTime.parse("2026-09-02T17:30:00-04:00");
        private Optional<BackendSchedule> schedule = Optional.of(new BackendSchedule("schedule-1", USER_ID, List.of(1, 2, 3, 4, 5, 6, 7), LocalTime.of(8, 0), LocalTime.of(17, 30)));
        private String lastCreatedUserId;

        FakeGateway() {
            users.put("firebase-uid-worker", new BackendUser(USER_ID, "firebase-uid-worker", "Ana", "Perez", "TRABAJADOR", true));
        }

        @Override
        public Optional<BackendUser> findUserByAuthUid(String authUid) {
            return Optional.ofNullable(users.get(authUid));
        }

        @Override
        public Optional<BackendSchedule> findActiveSchedule(String userId, int chileDayOfWeek) {
            return schedule;
        }

        @Override
        public synchronized Optional<BackendAttendance> findAttendance(String userId, LocalDate businessDate) {
            return Optional.ofNullable(attendances.get(key(userId, businessDate)));
        }

        @Override
        public synchronized BackendAttendance createServerEntry(String userId, LocalDate businessDate) {
            String key = key(userId, businessDate);
            if (attendances.containsKey(key)) {
                throw new ConflictException("Ya existe una entrada registrada para hoy.");
            }
            lastCreatedUserId = userId;
            BackendAttendance attendance = new BackendAttendance(userId, businessDate, entryTimestamp, AttendanceWorkflowService.ENTRY_PENDING, 0, null, null, null);
            attendances.put(key, attendance);
            return attendance;
        }

        @Override
        public synchronized BackendAttendance updateEntryResult(String userId, LocalDate businessDate, String state, int lateMinutes) {
            BackendAttendance current = attendances.get(key(userId, businessDate));
            BackendAttendance updated = new BackendAttendance(userId, businessDate, current.horaEntrada(), state, lateMinutes, current.horaSalida(), current.estadoSalida(), current.minutosSalidaAnticipada());
            attendances.put(key(userId, businessDate), updated);
            return updated;
        }

        @Override
        public synchronized BackendAttendance setServerExit(String userId, LocalDate businessDate) {
            BackendAttendance current = attendances.get(key(userId, businessDate));
            if (current.horaSalida() != null) {
                throw new ConflictException("SALIDA_YA_REGISTRADA", "La salida de hoy ya fue registrada.");
            }
            BackendAttendance updated = new BackendAttendance(userId, businessDate, current.horaEntrada(), current.estadoEntrada(), current.minutosAtraso(), exitTimestamp, null, null);
            attendances.put(key(userId, businessDate), updated);
            return updated;
        }

        @Override
        public synchronized BackendAttendance updateExitResult(String userId, LocalDate businessDate, String state, int earlyMinutes) {
            BackendAttendance current = attendances.get(key(userId, businessDate));
            BackendAttendance updated = new BackendAttendance(userId, businessDate, current.horaEntrada(), current.estadoEntrada(), current.minutosAtraso(), current.horaSalida(), state, earlyMinutes);
            attendances.put(key(userId, businessDate), updated);
            return updated;
        }

        private String key(String userId, LocalDate businessDate) {
            return userId + ":" + businessDate;
        }
    }
}
