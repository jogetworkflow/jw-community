package org.enhydra.jawe.components.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.net.URL;
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
import org.enhydra.jawe.components.graph.actions.ActivityReferredDocument;
import org.enhydra.jawe.components.graph.actions.ActualSize;
import org.enhydra.jawe.components.graph.actions.AddPoint;
import org.enhydra.jawe.components.graph.actions.DescendInto;
import org.enhydra.jawe.components.graph.actions.GraphPaste;
import org.enhydra.jawe.components.graph.actions.InsertActivitySet;
import org.enhydra.jawe.components.graph.actions.InsertMissingStartAndEndBubbles;
import org.enhydra.jawe.components.graph.actions.MoveDownParticipant;
import org.enhydra.jawe.components.graph.actions.MoveUpParticipant;
import org.enhydra.jawe.components.graph.actions.NextGraph;
import org.enhydra.jawe.components.graph.actions.PreviousGraph;
import org.enhydra.jawe.components.graph.actions.RemoveParticipant;
import org.enhydra.jawe.components.graph.actions.RemovePoint;
import org.enhydra.jawe.components.graph.actions.RemoveStartAndEndBubbles;
import org.enhydra.jawe.components.graph.actions.SaveAsJPG;
import org.enhydra.jawe.components.graph.actions.SaveAsSVG;
import org.enhydra.jawe.components.graph.actions.SetTransitionStyleNoRoutingBezier;
import org.enhydra.jawe.components.graph.actions.SetTransitionStyleNoRoutingOrthogonal;
import org.enhydra.jawe.components.graph.actions.SetTransitionStyleNoRoutingSpline;
import org.enhydra.jawe.components.graph.actions.SetTransitionStyleSimpleRoutingOrthogonal;
import org.enhydra.jawe.components.graph.actions.SetTransitionStyleSimpleRoutingSpline;
import org.enhydra.jawe.components.graph.actions.SimpleGraphLayout;
import org.enhydra.jawe.components.graph.actions.ZoomIn;
import org.enhydra.jawe.components.graph.actions.ZoomOut;

public class GraphSettings extends JaWEComponentSettings {

    public static final Stroke DEPARTMENT_STROKE = new BasicStroke(2);
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public void init(JaWEComponent comp) {
        PROPERTYFILE_PATH = "org/enhydra/jawe/components/graph/properties/";
        PROPERTYFILE_NAME = "togwegraphcontroller.properties";
        super.init(comp);
    }

