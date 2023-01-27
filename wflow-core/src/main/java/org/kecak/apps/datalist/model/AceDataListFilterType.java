package org.kecak.apps.datalist.model;

import org.joget.apps.datalist.model.DataList;

public interface AceDataListFilterType extends BootstrapDataListFilterType{
    String getAceTemplate(DataList datalist, String name, String label);
}
