package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Grade;

public interface GradeDao {

    Boolean addGrade(Grade grade);

    Boolean updateGrade(Grade grade);

    Boolean deleteGrade(String id);

    Grade getGrade(String id);

    Grade getGradeByName(String name);

    Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGradesByOrganizationId(String filterString, String organizationId);

    Collection<Grade> findGrades(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countGrades(String condition, Object[] params);
}
