package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class RequestHashVariable extends DefaultHashVariablePlugin {
    protected static Collection<String> list;

    @Override
    public String processHashVariable(String variableKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        if (request != null) {
            String syntax = getPrefix() + ".";
            String attribute = variableKey;
            String headerName = null;
            
            if (variableKey.contains(".")) {
                String[] temp = variableKey.split(".");
                attribute = variableKey.substring(0, variableKey.indexOf("."));
                headerName = variableKey.substring(variableKey.indexOf(".") + 1);
                syntax += attribute + ".NAME";
            } else {
                syntax += attribute;
            }
            
            if (isValid(syntax)) {
                if (headerName != null) {
                    String value = request.getHeader(headerName);
                    return (value != null)?value:"";
                } else if ("domainURL".equalsIgnoreCase(attribute) || "baseURL".equalsIgnoreCase(attribute)) {
                    String baseUrl = request.getScheme() + "://" + request.getServerName();
                    int port = request.getServerPort();
                    if (!((port == 80 && "http".equalsIgnoreCase(request.getScheme())) || (port == 443 && "https".equalsIgnoreCase(request.getScheme())))) {
                        baseUrl += ":" + Integer.toString(port);
                    }
                    if ("baseURL".equalsIgnoreCase(attribute)) {
                        baseUrl += request.getContextPath();
                    }
                    return baseUrl;
                } else {
                    try {
                        //convert first character to upper case
                        char firstChar = attribute.charAt(0);
                        firstChar = Character.toUpperCase(firstChar);
                        attribute = firstChar + attribute.substring(1, attribute.length());
                        Method method = HttpServletRequest.class.getMethod("get" + attribute, new Class[]{});
                        Object returnResult = ((Object) method.invoke(request, new Object[]{}));
                        
                        if ("queryString".equals(variableKey) && returnResult == null) {
                            return "";
                        }
                        
                        if ("requestURI".equals(variableKey) || "requestURL".equals(variableKey)) {
                            String forwardUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
                            if (forwardUri != null) {
                                if ("requestURI".equals(variableKey)) {
                                    returnResult = forwardUri;
                                } else {
                                    String uri = request.getRequestURI();
                                    returnResult = returnResult.toString().replace(uri, forwardUri);
                                }
                            }
                        }
                        
                        if (returnResult != null) {
                            return returnResult.toString();
                        }
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
        }
        return null;
    }
    
    protected boolean isValid(String syntax) {
        Collection<String> syntaxList = availableSyntax();
        return syntaxList.contains(syntax);
    }
    
    @Override
    public Collection<String> availableSyntax() {
        if (list == null) {
            list = new ArrayList<String>();

            list.add(getPrefix() + ".characterEncoding");
            list.add(getPrefix() + ".contextPath");
            list.add(getPrefix() + ".header.NAME");
            list.add(getPrefix() + ".locale");
            list.add(getPrefix() + ".method");
            list.add(getPrefix() + ".pathInfo");
            list.add(getPrefix() + ".protocol");
            list.add(getPrefix() + ".queryString");
            list.add(getPrefix() + ".remoteAddr");
            list.add(getPrefix() + ".requestURI");
            list.add(getPrefix() + ".requestURL");
            list.add(getPrefix() + ".requestedSessionId");
            list.add(getPrefix() + ".scheme");
            list.add(getPrefix() + ".serverName");
            list.add(getPrefix() + ".serverPort");
            list.add(getPrefix() + ".servletPath");
            list.add(getPrefix() + ".baseURL");
            list.add(getPrefix() + ".domainURL");
        }
        return list;
    }

    public String getName() {
        return "Request Hash Variable";
    }

    public String getPrefix() {
        return "request";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Request Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
