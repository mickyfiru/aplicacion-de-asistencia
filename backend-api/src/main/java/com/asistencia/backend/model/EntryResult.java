package com.asistencia.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record EntryResult(LocalDate fecha, LocalTime horaEntrada, String estado, int minutosAtraso) {
}
