package org.joget.commons.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Properties;
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
    
    public static String getIPAddress(String server) {
        InputStream input = null;
        File addressFile = new File(SetupManager.getBaseSharedDirectory() + "/serversIP.properties");
        if (Files.exists(addressFile.toPath())) {
            try {
                Properties props = new Properties();
                input = new FileInputStream(addressFile.getPath());
                props.load(input);

                if (props.get(server) != null) {
                    return props.getProperty(server);
                }
            } catch (Exception e) {
                LogUtil.debug(ServerUtil.class.getName(), "Error read serversIP file: " + e.getMessage());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    LogUtil.debug(ServerUtil.class.getName(), "Failed to close streams: " + e.getMessage());
                }
            }
        }
        return "";
    }
    
    //delete ip address from serversIP.properties & servers.json
    public static void deleteNode(String[] inactiveNodeList) {
        //delete server IP in serversIP.properties
        InputStream input = null;
        OutputStream output = null;
        File serversIPFile = new File(SetupManager.getBaseSharedDirectory() + "/serversIP.properties");
        if (Files.exists(serversIPFile.toPath())) {
            try {
                Properties props = new Properties();
                input = new FileInputStream(serversIPFile.getPath());
                props.load(input);
                boolean removed = false;
                for (String inactiveNode : inactiveNodeList) {
                    if (props.get(inactiveNode) != null) {
                        removed = true;
                        props.remove(inactiveNode);
                    }
                }
                //store the properties when node is removed
                if (removed) {
                    output = new FileOutputStream(serversIPFile.getPath());
                    props.store(output, null);
                }
            } catch (Exception e) {
                LogUtil.debug(ServerUtil.class.getName(), "Error read serversIP file: " + e.getMessage());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    LogUtil.debug(ServerUtil.class.getName(), "Failed to close streams: " + e.getMessage());
                }
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
                boolean removed = false;
                for (String inactiveNode : inactiveNodeList) {
                    if (servers.contains(inactiveNode)) {
                        removed = true;
                        servers.remove(inactiveNode);
                    }
                }
                //write the properties when node is removed
                if (removed) {
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
    
    /**
     * Removes inactive cluster in servers.json & serversIP.properties
     * @param activeNodes
     */
    public static void updateServersFile(Set<String> activeNodes) {
        List<String> inactiveNodeList = new ArrayList<String>();
        if (activeNodes != null) {
            List<String> nodeList = Arrays.asList(ServerUtil.getServerList());
            for (String node : nodeList) {
                boolean exist = false;
                for (String activeNode : activeNodes) {
                    if (activeNode.substring(activeNode.lastIndexOf(":") + 1).equals(node)) {
                        exist = true;
                    }
                }
                if (!exist) {
                    inactiveNodeList.add(node);
                }
            }
            deleteNode(inactiveNodeList.toArray(new String[0]));
        }
    }
}
