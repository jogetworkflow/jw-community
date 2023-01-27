package org.enhydra.jawe.components.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.JaWE;
import org.enhydra.jawe.JaWEConstants;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.controller.JaWEFrame;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Participants;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.Transitions;
import org.jgraph.JGraph;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

/**
 * Implementation of a marquee handler for Process Editor. This is also a place
 * where (after mouse click or release) participants, activities (normal, subflows,
 * block activities) and transitions are inserted, where persistent mode is achived and
 * where mouse cursors are changing, and where popup menu is implemented. When
 * inserting cells it calls WorkflowManager.
 */
public class GraphMarqueeHandler extends BasicMarqueeHandler {

    protected Point start;                                                                    // Starting point (where mouse was pressed).
    protected Point current;                                                              // Current point (where mouse was dragged).
    protected GraphPortViewInterface port;                             // Current port (when mouse was pressed).
    protected GraphPortViewInterface firstPort;                    // First port (when mouse was pressed).
    protected GraphPortViewInterface lastPort;                     // Last port (when mouse was dragged).
    protected Vector points = new Vector();
    protected Point popupPoint;                                                     // A point where popup window has been created last time
    protected GraphController graphController;
    protected String mainType = GraphEAConstants.SELECT_TYPE;
    protected String subType = GraphEAConstants.SELECT_TYPE_DEFAULT;

    /**
     * Creates custom marquee handler.
     */
    public GraphMarqueeHandler(GraphController graphController) {
        this.graphController = graphController;
    }

    /** Return true if this handler should be preferred over other handlers. */
    public boolean isForceMarqueeEvent(MouseEvent e) {
        boolean isSelectButtonSelected = isSelectButtonSelected();
        return ((isSelectButtonSelected && SwingUtilities.isRightMouseButton(e)) ||
                !isSelectButtonSelected || super.isForceMarqueeEvent(e));
    }

