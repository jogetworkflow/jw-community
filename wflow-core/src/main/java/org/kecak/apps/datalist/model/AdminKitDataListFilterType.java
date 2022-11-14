package org.kecak.apps.datalist.model;

import org.joget.apps.datalist.model.DataList;

public interface AdminKitDataListFilterType extends BootstrapDataListFilterType{
    String getAdminKitTemplate(DataList datalist, String name, String label);
}
