package com.asistencia.model.dataconnect;

import com.asistencia.util.ValidationUtil;

public class CreateDataConnectUserRequest {
    private final String nombre;
    private final String apellido;
    private final String rut;
    private final String correo;
    private final String authUid;
    private final String password;
    private final String rol;

    public CreateDataConnectUserRequest(String nombre, String apellido, String rut, String correo, String authUid, String password, String rol) {
        if (ValidationUtil.isBlank(nombre)) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (ValidationUtil.isBlank(apellido)) {
            throw new IllegalArgumentException("El apellido no puede estar vacio");
        }
        if (ValidationUtil.isBlank(rut)) {
            throw new IllegalArgumentException("El RUT no puede estar vacio");
        }
        if (!ValidationUtil.isValidEmail(correo)) {
            throw new IllegalArgumentException("El correo no tiene un formato valido");
        }
        if (ValidationUtil.isBlank(authUid)) {
            throw new IllegalArgumentException("El UID de Firebase Auth no puede estar vacio");
        }
        if (ValidationUtil.isBlank(password)) {
            throw new IllegalArgumentException("La contrasena no puede estar vacia");
        }
        if (ValidationUtil.isBlank(rol)) {
            throw new IllegalArgumentException("El rol no puede estar vacio");
        }
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.rut = rut.trim();
        this.correo = correo.trim().toLowerCase();
        this.authUid = authUid.trim();
        this.password = password;
        this.rol = rol.trim();
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

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }
}
