package com.asistencia.service;

import com.asistencia.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    @Test
    void loginCorrectoConAdministradorInicial() {
        ServiceTestSupport support = new ServiceTestSupport();

        Optional<Usuario> usuario = support.authService.login("admin@empresa.cl", "Admin123");

        assertTrue(usuario.isPresent());
        assertEquals("Administrador de prueba", usuario.get().getNombre());
    }

    @Test
    void loginIncorrectoRetornaVacio() {
        ServiceTestSupport support = new ServiceTestSupport();

        Optional<Usuario> usuario = support.authService.login("admin@empresa.cl", "clave-mala");

        assertTrue(usuario.isEmpty());
    }
}
