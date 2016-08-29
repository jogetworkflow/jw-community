package org.joget.apps.displaytag.export;

import org.displaytag.export.CsvView;

public class CustomCsvViewer extends CsvView {
    
    @Override
    public String getMimeType() {
        return "text/csv;charset=UTF-8"; //$NON-NLS-1$
    }
}
