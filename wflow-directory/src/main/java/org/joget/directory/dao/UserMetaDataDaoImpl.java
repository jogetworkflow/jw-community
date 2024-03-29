package org.joget.directory.dao;

import java.util.Collection;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.UserMetaData;

public class UserMetaDataDaoImpl extends AbstractSpringDao implements UserMetaDataDao {

    public UserMetaData getUserMetaData(String username, String key) {
        try {
            Collection datas = find("UserMetaData", "where e.username = ? and e.key = ?", new Object[]{username, key}, null, null, null, null);

            if (datas.size() > 0) {
                return (UserMetaData) datas.iterator().next();
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
            Collection<UserMetaData> datas = (Collection<UserMetaData>) find("UserMetaData", "where e.username = ?", new Object[]{username}, null, null, null, null);

            return datas;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User Meta Data Error!");
        }

        return null;
    }

    @Override
    public Collection<UserMetaData> getUserMetaDatasByKeyValue(String key, String value) {
        Collection<UserMetaData> result = null;
        try {
            result = (Collection<UserMetaData>) find("UserMetaData", "WHERE e.key = ? AND e.value = ?", new Object[]{key, value}, null, null, null, null);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User Meta Data Error!");
        }
        return result;
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

    @Override
    public Boolean deleteUserMetaDatasByUsernameKey(String username, String key) {
        try {
            Collection<UserMetaData> collection = (Collection<UserMetaData>) find("UserMetaData", "WHERE e.username = ? AND e.key = ?", new Object[]{username, key}, null, null, null, null);
            if (collection != null) {
                for (UserMetaData data : collection) {
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
