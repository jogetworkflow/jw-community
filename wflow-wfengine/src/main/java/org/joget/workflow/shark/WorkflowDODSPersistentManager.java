package org.joget.workflow.shark;

import com.lutris.dods.builder.generator.query.QueryBuilder;
import java.math.BigDecimal;
import org.enhydra.shark.api.internal.instancepersistence.PersistenceException;
import org.enhydra.shark.instancepersistence.DODSPersistentManager;
import org.enhydra.shark.instancepersistence.data.ProcessDO;
import org.enhydra.shark.instancepersistence.data.ProcessQuery;
import org.enhydra.shark.instancepersistence.data.ProcessStateDO;

public class WorkflowDODSPersistentManager extends DODSPersistentManager {

    @Override
    protected ProcessDO[] getPersistedProcesses(int type, String sqlWhere, int startAt, int limit) throws PersistenceException {
        ProcessDO[] DOs = null;
        ProcessQuery query = null;
        try {
            query = new ProcessQuery(/* dbt */);

            if (type == 1) {
                query.setQueryState(ProcessStateDO.createExisting((BigDecimal) _prStates.get("open.running")), QueryBuilder.EQUAL);
            } else if (type == -1) {
                for (int i = 0; i < _prOpenStatesBigDecimals.size(); i++) {
                    query.setQueryState(ProcessStateDO.createExisting((BigDecimal) _prOpenStatesBigDecimals.get(i)), QueryBuilder.NOT_EQUAL);
                }
            }
            
            if (null != sqlWhere) {
                query.getQueryBuilder().addWhere(sqlWhere);
                if (startAt > 0) {
                    query.setReadSkip(startAt);
                }
                if (limit > 0) {
                    query.setDatabaseLimit(limit);
                    // CUSTOMIZED: Added maxRows to limit the number of rows returned by the SQL query
                    query.setMaxRows(startAt + limit);
                }
            }
            DOs = query.getDOArray();
            return DOs;
        } catch (Throwable t) {
            throw new PersistenceException(t);
        }
    }
}

