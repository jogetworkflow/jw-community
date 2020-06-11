package org.joget.apps.app.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
}
