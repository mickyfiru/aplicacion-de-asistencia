package com.asistencia.ui;

import com.asistencia.model.Usuario;
import com.asistencia.service.AsistenciaService;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
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
        setSize(900, 560);
        setLocationRelativeTo(null);
    }

    private void buildContent() {
        //Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 12, 10, 12));
        header.setBackground(java.awt.Color.decode("#2C3E50"));
            //Lbl Conectado
        JLabel lblTrabajador = new JLabel("Trabajador Conectado " + usuario.getNombre());
        lblTrabajador.setForeground(java.awt.Color.WHITE);
        header.add(lblTrabajador, BorderLayout.WEST);
            //Boton Cerrar Sesión
        JButton logoutButton = new JButton("Cerrar sesión");
        logoutButton.addActionListener(event -> logout());
        logoutButton.putClientProperty("JButton.buttonType", "roundRect");
        logoutButton.setToolTipText("Cerrar sesión");
        logoutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);

        try {
            java.net.URL baseLogout = getClass().getResource("/images/logout.png");
            if (baseLogout != null) {
                ImageIcon javaLogout = new ImageIcon(baseLogout);
                java.awt.Image editLogout = javaLogout.getImage();

                java.awt.Image scaledLogout = editLogout.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
                logoutButton.setIcon(new ImageIcon(scaledLogout));

            } else {
                System.err.println("No se encontro foto de boton logout.png");
                logoutButton.setText("Salir");
                logoutButton.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            System.err.println("Error cargando logout.png");
        }

        header.add(logoutButton, BorderLayout.EAST);

        //Pestañas
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.decode("#34495E"));
        tabs.setForeground(Color.WHITE);
        tabs.putClientProperty("JTabbedPane.selectedBackground", Color.decode("#4892d7"));
        tabs.putClientProperty("JtabbedPane.showTabSeparators", true);

        //Panel asistencia
        JPanel panelAsistencia = new JPanel(new GridBagLayout());
        panelAsistencia.setBackground(Color.decode("#34495E"));
        
        JPanel innerPanel = new JPanel(new GridLayout(3, 1, 10, 20));
        innerPanel.setOpaque(false);
        
        estadoLabel.setForeground(Color.WHITE);
        estadoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        estadoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        innerPanel.add(estadoLabel);

        //Boton Entrada
        JButton entradaButton = new JButton("Registrar entrada");
        entradaButton.putClientProperty("JButton.buttonType", "roundRect");
        entradaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        entradaButton.addActionListener(event -> registrarEntrada());
        innerPanel.add(entradaButton);

        //Boton Salida
        JButton salidaButton = new JButton("Registrar salida");
        salidaButton.putClientProperty("JButton.buttonType", "roundRect");
        salidaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        salidaButton.addActionListener(event -> registrarSalida());
        innerPanel.add(salidaButton);

        panelAsistencia.add(innerPanel);
        tabs.addTab("Registro de Asistencia", panelAsistencia);
        
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
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
