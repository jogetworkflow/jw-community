package org.joget.apps.workflow.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;

import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Controller
public class DirectoryJsonController {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;
    @Autowired
    UserDao userDao;
    @Autowired
    EmploymentDao employmentDao;
    @Autowired
    WorkflowUserManager workflowUserManager;

    public EmploymentDao getEmploymentDao() {
        return employmentDao;
    }

    public void setEmploymentDao(EmploymentDao employmentDao) {
        this.employmentDao = employmentDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ExtDirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(ExtDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    /**
     * Manage org chart
     */
    @RequestMapping("/json/directory/admin/organization/list")
    public void listOrganization(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Organization> organizations = null;

        organizations = getDirectoryManager().getOrganizationsByFilter(name, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (organizations != null) {
            for (Organization organization : organizations) {
                Map data = new HashMap();
                data.put("id", organization.getId());
                data.put("name", organization.getName());
                data.put("description", organization.getDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalOrganizationsByFilter(name));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/dept/list")
    public void listDepartment(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Department> departments = null;

        if ("".equals(orgId)) {
            orgId = null;
        }

        departments = getDirectoryManager().getDepartmentsByOrganizationId(name, orgId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (departments != null) {
            for (Department department : departments) {
                Map data = new HashMap();
                data.put("id", department.getId());
                data.put("name", department.getName());
                data.put("description", department.getDescription());
                data.put("description", department.getDescription());
                data.put("organization.name", (department.getOrganization() != null) ? department.getOrganization().getName() : "");
                data.put("parent.name", (department.getParent() != null) ? department.getParent().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalDepartmentnsByOrganizationId(name, orgId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/subdept/list")
    public void listSubDepartment(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "deptId", required = false) String deptId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Department> departments = null;

        departments = getDirectoryManager().getDepartmentsByParentId(name, deptId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (departments != null) {
            for (Department department : departments) {
                Map data = new HashMap();
                data.put("id", department.getId());
                data.put("name", department.getName());
                data.put("description", department.getDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalDepartmentsByParentId(name, deptId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/grade/list")
    public void listGrade(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Grade> grades = null;

        grades = getDirectoryManager().getGradesByOrganizationId(name, orgId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (grades != null) {
            for (Grade grade : grades) {
                Map data = new HashMap();
                data.put("id", grade.getId());
                data.put("name", grade.getName());
                data.put("description", grade.getDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalGradesByOrganizationId(name, orgId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/group/list")
    public void listGroup(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Group> groups = null;

        if ("".equals(orgId)) {
            orgId = null;
        }

        groups = getDirectoryManager().getGroupsByOrganizationId(name, orgId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (groups != null) {
            for (Group group : groups) {
                Map data = new HashMap();
                data.put("id", group.getId());
                data.put("name", group.getName());
                data.put("description", group.getDescription());
                data.put("organization.name", (group.getOrganization() != null) ? group.getOrganization().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalGroupsByOrganizationId(name, orgId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/user/group/list")
    public void listUserGroup(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "userId", required = false) String userId, @RequestParam(value = "orgId", required = false) String orgId, @RequestParam(value = "inGroup", required = false) Boolean inGroup, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Group> groups = null;

        if ("".equals(userId)) {
            userId = null;
        }
        if ("".equals(orgId)) {
            orgId = null;
        }

        groups = getDirectoryManager().getGroupsByUserId(name, userId, orgId, inGroup, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (groups != null) {
            for (Group group : groups) {
                Map data = new HashMap();
                data.put("id", group.getId());
                data.put("name", group.getName());
                data.put("description", group.getDescription());
                data.put("organization.name", (group.getOrganization() != null) ? group.getOrganization().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalGroupsByUserId(name, userId, orgId, inGroup));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/user/list")
    public void listUser(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "deptId", required = false) String deptId, @RequestParam(value = "gradeId", required = false) String gradeId,
            @RequestParam(value = "groupId", required = false) String groupId, @RequestParam(value = "roleId", required = false) String roleId,
            @RequestParam(value = "active", required = false) String active, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<User> users = null;

        if ("".equals(orgId)) {
            orgId = null;
        }
        if ("".equals(deptId)) {
            deptId = null;
        }
        if ("".equals(gradeId)) {
            gradeId = null;
        }
        if ("".equals(groupId)) {
            groupId = null;
        }
        if ("".equals(roleId)) {
            roleId = null;
        }
        if ("".equals(active)) {
            active = null;
        }

        users = getDirectoryManager().getUsers(name, orgId, deptId, gradeId, groupId, roleId, active, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (users != null) {
            for (User user : users) {
                Map data = new HashMap();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("firstName", user.getFirstName());
                data.put("lastName", user.getLastName());
                data.put("email", user.getEmail());
                data.put("active", (user.getActive() == 1)? ResourceBundleUtil.getMessage("console.directory.user.common.label.status.active") : ResourceBundleUtil.getMessage("console.directory.user.common.label.status.inactive"));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalUsers(name, orgId, deptId, gradeId, groupId, roleId, active));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/user/notInGroup/list")
    public void listUserNotInGroup(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "groupId", required = false) String groupId,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<User> users = null;

        if ("".equals(groupId)) {
            groupId = null;
        }

        users = getUserDao().getUsersNotInGroup(name, groupId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (users != null) {
            for (User user : users) {
                Map data = new HashMap();
                data.put("id", user.getId());
                data.put("username", user.getUsername());
                data.put("firstName", user.getFirstName());
                data.put("lastName", user.getLastName());
                data.put("email", user.getEmail());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getUserDao().getTotalUsersNotInGroup(name, groupId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/user/deptAndGrade/options")
    public void getDeptAndGradeOptions(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam String orgId) throws JSONException, IOException {

        JSONObject jsonObject = new JSONObject();
        Collection<Department> departments = null;
        Collection<Grade> grades = null;

        if (orgId != null && orgId.trim().length() > 0) {
            Map empty = new HashMap();
            empty.put("id", "");
            empty.put("prefix", "");
            empty.put("name", "");

            //JSONArray deptArray = new JSONArray();
            departments = getRecursiveDepartmentList(orgId);
            if (departments != null) {
                jsonObject.accumulate("departments", empty);
                for (Department department : departments) {
                    Map data = new HashMap();
                    data.put("id", department.getId());
                    data.put("name", department.getName());
                    data.put("prefix", (department.getTreeStructure() != null) ? department.getTreeStructure() : "");
                    jsonObject.accumulate("departments", data);
                }
            }

            //JSONArray gradeArray = new JSONArray();
            grades = directoryManager.getGradesByOrganizationId(null, orgId, "name", false, null, null);
            if (grades != null) {
                jsonObject.accumulate("grades", empty);
                for (Grade grade : grades) {
                    Map data = new HashMap();
                    data.put("id", grade.getId());
                    data.put("name", grade.getName());
                    jsonObject.accumulate("grades", data);
                }
            }
        }

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/employment/list")
    public void listEmployment(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "deptId", required = false) String deptId, @RequestParam(value = "gradeId", required = false) String gradeId,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Employment> employments = null;

        if ("".equals(orgId)) {
            orgId = null;
        }
        if ("".equals(deptId)) {
            deptId = null;
        }
        if ("".equals(gradeId)) {
            gradeId = null;
        }

        employments = getDirectoryManager().getEmployments(name, orgId, deptId, gradeId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (employments != null) {
            for (Employment employment : employments) {
                Map data = new HashMap();
                data.put("user.id", employment.getUser().getId());
                data.put("user.username", employment.getUser().getUsername());
                data.put("user.firstName", employment.getUser().getFirstName());
                data.put("user.lastName", employment.getUser().getLastName());
                data.put("employeeCode", employment.getEmployeeCode());
                data.put("role", employment.getRole());
                data.put("organization.name", (employment.getOrganization() != null) ? employment.getOrganization().getName() : "");
                data.put("department.name", (employment.getDepartment() != null) ? employment.getDepartment().getName() : "");
                data.put("grade.name", (employment.getGrade() != null) ? employment.getGrade().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", getDirectoryManager().getTotalEmployments(name, orgId, deptId, gradeId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }
    
    @RequestMapping("/json/directory/admin/employment/notInOrganization/list")
    public void listEmploymentNotInOrganization(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Employment> employments = null;

        employments = employmentDao.getEmploymentsNotInOrganization(name, orgId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (employments != null) {
            for (Employment employment : employments) {
                Map data = new HashMap();
                data.put("user.id", employment.getUser().getId());
                data.put("user.username", employment.getUser().getUsername());
                data.put("user.firstName", employment.getUser().getFirstName());
                data.put("user.lastName", employment.getUser().getLastName());
                data.put("employeeCode", employment.getEmployeeCode());
                data.put("role", employment.getRole());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", employmentDao.getTotalEmploymentsNotInOrganization(name, orgId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/employment/noHaveOrganization/list")
    public void listEmploymentNoHaveOrganization(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Employment> employments = null;

        employments = employmentDao.getEmploymentsNoHaveOrganization(name, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (employments != null) {
            for (Employment employment : employments) {
                Map data = new HashMap();
                data.put("user.id", employment.getUser().getId());
                data.put("user.username", employment.getUser().getUsername());
                data.put("user.firstName", employment.getUser().getFirstName());
                data.put("user.lastName", employment.getUser().getLastName());
                data.put("employeeCode", employment.getEmployeeCode());
                data.put("role", employment.getRole());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", employmentDao.getTotalEmploymentsNoHaveOrganization(name));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/employment/noInDept/list")
    public void listEmploymentNotInDepartment(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "deptId", required = false) String deptId,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Employment> employments = null;

        employments = employmentDao.getEmploymentsNotInDepartment(name, orgId, deptId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (employments != null) {
            for (Employment employment : employments) {
                Map data = new HashMap();
                data.put("user.id", employment.getUser().getId());
                data.put("user.username", employment.getUser().getUsername());
                data.put("user.firstName", employment.getUser().getFirstName());
                data.put("user.lastName", employment.getUser().getLastName());
                data.put("employeeCode", employment.getEmployeeCode());
                data.put("role", employment.getRole());
                data.put("department.name", (employment.getDepartment() != null) ? employment.getDepartment().getName() : "");
                data.put("grade.name", (employment.getGrade() != null) ? employment.getGrade().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", employmentDao.getTotalEmploymentsNotInDepartment(name, orgId, deptId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/json/directory/admin/employment/noInGrade/list")
    public void listEmploymentNotInGrade(Writer writer, @RequestParam(value = "callback", required = false) String callback,
            @RequestParam(value = "name", required = false) String name, @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "gradeId", required = false) String gradeId,
            @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc,
            @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {

        Collection<Employment> employments = null;

        employments = employmentDao.getEmploymentsNotInGrade(name, orgId, gradeId, sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        if (employments != null) {
            for (Employment employment : employments) {
                Map data = new HashMap();
                data.put("user.id", employment.getUser().getId());
                data.put("user.username", employment.getUser().getUsername());
                data.put("user.firstName", employment.getUser().getFirstName());
                data.put("user.lastName", employment.getUser().getLastName());
                data.put("employeeCode", employment.getEmployeeCode());
                data.put("role", employment.getRole());
                data.put("department.name", (employment.getDepartment() != null) ? employment.getDepartment().getName() : "");
                data.put("grade.name", (employment.getGrade() != null) ? employment.getGrade().getName() : "");
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", employmentDao.getTotalEmploymentsNotInGrade(name, orgId, gradeId));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", StringEscapeUtils.escapeJavaScript(sort));
        jsonObject.accumulate("desc", desc);

        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    private Collection<Department> getRecursiveDepartmentList(String orgId) {
        Collection<Department> result = new ArrayList();
        Collection<Department> parents = directoryManager.getDepartmentListByOrganization(orgId, "name", false, null, null);
        if (parents != null && parents.size() > 0) {
            for (Department p : parents) {
                if (p.getParent() == null) {
                    result.add(p);
                    Collection<Department> childs = getRecursiveDepartmentListByParent(p.getId(), 0);
                    if (childs != null && childs.size() > 0) {
                        result.addAll(childs);
                    }
                }
            }
        }
        return result;
    }

    private Collection<Department> getRecursiveDepartmentListByParent(String parentId, int level) {
        Collection<Department> result = new ArrayList();
        Collection<Department> parents = directoryManager.getDepartmentsByParentId(null, parentId, "name", false, null, null);
        if (parents != null && parents.size() > 0) {
            for (Department p : parents) {
                String prefix = "";
                for (int i = 0; i < level + 1; i++) {
                    prefix += "--";
                }
                p.setTreeStructure(prefix);
                result.add(p);
                Collection<Department> childs = getRecursiveDepartmentListByParent(p.getId(), level + 1);
                if (childs != null && childs.size() > 0) {
                    result.addAll(childs);
                }
            }
        }
        return result;
    }

    @RequestMapping("/json/directory/user/sso")
    public void singleSignOn(Writer writer, HttpServletRequest httpRequest, HttpServletResponse httpResponse, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "username", required = false) String username, @RequestParam(value = "password", required = false) String password, @RequestParam(value = "hash", required = false) String hash) throws JSONException, IOException, ServletException {
        boolean loginWithFilter = false;
        
        //WorkflowHttpAuthProcessingFilter
        if (httpRequest.getParameter("j_username") != null && (httpRequest.getParameter("j_password") != null || hash != null)) {
            loginWithFilter = true;
        }
        
        String header = httpRequest.getHeader("Authorization");
        if ((header != null) && header.startsWith("Basic ")) {
            loginWithFilter = true;
        }
        
        if (username != null && !username.isEmpty() && !loginWithFilter) {
            String ip = AppUtil.getClientIp(httpRequest);
            
            try {
                if (password == null) {
                    password = hash;
                }
                Authentication request = new UsernamePasswordAuthenticationToken(username, password);
                Authentication result = authenticationManager.authenticate(request);
                SecurityContextHolder.getContext().setAuthentication(result);

                // generate new session to avoid session fixation vulnerability
                HttpSession session = httpRequest.getSession(false);
                if (session != null) {
                    SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(httpRequest, httpResponse);
                    session.invalidate();
                    session = httpRequest.getSession(true);
                    if (savedRequest != null) {
                        new HttpSessionRequestCache().saveRequest(httpRequest, httpResponse);
                    }
                }

                // add success to audit trail
                boolean authenticated = result.isAuthenticated();
                if (authenticated) {
                    workflowUserManager.clearCurrentThreadUser();
                }
                LogUtil.info(getClass().getName(), "Authentication for user " + username + " ("+ip+") : " + authenticated);
                WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + " ("+ip+") : " + authenticated);  
                
            } catch (AuthenticationException e) {
                // add failure to audit trail
                if (username != null) {
                    LogUtil.info(getClass().getName(), "Authentication for user " + username + " ("+ip+") : false");
                    WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                    workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + username + " ("+ip+") : false");
                }
            }
        }
        
        if (WorkflowUtil.isCurrentUserAnonymous() && (callback == null || callback.isEmpty())) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("username", WorkflowUtil.getCurrentUsername());
      
        boolean isAdmin = WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);
        if (isAdmin) {
            jsonObject.accumulate("isAdmin", "true");
        }
        
        // csrf token
        String csrfToken = SecurityUtil.getCsrfTokenName() + "=" + SecurityUtil.getCsrfTokenValue(httpRequest);
        jsonObject.accumulate("token", csrfToken);

        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping("/json/directory/user/csrfToken")
    public void csrfToken(Writer writer, HttpServletRequest httpRequest, HttpServletResponse httpResponse, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException, ServletException {
        JSONObject jsonObject = new JSONObject();
      
        jsonObject.accumulate("tokenName", SecurityUtil.getCsrfTokenName());
        jsonObject.accumulate("tokenValue", SecurityUtil.getCsrfTokenValue(httpRequest));

        AppUtil.writeJson(writer, jsonObject, callback);
    }
}
