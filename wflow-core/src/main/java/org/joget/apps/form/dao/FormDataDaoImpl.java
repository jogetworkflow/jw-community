package org.joget.apps.form.dao;

import java.util.logging.Level;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.SetupManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.type.Type;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.form.lib.SubForm;
import org.joget.apps.form.model.Section;
import org.joget.apps.form.service.FormService;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 */
public class FormDataDaoImpl extends HibernateDaoSupport implements FormDataDao {

    public static final String FORM_MAPPING_DIRECTORY = "app_forms";
    public static final String FORM_PREFIX_ENTITY = "FormRow_";
    public static final String FORM_PREFIX_TABLE_NAME = "app_fd_";
    public static final String FORM_PROPERTY_TABLE_NAME = "tableName";
    public static final String FORM_PREFIX_COLUMN = "c_";
    Map<String, HibernateTemplate> templateCache = new HashMap<String, HibernateTemplate>();
    ThreadLocal currentThreadForm = new ThreadLocal();
    private FormDefinitionDao formDefinitionDao;
    private FormService formService;

    public FormDefinitionDao getFormDefinitionDao() {
        return formDefinitionDao;
    }

    public void setFormDefinitionDao(FormDefinitionDao formDefinitionDao) {
        this.formDefinitionDao = formDefinitionDao;
    }

    public FormService getFormService() {
        return formService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    /**
     * Loads a data row for a form based on the primary key
     * @param form
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public FormRow load(Form form, String primaryKey) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        return load(entityName, tableName, primaryKey);
    }

    /**
     * Loads a data row for a form based on the primary key. 
     * This method runs outside of a db transaction, to cater to hibernate's auto schema update requirement.
     * @param form
     * @param primaryKey
     * @return 
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FormRow loadWithoutTransaction(Form form, String primaryKey) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        return load(entityName, tableName, primaryKey);
    }

    /**
     * Loads a data row for an entity and table based on the primary key
     * @param entityName
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public FormRow load(String entityName, String tableName, String primaryKey) {
        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        // load by primary key
        FormRow row = null;
        try {
            row = (FormRow) ht.load(entityName, primaryKey);
        } catch (ObjectRetrievalFailureException e) {
            // not found, ignore
        }
        return row;
    }

    /**
     * Loads a data row for a table based on the primary key
     * @param tableName
     * @param primaryKey
     * @return null if the row does not exist
     */
    @Override
    public FormRow loadByTableNameAndColumnName(String tableName, String columnName, String primaryKey) {
        String entityName = this.getEntityName(tableName, columnName);
        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        // load by primary key
        FormRow row = null;
        try {
            row = (FormRow) ht.load(entityName, primaryKey);
        } catch (ObjectRetrievalFailureException e) {
            // not found, ignore
        }
        return row;
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

        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        Collection result = (Collection) ht.execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException {
                        String query = "SELECT e FROM " + entityName + " e ";
                        if (condition != null) {
                            query += condition;
                        }

                        if (sort != null && !sort.trim().isEmpty()) {
                            String sortProperty = sort;
                            if (!FormUtil.PROPERTY_ID.equals(sortProperty) && !FormUtil.PROPERTY_DATE_CREATED.equals(sortProperty) && !FormUtil.PROPERTY_DATE_MODIFIED.equals(sortProperty)) {
                                Collection<String> columnNames = getFormDefinitionColumnNames(tableName);
                                if (columnNames.contains(sort)) {
                                    sortProperty = FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + sort;
                                }
                            }
                            query += " ORDER BY e." + sortProperty;

                            if (desc) {
                                query += " DESC";
                            }
                        }
                        Query q = session.createQuery(query);

                        int s = (start == null) ? 0 : start;
                        q.setFirstResult(s);

                        if (rows != null && rows > 0) {
                            q.setMaxResults(rows);
                        }

                        if (params != null) {
                            int i = 0;
                            for (Object param : params) {
                                q.setParameter(i, param);
                                i++;
                            }
                        }

                        return q.list();
                    }
                });

