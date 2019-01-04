package org.joget.apm;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;

public class APMUtil {
    public static Boolean isGlowroot = null;
    
    protected static Object dataSource = null;
    protected static Object rollupLevelService = null;
    protected static Object transactionRegistry = null;
    
    protected static void initUtilObjects(){
        if (dataSource == null) {
            try {
                Object embededGlowrootAgentInit = FieldUtils.readDeclaredStaticField(Class.forName("org.glowroot.agent.MainEntryPoint"), "glowrootAgentInit", true);
                Object embeddedAgentModule = FieldUtils.readDeclaredField(embededGlowrootAgentInit, "embeddedAgentModule", true);
                Object agentModule = FieldUtils.readDeclaredField(embeddedAgentModule, "agentModule", true);
                Object simpleRepoModule = FieldUtils.readDeclaredField(embeddedAgentModule, "simpleRepoModule", true);
                rollupLevelService = MethodUtils.invokeExactMethod(simpleRepoModule, "getRollupLevelService", new Object[0]);
                dataSource = FieldUtils.readDeclaredField(simpleRepoModule, "dataSource", true);
                transactionRegistry = FieldUtils.readDeclaredField(agentModule, "transactionRegistry", true);
            } catch (Exception e) {
                LogUtil.error(APMUtil.class.getName(), e, "");
            }
        }
    }
    
    protected static int getRollupLevelForView(long from, long to) throws Exception  {
        Class[] classes = rollupLevelService.getClass().getDeclaredClasses();
        Class dataKind = null;
        for (Class c : classes) {
            if (c.getName().contains("DataKind")) {
                dataKind = c;
                break;
            }
        }
        if (dataKind != null) {
            return (int) MethodUtils.invokeMethod(rollupLevelService, "getRollupLevelForView", new Object[]{from, to, dataKind.getEnumConstants()[0]});
        } else {
            return 0;
        }
    }
    
    protected static void updateData(Map<String, Map<String, Object>> data, String transactionName, double totalDurationNanos, long transactionCount, long errorCount, long slowTrace) {
        Map<String, Object> td = data.get(transactionName);
        if (td == null) {
            td = new HashMap<String, Object>();
            td.put("transactionName", transactionName);
            td.put("totalDurationNanos", Double.valueOf(0));
            td.put("transactionCount", Long.valueOf(0));
            td.put("errorCount", Long.valueOf(0));
            td.put("slowTrace", Long.valueOf(0));
            data.put(transactionName, td);
        } 
        td.put("totalDurationNanos", ((Double) td.get("totalDurationNanos")) + totalDurationNanos);
        td.put("transactionCount", ((Long) td.get("transactionCount")) + transactionCount);
        td.put("errorCount", ((Long) td.get("errorCount")) + errorCount);
        td.put("slowTrace", ((Long) td.get("errorCount")) + slowTrace);
    }
    
    public static long querySummariesInfo(String servername, String appId, long from, long to, int level, Map<String, Map<String, Object>> data) throws Exception {
        Object lock = FieldUtils.readDeclaredField(dataSource, "lock", true);
        long lastRolledUpTime = 0;
        synchronized (lock) {
            boolean closed = (boolean) FieldUtils.readDeclaredField(dataSource, "closed", true);
            if (closed) {
                return 0;
            }
            MethodUtils.invokeMethod(dataSource, true, "checkConnectionUnderLock");
            Object preparedStatement = MethodUtils.invokeMethod(dataSource, true, "prepareStatementUnderLock", new Object[]{getQuery(level), 60});
            ResultSet resultSet = null;
            try {
                MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{1, "Web"});
                MethodUtils.invokeMethod(preparedStatement, "setLong", new Object[]{2, from});
                MethodUtils.invokeMethod(preparedStatement, "setLong", new Object[]{3, to});
                
                if (appId != null && !appId.isEmpty()) {
                    MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{4, "http%://" + servername + "%/userview/"+appId+"/%"});
                } else {
                    MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{4, "http%://" + servername + "%"});
                }
                
                resultSet = (ResultSet) MethodUtils.invokeMethod(preparedStatement, "executeQuery");
                
