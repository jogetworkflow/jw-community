package org.enhydra.jawe.components.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.base.controller.JaWETypes;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;

public class DefaultGraphRouteRenderer extends MultiLinedRenderer implements
        GraphActivityRendererInterface {

    protected static int arc = 10; //5;

    /**
     * Paints activity. Overrides super class paint to add specific painting. First it
     * fills inner with color. Then it adds specific drawing for join type. Then it apply
     * JPanel with name and icon. At the end it draws shadow and border
     */
    public void paint(Graphics g) {
        int actW = GraphUtilities.getGraphController().getGraphSettings().getRouteWidth();
        int actH = GraphUtilities.getGraphController().getGraphSettings().getRouteHeight();
        int shadowWidth = GraphUtilities.getGraphController().getGraphSettings().getShadowWidth();

        GraphActivityInterface gact = (GraphActivityInterface) view.getCell();
        Activity act = (Activity) gact.getUserObject();

        g.setColor(bordercolor);
        boolean inclusive = act.isAndTypeJoin() || act.isAndTypeSplit();
        String gatewayImage = (inclusive) ? "/org/enhydra/jawe/images/gateway_inclusive.gif" : "/org/enhydra/jawe/images/gateway_exclusive.gif";
        g.drawImage(new ImageIcon(getClass().getResource(gatewayImage)).getImage(), 10, 0, null);
        if (selected) {
            // draw border
            g.setColor(Color.LIGHT_GRAY);
            ((Graphics2D) g).setStroke(borderStroke);
            g.drawRoundRect(0, 0, actW - 1 - shadowWidth, actH - 1 - shadowWidth + 2, arc, arc);
        }
    }

    protected Color getFillColor() {
        Activity act = (Activity) ((GraphActivityInterface) view.getCell()).getUserObject();
        Color c = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(act).getColor();
        GraphSettings gv = GraphUtilities.getGraphController().getGraphSettings();
        if (!gv.shouldUseBubbles()) {
            boolean isStartingAct = JaWEManager.getInstance().getXPDLUtils().isStartingActivity(act);
            boolean isEndingAct = JaWEManager.getInstance().getXPDLUtils().isEndingActivity(act);
            if (isStartingAct && isEndingAct) {
                c = gv.getStartEndActivityColor();
            } else if (isStartingAct) {
                c = gv.getStartActivityColor();
            } else if (isEndingAct) {
                c = gv.getEndActivityColor();
            }
        }
        return c;
    }

    public ImageIcon getIcon() {
        Activity act = (Activity) ((GraphActivityInterface) view.getCell()).getUserObject();

        String icon = act.getIcon().replaceAll(".gif", "graph.gif");

        ImageIcon ii = null;
        if (!icon.equals("")) {
            ii = (ImageIcon) Utils.getOriginalActivityIconsMap().get(icon);
        }

        if (ii == null) {
            ImageIcon imageIcon = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(act).getIcon();

            if (isDefaultImage(JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(act).getDisplayName())) {
                ii = imageIcon;
            } else {

                String imagePath = imageIcon.toString().replaceAll(".gif", "graph.gif");
                if (imagePath.contains("!")) {
                    imagePath = imagePath.substring(imagePath.indexOf("!") + 2, imagePath.length());
                }
                if (imagePath.contains("target/classes")) {
                    imagePath = imagePath.substring(imagePath.indexOf("target/classes") + 15, imagePath.length());
                }

                //CUSTOM
                try {
                    ii = new ImageIcon(ResourceManager.class.getClassLoader().getResource(imagePath));
                } catch (NullPointerException e) {
                    // ignore
                    ii = imageIcon;
                }
                //END CUSTOM
            }
        }

        return ii;
    }

    public boolean isDefaultImage(String displayName) {
        return displayName.equals("Route");
    }
}
