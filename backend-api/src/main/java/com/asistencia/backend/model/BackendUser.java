package com.asistencia.backend.model;

public record BackendUser(String id, String authUid, String nombre, String apellido, String rol, boolean activo) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(rol) || "ADMINISTRADOR".equalsIgnoreCase(rol);
    }
}
