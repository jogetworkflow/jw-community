package org.enhydra.jawe.components.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Utils;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

public class DefaultGraphActivityRenderer extends MultiLinedRenderer implements
        GraphActivityRendererInterface {

    protected static int arc = 10; //5;

    public static Dimension calculateWidthAndHeight(String label, int defaultWidth, int defaultHeight) {
        int maxWidth = 150;
        if (label != null && label.length() > 30) {
            defaultWidth += (label.length() / 10) * 10;
            if (defaultWidth > maxWidth) {
                defaultWidth = maxWidth;
            }
            defaultHeight = (int)(defaultWidth * 0.6);
        }
        Dimension dim = new Dimension(defaultWidth, defaultHeight);
        return dim;
    }

    /**
     * Paints activity. Overrides super class paint to add specific painting. First it
     * fills inner with color. Then it adds specific drawing for join type. Then it apply
     * JPanel with name and icon. At the end it draws shadow and border
     */
    public void paint(Graphics g) {
        int actW = GraphUtilities.getGraphController().getGraphSettings().getActivityWidth();
        int actH = GraphUtilities.getGraphController().getGraphSettings().getActivityHeight();
        int shadowWidth = GraphUtilities.getGraphController().getGraphSettings().getShadowWidth();
        boolean showShadow = GraphUtilities.getGraphController().getGraphSettings().isShadowEnabled();

        GraphActivityInterface gact = (GraphActivityInterface) view.getCell();
        Activity act = (Activity) gact.getUserObject();
        boolean frontJoin = false;
        if (XMLUtil.isANDTypeSplitOrJoin(act, 1)) {
            frontJoin = true;
        }
        boolean backJoin = false;
        if (XMLUtil.isANDTypeSplitOrJoin(act, 0)) {
            backJoin = true;
        }

        Color bckgC = getFillColor();
        if (selected) {
            bckgC = GraphUtilities.getGraphController().getGraphSettings().getSelectedActivityColor();
        }

        // CUSTOM
        String displayName = getDisplayName();
        Dimension dim = DefaultGraphActivityRenderer.calculateWidthAndHeight(displayName, actW, actH);
        actW = (int)dim.getWidth();
        actH = (int)dim.getHeight();
        int actType = act.getActivityType();
        if (actType == XPDLConstants.ACTIVITY_TYPE_ROUTE) {
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
            return;
        } else {
            Rectangle tempClipRect = new Rectangle(0, 0, actW, actH);
            g.setClip(tempClipRect);
        }
        // CUSTOM

        // fill activity
        g.setColor(bckgC);
        g.fillRoundRect(0, 0, actW - shadowWidth, actH - shadowWidth, arc, arc);

        // drawing panel
        super.setOpaque(false);
        Graphics gl = g.create(5, 5, actW - 9 - shadowWidth, actH - 9 - shadowWidth);
        Rectangle panelRect = new Rectangle(0, 0, actW - 9 - shadowWidth, actH - 9 - shadowWidth);
        super.setBounds(panelRect);
        graph.setHighlightColor(bckgC);
        setBorder(BorderFactory.createLineBorder(bckgC, 0));
        super.paint(gl);
        setBorder(BorderFactory.createLineBorder(bordercolor, 0));
        setForeground(bordercolor);

        // display limit
        String limit = act.getLimit();
        if (limit != null && limit.trim().length() > 0) {
            WorkflowProcess process = (WorkflowProcess)act.getParent().getParent();
            String durationUnit = process.getProcessHeader().getDurationUnit();
            if (durationUnit == null) {
                durationUnit = "";
            } else {
                durationUnit = durationUnit.toLowerCase();
            }
            g.setColor(Color.GRAY);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
            g.drawString(limit + durationUnit, 5, 35);
        }

        // shadow
        if (showShadow) {
            g.setColor(new Color(192, 192, 192));
            ((Graphics2D) g).setStroke(new BasicStroke(shadowWidth,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND));
            g.drawLine(shadowWidth, actH - shadowWidth, actW - shadowWidth, actH - shadowWidth);
            if (!backJoin) {
                g.drawLine(actW - shadowWidth,
                        actH - shadowWidth,
                        actW - shadowWidth,
                        shadowWidth);
            }
        }

        // draw border
        g.setColor(bordercolor);
        ((Graphics2D) g).setStroke(borderStroke);
        g.drawRoundRect(0, 0, actW - 1 - shadowWidth, actH - 1 - shadowWidth, arc, arc);

        // add > to front
        Color gCol = GraphUtilities.getGraphController().getGraphSettings().getBackgroundColor();
        if (frontJoin) {
            g.setColor(gCol);
            int[] x = {
                0, 4, 0
            };
            int[] y = {
                arc, actH / 2, actH - arc
            };
            g.fillPolygon(x, y, 3);
            g.setColor(bordercolor);
            ((Graphics2D) g).setStroke(borderStroke);
            g.drawLine(x[0], y[0], x[1], y[1]);
            g.drawLine(x[1], y[1], x[2], y[2]);
        }
        // add > to back
        if (backJoin) {
            g.setColor(gCol);
            // clean
            int[] x = {
                actW - shadowWidth - 4,
                actW,
                actW,
                actW - shadowWidth - 4,
                actW - shadowWidth
            };
            int[] y = {
                0, 0, actH, actH, actH / 2
            };
            g.fillPolygon(x, y, 5);
            g.setColor(new Color(192, 192, 192));
            ((Graphics2D) g).setStroke(new BasicStroke(shadowWidth,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_ROUND));
            g.drawLine(x[0] + 1, y[0], x[4] + 1, y[4]);
            g.drawLine(x[4] + 1, y[4], x[3] + 1, y[3] - shadowWidth);
            g.setColor(bordercolor);
            ((Graphics2D) g).setStroke(borderStroke);
            g.drawLine(x[0], y[0], x[4], y[4]);
            g.drawLine(x[4], y[4], x[3], y[3] - shadowWidth);
        }

        int type = act.getActivityType();
        if (type == XPDLConstants.ACTIVITY_TYPE_BLOCK && GraphUtilities.getGraphController().getGraphSettings().shouldDrawBlockLines()) {
            g.setColor(bordercolor);
            g.drawLine(3, 0, 3, actH - 2 - shadowWidth);
            g.drawLine(actW - 4 - shadowWidth, 0, actW - 4 - shadowWidth, actH - 2 - shadowWidth);
        } else if (type == XPDLConstants.ACTIVITY_TYPE_SUBFLOW && GraphUtilities.getGraphController().getGraphSettings().shouldDrawSubflowLines()) {
            g.setColor(bordercolor);
            ((Graphics2D) g).setStroke(borderStroke);
            g.drawRect(3, 3, actW - 7 - shadowWidth, actH - 7 - shadowWidth);

            //CUSTOM
            g.drawImage(new ImageIcon(getClass().getResource("/org/enhydra/jawe/images/subflowsmall.gif")).getImage(), actW / 2 - 7, actH - 20, null);
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
