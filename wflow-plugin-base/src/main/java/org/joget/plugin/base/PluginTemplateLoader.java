package org.joget.plugin.base;

import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class PluginTemplateLoader implements TemplateLoader {
    
    private String template;
    private long lastModified;
    
    public PluginTemplateLoader(String template) {
        this.template = template;
        this.lastModified = System.currentTimeMillis();
    }
    
    @Override
    public Object findTemplateSource(String name) {
        return template;
    }

    @Override
    public long getLastModified(Object templateSource) {
        return lastModified;
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return new StringReader(template);
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
        
    }
}
