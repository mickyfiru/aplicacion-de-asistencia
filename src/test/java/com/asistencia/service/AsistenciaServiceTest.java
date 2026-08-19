package com.asistencia.service;

import com.asistencia.model.Rol;
import com.asistencia.model.TipoAsistencia;
import com.asistencia.model.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsistenciaServiceTest {
    private static final ZoneId ZONE = ZoneId.of("UTC");

    @Test
    void registrarEntrada() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Luis Diaz", "luis@empresa.cl", "Clave123", Rol.USUARIO);
        AsistenciaService service = support.asistenciaService(clockAt("2026-08-17T09:00:00Z"));

        assertEquals(TipoAsistencia.ENTRADA, service.registrarEntrada(usuario).getTipo());
        assertEquals("Entrada registrada", service.obtenerEstadoActual(usuario));
    }

    @Test
    void registrarSalida() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Luis Diaz", "luis@empresa.cl", "Clave123", Rol.USUARIO);
        support.asistenciaService(clockAt("2026-08-17T09:00:00Z")).registrarEntrada(usuario);
        AsistenciaService salidaService = support.asistenciaService(clockAt("2026-08-17T17:40:00Z"));

        assertEquals(TipoAsistencia.SALIDA, salidaService.registrarSalida(usuario).getTipo());
        assertEquals("Salida registrada", salidaService.obtenerEstadoActual(usuario));
    }

    @Test
    void salidaSinEntradaEsInvalida() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Luis Diaz", "luis@empresa.cl", "Clave123", Rol.USUARIO);
        AsistenciaService service = support.asistenciaService(clockAt("2026-08-17T17:40:00Z"));

        assertThrows(IllegalStateException.class, () -> service.registrarSalida(usuario));
    }

    @Test
    void entradaDuplicadaEsInvalida() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Luis Diaz", "luis@empresa.cl", "Clave123", Rol.USUARIO);
        AsistenciaService service = support.asistenciaService(clockAt("2026-08-17T09:00:00Z"));
        service.registrarEntrada(usuario);

        assertThrows(IllegalStateException.class, () -> service.registrarEntrada(usuario));
    }

    private Clock clockAt(String instant) {
        return Clock.fixed(Instant.parse(instant), ZONE);
    }
}
