package org.enhydra.jawe.components.graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import org.enhydra.jawe.ActionBase;
import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.JaWEAction;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.XMLElementChoiceButton;
import org.enhydra.jawe.base.controller.JaWEType;
import org.enhydra.jawe.base.controller.JaWETypes;
import org.enhydra.jawe.components.graph.actions.SetActivityMode;
import org.enhydra.jawe.components.graph.actions.SetEndMode;
import org.enhydra.jawe.components.graph.actions.SetParticipantMode;
import org.enhydra.jawe.components.graph.actions.SetParticipantModeFreeTextExpression;
import org.enhydra.jawe.components.graph.actions.SetSelectMode;
import org.enhydra.jawe.components.graph.actions.SetStartMode;
import org.enhydra.jawe.components.graph.actions.SetTransitionMode;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Transition;

/**
 *  Container for displaying menubar, toolbar, process graphs ...
 *
 *  @author Sasa Bojanic
 */
public class GraphControllerPanel extends JPanel implements JaWEComponentView {

    protected GraphController controller;
    // various things needed for initializing and further work
    protected JScrollPane graphScrollPane;
    protected JToolBar toolbar;
    protected XMLElementChoiceButton showParticipantChoiceButton;
    protected XMLElementChoiceButton asChoiceButton;

    public GraphControllerPanel(GraphController controller) {
        this.controller = controller;
    }

    public void configure() {
    }

    public void init() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        JPanel toolbars = new JPanel();
        toolbars.setLayout(new BorderLayout());
        // creating toolbars
        toolbar = BarFactory.createToolbar("defaultToolbar", controller);
        toolbar.setFloatable(false);
        // creating working component
        graphScrollPane = createWorkingComponent();

