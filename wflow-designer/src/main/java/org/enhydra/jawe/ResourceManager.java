package org.enhydra.jawe;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Utility for implementing multi language support, and other manipulation with property
 * files.
 * 
 * @author Sasa Bojanic
 */
public class ResourceManager {

   public static final String RESOURCE_PATH = "org.enhydra.jawe.language.JaWE";

   protected static Locale defaultLocale;

   protected static ResourceBundle defaultResourceBoundle;

   protected static Locale choosenLocale;

   protected static ResourceBundle choosenResourceBundle;

   protected static AdditionalResourceManager addResMgr;

   static {
      reconfigure();
   }

   /**
    * Gets a language dependent string from the resource bundle.
    * <p>
    * Resource bundle represents the <i>property file</i>. For example, if property file
    * contains something like this:<BR>
    * <CENTER>menubar=file edit help</CENTER> method call
    * getLanguageDependentString("menubar") will give the string <i>file edit help</i> as
    * a result. <BR>
    * This method reads information from property file. If can't find desired resource,
    * returns <b>null</b>.
    * 
    * @param nm name of the resource to fetch.
    * @return String value of named resource.
    */
   public static String getLanguageDependentString(String nm) {
      String str;
      try {
         str = choosenResourceBundle.getString(nm);
      } catch (MissingResourceException mre) {
         try {
            str = defaultResourceBoundle.getString(nm);
         } catch (MissingResourceException mre1) {
            try {
               str = addResMgr.getLanguageDependentString(nm);
            } catch (Exception ex) {
               str = null;
            }
         }
      }
      return str;
   }

   public static String getLanguageDependentString(AdditionalResourceManager arm,
                                                   String nm) {
      String str = ResourceManager.getLanguageDependentString(nm);
      if (str == null && arm != null) {
         str = arm.getLanguageDependentString(nm);
      }
      return str;
   }

   public static String getResourceString(Properties properties, String nm) {
      String str = null;
      try {
         str = properties.getProperty(nm);
      } catch (Exception ex) {
      }
      return str;
   }

   public static List getResourceStrings(Properties properties, String startsWith) {
      return getResourceStrings(properties, startsWith, true);
   }

   public static List getResourceStrings(Properties properties,
                                         String startsWith,
                                         boolean values) {
      List rStrs = new ArrayList();
      int startIndex = startsWith.length();
      Iterator it = properties.entrySet().iterator();
      while (it.hasNext()) {
         Map.Entry me = (Map.Entry) it.next();
         if (((String) me.getKey()).startsWith(startsWith)) {
            if (values)
               rStrs.add(me.getValue());
            else
               rStrs.add(((String) me.getKey()).substring(startIndex));
         }
      }

      return rStrs;
   }

   /**
    * Gets the url from a resource string.
    * 
    * @param key the string key to the url in the properties.
    * @return the resource location.
    * @see java.lang.Class#getResource
    */
   public static URL getResource(Properties properties, String key) {
      String name = properties.getProperty(key);
      if (name != null) {
         URL url = ResourceManager.class.getClassLoader().getResource(name);
         return url;
      }
      return null;
   }

   public static void setDefault() {
      choosenResourceBundle = defaultResourceBoundle;
      choosenLocale = defaultLocale;
      // XMLUtil.setChoosenResources(choosenResourceBundle);
   }

   public static void setSystem() {
      choosenLocale = Locale.getDefault();
      choosenResourceBundle = ResourceBundle.getBundle(RESOURCE_PATH, choosenLocale);
      // XMLUtil.setChoosenResources(choosenResourceBundle);
   }

   /**
    * Returns the default resource bundle.
    */
   public static ResourceBundle getDefaultResourceBundle() {
      return defaultResourceBoundle;
   }

   /**
    * Returns the current locale.
    */
   public static ResourceBundle getChoosenResourceBundle() {
      return choosenResourceBundle;
   }

   /**
    * Returns the default locale.
    */
   public static Locale getDefaultLocale() {
      return defaultLocale;
   }

   /**
    * Returns the current locale.
    */
   public static Locale getChoosenLocale() {
      return choosenLocale;
   }

   /**
    * Sets the new resource and locale to be used.
    */
   public static void setChoosen(Locale loc) throws MissingResourceException {
      Locale previousLocale = choosenLocale;
      try {
         choosenLocale = loc;
         choosenResourceBundle = ResourceBundle.getBundle(RESOURCE_PATH, loc);
         // XMLUtil.setChoosenResources(choosenResourceBundle);
      } catch (Exception ex) {
         choosenLocale = previousLocale;
      }
   }

   public static void reconfigure() {
      try {
         // default is English
         defaultLocale = new Locale("en");
         defaultResourceBoundle = ResourceBundle.getBundle(RESOURCE_PATH, defaultLocale);
         // chose the default system settings at the start
         String startingLocale = JaWEManager.getInstance().getStartingLocale();
         if (startingLocale != null && startingLocale.length() > 0) {
            if (!startingLocale.equals("default")) {
               choosenLocale = new Locale(startingLocale);
            } else {
               choosenLocale = defaultLocale;
            }
         } else {
            choosenLocale = Locale.getDefault();
         }
         if (startingLocale != null && !startingLocale.equals("default")) {
            choosenResourceBundle = ResourceBundle.getBundle(RESOURCE_PATH, choosenLocale);
         } else {
            choosenResourceBundle = defaultResourceBoundle;
         }
         Properties properties = new Properties();
         properties.put("AdditionalLanguagePropertyFile.1",
                        JaWEConstants.JAWE_LANGUAGE_MISC_PROPERTYFILE_NAME);
         addResMgr = new AdditionalResourceManager(properties);

      } catch (MissingResourceException mre) {
         System.err.println(RESOURCE_PATH + ".properties not found");
         System.exit(1);
      }

      // XMLUtil.setDefaultResources(defaultResourceBoundle);
      // XMLUtil.setChoosenResources(choosenResourceBundle);
   }

}