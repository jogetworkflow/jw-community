package org.enhydra.jawe.base.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.base.editor.NewStandardXPDLElementEditor;
import org.enhydra.jawe.base.editor.XPDLElementEditor;
import org.enhydra.jawe.base.panel.InlinePanel;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLElement;

public class CancelChanges extends ActionBase {

    protected InlinePanel ipc;

    public CancelChanges(JaWEComponent jawecomponent) {
        super(jawecomponent);
        this.ipc = (InlinePanel) ((NewStandardXPDLElementEditor) jawecomponent).getView();
        enabled = false;

        jawecomponent.getView().getDisplay().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
                Utils.getUnqualifiedClassName(this.getClass()));

        jawecomponent.getView().getDisplay().getActionMap().put(Utils.getUnqualifiedClassName(this.getClass()), this);
    }

    public void enableDisableAction() {
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
        XMLElement el = ipc.getActiveElement();

        ipc.validateElement(el);

        boolean close = false;
        if (ipc.isModified()) {
            int sw = showModifiedWarning();
            if (sw == JOptionPane.YES_OPTION) {
                close = true;
                ipc.setModified(false);
            }
        } else {
            close = true;
        }

        if (close) {
            if (jawecomponent instanceof XPDLElementEditor) {
                XPDLElementEditor editor = ((XPDLElementEditor) jawecomponent);
                editor.close();
            } else if (el instanceof XMLCollectionElement) {
                ((NewStandardXPDLElementEditor) jawecomponent).editXPDLElement(((XMLCollectionElement) el).getParent().getParent());
            }
        }
    }

    public int showModifiedWarning() {
        if (!ipc.getPanelSettings().shouldShowModifiedWarning()) {
            return JOptionPane.NO_OPTION;
        }
        int option = JOptionPane.showConfirmDialog(JaWEManager.getInstance().getJaWEController().getJaWEFrame(),
                ipc.getPanelSettings().getLanguageDependentString("WarningReallyQuit"), ipc.getPanelSettings().getLanguageDependentString("DialogTitle"), JOptionPane.YES_NO_OPTION);
        return option;
    }
}
