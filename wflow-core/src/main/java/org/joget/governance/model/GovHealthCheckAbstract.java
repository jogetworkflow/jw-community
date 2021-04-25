package org.joget.governance.model;

import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class GovHealthCheckAbstract extends ExtDefaultPlugin implements GovHealthCheck {
    
    @Override
    public boolean isConfigurable() {
        String options = getPropertyOptions();
        return options != null && !options.isEmpty();
    }
}
