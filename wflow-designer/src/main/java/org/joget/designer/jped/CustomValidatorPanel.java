package org.joget.designer.jped;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultCellEditor;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;
import org.enhydra.jawe.JaWEAction;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;

import org.enhydra.jawe.Utils;
import org.enhydra.jawe.base.controller.JaWEFrame;
import org.enhydra.jawe.base.editor.StandardXPDLElementEditor;
import org.enhydra.jawe.base.xpdlvalidator.ValidationError;
import org.enhydra.jawe.components.XPDLTreeModel;
import org.enhydra.jawe.components.XPDLTreeNode;
import org.enhydra.jawe.components.graph.Graph;
import org.enhydra.jawe.components.graph.GraphController;
import org.enhydra.jawe.components.simplenavigator.SimpleNavigator;
import org.enhydra.jawe.components.simplenavigator.SimpleNavigatorPanel;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLValidationError;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.joget.designer.Designer;

/**
 * Panel to show the warnings and errors in the process, and to suggest corrective actions.
 */
public class CustomValidatorPanel extends JPanel implements JaWEComponentView {

    protected static Dimension listDimension = new Dimension(230, 250);
    protected CustomValidator controller;
    protected JLabel designLabel;
    protected JButton deployButton;
    protected JTable table;
    protected List errorList;

    public CustomValidatorPanel(
            CustomValidator controller) {

        this.controller = controller;
        init();
    }

    public void configure() {
    }

