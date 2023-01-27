package org.kecak.apps.datalist;

import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.json.JSONException;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 * Handler for DataJsonController, this interface will be called
 * when DataJsonController assigns values to {@link DataListColumnFormat#format(DataList, DataListColumn, Object, Object)}
 *
 * Implement in {@link DataListColumnFormat}. How the formatter will handle json data
 */
public interface DataJsonControllerDataListColumnHandler {
    default Object handleColumnValueResponse(@Nonnull DataList dataList, @Nonnull DataListColumn column, DataListColumnFormat formatter, Map<String, Object> row, String value) throws JSONException {
        return Optional.of(formatter)
                .map(f -> f.format(dataList, column, row, value))
                .map(s -> s.replaceAll("<[^>]*>", ""))
                .orElse(value);
    }
}
