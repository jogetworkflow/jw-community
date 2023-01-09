/**
 * Licensed under the Artistic License; you may not use this file
 * except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://displaytag.sourceforge.net/license.html
 *
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.displaytag.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.text.Collator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.Messages;
import org.displaytag.model.DefaultComparator;
import org.displaytag.decorator.DecoratorFactory;
import org.displaytag.decorator.DefaultDecoratorFactory;
import org.displaytag.exception.FactoryInstantiationException;
import org.displaytag.exception.TablePropertiesLoadException;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;
import org.displaytag.util.DefaultRequestHelperFactory;
import org.displaytag.util.ReflectHelper;
import org.displaytag.util.RequestHelperFactory;
import org.joget.commons.util.ResourceBundleUtil;


/**
 * The properties used by the Table tags. The properties are loaded in the following order, in increasing order of
 * priority. The locale of getInstance() is used to determine the locale of the property file to use; if the key
 * required does not exist in the specified file, the key will be loaded from a more general property file.
 * <ol>
 * <li>First, from the TableTag.properties included with the DisplayTag distribution.</li>
 * <li>Then, from the file displaytag.properties, if it is present; these properties are intended to be set by the user
 * for sitewide application. Messages are gathered according to the Locale of the property file.</li>
 * <li>Finally, if this class has a userProperties defined, all of the properties from that Properties object are copied
 * in as well.</li>
 * </ol>
 * @author Fabrizio Giustina
 * @author rapruitt
 * @version $Revision: 1166 $ ($Author: rapruitt $)
 */
public final class TableProperties implements Cloneable
{

    /**
     * name of the default properties file name ("displaytag.properties").
     */
    public static final String DEFAULT_FILENAME = "displaytag.properties"; //$NON-NLS-1$

    /**
     * The name of the local properties file that is searched for on the classpath. Settings in this file will override
     * the defaults loaded from TableTag.properties.
     */
    public static final String LOCAL_PROPERTIES = "displaytag"; //$NON-NLS-1$

    /**
     * property <code>export.banner</code>.
     */
    public static final String PROPERTY_STRING_EXPORTBANNER = "export.banner"; //$NON-NLS-1$

    /**
     * property <code>export.banner.sepchar</code>.
     */
    public static final String PROPERTY_STRING_EXPORTBANNER_SEPARATOR = "export.banner.sepchar"; //$NON-NLS-1$

    /**
     * property <code>export.decorated</code>.
     */
    public static final String PROPERTY_BOOLEAN_EXPORTDECORATED = "export.decorated"; //$NON-NLS-1$

    /**
     * property <code>export.amount</code>.
     */
    public static final String PROPERTY_STRING_EXPORTAMOUNT = "export.amount"; //$NON-NLS-1$

    /**
     * property <code>sort.amount</code>.
     */
    public static final String PROPERTY_STRING_SORTAMOUNT = "sort.amount"; //$NON-NLS-1$

    /**
     * property <code>basic.show.header</code>.
     */
    public static final String PROPERTY_BOOLEAN_SHOWHEADER = "basic.show.header"; //$NON-NLS-1$

    /**
     * property <code>basic.msg.empty_list</code>.
     */
    public static final String PROPERTY_STRING_EMPTYLIST_MESSAGE = "basic.msg.empty_list"; //$NON-NLS-1$

    /**
     * property <code>basic.msg.empty_list_row</code>.
     */
    public static final String PROPERTY_STRING_EMPTYLISTROW_MESSAGE = "basic.msg.empty_list_row"; //$NON-NLS-1$

    /**
     * property <code>basic.empty.showtable</code>.
     */
    public static final String PROPERTY_BOOLEAN_EMPTYLIST_SHOWTABLE = "basic.empty.showtable"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.placement</code>.
     */
    public static final String PROPERTY_STRING_BANNER_PLACEMENT = "paging.banner.placement"; //$NON-NLS-1$

    /**
     * property <code>error.msg.invalid_page</code>.
     */
    public static final String PROPERTY_STRING_PAGING_INVALIDPAGE = "error.msg.invalid_page"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.item_name</code>.
     */
    public static final String PROPERTY_STRING_PAGING_ITEM_NAME = "paging.banner.item_name"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.items_name</code>.
     */
    public static final String PROPERTY_STRING_PAGING_ITEMS_NAME = "paging.banner.items_name"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.no_items_found</code>.
     */
    public static final String PROPERTY_STRING_PAGING_NOITEMS = "paging.banner.no_items_found"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.one_item_found</code>.
     */
    public static final String PROPERTY_STRING_PAGING_FOUND_ONEITEM = "paging.banner.one_item_found"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.all_items_found</code>.
     */
    public static final String PROPERTY_STRING_PAGING_FOUND_ALLITEMS = "paging.banner.all_items_found"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.some_items_found</code>.
     */
    public static final String PROPERTY_STRING_PAGING_FOUND_SOMEITEMS = "paging.banner.some_items_found"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.group_size</code>.
     */
    public static final String PROPERTY_INT_PAGING_GROUPSIZE = "paging.banner.group_size"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.onepage</code>.
     */
    public static final String PROPERTY_STRING_PAGING_BANNER_ONEPAGE = "paging.banner.onepage"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.first</code>.
     */
    public static final String PROPERTY_STRING_PAGING_BANNER_FIRST = "paging.banner.first"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.last</code>.
     */
    public static final String PROPERTY_STRING_PAGING_BANNER_LAST = "paging.banner.last"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.full</code>.
     */
    public static final String PROPERTY_STRING_PAGING_BANNER_FULL = "paging.banner.full"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.page.link</code>.
     */
    public static final String PROPERTY_STRING_PAGING_PAGE_LINK = "paging.banner.page.link"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.page.selected</code>.
     */
    public static final String PROPERTY_STRING_PAGING_PAGE_SELECTED = "paging.banner.page.selected"; //$NON-NLS-1$

