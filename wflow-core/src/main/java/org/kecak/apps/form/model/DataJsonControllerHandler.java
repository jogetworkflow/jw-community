package org.kecak.apps.form.model;

import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.json.JSONException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handler for DataJsonController, this interface will be called
 * when DataJsonController assigns values to {@link FormData#addRequestParameterValues(String, String[])}
 *
 * Implement in {@link Element}. How the element will handle json data
 */
public interface DataJsonControllerHandler {
    String PARAMETER_OPTIMIZE_READONLY_ELEMENTS = "_OPTIMIZE_READONLY_ELEMENTS";
    String PARAMETER_DATA_JSON_CONTROLLER = "_DATA_JSON_CONTROLLER";
    String PARAMETER_AS_OPTIONS = "_AS_OPTIONS";

    /**
     *
     * @param values
     * @param element
     * @param formData
     * @return data that will be passed to request parameter
     */
    default String[] handleMultipartDataRequest(@Nonnull String[] values, @Nonnull Element element, FormData formData) {
        return values;
    }

    /**
     *
     * @param value can be one of JSONObject, JSONArray, String or primitives
     * @param element
     * @param formData
     * @return data that will be passed to request parameter
     */
    default String[] handleJsonDataRequest(@Nullable Object value, @Nonnull Element element, @Nonnull FormData formData) throws JSONException {
        if(value == null) {
            return new String[0];
        }

        if (value instanceof Double) {
            return new String[]{String.format("%f", value).replaceAll("(?<!\\.)0+$", "")};
        } else {
            return new String[]{String.valueOf(value)};
        }
    }

    /**
     * Handle values that will be thrown as response in DataJsonController
     *
     * @param element
     * @param formData
     * @value that will be shown as response
     */
    default Object handleElementValueResponse(@Nonnull Element element, @Nonnull FormData formData) throws JSONException {
        String[] values = FormUtil.getElementPropertyValues(element, formData);
        return String.join(";", values);
    }
}
