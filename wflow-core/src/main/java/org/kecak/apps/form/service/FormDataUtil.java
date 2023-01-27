package org.kecak.apps.form.service;

import com.kinnarastudio.commons.jsonstream.JSONCollectors;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kecak.apps.form.model.GridElement;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service("formDataUtil")
public class FormDataUtil implements ApplicationContextAware  {

    public final static Pattern DATA_PATTERN = Pattern.compile("data:(?<mime>[\\w/\\-\\.]+);(?<properties>(\\w+=[^;]+;)*)base64,(?<data>.*)");

    /**
     * Decode base64 to file
     *
     * @param uri
     * @return
     */
    @Nullable
    public static MultipartFile decodeFile(@Nonnull String uri) throws IllegalArgumentException {
        Matcher m = DATA_PATTERN.matcher(uri);

        if(m.find()) {
            String contentType = m.group("mime");
            String extension = contentType.split("/")[1];
            String fileName = getFileName(m.group("properties"), extension);
            String base64 = m.group("data");

            return decodeFile(fileName, contentType, base64.trim());
        } else {
            return null;
        }
    }

    /**
     *
     * @param properties
     * @param extension
     * @return
     */
    public static String getFileName(String properties, String extension) {
        for(String prop : properties.split(";")) {
            String[] keyValue = prop.split("=", 2);
            if(keyValue.length > 1 && keyValue[0].equalsIgnoreCase("filename")) {
                return keyValue[1];
            }
        }

        return "file." + extension;
    }

    /**
     * Collect grid element
     *
     * @param gridElement
     * @param rowSet
     * @return
     */
    public static JSONArray collectGridElement(@Nonnull final GridElement gridElement, @Nonnull final FormRowSet rowSet, final boolean asOptions) {
        return rowSet.stream()
                .map(r -> collectGridElement(gridElement, r, asOptions))
                .collect(JSONCollectors.toJSONArray());
    }
    
    /**
     * Collect grid element
     *
     * @param gridElement
     * @param row
     * @return
     */
    public static JSONObject collectGridElement(@Nonnull final GridElement gridElement, @Nonnull final FormRow row, boolean asOptions) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        final Map<String, String>[] columnProperties = gridElement.getColumnProperties();

