package org.enhydra.jawe.base.panel.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.enhydra.jawe.JaWE;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Settings;
import org.enhydra.jawe.base.panel.PanelContainer;
import org.enhydra.jawe.base.panel.PanelSettings;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.elements.Deadline;

public class XMLMultiLineTextPanel extends XMLBasicPanel {

    public static int SIZE_SMALL = 0;
    public static int SIZE_MEDIUM = 1;
    public static int SIZE_LARGE = 2;
    public static int SIZE_EXTRA_LARGE = 3;
    public static Dimension textAreaDimensionSmall = new Dimension(200, 60);
    public static Dimension textAreaDimensionMedium = new Dimension(300, 90);
    public static Dimension textAreaDimensionLarge = new Dimension(400, 120);
    public static Dimension textAreaDimensionExtraLarge = new Dimension(400, 300);
    protected JTextArea jta;
    protected JLabel jl;
    protected boolean falseRequiredForCC = false;

    public XMLMultiLineTextPanel(
            PanelContainer pc,
            XMLElement myOwner,
            boolean isVertical,
            int type,
            boolean wrapLines,
            boolean isEnabled) {
        this(pc, myOwner, myOwner.toName(), myOwner.isRequired(), isVertical, type, wrapLines, isEnabled);
    }

    public XMLMultiLineTextPanel(
            PanelContainer pc,
            XMLElement myOwner,
            String labelKey,
            boolean isFalseRequired,
            boolean isVertical,
            int type,
            boolean wrapLines,
            boolean isEnabled) {



        super(pc, myOwner, "", false, false, true);

        this.falseRequiredForCC = isFalseRequired;

        boolean rightAllignment = false;

        Color bkgCol = new Color(245, 245, 245);
        if (pc != null) {
            Settings settings = pc.getSettings();

            rightAllignment = settings.getSettingBoolean("XMLBasicPanel.RightAllignment");

            if (settings instanceof PanelSettings) {
                bkgCol = ((PanelSettings) settings).getBackgroundColor();
            }

        }


        JScrollPane jsp = new JScrollPane();
        jsp.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsp.setAlignmentY(Component.TOP_ALIGNMENT);

        String lbl = "";
        if (pc != null) {
            lbl = pc.getSettings().getLanguageDependentString(labelKey + "Key") + ": ";
        } else {
            lbl = ResourceManager.getLanguageDependentString(labelKey + "Key") + ": ";
        }
        jl = new JLabel(lbl);
        jl.setAlignmentX(Component.LEFT_ALIGNMENT);
        jl.setAlignmentY(Component.TOP_ALIGNMENT);

        if (rightAllignment) {
            jl.setHorizontalAlignment(SwingConstants.RIGHT);
        } else {
            jl.setHorizontalAlignment(SwingConstants.LEFT);
        }

        jta = new JTextArea();

        jta.setTabSize(4);
        jta.setText(myOwner.toValue());
        jta.getCaret().setDot(0);
        jta.setLineWrap(wrapLines);
        jta.setWrapStyleWord(wrapLines);

        jta.setAlignmentX(Component.LEFT_ALIGNMENT);
        jta.setAlignmentY(Component.TOP_ALIGNMENT);

        //CUSTOM
        if (JaWE.BASIC_MODE && myOwner.toName().equalsIgnoreCase("DeadlineCondition")) {
            if (myOwner instanceof Deadline) {
                jta.setEnabled(true);
            } else {
                jta.setEnabled(false);
            }
        } else {
            jta.setEnabled(isEnabled);
        }

        //END CUSTOM

        jta.setBackground(bkgCol);

        final XMLPanel p = this;
        jta.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (getPanelContainer() == null) {
                    return;
                }
                if (PanelUtilities.isModifyingEvent(e)) {
                    getPanelContainer().panelChanged(p, e);
                }
            }
        });

        jsp.setViewportView(jta);
        jsp.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsp.setAlignmentY(Component.TOP_ALIGNMENT);
        Dimension dim = new Dimension(textAreaDimensionMedium);
        if (type == XMLMultiLineTextPanel.SIZE_SMALL) {
            dim = new Dimension(textAreaDimensionSmall);
        } else if (type == XMLMultiLineTextPanel.SIZE_MEDIUM) {
            dim = new Dimension(textAreaDimensionMedium);
        } else if (type == XMLMultiLineTextPanel.SIZE_LARGE) {
            dim = new Dimension(textAreaDimensionLarge);
        } else {
            dim = new Dimension(textAreaDimensionExtraLarge);
        }
        jsp.setPreferredSize(dim);

        JPanel mainPanel = this;
        if (isVertical) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        }
        if (rightAllignment && !isVertical) {
            mainPanel.add(Box.createHorizontalGlue());
        }
        mainPanel.add(jl);
        if (!rightAllignment && !isVertical) {
            mainPanel.add(Box.createHorizontalGlue());

        }
        mainPanel.add(jsp);
        if (!rightAllignment && !isVertical) {
            mainPanel.add(Box.createHorizontalGlue());
        }
        if (isVertical) {
            add(mainPanel);
        }



    }

    public boolean validateEntry() {
        if (isEmpty() && getOwner().isRequired() && falseRequiredForCC && !getOwner().isReadOnly()) {
            //TODO CHECK THIS
            XMLBasicPanel.defaultErrorMessage(this.getWindow(), jl.getText());
            jta.requestFocus();
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return getText().trim().equals("");
    }

    public void setElements() {
        if (!getOwner().isReadOnly()) {
            myOwner.setValue(getText().trim());
        }
    }

    public String getText() {
        return jta.getText();
    }

    public Object getValue() {
        return getText();
    }

    public void requestFocus() {
        jta.requestFocus();
    }
}
