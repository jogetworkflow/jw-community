package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;

public interface DataListFilterType extends PropertyEditable {

    public String getTemplate(DataList datalist, String name, String label);

    public DataListFilterQueryObject getQueryObject(DataList datalist, String name);
}
