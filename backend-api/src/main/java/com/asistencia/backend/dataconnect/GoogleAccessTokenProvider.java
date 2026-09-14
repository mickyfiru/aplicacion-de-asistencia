package com.asistencia.backend.dataconnect;

import com.asistencia.backend.web.BackendDependencyException;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.List;

public class GoogleAccessTokenProvider implements DataConnectAccessTokenProvider {
    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");

    @Override
    public String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException exception) {
            throw new BackendDependencyException("No se pudo obtener credencial de servidor para Data Connect", exception);
        }
    }
}