    public void init() {
        setLayout(new BorderLayout());

        // add top design label
        designLabel = new JLabel(ResourceManager.getLanguageDependentString("DesignOK"));
        designLabel.setMinimumSize(new Dimension(200, 25));
        designLabel.setPreferredSize(new Dimension(200, 25));
        designLabel.setHorizontalAlignment(JLabel.CENTER);
        designLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/packagecheck.gif")));
        add(designLabel, BorderLayout.NORTH);

        // add bottom deploy button
        deployButton = new JButton(ResourceManager.getLanguageDependentString("DeployOK"));
        deployButton.setMinimumSize(new Dimension(200, 25));
        deployButton.setPreferredSize(new Dimension(200, 25));
        deployButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/packagecheck.gif")));
        add(deployButton, BorderLayout.SOUTH);

        // add scrollpane
        final JaWEFrame jf = JaWEManager.getInstance().getJaWEController().getJaWEFrame();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // add table
        table = new JTable() {
            @Override
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return (vColIndex != 0);
            }
        };
        table.setFillsViewportHeight(true);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setBackground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new CustomListListener());
        table.setModel(new DefaultTableModel(new Object[][] {}, new Object[] { "", "" }));
        table.getColumnModel().getColumn(0).setCellRenderer(new CustomCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new CustomButtonRenderer(new JCheckBox()));
        table.getColumnModel().getColumn(1).setCellEditor(new CustomButtonRenderer(new JCheckBox()));
        table.getColumnModel().getColumn(1).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(40);
        scrollPane.setViewportView(table);
    }

    public JaWEComponent getJaWEComponent() {
        return controller;
    }

    public JComponent getDisplay() {
        return this;
    }

    protected void fillListContent(List errors) {
        boolean hasError = false;
        boolean hasWarning = false;

        // update error list
        this.errorList = new ArrayList(errors);
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        Iterator it = errors.iterator();
        while (it.hasNext()) {
            Object el = it.next();
            if (el instanceof ValidationError) {
                ValidationError err = (ValidationError) el;

                // determine whether error or warning
                String color = "yellow";
                String icon = "warning_icon.gif";
                if (XMLValidationError.TYPE_ERROR.equals(err.getType())) {
                    color = "red";
                    icon = "error_icon.gif";
                    hasError = true;
                } else if (XMLValidationError.TYPE_WARNING.equals(err.getType())) {
                    hasWarning = true;
                }
                XMLElement element = Utils.getLocation(err.getElement());

                // determine error message
                String errorId = err.getId();
                String error = ResourceManager.getLanguageDependentString(errorId);
                if (error == null) {
                    error = errorId;
                }

                // determine error type, element type and name
                String errorType = ResourceManager.getLanguageDependentString(err.getType() + "TypeKey");
                String elementType = ResourceManager.getLanguageDependentString(element.toName() + "Key");
                String name = "";
                if (element instanceof XMLComplexElement) {
                    XMLElement idEl = (XMLElement) ((XMLComplexElement) element).get("Name");
                    name = (idEl != null) ? idEl.toValue() : "";
                    if (name == null || name.trim().length() == 0) {
                        idEl = (XMLElement) ((XMLComplexElement) element).get("Id");
                        name = (idEl != null) ? idEl.toValue() : "";
                    }
                }

                // compose label message
                String message =
                        "<html>"
//                        + "<span bgcolor=\"" + color + "\">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;"
                        + "<b><font size=\"2\">" + errorType + ": " + name + "</font></b><br>"
                        + "<font size=\"2\">" + error + "</font>"
                        + "</html>";
                JLabel label = new JLabel(message, new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/" + icon)), SwingConstants.LEFT);

                // add row
                tableModel.addRow(new Object[] { label, getSuggestionKey(error) });
            } else if (el instanceof String) {
                tableModel.addRow(new Object[] { el });
            }
        }

        // update label and button
        if (hasError) {
            // error, don't allow deployment
            designLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/delete.gif")));
            designLabel.setText(ResourceManager.getLanguageDependentString("DesignNotOK"));
            deployButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/delete.gif")));
            deployButton.setText(ResourceManager.getLanguageDependentString("DeployNotOK"));
            deployButton.setEnabled(false);
        } else {
            // no error, allow deployment
            designLabel.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/packagecheck.gif")));
            designLabel.setText(ResourceManager.getLanguageDependentString("DesignOK"));
            deployButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/packagecheck.gif")));
            deployButton.setText(ResourceManager.getLanguageDependentString("DeployOK"));
            if (true || Designer.isPackageFixed()) {
                deployButton.setEnabled(true);
                deployButton.setAction(new Deploy(JaWEManager.getInstance().getJaWEController()));
            } else {
                deployButton.setEnabled(false);
            }
        }
    }

    public void cleanup() {
        table = null;
    }

    protected List getErrorList() {
        return errorList;
    }

    /**
     * Retrieves the workflow element based on the selected row
     * @return
     */
    protected XMLElement getSelectedElement() {
        XMLElement element = null;
        int selected = table.getSelectedRow();
        if (selected >= 0 && errorList.size() > selected) {
            Object el = errorList.get(selected);
            if (el instanceof ValidationError) {
                // get error
                ValidationError err = (ValidationError) el;
                element = Utils.getLocation(err.getElement());
            }
        }
        return element;
    }

    /**
     * Listener to select the affected element in the graph when an error is selected.
     */
    class CustomListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent lse) {
            XMLElement element = getSelectedElement();
            if (element != null) {
                // select element in graph
                GraphController gc = (GraphController) JaWEManager.getInstance().getComponentManager().getComponent("GraphComponent");
                Graph g = null;
                WorkflowProcess wp = null;
                if (element instanceof Activity) {
                    wp = (WorkflowProcess) element.getParent().getParent();
                    g = gc.getGraph(wp);
                    gc.selectGraphForElement(wp);
                    g.selectActivity((Activity) element, false);
                } else if (element instanceof Transition) {
                    wp = (WorkflowProcess) element.getParent().getParent();
                    g = gc.getGraph(wp);
                    gc.selectGraphForElement(wp);
                    g.selectTransition((Transition) element, false);
                }

                // hilite affected process in tree
                if (wp != null) {
                    SimpleNavigator tcon = (SimpleNavigator) JaWEManager.getInstance().getComponentManager().getComponent("SimpleNavigatorComponent");
                    SimpleNavigatorPanel panel = (SimpleNavigatorPanel) (tcon.getView());
                    panel.getTree().removeTreeSelectionListener(tcon);
                    XPDLTreeNode n = ((XPDLTreeModel) panel.getTreeModel()).findNode(wp);
                    TreePath tp = null;
                    if (n != null) {
                        tp = new TreePath(n.getPath());
                        panel.getTree().addSelectionPath(tp);
                    }
                    if (tp != null) {
                        panel.getTree().scrollPathToVisible(tp);
                    }
                    panel.getTree().addTreeSelectionListener(tcon);
                }
            }
        }
    }

    /**
     * Custom cell renderer to display the error message
     */
    class CustomCellRenderer extends JLabel implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Component) {
                Component comp = (Component)value;
                if (isSelected) {
                    JLabel label = (JLabel)comp;
                    label.setOpaque(true);
                    label.setBackground(table.getSelectionBackground());
                } else {
                    comp.setBackground(Color.WHITE);
                }
                return comp;
            } else {
                setText(value.toString());
                setOpaque(true);
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                } else {
                    setBackground(Color.WHITE);
                }
                return this;
            }
        }
    }

    /**
     * Custom cell renderer and editor to show a button that can trigger an action
     */
    public class CustomButtonRenderer extends DefaultCellEditor implements TableCellRenderer {

        protected JPanel panel;
        protected JButton button;
        private String message;
        private boolean isPushed;

        public CustomButtonRenderer(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setBackground(Color.WHITE);
            button.setOpaque(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.add(button, BorderLayout.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null && value instanceof String && value.toString().trim().length() > 0) {
                button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/idea.png")));
            } else {
                button.setIcon(null);
            }
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (value != null && value instanceof String && value.toString().trim().length() > 0) {
                button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("org/enhydra/jawe/images/idea.png")));
            } else {
                button.setIcon(null);
            }
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }
            message = (value == null) ? "" : value.toString();
            isPushed = true;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && message != null && message.trim().length() > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        handleSuggestion(button, message);
                    }
                });
            }
            isPushed = false;
            return message;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

    }

    /**
     * Returns a non empty String if there is a suggestion for the error.
     * @param errorId
     * @return
     */
    protected String getSuggestionKey(String error) {
        if (error.equals(ResourceManager.getLanguageDependentString("ERROR_WORKFLOW_PROCESS_NOT_DEFINED"))
                || error.equals(ResourceManager.getLanguageDependentString("ErrorIncomingTransitionOrConnectionFromStartBubbleIsMissing"))
                || error.equals(ResourceManager.getLanguageDependentString("ErrorOutgoingTransitionOrConnectionToEndBubbleIsMissing"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_CONDITIONAL_TRANSITION_WITHOUT_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_EXCEPTION_TRANSITION_WITHOUT_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_UNCONDITIONAL_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_DEFAULT_EXCEPTION_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_OTHERWISE_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_CONDITION_EXPRESSION_POSSIBLY_INVALID"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_DEADLINE_EXPRESSION_POSSIBLY_INVALID"))) {
            return error;
        } else {
            return "";
        }
    }

    /**
     * Perform suggestion based on the error/warning message
     * @param button
     * @param error
     */
    protected void handleSuggestion(JButton button, String error) {
        // TODO: temporarily handle everything in this method, best to implement some design pattern to make this method cleaner
        if (error.equals(ResourceManager.getLanguageDependentString("ERROR_WORKFLOW_PROCESS_NOT_DEFINED"))) {

            // show initial help message
            String message = getSuggestionMessage(error, ResourceManager.getLanguageDependentString("GUIDE_ERROR_WORKFLOW_PROCESS_NOT_DEFINED"));
            JOptionPane.showMessageDialog(this.getRootPane(), message);

        } else if (error.equals(ResourceManager.getLanguageDependentString("ErrorIncomingTransitionOrConnectionFromStartBubbleIsMissing"))
                || error.equals(ResourceManager.getLanguageDependentString("ErrorOutgoingTransitionOrConnectionToEndBubbleIsMissing"))) {
            
            // suggest to insert missing start/end
            String message = getSuggestionMessage(error, ResourceManager.getLanguageDependentString("GUIDE_ERROR_START_END"));
            int result = JOptionPane.showConfirmDialog(this.getRootPane(), message, ResourceManager.getLanguageDependentString("GUIDE_TITLE"), JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                // insert missing start/end
                GraphController gc = (GraphController)JaWEManager.getInstance().getComponentManager().getComponent("GraphComponent");
                JaWEAction action = gc.getGraphSettings().getAction("InsertMissingStartAndEndBubbles");
                action.getAction().actionPerformed(null);
            }

        } else if (error.equals(ResourceManager.getLanguageDependentString("WARNING_CONDITIONAL_TRANSITION_WITHOUT_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_EXCEPTION_TRANSITION_WITHOUT_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_UNCONDITIONAL_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_DEFAULT_EXCEPTION_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_OTHERWISE_TRANSITION_WITH_EXPRESSION"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_CONDITION_EXPRESSION_POSSIBLY_INVALID"))
                || error.equals(ResourceManager.getLanguageDependentString("WARNING_DEADLINE_EXPRESSION_POSSIBLY_INVALID"))) {
            
            // suggest to edit transition properties
            String message = getSuggestionMessage(error, ResourceManager.getLanguageDependentString("GUIDE_WARNING_TRANSITION"));
            int result = JOptionPane.showConfirmDialog(this.getRootPane(), message, ResourceManager.getLanguageDependentString("GUIDE_TITLE"), JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                // show transition dialog
                XMLElement element = getSelectedElement();
                StandardXPDLElementEditor ed = new StandardXPDLElementEditor();
                ed.editXPDLElement(element);
            }

        } else {

            // show default message
            String message = getSuggestionMessage(error, ResourceManager.getLanguageDependentString("GUIDE_NO_SUGGESTIONS"));
            JOptionPane.showMessageDialog(this.getRootPane(), message, ResourceManager.getLanguageDependentString("GUIDE_TITLE"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected String getSuggestionMessage(String error, String message) {
        // help message
        String html = "<html>" + "<b>" + error + "</b>" + "<br><br><font size=\"3\">" + message + "</font><br><br>" + "</html>";
        return html;
    }

}
