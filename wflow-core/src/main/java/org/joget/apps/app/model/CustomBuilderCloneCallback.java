package org.joget.apps.app.model;

import java.util.List;

/**
 * This interface allows CustomBuilders to handle callbacks during
 * cloning of an app to allow modification of builder definitions.
 *
 * <p>This is particularly useful for instances where the builder
 * definition's contents should be changed due to logical or schematic
 * constraints such as unique builder definition IDs.</p>
 */
public interface CustomBuilderCloneCallback {

    /**
     * Processing to be done before cloning
     * @param definitions an unmodifiable list of CustomBuilder's definitions to be processed
     * @param appDefinition the AppDefinition currently being processed
     */
    void onClone(List<BuilderDefinition> definitions, AppDefinition appDefinition);
}
