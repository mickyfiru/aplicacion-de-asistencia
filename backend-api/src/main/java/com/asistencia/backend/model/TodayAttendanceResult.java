package com.asistencia.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record TodayAttendanceResult(
        LocalDate fecha,
        LocalTime horaEntrada,
        String estadoEntrada,
        int minutosAtraso,
        LocalTime horaSalida,
        String estadoSalida,
        Integer minutosAnticipada
) {
}
