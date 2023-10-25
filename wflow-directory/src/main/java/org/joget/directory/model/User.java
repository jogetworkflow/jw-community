package org.joget.directory.model;

import org.joget.commons.spring.model.Auditable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Hibernate;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.model.service.DirectoryUtil;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;
import org.springmodules.validation.bean.conf.loader.annotation.handler.RegExp;

public class User implements Serializable, Auditable {

    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    private String id;
    @NotBlank
    @RegExp(value = "^[\\.@0-9a-zA-Z_\\+-]+$")
    private String username;
    private String password;
    @NotBlank
    private String firstName;
    private String lastName;
    private String email;
    private Integer active;
    private String timeZone;
    private String locale;
    //join
    private Set roles;
    private Set groups;
    private Set employments;
    //additional field
    private String oldPassword;
    private String confirmPassword;
    private Boolean readonly = false;
    public static final String LOGIN_HASH_DELIMINATOR = "::";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZoneLabel() {
        return TimeZoneUtil.getList().get(getTimeZone());
    }

    public Set getRoles() {
        return roles;
    }

    public void setRoles(Set roles) {
        this.roles = roles;
    }

    public Set getGroups() {
        return groups;
    }

    public void setGroups(Set groups) {
        this.groups = groups;
    }

    public Set getEmployments() {
        //to handled no hibernate session issue in session replication of WorkflowUserDetails
        try {
            Hibernate.initialize(employments);
        } catch (Exception e){
            EmploymentDao dao = (EmploymentDao) DirectoryUtil.getApplicationContext().getBean("employmentDao");
            employments = new HashSet();
            Collection<Employment> temp = dao.findEmployments("where e.user.username = ?", new String[]{username}, null, null, null, null);
            if (temp != null && !temp.isEmpty()) {
                employments.addAll(temp);
            }
        }
        return employments;
    }

    public void setEmployments(Set employments) {
        this.employments = employments;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public Boolean getReadonly() {
        if (!readonly && getPassword() == null) {
            readonly = true;
        }
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public String getAuditTrailId() {
        return username;
    }

    public String getLoginHash() {
        return StringUtil.md5(username + LOGIN_HASH_DELIMINATOR + password);
    }
}
