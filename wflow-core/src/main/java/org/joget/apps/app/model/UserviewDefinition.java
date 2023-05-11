package org.joget.apps.app.model;

import java.util.Date;
import org.joget.commons.util.StringUtil;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

/**
 * Metadata for a userview.
 */
@Root
public class UserviewDefinition extends AbstractAppVersionedObject {

    @NotBlank
    @Element(required = false)
    private String name;
    @Element(required = false)
    private String description;
    @Element(required = false)
    private String json;
    @Element(required = false)
    private Date dateCreated;
    @Element(required = false)
    private Date dateModified;
    @Element(required = false)
    private String thumbnail;
    @Element(required = false)
    private String category;

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

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getName() {
        return name;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
