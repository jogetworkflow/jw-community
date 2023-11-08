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
import org.joget.commons.spring.web.CustomContextLoaderListener;
import org.joget.commons.spring.web.CustomDispatcherServlet;
import org.joget.commons.util.DatabaseUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
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
                        // ignore
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
                con.commit();
                
                if (!exists) {
                    // get schema file
                    String schemaFile = null;
                    if ("oracle".equals(dbType) || jdbcUrl.contains("oracle")) {
                        schemaFile = "/setup/sql/jwdb-oracle.sql";
                    } else if ("sqlserver".equals(dbType) || jdbcUrl.contains("sqlserver")) {
                        schemaFile = "/setup/sql/jwdb-mssql.sql";
                    } else if ("mysql".equals(dbType) || jdbcUrl.contains("mysql")) {
                        schemaFile = "/setup/sql/jwdb-mysql.sql";
                    } else if ("postgresql".equals(dbType) || jdbcUrl.contains("postgresql")) {
                        schemaFile = "/setup/sql/jwdb-postgres.sql";
                    } else {
                        throw new SQLException("Unrecognized database type, please setup the datasource manually");
                    }

                    if (dbName != null && stmt != null) {
                        // create database
                        con.setAutoCommit(true);
                        try (Statement stmt2 = con.createStatement()) {
                            LogUtil.info(getClass().getName(), "Create database " + dbName);
                            stmt2.executeUpdate("CREATE DATABASE " + dbName);
                        } catch (SQLException ex) {
                            // ignore
                        }

                        // switch database
                        LogUtil.info(getClass().getName(), "Use database " + dbName);
                        con.setCatalog(dbName);
                        con.setAutoCommit(false);
                    }
                    
                    // execute schema file
                    LogUtil.info(getClass().getName(), "Execute schema " + schemaFile);
                    ScriptRunner runner = new ScriptRunner(con, false, true);
                    in = getClass().getResourceAsStream(schemaFile);
                    runner.runScript(new BufferedReader(new InputStreamReader(in)));
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
                
                //run collation check before servlet init for mysql, else imported apps form data tables are with previous collation.
                DatabaseUtil.checkAndFixMySqlDbCollation(con);
                
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
                    
                    WorkflowUserManager wum = (WorkflowUserManager) context.getBean("workflowUserManager");
                    wum.setSystemThreadUser(true);
                    
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
                        
                        wum.setSystemThreadUser(false);
                    }
                }                
                
                LogUtil.info(getClass().getName(), "Profile init complete: " + profileName);
                LogUtil.info(getClass().getName(), "===== Database Setup Complete =====");
                
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
                //import form data
                appService.importFormData(fileContent);
                
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