                while (resultSet.next()) {
                    updateData(data, resultSet.getString(1), resultSet.getDouble(2), resultSet.getLong(3), resultSet.getLong(4), 0);
                    lastRolledUpTime = Math.max(lastRolledUpTime, resultSet.getLong(5));
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
        return lastRolledUpTime;
    }
    
    public static void querySlowTraces(String servername, String appId, long from, long to, Map<String, Map<String, Object>> data) throws Exception {
        Object lock = FieldUtils.readDeclaredField(dataSource, "lock", true);
        synchronized (lock) {
            boolean closed = (boolean) FieldUtils.readDeclaredField(dataSource, "closed", true);
            if (closed) {
                return;
            }
            MethodUtils.invokeMethod(dataSource, true, "checkConnectionUnderLock");
            Object preparedStatement = MethodUtils.invokeMethod(dataSource, true, "prepareStatementUnderLock", new Object[]{getTraceQuery(), 60});
            ResultSet resultSet = null;
            try {
                MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{1, "Web"});
                MethodUtils.invokeMethod(preparedStatement, "setLong", new Object[]{2, from});
                MethodUtils.invokeMethod(preparedStatement, "setLong", new Object[]{3, to});
                
                if (appId != null && !appId.isEmpty()) {
                    MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{4, "http%://" + servername + "%/userview/"+appId+"/%"});
                } else {
                    MethodUtils.invokeMethod(preparedStatement, "setString", new Object[]{4, "http%://" + servername + "%"});
                }
                
                resultSet = (ResultSet) MethodUtils.invokeMethod(preparedStatement, "executeQuery");
                
                while (resultSet.next()) {
                    updateData(data, resultSet.getString(1), 0, 0, 0, resultSet.getLong(2));
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        }
    }
    
    public static String getSummaries(String servername, String appId, long from, long to) {
        try {
            initUtilObjects();
            int level = getRollupLevelForView(from, to);
            
            Map<String, Map<String, Object>> data = new HashMap<String, Map<String, Object>>();
            long revisedFrom = from;
            long revisedTo = to;
            
            for (int rollupLevel = level; rollupLevel >= 0; rollupLevel--) {
                long lastRolledUpTime = querySummariesInfo(servername, appId, revisedFrom, revisedTo, rollupLevel, data);
                revisedFrom = Math.max(revisedFrom, lastRolledUpTime + 1);
                if (revisedFrom > revisedTo) {
                    break;
                }
            }
            
            querySlowTraces(servername, appId, from, to, data);
            
            JSONArray jarr = new JSONArray();
            for (String key : data.keySet()) {
                jarr.put(data.get(key));
            }
            return jarr.toString();
        } catch (Exception e) {
            LogUtil.error(APMUtil.class.getName(), e, "");
        }
        return "[]";
    }
    
    public static String getQuery(int level) {
        String sql = "select transaction_name, sum(total_duration_nanos), sum(transaction_count),";
        sql += " sum(error_count), max(capture_time) from aggregate_tn_rollup_" + level;
        sql += " where transaction_type = ? and capture_time > ? and capture_time <= ?";
        sql += " and transaction_name like ?";
        sql += " group by transaction_name";
        
        return sql;
    }
    
    public static String getTraceQuery() {
        String sql = "select transaction_name, count(id) from trace";
        sql += " where transaction_type = ? and capture_time > ? and capture_time <= ?";
        sql += " and transaction_name like ?";
        sql += " group by transaction_name";
        
        return sql;
    }
    
    public static Boolean isGlowrootAvailable() {
        if (isGlowroot == null) {
            try {
                Class.forName("org.glowroot.agent.api.Glowroot");
                isGlowroot = true;
            } catch (ClassNotFoundException e) {
                isGlowroot = false;
            }
        }
        return isGlowroot;
    }
    
    public static void setTransactionName(String name, Integer priority) {
        if (isGlowrootAvailable()) {
            initUtilObjects();
            try {
                if (priority == null) {
                    priority = 1000;
                }
                //rewrite glowroot transaction name if glowroot available
                Object transaction = MethodUtils.invokeMethod(transactionRegistry, true, "getCurrentTransaction");
                if (transaction != null) {
                    MethodUtils.invokeMethod(transaction, true, "setTransactionName", new Object[]{name, priority});
                }
            } catch (Exception e) {}
        }
    }
}
