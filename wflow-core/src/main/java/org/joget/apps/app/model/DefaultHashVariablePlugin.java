package org.joget.apps.app.model;

import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DefaultHashVariablePlugin extends ExtDefaultPlugin implements HashVariablePlugin {
    public String escapeHashVariable(String variable) {
        return variable;
    }
}
