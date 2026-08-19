package com.asistencia.ui;

import com.asistencia.model.ReporteAsistencia;
import com.asistencia.service.ReporteService;
import com.asistencia.util.DateTimeUtil;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

public class AttendanceReportPanel extends JPanel {
    public enum ReportType {
        ATRASOS,
        SALIDAS_ANTICIPADAS
    }

    private final ReporteService reporteService;
    private final ReportType reportType;
    private final JTextField fechaField = new JTextField(10);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Usuario", "Fecha", "Hora", "Observacion"},
            0
    );

    public AttendanceReportPanel(ReporteService reporteService, ReportType reportType) {
        this.reporteService = reporteService;
        this.reportType = reportType;
        buildContent();
        loadReport();
    }

    private void buildContent() {
        setLayout(new BorderLayout(10, 10));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechaField.setToolTipText("Formato yyyy-MM-dd. Dejar vacio para ver todos los registros.");
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(event -> loadReport());
        JButton todayButton = new JButton("Hoy");
        todayButton.addActionListener(event -> {
            fechaField.setText(LocalDate.now().toString());
            loadReport();
        });
        JButton clearButton = new JButton("Limpiar fecha");
        clearButton.addActionListener(event -> {
            fechaField.setText("");
            loadReport();
        });

        filters.add(new javax.swing.JLabel("Fecha"));
        filters.add(fechaField);
        filters.add(searchButton);
        filters.add(todayButton);
        filters.add(clearButton);
        add(filters, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadReport() {
        try {
            LocalDate fecha = DateTimeUtil.parseOptionalDate(fechaField.getText()).orElse(null);
            List<ReporteAsistencia> registros = reportType == ReportType.ATRASOS
                    ? reporteService.obtenerAtrasos(fecha)
                    : reporteService.obtenerSalidasAnticipadas(fecha);
            fillTable(registros);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<ReporteAsistencia> registros) {
        tableModel.setRowCount(0);
        for (ReporteAsistencia registro : registros) {
            tableModel.addRow(new Object[]{
                    registro.getUsuario(),
                    registro.getFecha(),
                    registro.getHora(),
                    registro.getObservacion()
            });
        }
    }
}
