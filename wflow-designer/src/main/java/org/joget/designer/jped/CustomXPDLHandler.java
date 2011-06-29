package org.joget.designer.jped;

import java.io.InputStream;
import java.net.URL;
import org.enhydra.jawe.base.xpdlhandler.XPDLHandlerSettings;
import org.jped.base.xpdlhandler.PluggableXPDLHandler;
import org.enhydra.shark.xpdl.elements.Package;

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
            InputStream in = null;
            byte[] bytes = null;
            Package pkg = null;

            try {

                try {
                    // read from URL
                    URL url = new URL(pkgReference);
                    in = url.openStream();
                    byte[] buffer = new byte[4096];
                    String contents = "";
                    int length;
                    while ((length = in.read(buffer, 0, buffer.length)) > 0) {
                        contents += new String(buffer, 0, length);
                    }
                    bytes = contents.getBytes();

                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ie) {
                        }
                    }
                }

                // open package
                if (bytes != null) {

                    pkg = super.openPackageFromStream(bytes, true);
                    return pkg;

                }

            } catch (Exception e) {
                throw new RuntimeException("Unable to open URL " + pkgReference, e);
            } finally {
                return pkg;
            }

        } else {
            return super.openPackage(pkgReference, handleExternalPackages);
        }
    }
}
