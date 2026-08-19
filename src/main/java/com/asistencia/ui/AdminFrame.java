package com.asistencia.ui;

import com.asistencia.model.Usuario;
import com.asistencia.service.ReporteService;
import com.asistencia.service.UsuarioService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

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
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(10, 12, 10, 12));
        header.add(new JLabel("Administrador conectado: " + usuario.getNombre()), BorderLayout.WEST);

        JButton logoutButton = new JButton("Cerrar sesion");
        logoutButton.addActionListener(event -> logout());
        header.add(logoutButton, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
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
