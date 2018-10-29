package org.joget.apps.app.dao;

import java.util.Objects;
import org.eclipse.jgit.api.Git;
import org.joget.apps.app.model.AppDefinition;

public class GitCommitHelper {

    AppDefinition appDefinition;
    Git git;
    String commitMessage;
    boolean syncPlugins;
    boolean syncResources;

    public GitCommitHelper() {
        commitMessage = "";
    }
    
    public AppDefinition getAppDefinition() {
        return appDefinition;
    }

    public void setAppDefinition(AppDefinition appDefinition) {
        this.appDefinition = appDefinition;
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
    
}
