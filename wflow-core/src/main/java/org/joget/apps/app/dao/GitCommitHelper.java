package org.joget.apps.app.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.commons.util.LogUtil;

public class GitCommitHelper {

    File workingDir;
    AppDefinition appDefinition;
    Git localGit;
    Git git;
    String commitMessage;
    private Map<String, String> pendingFilesToSave = new LinkedHashMap<>();
    private Set<String> pendingFilesToDelete = new LinkedHashSet<>();
    private Set<String> pendingDirsToDelete = new LinkedHashSet<>();
    boolean syncPlugins;
    boolean syncResources;
    int mergeAttempts = 0;
    private boolean committed = false;

    public GitCommitHelper() {
        commitMessage = "";
    }
    
    public AppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(AppDefinition appDefinition) {
        this.appDefinition = appDefinition;
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }
    
    public Git getLocalGit() {
        return localGit;
    }

    public void setLocalGit(Git localGit) {
        this.localGit = localGit;
    }

    public Git getGit() {
        return git;
    }

    public void setGit(Git git) {
        this.git = git;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public int getMergeAttempts() {
        return mergeAttempts;
    }

    public void incrementMergeAttempts() {
        this.mergeAttempts++;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    public void addCommitMessage(String newCommitMessage) {
        if (newCommitMessage == null || newCommitMessage.isEmpty()) {
            return;
        }
        String messageToAppend = newCommitMessage + ". \n";
        if (commitMessage == null) {
            commitMessage = "";
        }
        if (!commitMessage.contains(messageToAppend)) {
            commitMessage += messageToAppend;
        }
    }
    
    public boolean hasChanges() {
        try {
            Status status = git.status().call();
            Set uncommittedChanges = status.getUncommittedChanges();
            if (uncommittedChanges != null && !uncommittedChanges.isEmpty()) {
                LogUtil.debug(GitCommitHelper.class.getName(), workingDir.getAbsolutePath() + " detected " + uncommittedChanges.size() + " changes.");
                return true;
            }
        } catch (Exception e) {
            LogUtil.debug(GitCommitHelper.class.getName(), e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GitCommitHelper other = (GitCommitHelper) obj;
        if (!Objects.equals(this.git.toString(), other.git.toString())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.git);
        return hash;
    }
    
    public boolean isSyncPlugins() {
        return syncPlugins;
    }

    public void setSyncPlugins(boolean syncPlugins) {
        this.syncPlugins = syncPlugins;
    }

    public boolean isSyncResources() {
        return syncResources;
    }

    public void setSyncResources(boolean syncResources) {
        this.syncResources = syncResources;
    }
    
    public Map<String, String> getPendingFilesToSave() {
        return pendingFilesToSave;
    }

    public Set<String> getPendingFilesToDelete() {
        return pendingFilesToDelete;
    }

    public Set<String> getPendingDirsToDelete() {
        return pendingDirsToDelete;
    }
    
    public void addPendingFileToSave(String path, String fileContents) {
        init();
        pendingFilesToSave.put(path, fileContents);
    }

    public void addPendingFileToDelete(String path) {
        init();
        pendingFilesToDelete.add(path);
    }

    public void addPendingDirToDelete(String path) {
        init();
        pendingDirsToDelete.add(path);
    }
    
    /**
     * Only init when there is file changes
     */
    public void init() {
        if (getGit() != null) {
            //already init temporary working git repo
            return;
        }
        AppDevUtil.initGitCommitHelper(this);
    }

    public void processPending() throws Exception {
        // process pending files to save
        if (pendingFilesToSave != null) {
            for (java.util.Map.Entry<String, String> entry : pendingFilesToSave.entrySet()) {
                File file = new File(workingDir, entry.getKey());
                FileUtils.writeStringToFile(file, entry.getValue(), "UTF-8");
                AppDevUtil.gitAdd(git, entry.getKey());
            }
        }

        // process pending files to delete
        if (pendingFilesToDelete != null) {
            for (String path : pendingFilesToDelete) {
                File file = new File(workingDir, path);
                file.delete();
                AppDevUtil.gitRemove(git, path);
            }
        }

        // process pending dirs to delete
        if (pendingDirsToDelete != null && pendingDirsToDelete.contains("*")) {
            final String[] dirs = new String[] { "forms", "lists", "userviews", "plugins", "builder", "resources" };
            for (String dir : dirs) {
                File tempDir = new File(workingDir, dir);
                FileUtils.deleteDirectory(tempDir);  
            }
            Collection<File> files = FileUtils.listFiles(workingDir, new String[]{"json", "xml", "xpdl", "jar"}, true);
            for (File file : files) {
                file.delete();
            }
            List<String> deletedPaths = AppDevUtil.gitDeletedDiff(git, null);
            for (String path: deletedPaths) {
                AppDevUtil.gitRemove(git, path);
            }
        }
    }
    
    /**
     * Process pending changes and, when there are committable changes, sync plugins/resources
     * and perform a pull-and-commit against the temporary working directory. The actual push to
     * the remote repository is handled separately by the caller.
     *
     * @return {@code true} if a commit was performed
     */
    public boolean commit() throws Exception {
        // process pending files and dirs
        processPending();

        // perform commit
        String message = getCommitMessage();
        if (hasChanges() && message != null && !message.trim().isEmpty()) {
            // sync plugins
            if (isSyncPlugins()) {
                AppDevUtil.syncAppPlugins(appDefinition);
            }

            // sync resources
            if (isSyncResources()) {
                AppDevUtil.syncAppResources(appDefinition);
            }

            AppDevUtil.gitPullAndCommit(appDefinition, git, workingDir, message);
            setCommitted(true);
            return true;
        }
        return false;
    }

    public void clean() {
        if (localGit != null) {
            localGit.getRepository().close();
        }
        if (git != null) {
            git.getRepository().close();
            if (workingDir.exists()) {
                if (!deleteWorkingDir()) {
                    // Retry once after a short delay in case files were locked
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    if (!deleteWorkingDir()) {
                        LogUtil.warn(GitCommitHelper.class.getName(), "Failed to clean git working directory after retry: " + workingDir.getAbsolutePath());
                    }
                }
            }
        }
        if (committed && appDefinition != null) {
            String baseDir = AppDevUtil.getAppDevBaseDirectory();
            String projectDirName = AppDevUtil.getAppGitDirectory(appDefinition);
            File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
            AppDevUtil.maybeGcAfterCommit(projectDir);
        }
    }

    private boolean deleteWorkingDir() {
        try {
            Files.walkFileTree(workingDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isWritable(file)) {
                        //When you try to delete the file on Windows and it is marked as read-only
                        //it would fail unless this change
                        file.toFile().setWritable(true);
                    }

                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Skip files that no longer exist (e.g. deleted by another cluster node)
                    if (exc instanceof java.nio.file.NoSuchFileException) {
                        return FileVisitResult.CONTINUE;
                    }
                    throw exc;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                    } catch (java.nio.file.NoSuchFileException e) {
                        // Already deleted â ignore
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (Exception e) {
            LogUtil.debug(GitCommitHelper.class.getName(), "Failed to delete working directory " + workingDir.getAbsolutePath() + ": " + e.getMessage());
            return false;
        }
    }
}
