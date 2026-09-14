package com.asistencia.auth;

import java.util.Optional;

public class AuthSessionManager {
    private static final long REFRESH_SKEW_SECONDS = 60;

    private final FirebaseAuthRestClient authClient;
    private FirebaseAuthSession session;

    public AuthSessionManager(FirebaseAuthRestClient authClient) {
        this.authClient = authClient;
    }

    public synchronized void start(FirebaseAuthSession session) {
        this.session = session;
    }

    public synchronized Optional<FirebaseAuthSession> current() {
        return Optional.ofNullable(session);
    }

    public synchronized FirebaseAuthSession requireValidSession() {
        if (session == null) {
            throw new IllegalStateException("No hay una sesion Firebase activa");
        }
        if (session.expiresWithinSeconds(REFRESH_SKEW_SECONDS)) {
            session = authClient.refresh(session.getRefreshToken());
        }
        return session;
    }

    public synchronized void clear() {
        session = null;
    }
}
