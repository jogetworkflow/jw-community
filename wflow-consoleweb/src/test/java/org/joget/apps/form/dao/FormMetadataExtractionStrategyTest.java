package org.joget.apps.form.dao;

import java.io.InputStream;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.AopTestUtils;

/**
 * Guards the #2253 setting itself, so that deleting it is a test failure rather than a silent
 * return to whole-catalog metadata reads.
 *
 * <p>The fix is configuration spread over six files — one Java call site and five Spring
 * {@code SessionFactory} beans (a sixth lives in the separate joget-enterprise repo). Its effect is
 * invisible to every functional test: with the setting removed, the schema is still maintained
 * correctly, just far more expensively. Nothing else in the suite would notice, and the cost is
 * only observable server-side on a large database, which CI does not have. So this test asserts the
 * configuration is present rather than trying to measure its effect.</p>
 *
 * <p>The Java assertion is not a string comparison against the source: it builds a real
 * {@link MetadataSources} through {@link FormDataDaoImpl#createMetadataSources()} and reads the
 * setting back out of the resulting service registry, so it fails if the property is set to the
 * wrong key, overwritten later in the method, or lost to a merge.</p>
 *
 * @see <a href="https://gitlab.joget.org/joget/jw-community/-/issues/2253">#2253</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class FormMetadataExtractionStrategyTest {

    /**
     * Spring context files carrying a SessionFactory bean with {@code hbm2ddl.auto=update}.
     *
     * <p>Deliberately a superset across branches: {@code wfsharkApplicationContext.xml} is a DDL
     * site on 9.1-SNAPSHOT and does not exist on 8.2-SNAPSHOT. A context that is not on the
     * classpath is skipped rather than failed, so this file forward-merges unchanged — but see
     * {@link #MIN_CONTEXTS_CHECKED}, which stops "skipped everything" from passing silently.</p>
     *
     * <p>ADD TO THIS LIST when a new SessionFactory with hbm2ddl.auto=update appears. Two beans
     * were originally missed on 8.2 because a grep found three of five.</p>
     */
    private static final String[] DDL_CONTEXTS = {
        "commonsApplicationContext.xml",    // setupSessionFactory
        "appsApplicationContext.xml",       // appSessionFactory + reportSessionFactory
        "directoryApplicationContext.xml",  // directorySessionFactory
        "wfengineApplicationContext.xml",   // workflowUpdateSessionFactory
        "wfsharkApplicationContext.xml"     // 9.1-SNAPSHOT only
    };

    /** Fewer than this many contexts actually inspected means the test is not testing anything. */
    private static final int MIN_CONTEXTS_CHECKED = 3;

    /**
     * The DDL {@code SessionFactory} beans, by Spring bean id.
     *
     * <p>Superset across branches, same rule as {@link #DDL_CONTEXTS}: a bean that is not in this
     * context is skipped, not failed, but {@link #MIN_BEANS_CHECKED} stops "skipped everything"
     * from passing.</p>
     */
    private static final String[] DDL_BEANS = {
        "appSessionFactory",
        "reportSessionFactory",
        "setupSessionFactory",
        "directorySessionFactory",
        "workflowUpdateSessionFactory",
        "sharkWorkflowUpdateSessionFactory"   // 9.1-SNAPSHOT only
    };

    /** Fewer than this many beans actually built means the test is not testing anything. */
    private static final int MIN_BEANS_CHECKED = 2;

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The XML {@code <prop>} must survive bean construction and still be in effect on the BUILT
     * SessionFactory.
     *
     * <p>Asserting the XML text (as {@link #everyDdlSessionFactoryContextPinsTheExtractionStrategy}
     * does) is not sufficient on its own, because
     * {@code InMemoryCacheSessionFactoryBean.setHibernateProperties()} does
     * {@code hibernateProperties.putAll(cacheManagerProperties)} — cache-manager properties
     * OVERWRITE the XML. Today neither implementation returns this key
     * ({@code NoInMemoryCacheManager} returns an empty Properties and is disabled;
     * {@code IgniteCacheManager} returns only the four cache keys), so nothing is lost — but that is
     * a property of code in another module, and in the enterprise repo at that. This test reads the
     * value back out of the live SessionFactory so that if a cache manager ever starts supplying it,
     * the silent revert to whole-catalog reads is a test failure instead of a support ticket.</p>
     */
    @Test
    public void everyDdlSessionFactoryBeanHasIndividuallyInEffect() {
        int checked = 0;
        StringBuilder skipped = new StringBuilder();
        for (String beanName : DDL_BEANS) {
            if (!applicationContext.containsBean(beanName)) {
                // Not on this branch / not in this context.
                skipped.append(' ').append(beanName);
                continue;
            }
            SessionFactory sf = (SessionFactory) applicationContext.getBean(beanName);
            Map<String, Object> settings = ((SessionFactoryImplementor) sf).getServiceRegistry()
                    .getService(ConfigurationService.class).getSettings();
            Object strategy = settings.get(FormDataDaoImpl.HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY);
            checked++;
            Assert.assertEquals(beanName + " runs hbm2ddl but its BUILT SessionFactory does not have "
                    + FormDataDaoImpl.HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY + "=individually in"
                    + " effect. The XML <prop> is present but was lost during bean construction —"
                    + " most likely InMemoryCacheSessionFactoryBean.setHibernateProperties()"
                    + " putAll-ing a cache manager property over it. Startup has silently reverted"
                    + " to reading the WHOLE database catalog (#2253).",
                    "individually", String.valueOf(strategy));
        }
        Assert.assertTrue("only " + checked + " DDL SessionFactory bean(s) were in this context"
                + " (skipped:" + skipped + ") — this test would pass without checking anything.",
                checked >= MIN_BEANS_CHECKED);
    }

    @Test
    public void formDaoBuildsItsMetadataSourcesWithIndividuallyExtraction() {
        FormDataDaoImpl dao = (FormDataDaoImpl) AopTestUtils.getUltimateTargetObject(formDataDao);
        MetadataSources sources = dao.createMetadataSources();

        Map<String, Object> settings = sources.getServiceRegistry()
                .getService(ConfigurationService.class).getSettings();
        Object strategy = settings.get(FormDataDaoImpl.HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY);

        Assert.assertEquals("FormDataDaoImpl.createMetadataSources() must configure "
                + FormDataDaoImpl.HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY + "=individually."
                + " Without it every form schema change reads the column metadata of the WHOLE"
                + " database (#2253): measured at 46,678 rows and ~3.9s per call on a"
                + " 46,679-column database.", "individually", String.valueOf(strategy));
    }

    /**
     * Every Spring context that runs {@code hbm2ddl.auto=update} must also pin the extraction
     * strategy. Read from the CLASSPATH, not the source tree, so it also catches a module that was
     * not rebuilt.
     */
    @Test
    public void everyDdlSessionFactoryContextPinsTheExtractionStrategy() throws Exception {
        int checked = 0;
        StringBuilder skipped = new StringBuilder();
        for (String resource : DDL_CONTEXTS) {
            String xml = read(resource);
            if (xml == null) {
                // Not on this branch / not a dependency of this module.
                skipped.append(' ').append(resource);
                continue;
            }
            checked++;
            if (!xml.contains("hibernate.hbm2ddl.auto\">update<")) {
                // This context no longer runs DDL at all; nothing to pin.
                continue;
            }
            int ddlBeans = count(xml, "hibernate.hbm2ddl.auto\">update<");
            int pinned = count(xml, FormDataDaoImpl.HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY);
            Assert.assertEquals(resource + " has " + ddlBeans + " SessionFactory bean(s) with"
                    + " hbm2ddl.auto=update but " + pinned + " with"
                    + " hibernate.hbm2ddl.jdbc_metadata_extraction_strategy. Every bean that runs"
                    + " DDL must pin the strategy, or it silently reverts to reading the whole"
                    + " catalog on startup (#2253).", ddlBeans, pinned);
        }
        Assert.assertTrue("only " + checked + " DDL context(s) were on the classpath (skipped:"
                + skipped + ") — this test would pass without checking anything. Fix the classpath"
                + " or the DDL_CONTEXTS list.", checked >= MIN_CONTEXTS_CHECKED);
    }

    private static String read(String resource) throws Exception {
        InputStream is = FormMetadataExtractionStrategyTest.class.getClassLoader()
                .getResourceAsStream(resource);
        if (is == null) {
            return null;
        }
        try {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toString("UTF-8");
        } finally {
            is.close();
        }
    }

    private static int count(String haystack, String needle) {
        int n = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) {
            n++;
        }
        return n;
    }
}
