package org.joget.apps.app.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.collections4.map.LRUMap;
import org.joget.commons.util.StringUtil;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class responsible for replacing placeholders in application messages
 * with their corresponding values. This class provides multiple mechanisms to
 * process placeholders, including a streaming JSON parser and a regex-based fallback.
 * Processed values are cached to optimize performance.
 */
public final class AppMessageReplacer {
    private static final JsonMapper JSON_MAPPER = new JsonMapper();
    private static final Map<String, String> APP_MESSAGE_CACHE = Collections.synchronizedMap(new LRUMap<>(200));
    private static final Pattern APP_MESSAGE_PATTERN = Pattern.compile("(((['\"]).*?label.*?\\3\\s*:\\s*\\3)((?:\\\\\\3|(?:(?!\\3).))+)\\3)");
    private static final Pattern I18N_MESSAGE_PATTERN = Pattern.compile("(#i18n\\.([^#]+)#)");
    private static final Set<String> LABEL_NAME_BLOCKLIST = new HashSet<>(Arrays.asList(
            "labelColumn",  // FormOptionsBinder
            "optionLabel", // UserOptionsBinder
            "css-label-position", // Builder CSS properties
            "css-tablet-label-position"
    ));

    /**
     * Replaces placeholders in the given content with corresponding application message values.
     * This method attempts to replace the placeholders from the application message store either through
     * a JSON-based streaming replacement or a fallback regular expression-based replacement for invalid JSON.
     *
     * @param content    the content in which placeholders are to be replaced with application messages
     * @param escapeType the escape type to process the placeholders during replacement
     * @return the processed content with placeholders replaced by application message values
     */
    public static String replaceAppMessages(String content, String escapeType) {
        Map<String, String> appMessages = AppUtil.getAppMessageFromStore();
        if (appMessages == null) {
            return content;
        }

        String appMessageContent = appMessages.hashCode() + "::" + content + "::" + escapeType;
        String cacheKey = StringUtil.md5Base16Utf8(appMessageContent); // hash to minimize memory usage
        String cached = APP_MESSAGE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String result;
        try {
            result = streamingJsonReplacement(content, escapeType, appMessages);
        } catch (Exception e) {
            // fallback for invalid JSON (e.g. plain text templates)
            result = regexJsonReplacement(content, escapeType, appMessages);
        }

        APP_MESSAGE_CACHE.put(cacheKey, result);
        return result;
    }

