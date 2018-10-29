package org.joget.apps.app.web;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppDevUtil;
import org.joget.apps.app.dao.GitCommitHelper;
import static org.joget.apps.app.service.AppDevUtil.ATTRIBUTE_GIT_COMMIT_REQUEST;
import static org.joget.apps.app.service.AppDevUtil.ATTRIBUTE_GIT_PULL_REQUEST;
import static org.joget.apps.app.service.AppDevUtil.getGitBranchName;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import static org.joget.apps.app.service.AppDevUtil.getAppDevProperties;

@WebFilter(urlPatterns="/web/*")
public class GitRequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                                

        chain.doFilter(request, response);	        
        
        // commit
        Collection<AppDefinition> pushAppDefs = new LinkedHashSet<>();
        Map<String, GitCommitHelper> gitCommitMap = (Map<String, GitCommitHelper>)WorkflowUtil.getHttpServletRequest().getAttribute(ATTRIBUTE_GIT_COMMIT_REQUEST);
        if (gitCommitMap != null && !gitCommitMap.isEmpty()) {
            try {
                for (String appId: gitCommitMap.keySet()) {
                    GitCommitHelper gitCommitHelper = gitCommitMap.get(appId);
                    Git git = gitCommitHelper.getGit();
                    AppDefinition appDef = gitCommitHelper.getAppDefinition();
                    
                    // sync plugins
                    if (gitCommitHelper.isSyncPlugins()) {
                        AppDevUtil.syncAppPlugins(appDef);
                    }

                    // sync resources
                    if (gitCommitHelper.isSyncResources()) {
                        AppDevUtil.syncAppResources(appDef);
                    }
                    
                    // perform commit
                    String commitMessage = gitCommitHelper.getCommitMessage();
                    if (commitMessage != null && !commitMessage.trim().isEmpty()) {
                        AppDevUtil.gitCommit(git, commitMessage);
                        pushAppDefs.add(appDef);
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, ex.getMessage());
            }
        }
        
        // push
        if (pushAppDefs != null && !pushAppDefs.isEmpty()) {
            for (AppDefinition appDef: pushAppDefs) {
                String baseDir = AppDevUtil.getAppDevBaseDirectory();
                String projectDirName = AppDevUtil.getAppGitDirectory(appDef);
                File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
                String gitBranch = getGitBranchName(appDef);
                Properties gitProperties = getAppDevProperties(appDef);
                String gitUri = gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_URI);
                String gitUsername = gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_USERNAME);
                String gitPassword = gitProperties.getProperty(AppDevUtil.PROPERTY_GIT_PASSWORD);
                try {
                    Git git = AppDevUtil.gitInit(projectDir);
                    if (gitUri != null && !gitUri.trim().isEmpty()) {
                        boolean gitPullRequestDone = "true".equals(WorkflowUtil.getHttpServletRequest().getAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appDef.getAppId()));
                        if (!gitPullRequestDone) {
                            try {
                                AppDevUtil.gitAddRemote(git, gitUri);
                                AppDevUtil.gitPull(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, MergeStrategy.RECURSIVE);
                            } catch(RefNotAdvertisedException re) {
                                // ignore
                            }
                            WorkflowUtil.getHttpServletRequest().setAttribute(ATTRIBUTE_GIT_PULL_REQUEST + appDef.getAppId(), "true");
                        }
                        AppDevUtil.gitPush(git, gitUri, gitUsername, gitPassword);
                    }
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, ex.getMessage());
                }
            }
        }

    }

    @Override
    public void destroy() {
    }
    
}