    public void loadDefault(JaWEComponent comp, Properties properties) {
        arm = new AdditionalResourceManager(properties);

        componentSettings.put("UseParticipantChoiceButton",
                new Boolean(properties.getProperty("GraphPanel.UseParticipantChoiceButton",
                "true").equals("true")));
        componentSettings.put("UseActivitySetChoiceButton",
                new Boolean(properties.getProperty("GraphPanel.UseActivitySetChoiceButton",
                "true").equals("true")));
        componentSettings.put("GraphOverview.Class",
                properties.getProperty("GraphOverview.Class",
                "org.enhydra.jawe.components.graph.overviewpanel.GraphOverviewPanel"));
        componentSettings.put("ShowGraphOverview",
                new Boolean(properties.getProperty("GraphOverview.Show",
                "true").equals("true")));
        componentSettings.put("NameWrapping",
                new Boolean(properties.getProperty("Graph.NameWrapping",
                "true").equals("true")));
        componentSettings.put("WordWrapping",
                new Boolean(properties.getProperty("Graph.WrappingStyleWordStatus",
                "true").equals("true")));
        componentSettings.put("ShowGrid",
                new Boolean(properties.getProperty("Graph.ShowGrid", "false").equals("true")));
        componentSettings.put("ShowIcons",
                new Boolean(properties.getProperty("Graph.ShowIcon", "true").equals("true")));
        componentSettings.put("ShowShadow",
                new Boolean(properties.getProperty("Graph.ShowShadow", "true").equals("true")));
        componentSettings.put("ShowTransitionCondition",
                new Boolean(properties.getProperty("Graph.ShowTransitionCondition",
                "false").equals("true")));
        componentSettings.put("ShowTransitionNameForCondition",
                new Boolean(properties.getProperty("Graph.ShowTransitionNameForCondition",
                "false").equals("true")));
        componentSettings.put("UseBubbles",
                new Boolean(properties.getProperty("Graph.UseBubbles", "true").equals("true")));
        componentSettings.put("DrawBlockLines",
                new Boolean(properties.getProperty("Graph.DrawBlockLines",
                "true").equals("true")));
        componentSettings.put("DrawSubflowLines",
                new Boolean(properties.getProperty("Graph.DrawSubflowLines",
                "true").equals("true")));

        componentSettings.put("GraphClass",
                properties.getProperty("Graph.Class",
                "org.enhydra.jawe.components.graph.Graph"));
        componentSettings.put("GraphManagerClass",
                properties.getProperty("GraphManager.Class",
                "org.enhydra.jawe.components.graph.GraphManager"));
        componentSettings.put("GraphMarqueeHandlerClass",
                properties.getProperty("GraphMarqueeHandler.Class",
                "org.enhydra.jawe.components.graph.GraphMarqueeHandler"));
        componentSettings.put("GraphModelClass",
                properties.getProperty("GraphModel.Class",
                "org.enhydra.jawe.components.graph.JaWEGraphModel"));
        componentSettings.put("GraphObjectFactoryClass",
                properties.getProperty("GraphObjectFactory.Class",
                "org.enhydra.jawe.components.graph.GraphObjectFactory"));
        componentSettings.put("GraphObjectRendererFactoryClass",
                properties.getProperty("GraphObjectRendererFactoryClass",
                "org.enhydra.jawe.components.graph.GraphObjectRendererFactory"));
        componentSettings.put("DefaultTransitionStyle",
                properties.getProperty("Graph.DefaultTransitionStyle",
                "NO_ROUTING_SPLINE"));

        componentSettings.put("GridSize",
                new Integer(properties.getProperty("Graph.GridSize", "10")));
        componentSettings.put("ShadowWidth",
                new Integer(properties.getProperty("Graph.ShadowWidth", "3")));
        componentSettings.put("GraphFontSize",
                new Integer(properties.getProperty("Graph.FontSize", "12")));
        componentSettings.put("ActivityHeight",
                new Integer(properties.getProperty("Graph.ActivityHeight",
                "55")));
        componentSettings.put("ActivityWidth",
                new Integer(properties.getProperty("Graph.ActivityWidth",
                "85")));
        componentSettings.put("ParticipantNameWidth",
                new Integer(properties.getProperty("Graph.ParticipantNameWidth",
                "50")));
        componentSettings.put("MinParWidth",
                new Integer(properties.getProperty("Graph.ParticipantMinWidth",
                "800")));
        componentSettings.put("MinParHeight",
                new Integer(properties.getProperty("Graph.ParticipantMinHeight",
                "150")));
        String textPos = properties.getProperty("Graph.TextPosition", "right");
        int tpv = RIGHT;
        if ("left".equalsIgnoreCase(textPos)) {
            tpv = LEFT;
        } else if ("up".equalsIgnoreCase(textPos)) {
            tpv = UP;
        } else if ("down".equalsIgnoreCase(textPos)) {
            tpv = DOWN;
        }
        componentSettings.put("TextPosition", new Integer(tpv));

        componentSettings.put("Graph.HistoryManager.Class",
                properties.getProperty("Graph.HistoryManager.Class",
                "org.enhydra.jawe.HistoryMgr"));
        componentSettings.put("Graph.HistorySize",
                new Integer(properties.getProperty("Graph.HistorySize", "15")));

        //CUSTOM

        Color color;
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.BubbleStartColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=227,G=254,B=166");
        }
        componentSettings.put("BubbleStartColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.BubbleEndColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=243,G=201,B=197");
        }
        componentSettings.put("BubbleEndColor", color);
        //END CUSTOM

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.BubbleStartConnectionColor"));
        } catch (Exception e) {
            color = Utils.getColor("SystemColor.textHighlight");
        }
        componentSettings.put("BubbleStartConnectionColor", color);

        //CUSTOM
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.BubbleEndConnectionColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=218,G=26,B=43");
        }
        componentSettings.put("BubbleEndConnectionColor", color);
        //END CUSTOM

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.ActivitySelectedColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=248,G=242,B=14");
        }
        componentSettings.put("ActivitySelectedColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.StartActivityColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=102, G=204, B=51");
        }
        componentSettings.put("StartActivityColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.EndActivityColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=236, G=120, B=98");
        }
        componentSettings.put("EndActivityColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.StartEndActivityColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=255, G=255, B=175");
        }
        componentSettings.put("StartEndActivityColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.BackgroundColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=245,G=245,B=245");
        }
        componentSettings.put("BackgroundColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.GridColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=187,G=247,B=190");
        }
        componentSettings.put("GridColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.TextColor"));
        } catch (Exception e) {
            color = Utils.getColor("SystemColor.textText");
        }
        componentSettings.put("TextColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.ParticipantBorderColor"));
        } catch (Exception e) {
            color = Utils.getColor("SystemColor.textText");
        }
        componentSettings.put("ParticipantBorderColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.ParticipantFreeTextExpressionColor"));
        } catch (Exception e) {
            color = Utils.getColor("Color.white");
        }
        componentSettings.put("ParticipantFreeTextExpressionColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.ParticipantCommonExpressionColor"));
        } catch (Exception e) {
            color = Utils.getColor("R=255,G=255,B=196");
        }
        componentSettings.put("ParticipantCommonExpressionColor", color);

        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.HandleColor"));
        } catch (Exception e) {
            color = Utils.getColor("Color.pink");
        }
        componentSettings.put("HandleColor", color);
        try {
            color = Utils.getColor(ResourceManager.getResourceString(properties,
                    "Graph.MarqueeColor"));
        } catch (Exception e) {
            color = Utils.getColor("SystemColor.textHighlight");
        }
        componentSettings.put("MarqueeColor", color);

        ImageIcon cicon;
        URL iconURL = ResourceManager.getResource(properties,
                "Graph.XPDLElement.Image.Defualt");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/genericactivity.gif"));
        }
        componentSettings.put("DefaultActivityIcon", cicon);

        //CUSTOM
        iconURL = ResourceManager.getResource(properties, "Graph.XPDLElement.Image.Start");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/start.gif"));
        }
        componentSettings.put("BubbleGraphStart", cicon);

        iconURL = ResourceManager.getResource(properties, "Graph.XPDLElement.Image.End");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/end.gif"));
        }
        componentSettings.put("BubbleGraphEnd", cicon);

        cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/toolbarstart.gif"));
        componentSettings.put("BubbleToolBarStart", cicon);

        cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/toorbarend.gif"));
        componentSettings.put("BubbleToolBarEnd", cicon);

        //END CUSTOM

        iconURL = ResourceManager.getResource(properties,
                "Graph.XPDLElement.Image.FreeTextParticipant");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/freetextparticipant.png"));
        }
        componentSettings.put("FreeTextParticipant", cicon);

        iconURL = ResourceManager.getResource(properties,
                "Graph.XPDLElement.Image.CommonExpresionParticipant");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/commonexpparticipant.png"));
        }
        componentSettings.put("CommonExpresionParticipant", cicon);

        iconURL = ResourceManager.getResource(properties, "GraphPanel.Image.Participants");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/participantsselect.gif"));
        }
        componentSettings.put("Participants", cicon);

        iconURL = ResourceManager.getResource(properties,
                "GraphPanel.Image.ActivitySetSelect");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/activitysetselect.gif"));
        }
        componentSettings.put("ActivitySetSelect", cicon);

        iconURL = ResourceManager.getResource(properties, "GraphPanel.Image.Selection");
        if (iconURL != null) {
            cicon = new ImageIcon(iconURL);
        } else {
            cicon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/select.gif"));
        }
        componentSettings.put("Selection", cicon);

        // menus, toolbars and actions
        loadDefaultMenusToolbarsAndActions(comp);
        componentSettings.putAll(Utils.loadAllMenusAndToolbars(properties));
        componentAction.putAll(Utils.loadActions(properties, comp, componentAction));
    }

    protected void loadDefaultMenusToolbarsAndActions(JaWEComponent comp) {
        // menu
        componentSettings.put("ACTIVITYMenu",
                "jaweAction_Cut jaweAction_Copy jaweAction_Delete jaweAction_EditProperties - ActivityReferredDocument SelectConnectingTransitionsForSelectedActivities");
        componentSettings.put("ACTIVITY_BLOCKMenu", "DescendInto");
        componentSettings.put("ACTIVITY_SUBFLOWMenu", "DescendInto");
        componentSettings.put("ENDMenu", "jaweAction_Delete");
        componentSettings.put("PARTICIPANTMenu",
                "RemoveParticipant jaweAction_Delete jaweAction_EditProperties - MoveUpParticipant MoveDownParticipant");
        componentSettings.put("SELECTMenu", "GraphPaste");
        componentSettings.put("STARTMenu", "jaweAction_Delete");
        componentSettings.put("TRANSITIONMenu",
                "AddPoint RemovePoint jaweAction_Delete *SetTransitionStyle jaweAction_EditProperties - SelectConnectingActivitiesForSelectedTransitions");
        componentSettings.put("SetTransitionStyleMenu",
                "SetTransitionStyleNoRoutingBezier SetTransitionStyleNoRoutingOrthogonal SetTransitionStyleNoRoutingSpline - SetTransitionStyleSimpleRoutingBezier SetTransitionStyleSimpleRoutingOrthogonal SetTransitionStyleSimpleRoutingSpline");
        componentSettings.put("SetTransitionStyleLangName", "SetTransitionStyle");

        // toolbar
        componentSettings.put("defaultToolbarToolbar", "*graphEditToolbar");
        componentSettings.put("toolboxToolbar",
                "SetSelectMode - SetParticipantModePARTICIPANT_ROLE SetParticipantModeFreeTextExpression SetParticipantModeCommonExpression - SetStartMode SetEndMode - SetActivityMode* - SetTransitionMode*");
        componentSettings.put("graphEditToolbarToolbar",
                "SaveAsJPG SaveAsSVG - ZoomIn ActualSize ZoomOut - MoveUpParticipant MoveDownParticipant - PreviousGraph NextGraph - InsertMissingStartAndEndBubbles RemoveStartAndEndBubbles - RotateProcess SimpleGraphLayout - InsertActivitySet");

        // actions
        ActionBase action = null;
        ImageIcon icon;
        String langDepName;
        JaWEAction ja;

        // SetPerformerExpression
        try {
            String clsName = "org.enhydra.jawe.components.graph.actions.SetPerformerExpression";
            try {
                action = (ActionBase) Class.forName(clsName).getConstructor(new Class[]{
                            JaWEComponent.class
                        }).newInstance(new Object[]{
                            comp
                        });
            } catch (Exception e) {
            }
            icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/commonexpparticipantsetexp.png"));
            langDepName = "SetPerformerExpression";
            ja = new JaWEAction(action, icon, langDepName);
            componentAction.put(langDepName, ja);
        } catch (Exception ex) {
        }
        // ActualSize
        action = new ActualSize(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/actualsize.gif"));
        langDepName = "ActualSize";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // ActivityReferredDocument
        action = new ActivityReferredDocument(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/referred_document.png"));
        langDepName = "ActivityReferredDocument";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SelectConnectingTransitionsForSelectedActivities
        try {
            String clsName = "org.enhydra.jawe.components.graph.actions.SelectConnectingTransitionsForSelectedActivities";
            try {
                action = (ActionBase) Class.forName(clsName).getConstructor(new Class[]{
                            JaWEComponent.class
                        }).newInstance(new Object[]{
                            comp
                        });
            } catch (Exception e) {
                action = null;
            }
            icon = null;
            langDepName = "SelectConnectingTransitionsForSelectedActivities";
            ja = new JaWEAction(action, icon, langDepName);
            componentAction.put(langDepName, ja);
        } catch (Exception ex) {
        }

        // AddPoint
        action = new AddPoint(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/addpoint.gif"));
        langDepName = "AddPoint";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // DescendInto
        action = new DescendInto(comp);
        langDepName = "DescendInto";
        ja = new JaWEAction(action, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // GraphPaste
        action = new GraphPaste(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/paste.gif"));
        langDepName = "Paste";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // InsertActivitySet
        action = new InsertActivitySet(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/activitysetnew.gif"));
        langDepName = "InsertActivitySet";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // MoveDownParticipant
        action = new MoveDownParticipant(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/participantdownright.gif"));
        langDepName = "MoveDownParticipant";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // MoveUpParticipant
        action = new MoveUpParticipant(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/participantupleft.gif"));
        langDepName = "MoveUpParticipant";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // NextGraph
        action = new NextGraph(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/nav_right_red.png"));
        langDepName = "NextGraph";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // PreviousGraph
        action = new PreviousGraph(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/nav_left_red.png"));
        langDepName = "PreviousGraph";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // InsertMissingStartAndEndBubbles
        action = new InsertMissingStartAndEndBubbles(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/startend.gif"));
        langDepName = "InsertMissingStartAndEndBubbles";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // RemoveStartAndEndBubbles
        action = new RemoveStartAndEndBubbles(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/startend_remove.gif"));
        langDepName = "RemoveStartAndEndBubbles";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // RemoveParticipant
        action = new RemoveParticipant(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/participantremove.png"));
        langDepName = "RemoveParticipant";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // RemovePoint
        action = new RemovePoint(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/removepoint.gif"));
        langDepName = "RemovePoint";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // RotateProcess
        try {
            String clsName = "org.enhydra.jawe.components.graph.actions.RotateProcess";
            try {
                action = (ActionBase) Class.forName(clsName).getConstructor(new Class[]{
                            JaWEComponent.class
                        }).newInstance(new Object[]{
                            comp
                        });
            } catch (Exception e) {
                action = null;
            }
            icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/process_rotate.gif"));
            langDepName = "RotateProcess";
            ja = new JaWEAction(action, icon, langDepName);
            componentAction.put(langDepName, ja);
        } catch (Exception ex) {
        }

        // SimpleGraphLayout
        action = new SimpleGraphLayout(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/graph_layout.png"));
        langDepName = "SimpleGraphLayout";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SaveAsJPG
        action = new SaveAsJPG(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/saveasjpg.gif"));
        langDepName = "SaveAsJPG";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SaveAsSVG
        action = new SaveAsSVG(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/saveassvg.gif"));
        langDepName = "SaveAsSVG";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleNoRoutingBezier
        action = new SetTransitionStyleNoRoutingBezier(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionbezier.gif"));
        langDepName = "SetTransitionStyleNoRoutingBezier";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleNoRoutingOrthogonal
        action = new SetTransitionStyleNoRoutingOrthogonal(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionortogonal.gif"));
        langDepName = "SetTransitionStyleNoRoutingOrthogonal";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleNoRoutingSpline
        action = new SetTransitionStyleNoRoutingSpline(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionspline.gif"));
        langDepName = "SetTransitionStyleNoRoutingSpline";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleSimpleRoutingBezier
        action = new SetTransitionStyleNoRoutingBezier(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionbeziersr.gif"));
        langDepName = "SetTransitionStyleSimpleRoutingBezier";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleSimpleRoutingOrthogonal
        action = new SetTransitionStyleSimpleRoutingOrthogonal(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionortogonalsr.gif"));
        langDepName = "SetTransitionStyleSimpleRoutingOrthogonal";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SetTransitionStyleSimpleRoutingSpline
        action = new SetTransitionStyleSimpleRoutingSpline(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/transitionsplinesr.gif"));
        langDepName = "SetTransitionStyleSimpleRoutingSpline";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // SelectConnectingActivitiesForSelectedTransitions
        try {
            String clsName = "org.enhydra.jawe.components.graph.actions.SelectConnectingActivitiesForSelectedTransitions";
            try {
                action = (ActionBase) Class.forName(clsName).getConstructor(new Class[]{
                            JaWEComponent.class
                        }).newInstance(new Object[]{
                            comp
                        });
            } catch (Exception e) {
                action = null;
            }
            icon = null;
            langDepName = "SelectConnectingActivitiesForSelectedTransitions";
            ja = new JaWEAction(action, icon, langDepName);
            componentAction.put(langDepName, ja);
        } catch (Exception ex) {
        }

        // ZoomIn
        action = new ZoomIn(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/zoomin.gif"));
        langDepName = "ZoomIn";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);

        // ZoomOut
        action = new ZoomOut(comp);
        icon = new ImageIcon(ResourceManager.class.getClassLoader().getResource("org/enhydra/jawe/images/zoomout.gif"));
        langDepName = "ZoomOut";
        ja = new JaWEAction(action, icon, langDepName);
        componentAction.put(action.getValue(Action.NAME), ja);
    }

    public String getMenuActionOrder(String menuName) {
        return (String) componentSettings.get(menuName + "Menu");
    }

    public String getToolbarActionOrder(String toolbarName) {
        return (String) componentSettings.get(toolbarName + "Toolbar");
    }

    public boolean shouldDrawBlockLines() {
        return ((Boolean) componentSettings.get("DrawBlockLines")).booleanValue();
    }

    public boolean shouldDrawSubflowLines() {
        return ((Boolean) componentSettings.get("DrawSubflowLines")).booleanValue();
    }

    public boolean isNameWrappingEnabled() {
        return ((Boolean) componentSettings.get("NameWrapping")).booleanValue();
    }

    public boolean isWordWrappingEnabled() {
        return ((Boolean) componentSettings.get("WordWrapping")).booleanValue();
    }

    public boolean shouldShowGrid() {
        return ((Boolean) componentSettings.get("ShowGrid")).booleanValue();
    }

    public boolean shouldShowIcons() {
        return ((Boolean) componentSettings.get("ShowIcons")).booleanValue();
    }

    public boolean isShadowEnabled() {
        return ((Boolean) componentSettings.get("ShowShadow")).booleanValue();
    }

    public boolean shouldShowTransitionCondition() {
        return ((Boolean) componentSettings.get("ShowTransitionCondition")).booleanValue();
    }

    public boolean shouldShowTransitionNameForCondition() {
        return ((Boolean) componentSettings.get("ShowTransitionNameForCondition")).booleanValue();
    }

    public boolean shouldShowTransitionName() {
        //CUSTOM
        return true;
        //END CUSTOM
    }

    public boolean shouldUseBubbles() {
        return ((Boolean) componentSettings.get("UseBubbles")).booleanValue();
    }

    public boolean useParticipantChoiceButton() {
        return ((Boolean) componentSettings.get("UseParticipantChoiceButton")).booleanValue();
    }

    public boolean useActivitySetChoiceButton() {
        return ((Boolean) componentSettings.get("UseActivitySetChoiceButton")).booleanValue();
    }

    public boolean shouldShowGraphOverview() {
        return ((Boolean) componentSettings.get("ShowGraphOverview")).booleanValue();
    }

    public String getGraphObjectFactory() {
        return (String) componentSettings.get("GraphObjectFactoryClass");
    }

    public String getGraphObjectRendererFactory() {
        return (String) componentSettings.get("GraphObjectRendererFactoryClass");
    }

    public String getGraphMarqueeHandler() {
        return (String) componentSettings.get("GraphMarqueeHandlerClass");
    }

    public String getGraphClass() {
        return (String) componentSettings.get("GraphClass");
    }

    public String getGraphModelClass() {
        return (String) componentSettings.get("GraphModelClass");
    }

    public String getGraphManager() {
        return (String) componentSettings.get("GraphManagerClass");
    }

    public String getDefaultTransitionStyle() {
        return (String) componentSettings.get("DefaultTransitionStyle");
    }

    public ImageIcon getDefaultActivityIcon() {
        return (ImageIcon) componentSettings.get("DefaultActivityIcon");
    }

    //CUSTOM
    public ImageIcon getBubbleStartIcon() {
        return (ImageIcon) componentSettings.get("BubbleToolBarStart");
    }

    public ImageIcon getBubbleEndIcon() {
        return (ImageIcon) componentSettings.get("BubbleToolBarEnd");
    }

    public ImageIcon getBubbleGraphStartIcon() {
        return (ImageIcon) componentSettings.get("BubbleGraphStart");
    }

    public ImageIcon getBubbleGraphEndIcon() {
        return (ImageIcon) componentSettings.get("BubbleGraphEnd");
    }
    //END CUSTOM

    public ImageIcon getFreeTextParticipantIcon() {
        return (ImageIcon) componentSettings.get("FreeTextParticipant");
    }

    public ImageIcon getCommonExpresionParticipantIcon() {
        return (ImageIcon) componentSettings.get("CommonExpresionParticipant");
    }

    public ImageIcon getParticipantsIcon() {
        return (ImageIcon) componentSettings.get("Participants");
    }

    public ImageIcon getActivitySetSelectIcon() {
        return (ImageIcon) componentSettings.get("ActivitySetSelect");
    }

    public ImageIcon getSelectionIcon() {
        return (ImageIcon) componentSettings.get("Selection");
    }

    public int getGridSize() {
        return ((Integer) componentSettings.get("GridSize")).intValue();
    }

    public int getShadowWidth() {
        return ((Integer) componentSettings.get("ShadowWidth")).intValue();
    }

    public int getTextPos() {
        return ((Integer) componentSettings.get("TextPosition")).intValue();
    }

    public int getGraphFontSize() {
        return ((Integer) componentSettings.get("GraphFontSize")).intValue();
    }

    public int getActivityHeight() {
        return ((Integer) componentSettings.get("ActivityHeight")).intValue();
    }

    public int getActivityWidth() {
        return ((Integer) componentSettings.get("ActivityWidth")).intValue();
    }

    public int getRouteHeight() {
        return ((Integer) componentSettings.get("RouteHeight")).intValue();
    }

    public int getRouteWidth() {
        return ((Integer) componentSettings.get("RouteWidth")).intValue();
    }

    public int getMinParWidth() {
        return ((Integer) componentSettings.get("MinParWidth")).intValue();
    }

    public int getMinParHeight() {
        return ((Integer) componentSettings.get("MinParHeight")).intValue();
    }

    public int getParticipantNameWidth() {
        return ((Integer) componentSettings.get("ParticipantNameWidth")).intValue();
    }

    public Color getBubbleStartColor() {
        return (Color) componentSettings.get("BubbleStartColor");
    }

    public Color getBubbleEndColor() {
        return (Color) componentSettings.get("BubbleEndColor");
    }

    //CUSTOM
    /**
     * Get Bubble start connection color
     */
    public Color getBubbleConectionColor() {
        return (Color) componentSettings.get("BubbleStartConnectionColor");
    }

    public Color getBubbleEndConnectionColor() {
        return (Color) componentSettings.get("BubbleEndConnectionColor");
    }
    //END CUSTOM

    public Color getSelectedActivityColor() {
        return (Color) componentSettings.get("ActivitySelectedColor");
    }

    public Color getStartActivityColor() {
        return (Color) componentSettings.get("StartActivityColor");
    }

    public Color getEndActivityColor() {
        return (Color) componentSettings.get("EndActivityColor");
    }

    public Color getStartEndActivityColor() {
        return (Color) componentSettings.get("StartEndActivityColor");
    }

    public Color getBackgroundColor() {
        return (Color) componentSettings.get("BackgroundColor");
    }

    public Color getGridColor() {
        return (Color) componentSettings.get("GridColor");
    }

    public Color getTextColor() {
        return (Color) componentSettings.get("TextColor");
    }

    public Color getParticipantBorderColor() {
        return (Color) componentSettings.get("ParticipantBorderColor");
    }

    public Color getParticipantFreeTextExpressionColor() {
        return (Color) componentSettings.get("ParticipantFreeTextExpressionColor");
    }

    public Color getParticipantCommonExpressionColor() {
        return (Color) componentSettings.get("ParticipantCommonExpressionColor");
    }

    public Color getHandleColor() {
        return (Color) componentSettings.get("HandleColor");
    }

    public Color getMarqueeColor() {
        return (Color) componentSettings.get("MarqueeColor");
    }

    public String historyManagerClass() {
        return (String) componentSettings.get("Graph.HistoryManager.Class");
    }

    public int historySize() {
        return ((Integer) componentSettings.get("Graph.HistorySize")).intValue();
    }

    public String overviewClass() {
        return (String) componentSettings.get("GraphOverview.Class");
    }

    public boolean performAutomaticLayoutOnInsertion() {
        return false; /*enable layout saving*/
    }
}
