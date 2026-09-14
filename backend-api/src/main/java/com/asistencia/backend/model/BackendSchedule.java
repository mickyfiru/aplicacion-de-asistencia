package com.asistencia.backend.model;

import java.time.LocalTime;
import java.util.List;

public record BackendSchedule(String id, String userId, List<Integer> diasSemana, LocalTime horaEntrada, LocalTime horaSalida) {
}
