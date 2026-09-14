package com.asistencia.backend.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExitResult(LocalDate fecha, LocalTime horaSalida, String estado, int minutosAnticipada) {
}
