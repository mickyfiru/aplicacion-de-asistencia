package com.asistencia.service;

import com.asistencia.auth.AuthSessionManager;
import com.asistencia.auth.FirebaseAuthRestClient;
import com.asistencia.auth.FirebaseAuthSession;
import com.asistencia.exception.AuthenticatedUserNotRegisteredException;
import com.asistencia.exception.InvalidCredentialsException;
import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.model.dataconnect.DataConnectUser;
import com.asistencia.repository.dataconnect.CurrentUserDataConnectGateway;
import com.asistencia.util.ValidationUtil;

import java.util.Optional;

public class FirebaseLoginService implements LoginService {
    private static final String USER_NOT_REGISTERED = "Tu cuenta está autenticada, pero no está registrada en el sistema de asistencia.";

    private final FirebaseAuthRestClient authClient;
    private final AuthSessionManager sessionManager;
    private final CurrentUserDataConnectGateway currentUserGateway;

    public FirebaseLoginService(
            FirebaseAuthRestClient authClient,
            AuthSessionManager sessionManager,
            CurrentUserDataConnectGateway currentUserGateway
    ) {
        this.authClient = authClient;
        this.sessionManager = sessionManager;
        this.currentUserGateway = currentUserGateway;
    }

    @Override
    public Optional<Usuario> login(String correo, String password) {
        if (ValidationUtil.isBlank(correo) || ValidationUtil.isBlank(password)) {
            return Optional.empty();
        }
        try {
            FirebaseAuthSession session = authClient.signInWithPassword(correo, password);
            sessionManager.start(session);
            DataConnectUser user = currentUserGateway.findCurrentUser()
                    .orElseThrow(() -> new AuthenticatedUserNotRegisteredException(USER_NOT_REGISTERED));
            if (!user.isActivo()) {
                throw new AuthenticatedUserNotRegisteredException("Tu usuario existe, pero no está activo en el sistema de asistencia.");
            }
            return Optional.of(toSwingUser(user));
        } catch (InvalidCredentialsException exception) {
            sessionManager.clear();
            return Optional.empty();
        } catch (RuntimeException exception) {
            sessionManager.clear();
            throw exception;
        }
    }

    @Override
    public void logout() {
        sessionManager.clear();
    }

    private Usuario toSwingUser(DataConnectUser user) {
        return new Usuario(null, user.getNombre(), user.getCorreo(), "", role(user.getRol()), user.isActivo());
    }

    private Rol role(String rol) {
        return "ADMIN".equalsIgnoreCase(rol) || "ADMINISTRADOR".equalsIgnoreCase(rol)
                ? Rol.ADMINISTRADOR
                : Rol.USUARIO;
    }
}
