package com.asistencia.service;

import com.asistencia.dao.AsistenciaDAO;
import com.asistencia.model.Asistencia;
import com.asistencia.model.TipoAsistencia;
import com.asistencia.model.Usuario;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class AsistenciaService {
    private final AsistenciaDAO asistenciaDAO;
    private final Clock clock;

    public AsistenciaService(AsistenciaDAO asistenciaDAO) {
        this(asistenciaDAO, Clock.systemDefaultZone());
    }

    public AsistenciaService(AsistenciaDAO asistenciaDAO, Clock clock) {
        this.asistenciaDAO = asistenciaDAO;
        this.clock = clock;
    }

    public Asistencia registrarEntrada(Usuario usuario) {
        validarUsuarioAutenticado(usuario);
        LocalDate fecha = LocalDate.now(clock);
        LocalTime hora = LocalTime.now(clock).withNano(0);

        if (asistenciaDAO.existeRegistro(usuario.getId(), fecha, TipoAsistencia.ENTRADA)) {
            throw new IllegalStateException("Ya existe una entrada registrada para hoy");
        }
        if (asistenciaDAO.existeRegistro(usuario.getId(), fecha, TipoAsistencia.SALIDA)) {
            throw new IllegalStateException("El turno de hoy ya fue cerrado");
        }

        return asistenciaDAO.crear(new Asistencia(usuario.getId(), TipoAsistencia.ENTRADA, fecha, hora));
    }

    public Asistencia registrarSalida(Usuario usuario) {
        validarUsuarioAutenticado(usuario);
        LocalDate fecha = LocalDate.now(clock);
        LocalTime hora = LocalTime.now(clock).withNano(0);

        if (!asistenciaDAO.existeRegistro(usuario.getId(), fecha, TipoAsistencia.ENTRADA)) {
            throw new IllegalStateException("No puede registrar salida sin una entrada previa");
        }
        if (asistenciaDAO.existeRegistro(usuario.getId(), fecha, TipoAsistencia.SALIDA)) {
            throw new IllegalStateException("Ya existe una salida registrada para hoy");
        }

        return asistenciaDAO.crear(new Asistencia(usuario.getId(), TipoAsistencia.SALIDA, fecha, hora));
    }

    public String obtenerEstadoActual(Usuario usuario) {
        validarUsuarioAutenticado(usuario);
        LocalDate fecha = LocalDate.now(clock);
        Optional<Asistencia> ultima = asistenciaDAO.buscarUltimaPorUsuarioYFecha(usuario.getId(), fecha);
        if (ultima.isEmpty()) {
            return "Sin registro";
        }
        return ultima.get().getTipo() == TipoAsistencia.ENTRADA ? "Entrada registrada" : "Salida registrada";
    }

    private void validarUsuarioAutenticado(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || !usuario.isActivo()) {
            throw new IllegalArgumentException("Debe iniciar sesion con un usuario activo");
        }
    }
}
