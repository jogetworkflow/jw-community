package org.enhydra.shark.repositorypersistence;

import com.lutris.appserver.server.sql.DBQuery;
import com.lutris.appserver.server.sql.DBTransaction;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.repositorypersistence.RepositoryException;
import org.enhydra.shark.repositorypersistence.data.CustomXPDLDataQuery;
import org.enhydra.shark.repositorypersistence.data.CustomXPDLQuery;
import org.enhydra.shark.repositorypersistence.data.XPDLDO;
import org.enhydra.shark.repositorypersistence.data.XPDLDataDO;

public class CustomDODSRepositoryPersistenceManager extends DODSRepositoryPersistenceManager {

    @Override
    public byte[] getXPDL(WMSessionHandle shandle, String xpdlId) throws RepositoryException {
        try {
            return getXPDL(shandle, xpdlId, null);
        } catch (Exception ex) {
            throw new RepositoryException("No xpdl with Id=" + xpdlId + " in repository", ex);
        }
    }

    @Override
    public byte[] getSerializedXPDLObject(WMSessionHandle shandle, String xpdlId) throws RepositoryException {
        try {
            return getSerializedXPDLObject(shandle, xpdlId, null);
        } catch (Exception ex) {
            throw new RepositoryException("No xpdl with Id=" + xpdlId + " in repository", ex);
        }
    }
    
    @Override
    public byte[] getXPDL(WMSessionHandle shandle, String xpdlId, String xpdlVersion) throws RepositoryException {
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            byte[] result = null;
            BigDecimal oid = getOid(shandle, xpdlId, xpdlVersion);
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLDataQuery q = new CustomXPDLDataQuery(dbt);
            q.getQueryBuilder().setSelectClause("SHKXPDLData.XPDLContent");
            q.getQueryBuilder().addWhere(XPDLDataDO.XPDL, oid, "=");
            q.setMaxRows(1);
            
            dbQuery = XPDLDataDO.createQuery(dbt);
            dbQuery.query(q);
            rs = q.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = rs.getBytes(1);
                }
            }
            q.getQueryBuilder().close();
            return result;
        } catch (Exception ex) {
            throw new RepositoryException("No xpdl [" + xpdlId + "," + xpdlVersion + "] in repository", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
    }

    @Override
    public byte[] getSerializedXPDLObject(WMSessionHandle shandle, String xpdlId, String xpdlVersion)
            throws RepositoryException {
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            byte[] result = null;
            BigDecimal oid = getOid(shandle, xpdlId, xpdlVersion);
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLDataQuery q = new CustomXPDLDataQuery(dbt);
            q.getQueryBuilder().setSelectClause("SHKXPDLData.XPDLClassContent");
            q.getQueryBuilder().addWhere(XPDLDataDO.XPDL, oid, "=");
            q.setMaxRows(1);
            
            dbQuery = XPDLDataDO.createQuery(dbt);
            dbQuery.query(q);
            rs = q.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = rs.getBytes(1);
                }
            }
            q.getQueryBuilder().close();
            return result;
        } catch (Exception ex) {
            throw new RepositoryException("No xpdl [" + xpdlId + "," + xpdlVersion + "] in repository", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
    }
    
    protected BigDecimal getOid(WMSessionHandle shandle, String xpdlId, String xpdlVersion) {
        BigDecimal result = null;
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("SHKXPDLS.oid");
            query.setQueryXPDLId(xpdlId);
            
            if (xpdlVersion == null) {
                query.getQueryBuilder().addOrderByColumn("SHKXPDLS.oid", "DESC");
            } else {
                query.setQueryXPDLVersion(xpdlVersion);
            }
            
            query.setMaxRows(1);
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = rs.getBigDecimal(1);
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return result;
    }
    
    @Override
    public String getCurrentVersion(WMSessionHandle shandle, String xpdlId) throws RepositoryException {
        String result = null;
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("SHKXPDLS.XPDLVersion");
            query.setQueryXPDLId(xpdlId);
            query.getQueryBuilder().addOrderByColumn("SHKXPDLS.oid", "DESC");
            query.setMaxRows(1);
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = rs.getString(1);
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return result;
    }
  
    @Override
    public List getXPDLVersions(WMSessionHandle shandle, String xpdlId)
            throws RepositoryException {
        List xpdlVersions = new ArrayList();
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("SHKXPDLS.XPDLVersion");
            query.setQueryXPDLId(xpdlId);
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    xpdlVersions.add(rs.getString(1));
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            throw new RepositoryException("No xpdl with Id=" + xpdlId + " in repository", ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return xpdlVersions;
    }

    @Override
    public boolean doesXPDLExist(WMSessionHandle shandle, String xpdlId) throws RepositoryException {
        boolean result = false;
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("SHKXPDLS.XPDLVersion");
            query.setQueryXPDLId(xpdlId);
            query.setMaxRows(1);
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = true;
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return result;
    }

    @Override
    public boolean doesXPDLExist(WMSessionHandle shandle, String xpdlId, String xpdlVersion) throws RepositoryException {
        boolean result = false;
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("SHKXPDLS.XPDLVersion");
            query.setQueryXPDLId(xpdlId);
            query.setQueryXPDLVersion(xpdlVersion);
            query.setMaxRows(1);
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    result = true;
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return result;
    }

    @Override
    public List getExistingXPDLIds(WMSessionHandle shandle) throws RepositoryException {
        List xpdlIds = new ArrayList();
        DBQuery dbQuery = null;
        ResultSet rs = null;
        try {
            
            DBTransaction dbt = getDBTransaction();
            CustomXPDLQuery query = new CustomXPDLQuery(dbt);
            query.getQueryBuilder().setSelectClause("DISTINCT SHKXPDLS.XPDLId");
            
            dbQuery = XPDLDO.createQuery(dbt);
            dbQuery.query(query);
            rs = query.getResultSet();
            
            if (rs != null) {
                while (rs.next()) {
                    xpdlIds.add(rs.getString(1));
                }
            }
            query.getQueryBuilder().close();
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
            }
            if (dbQuery != null) {
                dbQuery.release();
            }
        }
        
        return xpdlIds;
    }
}
