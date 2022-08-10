package org.joget.commons.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.joget.commons.spring.model.Setting;

public class ServerUtil {
    public static final String SYSTEM_PROPERTY_NODE_NAME = "wflow.name";
    
    protected static String serverName = null;
    protected static Map<String, Runnable> cleaningTasks = new HashMap<String, Runnable>();
    protected static Map<String, Runnable> allServersCleaningTasks = new HashMap<String, Runnable>();
    protected static final String CLUSTER_PREFIX = "node::";
    
    public static void addServerShutdownCleaningTask(String name, Runnable runnable) {
        if (!cleaningTasks.containsKey(name)) {
            cleaningTasks.put(name, runnable);
        }
    }
    
    public static void addAllServersShutdownCleaningTask(String name, Runnable runnable) {
        if (!allServersCleaningTasks.containsKey(name)) {
            allServersCleaningTasks.put(name, runnable);
        }
    }
    
    public static void registerServer() {
        writeServer();
    }
    
    public static void unregisterServer() {
        writeServer();
    }
    
    public static String getServerName() {
        if (serverName == null) {
            serverName = System.getProperty(SYSTEM_PROPERTY_NODE_NAME);
            if (serverName == null) {
                serverName = "";
            }
        }
        return serverName;
    }
    
    public static String[] getServerList() {
        Set<String> servers = new HashSet<String>();
        Gson gson = new Gson();
        
        String serverFilePath = SetupManager.getBaseSharedDirectory() + "/servers.json";
        
        try {
            String serverJson = FileUtils.readFileToString(new File(serverFilePath), "UTF-8");
            servers = gson.fromJson(serverJson, new TypeToken<Set<String>>(){}.getType());
        } catch (Exception e) {
            LogUtil.debug(ServerUtil.class.getName(), "Error read servers file: " + e.getMessage());
        }
        
        if (servers == null) {
            servers = new HashSet<String>();
        }
        
        return servers.toArray(new String[0]);
    }
    
    //read ip address from serversIP.txt
    public static String getIPAddress(String server) {
        File addressFile = new File(SetupManager.getBaseSharedDirectory() + "/serversIP.txt");
        String ipAddr = "";
        try {
            ipAddr = FileUtils.readFileToString(new File(addressFile.getPath()), "UTF-8");
        } catch (Exception e) {
            LogUtil.debug(ServerUtil.class.getName(), "Error read servers file: " + e.getMessage());
        }
        
        String[] serverIps = ipAddr.split(",");
        for(String ip: serverIps){
            if(ip.contains(server)){
                ip = ip.substring(server.length()+1);
                return ip;
            }
        }
        return "";
    }
    
    //delete ip address from serversIP.txt & servers.json
    public static void deleteNode(String server) {
        //delete server IP in serversIP.txt
        File serversIPFile = new File(SetupManager.getBaseSharedDirectory() + "/serversIP.txt");
        if (Files.exists(serversIPFile.toPath())) {
            String ipAddr = "";
            String newIpAddr = "";
            try {
                ipAddr = FileUtils.readFileToString(new File(serversIPFile.getPath()), "UTF-8");
                String[] serverIps = ipAddr.split(",");
                if (ipAddr.contains(server)) {
                    for (String ip : serverIps) {
                        if (!ip.contains(server)) {
                            if (!newIpAddr.isEmpty()) {
                                newIpAddr += ",";
                            }
                            newIpAddr += ip;
                        }
                    }
                    FileUtils.writeStringToFile(new File(serversIPFile.getPath()), newIpAddr, "UTF-8");
                }
            } catch (Exception e) {
                LogUtil.debug(ServerUtil.class.getName(), "Error read serversIP file: " + e.getMessage());
            }
        }
        //delete server in server.json
        Set<String> servers = new HashSet<String>();
        Gson gson = new Gson();
        String serverFilePath = SetupManager.getBaseSharedDirectory() + "/servers.json";
        if (Files.exists(serversIPFile.toPath())) {
            try {
                String serverJson = FileUtils.readFileToString(new File(serverFilePath), "UTF-8");
                servers = gson.fromJson(serverJson, new TypeToken<Set<String>>() {
                }.getType());
                if (servers.contains(server)) {
                    servers.remove(server);
                    serverJson = gson.toJson(servers);
                    FileUtils.writeStringToFile(new File(serverFilePath), serverJson, "UTF-8");
                }
            } catch (Exception e) {
                LogUtil.debug(ServerUtil.class.getName(), "Error read servers file: " + e.getMessage());
            }
        }
    }

    /**
     * Get list of active cluster node
     * allowed nodes
     * @return 
     */
    public static String[] getActiveServerList() {
        List<String> nodeList = new ArrayList<String>();
        try {
            // get registered nodes
            SetupDao setupDao = (SetupDao) SecurityUtil.getApplicationContext().getBean("setupDao");
            Collection<Setting> nodes = setupDao.find("WHERE property like '" + CLUSTER_PREFIX + "%'", null, "property", Boolean.FALSE, null, null);
            for (Setting n : nodes) {
                String key = n.getProperty().substring(n.getProperty().lastIndexOf(":") + 1);
                nodeList.add(key);
            }
        } catch (Exception e) {
            LogUtil.debug(ServerUtil.class.getName(), "Error getting active node: " + e.getMessage());
        }
        
        if (nodeList.size() <= 0) {
            return getServerList();
        }
        return nodeList.toArray(new String[0]);
    }
    
    protected static void writeServer() {
        Set<String> servers = new HashSet<String>();
        Gson gson = new Gson();
                
        String serverFilePath = SetupManager.getBaseSharedDirectory() + "/servers.json";
        
        try {
            String serverJson = FileUtils.readFileToString(new File(serverFilePath), "UTF-8");
            servers = gson.fromJson(serverJson, new TypeToken<Set<String>>(){}.getType());
        } catch (Exception e) {
            LogUtil.debug(ServerUtil.class.getName(), "Error read servers file: " + e.getMessage());
        }
        
        if (servers == null) {
            servers = new HashSet<String>();
        }
        
        String lServerName = getServerName();
        
        if (!servers.contains(lServerName)) {
            servers.add(lServerName);
        } else {
            servers.remove(lServerName);

            if (!cleaningTasks.isEmpty()) {
                for (Runnable runnable : cleaningTasks.values()) {
                    runnable.run();
                }
            }

            if (servers.isEmpty()) {
                if (!allServersCleaningTasks.isEmpty()) {
                    for (Runnable runnable : allServersCleaningTasks.values()) {
                        runnable.run();
                    }
                }
            }

            serverName = null;
        }
        
        try {
            String serverJson = gson.toJson(servers);
            FileUtils.writeStringToFile(new File(serverFilePath), serverJson, "UTF-8");
        } catch (Exception e) {
            LogUtil.debug(ServerUtil.class.getName(), "Error write servers file: " + e.getMessage());
        }
    }
}
