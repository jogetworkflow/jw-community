package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.UserMetaData;

public interface UserMetaDataDao {
    
    UserMetaData getUserMetaData(String username, String key);
    
    Collection<UserMetaData> getUserMetaDatas(String username);

    Collection<UserMetaData> getUserMetaDatasByKeyValue(String key, String value);

    Boolean addUserMetaData(UserMetaData data);
    
    Boolean updateUserMetaData(UserMetaData data);
    
    Boolean deleteUserMetaData(String username, String key);
    
    Boolean deleteUserMetaDatas(String username);

    Boolean deleteUserMetaDatasByUsernameKey(String username, String key);
}
