package org.enhydra.shark.xpdl.elements;

import org.enhydra.jawe.JaWE;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLCollectionElement;

public class Participant extends XMLCollectionElement {

    public Participant(Participants parent) {
        super(parent, true);
    }

    protected void fillStructure() {
        XMLAttribute attrName = new XMLAttribute(this, "Name", false);
        ParticipantType refParticipantType = new ParticipantType(this);
        Description refDescription = new Description(this); // min=0
        ExternalReference refExternalReference = new ExternalReference(this, false); // min=0
        ExtendedAttributes refExtendedAttributes = new ExtendedAttributes(this); // min=0

        super.fillStructure();
        add(attrName);
        add(refParticipantType);
        add(refDescription);

        //CUSTOM
        if (!JaWE.BASIC_MODE) {
            add(refExternalReference);
            add(refExtendedAttributes);
        }
        //END CUSTOM
    }

    public String getName() {
        return get("Name").toValue();
    }

    public void setName(String name) {
        set("Name", name);
    }

    public String getDescription() {
        return get("Description").toValue();
    }

    public void setDescription(String description) {
        set("Description", description);
    }

    public ExtendedAttributes getExtendedAttributes() {
        return (ExtendedAttributes) get("ExtendedAttributes");
    }

    public ExternalReference getExternalReference() {
        return (ExternalReference) get("ExternalReference");
    }

    public ParticipantType getParticipantType() {
        return (ParticipantType) get("ParticipantType");
    }
}
