package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.UserMetaData;

public interface UserMetaDataDao {
    
    UserMetaData getUserMetaData(String username, String key);
    
    Collection<UserMetaData> getUserMetaDatas(String username);
    
    Boolean addUserMetaData(UserMetaData data);
    
    Boolean updateUserMetaData(UserMetaData data);
    
    Boolean deleteUserMetaData(String username, String key);
    
    Boolean deleteUserMetaDatas(String username);
}
