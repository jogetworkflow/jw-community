package org.joget.designer.jped;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpResponseException;
import org.enhydra.jawe.base.xpdlhandler.XPDLHandlerSettings;
import org.jped.base.xpdlhandler.PluggableXPDLHandler;
import org.enhydra.shark.xpdl.elements.Package;
import org.joget.designer.Designer;

public class CustomXPDLHandler extends PluggableXPDLHandler {

    public CustomXPDLHandler() {
        super();
    }

    public CustomXPDLHandler(XPDLHandlerSettings settings) throws Exception {
        super(settings);
    }

    @Override
    public Package openPackage(String pkgReference, boolean handleExternalPackages) {

        if (pkgReference != null && pkgReference.startsWith("http")) {
            // open XPDL from URL
            byte[] bytes = null;
            Package pkg = null;

            try {
                String sessionId = Designer.SESSION;
                int port =  Integer.parseInt(Designer.PORT);
                String cookieDomain =  Designer.DOMAIN;
                String cookiePath = Designer.CONTEXTPATH;
                    String contents = "";
                try {
                    contents = HttpUtil.httpPost(null, pkgReference, port, sessionId, cookieDomain, cookiePath,  Designer.USERNAME, null, false, false, null, null);
                } catch(AuthenticationException ae) {
                    System.exit(0);
                } catch(HttpResponseException he) {
                    System.exit(0);
                }
                if (contents == null) {
                    System.exit(0);
                }
                bytes = contents.getBytes("UTF-8");

                // open package
                if (bytes != null) {

                    pkg = super.openPackageFromStream(bytes, true);
                    return pkg;

                }

            } catch (Exception e) {
                e.printStackTrace();
                //throw new RuntimeException("Unable to open URL " + pkgReference, e);
            }
            return pkg;
        } else {
            return super.openPackage(pkgReference, handleExternalPackages);
        }
    }
}
