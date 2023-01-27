package org.joget.apps.form.model;

import java.util.Map;

public interface FileDownloadSecurity {

    String PARAMETER_AS_LINK = "_AS_LINK";

    boolean isDownloadAllowed(Map requestParameters);
}
