package org.joget.apps.app.model;

import java.util.Collection;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Metadata definition for an App. An App that consists of a workflow package, forms, lists, userviews.
 */
@Root
public class AppDefinition extends AbstractVersionedObject {

    @ElementList(required = false)
    private Collection<PackageDefinition> packageDefinitionList;
    @ElementList(required = false)
    private Collection<FormDefinition> formDefinitionList;
    @ElementList(required = false)
    private Collection<UserviewDefinition> userviewDefinitionList;
    @ElementList(required = false)
    private Collection<DatalistDefinition> datalistDefinitionList;
    @ElementList(required = false)
    private Collection<PluginDefaultProperties> pluginDefaultPropertiesList;
    @ElementList(required = false)
    private Collection<EnvironmentVariable> environmentVariableList;
    @ElementList(required = false)
    private Collection<Message> messageList;
    private Boolean published;

    /**
     * For an App, the package ID is equivalent to the ID.
     * @return
     */
    @Override
    public String getAppId() {
        return getId();
    }

    @Override
    public void setAppId(String id) {
        super.setAppId(id);
        setId(id);
    }

    /**
     * Convenience method to return the first package definition for this app.
     * @return null if there is none defined.
     */
    public PackageDefinition getPackageDefinition() {
        PackageDefinition packageDef = null;
        Collection<PackageDefinition> list = getPackageDefinitionList();
        if (list != null && !list.isEmpty()) {
            packageDef = list.iterator().next();
        }
        return packageDef;
    }

    public Collection<PackageDefinition> getPackageDefinitionList() {
        return packageDefinitionList;
    }

    public void setPackageDefinitionList(Collection<PackageDefinition> packageDefinitionList) {
        this.packageDefinitionList = packageDefinitionList;
    }

    public Collection<DatalistDefinition> getDatalistDefinitionList() {
        return datalistDefinitionList;
    }

    public void setDatalistDefinitionList(Collection<DatalistDefinition> datalistDefinitionList) {
        this.datalistDefinitionList = datalistDefinitionList;
    }

    public Collection<FormDefinition> getFormDefinitionList() {
        return formDefinitionList;
    }

    public void setFormDefinitionList(Collection<FormDefinition> formDefinitionList) {
        this.formDefinitionList = formDefinitionList;
    }

    public Collection<EnvironmentVariable> getEnvironmentVariableList() {
        return environmentVariableList;
    }

    public void setEnvironmentVariableList(Collection<EnvironmentVariable> environmentVariableList) {
        this.environmentVariableList = environmentVariableList;
    }

    public Collection<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(Collection<Message> messageList) {
        this.messageList = messageList;
    }

    public Collection<PluginDefaultProperties> getPluginDefaultPropertiesList() {
        return pluginDefaultPropertiesList;
    }

    public void setPluginDefaultPropertiesList(Collection<PluginDefaultProperties> pluginDefaultPropertiesList) {
        this.pluginDefaultPropertiesList = pluginDefaultPropertiesList;
    }

    public Collection<UserviewDefinition> getUserviewDefinitionList() {
        return userviewDefinitionList;
    }

    public void setUserviewDefinitionList(Collection<UserviewDefinition> userviewDefinitionList) {
        this.userviewDefinitionList = userviewDefinitionList;
    }

    public boolean isPublished() {
        if (published == null) {
            return false;
        }
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}
