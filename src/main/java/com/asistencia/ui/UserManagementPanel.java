package com.asistencia.ui;

import com.asistencia.model.Usuario;
import com.asistencia.service.UsuarioService;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private final UsuarioService usuarioService;
    private final UserTableModel tableModel = new UserTableModel();
    private final JTable table = new JTable(tableModel);

    public UserManagementPanel(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        buildContent();
        refreshUsers();
    }

    private void buildContent() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newButton = new JButton("Nuevo usuario");
        newButton.addActionListener(event -> createUser());
        JButton editButton = new JButton("Modificar");
        editButton.addActionListener(event -> editUser());
        JButton deleteButton = new JButton("Eliminar");
        deleteButton.addActionListener(event -> deleteUser());
        JButton refreshButton = new JButton("Actualizar");
        refreshButton.addActionListener(event -> refreshUsers());

        buttons.add(newButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);
        add(buttons, BorderLayout.NORTH);
    }

    private void createUser() {
        UserDialog dialog = new UserDialog(getWindow(), null);
        if (!dialog.isAccepted()) {
            return;
        }
        try {
            usuarioService.crearUsuario(dialog.getNombre(), dialog.getCorreo(), dialog.getPassword(), dialog.getRol());
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente");
            refreshUsers();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void editUser() {
        Usuario selected = getSelectedUser();
        if (selected == null) {
            showError("Debe seleccionar un usuario");
            return;
        }
        UserDialog dialog = new UserDialog(getWindow(), selected);
        if (!dialog.isAccepted()) {
            return;
        }
        try {
            usuarioService.modificarUsuario(
                    selected.getId(),
                    dialog.getNombre(),
                    dialog.getCorreo(),
                    dialog.getPassword(),
                    dialog.getRol(),
                    selected.isActivo()
            );
            JOptionPane.showMessageDialog(this, "Usuario modificado correctamente");
            refreshUsers();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteUser() {
        Usuario selected = getSelectedUser();
        if (selected == null) {
            showError("Debe seleccionar un usuario");
            return;
        }
        int result = JOptionPane.showConfirmDialog(
                this,
                "La eliminacion sera logica para conservar los registros historicos. Desea continuar?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION
        );
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            usuarioService.eliminarUsuario(selected.getId());
            JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente");
            refreshUsers();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    private void refreshUsers() {
        List<Usuario> usuarios = usuarioService.listarUsuariosActivos();
        tableModel.setUsuarios(usuarios);
    }

    private Usuario getSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        return tableModel.getUsuarioAt(modelRow);
    }

    private Window getWindow() {
        return javax.swing.SwingUtilities.getWindowAncestor(this);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
