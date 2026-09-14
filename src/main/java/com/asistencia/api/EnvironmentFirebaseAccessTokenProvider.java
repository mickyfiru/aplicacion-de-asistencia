package com.asistencia.api;

public class EnvironmentFirebaseAccessTokenProvider implements FirebaseAccessTokenProvider {
    private final String environmentVariableName;

    public EnvironmentFirebaseAccessTokenProvider() {
        this("FIREBASE_ID_TOKEN");
    }

    public EnvironmentFirebaseAccessTokenProvider(String environmentVariableName) {
        this.environmentVariableName = environmentVariableName;
    }

    @Override
    public String getAccessToken() {
        String token = System.getenv(environmentVariableName);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Debe configurar " + environmentVariableName + " para llamar a Firebase Data Connect");
        }
        return token.trim();
    }
}