    /**
     *  We don't want special cursor
     */
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        marqueeBounds = new Rectangle2D.Double(startPoint.getX(), startPoint.getY(), 0, 0);
        if (e != null) {
            if (!(e.getSource() instanceof JGraph)) {
                throw new IllegalArgumentException(
                        "MarqueeHandler cannot handle event from unknown source: " + e);
            }
        }
    }

    public void mouseReleased(MouseEvent ev) {
        try {
            if (ev != null && marqueeBounds != null) {
                Rectangle2D bounds = getGraph().fromScreen(marqueeBounds);// HM, JGraph3.4.1
                CellView[] rootViews = getGraph().getGraphLayoutCache().getRoots(bounds);

                // added - getting all views in model (except forbidden objects)
                CellView[] views = AbstractCellView.getDescendantViews(rootViews);
                ArrayList wholeList = new ArrayList();
                ArrayList participantList = new ArrayList();
                ArrayList otherList = new ArrayList();
                for (int i = 0; i < views.length; i++) {
                    if (bounds.contains(views[i].getBounds())) {
                        if (views[i].getCell() instanceof DefaultGraphCell && !(((DefaultGraphCell) views[i].getCell()).getUserObject() instanceof Participant)) {
                            otherList.add(views[i].getCell());
                        } else {
                            participantList.add(views[i].getCell());
                        }
                        wholeList.add(views[i].getCell());
                    }
                }

                Object[] cells = wholeList.toArray();

                getGraph().getUI().selectCellsForEvent(getGraph(), cells, ev);
                Rectangle dirty = marqueeBounds.getBounds();// HM, JGraph3.4.1
                dirty.width++;
                dirty.height++;// HM, JGraph3.4.1
                getGraph().repaint(dirty);
            }
        } finally {
            currentPoint = null;
            startPoint = null;
            marqueeBounds = null;
        }
    }

    /**
     * Creates popup menu and adds a various actions (depending of where
     * mouse was pressed - which cell(s) is/are selected).
     */
    protected JPopupMenu createPopupMenu(final Object cell) {

        boolean isWorkflowElement = (cell instanceof WorkflowElement);

        String type = null;
        String subtype = null;
        String userSpec = null;
        if (isWorkflowElement) {
            XMLElement el = (XMLElement) ((DefaultGraphCell) cell).getUserObject();
            if (cell instanceof GraphActivityInterface && !(cell instanceof GraphBubbleActivityInterface)) {
                type = JaWEConstants.ACTIVITY_TYPE;
                subtype = Utils.getActivityStringType(((Activity) el).getActivityType());
                userSpec = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(el).getTypeId();
            }
            if (cell instanceof GraphParticipantInterface) {
                type = JaWEConstants.PARTICIPANT_TYPE;
                subtype = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(el).getTypeId();
            }
            if (cell instanceof GraphTransitionInterface) {
                type = JaWEConstants.TRANSITION_TYPE;
                subtype = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(el).getTypeId();
            }
            if (cell instanceof GraphBubbleActivityInterface) {
                if (((GraphBubbleActivityInterface) cell).isStart()) {
                    type = GraphEAConstants.START_TYPE;
                } else {
                    type = GraphEAConstants.END_TYPE;
                }
                subtype = ((GraphBubbleActivityInterface) cell).getType();
            }
        } else {
            type = GraphEAConstants.SELECT_TYPE;
        }

        JPopupMenu retMenu = BarFactory.createPopupMenu(type, graphController);

        if (subtype != null) {
            JPopupMenu specMenu = BarFactory.createPopupMenu(subtype, graphController);

            Component[] spec = specMenu.getComponents();

            if (spec.length != 0) {
                retMenu.addSeparator();
            }

            for (int i = 0; i < spec.length; i++) {
                retMenu.add(spec[i]);
            }
        }

        if (subtype != null && userSpec != null && !subtype.equals(userSpec)) {
            JPopupMenu specMenu = BarFactory.createPopupMenu(userSpec, graphController);

            Component[] spec = specMenu.getComponents();

            if (spec.length != 0) {
                retMenu.addSeparator();
            }

            for (int i = 0; i < spec.length; i++) {
                retMenu.add(spec[i]);
            }
        }

        if (cell instanceof GraphParticipantInterface && ((WorkflowElement) cell).getPropertyObject() instanceof CommonExpressionParticipant) {
            JMenuItem se = BarFactory.createMenuItem(getGraphController().getSettings().getAction(("SetPerformerExpression")),
                    getGraphController(), false);
            retMenu.addSeparator();
            retMenu.add(se);
        }


        return retMenu;
    }

    /**
     * Gets the point of last popup menu creation.
     */
    public Point getPopupPoint() {
        return popupPoint;
    }

    public boolean validateSource(GraphPortViewInterface pPort) {
        //    if port is a valid
        if (pPort != null && pPort.getCell() != null // and it is a port
                && (pPort.getCell() instanceof GraphPortInterface)) {
            // return if it accepts to be a source or a target
            GraphActivityInterface sourceActivity = pPort.getGraphActivity();
            if (!sourceActivity.acceptsSource()) {
                return false;
            }

            boolean isExceptionalTrans = getSubType().equals(JaWEConstants.TRANSITION_TYPE_EXCEPTION);
            if (!(sourceActivity instanceof GraphBubbleActivityInterface) &&
                    !JaWEManager.getInstance().getTransitionHandler().acceptsSource((Activity) sourceActivity.getUserObject(), isExceptionalTrans)) {
                JOptionPane.showMessageDialog(getJaWEFrame(), getGraphController().getSettings().getLanguageDependentString(
                        "WarningCannotAcceptMoreOutgoingTransitions"), getJaWEFrame().getAppTitle(),
                        JOptionPane.INFORMATION_MESSAGE);

                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> if parent cell of given port accepts source or target, depending
     * on <code>source</code> parameter.
     */
    public boolean validateConnection(GraphPortViewInterface pFirstPort, GraphPortViewInterface pSecondPort, Transition t) {
        // if ports are valid
        if (pFirstPort != null && pFirstPort.getCell() != null && (pFirstPort.getCell() instanceof GraphPortInterface) &&
                pSecondPort != null && pSecondPort.getCell() != null && (pSecondPort.getCell() instanceof GraphPortInterface)) {
            // return if it accepts to be a source or a target
            GraphActivityInterface sourceActivity = pFirstPort.getGraphActivity();
            GraphActivityInterface targetActivity = pSecondPort.getGraphActivity();
            if (!targetActivity.acceptsTarget()) {
                return false;
            }

            boolean isExceptionalTrans = XMLUtil.isExceptionalTransition(t) || getSubType().equals(JaWEConstants.TRANSITION_TYPE_EXCEPTION);
            // do not allow start-end connection
            if ((sourceActivity instanceof GraphBubbleActivityInterface) && (targetActivity instanceof GraphBubbleActivityInterface)) {
                JOptionPane.showMessageDialog(getJaWEFrame(),
                        getGraphController().getSettings().getLanguageDependentString("ErrorCannotConnectStartAndEnd"),
                        getJaWEFrame().getAppTitle(), JOptionPane.ERROR_MESSAGE);
                return false;
            }

            Set targetActIncomingTrans = new HashSet();
            Set targetActNonExcTrans = new HashSet();
            Set targetActTrans = new HashSet();
            Set sourceActNonExcTrans = new HashSet();
            if (!(targetActivity instanceof GraphBubbleActivityInterface)) {
                targetActIncomingTrans.addAll(XMLUtil.getIncomingTransitions((Activity) targetActivity.getPropertyObject()));
                targetActTrans.addAll(XMLUtil.getOutgoingTransitions((Activity) targetActivity.getPropertyObject()));
                targetActNonExcTrans.addAll(XMLUtil.getNonExceptionalOutgoingTransitions((Activity) targetActivity.getPropertyObject()));
            }
            if (!(sourceActivity instanceof GraphBubbleActivityInterface)) {
                sourceActNonExcTrans.addAll(XMLUtil.getNonExceptionalOutgoingTransitions((Activity) sourceActivity.getPropertyObject()));
            }
            // if target is end bubble, do not allow connection if source is already connected
            // to another end bubble, or to some other activity except itself or if transition is exceptional one
            if (targetActivity instanceof GraphBubbleActivityInterface) { // must be end bubble in that case
                if (GraphManager.hasConnectedEndBubble(sourceActivity) ||
                        !(sourceActNonExcTrans.size() == 0 || (sourceActNonExcTrans.size() == 1 && JaWEManager.getInstance().getXPDLUtils().hasCircularTransitions(sourceActNonExcTrans)))) {
                    JOptionPane.showMessageDialog(getJaWEFrame(),
                            getGraphController().getSettings().getLanguageDependentString(
                            "ErrorEndingActivityCannotHaveOutgoingTransitions"),
                            getJaWEFrame().getAppTitle(), JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                return true;
            }

            // if source is start bubble, do not allow connection if target is already connected
            // to another start bubble, or some other activity except itself
            if (sourceActivity instanceof GraphBubbleActivityInterface) { // must be start bubble in that case
                if (GraphManager.hasConnectedStartBubble(targetActivity) ||
                        !(targetActIncomingTrans.size() == 0 || (targetActIncomingTrans.size() == 1 && JaWEManager.getInstance().getXPDLUtils().hasCircularTransitions(targetActIncomingTrans)))) {
                    JOptionPane.showMessageDialog(getJaWEFrame(),
                            getGraphController().getSettings().getLanguageDependentString(
                            "ErrorStartingActivityCannotHaveIncomingTransitions"),
                            getJaWEFrame().getAppTitle(), JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (!JaWEManager.getInstance().getTransitionHandler().acceptsTarget((Activity) targetActivity.getUserObject())) {
                    JOptionPane.showMessageDialog(getJaWEFrame(), getGraphController().getSettings().getLanguageDependentString(
                            "WarningCannotAcceptMoreIncomingTransitions"), getJaWEFrame().getAppTitle(),
                            JOptionPane.WARNING_MESSAGE);

                    return false;
                }

                return true;
            }

            boolean circularTransition = (sourceActivity == targetActivity);
            if (GraphManager.hasConnectedStartBubble(targetActivity) && !circularTransition) {
                JOptionPane.showMessageDialog(getJaWEFrame(),
                        getGraphController().getSettings().getLanguageDependentString(
                        "ErrorStartingActivityCannotHaveIncomingTransitions"),
                        getJaWEFrame().getAppTitle(), JOptionPane.ERROR_MESSAGE);
                return false;
            }



            if (!(sourceActivity instanceof GraphBubbleActivityInterface)) {
                if (GraphManager.hasConnectedEndBubble(sourceActivity) && !(isExceptionalTrans || circularTransition)) {
                    JOptionPane.showMessageDialog(getJaWEFrame(), getGraphController().getSettings().getLanguageDependentString("ErrorEndingActivityCannotHaveOutgoingTransitions"), getJaWEFrame().getAppTitle(), JOptionPane.INFORMATION_MESSAGE);

                    return false;
                }
            }

            Activity a = (Activity) sourceActivity.getUserObject();
            Activity b = (Activity) targetActivity.getUserObject();
            List status = new ArrayList(1);

            if (!JaWEManager.getInstance().getTransitionHandler().allowsConnection(a, b, t, isExceptionalTrans, status)) {
                String errorMsg = "WarningSourceActivityCannotHaveMoreOutgoingTransitions";
                boolean isError = false;
                if (((Integer) status.get(0)).intValue() == 2) {
                    errorMsg = "WarningTargetActivityCannotHaveMoreIncomingTransitions";
                } else if (((Integer) status.get(0)).intValue() == 3) {
                    isError = true;
                    errorMsg = "ErrorActivityCannotHaveMoreThenOneIncomingOutgoingTransitionFromToTheSameActivity";
                }
                JOptionPane.showMessageDialog(getJaWEFrame(),
                        getGraphController().getSettings().getLanguageDependentString(errorMsg),
                        getJaWEFrame().getAppTitle(), isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            return true;
        }

        return false;
    }

    protected GraphController getGraphController() {
        return graphController;
    }

    protected Graph getGraph() {
        return getGraphController().getSelectedGraph();
    }

    protected GraphManager getGraphManager() {
        return getGraph().getGraphManager();
    }

    protected JaWEController getJaWEController() {
        return JaWEManager.getInstance().getJaWEController();
    }

    protected JaWEFrame getJaWEFrame() {
        return getJaWEController().getJaWEFrame();
    }

    public boolean isSelectButtonSelected() {
        return mainType.equals(GraphEAConstants.SELECT_TYPE);
    }

    protected boolean isParticipantButtonSelected() {
        return mainType.equals(JaWEConstants.PARTICIPANT_TYPE);
    }

    protected boolean isActivityButtonSelected() {
        return mainType.equals(JaWEConstants.ACTIVITY_TYPE);
    }

    public boolean isTransitionButtonSelected() {
        return mainType.equals(JaWEConstants.TRANSITION_TYPE);
    }

    protected boolean isStartButtonSelected() {
        return mainType.equals(GraphEAConstants.START_TYPE);
    }

    protected boolean isEndButtonSelected() {
        return mainType.equals(GraphEAConstants.END_TYPE);
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public int getStatus() {
        if (isSelectButtonSelected()) {
            return JaWEGraphUI.SELECTION;
        } else if (isParticipantButtonSelected()) {
            return JaWEGraphUI.INSERT_PARTICIPANT;
        } else if (isTransitionButtonSelected()) {
            return JaWEGraphUI.INSERT_TRANSITION_START;
        } else {
            return JaWEGraphUI.INSERT_ELEMENT;
        }
    }

    public void setSelectionMode() {
        mainType = GraphEAConstants.SELECT_TYPE;
        subType = GraphEAConstants.SELECT_TYPE_DEFAULT;

        getGraph().setCursor(Cursor.getDefaultCursor());
        reset();
    }

    public void reset() {
        firstPort = null;
        port = null;
        start = null;
        current = null;
        getGraph().repaint();

        ((JaWEGraphUI) getGraph().getUI()).reset();
    }

    public void popupMenu(Point pPopupPoint) {
        double scale = getGraph().getScale();
        Point p = new Point();
        p.setLocation(pPopupPoint.getX() / scale, pPopupPoint.getY() / scale);
        Object cell = getGraph().getFirstCellForLocation(p.x, p.y);
        // needed for addPoint, etc.
        this.popupPoint = new Point(p);
        JPopupMenu menu = createPopupMenu(cell);
        menu.show(getGraph(), (int) pPopupPoint.getX(), (int) pPopupPoint.getY());
    }

    public void insertParticipant() {
        graphController.setUpdateInProgress(true);
        Participant toInsert = null;

        //CUSTOM
        Participants pars = null;
        if (JaWE.BASIC_MODE) {
            pars = this.getJaWEController().getSelectionManager().getWorkingPKG().getParticipants();
        } else {
            pars = getGraph().getWorkflowProcess().getParticipants();
        }
        //END CUSTOM

        String idForVO = null;
        boolean insertIntoCollection = false;
        if (GraphEAConstants.PARTICIPANT_TYPE_FREE_TEXT_EXPRESSION.equals(subType)) {
            if (!getGraphManager().isFreeTextExpressionParticipantShown()) {
                toInsert = FreeTextExpressionParticipant.getInstance();
                idForVO = toInsert.getId();
                setSelectionMode();
            } else {
                return;
            }
        } else if (GraphEAConstants.PARTICIPANT_TYPE_COMMON_EXPRESSION.equals(subType)) {
            toInsert = CommonExpressionParticipants.getInstance().generateCommonExpressionParticipant(getGraph().getXPDLObject());
            idForVO = CommonExpressionParticipants.getInstance().getIdForVisualOrderEA(toInsert.getId());
        } else {
            toInsert = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(
                    pars, subType, false);
            idForVO = toInsert.getId();
            insertIntoCollection = true;
        }
        JaWEManager.getInstance().getJaWEController().startUndouableChange();
        if (insertIntoCollection) {
            pars.add(toInsert);
        }
        List vo = GraphUtilities.getParticipantVisualOrder(getGraph().getXPDLObject());
        vo.add(idForVO);
        GraphUtilities.setParticipantVisualOrder(getGraph().getXPDLObject(), vo);
        getGraphManager().insertParticipantAndArrangeParticipants(toInsert);
        List toSelect = new ArrayList();
        toSelect.add(toInsert);
        JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
        graphController.setUpdateInProgress(false);
        graphController.adjustActions();
    }

    public void insertSpecialElement() {
//      if (isActivitySetButtonSelected()) {
//         Graph g = getGraph();
//         if (g != null) {
//            JaWEManager.getInstance().getJaWEController().startUndouableChange();
//            ActivitySets ass=g.getWorkflowProcess().getActivitySets();
//            ActivitySet as=JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(ass,
//                  getSelectedButtonType(), true);
//            JaWEManager.getInstance().getJaWEController().endUndouableChange();
//         }
//      }
    }

    public void insertElement(Point whereTo) {
        // if activity is selected
        if (isActivityButtonSelected()) {
            if (!getGraphManager().doesRootParticipantExist()) {
                JaWEFrame frame = JaWEManager.getInstance().getJaWEController().getJaWEFrame();
                JOptionPane.showMessageDialog(frame, getGraphController().getSettings().getLanguageDependentString(
                        "WarningInvalidOperation"), frame.getAppTitle(),
                        JOptionPane.WARNING_MESSAGE);

            } else {
                GraphParticipantInterface gpar = getGraphManager().findParentActivityParticipantForLocation(whereTo, null,
                        null);
                Point partLoc = getGraphManager().getBounds(gpar, null).getBounds().getLocation();
                Point off = new Point(whereTo.x - partLoc.x, whereTo.y - partLoc.y);
                String partId = gpar.getPropertyObject().get("Id").toValue();
                Activities acts = (Activities) getGraph().getXPDLObject().get("Activities");
                Activity act = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(acts, subType, false);
                GraphUtilities.setOffsetPoint(act, off);
                GraphUtilities.setParticipantId(act, partId);
                int acttype = act.getActivityType();
                if (acttype == XPDLConstants.ACTIVITY_TYPE_NO || acttype == XPDLConstants.ACTIVITY_TYPE_TOOL) {
                    if (!partId.equals(FreeTextExpressionParticipant.getInstance().getId())) {
                        act.setPerformer(partId);
                    }
                }
                graphController.setUpdateInProgress(true);
                JaWEManager.getInstance().getJaWEController().startUndouableChange();
                acts.add(act);
                getGraphManager().insertActivity(act);
                List toSelect = new ArrayList();
                toSelect.add(act);
                JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                getGraph().selectActivity(act, false);
                graphController.setUpdateInProgress(false);
            }
        } // if start button is selected
        else if (isStartButtonSelected()) {
            if (!getGraphManager().doesRootParticipantExist()) {
                JaWEFrame frame = JaWEManager.getInstance().getJaWEController().getJaWEFrame();
                JOptionPane.showMessageDialog(frame, getGraphController().getSettings().getLanguageDependentString(
                        "WarningInvalidOperation"), frame.getAppTitle(),
                        JOptionPane.WARNING_MESSAGE);

            } else {
                GraphParticipantInterface gpar = getGraphManager().findParentActivityParticipantForLocation(whereTo, null,
                        null);
                Point partLoc = getGraphManager().getBounds(gpar, null).getBounds().getLocation();
                Point offset = new Point(whereTo.x - partLoc.x, whereTo.y - partLoc.y);
                String partId = gpar.getPropertyObject().get("Id").toValue();
                graphController.setUpdateInProgress(true);
                JaWEManager.getInstance().getJaWEController().startUndouableChange();
                ExtendedAttribute ea = GraphUtilities.createStartOrEndExtendedAttribute(getGraph().getXPDLObject(), true, partId, offset, subType,
                        true);
                getGraphManager().insertStart(ea);
                List toSelect = new ArrayList();
                toSelect.add(ea);
                JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                getGraph().selectBubble(ea, false);
                graphController.setUpdateInProgress(false);
            }
        }

        // if end button is selected
        if (isEndButtonSelected()) {
            if (!getGraphManager().doesRootParticipantExist()) {
                JaWEFrame frame = JaWEManager.getInstance().getJaWEController().getJaWEFrame();
                JOptionPane.showMessageDialog(frame, getGraphController().getSettings().getLanguageDependentString(
                        "WarningInvalidOperation"), frame.getAppTitle(),
                        JOptionPane.WARNING_MESSAGE);

            } else {
                GraphParticipantInterface gpar = getGraphManager().findParentActivityParticipantForLocation(whereTo, null,
                        null);
                Point partLoc = getGraphManager().getBounds(gpar, null).getBounds().getLocation();
                Point offset = new Point(whereTo.x - partLoc.x, whereTo.y - partLoc.y);
                String partId = gpar.getPropertyObject().get("Id").toValue();
                graphController.setUpdateInProgress(true);
                JaWEManager.getInstance().getJaWEController().startUndouableChange();
                ExtendedAttribute ea = GraphUtilities.createStartOrEndExtendedAttribute(getGraph().getXPDLObject(), false, partId, offset, subType,
                        true);
                getGraphManager().insertEnd(ea);
                List toSelect = new ArrayList();
                toSelect.add(ea);
                JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                getGraph().selectBubble(ea, false);
                graphController.setUpdateInProgress(false);
            }
        }
    }

    public boolean insertTransitionFirstPort(GraphPortViewInterface pPort) {
        if (pPort != null) {
            if (firstPort == null) {
                // start the transition only if start is valid
                if (validateSource(pPort)) {
                    points = new Vector();
                    firstPort = pPort;
                    double scale = getGraph().getScale();
                    start = firstPort.getBounds().getBounds().getLocation();// HM, JGraph3.4.1
                    start.x += firstPort.getGraphPortSize().width / 2;
                    start.y += firstPort.getGraphPortSize().height / 2;
                    start = new Point((int) (start.getX() * scale), (int) (start.getY() * scale));

                    return true;
                }
            }
        }

        return false;
    }

    public boolean insertTransitionSecondPort(GraphPortViewInterface pPort) {
        if (pPort != null) {
            // normal
            if (pPort != firstPort) {
                if (validateConnection(firstPort, pPort, null)) {
                    GraphActivityInterface s = ((GraphPortInterface) firstPort.getCell()).getActivity();
                    GraphActivityInterface t = ((GraphPortInterface) pPort.getCell()).getActivity();
                    XMLElement sxpdl = s.getPropertyObject();
                    XMLElement txpdl = t.getPropertyObject();
                    if (sxpdl instanceof Activity && txpdl instanceof Activity) {
                        Transitions tras = (Transitions) getGraph().getXPDLObject().get("Transitions");
                        String fromId = ((Activity) sxpdl).getId();
                        String toId = ((Activity) txpdl).getId();
                        Transition tra = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(tras, subType, false);
                        tra.setFrom(fromId);
                        tra.setTo(toId);
                        if (fromId.equals(toId)) {
                            GraphUtilities.setStyle(tra, GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_BEZIER);
                        } else {
                            GraphUtilities.setStyle(tra, getGraphController().getGraphSettings().getDefaultTransitionStyle());
                        }
                        GraphUtilities.setBreakpoints(tra, points);
                        points.clear();
                        graphController.setUpdateInProgress(true);
                        JaWEManager.getInstance().getJaWEController().startUndouableChange();
                        tras.add(tra);
                        getGraphManager().insertTransition(tra);
                        List toSelect = new ArrayList();
                        toSelect.add(tra);
                        JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                        getGraph().selectTransition(tra, false);
                        graphController.setUpdateInProgress(false);
                    } else if (sxpdl instanceof ExtendedAttribute && txpdl instanceof Activity) {
                        connectStartOrEndBubble((ExtendedAttribute) sxpdl, ((Activity) txpdl).getId());
                    } else if (txpdl instanceof ExtendedAttribute && sxpdl instanceof Activity) {
                        connectStartOrEndBubble((ExtendedAttribute) txpdl, ((Activity) sxpdl).getId());
                    }
                    return true;
                }
                // circular
            } else {
                if (validateConnection(pPort, pPort, null) && points.size() > 0) { // CUSTOM: only allow circular if there is another point

                    Point realP = (Point) getGraph().fromScreen(new Point(start));
                    List breakpoints = new ArrayList();
                    if (points.size() == 0) {
                        int rp50x1 = realP.x - 50;
                        int rp50x2 = realP.x + 50;
                        if (rp50x1 < 0) {
                            rp50x2 = rp50x2 - rp50x1;
                            rp50x1 = 0;
                        }
                        int rp50y = realP.y - 50;
                        if (rp50y < 0) {
                            rp50y = realP.y + 50;
                        }

                        breakpoints.add(new Point(Math.abs(rp50x1), Math.abs(rp50y)));
                        breakpoints.add(new Point(Math.abs(rp50x2), Math.abs(rp50y)));
                    } else {
                        breakpoints.addAll(points);
                        points.clear();
                    }

                    Activity act = (Activity) ((GraphPortInterface) firstPort.getCell()).getActivity().getPropertyObject();

                    Transitions tras = (Transitions) getGraph().getXPDLObject().get("Transitions");
                    Transition tra = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(tras, subType, false);
                    tra.setFrom(act.getId());
                    tra.setTo(act.getId());
                    GraphUtilities.setStyle(tra, GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_BEZIER);
                    GraphUtilities.setBreakpoints(tra, breakpoints);
                    getGraphController().setUpdateInProgress(true);
                    JaWEManager.getInstance().getJaWEController().startUndouableChange();
                    tras.add(tra);
                    getGraphManager().insertTransition(tra);
                    List toSelect = new ArrayList();
                    toSelect.add(tra);
                    JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                    getGraph().selectTransition(tra, false);
                    getGraphController().setUpdateInProgress(false);

                    return true;
                }
            }
        }

        return false;
    }

    public void overlay(JGraph graph, Graphics g, boolean clear) {
        super.overlay(graph, g, clear);
        if (getGraph() != null) {
            paintPort(getGraph().getGraphics());
        }
        if (start != null) {
            if (isTransitionButtonSelected()) {
                drawTransition(g);
            }
        }
    }

    protected void drawTransition(MouseEvent ev) {
        Graphics g = getGraph().getGraphics();
        Color bg = getGraph().getBackground();
        Color fg = graphController.getGraphSettings().getBubbleConectionColor();
        g.setColor(fg);
        g.setXORMode(bg);
        overlay(getGraph(), g, false);
        current = (Point) getGraph().snap(ev.getPoint());
        double scale = getGraph().getScale();
        port = (GraphPortViewInterface) getGraph().getPortViewAt(ev.getX(), ev.getY());
        if (port != null) {
            current = port.getBounds().getBounds().getLocation();//HM, JGraph3.4.1
            current = new Point((int) (current.x * scale),
                    (int) (current.y * scale));
            current.x += port.getGraphPortSize().width / 2;
            current.y += port.getGraphPortSize().height / 2;

        }
        g.setColor(bg);
        g.setXORMode(fg);
        overlay(getGraph(), g, false);
    }

    protected void drawTransition(Graphics g) {
        Point l = start;
        if (points.size() != 0) {
            l = (Point) points.get(points.size() - 1);
        }
        if (current != null) {
            g.drawLine(l.x, l.y, current.x, current.y);
        }
    }

    protected void paintPort(Graphics g) {
        if (port != null) {
            boolean offset = (GraphConstants.getOffset(port.getAttributes()) != null);
            Rectangle r = (offset) ? port.getBounds().getBounds()//HM, JGraph3.4.1
                    : port.getParentView().getBounds().getBounds();//HM, JGraph3.4.1
            r = (Rectangle) getGraph().toScreen(new Rectangle(r));//HM, JGraph3.4.1
            int s = 3;
            r.translate(-s, -s);
            r.setSize(r.width + 2 * s, r.height + 2 * s);
            JaWEGraphUI ui = (JaWEGraphUI) getGraph().getUI();
            ui.paintCell(g, port, r, true);
        }
    }

    public void connectStartOrEndBubble(ExtendedAttribute sea, String actId) {
        GraphBubbleActivityInterface gactb = getGraphManager().getBubble(sea);
        if (gactb != null) {
            GraphTransitionInterface gtra = getGraphManager().connectStartOrEndBubble(gactb, actId);
            if (gtra != null) {
                getGraphController().setUpdateInProgress(true);
                JaWEManager.getInstance().getJaWEController().startUndouableChange();
                StartEndDescription sed = gactb.getStartEndDescription();
                sed.setActId(actId);
                sea.setVValue(sed.toString());
                List toSelect = new ArrayList();
                toSelect.add(getGraph().getXPDLObject());
                JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
                getGraph().clearSelection();
                getGraphController().setUpdateInProgress(false);
            }
        }
    }

    public String getMainType() {
        return mainType;
    }

    public String getSubType() {
        return subType;
    }

    public void setType(String mainType, String subType, Cursor cursor) {
        this.mainType = mainType;
        this.subType = subType;

        if (cursor != null) {
            getGraph().setCursor(cursor);
        } else {
            getGraph().setCursor(Cursor.getDefaultCursor());
        }

        reset();
    }
}
