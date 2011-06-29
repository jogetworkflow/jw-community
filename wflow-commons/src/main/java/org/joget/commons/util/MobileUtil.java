package org.joget.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class MobileUtil {

    private MobileUtil(){
    }

    public static Boolean mobileDeviceDetect(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String accept = request.getHeader("Accept");

        //Check for iphone or ipod or android or opera mini or blackberry or htc
        Pattern p = Pattern.compile("(iphone|ipod|android|opera mini|blackberry|htc|opera mobi)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(userAgent);
        if (m.find()) {
            return true;
        }

        //check for palm os
        p = Pattern.compile("(palm os|palm|hiptop|avantgo|plucker|xiino|blazer|elaine)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(userAgent);
        if (m.find()) {
            return true;
        }

        //check for windows mobile
        p = Pattern.compile("(microsoft windows; ppc;|windows ce; ppc;|windows ce; smartphone;|windows ce; iemobile)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(userAgent);
        if (m.find()) {
            return true;
        }

        //check for some of the most common terms used in agents to identify them as being mobile devices
        p = Pattern.compile("(up.browser|up.link|mmp|symbian|smartphone|midp|wap|vodafone|o2|pocket|kindle|mobile|pda|psp|treo)", Pattern.CASE_INSENSITIVE);
        m = p.matcher(userAgent);
        if (m.find()) {
            return true;
        }

        //check the device showing signs of support for text/vnd.wap.wml or application/vnd.wap.xhtml+xml
        if ((accept.indexOf("text/vnd.wap.wml") != -1) || (accept.indexOf("application/vnd.wap.xhtml+xml") != -1)) {
            return true;
        }

        //check is that the device giving us a X_WAP_PROFILE or PROFILE header - only mobile devices would do this
        if ((request.getHeader("X-Wap-Profile") != null) || (request.getHeader("Profile") != null)) {
            return true;
        }

        return false;
    }
}
