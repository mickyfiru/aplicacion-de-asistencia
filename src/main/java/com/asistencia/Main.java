package com.asistencia;

import javax.swing.SwingUtilities;

import com.asistencia.api.DataConnectGraphQlClient;
import com.asistencia.auth.AuthSessionManager;
import com.asistencia.auth.FirebaseAuthRestClient;
import com.asistencia.auth.FirebaseIdTokenProvider;
import com.asistencia.config.FirebaseAuthConfig;
import com.asistencia.config.FirebaseDataConnectConfig;
import com.asistencia.dao.AsistenciaDAO;
import com.asistencia.dao.UsuarioDAO;
import com.asistencia.database.DatabaseConnection;
import com.asistencia.database.DatabaseInitializer;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectGateway;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectRepository;
import com.asistencia.service.AsistenciaService;
import com.asistencia.service.AuthService;
import com.asistencia.service.FirebaseLoginService;
import com.asistencia.service.FirebaseWorkerAttendanceService;
import com.asistencia.service.LoginService;
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService;
import com.asistencia.service.WorkerAttendanceOperations;
import com.asistencia.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        DatabaseInitializer initializer = new DatabaseInitializer(databaseConnection);
        initializer.initialize();

        UsuarioDAO usuarioDAO = new UsuarioDAO(databaseConnection);
        AsistenciaDAO asistenciaDAO = new AsistenciaDAO(databaseConnection);

        AuthService sqliteAuthService = new AuthService(usuarioDAO);
        UsuarioService usuarioService = new UsuarioService(usuarioDAO);
        AsistenciaService sqliteAsistenciaService = new AsistenciaService(asistenciaDAO);
        ReporteService reporteService = new ReporteService(usuarioDAO, asistenciaDAO);
        LoginService loginService = sqliteAuthService;
        WorkerAttendanceOperations asistenciaService = sqliteAsistenciaService;

        if (FirebaseAuthConfig.isConfigured()) {
            FirebaseAuthConfig firebaseAuthConfig = new FirebaseAuthConfig();
            FirebaseAuthRestClient firebaseAuthClient = new FirebaseAuthRestClient(firebaseAuthConfig);
            AuthSessionManager sessionManager = new AuthSessionManager(firebaseAuthClient);
            DataConnectGraphQlClient dataConnectClient = new DataConnectGraphQlClient(
                    new FirebaseDataConnectConfig(),
                    new FirebaseIdTokenProvider(sessionManager),
                    firebaseAuthConfig
            );
            CurrentUserDataConnectGateway currentUserGateway = new CurrentUserDataConnectRepository(dataConnectClient);
            loginService = new FirebaseLoginService(firebaseAuthClient, sessionManager, currentUserGateway);
            asistenciaService = new FirebaseWorkerAttendanceService(currentUserGateway);
        }

        LoginService selectedLoginService = loginService;
        WorkerAttendanceOperations selectedAsistenciaService = asistenciaService;

        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
            } catch (Exception ignored) {
                // Swing can continue with its default look and feel.
            }
            new LoginFrame(selectedLoginService, usuarioService, selectedAsistenciaService, reporteService).setVisible(true);
        });
    }
}
