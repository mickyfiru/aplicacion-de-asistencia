package com.asistencia.ui;

import com.asistencia.model.Usuario;
import com.asistencia.service.AsistenciaService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class WorkerFrame extends JFrame {
    private final Usuario usuario;
    private final AsistenciaService asistenciaService;
    private final JFrame loginFrame;
    private final JLabel estadoLabel = new JLabel();

    public WorkerFrame(Usuario usuario, AsistenciaService asistenciaService, JFrame loginFrame) {
        this.usuario = usuario;
        this.asistenciaService = asistenciaService;
        this.loginFrame = loginFrame;
        configureFrame();
        buildContent();
        refreshState();
    }

    private void configureFrame() {
        setTitle("Panel del trabajador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildContent() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        panel.add(new JLabel("Usuario conectado: " + usuario.getNombre()));
        panel.add(estadoLabel);

        JButton entradaButton = new JButton("Registrar entrada");
        entradaButton.addActionListener(event -> registrarEntrada());
        panel.add(entradaButton);

        JButton salidaButton = new JButton("Registrar salida");
        salidaButton.addActionListener(event -> registrarSalida());
        panel.add(salidaButton);

        JButton logoutButton = new JButton("Cerrar sesion");
        logoutButton.addActionListener(event -> logout());
        panel.add(logoutButton);

        add(panel, BorderLayout.CENTER);
    }

    private void registrarEntrada() {
        try {
            asistenciaService.registrarEntrada(usuario);
            JOptionPane.showMessageDialog(this, "Entrada registrada correctamente");
            refreshState();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void registrarSalida() {
        try {
            asistenciaService.registrarSalida(usuario);
            JOptionPane.showMessageDialog(this, "Salida registrada correctamente");
            refreshState();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshState() {
        estadoLabel.setText("Estado actual: " + asistenciaService.obtenerEstadoActual(usuario));
    }

    private void logout() {
        dispose();
        loginFrame.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
