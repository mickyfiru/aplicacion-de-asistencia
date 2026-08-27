package com.asistencia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional; 

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;
import com.asistencia.service.AsistenciaService;
import com.asistencia.service.AuthService; 
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService; 

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
        setResizable(false); 
    }

    private void buildContent() {
        // Panel Principal para dividirlo
        JPanel panelPrincipal = new JPanel(new BorderLayout(30, 0));
        panelPrincipal.setBorder(new EmptyBorder(30, 40, 30, 40));
        panelPrincipal.setBackground(Color.decode("#2C3E50"));

        //Lado Izquierdo | Foto foca
        JLabel lblFoto = new JLabel();
        try {
            java.net.URL imageUrl = getClass().getResource("/images/foca.png");
            if (imageUrl != null) {
                ImageIcon iconoOriginal = new ImageIcon(imageUrl);
                java.awt.Image imagenBase = iconoOriginal.getImage();
                java.awt.Image imagenRedimensionada = imagenBase.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH);
                lblFoto.setIcon(new ImageIcon(imagenRedimensionada));
            } else {    
                System.err.println("No se pudo cargar la imagen: foca.png");
            }   
        } catch (Exception e) {
            System.err.println("Error cargando la imagen");
        }
        panelPrincipal.add(lblFoto, BorderLayout.WEST);

        //Lado Derecho | Formulario
        JPanel panel = new JPanel(new GridBagLayout()); 
        panel.setBackground(Color.decode("#2C3E50"));
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.insets = new Insets(10, 10, 10, 10); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 

        // Título  
        gbc.gridx = 0; 
        gbc.gridy = 0; 
        gbc.gridwidth = 2; // Usamos esto para que ocupe las columnas como si fueran 1 en vez de 2.
        JLabel lbTitulo = new JLabel("Bienvenido", JLabel.CENTER);
        lbTitulo.putClientProperty("FlatLaf.styleClass", "h1");
        lbTitulo.setForeground(Color.WHITE); 
        panel.add(lbTitulo, gbc);
        gbc.gridwidth = 1; // Luego usamos esto para que vuelvan a ser columnas aparte y no afecte al resto de campos.

        // Campo de correo
        gbc.gridx = 0;
        gbc.gridy = 1; // Ajustamos coordenadas de Y
        JLabel lblCorreo = new JLabel("Correo");
        lblCorreo.setForeground(Color.WHITE); 
        panel.add(lblCorreo, gbc); 
        gbc.gridx = 1; 
        correoField.putClientProperty("JTextField.placeholderText", "ejemplo@correo.com");
        panel.add(correoField, gbc); 

        //Campo de contrasena
        gbc.gridx = 0; 
        gbc.gridy = 2; // Ajustamos coordenadas de Y
        JLabel lblContrasena = new JLabel("Contrasena");
        lblContrasena.setForeground(Color.WHITE); 
        panel.add(lblContrasena, gbc); 
        gbc.gridx = 1; 
        passwordField.putClientProperty("JTextField.placeholderText", "********");
        panel.add(passwordField, gbc); 

        //Boton Login
        JButton loginButton = new JButton("Iniciar sesion"); 
        loginButton.addActionListener(event -> login()); 
        loginButton.putClientProperty("JButton.buttonType", "roundRect"); 
        loginButton.setBackground(Color.decode("#E67E22")); //Fondo botón
        loginButton.setForeground(Color.WHITE); // Letras Botón
        getRootPane().setDefaultButton(loginButton); 
        gbc.gridx = 1; 
        gbc.gridy = 3; // Ajustamos coordenadas de Y
        panel.add(loginButton, gbc); 

        //Construccion de ventana
        panelPrincipal.add(panel, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);
        // Ajustamos al tamaño ideal del contenido y centramos en pantalla.
        pack();
        setLocationRelativeTo(null);
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