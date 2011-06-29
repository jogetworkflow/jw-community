package org.joget.commons.util;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONObject;

public class StringUtil {
    public static String TYPE_REGEX = "regex";
    public static String TYPE_JSON = "json";

    public static String encodeUrlParam(String url) {
        String urlResult = url;
        try {
            String[] urlPart = urlResult.split("\\?");

            urlResult = urlPart[0];

            if (urlPart.length > 1) {
                String[] params = urlPart[1].split("&");

                int i = 0;
                for (String a : params) {
                    if (i == 0) {
                        urlResult += "?";
                        i++;
                    } else {
                        urlResult += "&";
                    }
                    String[] value = a.split("=");
                    urlResult += URLEncoder.encode(value[0], "UTF-8") + "=" + URLEncoder.encode(value[1], "UTF-8");
                }
            }
        } catch (Exception e) {
            LogUtil.error(StringUtil.class.getName(), e, "");
        }

        return urlResult;
    }

    public static String escapeRegex(String inStr) {
        return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
    }
    
    public static String escapeString(String inStr, String format, Map<String, String> replaceMap){
        if(replaceMap != null){
            Iterator it = replaceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                inStr = inStr.replaceAll(pairs.getKey(), escapeRegex(pairs.getValue()));
            }
        }
        
        if(format == null){
            return inStr;
        }
        if(format.equals(TYPE_REGEX)){
            return escapeRegex(inStr);
        }
        if(format.equals(TYPE_JSON)){
            return escapeRegex(JSONObject.escape(inStr));
        }
        return inStr;
    }

    public class IgnoreCaseComparator implements Comparator<String> {
        public int compare(String strA, String strB) {
            return strA.compareToIgnoreCase(strB);
        }
    }

    public static String md5(String content) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = content.getBytes();
            m.update(data, 0, data.length);
            BigInteger i = new BigInteger(1, m.digest());
            return String.format("%1$032X", i);
        } catch (Exception ex) {
            LogUtil.error(StringUtil.class.getName(), ex, "");
        }
        return "";
    }

    public static String md5Base16(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(content.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                String hex = Integer.toHexString((int) 0x00FF & b);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
