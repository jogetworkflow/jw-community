package org.joget.designer.jped;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentSettings;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.XPDLElementChangeInfo;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.elements.Package;

public class CustomValidator implements Observer, JaWEComponent {

    public CustomValidator(JaWEComponentSettings settings)
            throws Exception {
        type = "SPECIAL";
        updateInProgress = false;
        init();
        JaWEManager.getInstance().getJaWEController().addObserver(this);
    }

    protected void init() {
        panel = new CustomValidatorPanel(this);
    }

    public JaWEComponentSettings getSettings() {
        return null;
    }

    public void update(Observable o, Object arg) {
        if (!(arg instanceof XPDLElementChangeInfo)) {
            return;
        }
        XPDLElementChangeInfo info = (XPDLElementChangeInfo) arg;
        int action = info.getAction();
        if (action != 10) {
            return;
        } else {
            long start = System.currentTimeMillis();
            JaWEManager.getInstance().getLoggingManager().info("CustomValidator -> update for event " + info + " started ...");
            update(info);
            JaWEManager.getInstance().getLoggingManager().info("CustomValidator -> update ended...");
            long end = System.currentTimeMillis();
            double diffs = (double) (end - start) / 1000D;
            JaWEManager.getInstance().getLoggingManager().debug("THE UPDATE OF SEARCH NAVIG COMPONENT LASTED FOR " + diffs + " SECONDS!");
            return;
        }
    }

    public void update(XPDLElementChangeInfo info) {
        if (updateInProgress) {
            return;
        }
        if (info.getSource() == this) {
            return;
        }
        updateInProgress = true;
        try {
            int action = info.getAction();
            if (action == XPDLElementChangeInfo.VALIDATION_ERRORS) {
                panel.fillListContent(info.getChangedSubElements());
            }
        } finally {
            updateInProgress = false;
        }
    }

    public JaWEComponentView getView() {
        return panel;
    }

    public String getName() {
        return "CustomValidator";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean adjustXPDL(Package pckg) {
        return false;
    }

    public List checkValidity(XMLElement el, boolean fullCheck) {
        return null;
    }

    public boolean canCreateElement(XMLCollection col) {
        return true;
    }

    public boolean canInsertElement(XMLCollection col, XMLElement el) {
        return true;
    }

    public boolean canModifyElement(XMLElement el) {
        return true;
    }

    public boolean canRemoveElement(XMLCollection col, XMLElement el) {
        return true;
    }

    public boolean canDuplicateElement(XMLCollection col, XMLElement el) {
        return true;
    }

    public boolean canRepositionElement(XMLCollection col, XMLElement el) {
        return true;
    }

    public void setUpdateInProgress(boolean inProgress) {
        updateInProgress = inProgress;
    }

    public boolean isUpdateInProgress() {
        return updateInProgress;
    }

    protected String type;
    protected CustomValidatorPanel panel;
    protected boolean updateInProgress;

}
