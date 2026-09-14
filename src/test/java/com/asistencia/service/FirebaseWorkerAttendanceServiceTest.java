package com.asistencia.service;

import com.asistencia.exception.DuplicateAttendanceException;
import com.asistencia.exception.MissingScheduleException;
import com.asistencia.model.Usuario;
import com.asistencia.model.dataconnect.DataConnectAttendance;
import com.asistencia.model.dataconnect.DataConnectSchedule;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectGateway;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebaseWorkerAttendanceServiceTest {
    @Test
    void cambioDeRelojLocalNoAlteraHoraEntradaGuardada() {
        FakeGateway gateway = new FakeGateway();
        FirebaseWorkerAttendanceService service = service(gateway, "2026-09-13T03:00:00Z");

        service.registrarEntrada(usuario());

        assertTrue(gateway.entryCalled);
        assertEquals(OffsetDateTime.parse("2026-09-13T12:12:00Z"), gateway.entryAttendance.getHoraEntrada());
    }

    @Test
    void dobleEntradaManejadaPorCapaClienteConTimestampServidor() {
        FakeGateway gateway = new FakeGateway();
        gateway.latestAttendance = Optional.of(gateway.entryAttendance);
        FirebaseWorkerAttendanceService service = service(gateway, "2026-09-13T16:00:00Z");

        assertThrows(DuplicateAttendanceException.class, () -> service.registrarEntrada(usuario()));
    }

    @Test
    void dobleSalidaPropagaRechazoControlado() {
        FakeGateway gateway = new FakeGateway();
        gateway.exitException = new DuplicateAttendanceException("Ya existe una salida registrada para hoy.");
        FirebaseWorkerAttendanceService service = service(gateway, "2026-09-13T16:00:00Z");

        assertThrows(DuplicateAttendanceException.class, () -> service.registrarSalida(usuario()));
    }

    @Test
    void salidaSinEntradaPropagaRechazoControlado() {
        FakeGateway gateway = new FakeGateway();
        gateway.exitException = new IllegalStateException("No existe una entrada abierta para registrar salida.");
        FirebaseWorkerAttendanceService service = service(gateway, "2026-09-13T16:00:00Z");

        assertThrows(IllegalStateException.class, () -> service.registrarSalida(usuario()));
    }

    @Test
    void horarioInexistenteEsRechazado() {
        FakeGateway gateway = new FakeGateway();
        gateway.schedule = Optional.empty();
        FirebaseWorkerAttendanceService service = service(gateway, "2026-09-13T16:00:00Z");

        assertThrows(MissingScheduleException.class, () -> service.registrarEntrada(usuario()));
    }

    @Test
    void calculaAtrasoVisualConAmericaSantiago() {
        AttendancePresentationCalculator calculator = new AttendancePresentationCalculator();
        DataConnectSchedule schedule = new DataConnectSchedule("schedule-1", "user-1", List.of(1), LocalTime.of(8, 0), LocalTime.of(17, 30), true);
        DataConnectAttendance attendance = new DataConnectAttendance("user-1", null, OffsetDateTime.parse("2026-09-14T11:15:00Z"), "PENDIENTE", 0, null, null, null);

        assertEquals("ATRASO", calculator.entryState(attendance, schedule));
        assertEquals(15, calculator.lateMinutes(attendance, schedule));
    }

    @Test
    void calculaSalidaAnticipadaVisualConAmericaSantiago() {
        AttendancePresentationCalculator calculator = new AttendancePresentationCalculator();
        DataConnectSchedule schedule = new DataConnectSchedule("schedule-1", "user-1", List.of(1), LocalTime.of(8, 0), LocalTime.of(17, 30), true);
        DataConnectAttendance attendance = new DataConnectAttendance("user-1", null, OffsetDateTime.parse("2026-09-14T11:00:00Z"), "PENDIENTE", 0, OffsetDateTime.parse("2026-09-14T20:10:00Z"), null, null);

        assertEquals("ANTICIPADA", calculator.exitState(attendance, schedule));
        assertEquals(20, calculator.earlyExitMinutes(attendance, schedule));
    }

    private FirebaseWorkerAttendanceService service(FakeGateway gateway, String instant) {
        return new FirebaseWorkerAttendanceService(
                gateway,
                new AttendancePresentationCalculator(),
                Clock.fixed(Instant.parse(instant), ZoneId.of("America/Santiago"))
        );
    }

    private Usuario usuario() {
        return new Usuario(null, "Ana", "ana@example.com", "", com.asistencia.model.Rol.USUARIO, true);
    }

    private static class FakeGateway implements CurrentUserDataConnectGateway {
        private final DataConnectUser user = new DataConnectUser("user-1", "Ana", "Perez", "11.111.111-1", "ana@example.com", "uid", null, "TRABAJADOR", true);
        private final DataConnectAttendance entryAttendance = new DataConnectAttendance("user-1", null, OffsetDateTime.parse("2026-09-13T12:12:00Z"), "PENDIENTE", 0, null, null, null);
        private final DataConnectAttendance exitAttendance = new DataConnectAttendance("user-1", null, OffsetDateTime.parse("2026-09-13T12:12:00Z"), "PENDIENTE", 0, OffsetDateTime.parse("2026-09-13T21:30:00Z"), null, null);
        private Optional<DataConnectSchedule> schedule = Optional.of(new DataConnectSchedule("schedule-1", "user-1", List.of(1, 2, 3, 4, 5, 6, 7), LocalTime.of(8, 0), LocalTime.of(17, 30), true));
        private Optional<DataConnectAttendance> latestAttendance = Optional.empty();
        private RuntimeException exitException;
        private boolean entryCalled;

        @Override
        public Optional<DataConnectUser> findCurrentUser() {
            return Optional.of(user);
        }

        @Override
        public Optional<DataConnectSchedule> findCurrentSchedule(int diaSemana) {
            return schedule;
        }

        @Override
        public Optional<DataConnectAttendance> findOpenAttendance() {
            return Optional.empty();
        }

        @Override
        public Optional<DataConnectAttendance> findLatestAttendance() {
            return latestAttendance;
        }

        @Override
        public List<DataConnectAttendance> findMyAttendances(OffsetDateTime desde, OffsetDateTime hasta) {
            return List.of();
        }

        @Override
        public DataConnectAttendance markMyEntry() {
            entryCalled = true;
            latestAttendance = Optional.of(entryAttendance);
            return entryAttendance;
        }

        @Override
        public DataConnectAttendance markMyExit() {
            if (exitException != null) {
                throw exitException;
            }
            latestAttendance = Optional.of(exitAttendance);
            return exitAttendance;
        }
    }
}
