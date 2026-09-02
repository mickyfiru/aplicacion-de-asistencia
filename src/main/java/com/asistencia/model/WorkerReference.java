package com.asistencia.model;

import com.asistencia.util.ValidationUtil;

public class WorkerReference {
    private final int workerId;
    private final String name;
    private final String identifier;

    public WorkerReference(int workerId, String name, String identifier) {
        if (workerId <= 0) {
            throw new IllegalArgumentException("El ID del trabajador debe ser valido");
        }
        if (ValidationUtil.isBlank(name)) {
            throw new IllegalArgumentException("El nombre del trabajador no puede estar vacio");
        }
        if (ValidationUtil.isBlank(identifier)) {
            throw new IllegalArgumentException("El identificador o RUT no puede estar vacio");
        }
        this.workerId = workerId;
        this.name = name.trim();
        this.identifier = identifier.trim();
    }

    public int getWorkerId() {
        return workerId;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }
}
