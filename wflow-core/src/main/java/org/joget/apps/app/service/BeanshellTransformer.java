package org.joget.apps.app.service;

import org.eclipse.transformer.action.ActionContext;
import org.eclipse.transformer.action.ByteData;
import org.eclipse.transformer.action.SelectionRule;
import org.eclipse.transformer.action.SignatureRule;
import org.eclipse.transformer.action.impl.ByteDataImpl;
import org.eclipse.transformer.action.impl.JavaActionImpl;
import org.eclipse.transformer.action.impl.SelectionRuleImpl;
import org.eclipse.transformer.action.impl.SignatureRuleImpl;
import org.eclipse.transformer.jakarta.JakartaTransform;
import org.joget.commons.util.DynamicDataSource;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeanshellTransformer {

    public static final Pattern JAVAX_PACKAGE_PATTERN = Pattern.compile("(?<=^|\\s|\\b)(?<!\\\"|\\.)javax(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*");

    private static final Map<String, String> jakartaPackages = new HashMap<>();
    private static JavaActionImpl javaAction;
    private static Cache cache;

    static {
        // JakartaTransform class is provided by Eclipse Transformer.
        // It is just a dependency that provides javax -> jakarta namespace mappings.
        try (InputStream is = JakartaTransform.class.getResourceAsStream(JakartaTransform.DEFAULT_RENAMES_REFERENCE)) {
            if (is == null) {
                throw new NullPointerException("Unable to retrieve Jakarta packages");
            }

            // Get mappings to transform and add to map
            Properties renames = new Properties();
            renames.load(is);
            renames.forEach((key, value) -> {
                String javaxPackage = key.toString();
                String jakartaPackage = value.toString();
                jakartaPackages.put(javaxPackage, jakartaPackage);
            });

            // Create Java source transformer. Might be NOP logger, should be fine.
            Logger logger = LoggerFactory.getLogger(BeanshellTransformer.class);
            SelectionRule selRule = new SelectionRuleImpl(logger, new HashMap<>(), new HashMap<>());
            SignatureRule sigRule = new SignatureRuleImpl(logger, jakartaPackages, null, null, null, null, null, null);
            ActionContext ac = new ActionContext(logger, selRule, sigRule);
            javaAction = new JavaActionImpl(ac);

            cache = AppUtil.getCache("org.joget.cache.BEANSHELL_TRANSFORMER_CACHE");
        } catch (Exception e) {
            LogUtil.error(BeanshellTransformer.class.getName(), e, "Unable to start Beanshell Transformer");
        }
    }

    /**
     * Transforms beanshell code from {@code javax} to {@code jakarta} namespace.
     *
     * @param script the beanshell to transform
     * @return the transformed beanshell
     */
    public static String doTransform(String script) {
        // get from cache
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "::" + script.hashCode();
        Object cachedElem = cache.get(cacheKey);
        if (cachedElem != null) {
            return (String) cachedElem;
        }

        // transform
        if (requireTransform(script)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(script.getBytes(StandardCharsets.UTF_8));
            ByteData byteData = new ByteDataImpl(cacheKey + ".java", byteBuffer, StandardCharsets.UTF_8);
            ByteData resultByteData = javaAction.apply(byteData);
            script = new String(resultByteData.buffer().array());
            cache.put(cacheKey, script);
        }
        return script;
    }

    /**
     * Checks whether a beanshell script requires {@code javax} to {@code jakarta} namespace transformation.
     *
     * @param script the beanshell to check
     * @return {@code true} if transformation required; {@code false} otherwise.
     */
    public static boolean requireTransform(String script) {
        Matcher matcher = JAVAX_PACKAGE_PATTERN.matcher(script);
        while (matcher.find()) {
            String packageName = matcher.group();
            if (isJavaxPackage(packageName)) {
                return true;
            }
        }
        return matcher.find();
    }

    private static boolean isJavaxPackage(String packageName) {
        return jakartaPackages.keySet().stream().anyMatch(packageName::startsWith);
    }
}
