package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.hibernate.query.Query;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DAO to load/store FormDefinition objects
 */
public class FormDefinitionDaoImpl extends AbstractAppVersionedObjectDao<FormDefinition> implements FormDefinitionDao {

    public static final String ENTITY_NAME = "FormDefinition";
    
    private FormColumnCache formColumnCache;
    private AppDefCache cache;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    public AppDefCache getCache() {
        return cache;
    }

    public void setCache(AppDefCache cache) {
        this.cache = cache;
    }
    
    private String getCacheKey(String id, String appId, Long version){
        return DynamicDataSourceManager.getCurrentProfile()+"_"+appId+"_"+version+"_FORM_"+id;
    }
    
    public FormColumnCache getFormColumnCache() {
        return formColumnCache;
    }

    public void setFormColumnCache(FormColumnCache formColumnCache) {
        this.formColumnCache = formColumnCache;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    /**
     * Retrieves FormDefinitions mapped to a table name.
     *
     * <p>DELIBERATELY NOT the generic cacheable {@code find(..)} helper (unlike every other DAO
     * method here) — this is the full-rebuild counterpart of {@link #loadFormDefinitionByTableNameAndIds},
     * which already bypasses the query cache for the same reason: its only production caller,
     * {@code FormColumnResolver}, is deciding what columns a form data table's Hibernate mapping
     * must carry, and a Hibernate query cache entry that outlives the form save it should have been
     * invalidated by (Ignite's query cache in particular; see #2085) would otherwise make the "full,
     * authoritative rebuild" fallback re-read the SAME stale row forever — the very re-check
     * {@code FormColumnResolver.isComplete} exists to catch can never pass, because both the
     * incremental and full paths were consulting the identical stale cache entry. Downstream results
     * are already cached correctly, in {@code FormColumnCache}, once validated against the manifest;
     * caching this raw read a second time only reintroduces the staleness that cache was built to
     * rule out.</p>
     *
     * @param tableName
     * @return
     */
    @Override
    public Collection<FormDefinition> loadFormDefinitionByTableName(String tableName) {
        Query q = findSession().createQuery("SELECT e FROM " + getEntityName() + " e WHERE e.tableName = ?1");
        q.setParameter(1, tableName);
        return q.list();
    }

    /**
     * NATIVE SQL, not HQL, and that is measured rather than stylistic: the same statement as HQL
     * costs 5.6 ms at 1000 definitions and GROWS with row count, because Hibernate's handling of a
     * function term scales at ~4 us/row. As native SQL on the same session it is flat at ~1.8 ms.
     *
     * <p><b>No function calls in the projection, deliberately.</b> An earlier version selected
     * {@code LENGTH(json)} to strengthen change detection. That was wrong twice over: {@code LENGTH}
     * does not exist on SQL Server (it is {@code LEN}), and detecting that by catching the failure
     * is worse than useless inside a caller-owned transaction — Hibernate converts the
     * {@code SQLGrammarException} and marks the transaction rollback-only, so retrying a degraded
     * query on the same session still kills the caller's request at commit. Plain columns cannot
     * fail that way. What the length term was for is now handled portably by the recency rule in
     * {@code FormDataDaoImpl.isRecentlyModified}.</p>
     *
     * <p>Column labels are never referenced — results are read positionally — so Oracle folding
     * identifiers to upper case does not matter.</p>
     */
    @Override
    public Collection<Object[]> getFormDefinitionManifestByTableName(String tableName) {
        org.hibernate.query.NativeQuery q = findSession().createNativeQuery(
                "SELECT appId, appVersion, formId, dateModified FROM app_form WHERE tableName = ?");
        q.setParameter(1, tableName);
        List rows = q.list();
        Collection<Object[]> result = new ArrayList<Object[]>();
        for (Object row : rows) {
            result.add((Object[]) row);
        }
        return result;
    }

    @Override
    public Collection<FormDefinition> loadFormDefinitionByTableNameAndIds(String tableName, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<FormDefinition>();
        }
        Query q = findSession().createQuery(
                "SELECT e FROM " + getEntityName() + " e WHERE e.tableName = ?1 AND e.id IN (?2)");
        q.setParameter(1, tableName);
        q.setParameterList(2, ids);
        return q.list();
    }

