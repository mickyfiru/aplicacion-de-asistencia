package com.asistencia.ui;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class UserDialog extends JDialog {
    private final JTextField nombreField = new JTextField();
    private final JTextField correoField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<Rol> rolCombo = new JComboBox<>(Rol.values());
    private boolean accepted;

    public UserDialog(java.awt.Window owner, Usuario usuario) {
        super(owner, usuario == null ? "Nuevo usuario" : "Modificar usuario", ModalityType.APPLICATION_MODAL);
        buildContent(usuario);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildContent(Usuario usuario) {
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Nombre"));
        form.add(nombreField);
        form.add(new JLabel("Correo"));
        form.add(correoField);
        form.add(new JLabel("Contrasena"));
        form.add(passwordField);
        form.add(new JLabel("Rol"));
        form.add(rolCombo);

        if (usuario != null) {
            nombreField.setText(usuario.getNombre());
            correoField.setText(usuario.getCorreo());
            rolCombo.setSelectedItem(usuario.getRol());
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                getTitle(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        accepted = result == JOptionPane.OK_OPTION;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getNombre() {
        return nombreField.getText();
    }

    public String getCorreo() {
        return correoField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public Rol getRol() {
        return (Rol) rolCombo.getSelectedItem();
    }
}
