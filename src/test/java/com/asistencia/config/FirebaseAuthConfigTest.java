package com.asistencia.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebaseAuthConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void variableDeEntornoTienePrioridadSobreArchivoLocal() throws Exception {
        Path config = tempDir.resolve("firebase-auth.properties");
        Files.writeString(config, "firebase.web.api.key=archivo-key\n");

        String value = FirebaseAuthConfig.loadApiKey("env-key", config);

        assertEquals("env-key", value);
    }

    @Test
    void leeApiKeyDesdePropiedadLocal() throws Exception {
        Path config = tempDir.resolve("firebase-auth.properties");
        Files.writeString(config, "firebase.web.api.key=archivo-key\n");

        String value = FirebaseAuthConfig.loadApiKey(null, config);

        assertEquals("archivo-key", value);
    }

    @Test
    void archivoAusenteSeConsideraNoConfigurado() {
        String value = FirebaseAuthConfig.loadApiKey(null, tempDir.resolve("firebase-auth.properties"));

        assertTrue(value.isBlank());
    }

    @Test
    void propiedadVaciaSeConsideraNoConfigurada() throws Exception {
        Path config = tempDir.resolve("firebase-auth.properties");
        Files.writeString(config, "firebase.web.api.key=\n");

        String value = FirebaseAuthConfig.loadApiKey(null, config);

        assertTrue(value.isBlank());
    }
}