    public Collection<FormDefinition> getFormDefinitionList(String filterString, AppDefinition appDefinition, String sort, Boolean desc, Integer start, Integer rows) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.find(conditions, params.toArray(), appDefinition, sort, desc, start, rows);
    }

    public Long getFormDefinitionListCount(String filterString, AppDefinition appDefinition) {
        String conditions = "";
        List params = new ArrayList();

        if (filterString == null) {
            filterString = "";
        }
        conditions = "and (id like ? or name like ? or tableName like ?)";
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");
        params.add("%" + filterString + "%");

        return this.count(conditions, params.toArray(), appDefinition);
    }
    
    protected FormDefinition load(String id, AppDefinition appDefinition) {
        FormDefinition formDef = super.loadById(id, appDefinition);
        return formDef;
    }

    @Override
    public FormDefinition loadById(String id, AppDefinition appDefinition) {
        String cacheKey = getCacheKey(id, appDefinition.getAppId(), appDefinition.getVersion());
        FormDefinition formDefinition = (FormDefinition)cache.getObject(cacheKey, appDefinition);

        if (formDefinition == null) {
            FormDefinition formDef = load(id, appDefinition);

            if (formDef != null) {
                cache.put(cacheKey, formDef, appDefinition);
            }
            return formDef;
        } else {
            return formDefinition;
        }
    }
    
    @Override
    public boolean add(FormDefinition object) {
        // save in db
        Date date = new Date();
        object.setDateCreated(date);
        object.setDateModified(date);
        
        boolean result = super.add(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), date);

        addToGit(object);
        
        // clear cache
        formColumnCache.remove(object.getTableName());
        return result;
    }

    @Override
    public boolean update(FormDefinition object) {
        // The table this form was mapped to BEFORE this update. Read first: once super.update()
        // has merged, the old value is gone and only the new table would be invalidated.
        String previousTableName = loadPersistedTableName(object);

        // update object
        object.setDateModified(new Date());

        boolean result = super.update(object);
        appDefinitionDao.updateDateModified(object.getAppDefinition(), object.getDateModified());

        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "forms/" + object.getId() + ".json";
            String json = AppDevUtil.formatJson(object.getJson());
            String commitMessage = "Update form " + object.getId();
            AppDevUtil.fileSave(object.getAppDefinition(), filename, json, commitMessage);

            // save yaml
            filename = "forms/" + object.getId() + ".yaml";
            JSONObject jsonObj = new JSONObject(json);
            Map<String, Object> map = jsonObj.toMap();
            String yaml = AppDevUtil.mapToYamlString(map);
            AppDevUtil.fileSave(object.getAppDefinition(), filename, yaml, "");

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(object.getAppDefinition());
        }
        
        // clear from cache
        formColumnCache.remove(object.getTableName());
        // A retarget changes TWO tables, and the old one LOST a definition. reset() rather than
        // remove(): the rebuild is additive, so evicting alone would re-derive the same union from
        // the surviving base and the moved form's columns would never disappear.
        if (previousTableName != null && !previousTableName.equals(object.getTableName())) {
            formColumnCache.reset(previousTableName);
        }
        cache.remove(getCacheKey(object.getId(), object.getAppId(), object.getAppVersion()), object.getAppDefinition());

        return result;
    }

    /**
     * The {@code tableName} currently persisted for this form, or null if it cannot be determined.
     *
     * <p>BEST EFFORT, and the limit is load-bearing enough to state. {@link #update} receives an
     * object the caller has ALREADY mutated, and persists it with {@code session.merge()}. When the
     * caller mutated a DETACHED instance — an {@link AppDefCache} hit, the common path — the session
     * holds no dirty copy and this returns the genuine previous value. On a cache MISS the instance
     * is session-managed and dirty, so Hibernate auto-flushes before this query and it returns the
     * NEW value; old equals new, no extra invalidation is issued, and behaviour is exactly what it
     * was before this method existed.</p>
     *
     * <p>That trade is deliberate. Missing the extra eviction leaves the old table holding a
     * SUPERSET of its true column set, which cannot cause the #2085 failure — a MISSING column is
     * what breaks HQL and drops data, and the store mapping is built from the submitted row's own
     * keys rather than from this union. The old table is then recomputed correctly at its next
     * invalidation.</p>
     *
     * <p>Not entirely free, though, and worth stating precisely: the LOAD mapping does emit a
     * property per union column, and {@code createSessionFactory} runs {@code SchemaUpdate} over
     * it, so a surplus column that lingers long enough gets physically added to the table as a
     * {@code longtext}. SchemaUpdate never drops it again. So the cost of missing an eviction is
     * schema bloat, not incorrectness — but it is not "inert".</p>
     */
    protected String loadPersistedTableName(FormDefinition object) {
        if (object == null || object.getId() == null || object.getAppDefinition() == null) {
            return null;
        }
        try {
            Query q = findSession().createQuery(
                    "SELECT e.tableName FROM " + getEntityName()
                            + " e WHERE e.appId = ?1 AND e.appVersion = ?2 AND e.id = ?3");
            q.setParameter(1, object.getAppDefinition().getAppId());
            q.setParameter(2, object.getAppDefinition().getVersion());
            q.setParameter(3, object.getId());
            List result = q.list();
            return result.isEmpty() ? null : (String) result.get(0);
        } catch (Exception e) {
            // Never fail an update over a cache hint. Worst case is the pre-existing behaviour.
            LogUtil.debug(FormDefinitionDaoImpl.class.getName(),
                    "Unable to resolve the previous table name for form \"" + object.getId() + "\"");
            return null;
        }
    }

    @Override
    public boolean delete(String id, AppDefinition appDef) {
        boolean result = false;
        try {
            FormDefinition obj = super.loadById(id, appDef);

            if (obj != null) {
                appDef = obj.getAppDefinition();

                // delete obj
                super.delete(getEntityName(), obj);
                appDefinitionDao.updateDateModified(appDef);
                result = true;
                
                // clear from cache. reset(), not remove(): the table has LOST a definition, and an
                // additive rebuild cannot drop its columns without a full re-read.
                formColumnCache.reset(obj.getTableName());
                cache.remove(getCacheKey(id, appDef.getId(), appDef.getVersion()), appDef);
                
                if (!AppDevUtil.isGitDisabled()) {
                    // delete json
                    String filename = "forms/" + id + ".json";
                    String commitMessage = "Delete form " + id;
                    AppDevUtil.fileDelete(appDef, filename, commitMessage);

                    // sync app plugins
                    AppDevUtil.dirSyncAppPlugins(appDef);  
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
        return result;
    }

    public Collection<String> getTableNameList(AppDefinition appDefinition) {
        final AppDefinition appDef = appDefinition;
        
        String query = "SELECT DISTINCT e.tableName FROM " + getEntityName() + " e where e.appId = ?1 and e.appVersion = ?2";

        Query q = findSession().createQuery(query);
        setCacheable(q, null);
        q.setParameter(1, appDef.getAppId());
        q.setParameter(2, appDef.getVersion());

        return q.list();
    }

    public static void addToGit(FormDefinition formDef) {
        if (!AppDevUtil.isGitDisabled()) {
            // save json
            String filename = "forms/" + formDef.getId() + ".json";
            String json = AppDevUtil.formatJson(formDef.getJson());
            String commitMessage = "Add form " + formDef.getId();
            AppDevUtil.fileSave(formDef.getAppDefinition(), filename, json, commitMessage);

            // save YAML
            try {
                String yamlFilename = "forms/" + formDef.getId() + ".yaml";
                JSONObject jsonObj = new JSONObject(json);
                Map<String, Object> map = jsonObj.toMap();
                String yaml = AppDevUtil.mapToYamlString(map);
                AppDevUtil.fileSave(formDef.getAppDefinition(), yamlFilename, yaml, "");
            } catch (JSONException e) {
                // JSON might be encrypted from a protected app, skip saving YAML
            }

            // sync app plugins
            AppDevUtil.dirSyncAppPlugins(formDef.getAppDefinition());
        }
    }
}