    /**
     * property <code>paging.banner.page.separator</code>.
     */
    public static final String PROPERTY_STRING_PAGING_PAGE_SPARATOR = "paging.banner.page.separator"; //$NON-NLS-1$

    /**
     * property <code>factory.requestHelper</code>.
     */
    public static final String PROPERTY_CLASS_REQUESTHELPERFACTORY = "factory.requestHelper"; //$NON-NLS-1$

    /**
     * property <code>factory.decorators</code>.
     */
    public static final String PROPERTY_CLASS_DECORATORFACTORY = "factory.decorator"; //$NON-NLS-1$

    /**
     * property <code>locale.provider</code>.
     */
    public static final String PROPERTY_CLASS_LOCALEPROVIDER = "locale.provider"; //$NON-NLS-1$

    /**
     * property <code>locale.resolver</code>.
     */
    public static final String PROPERTY_CLASS_LOCALERESOLVER = "locale.resolver"; //$NON-NLS-1$

    /**
     * property <code>css.tr.even</code>: holds the name of the css class for even rows. Defaults to <code>even</code>.
     */
    public static final String PROPERTY_CSS_TR_EVEN = "css.tr.even"; //$NON-NLS-1$

    /**
     * property <code>css.tr.odd</code>: holds the name of the css class for odd rows. Defaults to <code>odd</code>.
     */
    public static final String PROPERTY_CSS_TR_ODD = "css.tr.odd"; //$NON-NLS-1$

    /**
     * property <code>css.table</code>: holds the name of the css class added to the main table tag. By default no css
     * class is added.
     */
    public static final String PROPERTY_CSS_TABLE = "css.table"; //$NON-NLS-1$

    /**
     * property <code>css.th.sortable</code>: holds the name of the css class added to the the header of a sortable
     * column. By default no css class is added.
     */
    public static final String PROPERTY_CSS_TH_SORTABLE = "css.th.sortable"; //$NON-NLS-1$

    /**
     * property <code>css.th.sorted</code>: holds the name of the css class added to the the header of a sorted column.
     * Defaults to <code>sorted</code>.
     */
    public static final String PROPERTY_CSS_TH_SORTED = "css.th.sorted"; //$NON-NLS-1$

    /**
     * property <code>css.th.ascending</code>: holds the name of the css class added to the the header of a column
     * sorted in ascending order. Defaults to <code>order1</code>.
     */
    public static final String PROPERTY_CSS_TH_SORTED_ASCENDING = "css.th.ascending"; //$NON-NLS-1$

    /**
     * property <code>css.th.descending</code>: holds the name of the css class added to the the header of a column
     * sorted in descending order. Defaults to <code>order2</code>.
     */
    public static final String PROPERTY_CSS_TH_SORTED_DESCENDING = "css.th.descending"; //$NON-NLS-1$

    /**
     * prefix used for all the properties related to export ("export"). The full property name is <code>export.</code>
     * <em>[export type]</em><code>.</code><em>[property name]</em>
     */
    public static final String PROPERTY_EXPORT_PREFIX = "export"; //$NON-NLS-1$

    /**
     * prefix used to set the media decorator property name. The full property name is <code>decorator.media.</code>
     * <em>[export type]</em>.
     */
    public static final String PROPERTY_DECORATOR_SUFFIX = "decorator"; //$NON-NLS-1$

    /**
     * used to set the media decorator property name. The full property name is <code>decorator.media.</code>
     * <em>[export type]</em>
     */
    public static final String PROPERTY_DECORATOR_MEDIA = "media"; //$NON-NLS-1$

    /**
     * used to set the totaler property name. The property name is <code>totaler</code>
     */
    public static final String TOTALER_NAME = "totaler"; //$NON-NLS-1$

    /**
     * property <code>export.types</code>: holds the list of export available export types.
     */
    public static final String PROPERTY_EXPORTTYPES = "export.types"; //$NON-NLS-1$

    /**
     * export property <code>label</code>.
     */
    public static final String EXPORTPROPERTY_STRING_LABEL = "label"; //$NON-NLS-1$

    /**
     * export property <code>class</code>.
     */
    public static final String EXPORTPROPERTY_STRING_CLASS = "class"; //$NON-NLS-1$

