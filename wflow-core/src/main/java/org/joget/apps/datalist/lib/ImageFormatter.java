package org.joget.apps.datalist.lib;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormatDefault;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.service.FileUtil;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;

public class ImageFormatter extends DataListColumnFormatDefault{
    
    @Override
    public String getName() {
        return "Image Formatter";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Image Formatter";
    }

    @Override
    public String getLabel() {
        return "Image Formatter";
    }
    
    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/imageFormatter.json", null, true, null);
    }

    @Override
    public String format(DataList dataList, DataListColumn column, Object row, Object value) {
        String result = "";
        
        String height = getPropertyString("height");
        String width = getPropertyString("width");
        String style = "";
        
        if(!height.isEmpty() && !width.isEmpty()) {
            if (StringUtils.isNumeric(width)){
                width = width + "px";
            }
            
            if (height.endsWith("%")) {               
                style = "padding-top:"+height+";height:0px;width:"+width+";background-size:cover;background-repeat: no-repeat;display:inline-block;";          
            } else {
                if (StringUtils.isNumeric(height)) {
                    height = height + "px";
                }             
                style = "height:"+height+";width:"+width+";background-size:cover;background-repeat: no-repeat;display:inline-block;";
            }
        }
        
        String fullsize = getPropertyString("imagefullsize");
        
        if (value != null && !((String) value).isEmpty()) {
            String imageSrc = getPropertyString("imageSrc");
            
            if ("form".equalsIgnoreCase(imageSrc)) {
                String formDefId = getPropertyString("formDefId");
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                
                String id = (String) DataListService.evaluateColumnValueFromRow(row, dataList.getBinder().getPrimaryKeyColumnName());
                
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

                //suport for multi values
                for (String v : value.toString().split(";")) {
                    if (!v.isEmpty()) {
                        // determine actual path for the file uploads
                        String fileName = v;
                        String encodedFileName = fileName;
                        boolean thumbnailExist = false;

                        try {
                            encodedFileName = URLEncoder.encode(fileName, "UTF8").replaceAll("\\+", "%20");
                        } catch (UnsupportedEncodingException ex) {
                            // ignore
                        }
                        
                        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
                        
                        try {
                            File thumbnail = FileUtil.getFile(encodedFileName + FileManager.THUMBNAIL_EXT, appService.getFormTableName(appDef, formDefId), id);

                            // Use thumbnail image to load in list
                            thumbnailExist = thumbnail.exists();
                        } catch (Exception e) {
                            LogUtil.error(FileUtil.class.getName(), e, encodedFileName);
                        }

                        // Add timestamp to the image URL to avoid caching issues
                        String timestamp = ".?timestamp=" + String.valueOf(System.currentTimeMillis());
                        String suffix = "";    
                        
                        String imgPath = request.getContextPath() + "/web/client/app/" + appDef.getAppId() + "/" + appDef.getVersion().toString() + "/form/download/" + formDefId + "/" + id + "/" + encodedFileName;
                      
                        if (!result.isEmpty()) {
                            result += " ";
                        }
                        
                        if(!fullsize.isEmpty()){
                            result += "<a href=\""+imgPath + timestamp + "\" target=\"_blank\" \"> "; 
                        }
                          
                        if (thumbnailExist) {
                            suffix = FileManager.THUMBNAIL_EXT;
                        }
                        
                        imgPath += suffix + timestamp;
                        
                        if(!height.isEmpty() && !width.isEmpty()){
                            result += "<div style=\"background-image:url('"+imgPath+"');"+style+"\" /></div>";  
                        }else{
                            result += "<img src=\""+imgPath+"\" />";
                        }
                        
                        if(!fullsize.isEmpty()){
                            result += "</a> ";   
                        }
                         
                    }
                }
            } else {
                //suport for multi values
                for (String v : value.toString().split(";")) {
                    if (!v.isEmpty() && v.contains(".")) {
                        if (!result.isEmpty()) {
                            result += " ";
                        }

                        // Add timestamp to the image URL to avoid caching issues
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String imgPathWithTimestamp = v + "?timestamp=" + timestamp;

                        if (!fullsize.isEmpty()) {
                            result += "<a href=\"" + imgPathWithTimestamp + "\" target=\"_blank\" \"> ";
                        }

                        if (!height.isEmpty() && !width.isEmpty()) {
                            result += "<div style=\"background-image:url('" + imgPathWithTimestamp + "');" + style + "\" /></div>";
                        } else {
                            result += "<img src=\"" + imgPathWithTimestamp + "\"/>";
                        }
                        
                        if(!fullsize.isEmpty()){
                            result += "</a> ";                            
                        }
                    }
                }
            }
        }
        
        if (result.isEmpty() && !getPropertyString("defaultImage").isEmpty()) {

            String timestamp = String.valueOf(System.currentTimeMillis());
            String defaultImgPath = getPropertyString("defaultImage") + "?timestamp=" + timestamp;

            if (!fullsize.isEmpty()) {
                result += "<a href=\"" + defaultImgPath + "\" target=\"_blank\" \"> ";
            }

            if (!height.isEmpty() && !width.isEmpty()) {
                result += "<div style=\"background-image:url('" + defaultImgPath + "');" + style + "\" /></div>";
            } else {
                result += "<img src=\"" + defaultImgPath + "\" " + style + ">";
            }
            
            if(!fullsize.isEmpty()){
                result += "</a> ";                            
            }
        }

        return result;
    }
}
