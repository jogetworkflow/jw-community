package org.joget.plugin.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Used to add menu to admin console and render the page for the added menu.
 */
public interface ConsolePagePlugin {
    
    /**
     * Annotation to put on method to mark the method as additional URL path 
     * and using the method to handle the path request.
     * 
     * The value is using AntPathMatcher syntax
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Path {
        public String[] value() default {}; // using AntPathMatcher syntax
    }
    
    /**
     * Annotation to put on path method parameter to mark a parameter to receive the value in path pattern
     * eg. the path is `/form/{id}`, then using @PathParam("id") for parameter to retrieve the value 
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface PathParam {
        public String value(); //the path paramter name
    }
    
    public enum Location {
        DIRECTORY,
        MONITOR,
        SETTINGS
    };
    
    /**
     * Unique identifier of the page without space 
     * 
     * @return 
     */
    public String getName();
    
    /**
     * The icon used for menu rendering
     * 
     * @return 
     */
    public String getPluginIcon();
    
    
    /**
     * The label used for menu rendering
     * 
     * @return 
     */
    public String getLabel();
    
    /**
     * The order of the menu. The original page menu having order with position x 100. eg 100, 200, 300.
     * 
     * @return 
     */
    public int getOrder();
    
    /**
     * The location to render the menu
     * 
     * @return 
     */
    public Location getLocation();
    
    /**
     * Used to decide the current user having permission to see this menu
     * 
     * @return 
     */
    public boolean isAuthorized();
    
    /**
     * return the HTML of the page rendering
     * 
     * @param request
     * @param response
     * @return the HTML to render for the console page
     */
    public String render(HttpServletRequest request, HttpServletResponse response);
}
