package org.enhydra.shark.xpdl.elements;

import org.enhydra.jawe.JaWE;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLComplexElement;

public class Script extends XMLComplexElement {

    //CUSTOM
    public static String DEFAULT_TYPE = "text/javascript";
    //END CUSTOM

    public Script(Package parent) {
        super(parent, false);
    }

    protected void fillStructure() {
        //CUSTOM
        XMLAttribute attrType = new XMLAttribute(this, "Type", true); // required
        add(attrType);

        if (!JaWE.BASIC_MODE) {

            XMLAttribute attrVersion = new XMLAttribute(this, "Version", false);
            XMLAttribute attrGrammar = new XMLAttribute(this, "Grammar", false);


            add(attrVersion);
            add(attrGrammar);
        }

        //END CUSTOM
    }

    public String getGrammar() {
        return get("Grammar").toValue();
    }

    public void setGrammar(String grammar) {
        set("Grammar", grammar);
    }

    public String getType() {
        return get("Type").toValue();
    }

    public void setType(String type) {
        set("Type", type);
    }

    public String getVersion() {
        return get("Version").toValue();
    }

    public void setVersion(String version) {
        set("Version", version);
    }
}
