package org.joget.apps.app.controller;

import au.com.bytecode.opencsv.CSVWriter;
import com.github.underscore.lodash.U;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import static org.joget.apps.app.controller.UserviewWebController.isBackendLicense;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.AppResourceDao;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppOverviewTool;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CreateAppOption;
import org.joget.apps.app.model.CustomBuilder;
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
import org.joget.apps.app.model.ImportAppException;
import org.joget.apps.app.model.ProcessFormModifier;
import org.joget.apps.app.model.StartProcessFormModifier;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppOverviewUtil;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AuditTrailManager;
import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.apps.app.service.MarketplaceUtil;
import org.joget.apps.app.service.PushServiceUtil;
import org.joget.apps.app.service.TaggingUtil;
import org.joget.apps.app.web.LocalLocaleResolver;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.datalist.service.JsonUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.lib.DefaultFormBinder;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormERDEntityRetriever;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.generator.service.GeneratorUtil;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.workflow.security.EnhancedWorkflowUserManager;
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
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.DateUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagedList;
import org.joget.commons.util.PagingUtils;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupDao;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.dao.DepartmentDao;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.GradeDao;
import org.joget.directory.dao.GroupDao;
import org.joget.directory.dao.OrganizationDao;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Organization;
import org.joget.directory.model.service.DirectoryManagerPlugin;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.UserSecurity;
import org.joget.logs.LogViewerAppender;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowManagerImpl;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.HtmlUtils;
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
    DataListService dataListService;
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
    AppResourceDao appResourceDao;
    @Resource
    UserviewDefinitionDao userviewDefinitionDao;
    @Resource
    DatalistDefinitionDao datalistDefinitionDao;
    @Resource
    BuilderDefinitionDao builderDefinitionDao;
    @Resource
    FormDataDao formDataDao;
    @Autowired
    LocaleResolver localeResolver;
    @Autowired
    UserMetaDataDao userMetaDataDao;
    @Autowired
    AuditTrailManager auditTrailManager;
    @Autowired
    WorkflowAssignmentDao workflowAssignmentDao;

    @RequestMapping({"/index", "/", "/home"})
    public String index() {
        String landingPage = WorkflowUtil.getSystemSetupValue("landingPage");

        if (landingPage == null || landingPage.trim().isEmpty()) {
            landingPage = "/home";
        }
        return "redirect:" + landingPage;
    }

    @RequestMapping({"/console", "/console/home"})
    public String consoleHome() {
        return "console/home";
    }
    
    @RequestMapping("/offline")
    public String offline() {
        return "console/offline";
    }

    @RequestMapping("/help/guide")
    public void consoleHelpGuide(Writer writer, @RequestParam("key") String key, @RequestParam(value = "locale", required = false) String localeCode) throws IOException {
        if (key != null && !key.trim().isEmpty()) {
            if (localeCode != null && !localeCode.isEmpty()) {
                String[] temp = localeCode.split("_");
                Locale locale = null;
                if(temp.length == 1){
                    locale = new Locale(temp[0]);
                }else if (temp.length == 2){
                    locale = new Locale(temp[0], temp[1]);
                }else if (temp.length == 3){
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }
                LocaleContextHolder.setLocale(locale);
            }
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
        
        try{
            model.addAttribute("orgId", URLEncoder.encode(id, "UTF-8"));
        } catch (Exception e) {
        }
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
            String url = contextPath + "/web/console/directory/org/view/" + StringEscapeUtils.escapeHtml(id);
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
                    url += "/web/console/directory/org/view/" + StringEscapeUtils.escapeHtml(id);
                }
            } else {
                url += "/web/console/directory/dept/view/" + StringEscapeUtils.escapeHtml(department.getId());
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
                url += "/web/console/directory/grade/view/" + StringEscapeUtils.escapeHtml(grade.getId());
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
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
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
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        Group group = groupDao.getGroup(id);
        if (group.getOrganization() != null) {
            group.setOrganizationId(group.getOrganization().getId());
        }
        model.addAttribute("group", group);
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
            Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
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
            url += "/web/console/directory/group/view/" + StringEscapeUtils.escapeHtml(group.getId());
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
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roleDao.getRoles(null, "name", false, null, null));
        model.addAttribute("timezones", TimeZoneUtil.getList());

        Map<String, String> status = new HashMap<String, String>();
        status.put("1", "Active");
        status.put("0", "Inactive");
        model.addAttribute("status", status);
        
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            model.addAttribute("userFormFooter", us.getUserCreationFormFooter());
        } else {
            model.addAttribute("userFormFooter", "");
        }

        User user = new User();
        user.setActive(1);
        Set roles = new HashSet();
        roles.add(roleDao.getRole("ROLE_USER"));
        user.setRoles(roles);
        //user.setTimeZone(TimeZoneUtil.getServerTimeZone());
        model.addAttribute("user", user);
        model.addAttribute("employments", new HashSet<Employment>());
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
            
            UserSecurity us = DirectoryUtil.getUserSecurity();
            if (us != null) {
                model.addAttribute("addOnButtons", us.getUserDetailsButtons(user));
            }
        }

        model.addAttribute("isCustomDirectoryManager", DirectoryUtil.isCustomDirectoryManager());

        return "console/directory/userView";
    }

    @RequestMapping("/console/directory/user/edit/(*:id)")
    public String consoleUserEdit(ModelMap model, @RequestParam("id") String id) {
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
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
        model.addAttribute("employeeStartDate", employment.getStartDate());
        model.addAttribute("employeeEndDate", employment.getEndDate());
        model.addAttribute("employments", (user.getEmployments() != null)?user.getEmployments():(new HashSet<Employment>()));
        
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            model.addAttribute("userFormFooter", us.getUserEditingFormFooter(user));
        } else {
            model.addAttribute("userFormFooter", "");
        }
        
        return "console/directory/userEdit";
    }

    @RequestMapping(value = "/console/directory/user/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserSubmit(ModelMap model, @RequestParam("action") String action, @ModelAttribute("user") User user, BindingResult result,
            @RequestParam(value = "employeeCode", required = false) String employeeCode, @RequestParam(value = "employeeRole", required = false) String employeeRole,
            @RequestParam(value = "employeeDeptOrganization", required = false) String[] employeeDeptOrganization,
            @RequestParam(value = "employeeDepartment", required = false) String[] employeeDepartment,
            @RequestParam(value = "employeeDepartmentHod", required = false) String[] employeeDepartmentHod, 
            @RequestParam(value = "employeeGradeOrganization", required = false) String[] employeeGradeOrganization,
            @RequestParam(value = "employeeGrade", required = false) String[] employeeGrade,
            @RequestParam(value = "employeeStartDate", required = false) String employeeStartDate, @RequestParam(value = "employeeEndDate", required = false) String employeeEndDate) {
        // validate ID
        validator.validate(user, result);

        UserSecurity us = DirectoryUtil.getUserSecurity();
        User u = null;

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();
            
            if ("create".equals(action)) {
                // check username exist
                if (directoryManager.getUserByUsername(user.getUsername()) != null || (us != null && us.isDataExist(user.getUsername()))) {
                    errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.usernameExists"));
                }
                
                if (us != null) {
                    Collection<String> validationErrors = us.validateUserOnInsert(user);
                    if (validationErrors != null && !validationErrors.isEmpty()) {
                        errors.addAll(validationErrors);
                    }
                }
                
                errors.addAll(validateEmploymentDate(employeeStartDate, employeeEndDate));

                if (errors.isEmpty()) {
                    user.setId(user.getUsername());
                    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                        user.setConfirmPassword(user.getPassword());
                        if (us != null) {
                            user.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                        } else {
                            //md5 password
                            user.setPassword(StringUtil.md5Base16(user.getPassword()));
                        }
                    }

                    //set roles
                    if (user.getRoles() != null && user.getRoles().size() > 0) {
                        Set roles = new HashSet();
                        for (String roleId : (Set<String>) user.getRoles()) {
                            roles.add(roleDao.getRole(roleId));
                        }
                        user.setRoles(roles);
                    }

                    invalid = !userDao.addUser(user);

                    if (us != null && !invalid) {
                        us.insertUserPostProcessing(user);
                    }
                    u = user;
                }
            } else {
                user.setUsername(user.getId());
                
                if (us != null) {
                    Collection<String> validationErrors = us.validateUserOnUpdate(user);
                    if (validationErrors != null && !validationErrors.isEmpty()) {
                        errors.addAll(validationErrors);
                    }
                }
                
                errors.addAll(validateEmploymentDate(employeeStartDate, employeeEndDate));
                
                if (errors.isEmpty()) {
                    boolean passwordReset = false;

                    u = userDao.getUserById(user.getId());
                    u.setFirstName(user.getFirstName());
                    u.setLastName(user.getLastName());
                    u.setEmail(user.getEmail());
                    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                        u.setConfirmPassword(user.getPassword());
                        if (us != null) {
                            passwordReset = true;
                            u.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                        } else {
                            //md5 password
                            u.setPassword(StringUtil.md5Base16(user.getPassword()));
                        }
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
                    if (us != null && !invalid) {
                        us.updateUserPostProcessing(u);
                        if (passwordReset) {
                            us.passwordResetPostProcessing(u);
                        }
                    }
                }
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid || u == null) {
            Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
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
            model.addAttribute("employeeStartDate", employeeStartDate);
            model.addAttribute("employeeEndDate", employeeEndDate);
            
            //convert department & grade to employments
            Collection<Employment> employments = new ArrayList<Employment>();
            if (employeeDepartment != null) {
                for (int i = 0; i < employeeDepartment.length; i++) {
                    Employment t = new Employment();
                    t.setOrganizationId(employeeDeptOrganization[i]);
                    t.setDepartmentId(employeeDepartment[i]);
                    if ("true".equalsIgnoreCase(employeeDepartmentHod[i])) {
                        Set<String> hod = new HashSet<String>();
                        hod.add(employeeDepartment[i]);
                        t.setHods(hod);
                    }
                    if (employeeGradeOrganization != null) {
                        for (int j = 0; j < employeeGradeOrganization.length; j++) {
                            if (employeeGradeOrganization[j].equals(employeeDeptOrganization[i])) {
                                t.setGradeId(employeeGrade[j]);
                            }
                        }
                    }
                    employments.add(t);
                }
            }
            model.addAttribute("employments", employments);
            
            if (us != null) {
                if ("create".equals(action)) {
                    model.addAttribute("userFormFooter", us.getUserCreationFormFooter());
                } else {
                    model.addAttribute("userFormFooter", us.getUserEditingFormFooter(user));
                }
            } else {
                model.addAttribute("userFormFooter", "");
            }
            
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
                    employment = (Employment) u.getEmployments().iterator().next();
                } catch (Exception e) {
                    employment = new Employment();
                }
            }

            employment.setUserId(user.getId());
            employment.setEmployeeCode(employeeCode);
            employment.setRole(employeeRole);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                if (employeeStartDate != null && employeeStartDate.trim().length() > 0) {
                    employment.setStartDate(df.parse(employeeStartDate));
                } else {
                    employment.setStartDate(null);
                }
                if (employeeEndDate != null && employeeEndDate.trim().length() > 0) {
                    employment.setEndDate(df.parse(employeeEndDate));
                } else {
                    employment.setEndDate(null);
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

            //handle departments & grade
            Set<String> existingDepartments = new HashSet<String>();
            Set<String> existingHods = new HashSet<String>();
            Set<String> existingGrades = new HashSet<String>();
            Set<String> existingOrgs = new HashSet<String>();
            if (u.getEmployments() != null && !u.getEmployments().isEmpty()) {
                for (Employment e : (Set<Employment>) u.getEmployments()) {
                    if (e.getDepartmentId() != null) {
                        existingDepartments.add(e.getDepartmentId());
                    }
                    if (e.getHods() != null && !e.getHods().isEmpty()) {
                        existingHods.add(e.getDepartmentId());
                    }
                    if (e.getGradeId() != null) {
                        existingGrades.add(e.getGradeId());
                    }
                    if (e.getOrganizationId() != null) {
                        existingOrgs.add(e.getOrganizationId());
                    }
                }
            }
            if (employeeDepartment != null) {
                for (int i = 0; i < employeeDepartment.length; i++) {
                    if (employeeDepartment[i].isEmpty()) { //if department id is empty
                        if (existingOrgs.contains(employeeDeptOrganization[i])) {
                            existingOrgs.remove(employeeDeptOrganization[i]);
                        } else if (!employeeDeptOrganization[i].isEmpty()) {
                            employmentDao.assignUserToOrganization(u.getId(), employeeDeptOrganization[i]);
                        }
                    } else {
                        if (existingDepartments.contains(employeeDepartment[i])) {
                            existingDepartments.remove(employeeDepartment[i]);
                        } else {
                            employmentDao.assignUserToDepartment(u.getId(), employeeDepartment[i]);
                        }
                        existingOrgs.remove(employeeDeptOrganization[i]);

                        if ("true".equalsIgnoreCase(employeeDepartmentHod[i])) {
                            if (existingHods.contains(employeeDepartment[i])) {
                                existingHods.remove(employeeDepartment[i]);
                            } else {
                                employmentDao.assignUserAsDepartmentHOD(u.getId(), employeeDepartment[i]);
                            }
                        }
                    }
                }
            }
            if (employeeGrade != null) {
                for (int i = 0; i < employeeGrade.length; i++) {
                    if (existingGrades.contains(employeeGrade[i])) {
                        existingGrades.remove(employeeGrade[i]);
                    } else {
                        employmentDao.assignUserToGrade(u.getId(), employeeGrade[i]);
                    }
                    existingOrgs.remove(employeeGradeOrganization[i]);
                }
            }
            for (String d : existingHods) {
                employmentDao.unassignUserAsDepartmentHOD(u.getId(), d);
            }
            for (String d : existingDepartments) {
                employmentDao.unassignUserFromDepartment(u.getId(), d);
            }
            for (String d : existingGrades) {
                employmentDao.unassignUserFromGrade(u.getId(), d);
            }
            //if user not assign to any dept & grade of an org, remove the org
            for (String d : existingOrgs) {
                employmentDao.unassignUserFromOrganization(u.getId(), d);
            }
            
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath;
            url += "/web/console/directory/user/view/" + StringEscapeUtils.escapeHtml(user.getId()) + ".";
            model.addAttribute("url", url);
            return "console/dialogClose";
        }
    }

    @RequestMapping(value = "/console/directory/user/delete", method = RequestMethod.POST)
    public String consoleUserDelete(@RequestParam(value = "ids") String ids) {
        String currentUsername = workflowUserManager.getCurrentUsername();
        
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            
            if (id != null && !id.equals(currentUsername)) {
                userDao.deleteUser(id);

                UserSecurity us = DirectoryUtil.getUserSecurity();
                if (us != null) {
                    us.deleteUserPostProcessing(id);
                }
            }
        }
        return "console/directory/userList";
    }

    @RequestMapping("/console/directory/user/(*:id)/group/assign/view")
    public String consoleUserGroupAssign(ModelMap model, @RequestParam(value = "id") String id) {
        model.addAttribute("id", id);
        Collection<Organization> organizations = organizationDao.getOrganizationsByFilter(null, "name", false, null, null);
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
    public String profile(ModelMap map, HttpServletResponse response) throws IOException{
        User user = userDao.getUser(workflowUserManager.getCurrentUsername());
        
        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        map.addAttribute("user", user);
        map.addAttribute("timezones", TimeZoneUtil.getList());

        String enableUserLocale = setupManager.getSettingValue("enableUserLocale");
        Map<String, String> localeStringList = new TreeMap<String, String>();
        if (enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
            String userLocale = setupManager.getSettingValue("userLocale");
            Collection<String> locales = new HashSet();
            locales.addAll(Arrays.asList(userLocale.split(",")));

            Locale[] localeList = Locale.getAvailableLocales();
            for (int x = 0; x < localeList.length; x++) {
                String code = localeList[x].toString();
                if (locales.contains(code)) {
                    localeStringList.put(code, code + " - " +localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
                }
            }
        }
        
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            map.addAttribute("policies", us.passwordPolicies());
            map.addAttribute("userProfileFooter", us.getUserProfileFooter(user));
        } else {
            map.addAttribute("policies", "");
            map.addAttribute("userProfileFooter", "");
        }

        map.addAttribute("enableUserLocale", enableUserLocale);
        map.addAttribute("localeStringList", localeStringList);

        return "console/profile";
    }

    @RequestMapping(value = "/console/profile/submit", method = RequestMethod.POST)
    public String profileSubmit(ModelMap model, HttpServletRequest request, HttpServletResponse response, @ModelAttribute("user") User user, BindingResult result) throws IOException {
        User currentUser = userDao.getUser(workflowUserManager.getCurrentUsername());
        
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        Collection<String> errors = new ArrayList<String>();
        Collection<String> passwordErrors = new ArrayList<String>();
        
        boolean authenticated = false;
        
        if (!currentUser.getUsername().equals(user.getUsername())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        } else {
            try {
                if (directoryManager.authenticate(currentUser.getUsername(), user.getOldPassword())) {
                    authenticated = true;
                }
            } catch (Exception e) {
            }
        }
        
        
        UserSecurity us = DirectoryUtil.getUserSecurity();

        if (!authenticated) {
            if (errors == null) {
                errors = new ArrayList<String>();
            }
            errors.add(ResourceBundleUtil.getMessage("console.directory.user.error.label.authenticationFailed"));
        } else {
            if (us != null) {
                errors = us.validateUserOnProfileUpdate(user);
            }

            if (user.getPassword() != null && !user.getPassword().isEmpty() && us != null) {
                passwordErrors = us.validatePassword(user.getUsername(), user.getOldPassword(), user.getPassword(), user.getConfirmPassword());   
            }
        }

        if (!authenticated || (passwordErrors != null && !passwordErrors.isEmpty()) || (errors != null && !errors.isEmpty())) {
            model.addAttribute("passwordErrors", passwordErrors);
            model.addAttribute("errors", errors);
            model.addAttribute("user", user);
            model.addAttribute("timezones", TimeZoneUtil.getList());

            String enableUserLocale = setupManager.getSettingValue("enableUserLocale");
            Map<String, String> localeStringList = new TreeMap<String, String>();
            if (enableUserLocale != null && enableUserLocale.equalsIgnoreCase("true")) {
                String userLocale = setupManager.getSettingValue("userLocale");
                Collection<String> locales = new HashSet();
                locales.addAll(Arrays.asList(userLocale.split(",")));

                Locale[] localeList = Locale.getAvailableLocales();
                for (int x = 0; x < localeList.length; x++) {
                    String code = localeList[x].toString();
                    if (locales.contains(code)) {
                        localeStringList.put(code, code + " - " + localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
                    }
                }
            }
            model.addAttribute("enableUserLocale", enableUserLocale);
            model.addAttribute("localeStringList", localeStringList);
            
            if (us != null) {
                model.addAttribute("policies", us.passwordPolicies());
                model.addAttribute("userProfileFooter", us.getUserProfileFooter(currentUser));
            } else {
                model.addAttribute("policies", "");
                model.addAttribute("userProfileFooter", "");
            }

            return "console/profile";
        } else {
            if (currentUser.getUsername().equals(user.getUsername())) {
                currentUser.setFirstName(user.getFirstName());
                currentUser.setLastName(user.getLastName());
                currentUser.setEmail(user.getEmail());
                currentUser.setTimeZone(user.getTimeZone());
                currentUser.setLocale(user.getLocale());
                if (user.getPassword() != null && user.getConfirmPassword() != null && user.getPassword().length() > 0 && user.getPassword().equals(user.getConfirmPassword())) {
                    if (us != null) {
                        currentUser.setPassword(us.encryptPassword(user.getUsername(), user.getPassword()));
                    } else {
                        currentUser.setPassword(StringUtil.md5Base16(user.getPassword()));
                    }
                    currentUser.setConfirmPassword(user.getPassword());
                }
                userDao.updateUser(currentUser);

                if (us != null) {
                    us.updateUserProfilePostProcessing(currentUser);
                }
            }
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
    public String consoleAppCreate(ModelMap model, @RequestParam(value = "templateAppId", required = false) String templateAppId) {
        model.addAttribute("appDefinition", new AppDefinition());
        
        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        model.addAttribute("appList", appDefinitionList);
        
        Map<String, String> templateAppList = MarketplaceUtil.getTemplateOptions();
        model.addAttribute("templateAppList", templateAppList);
        
        model.addAttribute("pluginOptions", AppUtil.getCreateAppOptions());
        
        if (templateAppId != null && !templateAppId.isEmpty()) {
            model.addAttribute("type", "template");
            model.addAttribute("templateAppId", StringUtil.stripAllHtmlTag(templateAppId));
        } else {
            model.addAttribute("type", "");
        }
        
        return "console/apps/appCreate";
    }

    @RequestMapping(value = "/console/app/submit", method = RequestMethod.POST)
    public String consoleAppSubmit(ModelMap model, HttpServletRequest request, @ModelAttribute("appDefinition") AppDefinition appDefinition, BindingResult result, 
            @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "templateAppId", required = false) String templateAppId, 
            @RequestParam(value = "tablePrefix", required = false) String tablePrefix, 
            @RequestParam(value = "type", required = false) String type, 
            @RequestParam(value = "pluginProperties", required = false) String properties) {
        // validate ID
        validator.validate(appDefinition, result);
        
        Map<String, Plugin> pluginOptions = AppUtil.getCreateAppOptions();

        boolean invalid = result.hasErrors();
        if (!invalid) {
            Collection<String> errors = new ArrayList<String>();
            
            if (pluginOptions.containsKey(type)) {
                // check for duplicate, so that the plugin no need to check this again
                AppDefinition appDef = appDefinitionDao.loadById(appDefinition.getAppId());
                if (appDef != null) {
                    errors.add("console.app.error.label.idExists");
                    invalid = true;
                } else {    
                    errors = AppUtil.executeCreateAppOptionPlugin((CreateAppOption) pluginManager.getPlugin(type), properties, appDefinition.getAppId(), appDefinition.getName(), request);
                    if (errors != null && !errors.isEmpty()) {
                        model.addAttribute("pluginErrors", errors);
                        invalid = true;
                    }
                }
            } else if (templateAppId != null && !templateAppId.isEmpty()) {
                errors = appService.createAppDefinitionFromTemplate(appDefinition, templateAppId, tablePrefix);
                if (!errors.isEmpty()) {
                    model.addAttribute("errors", errors);
                    invalid = true;
                }
            } else {
                // create app
                AppDefinition copy = null;
                if (copyAppId != null && !copyAppId.isEmpty()) {
                    Long copyVersion = appService.getPublishedVersion(copyAppId);
                    copy = appService.getAppDefinition(copyAppId, (copyVersion != null)?copyVersion.toString():null);
                }

                errors = appService.createAppDefinition(appDefinition, copy, tablePrefix);
            }
            if (errors != null && !errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            model.addAttribute("appList", appDefinitionList);
            
            Map<String, String> templateAppList = MarketplaceUtil.getTemplateOptions();
            model.addAttribute("templateAppList", templateAppList);
            
            if ((templateAppId != null && !templateAppId.isEmpty()) || (copyAppId != null && !copyAppId.isEmpty())) {
                try {
                    JSONObject o = new JSONObject();
                    //retrieve config from parameters
                    for (String key : request.getParameterMap().keySet()) {
                        if (key.startsWith("rp_")) {
                            o.put(StringUtil.stripAllHtmlTag(key), StringUtil.stripAllHtmlTag(request.getParameter(key)));
                        }
                    }
                    model.addAttribute("templateConfig", o.toString());
                } catch (Exception e) {
                    //ignore
                }
            }
            
            if (pluginOptions.containsKey(type)) {
                model.addAttribute("type", StringUtil.stripAllHtmlTag(type));
                
                //verify the properties is a proper json format
                try {
                    JSONObject obj = new JSONObject(properties);
                    model.addAttribute("properties", properties);
                } catch (Exception e) {
                    //ignore it
                }
            } else if (templateAppId != null && !templateAppId.isEmpty()) {
                model.addAttribute("type", "template");
                model.addAttribute("templateAppId", StringUtil.stripAllHtmlTag(templateAppId));
                model.addAttribute("tablePrefix", StringUtil.stripAllHtmlTag(tablePrefix));
            } else if (copyAppId != null && !copyAppId.isEmpty()) {
                model.addAttribute("type", "duplicate");
                model.addAttribute("copyAppId", StringUtil.stripAllHtmlTag(copyAppId));
                model.addAttribute("tablePrefix", StringUtil.stripAllHtmlTag(tablePrefix));
            } else {
                model.addAttribute("type", "");
            }
            model.addAttribute("pluginOptions", pluginOptions);
            
            return "console/apps/appCreate";
        } else {
            String appId = StringEscapeUtils.escapeHtml(appDefinition.getId());
            model.addAttribute("appId", appId);
            model.addAttribute("appVersion", appDefinition.getVersion());
            return "console/apps/packageUploadSuccess";
        }
    }

    @RequestMapping({"/json/console/app/list", "/json/console/monitor/app/list"})
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

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/versioning")
    public String consoleAppVersioning(ModelMap map, @RequestParam(value = "appId") String appId) throws JSONException {
        appId = SecurityUtil.validateStringInput(appId);
        AppDefinition appDef = appService.getAppDefinition(appId, null);
        if (appDef == null) {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/desktop/apps";
            
            //make it redirect to app version 0 and show the overlay with all apps.
            String script = "if (parent && parent.PopupDialog !== undefined){parent.PopupDialog.closeDialog();}\n";
            script += "if (parent && parent.AdminBar !== undefined){parent.AdminBar.showQuickOverlay('"+StringUtil.escapeString(url, StringUtil.TYPE_JAVASCIPT)+"');}\n";
            script += "if (parent && parent.CustomBuilder !== undefined){parent.CustomBuilder.renderAppNotExist('"+StringUtil.escapeString(appId, StringUtil.TYPE_JAVASCIPT)+"');}\n";
            
            map.addAttribute("script", script);
            
            return "console/dialogClose";
        }
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        return "console/apps/appVersion";
    }

    @RequestMapping("/json/console/app/(*:appId)/version/list")
    public void consoleAppVersionListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        Collection<AppDefinition> appDefList = appDefinitionDao.findVersions(appId, sort, desc, null, null);

        TreeMap<Long, AppDefinition> appDefMap = new TreeMap<>();
        if (!appDefList.isEmpty()) {
            for (AppDefinition appDef: appDefList) {
                appDefMap.put(appDef.getVersion(), appDef);
            }            
            
            if (!AppDevUtil.isGitDisabled()) {
                // get app versions from Git
                try {
                    AppDefinition appDef = appDefList.iterator().next();
                    List<String> branches = AppDevUtil.getAppGitBranches(appDef);
                    for (String branch: branches) {
                        StringTokenizer st = new StringTokenizer(branch, "_");
                        String version = (st.countTokens() == 2) ? branch.substring(branch.indexOf("_")+1) : null;
                        if (version != null && !appDefMap.containsKey(Long.valueOf(version))) {
                            AppDefinition tempAppDef = AppDevUtil.createDummyAppDefinition(appId, Long.valueOf(version));
                            tempAppDef.setDescription("Git: " + branch);
                            appDefMap.put(tempAppDef.getVersion(), tempAppDef);
                        }
                    }            
                } catch(Exception e) {
                    LogUtil.error(getClass().getName(), e, e.getMessage());
                }
            }
        }
        // reverse sort versions
        List<AppDefinition> newAppDefList = new ArrayList<>(appDefMap.values());
        Collections.sort(newAppDefList, new Comparator<AppDefinition>() {
            @Override
            public int compare(AppDefinition a1, AppDefinition a2) {
                return a2.getVersion().compareTo(a1.getVersion());
            }
        });
        // handle paging
        int count = newAppDefList.size();
        if (start != null && start >= 0 && rows != null && rows > 0) {
            int end = start + rows;
            if (end > count) {
                end = count;
            }
            newAppDefList = newAppDefList.subList(start, end);
        }        
        
        // generate JSON output
        JSONObject jsonObject = new JSONObject();
        if (newAppDefList != null && newAppDefList.size() > 0) {
            for (AppDefinition appDef : newAppDefList) {
                Map data = new HashMap();
                data.put("version", appDef.getVersion().toString());
                data.put("published", (appDef.isPublished()) ? "<span class=\"tick\"></span>" : "");
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(appDef.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(appDef.getDateModified(), null, AppUtil.getAppDateFormat()));
                data.put("description", StringUtil.escapeString(appDef.getDescription(), StringUtil.TYPE_NL2BR));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/version/new", method = RequestMethod.POST)
    @Transactional
    public String consoleAppCreate(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) Long version) {
        AppDefinition appDef = appService.createNewAppDefinitionVersion(appId, version);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/publish", method = RequestMethod.POST)
    @Transactional
    public String consoleAppPublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        appService.publishApp(appId, version);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/rename/(*:name)", method = RequestMethod.POST)
    @Transactional
    public String consoleAppRename(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "name") String name) {
        //Rename app
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setName(name);
            appDefinitionDao.merge(appDef);
        }

        return "console/apps/dialogClose";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/note")
    public String consoleAppNote(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        appId = SecurityUtil.validateStringInput(appId);
        version = SecurityUtil.validateStringInput(version);
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("saved", "");
        
        return "console/apps/note";
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/note/submit", method = RequestMethod.POST)
    @Transactional
    public String consoleAppNoteSubmit(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "description") String description) {
        appId = SecurityUtil.validateStringInput(appId);
        version = SecurityUtil.validateStringInput(version);

        //Rename app
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            appDef.setDescription(description);
            appDefinitionDao.merge(appDef);
        }
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("saved", "true");

        return "console/apps/note";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/unpublish", method = RequestMethod.POST)
    @Transactional
    public String consoleAppUnpublish(@RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        appService.unpublishApp(appId);
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
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/tagging")
    public void consoleAppTaggingJson(HttpServletRequest request, Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String patch) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        String json = "";
        if ("post".equalsIgnoreCase(request.getMethod())) {
            json = TaggingUtil.updateDefinitionWithPatch(appDef, patch);
        } else {
            json = TaggingUtil.getDefinition(appDef);
        }
        
        writer.write(json);
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/exportconfig")
    public String consoleAppExportConfig(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        Collection<String> tableNameList = formDefinitionDao.getTableNameList(appDef);
        map.addAttribute("tableNameList", tableNameList);
        
        Collection<Group> userGroups = appService.getAppUserGroups(appDef);
        map.addAttribute("userGroups", userGroups);
        
        return "console/apps/exportConfig";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/export")
    public void consoleAppExport(HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        ServletOutputStream output = null;
        try {
            // verify app
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // determine output filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = sdf.format(new Date());
            String filename = APP_ZIP_PREFIX + appDef.getId() + "-" + appDef.getVersion() + "-" + timestamp + ".jwa";

            // set response headers
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition", "inline; filename=" + filename);
            output = response.getOutputStream();

            // export app
            appService.exportApp(appDef.getId(), appDef.getVersion().toString(), output);

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
        Collection<String> errors = new ArrayList<String>();
        
        MultipartFile appZip = null;
        
        try {
            appZip = FileStore.getFile("appZip");
        } catch (FileLimitException e) {
            errors.add(ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }
        
        AppDefinition appDef = null;
        try {
            if (appZip != null) {
                byte[] bytes = appZip.getBytes();
                if (appService.isGitSrcZip(bytes)) {
                    appDef = appService.importAppDefFromGitSrc(bytes);
                } else {
                    appDef = appService.importApp(bytes);
                }
            }
        } catch (ImportAppException e) {
            errors.add(e.getMessage());
        }

        if (appDef == null || !errors.isEmpty()) {
            map.addAttribute("error", true);
            map.addAttribute("errorList", errors);
            return "console/apps/import";
        } else {
            String appId = appDef.getAppId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/builders";
            map.addAttribute("url", url);
            map.addAttribute("appId", appId);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("isPublished", appDef.isPublished());
            return "console/apps/packageUploadSuccess";
        }
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/package/xpdl", "/json/console/app/(*:appId)/(~:version)/package/xpdl"})
    public void getPackageXpdl(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        response.setContentType("application/xml; charset=utf-8");
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
                    xpdl = xpdl.replace("${packageId}", StringUtil.escapeString(appDef.getId(), StringUtil.TYPE_XML, null));
                    xpdl = xpdl.replace("${packageName}", StringUtil.escapeString(appDef.getName(), StringUtil.TYPE_XML, null));
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
        String error = null;
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        MultipartFile packageXpdl = null;
        
        try {
            packageXpdl = FileStore.getFile("packageXpdl");
        } catch (FileLimitException e) {
            error = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }
        JSONObject jsonObject = new JSONObject();

        // TODO: authenticate user
        boolean authenticated = !workflowUserManager.isCurrentUserAnonymous();

        if (authenticated) {
            if (error == null && packageXpdl != null) {
                try {
                    // deploy package
                    appService.deployWorkflowPackage(appId, version, packageXpdl.getBytes(), true);

                    jsonObject.accumulate("status", "complete");
                } catch (Exception e) {
                    jsonObject.accumulate("errorMsg", e.getMessage().replace(":", ""));
                }
            } else {
                jsonObject.accumulate("errorMsg", error);
            }
        } else {
            jsonObject.accumulate("errorMsg", "unauthenticated");
        }
        AppUtil.writeJson(writer, jsonObject, null);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload")
    public String consolePackageUpload(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        return "console/apps/packageUpload";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/package/upload/submit", method = RequestMethod.POST)
    public String consolePackageUploadSubmit(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        MultipartFile packageXpdl = null;
        String error = null;
        String xpdlJson = "";
        
        try {
            packageXpdl = FileStore.getFile("packageXpdl");
        } catch (FileLimitException e) {
            error = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }
        
        if (packageXpdl == null || packageXpdl.isEmpty()) {
            error = "Package XPDL is empty";
        } else {
            try {
                xpdlJson = U.xmlToJson(new String(packageXpdl.getBytes(), "UTF-8"));
            } catch (Exception e) {
                error = e.getMessage();
            }
        }
        
        //close popup dialog & update the process design in process builder
        if ((xpdlJson != null && !xpdlJson.isEmpty()) && (error == null || error.isEmpty())) {
            map.addAttribute("script", "parent.JPopup.hide(\"uploadXpdlDialog\");parent.ProcessBuilder.updateJsonFromUploadedXpdl(\""+StringUtil.escapeString(xpdlJson, StringUtil.TYPE_JAVASCIPT, null)+"\");");
        } else {
            map.addAttribute("script", "parent.JPopup.hide(\"uploadXpdlDialog\");parent.alert(\""+StringUtil.escapeString(error, StringUtil.TYPE_JAVASCIPT, null)+"\");");
        }
        
        return "console/apps/dialogClose";
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/processes", "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)"})
    public String consoleProcessView(ModelMap map, @RequestParam("appId") String appId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "version", required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        //for launching workflow designer
        User user = workflowUserManager.getCurrentUser();
        map.addAttribute("username", (user != null)?user.getUsername():WorkflowUserManager.ROLE_ANONYMOUS);

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
        checkAppPublishedVersion(appDef);
        map.clear();
        if (!processFound) {
            return "redirect:/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/process/builder";
        } 
        return "redirect:/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/process/builder#" + process.getIdWithoutVersion();
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
        map.addAttribute("appId", appDef.getId());
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

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/submit", method = RequestMethod.POST)
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
        activityDefId = SecurityUtil.validateStringInput(activityDefId);
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

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/form/remove", method = RequestMethod.POST)
    public String consoleActivityFormRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // check for existing auto continue flag
        boolean autoContinue = false;
        if (packageDef != null) {
            autoContinue = appService.isActivityAutoContinue(packageDef.getId(), packageDef.getVersion().toString(), processDefId, activityDefId);
        }

        // remove mapping
        processDefId = SecurityUtil.validateStringInput(processDefId);
        activityDefId = SecurityUtil.validateStringInput(activityDefId);
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

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/continue", method = RequestMethod.POST)
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
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/draft", method = RequestMethod.POST)
    public void consoleActivitySaveAsDraftSubmit(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId, @RequestParam String disable) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();

        // set and save
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
        if (paf == null) {
            paf = new PackageActivityForm();
            paf.setProcessDefId(processDefId);
            paf.setActivityDefId(activityDefId);
        }
        boolean disableSaveAsDraft = Boolean.parseBoolean(disable);
        paf.setDisableSaveAsDraft(disableSaveAsDraft);
        packageDefinitionDao.addAppActivityForm(appId, appDef.getVersion(), paf);

        // write output
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("disable", disableSaveAsDraft);
        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin")
    public String consoleActivityPlugin(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/route/(*:activityDefId)/plugin")
    public String consoleRoutePlugin(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
        map.addAttribute("process", process);
        map.addAttribute("activity", activity);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/routePluginAdd";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activityForm/(*:activityDefId)/plugin")
    public String consoleActivityFormPlugin(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        WorkflowProcess process = workflowManager.getProcess(processDefId);
        WorkflowActivity activity = workflowManager.getProcessActivityDefinition(processDefId, activityDefId);
        map.addAttribute("process", process);
        map.addAttribute("activity", activity);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        
        if (WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS.equals(activityDefId)) {
            map.addAttribute("pluginClass", "org.joget.apps.app.model.StartProcessFormModifier");
        } else {
            map.addAttribute("pluginClass", "org.joget.apps.app.model.ProcessFormModifier");
        }
        
        return "console/apps/activityFormPluginAdd";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/(*:activityType)/(*:activityDefId)/plugin/submit", method = RequestMethod.POST)
    public String consoleActivityPluginSubmit(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityType, @RequestParam String activityDefId, @RequestParam("id") String pluginName) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        PackageActivityPlugin activityPlugin = new PackageActivityPlugin();
        activityPlugin.setProcessDefId(processDefId);
        activityPlugin.setActivityDefId(activityDefId);
        activityPlugin.setPluginName(pluginName);

        packageDefinitionDao.addAppActivityPlugin(appId, appDef.getVersion(), activityPlugin);
        
        map.addAttribute("activityType", activityType);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        map.addAttribute("pluginName", URLEncoder.encode(pluginName, "UTF-8"));
        return "console/apps/activityPluginAddSuccess";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/activity/(*:activityDefId)/plugin/remove", method = RequestMethod.POST)
    public String consoleActivityPluginRemove(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityDefId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        packageDefinitionDao.removeAppActivityPlugin(appId, appDef.getVersion(), processDefId, activityDefId);
        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefId, "UTF-8"));
        return "console/apps/activityFormRemoveSuccess";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/(*:activityType)/(*:activityDefId)/plugin/configure")
    public String consoleActivityPluginConfigure(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String activityType, @RequestParam String activityDefId, @RequestParam("param_tab") String tab, @RequestParam(value = "pluginname", required = false) String pluginName) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        tab = SecurityUtil.validateStringInput(tab);
        String processDefIdWithVersion = processDefId;

        if (packageDef != null) {
            activityDefId = SecurityUtil.validateStringInput(activityDefId);
            processDefIdWithVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
            PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefIdWithVersion, activityDefId);
            
            if (activityPlugin == null || (pluginName != null && !pluginName.isEmpty())) {
                activityPlugin = new PackageActivityPlugin();
                activityPlugin.setProcessDefId(processDefIdWithVersion);
                activityPlugin.setActivityDefId(activityDefId);
                activityPlugin.setPluginName(pluginName);
            }
            
            Plugin plugin = pluginManager.getPlugin(activityPlugin.getPluginName());
          
            if (activityPlugin.getPluginProperties() != null && activityPlugin.getPluginProperties().trim().length() > 0) {
                if (!(plugin instanceof PropertyEditable)) {
                    Map propertyMap = new HashMap();
                    propertyMap = CsvUtil.getPluginPropertyMap(activityPlugin.getPluginProperties());
                    map.addAttribute("propertyMap", propertyMap);
                } else {
                    map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(activityPlugin.getPluginProperties()));
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
                        map.addAttribute("defaultProperties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
                    }
                }
            }

            if (plugin instanceof PropertyEditable) {
                PropertyEditable pe = (PropertyEditable) plugin;
                map.addAttribute("propertyEditable", pe);
                map.addAttribute("propertiesDefinition", PropertyUtil.injectHelpLink(plugin.getHelpLink(), pe.getPropertyOptions()));
            }

            map.addAttribute("appDef", appDef);
            map.addAttribute("plugin", plugin);
            
            try {
                processDefId =  URLEncoder.encode(processDefId, "UTF-8");
            } catch (UnsupportedEncodingException e) {}

            String url = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + StringEscapeUtils.escapeHtml(processDefId) + "/" + StringEscapeUtils.escapeHtml(activityType) + "/" + StringEscapeUtils.escapeHtml(activityDefId) + "/plugin/configure/submit?param_activityPluginId=" + activityPlugin.getUid()+"&param_tab="+tab;
            if (pluginName != null) {
                url += "&pluginname="+URLEncoder.encode(pluginName, "UTF-8");
            }
            map.addAttribute("actionUrl", url);
            
            boolean showCancel = true;
            
            if ("activityForm".equalsIgnoreCase(activityType)) {
                if(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS.equals(activityDefId)) {
                    Map<String, Plugin> modifierPluginMap = pluginManager.loadPluginMap(StartProcessFormModifier.class);
                    if (modifierPluginMap.size() == 1) {
                        showCancel = false;
                    }
                } else {
                    Map<String, Plugin> modifierPluginMap = pluginManager.loadPluginMap(ProcessFormModifier.class);
                    if (modifierPluginMap.size() == 1) {
                        showCancel = false;
                    }
                }
            }
            
            if (showCancel) {
                String cancelUrl = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + StringEscapeUtils.escapeHtml(processDefId) + "/" + StringEscapeUtils.escapeHtml(activityType) + "/" + StringEscapeUtils.escapeHtml(activityDefId) + "/plugin?title="+URLEncoder.encode(request.getParameter("title"), "UTF-8");
                map.addAttribute("cancelUrl", cancelUrl);

                map.addAttribute("cancelLabel", "console.process.config.label.mapTools.changePlugin");
            }
        }

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/processes/(*:param_processDefId)/(*:activityType)/(*:param_activityDefId)/plugin/configure/submit", method = RequestMethod.POST)
    @Transactional
    public String consoleActivityPluginConfigureSubmit(ModelMap map, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_processDefId") String processDefId, @RequestParam String activityType, @RequestParam("param_activityDefId") String activityDefId, @RequestParam("param_tab") String tab, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request, @RequestParam(value = "pluginname", required = false) String pluginName) throws IOException {
        AppDefinition appDef = appService.loadAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        processDefId = SecurityUtil.validateStringInput(processDefId);
        String processDefIdWithVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        activityDefId = SecurityUtil.validateStringInput(activityDefId);
        tab = SecurityUtil.validateStringInput(tab);
        PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(processDefIdWithVersion, activityDefId);
        
        if (activityPlugin == null || (pluginName != null && !pluginName.isEmpty())) {
            activityPlugin = new PackageActivityPlugin();
            activityPlugin.setProcessDefId(processDefIdWithVersion);
            activityPlugin.setActivityDefId(activityDefId);
            activityPlugin.setPluginName(pluginName);
                
            packageDefinitionDao.addAppActivityPlugin(appId, appDef.getVersion(), activityPlugin);
        }
        
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
                activityPlugin.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(activityPlugin.getPluginProperties(), pluginProperties));
            }
            packageDef.addPackageActivityPlugin(activityPlugin);
        }

        // update and save
        packageDefinitionDao.saveOrUpdate(packageDef);

        map.addAttribute("activityDefId", activityDefId);
        map.addAttribute("processDefId", URLEncoder.encode(processDefIdWithVersion, "UTF-8"));
        
        map.addAttribute("tab", tab);

        return "console/apps/activityPluginConfigSuccess";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)")
    public String consoleParticipant(ModelMap map, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam String participantId) throws UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
                PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
                String oldJson = "";
                if (participantExisting != null && PackageParticipant.TYPE_PLUGIN.equals(participantExisting.getType())) {
                    oldJson = participantExisting.getPluginProperties();
                }
                
                participant.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(oldJson, pluginProperties));
            }
        } else if ((PackageParticipant.TYPE_GROUP.equals(type) || PackageParticipant.TYPE_USER.equals(type)) && packageDef != null) {
            //Using Set to prevent duplicate value
            Set values = new HashSet();
            StringTokenizer valueToken = new StringTokenizer(value, ",");
            while (valueToken.hasMoreTokens()) {
                values.add((String) valueToken.nextElement());
            }
            
            PackageParticipant participantExisting = packageDef.getPackageParticipant(processDefId, participantId);
            if (participantExisting != null && participantExisting.getValue() != null) {
                
                StringTokenizer existingValueToken = (type.equals(participantExisting.getType())) ? new StringTokenizer(participantExisting.getValue().replaceAll(";", ","), ",") : null;
                while (existingValueToken != null && existingValueToken.hasMoreTokens()) {
                    values.add((String) existingValueToken.nextElement());
                }
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
        participant.setType(type);
        participant.setValue(value);

        packageDefinitionDao.addAppParticipant(appDef.getId(), appDef.getVersion(), participant);

        map.addAttribute("appId", appDef.getId());
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
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/processes/(*:processDefId)/participant/(*:participantId)/pconfigure")
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
        String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        participantId = SecurityUtil.validateStringInput(participantId);

        if (value != null && value.trim().length() > 0) {
            plugin = pluginManager.getPlugin(value);
        } else {
            if (packageDef != null) {
                PackageParticipant participant = packageDef.getPackageParticipant(processDefIdWithoutVersion, participantId);
                plugin = pluginManager.getPlugin(participant.getValue());

                if (participant.getPluginProperties() != null && participant.getPluginProperties().trim().length() > 0) {
                    if (!(plugin instanceof PropertyEditable)) {
                        Map propertyMap = new HashMap();
                        propertyMap = CsvUtil.getPluginPropertyMap(participant.getPluginProperties());
                        map.addAttribute("propertyMap", propertyMap);
                    } else {
                        map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(participant.getPluginProperties()));
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
                    map.addAttribute("defaultProperties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
                }
            }
            if (plugin instanceof PropertyEditable) {
                PropertyEditable pe = (PropertyEditable) plugin;
                map.addAttribute("propertyEditable", pe);
                map.addAttribute("propertiesDefinition", PropertyUtil.injectHelpLink(plugin.getHelpLink(), pe.getPropertyOptions()));
            }

            String url = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + StringEscapeUtils.escapeHtml(processDefIdWithoutVersion) + "/participant/" + StringEscapeUtils.escapeHtml(participantId) + "/submit/plugin?param_value=" + ClassUtils.getUserClass(plugin).getName();

            map.addAttribute("appDef", appDef);
            map.addAttribute("plugin", plugin);
            map.addAttribute("actionUrl", url);
            
            try {
                processDefId =  URLEncoder.encode(processDefId, "UTF-8");
            } catch (UnsupportedEncodingException e) {}
            
            String cancelUrl = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/processes/" + StringEscapeUtils.escapeHtml(processDefId) + "/participant/" + StringEscapeUtils.escapeHtml(participantId) + "?tab=plugin&title="+URLEncoder.encode(request.getParameter("title"), "UTF-8");
            map.addAttribute("cancelUrl", cancelUrl);
            map.addAttribute("cancelLabel", "console.process.config.label.mapTools.changePlugin");
        }

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
            if (participantExisting != null && participantExisting.getValue() != null) {
                //Using Set to prevent duplicate value
                Set values = new HashSet();
                StringTokenizer existingValueToken = new StringTokenizer(participantExisting.getValue().replaceAll(";", ","), ",");
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
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/datalistList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalist/create")
    public String consoleDatalistCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        DatalistDefinition datalistDefinition = new DatalistDefinition();
        map.addAttribute("datalistDefinition", datalistDefinition);
        
        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        map.addAttribute("appList", appDefinitionList);
        
        return "console/apps/datalistCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/submit/(*:action)", method = RequestMethod.POST)
    public String consoleDatalistSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("datalistDefinition") DatalistDefinition datalistDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyListId", required = false) String copyListId) {
        DatalistDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyListId != null && !copyListId.isEmpty()) {
            Long copyVersion = appService.getPublishedVersion(copyAppId);
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, (copyVersion != null)?copyVersion.toString():null);
            copy = datalistDefinitionDao.loadById(copyListId, copyAppDef);
        }
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
                String json = JsonUtil.generateDefaultList(datalistDefinition.getId(), datalistDefinition, copy);
                datalistDefinition.setJson(json);
                invalid = !datalistDefinitionDao.add(datalistDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("datalistDefinition", datalistDefinition);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            map.addAttribute("appList", appDefinitionList);
        
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
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(datalistDefinition.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(datalistDefinition.getDateModified(), null, AppUtil.getAppDateFormat()));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
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
    public void consoleDatalistOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows, @RequestParam(value = "customAppId", required = false) String customAppId) throws IOException, JSONException {

        Collection<DatalistDefinition> datalistDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }
        AppDefinition appDef = null;
        if (customAppId != null && !customAppId.isEmpty()) {
            appDef = appService.getPublishedAppDefinition(customAppId);
        } else {
            if (version == null || version.isEmpty()) {
                Long appVersion = appService.getPublishedVersion(appId);
                if (appVersion != null) {
                    version = appVersion.toString();
                }
            }
            appDef = appService.getAppDefinition(appId, version);
        }
        if (appDef != null) {
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, sort, desc, start, rows);
        } else {
            datalistDefinitionList = new ArrayList<DatalistDefinition>();
        }

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
        AppUtil.writeJson(writer, jsonArray, callback);
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userviews")
    public String consoleUserviewList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/userviewList";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/userview/create")
    public String consoleUserviewCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        UserviewDefinition userviewDefinition = new UserviewDefinition();
        map.addAttribute("userviewDefinition", userviewDefinition);
        
        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        map.addAttribute("appList", appDefinitionList);
        
        return "console/apps/userviewCreate";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/userview/submit/(*:action)", method = RequestMethod.POST)
    public String consoleUserviewSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("userviewDefinition") UserviewDefinition userviewDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyUserviewId", required = false) String copyUserviewId) {
        UserviewDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyUserviewId != null && !copyUserviewId.isEmpty()) {
            Long copyVersion = appService.getPublishedVersion(copyAppId);
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, (copyVersion != null)?copyVersion.toString():null);
            copy = userviewDefinitionDao.loadById(copyUserviewId, copyAppDef);
            if (copy != null) {
                copy = userviewService.combinedUserviewDefinition(copy, true);
            }
        }
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
                String json = GeneratorUtil.createNewUserviewJson(userviewDefinition.getId(), userviewDefinition.getName(), userviewDefinition.getDescription(), copy);
                json = userviewService.saveUserviewPages(json, userviewDefinition.getId(), appDef);
                userviewDefinition.setJson(json);
                invalid = !userviewDefinitionDao.add(userviewDefinition);
            }

            if (!errors.isEmpty()) {
                map.addAttribute("errors", errors);
                invalid = true;
            }
        }

        map.addAttribute("userviewDefinition", userviewDefinition);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            map.addAttribute("appList", appDefinitionList);
        
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
                data.put("dateCreated", TimeZoneUtil.convertToTimeZone(userviewDefinition.getDateCreated(), null, AppUtil.getAppDateFormat()));
                data.put("dateModified", TimeZoneUtil.convertToTimeZone(userviewDefinition.getDateModified(), null, AppUtil.getAppDateFormat()));
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
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
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/options")
    public void consoleUserviewOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows, @RequestParam(value = "customAppId", required = false) String customAppId) throws IOException, JSONException {

        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }
        AppDefinition appDef = null;
        if (customAppId != null && !customAppId.isEmpty()) {
            appDef = appService.getPublishedAppDefinition(customAppId);
        } else {
            if (version == null || version.isEmpty()) {
                Long appVersion = appService.getPublishedVersion(appId);
                if (appVersion != null) {
                    version = appVersion.toString();
                }
            }
            appDef = appService.getAppDefinition(appId, version);
        }
        if (appDef != null) {
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, sort, desc, start, rows);
        } else {
            userviewDefinitionList = new ArrayList<UserviewDefinition>();
        }

        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (UserviewDefinition userviewDef : userviewDefinitionList) {
            Map data = new HashMap();
            data.put("value", userviewDef.getId());
            data.put("label", userviewDef.getName());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/properties")
    public String consoleProperties(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        boolean protectedReadonly = false;
        if (result != null) {
            protectedReadonly = result.contains("status=invalidLicensor");
            if (!protectedReadonly) {
                return result;
            }
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("protectedReadonly", protectedReadonly);
        
        return "console/apps/properties";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable")
    public String consoleEnvVariable(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        boolean protectedReadonly = false;
        if (result != null) {
            protectedReadonly = result.contains("status=invalidLicensor");
            if (!protectedReadonly) {
                return result;
            }
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("protectedReadonly", protectedReadonly);
        
        return "console/apps/envVariable";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/resources")
    public String consoleResources(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        boolean protectedReadonly = false;
        if (result != null) {
            protectedReadonly = result.contains("status=invalidLicensor");
            if (!protectedReadonly) {
                return result;
            }
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("protectedReadonly", protectedReadonly);
        
        return "console/apps/resources";
    }
    
    @RequestMapping(value = "/json/console/app/(*:appId)/(~:version)/message/submit", method = RequestMethod.POST)
    public void consoleAppMessageJsonSubmit(HttpServletResponse response, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam String data, @RequestParam String locale) throws IOException {
        try {
            AppDefinition appDef = appService.getAppDefinition(appId, version);

            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                String key = obj.get("key").toString();
                String id = key + "_" + locale;
                String value = obj.get("value").toString();

                // check exist
                Message m = messageDao.loadById(id, appDef);
                if (m != null) {
                    if (value == null || (value != null && value.isEmpty())) {
                        messageDao.delete(id, appDef);
                    } else {
                        m.setMessage(value);
                        messageDao.update(m);
                    }
                } else if (value != null && !value.isEmpty()) {
                    m = new Message();
                    m.setAppDefinition(appDef);
                    m.setId(id);
                    m.setLocale(locale);
                    m.setMessageKey(key);
                    m.setMessage(value);
                    messageDao.add(m);
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
        }
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/message/keys")
    public void consoleMessageListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<String> messageKeys = null;
        if (appDef != null) {
            messageKeys = messageDao.getKeyList(appDef);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", messageKeys);

        AppUtil.writeJson(writer, jsonObject, callback);
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
        JSONArray jsonArray = new JSONArray();
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                Map data = new HashMap();
                data.put("id", message.getId());
                data.put("messageKey", message.getMessageKey());
                data.put("locale", message.getLocale());
                data.put("message", message.getMessage());
                jsonArray.put(data);
            }
        }
        jsonObject.put("data", jsonArray);
        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/delete", method = RequestMethod.POST)
    public String consoleAppMessageDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            messageDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/generatepo")
    public String consoleAppMessageGeneratePO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
            // verify app
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // validate locale input
            locale = SecurityUtil.validateStringInput(locale);
            
            // determine output filename
            String filename = appDef.getId() + "_" + appDef.getVersion() + "_" + locale + ".po";

            // set response headers
            response.setContentType("text/plain; charset=utf-8");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            output = response.getOutputStream();

            appService.generatePO(appDef.getId(), appDef.getVersion().toString(), locale, output);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (output != null) {
                output.flush();
            }
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/message/importpo")
    public String consoleAppMessageImportPO(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String containerId, @RequestParam(required = false) String columnId, @RequestParam(required = false) String lang) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("containerId", containerId);
        map.addAttribute("columnId", columnId);
        map.addAttribute("lang", lang);
        
        return "console/apps/messageImportPO";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/message/importpo/submit", method = RequestMethod.POST)
    public String consoleAppMessageInportPOUpload(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String containerId, @RequestParam(required = false) String columnId, @RequestParam(required = false) String lang) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        Setting setting = setupManager.getSettingByProperty("systemLocale");
        String systemLocale = (setting != null) ? setting.getValue() : null;
        if (systemLocale == null || systemLocale.equalsIgnoreCase("")) {
            systemLocale = "en_US";
        }
        
        String errorMsg = null;
        
        MultipartFile multiPartfile = null;
        
        try {
            multiPartfile = FileStore.getFile("localeFile");
        } catch (FileLimitException e) {
            errorMsg = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }

        if (multiPartfile != null) {
            try {
                lang = appService.importPOAndReturnLocale(appId, version, systemLocale, multiPartfile);
            } catch (IOException e) {
                errorMsg = ResourceBundleUtil.getMessage("console.app.message.import.po.error.invalidPoFile");
            }
        }
        
        if (errorMsg != null) {
            map.addAttribute("appId", appDef.getId());
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("errorMessage", errorMsg);
            map.addAttribute("containerId", containerId);
            map.addAttribute("columnId", columnId);
            map.addAttribute("lang", lang);
            
            return "console/apps/messageImportPO";
        }
        if (containerId != null && columnId != null && lang != null) {
            map.addAttribute("script", "parent.JPopup.hide(\"importPoDialog\");parent.I18nEditor.loadLocale(\"#"+containerId+"\", \""+lang+"\", \""+columnId+"\");");
        } else {
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/message";
            map.addAttribute("url", url);
        }
        return "console/apps/dialogClose";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/create")
    public String consoleAppEnvVariableCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("environmentVariable", new EnvironmentVariable());
        return "console/apps/envVariableCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/envVariable/edit/(*:id)")
    public String consoleAppEnvVariableEdit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        EnvironmentVariable environmentVariable = environmentVariableDao.loadById(id, appDef);
        map.addAttribute("environmentVariable", environmentVariable);
        return "console/apps/envVariableEdit";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/submit/(*:action)", method = RequestMethod.POST)
    public String consoleAppEnvVariableSubmit(ModelMap map, @RequestParam("action") String action, @RequestParam String appId, @RequestParam(required = false) String version, @ModelAttribute("environmentVariable") EnvironmentVariable environmentVariable, BindingResult result) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
            String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/envVariable";
            map.addAttribute("url", url);
            return "console/apps/dialogClose";
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

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/envVariable/delete", method = RequestMethod.POST)
    public String consoleAppEnvVariableDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            environmentVariableDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/resource/create")
    public String consoleAppResourceCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        return "console/apps/appResourceCreate";
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/resource/submit", method = RequestMethod.POST)
    public String consoleAppResourceSubmit(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Collection<String> errors = new ArrayList<String>();
        
        MultipartFile file = null;
        
        try {
            file = FileStore.getFile("file");
        } catch (FileLimitException e) {
            errors.add(ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }

        if (file == null || !errors.isEmpty()) {
            map.addAttribute("errors", errors);
            return "console/apps/appResourceCreate";
        } else {
            //store file
            AppResourceUtil.storeFile(appDef, file, false);
            
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/resources";
            map.addAttribute("url", url);
            return "console/apps/dialogClose";
        }
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/resource/permission")
    public String consoleAppResourcePermission(ModelMap map, @RequestParam String id, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(required = false) Boolean upload) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("upload", upload);

        AppResource appResource = appResourceDao.loadById(id, appDef);
        map.addAttribute("appResource", appResource);
        map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(appResource.getPermissionProperties()));
        
        return "console/apps/appResourcePermission";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/resource/permission/submit", method = RequestMethod.POST)
    public String consoleAppResourcePermissionSubmit(ModelMap map, @RequestParam String id, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String permissionProperties) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        AppResource appResource = appResourceDao.loadById(id, appDef);
        map.addAttribute("appResource", appResource);
        appResource.setPermissionProperties(PropertyUtil.propertiesJsonStoreProcessing(appResource.getPermissionProperties(), permissionProperties));
        Map<String, Object> value = PropertyUtil.getPropertiesValueFromJson(appResource.getPermissionProperties());
        if (value.containsKey("permission") && value.get("permission") instanceof Map && ((Map) value.get("permission")).containsKey("className")) {
            Map permission = (Map) value.get("permission");
            appResource.setPermissionClass(permission.get("className").toString());
        }
        
        appResourceDao.update(appResource);
        
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/resources";
        map.addAttribute("url", url);
        return "console/apps/dialogClose";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/resource/list")
    public void consoleAppResourceListJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "filter", required = false) String filterString, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<AppResource> resources = null;
        Long count = null;

        if (appDef != null) {
            resources = appResourceDao.getResources(filterString, appDef, sort, desc, start, rows);
            count = appResourceDao.getResourcesCount(filterString, appDef);
        }

        JSONObject jsonObject = new JSONObject();
        if (resources != null && resources.size() > 0) {
            for (AppResource r : resources) {
                Map data = new HashMap();
                data.put("id", r.getId());
                data.put("filesize", r.getFilesizeString());
                data.put("permissionClass", r.getPermissionClass());
                Plugin p = pluginManager.getPlugin(r.getPermissionClass());
                data.put("permissionClassLabel", (p != null)?p.getI18nLabel():"");
                data.put("permissionProperties", r.getPermissionProperties());
                jsonObject.accumulate("data", data);
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/resource/delete", method = RequestMethod.POST)
    public String consoleAppResourceDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            appResourceDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/create")
    public String consoleAppPluginDefaultCreate(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        map.addAttribute("pluginType", getPluginTypeForDefaultProperty());

        return "console/apps/pluginDefaultCreate";
    }

    @RequestMapping("/console/app/(*:appId)/(~:version)/pluginDefault/config")
    public String consoleAppPluginDefaultConfig(ModelMap map, HttpServletRequest request, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam("id") String id, @RequestParam(required = false) String action) throws UnsupportedEncodingException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        Plugin plugin = pluginManager.getPlugin(id);

        PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(id, appDef);
        
        if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
            if (!(plugin instanceof PropertyEditable)) {
                Map propertyMap = new HashMap();
                propertyMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                map.addAttribute("propertyMap", propertyMap);
            } else {
                map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(pluginDefaultProperties.getPluginProperties()));
            }
        }

        if (plugin instanceof PropertyEditable) {
            PropertyEditable pe = (PropertyEditable) plugin;
            map.addAttribute("propertyEditable", pe);
            map.addAttribute("propertiesDefinition", PropertyUtil.injectHelpLink(plugin.getHelpLink(), pe.getPropertyOptions()));
        }

        String url = request.getContextPath() + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/pluginDefault/submit/";
        if (pluginDefaultProperties == null) {
            url += "create";
        } else {
            url += "edit";
        }
        url += "?param_id=" + ClassUtils.getUserClass(plugin).getName();

        map.addAttribute("appDef", appDef);
        map.addAttribute("plugin", plugin);
        map.addAttribute("skipValidation", false);
        map.addAttribute("actionUrl", url);

        return "console/plugin/pluginConfig";
    }

    @RequestMapping(value = "/console/app/(*:param_appId)/(~:param_version)/pluginDefault/submit/(*:param_action)", method = RequestMethod.POST)
    public String consoleAppPluginDefaultSubmit(ModelMap map, HttpServletRequest request, @RequestParam("param_action") String action, @RequestParam("param_appId") String appId, @RequestParam(value = "param_version", required = false) String version, @RequestParam("param_id") String id, @RequestParam(required = false) String pluginProperties) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appDef.getId());
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
            pluginDefaultProperties.setPluginProperties(PropertyUtil.propertiesJsonStoreProcessing(pluginDefaultProperties.getPluginProperties(), pluginProperties));
        }

        if ("create".equals(action)) {
            pluginDefaultPropertiesDao.add(pluginDefaultProperties);
        } else {
            pluginDefaultPropertiesDao.update(pluginDefaultProperties);
        }
        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/properties";
        map.addAttribute("url", url);
        return "console/apps/dialogClose";
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
                Plugin p = pluginManager.getPlugin(pluginDefaultProperties.getId());
                if (p != null) {
                    data.put("pluginName", p.getI18nLabel());
                    data.put("pluginDescription", p.getI18nDescription());
                    jsonObject.accumulate("data", data);
                } else {
                    count--;
                }
            }
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/pluginDefault/delete", method = RequestMethod.POST)
    public String consoleAppPluginDefaultDelete(@RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            pluginDefaultPropertiesDao.delete(id, appDef);
        }
        return "console/apps/dialogClose";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/forms") 
    public String consoleFormList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }
        
        if (version != null && !version.isEmpty()) {
            version = "/" + version;
        }
        return "redirect:/web/console/app/"+appId+version+"/builders";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/builders")
    public String consoleBuilderList(ModelMap map, @RequestParam String appId, @RequestParam(required = false) String version) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        checkAppPublishedVersion(appDef);
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        
        Properties props = AppDevUtil.getAppDevProperties(appDef);
        String properties = "{}";
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", appDef.getAppId());
            jsonObject.put("name", appDef.getName());
            jsonObject.put(WorkflowUserManager.ROLE_ADMIN, props.getProperty(WorkflowUserManager.ROLE_ADMIN));
            jsonObject.put(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP, props.getProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP));
            jsonObject.put("orgId", props.getProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_ORG));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_URI, props.getProperty(AppDevUtil.PROPERTY_GIT_URI));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_USERNAME, props.getProperty(AppDevUtil.PROPERTY_GIT_USERNAME));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_PASSWORD, props.getProperty(AppDevUtil.PROPERTY_GIT_PASSWORD));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_PULL, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_PULL));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC));
            properties = jsonObject.toString(4);
        } catch (Exception e) {
            LogUtil.error(ConsoleWebController.class.getName(), e, "");
        }
        map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(properties));
        
        AppUtil.findMissingPlugins(appDef);
        
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());

        // get app info
        String appInfo = consoleWebPlugin.getAppInfo(appId, version);
        map.addAttribute("appInfo", appInfo);

        String systemTheme = setupManager.getSettingValue("systemTheme");
        map.addAttribute("systemTheme", systemTheme);
        return "console/apps/builders";
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/builders/missingPlugins")
    public void consoleBuilderMissingPlugins(Writer writer, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONObject jsonObject = new JSONObject();
        if (appDef != null) {
            List<String> missingPlugins = AppUtil.findMissingPlugins(appDef);
            jsonObject.accumulate("result", missingPlugins);
            jsonObject.accumulate("error", ResourceBundleUtil.getMessage("dependency.tree.warning.MissingPlugin"));
        } else {
            jsonObject.accumulate("error", "App not found!");
        }
        
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/builders/overview")
    public void consoleBuilderOverview(Writer writer, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        if (appDef != null) {
            writer.write(AppOverviewUtil.getOverview(appDef));
        }
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/builders/overviewTools")
    public void consoleBuilderOverviewTools(Writer writer, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        Map<String, AppOverviewTool> tools = AppOverviewUtil.getTools();
        
        JSONArray jsonArr = new JSONArray();
        for (AppOverviewTool tool : tools.values()) {
            JSONObject obj = new JSONObject();
            obj.put("icon", tool.getIcon());
            obj.put("label", tool.getI18nLabel());
            obj.put("className", tool.getClassName());
            
            jsonArr.put(obj);
        }
        
        AppUtil.writeJson(writer, jsonArr, callback);
    }

    protected void checkAppPublishedVersion(AppDefinition appDef) {
        String appId = appDef.getId();
        Long publishedVersion = appService.getPublishedVersion(appId);
        if (publishedVersion == null || publishedVersion <= 0) {
            appDef.setPublished(Boolean.FALSE);
        }
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
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(formDef.getDateCreated(), null, AppUtil.getAppDateFormat()));
            data.put("dateModified", TimeZoneUtil.convertToTimeZone(formDef.getDateModified(), null, AppUtil.getAppDateFormat()));
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/forms/options")
    public void consoleFormOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows, @RequestParam(value = "customAppId", required = false) String customAppId) throws IOException, JSONException {

        Collection<FormDefinition> formDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }
        
        AppDefinition appDef = null;
        if (customAppId != null && !customAppId.isEmpty()) {
            appDef = appService.getPublishedAppDefinition(customAppId);
        } else {
            if (version == null || version.isEmpty()) {
                Long appVersion = appService.getPublishedVersion(appId);
                if (appVersion != null) {
                    version = appVersion.toString();
                }
            }
            appDef = appService.getAppDefinition(appId, version);
        }
        
        if (appDef != null) {
            formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, sort, desc, start, rows);
        } else {
            formDefinitionList = new ArrayList<FormDefinition>();
        }

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
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/formsWithCustomTable/options")
    public void consoleFormWithCustomTableOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

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
        
        Collection<String> customTables = CustomFormDataTableUtil.getTables(appDef);
        for (String table : customTables) {
            Map data = new HashMap();
            data.put("value", table);
            data.put("label", table);
            jsonArray.put(data);
        }
        
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/tableName/options")
    public void consoleFormTableNameOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        Collection<String> tableNameList = null;
        if (appDef != null) {
            tableNameList = formDefinitionDao.getTableNameList(appDef);
        } else {
            tableNameList = new ArrayList<String>();
        }
        Set<String> existingTables = new HashSet<String>();
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (String name : tableNameList) {
            existingTables.add(name);
            Map data = new HashMap();
            data.put("value", name);
            data.put("label", name);
            jsonArray.put(data);
        }
        
        Collection<String> customTables = CustomFormDataTableUtil.getTables(appDef);
        for (String table : customTables) {
            if (!existingTables.contains(table)) {
                Map data = new HashMap();
                data.put("value", table);
                data.put("label", table);
                jsonArray.put(data);
            }
        }
        
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/columns/options")
    public void consoleFormColumnsOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "formDefId", required = false) String formDefId, @RequestParam(value = "tableName", required = false) String formTable, @RequestParam(value = "tables", required = false) String tables, @RequestParam(value = "fields", required = false) String fields, @RequestParam(value = "customAppId", required = false) String customAppId) throws IOException, JSONException {
        AppDefinition appDef = null;
        if (customAppId != null && !customAppId.isEmpty()) {
            appDef = appService.getPublishedAppDefinition(customAppId);
        } else {
            appDef = appService.getAppDefinition(appId, version);
        }
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            if (formDefId != null) {
                String tableName = appService.getFormTableName(appDef, formDefId);
                if (tableName != null) {
                    populateColumns(jsonArray, tableName, false);
                } else {
                    populateColumns(jsonArray, formDefId, false);
                }
            }
            if (formTable != null) {
                populateColumns(jsonArray, formTable, false);
            }
            
            if (tables != null && !tables.isEmpty()) {
                for (String t : tables.split(";")) {
                    populateColumns(jsonArray, t, true);
                }
            }
            
            if (fields != null && !fields.isEmpty()) {
                for (String t : fields.split(";")) {
                    Map m = new HashMap();
                    m.put("value", t);
                    m.put("label", t);
                    jsonArray.put(m);
                }
            }
        } catch (Exception e) {
            //ignore
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/binder/columns/options")
    public void consoleFormBinderColumnsOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam String binderJson) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            if (binderJson != null && !binderJson.isEmpty()) {
                JSONObject binderObj = new JSONObject(binderJson);
                if (!binderObj.isNull(FormUtil.PROPERTY_CLASS_NAME)) {
                    String className = binderObj.getString(FormUtil.PROPERTY_CLASS_NAME);
                    if (className != null && className.trim().length() > 0) {
                        FormBinder  binder = (FormBinder) pluginManager.getPlugin(className);
                        if (binder != null && binder instanceof FormERDEntityRetriever) {
                            // set child properties
                            Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(binderObj);
                            binder.setProperties(properties);
                            
                            for (String id : ((FormERDEntityRetriever) binder).getEntity().getFields().keySet()) {
                                Map m = new HashMap();
                                m.put("value", id);
                                m.put("label", id);
                                jsonArray.put(m);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/datalist/columns/options")
    public void consoleDatalistColumnsOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "listId", required = false) String datalistId) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(datalistId, appDef);
        
            if (datalistDefinition != null) {
                String json = datalistDefinition.getJson();
                if (json != null) {
                    // strip enclosing brackets
                    json = json.trim();
                    if (json.startsWith("(")) {
                        json = json.substring(1);
                    }
                    if (json.endsWith(")")) {
                        json = json.substring(0, json.length() - 1);
                    }
                    JSONObject obj = new JSONObject(json);
                    JSONArray columns = obj.getJSONArray(JsonUtil.PROPERTY_COLUMNS);
                    
                    for (int i = 0; i < columns.length(); i++) {
                        JSONObject column = columns.getJSONObject(i);
                        String name = column.get(JsonUtil.PROPERTY_NAME).toString();
                        
                        Map op = new HashMap();
                        op.put("value", name);
                        op.put("label", name);
                        jsonArray.put(op);
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/menu/options")
    public void consoleUserviewMenuOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "userviewId", required = false) String userviewId) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            for (Iterator<String> i = userviewService.getAllMenuIds(appId, version, userviewId).iterator(); i.hasNext();) {
                String id = i.next();
                blank = new HashMap();
                blank.put("value", id);
                blank.put("label", id);
                jsonArray.put(blank);
            }
        } catch (Exception e) {
            //ignore
        }
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/userview/menu/tree/options")
    public void consoleUserviewMenuTreeOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "userviewId", required = false) String userviewId, @RequestParam(value = "customAppId", required = false) String customAppId) throws IOException, JSONException {
        AppDefinition appDef = null;
        if (customAppId != null && !customAppId.isEmpty()) {
            appDef = appService.getPublishedAppDefinition(customAppId);
        } else {
            if (version == null || version.isEmpty()) {
                Long appVersion = appService.getPublishedVersion(appId);
                if (appVersion != null) {
                    version = appVersion.toString();
                }
            }
            appDef = appService.getAppDefinition(appId, version);
        }
        JSONObject obj = userviewService.getMenuTree(appDef, userviewId);
        
        AppUtil.writeJson(writer, obj, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/workflowVariable/options")
    public void consoleWorkflowVariableOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            Map<String, String> options = new HashMap<String, String>();
            Collection<WorkflowProcess> processList = null;
            if (appDef != null) {
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                if (packageDefinition != null) {
                    Long packageVersion = packageDefinition.getVersion();
                    processList = workflowManager.getProcessList(appId, packageVersion.toString());
                    for (WorkflowProcess wp : processList) {
                        String processDefId = wp.getId();
                        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processDefId);

                        for (WorkflowVariable v : variableList) {
                            String processname = options.get(v.getId());
                            if (processname != null) {
                                processname += ", " + wp.getName();
                            } else {
                                processname = wp.getName();
                            }
                            options.put(v.getId(), processname);
                        }
                    }
                }
            }
            
            if (!options.isEmpty()) {
                for (String key : options.keySet()) {
                    Map<String, String> op = new HashMap<String, String>();
                    op.put("value", key);
                    op.put("label", key + " (" + options.get(key) + ")");
                    jsonArray.put(op);
                }
            }
        } catch (Exception e) {
            //ignore
        }
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/envVariable/options")
    public void consoleEnvVariableOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            if (appDef != null) {
                Collection<EnvironmentVariable> envList = appDef.getEnvironmentVariableList();
                if (envList != null) {
                    for (EnvironmentVariable e : envList) {
                        Map op = new HashMap();
                        op.put("value", e.getId());
                        op.put("label", e.getId());
                        jsonArray.put(op);
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/appResource/options")
    public void consoleAppResourceOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            if (appDef != null) {
                Collection<AppResource> list = appDef.getResourceList();
                if (list != null) {
                    for (AppResource r : list) {
                        Map op = new HashMap();
                        op.put("value", r.getId());
                        op.put("label", r.getId());
                        jsonArray.put(op);
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }
        jsonArray = sortJSONArray(jsonArray, "label", false);
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/processActivity/options")
    public void consoleProcessActivityOptionsJson(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        JSONObject jsonObject = new JSONObject();
        
        try {
            if (appDef != null) {
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                if (packageDefinition != null) {
                    Long packageVersion = packageDefinition.getVersion();
                    Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());
                    for (WorkflowProcess wp : processList) {
                        JSONArray arr = new JSONArray();
                        
                        JSONObject runprocess = new JSONObject();
                        runprocess.put("value", WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
                        runprocess.put("label", ResourceBundleUtil.getMessage("console.process.config.label.startProcess"));
                        arr.put(runprocess);
                        
                        //get activity list
                        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(wp.getId());
                        if (activityList != null) {
                            for (WorkflowActivity act : activityList) {
                                if (act.getType().equals(WorkflowActivity.TYPE_NORMAL)) {
                                    JSONObject temp = new JSONObject();
                                    temp.put("value", act.getId());
                                    temp.put("label", act.getName());
                                    arr.put(temp);
                                }
                            }
                        } 
        
                        jsonObject.put(wp.getName(), arr);
                    }
                }
            }
        } catch (Exception e) {
            //ignore
        }
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping("/json/console/app/options")
    public void consoleAppOptionsJson(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            if (appDefinitionList != null) {
                for (AppDefinition a : appDefinitionList) {
                    Map op = new HashMap();
                    op.put("value", a.getAppId());
                    op.put("label", a.getName());
                    jsonArray.put(op);
                }
            }
        } catch (Exception e) {
            //ignore
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/timezone/options")
    public void consoleTimezoneOptionsJson(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        
        try {
            Map<String, String> list = TimeZoneUtil.getList();
            if (list != null) {
                for (Map.Entry e : list.entrySet()) {
                    Map op = new HashMap();
                    op.put("value", e.getKey());
                    op.put("label", e.getValue());
                    jsonArray.put(op);
                }
            }
        } catch (Exception e) {
            //ignore
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    protected void populateColumns(JSONArray jsonArray, String tableName, boolean prefix) {
        JSONArray cjsonArray = new JSONArray();
        String prefixString = "";
        if (prefix) {
            prefixString = tableName;
            if (prefixString.startsWith("app_fd_")) {
                prefixString = prefixString.substring("app_fd_".length());
            }
            prefixString += ".";
        }
        Collection<String> columnNames = formDataDao.getFormDefinitionColumnNames(tableName);
        Map id = new HashMap();
        id.put("value", prefixString + FormUtil.PROPERTY_ID);
        id.put("label", prefixString + FormUtil.PROPERTY_ID);
        cjsonArray.put(id);
        for (String columnName : columnNames) {
            Map data = new HashMap();
            data.put("value", prefixString + columnName);
            data.put("label", prefixString + columnName);
            cjsonArray.put(data);
        }
        Map cd = new HashMap();
        cd.put("value", prefixString + FormUtil.PROPERTY_DATE_CREATED);
        cd.put("label", prefixString + FormUtil.PROPERTY_DATE_CREATED);
        cjsonArray.put(cd);
        Map md = new HashMap();
        md.put("value", prefixString + FormUtil.PROPERTY_DATE_MODIFIED);
        md.put("label", prefixString + FormUtil.PROPERTY_DATE_MODIFIED);
        cjsonArray.put(md);
        Map mdb = new HashMap();
        mdb.put("value", prefixString + FormUtil.PROPERTY_MODIFIED_BY);
        mdb.put("label", prefixString + FormUtil.PROPERTY_MODIFIED_BY);
        cjsonArray.put(mdb);
        Map cdb = new HashMap();
        cdb.put("value", prefixString + FormUtil.PROPERTY_CREATED_BY);
        cdb.put("label", prefixString + FormUtil.PROPERTY_CREATED_BY);
        cjsonArray.put(cdb);
        Map mdbn = new HashMap();
        mdbn.put("value", prefixString + FormUtil.PROPERTY_MODIFIED_BY_NAME);
        mdbn.put("label", prefixString + FormUtil.PROPERTY_MODIFIED_BY_NAME);
        cjsonArray.put(mdbn);
        Map cdbn = new HashMap();
        cdbn.put("value", prefixString + FormUtil.PROPERTY_CREATED_BY_NAME);
        cdbn.put("label", prefixString + FormUtil.PROPERTY_CREATED_BY_NAME);
        cjsonArray.put(cdbn);
        cjsonArray = sortJSONArray(cjsonArray, "label", false);
        try {
            for (int i = 0; i < cjsonArray.length(); i++) {
                jsonArray.put(cjsonArray.get(i));
            }
        } catch (Exception e){}
    } 

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/create")
    public String consoleFormCreate(ModelMap model, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("appVersion", version);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formDefinition", new FormDefinition());
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);
        
        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        model.addAttribute("appList", appDefinitionList);
        
        return "console/apps/formCreate";
    }

    @RequestMapping("/json/console/app/(*:appId)/(~:version)/form/tableNameList")
    public void consoleFormTableNameList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException, JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        Collection<String> tableNameList = formDefinitionDao.getTableNameList(appDef);

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("tableName", tableNameList);
        if (callback != null && callback.trim().length() != 0) {
            writer.write(HtmlUtils.htmlEscape(callback) + "(" + jsonObject + ");");
        } else {
            jsonObject.write(writer);
        }
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/submit", method = RequestMethod.POST)
    public String consoleFormSubmit(ModelMap model, @ModelAttribute("formDefinition") FormDefinition formDefinition, BindingResult result, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "activityDefId", required = false) String activityDefId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyFormId", required = false) String copyFormId) throws UnsupportedEncodingException {
        FormDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyFormId != null && !copyFormId.isEmpty()) {
            Long copyVersion = appService.getPublishedVersion(copyAppId);
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, (copyVersion != null)?copyVersion.toString():null);
            copy = formDefinitionDao.loadById(copyFormId, copyAppDef);
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // validate ID
        validator.validate(formDefinition, result);
        boolean invalid = result.hasErrors();
        if (!invalid) {
            // create form
            String defaultJson = FormUtil.generateDefaultForm(formDefinition.getId(), formDefinition, copy);
            formDefinition.setJson(defaultJson);
            Collection<String> errors = appService.createFormDefinition(appDef, formDefinition);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }

        String formId = formDefinition.getId();
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("appVersion", version);
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("formId", formId);
        model.addAttribute("formDefinition", formDefinition);
        model.addAttribute("activityDefId", activityDefId);
        model.addAttribute("processDefId", processDefId);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            model.addAttribute("appList", appDefinitionList);
        
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
    public String consoleFormUpdate(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId, @RequestParam(value = "json") String json) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }        
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        // load existing form definition and update fields
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        Form form = (Form) formService.createElementFromJson(json);
        formDef.setName(form.getPropertyString("name"));
        formDef.setTableName(form.getPropertyString("tableName"));
        formDef.setJson(PropertyUtil.propertiesJsonStoreProcessing(formDef.getJson(), json));
        formDef.setDescription(form.getPropertyString("description"));

        // update
        formDefinitionDao.update(formDef);
        formDataDao.clearFormCache(form);
        return "console/apps/dialogClose";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/form/delete", method = RequestMethod.POST)
    public String consoleFormDelete(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "ids") String formId) {
        String result = checkVersionExist(map, appId, version);
        if (result != null) {
            return result;
        }
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
        // additional UserSecurity settings
        UserSecurity us = DirectoryUtil.getUserSecurity();
        model.addAttribute("userSecurity", us);
        return "console/run/inbox";
    }

    @RequestMapping("/console/setting/general")
    public String consoleSettingGeneral(ModelMap map) {
        Collection<Setting> settingList = setupManager.getSettingList("", null, null, null, null);

        Map<String, String> settingMap = new HashMap<String, String>();
        for (Setting setting : settingList) {
            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(setting.getProperty()) || SetupManager.SMTP_PASSWORD.equals(setting.getProperty()) || "smtpStorepass".equals(setting.getProperty())) {
                settingMap.put(setting.getProperty(), SetupManager.SECURE_VALUE);
            } else {
                settingMap.put(setting.getProperty(), setting.getValue());
            }
        }

        Locale[] localeList = Locale.getAvailableLocales();
        Map<String, String> localeStringList = new TreeMap<String, String>();
        for (int x = 0; x < localeList.length; x++) {
            localeStringList.put(localeList[x].toString(), localeList[x].toString() + " - " +localeList[x].getDisplayName(LocaleContextHolder.getLocale()));
        }

        map.addAttribute("serverTZ", TimeZoneUtil.getServerTimeZoneID());
        map.addAttribute("timezones", TimeZoneUtil.getList());
        map.addAttribute("localeList", localeStringList);
        map.addAttribute("settingMap", settingMap);

        // additional UserSecurity settings
        UserSecurity us = DirectoryUtil.getUserSecurity();
        map.addAttribute("userSecurity", us);
        
        // userviews to select app center
        Collection<UserviewDefinition> userviewDefinitionList = new ArrayList<UserviewDefinition>();
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        for (Iterator<AppDefinition> i = appDefinitionList.iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            userviewDefinitionList.addAll(appDef.getUserviewDefinitionList());
            map.addAttribute("userviewDefinitionList", userviewDefinitionList);
        }        
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

        AppUtil.writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/console/setting/general/submit", method = RequestMethod.POST)
    public String consoleSettingGeneralSubmit(HttpServletRequest request, ModelMap map) {
        boolean localeChanged = false;
        
        List<String> settingsIsNotNull = new ArrayList<String>();

        List<String> booleanSettingsList = new ArrayList<String>();
        booleanSettingsList.add("enableNtlm");
        booleanSettingsList.add("rightToLeft");
        booleanSettingsList.add("enableUserLocale");
        booleanSettingsList.add("dateFormatFollowLocale");
        booleanSettingsList.add("datepickerFollowLocale");
        booleanSettingsList.add("disableAdminBar");
        booleanSettingsList.add("disableWebConsole");
        booleanSettingsList.add("disablePerformanceAnalyzer");
        booleanSettingsList.add("disableListRenderHtml");
        
        boolean refreshPlugins = false;

        //request params
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String paramName = (String) e.nextElement();
            String paramValue = request.getParameter(paramName);
            
            if ("OWASP_CSRFTOKEN".equals(paramName)) {
                continue;
            }

            if (booleanSettingsList.contains(paramName)) {
                settingsIsNotNull.add(paramName);
                paramValue = "true";
            }

            Setting setting = setupManager.getSettingByProperty(paramName);
            if (setting == null) {
                setting = new Setting();
                setting.setProperty(paramName);
            }
            
            if ("dataFileBasePath".equals(paramName)) {
                String orgValue = (setting.getValue() != null)?setting.getValue():"";
                if (!orgValue.equals(paramValue)) {
                    refreshPlugins = true;
                }
            }
            
            if (SetupManager.MASTER_LOGIN_PASSWORD.equals(paramName) || SetupManager.SMTP_PASSWORD.equals(paramName) || "smtpStorepass".equals(paramName)) {
                if (!SetupManager.SECURE_VALUE.equals(paramValue)) {
                    setting.setValue(SecurityUtil.encrypt(paramValue));
                }
            } else {
                
                //check for locale changes
                if ("systemLocale".equals(paramName) && !paramValue.equals(setting.getValue())) {
                    localeChanged = true;
                }
                
                setting.setValue(paramValue);
            }
            
            if (HostManager.isVirtualHostEnabled() && ("dataFileBasePath".equals(paramName) || "designerwebBaseUrl".equals(paramName))) {
                setting.setValue("");
            }
            
            setupManager.saveSetting(setting);
            
            //only run it after saved the value, workflow manager do check the value before run the migration
            if ("deleteProcessOnCompletion".equals(paramName) && "archive".equals(paramValue) && !"archive".equals(setting.getOriginalValue())) {
                workflowManager.internalMigrateProcessHistories();
            }
        }

        for (String s : booleanSettingsList) {
            if (!settingsIsNotNull.contains(s)) {
                Setting setting = setupManager.getSettingByProperty(s);
                if (setting == null) {
                    setting = new Setting();
                    setting.setProperty(s);
                }
                setting.setValue("false");
                setupManager.saveSetting(setting);
            }
        }

        //clear all caches & update the settings
        setupManager.clearCache();
        
        //only reset the locale when setting changed
        if (localeChanged) {
            ((LocalLocaleResolver) localeResolver).reset(request);
        }
        
        if (refreshPlugins) {
            pluginManager.refresh();
        } else {
            pluginManager.clearCache();
        }
        workflowManager.internalUpdateDeadlineChecker();
        FileStore.updateFileSizeLimit();

        return "redirect:/web/console/setting/general";
    }

    @RequestMapping("/console/setting/datasource")
    public String consoleSettingDatasource(ModelMap map) {
        Map<String, String> settingMap = new HashMap<String, String>();

        Properties properties = DynamicDataSourceManager.getProperties();
        for (Object key : properties.keySet()) {
            if (!DynamicDataSourceManager.SECURE_FIELD.equals(key)) {
                settingMap.put(key.toString(), properties.getProperty(key.toString()));
            } else {
                settingMap.put(key.toString(), DynamicDataSourceManager.SECURE_VALUE);
            }
        }

        map.addAttribute("settingMap", settingMap);
        map.addAttribute("profileList", DynamicDataSourceManager.getProfileList());
        map.addAttribute("currentProfile", DynamicDataSourceManager.getCurrentProfile());

        return "console/setting/datasource";
    }

    @RequestMapping(value = "/console/setting/profile/change", method = RequestMethod.POST)
    public void consoleProfileChange(Writer writer, @RequestParam("profileName") String profileName) {
        WorkflowUtil.switchProfile(profileName);
    }

    @RequestMapping(value = "/console/setting/profile/create", method = RequestMethod.POST)
    public void consoleProfileCreate(Writer writer, HttpServletRequest request, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            SecurityUtil.validateStringInput(profileName);
            //get 
            String secureValue = DynamicDataSourceManager.getProperty(DynamicDataSourceManager.SECURE_FIELD);
            
            DynamicDataSourceManager.createProfile(profileName);
            DynamicDataSourceManager.changeProfile(profileName);

            //request params
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String paramName = (String) e.nextElement();
                if (!paramName.equals("profileName")) {
                    String paramValue = request.getParameter(paramName);
                    
                    if (DynamicDataSourceManager.SECURE_FIELD.equals(paramName) && DynamicDataSourceManager.SECURE_VALUE.equals(paramValue)) {
                        paramValue = secureValue;
                    }
                    
                    DynamicDataSourceManager.writeProperty(paramName, paramValue);
                }
            }
            WorkflowUtil.switchProfile(profileName);
        }
    }

    @RequestMapping(value = "/console/setting/profile/delete", method = RequestMethod.POST)
    public void consoleProfileDelete(Writer writer, @RequestParam("profileName") String profileName) {
        if (!HostManager.isVirtualHostEnabled()) {
            SecurityUtil.validateStringInput(profileName);
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
                
                if (DynamicDataSourceManager.SECURE_FIELD.equals(paramName) && DynamicDataSourceManager.SECURE_VALUE.equals(paramValue)) {
                    paramValue = DynamicDataSourceManager.getProperty(DynamicDataSourceManager.SECURE_FIELD);
                }
                
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
        String className = "";
        if (DirectoryUtil.isOverridden()) {
            className = DirectoryUtil.getOverriddenDirectoryManagerClassName();
        } else if (settingMap.get("directoryManagerImpl") != null) {
            className = settingMap.get("directoryManagerImpl");
        }
        
        if (className != null && !className.isEmpty()) {
            Plugin plugin = pluginManager.getPlugin(className);
            if (plugin != null) {
                map.addAttribute("directoryManagerName", plugin.getI18nLabel());
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
        directoryManagerImpl = SecurityUtil.validateStringInput(directoryManagerImpl);
        Plugin plugin = pluginManager.getPlugin(directoryManagerImpl);
        
        if (plugin != null) {
            String properties = "";
            if (directoryManagerImpl != null && directoryManagerImpl.equals(DirectoryUtil.getOverriddenDirectoryManagerClassName())) {
                properties = setupManager.getSettingValue(DirectoryUtil.CUSTOM_IMPL_PROPERTIES);
            } else {
                properties = setupManager.getSettingValue(DirectoryUtil.IMPL_PROPERTIES);
            }

            if (!(plugin instanceof PropertyEditable)) {
                Map propertyMap = new HashMap();
                propertyMap = CsvUtil.getPluginPropertyMap(properties);
                map.addAttribute("propertyMap", propertyMap);
            } else {
                map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(properties));
            }

            if (plugin instanceof PropertyEditable) {
                PropertyEditable pe = (PropertyEditable) plugin;
                map.addAttribute("propertyEditable", pe);
                map.addAttribute("propertiesDefinition", PropertyUtil.injectHelpLink(plugin.getHelpLink(), pe.getPropertyOptions()));
            }

            map.addAttribute("plugin", plugin);

            String url = request.getContextPath() + "/web/console/setting/directoryManagerImpl/config/submit?id=" + StringEscapeUtils.escapeHtml(directoryManagerImpl);
            map.addAttribute("actionUrl", url);
            
            return "console/plugin/pluginConfig";
        } else {
            return "error404";
        }
    }

    @RequestMapping(value = "/console/setting/directoryManagerImpl/config/submit", method = RequestMethod.POST)
    public String consoleSettingDirectoryManagerImplConfigSubmit(ModelMap map, @RequestParam("id") String id, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) {
        Plugin plugin = (Plugin) pluginManager.getPlugin(id);

        String settingName = "";
        if (id != null && id.equals(DirectoryUtil.getOverriddenDirectoryManagerClassName())) {
            settingName = DirectoryUtil.CUSTOM_IMPL_PROPERTIES;
        } else {
            settingName = DirectoryUtil.IMPL_PROPERTIES;
        }
        
        //save plugin
        Setting setting = setupManager.getSettingByProperty("directoryManagerImpl");
        if (setting == null) {
            setting = new Setting();
            setting.setProperty("directoryManagerImpl");
        }
        setting.setValue(id);
        setupManager.saveSetting(setting);

        Setting propertySetting = setupManager.getSettingByProperty(settingName);
        if (propertySetting == null) {
            propertySetting = new Setting();
            propertySetting.setProperty(settingName);
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
            propertySetting.setValue(PropertyUtil.propertiesJsonStoreProcessing(propertySetting.getValue(), pluginProperties));
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

    @RequestMapping(value = "/console/setting/plugin/refresh", method = RequestMethod.POST)
    public void consoleSettingPluginRefresh(Writer writer) {
        setupManager.clearCache();
        pluginManager.refresh();
    }

    @RequestMapping("/console/setting/plugin/upload")
    public String consoleSettingPluginUpload() {
        return "console/setting/pluginUpload";
    }

    @RequestMapping(value = "/console/setting/plugin/upload/submit", method = RequestMethod.POST)
    public String consoleSettingPluginUploadSubmit(ModelMap map, HttpServletRequest request) throws IOException {
        MultipartFile pluginFile;
        String jarname = "";
        try {
            pluginFile = FileStore.getFile("pluginFile");
        } catch (FileLimitException e) {
            map.addAttribute("errorMessage", ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
            return "console/setting/pluginUpload";
        }

        InputStream in = null;
        try {
            in = pluginFile.getInputStream();
            jarname = pluginFile.getOriginalFilename();
            
            if (!jarname.endsWith(".jar")) {
                map.addAttribute("errorMessage", ResourceBundleUtil.getMessage("console.app.message.error.invalidJar"));
                return "console/setting/pluginUpload";
            } else {
                pluginManager.upload(jarname, in);
            }
        } catch (Exception e) {
            map.addAttribute("errorMessage", ResourceBundleUtil.getMessage("console.app.message.error.invalidJar"));
            return "console/setting/pluginUpload";
        } finally {
            if (in != null) {
                in.close();
            }
        }
        
        auditTrailManager.addAuditTrail(PluginManager.class.getName(), "installPlugin", jarname, null, null, null);
        LogUtil.info(PluginManager.class.getName(), workflowUserManager.getCurrentUsername() + " installed plugin (" + jarname + ").");
        
        String url = request.getContextPath() + "/web/console/setting/plugin";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/setting/plugin/uninstall", method = RequestMethod.POST)
    public String consoleSettingPluginUninstall(ModelMap map, @RequestParam("selectedPlugins") String selectedPlugins) {
        StringTokenizer strToken = new StringTokenizer(selectedPlugins, ",");
        Set<String> uninstalledJars = new HashSet<String>();
        while (strToken.hasMoreTokens()) {
            String pluginClassName = (String) strToken.nextElement();
            String jar = pluginManager.getJarFileName(pluginClassName);
            if (jar != null) {
                uninstalledJars.add(jar);
                pluginManager.uninstall(pluginClassName);
            }
        }
        if (!uninstalledJars.isEmpty()) {
            String plugins = StringUtils.join(uninstalledJars, ", ");
            auditTrailManager.addAuditTrail(PluginManager.class.getName(), "uninstallPlugin", plugins, null, null, null);
            LogUtil.info(PluginManager.class.getName(), workflowUserManager.getCurrentUsername() + " uninstalled plugin (" + plugins + ").");
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

        AppUtil.writeJson(writer, jsonObject, callback);
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
        
        String errorMsg = null;
        MultipartFile multiPartfile = null;
        
        try {
            multiPartfile = FileStore.getFile("localeFile");
        } catch (FileLimitException e) {
            errorMsg = ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()});
        }

        if (multiPartfile != null) {
            try {
                ResourceBundleUtil.POFileImport(multiPartfile, systemLocale);
            } catch (IOException e) {
                errorMsg = ResourceBundleUtil.getMessage("console.setting.message.import.error.invalidPoFile");
            }
        }
        
        if (errorMsg != null) {
            map.addAttribute("errorMessage", errorMsg);
            map.addAttribute("localeList", getSortedLocalList());
            return "console/setting/messageImport";
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
    public void consoleMonitorRunningListJson(Writer writer, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "recordId", required = false) String recordId, @RequestParam(value = "requester", required = false) String requester, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(appId, processId, processName, version, recordId, requester, sort, desc, start, rows);
        int count = workflowManager.getRunningProcessSize(appId, processId, processName, version, recordId, requester);

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelValue(workflowProcess.getStartedTime(), workflowProcess.getFinishTime(), workflowProcess.getDue());

            Map data = new HashMap();
            data.put("recordId", workflowProcess.getRecordId());
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", TimeZoneUtil.convertToTimeZone(workflowProcess.getStartedTime(), null, AppUtil.getAppDateFormat()));
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? TimeZoneUtil.convertToTimeZone(workflowProcess.getDue(), null, AppUtil.getAppDateFormat()) : "-");

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
        
        String recordId = wfProcess.getInstanceId();
        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(recordId);
        if (link != null) {
            recordId = link.getOriginProcessId();
        }
        map.addAttribute("recordId", recordId);

        AppDefinition appDef = appService.getAppDefinitionWithProcessDefId(wfProcess.getId());
        map.addAttribute("appDef", appDef);
        
        return "console/monitor/runningProcess";
    }

    @RequestMapping(value = "/console/monitor/running/process/abort/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessAbort(ModelMap map, @RequestParam("id") String processId) {
        appService.getAppDefinitionForWorkflowProcess(processId);
        workflowManager.processAbort(processId);
        return "console/dialogClose";
    }

    @RequestMapping(value = "/console/monitor/running/process/reevaluate/(*:id)", method = RequestMethod.POST)
    public String consoleMonitorRunningProcessReevaluate(ModelMap map, @RequestParam("id") String processId) {
        appService.getAppDefinitionForWorkflowProcess(processId);
        workflowManager.reevaluateAssignmentsForProcess(processId);
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/(*:mode)")
    public String consoleMonitorCompleted(ModelMap map, @RequestParam("mode") String mode) {
        if (!("completed".equals(mode) || "archived".equals(mode))) {
            mode = "completed";
        }
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        map.addAttribute("appDefinitionList", appDefinitionList);
        map.addAttribute("mode", mode);
        
        if ("completed".equals(mode)) {
            map.addAttribute("hasNonArchivedProcessData", AppUtil.hasNonArchivedProcessData());
        }
        
        return "console/monitor/completed";
    }
    
    @RequestMapping(value = "/console/monitor/completed/process/archive", method = RequestMethod.POST)
    public String consoleMonitorCompletedProcessArchive() {
        Setting setting = setupManager.getSettingByProperty("deleteProcessOnCompletion");
        if (setting == null) {
            setting = new Setting();
            setting.setProperty("deleteProcessOnCompletion");
        }
        setting.setValue("archive");
        setupManager.saveSetting(setting);
        workflowManager.internalMigrateProcessHistories();
        
        return "console/dialogClose";
    }
    
    @RequestMapping(value = "/json/console/monitor/completed/process/archive/(*:mode)", method = RequestMethod.POST)
    public void consoleMonitorArchivePauseResume(Writer writer, @RequestParam("mode") String mode) throws IOException {
        if ("resume".equals(mode)) {
            workflowManager.internalMigrateProcessHistories();
        } else if ("pause".equals(mode)) {
            //not using setupManager due to the value is cached
            SetupDao setupDao = (SetupDao) WorkflowUtil.getApplicationContext().getBean("setupDao");
            Collection<Setting> result = setupDao.find("WHERE property = ?", new String[]{WorkflowManagerImpl.ARCHIVE_SETTING}, null, null, null, null);
            Setting status = (result.isEmpty()) ? null : result.iterator().next();
           
            if (status != null) {
                status.setValue(status.getValue().replace("STARTED", "PAUSE"));
                setupDao.saveOrUpdate(status);
            }
        }
        
        writer.write(Double.toString(AppUtil.getArchivedProcessStatus()));
    }

    @RequestMapping("/json/console/monitor/(*:mode)/list")
    public void consoleMonitorCompletedListJson(Writer writer, @RequestParam("mode") String mode, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "recordId", required = false) String recordId, @RequestParam(value = "requester", required = false) String requester, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if (!("completed".equals(mode) || "archived".equals(mode))) {
            mode = "completed";
        }
        
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList;
        int count;
        
        if (mode.equals("completed")) {
            processList = workflowManager.getCompletedProcessList(appId, processId, processName, version, recordId, requester, sort, desc, start, rows);
            count = workflowManager.getCompletedProcessSize(appId, processId, processName, version, recordId, requester);
        } else {
            processList = workflowAssignmentDao.getProcessHistories(appId, null, processId, processName, version, recordId, requester, sort, desc, start, rows);
            count = (int) workflowAssignmentDao.getProcessHistoriesSize(appId, null, processId, processName, version, recordId, requester);
        }

        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelValue(workflowProcess.getStartedTime(), workflowProcess.getFinishTime(), workflowProcess.getDue());

            Map data = new HashMap();
            data.put("recordId", workflowProcess.getRecordId());
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", TimeZoneUtil.convertToTimeZone(workflowProcess.getStartedTime(), null, AppUtil.getAppDateFormat()));
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? TimeZoneUtil.convertToTimeZone(workflowProcess.getDue(), null, AppUtil.getAppDateFormat()) : "-");
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/monitor/(*:mode)/process/view/(*:id)")
    public String consoleMonitorCompletedProcess(ModelMap map, @RequestParam("mode") String mode, @RequestParam("id") String processId) {
        if (!("completed".equals(mode) || "archived".equals(mode))) {
            mode = "completed";
        }
        
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);
        double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(processId);

        map.addAttribute("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

        WorkflowProcess trackWflowProcess = workflowManager.getRunningProcessInfo(processId);
        map.addAttribute("wfProcess", wfProcess);
        map.addAttribute("trackWflowProcess", trackWflowProcess);
        
        String recordId = wfProcess.getInstanceId();
        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(recordId);
        if (link != null) {
            recordId = link.getOriginProcessId();
        } else if (wfProcess.getRecordId() != null) {
            recordId = wfProcess.getRecordId();
        }
        map.addAttribute("recordId", recordId);

        AppDefinition appDef = appService.getAppDefinitionWithProcessDefId(wfProcess.getId());
        if (appDef == null) {
            Long procVer = Long.parseLong(wfProcess.getVersion());
            String appId = wfProcess.getPackageId();
            Collection<AppDefinition> appDefList = appDefinitionDao.findVersions(appId, "version", true, 0, -1);
            for (AppDefinition appDefVer: appDefList) {
                Long packageVer = appDefVer.getPackageDefinition().getVersion();
                if (packageVer >= procVer) {
                    appDef = appDefVer;
                } else {
                    break;
                }
            }
        }
        map.addAttribute("appDef", appDef);
        map.addAttribute("mode", mode);
        return "console/monitor/completedProcess";
    }

    @RequestMapping(value = "/console/monitor/process/delete", method = RequestMethod.POST)
    public String consoleMonitorProcessDelete(@RequestParam(value = "ids") String ids) {
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            try {
                appService.getAppDefinitionForWorkflowProcess(id);
                workflowManager.removeProcessInstance(id);
            } finally {
                AppUtil.resetAppDefinition();
            }
        }
        return "console/dialogClose";
    }

    @RequestMapping("/console/monitor/process/viewGraph/(*:id)")
    public String consoleMonitorProcessViewGraph(ModelMap map, @RequestParam("id") String processId) {
        return consoleMonitorProcessGraph(map, processId, true);
    }
    
    @RequestMapping("/console/monitor/process/graph/(*:id)")
    public String consoleMonitorProcessGraph(ModelMap map, @RequestParam("id") String processId, Boolean useOldViewer) {
        // get process info
        WorkflowProcess wfProcess = workflowManager.getRunningProcessById(processId);

        if (wfProcess != null) {
            // get process xpdl
            byte[] xpdlBytes = workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion());
            if (xpdlBytes != null) {
                String xpdl = null;

                try {
                    xpdl = new String(xpdlBytes, "UTF-8");
                } catch (Exception e) {
                    LogUtil.debug(ConsoleWebController.class.getName(), "XPDL cannot load");
                }
                
                try {
                    JSONObject jsonDef = new JSONObject();
                    String xpdlJson = U.xmlToJson(xpdl);
                    jsonDef.put("xpdl", new JSONObject(xpdlJson));
                
                    // get running activities
                    Collection<String> runningActivityIdList = new ArrayList<String>();
                    List<WorkflowActivity> activityList = (List<WorkflowActivity>) workflowManager.getActivityList(processId, 0, -1, "id", false);
                    if (activityList != null) {
                        for (WorkflowActivity wa : activityList) {
                            if (wa.getState().indexOf("open") >= 0) {
                                runningActivityIdList.add(wa.getActivityDefId());
                            }
                        }
                    }

                    String[] runningActivityIds = (String[]) runningActivityIdList.toArray(new String[0]);

                    map.addAttribute("wfProcess", wfProcess);
                    map.addAttribute("json", jsonDef.toString());
                    map.addAttribute("runningActivityIds", runningActivityIds);
                } catch (Exception e) {
                    LogUtil.error(ConsoleWebController.class.getName(), e, "");
                }
            }
        }

        String viewer = (useOldViewer == null || !useOldViewer) ? "pbuilder/pviewer" : "console/monitor/processGraph";
        return viewer;
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
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(workflowActivity.getCreatedTime(), null, AppUtil.getAppDateFormat()));
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

        if (!("running".equals(processStatus) || "completed".equals(processStatus) || "archived".equals(processStatus))) {
            processStatus = "completed";
        }

        map.addAttribute("processStatus", processStatus);
        return "console/monitor/activity";
    }

    @RequestMapping("/console/monitor/running/activity/reassign")
    public String consoleMonitorActivityReassign(ModelMap map, @RequestParam("state") String state, @RequestParam("processDefId") String processDefId, @RequestParam("activityId") String activityId, @RequestParam("processId") String processId) {
        map.addAttribute("activityId", activityId);
        map.addAttribute("processId", processId);
        map.addAttribute("state", state);
        map.addAttribute("processDefId", processDefId);
        Collection<Organization> organizations = null;
        if (DirectoryUtil.isExtDirectoryManager()) {
            organizations = directoryManager.getOrganizationsByFilter(null, "name", false, null, null);
        }
        map.addAttribute("organizations", organizations);

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
        if (HostManager.isVirtualHostEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
                
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
    public void consoleMonitorLogsJson(HttpServletResponse response, Writer writer, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        if (HostManager.isVirtualHostEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        File[] files = LogUtil.tomcatLogFiles();
        List<Map> fileList = new ArrayList<Map>();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String lowercaseFN = file.getName().toLowerCase(Locale.ENGLISH);
                    Date lastModified = new Date(file.lastModified());
                    Date current = new Date();

                    if ("catalina.out".equals(lowercaseFN) || (lowercaseFN.indexOf(".log") > 0 && !lowercaseFN.startsWith("admin") && !lowercaseFN.startsWith("host-manager") && !lowercaseFN.startsWith("manager"))
                        && (lastModified.getTime() > (current.getTime() - (5*1000*60*60*24))) && file.length() > 0) {
                        Map data = new HashMap();
                        data.put("filename", file.getName());
                        data.put("filesize", file.length());
                        data.put("date", TimeZoneUtil.convertToTimeZone(new Date(file.lastModified()), null, AppUtil.getAppDateFormat()));
                        fileList.add(data);
                    }
                }
            }
        }
        
        if (sort == null || sort.isEmpty()) {
            sort = "filename";
        }
        
        PagedList<Map> pagedList = new PagedList<Map>(true, fileList, sort, desc, start, rows, fileList.size());

        for (Map file : pagedList) {
            jsonObject.accumulate("data", file);
        }

        jsonObject.accumulate("total", fileList.size());
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/console/i18n/(*:name)")
    public String consoleI18n(ModelMap map, HttpServletResponse response, @RequestParam("name") String name, @RequestParam(value = "type", required = false) String type) throws IOException {
        Properties keys = new Properties();
        
        //get message key from property file
        InputStream inputStream = null;
        try {
            if ("cbuilder".equals(name)) {
                if (type != null && (type.endsWith("form") || type.endsWith("datalist") || type.endsWith("process"))) {
                    InputStream inputStream2 = null;
                    try {
                        if (type.endsWith("form")) {
                            inputStream2 = this.getClass().getClassLoader().getResourceAsStream("fbuilder.properties");
                        } else if (type.endsWith("datalist")) {
                            inputStream2 = this.getClass().getClassLoader().getResourceAsStream("dbuilder.properties");
                        } else if (type.endsWith("process")) {
                            inputStream2 = this.getClass().getClassLoader().getResourceAsStream("pbuilder.properties");
                        }
                        
                        keys.load(inputStream2);
                    } finally {
                        if (inputStream2 != null) {
                            inputStream2.close();
                        }
                    }
                } else if (type != null) {
                    CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
                    if (builder != null) {
                        ResourceBundle bundle =  pluginManager.getPluginMessageBundle(builder.getClassName(), builder.getResourceBundlePath());
                        if (bundle != null) {
                            map.addAttribute("bundle", bundle);
                        }
                    }
                }
                
                //reuse userview builder message bundle
                inputStream = this.getClass().getClassLoader().getResourceAsStream("ubuilder.properties");
            } else {
                inputStream = this.getClass().getClassLoader().getResourceAsStream(name + ".properties");
            }
            if (inputStream != null) {
                keys.load(inputStream);
                map.addAttribute("name", name);
                map.addAttribute("keys", keys.keys());

                return "console/i18n/lang";
            } else {
                return "error404";
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
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
        return PluginManager.getPluginType();
    }

    protected Map<String, String> getPluginTypeForDefaultProperty() {
        Map<String, String> pluginTypeMap = new ListOrderedMap();
        pluginTypeMap.put("org.joget.plugin.base.AuditTrailPlugin", ResourceBundleUtil.getMessage("setting.plugin.auditTrail"));
        pluginTypeMap.put("org.joget.workflow.model.DeadlinePlugin", ResourceBundleUtil.getMessage("setting.plugin.deadline"));
        pluginTypeMap.put("org.joget.workflow.model.ParticipantPlugin", ResourceBundleUtil.getMessage("setting.plugin.processParticipant"));
        pluginTypeMap.put("org.joget.plugin.base.ApplicationPlugin", ResourceBundleUtil.getMessage("setting.plugin.processTool"));
        pluginTypeMap.put("org.joget.apps.app.model.ProcessFormModifier", ResourceBundleUtil.getMessage("setting.plugin.processFormModifier"));

        return PagingUtils.sortMapByValue(pluginTypeMap, false);
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

        // verify app license
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        //LogUtil.debug(getClass().getName(), "App info: " + consoleWebPlugin.getAppInfo(appId, version));
        return page;
    }
    
    protected Collection<String> validateEmploymentDate (String employeeStartDate, String employeeEndDate) {
        Collection<String> errors = new ArrayList<String> ();
        String format = "yyyy-MM-dd";
        
        //validate start date and end date
        if (!DateUtil.validateDateFormat(employeeStartDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.startDate.invalid"));
        }
        
        if (!DateUtil.validateDateFormat(employeeEndDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.endDate.invalid"));
        }
        
        if (!DateUtil.compare(employeeStartDate, employeeEndDate, format)) {
            errors.add(ResourceBundleUtil.getMessage("console.directory.employment.error.startdate.endDate.compare"));
        }
        
        return errors;
    }
    @RequestMapping(value="/console/app/(*:appId)/(~:version)/userview/(*:userviewId)/screenshot/submit", method = RequestMethod.POST)
    public void consoleUserviewScreenshotSubmit(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "userviewId") String userviewId) throws IOException {

        // validate input
        appId = SecurityUtil.validateStringInput(appId);
        version = SecurityUtil.validateStringInput(version);
        userviewId = SecurityUtil.validateStringInput(userviewId);
        
        // check to ensure that userview is published
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        // get base64 encoded image in POST body
        String imageBase64 = request.getParameter("base64data");
        imageBase64 = imageBase64.substring("data:image/png;base64,".length());
        
        // convert into bytes
        byte[] decodedBytes = Base64.decodeBase64(imageBase64.getBytes());        
        
        // save into image file
        String appVersion = (version != null && !version.isEmpty()) ? appDef.getVersion().toString() : "";
        String filename = SecurityUtil.normalizedFileName(appDef.getId() + "_" + appVersion + "_" + userviewId + ".png");
        String path = SetupManager.getBaseDirectory() + File.separator + "app_screenshots";
        new File(path).mkdirs();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
        File f = new File(path, filename);
        ImageIO.write(image, "png", f);
        
        LogUtil.debug(getClass().getName(), "Created screenshot for userview " + userviewId + " in " + appId);
    }

    @RequestMapping(value="/userview/screenshot/(*:appId)/(*:userviewId)")
    public void consoleUserviewScreenshot(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "userviewId") String userviewId) throws IOException {
        // validate input
        appId = SecurityUtil.validateStringInput(appId);
        version = SecurityUtil.validateStringInput(version);
        userviewId = SecurityUtil.validateStringInput(userviewId);
        
        version = (version != null) ? version : "";
        String filename = SecurityUtil.normalizedFileName(appId + "_" + version + "_" + userviewId + ".png");
        String path = SetupManager.getBaseDirectory() + File.separator + "app_screenshots";
        InputStream imageInput = null;
        OutputStream out = null;
        try {
            File f = new File(path, filename);
            if (!f.exists()) {
                String defaultImage = "images/sampleapp.png";
                imageInput = getClass().getClassLoader().getResourceAsStream(defaultImage);
            } else {
                imageInput = new FileInputStream(f);
            }

            response.setContentType("image/png");
            out = response.getOutputStream();
            byte[] bbuf = new byte[65536];
            DataInputStream in = new DataInputStream(imageInput);
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }
        } finally {
            try {
                imageInput.close();
            } catch(Exception e) {  
                LogUtil.error(getClass().getName(), e, "");
            }
            try {
                out.close();        
            } catch(Exception e) {                
                LogUtil.error(getClass().getName(), e, "");
            }
        }
    }        
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/navigator")
    public String consoleAppNavigator(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<FormDefinition> formDefinitionList = null;
        Collection<DatalistDefinition> datalistDefinitionList = null;
        Collection<UserviewDefinition> userviewDefinitionList = null;

        if (appDef != null) {
            formDefinitionList = formDefinitionDao.getFormDefinitionList(null, appDef, "name", false, null, null);
            datalistDefinitionList = datalistDefinitionDao.getDatalistDefinitionList(null, appDef, "name", false, null, null);
            userviewDefinitionList = userviewDefinitionDao.getUserviewDefinitionList(null, appDef, "name", false, null, null);
        }

        map.addAttribute("appDef", appDef);
        map.addAttribute("tagDef", TaggingUtil.getDefinition(appDef));
        map.addAttribute("formDefinitionList", formDefinitionList);
        map.addAttribute("datalistDefinitionList", datalistDefinitionList);
        map.addAttribute("userviewDefinitionList", userviewDefinitionList);
        
        return "console/apps/navigator";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/customBuilders")
    public String consoleAppCustomBuildersNav(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) {
        AppDefinition appDef = appService.getAppDefinition(appId, version);

        Collection<BuilderDefinition> builderDefinitionList = null;

        if (appDef != null) {
            builderDefinitionList = builderDefinitionDao.getList(appDef, "name", false, null, null);
        }

        map.addAttribute("appDef", appDef);
        map.addAttribute("tagDef", TaggingUtil.getDefinition(appDef));
        map.addAttribute("builderDefinitionList", builderDefinitionList);
        
        Map<String, CustomBuilder> builders = CustomBuilderUtil.getBuilderList();
        map.addAttribute("builders", builders);
        
        return "console/apps/customBuilders";
    }
    
    @RequestMapping({"/desktop","/desktop/home"})
    public String desktopHome() {
        UserviewDefinition defaultUserview = userviewService.getDefaultUserview();
        if (defaultUserview != null && !isBackendLicense()) {
            // redirect to app center userview
            String path = "redirect:/web/userview/" + defaultUserview.getAppId() + "/" + defaultUserview.getId();
            return path;
        } else {
            return "redirect:/web/console/home";
        }
    }

    @RequestMapping("/desktop/apps")
    public String desktopApps(ModelMap model) {
        // get published apps
        Collection<AppDefinition> publishedList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        
        // get app def ids of published apps
        Collection<String> publishedIdSet = new HashSet<String>();
        for (AppDefinition appDef: publishedList) {
            publishedIdSet.add(appDef.getAppId());
        }
        
        // get list of unpublished apps
        Collection<AppDefinition> unpublishedList = new ArrayList<AppDefinition>();
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        for (Iterator<AppDefinition> i=appDefinitionList.iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            if (!publishedIdSet.contains(appDef.getAppId())) {
                unpublishedList.add(appDef);
            }
        }
        model.addAttribute("appDefinitionList", appDefinitionList);
        model.addAttribute("appPublishedList", publishedList);
        model.addAttribute("appUnpublishedList", unpublishedList);
        return "desktop/apps";
    }
    
    @RequestMapping("/desktop/app/import")
    public String desktopAppImport() {
        return "console/apps/import";
    }

    @RequestMapping(value = "/desktop/app/import/submit", method = RequestMethod.POST)
    public String desktopAppImportSubmit(ModelMap map) throws IOException {
        Collection<String> errors = new ArrayList<String>();
        
        MultipartFile appZip = null;
        
        try {
            appZip = FileStore.getFile("appZip");
        } catch (FileLimitException e) {
            errors.add(ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }

        AppDefinition appDef = null;
        if (appZip != null) {
            appDef = appService.importApp(appZip.getBytes());
        }

        if (appDef == null || !errors.isEmpty()) {
            map.addAttribute("error", true);
            map.addAttribute("errorList", errors);
            return "console/apps/import";
        } else {
            String appId = appDef.getAppId();
            String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
            String url = contextPath + "/web/console/app/" + appId + "/builders";
            map.addAttribute("url", url);
            map.addAttribute("appId", appId);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("isPublished", appDef.isPublished());
            return "console/apps/packageUploadSuccess";
        }
    }    

    @RequestMapping({"/desktop/marketplace/app"})
    public String marketplaceApp(ModelMap model, @RequestParam(value = "url") String url) {
        boolean trusted = false;
        String trustedUrlsKey = "appCenter.link.marketplace.trusted";
        String trustedUrls = ResourceBundleUtil.getMessage(trustedUrlsKey);
        if (trustedUrls != null && !trustedUrls.isEmpty()) {
            StringTokenizer st = new StringTokenizer(trustedUrls, ",");
            while (st.hasMoreTokens()) {
                String trustedUrl = st.nextToken().trim();
                if (url.startsWith(trustedUrl)) {
                    trusted = true;
                    break;
                }
            }
        }
        
        if (trusted) {
            model.addAttribute("appUrl", url);
        } else {
            model.addAttribute("appUrl", "");
        }
        
        return "desktop/marketplaceApp";
    }
    
    @RequestMapping({"/json/console/app/(*:appId)/(~:version)/userview/(*:userviewId)/json"})
    public void getUserviewJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "userviewId") String userviewId) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userview == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String userviewJson = userview.getJson();
        writer.write(PropertyUtil.propertiesJsonLoadProcessing(userviewJson));
    }

    @RequestMapping({"/json/console/app/(*:appId)/(~:version)/form/(*:formId)/json"})
    public void getFormJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "formId") String formId) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        if (formDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String formJson = formDef.getJson();
        writer.write(PropertyUtil.propertiesJsonLoadProcessing(formJson));
    }

    @RequestMapping({"/json/console/app/(*:appId)/(~:version)/datalist/(*:datalistId)/json"})
    public void getDatalistJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "datalistId") String datalistId) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DatalistDefinition datalistDef = datalistDefinitionDao.loadById(datalistId, appDef);
        if (datalistDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String datalistJson = datalistDef.getJson();
        writer.write(PropertyUtil.propertiesJsonLoadProcessing(datalistJson));
    }
    
    @RequestMapping("/json/console/locales")
    public void consoleJsonLocaleList(Writer writer) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("data", getSortedLocalList());

        jsonObject.write(writer);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/adminbar/builder/menu")
    public void consoleJsonAdminbarBuilderMenu(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        JSONArray jsonArr = new JSONArray();
        JSONArray elementsArr = null;
        Map obj = null;
        
        String systemSettings = setupManager.getSettingValue("systemTheme");
        String baseUrl = request.getContextPath() + "/web/console/app/"+appDef.getAppId()+"/" + appDef.getVersion();
        
        Map data = new HashMap();
        data.put("value", "form");
        data.put("label", ResourceBundleUtil.getMessage("fbuilder.title"));
        data.put("icon", "fas fa-file-alt");
        data.put("color", "#3f84f4");
        data.put("theme", systemSettings);
        if (appDef.getFormDefinitionList() != null) {
            List<FormDefinition> list = new ArrayList<FormDefinition>();
            list.addAll(appDef.getFormDefinitionList());
            
            Collections.sort(list, new Comparator<FormDefinition>() {

                @Override
                public int compare(FormDefinition o1, FormDefinition o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });
            
            elementsArr = new JSONArray();
            for (FormDefinition e : list) {
                obj = new HashMap();
                obj.put("id", e.getId());
                obj.put("url", baseUrl + "/form/builder/" + e.getId());
                obj.put("label", e.getName());
                obj.put("subLabel", e.getTableName());
                elementsArr.put(obj);
            }
            data.put("elements", elementsArr);
        }
        jsonArr.put(data);
        
        data = new HashMap();
        data.put("value", "datalist");
        data.put("label", ResourceBundleUtil.getMessage("dbuilder.title"));
        data.put("icon", "fas fa-table");
        data.put("color", "#6638b6");
        data.put("theme", systemSettings);
        if (appDef.getDatalistDefinitionList() != null) {
            List<DatalistDefinition> list = new ArrayList<DatalistDefinition>();
            list.addAll(appDef.getDatalistDefinitionList());
            
            Collections.sort(list, new Comparator<DatalistDefinition>() {

                @Override
                public int compare(DatalistDefinition o1, DatalistDefinition o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });
            
            elementsArr = new JSONArray();
            for (DatalistDefinition e : list) {
                obj = new HashMap();
                obj.put("id", e.getId());
                obj.put("url", baseUrl + "/datalist/builder/" + e.getId());
                obj.put("label", e.getName());
                elementsArr.put(obj);
            }
            data.put("elements", elementsArr);
        }
        jsonArr.put(data);
        
        data = new HashMap();
        data.put("value", "userview");
        data.put("label", ResourceBundleUtil.getMessage("ubuilder.title"));
        data.put("icon", "fas fa-desktop");
        data.put("color", "#f3b328");
        // update appPublished after app generation
        data.put("appPublished", Boolean.toString(appDef.isPublished()));
        if (appDef.isPublished()) {
            data.put("published", "<small class=\"published\"> (" + ResourceBundleUtil.getMessage("console.app.common.label.published") + ")</small>");
        }
        data.put("theme", systemSettings);
        if (appDef.getUserviewDefinitionList() != null) {
            List<UserviewDefinition> list = new ArrayList<UserviewDefinition>();
            list.addAll(appDef.getUserviewDefinitionList());
            
            Collections.sort(list, new Comparator<UserviewDefinition>() {

                @Override
                public int compare(UserviewDefinition o1, UserviewDefinition o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });
            
            elementsArr = new JSONArray();
            for (UserviewDefinition e : list) {
                obj = new HashMap();
                obj.put("id", e.getId());
                obj.put("url", baseUrl + "/userview/builder/" + e.getId());
                obj.put("label", e.getName());
                elementsArr.put(obj);
            }
            data.put("elements", elementsArr);
        }
        jsonArr.put(data);
        
        data = new HashMap();
        data.put("value", "process");
        data.put("label", ResourceBundleUtil.getMessage("pbuilder.title"));
        data.put("icon", "fas fa-project-diagram");
        data.put("color", "#dc4438");
        data.put("theme", systemSettings);
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        if (packageDefinition != null) {
            Long packageVersion = packageDefinition.getVersion();
            Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());
            
            List<WorkflowProcess> list = new ArrayList<WorkflowProcess>();
            list.addAll(processList);
            Collections.sort(list, new Comparator<WorkflowProcess>() {

                @Override
                public int compare(WorkflowProcess o1, WorkflowProcess o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });
            
            elementsArr = new JSONArray();
            for (WorkflowProcess p : list) {
                obj = new HashMap();
                obj.put("id", p.getIdWithoutVersion());
                obj.put("url", baseUrl + "/process/builder#" + p.getIdWithoutVersion());
                obj.put("label", p.getName());
                elementsArr.put(obj);
            }
            data.put("elements", elementsArr);
            data.put("theme", systemSettings);
        }
        jsonArr.put(data);
        
        List<BuilderDefinition> list = new ArrayList<BuilderDefinition>();
        if (appDef.getBuilderDefinitionList() != null) {
            list.addAll(appDef.getBuilderDefinitionList());
        }
        Collections.sort(list, new Comparator<BuilderDefinition>() {

            @Override
            public int compare(BuilderDefinition o1, BuilderDefinition o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        
        for (CustomBuilder cb : CustomBuilderUtil.getBuilderList().values()) {
            Map cdata = new HashMap();
            cdata.put("value", cb.getObjectName());
            cdata.put("label", cb.getLabel());
            cdata.put("icon", cb.getIcon());
            cdata.put("color", cb.getColor());
            cdata.put("theme", systemSettings);
            if (!list.isEmpty()) {
                elementsArr = new JSONArray();
                for (BuilderDefinition e : list) {
                    if (cb.getObjectName().equals(e.getType())) {
                        obj = new HashMap();
                        obj.put("id", e.getId());
                        obj.put("url", baseUrl + "/cbuilder/"+cb.getObjectName()+"/design/" + e.getId());
                        obj.put("label", e.getName());
                        elementsArr.put(obj);
                    }
                }
                cdata.put("elements", elementsArr);
            }
            jsonArr.put(cdata);
        }

        jsonArr.write(writer);
    }
    
    protected JSONArray sortJSONArray(JSONArray jsonArr, final String fieldName, final boolean desc) {
        try {
            JSONArray sortedJsonArray = new JSONArray();

            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArr.length(); i++) {
                jsonValues.add(jsonArr.getJSONObject(i));
            }
            Collections.sort( jsonValues, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        valA = (String) a.get(fieldName);
                        valB = (String) b.get(fieldName);
                    } catch (JSONException e) {}

                    if (desc) {
                        return -valA.compareTo(valB);
                    } else {
                        return valA.compareTo(valB);
                    }
                }
            });

            for (int i = 0; i < jsonArr.length(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }
            return sortedJsonArray;
        } catch (Exception e) {
        }
        return jsonArr;
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/dev/submit", method = RequestMethod.POST)
    public void consoleDevSubmit(Writer writer, String id, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "json", required = false) String json) throws JSONException, IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        
        String oldProperties = "{}";  
        Properties props = AppDevUtil.getAppDevProperties(appDef);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", appDef.getAppId());
            jsonObject.put("name", appDef.getName());
            jsonObject.put(WorkflowUserManager.ROLE_ADMIN, props.getProperty(WorkflowUserManager.ROLE_ADMIN));
            jsonObject.put(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP, props.getProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP));
            jsonObject.put("orgId", props.getProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_ORG));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_URI, props.getProperty(AppDevUtil.PROPERTY_GIT_URI));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_USERNAME, props.getProperty(AppDevUtil.PROPERTY_GIT_USERNAME));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_PASSWORD, props.getProperty(AppDevUtil.PROPERTY_GIT_PASSWORD));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_PULL, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_PULL));
            jsonObject.put(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC, props.getProperty(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC));
            oldProperties = jsonObject.toString(4);
        } catch (Exception e) {
            LogUtil.error(ConsoleWebController.class.getName(), e, "");
        }
        
        try {
            //retrieve from request body if the json is send in file
            json = AppUtil.getSubmittedJsonDefinition(json);

            json = PropertyUtil.propertiesJsonStoreProcessing(oldProperties, json);
            Properties appProps = new Properties();
            JSONObject jsonObject = new JSONObject(json);

            if (!jsonObject.isNull(WorkflowUserManager.ROLE_ADMIN)) {
                appProps.setProperty(WorkflowUserManager.ROLE_ADMIN, jsonObject.getString(WorkflowUserManager.ROLE_ADMIN));
            }
            if (!jsonObject.isNull(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP)) {
                appProps.setProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP, jsonObject.getString(EnhancedWorkflowUserManager.ROLE_ADMIN_GROUP));
            }
            if (!jsonObject.isNull("orgId")) {
                appProps.setProperty(EnhancedWorkflowUserManager.ROLE_ADMIN_ORG, jsonObject.getString("orgId"));
            }
            if (!jsonObject.isNull(AppDevUtil.PROPERTY_GIT_URI)) {
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_URI, jsonObject.getString(AppDevUtil.PROPERTY_GIT_URI));
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_USERNAME, jsonObject.getString(AppDevUtil.PROPERTY_GIT_USERNAME));
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_PASSWORD, jsonObject.getString(AppDevUtil.PROPERTY_GIT_PASSWORD));
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT, jsonObject.get(AppDevUtil.PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT).toString());
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_CONFIG_PULL, jsonObject.get(AppDevUtil.PROPERTY_GIT_CONFIG_PULL).toString());
                appProps.setProperty(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC, jsonObject.get(AppDevUtil.PROPERTY_GIT_CONFIG_AUTO_SYNC).toString());
            }
            AppDevUtil.setAppDevProperties(appDef, appProps);
            
            appDef.setName(jsonObject.getString("name"));
            appDefinitionDao.merge(appDef);

            JSONObject result = new JSONObject();
            result.accumulate("success", true);
            result.write(writer);
        } catch (Exception e) {
            JSONObject result = new JSONObject();
            result.accumulate("error", e.getLocalizedMessage());
            result.write(writer);
        }
    }    

    @RequestMapping(value = "/console/profile/subscription", method = RequestMethod.POST)
    public void profileSubscription(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "subscription") String subscription, @RequestParam(value = "deviceId") String deviceId, @RequestParam(value = "appId") String appId, @RequestParam(value = "userviewId") String userviewId) throws IOException {
        // get current user
        User currentUser = userDao.getUser(workflowUserManager.getCurrentUsername());
        if (currentUser == null || workflowUserManager.isCurrentUserAnonymous()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        deviceId = SecurityUtil.validateStringInput(deviceId);
        userviewId = SecurityUtil.validateStringInput(userviewId);
        appId = SecurityUtil.validateStringInput(appId);

        // save subscription json to user metadata
        Boolean result = PushServiceUtil.storeUserPushSubscription(currentUser.getUsername(), deviceId, appId, userviewId, subscription);
        if (result) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "/json/push/message", method = RequestMethod.POST)
    public void sendPushMessage(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "username") String username, @RequestParam(value = "title", required = false) String title, @RequestParam(value = "text", required = false) String text, @RequestParam(value = "url", required = false) String url, @RequestParam(value = "icon", required = false) String icon, @RequestParam(value = "badge", required = false) String badge) throws IOException, JSONException {
        // get  user
        User user = userDao.getUser(username);
        if (user == null || workflowUserManager.isCurrentUserAnonymous()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // send test message
        String message;
        int result = PushServiceUtil.sendUserPushNotification(username, title, text, url, icon, badge, false);
        message = ResourceBundleUtil.getMessage("push.messagesSent") + ": " + result;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        jsonObject.write(response.getWriter());
    }

    @RequestMapping({"/console/app/(*:appId)/(~:version)/logs", "/console/monitor/slogs"})
    public String appLogs(ModelMap map, @RequestParam(required = false) String appId, @RequestParam(required = false) String version) {
        boolean supportMultipleNode = false;
        
        Map<String, String> nodes = new ListOrderedMap();
        String[] tempNodes = ServerUtil.getActiveServerList();
        for (String node : tempNodes) {
            if (node.equals(ServerUtil.getServerName())) {
                nodes.put(node, node + ResourceBundleUtil.getMessage("console.monitor.current"));
            } else {
                nodes.put(node, node);
            }
        }
        Map nodeList = PagingUtils.sortMapByValue(nodes, false);
        map.addAttribute("nodes", nodeList);
        map.addAttribute("currentNode", ServerUtil.getServerName());

        if (tempNodes.length > 1) {
            supportMultipleNode = true;
        }
        map.addAttribute("supportMultipleNode", supportMultipleNode);
        
        if (appId != null) {
            String result = checkVersionExist(map, appId, version);
            boolean protectedReadonly = false;
            if (result != null) {
                protectedReadonly = result.contains("status=invalidLicensor");
                if (!protectedReadonly) {
                    return result;
                }
            }

            AppDefinition appDef = appService.getAppDefinition(appId, version);
            checkAppPublishedVersion(appDef);
            map.addAttribute("appId", appDef.getId());
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("protectedReadonly", protectedReadonly);

            return "console/apps/logViewer";
        } else {
            map.addAttribute("appId", LogViewerAppender.CONSOLE_LOG);
            return "console/monitor/systemLog";
        }
    }

    @RequestMapping(value = "/json/log/broadcast", method = RequestMethod.POST)
    public void broadcast(HttpServletRequest httpRequest, Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "profile") String profile, @RequestParam(value = "node") String node) {
        if (profile != null) {
            try {
                HostManager.setCurrentProfile(profile);
                
                Setting setting = setupManager.getSettingByProperty(node + "LogToken");
                if (setting != null) {
                    String httpToken = httpRequest.getHeader("token");
                    //validate token
                    if (setting.getValue().equals(httpToken)){
                        LogViewerAppender.broadcast(appId, IOUtils.toString(httpRequest.getReader()), node);
                    }
                }
            } catch (Exception e) {
                //ignore it
            } finally {
                HostManager.resetProfile();
            }
        }
    }
}