    /**
     * export property <code>include_header</code>.
     */
    public static final String EXPORTPROPERTY_BOOLEAN_EXPORTHEADER = "include_header"; //$NON-NLS-1$

    /**
     * export property <code>filename</code>.
     */
    public static final String EXPORTPROPERTY_STRING_FILENAME = "filename"; //$NON-NLS-1$

    /**
     * Property <code>pagination.sort.param</code>. If external pagination and sorting is used, it holds the name of the
     * parameter used to hold the sort criterion in generated links
     */
    public static final String PROPERTY_STRING_PAGINATION_SORT_PARAM = "pagination.sort.param"; //$NON-NLS-1$

    /**
     * Property <code>pagination.sortdirection.param</code>. If external pagination and sorting is used, it holds the
     * name of the parameter used to hold the sort direction in generated links (asc or desc)
     */
    public static final String PROPERTY_STRING_PAGINATION_SORT_DIRECTION_PARAM = "pagination.sortdirection.param"; //$NON-NLS-1$

    /**
     * Property <code>pagination.pagenumber.param</code>. If external pagination and sorting is used, it holds the name
     * of the parameter used to hold the page number in generated links
     */
    public static final String PROPERTY_STRING_PAGINATION_PAGE_NUMBER_PARAM = "pagination.pagenumber.param"; //$NON-NLS-1$

    /**
     * Property <code>pagination.searchid.param</code>. If external pagination and sorting is used, it holds the name of
     * the parameter used to hold the search ID in generated links
     */
    public static final String PROPERTY_STRING_PAGINATION_SEARCH_ID_PARAM = "pagination.searchid.param"; //$NON-NLS-1$

    /**
     * Property <code>pagination.sort.asc.value</code>. If external pagination and sorting is used, it holds the value
     * of the parameter of the sort direction parameter for "ascending"
     */
    public static final String PROPERTY_STRING_PAGINATION_ASC_VALUE = "pagination.sort.asc.value"; //$NON-NLS-1$

    /**
     * Property <code>pagination.sort.desc.value</code>. If external pagination and sorting is used, it holds the value
     * of the parameter of the sort direction parameter for "descending"
     */
    public static final String PROPERTY_STRING_PAGINATION_DESC_VALUE = "pagination.sort.desc.value"; //$NON-NLS-1$

    /**
     * Property <code>pagination.sort.skippagenumber</code>. If external pagination and sorting is used, it determines
     * if the current page number must be added in sort links or not. If this property is true, it means that each click
     * on a generated sort link will re-sort the list, and go back to the default page number. If it is false, each
     * click on a generated sort link will re-sort the list, and ask the current page number.
     */
    public static final String PROPERTY_BOOLEAN_PAGINATION_SKIP_PAGE_NUMBER_IN_SORT = "pagination.sort.skippagenumber"; //$NON-NLS-1$

    /**
     * Property <code>comparator.default</code>.  If present, will use use as the classname of the default comparator.
     * Will be overriden by column level comparators.
     */
    public static final String PROPERTY_DEFAULT_COMPARATOR = "comparator.default"; //$NON-NLS-1$

    // </JBN>

    /**
     * Separator char used in property names.
     */
    private static final char SEP = '.';

    /**
     * logger.
     */
    private static Log log = LogFactory.getLog(TableProperties.class);

    /**
     * The userProperties are local, non-default properties; these settings override the defaults from
     * displaytag.properties and TableTag.properties.
     */
    private static Properties userProperties = new Properties();

    /**
     * Configured resource provider. If no ResourceProvider is configured, an no-op one is used. This instance is
     * initialized at first use and shared.
     */
    private static I18nResourceProvider resourceProvider;

    /**
     * Configured locale resolver.
     */
    private static LocaleResolver localeResolver;

    /**
     * TableProperties for each locale are loaded as needed, and cloned for public usage.
     */
    private static Map<Locale, TableProperties> prototypes = new HashMap<Locale, TableProperties>();

    /**
     * Loaded properties (defaults from defaultProperties + custom from bundle).
     */
    private Properties properties;

    /**
     * The locale for these properties.
     */
    private Locale locale;

    /**
     * Cache for dinamically instantiated object (request factory, decorator factory).
     */
    private Map<String, Object> objectCache = new HashMap<String, Object>();

    /**
     * Setter for I18nResourceProvider. A resource provider is usually set using displaytag properties, this accessor is
     * needed for tests.
     * @param provider I18nResourceProvider instance
     */
    protected static void setResourceProvider(I18nResourceProvider provider)
    {
        resourceProvider = provider;
    }

    /**
     * Setter for LocaleResolver. A locale resolver is usually set using displaytag properties, this accessor is needed
     * for tests.
     * @param resolver LocaleResolver instance
     */
    protected static void setLocaleResolver(LocaleResolver resolver)
    {
        localeResolver = resolver;
    }

