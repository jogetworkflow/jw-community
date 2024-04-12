package org.joget.apps.app.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AppOverviewData extends HashMap {
    
    /**
     * Check is the builder item data is required to update based on the last modified date
     * @param key
     * @param lastModified
     * @return 
     */
    public boolean isItemRequireUpdate(String key, Date lastModified) {
        BuilderItem item = getItem(key);
        if (item != null && item.lastModifiedDate.equals(lastModified)) {
            return false;
        } else {
            put(key, new BuilderItem(lastModified));
            return true;
        }
    }
    
    /**
     * Check and remove builder items not exist in the key set
     * @param checkedKeys 
     */
    public void checkAndRemoveDeletedItems(Set<String> checkedKeys) {
        Iterator<Map.Entry> iter = entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            if(!checkedKeys.contains(entry.getKey().toString())){
                iter.remove();
            }
        }
    }
    
    /**
     * Add data to builder item
     * @param key
     * @param tool
     * @param selector
     * @param label
     * @param content 
     */
    public void addItemData(String key, AppOverviewTool tool, String selector, String label, String content) {
        addItemData(key, tool, selector, label, content, false);
    }
    
    /**
     * Add data to builder item
     * @param key
     * @param tool
     * @param selector
     * @param label
     * @param content 
     * @param isError 
     */
    public void addItemData(String key, AppOverviewTool tool, String selector, String label, String content, boolean isError) {
        addItemData(key, tool, selector, label, content, false, null, null);
    }
    
    /**
     * Add data to builder item
     * @param key
     * @param tool
     * @param selector
     * @param label
     * @param content 
     * @param isError 
     */
    public void addItemData(String key, AppOverviewTool tool, String selector, String label, String content, boolean isError, String badge, String badgeColor) {
        BuilderItem item = getItem(key);
        if (item != null && tool != null 
                && selector != null && !selector.isEmpty()
                && content != null && !content.isEmpty()) {
            item.getData().add(new Data(tool, selector, label, content, isError, badge, badgeColor));
        }
    }
    
    /**
     * Use to hold temporary data for the scanning, data will get cleared when checkAndRemoveDeletedItems called 
     * 
     * @param key
     * @param data 
     */
    public void putTempData(String key, Object data) {
        put("TEMP::" + key, data);
    }
    
    /**
     * Retrieve temporary data for the scanning
     * 
     * @param key
     * @return 
     */
    public Object getTempData(String key) {
        return get("TEMP::" + key);
    }
    
    /**
     * Retrieve the item by key
     * 
     * @param key
     * @return 
     */
    public BuilderItem getItem(String key) {
        Object item = get(key);
        return (item != null && item instanceof BuilderItem)?((BuilderItem)item):null;
    }
    
    public class BuilderItem {
        protected Date lastModifiedDate;
        protected Collection<Data> data = new ArrayList<Data>();
        
        public BuilderItem(){
        }
        
        public BuilderItem(Date lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }

        public Date getLastModifiedDate() {
            return lastModifiedDate;
        }

        public void setLastModifiedDate(Date lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }

        public Collection<Data> getData() {
            return data;
        }

        public void setData(Collection<Data> data) {
            this.data = data;
        }
    }
    
    public static class Data {
        protected String tool;
        protected String path;
        protected String label;
        protected String content;
        protected boolean isError = false;
        protected String badge;
        protected String badgeColor;
        
        public Data(){
        }
        
        public Data(AppOverviewTool tool, String path, String label, String content, boolean isError, String badge, String badgeColor) {
            this.tool = tool.getClassName();
            this.path = path;
            this.label = label;
            this.content = content;
            this.isError = isError;
            this.badge = badge;
            this.badgeColor = badgeColor;
        }

        public String getTool() {
            return tool;
        }

        public void setTool(String tool) {
            this.tool = tool;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isIsError() {
            return isError;
        }

        public void setIsError(boolean isError) {
            this.isError = isError;
        }

        public String getBadge() {
            return badge;
        }

        public void setBadge(String badge) {
            this.badge = badge;
        }

        public String getBadgeColor() {
            return badgeColor;
        }

        public void setBadgeColor(String badgeColor) {
            this.badgeColor = badgeColor;
        }
    }
}
