package org.joget.designer.jped;

import org.enhydra.jawe.base.componentmanager.ComponentManager;
import org.enhydra.jawe.components.graph.GraphController;
import org.enhydra.jawe.components.simplenavigator.SimpleNavigator;
import org.enhydra.jawe.components.xpdlview.XPDLViewController;
import org.jped.components.graph.JPEdGraphController;
import org.jped.components.graph.JPEdGraphSettings;
import org.jped.components.simplenavigator.SimpleNavigatorSettings;
import org.jped.components.xpdlview.JPEdXPDLViewSettings;

public class CustomComponentManager extends ComponentManager {

    public CustomComponentManager() throws Exception {
        super();
    }

    public void init() {
        try {
            GraphController gc = new JPEdGraphController(new JPEdGraphSettings());
            XPDLViewController xpdlc = new XPDLViewController(new JPEdXPDLViewSettings());
            SimpleNavigator snc = new SimpleNavigator(new SimpleNavigatorSettings());
            CustomValidator vldtr = new CustomValidator(null);

            addComponent(gc);
            registerComponents(gc);
            addComponent(xpdlc);
            registerComponents(xpdlc);
            addComponent(snc);
            registerComponents(snc);
            addComponent(vldtr);
            registerComponents(vldtr);
        } catch (Exception e) {
            System.err.println("Man, we are in troubles!");
            e.printStackTrace();
        }
    }
}
