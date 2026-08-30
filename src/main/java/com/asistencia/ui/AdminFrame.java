package com.asistencia.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.asistencia.model.Usuario; 
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService; 

public class AdminFrame extends JFrame {
    private final JFrame loginFrame;

    public AdminFrame(Usuario usuario, UsuarioService usuarioService, ReporteService reporteService, JFrame loginFrame) {
        this.loginFrame = loginFrame;
        configureFrame();
        buildContent(usuario, usuarioService, reporteService);
    }

    private void configureFrame() {
        setTitle("Panel principal del administrador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
    }

    private void buildContent(Usuario usuario, UsuarioService usuarioService, ReporteService reporteService) {
        //Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 12, 10, 12));
        header.setBackground(Color.decode("#2C3E50"));

        //Label Admin conectado
        JLabel lblAdmin = new JLabel("Administrador conectado: " + usuario.getNombre());
        lblAdmin.setForeground(Color.WHITE);
        header.add(lblAdmin, BorderLayout.WEST);

        //Botón de cerrar sesión
        JButton logoutButton = new JButton();
        logoutButton.addActionListener(event -> logout());
        logoutButton.putClientProperty("JButton.buttonType", "roundRect");
        logoutButton.setToolTipText("Cerrar sesión");
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        tabs.putClientProperty("JTabbedPane.showTabSeparators", true);

        tabs.addTab("Gestion de usuarios", new UserManagementPanel(usuarioService));
        tabs.addTab("Reporte de atrasos", new AttendanceReportPanel(reporteService, AttendanceReportPanel.ReportType.ATRASOS));
        tabs.addTab("Salidas anticipadas", new AttendanceReportPanel(reporteService, AttendanceReportPanel.ReportType.SALIDAS_ANTICIPADAS));
        tabs.addTab("Inasistencias", new AbsenceReportPanel(reporteService));

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        dispose();
        loginFrame.setVisible(true);
    }
}
