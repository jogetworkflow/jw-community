package org.enhydra.shark.xpdl.elements;

import org.enhydra.jawe.JaWE;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XPDLConstants;

public class DataField extends XMLCollectionElement {

    public DataField(DataFields dfs) {
        super(dfs, true);
    }

    protected void fillStructure() {
        DataType refDataType = new DataType(this);
        InitialValue refInitialValue = new InitialValue(this); // min=0
        Length refLength = new Length(this); // min=0
        Description refDescription = new Description(this); // min=0
        ExtendedAttributes refExtendedAttributes = new ExtendedAttributes(this); // min=0

        //XMLAttribute attrName=new XMLAttribute(this,"Name", false);
        XMLAttribute attrIsArray = new XMLAttribute(this, "IsArray",
                false, new String[]{
                    XPDLConstants.DATA_FIELD_IS_ARRAY_TRUE,
                    XPDLConstants.DATA_FIELD_IS_ARRAY_FALSE
                }, 1);

        //CUSTOM

        super.fillStructure();
        this. //add(attrName);
                add(attrIsArray);
        add(refDataType);

        if (!JaWE.BASIC_MODE) {
            add(refInitialValue);
            add(refLength);
            add(refDescription);
            add(refExtendedAttributes);
        }
        //END CUSTOM
    }

    public XMLAttribute getIsArrayAttribute() {
        return (XMLAttribute) get("IsArray");
    }

    public boolean getIsArray() {
        return new Boolean(get("IsArray").toValue()).booleanValue();
    }

    public void setIsArray(boolean isArray) {
        set("IsArray", String.valueOf(isArray).toUpperCase());
    }

    public String getName() {
        return get("Name").toValue();
    }

    public void setName(String name) {
        set("Name", name);
    }

    public DataType getDataType() {
        return (DataType) get("DataType");
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

    public String getInitialValue() {
        return get("InitialValue").toValue();
    }

    public void setInitialValue(String initialValue) {
        set("InitialValue", initialValue);
    }

    public String getLength() {
        return get("Length").toValue();
    }

    public void setLength(String length) {
        set("Length", length);
    }
}
