package org.enhydra.jawe.components.graph;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

/**
 * Class used to display end object.
 */
public class DefaultGraphBubbleActivityRenderer extends VertexRenderer implements GraphActivityRendererInterface {

    private BasicStroke borderStroke = new BasicStroke(1);

    /**
     * Paints End. Overrides super class paint
     * to add specific painting.
     */
    public void paint(Graphics g) {

        //CUSTOM
        if (!((GraphBubbleActivityInterface) view.getCell()).isStart()) {
            setIcon(GraphUtilities.getGraphController().getGraphSettings().getBubbleGraphEndIcon());
        }

        setText(null);

        int b = borderWidth;
        Graphics2D g2 = (Graphics2D) g;
        Object AntiAlias = RenderingHints.VALUE_ANTIALIAS_ON;//Harald Meister
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, AntiAlias);//Harald Meister
        Dimension d = getSize();
        boolean tmp = selected;

        if (super.isOpaque()) {
            //CUSTOM
            if (((GraphBubbleActivityInterface) view.getCell()).isStart()) {
                g.setColor(GraphUtilities.getGraphController().getGraphSettings().getBubbleStartColor());
                g.fillOval(b - 1, b - 1, d.height - b, d.height - b);
            } else {
                g.setColor(GraphUtilities.getGraphController().getGraphSettings().getBubbleEndColor());
                g.fillOval(b - 1, b - 1, d.height - b, d.height - b);
            }
            //END CUSTOM
        }
        try {
            setBorder(null);
            setOpaque(false);
            selected = false;
            super.paint(g);
        } finally {
            selected = tmp;
        }

        if (((GraphBubbleActivityInterface) view.getCell()).isStart()) {
            g.setColor(GraphUtilities.getGraphController().getGraphSettings().getBubbleConectionColor());
            g2.setStroke(borderStroke);
            g.drawOval(b - 1, b - 1, d.height - b, d.height - b);
        } else {
            g.setColor(GraphUtilities.getGraphController().getGraphSettings().getBubbleEndConnectionColor());
            g2.setStroke(borderStroke);
            g.drawOval(b - 1, b - 1, d.height - b, d.height - b);
        }

        //END CUSTOM

        if (selected) {
            g2.setStroke(GraphConstants.SELECTION_STROKE);
            g.setColor(highlightColor);
            g.drawOval(b - 1, b - 1, d.height - b, d.height - b);
        }
    }

    public Point2D getPerimeterPoint(VertexView pView, Point2D p) {
        Rectangle2D bounds = pView.getBounds();
        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        double xCenter = x + width / 2;
        double yCenter = y + height / 2;
        double dx = p.getX() - xCenter; // Compute Angle
        double dy = p.getY() - yCenter;
        double alpha = Math.atan2(dy, dx);
        double xout = 0, yout = 0;
        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double beta = pi2 - alpha;
        double t = Math.atan2(height, width);
        if (alpha < -pi + t || alpha > pi - t) { // Left edge
            xout = x;
            yout = yCenter - width * Math.tan(alpha) / 2;
        } else if (alpha < -t) { // Top Edge
            yout = y;
            xout = xCenter - height * Math.tan(beta) / 2;
        } else if (alpha < t) { // Right Edge
            xout = x + width;
            yout = yCenter + width * Math.tan(alpha) / 2;
        } else { // Bottom Edge
            yout = y + height;
            xout = xCenter + height * Math.tan(beta) / 2;
        }
        return new Point2D.Double(xout, yout);
    }
}
