package com.asistencia.service;

import com.asistencia.model.Asistencia;
import com.asistencia.model.Usuario;

public interface WorkerAttendanceOperations {
    Asistencia registrarEntrada(Usuario usuario);

    Asistencia registrarSalida(Usuario usuario);

    String obtenerEstadoActual(Usuario usuario);
}