        JToolBar toolbox = createToolbox();
        toolbox.setOrientation(SwingConstants.VERTICAL);
        toolbox.setFloatable(false);
        add(toolbar, BorderLayout.NORTH);
        toolbars.add(toolbox, BorderLayout.CENTER);
        add(toolbars, BorderLayout.WEST);
        add(graphScrollPane, BorderLayout.CENTER);
    }

    public JaWEComponent getJaWEComponent() {
        return controller;
    }

    public JComponent getDisplay() {
        return this;
    }

    protected JToolBar createToolbox() {
        String toolbarName = "toolbox";
        String actionOrder = controller.getSettings().getToolbarActionOrder(toolbarName);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();

        // set tooltip delay to 100ms
        ToolTipManager.sharedInstance().setInitialDelay(100);

        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        String[] act = Utils.tokenize(actionOrder, BarFactory.ACTION_DELIMITER);

        for (int j = 0; j < act.length; j++) {
            if (act[j].equals(BarFactory.ACTION_SEPARATOR)) {
                toolbar.addSeparator();
            } else if (act[j].equals("SetSelectMode")) {
                JaWEAction ja = new JaWEAction();
                ja.setAction(new SetSelectMode(controller));
                ja.setIcon(((GraphSettings) controller.getSettings()).getSelectionIcon());
                ja.setLangDepName(controller.getSettings().getLanguageDependentString("SelectionKey"));
                JButton b = BarFactory.createToolbarButton(ja, controller);
                decorateToolboxButton(b, ja);
                toolbar.add(b);
                controller.getSettings().addAction("SetSelectMode", ja);
            } else if (act[j].equals("SetParticipantModeCommonExpression")) {
                JaWEAction ja = new JaWEAction();
                try {
                    String clsName = "org.enhydra.jawe.components.graph.actions.SetParticipantModeCommonExpression";
                    ActionBase action = (ActionBase) Class.forName(clsName).getConstructor(new Class[]{
                                GraphController.class
                            }).newInstance(new Object[]{
                                controller
                            });
                    ja.setAction(action);
                } catch (Exception ex) {
                }
                ja.setIcon(((GraphSettings) controller.getSettings()).getCommonExpresionParticipantIcon());
                ja.setLangDepName(controller.getSettings().getLanguageDependentString("CommonExpressionParticipantKey"));
                JButton b = BarFactory.createToolbarButton(ja, controller);
                decorateToolboxButton(b, ja);
                toolbar.add(b);
                controller.getSettings().addAction("SetParticipantModeCommonExpression", ja);
            } else if (act[j].equals("SetParticipantModeFreeTextExpression")) {
                JaWEAction ja = new JaWEAction();
                ja.setAction(new SetParticipantModeFreeTextExpression(controller));
                ja.setIcon(((GraphSettings) controller.getSettings()).getFreeTextParticipantIcon());
                ja.setLangDepName(controller.getSettings().getLanguageDependentString("FreeTextExpressionParticipantKey"));
                JButton b = BarFactory.createToolbarButton(ja, controller);
                decorateToolboxButton(b, ja);
                toolbar.add(b);
                controller.getSettings().addAction("SetParticipantModeFreeTextExpression", ja);
            } //CUSTOM
            else if (act[j].equals("SetStartMode")) {
                JaWEAction ja = new JaWEAction();
                ja.setAction(new SetStartMode(controller));
                ja.setIcon(((GraphSettings) controller.getSettings()).getBubbleStartIcon());
                ja.setLangDepName(controller.getSettings().getLanguageDependentString("StartBubbleKey"));
                JButton b = BarFactory.createToolbarButton(ja, controller);
                decorateToolboxButton(b, ja);
                toolbar.add(b);
                controller.getSettings().addAction("SetStartMode", ja);
            } else if (act[j].equals("SetEndMode")) {
                JaWEAction ja = new JaWEAction();
                ja.setAction(new SetEndMode(controller));
                ja.setIcon(((GraphSettings) controller.getSettings()).getBubbleEndIcon());
                ja.setLangDepName(controller.getSettings().getLanguageDependentString("EndBubbleKey"));
                JButton b = BarFactory.createToolbarButton(ja, controller);
                decorateToolboxButton(b, ja);
                toolbar.add(b);
                controller.getSettings().addAction("SetEndMode", ja);
                //END CUSTOM
            } else if (act[j].startsWith("SetParticipantMode")) {
                String type = act[j].substring("SetParticipantMode".length());
                if (type.equals("*")) {
                    List parTypes = jts.getTypes(Participant.class);
                    for (int i = 0; i < parTypes.size(); i++) {
                        JaWEType jt = (JaWEType) parTypes.get(i);
                        JaWEAction ja = new JaWEAction();
                        ja.setAction(new SetParticipantMode(controller, jt.getTypeId()));
                        ja.setIcon(jt.getIcon());
                        ja.setLangDepName(jt.getDisplayName());
                        JButton b = BarFactory.createToolbarButton(ja, controller);
                        decorateToolboxButton(b, ja);
                        toolbar.add(b);
                        controller.getSettings().addAction(jt.getTypeId(), ja);
                    }
                } else if (!(type.equals("SetParticipantModeCommonExpression") || type.equals("SetParticipantModeFreeTextExpression"))) {
                    JaWEType jt = jts.getType(type);
                    if (jt == null) {
                        continue;
                    }
                    JaWEAction ja = new JaWEAction();
                    ja.setAction(new SetParticipantMode(controller, jt.getTypeId()));
                    ja.setIcon(jt.getIcon());
                    ja.setLangDepName(jt.getDisplayName());
                    JButton b = BarFactory.createToolbarButton(ja, controller);
                    decorateToolboxButton(b, ja);
                    toolbar.add(b);
                    controller.getSettings().addAction(jt.getTypeId(), ja);
                }
            } else if (act[j].startsWith("SetActivityMode")) {
                String type = act[j].substring("SetActivityMode".length());
                if (type.equals("*")) {
                    List actTypes = jts.getTypes(Activity.class);
                    for (int i = 0; i < actTypes.size(); i++) {
                        JaWEType jt = (JaWEType) actTypes.get(i);
                        JaWEAction ja = new JaWEAction();
                        ja.setAction(new SetActivityMode(controller, jt.getTypeId()));
                        ja.setIcon(jt.getIcon());
                        ja.setLangDepName(jt.getDisplayName());
                        JButton b = BarFactory.createToolbarButton(ja, controller);
                        decorateToolboxButton(b, ja);
                        toolbar.add(b);
                        controller.getSettings().addAction(jt.getTypeId(), ja);
                    }
                } else {
                    JaWEType jt = jts.getType(type);
                    if (jt == null) {
                        continue;
                    }
                    JaWEAction ja = new JaWEAction();
                    ja.setAction(new SetActivityMode(controller, jt.getTypeId()));
                    ja.setIcon(jt.getIcon());
                    ja.setLangDepName(jt.getDisplayName());
                    JButton b = BarFactory.createToolbarButton(ja, controller);
                    decorateToolboxButton(b, ja);
                    toolbar.add(b);
                    controller.getSettings().addAction(jt.getTypeId(), ja);
                }
            } else if (act[j].startsWith("SetTransitionMode")) {
                String type = act[j].substring("SetTransitionMode".length());
                if (type.equals("*")) {
                    List traTypes = jts.getTypes(Transition.class);
                    for (int i = 0; i < traTypes.size(); i++) {
                        JaWEType jt = (JaWEType) traTypes.get(i);
                        JaWEAction ja = new JaWEAction();
                        ja.setAction(new SetTransitionMode(controller, jt.getTypeId()));
                        ja.setIcon(jt.getIcon());
                        ja.setLangDepName(jt.getDisplayName());
                        JButton b = BarFactory.createToolbarButton(ja, controller);
                        decorateToolboxButton(b, ja);
                        toolbar.add(b);
                        controller.getSettings().addAction(jt.getTypeId(), ja);
                    }
                } else {
                    JaWEType jt = jts.getType(type);
                    if (jt == null) {
                        continue;
                    }
                    JaWEAction ja = new JaWEAction();
                    ja.setAction(new SetTransitionMode(controller, jt.getTypeId()));
                    ja.setIcon(jt.getIcon());
                    ja.setLangDepName(jt.getDisplayName());
                    JButton b = BarFactory.createToolbarButton(ja, controller);
                    decorateToolboxButton(b, ja);
                    toolbar.add(b);
                    controller.getSettings().addAction(jt.getTypeId(), ja);
                }
            }
        }

        toolbar.setName(controller.getSettings().getLanguageDependentString(toolbarName + BarFactory.LABEL_POSTFIX));

        return toolbar;

    }

    protected void decorateToolboxButton(final JButton b, final JaWEAction ja) {
        b.setText(ja.getLangDepName());
        b.setFont(new Font("sansserif",Font.PLAIN,10));
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setMinimumSize(new Dimension(70, 40));
        b.setMaximumSize(new Dimension(70, 40));
        b.setPreferredSize(new Dimension(70, 40));
        // make it draggable
        b.addMouseListener(new MouseAdapter() {
            boolean pressed = false;

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (pressed) {
                    ja.getAction().actionPerformed(null);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Graph graph = controller.getSelectedGraph();
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                Point location = graph.getLocationOnScreen();
                int pointerX = (int)pointerInfo.getLocation().getX();
                int pointerY = (int)pointerInfo.getLocation().getY();
                int graphX = (int)graph.getParent().getLocationOnScreen().getX();
                int x = (int)pointerX - (int)location.getX();
                int y = (int)pointerY -  (int)location.getY();
                if (pressed && x > 0 && pointerX > graphX) {
                    if (graph.isEditable()) {
                        GraphMarqueeHandler marquee = (GraphMarqueeHandler)graph.getMarqueeHandler();
                        int status = marquee.getStatus();
                        if (status == JaWEGraphUI.INSERT_PARTICIPANT) {
                            marquee.insertParticipant();
                        } else if (status == JaWEGraphUI.INSERT_ELEMENT) {
                            marquee.insertElement((Point) graph.fromScreen(new Point(x, y)));
                        } else if (status == JaWEGraphUI.INSERT_TRANSITION_START) {
                            JaWEGraphUI ui = (JaWEGraphUI)graph.getUI();
                            ui.insertTransitionStart(x, y);
                        }
                        // reset to selection mode for non-transitions
                        if (status != JaWEGraphUI.INSERT_TRANSITION_START) {
                            marquee.setSelectionMode();
                        }
                    }
                }
                pressed = false;
            }

        });

    }

    protected JScrollPane createWorkingComponent() {
        JScrollPane lGraphScrollPane = new JScrollPane();
        JViewport port = lGraphScrollPane.getViewport();
        port.setScrollMode(JViewport.BLIT_SCROLL_MODE);

        // Harald Meister: set bigger scroll-amounts, especially useful for
        // mouse-wheel-scolling in large workflows
        lGraphScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        lGraphScrollPane.getHorizontalScrollBar().setUnitIncrement(40);

        return lGraphScrollPane;
    }

    public void graphSelected(Graph graph) {
        graphScrollPane.setViewportView(graph);
    }

    public void enableDisableButtons() {
    }
}