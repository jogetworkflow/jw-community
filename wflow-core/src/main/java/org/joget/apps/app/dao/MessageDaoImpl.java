package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.hibernate.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.Message;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

public class MessageDaoImpl extends AbstractAppVersionedObjectDao<Message> implements MessageDao {

    public static final String ENTITY_NAME = "Message";
    private Cache cache;

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }
    
    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String messageKey, String locale, String appId, String version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_MSG_"+messageKey+":"+locale;
    }

    public Message loadByMessageKey(String messageKey, String locale, AppDefinition appDefinition) {
        String key = getCacheKey(messageKey, locale, appDefinition.getId(), appDefinition.getVersion().toString());
        Element element = cache.get(key);

        if (element == null) {
            Message message = loadById(messageKey + Message.ID_SEPARATOR + locale, appDefinition);

            if (message != null) {
                element = new Element(key, (Serializable) message);
                cache.put(element);
            }
            return message;
        } else {
            return (Message) element.getValue();
        }
    }

    public Collection<Message> getMessageList(String filterString, String locale, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
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

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
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
        String query = "SELECT distinct e.locale FROM " + getEntityName() + " e " + condition + " ORDER BY e.locale";

        Query q = findSession().createQuery(query);
        q.setFirstResult(0);

        if (params != null) {
            int i = 0;
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
                        String key = getCacheKey(object.getMessageKey(), object.getLocale(), object.getAppId(), object.getAppVersion().toString());
                        cache.remove(key);
        
                        list.remove(obj);
                        break;
                    }
                }
                obj.setAppDefinition(null);

                // delete obj
                super.delete(getEntityName(), obj);
                result = true;
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }
    
    @Override
    public boolean update(Message object) {
        String key = getCacheKey(object.getMessageKey(), object.getLocale(), object.getAppId(), object.getAppVersion().toString());
        cache.remove(key);
        return super.update(object);
    }
}
