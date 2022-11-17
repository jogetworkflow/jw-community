package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.joget.apps.app.model.UserReplacement;
import org.joget.commons.spring.model.AbstractSpringDao;

public class UserReplacementDaoImpl extends AbstractSpringDao implements UserReplacementDao {
    
    public static final String ENTITY_NAME = "UserReplacement";

    public void saveUserReplacement(UserReplacement userReplacement) {
        super.saveOrUpdate(ENTITY_NAME, userReplacement);
    }

    public UserReplacement getUserReplacement(String id) {
        return (UserReplacement) super.find(ENTITY_NAME, id);
    }
    
    public List<UserReplacement> getTodayUserReplacements(String username) {
        Collection<Object> params = new ArrayList<Object>();
        String condition = " where e.replacementUser = ? and ? between e.startDate and e.endDate";
        params.add(username);
        params.add(new Date());
        
        return (List<UserReplacement>) super.find(ENTITY_NAME, condition, params.toArray(), null, null, null, null);
    }
    
    public List<UserReplacement> getUserReplacements(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows) {
        return (List<UserReplacement>) super.find(ENTITY_NAME, condition, param, sort, desc, start, rows);
    }

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    public void delete(String id) {
        super.delete(ENTITY_NAME, getUserReplacement(id));
    }

    public List<UserReplacement> getUserTodayReplacedBy(String username, String appId, String processId) {
        String pId = appId + ":" +processId;
        Collection<Object> params = new ArrayList<Object>();
        String condition = " where e.username = ? and ? between e.startDate and e.endDate ";
        condition += "and ((e.processIds like ? or e.processIds like ? or e.processIds like ? or e.processIds = ?) ";
        condition += "or (e.processIds = '' and (e.appId like ? or e.appId like ? or e.appId like ? or e.appId = ?)))";
        params.add(username);
        params.add(new Date());
        params.add(pId + ";%");
        params.add("%;"+pId);
        params.add("%;"+pId+";%");
        params.add(pId);
        params.add(appId + ";%");
        params.add("%;"+appId);
        params.add("%;"+appId+";%");
        params.add(appId);
        
        return (List<UserReplacement>) super.find(ENTITY_NAME, condition, params.toArray(), null, null, null, null);
    }
}