    /**
     * Processes the given JSON content by replacing string values within JSON objects
     * based on application message mappings. The replacement is performed using
     * a streaming JSON parser and generator for efficient processing.
     *
     * @param content     the JSON content to process for replacing placeholders with application messages
     * @param escapeType  the escape type to apply to processed values during replacement
     * @param appMessages a mapping of placeholder keys to their corresponding replacement values
     * @return the resulting JSON content with placeholders replaced by application message values
     * @throws Exception if an error occurs during JSON parsing or processing
     */
    public static String streamingJsonReplacement(String content, String escapeType, Map<String, String> appMessages) throws Exception {
        try (StringReader reader = new StringReader(content);
             StringWriter writer = new StringWriter();
             JsonParser parser = JSON_MAPPER.getFactory().createParser(reader);
             JsonGenerator generator = JSON_MAPPER.getFactory().createGenerator(writer)) {

            String currentFieldName = null;

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == null) break;

                switch (token) {
                    case FIELD_NAME:
                        currentFieldName = parser.getCurrentName();
                        generator.writeFieldName(currentFieldName);
                        break;
                    case VALUE_STRING:
                        String fieldValue = parser.getText();
                        // field name must NOT be in the blocklist and field value must NOT be "true" or "false"
                        boolean shouldTranslate = currentFieldName != null && currentFieldName.contains("label")
                                && !LABEL_NAME_BLOCKLIST.contains(currentFieldName)
                                && !(fieldValue.equals("true") || fieldValue.equals("false"));
                        String newText = translateValue(fieldValue, appMessages, shouldTranslate);
                        generator.writeString(newText);
                        break;
                    default:
                        // forward other token types as-is
                        generator.copyCurrentEvent(parser);
                        break;
                }
            }
            generator.flush();
            return writer.toString();
        }
    }

    /**
     * Replaces placeholders in the provided content using regex patterns.
     * It identifies placeholders corresponding to application-specific messages
     * and replaces them with their mapped values from the provided message map.
     * Prefer using {@link #streamingJsonReplacement(String, String, Map)} for more efficient processing.
     *
     * @param content     the JSON content to process for placeholder replacement
     * @param escapeType  the escape type to apply to processed values during replacement
     * @param appMessages a mapping of placeholder keys to their corresponding replacement values
     * @return the resulting JSON content with placeholders replaced by application message values
     */
    public static String regexJsonReplacement(String content, String escapeType, Map<String, String> appMessages) {
        String key = "", match = "";

        // match app message pattern
        Matcher appMessageMatcher = APP_MESSAGE_PATTERN.matcher(content);
        while (appMessageMatcher.find()) {
            match = appMessageMatcher.group();
            key = appMessageMatcher.group(4);
            if (escapeType != null) {
                key = StringUtil.unescapeString(key, escapeType, null);
            }
            if (appMessages.containsKey(key)) {
                String translated = appMessages.get(key);
                if (escapeType != null) {
                    translated = StringUtil.escapeString(translated, escapeType, null);
                }
                content = content.replaceAll(StringUtil.escapeRegex(match), StringUtil.escapeRegex(appMessageMatcher.group(2) + translated + appMessageMatcher.group(3)));
            }
        }

        // match i18n hash variable pattern
        Matcher i18nMessageMatcher = I18N_MESSAGE_PATTERN.matcher(content);
        while (i18nMessageMatcher.find()) {
            match = i18nMessageMatcher.group();
            key = i18nMessageMatcher.group(2);
            if (escapeType != null) {
                key = StringUtil.unescapeString(key, escapeType, null);
            }
            if (appMessages.containsKey(key)) {
                String translated = appMessages.get(key);
                if (escapeType != null) {
                    translated = StringUtil.escapeString(translated, escapeType, null);
                }
                content = content.replaceAll(StringUtil.escapeRegex(match), StringUtil.escapeRegex(translated));
            }
        }
        return content;
    }

    /**
     * Translates the provided value using the given application message mappings, based on specific conditions.<p>
     * Translation is performed only if explicitly enabled, the value exists in the message map, or it matches
     * the defined internationalization (i18n) message patterns.
     *
     * @param value           the string value to be translated
     * @param appMessages     a mapping of keys to their corresponding translated messages
     * @param shouldTranslate whether translation should explicitly occur, regardless of other conditions
     * @return the translated value if applicable; otherwise, returns the original value
     */
    private static String translateValue(String value, Map<String, String> appMessages, boolean shouldTranslate) {
        // perform null and empty string check first, then
        // only translate if shouldTranslate OR value is in appMessages OR contains i18n pattern
        if (value == null || value.trim().isEmpty() || (!shouldTranslate && !appMessages.containsKey(value) && !I18N_MESSAGE_PATTERN.matcher(value).find())) {
            return value;
        }

        // First, check if the label has a direct translation
        if (shouldTranslate && appMessages.containsKey(value)) {
            return appMessages.get(value);
        }

        // If no direct translation, check if it contains i18n patterns
        Matcher m = I18N_MESSAGE_PATTERN.matcher(value);
        if (!m.find()) {
            // No i18n patterns found, return original value
            return value;
        }

        // Reset matcher and replace each i18n pattern manually
        m.reset();
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (m.find()) {
            // Append text before the match
            sb.append(value, lastEnd, m.start());

            // Get the key and replacement
            String key = m.group(2);
            String replacement = appMessages.get(key);
            sb.append(replacement != null ? replacement : m.group(1));

            lastEnd = m.end();
        }

        // Append remaining text after last match
        sb.append(value, lastEnd, value.length());

        return sb.toString();
    }
}
