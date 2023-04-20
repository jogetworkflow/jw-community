package org.joget.apps.datalist.lib;

import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataListDisplayColumnDefault;
import org.joget.apps.datalist.model.DataListQueryParam;

public class RowNumberColumn extends DataListDisplayColumnDefault {

    @Override
    public String getName() {
        return "RowNumberColumn";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Display row number in list";
    }

    @Override
    public String getColumnHeader() {
        return getPropertyString("label");
    }

    @Override
    public String getRowValue(Object row, int index) {
        DataListQueryParam param = getDatalist().getQueryParam(null, null);
        int offset = param.getStart() + 1;
        
        String rowNo = Integer.toString(offset + index);
        
        if (!getPropertyString("addLeadingZero").isEmpty()) {
            try {
                int digit = Integer.parseInt(getPropertyString("addLeadingZero"));
                if (digit > rowNo.length()) {
                    rowNo = StringUtils.leftPad(rowNo, digit, '0');
                }
            } catch (Exception e){
                //ignore
            }
        }
        
        return rowNo;
    }

    @Override
    public Boolean isRenderHtml() {
        return false;
    }

    @Override
    public String getLabel() {
        return "Row Number";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/rowNumberColumn.json", null, true, null);
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-list-ol\"></i>";
    }
}
