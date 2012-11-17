package org.joget.plugin.base;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.StringMap;
import org.joget.commons.util.HostManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

public class PluginManager implements ApplicationContextAware {

    private Felix felix = null;
    private String baseDirectory = SetupManager.getBaseSharedDirectory() + File.separator + "app_plugins";
    private ApplicationContext applicationContext;
    private Map<Class, Map<String, Plugin>> pluginCache = new HashMap<Class, Map<String, Plugin>>();

    public PluginManager() {
        init();
    }

    public PluginManager(String baseDirectory) {
        if (baseDirectory != null) {
            this.baseDirectory = baseDirectory;
        }
        init();
    }

    /**
     * Retrieves plugin base directory from system setup
     */
    public String getBaseDirectory() {
        try {
            SetupManager setupManager = (SetupManager) applicationContext.getBean("setupManager");
            String dataFileBasePath = setupManager.getSettingValue("dataFileBasePath");
            if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
                return dataFileBasePath + File.separator + "plugins";
            } else {
                return baseDirectory;
            }
        } catch (Exception ex) {
            return baseDirectory;
        }
    }

    /**
     * Initializes the plugin manager
     */
    protected void init() {
        Properties config = new Properties();
        try {
            config.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ex) {
            LogUtil.error(PluginManager.class.getName(), ex, "");
        }

        // Create a case-insensitive configuration property map.
        Map configMap = new StringMap(false);
        configMap.putAll(config);
        // Configure the Felix instance to be embedded.

        // Explicitly specify the directory to use for caching bundles.
        String targetCache = "target/felix-cache/";
        String tempDir = System.getProperty("java.io.tmpdir");
        File targetDir = new File(tempDir, targetCache);
        File targetCacheDir = new File(targetDir, "cache");

        if (HostManager.isVirtualHostEnabled()) {
            // locate empty cache directory to use
            boolean proceed = false;
            int count = 0;
            while (!proceed) {
                // check for existing cache
                String dirName = "cache" + count;
                targetCacheDir = new File(targetDir, dirName);
                File[] bundles = targetCacheDir.listFiles();
                proceed = bundles == null || bundles.length <= 1;
                count++;
            }
        }
        // set configuration
        configMap.put("org.osgi.framework.storage", targetCacheDir.getAbsolutePath());
        configMap.put("felix.log.level", "0");
        configMap.put("org.osgi.framework.storage.clean", "onFirstInit");

        try {
            if (felix == null) {
                felix = new Felix(configMap);
                felix.start();
            }
            //refresh();
            LogUtil.info(PluginManager.class.getName(), "PluginManager initialized");
        } catch (Exception ex) {
            LogUtil.error(PluginManager.class.getName(), ex, "Could not create framework");
        }

    }

    /**
     * Find and install plugins from the baseDirectory
     */
    public void refresh() {
        uninstallAll(false);
        installBundles();
    }

    protected void installBundles() {

        Collection<URL> urlList = new ArrayList<URL>();
        File baseDirFile = new File(getBaseDirectory());
        recurseDirectory(urlList, baseDirFile);

        Collection<Bundle> bundleList = new ArrayList<Bundle>();
        for (URL url : urlList) {
            // install the JAR file as a bundle
            String location = url.toExternalForm();
            Bundle bundle = installBundle(location);
            if (bundle != null) {
                bundleList.add(bundle);
            }

        }

        for (Bundle bundle : bundleList) {
            startBundle(bundle);
        }


    }

    protected void recurseDirectory(Collection<URL> urlList, File baseDirFile) {
        File[] files = baseDirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                //LogUtil.info(getClass().getName(), " -" + file.getName());
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    try {
                        urlList.add(file.toURI().toURL());
                        LogUtil.debug(PluginManager.class.getName(), " found jar " + file.toURI().toURL());
                    } catch (MalformedURLException ex) {
                        LogUtil.error(PluginManager.class.getName(), ex, "");
                    }
                } else if (file.isDirectory()) {
                    recurseDirectory(urlList, file);
                }
            }
        }
    }

    protected Bundle installBundle(String location) {
        try {
            BundleContext context = felix.getBundleContext();
            Bundle newBundle = context.installBundle(location);
            if (newBundle.getSymbolicName() == null) {
                newBundle.uninstall();
                newBundle = null;
            } else {
                newBundle.update();
            }
            // clear cache
            pluginCache.clear();
            return newBundle;
        } catch (Exception be) {
            LogUtil.error(PluginManager.class.getName(), be, "Failed bundle installation from " + location + ": " + be.toString());
            return null;
        }
    }

    protected boolean startBundle(Bundle bundle) {
        try {
            //bundle.update();
            bundle.start();
            LogUtil.info(PluginManager.class.getName(), "Bundle " + bundle.getSymbolicName() + " started");
        } catch (Exception be) {
            LogUtil.error(PluginManager.class.getName(), be, "Failed bundle start for " + bundle + ": " + be.toString());
            return true;
        }
        return false;
    }

    /**
     * List registered plugins
     * @return
     */
    public Collection<Plugin> list() {
        return list(null);
    }

    /**
     * Returns a list of plugins, both from the OSGI container and the classpath.
     * Plugins from the OSGI container will take priority if there are conflicting classes.
     * @param clazz Optional filter for type of plugins to return, null will return all.
     * @return
     */
    public Collection<Plugin> list(Class clazz) {
        // lookup in cache
        Class classFilter = (clazz != null) ? clazz : Plugin.class;
        Map<String, Plugin> pluginMap = pluginCache.get(classFilter);
        if (pluginMap == null) {
            // load plugins
            pluginMap = internalLoadPluginMap(clazz);

            // store in cache
            pluginCache.put(classFilter, pluginMap);
        }
        Collection<Plugin> pluginList = new ArrayList<Plugin>();
        pluginList.addAll(pluginMap.values());
        return pluginList;
    }

    /**
     * Returns a list of plugins from the OSGI container only.
     * Plugins from the OSGI container will take priority if there are conflicting classes.
     * @param clazz Optional filter for type of plugins to return, null will return all.
     * @return
     */
    public Collection<Plugin> listOsgiPlugin(Class clazz) {
        Map<String, Plugin> pluginMap = new TreeMap<String, Plugin>();

        // find OSGI plugins
        Collection<Plugin> pluginList = loadOsgiPlugins();
        for (Plugin plugin : pluginList) {
            if (clazz == null || clazz.isAssignableFrom(plugin.getClass())) {
                pluginMap.put(plugin.getName(), plugin);
            }
        }

        return pluginMap.values();
    }

    /**
     * Returns a map of plugins with class name as key, both from the OSGI container and the classpath.
     * Plugins from the OSGI container will take priority if there are conflicting classes.
     * @param clazz Optional filter for type of plugins to return, null will return all.
     * @return
     */
    public Map<String, Plugin> loadPluginMap(Class clazz) {
        Map<String, Plugin> pluginMap = new HashMap<String, Plugin>();
        for (Plugin plugin : list(clazz)) {
            pluginMap.put(plugin.getClass().getName(), plugin);
        }
        return pluginMap;
    }

    /**
     * Returns a list of plugins, both from the OSGI container and the classpath.
     * Plugins from the OSGI container will take priority if there are conflicting classes.
     * @param clazz Optional filter for type of plugins to return, null will return all.
     * @return A Map of name=pluginObject
     */
    protected Map<String, Plugin> internalLoadPluginMap(Class clazz) {
        Map<String, Plugin> pluginMap = new TreeMap<String, Plugin>();

        Class classFilter = (clazz != null) ? clazz : Plugin.class;

        // find plugins in classpath
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(classFilter));
        Set<BeanDefinition> components = provider.findCandidateComponents("org.joget");
        for (BeanDefinition component : components) {
            String beanClassName = component.getBeanClassName();
            try {
                Object beanObj;
                Class beanClass = Class.forName(beanClassName);
                beanObj = beanClass.newInstance();
                if (beanObj instanceof Plugin) {
                    Plugin plugin = (Plugin) beanObj;
                    pluginMap.put(plugin.getName(), plugin);
                }
            } catch (Exception ex) {
                LogUtil.warn(PluginManager.class.getName(), " Error loading plugin class  " + beanClassName);
            }
        }

        // find OSGI plugins
        Collection<Plugin> pluginList = loadOsgiPlugins();
        for (Plugin plugin : pluginList) {
            if (clazz == null || clazz.isAssignableFrom(plugin.getClass())) {
                pluginMap.put(plugin.getName(), plugin);
            }
        }

        LogUtil.debug(PluginManager.class.getName(), " Loaded plugins from classpath and OSGI container");
        return pluginMap;
    }

    /**
     * Load all plugins from the OSGI container
     * @return
     */
    protected Collection<Plugin> loadOsgiPlugins() {
        Collection<Plugin> list = new ArrayList<Plugin>();
        BundleContext context = felix.getBundleContext();
        Bundle[] bundles = context.getBundles();
        for (Bundle b : bundles) {
            ServiceReference[] refs = b.getRegisteredServices();
            if (refs != null) {
                for (ServiceReference sr : refs) {
                    LogUtil.debug(PluginManager.class.getName(), " bundle service: " + sr);
                    Object obj = context.getService(sr);
                    if (obj instanceof Plugin) {
                        list.add((Plugin) obj);
                    }
                    context.ungetService(sr);
                }
            }
        }
        return list;
    }

    /**
     * Disable plugin
     * @param name
     */
    public boolean disable(String name) {
        boolean result = false;
        BundleContext context = felix.getBundleContext();
        ServiceReference sr = context.getServiceReference(name);
        if (sr != null) {
            try {
                sr.getBundle().stop();
                context.ungetService(sr);
                result = true;
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
            }
        }
        return result;
    }

    /**
     * Install a new plugin
     * @return
     */
    public boolean upload(String filename, InputStream in) {
        String location = null;
        File outputFile = null;
        try {
            // check filename
            if (filename == null || filename.trim().length() == 0) {
                throw new PluginException("Invalid plugin name");
            }
            if (!filename.endsWith(".jar")) {
                filename += ".jar";
            }

            // write file
            FileOutputStream out = null;
            try {
                outputFile = new File(getBaseDirectory(), filename);
                File outputDir = outputFile.getParentFile();
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
                out = new FileOutputStream(outputFile);
                BufferedInputStream bin = new BufferedInputStream(in);
                int len = 0;
                byte[] buffer = new byte[4096];
                while ((len = bin.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
                location = outputFile.toURI().toURL().toExternalForm();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    LogUtil.error(PluginManager.class.getName(), ex, "");
                }
            }

            // validate jar file
            boolean isValid = false;
            try {
                JarFile jarFile = new JarFile(outputFile);
                isValid = true;
            } catch (IOException ex) {
                //delete invalid file
                try {
                    outputFile.delete();
                } catch (Exception e) {
                    LogUtil.error(PluginManager.class.getName(), ex, "");
                }

                LogUtil.error(PluginManager.class.getName(), ex, "");
                throw new PluginException("Invalid jar file");
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
                throw new PluginException("Invalid jar file");
            }

            // install
            if (location != null && isValid) {
                Bundle newBundle = installBundle(location);
                if (newBundle != null) {
                    startBundle(newBundle);
                }
            }

            return true;
        } catch (Exception ex) {
            LogUtil.error(PluginManager.class.getName(), ex, "");
            throw new PluginException("Unable to write plugin file", ex);
        }
    }

    /**
     * Uninstall/remove all plugin, without deleting the plugin file
     * @param name
     * @return
     */
    public void uninstallAll(boolean deleteFiles) {
        Collection<Plugin> pluginList = this.list();
        for (Plugin plugin : pluginList) {
            uninstall(plugin.getClass().getName(), deleteFiles);
        }
    }

    /**
     * Uninstall/remove a plugin, and delete the plugin file
     * @param name
     * @return
     */
    public boolean uninstall(String name) {
        return uninstall(name, true);
    }

    /**
     * Uninstall/remove a plugin
     * @param name
     * @return
     */
    public boolean uninstall(String name, boolean deleteFile) {
        boolean result = false;
        BundleContext context = felix.getBundleContext();
        ServiceReference sr = context.getServiceReference(name);
        if (sr != null) {
            try {
                Bundle bundle = sr.getBundle();
                bundle.stop();
                bundle.uninstall();
                String location = bundle.getLocation();
                context.ungetService(sr);

                // delete location
                if (deleteFile) {
                    File file = new File(new URI(location));
                    boolean deleted = file.delete();
                }
                result = true;

                // clear cache
                pluginCache.clear();
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
            }
        }
        return result;
    }

    /**
     * Returns a plugin, from either the OSGI container and the classpath.
     * Plugins from the OSGI container will take priority if there are conflicting classes.
     * @param name Class name of the required plugin
     * @return
     */
    public Plugin getPlugin(String name) {
        if (name != null && name.trim().length() > 0 && !"null".equalsIgnoreCase(name)) {
            Plugin plugin = loadOsgiPlugin(name);
            if (plugin == null) {
                plugin = loadClassPathPlugin(name);
            }
            return plugin;
        } else {
            return null;
        }
    }

    /**
     * Retrieve a plugin from the OSGI container
     * @param name Fully qualified class name for the required plugin
     * @return
     */
    protected Plugin loadOsgiPlugin(String name) {
        Plugin plugin = null;
        try {
            BundleContext context = felix.getBundleContext();

            ServiceReference sr = context.getServiceReference(name);
            if (sr != null) {
                Class clazz = sr.getBundle().loadClass(name);
                Object obj = clazz.newInstance();
                boolean isPlugin = obj instanceof Plugin;
                LogUtil.debug(PluginManager.class.getName(), " plugin obj " + obj + " class: " + obj.getClass().getName() + " " + isPlugin);
                LogUtil.debug(PluginManager.class.getName(), " plugin classloader: " + obj.getClass().getClassLoader());
                LogUtil.debug(PluginManager.class.getName(), " current classloader: " + Plugin.class.getClassLoader());
                if (isPlugin) {
                    plugin = (Plugin) obj;
                }
                context.ungetService(sr);
            }
        } catch (Exception ex) {
            LogUtil.error(PluginManager.class.getName(), ex, "");
            throw new PluginException("Plugin " + name + " could not be retrieved", ex);
        }
        return plugin;
    }

    /**
     * Retrieve a plugin using the system classloader
     * @param name Fully qualified class name for the required plugin
     * @return
     */
    protected Plugin loadClassPathPlugin(String name) {
        Plugin plugin = null;
        try {
            Class clazz = Class.forName(name);
            Object obj = clazz.newInstance();
            plugin = (Plugin) obj;
        } catch (Exception ex) {
            LogUtil.debug(PluginManager.class.getName(), "plugin " + name + " not found in classpath");
        }
        return plugin;
    }

    /**
     * Retrieves an InputStream to a resource from a plugin. The plugin may either be from OSGI container or system classpath.
     * @param pluginName
     * @param resourceUrl
     * @return
     * @throws IOException
     */
    public InputStream getPluginResource(String pluginName, String resourceUrl) throws IOException {
        InputStream result = null;

        URL url = getPluginResourceURL(pluginName, resourceUrl);
        if (url != null) {
            // get inputstream from url
            if (url != null) {
                result = url.openConnection().getInputStream();
            }
        }

        return result;
    }

    /**
     * Reads a resource from a plugin. java.util.Formatter text patterns supported.
     * @param pluginName
     * @param resourceUrl
     * @param arguments
     * @param removeNewLines
     * @param translationPath
     * @return null if the resource is not found or in the case of an exception
     * @see java.util.Formatter
     */
    public String readPluginResourceAsString(String pluginName, String resourceUrl, Object[] arguments, boolean removeNewLines, String translationPath) {
        String output = null;
        InputStream input = null;
        ByteArrayOutputStream stream = null;
        if (pluginName != null && resourceUrl != null) {
            try {
                input = getPluginResource(pluginName, resourceUrl);
                if (input != null) {
                    // write output
                    stream = new ByteArrayOutputStream();
                    byte[] bbuf = new byte[65536];
                    int length = 0;
                    while ((input != null) && ((length = input.read(bbuf)) != -1)) {
                        stream.write(bbuf, 0, length);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, "Error reading resource " + resourceUrl, e);
            } finally {
                try {
                    if (stream != null) {
                        stream.flush();
                        stream.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception e) {
                    Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, "Error closing IO", e);
                }
            }
        }
        // set and return output
        if (stream != null) {
            output = new String(stream.toByteArray());

            if (arguments != null && arguments.length > 0) {
                // format arguments
                output = String.format(output, arguments);
            }

            if (removeNewLines) {
                // compress by removing new lines
                output = output.replace('\n', ' ');
                output = output.replace('\r', ' ');
            }
        }

        output = processPluginTranslation(output, pluginName, translationPath);

        return output;
    }

    /**
     * Reads a message bundle from a plugin.
     * @param pluginName
     * @param translationPath
     * @return null if the resource bundle is not found or in the case of an exception
     */
    public ResourceBundle getPluginMessageBundle(String pluginName, String translationPath) {
        ResourceBundle bundle = null;

        // get plugin
        Plugin plugin = getPlugin(pluginName);
        if (plugin != null) {
            
            LocaleResolver localeResolver = (LocaleResolver) getBean("localeResolver");  
            Locale locale = localeResolver.resolveLocale(getHttpServletRequest());

            try {
                bundle = ResourceBundle.getBundle(translationPath, locale, plugin.getClass().getClassLoader());
            } catch (Exception e) {
                LogUtil.debug(PluginManager.class.getName(), translationPath + " translation file not found");
            }
        }
        return bundle;
    }

    public String processPluginTranslation(String content, String pluginName, String translationPath) {
        if (!(content != null && content.indexOf("@@") >= 0)) {
            return content;
        }

        Pattern pattern = Pattern.compile("\\@@([^@@^\"^ ])*\\.([^@@^\"])*\\@@");
        Matcher matcher = pattern.matcher(content);

        List<String> keyList = new ArrayList<String>();
        while (matcher.find()) {
            keyList.add(matcher.group());
        }

        if (!keyList.isEmpty()) {
            ResourceBundle bundle = null;
            
            if (translationPath != null && !translationPath.isEmpty()) {
                bundle = getPluginMessageBundle(pluginName, translationPath);
            }

            for (String key : keyList) {
                String tempKey = key.replaceAll("@@", "");
                String label = null;

                if (bundle != null && bundle.containsKey(tempKey)) {
                    label = bundle.getString(tempKey);
                } else if (ResourceBundleUtil.getMessage(tempKey) != null) {
                    label = ResourceBundleUtil.getMessage(tempKey);
                }

                if (label != null) {
                    content = content.replaceAll(StringUtil.escapeRegex(key), StringUtil.escapeRegex(label));
                }
            }
        }

        return content;
    }

    public String getMessage(String key, String pluginName, String translationPath) {
        return processPluginTranslation("@@" + key + "@@", pluginName, translationPath);
    }

    public String getPluginFreeMarkerTemplate(Map data, final String pluginName, final String templatePath, String translationPath) {
        String result = "";
        try {
            // init configuration
            Configuration configuration = new Configuration();

            configuration.setObjectWrapper(new DefaultObjectWrapper() {
                // override object wrapper for Maps to support list-ordered maps

                @Override
                public TemplateModel wrap(Object obj) throws TemplateModelException {
                    if (obj instanceof Map) {
                        return new ListOrderedHash((Map) obj, this);
                    } else {
                        return super.wrap(obj);
                    }
                }
            });

            // set template loader
            configuration.setTemplateLoader(new URLTemplateLoader() {

                @Override
                protected URL getURL(String string) {
                    URL url = getPluginResourceURL(pluginName, templatePath);
                    return url;
                }

                @Override
                public long getLastModified(Object templateSource) {
                    return 0;
                }
                
            });

            // Get or create a template
            Template temp = configuration.getTemplate(templatePath);

            // Merge data-model with template
            Writer out = new StringWriter();
            temp.process(data, out);
            out.flush();
            result = out.toString();

            result = processPluginTranslation(result, pluginName, translationPath);

        } catch (Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, null, ex);
            result = ex.toString();
        }
        return result;
    }

    /**
     * Retrieves a URL to a resource from a plugin. The plugin may either be from OSGI container or system classpath.
     * @param pluginName
     * @param pluginName
     * @param resourceUrl
     * @return
     */
    public URL getPluginResourceURL(String pluginName, String resourceUrl) {
        URL url = null;

        // get plugin
        Plugin plugin = getPlugin(pluginName);

        if (plugin != null) {
            // get class loader for plugin
            ClassLoader loader = plugin.getClass().getClassLoader();

            // get resource url, remove first /
            if (resourceUrl.startsWith("/")) {
                resourceUrl = resourceUrl.substring(1);
            }
            url = loader.getResource(resourceUrl);
        }

        return url;
    }

    /**
     * Execute a plugin
     * @param name The fully qualified class name of the plugin
     * @param properties
     * @return
     */
    public Object execute(String name, Map properties) {
        Object result = null;
        Plugin plugin = getPlugin(name);
        if (plugin != null) {
            result = plugin.execute(properties);
            LogUtil.info(PluginManager.class.getName(), " Executed plugin " + plugin + ": " + result);
        } else {
            LogUtil.info(PluginManager.class.getName(), " Plugin " + name + " not found");
        }
        return result;
    }

    /**
     * Stop the plugin manager
     */
    public synchronized void shutdown() {
        if (felix != null) {
            try {
                uninstallAll(false);
                felix.stop();
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "Could not stop Felix");
            }
            felix = null;
        }
    }

    @Override
    public void finalize() {
        shutdown();
    }

    public Object testPlugin(String name, String location, Map properties, boolean override) {
        LogUtil.info(PluginManager.class.getName(), "====testPlugin====");
        // check for existing plugin
        Plugin plugin = getPlugin(name);
        boolean existing = (plugin != null);
        boolean install = (location != null && location.trim().length() > 0);

        // install plugin
        if (install && (!existing || override)) {
            InputStream in = null;
            try {
                LogUtil.info(PluginManager.class.getName(), " ===install=== ");
                File file = new File(location);
                if (file.exists()) {
                    in = new FileInputStream(file);
                    upload(file.getName(), in);
                }
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    LogUtil.error(PluginManager.class.getName(), ex, "");
                }
            }
        }

        // execute plugin
        LogUtil.info(PluginManager.class.getName(), " ===execute=== ");
        Object result = execute(name, properties);
        LogUtil.info(PluginManager.class.getName(), "  result: " + result);

        // uninstall plugin
        if (install && (!existing || override)) {
            LogUtil.info(PluginManager.class.getName(), " ===uninstall=== ");
            uninstall(name);
        }
        LogUtil.info(PluginManager.class.getName(), "====testPlugin end====");

        return result;
    }

    public static void main(String[] args) {
//        String pluginDirectory = "target/wflow-bundles";
        PluginManager pm = new PluginManager();

        FileInputStream in = null;
        try {
            LogUtil.info(PluginManager.class.getName(), " ===Plugin List=== ");
            for (Plugin p : pm.list()) {
                LogUtil.info(PluginManager.class.getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            }
            String samplePluginFile = "../wflow-plugins/wflow-plugin-sample/target/wflow-plugin-sample-3.1-SNAPSHOT.jar";
            String samplePlugin = "org.joget.plugin.sample.SamplePlugin";

            try {
                LogUtil.info(PluginManager.class.getName(), " ===Install SamplePlugin=== ");
                File file = new File(samplePluginFile);
                in = new FileInputStream(file);
                pm.upload(file.getName(), in);
            } catch (Exception ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    LogUtil.error(PluginManager.class.getName(), ex, "");
                }
            }

            LogUtil.info(PluginManager.class.getName(), " ===Plugin List after install=== ");
            for (Plugin p : pm.list()) {
                LogUtil.info(PluginManager.class.getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            }

            LogUtil.info(PluginManager.class.getName(), " ===Execute SamplePlugin=== ");
            pm.execute(samplePlugin, null);

            LogUtil.info(PluginManager.class.getName(), " ===Uninstall SamplePlugin=== ");
            pm.uninstall(samplePlugin);
            LogUtil.info(PluginManager.class.getName(), " ===New Plugin List after removal=== ");
            for (Plugin p : pm.list()) {
                LogUtil.info(PluginManager.class.getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            }
            pm.refresh();
            LogUtil.info(PluginManager.class.getName(), " ===New Plugin List after refresh=== ");
            for (Plugin p : pm.list()) {
                LogUtil.info(PluginManager.class.getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            }

            pm.testPlugin(samplePlugin, samplePluginFile, null, true);
        } finally {
            pm.shutdown();
        }
    }
    
    public HttpServletRequest getHttpServletRequest() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            return request;
        } catch (NoClassDefFoundError e) {
            // ignore if servlet request class is not available
            return null;
        } catch (IllegalStateException e) {
            // ignore if servlet request is not available, e.g. when triggered from a deadline
            return null;
        }
    }

    public Object getBean(String beanName) {
        Object bean = null;
        if (applicationContext != null) {
            bean = applicationContext.getBean(beanName);
        }
        return bean;
    }

    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.applicationContext = appContext;
        refresh();
    }
}
