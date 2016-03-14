package org.enhydra.shark.xpdl.elements;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.base.xpdlobjectfactory.XPDLObjectFactory;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XPDLConstants;

/**
 *  Represents coresponding element from XPDL schema.
 *
 *  @author Sasa Bojanic
 */
public class FormalParameters extends XMLCollection {

   public FormalParameters (ApplicationTypes parent) {
      super(parent, false);
   }

   public FormalParameters (WorkflowProcess parent) {
      super(parent, false);
   }

   public XMLElement generateNewElement() {
      return new FormalParameter(this);
   }

   public FormalParameter getFormalParameter (String Id) {
      return (FormalParameter)super.getCollectionElement(Id);
   }

   // CUSTOM: update editable parameters for all activities
    @Override
    public void add(XMLElement el) {
        super.add(el);
        // add editable variable for all existing activities when a variable is added
        XPDLObjectFactory xpdlObjectFactory = JaWEManager.getInstance().getXPDLObjectFactory();
        FormalParameter fp = (FormalParameter)el;
        XMLElement parent = getParent();
        if (parent instanceof WorkflowProcess) {
            parent.setNotifyListeners(false);
            parent.setNotifyMainListeners(false);
            WorkflowProcess wp = (WorkflowProcess)parent;
            Activities acts = wp.getActivities();
            for (int i=0; i<acts.size(); i++) {
                Activity act = (Activity)acts.get(i);
                if (act.getActivityType() == XPDLConstants.ACTIVITY_TYPE_NO) {
                    ExtendedAttribute attribute = xpdlObjectFactory.createXPDLObject(act.getExtendedAttributes(), null, true);
                    attribute.setName("VariableToProcess_UPDATE");
                    attribute.setVValue(fp.getId());
                }
            }
            parent.setNotifyListeners(true);
            parent.setNotifyMainListeners(true);
        }
    }

    @Override
    public int remove(XMLElement el) {
        if (el instanceof DataField) {
            // remove editable variable for all existing activities when a variable is removed
            DataField df = (DataField)el;
            XMLElement parent = getParent();
            if (parent instanceof WorkflowProcess) {
                parent.setNotifyListeners(false);
                parent.setNotifyMainListeners(false);
                WorkflowProcess wp = (WorkflowProcess)parent;
                Activities acts = wp.getActivities();
                for (int i=0; i<acts.size(); i++) {
                    Activity act = (Activity)acts.get(i);
                    if (act.getActivityType() == XPDLConstants.ACTIVITY_TYPE_NO) {
                        ExtendedAttributes attributes = act.getExtendedAttributes();
                        int position = -1;
                        for (int j=0; j<attributes.size(); j++) {
                            ExtendedAttribute attribute = (ExtendedAttribute)attributes.get(j);
                            String attrName = attribute.getName();
                            if (("VariableToProcess_UPDATE".equals(attrName) || ("VariableToProcess_VIEW".equals(attrName))) && df.getId().equals(attribute.getVValue())) {
                                position = j;
                                break;
                            }
                        }
                        if (position >= 0) {
                            attributes.remove(position);
                        }
                    }
                }
                parent.setNotifyListeners(true);
                parent.setNotifyMainListeners(true);
            }
        }
        int result = super.remove(el);
        return result;
    }
   // END CUSTOM
}
