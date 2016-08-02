package org.joget.workflow.shark;

import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowVariable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmodel.WfActivity;
import org.enhydra.shark.api.client.wfmodel.WfAssignment;
import org.enhydra.shark.api.client.wfmodel.WfProcessIterator;
import org.enhydra.shark.api.client.wfmodel.WfProcessMgr;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.MiscUtilities;
import org.enhydra.shark.utilities.WMEntityUtilities;

public class JSPClientUtilities {
   private static PackageFileFilter packageFileFilter = new PackageFileFilter();

   private static Properties p = new Properties();

   private static String EXTERNAL_PACKAGES_REPOSITORY = "repository/external";

   private static boolean _debug_ = false;

   private static boolean sharkConfigured = false;

   private static boolean ipc = false;

   private static String engineName = "SharkExampleJSP";

/*   private static WMConnectInfo wmconnInfo = new WMConnectInfo("admin",
                                                               "enhydra",
                                                               engineName,
                                                               "");
*/
   public static final String VARIABLE_TO_PROCESS_UPDATE = "VariableToProcess_UPDATE";

   public static final String VARIABLE_TO_PROCESS_VIEW = "VariableToProcess_VIEW";

   public static void initProperties(String realPath) throws Exception {
      if (_debug_){
         System.err.println("#_init_#");
      }
      
      if (!ipc) {
         ipc = true;
         InputStream in = null;
         InputStream in2 = null;
         try {
            realPath = replaceAll(realPath, "\\", "/");
            if (!realPath.endsWith("/")) {
               realPath = realPath + "/";
            }
            in = JSPClientUtilities.class.getResourceAsStream(realPath + "conf/Shark.conf");
            p.load(in);
            for (Iterator it = p.keySet().iterator(); it.hasNext();) {
               String key = (String) it.next();
               String value = p.getProperty(key);
               if (0 <= value.indexOf("@@")) {
                  if (_debug_){
                     System.err.print("key is " + key + ", old value is" + value);
                  }

                  value = replaceAll(value, "@@/", realPath);
                  p.setProperty(key, value);
                  if (_debug_){
                     System.err.println(", new value is" + value);
                  }
               }
            }
            in2 = JSPClientUtilities.class.getResourceAsStream(realPath + "conf/SharkJSPClient.conf");
            p.load(in2);
         } catch (Exception e) {
            LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         } finally {
             try {
                if (in != null) {
                    in.close();
                }
             } catch(IOException e) {                 
             }
             try {
                if (in2 != null) {
                    in2.close();
                }
             } catch(IOException e) {                 
             }
         }
         p.setProperty("enginename", engineName);
      }      
   }

   public static void setProperty(String property, String value) throws Exception {
       p.setProperty(property, value);
   }
   
   public static void init() throws Exception {
      if (!sharkConfigured) {
         Shark.configure(p);
         sharkConfigured = true;
      }
   }

   public static String packageLoad(SharkConnection sc, String xpdlName) throws Exception {
	return "";
/*	   if (_debug_){
         System.err.println("#_packageLoad_#");
      String realPath = EXTERNAL_PACKAGES_REPOSITORY + "/" + xpdlName;
      PackageAdministration pa = Shark.getInstance().getPackageAdministration();
      String pkgId = XMLUtil.getIdFromFile(realPath);
      if (!pa.isPackageOpened(sc.getSessionHandle(), pkgId)) {
         try {
            pa.openPackage(sc.getSessionHandle(), realPath);
         } catch (Exception e) {
            LogUtil.error(JSPClientUtilities.class.getName(), $1, "");
            throw e;
         }
      }
      return pkgId;
*/   }

   public static void processStart(SharkConnection sc, String mgrName) throws Exception {
      if (_debug_){
         System.err.println("#_processStartName_#");
      }
      
      try {
         if (!isProcessRunning(sc, mgrName)) {
            WfProcessMgr mgr = sc.getProcessMgr(mgrName);
            mgr.create_process(null).start();
         }
      } catch (Exception e) {
         LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         throw e;
      }
   }

   public static boolean isProcessRunning(SharkConnection sc, String mgrName)
      throws Exception {
      System.err.println("#_isProcessRunning_# (" + mgrName + ")");
      try {
         WfProcessMgr pMgr = sc.getProcessMgr(mgrName);
         WfProcessIterator pit = pMgr.get_iterator_process();
         pit.set_query_expression("state.equals(\""
                                  + SharkConstants.STATE_OPEN_RUNNING + "\")");
         if (_debug_) {
            System.err.println("#_" + pit.how_many() + "_#");
            System.err.println("#_" + pit.get_next_n_sequence(0).length + "_#");
         }
         return 0 < pit.get_next_n_sequence(0).length;
      } catch (Exception e) {
         LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         throw e;
      }
   }

   public static void activityComplete(SharkConnection sConn, String activityId)
      throws Exception {
      try {
         if (null != activityId) {
            try {
               WfAssignment a = getAssignment(sConn, activityId);
               if (!isMine(sConn, a)){
                  assignmentAccept(sConn, a);
               }
               a.activity().complete();
            } catch (Exception e) {
               throw e;
            }
         }
      } catch (Exception e) {
         LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         throw e;
      }
   }

