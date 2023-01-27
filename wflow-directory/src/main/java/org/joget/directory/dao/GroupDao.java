package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Group;

public interface GroupDao {

    Boolean addGroup(Group group);

    Boolean updateGroup(Group group);

    Boolean deleteGroup(String id);

    Group getGroup(String id);

    Group getGroupByName(String name);

    Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGroupsByOrganizationId(String filterString, String organizationId);

    Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup);

    Collection<Group> findGroups(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countGroups(String condition, Object[] params);
}
