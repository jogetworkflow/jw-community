package org.joget.commons.util;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.util.ReflectionUtils;

public class StringUtil {

    public static String TYPE_REGEX = "regex";
    public static String TYPE_JSON = "json";

    static final Whitelist whitelistRelaxed;
    static {
        // configure jsoup whitelist
        whitelistRelaxed = Whitelist.relaxed().addTags("span", "div").addAttributes(":all","id","style","class","title","target");
        java.lang.reflect.Field field = ReflectionUtils.findField(whitelistRelaxed.getClass(), "protocols");
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, whitelistRelaxed, new HashMap());
    }

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
                        String key = URLDecoder.decode(param[0], "UTF-8");
                        String value = URLDecoder.decode(param[1], "UTF-8");
                        
                        String[] values = (String[]) result.get(key);
                        if (values != null) {
                            List temp = Arrays.asList(values);
                            temp.add(value);
                            values = (String[]) temp.toArray(new String[0]);
                        } else {
                            values = new String[]{value};
                        }
                        
                        result.put(key, values);
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
                for (String value : values) {
                    queryString += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                }
            }
            if (queryString.endsWith("&")) {
                queryString = queryString.substring(0, queryString.length()-1);
            }
        } catch (Exception e) {
        }
        return queryString;
    }

    public static String escapeRegex(String inStr) {
        return (inStr != null) ?  inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1") : null;
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
    
    public static String stripAllHtmlTag(String content) {
        if (content != null && !content.isEmpty()) {
            content = Jsoup.clean(content, Whitelist.none());
        }
        return content;
    }
    
    public static String stripHtmlTag(String content, String[] allowedTag) {
        if (content != null && !content.isEmpty()) {
            Whitelist whitelist = Whitelist.none().addAttributes(":all","style","class","title","id","src","href","target");
            for (String tag : allowedTag) {
                whitelist.addTags(tag);
            }
            java.lang.reflect.Field field = ReflectionUtils.findField(whitelist.getClass(), "protocols");
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, whitelist, new HashMap());
            content = Jsoup.clean(content, whitelist);
        }
        return content;
    }
    
    public static String stripHtmlRelaxed(String content) {
        if (content != null && content.indexOf("<") >= 0) {
            content = Jsoup.clean(content, whitelistRelaxed);
        }
        return content;
    }
    
    public static String encryptContent(String content) {
        //parse content
        if (content != null && content.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(content);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }

            try {
                if (!sList.isEmpty()) {
                    for (String s : sList) {
                        String tempS = s.replaceAll(SecurityUtil.ENVELOPE, "");
                        tempS = SecurityUtil.encrypt(tempS);

                        content = content.replaceAll(s, tempS);
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(StringUtil.class.getName(), ex, "");
            }
        }

        return content;
    }

    public static String decryptContent(String content) {
        //parse content
        if (content != null && content.contains(SecurityUtil.ENVELOPE)) {
            Pattern pattern = Pattern.compile(SecurityUtil.ENVELOPE + "((?!" + SecurityUtil.ENVELOPE + ").)*" + SecurityUtil.ENVELOPE);
            Matcher matcher = pattern.matcher(content);
            Set<String> sList = new HashSet<String>();
            while (matcher.find()) {
                sList.add(matcher.group(0));
            }

            try {
                if (!sList.isEmpty()) {
                    for (String s : sList) {
                        String tempS = SecurityUtil.decrypt(s);
                        content = content.replaceAll(StringUtil.escapeRegex(s), tempS);
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(StringUtil.class.getName(), ex, "");
            }
        }

        return content;
    }
    
    public static byte[] searchAndReplaceByteContent(byte[] bytes, String search, String replacement) {
        if (search != null && replacement != null) {
            try {
                String content = new String(bytes, "UTF-8");

                content = content.replaceAll(search, replacement);
                bytes = content.getBytes("UTF-8");
            } catch (Exception e) {
                //ignore
            }
        }
        return bytes;
    }
    
    public static byte[] searchAndReplaceByteContent(byte[] bytes, Map<String, String> replacements) {
        if (replacements != null && !replacements.isEmpty()) {
            try {
                String content = new String(bytes, "UTF-8");
                
                for (String search : replacements.keySet()) {
                    content = content.replaceAll(search, replacements.get(search));
                }
                bytes = content.getBytes("UTF-8");
            } catch (Exception e) {
                //ignore
            }
        }
        return bytes;
    }
    
    public static boolean validateEmail(String email, boolean multiple) {
        String[] emails;
        if (multiple) {
            emails = email.split(";");
        } else {
            emails = new String[]{email};
        }
        
        boolean valid = true;
        for (String e : emails) {
            String expression = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-+]+)*(\\.[A-Za-z-]{2,})$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(e.trim());
            if (!matcher.matches()) {
                valid = false;
                break;
            }
        }
                
        return valid;        
    }
}
