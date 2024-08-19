package org.joget.directory.model.idp;

public abstract class AbstractMultiInstanceIdentityProviderPlugin extends AbstractIdentityProviderPlugin implements MultiInstanceIdentityProviderPlugin {
    @Override
    public String getConfigName() {
        String instanceName = getPropertyString("configName");
        if (instanceName.isEmpty()) {
            instanceName = getName();
        }
        return instanceName;
    }
}
