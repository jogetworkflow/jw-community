package org.joget.apps.app.controller;

import au.com.bytecode.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.lib.DefaultFormBinder;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormService;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.spring.model.ResourceBundleMessage;
import org.joget.commons.spring.model.ResourceBundleMessageDao;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Group;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.dao.DepartmentDao;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.GradeDao;
import org.joget.directory.dao.GroupDao;
import org.joget.directory.dao.OrganizationDao;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Organization;
import org.joget.directory.model.service.DirectoryManagerPlugin;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.workflow.model.ParticipantPlugin;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.workflow.util.XpdlImageUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

@Controller
public class ConsoleWebController {

    public static final String APP_ZIP_PREFIX = "APP_";
    @Autowired
    UserDao userDao;
    @Autowired
    OrganizationDao organizationDao;
    @Autowired
    DepartmentDao departmentDao;
    @Autowired
    GradeDao gradeDao;
    @Autowired
    GroupDao groupDao;
    @Autowired
    RoleDao roleDao;
    @Autowired
    EmploymentDao employmentDao;
    @Autowired
    WorkflowManager workflowManager;
    @Autowired
    WorkflowUserManager workflowUserManager;
    @Autowired
    PluginManager pluginManager;
    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;
    @Autowired
    ResourceBundleMessageDao rbmDao;
    @Autowired
    Validator validator;
    @Autowired
    AppService appService;
    @Autowired
    UserviewService userviewService;
    @Autowired
    FormService formService;
    @Autowired
    SetupManager setupManager;
    @Resource
    AppDefinitionDao appDefinitionDao;
    @Resource
    FormDefinitionDao formDefinitionDao;
    @Resource
    PackageDefinitionDao packageDefinitionDao;
    @Resource
    MessageDao messageDao;
    @Resource
    EnvironmentVariableDao environmentVariableDao;
    @Resource
    PluginDefaultPropertiesDao pluginDefaultPropertiesDao;
    @Resource
    UserviewDefinitionDao userviewDefinitionDao;
    @Resource
    DatalistDefinitionDao datalistDefinitionDao;
    @Resource
    FormDataDao formDataDao;
    @Autowired
    LocaleResolver localeResolver;

    @RequestMapping("/index")
    public String index() {
        String landingPage = WorkflowUtil.getSystemSetupValue("landingPage");
        
        if (landingPage == null || landingPage.trim().isEmpty()) {
            landingPage = "/home";
        }
        return "redirect:" + landingPage;
    }

    @RequestMapping("/console/home")
    public String consoleHome() {
        return "console/home";
    }

    @RequestMapping("/help/guide")
    public void consoleHelpGuide(Writer writer, @RequestParam("key") String key) throws IOException {
        if (key != null && !key.trim().isEmpty()) {
            String message = ResourceBundleUtil.getMessage(key);
            if (message != null && !message.trim().isEmpty()) {
                message = pluginManager.processPluginTranslation(message, getClass().getName(), "console");
                writer.write(message);
            }
        }
    }

