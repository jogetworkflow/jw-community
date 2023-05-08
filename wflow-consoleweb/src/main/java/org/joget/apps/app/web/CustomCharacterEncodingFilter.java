package org.joget.apps.app.web;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.CharacterEncodingFilter;

public class CustomCharacterEncodingFilter extends CharacterEncodingFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String url = (request.getRequestURL() != null)?request.getRequestURL().toString():"";
        
        //to support old url ends with .
        if (url.endsWith(".")) {
            url = url.substring(0, url.length() - 1);
        }
        
        //get last part of url
        if (url.contains("/")) {
            url = url.substring(url.lastIndexOf("/"));
        }
        if (request.getSession() != null && request.getSession().getServletContext() != null &&
                url.contains(".") && !(url.endsWith(".js") || url.endsWith(".css") ||
                url.endsWith(".html") || url.endsWith(".txt") || url.endsWith(".xml") ||
                url.endsWith(".json"))) { 
            //possible an application file
            String ct = request.getSession().getServletContext().getMimeType(url);
            if (ct != null && ct.startsWith("application/") && 
                    !(ct.contains("xml") || ct.contains("json") || 
                    ct.contains("javascript") || ct.contains("yaml"))) {
                //do not set the Character Encoding
                filterChain.doFilter(request, response);
            } else {
                super.doFilterInternal(request, response, filterChain);
            }
        } else {
            super.doFilterInternal(request, response, filterChain);
        }
    }
}
