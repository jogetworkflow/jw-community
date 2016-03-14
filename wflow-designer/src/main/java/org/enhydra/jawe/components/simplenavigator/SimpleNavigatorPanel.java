package org.enhydra.jawe.components.simplenavigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.XPDLElementChangeInfo;
import org.enhydra.jawe.base.controller.JaWEActions;
import org.enhydra.jawe.components.XPDLTreeCellRenderer;
import org.enhydra.jawe.components.XPDLTreeModel;
import org.enhydra.jawe.components.XPDLTreeNode;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ActivitySet;
import org.enhydra.shark.xpdl.elements.ActivitySets;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.Transitions;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.WorkflowProcesses;

/**
 *  Used to display Package hierarchy tree.
 *
 *  @author Sasa Bojanic
 */
public class SimpleNavigatorPanel extends JPanel implements JaWEComponentView {

    protected XPDLTreeModel treeModel;
    protected JTree tree;
    protected JToolBar toolbar;
    protected JScrollPane scrollPane;
    protected SimpleNavigator controller;
    protected int xClick, yClick;
    protected MouseListener mouseListener;
    protected XPDLTreeCellRenderer renderer;

    public SimpleNavigatorPanel(SimpleNavigator controller) {
        this.controller = controller;
    }

    public void configure() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        toolbar = BarFactory.createToolbar("defaultToolbar", controller);
        toolbar.setFloatable(false);
        if (toolbar.getComponentCount() > 0) {
            add(toolbar, BorderLayout.NORTH);
        }
        init();
    }

    public void printTreeModel() {
        printTreeModel(treeModel.getRootNode());
    }

    public void printTreeModel(XPDLTreeNode n) {
        System.err.println("There are " + n.getChildCount() + " children for " + n.getXPDLElement());
        for (int i = 0; i < n.getChildCount(); i++) {
            printTreeModel((XPDLTreeNode) n.getChildAt(i));
        }

    }

    public void init() {
        controller.getSettings().adjustActions();
        treeModel = new SimpleNavigatorTreeModel(controller);

        tree = new JTree(treeModel) {

            public void scrollRectToVisible(Rectangle aRect) {
                aRect.x = scrollPane.getHorizontalScrollBar().getValue();
                super.scrollRectToVisible(aRect);
            }
        };

        // setting some tree properties
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        //CUSTOM
        tree.setToggleClickCount(0);
        //END CUSTOM
        renderer = new XPDLTreeCellRenderer(controller);
        Color bckColor = controller.getNavigatorSettings().getBackGroundColor();

        renderer.setBackgroundNonSelectionColor(bckColor);
        renderer.setBackgroundSelectionColor(controller.getNavigatorSettings().getSelectionColor());
        tree.setBackground(bckColor);
        tree.setCellRenderer(renderer);
        tree.addTreeSelectionListener(controller);

        /** MouseListener for JTree */
        mouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                xClick = me.getX();
                yClick = me.getY();
                TreePath path = tree.getPathForLocation(xClick, yClick);

                if (path != null) {
                    tree.setAnchorSelectionPath(path);

                    if (SwingUtilities.isRightMouseButton(me)) {
                        if (!tree.isPathSelected(path)) {
                            tree.setSelectionPath(path);
                        }

                        JPopupMenu popup = BarFactory.createPopupMenu("default", controller);

                        popup.show(tree, me.getX(), me.getY());
                    }

                    if (me.getClickCount() > 1 && !SwingUtilities.isRightMouseButton(me)) {
                        JaWEManager.getInstance().getJaWEController().getJaWEActions().getAction(JaWEActions.EDIT_PROPERTIES_ACTION).actionPerformed(null);
                        //CUSTOM
                        tree.updateUI();
                        //END CUSTOM
                    }
                } else {
                    TreePath close = tree.getClosestPathForLocation(xClick, yClick);
                    Rectangle rect = tree.getPathBounds(close);
                    if (rect == null || !(rect.y < yClick && rect.y + rect.height > yClick)) {
                        JaWEManager.getInstance().getJaWEController().getSelectionManager().setSelection((XMLElement) null, false);
                        tree.clearSelection();
                    }
                }

                tree.getParent().requestFocus();
            }
        };

        tree.addMouseListener(mouseListener);


        // creates panel
        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(tree);
        JViewport port = scrollPane.getViewport();
        port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setBackground(Color.lightGray);

        add(scrollPane, BorderLayout.CENTER);
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public JTree getTree() {
        return tree;
    }

    public JaWEComponent getJaWEComponent() {
        return controller;
    }

    public JComponent getDisplay() {
        return this;
    }

    public Point getMouseClickLocation() {
        return new Point(xClick, yClick);
    }

    public void handleXPDLChangeEvent(XPDLElementChangeInfo info) {
        int action = info.getAction();
        XMLElement el = info.getChangedElement();
        List l = info.getChangedSubElements();

        tree.removeTreeSelectionListener(controller);

        if (action == XPDLElementChangeInfo.SELECTED) {
            tree.clearSelection();
        }

        if (el instanceof Activity || el instanceof Package || el instanceof WorkflowProcesses || el instanceof WorkflowProcess || el instanceof ActivitySets || el instanceof ActivitySet || el instanceof Activities || (el instanceof Transitions && action == XPDLElementChangeInfo.SELECTED)) {
            if (action == XMLElementChangeInfo.INSERTED) {
                if (l != null && l.size() > 0) {
                    Iterator it1 = l.iterator();
                    while (it1.hasNext()) {
                        treeModel.insertNode((XMLElement) it1.next());
                    }
                } else {
                    if (el instanceof Package) {
                        treeModel.insertNode(el);
                    }
                }
                if (el instanceof Package) {
                    controller.getSettings().adjustActions();
                }
            } else if (action == XMLElementChangeInfo.REMOVED) {
                if (l != null && l.size() > 0) {
                    Iterator it1 = l.iterator();
                    while (it1.hasNext()) {
                        treeModel.removeNode((XMLElement) it1.next());
                    }
                } else {
                    treeModel.removeNode(el);
                }
                if (treeModel.getRootNode().getChildCount() == 0) {
                    reinitialize();
                    return;
                }
            } else if (action == XPDLElementChangeInfo.SELECTED) {
                if (el != null) {
                    List toSelect = new ArrayList();
                    if (l != null) {
                        toSelect.addAll(l);
                    }
                    if (toSelect.size() == 0) {
                        toSelect.add(el);
                    }
                    for (int i = 0; i < toSelect.size(); i++) {
                        XMLElement toSel = (XMLElement) toSelect.get(i);
                        if (toSelect instanceof Transitions || toSelect instanceof Transition) {
                            continue;
                        }
                        XPDLTreeNode n = treeModel.findNode(toSel);
                        TreePath tp = null;
                        if (n != null) {
                            tp = new TreePath(n.getPath());
                            tree.addSelectionPath(tp);
                        }
                        if (tp != null) {
                            tree.scrollPathToVisible(tp);
                        }
                    }
                }
            } else if (action == XMLElementChangeInfo.REPOSITIONED) {
                List elsToReposition = new ArrayList();
                List newPositions = new ArrayList();
                if (el instanceof XMLCollection) {
                    if (l != null) {
                        elsToReposition.addAll(l);
                        newPositions.addAll((List) info.getNewValue());
                    }
                    for (int j = 0; j < elsToReposition.size(); j++) {
                        XMLElement eltr = (XMLElement) elsToReposition.get(j);
                        treeModel.repositionNode(eltr, ((Integer) newPositions.get(j)).intValue());
                    }
                }
            }
        }

        tree.addTreeSelectionListener(controller);
    }

    protected void reinitialize() {
        remove(scrollPane);
        treeModel.clearTree();
        tree.getSelectionModel().clearSelection();
        tree.removeMouseListener(mouseListener);
        tree.removeTreeSelectionListener(controller);
        tree.setCellRenderer(null);
        init();
    }

    // before doing this, listener has to be removed
    public void setCurrentSelection() {
        List toSelect = JaWEManager.getInstance().getJaWEController().getSelectionManager().getSelectedElements();
        for (int i = 0; i < toSelect.size(); i++) {
            XMLElement toSel = (XMLElement) toSelect.get(i);
            if (toSel instanceof Package || toSel instanceof WorkflowProcess || toSel instanceof ActivitySet || toSel instanceof Activity) {
                XPDLTreeNode n = treeModel.findNode(toSel);
                TreePath tp = null;
                if (n != null) {
                    tp = new TreePath(n.getPath());
                    tree.addSelectionPath(tp);
                }
                if (tp != null) {
                    tree.scrollPathToVisible(tp);
                }
            }
        }
    }
}
