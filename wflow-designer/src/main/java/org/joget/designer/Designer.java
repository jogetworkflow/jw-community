package org.joget.designer;

import org.enhydra.jawe.JaWE;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.editor.XPDLElementEditor;

public class Designer {

    public static String URLPATH = "";
    public static String USERNAME = "";
    public static String HASH = "";
    public static String APP_ID = "";
    public static String APP_VERSION = "";

    /**
     * Checks to see if a package is defined and fixed (non-changeable)
     * @return
     */
    public static boolean isPackageFixed() {
        return APP_ID != null && !APP_ID.isEmpty();
    }

    public static void main(String[] args) throws Throwable {

        System.setProperty("Splash", "/org/enhydra/jawe/images/wfdesigner.jpg");

        String[] argument = new String[2];
        int index = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("path:")) {
                URLPATH = args[i].substring("path:".length());
            } else if (args[i].startsWith("appId:")) {
                APP_ID = args[i].substring("appId:".length());
            } else if (args[i].startsWith("appVersion:")) {
                APP_VERSION = args[i].substring("appVersion:".length());
            } else if (args[i].startsWith("username:")) {
                USERNAME = args[i].substring("username:".length());
            } else if (args[i].startsWith("hash:")) {
                HASH = args[i].substring("hash:".length());
            } else if (args[i].startsWith("locale:")) {
                argument[1] = args[i].substring("locale:".length());
            } else {
                argument[0] = args[i];
            }
        }

        // launch JaWE
        JaWE.main(argument);

        // Automatically create new package when launched without any existing package.
        if (APP_ID == null || APP_ID.isEmpty()) {
            JaWEController jc = JaWEManager.getInstance().getJaWEController();
            if (jc.tryToClosePackage(jc.getMainPackageId(), false)) {
                //create new package
                jc.newPackage(jc.getJaWETypes().getDefaultType(Package.class));
                //pop up properties
                XPDLElementEditor ed = JaWEManager.getInstance().getXPDLElementEditor();
                ed.editXPDLElement(jc.getSelectionManager().getWorkingPKG());
            }
        }

    }
}
