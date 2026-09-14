package com.asistencia.backend.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record BackendAttendance(
        String userId,
        LocalDate fecha,
        OffsetDateTime horaEntrada,
        String estadoEntrada,
        int minutosAtraso,
        OffsetDateTime horaSalida,
        String estadoSalida,
        Integer minutosSalidaAnticipada
) {
}