    @RequestMapping("/console/directory/orgs")
    public String consoleOrgList(ModelMap model) {
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/org/create")
    public String consoleOrgCreate(ModelMap model) {
        model.addAttribute("organization", new Organization());
        return "console/directory/orgCreate";
    }

    @RequestMapping("/console/directory/org/view/(*:id)")
    public String consoleOrgView(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("organization", directoryManager.getOrganization(id));
        Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, id, "name", false, null, null);
        Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, id, "name", false, null, null);
        model.addAttribute("departments", departments);
        model.addAttribute("grades", grades);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/orgView";
    }

    @RequestMapping("/console/directory/org/edit/(*:id)")
    public String consoleOrgEdit(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("organization", organizationDao.getOrganization(id));
        return "console/directory/orgEdit";
    }

    @RequestMapping(value = "/console/directory/org/submit/(*:action)", method = RequestMethod.POST)
    public String consoleOrgSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("organization") Organization organization, BindingResult result) {
        // validate ID
        validator.validate(organization, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (organizationDao.getOrganization(organization.getId()) != null) {
                    errors.add("console.directory.org.error.label.idExists");
                } else {
                    invalid = !organizationDao.addOrganization(organization);
                }
            } else {
                Organization o = organizationDao.getOrganization(organization.getId());
                o.setName(organization.getName());
                o.setDescription(organization.getDescription());
                invalid = !organizationDao.updateOrganization(o);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            if ("create".equals(action)) {
                return "console/directory/orgCreate";
            } else {
                return "console/directory/orgEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/directory/org/view/" + id;
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/org/delete", method = RequestMethod.POST)
    public String consoleOrgDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String organizationId = (String) strToken.nextElement();
            organizationDao.deleteOrganization(organizationId);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/org/(*:id)/user/assign/view")
    public String consoleOrgUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        return "console/directory/orgUserAssign";
    }

    @RequestMapping(value = "/console/directory/org/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleOrgUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToOrganization(userId, id);
        }
        return "console/directory/orgUserAssign";
    }

    @RequestMapping(value = "/console/directory/org/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleOrgUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromOrganization(userId, id);
        }
        return "console/directory/orgView";
    }

    @RequestMapping("/console/directory/dept/create")
    public String consoleDeptCreate(ModelMap model, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("department", new Department());
        if (parentId != null && parentId.trim().length() > 0) {
            model.addAttribute("parent", departmentDao.getDepartment(parentId));
        }
        return "console/directory/deptCreate";
    }

    @RequestMapping("/console/directory/dept/view/(*:id)")
    public String consoleDeptView(ModelMap model, @RequestParam("id") String id) {
        Department department = directoryManager.getDepartmentById(id);
        model.addAttribute("department", department);

        User hod = directoryManager.getDepartmentHod(id);
        model.addAttribute("hod", hod);

        if (department != null && department.getOrganization() != null) {
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, department.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("grades", grades);
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/dept/edit/(*:id)")
    public String consoleDeptEdit(ModelMap model, @RequestParam("id") String id, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("department", departmentDao.getDepartment(id));
        if (parentId != null && parentId.trim().length() > 0) {
            model.addAttribute("parent", departmentDao.getDepartment(parentId));
        }
        return "console/directory/deptEdit";
    }

    @RequestMapping(value = "/console/directory/dept/submit/(*:action)", method = RequestMethod.POST)
    public String consoleDeptSubmit(ModelMap model, @RequestParam("action") String action, @RequestParam("orgId") String orgId, @RequestParam(value = "parentId", required = false) String parentId, @ModelAttribute("department") Department department, BindingResult result) {
        Organization organization = organizationDao.getOrganization(orgId);
        Department parent = null;
        if (parentId != null && parentId.trim().length() > 0) {
            parent = departmentDao.getDepartment(parentId);
        }

        // validate ID
        validator.validate(department, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (departmentDao.getDepartment(department.getId()) != null) {
                    errors.add("console.directory.department.error.label.idExists");
                } else {
                    department.setOrganization(organization);
                    if (parent != null) {
                        department.setParent(parent);
                    }
                    invalid = !departmentDao.addDepartment(department);
                }
            } else {
                Department d = departmentDao.getDepartment(department.getId());
                d.setName(department.getName());
                d.setDescription(department.getDescription());
                invalid = !departmentDao.updateDepartment(d);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            model.addAttribute("department", department);
            if (parent != null) {
                model.addAttribute("parent", parent);
            }
            if ("create".equals(action)) {
                return "console/directory/deptCreate";
            } else {
                return "console/directory/deptEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            if ("create".equals(action)) {
                if (parent != null) {
                    url += "/web/console/directory/dept/view/" + parent.getId();
                } else {
                    url += "/web/console/directory/org/view/" + id;
                }
            } else {
                url += "/web/console/directory/dept/view/" + department.getId();
            }
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/dept/delete", method = RequestMethod.POST)
    public String consoleDeptDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            departmentDao.deleteDepartment(id);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/dept/(*:id)/hod/set/view")
    public String consoleDeptHodSet(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Department department = departmentDao.getDepartment(id);
        if (department != null && department.getOrganization() != null) {
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, department.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("grades", grades);
        }
        return "console/directory/deptHodSetView";
    }

    @RequestMapping(value = "/console/directory/dept/(*:deptId)/hod/set/submit", method = RequestMethod.POST)
    public String consoleDeptHodSetSubmit(ModelMap model, @RequestParam(value = "deptId") String deptId, @RequestParam(value = "userId") String userId) {
        employmentDao.assignUserAsDepartmentHOD(userId, deptId);
        return "console/directory/deptHodSetView";
    }

    @RequestMapping(value = "/console/directory/dept/(*:deptId)/hod/remove", method = RequestMethod.POST)
    public String consoleDeptHodRemove(@RequestParam(value = "deptId") String deptId, @RequestParam(value = "userId") String userId) {
        employmentDao.unassignUserAsDepartmentHOD(userId, deptId);
        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/dept/(*:id)/user/assign/view")
    public String consoleDeptUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Department department = directoryManager.getDepartmentById(id);
        if (department != null && department.getOrganization() != null) {
            model.addAttribute("organizationId", department.getOrganization().getId());
        }
        return "console/directory/deptUserAssign";
    }

    @RequestMapping(value = "/console/directory/dept/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleDeptUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToDepartment(userId, id);
        }
        return "console/directory/deptUserAssign";
    }

    @RequestMapping(value = "/console/directory/dept/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleDeptUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromDepartment(userId, id);
        }
        return "console/directory/deptView";
    }

    @RequestMapping("/console/directory/grade/create")
    public String consoleGradeCreate(ModelMap model, @RequestParam("orgId") String orgId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("grade", new Grade());
        return "console/directory/gradeCreate";
    }

    @RequestMapping("/console/directory/grade/view/(*:id)")
    public String consoleGradeView(ModelMap model, @RequestParam("id") String id) {
        Grade grade = directoryManager.getGradeById(id);
        model.addAttribute("grade", grade);

        if (grade != null && grade.getOrganization() != null) {
            Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, grade.getOrganization().getId(), "name", false, null, null);
            model.addAttribute("departments", departments);
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/gradeView";
    }

    @RequestMapping("/console/directory/grade/edit/(*:id)")
    public String consoleGradeEdit(ModelMap model, @RequestParam("id") String id, @RequestParam("orgId") String orgId) {
        model.addAttribute("organization", organizationDao.getOrganization(orgId));
        model.addAttribute("grade", gradeDao.getGrade(id));
        return "console/directory/gradeEdit";
    }

    @RequestMapping(value = "/console/directory/grade/submit/(*:action)", method = RequestMethod.POST)
    public String consoleGradeSubmit(ModelMap model, @RequestParam("action") String action, @RequestParam("orgId") String orgId, @ModelAttribute("grade") Grade grade, BindingResult result) {
        Organization organization = organizationDao.getOrganization(orgId);

        // validate ID
        validator.validate(grade, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (gradeDao.getGrade(grade.getId()) != null) {
                    errors.add("console.directory.grade.error.label.idExists");
                } else {
                    grade.setOrganization(organization);
                    invalid = !gradeDao.addGrade(grade);
                }
            } else {
                Grade g = gradeDao.getGrade(grade.getId());
                g.setName(grade.getName());
                g.setDescription(grade.getDescription());
                invalid = !gradeDao.updateGrade(g);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            model.addAttribute("organization", organization);
            model.addAttribute("grade", grade);
            if ("create".equals(action)) {
                return "console/directory/gradeCreate";
            } else {
                return "console/directory/gradeEdit";
            }
        } else {
            String id = organization.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            if ("create".equals(action)) {
                url += "/web/console/directory/org/view/" + id;
            } else {
                url += "/web/console/directory/grade/view/" + grade.getId();
            }
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/grade/delete", method = RequestMethod.POST)
    public String consoleGradeDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            gradeDao.deleteGrade(id);
        }
        return "console/directory/orgList";
    }

    @RequestMapping("/console/directory/grade/(*:id)/user/assign/view")
    public String consoleGradeUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Grade grade = directoryManager.getGradeById(id);
        if (grade != null && grade.getOrganization() != null) {
            model.addAttribute("organizationId", grade.getOrganization().getId());
        }
        return "console/directory/gradeUserAssign";
    }

    @RequestMapping(value = "/console/directory/grade/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleGradeUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.assignUserToGrade(userId, id);
        }
        return "console/directory/gradeUserAssign";
    }

    @RequestMapping(value = "/console/directory/grade/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleGradeUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            employmentDao.unassignUserFromGrade(userId, id);
        }
        return "console/directory/gradeView";
    }

    @RequestMapping("/console/directory/groups")
    public String consoleGroupList(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/group/create")
    public String consoleGroupCreate(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("group", new Group());
        return "console/directory/groupCreate";
    }

    @RequestMapping("/console/directory/group/view/(*:id)")
    public String consoleGroupView(ModelMap model, @RequestParam("id") String id) {
        model.addAttribute("group", directoryManager.getGroupById(id));
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/groupView";
    }

    @RequestMapping("/console/directory/group/edit/(*:id)")
    public String consoleGroupEdit(ModelMap model, @RequestParam("id") String id) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        Group group = groupDao.getGroup(id);
        if (group.getOrganization() != null) {
            group.setOrganizationId(group.getOrganization().getId());
        }
        model.addAttribute("group", groupDao.getGroup(id));
        return "console/directory/groupEdit";
    }

    @RequestMapping(value = "/console/directory/group/submit/(*:action)", method = RequestMethod.POST)
    public String consoleGroupSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("group") Group group, BindingResult result) {
        // validate ID
        validator.validate(group, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check id exist
                if (groupDao.getGroup(group.getId()) != null) {
                    errors.add("console.directory.group.error.label.idExists");
                } else {
                    if (group.getOrganizationId() != null && group.getOrganizationId().trim().length() > 0) {
                        group.setOrganization(organizationDao.getOrganization(group.getOrganizationId()));
                    }
                    invalid = !groupDao.addGroup(group);
                }
            } else {
                Group g = groupDao.getGroup(group.getId());
                group.setUsers(g.getUsers());
                if (group.getOrganizationId() != null && group.getOrganizationId().trim().length() > 0) {
                    group.setOrganization(organizationDao.getOrganization(group.getOrganizationId()));
                }
                invalid = !groupDao.updateGroup(group);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
            model.addAttribute("organizations", organizations);
            model.addAttribute("group", group);
            if ("create".equals(action)) {
                return "console/directory/groupCreate";
            } else {
                return "console/directory/groupEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            url += "/web/console/directory/group/view/" + group.getId();
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/group/delete", method = RequestMethod.POST)
    public String consoleGroupDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            groupDao.deleteGroup(id);
        }
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/group/(*:id)/user/assign/view")
    public String consoleGroupUserAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        return "console/directory/groupUserAssign";
    }

    @RequestMapping(value = "/console/directory/group/(*:id)/user/assign/submit", method = RequestMethod.POST)
    public String consoleGroupUserAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            userDao.assignUserToGroup(userId, id);
        }
        return "console/directory/groupUserAssign";
    }

    @RequestMapping(value = "/console/directory/group/(*:id)/user/unassign", method = RequestMethod.POST)
    public String consoleGroupUserUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String userId = (String) strToken.nextElement();
            userDao.unassignUserFromGroup(userId, id);
        }
        return "console/directory/groupList";
    }

    @RequestMapping("/console/directory/users")
    public String consoleUserList(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());
        return "console/directory/userList";
    }

    @RequestMapping("/console/directory/user/create")
    public String consoleUserCreate(ModelMap model) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
        model.addAttribute("timezones", TimeZoneUtil.getList());

        Map<String, String> status = new HashMap<String, String>();
        status.put("1", "Active");
        status.put("0", "Inactive");
        model.addAttribute("status", status);

        User user = new User();
        user.setActive(1);
        Set roles = new HashSet();
        roles.add(roleDao.getRole("ROLE_USER"));
        user.setRoles(roles);
        model.addAttribute("user", user);
        model.addAttribute("employeeDepartmentHod", "no");
        return "console/directory/userCreate";
    }

    @RequestMapping("/console/directory/user/view/(*:id)")
    public String consoleUserView(ModelMap model, @RequestParam("id") String id) {
        User user = directoryManager.getUserById(id);

        model.addAttribute("user", user);

        if (user != null) {
            //get only 1st employment
            if (user.getEmployments() != null && user.getEmployments().size() > 0) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                model.addAttribute("employment", directoryManager.getEmployment(employment.getId()));
            }

            //get roles
            String roles = "";
            if (user.getRoles() != null && user.getRoles().size() > 0) {
                for (Role role : (Set<Role>) user.getRoles()) {
                    roles += role.getName() + ", ";
                }

                //remove trailing comma
                roles = roles.substring(0, roles.length() - 2);
            }
            model.addAttribute("roles", roles);
        }

        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/userView";
    }

    @RequestMapping("/console/directory/user/edit/(*:id)")
    public String consoleUserEdit(ModelMap model, @RequestParam("id") String id) {
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
        model.addAttribute("timezones", TimeZoneUtil.getList());

        Map<String, String> status = new HashMap<String, String>();
        status.put("1", "Active");
        status.put("0", "Inactive");
        model.addAttribute("status", status);

        User user = userDao.getUserById(id);
        model.addAttribute("user", user);

        Employment employment = null;
        if (user.getEmployments() != null && user.getEmployments().size() > 0) {
            employment = (Employment) user.getEmployments().iterator().next();
        } else {
            employment = new Employment();
        }

        model.addAttribute("employeeCode", employment.getEmployeeCode());
        model.addAttribute("employeeRole", employment.getRole());
        model.addAttribute("employeeOrganization", employment.getOrganizationId());
        model.addAttribute("employeeDepartment", employment.getDepartmentId());
        model.addAttribute("employeeGrade", employment.getGradeId());
        model.addAttribute("employeeStartDate", employment.getStartDate());
        model.addAttribute("employeeEndDate", employment.getEndDate());
        model.addAttribute("employeeDepartmentHod", (employment.getHods() != null && employment.getHods().size() > 0) ? "yes" : "no");

        return "console/directory/userEdit";
    }

    @RequestMapping(value = "/console/directory/user/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("user") User user, BindingResult result,
            @RequestParam(value = "employeeCode", required = false) String employeeCode, @RequestParam(value = "employeeRole", required = false) String employeeRole,
            @RequestParam(value = "employeeOrganization", required = false) String employeeOrganization, @RequestParam(value = "employeeDepartment", required = false) String employeeDepartment,
            @RequestParam(value = "employeeDepartmentHod", required = false) String employeeDepartmentHod, @RequestParam(value = "employeeGrade", required = false) String employeeGrade,
            @RequestParam(value = "employeeStartDate", required = false) String employeeStartDate, @RequestParam(value = "employeeEndDate", required = false) String employeeEndDate) {
        // validate ID
        validator.validate(user, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check username exist
                if (userDao.getUser(user.getUsername()) != null) {
                    errors.add("console.directory.user.error.label.usernameExists");
                }


                if (errors.isEmpty()) {
                    user.setId(user.getUsername());
                    //md5 password
                    user.setPassword(StringUtil.md5Base16(user.getPassword()));

                    //set roles
                    if (user.getRoles() != null && user.getRoles().size() > 0) {
                        Set roles = new HashSet();
                        for (String roleId : (Set<String>) user.getRoles()) {
                            roles.add(roleDao.getRole(roleId));
                        }
                        user.setRoles(roles);
                    }

                    invalid = !userDao.addUser(user);
                }
            } else {
                User u = userDao.getUserById(user.getId());
                u.setFirstName(user.getFirstName());
                u.setLastName(user.getLastName());
                u.setEmail(user.getEmail());
                if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                    //set md5 password
                    u.setPassword(StringUtil.md5Base16(user.getPassword()));
                }
                //set roles
                if (user.getRoles() != null && user.getRoles().size() > 0) {
                    Set roles = new HashSet();
                    for (String roleId : (Set<String>) user.getRoles()) {
                        roles.add(roleDao.getRole(roleId));
                    }
                    u.setRoles(roles);
                }
                u.setTimeZone(user.getTimeZone());
                u.setActive(user.getActive());

                invalid = !userDao.updateUser(u);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
            model.addAttribute("organizations", organizations);
            model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
            model.addAttribute("timezones", TimeZoneUtil.getList());

            Map<String, String> status = new HashMap<String, String>();
            status.put("1", "Active");
            status.put("0", "Inactive");
            model.addAttribute("status", status);

            model.addAttribute("user", user);

            model.addAttribute("employeeCode", employeeCode);
            model.addAttribute("employeeRole", employeeRole);
            model.addAttribute("employeeOrganization", employeeOrganization);
            model.addAttribute("employeeDepartment", employeeDepartment);
            model.addAttribute("employeeGrade", employeeGrade);
            model.addAttribute("employeeStartDate", employeeStartDate);
            model.addAttribute("employeeEndDate", employeeEndDate);
            model.addAttribute("employeeDepartmentHod", employeeDepartmentHod);
            if ("create".equals(action)) {
                return "console/directory/userCreate";
            } else {
                return "console/directory/userEdit";
            }
        } else {
            //set employment detail
            Employment employment = null;
            if ("create".equals(action)) {
                employment = new Employment();
            } else {
                try {
                    employment = (Employment) userDao.getUserById(user.getId()).getEmployments().iterator().next();
                } catch (Exception e) {
                    employment = new Employment();
                }
            }

            employment.setUserId(user.getId());
            employment.setEmployeeCode(employeeCode);
            employment.setRole(employeeRole);
            employment.setOrganizationId((employeeOrganization != null && !employeeOrganization.isEmpty()) ? employeeOrganization : null);
            employment.setDepartmentId((employeeDepartment != null && !employeeDepartment.isEmpty()) ? employeeDepartment : null);
            employment.setGradeId((employeeGrade != null && !employeeGrade.isEmpty()) ? employeeGrade : null);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (employeeStartDate != null && employeeStartDate.trim().length() > 0) {
                    employment.setStartDate(df.parse(employeeStartDate));
                }
                if (employeeEndDate != null && employeeEndDate.trim().length() > 0) {
                    employment.setEndDate(df.parse(employeeEndDate));
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "Set Employee Date error");
            }
            if (employment.getId() == null) {
                employment.setUser(user);
                employmentDao.addEmployment(employment);
            } else {
                employmentDao.updateEmployment(employment);
            }

            //Hod
            if ("yes".equals(employeeDepartmentHod) && employeeDepartment != null && employeeDepartment.trim().length() > 0) {
                User prevHod = userDao.getHodByDepartmentId(employeeDepartment);
                if (prevHod != null) {
                    employmentDao.unassignUserAsDepartmentHOD(prevHod.getId(), employeeDepartment);
                }
                employmentDao.assignUserAsDepartmentHOD(user.getId(), employeeDepartment);
            } else {
                User prevHod = userDao.getHodByDepartmentId(employeeDepartment);
                if (prevHod != null && prevHod.getId().equals(user.getId())) {
                    employmentDao.unassignUserAsDepartmentHOD(prevHod.getId(), employeeDepartment);
                }
            }

            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            url += "/web/console/directory/user/view/" + user.getId() + ".";
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/user/delete", method = RequestMethod.POST)
    public String consoleUserDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            userDao.deleteUser(id);
        }
        return "console/directory/userList";
    }

    @RequestMapping("/console/directory/user/(*:id)/group/assign/view")
    public String consoleUserGroupAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Collection<Organization> organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        return "console/directory/userGroupAssign";
    }

    @RequestMapping("/console/directory/user/(*:id)/reportTo/assign/view")
    public String consoleUserReportToAssign(ModelMap model, @RequestParam(value = "id") String id) {
        User user = userDao.getUserById(id);
        model.addAttribute("id", id);
        if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
            Employment e = (Employment) user.getEmployments().iterator().next();
            Collection<Department> departments = directoryManager.getDepartmentsByOrganizationId(null, e.getOrganizationId(), "name", false, null, null);
            Collection<Grade> grades = directoryManager.getGradesByOrganizationId(null, e.getOrganizationId(), "name", false, null, null);
            model.addAttribute("organizationId", e.getOrganizationId());
            model.addAttribute("departments", departments);
            model.addAttribute("grades", grades);
        }
        return "console/directory/userReportToAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/reportTo/assign/submit", method = RequestMethod.POST)
    public String consoleUserReportToAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "userId") String userId) {
        employmentDao.assignUserReportTo(id, userId);
        return "console/directory/userReportToAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/reportTo/unassign", method = RequestMethod.POST)
    public String consoleUserReportToUnassign(@RequestParam(value = "id") String id) {
        employmentDao.unassignUserReportTo(id);
        return "console/directory/userView";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/group/assign/submit", method = RequestMethod.POST)
    public String consoleUserGroupAssignSubmit(ModelMap model, @RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String groupId = (String) strToken.nextElement();
            userDao.assignUserToGroup(id, groupId);
        }
        return "console/directory/userGroupAssign";
    }

    @RequestMapping(value = "/console/directory/user/(*:id)/group/unassign", method = RequestMethod.POST)
    public String consoleUserGroupUnassign(@RequestParam(value = "id") String id, @RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String groupId = (String) strToken.nextElement();
            userDao.unassignUserFromGroup(id, groupId);
        }
        return "console/directory/userList";
    }

    @RequestMapping("/console/profile")
    public String profile(ModelMap map) {
        User user = userDao.getUser(workflowUserManager.getCurrentUsername());
        map.addAttribute("user", user);
        map.addAttribute("timezones", TimeZoneUtil.getList());
        
        String enableUserLocale = setupManager.getSettingValue("enableUserLocale");
        Map<String, String> localeStringList = new TreeMap<String, String>();
        if(enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
            String userLocale = setupManager.getSettingValue("userLocale");
            Collection<String> locales = new HashSet();
            locales.addAll(Arrays.asList(userLocale.split(",")));
            
            Locale[] localeList = Locale.getAvailableLocales();
            for (int x = 0; x < localeList.length; x++) {
                String code = localeList[x].toString();
                if (locales.contains(code)) {
                    localeStringList.put(code, code + " - " +localeList[x].getDisplayName(localeResolver.resolveLocale(WorkflowUtil.getHttpServletRequest())));
                }
            }
        }
        map.addAttribute("enableUserLocale", enableUserLocale);
        map.addAttribute("localeStringList", localeStringList);
        
        return "console/profile";
    }

    @RequestMapping(value = "/console/profile/submit", method = RequestMethod.POST)
    public String profileSubmit(ModelMap model, @ModelAttribute("user") User user, BindingResult result) {
        User currentUser = userDao.getUser(workflowUserManager.getCurrentUsername());

        if (currentUser.getUsername().equals(user.getUsername())) {
            currentUser.setFirstName(user.getFirstName());
            currentUser.setLastName(user.getLastName());
            currentUser.setEmail(user.getEmail());
            currentUser.setTimeZone(user.getTimeZone());
            currentUser.setLocale(user.getLocale());
            if (user.getPassword() != null && user.getConfirmPassword() != null && user.getPassword().length() > 0 && user.getPassword().equals(user.getConfirmPassword())) {
                currentUser.setPassword(StringUtil.md5Base16(user.getPassword()));
            }
            userDao.updateUser(currentUser);
        }

        return "console/dialogClose";
    }

    @RequestMapping("/console/app/menu")
    public String consoleMenuAppList(ModelMap model) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        model.addAttribute("appDefinitionList", appDefinitionList);
        return "console/apps/appMenu";
    }

    @RequestMapping("/console/app/create")
    public String consoleAppCreate(ModelMap model) {
        model.addAttribute("appDefinition", new AppDefinition());
        return "console/apps/appCreate";
    }

    @RequestMapping("/console/app/submit")
    public String consoleAppSubmit(ModelMap model, @ModelAttribute("appDefinition") AppDefinition appDefinition, BindingResult result) {

        // validate ID
        validator.validate(appDefinition, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // create app
            Collection<String> errors = appService.createAppDefinition(appDefinition);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            return "console/apps/appCreate";
        } else {
            String appId = appDefinition.getId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/processes";
            model.addAttribute("url", url);
            return "console/apps/dialogClose";
        }
    }

    @RequestMapping("/json/console/app/list")
    public void consoleAppListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, name, sort, desc, start, rows);
        Long count = appDefinitionDao.countLatestVersions(null, null, name);

        JSONObject jsonObject = new JSONObject();
        for (AppDefinition appDef : appDefinitionList) {
            Map data = new HashMap();
            data.put("id", appDef.getId());
            data.put("name", appDef.getName());
            data.put("version", appDef.getVersion());
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/versioning")
    public String consoleAppVersioning(ModelMap model, @RequestParam(value = "appId") String appId) {
        model.addAttribute("appId", appId);
        return "console/apps/appVersion";
    }

    @RequestMapping("/json/console/app/(*:appId)/version/list")
    public void consoleAppVersionListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        Collection<AppDefinition> appDefList = appDefinitionDao.findVersions(appId, sort, desc, start, rows);
        Long count = appDefinitionDao.countVersions(appId);

        JSONObject jsonObject = new JSONObject();
        if (appDefList != null && appDefList.size() > 0) {
            for (AppDefinition appDef : appDefList) {
                Map data = new HashMap();
                data.put("version", appDef.getVersion().toString());
                data.put("published", (appDef.isPublished()) ? "<div class=\"tick\"></div>" : "");
                data.put("dateCreated", appDef.getDateCreated());
                data.put("dateModified", appDef.getDateModified());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/version/new", method = RequestMethod.POST)
    public String consoleAppPublish(@RequestParam(value = "appId") String appId) {
        AppDefinition appDef = appService.createNewAppDefinitionVersion(appId);

        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/publish", method = RequestMethod.POST)
    public String consoleAppPublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        //Unset previous published version
        Long previousVersion = appService.getPublishedVersion(appId);
        if (previousVersion != null && previousVersion != 0) {
            AppDefinition prevAppDef = appService.getAppDefinition(appId, previousVersion.toString());
            prevAppDef.setPublished(Boolean.FALSE);
            appDefinitionDao.saveOrUpdate(prevAppDef);
        }

        //Set current published version
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setPublished(Boolean.TRUE);
            appDefinitionDao.saveOrUpdate(appDef);
        }

        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/rename/(*:name)", method = RequestMethod.POST)
    public String consoleAppRename(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "name") String name) {
        //Rename app
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setName(name);
            appDefinitionDao.saveOrUpdate(appDef);
        }

        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/unpublish", method = RequestMethod.POST)
    public String consoleAppUnpublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        //Set version to unpublish
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setPublished(Boolean.FALSE);
            appDefinitionDao.saveOrUpdate(appDef);
        }

        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/delete", method = RequestMethod.POST)
    public String consoleAppDelete(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        Long appVersion;
        if (AppDefinition.VERSION_LATEST.equals(version)) {
            appVersion = appDefinitionDao.getLatestVersion(appId);
        } else {
            appVersion = Long.parseLong(version);
        }
        appService.deleteAppDefinitionVersion(appId, appVersion);
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/export")
    public void consoleAppExport(HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        ServletOutputStream output = null;
        try {
            // determine output filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String filename = APP_ZIP_PREFIX + appId + "-" + version + "-" + timestamp + ".zip";

            // set response headers
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition", "inline; filename=" + filename);
            output = response.getOutputStream();

            // export app
            appService.exportApp(appId, version, output);

        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (output != null) {
                output.flush();
            }
        }
    }

    @RequestMapping("/console/app/import")
    public String consoleAppImport() {
        return "console/apps/import";
    }

    @RequestMapping(value = "/console/app/import/submit", method = RequestMethod.POST)
    public String consoleAppImportSubmit(ModelMap map) throws IOException {
        MultipartFile appZip = FileStore.getFile("appZip");

        AppDefinition appDef = appService.importApp(appZip.getBytes());

        if (appDef == null) {
            map.addAttribute("error", true);
            return "console/apps/import";
        } else {
            String appId = appDef.getAppId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/processes";
            map.addAttribute("url", url);
            map.addAttribute("appId", appId);
            map.addAttribute("appVersion", appDef.getVersion());
            return "console/apps/packageUploadSuccess";
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/package/xpdl")
    public void getPackageXpdl(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            byte[] content = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
            String xpdl = new String(content, "UTF-8");
            writer.write(xpdl);
        } else {
            // read default xpdl
            InputStream input = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                // get resource input stream
                String url = "/org/joget/apps/app/model/default.xpdl";
                input = pluginManager.getPluginResource(DefaultFormBinder.class.getName(), url);
                if (input != null) {
                    // write output
                    byte[] bbuf = new byte[65536];
                    int length = 0;
                    while ((input != null) && ((length = input.read(bbuf)) != -1)) {
                        out.write(bbuf, 0, length);
                    }
                    // form xpdl
                    String xpdl = new String(out.toByteArray(), "UTF-8");

                    // replace package ID and name
                    xpdl = xpdl.replace("${packageId}", appId);
                    xpdl = xpdl.replace("${packageName}", appDef.getName());
                    writer.write(xpdl);
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        }
    }

    @RequestMapping(value = "/json/console/app/(*:appId)/(~:version)/package/deploy", method = RequestMethod.POST)
    public void consolePackageDeploy(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        MultipartFile packageXpdl = FileStore.getFile("packageXpdl");
        JSONObject jsonObject = new JSONObject();

        // TODO: authenticate user
        boolean authenticated = !workflowUserManager.isCurrentUserAnonymous();

        if (authenticated) {
            try {
                // deploy package
                PackageDefinition packageDef = appService.deployWorkflowPackage(appId, version, packageXpdl.getBytes(), true);

                if (packageDef != null) {
                    // generate image for each process
                    List<WorkflowProcess> processList = workflowManager.getProcessList("", Boolean.TRUE, 0, 10000, packageDef.getId(), Boolean.FALSE, Boolean.FALSE);
                    for (WorkflowProcess process : processList) {
                        XpdlImageUtil.generateXpdlImage(getDesignerwebBaseUrl(request), process.getId(), true);
                    }
                }

                jsonObject.accumulate("status", "complete");
            } catch (Exception e) {
                jsonObject.accumulate("errorMsg", e.getMessage().replace(":", ""));
            }
        } else {
            jsonObject.accumulate("errorMsg", "unauthenticated");
        }
        writeJson(writer, jsonObject, null);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload")
    public String consolePackageUpload(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        return "console/apps/packageUpload";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload/submit", method = RequestMethod.POST)
    public String consolePackageUploadSubmit(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        MultipartFile packageXpdl = FileStore.getFile("packageXpdl");
        try {
            if (packageXpdl == null || packageXpdl.isEmpty()) {
                throw new RuntimeException("Package XPDL is empty");
            }
            PackageDefinition packageDef = appService.deployWorkflowPackage(appId, version, packageXpdl.getBytes(), false);
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageDef.getVersion().toString());
            for (WorkflowProcess process : processList) {
                XpdlImageUtil.generateXpdlImage(getDesignerwebBaseUrl(request), process.getId(), true);
            }

        } catch (Throwable e) {
            map.addAttribute("errorMessage", e.getMessage());
            return "console/apps/packageUpload";
        }
        return "console/apps/packageUploadSuccess";
    }

    protected String getDesignerwebBaseUrl(HttpServletRequest request) {
        String designerwebBaseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        if (WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0) {
            designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
        }
        if (designerwebBaseUrl.endsWith("/")) {
            designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length() - 1);
        }

        return designerwebBaseUrl;
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/processes", "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)"})
    public String consoleProcessView(ModelMap map, @RequestParam("appId") String appId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "version", required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        //for launching workflow designer
        User user = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());
        map.addAttribute("loginHash", user.getLoginHash());
        map.addAttribute("username", user.getUsername());

        WorkflowProcess process = null;
        boolean processFound = false;
        Collection<WorkflowProcess> processList = null;
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        if (packageDefinition != null) {
            Long packageVersion = packageDefinition.getVersion();
            processList = workflowManager.getProcessList(appId, packageVersion.toString());
            if (processDefId != null && processDefId.trim().length() > 0) {
                // find matching process by definition (without version)
                for (WorkflowProcess wp : processList) {
                    String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId());
                    if (processIdWithoutVersion.equals(processDefId) && wp.getVersion().equals(packageVersion.toString())) {
                        process = wp;
                        processDefId = wp.getId();
                        processFound = true;
                        break;
                    }
                }
            }
        }
        if (!processFound) {
            // specific process not found, get list of processes
            if (processList != null && processList.size() == 1) {
                // redirect to the only process
                WorkflowProcess wp = processList.iterator().next();
                return "redirect:/web/console/app/" + appId + "/" + version + "/processes/" + wp.getIdWithoutVersion();
            } else {
                // show process list
                map.addAttribute("processList", processList);
                return "console/apps/processList";
            }
        }

        //get activity list
        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(processDefId);

        //add 'Run Process' activity to activityList
        WorkflowActivity runProcessActivity = new WorkflowActivity();
        runProcessActivity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
        runProcessActivity.setName("Run Process");
        runProcessActivity.setType("normal");
        activityList.add(runProcessActivity);

        //remove route
        Iterator iterator = activityList.iterator();
        while (iterator.hasNext()) {
            WorkflowActivity activity = (WorkflowActivity) iterator.next();
            if (activity.getType().equals(WorkflowActivity.TYPE_ROUTE)) {
                iterator.remove();
            }
        }

        //get activity plugin mapping
        Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();
        Map<String, PackageActivityPlugin> activityPluginMap = (packageDefinition != null) ? packageDefinition.getPackageActivityPluginMap() : new HashMap<String, PackageActivityPlugin>();
        for (String activityDefId : activityPluginMap.keySet()) {
            PackageActivityPlugin pap = activityPluginMap.get(activityDefId);
            String pluginName = pap.getPluginName();
            Plugin plugin = pluginManager.getPlugin(pluginName);
            pluginMap.put(activityDefId, plugin);
        }

        //get activity form mapping
        Map<String, PackageActivityForm> activityFormMap = (packageDefinition != null) ? packageDefinition.getPackageActivityFormMap() : new HashMap<String, PackageActivityForm>();

        // get form map
        Map<String, FormDefinition> formMap = new HashMap<String, FormDefinition>();
        for (String activityDefId : activityFormMap.keySet()) {
            PackageActivityForm paf = activityFormMap.get(activityDefId);
            String formId = paf.getFormId();
            if (PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE.equals(paf.getType()) && formId != null && !formId.isEmpty()) {
                FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
                formMap.put(activityDefId, formDef);
            }
        }

        //get variable list
        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processDefId);

        //get participant list
        Collection<WorkflowParticipant> participantList = workflowManager.getProcessParticipantDefinitionList(processDefId);

        WorkflowParticipant processStartWhiteList = new WorkflowParticipant();
        processStartWhiteList.setId("processStartWhiteList");
        processStartWhiteList.setName(ResourceBundleUtil.getMessage("console.app.process.common.label.processStartWhiteList"));
        processStartWhiteList.setPackageLevel(false);
        participantList.add(processStartWhiteList);

        // get participant map
        Map<String, PackageParticipant> participantMap = (packageDefinition != null) ? packageDefinition.getPackageParticipantMap() : new HashMap<String, PackageParticipant>();

        // get participant plugin map
        Map<String, Plugin> participantPluginMap = pluginManager.loadPluginMap(ParticipantPlugin.class);

        String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        map.addAttribute("processIdWithoutVersion", processIdWithoutVersion);
        map.addAttribute("process", process);
        map.addAttribute("activityList", activityList);
        map.addAttribute("pluginMap", pluginMap);
        map.addAttribute("participantPluginMap", participantPluginMap);
        map.addAttribute("activityFormMap", activityFormMap);
        map.addAttribute("formMap", formMap);
        map.addAttribute("variableList", variableList);
        map.addAttribute("participantList", participantList);
        map.addAttribute("participantMap", participantMap);
        map.addAttribute("isExtDirectoryManager", DirectoryUtil.isExtDirectoryManager());
        map.addAttribute("usersMap", DirectoryUtil.getUsersMap());
        map.addAttribute("groupsMap", DirectoryUtil.getGroupsMap());
        map.addAttribute("departmentsMap", DirectoryUtil.getDepartmentsMap());

        return "console/apps/processView";
    }

    protected String getActivityNameForParticipantMapping(String processDefId, String activityDefId) {
        String activity = "Previous Activity";

        if (activityDefId != null && activityDefId.trim().length() > 0) {
            if (WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS.equals(activityDefId)) {
                activity = "Run Process";
            } else {
                WorkflowActivity wa = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
                if (wa != null) {
                    activity = wa.getName();
                }
            }
        }

        return activity;
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form")
    public String consoleActivityForm(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);

        if (activityDefId.equals(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS)) {
            activity = new WorkflowActivity();
            activity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
            activity.setName("Run Process");
        }

        PackageDefinition packageDef = appDef.getPackageDefinition();
        String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        PackageActivityForm activityForm = packageDef.getPackageActivityForm(processDefIdWithoutVersion, activityDefId);
        if (activityForm != null && PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(activityForm.getType())) {
            map.addAttribute("externalFormUrl", activityForm.getFormUrl());
            map.addAttribute("externalFormIFrameStyle", activityForm.getFormIFrameStyle());
        }

        map.addAttribute("process", process);
        map.addAttribute("activity", activity);

        return "console/apps/activityFormAdd";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/submit")
    public String consoleActivityFormSubmit(
            ModelMap map,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String activityDefId,
            @RequestParam(value = "id", required = false) String formId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "externalFormUrl", required = false) String externalFormUrl,
            @RequestParam(value = "externalFormIFrameStyle", required = false) String externalFormIFrameStyle) throws UnsupportedEncodingException {

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        boolean autoContinue = false;
        if (packageDef != null) {
            autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
        }
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        PackageActivityForm activityForm = new PackageActivityForm();
        activityForm.setProcessDefId(processDefId);
        activityForm.setActivityDefId(activityDefId);
        if (PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(type) && externalFormUrl != null) {
            activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL);
            activityForm.setFormUrl(externalFormUrl);
            activityForm.setFormIFrameStyle(externalFormIFrameStyle);
        } else {
            activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
            activityForm.setFormId(formId);
        }
        activityForm.setAutoContinue(autoContinue);

        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), activityForm);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        return "console/apps/activityFormAddSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/remove")
    public String consoleActivityFormRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // check for existing auto continue flag
        boolean autoContinue = false;
        if (packageDef != null) {
            autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
        }

        // remove mapping
        packageDefinitionDao.removeAppActivityForm(appId, appDef.getVersion(), processDefId, activityDefId);

        if (autoContinue) {
            // save autoContinue flag
            PackageActivityForm paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
            paf.setAutoContinue(autoContinue);
            packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);
        }

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityFormRemoveSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/continue")
    public void consoleActivityContinueSubmit(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam String auto) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // set and save
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
        if (paf == null) {
            paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
        }
        boolean autoContinue = Boolean.parseBoolean(auto);
        paf.setAutoContinue(autoContinue);
        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);

        // write output
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("auto", autoContinue);
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin")
    public String consoleActivityPlugin(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
        map.addAttribute("process", process);
        map.addAttribute("activity", activity);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityPluginAdd";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/submit")
    public String consoleActivityPluginSubmit(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam("id") String pluginName) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        PackageActivityPlugin activityPlugin = new PackageActivityPlugin();
        activityPlugin.setProcessDefId(processDefId);
        activityPlugin.setActivityDefId(activityDefId);
        activityPlugin.setPluginName(pluginName);

        packageDefinitionDao.addAppActivityPlugin(appId, appDef.getVersion(), activityPlugin);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityPluginAddSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/remove")
    public String consoleActivityPluginRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        packageDefinitionDao.removeAppActivityPlugin(appId, appDef.getVersion(), processDefId, activityDefId);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityFormRemoveSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/configure")
    public String consoleActivityPluginConfigure(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        if (packageDef != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
            PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefId, activityDefId);
            Plugin plugin = pluginManager.getPlugin(activityPlugin.getPluginName());

            if (activityPlugin.getPluginProperties() != null && activityPlugin.getPluginProperties().trim().length() > 0) {
                if (!(plugin instanceof PropertyEditable)) {
                    Map propertyMap = new HashMap();
                    propertyMap = CsvUtil.getPluginPropertyMap(activityPlugin.getPluginProperties());
                    map.addAttribute("propertyMap", propertyMap);
                } else {
                    map.addAttribute("properties", activityPlugin.getPluginProperties());
                }
            }

            if (plugin != null) {
                PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(activityPlugin.getPluginName(), appDef);

                if (pluginDefaultProperties != null) {
                    if (!(plugin instanceof PropertyEditable)) {
                        Map defaultPropertyMap = new HashMap();

                        String properties = pluginDefaultProperties.getPluginProperties();
                        if (properties != null && properties.trim().length() > 0) {
                            defaultPropertyMap = CsvUtil.getPluginPropertyMap(properties);
                        }
                        map.addAttribute("defaultPropertyMap", defaultPropertyMap);
                    } else {
                        map.addAttribute("defaultProperties", pluginDefaultProperties.getPluginProperties());
                    }
                }
            }

            if (plugin instanceof PropertyEditable) {
                map.addAttribute("propertyEditable", (PropertyEditable) plugin);
            }

            map.addAttribute("plugin", plugin);

            String url = request.getContextPath() + "/web/console/app/" + appId + "/" + appDef.getVersion() + "/processes/" + URLEncoder.encode(processDefId, "UTF-8") + "/activity/" + activityDefId + "/plugin/configure/submit?param_activityPluginId=" + activityPlugin.getUid();
            map.addAttribute("actionUrl", url);
        }

        return "console/plugin/pluginConfig";
    }

    @RequestMapping("/console/app/(*:param_appId)/(~:param_version)/processes/(*:param_processDefId)/activity/(*:param_activityDefId)/plugin/configure/submit")
    public String consoleActivityPluginConfigureSubmit(ModelMap map, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_processDefId") String processDefId, @RequestParam("param_activityDefId") String activityDefId, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefId, activityDefId);
        if (activityPlugin != null) {
            if (pluginProperties == null) {
                //request params
                Map<String, String> propertyMap = new HashMap();
                Enumeration<String> e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = e.nextElement();

                    if (!paramName.startsWith("param_")) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                    }
                }

                // form csv properties
                StringWriter sw = new StringWriter();
                try {
                    CSVWriter writer = new CSVWriter(sw);
                    Iterator it = propertyMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> pairs = (Map.Entry) it.next();
                        writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                    }
                    writer.close();
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, "");
                }
                String pluginProps = sw.toString();
                activityPlugin.setPluginProperties(pluginProps);
            } else {
                activityPlugin.setPluginProperties(pluginProperties);
            }
        }

        // update and save
        packageDefinitionDao.saveOrUpdate(packageDef);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        return "console/apps/activityPluginConfigSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)")
    public String consoleParticipant(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String participantId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        map.addAttribute("processDefId", processIdWithoutVersion);
        map.addAttribute("participantId", participantId);

        //get activity list
        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(processDefId);

        //add 'Run Process' activity to activityList
        WorkflowActivity runProcessActivity = new WorkflowActivity();
        runProcessActivity.setId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
        runProcessActivity.setName("Run Process");
        runProcessActivity.setType("normal");
        activityList.add(runProcessActivity);

        //remove route & tool
        Iterator iterator = activityList.iterator();
        while (iterator.hasNext()) {
            WorkflowActivity activity = (WorkflowActivity) iterator.next();
            if ((activity.getType().equals(WorkflowActivity.TYPE_ROUTE)) || (activity.getType().equals(WorkflowActivity.TYPE_TOOL))) {
                iterator.remove();
            }
        }
        map.addAttribute("activityList", activityList);

        //get variable list
        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processDefId);
        map.addAttribute("variableList", variableList);

        Collection<Organization> organizations = null;
        if (DirectoryUtil.isExtDirectoryManager()) {
            organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        }
        map.addAttribute("organizations", organizations);
        map.addAttribute("isExtDirectoryManager", DirectoryUtil.isExtDirectoryManager());

        return "console/apps/participantAdd";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/processes/(*:param_processDefId)/participant/(*:param_participantId)/submit/(*:param_type)", method = RequestMethod.POST)
    public String consoleParticipantSubmit(
            ModelMap map,
            HttpServletRequest request,
            @RequestParam("param_appId") String appId,
            @RequestParam(value = "param_version", required = false) String version,
            @RequestParam("param_processDefId") String processDefId,
            @RequestParam("param_participantId") String participantId,
            @RequestParam("param_type") String type,
            @RequestParam(value = "param_value", required = false) String value,
            @RequestParam(value = "pluginProperties", required = false) String pluginProperties) throws UnsupportedEncodingException {

        PackageParticipant participant = new PackageParticipant();
        participant.setProcessDefId(processDefId);
        participant.setParticipantId(participantId);

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        if (PackageParticipant.TYPE_PLUGIN.equals(type)) {
            if (pluginProperties == null) {
                //request params
                Map<String, String> propertyMap = new HashMap();
                Enumeration<String> e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = e.nextElement();

                    if (!paramName.startsWith("param_")) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                    }
                }

                // form csv properties
                StringWriter sw = new StringWriter();
                try {
                    CSVWriter writer = new CSVWriter(sw);
                    Iterator it = propertyMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> pairs = (Map.Entry) it.next();
                        writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                    }
                    writer.close();
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, "");
                }
                String pluginProps = sw.toString();
                participant.setPluginProperties(pluginProps);
            } else {
                participant.setPluginProperties(pluginProperties);
            }
        } else if ((PackageParticipant.TYPE_GROUP.equals(type) || PackageParticipant.TYPE_USER.equals(type)) && packageDef != null) {
            PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
            if (participantExisting != null) {
                //Using Set to prevent duplicate value
                Set values = new HashSet();
                StringTokenizer valueToken = new StringTokenizer(value, ",");
                StringTokenizer existingValueToken = (type.equals(participantExisting.getType())) ? new StringTokenizer(participantExisting.getValue(), ",") : null;
                while (valueToken.hasMoreTokens()) {
                    values.add((String) valueToken.nextElement());
                }
                while (existingValueToken != null && existingValueToken.hasMoreTokens()) {
                    values.add((String) existingValueToken.nextElement());
                }

                //Convert Set to String
                value = "";
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    value += i.next().toString() + ',';
                }
                if (value.length() > 0) {
                    value = value.substring(0, value.length() - 1);
                }
            }
        }
        participant.setType(type);
        participant.setValue(value);

        packageDefinitionDao.addAppParticipant(appId, appDef.getVersion(), participant);

        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("participantId", participantId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));

        if (PackageParticipant.TYPE_PLUGIN.equals(type)) {
            return "console/apps/participantPluginConfigSuccess";
        } else {
            return "console/apps/participantAddSuccess";
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)/plugin/configure")
    public String consoleParticipantPluginConfigure(
            ModelMap map,
            HttpServletRequest request,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String participantId,
            @RequestParam(value = "value", required = false) String value) throws UnsupportedEncodingException, IOException {

        Plugin plugin = null;

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        if (value != null && value.trim().length() > 0) {
            plugin = pluginManager.getPlugin(value);
        } else {
            if (packageDef != null) {
                PackageParticipant participant = packageDef.getPackageParticipant(processDefId, participantId);
                plugin = pluginManager.getPlugin(participant.getValue());

                if (participant.getPluginProperties() != null && participant.getPluginProperties().trim().length() > 0) {
                    if (!(plugin instanceof PropertyEditable)) {
                        Map propertyMap = new HashMap();
                        propertyMap = CsvUtil.getPluginPropertyMap(participant.getPluginProperties());
                        map.addAttribute("propertyMap", propertyMap);
                    } else {
                        map.addAttribute("properties", participant.getPluginProperties());
                    }
                }
            }
        }

        if (plugin != null) {
            PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(value, appDef);

            if (pluginDefaultProperties != null) {
                if (!(plugin instanceof PropertyEditable)) {
                    Map defaultPropertyMap = new HashMap();

                    String properties = pluginDefaultProperties.getPluginProperties();
                    if (properties != null && properties.trim().length() > 0) {
                        defaultPropertyMap = CsvUtil.getPluginPropertyMap(properties);
                    }
                    map.addAttribute("defaultPropertyMap", defaultPropertyMap);
                } else {
                    map.addAttribute("defaultProperties", pluginDefaultProperties.getPluginProperties());
                }
            }
        }

        if (plugin instanceof PropertyEditable) {
            map.addAttribute("propertyEditable", (PropertyEditable) plugin);
        }

        String url = request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/processes/" + URLEncoder.encode(processDefId, "UTF-8") + "/participant/" + participantId + "/submit/plugin?param_value=" + plugin.getClass().getName();

        map.addAttribute("plugin", plugin);
        map.addAttribute("actionUrl", url);

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)/remove", method = RequestMethod.POST)
    @Transactional
    public String consoleParticipantRemove(ModelMap map,
            @RequestParam String appId,
            @RequestParam(required = false) String version,
            @RequestParam String processDefId,
            @RequestParam String participantId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "value", required = false) String value) throws UnsupportedEncodingException {

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        if ((PackageParticipant.TYPE_USER.equals(type) || PackageParticipant.TYPE_GROUP.equals(type)) && value != null) {
            PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
            if (participantExisting != null) {
                //Using Set to prevent duplicate value
                Set values = new HashSet();
                StringTokenizer existingValueToken = new StringTokenizer(participantExisting.getValue(), ",");
                while (existingValueToken.hasMoreTokens()) {
                    String temp = (String) existingValueToken.nextElement();
                    if (!temp.equals(value)) {
                        values.add(temp);
                    }
                }

                //Convert Set to String
                String result = "";
                Iterator i = values.iterator();
                while (i.hasNext()) {
                    result += i.next().toString() + ',';
                }
                if (value.length() > 0) {
                    result = result.substring(0, result.length() - 1);
                }
                participantExisting.setValue(result);
                packageDefinitionDao.addAppParticipant(appId, appDef.getVersion(), participantExisting);
            }
        } else {
            packageDefinitionDao.removeAppParticipant(appId, appDef.getVersion(), processDefId, participantId);
        }

        return "console/apps/participantAdd";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalists")
    public String consoleDatalistList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/datalistList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalist/create")
    public String consoleDatalistCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        DatalistDefinition datalistDefinition = new DatalistDefinition();
        map.addAttribute("datalistDefinition", datalistDefinition);
        return "console/apps/datalistCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/submit/(*:action)", method = RequestMethod.POST)
    public String consoleDatalistSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("datalistDefinition") DatalistDefinition datalistDefinition, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(datalistDefinition, result);
        datalistDefinition.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            // check exist
            if (datalistDefinitionDao.loadById(datalistDefinition.getId(), appDef) != null) {
                errors.add("console.datalist.error.label.exists");
            } else {
                datalistDefinition.setJson("{\"id\":\"" + datalistDefinition.getId() + "\",\"name\":\"" + datalistDefinition.getName() + "\",\"pageSize\":\"10\",\"order\":\"\",\"orderBy\":\"\",\"actions\":[],\"rowActions\":[],\"filters\":[],\"binder\":{\"name\":\"\",\"className\":\"\",\"properties\":{}},\"columns\":[]}");
                invalid = !datalistDefinitionDao.add(datalistDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("datalistDefinition", datalistDefinition);

        if (invalid) {
            return "console/apps/datalistCreate";
        } else {
            return "console/apps/datalistSaved";
        }
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/datalist/list")
    public void consoleDatalistListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<DatalistDefinition> datalistDefinitionList = null;
        Long count = null;

        if (appDef != null) {
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(filterString, appDef, sort, desc, start, rows);
            count = datalistDefinitionDao.getDatalistDefinitionListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (datalistDefinitionList != null && datalistDefinitionList.size() > 0) {
            for (DatalistDefinition datalistDefinition : datalistDefinitionList) {
                Map data = new HashMap();
                data.put("id", datalistDefinition.getId());
                data.put("name", datalistDefinition.getName());
                data.put("description", datalistDefinition.getDescription());
                data.put("dateCreated", datalistDefinition.getDateCreated());
                data.put("dateModified", datalistDefinition.getDateModified());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/delete", method = RequestMethod.POST)
    public String consoleDatalistDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            datalistDefinitionDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/datalist/options")
    public void consoleDatalistOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<DatalistDefinition> datalistDefinitionList = null;
        
        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (DatalistDefinition datalistDef : datalistDefinitionList) {
            Map data = new HashMap();
            data.put("value", datalistDef.getId());
            data.put("label", datalistDef.getName());
            jsonArray.put(data);
        }
        writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userviews")
    public String consoleUserviewList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/userviewList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userview/create")
    public String consoleUserviewCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        UserviewDefinition userviewDefinition = new UserviewDefinition();
        map.addAttribute("userviewDefinition", userviewDefinition);
        return "console/apps/userviewCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserviewSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("userviewDefinition") UserviewDefinition userviewDefinition, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(userviewDefinition, result);
        userviewDefinition.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            // check exist
            if (userviewDefinitionDao.loadById(userviewDefinition.getId(), appDef) != null) {
                errors.add("console.userview.error.label.exists");
            } else {
                userviewDefinition.setJson("{\"className\":\"org.joget.apps.userview.model.Userview\",\"properties\":{\"id\":\"" + userviewDefinition.getId() + "\",\"name\":\"" + userviewDefinition.getName() + "\",\"description\":\"" + userviewDefinition.getDescription() + "\",\"welcomeMessage\":\"#date.EEE, d MMM yyyy#\",\"logoutText\":\"Logout\",\"footerMessage\":\"Powered by Joget\",},\"setting\":{\"properties\":{\"theme\":{\"className\":\"org.joget.apps.userview.lib.DefaultTheme\",\"properties\":{}}}},\"categories\":[]}");
                invalid = !userviewDefinitionDao.add(userviewDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("userviewDefinition", userviewDefinition);

        if (invalid) {
            return "console/apps/userviewCreate";
        } else {
            return "console/apps/userviewSaved";
        }
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/list")
    public void consoleUserviewListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<UserviewDefinition> userviewDefinitionList = null;
        Long count = null;

        if (appDef != null) {
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(filterString, appDef, sort, desc, start, rows);
            count = userviewDefinitionDao.getUserviewDefinitionListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (userviewDefinitionList != null && userviewDefinitionList.size() > 0) {
            for (UserviewDefinition userviewDefinition : userviewDefinitionList) {
                Map data = new HashMap();
                data.put("id", userviewDefinition.getId());
                data.put("name", userviewDefinition.getName());
                data.put("description", userviewDefinition.getDescription());
                data.put("dateCreated", userviewDefinition.getDateCreated());
                data.put("dateModified", userviewDefinition.getDateModified());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/delete", method = RequestMethod.POST)
    public String consoleUserviewDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            userviewDefinitionDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/properties")
    public String consoleProperties(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("localeList", messageDao.getLocaleList(appDef));
        return "console/apps/properties";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/create")
    public String consoleAppMessageCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("localeList", getSortedLocalList());

        Message message = new Message();
        message.setLocale(AppUtil.getAppLocale());
        map.addAttribute("message", message);
        return "console/apps/messageCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/edit")
    public String consoleAppMessageEdit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Message message = messageDao.loadById(id, appDef);
        map.addAttribute("message", message);
        return "console/apps/messageEdit";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/submit/(*:action)", method = RequestMethod.POST)
    public String consoleAppMessageSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("message") Message message, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(message, result);
        message.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (messageDao.loadById(message.getId(), appDef) != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    invalid = !messageDao.add(message);
                }
            } else {
                Message o = messageDao.loadById(message.getId(), appDef);
                o.setMessage(message.getMessage());
                invalid = !messageDao.update(o);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("message", message);
            if ("create".equals(action)) {
                map.addAttribute("localeList", getSortedLocalList());
                return "console/apps/messageCreate";
            } else {
                return "console/apps/messageEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/" + version + "/properties?tab=message";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/message/list")
    public void consoleMessageListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "locale", required = false) String locale, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<Message> messageList = null;
        Long count = null;

        if (locale != null && locale.trim().isEmpty()) {
            locale = null;
        }

        if (appDef != null) {
            messageList = messageDao.getMessageList(filterString, locale, appDef, sort, desc, start, rows);
            count = messageDao.getMessageListCount(filterString, locale, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                Map data = new HashMap();
                data.put("id", message.getId());
                data.put("messageKey", message.getMessageKey());
                data.put("locale", message.getLocale());
                data.put("message", message.getMessage());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/delete", method = RequestMethod.POST)
    public String consoleAppMessageDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            messageDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/message/generatepo")
    public String consoleAppMessageGeneratePO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("localeList", getSortedLocalList());
        map.addAttribute("locale", AppUtil.getAppLocale());
        
        return "console/apps/messageGeneratePO";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/message/generatepo/download")
    public void consoleAppMessageGeneratePODownload(HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "locale", required = false) String locale) throws IOException {
        ServletOutputStream output = null;
        try {
            // determine output filename
            String filename = appId + "_" + version + "_" + locale + ".po";

            // set response headers
            response.setContentType("text/plain");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            output = response.getOutputStream();

            appService.generatePO(appId, version, locale, output);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (output != null) {
                output.flush();
            }
        }
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/message/importpo")
    public String consoleAppMessageImportPO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        return "console/apps/messageImportPO";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/importpo/submit", method = RequestMethod.POST)
    public String consoleAppMessageInportPOUpload(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) throws Exception {
        Setting setting = setupManager.getSettingByProperty("systemLocale");
        String systemLocale = (setting != null) ? setting.getValue() : null;
        if (systemLocale == null || systemLocale.equalsIgnoreCase("")) {
            systemLocale = "en_US";
        }

        try {
            MultipartFile multiPartfile = FileStore.getFile("localeFile");
            appService.importPO(appId, version, systemLocale, multiPartfile);
        } catch (IOException e) {
        }
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appId + "/" + version + "/properties?tab=message";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/create")
    public String consoleAppEnvVariableCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("environmentVariable", new EnvironmentVariable());
        return "console/apps/envVariableCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/edit/(*:id)")
    public String consoleAppEnvVariableEdit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        EnvironmentVariable environmentVariable = environmentVariableDao.loadById(id, appDef);
        map.addAttribute("environmentVariable", environmentVariable);
        return "console/apps/envVariableEdit";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/submit/(*:action)", method = RequestMethod.POST)
    public String consoleAppEnvVariableSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("environmentVariable") EnvironmentVariable environmentVariable, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        // validation
        validator.validate(environmentVariable, result);
        environmentVariable.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (environmentVariableDao.loadById(environmentVariable.getId(), appDef) != null) {
                    errors.add("console.app.envVariable.error.label.exists");
                } else {
                    invalid = !environmentVariableDao.add(environmentVariable);
                }
            } else {
                EnvironmentVariable o = environmentVariableDao.loadById(environmentVariable.getId(), appDef);
                o.setRemarks(environmentVariable.getRemarks());
                o.setValue(environmentVariable.getValue());
                invalid = !environmentVariableDao.update(o);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("environmentVariable", environmentVariable);
            if ("create".equals(action)) {
                return "console/apps/envVariableCreate";
            } else {
                return "console/apps/envVariableEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/" + version + "/properties?tab=variable";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/envVariable/list")
    public void consoleEnvVariableListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<EnvironmentVariable> environmentVariableList = null;
        Long count = null;

        if (appDef != null) {
            environmentVariableList = environmentVariableDao.getEnvironmentVariableList(filterString, appDef, sort, desc, start, rows);
            count = environmentVariableDao.getEnvironmentVariableListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (environmentVariableList != null && environmentVariableList.size() > 0) {
            for (EnvironmentVariable environmentVariable : environmentVariableList) {
                Map data = new HashMap();
                data.put("id", environmentVariable.getId());
                data.put("value", environmentVariable.getValue());
                data.put("remarks", environmentVariable.getRemarks());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/delete", method = RequestMethod.POST)
    public String consoleAppEnvVariableDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            environmentVariableDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/create")
    public String consoleAppPluginDefaultCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("pluginType", getPluginTypeForDefaultProperty());

        return "console/apps/pluginDefaultCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/config")
    public String consoleAppPluginDefaultConfig(ModelMap map, HttpServletRequest request, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id, @RequestParam(required = false) String action) throws UnsupportedEncodingException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Plugin plugin = pluginManager.getPlugin(id);

        if (!"create".equals(action)) {
            PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(id, appDef);

            if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
                if (!(plugin instanceof PropertyEditable)) {
                    Map propertyMap = new HashMap();
                    propertyMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                    map.addAttribute("propertyMap", propertyMap);
                } else {
                    map.addAttribute("properties", pluginDefaultProperties.getPluginProperties());
                }
            }
        }

        if (plugin instanceof PropertyEditable) {
            map.addAttribute("propertyEditable", (PropertyEditable) plugin);
        }

        String url = request.getContextPath() + "/web/console/app/" + appId + "/" + version + "/pluginDefault/submit/";
        if ("create".equals(action)) {
            url += "create";
        } else {
            url += "edit";
        }
        url += "?param_id=" + plugin.getClass().getName();

        map.addAttribute("plugin", plugin);
        map.addAttribute("skipValidation", true);
        map.addAttribute("actionUrl", url);

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/pluginDefault/submit/(*:param_action)", method = RequestMethod.POST)
    public String consoleAppPluginDefaultSubmit(ModelMap map, HttpServletRequest request, @RequestParam("param_action") String action, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_id") String id, @RequestParam(required = false) String pluginProperties) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        PluginDefaultProperties pluginDefaultProperties = null;

        if ("create".equals(action)) {
            pluginDefaultProperties = new PluginDefaultProperties();
            pluginDefaultProperties.setAppDefinition(appDef);
            pluginDefaultProperties.setId(id);
        } else {
            pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(id, appDef);
        }

        try {
            Plugin plugin = (Plugin) pluginManager.getPlugin(id);
            pluginDefaultProperties.setPluginName(plugin.getName());
            pluginDefaultProperties.setPluginDescription(plugin.getDescription());
        } catch (Exception e) {
        }

        if (pluginProperties == null) {
            //request params
            Map<String, String> propertyMap = new HashMap();
            Enumeration<String> e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = e.nextElement();

                if (!paramName.startsWith("param_")) {
                    String[] paramValue = (String[]) request.getParameterValues(paramName);
                    propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                }
            }

            // form csv properties
            StringWriter sw = new StringWriter();
            try {
                CSVWriter writer = new CSVWriter(sw);
                Iterator it = propertyMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                }
                writer.close();
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
            String pluginProps = sw.toString();
            pluginDefaultProperties.setPluginProperties(pluginProps);
        } else {
            pluginDefaultProperties.setPluginProperties(pluginProperties);
        }

        if ("create".equals(action)) {
            pluginDefaultPropertiesDao.add(pluginDefaultProperties);
        } else {
            pluginDefaultPropertiesDao.update(pluginDefaultProperties);
        }
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appId + "/" + version + "/properties?tab=pluginDefault";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/pluginDefault/list")
    public void consolePluginDefaultListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<PluginDefaultProperties> pluginDefaultPropertiesList = null;
        Long count = null;

        if (appDef != null) {
            pluginDefaultPropertiesList = pluginDefaultPropertiesDao.getPluginDefaultPropertiesList(filterString, appDef, sort, desc, start, rows);
            count = pluginDefaultPropertiesDao.getPluginDefaultPropertiesListCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (pluginDefaultPropertiesList != null && pluginDefaultPropertiesList.size() > 0) {
            for (PluginDefaultProperties pluginDefaultProperties : pluginDefaultPropertiesList) {
                Map data = new HashMap();
                data.put("id", pluginDefaultProperties.getId());
                data.put("pluginName", pluginDefaultProperties.getPluginName());
                data.put("pluginDescription", pluginDefaultProperties.getPluginDescription());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/pluginDefault/delete", method = RequestMethod.POST)
    public String consoleAppPluginDefaultDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            pluginDefaultPropertiesDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/forms")
    public String consoleFormList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        return "console/apps/formList";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/forms")
    public void consoleFormListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<FormDefinition> formDefinitionList = null;
        Long count = null;

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        formDefinitionList = formDefinitionDao.getFormDefinitionList(name, appDef, sort, desc, start, rows);
        count = formDefinitionDao.getFormDefinitionListCount(null, appDef);

        JSONObject jsonObject = new JSONObject();
        for (FormDefinition formDef : formDefinitionList) {
            Map data = new HashMap();
            data.put("id", formDef.getId());
            data.put("name", formDef.getName());
            data.put("tableName", formDef.getTableName());
            data.put("dateCreated", formDef.getDateCreated());
            data.put("dateModified", formDef.getDateModified());
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/forms/options")
    public void consoleFormOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        Collection<FormDefinition> formDefinitionList = null;
        
        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (FormDefinition formDef : formDefinitionList) {
            Map data = new HashMap();
            data.put("value", formDef.getId());
            data.put("label", formDef.getName());
            jsonArray.put(data);
        }
        writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/create")
    public String consoleFormCreate(ModelMap model, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        model.addAttribute("appId", appId);
        model.addAttribute("appVersion", version);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formDefinition", new FormDefinition());
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);
        return "console/apps/formCreate";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/tableNameList")
    public void consoleFormTableNameList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        Collection<String> tableNameList = formDefinitionDao.getTableNameList(appDef);

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("tableName", tableNameList);
        if (callback != null && callback.trim().length() != 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/submit")
    public String consoleFormSubmit(ModelMap model, @ModelAttribute("formDefinition") FormDefinition formDefinition, BindingResult result, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId) throws UnsupportedEncodingException {

        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // validate ID
        validator.validate(formDefinition, result);
        boolean invalid = result.hasErrors();
        if (!invalid) {
            // create form
            Collection<String> errors = appService.createFormDefinition(appDef, formDefinition);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        String formId = formDefinition.getId();
        model.addAttribute("appId", appId);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formId", formId);
        model.addAttribute("formDefinition", formDefinition);
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);

        if (invalid) {
            return "console/apps/formCreate";
        } else {
            if (activityDefId != null && activityDefId.trim().length() > 0 && processDefId != null && processDefId.trim().length() > 0) {
                PackageDefinition packageDef = appDef.getPackageDefinition();
                boolean autoContinue = false;
                if (packageDef != null) {
                    autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
                }
                processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
                PackageActivityForm activityForm = new PackageActivityForm();
                activityForm.setProcessDefId(processDefId);
                activityForm.setActivityDefId(activityDefId);
                activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
                activityForm.setFormId(formId);
                activityForm.setAutoContinue(autoContinue);

                packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), activityForm);
                model.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
            }

            return "console/apps/formSaved";
        }
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/(*:formId)/update", method = RequestMethod.POST)
    public String consoleFormUpdate(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId, @RequestParam(value = "json") String json) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // load existing form definition and update fields
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        Form form = (Form) formService.createElementFromJson(json);
        formDef.setName(form.getPropertyString("name"));
        formDef.setTableName(form.getPropertyString("tableName"));
        formDef.setJson(json);

        // update
        formDefinitionDao.update(formDef);
        formDataDao.clearFormCache(form);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/delete", method = RequestMethod.POST)
    public String consoleFormDelete(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        StringTokenizer strToken = new StringTokenizer(formId, ",");
        while (strToken.hasMoreTokens()) {
            String id = strToken.nextToken();
            formDefinitionDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/run/apps")
    public String consoleRunApps(ModelMap model) {
        // get list of published apps.
        Collection<AppDefinition> resultAppDefinitionList = appService.getPublishedApps(null);
        model.addAttribute("appDefinitionList", resultAppDefinitionList);
        return "console/run/runApps";
    }

    @RequestMapping("/console/run/processes")
    public String consoleRunProcesses(ModelMap model) {
        // get list of published processes
        Map<AppDefinition, Collection<WorkflowProcess>> appProcessMap = appService.getPublishedProcesses(null);
        model.addAttribute("appDefinitionList", appProcessMap.keySet());
        model.addAttribute("appProcessMap", appProcessMap);
        return "console/run/runProcesses";
    }

    @RequestMapping("/console/run/inbox")
    public String consoleRunInbox(ModelMap model) {
        User user = directoryManager.getUserByUsername(WorkflowUtil.getCurrentUsername());
        if (user != null) {
            model.addAttribute("rssLink", "/web/rss/client/inbox?j_username=" + user.getUsername() + "&hash=" + user.getLoginHash());
        }
        return "console/run/inbox";
    }

    @RequestMapping("/console/setting/general")
    public String consoleSettingGeneral(ModelMap map) {
        Collection<Setting> settingList = setupManager.getSettingList("", null, null, null, null);

        Map<String, String> settingMap = new HashMap<String, String>();
        for (Setting setting : settingList) {
            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(setting.getProperty())) {
                settingMap.put(setting.getProperty(), SetupManager.SECURE_VALUE);
            } else {
                settingMap.put(setting.getProperty(), setting.getValue());
            }
        }

        Properties properties = DynamicDataSourceManager.getProperties();
        for (Object key : properties.keySet()) {
            settingMap.put(key.toString(), properties.getProperty(key.toString()));
        }

        Locale[] localeList = Locale.getAvailableLocales();
        Map<String, String> localeStringList = new TreeMap<String, String>();
        for (int x = 0; x < localeList.length; x++) {
            localeStringList.put(localeList[x].toString(), localeList[x].toString() + " - " +localeList[x].getDisplayName(localeResolver.resolveLocale(WorkflowUtil.getHttpServletRequest())));
        }

        map.addAttribute("localeList", localeStringList);
        map.addAttribute("settingMap", settingMap);

        return "console/setting/general";
    }
    
    @RequestMapping("/console/setting/general/loginHash")
    public void loginHash(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("username") String username, @RequestParam("password") String password) throws JSONException, IOException {
        if (SetupManager.SECURE_VALUE.equals(password)) {
            password = setupManager.getSettingValue(SetupManager.MASTER_LOGIN_PASSWORD);
            password = SecurityUtil.decrypt(password);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(StringUtil.md5Base16(password));
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("hash", user.getLoginHash());

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/general/submit", method = RequestMethod.POST)
    public String consoleSettingGeneralSubmit(HttpServletRequest request, ModelMap map) {
        boolean deleteProcessOnCompletionIsNull = true;
        boolean enableNtlmIsNull = true;
        boolean rightToLeftIsNull = true;
        boolean enableUserLocale = true;
        boolean dateFormatFollowLocale = true;

        //request params
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = request.getParameter(paramName);

            if (paramName.equals("deleteProcessOnCompletion")) {
                deleteProcessOnCompletionIsNull = false;
                paramValue = "true";
            }

            if (paramName.equals("enableNtlm")) {
                enableNtlmIsNull = false;
                paramValue = "true";
            }

            if (paramName.equals("rightToLeft")) {
                rightToLeftIsNull = false;
                paramValue = "true";
            }
            
            if (paramName.equals("enableUserLocale")) {
                enableUserLocale = false;
                paramValue = "true";
            }
            
            if (paramName.equals("dateFormatFollowLocale")) {
                dateFormatFollowLocale = false;
                paramValue = "true";
            }

            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(paramName)) {
                if (SetupManager.SECURE_VALUE.equals(paramValue)) {
                    paramValue = setupManager.getSettingValue(SetupManager.MASTER_LOGIN_PASSWORD);
                } else {
                    paramValue = SecurityUtil.encrypt(paramValue);
                }
            }

            Setting setting = setupManager.getSettingByProperty(paramName);
            if (setting == null) {
                setting = new Setting();
                setting.setProperty(paramName);
                setting.setValue(paramValue);
            } else {
                setting.setValue(paramValue);
            }
            setupManager.saveSetting(setting);
        }

        if (deleteProcessOnCompletionIsNull) {
            Setting setting = setupManager.getSettingByProperty("deleteProcessOnCompletion");
            if (setting == null) {
                setting = new Setting();
                setting.setProperty("deleteProcessOnCompletion");
            }
            setting.setValue("false");
            setupManager.saveSetting(setting);
        }

        if (enableNtlmIsNull) {
            Setting setting = setupManager.getSettingByProperty("enableNtlm");
            if (setting == null) {
                setting = new Setting();
                setting.setProperty("enableNtlm");
            }
            setting.setValue("false");
            setupManager.saveSetting(setting);
        }

        if (rightToLeftIsNull) {
            Setting setting = setupManager.getSettingByProperty("rightToLeft");
            if (setting == null) {
                setting = new Setting();
                setting.setProperty("rightToLeft");
            }
            setting.setValue("false");
            setupManager.saveSetting(setting);
        }
        
        if (enableUserLocale) {
            Setting setting = setupManager.getSettingByProperty("enableUserLocale");
            if (setting == null) {
                setting = new Setting();
                setting.setProperty("enableUserLocale");
            }
            setting.setValue("false");
            setupManager.saveSetting(setting);
        }
        
        if (dateFormatFollowLocale) {
            Setting setting = setupManager.getSettingByProperty("dateFormatFollowLocale");
            if (setting == null) {
                setting = new Setting();
                setting.setProperty("dateFormatFollowLocale");
            }
            setting.setValue("false");
            setupManager.saveSetting(setting);
        }

        pluginManager.refresh();
        workflowManager.internalUpdateDeadlineChecker();

        return "redirect:/web/console/setting/general";
    }

    @RequestMapping("/console/setting/datasource")
    public String consoleSettingDatasource(ModelMap map) {
        Map<String, String> settingMap = new HashMap<String, String>();

        Properties properties = DynamicDataSourceManager.getProperties();
        for (Object key : properties.keySet()) {
            settingMap.put(key.toString(), properties.getProperty(key.toString()));
        }

        map.addAttribute("settingMap", settingMap);
        map.addAttribute("profileList", DynamicDataSourceManager.getProfileList());
        map.addAttribute("currentProfile", DynamicDataSourceManager.getCurrentProfile());

        return "console/setting/datasource";
    }

    @RequestMapping(value = "/console/setting/profile/change", method = RequestMethod.POST)
    public void consoleProfileChange(Writer writer, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            DynamicDataSourceManager.changeProfile(profileName);
        }
    }

    @RequestMapping(value = "/console/setting/profile/create", method = RequestMethod.POST)
    public void consoleProfileCreate(Writer writer, HttpServletRequest request, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            DynamicDataSourceManager.createProfile(profileName);
            DynamicDataSourceManager.changeProfile(profileName);

            //request params
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = (String) e.nextElement();
                if (!paramName.equals("profileName")) {
                    String paramValue = request.getParameter(paramName);
                    DynamicDataSourceManager.writeProperty(paramName, paramValue);
                }
            }
        }
    }

    @RequestMapping(value = "/console/setting/profile/delete", method = RequestMethod.POST)
    public void consoleProfileDelete(Writer writer, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            DynamicDataSourceManager.deleteProfile(profileName);
        }
    }

    @RequestMapping(value = "/console/setting/datasource/submit", method = RequestMethod.POST)
    public String consoleSetupDatasourceSubmit(HttpServletRequest request, ModelMap map) {
        //request params
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            if (!paramName.equals("profileName")) {
                String paramValue = request.getParameter(paramName);
                DynamicDataSourceManager.writeProperty(paramName, paramValue);
            }
        }

        return "redirect:/web/console/setting/datasource";
    }

    @RequestMapping("/console/setting/directory")
    public String consoleSettingDirectory(ModelMap map) {
        Collection<Setting> settingList = setupManager.getSettingList("", null, null, null, null);

        Map<String, String> settingMap = new HashMap<String, String>();
        for (Setting setting : settingList) {
            settingMap.put(setting.getProperty(), setting.getValue());
        }

        //get directory manager plugin list
        Collection<Plugin> pluginList = pluginManager.list();
        Iterator i = pluginList.iterator();
        while (i.hasNext()) {
            Plugin plugin = (Plugin) i.next();
            if (!(plugin instanceof DirectoryManagerPlugin)) {
                i.remove();
            }
        }

        if (settingMap.get("directoryManagerImpl") != null) {
            Plugin plugin = pluginManager.getPlugin(settingMap.get("directoryManagerImpl"));
            if (plugin != null) {
                map.addAttribute("directoryManagerName", plugin.getName());
                if (!DirectoryUtil.isCustomDirectoryManager()) {
                    map.addAttribute("directoryManagerConfigError", true);
                }
            }
        }

        map.addAttribute("settingMap", settingMap);
        map.addAttribute("directoryManagerPluginList", pluginList);

        return "console/setting/directoryManager";
    }

    @RequestMapping(value = "/console/setting/directoryManagerImpl/remove", method = RequestMethod.POST)
    public void consoleSettingDirectoryManagerImplRemove(Writer writer, ModelMap map) {
        setupManager.deleteSetting("directoryManagerImpl");
        setupManager.deleteSetting("directoryManagerImplProperties");
    }

    @RequestMapping("/console/setting/directoryManagerImpl/config")
    public String consoleSettingDirectoryManagerImplConfig(ModelMap map, @RequestParam("directoryManagerImpl") String directoryManagerImpl, HttpServletRequest request) throws IOException {
        Plugin plugin = pluginManager.getPlugin(directoryManagerImpl);

        String properties = setupManager.getSettingValue("directoryManagerImplProperties");
        if (!(plugin instanceof PropertyEditable)) {
            Map propertyMap = new HashMap();
            propertyMap = CsvUtil.getPluginPropertyMap(properties);
            map.addAttribute("propertyMap", propertyMap);
        } else {
            map.addAttribute("properties", properties);
        }

        if (plugin instanceof PropertyEditable) {
            map.addAttribute("propertyEditable", (PropertyEditable) plugin);
        }

        map.addAttribute("plugin", plugin);

        String url = request.getContextPath() + "/web/console/setting/directoryManagerImpl/config/submit?id=" + directoryManagerImpl;
        map.addAttribute("actionUrl", url);

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/setting/directoryManagerImpl/config/submit", method = RequestMethod.POST)
    public String consoleSettingDirectoryManagerImplConfigSubmit(ModelMap map, @RequestParam("id") String id, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) {
        Plugin plugin = (Plugin) pluginManager.getPlugin(id);

        //save plugin
        Setting setting = setupManager.getSettingByProperty("directoryManagerImpl");
        if (setting == null) {
            setting = new Setting();
            setting.setProperty("directoryManagerImpl");
        }
        setting.setValue(id);
        setupManager.saveSetting(setting);

        Setting propertySetting = setupManager.getSettingByProperty("directoryManagerImplProperties");
        if (propertySetting == null) {
            propertySetting = new Setting();
            propertySetting.setProperty("directoryManagerImplProperties");
        }

        if (pluginProperties == null) {
            //request params
            Map<String, String> propertyMap = new HashMap();
            Enumeration<String> e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = e.nextElement();

                if (!paramName.startsWith("param_")) {
                    String[] paramValue = (String[]) request.getParameterValues(paramName);
                    propertyMap.put(paramName, CsvUtil.getDeliminatedString(paramValue));
                }
            }

            // form csv properties
            StringWriter sw = new StringWriter();
            try {
                CSVWriter writer = new CSVWriter(sw);
                Iterator it = propertyMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pairs = (Map.Entry) it.next();
                    writer.writeNext(new String[]{pairs.getKey(), pairs.getValue()});
                }
                writer.close();
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
            String pluginProps = sw.toString();
            propertySetting.setValue(pluginProps);
        } else {
            propertySetting.setValue(pluginProperties);
        }
        setupManager.saveSetting(propertySetting);

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/setting/directory";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/plugin")
    public String consoleSettingPlugin(ModelMap map) {
        map.addAttribute("pluginType", getPluginType());
        return "console/setting/plugin";
    }

    @RequestMapping("/console/setting/plugin/refresh")
    public void consoleSettingPluginRefresh(Writer writer) {
        pluginManager.refresh();
    }

    @RequestMapping("/console/setting/plugin/upload")
    public String consoleSettingPluginUpload() {
        return "console/setting/pluginUpload";
    }

    @RequestMapping(value = "/console/setting/plugin/upload/submit", method = RequestMethod.POST)
    public String consoleSettingPluginUploadSubmit(ModelMap map, HttpServletRequest request) throws IOException {
        MultipartFile pluginFile = FileStore.getFile("pluginFile");

        try {
            pluginManager.upload(pluginFile.getOriginalFilename(), pluginFile.getInputStream());
        } catch (Exception e) {
            if (e.getCause().getMessage() != null && e.getCause().getMessage().contains("Invalid jar file")) {
                map.addAttribute("errorMessage", "Invalid jar file");
            } else {
                map.addAttribute("errorMessage", "Error uploading plugin");
            }
            return "console/setting/pluginUpload";
        }
        String url = request.getContextPath() + "/web/console/setting/plugin";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/setting/plugin/uninstall", method = RequestMethod.POST)
    public String consoleSettingPluginUninstall(ModelMap map, @RequestParam("selectedPlugins") String selectedPlugins) {
        StringTokenizer strToken = new StringTokenizer(selectedPlugins, ",");
        while (strToken.hasMoreTokens()) {
            String pluginClassName = (String) strToken.nextElement();
            pluginManager.uninstall(pluginClassName);
        }
        return "redirect:/web/console/setting/plugin";
    }

    @RequestMapping("/console/setting/message")
    public String consoleSettingMessage(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());
        return "console/setting/message";
    }

    @RequestMapping("/console/setting/message/create")
    public String consoleSettingMessageCreate(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());

        ResourceBundleMessage message = new ResourceBundleMessage();
        map.addAttribute("message", message);
        return "console/setting/messageCreate";
    }

    @RequestMapping("/console/setting/message/edit/(*:id)")
    public String consoleSettingMessageEdit(ModelMap map, @RequestParam("id") String id) {
        ResourceBundleMessage message = rbmDao.getMessageById(id);
        map.addAttribute("message", message);
        return "console/setting/messageEdit";
    }

    @RequestMapping(value = "/console/setting/message/submit/(*:action)", method = RequestMethod.POST)
    public String consoleSettingMessageSubmit(ModelMap map, @RequestParam("action") String action, @ModelAttribute("message") ResourceBundleMessage message, BindingResult result) {
        // validation
        validator.validate(message, result);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            if ("create".equals(action)) {
                // check exist
                if (rbmDao.getMessage(message.getKey(), message.getLocale()) != null) {
                    errors.add("console.app.message.error.label.exists");
                } else {
                    rbmDao.saveOrUpdate(message);
                    invalid = false;
                }
            } else {
                ResourceBundleMessage o = rbmDao.getMessageById(message.getId());
                o.setMessage(message.getMessage());
                rbmDao.saveOrUpdate(o);
                invalid = false;
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            map.addAttribute("message", message);
            if ("create".equals(action)) {
                map.addAttribute("localeList", getSortedLocalList());
                return "console/setting/messageCreate";
            } else {
                return "console/setting/messageEdit";
            }
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/setting/message";
            map.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping("/json/console/setting/message/list")
    public void consoleSettingMessageListJson(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "locale", required = false) String locale, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        String condition = "";
        List<String> param = new ArrayList<String>();

        if (locale != null && locale.trim().length() != 0) {
            condition += "e.locale = ? ";
            param.add(locale);
        }

        if (filterString != null && filterString.trim().length() != 0) {
            if (!condition.isEmpty()) {
                condition += " and";
            }
            condition += " (e.key like ? or e.message like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
        }

        if (condition.length() > 0) {
            condition = "WHERE " + condition;
        }

        List<ResourceBundleMessage> messageList = rbmDao.getMessages(condition, param.toArray(new String[param.size()]), sort, desc, start, rows);
        Long count = rbmDao.count(condition, param.toArray(new String[param.size()]));

        JSONObject jsonObject = new JSONObject();
        if (messageList != null && messageList.size() > 0) {
            for (ResourceBundleMessage message : messageList) {
                Map data = new HashMap();
                data.put("id", message.getId());
                data.put("key", message.getKey());
                data.put("locale", message.getLocale());
                data.put("message", message.getMessage());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/message/delete", method = RequestMethod.POST)
    public String consoleSettingMessageDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            ResourceBundleMessage o = rbmDao.getMessageById(id);
            rbmDao.delete(o);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/setting/message/import")
    public String consoleSettingMessageImport(ModelMap map) {
        map.addAttribute("localeList", getSortedLocalList());
        return "console/setting/messageImport";
    }

    @RequestMapping(value = "/console/setting/message/import/submit", method = RequestMethod.POST)
    public String consoleSettingMessagePOFileUpload(ModelMap map) throws Exception {
        Setting setting = setupManager.getSettingByProperty("systemLocale");
        String systemLocale = (setting != null) ? setting.getValue() : null;
        if (systemLocale == null || systemLocale.equalsIgnoreCase("")) {
            systemLocale = "en_US";
        }

        try {
            MultipartFile multiPartfile = FileStore.getFile("localeFile");
            ResourceBundleUtil.POFileImport(multiPartfile, systemLocale);
        } catch (IOException e) {
        }
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/setting/message";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/running")
    public String consoleMonitorRunning(ModelMap map) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        map.addAttribute("appDefinitionList", appDefinitionList);
        return "console/monitor/running";
    }

    @RequestMapping("/json/console/monitor/running/list")
    public void consoleMonitorRunningListJson(Writer writer, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(appId, processId, processName, version, sort, desc, start, rows);
        int count = workflowManager.getRunningProcessSize(appId, processId, processName, version);

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", workflowProcess.getStartedTime());
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? workflowProcess.getDue() : "-");

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/running/process/view/(*:id)")
    public String consoleMonitorRunningProcess(ModelMap map, @RequestParam("id") String processId) {
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(processId);

        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
        map.addAttribute("wfProcess", wfProcess);
        map.addAttribute("trackWflowProcess", trackWflowProcess);

        return "console/monitor/runningProcess";
    }

    @RequestMapping(value = "/console/monitor/running/process/abort/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessAbort(ModelMap map, @RequestParam("id") String processId) {
        workflowManager.processAbort(processId);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/monitor/running/process/reevaluate/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessReevaluate(ModelMap map, @RequestParam("id") String processId) {
        workflowManager.reevaluateAssignmentsForProcess(processId);
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/completed")
    public String consoleMonitorCompleted(ModelMap map) {
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        map.addAttribute("appDefinitionList", appDefinitionList);
        return "console/monitor/completed";
    }

    @RequestMapping("/json/console/monitor/completed/list")
    public void consoleMonitorCompletedListJson(Writer writer, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getCompletedProcessList(appId, processId, processName, version, sort, desc, start, rows);
        int count = workflowManager.getCompletedProcessSize(appId, processId, processName, version);

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", workflowProcess.getStartedTime());
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? workflowProcess.getDue() : "-");
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/completed/process/view/(*:id)")
    public String consoleMonitorCompletedProcess(ModelMap map, @RequestParam("id") String processId) {
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(processId);

        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
        map.addAttribute("wfProcess", wfProcess);
        map.addAttribute("trackWflowProcess", trackWflowProcess);

        return "console/monitor/completedProcess";
    }

    @RequestMapping(value = "/console/monitor/process/delete", method = RequestMethod.POST)
    public String consoleMonitorProcessDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            workflowManager.removeProcessInstance(id);
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/process/viewGraph/(*:id)")
    public String consoleMonitorProcessViewGraph(ModelMap map, @RequestParam("id") String processId) {
        // get process info
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        // get process xpdl
        byte[] xpdlBytes = workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion());
        if (xpdlBytes != null) {
            String xpdl = null;

            try {
                xpdl = new String(xpdlBytes, "UTF-8");
            } catch (Exception e) {
                LogUtil.debug(ConsoleWebController.class.getName(), "XPDL cannot load");
            }
            // get running activities
            Collection<String> runningActivityIdList = new ArrayList<String>();
            List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, 0, -1, "id", false);
            for (WorkflowActivity wa : activityList) {
                if (wa.getState().indexOf("open") >= 0) {
                    runningActivityIdList.add(wa.getActivityDefId());
                }
            }
            String[] runningActivityIds = (String[]) runningActivityIdList.toArray(new String[0]);

            map.addAttribute("wfProcess", wfProcess);
            map.addAttribute("xpdl", xpdl);
            map.addAttribute("runningActivityIds", runningActivityIds);
        }

        return "console/monitor/processGraph";
    }

    @RequestMapping("/json/console/monitor/activity/list")
    public void activityList(Writer writer, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {

        List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, start, rows, sort, desc);

        Integer total = workflowManager.getActivitySize(processId);
        JSONObject jsonObject = new JSONObject();
        for (WorkflowActivity workflowActivity : activityList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(workflowActivity.getId());
            Map data = new HashMap();
            data.put("id", workflowActivity.getId());
            data.put("name", workflowActivity.getName());
            data.put("state", workflowActivity.getState());
            data.put("dateCreated", workflowActivity.getCreatedTime());
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));
            
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping(value = "/console/monitor/(*:processStatus)/process/activity/view/(*:id)")
    public String consoleMonitorActivityView(ModelMap map, @RequestParam("processStatus") String processStatus, @RequestParam("id") String activityId) {
        WorkflowActivity wflowActivity = workflowManager.getActivityById(activityId);
        Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activityId);
        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(activityId);
        WorkflowActivity trackWflowActivity = workflowManager.getRunningActivityInfo(activityId);

        map.addAttribute("activity", wflowActivity);
        map.addAttribute("variableList", variableList);
        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));
    
        if (trackWflowActivity != null) {
            map.addAttribute("trackWflowActivity", trackWflowActivity);
            String[] assignmentUsers = trackWflowActivity.getAssignmentUsers();
            if (assignmentUsers != null && assignmentUsers.length > 0) {
                map.addAttribute("assignUserSize", assignmentUsers.length);
            }
        }

        //TODO - Show Activity Form
        //get form
//        String processDefId = wflowActivity.getProcessDefId();
//        String version = processDefId.split("#")[1];
//        Collection<ActivityForm> formList = activityFormDao.getFormByActivity(wflowActivity.getProcessDefId(), Integer.parseInt(version), wflowActivity.getActivityDefId());
//
//        if (formList != null && !formList.isEmpty()) {
//            ActivityForm form = formList.iterator().next();
//
//            if (form.getType().equals(ActivityFormDao.ACTIVITY_FORM_TYPE_SINGLE)) {
//                map.addAttribute("version", version);
//                map.addAttribute("formId", form.getFormId());
//            }
//        }

        map.addAttribute("processStatus", processStatus);
        return "console/monitor/activity";
    }

    @RequestMapping("/console/monitor/running/activity/reassign")
    public String consoleMonitorActivityReassign(ModelMap map, @RequestParam("state") String state, @RequestParam("processDefId") String processDefId, @RequestParam("activityId") String activityId, @RequestParam("processId") String processId) {
        map.addAttribute("activityId", activityId);
        map.addAttribute("processId", processId);
        map.addAttribute("state", state);
        map.addAttribute("processDefId", processDefId);

        WorkflowActivity trackWflowActivity = workflowManager.getRunningActivityInfo(activityId);
        map.addAttribute("trackWflowActivity", trackWflowActivity);

        return "console/monitor/activityReassign";
    }

    @RequestMapping("/console/monitor/sla")
    public String consoleMonitorSla(ModelMap map) {
        map.addAttribute("processDefinitionList", workflowManager.getProcessList("name", false, 0, -1, null, true, false));
        return "console/monitor/sla";
    }

    @RequestMapping("/console/monitor/audit")
    public String consoleMonitorAuditTrail(ModelMap map) {
        return "console/monitor/auditTrail";
    }
    
    @RequestMapping("/console/monitor/logs")
    public String consoleMonitorLogs(ModelMap map) {
        return "console/monitor/logs";
    }
    
    @RequestMapping("/console/monitor/log/(*:fileName)")
    public void consoleMonitorLogs(HttpServletResponse response, @RequestParam("fileName") String fileName) throws IOException {
        ServletOutputStream stream = response.getOutputStream();
        
        String decodedFileName = fileName;
        try {
            decodedFileName = URLDecoder.decode(fileName, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        
        File file = LogUtil.getTomcatLogFile(decodedFileName);
        if (file == null || file.isDirectory() || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] bbuf = new byte[65536];

        try {
            // set attachment filename
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(decodedFileName, "UTF8"));
            
            // send output
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                stream.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            stream.flush();
            stream.close();
        }
    }
    
    @RequestMapping("/json/console/monitor/logs/list")
    public void consoleMonitorLogsJson(Writer writer, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        File[] files = LogUtil.tomcatLogFiles();
        Collection<File> fileList = new ArrayList<File>();
        
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String lowercaseFN = file.getName().toLowerCase();
                    Date lastModified = new Date(file.lastModified());
                    Date current = new Date();
                    
                    if ((lowercaseFN.startsWith("joget") || lowercaseFN.startsWith("catalina") || lowercaseFN.startsWith("localhost")) 
                        && (lastModified.getTime() > (current.getTime() - (5*1000*60*60*24))) && file.length() > 0) {
                        fileList.add(file);
                    }
                }
            }
        }
        files = fileList.toArray(new File[0]);
        
        Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (File file : files) {
            Map data = new HashMap();
            data.put("filename", file.getName()); 
            data.put("filesize", file.length());
            data.put("date", sf.format(new Date(file.lastModified())));

            jsonObject.accumulate("data", data);
        }
        
        jsonObject.accumulate("total", fileList.size());
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/i18n/(*:name)")
    public String consoleI18n(ModelMap map, @RequestParam("name") String name) throws IOException {
        Properties keys = new Properties();

        //get message key from property file
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(name + ".properties");
        if (inputStream != null) {
            keys.load(inputStream);
        }
        map.addAttribute("name", name);
        map.addAttribute("keys", keys.keys());

        return "console/i18n/lang";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/builder/navigator/(*:builder)/(*:id)")
    public String consoleBuilderNavigator(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "builder") String builder, @RequestParam(value = "id", required = false) String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<FormDefinition> formDefinitionList = null;
        Collection<DatalistDefinition> datalistDefinitionList = null;
        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (appDef != null) {
            formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, "name", false, null, null);
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, "name", false, null, null);
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, "name", false, null, null);
        }

        map.addAttribute("builder", builder);
        map.addAttribute("id", id);
        map.addAttribute("appDef", appDef);
        map.addAttribute("formDefinitionList", formDefinitionList);
        map.addAttribute("datalistDefinitionList", datalistDefinitionList);
        map.addAttribute("userviewDefinitionList", userviewDefinitionList);

        return "console/apps/builderItems";
    }

    protected Map<String, String> getPluginType() {
        Map<String, String> pluginTypeMap = new ListOrderedMap();
        pluginTypeMap.put("org.joget.plugin.base.AuditTrailPlugin", "Audit Trail");
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListAction", "Data List Action");
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListBinder", "Data List Binder");
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListColumnFormat", "Data List Column Format");
        pluginTypeMap.put("org.joget.apps.datalist.model.DataListFilterType", "Data List Filter Type");
        pluginTypeMap.put("org.joget.workflow.model.DeadlinePlugin", "Deadline");
        pluginTypeMap.put("org.joget.directory.model.service.DirectoryManagerPlugin", "Directory Manager");
        pluginTypeMap.put("org.joget.apps.form.model.Element", "Form Element");
        pluginTypeMap.put("org.joget.apps.form.model.FormLoadElementBinder", "Form Load Binder");
        pluginTypeMap.put("org.joget.apps.form.model.FormLoadOptionsBinder", "Form Options Binder");
        pluginTypeMap.put("org.joget.apps.form.model.FormStoreBinder", "Form Store Binder");
        pluginTypeMap.put("org.joget.apps.form.model.FormPermission", "Form Permission");
        pluginTypeMap.put("org.joget.apps.form.model.FormValidator", "Form Validator");
        pluginTypeMap.put("org.joget.apps.app.model.HashVariablePlugin", "Hash Variable");
        pluginTypeMap.put("org.joget.workflow.model.ParticipantPlugin", "Process Participant");
        pluginTypeMap.put("org.joget.plugin.base.ApplicationPlugin", "Process Tool");
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewMenu", "Userview Menu");
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewPermission", "Userview Permission");
        pluginTypeMap.put("org.joget.apps.userview.model.UserviewTheme", "Userview Theme");

        return pluginTypeMap;
    }

    protected Map<String, String> getPluginTypeForDefaultProperty() {
        Map<String, String> pluginTypeMap = new ListOrderedMap();
        pluginTypeMap.put("org.joget.plugin.base.AuditTrailPlugin", "Audit Trail");
        pluginTypeMap.put("org.joget.workflow.model.DeadlinePlugin", "Deadline");
        pluginTypeMap.put("org.joget.workflow.model.ParticipantPlugin", "Process Participant");
        pluginTypeMap.put("org.joget.plugin.base.ApplicationPlugin", "Process Tool");

        return pluginTypeMap;
    }

    protected static void writeJson(Writer writer, JSONObject jsonObject, String callback) throws IOException, JSONException {
        if (callback != null && callback.trim().length() > 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(");
        }
        jsonObject.write(writer);
        if (callback != null && callback.trim().length() > 0) {
            writer.write(")");
        }
    }

    protected static void writeJson(Writer writer, JSONArray jsonArray, String callback) throws IOException, JSONException {
        if (callback != null && callback.trim().length() > 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(");
        }
        jsonArray.write(writer);
        if (callback != null && callback.trim().length() > 0) {
            writer.write(")");
        }
    }

    protected String[] getSortedLocalList() {
        Locale[] localeList = Locale.getAvailableLocales();
        String[] localeStringList = new String[localeList.length];
        for (int i = 0; i < localeList.length; i++) {
            localeStringList[i] = localeList[i].toString();
        }
        Arrays.sort(localeStringList);

        return localeStringList;
    }

    protected String checkVersionExist(ModelMap map, String appId, String version) {
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        
        // get app info
        String appInfo = consoleWebPlugin.getAppInfo(appId, version);
        map.put("appInfo", appInfo);
        
        // verify app license
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        //LogUtil.debug(getClass().getName(), "App info: " + consoleWebPlugin.getAppInfo(appId, version));
        return page;
    }

}
