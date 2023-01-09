package org.joget.apps.app.dao;

import java.util.List;
import org.joget.apps.app.model.UserReplacement;

public interface UserReplacementDao {
    
    public void saveUserReplacement(UserReplacement userReplacement);

    public UserReplacement getUserReplacement(String id);
    
    public List<UserReplacement> getUserTodayReplacedBy(String username, String appId, String processId);
    
    public List<UserReplacement> getTodayUserReplacements(String username);
    
    public List<UserReplacement> getUserReplacements(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows);

    public Long count(String condition, Object[] params);

    public void delete(String id);
}
