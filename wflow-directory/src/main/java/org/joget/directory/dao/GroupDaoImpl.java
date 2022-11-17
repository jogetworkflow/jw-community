package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Group;

public class GroupDaoImpl extends AbstractSpringDao implements GroupDao {

    private OrganizationDao organizationDao;

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }

    public Boolean addGroup(Group group) {
        try {
            save("Group", group);
            return true;
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Add Group Error!");
            return false;
        }
    }

    public Boolean updateGroup(Group group) {
        try {
            merge("Group", group);
            return true;
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Update Group Error!");
            return false;
        }
    }

    public Boolean deleteGroup(String id) {
        try {
            Group group = getGroup(id);
            if (group != null && group.getUsers() != null) {
                group.getUsers().clear();
            }
            delete("Group", group);
            return true;
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Delete Group Error!");
            return false;
        }
    }

    public Group getGroup(String id) {
        try {
            return (Group) find("Group", id);
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Group Error!");
            return null;
        }
    }

    public Group getGroupByName(String name) {
        try {
            Group group = new Group();
            group.setName(name);
            List groups = findByExample("Group", group);

            if (groups.size() > 0) {
                return (Group) groups.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Group By Name Error!");
        }

        return null;
    }

    public Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            return find("Group", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Groups By Organization Id Error!");
        }

        return null;
    }

    public Long getTotalGroupsByOrganizationId(String filterString, String organizationId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            return count("Group", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Total Groups By Organization Id Error!");
        }

        return 0L;
    }

    public Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (userId != null) {
                condition += " and e.id";
                if (inGroup != null && !inGroup) {
                    condition += " not";
                }
                condition += " in (select g.id from Group g join g.users u where u.id = ?)";
                param.add(userId);
            }
            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }

            return find("Group", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Groups By User Id Error!");
        }

        return null;
    }

    public Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (userId != null) {
                condition += " and e.id";
                if (inGroup != null && !inGroup) {
                    condition += " not";
                }
                condition += " in (select g.id from Group g join g.users u where u.id = ?)";
                param.add(userId);
            }
            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }

            return count("Group", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Get Total Groups By User Id Error!");
        }

        return 0L;
    }

    public Collection<Group> findGroups(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            return find("Group", condition, params, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Find Groups Error!");
        }

        return null;
    }

    public Long countGroups(String condition, Object[] params) {
        try {
            return count("Group", condition, params);
        } catch (Exception e) {
            LogUtil.error(GroupDaoImpl.class.getName(), e, "Count Groups Error!");
        }

        return 0L;
    }
}
