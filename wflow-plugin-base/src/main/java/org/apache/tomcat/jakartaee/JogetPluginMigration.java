package org.apache.tomcat.jakartaee;

import java.lang.reflect.Field;
import java.util.List;
import org.joget.commons.util.LogUtil;

public class JogetPluginMigration extends Migration {
    
    public JogetPluginMigration() {
        super();
        
        try {
            Field field = Migration.class.getDeclaredField("converters");
            field.setAccessible(true); // Bypass private access
            List<Converter> converters = (List<Converter>) field.get(this);
            
            //change the manifest coverter
            converters.set(2, new JogetPluginManifestConverter());
        } catch (Exception e) {
            LogUtil.error(JogetPluginMigration.class.getName(), e, "");
        }
    }
}
