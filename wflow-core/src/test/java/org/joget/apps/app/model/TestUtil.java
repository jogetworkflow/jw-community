package org.joget.apps.app.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormService;

public class TestUtil {
    
    public static Form getForm(String name, FormData formData) throws IOException {
        String json = readFile("/forms/" + name + ".json");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        return formService.loadFormFromJson(json, formData);
    }
    
    public static  String readFile(String filePath) throws IOException {
        // deploy package
        BufferedReader reader = null;
        String fileContents = "";
        String line;
        try {
            InputStream in = TestUtil.class.getResourceAsStream(filePath);
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in));
                while ((line = reader.readLine()) != null) {
                    fileContents += line + "\n";
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return fileContents;
    }
    
    public static AppDefinition createAppDefinition(String id, Long version) {
        // create test app
        AppDefinition appDef = new AppDefinition();
        appDef.setId(id);
        appDef.setVersion(version);
        appDef.setName(id);

        // save test app
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        appService.createAppDefinition(appDef);

        return appDef;
    }
    
    public static FormDefinition createFormDefinition(AppDefinition appDef, String formId, String tableName, String fileName) throws IOException {
        // create test form
        FormDefinition formDef = new FormDefinition();
        formDef.setId(formId);
        formDef.setAppId(appDef.getAppId());
        formDef.setAppVersion(appDef.getVersion());
        formDef.setAppDefinition(appDef);
        formDef.setName(formId);
        formDef.setTableName(tableName);
        String jsonFileName = "/forms/" + fileName + ".json";
        String formJson = readFile(jsonFileName);
        if (formJson == null || formJson.trim().isEmpty()) {
            formJson = "{ \"className\":\"org.joget.apps.form.model.Form\" }";
        }
        formDef.setJson(formJson);

        // save test form
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        appService.createFormDefinition(appDef, formDef);

        return formDef;
    }
}
