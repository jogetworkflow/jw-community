package org.enhydra.jawe.components.simplenavigator;

import java.awt.Color;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.AdditionalResourceManager;
import org.enhydra.jawe.JaWEAction;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentSettings;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.components.simplenavigator.actions.NewProcess;
import org.enhydra.jawe.components.simplenavigator.actions.CollapseAll;
import org.enhydra.jawe.components.simplenavigator.actions.ExpandAll;

public class SimpleNavigatorSettings extends JaWEComponentSettings {

    public void init(JaWEComponent comp) {
        super.init(comp);
    }

    public void loadDefault(JaWEComponent comp, Properties properties) {
        // defaults
        arm = new AdditionalResourceManager(properties);

        Color color;
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties, "BackgroundColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=245,G=245,B=245");
        }
        componentSettings.put("BackgroundColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties, "SelectionColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=40,G=150,B=240");
        }
        componentSettings.put("SelectionColor", color);

        loadDefaultMenusToolbarsAndActions(comp);
        componentSettings.putAll(Utils.loadAllMenusAndToolbars(properties));
        componentAction.putAll(Utils.loadActions(properties, comp, componentAction));
    }

    protected void loadDefaultMenusToolbarsAndActions(JaWEComponent comp) {

        //CUSTOM
        // menu
        componentSettings.put("defaultMenu", "ExpandAll CollapseAll NewProcess - jaweAction_Duplicate jaweAction_Cut jaweAction_Copy jaweAction_Paste jaweAction_Delete - jaweAction_EditProperties");

        // toolbar
        componentSettings.put("defaultToolbarToolbar", "ExpandAll CollapseAl NewProcess");
        //END CUSTOM

        // actions
        ActionBase action;
        ImageIcon icon;
        String langDepName;
        JaWEAction ja;

        // CollapseAll
        action = new CollapseAll(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/collapseall.png"));
        langDepName = "CollapseAll";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // ExpandAll
        action = new ExpandAll(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/expandall.png"));
        langDepName = "ExpandAll";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        //CUSTOM
        //NewProcess
        action = new NewProcess(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/processnew.gif"));
        langDepName = "NewProcess";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);
        //END CUSTOM
    }

    public String getMenuActionOrder(String menuName) {
        return (String) componentSettings.get(menuName + "Menu");
    }

    public String getToolbarActionOrder(String toolbarName) {
        return (String) componentSettings.get(toolbarName + "Toolbar");
    }

    public Color getBackGroundColor() {
        return (Color) componentSettings.get("BackgroundColor");
    }

    public Color getSelectionColor() {
        return (Color) componentSettings.get("SelectionColor");
    }
}
