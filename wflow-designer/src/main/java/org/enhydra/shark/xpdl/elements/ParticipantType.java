package org.enhydra.shark.xpdl.elements;

import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XPDLConstants;

/**
 *  Represents coresponding element from XPDL schema.
 *
 *  @author Sasa Bojanic
 */
public class ParticipantType extends XMLComplexElement {

   public ParticipantType (Participant parent) {
      super(parent, true);
   }

   protected void fillStructure () {
      // CUSTOM: Remove unused types
      // required
      XMLAttribute attrType=new XMLAttribute(this,"Type",
         true,
         new String[] {
//            XPDLConstants.PARTICIPANT_TYPE_RESOURCE_SET,
//            XPDLConstants.PARTICIPANT_TYPE_RESOURCE,
            XPDLConstants.PARTICIPANT_TYPE_ROLE,
//            XPDLConstants.PARTICIPANT_TYPE_ORGANIZATIONAL_UNIT,
//            XPDLConstants.PARTICIPANT_TYPE_HUMAN,
            XPDLConstants.PARTICIPANT_TYPE_SYSTEM
         }, 0);
      // END CUSTOM

      add(attrType);
   }

   public XMLAttribute getTypeAttribute() {
      return (XMLAttribute)get("Type");
   }

   public String getType () {
      return getTypeAttribute().toValue();
   }
   public void setTypeRESOURCE_SET () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_RESOURCE_SET);
   }
   public void setTypeRESOURCE () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_RESOURCE);
   }
   public void setTypeROLE () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_ROLE);
   }
   public void setTypeORGANIZATIONAL_UNIT () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_ORGANIZATIONAL_UNIT);
   }
   public void setTypeHUMAN () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_HUMAN);
   }
   public void setTypeSYSTEM () {
      getTypeAttribute().setValue(XPDLConstants.PARTICIPANT_TYPE_SYSTEM);
   }
}
