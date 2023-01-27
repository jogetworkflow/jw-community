package org.enhydra.jawe.base.controller.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.shark.xpdl.StandardPackageValidator;
import org.enhydra.shark.xpdl.elements.Package;

/**
 * Class that realizes <B>save</B> action.
 * @author Sasa Bojanic
 */
public class Save extends ActionBase {

    private String myName;

    public Save(JaWEComponent jawecomponent) {
        super(jawecomponent);
    }

    public Save(JaWEComponent jawecomponent, String name) {
        super(jawecomponent, name);
        this.myName = name;
    }

    public void enableDisableAction() {
        setEnabled(JaWEManager.getInstance().getJaWEController().isSaveEnabled(false));
    }

    public void actionPerformed(ActionEvent e) {
        JaWEController jc = JaWEManager.getInstance().getJaWEController();

        boolean save = true;
        boolean allowInvalidPackageSaving = jc.getControllerSettings().allowInvalidPackageSaving() && !"Released".equalsIgnoreCase(jc.getMainPackage().getRedefinableHeader().getPublicationStatus());
        boolean isModelOK = false;

        if (!allowInvalidPackageSaving) {
            StandardPackageValidator xpdlValidator = JaWEManager.getInstance().getXPDLValidator();
            xpdlValidator.init(
                    JaWEManager.getInstance().getXPDLHandler(),
                    jc.getMainPackage(),
                    false,
                    jc.getControllerSettings().getEncoding(),
                    JaWEManager.getInstance().getStartingLocale());

            isModelOK = jc.checkValidity(jc.getMainPackage(), false).size() == 0;
            if (!isModelOK) {
                String msg = jc.getSettings().getLanguageDependentString("ErrorCannotSaveIncorrectPackage");
                jc.getJaWEFrame().message(msg, JOptionPane.ERROR_MESSAGE);
                save = false;
            }
        }

        if (save) {
            String oldFilename = jc.getPackageFilename(jc.getMainPackageId());
            String newFilename = null;
            Package pkg = jc.getMainPackage();
            if (oldFilename == null || myName != null) {
                newFilename = jc.getJaWEFrame().saveDialog(
                        jc.getSettings().getLanguageDependentString("SaveAs" + BarFactory.LABEL_POSTFIX), 0,
                        pkg.getId());
                if (!newFilename.endsWith(".xpdl")) {
                    newFilename = newFilename + ".xpdl";
                }
            } else {
                newFilename = oldFilename;
            }
            if (newFilename != null) {
                jc.savePackage(pkg.getId(), newFilename);
            }
        }
    }
}
