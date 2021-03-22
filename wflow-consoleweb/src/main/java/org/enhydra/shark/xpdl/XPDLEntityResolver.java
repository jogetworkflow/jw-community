package org.enhydra.shark.xpdl;

import java.io.InputStream;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class XPDLEntityResolver implements EntityResolver {

    public static final String XPDL_SCHEMA = "org/enhydra/shark/xpdl/resources/TC-1025_schema_10_xpdl.xsd";

    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId != null) {
            return getSchemaInputSource();
        }
        return null;
    }

    public static InputSource getSchemaInputSource() {
        try {
            // CUSTOM: fix for JBoss EAP 6.4
            /*
            URL u = XPDLEntityResolver.class.getClassLoader().getResource(XPDL_SCHEMA);
            is = (InputStream) u.getContent();
            */
            InputStream is = XPDLEntityResolver.class.getClassLoader().getResourceAsStream(XPDL_SCHEMA);
            return new InputSource(is);
        } catch (Exception ex) {
        }
        return new InputSource(); // do not allow unknown entities, by returning blank path
    }
}
