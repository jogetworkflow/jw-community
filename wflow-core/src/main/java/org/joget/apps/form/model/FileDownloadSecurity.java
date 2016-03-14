package org.joget.apps.form.model;

import java.util.Map;

public interface FileDownloadSecurity {
    boolean isDownloadAllowed(Map requestParameters);
}
