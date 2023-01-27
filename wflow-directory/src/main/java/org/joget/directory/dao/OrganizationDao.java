package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Organization;

public interface OrganizationDao {

    Boolean addOrganization(Organization organization);

    Boolean updateOrganization(Organization organization);

    Boolean deleteOrganization(String id);

    Organization getOrganization(String id);

    Organization getOrganizationByName(String name);

    Collection<Organization> getOrganizationsByFilter(String filterString, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalOrganizationsByFilter(String filterString);

    Collection<Organization> findOrganizations(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countOrganizations(String condition, Object[] params);
}
