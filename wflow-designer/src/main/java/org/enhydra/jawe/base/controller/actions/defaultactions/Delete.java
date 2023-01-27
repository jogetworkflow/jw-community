/**
 * Miroslav Popov, Sep 19, 2005
 * miroslav.popov@gmail.com
 */
package org.enhydra.jawe.base.controller.actions.defaultactions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JMenuItem;
import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

/**
 * @author Miroslav Popov
 *
 */
public class Delete extends ActionBase {

    public Delete(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public void enableDisableAction() {
        JaWEController jc = (JaWEController) jawecomponent;

        if (jc.getSelectionManager().canDelete()) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        JaWEController jc = JaWEManager.getInstance().getJaWEController();
        List sel = jc.getSelectionManager().getSelectedElements();
        XMLElement firstSelected = jc.getSelectionManager().getSelectedElement();

        //CUSTOM
        if ((!(firstSelected instanceof WorkflowProcess)) || (((JMenuItem) e.getSource()).getAccelerator().getKeyCode() == KeyEvent.VK_DELETE && e.getModifiers() != 0)) {
            if (jc.confirmDelete(sel, firstSelected)) {
                jc.getEdit().delete();
            }
        }
        //CUSTOM

    }
}
