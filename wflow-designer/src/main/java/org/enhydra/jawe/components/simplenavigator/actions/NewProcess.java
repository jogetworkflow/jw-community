package org.enhydra.jawe.components.simplenavigator.actions;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.controller.JaWETypeChoiceButton;
import org.enhydra.jawe.base.xpdlobjectfactory.XPDLObjectFactory;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

public class NewProcess extends ActionBase {

    public NewProcess(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public void enableDisableAction() {
        if (JaWEManager.getInstance().getJaWEController().getMainPackage() != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!(e.getSource() instanceof JaWETypeChoiceButton)) {
            JaWEController jc = JaWEManager.getInstance().getJaWEController();
            Package pkg = jc.getMainPackage();
            if (pkg == null) {
                return;
            }
            jc.startUndouableChange();
            XPDLObjectFactory of = JaWEManager.getInstance().getXPDLObjectFactory();
            WorkflowProcess wp = of.createXPDLObject(pkg.getWorkflowProcesses(),
                    jc.getJaWETypes().getDefaultType(WorkflowProcess.class),
                    true);
            List toSelect = new ArrayList();
            toSelect.add(wp);
            jc.endUndouableChange(toSelect);
        }
    }
}
