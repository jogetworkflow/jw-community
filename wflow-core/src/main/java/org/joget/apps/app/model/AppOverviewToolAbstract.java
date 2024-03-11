package org.joget.apps.app.model;

import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class AppOverviewToolAbstract extends ExtDefaultPlugin implements AppOverviewTool{
    
    /**
     * Overview tool does not need to configure
     * 
     * @return 
     */
    @Override
    public String getPropertyOptions() {
        return "";
    }
}
