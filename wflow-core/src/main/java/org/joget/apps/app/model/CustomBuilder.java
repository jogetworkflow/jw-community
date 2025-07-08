package org.joget.apps.app.model;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface to provide additional builder to platform
 * 
 */
public interface CustomBuilder {
    
    /**
     * The classname of the builder implementation
     * @return 
     */
    public String getClassName();
    
    /**
     * The unique shortname of the builder
     * @return 
     */
    public String getName();
    
    /**
     * The unique label of the builder
     * @return 
     */
    public String getLabel();
    
    /**
     * The unique fontawesome icon of the builder
     * @return 
     */
    public String getIcon();
    
    /**
     * The unique color code of the builder
     * @return 
     */
    public String getColor();
    
    /**
     * The unique shortname of output of the builder
     * @return 
     */
    public String getObjectName();
    
    /**
     * The unique label of output of the builder
     * @return 
     */
    public String getObjectLabel();
    
    /**
     * The prefix for created object id
     * @return 
     */
    public String getIdPrefix();
    
    /**
     * The builder configuration in JSON format
     * @return 
     */
    public String getBuilderConfig();
    
    /**
     * The resource bundle file path
     * @return 
     */
    public String getResourceBundlePath();
    
    /**
     * populate the builder preview page
     * @param json
     * @param request
     * @param response 
     */
    public void builderPreview(String json, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * The JS files used in the builder
     * @param contextPath
     * @param buildNumber
     * @return 
     */
    public String getBuilderJS(String contextPath, String buildNumber);
    
    /**
     * The CSS files used in the builder
     * @param contextPath
     * @param buildNumber
     * @return 
     */
    public String getBuilderCSS(String contextPath, String buildNumber);
    
    /**
     * The HTML of builder 
     * @param def
     * @param json
     * @param request
     * @param response
     * @return 
     */
    public String getBuilderHTML(BuilderDefinition def, String json, HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Retrieve the builder end result based on json definition
     * @param json
     * @param config
     * @return 
     */
    public Object getBuilderResult(String json, Map<String, Object> config);
    
    /**
     * HTML to add to the create new page
     * @return 
     */
    public String getCreateNewPageHtml();
    
    /**
     * Used to create the JSON definition of new object
     * @param id
     * @param name
     * @param description
     * @param copyDef
     * @return 
     */
    public String createNewJSON(String id, String name, String description, BuilderDefinition copyDef);
    
    /**
     * Retrieve name from the JSON definition
     * @param json
     * @return 
     */
    public String getNameFromJSON(String json);
    
    /**
     * Retrieve description from the JSON definition
     * @param json
     * @return 
     */
    public String getDescriptionFromJSON(String json);
    
}
