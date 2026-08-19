package com.asistencia.ui;

import com.asistencia.model.ReporteInasistencia;
import com.asistencia.service.ReporteService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AbsenceReportPanel extends JPanel {
    private final ReporteService reporteService;
    private final JTextField fechaField = new JTextField(LocalDate.now().toString(), 10);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Usuario", "Fecha", "Observacion"},
            0
    );

    public AbsenceReportPanel(ReporteService reporteService) {
        this.reporteService = reporteService;
        buildContent();
        loadReport();
    }

    private void buildContent() {
        setLayout(new BorderLayout(10, 10));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Buscar");
        searchButton.addActionListener(event -> loadReport());
        JButton todayButton = new JButton("Hoy");
        todayButton.addActionListener(event -> {
            fechaField.setText(LocalDate.now().toString());
            loadReport();
        });
        filters.add(new JLabel("Fecha"));
        filters.add(fechaField);
        filters.add(searchButton);
        filters.add(todayButton);
        add(filters, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadReport() {
        try {
            LocalDate fecha = LocalDate.parse(fechaField.getText().trim());
            List<ReporteInasistencia> registros = reporteService.obtenerInasistencias(fecha);
            tableModel.setRowCount(0);
            for (ReporteInasistencia registro : registros) {
                tableModel.addRow(new Object[]{
                        registro.getUsuario(),
                        registro.getFecha(),
                        registro.getObservacion()
                });
            }
        } catch (DateTimeParseException exception) {
            JOptionPane.showMessageDialog(this, "La fecha debe tener formato yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
