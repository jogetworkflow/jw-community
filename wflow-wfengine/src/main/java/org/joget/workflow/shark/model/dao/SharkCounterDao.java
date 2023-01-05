package org.joget.workflow.shark.model.dao;

import com.lutris.appserver.server.sql.ObjectIdAllocationError;
import java.util.Collection;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.shark.model.SharkCounter;

/**
 * Use to replace shark DODSUtilities.updateCaches implementation which does not lock 
 * table when update the next counter in clustering environment
 */
public class SharkCounterDao extends AbstractSpringDao {
    
    public static final String ENTITY_NAME = "SharkCounter";
    private final static long CACHE_SIZE = 200;

    public SharkCounter getNext(String objectName, Long old) {
        int retryCount = 0;
        boolean retry = false;
        
        SharkCounter temp = new SharkCounter();
        do {
            SessionFactory sf = super.getSessionFactory();
            Session session = null;
            Transaction transaction = null;
            try {
                session = sf.openSession();
                transaction = session.beginTransaction();
                
                //find the counter by object name
                Collection<SharkCounter> result = (Collection<SharkCounter>) super.find(ENTITY_NAME, " where name = ?", new String[]{objectName}, null, null, null, null);
                
                if (!result.isEmpty()) {
                    SharkCounter next = result.iterator().next();
                    
                    //lock it for update
                    session.refresh(ENTITY_NAME, next, new LockOptions(LockMode.PESSIMISTIC_WRITE));
                    
                    LogUtil.debug(SharkCounterDao.class.getName(), "Retrieved number is " + next.getNextNumber() + ", old number is " + old);
                    
                    temp.setNextNumber(next.getNextNumber());
                    temp.setMaxNumber(next.getNextNumber() + CACHE_SIZE);
                    
                    //update the next oid
                    Query query = session.createQuery("update " + ENTITY_NAME + " set nextNumber=?1, version=?2 where name=?3 and nextNumber=?4");
                    query.setParameter(1, temp.getMaxNumber());
                    query.setParameter(2, next.getVersion() + 1);
                    query.setParameter(3, objectName);
                    query.setParameter(4, temp.getNextNumber());
                    query.executeUpdate();
                    
                    session.flush(); 
                    transaction.commit();
                    
                    return temp;
                } else {
                    return null;
                }
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                
                retry = true;
                
                //retry when update fail for 50 times
                if (retryCount++ >= 50) {
                    LogUtil.error(SharkCounterDao.class.getName(), e, "ObjectIdAllocator: Failed to allocate counter for " +objectName+ ". Tried 50 times. Giving up. Last number is " + old.toString());
                    retry = false;
                    break;
                } else {
                    LogUtil.debug(SharkCounterDao.class.getName(), e.getMessage());
                }
                LogUtil.info(SharkCounterDao.class.getName(), "ObjectIdAllocator: Failed to allocate counter for " +objectName+ ". Trying again....");
            } finally {
                if (session != null) {
                    session.clear();
                    session.close();
                }
            }
        } while (retry);
        
        throw new ObjectIdAllocationError("Failed to allocate counter for " + objectName + ".");
    }
}