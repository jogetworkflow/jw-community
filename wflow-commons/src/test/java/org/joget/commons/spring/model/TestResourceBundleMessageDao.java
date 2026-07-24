package org.joget.commons.spring.model;

import java.util.concurrent.atomic.AtomicInteger;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.aop.framework.Advised;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Verifies getMessage() runs under PROPAGATION_SUPPORTS with no ambient transaction:
 * a cache miss must still be able to open a Hibernate session (via Spring transaction
 * synchronization) rather than throwing "Could not obtain transaction-synchronized
 * Session for current thread".
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testCommonsApplicationContext.xml"})
public class TestResourceBundleMessageDao {

    @Autowired
    ResourceBundleMessageDao resourceBundleMessageDao;

    @Test
    public void testGetMessageCacheMissAndHitWithNoAmbientTransaction() throws Exception {
        String locale = "zz_ZZ_" + System.nanoTime();
        AtomicInteger observedCalls = new AtomicInteger();
        MethodInterceptor transactionStateObserver = invocation -> {
            if ("getMessage".equals(invocation.getMethod().getName())) {
                observedCalls.incrementAndGet();
                Assert.assertTrue(
                        "SUPPORTS must activate synchronization so a cache miss can open a Hibernate session",
                        TransactionSynchronizationManager.isSynchronizationActive());
                Assert.assertFalse(
                        "getMessage must not open an actual transaction when there is no ambient transaction",
                        TransactionSynchronizationManager.isActualTransactionActive());
            }
            return invocation.proceed();
        };
        Advised advisedDao = (Advised) resourceBundleMessageDao;
        // Appending the observer places it inside the existing transaction interceptor,
        // allowing it to inspect the state established by txAdviceCacheReadOnly.
        advisedDao.addAdvice(transactionStateObserver);

        try {
            // Cache miss: no cached map for this locale yet, so the DAO must hit super.find(),
            // which requires a Hibernate session. This call has no ambient Spring/JTA transaction
            // (no @Transactional here), so this is exactly the SUPPORTS risk path.
            ResourceBundleMessage miss = resourceBundleMessageDao.getMessage("no.such.key", locale);
            Assert.assertNull(miss);

            // Cache hit: the locale map populated by the first call is now cached.
            ResourceBundleMessage hit = resourceBundleMessageDao.getMessage("no.such.key", locale);
            Assert.assertNull(hit);

            Assert.assertEquals("The transaction-state observer must inspect both calls",
                    2, observedCalls.get());
        } finally {
            advisedDao.removeAdvice(transactionStateObserver);
        }
    }
}
