package org.joget.apps.app.dao;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.LogUtil;

public class GitCommitHelper {

    File workingDir;
    AppDefinition appDefinition;
    Git localGit;
    Git git;
    String commitMessage;
    boolean syncPlugins;
    boolean syncResources;
    Map<String, String> changesContents = new HashMap<String, String>();

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

    public void addCommitMessage(String newCommitMessage) {
        String messageToAppend = newCommitMessage + ". \n";
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

    public String getChangesContent(String path) {
        if (path.contains("forms" + File.separator)) {
            path = path.substring(path.indexOf("forms" + File.separator));
        } else if (path.contains("lists" + File.separator)) {
            path = path.substring(path.indexOf("lists" + File.separator));
        } else if (path.contains("userviews" + File.separator)) {
            path = path.substring(path.indexOf("userviews" + File.separator));
        } else if (path.contains("builder" + File.separator)) {
            path = path.substring(path.indexOf("builder" + File.separator));
        } else {
            path = path.substring(path.lastIndexOf(File.separator)+1);
        }
        return changesContents.get(path);
    }
    
    public void addChangesContent(String path, String content) {
        this.changesContents.put(path, content);
    }
    
    public void clean() {
        changesContents.clear();
        git.getRepository().close();
        if (workingDir.exists()) {
            try {
                FileUtils.deleteDirectory(workingDir);
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
