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
import static org.joget.apps.app.service.AppDevUtil.getGitBranchName;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.util.WorkflowUtil;
import static org.joget.apps.app.service.AppDevUtil.getAppDevProperties;
import org.joget.commons.util.PluginThread;

@WebFilter(urlPatterns="/web/*", asyncSupported=true)
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
            
            for (String appId: gitCommitMap.keySet()) {
                GitCommitHelper gitCommitHelper = gitCommitMap.get(appId);
                
                if (gitCommitHelper != null) {
                    try {
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
                            AppDevUtil.gitPullAndCommit(appDef, git, gitCommitHelper.getWorkingDir(), commitMessage);
                            pushAppDefs.add(appDef);
                        }
                    } catch (Exception ex) {
                        LogUtil.error(getClass().getName(), ex, ex.getMessage());
                    } finally {  
                        try {
                            gitCommitHelper.clean();
                        } catch (Exception e) {
                            LogUtil.debug(GitRequestFilter.class.getName(), appId + " - " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        // push
        if (pushAppDefs != null && !pushAppDefs.isEmpty()) {
            final Collection<AppDefinition> threadPushAppDefs = pushAppDefs;
            Thread pushToRemote = new PluginThread(new Runnable() {
                
                public void run() {
                    for (AppDefinition appDef: threadPushAppDefs) {
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
                                
                                try {
                                    AppDevUtil.gitAddRemote(git, gitUri);
                                } catch(RefNotAdvertisedException re) {
                                    // ignore
                                }
                                AppDevUtil.gitPullAndPush(projectDir, git, gitBranch, gitUri, gitUsername, gitPassword, MergeStrategy.RECURSIVE, appDef);
                            }
                        } catch (Exception ex) {
                            LogUtil.error(getClass().getName(), ex, ex.getMessage());
                        }
                    }
                }
            });
            pushToRemote.setDaemon(false);
            pushToRemote.start();
        }
    }

    @Override
    public void destroy() {
    }
    
}
