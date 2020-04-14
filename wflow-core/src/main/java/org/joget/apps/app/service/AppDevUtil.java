package org.joget.apps.app.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteListCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.hibernate.proxy.HibernateProxy;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.dao.GitCommitHelper;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.form.dao.FormDataDaoImpl;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.UuidGenerator;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;

public class AppDevUtil {

    public static final String FILE_APP_PROPERTIES = "app.properties";
    public static final String PROPERTY_GIT_URI = "gitUri";
    public static final String PROPERTY_GIT_USERNAME = "gitUsername";
    public static final String PROPERTY_GIT_PASSWORD = "gitPassword";
    public static final String PROPERTY_GIT_CONFIG_PULL = "gitConfigPull";
    public static final String PROPERTY_GIT_CONFIG_EXCLUDE_COMMIT = "gitConfigExcludeCommit";
    public static final String ATTRIBUTE_GIT_PULL_REQUEST = "GIT_PULL_REQUEST";
    public static final String ATTRIBUTE_GIT_PUSH_REQUEST = "GIT_PUSH_REQUEST";
    public static final String ATTRIBUTE_GIT_COMMIT_REQUEST = "GIT_COMMIT_REQUEST";
    public static final String ATTRIBUTE_GIT_SYNC_APP = "GIT_SYNC_APP";
    public static final String PROPERTY_GIT_CONFIG_AUTO_SYNC = "gitConfigAutoSync";
    
    public static Map<String, Set<String>> workingPulls = new HashMap<String, Set<String>>();
    protected static Random random = new Random();
    
    public static String getAppDevBaseDirectory() {
        String dir = SetupManager.getBaseDirectory() + File.separator + "app_src";
        return dir;
    }
    
