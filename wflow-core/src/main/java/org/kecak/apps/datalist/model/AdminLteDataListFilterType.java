package org.kecak.apps.datalist.model;

import org.joget.apps.datalist.model.DataList;

public interface AdminLteDataListFilterType extends BootstrapDataListFilterType{
    String getAdminLteTemplate(DataList datalist, String name, String label);
}
