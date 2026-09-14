package com.asistencia.backend.web;

import java.time.LocalTime;
import java.util.List;

public record ScheduleResponse(List<Integer> diasSemana, LocalTime horaEntrada, LocalTime horaSalida) {
}
