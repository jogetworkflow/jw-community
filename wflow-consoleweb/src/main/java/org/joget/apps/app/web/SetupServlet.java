package org.joget.apps.app.web;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.spring.web.CustomContextLoaderListener;
import org.joget.commons.spring.web.CustomDispatcherServlet;
import org.joget.commons.util.*;
import org.kecak.apps.scheduler.SchedulerManager;
import org.kecak.apps.scheduler.SchedulerPluginJob;
import org.kecak.apps.scheduler.model.SchedulerDetails;
import org.kecak.apps.scheduler.model.TriggerTypes;
import org.kecak.commons.security.SecureDataEncryptionImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * Servlet to handle first-time database setup and initialization.
 */
public class SetupServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // check for existing setup, return 403 forbidden if already configured (currentProfile key exists in app_datasource.properties) or virtual hosting is enabled
        Properties properties = DynamicDataSourceManager.loadProfileProperties();
        if (HostManager.isVirtualHostEnabled() || properties == null || properties.containsKey("currentProfile")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // handle request
        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            // forward to the setup JSP
            request.getRequestDispatcher("/WEB-INF/jsp/setup/setup.jsp").forward(request, response);
        } else if ("/init".equals(path)) {
            // only allow POST for initialization request
            if (!"post".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            // get parameters
            String jdbcDriver = request.getParameter("jdbcDriver");
            String jdbcUrl = request.getParameter("jdbcUrl");
            String jdbcFullUrl = request.getParameter("jdbcFullUrl");
            String jdbcUser = request.getParameter("jdbcUser");
            String jdbcPassword = request.getParameter("jdbcPassword");
            String dbType = request.getParameter("dbType");
            String dbName = request.getParameter("dbName");
            String sampleApps = request.getParameter("sampleApps");
            String sampleUsers = request.getParameter("sampleUsers");
            if ("custom".equals(dbType)) {
                dbName = null;
            }

            // validate dbName
            if (dbName != null) {
                dbName = SecurityUtil.validateStringInput(dbName);
            }
            
            // create datasource
            LogUtil.info(getClass().getName(), "===== Starting Database Setup =====");
            boolean success = false;
            String message = "";
            Connection con = null;
            Statement stmt = null;
            ResultSet rs = null;
            InputStream in = null;
            InputStream inQuartz = null;
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(jdbcDriver);
            ds.setUrl(jdbcUrl);
            ds.setUsername(jdbcUser);
            ds.setPassword(jdbcPassword);
            try {
                // test connection
                con = ds.getConnection();
                con.setAutoCommit(false);
                success = true;
                message = ResourceBundleUtil.getMessage("setup.datasource.label.success");

                // switch database
                if (dbName != null) {
                    try {
                        LogUtil.info(getClass().getName(), "Use database " + dbName);
                        con.setCatalog(dbName);
                    } catch (SQLException ex) {
                        LogUtil.error(getClass().getName(), ex, ex.getMessage());
                    }
                }

                // check for existing tables
                boolean exists = false;
                try {
                    stmt = con.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM dir_role");
                    if (rs.next()) {
                        exists = true;
                        LogUtil.info(getClass().getName(), "Database already initialized " + jdbcUrl);
                    }
                } catch (SQLException ex) {
                    LogUtil.info(getClass().getName(), "Database not yet initialized " + jdbcUrl);
                }
                
                if (!exists) {
                    // get schema file
                    String schemaFile = null;
                    String quartzSchemaFile;
                    if ("oracle".equals(dbType) || jdbcUrl.contains("oracle")) {
                        schemaFile = "/setup/sql/jwdb-oracle.sql";
                        quartzSchemaFile = "/setup/sql/quartz-oracle.sql";
                    } else if ("sqlserver".equals(dbType) || jdbcUrl.contains("sqlserver")) {
                        schemaFile = "/setup/sql/jwdb-mssql.sql";
                        quartzSchemaFile = "/setup/sql/quartz-mssql.sql";
                    } else if ("mysql".equals(dbType) || jdbcUrl.contains("mysql")) {
                        schemaFile = "/setup/sql/jwdb-mysql.sql";
                        quartzSchemaFile = "/setup/sql/quartz-mysql.sql";
                    } else {
                        throw new SQLException("Unrecognized database type, please setup the datasource manually");
                    }

                    if (dbName != null && stmt != null) {
                        // create database
                        try {
                            LogUtil.info(getClass().getName(), "Create database " + dbName);
                            stmt.executeUpdate("CREATE DATABASE " + dbName);
                        } catch (SQLException ex) {
                            // ignore
                        }

                        // switch database
                        LogUtil.info(getClass().getName(), "Use database " + dbName);
                        con.setCatalog(dbName);
                    }
                    
                    // execute schema file
                    LogUtil.info(getClass().getName(), "Execute schema " + schemaFile);
                    ScriptRunner runner = new ScriptRunner(con, false, true);
                    in = getClass().getResourceAsStream(schemaFile);
                    inQuartz = getClass().getResourceAsStream(quartzSchemaFile);
                    runner.runScript(new BufferedReader(new InputStreamReader(in)));
                    runner.runScript(new BufferedReader(new InputStreamReader(inQuartz)));
                }
                if ("true".equals(sampleUsers)) {
                    // create users
                    String schemaFile = "/setup/sql/jwdb-users.sql";
                    LogUtil.info(getClass().getName(), "Create users using schema " + schemaFile);
                    ScriptRunner runner = new ScriptRunner(con, false, true);
                    in = getClass().getResourceAsStream(schemaFile);
                    runner.runScript(new BufferedReader(new InputStreamReader(in)));
                }
                
                con.commit();
                LogUtil.info(getClass().getName(), "Datasource init complete: " + success);
                
                // save profile
                String profileName = (dbName != null && !dbName.trim().isEmpty()) ? dbName : "custom";
                String jdbcUrlToSave = (jdbcFullUrl != null && !jdbcFullUrl.trim().isEmpty()) ? jdbcFullUrl : jdbcUrl;
                LogUtil.info(getClass().getName(), "Save profile " + profileName);
                DynamicDataSourceManager.createProfile(profileName);
                DynamicDataSourceManager.changeProfile(profileName);
                DynamicDataSourceManager.writeProperty("workflowDriver", jdbcDriver);
                DynamicDataSourceManager.writeProperty("workflowUrl", jdbcUrlToSave);
                DynamicDataSourceManager.writeProperty("workflowUser", jdbcUser);
                DynamicDataSourceManager.writeProperty("workflowPassword", jdbcPassword);
                
                // initialize spring application context
                ServletContext sc = request.getServletContext();
                ServletContextEvent sce = new ServletContextEvent(sc);
                CustomContextLoaderListener cll = new CustomContextLoaderListener();
                cll.contextInitialized(sce);
                DispatcherServlet servlet = CustomDispatcherServlet.getCustomDispatcherServlet();
                // reset webApplicationContext field
                Field wacField = FrameworkServlet.class.getDeclaredField("webApplicationContext");
                wacField.setAccessible(true);
                wacField.set(servlet, null);
                // reinitialize DispatcherServlet
                servlet.init();
                
                if (sampleApps != null) {
                    // import sample apps
                    ApplicationContext context = AppUtil.getApplicationContext();
                    InputStream setupInput = getClass().getResourceAsStream("/setup/setup.properties");
                    Properties setupProps = new Properties();
                    try {
                        setupProps.load(setupInput);
                        String sampleDelimitedApps = setupProps.getProperty("sample.apps");
                        StringTokenizer appTokenizer = new StringTokenizer(sampleDelimitedApps, ",");
                        while (appTokenizer.hasMoreTokens()) {
                            String appPath = appTokenizer.nextToken();
                            importApp(context, appPath);
                        }
                    } finally {
                        if (setupInput != null) {
                            setupInput.close();
                        }
                    }
                }                
                
                LogUtil.info(getClass().getName(), "Profile init complete: " + profileName);
                LogUtil.info(getClass().getName(), "===== Database Setup Complete =====");

                //Initialize security
                final SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
                {
                    final Setting setting = new Setting();
                    setting.setProperty(SecureDataEncryptionImpl.PROPERTY_SETUP_SECURITY_SALT);
                    setting.setValue(SecurityUtil.generateRandomString(32, true, true, true, false, true));
                    setupManager.saveSetting(setting);
                }

                {
                    final Setting setting = new Setting();
                    setting.setProperty(SecureDataEncryptionImpl.PROPERTY_SETUP_SECURITY_KEY);
                    setting.setValue(SecurityUtil.generateRandomString(16, true, true, true, false, true));
                    setupManager.saveSetting(setting);
                }

                LogUtil.info(getClass().getName(), "===== Security Setup Complete =====");

                //Initialize Scheduler Job
                SchedulerDetails schedulerDetails = new SchedulerDetails();
                schedulerDetails.setJobName("SchedulerPluginJob");
                schedulerDetails.setJobClassName(SchedulerPluginJob.class.getName());
//                    schedulerDetails.setCronExpression("0 0/1 * * * ? *"); // run every 1 minute
                schedulerDetails.setCronExpression("0 0/5 * * * ? *"); // run every 5 minutes
                schedulerDetails.setGroupJobName("SchedulerPluginJob");
                schedulerDetails.setGroupTriggerName("SchedulerPluginJob");
                Date now = new Date();
                schedulerDetails.setDateCreated(now);
                schedulerDetails.setCreatedBy("admin");
                schedulerDetails.setDateModified(now);
                schedulerDetails.setModifiedBy("admin");
                schedulerDetails.setTriggerTypes(TriggerTypes.CRON);
                schedulerDetails.setTriggerName("SchedulerPluginJob");
                SchedulerManager schedulerManager = (SchedulerManager) AppUtil.getApplicationContext().getBean("schedulerManager");
                schedulerManager.saveOrUpdateJobDetails(schedulerDetails);

                LogUtil.info(getClass().getName(), "===== Scheduler Setup Complete =====");

            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), null, ex.toString());
                success = false;
                message = ex.getMessage().replace("'", " ");
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
                try {
                    ds.close();
                } catch (SQLException ex) {
                    // ignore
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
                if(inQuartz != null) {
                    try {
                        inQuartz.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }

            // send result
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                out.println("{\"action\":\"init\",\"result\":\"" + success + "\",\"message\":\"" + StringEscapeUtils.escapeJavaScript(message) + "\"}");
            } finally {
                out.close();
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Import an app from a specified path.
     * @param context
     * @param path
     */
    protected void importApp(ApplicationContext context, String path) {
        LogUtil.info(getClass().getName(), "Import app " + path);
        InputStream in = null;
        try {
            final AppService appService = (AppService)context.getBean("appService");
            in = getClass().getResourceAsStream(path);
            byte[] fileContent = readInputStream(in);
            final AppDefinition appDef = appService.importApp(fileContent);
            if (appDef != null) {
                TransactionTemplate transactionTemplate = (TransactionTemplate) AppUtil.getApplicationContext().getBean("transactionTemplate");
                transactionTemplate.execute(new TransactionCallback<Object>() {
                    public Object doInTransaction(TransactionStatus ts) {
                        appService.publishApp(appDef.getId(), null);
                        return null;
                    }
                });
            }
        } catch(Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Failed to import app " + path);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException e) {                
            }
        }
    }    

    /**
     * Reads a specified InputStream, returning its contents in a byte array
     * @param in
     * @return
     * @throws IOException 
     */
    protected byte[] readInputStream(InputStream in) throws IOException {
        byte[] fileContent;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            BufferedInputStream bin = new BufferedInputStream(in);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = bin.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
            fileContent = out.toByteArray();
            return fileContent;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        }
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet to handle first-time database setup and initialization";
    }

}
