package org.joget.commons.util;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HashMap;
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
                urlResult += "?" + constructUrlQueryString(getUrlParams(urlPart[1]));
            }
        } catch (Exception e) {
            LogUtil.error(StringUtil.class.getName(), e, "");
        }

        return urlResult;
    }

    public static String mergeRequestQueryString(String queryString1, String queryString2) {
        if (queryString1 == null || queryString2 == null) {
            return queryString1;
        }

        Map<String, String[]> params = getUrlParams(queryString1);
        params.putAll(getUrlParams(queryString2));

        return constructUrlQueryString(params);
    }

    public static String addParamsToUrl(String url, String paramKey, String paramValue) {
        return addParamsToUrl(url, paramKey, new String[]{paramValue});
    }

    public static String addParamsToUrl(String url, String paramKey, String[] paramValues) {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(paramKey, paramValues);
        return addParamsToUrl(url, params);
    }

    public static String addParamsToUrl(String url, Map<String, String[]> params) {
        String urlResult = url;
        try {
            String[] urlPart = urlResult.split("\\?");

            urlResult = urlPart[0];
            Map<String, String[]> resultParams = new HashMap<String, String[]>();

            if (urlPart.length > 1) {
                resultParams.putAll(getUrlParams(urlPart[1]));
            }
            resultParams.putAll(params);

            if (!resultParams.isEmpty()) {
                urlResult += "?" + constructUrlQueryString(resultParams);
            }
        } catch (Exception e) {
            LogUtil.error(StringUtil.class.getName(), e, "");
        }

        return urlResult;
    }

    private static Map<String, String[]> getUrlParams(String url) {
        Map<String, String[]> result = new HashMap<String, String[]>();
        try {
            String queryString = url;
            if (url.contains("?")) {
                queryString = url.substring(url.indexOf("?") + 1);
            }
            if (queryString.length() > 1) {
                String[] params = queryString.split("&");

                for (String a : params) {
                    if (!a.isEmpty()) {
                        String[] param = a.split("=");
                        String[] values = param[1].split(",");
                        for (int i = 0; i < values.length; i++) {
                            values[i] = URLDecoder.decode(values[i], "UTF-8");
                        }
                        result.put(URLDecoder.decode(param[0], "UTF-8"), values);
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String constructUrlQueryString(Map<String, String[]> params) {
        String queryString = "";
        try {
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                queryString += URLEncoder.encode(key, "UTF-8") + "=";

                String paramValues = "";
                for (String value : values) {
                    paramValues += URLEncoder.encode(value, "UTF-8") + ",";
                }
                if (paramValues.endsWith(",")) {
                    paramValues = paramValues.substring(0, paramValues.length()-1);
                }

                queryString += paramValues + "&";
            }
            if (queryString.endsWith("&")) {
                queryString = queryString.substring(0, queryString.length()-1);
            }
        } catch (Exception e) {
        }
        return queryString;
    }

    public static String escapeRegex(String inStr) {
        return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
    }

    public static String escapeString(String inStr, String format, Map<String, String> replaceMap) {
        if (replaceMap != null) {
            Iterator it = replaceMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
                inStr = inStr.replaceAll(pairs.getKey(), escapeRegex(pairs.getValue()));
            }
        }

        if (format == null) {
            return inStr;
        }
        if (format.equals(TYPE_REGEX)) {
            return escapeRegex(inStr);
        }
        if (format.equals(TYPE_JSON)) {
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
