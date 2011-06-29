package org.joget.workflow.model;

import java.util.Map;
import org.joget.plugin.base.DefaultPlugin;

public abstract class DefaultParticipantPlugin extends DefaultPlugin implements ParticipantPlugin {

    public final Object execute(Map props) {
        return getActivityAssignments(props);
    }
}
