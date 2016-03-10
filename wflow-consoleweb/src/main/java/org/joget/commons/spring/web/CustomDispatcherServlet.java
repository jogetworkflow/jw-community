package org.joget.commons.spring.web;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Overrides Spring's DispatcherServlet to support re-initialization of the 
 * ApplicationContext if previous attempts fail.
 */
public class CustomDispatcherServlet extends DispatcherServlet {

    private static CustomDispatcherServlet customDispatcherServlet;

    public static CustomDispatcherServlet getCustomDispatcherServlet() {
        return customDispatcherServlet;
    }
    
    public CustomDispatcherServlet() {
        customDispatcherServlet = this;
    }

    public CustomDispatcherServlet(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
        customDispatcherServlet = this;
    }
    
}
