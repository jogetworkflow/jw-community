package org.joget.workflow.shark.model.dao;

import com.lutris.appserver.server.sql.ObjectIdAllocationError;
import java.util.Collection;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.shark.model.SharkCounter;

/**
 * Use to replace shark DODSUtilities.updateCaches implementation which does not lock 
 * table when update the next counter in clustering environment
 */
public class SharkCounterDao extends AbstractSpringDao {
    
    public static final String ENTITY_NAME = "SharkCounter";
    private final static String SYSTEM_PROPERTY_SHARK_CACHE_SIZE = "wflow.sharkCacheSize";
    private final static long DEFAULT_CACHE_SIZE = 1000;

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
                session.setHibernateFlushMode(FlushMode.MANUAL);
                transaction = session.beginTransaction();
                
                //find the counter by object name
                Query find = session.createQuery("SELECT e FROM " + ENTITY_NAME + " e where name = ?1");
                find.setParameter(1, objectName);
                Collection<SharkCounter> result = (Collection<SharkCounter>) find.list();
                
                long cacheSize = SharkCounterDao.getCacheSize();            
                if (!result.isEmpty()) {
                    SharkCounter next = result.iterator().next();
                    
                    //lock it for update
                    session.refresh(next, new LockOptions(LockMode.PESSIMISTIC_WRITE));
                    
                    LogUtil.debug(SharkCounterDao.class.getName(), "Retrieved number is " + next.getNextNumber() + ", old number is " + old);
                    
                    temp.setNextNumber(next.getNextNumber());
                    temp.setMaxNumber(next.getNextNumber() + cacheSize);
                    
                    //update the next oid
                    next.setNextNumber(temp.getMaxNumber());
                    next.setVersion(next.getVersion() + 1);
                    
                    session.merge(ENTITY_NAME, next);
                    
                    session.flush();
                    transaction.commit();
                    
                    session.evict(next);
                    
                    return temp;
                } else {
                    temp.setName(objectName);
                    temp.setNextNumber(1 + cacheSize);
                    temp.setVersion(0);
                    temp.setOid(Math.abs(objectName.hashCode()) + 0l);
                    
                    session.merge(ENTITY_NAME, temp);
                    
                    session.flush();
                    transaction.commit();
                    
                    session.evict(temp);
                    
                    temp.setNextNumber(1l);
                    temp.setMaxNumber(1 + cacheSize);
                    return temp;
                }
            } catch (Exception e) {
                retry = true;
                
                //retry when update fail for 50 times
                if (retryCount++ >= 50) {
                    LogUtil.error(SharkCounterDao.class.getName(), e, "ObjectIdAllocator: Failed to allocate counter for " +objectName+ ". Tried 50 times. Giving up. Last number is " + old);
                    retry = false;
                    break;
                } else {
                    LogUtil.debug(SharkCounterDao.class.getName(), e.getMessage());
                }
                LogUtil.info(SharkCounterDao.class.getName(), "ObjectIdAllocator: Failed to allocate counter for " +objectName+ ". Trying again....");
            } finally {
                if (session != null) {
                    session.flush();
                    if (transaction != null && !transaction.getStatus().equals(TransactionStatus.COMMITTED) && transaction.getStatus().equals(TransactionStatus.ACTIVE)) {
                        transaction.commit();
                    }
                    session.clear();
                    session.close();
                }
            }
        } while (retry);
        
        throw new ObjectIdAllocationError("Failed to allocate counter for " + objectName + ".");
    }

    public static long getCacheSize() {
        // set cache size
        long cacheSize = DEFAULT_CACHE_SIZE;
        String cacheSizeStr = System.getProperty(SYSTEM_PROPERTY_SHARK_CACHE_SIZE);
        if (cacheSizeStr != null) {
            try {
                cacheSize = Long.parseLong(cacheSizeStr);
            } catch(NumberFormatException e) {
                // ignore
            }
        }
        return cacheSize;
    }
}
