package org.joget.commons.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collection;
import javax.sql.DataSource;
import org.joget.commons.spring.model.Setting;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Database-portability cover for the two things #2085 added below the application layer:
 * {@link SetupDao#findFreshValues} and the {@code idx_setup_property} index.
 *
 * <p>Joget ships on MariaDB/MySQL, PostgreSQL, SQL Server and Oracle, and the suite normally only
 * runs on the first. Both additions are dialect-sensitive in ways that would not show up on MySQL:
 * the query projects a scalar out of {@code wf_setup.value}, which is {@code text} on
 * MySQL/PostgreSQL but {@code CLOB} on Oracle and {@code ntext} on SQL Server; and the index is
 * declared in the Hibernate mapping so that hbm2ddl emits dialect-appropriate DDL.</p>
 *
 * <p>Run across all four with:
 * {@code bash skills/joget-java-testing/scripts/run-db-matrix.sh -m wflow-commons -t 'TestSettingFreshRead'}</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testCommonsApplicationContext.xml"})
public class TestSettingFreshRead {

    private static final String PROPERTY = "testFreshReadSetting";

    @Autowired
    SetupDao setupDao;

    @Autowired
    SetupManager setupManager;

    @Autowired
    ApplicationContext applicationContext;

    @After
    public void cleanUp() {
        try {
            setupDao.delete(PROPERTY);
        } catch (Exception ignored) {
            // nothing to clean
        }
    }

    /**
     * The scalar projection must execute and round-trip a value on every dialect.
     *
     * <p>{@code value} is a LOB column on Oracle ({@code CLOB}) and SQL Server ({@code ntext}).
     * Selecting a LOB as a scalar is the specific portability risk here — some dialects will not
     * return it as a plain String, and Oracle in particular is restrictive about what may be done
     * with a CLOB in a projection.</p>
     */
    @Test
    public void freshValueRoundTripsOnThisDialect() {
        Assert.assertNull("fixture assumption: property must not already exist",
                only(setupDao.findFreshValues(PROPERTY)));

        Setting setting = new Setting();
        setting.setProperty(PROPERTY);
        setting.setValue("1786069027718");
        setupManager.saveSetting(setting);

        Assert.assertEquals("1786069027718", only(setupDao.findFreshValues(PROPERTY)));

        setting.setValue("1786069999999");
        setupManager.saveSetting(setting);

        Assert.assertEquals("the fresh read must return the updated value, not the first one",
                "1786069999999", only(setupDao.findFreshValues(PROPERTY)));
    }

    /**
     * A missing property must return null rather than throw — {@code LongTermCache.getFresh} treats
     * null as "never invalidated", so an exception here would fail every cache lookup.
     */
    @Test
    public void missingPropertyReturnsNull() {
        Assert.assertNull(only(setupDao.findFreshValues("noSuchProperty_" + System.nanoTime())));
    }

    /**
     * {@code idx_setup_property} must exist on {@code wf_setup}, whichever dialect this is.
     *
     * <p>It is declared in {@code Setting.hbm.xml} so hbm2ddl (auto=update) creates it on upgrade
     * as well as on a fresh install, on every dialect, without a manual step. Without it the
     * lookup is a full table scan — measured at 8.1 ms against a 20,000-row {@code wf_setup} vs
     * 0.09 ms indexed — and {@code CACHE_LAST_CLEAR_} rows accumulate one per cache key ever
     * invalidated, so that table does grow.</p>
     */
    @Test
    public void propertyColumnIsIndexedOnThisDialect() throws Exception {
        DataSource ds = (DataSource) applicationContext.getBean("setupDataSource");
        boolean found = false;
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            // Dialects disagree on identifier case, so try the spellings rather than guess.
            for (String table : new String[]{"wf_setup", "WF_SETUP"}) {
                try (ResultSet rs = md.getIndexInfo(c.getCatalog(), null, table, false, false)) {
                    while (rs.next()) {
                        String column = rs.getString("COLUMN_NAME");
                        if (column != null && "property".equalsIgnoreCase(column)) {
                            found = true;
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        Assert.assertTrue("no index on wf_setup.property — every setting lookup is a full table "
                + "scan on this dialect", found);
    }

    /**
     * DUPLICATE rows under one property must not hide the newest value.
     *
     * <p>{@code wf_setup.property} is not unique, and {@code updateSetting} resolves the existing row
     * through the ~20 s debounced setup cache — so two nodes invalidating a cache key for the first
     * time can each miss and insert their own UUID row. Thereafter each node keeps updating "its"
     * row. The read used to be {@code setMaxResults(1)} with no {@code ORDER BY}, which picks an
     * arbitrary duplicate: a node could sit permanently on the row that stopped advancing and never
     * see the other node's invalidations — #2085 by a slower route.</p>
     *
     * <p>Rows are inserted through plain JDBC precisely BECAUSE the DAO would not create a duplicate;
     * the situation being tested is one only a race produces.</p>
     *
     * <p><b>Both insertion orders are exercised, and that is what makes this falsifiable.</b> No
     * dialect guarantees the order rows come back in, so testing one order would let a "first row
     * wins" implementation pass by luck on whichever engine happens to favour it. Whatever order a
     * given engine returns, one of the two cases below must break such an implementation.</p>
     *
     * <p>Falsifiable: restore {@code setMaxResults(1)} in {@code findFreshValues} (fails the row
     * count), or take the first parsed value instead of the max in {@code getLastClearTimeFresh}
     * (fails one of the two orders).</p>
     */
    @Test
    public void duplicateRowsResolveToTheNewestValue() throws Exception {
        assertNewestWins("1786069999999", "1786069027718");  // newer row inserted first
        assertNewestWins("1786069027718", "1786069999999");  // newer row inserted second
    }

    private void assertNewestWins(String firstInserted, String secondInserted) throws Exception {
        DataSource ds = (DataSource) applicationContext.getBean("setupDataSource");
        String property = "CACHE_LAST_CLEAR_" + PROPERTY;
        long expected = Math.max(Long.parseLong(firstInserted), Long.parseLong(secondInserted));
        try (Connection c = ds.getConnection()) {
            deleteByProperty(c, property);
            insert(c, "dupA_" + System.nanoTime(), property, firstInserted);
            insert(c, "dupB_" + System.nanoTime(), property, secondInserted);
        }
        try {
            Collection<String> all = setupDao.findFreshValues(property);
            Assert.assertEquals("both duplicate rows must be returned, not one arbitrary row: " + all,
                    2, all.size());

            LongTermCache cache = new LongTermCache();
            cache.setSetupManager(setupManager);
            Assert.assertEquals("the newest duplicate must win (inserted " + firstInserted + " then "
                    + secondInserted + ") — a stale pick means invalidations from another node are "
                    + "never seen", Long.valueOf(expected), cache.getLastClearTimeFresh(PROPERTY));
        } finally {
            try (Connection c = ds.getConnection()) {
                deleteByProperty(c, property);
            }
        }
    }

    private void insert(Connection c, String id, String property, String value) throws Exception {
        try (java.sql.PreparedStatement ps = c.prepareStatement(
                "INSERT INTO wf_setup (id, property, value) VALUES (?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, property);
            ps.setString(3, value);
            ps.executeUpdate();
        }
    }

    private void deleteByProperty(Connection c, String property) throws Exception {
        try (java.sql.PreparedStatement ps = c.prepareStatement(
                "DELETE FROM wf_setup WHERE property = ?")) {
            ps.setString(1, property);
            ps.executeUpdate();
        }
    }

    /** The single value under a property, or null. Fails loudly if the fixture produced duplicates. */
    private static String only(Collection<String> values) {
        if (values.isEmpty()) {
            return null;
        }
        Assert.assertEquals("fixture produced unexpected duplicate rows: " + values, 1, values.size());
        return values.iterator().next();
    }
}
