package org.joget.directory.model;

import org.joget.commons.spring.model.Auditable;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class Employment implements Serializable, Auditable {

    public static final String MANAGER = "Manager";
    private String id;
    private String userId;
    private Date startDate;
    private Date endDate;
    private String employeeCode;
    private String role;
    private String gradeId;
    private String departmentId;
    private String organizationId;
    //join
    private User user;
    private Department department;
    private Organization organization;
    private Set hods;
    private Grade grade;
    private EmploymentReportTo employmentReportTo;
    private Set subordinates;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(Set subordinates) {
        this.subordinates = subordinates;
    }

    public Set getHods() {
        return hods;
    }

    public void setHods(Set hods) {
        this.hods = hods;
    }

    public EmploymentReportTo getEmploymentReportTo() {
        return employmentReportTo;
    }

    public void setEmploymentReportTo(EmploymentReportTo employmentReportTo) {
        this.employmentReportTo = employmentReportTo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getAuditTrailId() {
        return id;
    }
}
