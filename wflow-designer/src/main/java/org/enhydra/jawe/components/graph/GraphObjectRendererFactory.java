package org.enhydra.jawe.components.graph;

import java.util.Properties;

import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Transition;

/**
 * Factory for rendering graph objects.
 * @author Sasa Bojanic
 */
public class GraphObjectRendererFactory {

    protected Properties properties;

    public void configure(Properties props) throws Exception {
        this.properties = props;
    }

    public GraphActivityRendererInterface createActivityRenderer(Activity act) {
        //CUSTOM
        int actType = act.getActivityType();
        if (actType == XPDLConstants.ACTIVITY_TYPE_ROUTE) {
            return new DefaultGraphRouteRenderer();
        } else {
            return new DefaultGraphActivityRenderer();
        }
        //CUSTOM
    }

    public GraphTransitionRendererInterface createTransitionRenderer(Transition tra) {
        return new DefaultGraphTransitionRenderer();
    }

    public GraphParticipantRendererInterface createParticipantRenderer(Participant par) {
        return new DefaultGraphParticipantRenderer();
    }

    public GraphActivityRendererInterface createBubbleRenderer(String type) {
        return new DefaultGraphBubbleActivityRenderer();
    }

    public GraphPortRendererInterface createPortRenderer(String type) {
        return new DefaultGraphPortRenderer();
    }
}
