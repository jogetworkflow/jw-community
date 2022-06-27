package org.joget.governance.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.governance.model.GovHealthCheckAbstract;
import org.joget.governance.model.GovHealthCheckResult;

public class ProcessDataIntegrityCheck extends GovHealthCheckAbstract {

    @Override
    public String getName() {
        return "ProcessDataIntegrityCheck";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return "Process Data Integrity";
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
        return ResourceBundleUtil.getMessage("governance.qualityAssurance");
    }

    @Override
    public String getSortPriority() {
        return "2";
    }

    @Override
    public GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult) {
        GovHealthCheckResult result = new GovHealthCheckResult();
        result.setStatus(GovHealthCheckResult.Status.PASS);
        result.setSuppressable(true);
        
        DataSource ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");

        checkOid(result, ds);
        checkUsers(result, ds);
        
        return result;
    }
    
    private void checkOid(GovHealthCheckResult result, DataSource ds) {
        String[] names = new String[]{"SHKActivities", "SHKActivityData", "SHKAndJoinTable", "SHKAssignmentsTable", "SHKCounters", "SHKDeadlines", 
            "SHKNextXPDLVersions", "SHKProcessData", "SHKProcessDefinitions", "SHKProcessRequesters", "SHKProcesses", "SHKResourcesTable", "SHKXPDLData", "SHKXPDLS"};
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            
            long nextoid = 0l;
            long minNextoid = 0l;
            pstmt = con.prepareStatement("select nextoid from objectid");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                nextoid = rs.getLong(1);
            }
            
            for (String name : names) {
                pstmt = con.prepareStatement("select max(oid) from " + name);
                rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    long temp = rs.getLong(1);
                    if (temp > minNextoid) {
                        minNextoid =  temp;
                    }
                }
            }
            
            if (nextoid < minNextoid) {
                result.setStatus(GovHealthCheckResult.Status.FAIL);
                result.addDetail(ResourceBundleUtil.getMessage("processDataCheck.oid", new String[]{Long.toString(minNextoid + 1)}));
            }
            
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
    }
    
    private void checkUsers(GovHealthCheckResult result, DataSource ds) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = ds.getConnection();
            
            Map<String, String> usernames = new HashMap<String, String>();
            pstmt = con.prepareStatement("select Username from SHKResourcesTable");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                usernames.put(rs.getString(1).toLowerCase(), rs.getString(1));
            }
            
            Map<String, User> users = DirectoryUtil.getUsersMap();
            for (String username : users.keySet()) {
                if (usernames.containsKey(username.toLowerCase())) {
                    String compare = usernames.get(username.toLowerCase());
                    String actual = users.get(username).getUsername();
                    if (!compare.equals(actual)) {
                        result.setStatus(GovHealthCheckResult.Status.FAIL);
                        result.addDetail(ResourceBundleUtil.getMessage("processDataCheck.user", new String[]{actual}));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
    }
    
    private Collection<String> getSharkUsers() {
        Collection<String> users = new ArrayList<String>();
        
        return users;
    }
}