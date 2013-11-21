package org.joget.directory.model;

import org.joget.commons.spring.model.Auditable;
import java.util.Set;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

public class Organization implements Auditable {

    @NotBlank
    @RegExp(value = "^[0-9a-zA-Z_-]+$")
    private String id;
    @NotBlank
    private String name;
    private String description;
    private String parentId;
    //join
    private Set departments;
    private Set groups;
    private Set grades;
    private Set employments;
    private Set childrens;
    private Organization parent;
    private Boolean readonly = false;

    public Organization getParent() {
        return parent;
    }

    public void setParent(Organization parent) {
        this.parent = parent;
    }

    public Set getGroups() {
        return groups;
    }

    public void setGroups(Set groups) {
        this.groups = groups;
    }

    public Set getEmployments() {
        return employments;
    }

    public void setEmployments(Set employments) {
        this.employments = employments;
    }

    public Set getChildrens() {
        return childrens;
    }

    public void setChildrens(Set childrens) {
        this.childrens = childrens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Set getDepartments() {
        return departments;
    }

    public void setDepartments(Set departments) {
        this.departments = departments;
    }

    public Set getGrades() {
        return grades;
    }

    public void setGrades(Set grades) {
        this.grades = grades;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public String getAuditTrailId() {
        return id;
    }
}