   public static boolean isMine(SharkConnection sConn, String activityId)
      throws Exception {
      WfAssignment a = getAssignment(sConn, activityId);
      return isMine(sConn, a);
   }

   public static boolean isMine(SharkConnection sConn, WfAssignment a) throws Exception {
      return a.get_accepted_status();
   }

   public static void assignmentAccept(SharkConnection sConn, String activityId)
      throws Exception {
      assignmentAccept(sConn, getAssignment(sConn, activityId));
   }

   private static void assignmentAccept(SharkConnection sConn, WfAssignment a)
      throws Exception {
      a.set_accepted_status(true);
   }

   public static WfAssignment getAssignment(SharkConnection sConn, String activityId)
      throws Exception {
      try {
         WfAssignment[] ar = sConn.getResourceObject().get_sequence_work_item(0);
         for (int i = 0; i < ar.length; ++i) {
            if (activityId.equals(ar[i].activity().key())) {
               return ar[i];
            }
         }
         throw new Exception("Activity:"
                             + activityId + " not found in "
                             + sConn.getResourceObject().resource_key() + "'s worklist");
      } catch (Exception e) {
         if (_debug_){
            System.err.println("zvekseptsn");
         }
         LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         throw e;
      }
   }

   public static WfAssignment getAssignmentByProcess(SharkConnection sConn, String processId)
      throws Exception {
      try {
         WfAssignment[] ar = sConn.getResourceObject().get_sequence_work_item(0);
         for (int i = 0; i < ar.length; ++i) {
            if (processId.equals(ar[i].activity().container().key())) {
               return ar[i];
            }
         }
         throw new Exception("Activity for process:"
                             + processId + " not found in "
                             + sConn.getResourceObject().resource_key() + "'s worklist");
      } catch (Exception e) {
         LogUtil.error(JSPClientUtilities.class.getName(), e, "");
         throw e;
      }
   }

   public static void variableSet(SharkConnection sConn,
                                  String activityId,
                                  String vName,
                                  String vValue) throws Exception {
      WfAssignment a = getAssignment(sConn, activityId);
      if (!isMine(sConn, a)){
         throw new Exception("I don't own activity " + activityId);
      }
      
      Map _m = new HashMap();
      Object c = a.activity().process_context().get(vName);
      
      if (c instanceof Long) {
         c = new Long(vValue);
      } else if (c instanceof Boolean) {
         c = Boolean.valueOf(vValue);
      } else if (c instanceof Double) {
         c = Double.valueOf(vValue);
      } else {
         c = vValue;
      }
      
      _m.put(vName, c);
      a.activity().set_result(_m);
   }

   public static List getVariableData(SharkConnection sConn, WfActivity act)
      throws Exception {
      return getVariableData(sConn, act, true);
   }

   public static List getVariableData(SharkConnection sConn, WfActivity act, boolean onlyForUpdate)
      throws Exception {

      List ret = new ArrayList();

      Map _m = act.process_context();
      String[][] eas = getExtAttribNVPairs(sConn, act);

      if (eas != null) {
         for (int i = 0; i < eas.length; i++) {
            String eaName = eas[i][0];

            if (!onlyForUpdate || eaName.equalsIgnoreCase(VARIABLE_TO_PROCESS_UPDATE)) {
               String variableId = eas[i][1];
               if (_m.containsKey(variableId)) {
                  Object c = _m.get(variableId);
                  // null check for Oracle DB to prevent missing workflow variable
                  if (c == null) {
                      c = "";
                  }
                  if (c instanceof String || c instanceof Long || c instanceof Boolean || c instanceof Double) {
                     WorkflowVariable vd = new WorkflowVariable();
                     vd.setId(variableId);
                     vd.setToUpdate(eaName.equalsIgnoreCase(VARIABLE_TO_PROCESS_UPDATE));
                     vd.setVal(c);
                     vd.setJavaClass(c.getClass());

                     WMEntity varEnt = Shark.getInstance().getAdminMisc().getVariableDefinitionInfoByUniqueProcessDefinitionName(
                                            sConn.getSessionHandle(),
                                            act.container().manager().name(),variableId);
                     
                     String varName = variableId;
                     if (varEnt != null) {
                        varName = varEnt.getName();
                        if (varName == null || varName.equals("")) {
                           varName = variableId;
                        }
                     }
                     String varDesc = "";
                     if (varEnt != null) {
                        WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Description");
                        filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
                        varDesc = Shark.getInstance()
                           .getXPDLBrowser()
                           .listAttributes(sConn.getSessionHandle(),
                                           varEnt,
                                           filter,
                                           false)
                           .getArray()[0].getValue().toString();
                     }

                     vd.setName(varName);
                     vd.setDescription(varDesc);
                     ret.add(vd);
                  }
               }
            }
         }
      }

      return ret;

   }

