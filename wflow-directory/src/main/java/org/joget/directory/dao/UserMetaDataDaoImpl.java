package org.joget.directory.dao;

import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.UserMetaData;

public class UserMetaDataDaoImpl extends AbstractSpringDao implements UserMetaDataDao {

    public UserMetaData getUserMetaData(String username, String key) {
        try {
            UserMetaData data = new UserMetaData();
            data.setUsername(username);
            data.setKey(key);
            List datas = findByExample("UserMetaData", data);

            if (datas.size() > 0) {
                return (UserMetaData) datas.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User Meta Data Error!");
        }

        return null;
    }

    public Collection<UserMetaData> getUserMetaDatas(String username) {
        try {
            UserMetaData data = new UserMetaData();
            data.setUsername(username);
            Collection<UserMetaData> datas = (Collection<UserMetaData>) findByExample("UserMetaData", data);

            return datas;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User Meta Data Error!");
        }

        return null;
    }
    
    public Boolean addUserMetaData(UserMetaData data) {
        try {
            save("UserMetaData", data);
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Add User Meta Data Error!");
            return false;
        }
    }

    public Boolean updateUserMetaData(UserMetaData data) {
        try {
            merge("UserMetaData", data);
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Update User Meta Data Error!");
            return false;
        }
    }

    public Boolean deleteUserMetaData(String username, String key) {
        try {
            UserMetaData data = getUserMetaData(username, key);
            if (data != null) {
                delete("UserMetaData", data);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Delete User Error!");
            return false;
        }
    }

    public Boolean deleteUserMetaDatas(String username) {
        try {
            Collection<UserMetaData> datas = getUserMetaDatas(username);
            if (datas != null && !datas.isEmpty()) {
                for (UserMetaData data : datas) {
                    delete("UserMetaData", data);
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Delete User Error!");
            return false;
        }
    }
    
}
