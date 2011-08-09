package org.joget.apps.datalist.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.displaytag.properties.SortOrderEnum;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListBuilderProperty;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Column;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.Section;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Test implementation for a binder that retrieves rows from a form table.
 */
public class FormRowDataListBinder extends DataListBinderDefault implements PropertyEditable {

    @Override
    public String getName() {
        return "Form Data Binder";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Retrieves data rows from a form table.";
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        String formDefField = null;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            String formJsonUrl = "[CONTEXT_PATH]/web/json/console/app/" + appDef.getId() + "/" + appDef.getVersion() + "/forms/options";
            formDefField = "{name:'formDefId',label:'@@datalist.formrowdatalistbinder.formId@@',type:'selectbox',options_ajax:'" + formJsonUrl + "'}";
        } else {
            formDefField = "{name:'formDefId',label:'@@datalist.formrowdatalistbinder.formId@@',type:'textfield'}";
        }
        Object[] arguments = new Object[]{formDefField};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/formRowDataListBinder.json", arguments, true, "message/datalist/formRowDataListBinder");
        return json;
    }

    @Override
    public String getDefaultPropertyValues() {
        return null;
    }

    @Override
    public DataListBuilderProperty[] getBuilderProperties() {
        DataListBuilderProperty[] builderProps = {};
        return builderProps;
    }

    @Override
    public DataListColumn[] getColumns() {
        List<DataListColumn> columns = new ArrayList<DataListColumn>();

        // retrieve columns
        Form form = getSelectedForm();
        if (form != null) {
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            String tableName = formDataDao.getFormTableName(form);
            Collection<String> columnNames = formDataDao.getFormDefinitionColumnNames(tableName);
            for (String columnName : columnNames) {
                Element element = FormUtil.findElement(columnName, form, null);
                if (element != null && !(element instanceof Form) && !(element instanceof Column) && !(element instanceof Section)) {
                    String id = element.getPropertyString(FormUtil.PROPERTY_ID);
                    String label = element.getPropertyString(FormUtil.PROPERTY_LABEL);
                    if (id != null && !id.isEmpty()) {
                        if (label == null || label.isEmpty()) {
                            label = id;
                        }
                        columns.add(new DataListColumn(id, label, true));
                    }
                }
            }
        }

        // sort columns by name
        Collections.sort(columns, new Comparator<DataListColumn>() {

            public int compare(DataListColumn o1, DataListColumn o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // add default metadata fields
        columns.add(0, new DataListColumn(FormUtil.PROPERTY_DATE_MODIFIED, "Date Modified", true));
        columns.add(0, new DataListColumn(FormUtil.PROPERTY_DATE_CREATED, "Date Created", true));
        columns.add(0, new DataListColumn(FormUtil.PROPERTY_ID, "ID", true));

        return columns.toArray(new DataListColumn[0]);
    }

    @Override
    public String getPrimaryKeyColumnName() {
        return "id";
    }

    @Override
    public DataListCollection getData(DataList dataList, Properties properties, String filterName, String filterValue, String sort, Boolean desc, int start, int rows) {
        DataListCollection resultList = new DataListCollection();

        Form form = getSelectedForm();
        if (form != null) {
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            String propertyName = getFormPropertyName(form, filterName);

            Object[] criteria = getCriteria(properties, propertyName, filterValue);

            FormRowSet rowSet = formDataDao.find(form, criteria[0].toString(), (Object[]) criteria[1], sort, desc, start, rows);
            resultList.addAll(rowSet);
            int total = getDataTotalRowCount(dataList, properties, filterName, filterValue);
            resultList.setObjectsPerPage(rows);
            resultList.setFullListSize(total);
            resultList.setSortCriterion(sort);
            if (desc != null) {
                if (desc.booleanValue()) {
                    resultList.setSortDirection(SortOrderEnum.DESCENDING);
                } else {
                    resultList.setSortDirection(SortOrderEnum.ASCENDING);
                }
            }
        }

        return resultList;
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Properties properties, String filterName, String filterValue) {
        int count = 0;
        Form form = getSelectedForm();
        if (form != null) {
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            String propertyName = getFormPropertyName(form, filterName);

            Object[] criteria = getCriteria(properties, propertyName, filterValue);

            Long rowCount = formDataDao.count(form, criteria[0].toString(), (Object[]) criteria[1]);
            count = rowCount.intValue();
        }
        return count;
    }

    protected Form getSelectedForm() {
        Form form = null;
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        String formDefId = getProperties().getProperty("formDefId");
        if (formDefId != null) {
            Long version = null;
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef != null) {
                String formJson = formDef.getJson();
                if (formJson != null) {
                    form = (Form) formService.createElementFromJson(formJson);
                }
            }
        }
        return form;
    }

    /**
     * Retrieves the object property name for the form column i.e. prefixed with FormUtil.PROPERTY_CUSTOM_PROPERTIES for custom form fields.
     * @param form
     * @param propertyName
     * @return
     */
    protected String getFormPropertyName(Form form, String propertyName) {
        if (propertyName != null && !propertyName.isEmpty()) {
            FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            Collection<String> columnNames = formDataDao.getFormDefinitionColumnNames(form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME));
            if (columnNames.contains(propertyName) && !FormUtil.PROPERTY_ID.equals(propertyName)) {
                propertyName = FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + propertyName;
            }
        }
        return propertyName;
    }

    protected Object[] getCriteria(Properties properties, String filterName, String filterValue) {
        Collection<Object> params = new ArrayList<Object>();
        String condition = "";

        if (filterName != null && !filterName.isEmpty() && filterValue != null) {
            condition = " WHERE " + filterName + " LIKE ?";
            params.add("%" + filterValue + "%");
        }

        String extraCondition = properties.getProperty("extraCondition");
        String keyName = properties.getProperty(Userview.USERVIEW_KEY_NAME);
        String keyValue = properties.getProperty(Userview.USERVIEW_KEY_VALUE);

        if (extraCondition != null && extraCondition.contains(USERVIEW_KEY_SYNTAX)) {
            if (keyValue == null) {
                keyValue = "";
            }
            extraCondition = extraCondition.replaceAll(USERVIEW_KEY_SYNTAX, keyValue);
        } else if (keyName != null && !keyName.isEmpty() && keyValue != null && !keyValue.isEmpty()) {
            if (condition.trim().length() > 0) {
                condition += " AND ";
            } else {
                condition += " WHERE ";
            }
            if (FormUtil.PROPERTY_ID.equals(keyName) || FormUtil.PROPERTY_DATE_CREATED.equals(keyName) || FormUtil.PROPERTY_DATE_MODIFIED.equals(keyName)) {
                condition += keyName + " = ?";
            } else {
                condition += FormUtil.PROPERTY_CUSTOM_PROPERTIES + "." + keyName + " = ?";
            }
            params.add(keyValue);
        }

        if (extraCondition != null && !extraCondition.isEmpty()) {
            if (condition.trim().length() > 0) {
                condition += " AND ";
            } else {
                condition += " WHERE ";
            }
            condition += extraCondition;
        }

        return new Object[]{condition, params.toArray()};
    }
}
