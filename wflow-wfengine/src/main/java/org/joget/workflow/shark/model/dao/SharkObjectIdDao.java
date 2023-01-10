package org.joget.workflow.shark.model.dao;

import java.util.Collection;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.shark.model.SharkObjectId;

/**
 * Use to replace shark StandardObjectIdAllocator implementation which does not lock 
 * table when update the next object id in clustering environment
 */
public class SharkObjectIdDao extends AbstractSpringDao {
    
    public static final String ENTITY_NAME = "SharkObjectId";
    private final static long CACHE_SIZE = 200;

    public SharkObjectId getNext(Long old) {
        int retryCount = 0;
        boolean retry = false;
        
        SharkObjectId temp = new SharkObjectId();
        do {
            SessionFactory sf = super.getSessionFactory();
            Session session = null;
            Transaction transaction = null;
            try {
                session = sf.openSession();
                transaction = session.beginTransaction();
                
                //find the last next oid
                Collection<SharkObjectId> result = (Collection<SharkObjectId>) super.find(ENTITY_NAME, "", null, null, null, null, null);
                if (!result.isEmpty()) {
                    SharkObjectId nextOid = result.iterator().next();
                    
                    //lock it for update
                    session.refresh(ENTITY_NAME, nextOid, new LockOptions(LockMode.PESSIMISTIC_WRITE));
                    
                    LogUtil.debug(SharkObjectIdDao.class.getName(), "Retrieved number is " + nextOid.getNextoid() + ", old number is " + old);
                    
                    temp.setNextoid(nextOid.getNextoid());
                    temp.setMaxoid(nextOid.getNextoid() + CACHE_SIZE);
                    
                    //update the next oid
                    Query query = session.createQuery("update " + ENTITY_NAME + " set nextoid=? where nextoid=?");
                    query.setLong(0, temp.getMaxoid());
                    query.setLong(1, temp.getNextoid());
                    query.executeUpdate();
                    
                    session.flush(); 
                    transaction.commit();
                    
                    return temp;
                }
            } catch (Exception e) {
                retry = true;
                
                //retry when update fail for 50 times
                if (retryCount++ >= 50) {
                    LogUtil.error(SharkObjectIdDao.class.getName(), e, "ObjectIdAllocator: Failed to allocate object ids. Tried 50 times. Giving up. Last number is " + old.toString());
                    retry = false;
                    break;
                } else {
                    LogUtil.debug(SharkObjectIdDao.class.getName(), e.getMessage());
                }
                LogUtil.info(SharkObjectIdDao.class.getName(), "ObjectIdAllocator: Failed to allocate object ids. Trying again....");
            } finally {
                if (session != null) {
                    session.clear();
                    session.close();
                }
            }
        } while (retry);
        
        return null;
    }
}
