package com.asistencia.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FirebaseAuthConfig {
    private static final String ENV_API_KEY = "FIREBASE_WEB_API_KEY";
    private static final String FILE_API_KEY = "firebase.web.api.key";
    private static final Path LOCAL_CONFIG = Path.of(System.getProperty("user.home"), ".app-asistencia", "firebase-auth.properties");

    private final String webApiKey;

    public FirebaseAuthConfig() {
        this(loadApiKey());
    }

    public FirebaseAuthConfig(String webApiKey) {
        if (webApiKey == null || webApiKey.isBlank()) {
            throw new IllegalStateException("Debe configurar FIREBASE_WEB_API_KEY o la propiedad firebase.web.api.key en " + LOCAL_CONFIG);
        }
        this.webApiKey = webApiKey.trim();
    }

    public String getWebApiKey() {
        return webApiKey;
    }

    public static boolean isConfigured() {
        return !loadApiKey().isBlank();
    }

    private static String loadApiKey() {
        return loadApiKey(System.getenv(ENV_API_KEY), LOCAL_CONFIG);
    }

    static String loadApiKey(String environmentValue, Path localConfig) {
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        if (!Files.isRegularFile(localConfig)) {
            return "";
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(localConfig)) {
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer " + localConfig, exception);
        }
        return properties.getProperty(FILE_API_KEY, "").trim();
    }
}
