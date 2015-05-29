package org.enhydra.shark.xpdl.elements;

import org.enhydra.jawe.JaWE;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XPDLConstants;

public class ProcessHeader extends XMLComplexElement {

    public ProcessHeader(WorkflowProcess parent) {
        super(parent, true);
    }

    protected void fillStructure() {

        //CUSTOM
        XMLAttribute attrDurationUnit;

        if (!JaWE.BASIC_MODE) {
            attrDurationUnit = new XMLAttribute(this, "DurationUnit",
                    false, new String[]{
                        XPDLConstants.DURATION_UNIT_NONE,
                        XPDLConstants.DURATION_UNIT_Y,
                        XPDLConstants.DURATION_UNIT_M,
                        XPDLConstants.DURATION_UNIT_D,
                        XPDLConstants.DURATION_UNIT_h,
                        XPDLConstants.DURATION_UNIT_m,
                        XPDLConstants.DURATION_UNIT_s
                    }, 0);
        } else {
            attrDurationUnit = new XMLAttribute(this, "DurationUnit",
                    false, new String[]{
                        XPDLConstants.DURATION_UNIT_NONE,
                        XPDLConstants.DURATION_UNIT_D,
                        XPDLConstants.DURATION_UNIT_h,
                        XPDLConstants.DURATION_UNIT_m,
                        XPDLConstants.DURATION_UNIT_s
                    }, 0);
        }
        //END CUSTOM

        Created refCreated = new Created(this); // min=0
        Description refDescription = new Description(this); // min=0
        Priority refPriority = new Priority(this); // min=0
        Limit refLimit = new Limit(this); // min=0
        ValidFrom refValidFrom = new ValidFrom(this); // min=0
        ValidTo refValidTo = new ValidTo(this); // min=0
        TimeEstimation refTimeEstimation = new TimeEstimation(this); // min=0

        add(attrDurationUnit);
        add(refCreated);
        add(refDescription);
        add(refPriority);
        add(refLimit);

        //CUSTOM
        if (!JaWE.BASIC_MODE) {
            add(refValidFrom);
            add(refValidTo);
            add(refTimeEstimation);
        }
        //END CUSTOM
    }

    public XMLAttribute getDurationUnitAttribute() {
        return (XMLAttribute) get("DurationUnit");
    }

    public String getDurationUnit() {
        return getDurationUnitAttribute().toValue();
    }

    public void setDurationUnitNONE() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_NONE);
    }

    public void setDurationUnitYEAR() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_Y);
    }

    public void setDurationUnitMONTH() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_M);
    }

    public void setDurationUnitDAY() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_D);
    }

    public void setDurationUnitHOUR() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_h);
    }

    public void setDurationUnitMINUTE() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_m);
    }

    public void setDurationUnitSECOND() {
        getDurationUnitAttribute().setValue(XPDLConstants.DURATION_UNIT_s);
    }

    public String getCreated() {
        return get("Created").toValue();
    }

    public void setCreated(String created) {
        set("Created", created);
    }

    public String getDescription() {
        return get("Description").toValue();
    }

    public void setDescription(String description) {
        set("Description", description);
    }

    public String getPriority() {
        return get("Priority").toValue();
    }

    public void setPriority(String priority) {
        set("Priority", priority);
    }

    public String getLimit() {
        return get("Limit").toValue();
    }

    public void setLimit(String limit) {
        set("Limit", limit);
    }

    public String getValidFrom() {
        return get("ValidFrom").toValue();
    }

    public void setValidFrom(String validFrom) {
        set("ValidFrom", validFrom);
    }

    public String getValidTo() {
        return get("ValidTo").toValue();
    }

    public void setValidTo(String validTo) {
        set("ValidTo", validTo);
    }

    public TimeEstimation getTimeEstimation() {
        return (TimeEstimation) get("TimeEstimation");
    }
}