        if (columnProperties == null && gridElement instanceof Element) {
            return collectElement((Element) gridElement, row);
        } else {
            final JSONObject jsonObject = Optional.ofNullable(columnProperties)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .collect(JSONCollectors.toJSONObject(gridElement::getField, props -> {
                        final String primaryKey = Optional.of(row).map(FormRow::getId).orElse("");
                        final String columnName = Optional.of(props)
                                .map(gridElement::getField)
                                .orElse("");
                        final String columnType = Optional.of(props)
                                .map(m -> m.getOrDefault("formatType", ""))
                                .orElse("");

                        return Optional.of(columnName)
                                .filter(s -> s != null && !s.isEmpty())
                                .map(row::getProperty)
                                .map(s -> {
                                    if (asOptions && "options".equals(columnType)) {
                                        return Optional.of(";")
                                                .map(s::split)
                                                .map(Arrays::stream)
                                                .orElseGet(Stream::empty)
                                                .filter(Objects::nonNull)
                                                .map(value -> {
                                                    String formattedValue = gridElement.formatColumn(columnName, props, primaryKey, value, appDefinition.getAppId(), appDefinition.getVersion(), "");
                                                    try {
                                                        JSONObject json = new JSONObject();
                                                        json.put(FormUtil.PROPERTY_VALUE, value);
                                                        json.put(FormUtil.PROPERTY_LABEL, formattedValue);
                                                        return json;
                                                    } catch (JSONException e) {
                                                        return formattedValue;
                                                    }
                                                })
                                                .collect(JSONCollectors.toJSONArray());
                                    } else {
                                        return gridElement.formatColumn(columnName, props, primaryKey, s, appDefinition.getAppId(), appDefinition.getVersion(), "");
                                    }
                                })
                                .orElse(null);
                    }));

            collectRowMetaData(row, jsonObject);

            return jsonObject;
        }
    }
    
    /**
     * Collect metadata
     *
     * @param form       Form
     * @param formData   FormData
     * @param jsonObject I/O JSONObject
     */
    public static void collectRowMetaData(@Nonnull final Form form, @Nonnull FormData formData, @Nonnull final JSONObject jsonObject) {
        Optional.ofNullable(formData.getLoadBinderData(form))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .ifPresent(r -> collectRowMetaData(r, jsonObject));
    }

    /**
     * Collect metadata
     *
     * @param row        FormRow
     * @param jsonObject I/O JSONObject
     */
    public static void collectRowMetaData(@Nonnull final FormRow row, @Nonnull final JSONObject jsonObject) {
        jsonPutOnce("_" + FormUtil.PROPERTY_ID, row.getId(), jsonObject);
        jsonPutOnce("_" + FormUtil.PROPERTY_DATE_CREATED, row.getDateCreated(), jsonObject);
        jsonPutOnce("_" + FormUtil.PROPERTY_CREATED_BY, row.getCreatedBy(), jsonObject);
        jsonPutOnce("_" + FormUtil.PROPERTY_DATE_MODIFIED, row.getDateModified(), jsonObject);
        jsonPutOnce("_" + FormUtil.PROPERTY_MODIFIED_BY, row.getModifiedBy(), jsonObject);
    }

    /**
     * Collect process metadata
     *
     * @param formData   Input FormData
     * @param jsonObject Input/Output JSONObject
     */
    public static void collectProcessMetaData(@Nonnull FormData formData, @Nonnull JSONObject jsonObject) {
        try {
            jsonObject.putOpt("activityId", formData.getActivityId());
            jsonObject.putOpt("processId", formData.getProcessId());
        } catch (JSONException e) {
            LogUtil.error(FormDataUtil.class.getName(), e, e.getMessage());
        }
    }

    /**
     * Collect element
     *
     * @param element
     * @param row
     * @return
     */
    public static JSONObject collectElement(@Nonnull final Element element, @Nonnull final FormRow row) {
        Objects.requireNonNull(element);

        final JSONObject jsonObject = row.entrySet().stream()
                .collect(JSONCollectors.toJSONObject(e -> e.getKey().toString(), Map.Entry::getValue));

        collectRowMetaData(row, jsonObject);

        return jsonObject;
    }

    /**
     * Collect element
     *
     * @param element
     * @param rowSet
     * @return
     */
    public static JSONArray collectElement(@Nonnull final Element element, @Nonnull final FormRowSet rowSet) {
        return rowSet.stream()
                .map(r -> collectElement(element, r))
                .collect(JSONCollectors.toJSONArray());
    }

    /**
     * @param key   I
     * @param value I
     * @param json  I/O
     */
    public static void jsonPutOnce(@Nonnull String key, Object value, @Nonnull final JSONObject json) {
        try {
            if (!json.has(key) && value != null)
                json.put(key, value);
        } catch (JSONException e) {
            LogUtil.error(FormDataUtil.class.getName(), e, e.getMessage());
        }
    }

    /**
     * Convert {@link FormRow} to {@link JSONObject}
     *
     * @param row
     * @return
     */
    @Deprecated
    @Nonnull
    public static JSONObject convertFromRowToJsonObject(@Nonnull final Element element, @Nonnull final FormData formData, @Nonnull final FormRow row, final boolean asOptions) {
        if (element instanceof GridElement) {
            return collectGridElement((GridElement) element, row, asOptions);
        } else if (element instanceof FormContainer) {
            return collectContainerElement((FormContainer) element, formData, row);
        } else {
            return collectElement(element, row);
        }
    }


    /**
     * Convert {@link FormRowSet} to {@link JSONArray}
     *
     * @param rowSet
     * @return
     */
    @Deprecated
    @Nonnull
    public static JSONArray convertFormRowSetToJsonArray(@Nonnull final Element element, @Nonnull final FormData formData, @Nullable final FormRowSet rowSet, final boolean asOptions) {
        return Optional.ofNullable(rowSet)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(r -> convertFromRowToJsonObject(element, formData, r, asOptions))
                .collect(JSONCollectors.toJSONArray());
    }

    /**
     * Collect container element
     *
     * @param containerElement
     * @param formData
     * @return
     */
    public static JSONObject collectContainerElement(@Nonnull final FormContainer containerElement, @Nonnull final FormData formData, @Nonnull final FormRow row) {
        assert containerElement instanceof Element;

        final JSONObject jsonObject = elementStream((Element) containerElement, formData)
                .filter(e -> !(e instanceof FormContainer))
                .collect(JSONCollectors.toJSONObject(e -> e.getPropertyString(FormUtil.PROPERTY_ID),
                        e -> FormUtil.getElementPropertyValue(e, formData)));

        collectRowMetaData(row, jsonObject);

        return jsonObject;
    }

    /**
     * Decode base64 to file
     *
     * @param filename
     * @param contentType
     * @param base64EncodedFile
     * @return
     */
    @Nullable
    public static MultipartFile decodeFile(@Nonnull String filename, String contentType, @Nonnull String base64EncodedFile) throws IllegalArgumentException {
        if (base64EncodedFile.isEmpty())
            return null;

        byte[] data = Base64.getDecoder().decode(base64EncodedFile);
        return new MockMultipartFile(filename, filename, contentType, data);
    }

    /**
     * Stream element children
     *
     * @param element
     * @return
     */
    @Nonnull
    public static Stream<Element> elementStream(@Nonnull Element element, FormData formData) {
        if (!element.isAuthorize(formData)) {
            return Stream.empty();
        }

        Stream<Element> stream = Stream.of(element);
        for (Element child : element.getChildren()) {
            stream = Stream.concat(stream, elementStream(child, formData));
        }
        return stream;
    }

    static ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }
}
