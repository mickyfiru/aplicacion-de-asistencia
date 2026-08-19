package com.asistencia.service;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioServiceTest {
    @Test
    void crearUsuarioValido() {
        ServiceTestSupport support = new ServiceTestSupport();

        Usuario usuario = support.usuarioService.crearUsuario("Ana Perez", "ana@empresa.cl", "Clave123", Rol.USUARIO);

        assertTrue(usuario.getId() > 0);
        assertTrue(support.authService.login("ana@empresa.cl", "Clave123").isPresent());
    }

    @Test
    void crearUsuarioRechazaCorreoDuplicado() {
        ServiceTestSupport support = new ServiceTestSupport();
        support.usuarioService.crearUsuario("Ana Perez", "ana@empresa.cl", "Clave123", Rol.USUARIO);

        assertThrows(IllegalArgumentException.class, () ->
                support.usuarioService.crearUsuario("Otra Ana", "ana@empresa.cl", "Clave123", Rol.USUARIO)
        );
    }

    @Test
    void modificarUsuarioActualizaDatos() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Ana Perez", "ana@empresa.cl", "Clave123", Rol.USUARIO);

        support.usuarioService.modificarUsuario(usuario.getId(), "Ana Soto", "ana.soto@empresa.cl", "Nueva123", Rol.ADMINISTRADOR, true);

        Optional<Usuario> actualizado = support.authService.login("ana.soto@empresa.cl", "Nueva123");
        assertTrue(actualizado.isPresent());
        assertEquals("Ana Soto", actualizado.get().getNombre());
        assertEquals(Rol.ADMINISTRADOR, actualizado.get().getRol());
    }

    @Test
    void eliminarUsuarioEsLogicoEImpideLogin() {
        ServiceTestSupport support = new ServiceTestSupport();
        Usuario usuario = support.usuarioService.crearUsuario("Ana Perez", "ana@empresa.cl", "Clave123", Rol.USUARIO);

        support.usuarioService.eliminarUsuario(usuario.getId());

        assertTrue(support.authService.login("ana@empresa.cl", "Clave123").isEmpty());
    }
}
