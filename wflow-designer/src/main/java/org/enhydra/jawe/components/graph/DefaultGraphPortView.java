package org.enhydra.jawe.components.graph;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;

/**
 * Represents a view for a Port object.
 *
 * @author Sasa Bojanic
 */
public class DefaultGraphPortView extends GraphPortViewInterface {

    protected static Map renderers = new HashMap();

    public DefaultGraphPortView(Object cell) {
        super(cell);
        AttributeMap map = new AttributeMap();
        // CUSTOM: larger area for mouse cursor detection to improve usability when adding transitions, changed from 30,30 to 130,130
        GraphConstants.setSize(map, new Dimension(130, 130));
        // END CUSTOM
        super.setAttributes(map);
    }

    /**
     * Sets size of all ports to given value.
     */
    public void setPortSize(Dimension d) {
        if (SIZE < 2) {
            SIZE = 2;
        }
        AttributeMap map = new AttributeMap();
        GraphConstants.setSize(map, d);
        super.setAttributes(map);
    }

    /**
     * Returns port's size.
     */
    public Dimension getGraphPortSize() {
        return (Dimension) getAttributes().get(GraphConstants.SIZE);
    }

    public GraphActivityInterface getGraphActivity() {
        return (GraphActivityInterface) getParentView().getCell();
    }

    public CellViewRenderer getRenderer() {
        String type = ((GraphPortInterface) super.getCell()).getType();
        GraphPortRendererInterface gprenderer = (GraphPortRendererInterface) renderers.get(type);
        if (gprenderer == null) {
            gprenderer = createRenderer(type);
            renderers.put(type, gprenderer);
        }
        return gprenderer;
    }

    public Rectangle2D getBounds() {
        AttributeMap map = new AttributeMap();
        Rectangle2D bounds = map.createRect(getLocation());
        bounds.setFrame(
                bounds.getX() - getGraphPortSize().width / 2,
                bounds.getY() - getGraphPortSize().height / 2,
                bounds.getWidth() + getGraphPortSize().width,
                bounds.getHeight() + getGraphPortSize().height);
        return bounds;
    }

    protected GraphPortRendererInterface createRenderer(String type) {
        return GraphUtilities.getGraphController().getGraphObjectRendererFactory().createPortRenderer(type);
    }
}
