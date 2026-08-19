package com.asistencia.ui;

import com.asistencia.model.Usuario;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class UserTableModel extends AbstractTableModel {
    private final String[] columns = {"ID", "Nombre", "Correo", "Rol", "Activo"};
    private List<Usuario> usuarios = new ArrayList<>();

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = new ArrayList<>(usuarios);
        fireTableDataChanged();
    }

    public Usuario getUsuarioAt(int row) {
        return usuarios.get(row);
    }

    @Override
    public int getRowCount() {
        return usuarios.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Usuario usuario = usuarios.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> usuario.getId();
            case 1 -> usuario.getNombre();
            case 2 -> usuario.getCorreo();
            case 3 -> usuario.getRol();
            case 4 -> usuario.isActivo() ? "Si" : "No";
            default -> "";
        };
    }
}
