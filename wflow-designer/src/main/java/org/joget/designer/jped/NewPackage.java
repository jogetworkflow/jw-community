package org.joget.designer.jped;

import java.awt.event.ActionEvent;
import org.jped.base.editor.NewPackageWizardList;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.editor.XPDLElementEditor;
import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.joget.designer.Designer;

public class NewPackage extends ActionBase {

    public NewPackage(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public void enableDisableAction() {
    }

    public void actionPerformed(ActionEvent e) {
        JaWEController jc=JaWEManager.getInstance().getJaWEController();
        if (jc.tryToClosePackage(jc.getMainPackageId(), true)) {
            //jc.newPackage(JaWEConstants.PACKAGE_DEFAULT);
            NewPackageWizardList wizz = new NewPackageWizardList(jc);
            wizz.setLocationRelativeTo(jc.getJaWEFrame());
            wizz.setVisible(true);

            Designer.APP_ID = "";
            Designer.APP_VERSION = "";

            //pop up properties
            XPDLElementEditor ed = JaWEManager.getInstance().getXPDLElementEditor();
            ed.editXPDLElement(jc.getSelectionManager().getWorkingPKG());
        }
    }

}
