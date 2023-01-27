package org.enhydra.jawe.base.panel.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.enhydra.jawe.JaWE;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Settings;
import org.enhydra.jawe.base.panel.PanelContainer;
import org.enhydra.jawe.base.panel.PanelSettings;
import org.enhydra.shark.xpdl.XMLElement;
import org.joget.designer.Designer;

public class XMLTextPanel extends XMLBasicPanel {

    protected JLabel jl;
    protected JTextField jtf;

    public XMLTextPanel(
            PanelContainer pc,
            XMLElement myOwnerL,
            boolean isVertical,
            boolean isPasswordField,
            boolean isEnabled) {

        this(pc, myOwnerL, null, isVertical, isPasswordField, isEnabled);
    }

    public XMLTextPanel(
            PanelContainer pc,
            XMLElement myOwnerL,
            String label,
            boolean isVertical,
            boolean isPasswordField,
            boolean isEnabled) {

        super(pc, myOwnerL, "", isVertical, false, true);

        boolean rightAllignment = false;
        Dimension textDim = new Dimension(250, 20);

        Color bkgCol = new Color(245, 245, 245);

        if (pc != null) {
            Settings settings = pc.getSettings();

            rightAllignment = settings.getSettingBoolean("XMLBasicPanel.RightAllignment");

            textDim = new Dimension(
                    settings.getSettingInt("SimplePanelTextWidth"),
                    settings.getSettingInt("SimplePanelTextHeight"));

            if (settings instanceof PanelSettings) {
                bkgCol = ((PanelSettings) settings).getBackgroundColor();
            }

        }
        if (label == null) {
            if (pc != null) {
                label = pc.getLabelGenerator().getLabel(myOwner);
            } else {
                label = ResourceManager.getLanguageDependentString(myOwner.toName() + "Key");
            }
        }
        jl = new JLabel(label + ": ");
        jl.setAlignmentX(Component.LEFT_ALIGNMENT);
        jl.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        if (rightAllignment) {
            jl.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            jl.setHorizontalAlignment(SwingConstants.LEFT);
        }

        if (!isPasswordField) {
            jtf = new JTextField();
        } else {
            jtf = new JPasswordField();
        }
        jtf.setText(myOwner.toValue());
        jtf.setAlignmentX(Component.LEFT_ALIGNMENT);
        jtf.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        jtf.setMinimumSize(new Dimension(textDim));
        jtf.setMaximumSize(new Dimension(textDim));
        jtf.setPreferredSize(new Dimension(textDim));

        //CUSTOM
        if (JaWE.BASIC_MODE && myOwner.toName().equalsIgnoreCase("Type")) {
            jtf.setEnabled(false);
        }else if(JaWE.BASIC_MODE && myOwner.toName().equalsIgnoreCase("Id") && Designer.isPackageFixed() && myOwner.getParent() instanceof org.enhydra.shark.xpdl.elements.Package){
            jtf.setEnabled(false);
        } else {
            jtf.setEnabled(isEnabled);
        }
        //END CUSTOM

        jtf.setBackground(bkgCol);

        if (rightAllignment && !isVertical) {
            add(Box.createHorizontalGlue());
        }
        add(jl);
        if (!rightAllignment && !isVertical) {
            add(Box.createHorizontalGlue());
        }
        add(jtf);

        final XMLPanel p = this;
        //add key listener
        jtf.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (getPanelContainer() == null) {
                    return;
                }
                if (PanelUtilities.isModifyingEvent(e)) {
                    getPanelContainer().panelChanged(p, e);
                }
            }
        });
    }

    public boolean validateEntry() {
        if (isEmpty() && getOwner().isRequired() && !getOwner().isReadOnly()) {

            XMLBasicPanel.defaultErrorMessage(this.getWindow(), jl.getText());
            jtf.requestFocus();
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return getText().trim().equals("");
    }

    public void setElements() {
        if (!getOwner().isReadOnly()) {
            getOwner().setValue(getText().trim());
        }
    }

    public String getText() {
        return jtf.getText();
    }

    public Object getValue() {
        return getText();
    }

    public void requestFocus() {
        jtf.requestFocus();
    }
}
