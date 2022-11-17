package org.joget.apps.app.model;

import java.util.Collection;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Metadata definition for an App. An App that consists of a workflow package, forms, lists, userviews.
 */
@Root
public class AppDefinition extends AbstractVersionedObject {

    @Path("packageDefinitionList")
    @ElementList(required = false,inline=true)
    private Collection<PackageDefinition> packageDefinitionList;

    @Path("formDefinitionList")
    @ElementList(required = false,inline=true)
    private Collection<FormDefinition> formDefinitionList;

    @Path("userviewDefinitionList")
    @ElementList(required = false,inline=true)
    private Collection<UserviewDefinition> userviewDefinitionList;

    @Path("datalistDefinitionList")
    @ElementList(required = false,inline=true)
    private Collection<DatalistDefinition> datalistDefinitionList;

    @Path("builderDefinitionList")
    @ElementList(required = false,inline=true)
    private Collection<BuilderDefinition> builderDefinitionList;

    @Path("pluginDefaultPropertiesList")
    @ElementList(required = false,inline=true)
    private Collection<PluginDefaultProperties> pluginDefaultPropertiesList;

    @Path("environmentVariableList")
    @ElementList(required = false,inline=true)
    private Collection<EnvironmentVariable> environmentVariableList;

    @Path("messageList")
    @ElementList(required = false,inline=true)
    private Collection<Message> messageList;
    
    @Path("resourceList")
    @ElementList(required = false,inline=true)
    private Collection<AppResource> resourceList;
    
    private Boolean published;
    @Element(required = false)
    private String license;
    @Element(required = false)
    private String description;
    @Element(required = false)
    private String meta;

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
    
    public Collection<AppResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(Collection<AppResource> resourceList) {
        this.resourceList = resourceList;
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
    
    public Collection<BuilderDefinition> getBuilderDefinitionList() {
        return builderDefinitionList;
    }

    public void setBuilderDefinitionList(Collection<BuilderDefinition> builderDefinitionList) {
        this.builderDefinitionList = builderDefinitionList;
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

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "{" + "id=" + getId() + ", version=" + getVersion() + ", published=" + published + '}';
    }
}
