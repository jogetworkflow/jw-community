package org.joget.apps.displaytag.export;

import java.util.Locale;
import org.displaytag.export.CsvView;
import org.joget.apps.app.service.AppUtil;
import org.w3c.www.mime.Utils;

public class CustomCsvViewer extends CsvView {
    
    @Override
    public String getMimeType() {
        String localeCode = AppUtil.getAppLocale();
        
        try{
            if (!"en_US".equals(localeCode)) {
                Locale locale = null;
                String[] temp = localeCode.split("_");

                if (temp.length == 1) {
                    locale = new Locale(temp[0]);
                } else if (temp.length == 2) {
                    locale = new Locale(temp[0], temp[1]);
                } else if (temp.length == 3) {
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }

                String charset = Utils.getCharset(locale);

                if (charset != null && !charset.isEmpty()) {
                    return "text/csv;charset=" + charset;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        
        return "text/csv"; //$NON-NLS-1$
    }
}