        FormRowSet rowSet = new FormRowSet();
        rowSet.addAll(result);
        return rowSet;
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

        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        Long count = (Long) ht.execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException {
                        Query q = session.createQuery("SELECT COUNT(*) FROM " + entityName + " e " + condition);

                        if (params != null) {
                            int i = 0;
                            for (Object param : params) {
                                q.setParameter(i, param);
                                i++;
                            }
                        }

                        return ((Long) q.iterate().next()).longValue();
                    }
                });

        return count;
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

        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        String result = (String) ht.execute(
                new HibernateCallback() {

                    public Object doInHibernate(Session session) throws HibernateException {
                        String query = "SELECT e.id FROM " + entityName + " e WHERE " + FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + fieldName + " = ?";

                        Query q = session.createQuery(query);

                        q.setFirstResult(0);
                        q.setMaxResults(1);
                        q.setParameter(0, value);

                        if (q.list().size() > 0) {
                            return ((String) q.iterate().next()).toString();
                        }
                        return null;
                    }
                });

        return result;
    }

    /**
     * Saves (creates or updates) form data
     * @param form
     */
    @Override
    public void saveOrUpdate(Form form, FormRowSet rowSet) {
        String entityName = getFormEntityName(form);
        String tableName = getFormTableName(form);
        saveOrUpdate(entityName, tableName, rowSet);
    }

    /**
     * Saves (creates or updates) form data
     * @param form
     */
    @Override
    public void saveOrUpdate(String entityName, String tableName, FormRowSet rowSet) {
        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, rowSet);

        // save the form data
        for (FormRow row : rowSet) {
            ht.saveOrUpdate(entityName, row);
        }
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

        // get hibernate template
        HibernateTemplate ht = getHibernateTemplate(entityName, tableName, null);

        // save the form data
        for (String key : primaryKeyValues) {
            Object obj = ht.load(entityName, key);
            ht.delete(entityName, obj);
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
     * Gets the defined table name for the form
     * @param form
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
     * Gets the hibernate template for the form
     * @param form
     * @return
     */
    protected HibernateTemplate getHibernateTemplate(String entityName, String tableName, FormRowSet rowSet) {

        // determine hibernate entity name
        String path = getFormMappingPath();

        // get the hibernate template
        HibernateTemplate ht = null;

        // lookup existing mapping from cache
        HibernateTemplate template = templateCache.get(entityName);
        if (template != null) {
            ht = template;
            Logger.getLogger(FormDataDaoImpl.class.getName()).log(Level.FINE, "  --- Form {0} hibernate template found in cache", entityName);

            Collection<String> columnList = getFormRowColumnNames(rowSet);
            if (!columnList.isEmpty()) {

                // get existing mapping file
                String filename = entityName + ".hbm.xml";
                File mappingFile = new File(path, filename);
                if (mappingFile.exists()) {
                    Logger.getLogger(FormDataDaoImpl.class.getName()).log(Level.FINE, "  --- Form {0} loaded form mapping file {1}", new Object[]{entityName, mappingFile.getName()});

                    // check for changes
                    boolean changes = false;
                    Configuration configuration = new Configuration().configure();
                    configuration.addFile(mappingFile);
                    PersistentClass pc = configuration.getClassMapping(entityName);
                    Property custom = pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES);
                    Component customComponent = (Component) custom.getValue();
                    Iterator i = customComponent.getPropertyIterator();
                    for (String propName : columnList) {
                        boolean found = false;
                        while (i.hasNext()) {
                            Property property = (Property) i.next();
                            if (propName.equalsIgnoreCase(property.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            changes = true;
                            break;
                        }
                    }
                    if (changes) {
                        Logger.getLogger(FormDataDaoImpl.class.getName()).log(Level.FINE, "  --- Form {0} changes detected", entityName);

                        // properties changed, close session factory
                        SessionFactory sf = ht.getSessionFactory();
                        sf.close();
                        Logger.getLogger(FormDataDaoImpl.class.getName()).log(Level.FINE, "  --- Form {0} existing session factory closed", entityName);

                        // delete existing mapping file
                        mappingFile.delete();

                        // clear template to be recreated
                        ht = null;
                    }
                } else {
                    ht = null;
                }
            }
        }

        if (ht == null) {
            // no existing or outdated template found, create new one
            ht = createHibernateTemplate(entityName, tableName, rowSet);
            Logger.getLogger(FormDataDaoImpl.class.getName()).log(Level.INFO, "  --- Form {0} hibernate template created", entityName);

            // save into cache
            templateCache.put(entityName, ht);
        }
        return ht;
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
     * @param form
     * @param rowSet
     * @return
     */
    @Override
    public Collection<String> getFormRowColumnNames(FormRowSet rowSet) {
        Collection<String> columnList = new ArrayList<String>();
        Collection<String> lowerCaseColumnSet = new HashSet<String>();
        if (rowSet != null && !rowSet.isEmpty()) {
            // get properties in the rowset
            FormRow row = rowSet.get(0);

            // iterate thru to add property (with checking for duplicates)
            for (String propName : row.stringPropertyNames()) {
                // ignore fixed meta data; TODO: find more efficient method; TODO: ignore button values
                if (!FormUtil.PROPERTY_ID.equals(propName) && !FormUtil.PROPERTY_DATE_CREATED.equals(propName) && !FormUtil.PROPERTY_DATE_MODIFIED.equals(propName)) {
                    String lowerCasePropName = propName.toLowerCase();
                    if (!lowerCaseColumnSet.contains(lowerCasePropName)) {
                        columnList.add(propName);
                        lowerCaseColumnSet.add(lowerCasePropName);
                    }
                }
            }
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
        Collection<String> columnList = new HashSet<String>();

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
                findAllElementIds(form, columnList);
            }
        }
        return columnList;
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

    /**
     * Recurse into child elements to find add all element property ID to columnList.
     * @param element
     * @param columnList
     */
    protected void findAllElementIds(org.joget.apps.form.model.Element element, Collection<String> columnList) {
        if (!(element instanceof Form) && !(element instanceof Section) && element.getProperties() != null) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (id != null && !id.isEmpty()) {
                columnList.add(id);
            }
        }
        if (!(element instanceof SubForm)) { // do not recurse into subforms
            Collection<org.joget.apps.form.model.Element> children = element.getChildren();
            if (children != null) {
                for (org.joget.apps.form.model.Element child : children) {
                    findAllElementIds(child, columnList);
                }
            }
        }
    }

    /**
     * Create a new hibernate template for the form
     * @param form
     * @param rowSet
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    protected HibernateTemplate createHibernateTemplate(String entityName, String tableName, FormRowSet rowSet) {
        setCurrentThreadForm(entityName, tableName, rowSet);
        HibernateTemplate ht = new HibernateTemplate(loadCustomSessionFactory());
        return ht;
    }

    protected SessionFactory loadCustomSessionFactory() {
        // to be injected using lookup-method by Spring
        return null;
    }

    protected Object getCurrentThreadForm() {
        return currentThreadForm.get();
    }

    protected void setCurrentThreadForm(String entityName, String tableName, FormRowSet rowSet) {
        Object metadata[] = new Object[]{entityName, tableName, rowSet};
        currentThreadForm.set(metadata);
    }

    /**
     * Returns a customized hibernate configuration for the form.
     * @param config
     * @return
     * @throws DOMException
     * @throws HibernateException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws MappingException
     * @throws TransformerException
     */
    @Override
    public Configuration customizeConfiguration(Configuration config) throws ParserConfigurationException, SAXException, IOException, TransformerException {

        Configuration configuration = new Configuration().configure();
        configuration.addClass(FormRow.class);
        PersistentClass pc = configuration.getClassMapping("FormRow");
        Property custom = pc.getProperty(FormUtil.PROPERTY_CUSTOM_PROPERTIES);
        Component customComponent = (Component) custom.getValue();

        // get existing mapping file
        Object[] metaData = (Object[]) getCurrentThreadForm();
        String entityName = (String) metaData[0];
        String tableName = (String) metaData[1];
        String path = getFormMappingPath();
        String filename = entityName + ".hbm.xml";
        File mappingFile = new File(path, filename);
        if (!mappingFile.exists()) {
            InputStream is = Form.class.getResourceAsStream("/org/joget/apps/form/model/FormRow.hbm.xml");
            Document document = XMLUtil.loadDocument(is);

            // set entity name for form
            FormRowSet rowSet = (FormRowSet) metaData[2];
            pc.setEntityName(entityName);

            // set column names
            Collection<String> formFields = null;
            if (rowSet != null) {
                // column names from submitted fields
                formFields = getFormRowColumnNames(rowSet);
            } else {
                // column names from all forms mapped to this table
                formFields = getFormDefinitionColumnNames(tableName);
            }
            for (String field : formFields) {
                SimpleValue simpleValue = new SimpleValue();
                String columnName = FORM_PREFIX_COLUMN + field;
                simpleValue.addColumn(new Column(columnName));
                simpleValue.setTypeName("text");
                Property property = new Property();
                property.setName(field);
                property.setValue(simpleValue);
                customComponent.addProperty(property);
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

            // add dynamic components
            Iterator propertyIterator = customComponent.getPropertyIterator();
            while (propertyIterator.hasNext()) {
                Property prop = (Property) propertyIterator.next();
                Element element = document.createElement("property");
                Type type = prop.getType();
                String propName = prop.getName();
                Column column = (Column) prop.getColumnIterator().next();
                if (propName != null && !propName.isEmpty()) {
                    String propType = "";
                    if (type.getReturnedClass().getName().equals("java.lang.String")) {
                        if (column != null && column.getName().startsWith(FORM_PREFIX_COLUMN)) {
                            propType = "text";
                        } else {
                            propType = "string";
                        }
                    } else {
                        propType = type.getReturnedClass().getName();
                    }
                    element.setAttribute("name", propName);
                    element.setAttribute("column", column.getName());
                    element.setAttribute("type", propType);
                    element.setAttribute("not-null", String.valueOf(false));
                    node.appendChild(element);
                }
            }

            // save xml
            XMLUtil.saveDocument(document, mappingFile.getPath());

        }
        // add mapping to config
        config.addFile(mappingFile);
        return config;
    }
}