    /**
     * Loads default properties (TableTag.properties).
     * @return loaded properties
     * @throws TablePropertiesLoadException if default properties file can't be found
     */
    private static Properties loadBuiltInProperties() throws TablePropertiesLoadException
    {
        Properties defaultProperties = new Properties();
        InputStream is = null;
        try
        {
            is = TableProperties.class.getResourceAsStream(DEFAULT_FILENAME);
            if (is == null)
            {
                throw new TablePropertiesLoadException(TableProperties.class, DEFAULT_FILENAME, null);
            }
            defaultProperties.load(is);
        }
        catch (IOException e)
        {
            throw new TablePropertiesLoadException(TableProperties.class, DEFAULT_FILENAME, e);
        } 
        finally
        {
            if (is != null)
            {
                try 
                {
                    is.close();
                } 
                catch (IOException ex) 
                {
                    // ignore
                }
            }
        }    
            

        return defaultProperties;
    }

    /**
     * Loads user properties (displaytag.properties) according to the given locale. User properties are not guarantee to
     * exist, so the method can return <code>null</code> (no exception will be thrown).
     * @param locale requested Locale
     * @return loaded properties
     */
    private static ResourceBundle loadUserProperties(Locale locale)
    {
        ResourceBundle bundle = null;
        try
        {
            bundle = ResourceBundle.getBundle(LOCAL_PROPERTIES, locale);
        }
        catch (MissingResourceException e)
        {
            // if no resource bundle is found, try using the context classloader
            try
            {
                bundle = ResourceBundle.getBundle(LOCAL_PROPERTIES, locale, Thread
                    .currentThread()
                    .getContextClassLoader());
            }
            catch (MissingResourceException mre)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(Messages.getString("TableProperties.propertiesnotfound", //$NON-NLS-1$
                        new Object[]{mre.getMessage()}));
                }
            }
        }

        return bundle;
    }

    /**
     * Returns the configured Locale Resolver. This method is called before the loading of localized properties.
     * @return LocaleResolver instance.
     * @throws TablePropertiesLoadException if the default <code>TableTag.properties</code> file is not found.
     */
    public static LocaleResolver getLocaleResolverInstance() throws TablePropertiesLoadException
    {

        if (localeResolver == null)
        {

            // special handling, table properties is not yet instantiated
            String className = null;

            ResourceBundle defaultUserProperties = loadUserProperties(Locale.getDefault());

            // if available, user properties have higher precedence
            if (defaultUserProperties != null)
            {
                try
                {
                    className = defaultUserProperties.getString(PROPERTY_CLASS_LOCALERESOLVER);
                }
                catch (MissingResourceException e)
                {
                    // no problem
                }
            }

            // still null? load defaults
            if (className == null)
            {
                Properties defaults = loadBuiltInProperties();
                className = defaults.getProperty(PROPERTY_CLASS_LOCALERESOLVER);
            }

            if (className != null)
            {
                try
                {
                    Class<LocaleResolver> classProperty = (Class<LocaleResolver>) ReflectHelper.classForName(className);
                    localeResolver = classProperty.newInstance();

                    log.info(Messages.getString("TableProperties.classinitializedto", //$NON-NLS-1$
                        new Object[]{ClassUtils.getShortClassName(LocaleResolver.class), className}));
                }
                catch (Exception e)
                {
                    log.warn(Messages.getString("TableProperties.errorloading", //$NON-NLS-1$
                        new Object[]{
                            ClassUtils.getShortClassName(LocaleResolver.class),
                            e.getClass().getName(),
                            e.getMessage()}));
                }
            }
            else
            {
                log.info(Messages.getString("TableProperties.noconfigured", //$NON-NLS-1$
                    new Object[]{ClassUtils.getShortClassName(LocaleResolver.class)}));
            }

            // still null?
            if (localeResolver == null)
            {
                // fallback locale resolver
                localeResolver = new LocaleResolver()
                {

                    public Locale resolveLocale(HttpServletRequest request)
                    {
                        return request.getLocale();
                    }
                };
            }
        }

        return localeResolver;
    }

    /**
     * Initialize a new TableProperties loading the default properties file and the user defined one. There is no
     * caching used here, caching is assumed to occur in the getInstance factory method.
     * @param myLocale the locale we are in
     * @throws TablePropertiesLoadException for errors during loading of properties files
     */
    private TableProperties(Locale myLocale) throws TablePropertiesLoadException
    {
        this.locale = myLocale;
        // default properties will not change unless this class is reloaded
        Properties defaultProperties = loadBuiltInProperties();

        properties = new Properties(defaultProperties);
        addProperties(myLocale);

        // Now copy in the user properties (properties file set by calling setUserProperties()).
        // note setUserProperties() MUST BE CALLED before the first TableProperties instantation
        Enumeration<Object> keys = userProperties.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key != null)
            {
                properties.setProperty(key, (String) userProperties.get(key));
            }
        }
    }

    /**
     * Try to load the properties from the local properties file, displaytag.properties, and merge them into the
     * existing properties.
     * @param userLocale the locale from which the properties are to be loaded
     */
    private void addProperties(Locale userLocale)
    {
        ResourceBundle bundle = loadUserProperties(userLocale);

        if (bundle != null)
        {
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
                String key = keys.nextElement();
                properties.setProperty(key, bundle.getString(key));
            }
        }
    }

    /**
     * Clones the properties as well.
     * @return a new clone of oneself
     */
    protected Object clone()
    {
        TableProperties twin;
        try
        {
            twin = (TableProperties) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // should never happen
            throw new UnhandledException(e);
        }
        twin.properties = (Properties) this.properties.clone();
        return twin;
    }

    /**
     * Returns a new TableProperties instance for the given locale.
     * @param request HttpServletRequest needed to extract the locale to use. If null the default locale will be used.
     * @return TableProperties instance
     */
    public static TableProperties getInstance(HttpServletRequest request)
    {
        Locale locale;
        if (request != null)
        {
            locale = getLocaleResolverInstance().resolveLocale(request);
        }
        else
        {
            // for some configuration parameters locale doesn't matter
            locale = Locale.getDefault();
        }

        TableProperties props = prototypes.get(locale);
        if (props == null)
        {
            TableProperties lprops = new TableProperties(locale);
            prototypes.put(locale, lprops);
            props = lprops;
        }
        return (TableProperties) props.clone();
    }

    /**
     * Unload all cached properties. This will not clear properties set by by setUserProperties; you must clear those
     * manually.
     */
    public static void clearProperties()
    {
        prototypes.clear();
    }

    /**
     * Local, non-default properties; these settings override the defaults from displaytag.properties and
     * TableTag.properties. Please note that the values are copied in, so that multiple calls with non-overlapping
     * properties will be merged, not overwritten. Note: setUserProperties() MUST BE CALLED before the first
     * TableProperties instantation.
     * @param overrideProperties - The local, non-default properties
     */
    public static void setUserProperties(Properties overrideProperties)
    {
        // copy keys here, so that this can be invoked more than once from different sources.
        // if default properties are not yet loaded they will be copied in constructor
        Enumeration<Object> keys = overrideProperties.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key != null)
            {
                userProperties.setProperty(key, (String) overrideProperties.get(key));
            }
        }
    }

    /**
     * The locale for which these properties are intended.
     * @return the locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_INVALIDPAGE</code> property.
     * @return String
     */
    public String getPagingInvalidPage()
    {
        return getProperty(PROPERTY_STRING_PAGING_INVALIDPAGE);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_ITEM_NAME</code> property.
     * @return String
     */
    public String getPagingItemName()
    {
        return getProperty(PROPERTY_STRING_PAGING_ITEM_NAME);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_ITEMS_NAME</code> property.
     * @return String
     */
    public String getPagingItemsName()
    {
        return getProperty(PROPERTY_STRING_PAGING_ITEMS_NAME);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_NOITEMS</code> property.
     * @return String
     */
    public String getPagingFoundNoItems()
    {
        return getProperty(PROPERTY_STRING_PAGING_NOITEMS);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_FOUND_ONEITEM</code> property.
     * @return String
     */
    public String getPagingFoundOneItem()
    {
        return getProperty(PROPERTY_STRING_PAGING_FOUND_ONEITEM);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_FOUND_ALLITEMS</code> property.
     * @return String
     */
    public String getPagingFoundAllItems()
    {
        return getProperty(PROPERTY_STRING_PAGING_FOUND_ALLITEMS);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_FOUND_SOMEITEMS</code> property.
     * @return String
     */
    public String getPagingFoundSomeItems()
    {
        return getProperty(PROPERTY_STRING_PAGING_FOUND_SOMEITEMS);
    }

    /**
     * Getter for the <code>PROPERTY_INT_PAGING_GROUPSIZE</code> property.
     * @return int
     */
    public int getPagingGroupSize()
    {
        // default size is 8
        return getIntProperty(PROPERTY_INT_PAGING_GROUPSIZE, 8);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_BANNER_ONEPAGE</code> property.
     * @return String
     */
    public String getPagingBannerOnePage()
    {
        return getProperty(PROPERTY_STRING_PAGING_BANNER_ONEPAGE);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_BANNER_FIRST</code> property.
     * @return String
     */
    public String getPagingBannerFirst()
    {
        return getProperty(PROPERTY_STRING_PAGING_BANNER_FIRST);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_BANNER_LAST</code> property.
     * @return String
     */
    public String getPagingBannerLast()
    {
        return getProperty(PROPERTY_STRING_PAGING_BANNER_LAST);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_BANNER_FULL</code> property.
     * @return String
     */
    public String getPagingBannerFull()
    {
        return getProperty(PROPERTY_STRING_PAGING_BANNER_FULL);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_PAGE_LINK</code> property.
     * @return String
     */
    public String getPagingPageLink()
    {
        return getProperty(PROPERTY_STRING_PAGING_PAGE_LINK);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_PAGE_SELECTED</code> property.
     * @return String
     */
    public String getPagingPageSelected()
    {
        return getProperty(PROPERTY_STRING_PAGING_PAGE_SELECTED);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_PAGING_PAGE_SPARATOR</code> property.
     * @return String
     */
    public String getPagingPageSeparator()
    {
        return getProperty(PROPERTY_STRING_PAGING_PAGE_SPARATOR);
    }

    /**
     * Is the given export option enabled?
     * @param exportType instance of MediaTypeEnum
     * @return boolean true if export is enabled
     */
    public boolean getAddExport(MediaTypeEnum exportType)
    {
        return getBooleanProperty(PROPERTY_EXPORT_PREFIX + SEP + exportType.getName());
    }

    /**
     * Should headers be included in given export type?
     * @param exportType instance of MediaTypeEnum
     * @return boolean true if export should include headers
     */
    public boolean getExportHeader(MediaTypeEnum exportType)
    {
        return getBooleanProperty(PROPERTY_EXPORT_PREFIX
            + SEP
            + exportType.getName()
            + SEP
            + EXPORTPROPERTY_BOOLEAN_EXPORTHEADER);
    }

    /**
     * Returns the label for the given export option.
     * @param exportType instance of MediaTypeEnum
     * @return String label
     */
    public String getExportLabel(MediaTypeEnum exportType)
    {
        return getProperty(PROPERTY_EXPORT_PREFIX + SEP + exportType.getName() + SEP + EXPORTPROPERTY_STRING_LABEL);
    }

    /**
     * Returns the file name for the given media. Can be null
     * @param exportType instance of MediaTypeEnum
     * @return String filename
     */
    public String getExportFileName(MediaTypeEnum exportType)
    {
        return getProperty(PROPERTY_EXPORT_PREFIX + SEP + exportType.getName() + SEP + EXPORTPROPERTY_STRING_FILENAME);
    }

    /**
     * Getter for the <code>PROPERTY_BOOLEAN_EXPORTDECORATED</code> property.
     * @return boolean <code>true</code> if decorators should be used in exporting
     */
    public boolean getExportDecorated()
    {
        return getBooleanProperty(PROPERTY_BOOLEAN_EXPORTDECORATED);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_EXPORTBANNER</code> property.
     * @return String
     */
    public String getExportBanner()
    {
        return getProperty(PROPERTY_STRING_EXPORTBANNER);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_EXPORTBANNER_SEPARATOR</code> property.
     * @return String
     */
    public String getExportBannerSeparator()
    {
        return getProperty(PROPERTY_STRING_EXPORTBANNER_SEPARATOR);
    }

    /**
     * Getter for the <code>PROPERTY_BOOLEAN_SHOWHEADER</code> property.
     * @return boolean
     */
    public boolean getShowHeader()
    {
        return getBooleanProperty(PROPERTY_BOOLEAN_SHOWHEADER);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_EMPTYLIST_MESSAGE</code> property.
     * @return String
     */
    public String getEmptyListMessage()
    {
        return getProperty(PROPERTY_STRING_EMPTYLIST_MESSAGE);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_EMPTYLISTROW_MESSAGE</code> property.
     * @return String
     */
    public String getEmptyListRowMessage()
    {
        return getProperty(PROPERTY_STRING_EMPTYLISTROW_MESSAGE);
    }

    /**
     * Getter for the <code>PROPERTY_BOOLEAN_EMPTYLIST_SHOWTABLE</code> property.
     * @return boolean <code>true</code> if table should be displayed also if no items are found
     */
    public boolean getEmptyListShowTable()
    {
        return getBooleanProperty(PROPERTY_BOOLEAN_EMPTYLIST_SHOWTABLE);
    }

    /**
     * Getter for the <code>PROPERTY_STRING_EXPORTAMOUNT</code> property.
     * @return boolean <code>true</code> if <code>export.amount</code> is <code>list</code>
     */
    public boolean getExportFullList()
    {
        return "list".equals(getProperty(PROPERTY_STRING_EXPORTAMOUNT)); //$NON-NLS-1$
    }

    /**
     * Getter for the <code>PROPERTY_STRING_SORTAMOUNT</code> property.
     * @return boolean <code>true</code> if <code>sort.amount</code> is <code>list</code>
     */
    public boolean getSortFullList()
    {
        return "list".equals(getProperty(PROPERTY_STRING_SORTAMOUNT)); //$NON-NLS-1$
    }

    /**
     * Should paging banner be added before the table?
     * @return boolean
     */
    public boolean getAddPagingBannerTop()
    {
        String placement = getProperty(PROPERTY_STRING_BANNER_PLACEMENT);
        return "top".equals(placement) || "both".equals(placement); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Should paging banner be added after the table?
     * @return boolean
     */
    public boolean getAddPagingBannerBottom()
    {
        String placement = getProperty(PROPERTY_STRING_BANNER_PLACEMENT);
        return "bottom".equals(placement) || "both".equals(placement); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the appropriate css class for a table row.
     * @param rowNumber row number
     * @return the value of <code>PROPERTY_CSS_TR_EVEN</code> if rowNumber is even or <code>PROPERTY_CSS_TR_ODD</code>
     * if rowNumber is odd.
     */
    public String getCssRow(int rowNumber)
    {
        return getProperty((rowNumber % 2 == 0) ? PROPERTY_CSS_TR_ODD : PROPERTY_CSS_TR_EVEN);
    }

    /**
     * Returns the appropriate css class for a sorted column header.
     * @param ascending <code>true</code> if column is sorded in ascending order.
     * @return the value of <code>PROPERTY_CSS_TH_SORTED_ASCENDING</code> if column is sorded in ascending order or
     * <code>PROPERTY_CSS_TH_SORTED_DESCENDING</code> if column is sorded in descending order.
     */
    public String getCssOrder(boolean ascending)
    {
        return getProperty(ascending ? PROPERTY_CSS_TH_SORTED_ASCENDING : PROPERTY_CSS_TH_SORTED_DESCENDING);
    }

    /**
     * Returns the configured css class for a sorted column header.
     * @return the value of <code>PROPERTY_CSS_TH_SORTED</code>
     */
    public String getCssSorted()
    {
        return getProperty(PROPERTY_CSS_TH_SORTED);
    }

    /**
     * Returns the configured css class for the main table tag.
     * @return the value of <code>PROPERTY_CSS_TABLE</code>
     */
    public String getCssTable()
    {
        return getProperty(PROPERTY_CSS_TABLE);
    }

    /**
     * Returns the configured css class for a sortable column header.
     * @return the value of <code>PROPERTY_CSS_TH_SORTABLE</code>
     */
    public String getCssSortable()
    {
        return getProperty(PROPERTY_CSS_TH_SORTABLE);
    }

    /**
     * Returns the configured list of media.
     * @return the value of <code>PROPERTY_EXPORTTYPES</code>
     */
    public String[] getExportTypes()
    {
        String list = getProperty(PROPERTY_EXPORTTYPES);
        if (list == null)
        {
            return new String[0];
        }

        return StringUtils.split(list);
    }

    /**
     * Returns the class responsible for the given export.
     * @param exportName export name
     * @return String classname
     */
    public String getExportClass(String exportName)
    {
        return getProperty(PROPERTY_EXPORT_PREFIX + SEP + exportName + SEP + EXPORTPROPERTY_STRING_CLASS);
    }

    /**
     * Returns an instance of configured requestHelperFactory.
     * @return RequestHelperFactory instance.
     * @throws FactoryInstantiationException if unable to load or instantiate the configurated class.
     */
    public RequestHelperFactory getRequestHelperFactoryInstance() throws FactoryInstantiationException
    {
        Object loadedObject = getClassPropertyInstance(PROPERTY_CLASS_REQUESTHELPERFACTORY);

        // should not be null, but avoid errors just in case... see DISPL-148
        if (loadedObject == null)
        {
            return new DefaultRequestHelperFactory();
        }

        try
        {
            return (RequestHelperFactory) loadedObject;
        }
        catch (ClassCastException e)
        {
            throw new FactoryInstantiationException(getClass(), PROPERTY_CLASS_REQUESTHELPERFACTORY, loadedObject
                .getClass()
                .getName(), e);
        }
    }

    /**
     * Returns an instance of configured DecoratorFactory.
     * @return DecoratorFactory instance.
     * @throws FactoryInstantiationException if unable to load or instantiate the configurated class.
     */
    public DecoratorFactory getDecoratorFactoryInstance() throws FactoryInstantiationException
    {
        Object loadedObject = getClassPropertyInstance(PROPERTY_CLASS_DECORATORFACTORY);

        if (loadedObject == null)
        {
            return new DefaultDecoratorFactory();
        }

        try
        {
            return (DecoratorFactory) loadedObject;
        }
        catch (ClassCastException e)
        {
            throw new FactoryInstantiationException(getClass(), PROPERTY_CLASS_DECORATORFACTORY, loadedObject
                .getClass()
                .getName(), e);
        }
    }

    public String getPaginationSortParam()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_SORT_PARAM);
        if (result == null)
        {
            result = "sort";
        }
        return result;
    }

    public String getPaginationPageNumberParam()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_PAGE_NUMBER_PARAM);
        if (result == null)
        {
            result = "page";
        }
        return result;
    }

    public String getPaginationSortDirectionParam()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_SORT_DIRECTION_PARAM);
        if (result == null)
        {
            result = "dir";
        }
        return result;
    }

    public String getPaginationSearchIdParam()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_SEARCH_ID_PARAM);
        if (result == null)
        {
            result = "searchId";
        }
        return result;
    }

    public String getPaginationAscValue()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_ASC_VALUE);
        if (result == null)
        {
            result = "asc";
        }
        return result;
    }

    public String getPaginationDescValue()
    {
        String result = getProperty(PROPERTY_STRING_PAGINATION_DESC_VALUE);
        if (result == null)
        {
            result = "desc";
        }
        return result;
    }

    public boolean getPaginationSkipPageNumberInSort()
    {
        String s = getProperty(PROPERTY_BOOLEAN_PAGINATION_SKIP_PAGE_NUMBER_IN_SORT);
        if (s == null)
        {
            return true;
        }
        else
        {
            return getBooleanProperty(PROPERTY_BOOLEAN_PAGINATION_SKIP_PAGE_NUMBER_IN_SORT);
        }
    }

    // </JBN>

    /**
     * Returns the configured resource provider instance. If necessary instantiate the resource provider from config and
     * then keep a cached instance.
     * @return I18nResourceProvider instance.
     * @see I18nResourceProvider
     */
    public I18nResourceProvider geResourceProvider()
    {
        String className = getProperty(PROPERTY_CLASS_LOCALEPROVIDER);

        if (resourceProvider == null)
        {
            if (className != null)
            {
                try
                {
                    Class<I18nResourceProvider> classProperty = (Class<I18nResourceProvider>) ReflectHelper
                        .classForName(className);
                    resourceProvider = classProperty.newInstance();

                    log.info(Messages.getString("TableProperties.classinitializedto", //$NON-NLS-1$
                        new Object[]{ClassUtils.getShortClassName(I18nResourceProvider.class), className}));
                }
                catch (Exception e)
                {
                    log.warn(Messages.getString("TableProperties.errorloading", //$NON-NLS-1$
                        new Object[]{
                            ClassUtils.getShortClassName(I18nResourceProvider.class),
                            e.getClass().getName(),
                            e.getMessage()}));
                }
            }
            else
            {
                log.info(Messages.getString("TableProperties.noconfigured", //$NON-NLS-1$
                    new Object[]{ClassUtils.getShortClassName(I18nResourceProvider.class)}));
            }

            // still null?
            if (resourceProvider == null)
            {
                // fallback provider, no i18n
                resourceProvider = new I18nResourceProvider()
                {

                    // Always returns null
                    public String getResource(String titleKey, String property, Tag tag, PageContext context)
                    {
                        return null;
                    }
                };
            }
        }

        return resourceProvider;
    }

    /**
     * Reads a String property.
     * @param key property name
     * @return property value or <code>null</code> if property is not found
     */
    public String getProperty(String key)
    {
        String value = ResourceBundleUtil.getMessage(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return this.properties.getProperty(key);
    }

    /**
     * Sets a property.
     * @param key property name
     * @param value property value
     */
    public void setProperty(String key, String value)
    {
        this.properties.setProperty(key, value);
    }

    /**
     * Reads a boolean property.
     * @param key property name
     * @return boolean <code>true</code> if the property value is "true", <code>false</code> for any other value.
     */
    private boolean getBooleanProperty(String key)
    {
        return Boolean.TRUE.toString().equals(getProperty(key));
    }

    /**
     * Returns an instance of a configured Class. Returns a configured Class instantiated
     * callingClass.forName([configuration value]).
     * @param key configuration key
     * @return instance of configured class
     * @throws FactoryInstantiationException if unable to load or instantiate the configurated class.
     */
    private Object getClassPropertyInstance(String key) throws FactoryInstantiationException
    {
        Object instance = objectCache.get(key);
        if (instance != null)
        {
            return instance;
        }

        String className = getProperty(key);

        // shouldn't be null, but better check it
        if (className == null)
        {
            return null;
        }

        try
        {
            Class< ? > classProperty = ReflectHelper.classForName(className);
            instance = classProperty.newInstance();
            objectCache.put(key, instance);
            return instance;
        }
        catch (Exception e)
        {
            throw new FactoryInstantiationException(getClass(), key, className, e);
        }
    }

    /**
     * Reads an int property.
     * @param key property name
     * @param defaultValue default value returned if property is not found or not a valid int value
     * @return property value
     */
    private int getIntProperty(String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(getProperty(key));
        }
        catch (NumberFormatException e)
        {
            // Don't care, use default
            log.warn(Messages.getString("TableProperties.invalidvalue", //$NON-NLS-1$
                new Object[]{key, getProperty(key), new Integer(defaultValue)}));
        }

        return defaultValue;
    }

    /**
     * Obtain the name of the decorator configured for a given media type.
     * @param thatEnum A media type
     * @return The name of the decorator configured for a given media type.
     */
	public String getMediaTypeDecoratorName(MediaTypeEnum thatEnum)
	{
        return getProperty(PROPERTY_DECORATOR_SUFFIX + SEP + PROPERTY_DECORATOR_MEDIA + SEP + thatEnum);
	}

    /**
     * the classname of the totaler
     * @return                     the classname of the totaler
     */
    public String getTotalerName()
    {
        return getProperty(TOTALER_NAME);
    }

    public Comparator<Object> getDefaultComparator()
    {
        String className = getProperty(PROPERTY_DEFAULT_COMPARATOR);
        if (className != null)
        {
            try
            {
                Class<Comparator<Object>> classProperty = (Class<Comparator<Object>>) ReflectHelper
                    .classForName(className);
                return classProperty.newInstance();
            }
            catch (Exception e)
            {
                log.warn(Messages.getString("TableProperties.errorloading", //$NON-NLS-1$
                    new Object[]{
                        ClassUtils.getShortClassName(Comparator.class),
                        e.getClass().getName(),
                        e.getMessage()}));
            }
        }
        return new DefaultComparator(Collator.getInstance(getLocale()));
    }
}