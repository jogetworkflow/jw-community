package org.joget.apps.form.dao;

import jakarta.persistence.EntityNotFoundException;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.SetupManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.sql.DataSource;
import javax.xml.transform.TransformerException;
import org.apache.commons.collections.map.LRUMap;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.service.FormService;
import org.joget.commons.cache.InMemoryCacheManager;
import org.joget.commons.util.DynamicDataSourceManager;
import static org.joget.commons.util.DynamicDataSourceManager.getProperties;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class FormDataDaoImpl implements FormDataDao {

    public static final String FORM_MAPPING_DIRECTORY = "app_forms";
    /**
     * Makes Hibernate read JDBC metadata for the tables it is actually migrating, instead of the
     * whole database catalog.
     *
     * <p>Every form schema change runs {@link #internalUpdateSchema}, and Hibernate's DEFAULT
     * strategy for that (<code>grouped</code>) loads the column metadata of EVERY table in the
     * schema before it can decide whether one form table needs a new column. On SQL Server that
     * single call is the <code>DECLARE @mssqljdbc_temp_sp_columns_result ...</code> statement in
     * #2253: measured at 46,678 rows and ~3.9 s per call against a 46,679-column database, to add
     * one column to a 12-column table. The cost is a function of the customer's whole schema and
     * has nothing to do with the form being changed, which is why it is ~1 s in a test environment
     * and ~17 s in production.
     *
     * <p><code>individually</code> looks up only the tables being migrated — one form table here —
     * so the cost stops scaling with the rest of the database.
     *
     * <p>Declared as a literal rather than Hibernate's own constant on purpose: it lives on
     * <code>AvailableSettings</code> in Hibernate 5.6 (DX8) but on <code>SchemaToolingSettings</code>
     * in 6.6 (DX9), and this class has to compile on both for the DX8 -> DX9 forward merge. The
     * property name itself is identical in both versions.
     *
     * @see <a href="https://gitlab.joget.org/joget/jw-community/-/issues/2253">#2253</a>
     */
    public static final String HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY = "hibernate.hbm2ddl.jdbc_metadata_extraction_strategy";

    public static final String FORM_PREFIX_ENTITY = "FormRow_";
    public static final String FORM_PREFIX_TABLE_NAME = "app_fd_";
    public static final String FORM_PROPERTY_TABLE_NAME = "tableName";
    public static final String WORKFLOW_ASSIGNMENT = "WORKFLOW_ASSIGNMENT";
    public static final String FORM_PREFIX_COLUMN = "c_";
    public static final int ACTION_TYPE_LOAD = 0;
    public static final int ACTION_TYPE_STORE = 1;
    public static final Set<String> RESERVED_KEYWORDS = Collections.unmodifiableSet(
        new HashSet<>(java.util.Arrays.asList("class", "true", "false", "null"))
    );
    private static final Pattern LEGACY_CUSTOM_PROPERTY_NUMERIC_SORT_PATTERN = Pattern.compile("(\\b\\w+\\.customProperties\\.[0-9a-zA-Z_][0-9a-zA-Z_]*\\b)\\s*\\+\\s*0+(?:\\.0+)?\\b");

    private FormDefinitionDao formDefinitionDao;
    private BuilderDefinitionDao builderDefinitionDao;
    private FormService formService;
    private FormColumnCache formColumnCache;

    /**
     * Answers "what columns does this table have?" — see {@link FormColumnResolver}.
     *
     * <p>Owned rather than Spring-injected so the existing setters stay the single wiring point:
     * every collaborator it needs is one this DAO is already given, and forwarding them keeps
     * {@code appsApplicationContext.xml} and every existing caller unchanged.</p>
     */
    private FormColumnResolver columnResolver = new FormColumnResolver();

    private Cache formSessionFactoryCache;
    private Cache joinFormSessionFactoryCache;
    private Cache formPersistentClassCache;

    /**
     * Pairs a {@link SessionFactory} with the {@link PersistentClass} it was actually built from, so
     * a reader can verify the two still belong together instead of trusting whichever provenance
     * happens to be filed under the same key. See {@code formSessionFactorySourceCache}'s javadoc:
     * without the identity check this exists for, two independent map reads/writes for {@code sf}
     * and its provenance can interleave with a concurrent rebuild and pair a NEW {@code sf} with
     * STALE provenance (or vice versa) - reviewer-confirmed 2026-09-09 against an earlier version of
     * this fix that used two separately-read entries.
     */
    static final class SessionFactoryProvenance {
        final SessionFactory sf;
        final PersistentClass pc;

        SessionFactoryProvenance(SessionFactory sf, PersistentClass pc) {
            this.sf = sf;
            this.pc = pc;
        }
    }

    /**
     * Tracks what each cached {@code formSessionFactoryCache} entry was actually built from, keyed
     * identically (by {@code getSessionFactoryCacheKey}), as a {@code (sf, pc)} pair so a reader can
     * check {@code provenance.sf == sf} before trusting it (see {@link SessionFactoryProvenance}).
     * Deliberately NOT the Spring-wired {@code formPersistentClassCache}: that cache expires on its
     * own fixed 120s TTL independently of {@code formSessionFactoryCache} (which is eternal) and is
     * also independently repopulated by {@code normalizeCustomPropertiesForAlias} for query-string
     * normalization, unrelated to this DAO's own generation tracking. Kept in lock-step with
     * {@code formSessionFactoryCache} at every write/clear site - see {@code getSessionFactory},
     * {@code clearFormCache}, {@code clearFormTableCache} and {@code clearCache} - so it can answer
     * "does the CACHED sf match the mapping now in hand", not just "is the mapping now in hand
     * current" (#2085 cross-node staleness, GitLab note_48496).
     *
     * <p>Bounded rather than a plain unbounded map: {@code formSessionFactoryCache}'s own backing
     * region ({@code SF_CACHE}) is a 5000-entry LRU heap, shared with {@code joinFormSessionFactoryCache},
     * so its entries can be evicted without ever calling this DAO's own {@code remove} methods -
     * reviewer-confirmed 2026-09-09. An unbounded companion map would then grow without limit over a
     * long-running, high-table-churn multitenant deployment. {@link LRUMap} matches the pattern
     * already used in this class for {@code processedQueryCache} below.</p>
     */
    private final Map<String, SessionFactoryProvenance> formSessionFactorySourceCache =
            Collections.synchronizedMap(new LRUMap(5000));

    private static final Document formRowDocument;
    static {
        InputStream is = null;
        try {
            is = Form.class.getResourceAsStream("/org/joget/apps/form/model/FormRow.hbm.xml");
            formRowDocument = XMLUtil.loadDocument(is);
        } catch (Exception e) {
            throw new HibernateException("Unable to load FormRow.hbm.xml", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
    }
       
    public FormDefinitionDao getFormDefinitionDao() {
        return formDefinitionDao;
    }

    public void setFormDefinitionDao(FormDefinitionDao formDefinitionDao) {
        this.formDefinitionDao = formDefinitionDao;
        columnResolver.setFormDefinitionDao(formDefinitionDao);
    }

    public BuilderDefinitionDao getBuilderDefinitionDao() {
        return builderDefinitionDao;
    }

    public void setBuilderDefinitionDao(BuilderDefinitionDao builderDefinitionDao) {
        this.builderDefinitionDao = builderDefinitionDao;
        columnResolver.setBuilderDefinitionDao(builderDefinitionDao);
    }

    public FormService getFormService() {
        return formService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
        columnResolver.setFormService(formService);
    }

    public FormColumnCache getFormColumnCache() {
        return formColumnCache;
    }

    public void setFormColumnCache(FormColumnCache formColumnCache) {
        this.formColumnCache = formColumnCache;
        columnResolver.setFormColumnCache(formColumnCache);
    }

    /** The column resolver this DAO delegates {@link #getFormDefinitionColumnNames} to. */
    public FormColumnResolver getColumnResolver() {
        return columnResolver;
    }

    /**
     * Replaces the column resolver — an extension and test seam.
     *
     * <p>Collaborators already given to this DAO are copied across, and the setters above keep
     * forwarding to whichever resolver is current, so wiring order does not matter.</p>
     */
    public void setColumnResolver(FormColumnResolver columnResolver) {
        columnResolver.setFormDefinitionDao(formDefinitionDao);
        columnResolver.setBuilderDefinitionDao(builderDefinitionDao);
        columnResolver.setFormService(formService);
        columnResolver.setFormColumnCache(formColumnCache);
        this.columnResolver = columnResolver;
    }

    /**
     * Method used by system to set cache object
     * @param cacheManager
     */
    // #2317: wrap each fetch in net.sf.ehcache.Cache (Joget's own JSR-107 shim, matching
    // EhCacheFactoryBean.getObject()) so MultiTenantPluginAspect's guards -- which match by
    // declaring type, not by name -- can see these caches at all; the raw JSR-107 provider
    // objects cacheManager.getCache(..) alone returns are a different class the aspect's
    // pointcuts never match. registerReadSensitive(..) on each, matching 8.2's own
    // setFormSessionFactoryCache/setJoinFormSessionFactoryCache/setFormPersistentClassCache
    // setters exactly (this class holds live Hibernate SessionFactory/PersistentClass objects --
    // reading one directly, not just mutating it, is itself the harm; see ManagedCacheRegistry's
    // own javadoc). Also registers each alias so AppUtil.getCache("org.joget.cache.SF_CACHE")/
    // AppUtil.getCache("default") return these SAME governed instances instead of falling
    // through to a separate, ungoverned raw fetch -- unlike fluCache/dynamicDataSourceManagerCache,
    // no PlatformCacheRegistrar-discoverable bean exists for either alias on 9.1, so that gap was
    // otherwise wide open regardless of this class's own registration.
    // Independent review (Sol) finding: constructs each into a LOCAL variable and registers it
    // BEFORE assigning to the field -- this setter is itself now guarded (see
    // MultiTenantPluginAspect.formDataDaoCacheSetterGuardCheck's setCacheManager(..) entry), but
    // ManagedCacheRegistry.registerReadSensitive(..) is ALSO independently guarded and can throw
    // for a restricted caller who somehow still reaches this method; assigning each field only
    // after its registration succeeds means a thrown SecurityException leaves that field
    // completely untouched, never pointing at an attacker-supplied cache.
    public void setCacheManager(CacheManager cacheManager) {
        net.sf.ehcache.Cache wrappedFormSessionFactoryCache =
                new net.sf.ehcache.Cache(cacheManager.getCache("org.joget.cache.SF_CACHE"));
        org.joget.commons.util.ManagedCacheRegistry.registerReadSensitive(wrappedFormSessionFactoryCache);
        org.joget.commons.util.PlatformCacheRegistrar.registerAlias(wrappedFormSessionFactoryCache);
        this.formSessionFactoryCache = wrappedFormSessionFactoryCache;

        net.sf.ehcache.Cache wrappedJoinFormSessionFactoryCache =
                new net.sf.ehcache.Cache(cacheManager.getCache("org.joget.cache.SF_CACHE"));
        org.joget.commons.util.ManagedCacheRegistry.registerReadSensitive(wrappedJoinFormSessionFactoryCache);
        this.joinFormSessionFactoryCache = wrappedJoinFormSessionFactoryCache;

        net.sf.ehcache.Cache wrappedFormPersistentClassCache =
                new net.sf.ehcache.Cache(cacheManager.getCache("default"));
        org.joget.commons.util.ManagedCacheRegistry.registerReadSensitive(wrappedFormPersistentClassCache);
        // "default" is also SecureDataEncryptionImpl's alias (joget-enterprise) -- both wrappers
        // are equally registerReadSensitive, so whichever one AppUtil.getCache("default") ends up
        // returning is equally protected; not registered here to avoid a startup-order-dependent
        // overwrite of the other's registration for no behavioral benefit.
        this.formPersistentClassCache = wrappedFormPersistentClassCache;
    }

    /**
     * Loads a data row for a form based on the primary key
     * @param form
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    public FormRow load(Form form, String primaryKey) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        return internalLoad(entityName, tableName, primaryKey);
    }
    
    /**
     * Loads a data row for a form based on the primary key
     * @param formDefId
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    public FormRow load(String formDefId, String tableName, String primaryKey) {
        String entityName = getFormEntityName(formDefId);
        tableName = getFormTableName(formDefId, tableName);
        return internalLoad(entityName, tableName, primaryKey);
    }

    /**
     * Loads a data row for a form based on the primary key. 
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param form
     * @param primaryKey
     * @return 
     */
    @Override
    public FormRow loadWithoutTransaction(Form form, String primaryKey) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        return internalLoad(entityName, tableName, primaryKey);
    }
    
    /**
     * Loads a data row for a form based on the primary key. 
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param formDefId
     * @param tableName
     * @param primaryKey
     * @return 
     */
    @Override
    public FormRow loadWithoutTransaction(String formDefId, String tableName, String primaryKey) {
        String entityName = getFormEntityName(formDefId);
        tableName = getFormTableName(formDefId, tableName);
        return internalLoad(entityName, tableName, primaryKey);
    }

    /**
     * Loads a data row for an entity and table based on the primary key
     * @param entityName
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    protected FormRow internalLoad(String entityName, String tableName, String primaryKey) {
        // get hibernate session
        Session session = getHibernateSession(entityName, tableName, null, ACTION_TYPE_LOAD);
        
        // load by primary key
        FormRow row = null;
        try {
            FormRowSet rows = internalFind(entityName, tableName, "where id=?", new String[]{primaryKey}, null, null, 0, 1);
            
            if (rows != null && !rows.isEmpty()) {
                row = (FormRow) rows.get(0);
            }
        } catch (ObjectRetrievalFailureException e) {
            // not found, ignore
        } catch (EntityNotFoundException e) {
            // not found, ignore
        } catch (ObjectNotFoundException e) {
            // not found, ignore
        } finally {
            closeSession(session);
        }
        return row;
    }

    /**
     * Loads a data row for a table based on the primary key
     * @param tableName
     * @param columnName is not used
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    public FormRow loadByTableNameAndColumnName(String tableName, String columnName, String primaryKey) {
        if (!tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName;
        }
        
        return internalLoad(tableName, tableName, primaryKey);
    }

    /**
     * Query to find a list of matching form rows.
     * @param formDefId
     * @param tableName
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    @Override
    public FormRowSet find(String formDefId, String tableName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        final String entityName = getFormEntityName(formDefId);
        final String newTableName = getFormTableName(formDefId, tableName);

        return internalFind(entityName, newTableName, condition, params, sort, desc, start, rows);
    }
    
    /**
     * Query to find a list of matching form rows.
     * @param form
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    @Override
    public FormRowSet find(Form form, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        final String entityName = getFormEntityName(form);
        final String tableName = getFormTableName(form);

        return internalFind(entityName, tableName, condition, params, sort, desc, start, rows);
    }
    
    /**
     * Query to find a list of matching form rows.
     * @param entityName
     * @param tableName
     * @param condition
     * @param params
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    protected FormRowSet internalFind(final String entityName, final String tableName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        // get hibernate template
        Session session = getHibernateSession(tableName, tableName, null, ACTION_TYPE_LOAD);

        try {
            String query = "SELECT e FROM " + tableName + " e ";
            if (condition != null) {
                String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
                newCondition = replaceColumnNameWithPrefix(tableName, newCondition);
                query += newCondition;
            }

            if ((sort != null && !sort.trim().isEmpty()) && !query.toLowerCase().contains("order by")) {
                String sortProperty = sort;
                if (!FormUtil.PROPERTY_ID.equals(sortProperty) && !FormUtil.PROPERTY_DATE_CREATED.equals(sortProperty) && !FormUtil.PROPERTY_DATE_MODIFIED.equals(sortProperty)
                         && !FormUtil.PROPERTY_CREATED_BY.equals(sortProperty)  && !FormUtil.PROPERTY_CREATED_BY_NAME.equals(sortProperty)
                         && !FormUtil.PROPERTY_MODIFIED_BY.equals(sortProperty)  && !FormUtil.PROPERTY_MODIFIED_BY_NAME.equals(sortProperty)) {
                    sortProperty = FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + sort;
                }
                query += " ORDER BY e." + sortProperty;

                if (desc) {
                    query += " DESC";
                }
            }
            Query q = session.createQuery(processQuery(query));
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            cacheManager.setCacheable(q, tableName);

            int s = (start == null) ? 0 : start;
            
            //setting this unnecessarily causing performance issue 
            if (s > 0) {
                q.setFirstResult(s);
            }

            if (rows != null && rows > 0) {
                q.setMaxResults(rows);
            }

            if (params != null) {
                int i = 1;
                for (Object param : params) {
                    q.setParameter(i, param);
                    i++;
                }
            }

            Collection result = q.list();

            FormRowSet rowSet = new FormRowSet();
            rowSet.addAll(result);
            return rowSet;
        } finally {
            closeSession(session);
        }
    }

    /**
     * Query total row count for a form.
     * @param formDefId
     * @param tableName
     * @param condition
     * @param params
     * @return 
     */
    @Override
    public Long count(String formDefId, String tableName, final String condition, final Object[] params) {

        final String entityName = getFormEntityName(formDefId);
        final String newTableName = getFormTableName(formDefId, tableName);

        return internalCount(entityName, newTableName, condition, params);
    }
    
    /**
     * Query total row count for a form.
     * @param form
     * @param condition
     * @param params
     * @return
     */
    @Override
    public Long count(Form form, final String condition, final Object[] params) {

        final String entityName = getFormEntityName(form);
        final String tableName = getFormTableName(form);

        return internalCount(entityName, tableName, condition, params);
    }
    
    /**
     * Query total row count for a form.
     * @param entityName
     * @param tableName
     * @param condition
     * @param params
     * @return
     */
    protected Long internalCount(final String entityName, final String tableName, final String condition, final Object[] params) {
        // get hibernate template
        Session session = getHibernateSession(tableName, tableName, null, ACTION_TYPE_LOAD);
        try {
            String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
            newCondition = replaceColumnNameWithPrefix(tableName, newCondition);
            Query q = session.createQuery(processQuery("SELECT COUNT(*) FROM " + tableName + " e " + newCondition));
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            cacheManager.setCacheable(q, tableName);

            if (params != null) {
                int i = 1;
                for (Object param : params) {
                    q.setParameter(i, param);
                    i++;
                }
            }

            return ((Long) q.list().get(0));
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * Replace the column name with c_ prefix to e.customProperties.
     * @param tableName
     * @param condition
     * @return 
     */
    protected String replaceColumnNameWithPrefix(String tableName, String condition) {
        String lower = condition.toLowerCase();
        if (condition != null && !condition.isEmpty() && (lower.contains(" c_") || lower.contains("(c_"))) {
            //there is no function or method name started with c_, so i think it is safe to replace any ` c_` & `(c_` directly
            condition = condition.replaceAll("(?i)([ \\(])c_", "$1" + StringUtil.escapeRegex("e."+FormUtil.PROPERTY_CUSTOM_PROPERTIES+"."));
        }
        return condition;
    }

    /**
     * Query to find find primary key based on a field name and it's value.
     * @param form
     * @param fieldName
     * @param value
     * @return
     */
    @Override
    public String findPrimaryKey(Form form, final String fieldName, final String value) {
        final String entityName = getFormEntityName(form);
        final String tableName = getFormTableName(form);

        return internalFindPrimaryKey(entityName, tableName, fieldName, value);
    }
    
    /**
     * Query to find find primary key based on a field name and it's value.
     * @param formDefId
     * @param tableName
     * @param fieldName
     * @param value
     * @return
     */
    @Override
    public String findPrimaryKey(String formDefId, String tableName, final String fieldName, final String value) {
        final String entityName = getFormEntityName(formDefId);
        final String newTableName = getFormTableName(formDefId, tableName);

        return internalFindPrimaryKey(entityName, newTableName, fieldName, value);
    }
    
    /**
     * Query to find find primary key based on a field name and it's value.
     * @param entityName
     * @param tableName
     * @param fieldName
     * @param value
     * @return
     */
    protected String internalFindPrimaryKey(final String entityName, final String tableName, final String fieldName, final String value) {
        // get hibernate template
        Session session = getHibernateSession(tableName, tableName, null, ACTION_TYPE_LOAD);
        try {
            String query = "SELECT e.id FROM " + tableName + " e WHERE " + FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + fieldName + " = ?1 order by e.dateCreated";

            Query q = session.createQuery(processQuery(query));
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            cacheManager.setCacheable(q, tableName);

            q.setFirstResult(0);
            q.setMaxResults(1);
            q.setParameter(1, value);

            List results = q.list();
            if (!results.isEmpty()) {
                return ((String) results.get(0));
            }
            return null;
        } finally {
            closeSession(session);
        }
    }

    /**
     * Saves (creates or updates) form data
     * @param form
     * @param rowSet
     */
    @Override
    public void saveOrUpdate(Form form, FormRowSet rowSet) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        internalSaveOrUpdate(entityName, tableName, rowSet);
    }
    
    /**
     * Saves (creates or updates) form data
     * @param formDefId
     * @param tableName
     * @param rowSet
     */
    @Override
    public void saveOrUpdate(String formDefId, String tableName, FormRowSet rowSet) {
        String entityName = getFormEntityName(formDefId);
        String newTableName = getFormTableName(formDefId, tableName);
        internalSaveOrUpdate(entityName, newTableName, rowSet);
    }

    /**
     * Saves (creates or updates) form data
     * @param entityName
     * @param tableName
     * @param rowSet 
     */
    protected void internalSaveOrUpdate(String entityName, String tableName, FormRowSet rowSet) {
        // get hibernate template
        Session session = getHibernateSession(entityName, tableName, rowSet, ACTION_TYPE_LOAD);

        try {
            // save the form data
            for (FormRow row : rowSet) {
                // check for bad keys
                Map<String, Object> temp = new HashMap<>();
                for (Object key : row.keySet()) {
                    String oldKey = String.valueOf(key);
                    String newKey = oldKey;
                    if (oldKey != null && !oldKey.isEmpty() && (Character.isDigit(oldKey.charAt(0)) || RESERVED_KEYWORDS.contains(oldKey.toLowerCase()))) {
                        newKey = "t__" + key;
                    } else if (oldKey.startsWith("t__")) {
                        String suffix = oldKey.substring(3);
                        if (!suffix.isEmpty() && (Character.isDigit(suffix.charAt(0)) || RESERVED_KEYWORDS.contains(suffix.toLowerCase()))) {
                            if (row.containsKey(suffix)) {
                                continue;
                            }
                        }
                    }
                    temp.put(newKey, row.get(key));
                }

                // clear and replace row values
                row.clear();
                row.putAll(temp);

                session.merge(entityName, row);
            }
            session.flush();
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * Call Hibernate to update DB schema
     * @param form
     * @param rowSet
     */
    @Override
    public void updateSchema(Form form, FormRowSet rowSet) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        Session session = getHibernateSession(entityName, tableName, rowSet, ACTION_TYPE_LOAD);
        
        closeSession(session);
    }

    /**
     * Call Hibernate to update DB schema
     * @param formDefId
     * @param tableName
     * @param rowSet
     */
    @Override
    public void updateSchema(String formDefId, String tableName, FormRowSet rowSet) {
        String entityName = getFormEntityName(formDefId);
        String newTableName = getFormTableName(formDefId, tableName);
        Session session = getHibernateSession(entityName, newTableName, rowSet, ACTION_TYPE_LOAD);
        
        closeSession(session);
    }

    /**
     * Delete form data by primary keys
     * @param form
     * @param primaryKeyValues 
     */
    @Override
    public void delete(Form form, String[] primaryKeyValues) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);

        internalDelete(entityName, tableName, primaryKeyValues);
    }
    
    /**
     * Delete form data by primary keys
     * @param formDefId
     * @param tableName
     * @param primaryKeyValues 
     */
    @Override
    public void delete(String formDefId, String tableName, String[] primaryKeyValues) {
        String entityName = getFormEntityName(formDefId);
        String newTableName = getFormTableName(formDefId, tableName);

        internalDelete(entityName, newTableName, primaryKeyValues);
    }
    
    /**
     * Delete form data by primary keys
     * @param formDefId
     * @param tableName
     * @param rows
     */
    @Override
    public void delete(String formDefId, String tableName, FormRowSet rows) {
        String entityName = getFormEntityName(formDefId);
        String newTableName = getFormTableName(formDefId, tableName);

        // get hibernate template
        Session session = getHibernateSession(entityName, newTableName, null, ACTION_TYPE_LOAD);
        try {
            // save the form data
            for (FormRow row : rows) {
                session.remove(row);
            }
            session.flush();
        } finally {
            closeSession(session);
        }
    }
    
    /**
     * Delete form data by primary keys
     * @param entityName
     * @param tableName
     * @param primaryKeyValues 
     */
    protected void internalDelete(String entityName, String tableName, String[] primaryKeyValues) {
        // get hibernate template
        Session session = getHibernateSession(entityName, tableName, null, ACTION_TYPE_LOAD);

        try {
            // save the form data
            for (String key : primaryKeyValues) {
                Object obj = this.internalLoad(entityName, tableName, key);
                if (obj != null) {
                    session.remove(obj);
                }
            }
            session.flush();
        } finally {
            closeSession(session);
        }
    }

    /**
     * Gets the generated hibernate entity name for the form
     * @param form
     * @return
     */
    @Override
    public String getFormEntityName(Form form) {
        String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
        String entityName = FormDataDaoImpl.FORM_PREFIX_ENTITY + formDefId;
        return entityName;
    }
    
    /**
     * Gets the generated hibernate entity name for the form
     * @param formDefId
     * @return 
     */
    @Override
    public String getFormEntityName(String formDefId) {
        String entityName = FormDataDaoImpl.FORM_PREFIX_ENTITY + formDefId;
        return entityName;
    }

    /**
     * Gets the defined table name for the form
     * @param form
     * @return 
     */
    @Override
    public String getFormTableName(Form form) {
        // determine table name
        String tableName = form.getPropertyString("tableName");
        if (tableName == null || tableName.trim().length() == 0) {
            // no table name specified, temp use ID
            String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            tableName = formDefId;
        }
        tableName = FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName;
        return tableName;
    }
    
    /**
     * Gets the defined table name for the form
     * @param formDefId
     * @param tableName
     * @return 
     */
    @Override
    public String getFormTableName(String formDefId, String tableName) {
        // determine table name
        if (tableName == null || tableName.trim().length() == 0) {
            // no table name specified, temp use ID
            tableName = formDefId;
        }
        tableName = FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName;
        return tableName;
    }

    /**
     * Clears in-memory cache of generated hibernate templates
     */
    public void clearCache() {
        joinFormSessionFactoryCache.removeAll();
        formSessionFactoryCache.removeAll();
        formPersistentClassCache.removeAll();
        formSessionFactorySourceCache.clear();
        formColumnCache.clear();
    }

    public void clearFormCache(Form form) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        formSessionFactoryCache.remove(getSessionFactoryCacheKey(entityName, tableName, null));
        formSessionFactorySourceCache.remove(getSessionFactoryCacheKey(entityName, tableName, null));
        formPersistentClassCache.remove(getPersistentClassCacheKey(entityName));
        
        // strip table prefix for form column cache
        if (tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }
        
        //clear cache for loading
        clearFormTableCache(tableName);
    }
    
    @Override
    public void clearFormTableCache(String tableName) {
        // reset(), not remove(): "forget this table" has to include the incremental base, or the
        // next build merges onto the very union the caller asked to discard.
        formColumnCache.reset(tableName);

        tableName = FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName;
        formSessionFactoryCache.remove(getSessionFactoryCacheKey(tableName, tableName, null));
        formSessionFactorySourceCache.remove(getSessionFactoryCacheKey(tableName, tableName, null));
        formPersistentClassCache.remove(getPersistentClassCacheKey(tableName));
        clearJoinCache(tableName);
        
        //remove mapping file to recreate
        // Guarded by the SAME "MAP:" lock createSessionFactoryWithSingleFlight takes, so this
        // delete cannot land between that method's write (XMLUtil.saveDocument) and its own
        // immediate re-read (metadataSources.addFile) of the SAME entity's mapping file - the
        // exact interleaving that would otherwise throw an uncaught MappingNotFoundException out
        // of a request that just built a fresh, correct mapping (#2085 follow-up finding,
        // 2026-09-08: this method was previously entirely unguarded, racing that guarded
        // write-then-read). Best-effort on timeout/nesting rather than throwing: this method is
        // called from app import and form save/publish, none of which expect or handle an
        // exception here, and skipping the delete outright would leave a stale mapping file
        // permanently uncleared - worse than the narrow window this closes.
        String path = getFormMappingPath();
        String filename = tableName + ".hbm.xml";
        File mappingFile = new File(path, filename);
        String key = "MAP:" + HostManager.getCurrentProfile() + ":" + tableName;
        int timeoutSeconds = FormBuildGuard.isNested() ? 0 : getLockTimeoutSeconds();
        ReentrantLock lock = FormBuildGuard.tryLock(key, timeoutSeconds);
        if (lock == null) {
            LogUtil.warn(FormDataDaoImpl.class.getName(), "Could not acquire the mapping build lock"
                    + " for \"" + tableName + "\" before deleting its cached Hibernate mapping file;"
                    + " deleting without it. A concurrent rebuild for this table may rarely see a"
                    + " transient MappingException and rebuild again on retry.");
        }
        try {
            if (mappingFile.exists()) {
                mappingFile.delete();
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }
    
    protected void clearJoinCache(String entity) {
        if (!entity.startsWith(FORM_PREFIX_TABLE_NAME)) {
            entity = FORM_PREFIX_TABLE_NAME + entity;
        }
        for (Iterator i = joinFormSessionFactoryCache.iterator(); i.hasNext();) {
            Cache.Entry entry = (Cache.Entry)i.next();
            if (entry.getKey().toString().contains("[" + entity + "]")) {
                i.remove();
            }            
        }
    }
    
    protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        String cacheKey = profile + ";" + entityName + ";" + tableName + ";";
        if (rowSet != null) {
            for (FormRow row : rowSet) {
                cacheKey += ";" + row.keySet();
                break;
            }
        }
        return cacheKey;
    }
    
    protected String getJoinSessionFactoryCacheKey(String[] entities) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        String cacheKey = profile;
        if (entities != null && entities.length > 0) {
            for (String e : entities) {
                cacheKey += ";[" + e + "]";
            }
        }
        return cacheKey;
    }

    protected String getPersistentClassCacheKey(String entityName) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        String cacheKey = profile + ";PC:" + entityName + ";";
        return cacheKey;
    }
    
    /**
     * Gets the hibernate session for the entity
     * @param entityName
     * @param tableName
     * @param rowSet
     * @param actionType
     * @return
     */
    protected Session getHibernateSession(String entityName, String tableName, FormRowSet rowSet,  int actionType) throws HibernateException {
        Session session;
        if (actionType == ACTION_TYPE_LOAD) {
            // for load, use the db table as entity name
            entityName = tableName;
            
            // find session factory and open session
            SessionFactory sf = findSessionFactory(entityName, tableName, null, actionType);
            session = sf.withOptions().autoJoinTransactions(true).openSession();
        } else {
            // find session factory and open session
            SessionFactory sf = findSessionFactory(entityName, tableName, rowSet, actionType);
            session = sf.withOptions().autoJoinTransactions(true).openSession();
        }
        return session;
    }  
    
    protected Session getJoinHibernateSession(String entityName, String[] joinsTables) {
        Session session;
        
        int size = 1;
        
        if (joinsTables != null) {
            size += joinsTables.length;
        }
        
        String[] entities = new String[size];
        if (!entityName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            entityName = FORM_PREFIX_TABLE_NAME + entityName;
        }
        entities[0] = entityName;
        
        if (joinsTables != null) {
            int i = 1;
            for (String e : joinsTables) {
                if (!e.startsWith(FORM_PREFIX_TABLE_NAME) && !WORKFLOW_ASSIGNMENT.equals(e)) {
                    e = FORM_PREFIX_TABLE_NAME + e;
                }
                entities[i] = e;
                i++;
            }
        }
        
        // find session factory and open session
        SessionFactory sf = findJoinSessionFactory(entities);
        session = sf.withOptions().autoJoinTransactions(true).openSession();
        
        return session;
    }

    /**
     * True if {@code builtFromPc} (what the cached {@code sf} was actually built from, tracked in
     * {@link #formSessionFactorySourceCache}) no longer matches {@code currentPc}'s own column set —
     * i.e. the cached {@code sf} is unverifiable or provably stale relative to the mapping now in
     * hand, regardless of whether that mapping is itself current. {@code builtFromPc == null} (no
     * provenance recorded — a cache entry from before this tracking existed, or one populated via a
     * non-LOAD path) fails closed: unknown provenance is not "unchanged".
     *
     * <p>Inherits {@link #hasMappingChanged}'s existing "an empty column set is not a change"
     * policy (reviewer-noted 2026-09-08/09): if {@code currentPc} itself carries zero columns, this
     * cannot distinguish that from "identical to builtFromPc" and reports no difference. Pre-existing
     * behaviour, not introduced here — see {@code hasMappingChanged}'s own javadoc for why.</p>
     */
    private boolean sessionFactoryMappingDiffersFrom(PersistentClass builtFromPc, PersistentClass currentPc) {
        if (builtFromPc == null) {
            return true;
        }
        return hasMappingChanged(builtFromPc, columnNamesOf(currentPc));
    }

    /** The custom-property column names {@code pc} itself carries, t__-prefix stripped. */
    private Collection<String> columnNamesOf(PersistentClass pc) {
        Property custom = pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES);
        Component customComponent = (Component) custom.getValue();
        Set<String> names = new HashSet<String>();
        Iterator i = customComponent.getProperties().iterator();
        while (i.hasNext()) {
            Property property = (Property) i.next();
            String propertyName = property.getName();
            if (propertyName.startsWith("t__")) {
                propertyName = propertyName.substring(3);
            }
            names.add(propertyName);
        }
        return names;
    }

    protected SessionFactory findSessionFactory(String entityName, String tableName, FormRowSet rowSet, int actionType) throws HibernateException {
        PersistentClass pc = null;

        // lookup cache
        String cacheKey = getSessionFactoryCacheKey(entityName, tableName, rowSet);
        SessionFactory sf = (SessionFactory)formSessionFactoryCache.get(cacheKey);
        // What sf itself was built from, if anything - see formSessionFactorySourceCache. These two
        // reads are against SEPARATE maps and are NOT atomic with each other or with a concurrent
        // rebuild's writes, so the identity check below (provenance.sf == sf) is load-bearing, not
        // defensive: without it, a rebuild racing between these two lines can pair THIS sf with a
        // stale-or-not-yet-updated provenance record (or vice versa) filed under the same key,
        // wrongly concluding they differ and closing a factory another thread just published, or
        // wrongly concluding they match when they do not (reviewer-confirmed 2026-09-09: Opus and
        // Sol independently found the same race against an earlier version of this fix that trusted
        // a same-key lookup without checking whose sf it actually described).
        PersistentClass sfBuiltFromPc = null;
        if (sf != null) {
            SessionFactoryProvenance provenance = formSessionFactorySourceCache.get(cacheKey);
            if (provenance != null && provenance.sf == sf) {
                sfBuiltFromPc = provenance.pc;
            }
        }
        if (sf != null) {
            if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " session factory found in cache");
            }
        }
            
        if (actionType == ACTION_TYPE_LOAD) {
            // find existing persistent class for comparison
            if (sf != null) {
                pc = (PersistentClass)formPersistentClassCache.get(getPersistentClassCacheKey(entityName));
                if (pc != null) {
                    if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " PersistentClass found in cache");
                    }
                }
            }

            Metadata metadata = null;
            if (pc == null) {
                try {
                    // locate entity hbm mapping file
                    String path = getFormMappingPath();
                    String filename = entityName + ".hbm.xml";
                    File mappingFile = new File(path, filename);
                    if (mappingFile.exists()) {
                        // load existing mapping
                        MetadataSources metadataSources = createMetadataSources();
                        metadataSources.addFile(mappingFile);
                        metadata = metadataSources.buildMetadata();
                        
                        pc = metadata.getEntityBinding(entityName);
                        if (pc != null) {
                            if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " loaded from mapping file " + mappingFile.getName());
                            }
                            // save into cache
                            formPersistentClassCache.put(getPersistentClassCacheKey(entityName), pc);
                        }
                    }
                } catch(MappingException me) {
                    // Could not read the XML file: either a parse failure on a corrupt/mid-write
                    // file (InvalidMappingException - the original, already-documented case), OR
                    // the file vanished between the exists() check above and addFile() actually
                    // opening it (MappingNotFoundException - a SIBLING of InvalidMappingException,
                    // not a subclass, so the narrower catch this replaced let it through
                    // uncaught). The second case is a real, live-confirmed race: addFile() reads
                    // and parses the file synchronously and eagerly (verified against this
                    // project's actual Hibernate 6.6.1 dependency), so a concurrent
                    // clearFormTableCache()/clearFormCache() deleting the SAME entity's mapping
                    // file - an unguarded delete, reachable from app import and form
                    // save/publish - can win the race between this thread's exists() check and
                    // its addFile() call, especially since createMetadataSources() in between
                    // does real work (builds a fresh Hibernate ServiceRegistry). Either way, pc
                    // stays null here and the mapping is regenerated below via the guarded
                    // single-flight rebuild path (#2085) - correct for BOTH causes, since a
                    // mapping this DAO cannot itself read is unverifiable regardless of why.
                }
            }
            if (pc != null) {
                // mapping exists, check for changes
                boolean changes = false;

                // check for table change
                if (!tableName.equals(pc.getTable().getName())) {
                    changes = true;
                }
                
                // check for cache access strategy
                String cacheAccessStrategy = pc.getCacheConcurrencyStrategy();
                if (!"transactional".equals(cacheAccessStrategy)) {
                    changes = true;
                }

                if (!changes) {
                    // Resolve the column set SYNCHRONOUSLY. This used to hop through
                    // syncFormDefinitionColumnNamesCache, which returns null while a background
                    // rebuild is in flight, and the null was then treated as "nothing changed" —
                    // so a node that had not yet seen the invalidation served a stale mapping and
                    // any HQL naming a new column blew up with UnknownPathException (#2085).
                    //
                    // Removing that hop does not reintroduce #7709: it only ever skipped work in
                    // the "column cache cold AND mapping unchanged" case, which is exactly the
                    // case where the work is required for correctness. When a rebuild IS needed,
                    // createSessionFactory already resolves the same column set synchronously on
                    // this thread. getFormDefinitionColumnNames is single-flighted per table, so
                    // concurrent callers collapse onto one load instead of stampeding.
                    Collection<String> formFields = getFormDefinitionColumnNames(tableName);

                    changes = hasMappingChanged(pc, formFields);
                }

                if (!changes && sf != null) {
                    // pc is current and trustworthy, but that alone does not prove the CACHED sf
                    // matches it - pc's own cache entry can have been repopulated (TTL expiry, or
                    // normalizeCustomPropertiesForAlias's independent write) without sf ever being
                    // rebuilt to match. Verify sf's own provenance directly instead of assuming it
                    // (#2085 cross-node staleness, note_48496) - cheap, in-memory, no disk/DB I/O,
                    // so this does not reintroduce the "rebuild on every TTL cycle" cost that a
                    // cruder "always rebuild when pc's cache misses" fix would (verified in
                    // FormSessionFactoryPcTtlRebuildPatchTest).
                    changes = sessionFactoryMappingDiffersFrom(sfBuiltFromPc, pc);
                }

                if (changes) {
                    if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " changes detected");
                    }

                    // properties changed, close session factory.
                    // isClosed() guard: several threads can reach this holding the SAME stale
                    // factory reference, and only the first should close it.
                    if (sf != null && !sf.isClosed()) {
                        sf.close();
                        if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                            LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " existing session factory closed");
                        }
                    }

                    // clear session factory to be recreated
                    sf = null;

                    //remove persistentClassCache
                    formPersistentClassCache.remove(getPersistentClassCacheKey(entityName));
                    // The closed sf's provenance record is now stale; the identity check on the read
                    // side would already ignore it once a NEW sf is cached under this key, but clear
                    // it explicitly rather than leave a record describing a closed factory around.
                    formSessionFactorySourceCache.remove(cacheKey);
                    clearJoinCache(entityName);
                } else if (metadata != null && sf == null) {
                    //no change, use existing mapping file if session factory not exist
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " get session factory");
                    sf = getSessionFactory(entityName, cacheKey, actionType, metadata, false);
                }
            } else if (sf != null) {
                // pc could not be resolved: it was not in formPersistentClassCache, AND the
                // on-disk <entity>.hbm.xml fallback above also came back empty (missing mapping
                // file, or a MappingException from reading it mid-rewrite by another thread's
                // createSessionFactoryWithSingleFlight, or mid-delete by clearFormTableCache).
                // formPersistentClassCache and
                // formSessionFactoryCache are separate cache regions with independent eviction
                // (formPersistentClassCache expires on a fixed TTL; the session factory cache is
                // eternal), so this is a routine, expected combination, not just a rare race.
                //
                // With no PersistentClass there is nothing left to verify sf's mapping against,
                // so this cannot silently fall through to "sf is open, reuse it" the way the code
                // below does — that would return a session factory whose staleness was simply
                // never checked (#2085). Treat it the same as a detected mapping change instead:
                // close it and let the block below rebuild it from scratch.
                if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " PersistentClass could not be resolved; treating cached session factory as unverifiable");
                }

                if (!sf.isClosed()) {
                    sf.close();
                }
                sf = null;
                formSessionFactorySourceCache.remove(cacheKey);
                clearJoinCache(entityName);
            }
        }
        
        if (sf == null || sf.isClosed()) {
            if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " create session factory");
            }
            sf = createSessionFactoryWithSingleFlight(entityName, tableName, rowSet, actionType, cacheKey);
        }
        return sf;
    }

    /**
     * Regenerates a mapping, collapsing concurrent regenerations of the same entity onto one.
     *
     * <p>Without this, every thread that detects a change closes the stale factory and then
     * independently writes {@code <entity>.hbm.xml}, builds a {@code SessionFactory} and runs
     * {@code SchemaUpdate} DDL — concurrent DDL against one table, N-1 wasted factories (which
     * are put into the cache one over another and may never be closed), and repeated writes of
     * the same file. Failing closed on change detection made this fire reliably where the old
     * fail-open path largely hid it, so it has to be handled here.</p>
     *
     * <p>Shares {@link FormBuildGuard} with the column build, and must: {@code createSessionFactory}
     * resolves columns, which runs plugin code, which can call {@code find()} and land back here.
     * Both mapping→columns and columns→mapping orderings therefore exist in production, and only
     * the guard's never-wait-while-nested rule stops them forming an ABBA deadlock.</p>
     *
     * <p><b>Nested callers are locked too, just never made to wait.</b> An earlier version returned
     * {@code createSessionFactory} directly when nested, which contradicted this method's own reason
     * for failing on timeout rather than proceeding unlocked: a nested regeneration reached through
     * plugin code can truncate and rewrite {@code <entity>.hbm.xml} while the thread holding the
     * stripe is parsing it — the exact corruption the timeout branch refuses to risk. A zero-timeout
     * {@code tryLock} cannot block, so the never-wait-while-nested rule is preserved and no ABBA
     * cycle can form, while the file still has exactly one writer.</p>
     */
    protected SessionFactory createSessionFactoryWithSingleFlight(String entityName, String tableName,
            FormRowSet rowSet, int actionType, String cacheKey) {
        String key = "MAP:" + HostManager.getCurrentProfile() + ":" + entityName;

        if (FormBuildGuard.holds(key)) {
            // This entity's regeneration is already on this thread's stack, and plugin code has
            // asked for it again. Recursing would rewrite the mapping file underneath the frame
            // that is parsing it, and the stripe is reentrant so the lock below would not stop it.
            throw new HibernateException("Re-entrant regeneration of the Hibernate mapping for \""
                    + entityName + "\" detected while the outer regeneration is still running."
                    + " Retry the request.");
        }

        // Zero while nested: a thread already inside a guarded build must not BLOCK on another
        // lock, but it must still hold this one to write the mapping file.
        int timeoutSeconds = FormBuildGuard.isNested() ? 0 : getLockTimeoutSeconds();
        ReentrantLock lock = FormBuildGuard.tryLock(key, timeoutSeconds);
        if (lock == null) {
            // Deliberately NOT regenerating outside the lock. Regeneration truncates and rewrites
            // <entity>.hbm.xml, and a second writer can be truncating that file while the first
            // is parsing it — file corruption, duplicate DDL, and factories overwriting each
            // other. Failing a request retriably is strictly better than that. Contrast the
            // column build, where a duplicated build is merely wasteful and so is allowed.
            String why = (timeoutSeconds > 0)
                    ? "Timed out after " + timeoutSeconds + "s waiting to regenerate"
                    : "Another thread is regenerating, and this one is already inside a build so it"
                      + " must not wait, for";
            throw new HibernateException(why + " the Hibernate mapping for \"" + entityName
                    + "\". Not regenerating outside the lock, as concurrent writers would corrupt the mapping file. Retry the request.");
        }

        try {
            // Double-check: a thread ahead of us may have already regenerated and cached it.
            SessionFactory existing = (SessionFactory) formSessionFactoryCache.get(cacheKey);
            if (existing != null && !existing.isClosed()) {
                if (LogUtil.isDebugEnabled(FormDataDaoImpl.class.getName())) {
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " session factory regenerated by another thread");
                }
                return existing;
            }
            return FormBuildGuard.runGuarded(key,
                    () -> createSessionFactory(entityName, tableName, rowSet, actionType));
        } finally {
            lock.unlock();
        }
    }

    /** Overridable so tests can drive the timeout paths without a 20s wait. */
    protected int getLockTimeoutSeconds() {
        return FormBuildGuard.DEFAULT_LOCK_TIMEOUT_SECONDS;
    }
    
    /**
     * Compares a cached Hibernate mapping against the form's current column set.
     *
     * <p>This is the correctness gate for cross-node staleness (#2085), so it FAILS CLOSED: an
     * unknown column set means the mapping cannot be vouched for, and an unverifiable mapping
     * must be regenerated rather than served. Regenerating one that turned out to be fine is
     * bounded, idempotent work; serving one that turned out to be stale is a 500 to the user the
     * moment any HQL names a column the mapping does not carry.</p>
     *
     * <p>An EMPTY column set is a different thing from an unknown one and is deliberately treated
     * as "no change": a table with no form definition and no builder definition legitimately
     * resolves to zero columns, and shrinking the mapping there would unmap columns that may
     * still hold data. Collapsing empty into unknown would also make every unmapped table
     * regenerate its mapping on every request.</p>
     *
     * @param pc the cached mapping
     * @param formFields the current column set; null means "could not be determined"
     * @return true if the mapping must be regenerated
     */
    protected boolean hasMappingChanged(PersistentClass pc, Collection<String> formFields) {
        if (formFields == FormColumnResolver.UNRESOLVED_COLUMNS) {
            // A cycle was cut, so we know nothing about this table's columns. Neither answer is
            // safe: "unchanged" serves a mapping we cannot vouch for (#2085 again), and "changed"
            // triggers a regeneration that re-enters the same cycle and publishes a mapping with
            // no custom columns. Abort without publishing anything and let the caller retry —
            // by then the outermost build has completed and cached, so there is no cycle left.
            throw new HibernateException("Cannot validate the Hibernate mapping for \"" + pc.getEntityName()
                    + "\": its form column set is unresolved because of a circular form dependency. Retry the request.");
        }
        if (formFields == null) {
            // Unknown - fail closed. See javadoc.
            return true;
        }
        if (formFields.isEmpty()) {
            // Known to be empty - deliberately not a change. See javadoc.
            return false;
        }

        Property custom = pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES);
        Component customComponent = (Component) custom.getValue();

        // compare existing mappings with new properties
        int size = customComponent.getPropertySpan();
        if (size != formFields.size()) {
            // dissimilar size, fields changed
            return true;
        }

        // similar size, so compare individual fields
        Iterator i = customComponent.getProperties().iterator();
        while (i.hasNext()) {
            Property property = (Property) i.next();
            String propertyName = property.getName();
            if (propertyName.startsWith("t__")) {
                propertyName = propertyName.substring(3);
            }
            boolean found = formFields.contains(propertyName);
            if (!found || RESERVED_KEYWORDS.contains(property.getName().toLowerCase())) {
                // property not found, fields changed
                return true;
            }
        }

        try {
            Property createdBy = pc.getProperty(FormUtil.PROPERTY_CREATED_BY);
            if (createdBy == null) {
                return true;
            }
        } catch (Exception ex) {
            return true;
        }

        return false;
    }

    protected SessionFactory findJoinSessionFactory(String[] entities) throws HibernateException {
        SessionFactory sf = null;        
        
        if (entities.length == 1) {
            sf = findSessionFactory(entities[0], entities[0], null, ACTION_TYPE_LOAD);
        } else {
            //check and refreash each mapping file for each entity
            for (String e : entities) {
                if (!WORKFLOW_ASSIGNMENT.equals(e)) {
                    findSessionFactory(e, e, null, ACTION_TYPE_LOAD);
                }
            }

            // lookup cache
            String cacheKey = getJoinSessionFactoryCacheKey(entities);
            sf = (SessionFactory)joinFormSessionFactoryCache.get(cacheKey);
            if (sf != null) {
                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entities + "] join session factory found in cache");
            }

            if (sf == null || sf.isClosed()) {
                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entities + "] create join session factory");
                sf = createJoinSessionFactory(entities);
            }
        }
        return sf;
    }
    
    protected MetadataSources createMetadataSources() {
        // create configuration
        Configuration configuration = new Configuration();
        configuration.setProperty("show_sql", "false");
        configuration.setProperty("cglib.use_reflection_optimizer", "true");
        if (InMemoryCacheManager.isAvailable()) {
            InMemoryCacheManager inMemoryCacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            Properties cacheManagerProperties = inMemoryCacheManager.getHibernateProperties();
            cacheManagerProperties.forEach((k, v) -> { 
                configuration.setProperty(k.toString(), v.toString());
            });
        }
        // after the cache manager properties on purpose, so this cannot be overridden by them
        configuration.setProperty(HBM2DDL_JDBC_METADATA_EXTRACTION_STRATEGY, "individually");
        configuration.setProperty(Environment.LOG_SLOW_QUERY, "500");

        // set datasource
        DataSource dataSource = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
        configuration.getProperties().put(Environment.JAKARTA_JTA_DATASOURCE, dataSource);
        configuration.getProperties().put(Environment.ALLOW_UPDATE_OUTSIDE_TRANSACTION, true);
        Properties properties = getProperties();
        
        // set schema from datasource properties
        String workflowSchema = (String) properties.get("workflowSchema");
        if (workflowSchema != null && !workflowSchema.isEmpty()) {
            configuration.setProperty(Environment.DEFAULT_SCHEMA, (String) properties.get("workflowSchema"));
        }
        
        final ServiceRegistry sr = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        MetadataSources metadataSources = new MetadataSources(sr);
        return metadataSources;
    }

    private static final Set<String> TEMPLATE_INDEXED_ROWS = Set.of("dateCreated", "createdBy");

    /**
     * The maximum length of an index name. This is derived from the smallest length across
     * MySQL/MariaDB, PostgreSQL, Microsoft SQL, and Oracle.
     */
    private static final int MAX_INDEX_NAME_LENGTH = 63;

    /**
     * Generates an index name for the specified table and column, ensuring the name adheres
     * to the maximum allowed index name length.
     * <p>
     * If the index name is longer than {@link #MAX_INDEX_NAME_LENGTH}, a 12-character string
     * based on UUIDv3 will replace the last 12 characters of the index name.
     *
     * @param tableName the name of the table for which the index is being created
     * @param columnName the name of the column for which the index is being created
     * @return a valid index name that is within the maximum allowed length
     */
    private static String getIndexName(String tableName, String columnName) {
        String candidate = "idx_" + tableName + "_" + columnName;
        if (candidate.length() <= MAX_INDEX_NAME_LENGTH) {
               return candidate;
           }
        String hash = UUID.nameUUIDFromBytes(candidate.getBytes(StandardCharsets.UTF_8))
                       .toString().replace("-", "").substring(0, 12);
        int prefixLength = MAX_INDEX_NAME_LENGTH - hash.length() - 1;
        return candidate.substring(0, prefixLength) + "_" + hash;
    }

    protected SessionFactory createSessionFactory(String entityName, String tableName, FormRowSet rowSet, int actionType) throws HibernateException {
        MetadataSources metadataSources = createMetadataSources();
        
        // get columns
        Collection<String> formFields;
        if (rowSet != null) {
            // column names from submitted fields
            formFields = getFormRowColumnNames(rowSet);
        } else {
            // column names from all forms mapped to this table
            formFields = getFormDefinitionColumnNames(tableName);
            if (formFields == FormColumnResolver.UNRESOLVED_COLUMNS) {
                // Publishing here would write a mapping with NO custom columns and run DDL
                // against it — far worse than failing the request. See FormColumnResolver.UNRESOLVED_COLUMNS.
                throw new HibernateException("Cannot generate the Hibernate mapping for \"" + entityName
                        + "\": its form column set is unresolved because of a circular form dependency. Retry the request.");
            }
        }

        // load default FormRow hbm xml
        Document document;
        synchronized(this) {
            document = (Document) formRowDocument.cloneNode(true);
        }

        // update entity-name
        NodeList classTags = document.getElementsByTagName("class");
        Node classNode = classTags.item(0);
        NamedNodeMap attributeMap = classNode.getAttributes();
        Node entityNameNode = attributeMap.getNamedItem("entity-name");
        entityNameNode.setNodeValue(entityName);
        attributeMap.setNamedItem(entityNameNode);

        // update table name
        Node tableNameNode = attributeMap.getNamedItem("table");
        tableNameNode.setNodeValue(tableName);
        attributeMap.setNamedItem(tableNameNode);

        // remove existing dynamic components
        NodeList componentTags = document.getElementsByTagName("dynamic-component");
        Node node = componentTags.item(0);
        XMLUtil.removeChildren(node);

        // Add indexes for selected columns in FormRow.hbm.xml template
        NodeList templateNodes = document.getElementsByTagName("property");
        if (templateNodes != null) {
            for (int i = 0; i < templateNodes.getLength(); i++) {
                Element el = (Element) templateNodes.item(i);
                if (el.hasAttribute("name") && TEMPLATE_INDEXED_ROWS.contains(el.getAttribute("name"))) {
                    el.setAttribute("index", getIndexName(tableName, el.getAttribute("name")));
                }
            }
        }
        
        Collection<String> indexesList = formColumnCache.getIndexes(tableName);
        if (indexesList != null && !indexesList.isEmpty()) {
            NodeList nodeList = document.getElementsByTagName("property");
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Element el = (Element) nodeList.item(j);
                    if (el.hasAttribute("name") && indexesList.contains(el.getAttribute("name"))) {
                        el.setAttribute("index", getIndexName(tableName, el.getAttribute("name")));
                    }
                }
            }
        }

        // add dynamic components
        for (String field : formFields) {
            Element element = document.createElement("property");
            String propName = field;
            String columnName = FORM_PREFIX_COLUMN + field;
            if (propName != null && !propName.isEmpty()) {
                String propType = "text";
                if (actionType == ACTION_TYPE_LOAD && (Character.isDigit(propName.charAt(0)) || RESERVED_KEYWORDS.contains(propName.toLowerCase()))) {
                    propName = "t__" + propName;
                }
                element.setAttribute("name", propName);
                element.setAttribute("column", columnName);
                element.setAttribute("type", propType);
                element.setAttribute("not-null", String.valueOf(false));
                
                if (indexesList != null && indexesList.contains(field)) {
                    element.setAttribute("index", getIndexName(tableName, columnName));
                }
                
                node.appendChild(element);
            }            
        }

        if (actionType == ACTION_TYPE_LOAD) {
            // locate entity hbm mapping file
            String path = getFormMappingPath();
            String filename = entityName + ".hbm.xml";        
            File mappingFile = new File(path, filename);
            try {
                // save new mapping file
                XMLUtil.saveDocument(document, mappingFile.getPath());
            } catch (TransformerException e) {
                throw new HibernateException("Unable to save " + mappingFile.getPath(), e);
            } catch (IOException e) {
                throw new HibernateException("Unable to save " + mappingFile.getPath(), e);
            }

            // add mapping to config
            metadataSources.addFile(mappingFile);
        } else {
            InputStream is = null;
            try {
                is = XMLUtil.documentToInputStream(document);
                metadataSources.addInputStream(is);
            } catch (TransformerException e) {
                throw new HibernateException("Unable to read document", e);
            } catch (IOException e) {
                throw new HibernateException("Unable to read document", e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
        
        String cacheKey = getSessionFactoryCacheKey(entityName, tableName, rowSet);
        Metadata metadata = metadataSources.buildMetadata();
        SessionFactory sf = getSessionFactory(entityName, cacheKey, actionType, metadata, true);
        
        return sf;
    }

    protected SessionFactory createJoinSessionFactory(String[] entities) throws HibernateException {
        // create metadatasources
        MetadataSources metadataSources = createMetadataSources();

        String path = getFormMappingPath();
        for (String e : entities) {
            if (WORKFLOW_ASSIGNMENT.equals(e)){ // include the assignment related hibernate mapping file
                metadataSources.addResource("/org/joget/workflow/model/WorkflowProcessLink.hbm.xml");
                metadataSources.addResource("/org/joget/apps/form/model/FormDataAssignment.hbm.xml");
            } else {
                String filename = e + ".hbm.xml";
                File mappingFile = new File(path, filename);
                // findJoinSessionFactory's own loop (its caller) already called findSessionFactory
                // for every entity here, which regenerates a missing/stale mapping - but that
                // guarantee only holds AT THE INSTANT each per-entity call returns. Each entity is
                // checked, and its FormBuildGuard "MAP:" lock released, one at a time; by the time
                // THIS loop reaches an entity's file, an unrelated concurrent request for that same
                // entity (its own findSessionFactory/clearFormTableCache/clearFormCache call) can
                // have rewritten or deleted it again. Unlike findSessionFactory's own disk-fallback
                // read, this had NO exception handling at all for that - not even the narrower
                // InvalidMappingException catch - so a raced read here threw an uncaught, opaque
                // Hibernate exception (#2085 follow-up finding, 2026-09-08). Failing closed with a
                // clear, retriable error is what every other guarded rebuild in this file already
                // does (see createSessionFactoryWithSingleFlight's timeout branch and
                // hasMappingChanged's UNRESOLVED_COLUMNS branch); actually closing the race would
                // need locking multiple entities at once here, which risks an ABBA deadlock between
                // two joins that reference the same tables in different orders and is deliberately
                // left as a separate, larger piece of work.
                try {
                    metadataSources.addFile(mappingFile);
                } catch (MappingException me) {
                    throw new HibernateException("Could not read the Hibernate mapping for \"" + e
                            + "\" while building the join session factory for [" + String.join(", ", entities)
                            + "]: it may be concurrently regenerated by another request. Retry the request.", me);
                }
            }
        }

        String cacheKey = getJoinSessionFactoryCacheKey(entities);
        Metadata metadata = metadataSources.buildMetadata();
        SessionFactory sf = getJoinSessionFactory(entities, cacheKey, metadata);
        
        return sf;
    }
    
    protected SessionFactory getSessionFactory(final String entityName, String cacheKey, int actionType, final Metadata metadata, boolean needUpdate) {
        SessionFactory sf = metadata.buildSessionFactory();
        sf.getStatistics().setStatisticsEnabled(true);
        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " session factory created");

        if (needUpdate) {
            // update schema
            internalUpdateSchema(sf, metadata, entityName);
        }
        
        PersistentClass pc = null;
        if (actionType == ACTION_TYPE_LOAD) {
            pc = metadata.getEntityBinding(entityName);
            // save into cache
            formPersistentClassCache.put(getPersistentClassCacheKey(entityName), pc);
        }

        // Record what sf was actually built from, tied to sf's own (eternal) lifecycle rather than
        // formPersistentClassCache's independent 120s TTL (#2085) - see formSessionFactorySourceCache.
        // Published BEFORE sf itself is published below: a reader that observes the new sf via
        // formSessionFactoryCache must never be able to observe an older (or missing) provenance
        // record for that SAME sf - the identity check on the read side (provenance.sf == sf) makes
        // this safe even if a concurrent write interleaves regardless, but ordering it this way
        // keeps the common, uncontended case from paying a spurious fail-closed rebuild.
        if (pc != null) {
            formSessionFactorySourceCache.put(cacheKey, new SessionFactoryProvenance(sf, pc));
        } else {
            formSessionFactorySourceCache.remove(cacheKey);
        }

        // save into cache
        formSessionFactoryCache.remove(cacheKey);
        formSessionFactoryCache.put(cacheKey, sf);
        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form " + entityName + " saved in cache");
        
        return sf;
    }
    
    protected SessionFactory getJoinSessionFactory(final String[] entities, String cacheKey, final Metadata metadata) {
        SessionFactory sf = metadata.buildSessionFactory();
        sf.getStatistics().setStatisticsEnabled(true);
        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entities + "] join session factory created");

        // save into cache
        joinFormSessionFactoryCache.remove(cacheKey);
        joinFormSessionFactoryCache.put(cacheKey, sf);
        LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entities + "] saved in cache");
        
        return sf;
    }
    
    /**
     * Trigger actual Hibernate schema update
     * @param sf
     * @param metadata
     * @param entityName
     * @throws HibernateException 
     */
    protected void internalUpdateSchema(final SessionFactory sf, final Metadata metadata, final String entityName) throws HibernateException {
        if (entityName.startsWith(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME) //for load
                || entityName.startsWith(FORM_PREFIX_ENTITY)) { //for store
            boolean tableExist = false;

            //try to check the table is exist in database or not
            Session session = null;
            try {
                session = sf.withOptions().autoJoinTransactions(true).openSession();
                String query = "SELECT 1 FROM " + entityName + " e WHERE e.id = ?1";

                Query q = session.createQuery(processQuery(query));

                q.setFirstResult(0);
                q.setMaxResults(1);
                q.setParameter(1, "dummy_test_record_id");

                q.list();
                tableExist = true; //when no exception, the table is exist
            } catch (Exception e) {
                if (isTableNotFoundException(e)) {
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "--- Form [" + entityName + "] schema not exist.");
                } else {
                    // unexpected error (e.g. connection failure): abort to avoid dropping an existing table
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "Error checking schema for [" + entityName + "] due to " + e.getMessage());
                    throw (e instanceof HibernateException) ? (HibernateException) e : new HibernateException("Error checking schema for [" + entityName + "]", e);
                }
            } finally {
                closeSession(session);
            }

            if (!tableExist) {
                // table does not exist, create it. Use createOnly so Hibernate does NOT issue a DROP first.
                try {
                    new SchemaExport().createOnly(EnumSet.of(TargetType.DATABASE), metadata);
                    LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entityName + "] schema created");
                } catch (Exception e) {
                    LogUtil.error(getClass().getName(), e, "Error creating schema");
                }
            } else {
                // table exists, update it
                try {
                    new SchemaUpdate().execute(EnumSet.of(TargetType.DATABASE), metadata);
                } catch (Exception e) {
                    LogUtil.error(getClass().getName(), e, "Error updating schema");
                }
                LogUtil.debug(FormDataDaoImpl.class.getName(), "  --- Form [" + entityName + "] schema updated");
            }
        }
    }

    /**
     * Returns true only when the throwable chain contains a SQLException whose
     * SQLState/error code unambiguously indicates "table or view does not exist".
     * Any other failure (connection drop, timeout, lock wait, etc.) returns false
     * so the caller will NOT proceed to (re)create the table.
     */
    protected static boolean isTableNotFoundException(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof java.sql.SQLException) {
                java.sql.SQLException sqle = (java.sql.SQLException) t;
                String state = sqle.getSQLState();
                int code = sqle.getErrorCode();
                if (state != null) {
                    // 42S02: MySQL / MariaDB
                    // 42P01: PostgreSQL
                    // S0002: SQL Server
                    if ("42S02".equals(state) || "42P01".equals(state)
                             || "S0002".equals(state)) {
                        return true;
                    }
                    // Oracle ORA-00942 "table or view does not exist"
                    if ("42000".equals(state) && code == 942) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Returns the base directory used to store hibernate mapping files
     * @return
     */
    protected String getFormMappingPath() {
        // determine path to mapping directory
        String path = SetupManager.getBaseDirectory();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += FormDataDaoImpl.FORM_MAPPING_DIRECTORY;
        new File(path).mkdirs();
        return path;
    }

    /**
     * Returns collection of all column names to be saved
     * @param rowSet
     * @return
     */
    @Override
    public Collection<String> getFormRowColumnNames(FormRowSet rowSet) {
        Collection<String> columnList = new ArrayList<String>();
        Collection<String> lowerCaseColumnSet = new HashSet<String>();
        if (rowSet != null && !rowSet.isEmpty()) {
            Set columnsName = new HashSet();
            
            for (FormRow row : rowSet) {
                if (row != null && !row.isEmpty()) {
                    columnsName.addAll(row.keySet());
                }
            }
            // Same naming rules as the LOAD side (FormColumnResolver): the two must agree, or a
            // column the store path accepts is one the load mapping does not carry.
            for (Object column : columnsName) {
                String columnName = (String) column;
                if (columnName != null && !columnName.isEmpty()
                        && !FormColumnResolver.PREDEFINED_COLUMNS.contains(columnName)) {
                    String lowerCasePropName = columnName.toLowerCase();

                    if (FormColumnResolver.COLUMN_NAME_PATTERN.matcher(columnName).matches() && !lowerCaseColumnSet.contains(lowerCasePropName) && !(columnName.startsWith(FORM_PREFIX_COLUMN) && columnsName.contains(columnName.substring(2)))) {
                        columnList.add(columnName);
                        lowerCaseColumnSet.add(lowerCasePropName);
                    }
                }
            }
            // No separate "remove predefined column" pass: the guard above already excluded them.
        }
        return columnList;
    }

    /**
     * Returns collection of all columns from forms mapped to a table
     * @param tableName
     * @return
     */
    @Override
    public Collection<String> getFormDefinitionColumnNames(String tableName) {
        return getColumnResolver().getColumnNames(tableName);
    }

    /**
     * Returns EntityName of form mapped to a table & column
     * @param tableName
     * @param columnName
     * @return
     */
    @Override
    public String getEntityName(String tableName, String columnName) {
        // strip table prefix
        if (tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }

        // get forms mapped to the table name
        Collection<FormDefinition> formList = getFormDefinitionDao().loadFormDefinitionByTableName(tableName);
        for (FormDefinition formDef : formList) {
            // get JSON
            String json = formDef.getJson();
            if (json != null) {
                Form form = (Form) getFormService().createElementFromJson(json);
                String formTableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
                if (tableName.equals(formTableName) && FormUtil.findElement(columnName, form, null) != null) {
                    return getFormEntityName(form);
                }
            }
        }
        return null;
    }

    protected void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }    

    public List<Map<String, Object>> findCustomQuery(Form form, final String[] fields, final String[] alias, final String[] joins, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        final String entityName = getFormEntityName(form);
        final String newTableName = getFormTableName(form);

        return internalFindCustom(entityName, newTableName, fields, alias, joins, condition, params, groupBys, havingCondition, havingParams, sort, desc, start, rows);
    }

    public List<Map<String, Object>> findCustomQuery(String formDefId, String tableName, final String[] fields, final String[] alias, final String[] joins, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        final String entityName = getFormEntityName(formDefId);
        final String newTableName = getFormTableName(formDefId, tableName);

        return internalFindCustom(entityName, newTableName, fields, alias, joins, condition, params, groupBys, havingCondition, havingParams, sort, desc, start, rows);
    }

    public Long countCustomQuery(Form form, final String[] joins, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams) {
        final String entityName = getFormEntityName(form);
        final String newTableName = getFormTableName(form);

        return internalCountCustom(entityName, newTableName, joins, condition, params, groupBys, havingCondition, havingParams);
    }

    public Long countCustomQuery(String formDefId, String tableName, final String[] joins, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams) {
        final String entityName = getFormEntityName(formDefId);
        final String newTableName = getFormTableName(formDefId, tableName);

        return internalCountCustom(entityName, newTableName, joins, condition, params, groupBys, havingCondition, havingParams);
    }
    
    /**
     * Query to find a list of matching form rows.
     * @param entityName
     * @param tableName
     * @param fields
     * @param alias
     * @param joinTableNames
     * @param condition
     * @param params
     * @param sort
     * @param havingCondition
     * @param groupBys
     * @param havingParams
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    protected List<Map<String, Object>> internalFindCustom(final String entityName, final String tableName, final String[] fields, final String[] alias, final String[] joinTableNames, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        // get hibernate template
        Session session = getJoinHibernateSession(tableName, joinTableNames);

        try {
            
            String fieldsQuery = "e";
            if (fields != null && fields.length > 0) {
                fieldsQuery = "";
                for (String f : fields) {
                    if (!fieldsQuery.isEmpty()) {
                        fieldsQuery += ", ";
                    }
                    fieldsQuery += f;
                }
                fieldsQuery = replaceColumnNameWithPrefix(tableName, fieldsQuery);
            }
            
            String conditionQuery = "";
            if (condition != null && !condition.isEmpty()) {
                String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
                conditionQuery = " " + newCondition;
                
                conditionQuery = replaceColumnNameWithPrefix(tableName, conditionQuery);
            }
            String havingConditionQuery = "";
            if (havingCondition != null && !havingCondition.isEmpty()) {
                havingConditionQuery = " HAVING " + havingCondition;
                
                havingConditionQuery = replaceColumnNameWithPrefix(tableName, havingConditionQuery);
            }
            
            String joinQuery = "";
            if (joinTableNames != null && joinTableNames.length > 0) {
                for (String t : joinTableNames) {
                    if (!t.equals(WORKFLOW_ASSIGNMENT)) { //no need to join for WORKFLOW_ASSIGNMENT, it is just use to include the assignment related hibernate mapping file
                        joinQuery += ", ";
                        if (t.startsWith(FORM_PREFIX_TABLE_NAME)) {
                            t = t.substring(FORM_PREFIX_TABLE_NAME.length());
                        }
                        joinQuery += FORM_PREFIX_TABLE_NAME + t + " AS " + t;
                    }
                }
                joinQuery += " ";
            }
            
            String query = "SELECT " + fieldsQuery + " FROM " + tableName + " e " + joinQuery + conditionQuery;
            
            if (groupBys != null && groupBys.length > 0) {
                String groupByQuery = "";
                for (String f : groupBys) {
                    if (!groupByQuery.isEmpty()) {
                        groupByQuery += ", ";
                    }
                    groupByQuery += f;
                }
                groupByQuery = replaceColumnNameWithPrefix(tableName, groupByQuery);
                
                query += " GROUP BY " + groupByQuery + havingConditionQuery;
            }

            if ((sort != null && !sort.trim().isEmpty()) && !query.toLowerCase().contains("order by")) {
                String sortQuery = replaceColumnNameWithPrefix(tableName, sort);
                query += " ORDER BY " + sortQuery;

                if (desc) {
                    query += " DESC";
                }
            }
            
            Query q = session.createQuery(processQuery(query));
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            cacheManager.setCacheable(q, tableName);

            int s = (start == null) ? 0 : start;
            
            //setting this unnecessarily causing performance issue 
            if (s > 0) {
                q.setFirstResult(s);
            }

            if (rows != null && rows > 0) {
                q.setMaxResults(rows);
            }

            int i = 1;
            if (params != null) {
                for (Object param : params) {
                    q.setParameter(i, param);
                    i++;
                }
            }
            if (groupBys != null && groupBys.length > 0 && havingParams != null) {
                for (BigDecimal param : havingParams) {
                    q.setParameter(i, param);
                    i++;
                }
            }

            Collection result = q.list();

            List<Map<String, Object>> rowSet = new ArrayList<Map<String, Object>>();
            for (Object o : result) {
                Map<String, Object> r = new HashMap<String, Object>();
                if (o instanceof FormRow) { //if there is no join tables, it return as FormRow
                    r = (Map<String, Object>) ((FormRow) o).getCustomProperties();
                } else {
                    Object[] arr = (!o.getClass().isArray()) ? new Object[] { o } : (Object[]) o;
                    
                    for (int j = 0; j < alias.length; j++) {
                        Object tempValue = arr[j];
                        if (tempValue == null) {
                            tempValue = "";
                        } else if (!(tempValue instanceof Date)) {
                            tempValue = tempValue.toString();
                        }

                        if (alias[j].contains(".")) {
                            String prefix = alias[j].substring(0, alias[j].indexOf("."));
                            String name = alias[j].substring(alias[j].indexOf(".") + 1);

                            Map<String, Object> sub = (Map<String, Object>) r.get(prefix);
                            if (sub == null) {
                                sub = new HashMap<String, Object>();
                            }
                            sub.put(name, tempValue);
                            r.put(prefix, sub);
                        }
                        r.put(alias[j], tempValue);
                    }
                }
                rowSet.add(r);
            }
            return rowSet;
        } finally {
            closeSession(session);
        }
    }

    /**
     * Query total row count for a form.
     * @param entityName
     * @param tableName
     * @param joinTableNames
     * @param condition
     * @param params
     * @param havingCondition
     * @param havingParams
     * @param groupBys
     * @return
     */
    protected Long internalCountCustom(final String entityName, final String tableName, final String[] joinTableNames, final String condition, final Object[] params, final String[] groupBys, final String havingCondition, final BigDecimal[] havingParams) {
        // get hibernate template
        Session session = getJoinHibernateSession(tableName, joinTableNames);
        try {
            String selectField = "*"; 
            String groupByQuery = "";
            
            String conditionQuery = "";
            if (condition != null && !condition.isEmpty()) {
                String newCondition = StringUtil.replaceOrdinalParameters(condition, params);
                conditionQuery = " " + newCondition;
                
                conditionQuery = replaceColumnNameWithPrefix(tableName, conditionQuery);
            }
            String havingConditionQuery = "";
            if (havingCondition != null && !havingCondition.isEmpty()) {
                havingConditionQuery = " HAVING " + havingCondition;
                havingConditionQuery = replaceColumnNameWithPrefix(tableName, havingConditionQuery);
            }
            
            if (groupBys != null && groupBys.length > 0) {
                selectField = replaceColumnNameWithPrefix(tableName, groupBys[0]);
                for (String f : groupBys) {
                    if (!groupByQuery.isEmpty()) {
                        groupByQuery += ", ";
                    }
                    groupByQuery += f;
                }
                groupByQuery = replaceColumnNameWithPrefix(tableName, groupByQuery);
                
                groupByQuery = " GROUP BY " + groupByQuery + havingConditionQuery;
            }
            
            String joinQuery = "";
            if (joinTableNames != null && joinTableNames.length > 0) {
                for (String t : joinTableNames) {
                    if (!t.equals(WORKFLOW_ASSIGNMENT)) { //no need to join for WORKFLOW_ASSIGNMENT, it is just use to include the assignment related hibernate mapping file
                        joinQuery += ", ";
                        if (t.startsWith(FORM_PREFIX_TABLE_NAME)) {
                            t = t.substring(FORM_PREFIX_TABLE_NAME.length());
                        }
                        joinQuery += FORM_PREFIX_TABLE_NAME + t + " AS " + t;
                    }
                }
                joinQuery += " ";
            }
            
            String query = "SELECT COUNT("+selectField+") FROM " + tableName + " e " + joinQuery + conditionQuery + groupByQuery;
            
            Query q = session.createQuery(processQuery(query));
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            cacheManager.setCacheable(q, tableName);

            int i = 1;
            if (params != null) {
                for (Object param : params) {
                    q.setParameter(i, param);
                    i++;
                }
            }
            if (groupBys != null && groupBys.length > 0 && havingParams != null) {
                for (BigDecimal param : havingParams) {
                    q.setParameter(i, param);
                    i++;
                }
            }

            if (groupBys != null && groupBys.length > 0) {
                return ((long) q.list().size());
            } else {
                return ((Long) q.list().get(0));
            }
        } finally {
            closeSession(session);
        }
    }
    
    // least recently used (LRU) cache to hold processed queries
    static Map<String, String> processedQueryCache = Collections.synchronizedMap(new LRUMap(200));
    
    protected String processQuery(String query) {
        try {
            // lookup from LRU cache
            String cacheKey = StringUtil.md5Base16Utf8(query); // hash to minimize memory usage
            String result = processedQueryCache.get(cacheKey);
            if (result != null) {
                return result;
            }

            Pattern pattern = Pattern.compile("(\\w+\\.customProperties\\.)([0-9a-zA-Z_][0-9a-zA-Z_]*)");
            Matcher matcher = pattern.matcher(query);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String firstGroup = matcher.group(1);   // match "<alias>.customProperties."
                String secondGroup = matcher.group(2);  // match "<field_name>"

                // Check if property name starts with digit or is a reserved keyword
                if ((secondGroup.length() > 0 && Character.isDigit(secondGroup.charAt(0))) || RESERVED_KEYWORDS.contains(secondGroup.toLowerCase())) {
                    // Add t__ prefix if not already present
                    if (!secondGroup.startsWith("t__")) {
                        secondGroup = "t__" + secondGroup;
                    }
                }

                matcher.appendReplacement(sb, StringUtil.escapeRegex(firstGroup) + StringUtil.escapeRegex(secondGroup));
            }
            matcher.appendTail(sb);
            query = sb.toString();

            // Normalize indetifiers from case-sesitive to case-insensitive based on table metadata
            Map<String, String> aliasToTableMap = extractAliasToTableMapping(query);
            query = normalizeCustomPropertiesWithAliases(query, aliasToTableMap);
            query = rewriteLegacyCustomPropertyNumericSort(query);

            // save into cache
            processedQueryCache.put(cacheKey, query);                       
        } catch (Exception e) {
            LogUtil.error(query, e, query);
        }
        return query;
    }

    /**
     * Hibernate 6 validates operand types in HQL before SQL generation. Older app definitions sometimes sort
     * string-backed form fields numerically with "c_field + 0"; keep that compatible by converting the generated
     * customProperties expression to an explicit numeric cast.
     */
    protected String rewriteLegacyCustomPropertyNumericSort(String query) {
        Matcher matcher = LEGACY_CUSTOM_PROPERTY_NUMERIC_SORT_PATTERN.matcher(query);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "cast(" + Matcher.quoteReplacement(matcher.group(1)) + " as big_decimal)");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /***
     * Generate 'alias to table-name' mapping, to be used inside normalizeCustomPropertiesWithAliases()
     * @param query
     * @return aliasToTable
     */
    protected Map<String, String> extractAliasToTableMapping(String query) {
        Map<String, String> aliasToTable = new HashMap<>();

        // Match "app_fd_<table_name> <alias>" or "app_fd_<table_name> AS <alias>"
        Pattern pattern = Pattern.compile("(app_fd_\\w+)\\s+(?:AS\\s+)?(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            String tableName = matcher.group(1);   // match "app_fd_<table_name>"
            String alias = matcher.group(2);       // match "<alias>"
            aliasToTable.put(alias, tableName);
        }

        return aliasToTable;
    }

    /***
     * Normalize the query part by part based on the alias-table map iteration.
     * @param query
     * @param aliasToTableMap
     * @return query
     */
    protected String normalizeCustomPropertiesWithAliases(String query, Map<String, String> aliasToTableMap) {
        for (Map.Entry<String, String> entry : aliasToTableMap.entrySet()) {
            String alias = entry.getKey();
            String tableName = entry.getValue();

            query = normalizeCustomPropertiesForAlias(query, alias, tableName);
        }
        return query;
    }

    /***
     * Normalize the query based on alias and table name
     * @param query
     * @param alias
     * @param tableName
     * @return query
     */
    protected String normalizeCustomPropertiesForAlias(String query, String alias, String tableName) {
        try {
            Pattern checkPattern = Pattern.compile("(\\b" + Pattern.quote(alias) + "\\.customProperties\\.)([a-zA-Z_]\\w*)");
            Matcher checkMatcher = checkPattern.matcher(query);
            if (!checkMatcher.find()) {
                return query;
            }

            // Load metadata for the table and cache PersistentClass
            PersistentClass pc = (PersistentClass)formPersistentClassCache.get(getPersistentClassCacheKey(tableName));
            if (pc == null) {
                Metadata metadata = loadMetadataForTable(tableName);
                if (metadata == null) {
                    return query;
                }
                pc = metadata.getEntityBinding(tableName);
                if (pc == null) {
                    return query;
                }
                formPersistentClassCache.put(getPersistentClassCacheKey(tableName), pc);
            }
            Property customProp = pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES);
            if (customProp == null) {
                return query;
            }

            Component customComponent = (Component) customProp.getValue();

            // Map to store case-corrected property names
            Map<String, String> propertyNameMap = new HashMap<>();
            Iterator<Property> propIter = customComponent.getProperties().iterator();
            while (propIter.hasNext()) {
                Property prop = propIter.next();
                propertyNameMap.put(prop.getName().toLowerCase(), prop.getName());
            }

            // Replace <alias>.customProperties.<fieldName> with correct case
            Pattern pattern = checkPattern;
            Matcher matcher = pattern.matcher(query);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String prefix = matcher.group(1);           // match "<alias>.customProperties."
                String queryFieldName = matcher.group(2);   // match "<field_name>"

                String mappedName = propertyNameMap.get(queryFieldName.toLowerCase());
                if (mappedName != null && !mappedName.equals(queryFieldName)) {
                    // Replace with correct case is case mismatch detected
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(prefix + mappedName));
                } else {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                }
            }

            matcher.appendTail(sb);
            query = sb.toString();
            return query;
        } catch (Exception e) {
            LogUtil.debug(FormDataDaoImpl.class.getName(), "Error normalizing for alias " + alias + ": " + e.getMessage());
            return query;
        }
    }

    /***
     * To load table metadata in order to generate the `PersistentClass` if none was being cached
     * @param tableName
     * @return
     */
    private Metadata loadMetadataForTable(String tableName) {
        try {
            String path = getFormMappingPath();
            String filename = tableName + ".hbm.xml";
            File mappingFile = new File(path, filename);

            if (mappingFile.exists()) {
                MetadataSources metadataSources = createMetadataSources();
                metadataSources.addFile(mappingFile);
                return metadataSources.buildMetadata();
            }
        } catch (Exception e) {
            LogUtil.debug(FormDataDaoImpl.class.getName(), "Error loading metadata: " + e.getMessage());
        }
        return null;
    }
}
