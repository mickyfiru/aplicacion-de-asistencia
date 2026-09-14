package com.asistencia.auth;

import com.asistencia.api.FirebaseAccessTokenProvider;

public class FirebaseIdTokenProvider implements FirebaseAccessTokenProvider {
    private final AuthSessionManager sessionManager;

    public FirebaseIdTokenProvider(AuthSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public String getAccessToken() {
        return sessionManager.requireValidSession().getIdToken();
    }
}
