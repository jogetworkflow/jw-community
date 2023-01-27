package org.enhydra.jawe.components.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Transition;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphConstants;

/**
 * Factory for generating graph objects.
 * @author Sasa Bojanic
 */
public class GraphObjectFactory {

    protected Properties properties;

    public void configure(Properties props) throws Exception {
        this.properties = props;
    }

    public GraphActivityInterface createActivity(Map viewMap, Activity act, Point partPoint) {
        String type = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(act).getTypeId();
        Point offset = GraphUtilities.getOffsetPoint(act);
        GraphActivityInterface gact = createActivityCell(act, type);

        Map m = initActivityProperties(partPoint, offset, act, type);

        viewMap.put(gact, m);
        return gact;
    }

    protected GraphActivityInterface createActivityCell(Activity act, String type) {
        return new DefaultGraphActivity(act);
    }

    protected Map initActivityProperties(Point partPoint, Point offset, Activity act, String type) {
        AttributeMap map = new AttributeMap();

        //CUSTOM
        String displayName = act.getName();
        int actW = GraphUtilities.getGraphController().getGraphSettings().getActivityWidth();
        int actH = GraphUtilities.getGraphController().getGraphSettings().getActivityHeight();
        Dimension dim = DefaultGraphActivityRenderer.calculateWidthAndHeight(displayName, actW, actH);
        actW = (int)dim.getWidth();
        actH = (int)dim.getHeight();
        Rectangle bounds = new Rectangle(partPoint.x + offset.x, partPoint.y + offset.y, actW, actH);
        if (type.equals("ACTIVITY_ROUTE")) {
            bounds = new Rectangle(partPoint.x + offset.x, partPoint.y + offset.y, GraphUtilities.getGraphController().getGraphSettings().getRouteWidth(), GraphUtilities.getGraphController().getGraphSettings().getRouteHeight());
        }
        //CUSTOM

        GraphConstants.setBounds(map, bounds);
        GraphConstants.setOpaque(map, true);
        GraphConstants.setBorderColor(map, Color.darkGray);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }
        return map;
    }

    public GraphBubbleActivityInterface createStart(Map viewMap, ExtendedAttribute sea, Point partPoint) {
        GraphBubbleActivityInterface gact = createStartCell(sea);

        Map m = initStartProperties(partPoint, sea);

        viewMap.put(gact, m);
        return gact;
    }

    protected GraphBubbleActivityInterface createStartCell(ExtendedAttribute sea) {
        return new DefaultGraphBubbleActivity(sea);
    }

    protected Map initStartProperties(Point partPoint, ExtendedAttribute sea) {
        AttributeMap map = new AttributeMap();
        StartEndDescription sed = new StartEndDescription(sea);
        Rectangle bounds = new Rectangle(
                partPoint.x + sed.getOffset().x,
                partPoint.y + sed.getOffset().y,
                GraphUtilities.getGraphController().getGraphSettings().getActivityHeight() / 5 * 3,
                GraphUtilities.getGraphController().getGraphSettings().getActivityHeight() / 5 * 3);
        GraphConstants.setBounds(map, bounds);
        GraphConstants.setOpaque(map, true);
        GraphConstants.setBorderColor(map, Color.darkGray);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }
        return map;
    }

    public GraphBubbleActivityInterface createEnd(Map viewMap, ExtendedAttribute eea, Point partPoint) {
        GraphBubbleActivityInterface gact = createEndCell(eea);

        Map m = initEndProperties(partPoint, eea);

        viewMap.put(gact, m);
        return gact;
    }

    protected GraphBubbleActivityInterface createEndCell(ExtendedAttribute eea) {
        return new DefaultGraphBubbleActivity(eea);
    }

    protected Map initEndProperties(Point partPoint, ExtendedAttribute eea) {
        AttributeMap map = new AttributeMap();
        StartEndDescription sed = new StartEndDescription(eea);
        Rectangle bounds = new Rectangle(
                partPoint.x + sed.getOffset().x,
                partPoint.y + sed.getOffset().y,
                GraphUtilities.getGraphController().getGraphSettings().getActivityHeight() / 5 * 3,
                GraphUtilities.getGraphController().getGraphSettings().getActivityHeight() / 5 * 3);
        GraphConstants.setBounds(map, bounds);
        GraphConstants.setOpaque(map, true);
        GraphConstants.setBorderColor(map, Color.darkGray);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }
        return map;
    }

    public GraphParticipantInterface createParticipant(Rectangle bounds, Map viewMap, Participant par) {
        GraphParticipantInterface gpar = createParticipantCell(par);

        Map m = initParticipantProperties(bounds, par);

        viewMap.put(gpar, m);
        return gpar;
    }

    protected GraphParticipantInterface createParticipantCell(Participant par) {
        return new DefaultGraphParticipant(par);
    }

    protected Map initParticipantProperties(Rectangle bounds, Participant par) {
        AttributeMap map = new AttributeMap();
        GraphConstants.setBounds(map, bounds);
        GraphConstants.setOpaque(map, false);
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setMoveable(map, false);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }
        return map;
    }

    public GraphTransitionInterface createTransition(List points, Map viewMap, Transition tra) {
        GraphTransitionInterface gtra = createTransitionCell(tra);

        Map m = initTransitionProperties(points, tra);

        viewMap.put(gtra, m);

        return gtra;
    }

    public GraphTransitionInterface createBubbleTransition(List points, Map viewMap, String style) {
        GraphTransitionInterface gtra = createTransitionCell(null);

        // CUSTOM: set orthogonal transition type
        style = GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_ORTHOGONAL;
        // END CUSTOM
        Map m = initBubbleTransitionProperties(points, style);

        viewMap.put(gtra, m);

        return gtra;
    }

    protected GraphTransitionInterface createTransitionCell(Transition tra) {
        return new DefaultGraphTransition(tra);
    }

    protected Map initTransitionProperties(List points, Transition tra) {
        AttributeMap map = new AttributeMap();
//if (points!=null && points.size()>0) System.out.println("Setting points "+points);
        GraphConstants.setPoints(map, points);
        setTransitionStyle(GraphUtilities.getStyle(tra), map);

        //GraphConstants.setLineColor(map,Utils.getColor(JaWEConfig.getInstance().getTransitionColor()));
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setEndFill(map, true);
        GraphConstants.setEndSize(map, 10);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }

        return map;
    }

    protected Map initBubbleTransitionProperties(List points, String style) {
        AttributeMap map = new AttributeMap();
//if (points!=null && points.size()>0) System.out.println("Setting points "+points);
        GraphConstants.setPoints(map, points);
        setTransitionStyle(style, map);
        //GraphConstants.setLineColor(map,Utils.getColor(JaWEConfig.getInstance().getTransitionColor()));
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setEndFill(map, true);
        GraphConstants.setEndSize(map, 10);
        String fntn = JaWEManager.getFontName();
        int fntsize = GraphUtilities.getGraphController().getGraphSettings().getGraphFontSize();
        javax.swing.plaf.FontUIResource f;
        try {
            try {
                f = new javax.swing.plaf.FontUIResource(fntn, Font.PLAIN, fntsize);
            } catch (Exception ex) {
                f = new javax.swing.plaf.FontUIResource("Label.font", Font.PLAIN, fntsize);
            }
            GraphConstants.setFont(map, f);
        } catch (Exception ex) {
        }

        return map;
    }

    protected void setTransitionStyle(String style, AttributeMap map) {
        if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_BEZIER)) {
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
        } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_SPLINE)) {
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
        } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_BEZIER)) {
            GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_BEZIER);
        } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_ORTHOGONAL)) {
            GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_ORTHOGONAL);
        } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_SPLINE)) {
            GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_SPLINE);
        } else {
            GraphConstants.setLineStyle(map, GraphConstants.STYLE_ORTHOGONAL);
        }
    }

    public GraphPortInterface createPort(String name, String type) {
        GraphPortInterface gpor = createPortCell(name, type);
        return gpor;
    }

    protected GraphPortInterface createPortCell(String name, String type) {
        return new DefaultGraphPort(name, type);
    }

    protected Map initPortProperties(String type) {
        AttributeMap map = new AttributeMap();
        return map;
    }
}
