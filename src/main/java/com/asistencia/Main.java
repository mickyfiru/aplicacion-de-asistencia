package com.asistencia;

import javax.swing.SwingUtilities;

import com.asistencia.dao.AsistenciaDAO;
import com.asistencia.dao.UsuarioDAO;
import com.asistencia.database.DatabaseConnection;
import com.asistencia.database.DatabaseInitializer;
import com.asistencia.service.AsistenciaService;
import com.asistencia.service.AuthService;
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService;
import com.asistencia.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
        initializer.initialize();

        UsuarioDAO usuarioDAO = new UsuarioDAO(databaseConnection);
        AsistenciaDAO asistenciaDAO = new AsistenciaDAO(databaseConnection);

        AuthService authService = new AuthService(usuarioDAO);
        UsuarioService usuarioService = new UsuarioService(usuarioDAO);
        AsistenciaService asistenciaService = new AsistenciaService(asistenciaDAO);
        ReporteService reporteService = new ReporteService(usuarioDAO, asistenciaDAO);

        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
            } catch (Exception ignored) {
                // Swing can continue with its default look and feel.
            }
            new LoginFrame(authService, usuarioService, asistenciaService, reporteService).setVisible(true);
        });
    }
}
