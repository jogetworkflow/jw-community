package org.joget.apps.app.model;

import java.util.Date;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Metadata for a custom builder.
 */
@Root
public class BuilderDefinition extends AbstractAppVersionedObject {

    @NotBlank
    @Element(required = false)
    private String name;
    @NotBlank
    @Element(required = false)
    private String type;
    @Element(required = false)
    private String json;
    @Element(required = false)
    private String description;
    @Element(required = false)
    private Date dateCreated;
    @Element(required = false)
    private Date dateModified;

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getName() {
        return (name == null)?"":name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
