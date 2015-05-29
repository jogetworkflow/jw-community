package org.jped.base.controller.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.base.controller.JaWEController;
import org.jped.base.editor.configuration.ConfigurationPanel;

public class Preferences extends ActionBase {

    public Preferences(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public void enableDisableAction() {
    }

    public void actionPerformed(ActionEvent e) {
        JaWEController jcon = (JaWEController) jawecomponent;
        final JDialog d = new JDialog();
        final ConfigurationPanel config = new ConfigurationPanel();
        JButton btnOk = new JButton("ok");
        JButton btnCancel = new JButton("cancel");
        JPanel buttons = new JPanel();

        System.out.println("action performed " + e.getActionCommand());

        d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(btnOk);
        buttons.add(btnCancel);

        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                config.applyConfig();
                d.setVisible(false);
                d.dispose();
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                d.setVisible(false);
                d.dispose();
            }
        });

        d.getContentPane().add(config);
        d.getContentPane().add(buttons);

        d.setLocationRelativeTo(jcon.getJaWEFrame());
        d.setModal(true);
        d.pack();
        d.setVisible(true);
    }
}
