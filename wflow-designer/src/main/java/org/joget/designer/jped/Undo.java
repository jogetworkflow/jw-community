package org.joget.designer.jped;

import java.awt.event.ActionEvent;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.UndoHistoryManager;

public class Undo extends ActionBase {

    public Undo(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public void enableDisableAction() {
        JaWEController jc = JaWEManager.getInstance().getJaWEController();
        UndoHistoryManager um = jc.getUndoHistoryManager();
        setEnabled(um.canUndo());
    }

    public void actionPerformed(ActionEvent e) {
        JaWEController jc = JaWEManager.getInstance().getJaWEController();
        jc.undo();


    }
}
