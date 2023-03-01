package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Element;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageDaoImpl extends AbstractAppVersionedObjectDao<Message> implements MessageDao {

    public static final String ENTITY_NAME = "Message";
    private AppDefCache cache;

    @Autowired
    AppService appService;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }
    
    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String locale, String appId, String version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_MSG_"+locale;
    }
    
    public Message loadByMessageKey(String messageKey, String locale, AppDefinition appDefinition) {
        return getCachedMessageList(locale, appDefinition).get(messageKey);
    }
    
    public Map<String, Message> getCachedMessageList(String locale, AppDefinition appDefinition) {
        Map<String, Message> messageMap = new HashMap<String, Message>();
        String cacheKey = getCacheKey(locale, appDefinition.getAppId(), appDefinition.getVersion().toString());
        Element element = cache.get(cacheKey, appDefinition);
        if (element == null) {
            messageMap = new HashMap<String, Message>();
            
            if (!"en_US".equals(locale)) {
                Map<String, Message> enResults = getCachedMessageList("en_US", appDefinition);
                if (!enResults.isEmpty()) {
                    messageMap.putAll(enResults);
                }
            }
            
            Collection<Message> results = getMessageList(null, locale, appDefinition, null, null, null, null);
            for (Message message : results) {
                messageMap.put(message.getMessageKey(), message);
            }
            element = new Element(cacheKey, messageMap);
            cache.put(element, appDefinition);
        } else {
            messageMap = (HashMap<String, Message>) element.getObjectValue();
        }
        return messageMap;
    }

    public Collection<Message> getMessageList(String filterString, String locale, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        Session s = findSession();
        
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (messageKey like ? or message like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        if (locale != null) {
            conditions += "and locale = ?";
            params.add(locale);
        }

        Collection<Message> messages = this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
        for (Message m : messages) {
            s.evict(m);
        }
        return messages;
    }

    public Long getMessageListCount(String filterString, String locale, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (messageKey like ? or message like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        if (locale != null) {
            conditions += "and locale = ?";
            params.add(locale);
        }

        return this.count(conditions, params.toArray(), appDefinition);
    }

    public Collection<String> getLocaleList(AppDefinition appDefinition) {
        final String condition = generateQueryCondition(appDefinition);
        final Object[] params = generateQueryParams(appDefinition).toArray();

        // execute query and return result
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        String query = "SELECT distinct e.locale FROM " + getEntityName() + " e " + newCondition + " ORDER BY e.locale";

        Query q = findSession().createQuery(query);
        q.setFirstResult(0);

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }
    
    @Override
    public Collection<String> getKeyList(AppDefinition appDefinition) {
        final String condition = generateQueryCondition(appDefinition);
        final Object[] params = generateQueryParams(appDefinition).toArray();

        // execute query and return result
        String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
        String query = "SELECT distinct e.messageKey FROM " + getEntityName() + " e " + newCondition + " ORDER BY e.messageKey";

        Query q = findSession().createQuery(query);
        q.setFirstResult(0);

        if (params != null) {
            int i = 1;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }

        return q.list();
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            Message obj = loadById(id, appDef);

            // detach from app
            if (obj != null) {
                Collection<Message> list = appDef.getMessageList();
                for (Message object : list) {
                    if (obj.getId().equals(object.getId())) {
                        String key = getCacheKey(object.getLocale(), object.getAppId(), object.getAppVersion().toString());
                        cache.remove(key, appDef);
        
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                appDefinitionDao.updateDateModified(appDef);
                result = true;
                
                if (!AppDevUtil.isGitDisabled()) {
                    // save and commit app definition
                    appDef = appService.loadAppDefinition(appDef.getAppId(), appDef.getVersion().toString());
                    String filename = "appDefinition.xml";
                    String xml = AppDevUtil.getAppDefinitionXml(appDef);
                    String commitMessage = "Update app definition " + appDef.getId();
                    AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    @Override
    public boolean update(Message object) {
        String key = getCacheKey(object.getLocale(), object.getAppId(), object.getAppVersion().toString());
        cache.remove(key, object.getAppDefinition());
        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled()) {
            // save and commit app definition
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
        }
        
        return result;
    }
    
    @Override
    public boolean add(Message object) {
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition());
        
        if (!AppDevUtil.isGitDisabled() && !AppDevUtil.isImportApp()) {
            // save and commit app definition
            AppDefinition appDef = appService.loadAppDefinition(object.getAppId(), object.getAppVersion().toString());
            String filename = "appDefinition.xml";
            String xml = AppDevUtil.getAppDefinitionXml(appDef);
            String commitMessage = "Update app definition " + appDef.getId();
            AppDevUtil.fileSave(appDef, filename, xml, commitMessage);
        }
        
        return result;
    }    
    
}
