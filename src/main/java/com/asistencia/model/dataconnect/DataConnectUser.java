package com.asistencia.model.dataconnect;

public class DataConnectUser {
    private final String id;
    private final String nombre;
    private final String apellido;
    private final String rut;
    private final String correo;
    private final String authUid;
    private final String passwordHash;
    private final String rol;
    private final boolean activo;

    public DataConnectUser(
            String id,
            String nombre,
            String apellido,
            String rut,
            String correo,
            String authUid,
            String passwordHash,
            String rol,
            boolean activo
    ) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.correo = correo;
        this.authUid = authUid;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getRut() {
        return rut;
    }

    public String getCorreo() {
        return correo;
    }

    public String getAuthUid() {
        return authUid;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