    public static Properties getAppDevProperties(AppDefinition appDef) {
        // load from FILE_APP_PROPERTIES
        Properties props = new Properties();
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = appDef.getAppId();
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        File file = new File(projectDir, FILE_APP_PROPERTIES);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            props.load(fis);
        } catch(IOException e) {
            LogUtil.debug(AppDevUtil.class.getName(), FILE_APP_PROPERTIES + " could not be loaded for " + appDef.getAppId());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch(IOException e) {
                    // ignore
                }
            }
        }
        String gitPassword = props.getProperty(PROPERTY_GIT_PASSWORD);
        if (gitPassword != null) {
            String decryptedPass = SecurityUtil.decrypt(gitPassword);
            props.setProperty(PROPERTY_GIT_PASSWORD, decryptedPass);
        }
        return props;
    }
    
    public static void setAppDevProperties(AppDefinition appDef, Properties props) throws IOException {
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = appDef.getAppId();
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        File file = new File(projectDir, FILE_APP_PROPERTIES);
        Properties currentProps = new Properties();
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                currentProps.load(fis);
            }
        }
        currentProps.putAll(props);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            currentProps.store(fos, "");
        }
    }    

    public static File dirSetup(String baseDir, String projectDirName) {
        // create project directory
        File projectDir = new File(baseDir, projectDirName);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
            LogUtil.debug(AppDevUtil.class.getName(), "Create app project directory");
        }
        return projectDir;
    }

    public static void gitClone(String gitUri, String gitUsername, String gitPassword) throws GitAPIException {
        // clone git project
        LogUtil.debug(AppDevUtil.class.getName(), "Clone Git repository: " + gitUri);
        Git.cloneRepository()
                .setURI(gitUri)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
                .call();
    }    
    
    public static Git gitInit(File projectDir) throws GitAPIException, IllegalStateException {
        // init git directory
//        LogUtil.debug(GitUtil.class.getName(), "Init Git repository");
        Git git = Git.init()
                .setDirectory(projectDir)
                .call();
        return git;
    }

    public static void gitAddRemote(Git git, String gitUri) throws URISyntaxException, GitAPIException {
        RemoteListCommand remoteListCommand = git.remoteList();
        List<RemoteConfig> call = remoteListCommand.call();
        boolean isDiff = true;
        if (call != null) {
            for (RemoteConfig con : call) {
                if (con.getName().equals("origin")) {
                    for (URIish uri : con.getURIs()) {
                        if (uri.toString().equals(gitUri)) {
                            isDiff = false;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        if (isDiff) {
            // remove existing remote repo
            LogUtil.debug(AppDevUtil.class.getName(), "Remove existing Git remote repo");
            RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
            remoteRemoveCommand.setName("origin");
            remoteRemoveCommand.call();

            // add remote repo
            if (!gitUri.isEmpty()) {
                LogUtil.debug(AppDevUtil.class.getName(), "Add Git remote repo: " + gitUri);
                RemoteAddCommand remoteAddCommand = git.remoteAdd();
                remoteAddCommand.setName("origin");
                remoteAddCommand.setUri(new URIish(gitUri));
                remoteAddCommand.call();
            }
        }
    }
    
    public static void gitCheckout(Git git, String gitBranch) throws GitAPIException, IOException {
        gitCheckout(git, gitBranch, 0, 0);
    }
    
    public static void gitCheckout(Git git, String gitBranch, int lockedCount, int conflictCount) throws GitAPIException, IOException {
        String currentBranch = git.getRepository().getBranch();
        if (currentBranch == null || !currentBranch.equals(gitBranch)) {
            LogUtil.debug(AppDevUtil.class.getName(), "Checkout branch: " + gitBranch);
            boolean createBranch = !ObjectId.isId(gitBranch);
            if (createBranch) {
                Ref ref = git.getRepository().exactRef("refs/heads/" + gitBranch);
                if (ref != null) {
                    createBranch = false;
                }
            }
            try {
                git.checkout()
                        .setCreateBranch(createBranch)
                        .setName(gitBranch)
                        .call();    
            } catch (CheckoutConflictException e) {
                if (conflictCount < 5) {
                    // commit to repo
                    String username = WorkflowUserManager.ROLE_ANONYMOUS;
                    String email = "";
                    WorkflowUserManager wum = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
                    User user = wum.getCurrentUser();
                    if (user != null) {
                        username = user.getUsername();
                        email = user.getEmail();
                        if (email == null) {
                            email = "";
                        }
                    }
                    String commitMessage = "Fixing checkout conflict";
                    try {
                        if (e.getConflictingPaths() != null && !e.getConflictingPaths().isEmpty()) {
                            commitMessage += ": ";
                            for (String p : e.getConflictingPaths()) {
                                commitMessage += "\n" + p;
                                git.add().addFilepattern(p).call();
                            }
                        }
                    } catch (Exception ce) {
                        LogUtil.error(AppDevUtil.class.getName(), ce, "");
                    }
                    
                    try {
                        LogUtil.info(AppDevUtil.class.getName(), "Commit to Git repo by " + username + ": " + commitMessage);
                        git.commit()
                                .setAuthor(username, email)
                                .setMessage(commitMessage)
                                .call();
                    } catch (Exception ce) {
                        LogUtil.error(AppDevUtil.class.getName(), ce, commitMessage);
                    }

                    gitCheckout(git, gitBranch, lockedCount, conflictCount + 1);
                } else {
                    throw e;
                }
            } catch (JGitInternalException e) {
                //git may lock, try again
                if (e.getMessage().contains("Cannot lock") && lockedCount <= 10) {
                    LogUtil.info(AppDevUtil.class.getName(), "Git is locked. Wait 100ms...");
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {}
                    gitCheckout(git, gitBranch, lockedCount + 1, conflictCount);
                } else {
                    throw e;
                }
            }
        }
    }    
    
    public static List<String> gitBranches(Git git) throws GitAPIException, IOException {
        LogUtil.debug(AppDevUtil.class.getName(), "List branches:");
        List<String> branchList = new ArrayList<>();
        List<Ref> branchRefs = git.branchList()
                .setListMode(ListBranchCommand.ListMode.ALL)
                .call();
        for (Ref ref : branchRefs) {
            String branchName = Repository.shortenRefName(ref.getName());
            if (branchName.startsWith("origin/")) {
                branchName = branchName.substring("origin/".length(), branchName.length());
            }
            if (branchList.contains(branchName)) {
                branchList.remove(branchName);
            }
            branchList.add(0, branchName);
            LogUtil.debug(AppDevUtil.class.getName(), " -> " + branchName);
        }
        return branchList;
    }
    
    public static List<String> gitDiff(Git git, String[] extensions) throws GitAPIException {
        // get diff paths
        List<DiffEntry> diffEntries = git.diff()
                .call();
        List<String> paths = new ArrayList<>();
        for (DiffEntry entry: diffEntries) {
            String path = entry.getPath(DiffEntry.Side.NEW);
            if ("/dev/null".equals(path)) {
                continue;
            }
            int dotIndex = path.lastIndexOf(".");
            String ext = (dotIndex >= 0) ? path.substring(dotIndex + 1) : path;
            if (extensions == null || ArrayUtils.contains(extensions, ext)) {
                paths.add(path);
            }
        }
        return paths;
    }

    public static List<String> gitDeletedDiff(Git git, String[] extensions) throws GitAPIException {
        // get diff paths
        List<DiffEntry> diffEntries = git.diff()
                .call();
        List<String> paths = new ArrayList<>();
        for (DiffEntry entry: diffEntries) {
            String path = entry.getPath(DiffEntry.Side.NEW);
            if ("/dev/null".equals(path)) {
                path = entry.getOldPath();
                int dotIndex = path.lastIndexOf(".");
                String ext = (dotIndex >= 0) ? path.substring(dotIndex + 1) : path;
                if (extensions == null || ArrayUtils.contains(extensions, ext)) {
                    paths.add(path);
                }
            }
        }
        return paths;
    }
    
    public static void gitAdd(Git git, String path) throws GitAPIException {
        // add file to repo
        LogUtil.debug(AppDevUtil.class.getName(), "Add file to Git repo: " + path);
        git.add()
                .addFilepattern(path)
                .call();
    }
    
    public static void gitRemove(Git git, String path) throws GitAPIException {
        // add file to repo
        LogUtil.debug(AppDevUtil.class.getName(), "Remove file from Git repo: " + path);
        git.rm()
                .addFilepattern(path)
                .call();
    }
    
    public static void gitCommit(AppDefinition appDef, Git git, File workingDir, String commitMessage) throws GitAPIException {
        // commit to repo
        String username = WorkflowUserManager.ROLE_ANONYMOUS;
        String email = "";
        WorkflowUserManager wum = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
        User user = wum.getCurrentUser();
        if (user != null) {
            username = user.getUsername();
            email = user.getEmail();
            if (email == null) {
                email = "";
            }
        }
        LogUtil.info(AppDevUtil.class.getName(), "Commit to Git repo by " + username + ": " + commitMessage);
        git.commit()
                .setAuthor(username, email)
                .setMessage(commitMessage)
                .call();
        
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDef);
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        if (!projectDir.equals(workingDir)) {
            gitPushLocal(appDef, git, workingDir); //push if it is a temporary working dir
        }
    }
    
    public static void gitPullAndCommit(AppDefinition appDef, Git git, File workingDir, String commitMessage) throws GitAPIException {
        try {
            AppDevUtil.gitPullLocal(appDef, git, workingDir);
        } catch (Exception e) {
            LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git local repo " + appDef.getAppId() + ". Reason :" + e.getMessage());
        }
        
        gitCommit(appDef, git, workingDir, commitMessage);
    }

    public static void gitRenameBranch(Git git, String gitBranch) throws GitAPIException {
        git.branchRename()
                .setNewName(gitBranch)
                .call();        
    }
    
    public static void gitFetchMerge(File projectDir, Git git, String gitUri, String gitUsername, String gitPassword, MergeStrategy mergeStrategy, AppDefinition appDef) throws GitAPIException, IOException {
        // pull from repo
        LogUtil.info(AppDevUtil.class.getName(), "Pull from Git remote repo: " + gitUri);
        FetchResult fetchResult = git.fetch()
                .setRemote("origin")
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
                .call();
        MergeResult mergeResult = git.merge()
                .setCommit(true)
                .setStrategy(mergeStrategy)
                .include(git.getRepository().findRef("FETCH_HEAD"))
                .call();
        if (fetchResult != null) {
            for (TrackingRefUpdate ref: fetchResult.getTrackingRefUpdates()) {
                LogUtil.debug(AppDevUtil.class.getName(), "Fetch result: " + ref.getResult());
            }
        }
        
        if (mergeResult != null) {
            gitMergeCommit(mergeResult, projectDir, git, appDef);
        }
    }
    
    public static void gitPullLocal(AppDefinition appDef, Git git, File workingDir) throws GitAPIException, IOException {
        String gitBranch = getGitBranchName(appDef);
        try {
            // pull from repo
            LogUtil.debug(AppDevUtil.class.getName(), "Pull from Git local repo: " + appDef.getAppId());

            PullResult pullResult = git.pull()
                    .setRemote("local")
                    .setRemoteBranchName(gitBranch)
                    .call();
            FetchResult fetchResult = pullResult.getFetchResult();
            LogUtil.debug(AppDevUtil.class.getName(), "Pull successful from Git local repo: " + appDef.getAppId() + " - "+ pullResult.isSuccessful());
            
            if (fetchResult != null) {
                LogUtil.debug(AppDevUtil.class.getName(), "Fetch messages: " + fetchResult.getMessages());
            }
            MergeResult mergeResult = pullResult.getMergeResult();
            if (mergeResult != null) {
                gitMergeCommit(mergeResult, workingDir, git, appDef);
            }
        } catch(Exception ne) {
            // ignore
        }
    } 

    public static void gitPull(File projectDir, Git git, String gitBranch, String gitUri, String gitUsername, String gitPassword, MergeStrategy mergeStrategy, AppDefinition appDef) throws GitAPIException, IOException {
        // pull from repo
        LogUtil.info(AppDevUtil.class.getName(), "Pull from Git remote repo: " + gitUri);
        
        PullResult pullResult = git.pull()
                .setRemote("origin")
                .setRemoteBranchName(gitBranch)
                .setStrategy(mergeStrategy)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
                .call();
        FetchResult fetchResult = pullResult.getFetchResult();
        LogUtil.info(AppDevUtil.class.getName(), "Pull successful: " + pullResult.isSuccessful());
        if (fetchResult != null) {
            LogUtil.debug(AppDevUtil.class.getName(), "Fetch messages: " + fetchResult.getMessages());
        }
        MergeResult mergeResult = pullResult.getMergeResult();
        if (mergeResult != null) {
            gitMergeCommit(mergeResult, projectDir, git, appDef);
        }
    }    
    
    public static void gitMergeCommit(MergeResult mergeResult, File projectDir, Git git, AppDefinition appDef) throws IOException, GitAPIException {
        LogUtil.debug(AppDevUtil.class.getName(), "Merge result: " + mergeResult.getMergeStatus());
        if (mergeResult.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
            Map<String, int[][]> allConflicts = mergeResult.getConflicts();
            for (String path : allConflicts.keySet()) {
                int[][] c = allConflicts.get(path);
                // display conflicts
                LogUtil.debug(AppDevUtil.class.getName(), "Conflicts in file " + path);
                for (int i = 0; i < c.length; ++i) {
                    LogUtil.debug(AppDevUtil.class.getName(), "  Conflict #" + i);
                    for (int j = 0; j < (c[i].length) - 1; ++j) {
                        if (c[i][j] >= 0) {
                            LogUtil.debug(AppDevUtil.class.getName(), "    Chunk for "
                                    + mergeResult.getMergedCommits()[j] + " starts on line #"
                                    + c[i][j]);
                        }
                    }
                }
                // merge file conflicts and commit
                fileMergeOurs(projectDir, path);
                gitAdd(git, path);
            }
            gitCommit(appDef, git, projectDir, "merged conflicts");
        }
    }
    
    public static void fileMergeOurs(File projectDir, String path) throws IOException {
        LogUtil.debug(AppDevUtil.class.getName(), "Merge ours: " + path);
        
        path = SecurityUtil.normalizedFileName(path);
        
        File file = new File(projectDir, path);
        String fileContents = FileUtils.readFileToString(file, "UTF-8");
        String ours = "<<<<<<< HEAD\n";
        String separator = "=======";
        String theirs = ">>>>>>>";
        int start = fileContents.indexOf(ours);
        while (start >= 0) {
            int theirsIndex = fileContents.indexOf(theirs);
            int endTheirsIndex = fileContents.indexOf("\n", theirsIndex);
            String diffStr = fileContents.substring(start, endTheirsIndex);
            LogUtil.debug(AppDevUtil.class.getName(), "Merge conflict: " + diffStr);            
            // remove HEAD
            fileContents = StringUtils.replaceOnce(fileContents, ours, "");
            // remove separator and theirs content
            int separatorIndex = fileContents.indexOf(separator);
            theirsIndex = fileContents.indexOf(theirs);
            endTheirsIndex = fileContents.indexOf("\n", theirsIndex);
            String replaceStr = fileContents.substring(separatorIndex, endTheirsIndex+1);
            fileContents = StringUtils.replaceOnce(fileContents, replaceStr, "");
            start = fileContents.indexOf(ours);
        }
        FileUtils.writeStringToFile(file, fileContents, "UTF-8");
    }
    
    public static void gitPushLocal(AppDefinition appDef, Git git, File workingDir) throws GitAPIException {
        // push to remote
        LogUtil.debug(AppDevUtil.class.getName(), "Push to Git local repo " + appDef.getAppId());
        GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
        
        try {
            AppDevUtil.gitCheckout(gitCommitHelper.getLocalGit(), "master");
        } catch (Exception e) {
            LogUtil.debug(AppDevUtil.class.getName(), "Fail to checkout local repo " + appDef.getAppId() + " - " + e.getMessage());
        }
        
        Iterable<PushResult> pushResults = git.push()
                .setRemote("local")
                .call();
        for (PushResult pr: pushResults) {
            for (RemoteRefUpdate ref: pr.getRemoteUpdates()) {
                LogUtil.debug(AppDevUtil.class.getName(), "Push to Git local repo " + appDef.getAppId() + " result: " + ref.getStatus());
                if ("REJECTED_OTHER_REASON".equals(ref.getStatus().toString())) {
                    try {
                        gitPullLocal(appDef, git, workingDir);
                    } catch (Exception e) {
                        LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git local repo " + appDef.getAppId() + ". Reason :" + e.getMessage());
                    }
                    gitPushLocal(appDef, git, workingDir);
                }
            }
        }
        String gitBranch = getGitBranchName(appDef);
        try {
            AppDevUtil.gitCheckout(gitCommitHelper.getLocalGit(), gitBranch);
        } catch (Exception e) {
            LogUtil.debug(AppDevUtil.class.getName(), "Fail to checkout local repo " + appDef.getAppId() + " - " + e.getMessage());
        }
    }
    
    public static void gitPullAndPush(File projectDir, Git git, String gitBranch, String gitUri, String gitUsername, String gitPassword, MergeStrategy mergeStrategy, AppDefinition appDef) throws GitAPIException {
        try {
            gitPull(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, mergeStrategy, appDef);
        } catch (Exception e){
            LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git remote repo " + appDef.getAppId() + ". Reason :" + e.getMessage());
        }

        // push to remote
        LogUtil.info(AppDevUtil.class.getName(), "Push to Git remote repo: " + gitUri);
        Iterable<PushResult> pushResults = git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword))
                .call();
        for (PushResult pr: pushResults) {
            for (RemoteRefUpdate ref: pr.getRemoteUpdates()) {
                LogUtil.info(AppDevUtil.class.getName(), "Push result: " + ref.getStatus());
                if ("REJECTED_OTHER_REASON".equals(ref.getStatus().toString())) {
                    try {
                        gitPull(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, mergeStrategy, appDef);
                    } catch (Exception e) {
                        LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git remote repo " + appDef.getAppId() + ". Reason :" + e.getMessage());
                    }
                    gitPullAndPush(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, mergeStrategy, appDef);
                }
            }
        }
    }

    public static void gitLog(Git git, int start, int count) throws GitAPIException {
        // list commits
        LogUtil.debug(AppDevUtil.class.getName(), "Git log " + count + " commits from " + start);
        Iterable<RevCommit> commits = git.log()
                .setSkip(start)
                .setMaxCount(count)
                .call();
        for (RevCommit commit : commits) {
            LogUtil.debug(AppDevUtil.class.getName(), " -> " + commit.getName() + "; "
                    + commit.getAuthorIdent().getName() + "; "
                    + new Date(commit.getCommitTime() * 1000L) + "; "
                    + commit.getFullMessage());
        }
    }
    
    public static String formatJson(String json) {
        String formattedJson = json;
        try {
            int jsonIndentFactor = 4;
            formattedJson = new JSONObject(json).toString(jsonIndentFactor);
        } catch (JSONException e) {
            // ignore
        }
        return formattedJson;        
    }

    public static String getGitBranchName(AppDefinition appDef) {
        String branchName = appDef.getAppId() + "_" + appDef.getVersion();
        return branchName;
    }
    
    public static String getAppGitDirectory(AppDefinition appDef) {
        String dir = appDef.getAppId() + System.getProperty("file.separator") + getGitBranchName(appDef);
        return dir;
    }
    
    public static String getWorkingGitDirectory(AppDefinition appDef) {
        String dir = appDef.getAppId() + System.getProperty("file.separator") + UuidGenerator.getInstance().getUuid();
        return dir;
    }
    
    protected static String getPullUniqueKey() {
        return Integer.toString(random.nextInt()) + Long.toString(System.nanoTime());
    }
    
    protected static boolean isConcurrentPull(String projectDirName, String uniqueKey) {
        boolean isConcurrent = workingPulls.containsKey(projectDirName) && !workingPulls.get(projectDirName).isEmpty();
        
        Set<String> keys = workingPulls.get(projectDirName);
        if (keys == null) {
            keys = new HashSet<String>();
            workingPulls.put(projectDirName, keys);
        }
        synchronized (workingPulls.get(projectDirName)) {
            keys.add(uniqueKey);
        }
        
        return isConcurrent;
    }
    
    protected static void clearConcurrentPull(String projectDirName) {
        if (workingPulls.containsKey(projectDirName)) {
            synchronized (workingPulls.get(projectDirName)) {
                workingPulls.get(projectDirName).clear();
            }
        }
    }
    
    protected static void waitForConcurrentPullCompleted(String projectDirName, String uniqueKey) {
        int count = 0;
        while (workingPulls.get(projectDirName).contains(uniqueKey) && count < 20) { //should not wait longer than 2s
            try {
                Thread.sleep(100); 
            } catch (Exception e) {}
            count++;
        }
        if (workingPulls.get(projectDirName).contains(uniqueKey)) {
            synchronized (workingPulls.get(projectDirName)) {
                workingPulls.get(projectDirName).remove(uniqueKey);
            }
        }
    }

    public static Map<String, GitCommitHelper> fileInitCommit(AppDefinition appDef, String commitMessage) {
        String appId = appDef.getAppId();
        String gitBranch = getGitBranchName(appDef);
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDef);
        Map<String, GitCommitHelper> gitCommitMap = null;
        
        try {
            File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
            Git localGit = AppDevUtil.gitInit(projectDir);
            try {
                AppDevUtil.gitCheckout(localGit, gitBranch);
                
                Properties gitProperties = getAppDevProperties(appDef);
                if (gitProperties != null) {
                    boolean alwaysPull = Boolean.parseBoolean(gitProperties.getProperty(PROPERTY_GIT_CONFIG_PULL));
                    if (alwaysPull) {
                        // ensure git pull only done once per request
                        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                        boolean gitPullRequestDone = request != null && "true".equals(request.getAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appId));
                        if (!gitPullRequestDone) {
                            String pullKeys = getPullUniqueKey();
                            if (isConcurrentPull(projectDirName, pullKeys)) {
                                waitForConcurrentPullCompleted(projectDirName, pullKeys);
                            } else {
                                try {
                                    // perform git pull
                                    String gitUri = gitProperties.getProperty(PROPERTY_GIT_URI);
                                    String gitUsername = gitProperties.getProperty(PROPERTY_GIT_USERNAME);
                                    String gitPassword = gitProperties.getProperty(PROPERTY_GIT_PASSWORD);
                                    AppDevUtil.gitAddRemote(localGit, gitUri);
                                    AppDevUtil.gitPull(projectDir, localGit, gitBranch, gitUri, gitUsername, gitPassword, MergeStrategy.RECURSIVE, appDef);
                                } finally {
                                    clearConcurrentPull(projectDirName);
                                }
                            }
                            
                            // set flag to prevent further pulls in the same request
                            if (request != null) {
                                request.setAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appId, "true");
                            }
                        }
                    }
                }
            } catch(RefNotFoundException | RefNotAdvertisedException | JGitInternalException | URISyntaxException ne) {
                LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git remote repo " + appDef.getAppId() + ". Reason :" + ne.getMessage());
            }
            
            //create temporary git working folder
            String projectWorkingDirName = getWorkingGitDirectory(appDef);
            File projectWorkingDir = AppDevUtil.dirSetup(baseDir, projectWorkingDirName);
            Git git = AppDevUtil.gitInit(projectWorkingDir);
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setName("local");
            remoteAddCommand.setUri(new URIish(projectDir.getAbsolutePath()));
            remoteAddCommand.call();
            
            AppDevUtil.gitPullLocal(appDef, git, projectWorkingDir);
            try {
                
                AppDevUtil.gitCheckout(git, gitBranch);
            } catch(RefNotFoundException ne) {
                // ignore
            }
            
            // add git object
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null) {
                gitCommitMap = (Map<String, GitCommitHelper>)request.getAttribute(ATTRIBUTE_GIT_COMMIT_REQUEST);
            }
            if (gitCommitMap == null) {
                gitCommitMap = new LinkedHashMap<>();
            }
            GitCommitHelper gitCommitHelper = gitCommitMap.get(appId);
            if (gitCommitHelper == null) {
                gitCommitHelper = new GitCommitHelper();
                gitCommitMap.put(appId, gitCommitHelper);
            }
            gitCommitHelper.setWorkingDir(projectWorkingDir);
            gitCommitHelper.setAppDefinition(appDef);
            gitCommitHelper.setLocalGit(localGit);
            gitCommitHelper.setGit(git);
            gitCommitHelper.setCommitMessage(commitMessage);   
            if (request != null) {
                request.setAttribute(ATTRIBUTE_GIT_COMMIT_REQUEST, gitCommitMap);        
            }
        } catch (IOException | IllegalStateException | GitAPIException | URISyntaxException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
        return gitCommitMap;
    }
    
    public static GitCommitHelper getGitCommitHelper(AppDefinition appDef) {
        Map<String, GitCommitHelper> gitCommitMap = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {        
            gitCommitMap = (Map<String, GitCommitHelper>)request.getAttribute(ATTRIBUTE_GIT_COMMIT_REQUEST);
        }
        if (gitCommitMap == null) {
            gitCommitMap = fileInitCommit(appDef, "");
        }
        GitCommitHelper gitCommitHelper = gitCommitMap.get(appDef.getAppId());
        if (gitCommitHelper == null) {
            gitCommitMap = fileInitCommit(appDef, "");
            gitCommitHelper = gitCommitMap.get(appDef.getAppId());
        }
        return gitCommitHelper;      
    }
    
    public static void fileSave(AppDefinition appDef, String path, String fileContents, String commitMessage) {
        path = SecurityUtil.normalizedFileName(path);
        fileContents = compatibleNewline(fileContents);
        
        String gitBranch = getGitBranchName(appDef);
        
        try {
            GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
            Git git = gitCommitHelper.getGit();
            boolean noHead = false;
            try {
                AppDevUtil.gitCheckout(git, gitBranch);
            } catch(RefNotFoundException ne) {
                noHead = true;
            }
            if (noHead) {
                AppDevUtil.gitCommit(appDef, git, gitCommitHelper.getWorkingDir(), "Initial commit for " + gitBranch);
                AppDevUtil.gitRenameBranch(git, gitBranch);
            }
    
            // check for content changes
            File file = new File(gitCommitHelper.getWorkingDir(), path);
            boolean toSave = true;
            try {
                String currentContents = FileUtils.readFileToString(file, "UTF-8");
                toSave = (currentContents == null || !cleanForCompare(currentContents).equals(cleanForCompare(fileContents)));
            } catch(FileNotFoundException e) {
                // ignore
            }
             
            if (toSave) {
                // save file contents
                FileUtils.writeStringToFile(file, fileContents, "UTF-8");
                if (commitMessage != null) {
                    // git add to commit
                    String[] extensions = new String[] { "json", "xml", "xpdl" };
                    List<String> paths = AppDevUtil.gitDiff(git, extensions);
                    boolean modified = !paths.isEmpty() && paths.contains(path);
                    if (modified) {
                        AppDevUtil.gitAdd(git, path);
                        gitCommitHelper.addCommitMessage(commitMessage);
                    }
                }
            }

        } catch (IOException | GitAPIException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
    }    
    
    public static void fileDelete(AppDefinition appDefinition, String path, String commitMessage) {
        path = SecurityUtil.normalizedFileName(path);
        
        try {
            // checkout branch
            String gitBranch = getGitBranchName(appDefinition);
            GitCommitHelper gitCommitHelper = getGitCommitHelper(appDefinition);
            Git git = gitCommitHelper.getGit();
            try {
                AppDevUtil.gitCheckout(git, gitBranch);
            } catch(RefNotFoundException ne) {
                LogUtil.debug(AppDevUtil.class.getName(), "Fail to checkout branch " + gitBranch + ". Reason :" + ne.getMessage());
            }

            // delete file
            File file = new File(gitCommitHelper.getWorkingDir(), path);
            file.delete();

            // commit and push
            if (commitMessage != null) {
                AppDevUtil.gitRemove(git, path);
                gitCommitHelper.addCommitMessage(commitMessage);
            }
        } catch (IOException | GitAPIException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
    }    
    
    public static String fileReadToString(AppDefinition appDefinition, String path, boolean pull) {
        path = SecurityUtil.normalizedFileName(path);
        
        String json = null;
        try {
            File file = fileGetFileObject(appDefinition, path, pull);
            if (file != null && file.isFile()) {
                try {
                    json = FileUtils.readFileToString(file, "UTF-8");
                } catch (FileNotFoundException ex) {
                    LogUtil.debug(AppDevUtil.class.getName(), "File " + path + " not found");
                }
            } else {
                LogUtil.debug(AppDevUtil.class.getName(), "File " + path + " not found");                
            }
        } catch (IOException | URISyntaxException | GitAPIException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }        
        return json;
    }

    public static File fileGetFileObject(AppDefinition appDefinition, String path, boolean pull) throws IOException, GitAPIException, URISyntaxException {
        path = SecurityUtil.normalizedFileName(path);
        
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDefinition);
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        Git git = AppDevUtil.gitInit(projectDir);
        String gitBranch = getGitBranchName(appDefinition);
        try {
            AppDevUtil.gitCheckout(git, gitBranch);
            Properties gitProperties = getAppDevProperties(appDefinition);
            boolean alwaysPull = Boolean.parseBoolean(gitProperties.getProperty(PROPERTY_GIT_CONFIG_PULL));
            if (pull || alwaysPull) {
                // ensure git pull only done once per request
                String appId = appDefinition.getAppId();
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                boolean gitPullRequestDone = request != null && "true".equals(request.getAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appId));
                if (!gitPullRequestDone) {
                    String pullKeys = getPullUniqueKey();
                    if (isConcurrentPull(projectDirName, pullKeys)) {
                        waitForConcurrentPullCompleted(projectDirName, pullKeys);
                    } else {
                        try {
                            // perform git pull
                            String gitUri = gitProperties.getProperty(PROPERTY_GIT_URI);
                            String gitUsername = gitProperties.getProperty(PROPERTY_GIT_USERNAME);
                            String gitPassword = gitProperties.getProperty(PROPERTY_GIT_PASSWORD);
                            AppDevUtil.gitAddRemote(git, gitUri);
                            AppDevUtil.gitPull(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, MergeStrategy.RECURSIVE, appDefinition);
                        } finally {
                            clearConcurrentPull(projectDirName);
                        }
                    }
                    // set flag to prevent further pulls in the same request
                    if (request != null) {
                        request.setAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appId, "true");
                    }
                }
            }
        } catch(RefNotFoundException | RefNotAdvertisedException | JGitInternalException | URISyntaxException re) {
            LogUtil.debug(AppDevUtil.class.getName(), "Fail to pull from Git remote repo " + appDefinition.getAppId() + ". Reason :" + re.getMessage());
        }
        File file = new File(projectDir, path);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }
    
    public static List<String> getAppGitBranches(AppDefinition appDef) throws GitAPIException, IOException {
        LogUtil.debug(AppDevUtil.class.getName(), "List branches:");
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDef);
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        Git git = AppDevUtil.gitInit(projectDir);
        List<String> branchList = AppDevUtil.gitBranches(git);
        return branchList;
    }

    public static String getAppDefinitionXml(AppDefinition appDefinition) {
        String appDefinitionXml = null;
        ByteArrayOutputStream baos = null;

        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        AppDefinition appDef = appDefinition;
        if (appDef instanceof HibernateProxy) {
            appDef = (AppDefinition)((HibernateProxy)appDef).getHibernateLazyInitializer().getImplementation();
        }
        
        Collection<FormDefinition> formDefinitionList = appDef.getFormDefinitionList();
        Collection<DatalistDefinition> datalistDefinitionList = appDef.getDatalistDefinitionList();
        Collection<UserviewDefinition> userviewDefinitionList = appDef.getUserviewDefinitionList();
        Collection<BuilderDefinition> builderDefinitionList = appDef.getBuilderDefinitionList();
        Collection<PluginDefaultProperties> pluginDefaultProperties = appDef.getPluginDefaultPropertiesList();
        Collection<EnvironmentVariable> envVariableList = appDef.getEnvironmentVariableList();
        Collection<PackageDefinition> packageDefinitionList = appDef.getPackageDefinitionList();
        Date appDateCreated = appDef.getDateCreated();
        Date appDateModified = appDef.getDateModified();
        PackageDefinition origPackageDef = appDef.getPackageDefinition();
        PackageDefinition packageDef = origPackageDef;
        if (origPackageDef instanceof HibernateProxy) {
            packageDef = (PackageDefinition)((HibernateProxy)origPackageDef).getHibernateLazyInitializer().getImplementation();
        }        
        Date packageDateCreated = (packageDef != null) ? packageDef.getDateCreated() : null;
        Date packageDateModiDate = (packageDef != null) ? packageDef.getDateModified() : null;
        Long packageVersion =  (packageDef != null) ? packageDef.getVersion() : null;
        
        try {
            // remove unneeded elements
            appDef.setFormDefinitionList(null);
            appDef.setDatalistDefinitionList(null);
            appDef.setUserviewDefinitionList(null);
            appDef.setBuilderDefinitionList(null);
            appDef.setDateCreated(null);
            appDef.setDateModified(null);
            if (packageDef != null) {
                packageDef.setVersion(null);
                packageDef.setDateCreated(null);
                packageDef.setDateModified(null);
            }
            Collection<PackageDefinition> tempPackageDefinitionList = new ArrayList<>();
            tempPackageDefinitionList.add(packageDef);
            appDef.setPackageDefinitionList(tempPackageDefinitionList);
            appDef.setPluginDefaultPropertiesList(null);
            appDef.setEnvironmentVariableList(null);
            
            // generate XML
            baos = new ByteArrayOutputStream();
            Serializer serializer = new Persister();
            serializer.write(appDef, baos);
            appDefinitionXml = baos.toString("UTF-8");
        } catch (Exception ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            TimeZone.setDefault(current);
            appDef.setFormDefinitionList(formDefinitionList);
            appDef.setDatalistDefinitionList(datalistDefinitionList);
            appDef.setUserviewDefinitionList(userviewDefinitionList);
            appDef.setBuilderDefinitionList(builderDefinitionList);
            appDef.setPluginDefaultPropertiesList(pluginDefaultProperties);
            appDef.setEnvironmentVariableList(envVariableList);
            appDef.setDateCreated(appDateCreated);
            appDef.setDateModified(appDateModified);
            if (packageDef != null) {
                packageDef.setDateCreated(packageDateCreated);
                packageDef.setDateModified(packageDateModiDate);
                packageDef.setVersion(packageVersion);
            }
            appDef.setPackageDefinitionList(packageDefinitionList);
        }
        return appDefinitionXml;        
    }   
    
    public static String getAppConfigXml(AppDefinition appDefinition) {
        String appDefinitionXml = null;
        ByteArrayOutputStream baos = null;

        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        AppDefinition appDef = appDefinition;
        if (appDef instanceof HibernateProxy) {
            appDef = (AppDefinition)((HibernateProxy)appDef).getHibernateLazyInitializer().getImplementation();
        }
        
        Collection<FormDefinition> formDefinitionList = appDef.getFormDefinitionList();
        Collection<DatalistDefinition> datalistDefinitionList = appDef.getDatalistDefinitionList();
        Collection<UserviewDefinition> userviewDefinitionList = appDef.getUserviewDefinitionList();
        Collection<BuilderDefinition> builderDefinitionList = appDef.getBuilderDefinitionList();
        Collection<PluginDefaultProperties> pluginDefaultProperties = appDef.getPluginDefaultPropertiesList();
        Collection<EnvironmentVariable> envVariableList = appDef.getEnvironmentVariableList();
        Collection<PackageDefinition> packageDefinitionList = appDef.getPackageDefinitionList();
        Collection<Message> messageList = appDef.getMessageList();
        Collection<AppResource> resourceList = appDef.getResourceList();
        Date appDateCreated = appDef.getDateCreated();
        Date appDateModified = appDef.getDateModified();
        
        try {
            // remove unneeded elements
            appDef.setFormDefinitionList(null);
            appDef.setDatalistDefinitionList(null);
            appDef.setUserviewDefinitionList(null);
            appDef.setBuilderDefinitionList(null);
            appDef.setDateCreated(null);
            appDef.setDateModified(null);
            appDef.setPackageDefinitionList(null);
            appDef.setMessageList(null);
            appDef.setResourceList(null);
            
            // generate XML
            baos = new ByteArrayOutputStream();
            Serializer serializer = new Persister();
            serializer.write(appDef, baos);
            appDefinitionXml = baos.toString("UTF-8");
        } catch (Exception ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            TimeZone.setDefault(current);
            appDef.setFormDefinitionList(formDefinitionList);
            appDef.setDatalistDefinitionList(datalistDefinitionList);
            appDef.setUserviewDefinitionList(userviewDefinitionList);
            appDef.setBuilderDefinitionList(builderDefinitionList);
            appDef.setPluginDefaultPropertiesList(pluginDefaultProperties);
            appDef.setEnvironmentVariableList(envVariableList);
            appDef.setDateCreated(appDateCreated);
            appDef.setDateModified(appDateModified);
            appDef.setPackageDefinitionList(packageDefinitionList);
            appDef.setMessageList(messageList);
            appDef.setResourceList(resourceList);
        }
        return appDefinitionXml;        
    }     
    
    public static String getPackageXpdl(AppDefinition appDef) {
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            return getPackageXpdl(packageDef);
        }
        return null;
    }
    
    public static String getPackageXpdl(PackageDefinition packageDef) {
        String xpdl = null;
        try {
            if (packageDef != null) {
                WorkflowManager workflowManager = (WorkflowManager)AppUtil.getApplicationContext().getBean("workflowManager");
                try {
                    byte[] contents = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
                    if (contents != null) {
                        xpdl = new String(contents, "UTF-8");
                    }
                } catch (NullPointerException npe) {
                    // ignore non-existing package
                }
            }
        } catch (UnsupportedEncodingException | BeansException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
        return xpdl;
    }

    public static void dirCopy(AppDefinition appDef, String sourcePath, String targetDirName, String commitMessage) {
        String gitBranch = getGitBranchName(appDef);
        
        try {
            GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
            Git git = gitCommitHelper.getGit();
            AppDevUtil.gitCheckout(git, gitBranch);
            
            if (sourcePath != null && !sourcePath.isEmpty()) {
                File sourceDir = new File(sourcePath);
                if (sourceDir.exists()) {
                    File targetDir = new File(gitCommitHelper.getWorkingDir(), targetDirName);

                    // remove existing directory
                    FileUtils.deleteDirectory(targetDir);
                    // copy directory
                    targetDir.mkdirs();
                    FileUtils.copyDirectory(sourceDir, targetDir, true);
                }
            }
            
            if (commitMessage != null) {
                // git commit
                boolean diff = false;
                List<String> paths = AppDevUtil.gitDiff(git, null);
                for (String path: paths) {
                    if (path.startsWith(targetDirName)) {
                        AppDevUtil.gitAdd(git, path);
                        diff = true;
                    }
                }
                List<String> deletedPaths = AppDevUtil.gitDeletedDiff(git, null);
                for (String path: deletedPaths) {
                    if (path.startsWith(targetDirName)) {
                        AppDevUtil.gitRemove(git, path);
                        diff = true;
                    }
                }
                if (diff) {
                    gitCommitHelper.addCommitMessage(commitMessage);
                }
            }
        } catch (IOException | GitAPIException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
    } 
    
    public static void dirDelete(AppDefinition appDef, String commitMessage) {
        String gitBranch = getGitBranchName(appDef);
        
        try {
            GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
            Git git = gitCommitHelper.getGit();
            AppDevUtil.gitCheckout(git, gitBranch);
            
            final String[] dirs = new String[] { "forms", "lists", "userviews", "plugins", "builder", "resources" };
            for (String dir : dirs) {
                // delete dir
                File tempDir = new File(gitCommitHelper.getWorkingDir(), dir);
                FileUtils.deleteDirectory(tempDir);  
            }
            
            Collection<File> files = FileUtils.listFiles(gitCommitHelper.getWorkingDir(), new String[]{"json", "xml", "xpdl", "jar"}, true);
            for (File file : files) {
                file.delete();
            }
            
            if (commitMessage != null) {
                // git commit
                List<String> deletedPaths = AppDevUtil.gitDeletedDiff(git, null);
                for (String path: deletedPaths) {
                    AppDevUtil.gitRemove(git, path);
                }
                gitCommitHelper.addCommitMessage(commitMessage);
            }
        } catch (IOException | GitAPIException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
    }      

    public static void dirSyncAppResources(AppDefinition appDef) {
        GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
        gitCommitHelper.setSyncResources(true);
    }
    
    public static void syncAppResources(AppDefinition appDef) {
        String commitMessage =  "Update app resources " + appDef.getId();
        String sourcePath = AppResourceUtil.getBaseDirectory() + File.separator + appDef.getAppId()  + File.separator + appDef.getVersion() + File.separator;
        String targetDirName =  "resources";
        AppDevUtil.dirCopy(appDef, sourcePath, targetDirName, commitMessage);
    }    

    public static void dirSyncAppPlugins(AppDefinition appDef) {
        GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
        gitCommitHelper.setSyncPlugins(true);
    }
    
    public static void syncAppPlugins(AppDefinition appDef) {
        // get osgi plugins
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> pluginList = pluginManager.listOsgiPlugin(null);
        
        try {
            GitCommitHelper gitCommitHelper = getGitCommitHelper(appDef);
            
            String targetDirName = "plugins";
            File targetDir = new File(gitCommitHelper.getWorkingDir(), targetDirName);
            
            // remove existing directory
            FileUtils.deleteDirectory(targetDir);
            
            // copy plugins
            targetDir.mkdirs();

            // find all definition files
            final String[] extensions = new String[] { "json", "xml", "xpdl" };
            final String[] dirs = new String[] { "forms", "lists", "userviews", "builder"};
            Iterator<File> fileIterator = FileUtils.iterateFiles(gitCommitHelper.getWorkingDir(), new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    String path = file.getName();
                    int dotIndex = path.lastIndexOf(".");
                    String ext = (dotIndex >= 0) ? path.substring(dotIndex + 1) : path;
                    return ArrayUtils.contains(extensions, ext);
                }
            }, new AbstractFileFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return ArrayUtils.contains(dirs, name) || "builder".equals(dir.getName());
                }
                
            });
            // combine all definitions into a string for matching
            String concatAppDef = "";
            while(fileIterator.hasNext()) {
                File file = fileIterator.next();
                String fileContents = FileUtils.readFileToString(file, "UTF-8");
                concatAppDef += fileContents + "~~~";
            }
            // look for plugins used in any definition file
            for (Plugin plugin: pluginList) {
                String pluginClassName = ClassUtils.getUserClass(plugin).getName();
                if (concatAppDef.contains(pluginClassName)) {
                    // plugin used, copy
                    String path = pluginManager.getOsgiPluginPath(pluginClassName);
                    if (path != null) {
                        File src = new File(path);
                        File dest = new File(targetDir, src.getName());
                        FileUtils.copyFile(src, dest);
                    }
                }                
            }
            
            // commit
            String commitMessage =  "Update app plugins " + appDef.getId();
            AppDevUtil.dirCopy(appDef, null, targetDirName, commitMessage);
            
        } catch (IOException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }        
        
    }    
    
    public static AppDefinition dirSyncApp(String appId, Long appVersion) throws IOException, GitAPIException, URISyntaxException {
        AppDefinition updatedAppDef = null;
        
        // check if project dir exists
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao)AppUtil.getApplicationContext().getBean("appDefinitionDao");
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, appVersion);
        if (appDef == null) {
            appDef = AppDevUtil.createDummyAppDefinition(appId, appVersion);
        }
        
        File projectDir = null;
        if (appDef.getVersion() != null) {
            projectDir = AppDevUtil.fileGetFileObject(appDef, "appDefinition.xml", false);
        }
        
        if (projectDir == null || !projectDir.exists()) {
            // first time init project files
            LogUtil.info(AppDevUtil.class.getName(), "Git project not found, first time init for app " + appDef);
            appDefinitionDao.saveOrUpdate(appDef.getAppId(), appDef.getVersion(), true);
            LogUtil.info(AppDevUtil.class.getName(), "Git project init complete for app " + appDef);
        }
        
        // compare from app last modified date
        Date latestDate = AppDevUtil.dirLastModified(appDef);
        Properties appProps = AppDevUtil.getAppDevProperties(appDef);
        String appAutoSync = appProps.getProperty(PROPERTY_GIT_CONFIG_AUTO_SYNC);
        Date appLastModifiedDate = appDef.getDateModified();
        if (appLastModifiedDate != null) {
            Calendar lastModifiedCal = Calendar.getInstance();
            lastModifiedCal.setTime(appLastModifiedDate);
            lastModifiedCal.setTimeZone(TimeZone.getTimeZone("UTC"));
            appLastModifiedDate = lastModifiedCal.getTime();            
        }
        if ("true".equals(appAutoSync) && (appLastModifiedDate == null || (latestDate != null && latestDate.after(appLastModifiedDate)))) {
            LogUtil.info(AppDevUtil.class.getName(), "Change detected (" + latestDate + " vs " + appLastModifiedDate + "), init sync for app " + appDef);
            // sync app
            updatedAppDef = appDefinitionDao.syncAppDefinition(appDef.getAppId(), appDef.getVersion());
            LogUtil.info(AppDevUtil.class.getName(), "Sync complete for app " + appDef);
        }
        return updatedAppDef;
    }

    public static AppDefinition createDummyAppDefinition(String appId, Long appVersion) {
        // validate appId
        appId = SecurityUtil.validateStringInput(appId);
        
        // app version not in db, create dummy object
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId(appId);
        appDef.setVersion(appVersion);
        appDef.setFormDefinitionList(new ArrayList());
        appDef.setDatalistDefinitionList(new ArrayList());
        appDef.setUserviewDefinitionList(new ArrayList());
        appDef.setBuilderDefinitionList(new ArrayList());
        appDef.setEnvironmentVariableList(new ArrayList());
        appDef.setPluginDefaultPropertiesList(new ArrayList());
        appDef.setMessageList(new ArrayList());
        appDef.setResourceList(new ArrayList());
        
        return appDef;
    }

    public static AppDefinition createAppDefinitionFromXml(byte[] appDefData) throws RuntimeException {
        AppDefinition newAppDef = null;
        Serializer serializer = new Persister();
        try {
            newAppDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appDefData), false);
            if (newAppDef.getFormDefinitionList() == null) {
                newAppDef.setFormDefinitionList(new ArrayList());
            }
            if (newAppDef.getDatalistDefinitionList()== null) {
                newAppDef.setDatalistDefinitionList(new ArrayList());
            }
            if (newAppDef.getUserviewDefinitionList()== null) {
                newAppDef.setUserviewDefinitionList(new ArrayList());
            }
            if (newAppDef.getBuilderDefinitionList()== null) {
                newAppDef.setBuilderDefinitionList(new ArrayList());
            }
            if (newAppDef.getEnvironmentVariableList()== null) {
                newAppDef.setEnvironmentVariableList(new ArrayList());
            }
            if (newAppDef.getPluginDefaultPropertiesList()== null) {
                newAppDef.setPluginDefaultPropertiesList(new ArrayList());
            }
            if (newAppDef.getMessageList()== null) {
                newAppDef.setMessageList(new ArrayList());
            }
            if (newAppDef.getResourceList()== null) {
                newAppDef.setResourceList(new ArrayList());
            }
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return newAppDef;
    }
    
    public static Date dirLastModified(AppDefinition appDef) throws URISyntaxException, GitAPIException, IOException {
        Date latestDate = null;

        String profile = DynamicDataSourceManager.getCurrentProfile();
        String cacheKey = profile + "_dirLastModified_" + appDef.toString();
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("userviewMenuCache");
        if (cache != null) {
            Element element = cache.get(cacheKey);
            if (element != null) {
                latestDate = (Date)element.getObjectValue();
            }
        }
        if (latestDate == null) {
            File dir = AppDevUtil.fileGetFileObject(appDef, ".", false);
            if (dir != null && dir.isDirectory()) {
                // get latest modified date
                Collection<File> files = FileUtils.listFiles(dir, new String[]{ "json", "xml", "xpdl", "jar" }, true);
                for (File file: files) {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    Date dateModified = new Date(attr.lastModifiedTime().toMillis());
                    if (latestDate == null || dateModified.after(latestDate)) {
                        latestDate = dateModified;
                    }
                }
                Element element = new Element(cacheKey, latestDate);
                cache.put(element);
            }
        }
        if (latestDate != null) {
            Calendar latestCal = Calendar.getInstance();
            latestCal.setTime(latestDate);
            latestCal.set(Calendar.MILLISECOND, 0);
            latestDate = latestCal.getTime();
        }
        return latestDate;
    }
    
    public static Collection<AbstractAppVersionedObject> fileFindAll(Class clazz, AppDefinition appDefinition, boolean includeDetails) {
        Collection<AbstractAppVersionedObject> results = new ArrayList<>();
        try {
            String type = clazz.getName();
            String path = null;
            boolean resursive = false;
            if (type.endsWith("FormDefinition")) {
                path = "forms";
            } else if (type.endsWith("DatalistDefinition")) {
                path = "lists";
            } else if (type.endsWith("UserviewDefinition")) {
                path = "userviews";
            } else if (type.endsWith("BuilderDefinition")) {
                path = "builder";
                resursive = true;
            }
            if (path != null) {
                try {
                    File dir = fileGetFileObject(appDefinition, path, false);
                    if (dir != null && dir.isDirectory()) {
                        Collection<File> files = FileUtils.listFiles(dir, new String[]{ "json" }, resursive);
                        for (File file: files) {
                            AbstractAppVersionedObject newObj = AppDevUtil.createObjectFromJsonFile(file, clazz, includeDetails, appDefinition);
                            results.add(newObj);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    LogUtil.debug(AppDevUtil.class.getName(), "File " + path + " not found");
                }
            }
        } catch (IOException | IllegalAccessException | InstantiationException | URISyntaxException | GitAPIException | JSONException ex) {
            LogUtil.error(AppDevUtil.class.getName(), ex, ex.getMessage());
        }
        return results;
    }

    public static AbstractAppVersionedObject createObjectFromJsonFile(File file, Class clazz, boolean includeDetails, AppDefinition appDefinition) throws IllegalAccessException, IOException, JSONException, InstantiationException {
        AbstractAppVersionedObject newObj = null;
        String json = FileUtils.readFileToString(file, "UTF-8");
        if (json != null && !json.trim().isEmpty()) {
            JSONObject jsonObj = new JSONObject(json);
            newObj = (AbstractAppVersionedObject)clazz.newInstance();
            if (includeDetails) {
                Map<String, Object> propMap;
                try {
                    propMap = PropertyUtil.getProperties(jsonObj.getJSONObject("properties"));
                } catch(JSONException e) {
                    propMap = PropertyUtil.getProperties(jsonObj);
                }
                String id = (String)propMap.get("id");
                String name = (String)propMap.get("name");
                String description = (String)propMap.get("description");
                newObj.setId(id);
                newObj.setName(name);
                newObj.setDescription(description);
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                newObj.setDateCreated(new Date(attr.creationTime().toMillis()));
                newObj.setDateModified(new Date(attr.lastModifiedTime().toMillis()));
                newObj.setAppDefinition(appDefinition);
                newObj.setJson(json);
                if (newObj instanceof FormDefinition) {
                    ((FormDefinition) newObj).setTableName((String)propMap.get("tableName"));
                } else if (newObj instanceof UserviewDefinition) {
                    ((UserviewDefinition) newObj).setThumbnail((String)propMap.get("thumbnail"));
                } else if (newObj instanceof BuilderDefinition) {
                    String type = file.getParentFile().getName();
                    if (id == null) {
                        id = file.getName().substring(0, file.getName().indexOf("."));
                        newObj.setId(id);
                    }
                    if (name == null) {
                        if (id.equalsIgnoreCase(TaggingUtil.ID)) {
                            name = "Tagging";
                        } else if (type.equals(CustomFormDataTableUtil.TYPE)) {
                            name = id.replaceAll(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME, "");
                        } else {
                            name = id;
                        }
                        newObj.setName(name);
                    }
                    
                    ((BuilderDefinition) newObj).setType(type);
                }
            }
        }
        return newObj;
    }
    
    public static void addPluginsToZip(AppDefinition appDef, ZipOutputStream zip) {
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDef);
        try {
            File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
            String targetDirName = "plugins";
            File targetDir = new File(projectDir, targetDirName);
            
            if (targetDir.exists()) {
                File[] files = targetDir.listFiles();
                for (File file : files)
                {
                    if (file.canRead())
                    {
                        FileInputStream fis = null;
                        try {
                            zip.putNextEntry(new ZipEntry(file.getName()));
                            fis = new FileInputStream(file);
                            byte[] buffer = new byte[4092];
                            int byteCount = 0;
                            while ((byteCount = fis.read(buffer)) != -1)
                            {
                                zip.write(buffer, 0, byteCount);
                            }
                            zip.closeEntry();
                        } finally {
                            if (fis != null) {
                                fis.close();
                            }
                        }  
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppDevUtil.class.getName(), e, "");
        }
    }
    
    public static Collection<String> getPluginJarList(AppDefinition appDef) {
        if (appDef == null) {
            appDef = AppUtil.getCurrentAppDefinition();
        }
        
        Collection<String> plugins = new ArrayList<String>();
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = getAppGitDirectory(appDef);
        try {
            File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
            String targetDirName = "plugins";
            File targetDir = new File(projectDir, targetDirName);
            
            if (targetDir.exists()) {
                File[] files = targetDir.listFiles();
                for (File file : files)
                {
                    plugins.add(file.getName());
                }
            }
        } catch (Exception e) {
            LogUtil.error(AppDevUtil.class.getName(), e, "");
        }
        return plugins;
    }
    
    public static String cleanForCompare(String xpdl) {
        xpdl = xpdl.replaceAll("\n", "");
        xpdl = xpdl.replaceAll("\r", "");
        xpdl = xpdl.trim();
        
        return xpdl;
    } 
    
    public static String compatibleNewline(String content) {
        if (content == null) {
            return content;
        }
        
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", "\r\n");
        content = content.trim();
        
        return content;
    }
}
