package com.asistencia.service;

import com.asistencia.dao.AsistenciaDAO;
import com.asistencia.dao.UsuarioDAO;
import com.asistencia.database.DatabaseConnection;
import com.asistencia.database.DatabaseInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

class ServiceTestSupport {
    final Path databaseFile;
    final UsuarioDAO usuarioDAO;
    final AsistenciaDAO asistenciaDAO;
    final AuthService authService;
    final UsuarioService usuarioService;
    final ReporteService reporteService;

    ServiceTestSupport() {
        try {
            databaseFile = Files.createTempFile("asistencia-test", ".db");
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
        DatabaseConnection databaseConnection = new DatabaseConnection("jdbc:sqlite:" + databaseFile);
        new DatabaseInitializer(databaseConnection).initialize();
        usuarioDAO = new UsuarioDAO(databaseConnection);
        asistenciaDAO = new AsistenciaDAO(databaseConnection);
        authService = new AuthService(usuarioDAO);
        usuarioService = new UsuarioService(usuarioDAO);
        reporteService = new ReporteService(usuarioDAO, asistenciaDAO);
    }

    AsistenciaService asistenciaService(Clock clock) {
        return new AsistenciaService(asistenciaDAO, clock);
    }
}