   public static String[] xpdlsToLoad() throws Exception {
      List packageFiles = getDefinedPackageFiles(EXTERNAL_PACKAGES_REPOSITORY, true);
      Collection pfls = new ArrayList();
      Iterator pfi = packageFiles.iterator();
      Collections.sort(packageFiles);
      while (pfi.hasNext()) {
         File f = (File) pfi.next();
         String fileName;
         try {
            fileName = f.getCanonicalPath();
         } catch (Exception ex) {
            fileName = f.getAbsolutePath();
         }
         fileName = fileName.substring(EXTERNAL_PACKAGES_REPOSITORY.length() + 1);
         pfls.add(fileName);
      }
      String[] pfs = new String[pfls.size()];
      pfls.toArray(pfs);
      return pfs;
   }

   public static String[] processesToStart(SharkConnection sc) throws Exception {
      WfProcessMgr[] a = sc.get_iterator_processmgr().get_next_n_sequence(0);
      String[] ret = new String[a.length];
      for (int i = 0; i < a.length; ++i) {
         String n = a[i].name();
         if (_debug_){
            System.err.println("processName " + n);
         }
         
         ret[i] = n;
      }
      return ret;
   }

   /**
    * Replace all occurence of forReplace with replaceWith in input string.
    * 
    * @param input represents input string
    * @param forReplace represents substring for replace
    * @param replaceWith represents replaced string value
    * @return new string with replaced values
    */
   private static String replaceAll(String input, String forReplace, String replaceWith) {
      if (input == null){
         return null;
      }
      
      StringBuffer result = new StringBuffer();
      boolean hasMore = true;
      while (hasMore) {
         int start = input.indexOf(forReplace);
         int end = start + forReplace.length();
         if (start != -1) {
            result.append(input.substring(0, start) + replaceWith);
            input = input.substring(end);
         } else {
            hasMore = false;
            result.append(input);
         }
      }
      
      if (result.toString().equals("")){
         return input; // nothing is changed
      }else{
         return result.toString();
      }
   }

   static List getDefinedPackageFiles(String repository, boolean traverse) {
      File startingFolder = new File(repository);
      List packageFiles = new ArrayList();

      if (!startingFolder.exists()) {
         LogUtil.info(JSPClientUtilities.class.getName(), "Repository " + startingFolder + " doesn't exist");
      }
      
      if (traverse) {
         MiscUtilities.traverse(startingFolder, packageFiles, null);
      } else {
         packageFiles = Arrays.asList(startingFolder.listFiles(packageFileFilter));
      }

      return packageFiles;
   }

   public static void setPathToXPDLRepositoryFolder(String xpdlRepFolder)
      throws Exception {
      EXTERNAL_PACKAGES_REPOSITORY = xpdlRepFolder;
      System.err.println(xpdlRepFolder);
      File f = new File(xpdlRepFolder);
      System.err.println(f);

      if (!f.isAbsolute()) {
         System.err.println("isn't absolute");
         f = f.getAbsoluteFile();
      }
      
      System.err.println(f);

      if (!f.exists()) {
         throw new Exception("Folder " + xpdlRepFolder + " does not exist");
      }
      try {
         EXTERNAL_PACKAGES_REPOSITORY = f.getCanonicalPath();
      } catch (Exception ex) {
         EXTERNAL_PACKAGES_REPOSITORY = f.getAbsolutePath();
      }
   }

   // -1 -> do not show
   // 0 -> show for reading
   // 1 -> show for updating
   protected int variableType(String varId, String[][] eas) throws Exception {
      int type = -1;

      if (eas != null) {
         for (int i = 0; i < eas.length; i++) {
            String eaName = eas[i][0];
            String eaVal = eas[i][1];
            if (eaVal.equals(varId)) {
               if (eaName.equalsIgnoreCase(VARIABLE_TO_PROCESS_UPDATE)) {
                  type = 1;
                  break;
               } else if (eaName.equalsIgnoreCase(VARIABLE_TO_PROCESS_VIEW)) {
                  type = 0;
                  break;
               }
            }
         }
      }

      return type;
   }

   protected static String[][] getExtAttribNVPairs(SharkConnection sc, WfActivity act)
      throws Exception {
      XPDLBrowser xpdlb = Shark.getInstance().getXPDLBrowser();
      AdminMisc am = Shark.getInstance().getAdminMisc();
      WMEntity ent = am.getActivityDefinitionInfo(sc.getSessionHandle(), act.container()
         .key(), act.key());

      return WMEntityUtilities.getExtAttribNVPairs(sc.getSessionHandle(), xpdlb, ent);
   }

/*   public static UserTransaction getUserTransaction() throws Exception {
      String lookupName = p.getProperty("XaUserTransactionLookupName");
      InitialContext ic = new InitialContext();
      return (UserTransaction) ic.lookup(lookupName);
   }
*/
}

class PackageFileFilter implements FileFilter {
   public boolean accept(File pathname) {
      return !pathname.isDirectory();
   }
}
