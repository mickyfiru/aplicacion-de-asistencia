package com.asistencia.ui;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.service.AsistenciaService;
import com.asistencia.service.AuthService;
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final AsistenciaService asistenciaService;
    private final ReporteService reporteService;
    private final JTextField correoField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    public LoginFrame(
            AuthService authService,
            UsuarioService usuarioService,
            AsistenciaService asistenciaService,
            ReporteService reporteService
    ) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.asistenciaService = asistenciaService;
        this.reporteService = reporteService;
        configureFrame();
        buildContent();
    }

    private void configureFrame() {
        setTitle("Sistema de Control de Asistencia - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(430, 230);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void buildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Correo"), gbc);
        gbc.gridx = 1;
        panel.add(correoField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Contrasena"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Iniciar sesion");
        loginButton.addActionListener(event -> login());
        getRootPane().setDefaultButton(loginButton);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void login() {
        String correo = correoField.getText();
        String password = new String(passwordField.getPassword());
        Optional<Usuario> usuario = authService.login(correo, password);
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame nextFrame = usuario.get().getRol() == Rol.ADMINISTRADOR
                ? new AdminFrame(usuario.get(), usuarioService, reporteService, this)
                : new WorkerFrame(usuario.get(), asistenciaService, this);
        clearFields();
        setVisible(false);
        nextFrame.setVisible(true);
    }

    private void clearFields() {
        correoField.setText("");
        passwordField.setText("");
    }
}
