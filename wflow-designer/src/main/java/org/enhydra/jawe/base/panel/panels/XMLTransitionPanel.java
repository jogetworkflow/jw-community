package org.enhydra.jawe.base.panel.panels;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.base.panel.PanelContainer;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.elements.Condition;
import org.enhydra.shark.xpdl.elements.DataField;
import org.enhydra.shark.xpdl.elements.DataFields;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;

public class XMLTransitionPanel extends XMLGroupPanel {

   public XMLTransitionPanel(PanelContainer pc,
                        XMLElement myOwnerL,
                        Object[] elements,
                        String title,
                        boolean isVertical,
                        boolean hasBorder,
                        boolean hasEmptyBorder) {
      super(pc,
           myOwnerL,
           Arrays.asList(elements),
           title,
           isVertical,
           hasBorder,
           hasEmptyBorder);
   }

   public XMLTransitionPanel(PanelContainer pc,
                        XMLElement myOwnerL,
                        List elements,
                        String title,
                        boolean isVertical,
                        boolean hasBorder,
                        boolean hasEmptyBorder) {

      super(pc, myOwnerL, elements, title, isVertical, hasBorder, hasEmptyBorder);
   }

   public static final String DEFAULT_VARIABLE = "status";

    @Override
    public void setElements() {
        super.setElements();
        suggestCondition();
    }

    /**
     * Suggest to automatically add a Condition if a name is specified
     */
    protected void suggestCondition() {
        Transition transition = (Transition)getOwner();
        String name = transition.getName();
        Condition condition = transition.getCondition();
        String conditionType = (condition != null) ? condition.getType() : "";
        String expression = (condition != null) ? condition.toValue() : "";

        // check when transition name is specified and no existing condition
        if (name != null && name.trim().length() > 0 
                && conditionType.trim().length() == 0 || conditionType.equals(XPDLConstants.CONDITION_TYPE_CONDITION)
                && expression.trim().length() == 0) {

            // check for existing variable
            String variableName = checkDefaultVariable(transition);

            if (variableName != null) {
                // prompt to auto generate condition
                String message = "<html>"
                        + "<font size=\"3\">"
                        + ResourceManager.getLanguageDependentString("GUIDE_AUTO_CONDITION")
                        + "</font>"
                        + "</html>";
                int result = JOptionPane.showConfirmDialog(this.getRootPane(), message, ResourceManager.getLanguageDependentString("GUIDE_TITLE"), JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    // create default variable
                    createDefaultVariable(transition, variableName);
                    // set condition expression
                    populateExpression(transition, variableName, name);
                }
            }
        }
    }

    /**
     * Check for existence of the default variable
     * @return name of the default variable, null if not created
     */
    protected String checkDefaultVariable(Transition transition) {
        String variableName = null;
        // check for existing variables
        WorkflowProcess process = (WorkflowProcess) transition.getParent().getParent();
        Map dataFieldMap = XMLUtil.getPossibleDataFields(process);
        if (dataFieldMap.isEmpty() || dataFieldMap.get(DEFAULT_VARIABLE) != null) {
            variableName = DEFAULT_VARIABLE;
        }
        return variableName;
    }

    /**
     * Create default variable if no existing variables
     * @return name of the created variable, null if not created
     */
    protected void createDefaultVariable(Transition transition, String variableName) {
        WorkflowProcess process = (WorkflowProcess) transition.getParent().getParent();
        Map dataFieldMap = XMLUtil.getPossibleDataFields(process);
        if (dataFieldMap.isEmpty() || dataFieldMap.get(variableName) == null) {
            // default variable does not exist, create new one
            DataFields dfs = process.getDataFields();
            DataField df = (DataField)dfs.generateNewElement();
            df.setId(variableName);
            dfs.add(df);
        }
    }

    /**
     * Add condition expression variable == 'value'
     * @param variable
     * @param value
     */
    protected void populateExpression(Transition transition, String variable, String value) {
        // set expression
        String expression = variable + "=='" + value + "'";
        Condition condition = transition.getCondition();
        condition.setTypeCONDITION();
        condition.setValue(expression);

        // reset name to blank
        transition.setName("");
    }

}
