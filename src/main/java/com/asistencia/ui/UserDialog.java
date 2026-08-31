package com.asistencia.ui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.asistencia.model.Rol;
import com.asistencia.model.Usuario;

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
        JPanel form = new JPanel(new GridLayout(4, 2, 15, 15));
        form.setBackground(Color.decode("#2C3E50")); 
        form.setBorder(new EmptyBorder(20, 20, 10, 20));

        //placerholders
        nombreField.putClientProperty("JTextField.placeholderText", "Ej. Juan Perez");
        nombreField.putClientProperty("JTextField.showClearButton", true);

        correoField.putClientProperty("JTextField.placeholderText", "ejemplo@empresa.cl");
        correoField.putClientProperty("JTextField.showClearButton", true);
        
        passwordField.putClientProperty("JTextField.placeholderText", "********");
        passwordField.putClientProperty("JTextField.showToggleButton", true); 


        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setForeground(Color.WHITE);
        form.add(lblNombre);
        form.add(nombreField);

        JLabel lblCorreo = new JLabel("Correo electrónico:");
        lblCorreo.setForeground(Color.WHITE);
        form.add(lblCorreo);
        form.add(correoField);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(Color.WHITE);
        form.add(lblPass);
        form.add(passwordField);

        JLabel lblRol = new JLabel("Rol del sistema:");
        lblRol.setForeground(Color.WHITE);
        form.add(lblRol);
        form.add(rolCombo);

        if (usuario != null) {
            nombreField.setText(usuario.getNombre());
            correoField.setText(usuario.getCorreo());
            rolCombo.setSelectedItem(usuario.getRol());
        }

        // Modificaciones al JOptionPane
        Object oldColor = UIManager.get("Panel.background");
        Object oldOptionBg = UIManager.get("OptionPane.background"); 
        Object oldMessageFg = UIManager.get("OptionPane.messageForeground");
        Object oldButtonFg = UIManager.get("Button.foreground");
        
        UIManager.put("OptionPane.background", Color.decode("#2C3E50"));
        UIManager.put("Panel.background", Color.decode("#2C3E50"));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("Button.foreground", Color.WHITE);

        Object[] opcionesDeBotones = {"Guardar", "Cancelar"};

        int result = JOptionPane.showOptionDialog(
                this,
                form,
                getTitle(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcionesDeBotones, 
                opcionesDeBotones[0] 
        );
        
        // Restauración
        UIManager.put("Panel.background", oldColor);
        UIManager.put("OptionPane.background", oldOptionBg); 
        UIManager.put("OptionPane.messageForeground", oldMessageFg);
        UIManager.put("Button.foreground", oldButtonFg);
        
        accepted = result == 0;
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
