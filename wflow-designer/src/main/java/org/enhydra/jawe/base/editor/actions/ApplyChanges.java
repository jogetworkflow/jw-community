package org.enhydra.jawe.base.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
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

public class ApplyChanges extends ActionBase {

    protected InlinePanel ipc;

    public ApplyChanges(JaWEComponent jawecomponent) {
        super(jawecomponent);
        this.ipc = (InlinePanel) ((NewStandardXPDLElementEditor) jawecomponent).getView();
        enabled = false;

        jawecomponent.getView().getDisplay().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                Utils.getUnqualifiedClassName(this.getClass()));

        jawecomponent.getView().getDisplay().getActionMap().put(Utils.getUnqualifiedClassName(this.getClass()), this);
    }

    public void enableDisableAction() {
        if (ipc.isModified()) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!ipc.canApplyChanges()) {
            return;
        }
        XMLElement el = ipc.getActiveElement();

        jawecomponent.setUpdateInProgress(true);
        JaWEManager.getInstance().getJaWEController().startUndouableChange();
        ipc.apply();
        List toSelect = new ArrayList();
        XMLElement toSel = el;
        if (toSel != null) {
            toSelect.add(toSel);
        }
        JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
        jawecomponent.setUpdateInProgress(false);

        ipc.validateElement(el);

        ipc.setModified(false);

        //CUSTOM
        if(jawecomponent instanceof XPDLElementEditor) {
            XPDLElementEditor editor = ((XPDLElementEditor) jawecomponent);
            editor.close();
        } else if(el instanceof XMLCollectionElement) {
            ((NewStandardXPDLElementEditor)jawecomponent).editXPDLElement(((XMLCollectionElement)el).getParent().getParent());
        }
        //END CUSTOM
    }
}
