package org.joget.governance.lib;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.RegexMatchesFunctionResolver;
import org.joget.apps.form.dao.FormDataDaoImpl;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class OrphanedFormDataCheck extends GovHealthCheckAbstract {

    @Override
    public String getName() {
        return "OrphanedFormDataCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Orphaned Form Data";
    }

    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String getCategory() {
        return ResourceBundleUtil.getMessage("governance.security");
    }

    @Override
    public String getSortPriority() {
        return "5";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setSuppressable(true);
        
        Set<String> tables = getTables();
        
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        Collection<AppDefinition> latestAppDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        
        for (AppDefinition appDef : appDefinitionList) {
            if (appDef.getFormDefinitionList() != null) {
                for (FormDefinition f : appDef.getFormDefinitionList()) {
                    if (tables.contains(f.getTableName())) {
                        tables.remove(f.getTableName());
                    }
                }
            }
        }
        
        for (AppDefinition appDef : latestAppDefinitionList) {
            if (appDef.getFormDefinitionList() != null) {
                for (FormDefinition f : appDef.getFormDefinitionList()) {
                    if (tables.contains(f.getTableName())) {
                        tables.remove(f.getTableName());
                    }
                }
            }
        }
        
        if (!tables.isEmpty()) {
            Set<String> checked = new HashSet<String>();
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setXPathFunctionResolver(new RegexMatchesFunctionResolver());
            
            //find usages
            for (AppDefinition appDef : appDefinitionList) {
                checkUsages(appDef, tables, appService, xpath);
                checked.add(appDef.getAppId() + "::" + appDef.getVersion());
            }
            
            for (AppDefinition appDef : latestAppDefinitionList) {
                if (!checked.contains(appDef.getAppId() + "::" + appDef.getVersion())) {
                    checkUsages(appDef, tables, appService, xpath);
                    checked.add(appDef.getAppId() + "::" + appDef.getVersion());
                }
            }
        }
        
        if (!tables.isEmpty()) {
            result.setStatus(GovHealthCheckResult.Status.WARN);
            String list = "<ol><li>" + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + StringUtils.join(tables, "</li><li>" + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME) + "</li></ol>";
            result.addDetail(ResourceBundleUtil.getMessage("orphanedFormDataCheck.warn", new String[]{list}));
        } else {
            result.setStatus(GovHealthCheckResult.Status.PASS);
        }
        
        return result;
    }
    
    protected void checkUsages(AppDefinition appDef, Set<String> tables, AppService appService, XPath xpath) {
        if (!tables.isEmpty()) {
            Set<String> temp = new HashSet<String>();
            temp.addAll(tables);
            byte[] defXml = appService.getAppDefinitionXml(appDef, false);
            
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                ByteArrayInputStream input =  new ByteArrayInputStream(defXml);
                Document xml = builder.parse(input);
                
                for (String keyword : temp) {
                    String expression = "//json[contains(text(),'\""+keyword+"\"')]";
                    expression += " | //pluginProperties[contains(text(),'\""+keyword+"\"')] ";

                    //Hash variable
                    String regex = ".*#[^#]+\\."+StringUtil.escapeRegex(keyword)+"([\\}?\\.\\[]+[^#]*)*#.*";
                    expression += " | //json[jfn:regexmatches(text(),'"+regex+"')]";
                    expression += " | //pluginProperties[jfn:regexmatches(text(),'"+regex+"')] ";
                    
                    //handle for beanshell
                    expression += " | //json[contains(text(),'\""+keyword+"\\\"')]";
                    expression += " | //pluginProperties[contains(text(),'\""+keyword+"\\\"')] ";

                    //handle for jdbc query
                    expression += " | //json[contains(text(),'app_fd_"+keyword+"')]";
                    expression += " | //pluginProperties[contains(text(),'app_fd_"+keyword+"')] ";

                    //handle for form hash variable
                    expression += " | //json[contains(text(),'form."+keyword+".')]";
                    expression += " | //pluginProperties[contains(text(),'form."+keyword+".')] ";
                    
                    NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(xml, XPathConstants.NODESET);
                    if (nodeList.getLength() > 0) {
                        tables.remove(keyword);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
    }
    
    protected Set<String> getTables() {
        Set<String> tables = new HashSet<String>();
        
        DataSource ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection con = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            DatabaseMetaData md = con.getMetaData();
            rs = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tablesName = rs.getString(3);
                if (tablesName.startsWith(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME)) {
                    tables.add(tablesName.substring(7));
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch(Exception e) {
            }  
            try {
                if (con != null) {
                    con.close();
                }
            } catch(Exception e) {
            }
        }
        
        return tables;
    }
    
}