package com.asistencia.service;

import com.asistencia.model.AttendanceCorrectionRequest;
import com.asistencia.model.AttendanceRecord;
import com.asistencia.model.AttendanceStatus;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.model.WorkerReference;
import com.asistencia.repository.InMemoryAttendanceRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceCorrectionTest {
    @Test
    void soloAdministradorPuedeCorregirRegistrosYSeAuditaCambio() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:30:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        WorkerReference worker = new WorkerReference(20, "Luis Soto", "11.111.111-1");
        AttendanceRecord record = service.registerEntry(worker);
        Usuario administrator = new Usuario(1, "Admin", "admin@empresa.cl", "hash", Rol.ADMINISTRADOR, true);

        AttendanceCorrectionRequest request = new AttendanceCorrectionRequest(
                record.getRecordId(),
                LocalTime.of(8, 0),
                null,
                "Correccion autorizada por RRHH"
        );
        AttendanceRecord corrected = service.correctRecord(request, administrator);

        assertEquals(AttendanceStatus.A_TIEMPO, corrected.getAttendanceStatus());
        assertEquals(0, corrected.getLateMinutes());
        assertEquals(1, repository.findAuditLogsByRecord(record.getRecordId()).size());
    }

    @Test
    void trabajadorNoPuedeCorregirSusRegistros() {
        InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
        FakeOfficialTimeProvider timeProvider = new FakeOfficialTimeProvider(LocalDateTime.parse("2026-09-02T08:30:00"));
        AttendanceService service = new AttendanceService(repository, timeProvider);
        WorkerReference worker = new WorkerReference(20, "Luis Soto", "11.111.111-1");
        AttendanceRecord record = service.registerEntry(worker);
        Usuario normalUser = new Usuario(20, "Luis Soto", "luis@empresa.cl", "hash", Rol.USUARIO, true);
        AttendanceCorrectionRequest request = new AttendanceCorrectionRequest(
                record.getRecordId(),
                LocalTime.of(8, 0),
                null,
                "Intento no autorizado"
        );

        assertThrows(SecurityException.class, () -> service.correctRecord(request, normalUser));
    }
}
