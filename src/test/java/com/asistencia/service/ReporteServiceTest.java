package com.asistencia.service;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReporteServiceTest {
    private static final ZoneId ZONE = ZoneId.of("UTC");

    @Test
    void detectaAtraso() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Marta Rojas", "marta@empresa.cl", "Clave123", Rol.USUARIO);
        support.asistenciaService(clockAt("2026-08-17T09:31:00Z")).registrarEntrada(usuario);

        assertEquals(1, support.reporteService.obtenerAtrasos(LocalDate.parse("2026-08-17")).size());
    }

    @Test
    void detectaSalidaAnticipada() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Marta Rojas", "marta@empresa.cl", "Clave123", Rol.USUARIO);
        support.asistenciaService(clockAt("2026-08-17T09:00:00Z")).registrarEntrada(usuario);
        support.asistenciaService(clockAt("2026-08-17T17:00:00Z")).registrarSalida(usuario);

        assertEquals(1, support.reporteService.obtenerSalidasAnticipadas(LocalDate.parse("2026-08-17")).size());
    }

    @Test
    void detectaInasistencia() {
        ServiceTestSupport support = new ServiceTestSupport();
        support.usuarioService.crearUsuario("Marta Rojas", "marta@empresa.cl", "Clave123", Rol.USUARIO);

        assertEquals(1, support.reporteService.obtenerInasistencias(LocalDate.parse("2026-08-17")).size());
    }

    private Clock clockAt(String instant) {
        return Clock.fixed(Instant.parse(instant), ZONE);
    }
}
