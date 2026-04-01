package org.joget.apps.app.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.URIish;
import org.joget.apps.app.dao.GitCommitHelper;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.User;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for git-related methods in AppDevUtil.
 * Uses real JGit repositories in temporary directories â no Spring context needed.
 */
public class AppDevUtilGitTest {

    private File tempDir;
    private Git git;

    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir("appdevutil-git-test");
        git = Git.init().setDirectory(tempDir).call();
        // Create an initial commit so HEAD exists
        File readme = new File(tempDir, "README.md");
        FileUtils.writeStringToFile(readme, "init", "UTF-8");
        git.add().addFilepattern("README.md").call();
        git.commit().setAuthor("test", "test@test.com").setMessage("initial commit").call();
    }

    @After
    public void tearDown() throws Exception {
        if (git != null) {
            git.close();
        }
        if (tempDir != null && tempDir.exists()) {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    // ---- gitInit ----

    @Test
    public void testGitInit_createsRepository() throws Exception {
        File initDir = createTempDir("git-init-test");
        try {
            Git initGit = AppDevUtil.gitInit(initDir);
            assertNotNull(initGit);
            assertTrue(new File(initDir, ".git").exists());
            initGit.close();
        } finally {
            FileUtils.deleteDirectory(initDir);
        }
    }

    @Test
    public void testGitInit_existingRepo_returnsGit() throws Exception {
        // Calling gitInit on an already-initialized directory should succeed
        Git secondGit = AppDevUtil.gitInit(tempDir);
        assertNotNull(secondGit);
        secondGit.close();
    }

    // ---- gitAddRemote / gitRemoteList ----

    @Test
    public void testGitAddRemote_addsOrigin() throws Exception {
        String uri = "https://example.com/repo.git";
        AppDevUtil.gitAddRemote(git, uri);

        // Verify remote was added
        List<org.eclipse.jgit.transport.RemoteConfig> remotes = git.remoteList().call();
        assertEquals(1, remotes.size());
        assertEquals("origin", remotes.get(0).getName());
        assertEquals(uri, remotes.get(0).getURIs().get(0).toString());
    }

    @Test
    public void testGitAddRemote_sameUri_noChange() throws Exception {
        String uri = "https://example.com/repo.git";
        AppDevUtil.gitAddRemote(git, uri);
        // Call again with same URI â should be a no-op
        AppDevUtil.gitAddRemote(git, uri);

        List<org.eclipse.jgit.transport.RemoteConfig> remotes = git.remoteList().call();
        assertEquals(1, remotes.size());
        assertEquals(uri, remotes.get(0).getURIs().get(0).toString());
    }

    @Test
    public void testGitAddRemote_differentUri_replacesOrigin() throws Exception {
        String uri1 = "https://example.com/repo1.git";
        String uri2 = "https://example.com/repo2.git";
        AppDevUtil.gitAddRemote(git, uri1);
        AppDevUtil.gitAddRemote(git, uri2);

        List<org.eclipse.jgit.transport.RemoteConfig> remotes = git.remoteList().call();
        assertEquals(1, remotes.size());
        assertEquals(uri2, remotes.get(0).getURIs().get(0).toString());
    }

    @Test
    public void testGitAddRemote_emptyUri_removesOrigin() throws Exception {
        String uri = "https://example.com/repo.git";
        AppDevUtil.gitAddRemote(git, uri);
        AppDevUtil.gitAddRemote(git, "");

        List<org.eclipse.jgit.transport.RemoteConfig> remotes = git.remoteList().call();
        // Either no remotes or origin has no URIs
        for (org.eclipse.jgit.transport.RemoteConfig r : remotes) {
            if ("origin".equals(r.getName())) {
                fail("Origin should have been removed for empty URI");
            }
        }
    }

    // ---- gitBranches ----

    @Test
    public void testGitBranches_defaultBranch() throws Exception {
        List<String> branches = AppDevUtil.gitBranches(git);
        assertNotNull(branches);
        assertFalse(branches.isEmpty());
        // Default branch should be master (or main depending on git config)
        assertTrue(branches.contains("master") || branches.contains("main"));
    }

    @Test
    public void testGitBranches_multipleBranches() throws Exception {
        git.branchCreate().setName("develop").call();
        git.branchCreate().setName("feature-x").call();

        List<String> branches = AppDevUtil.gitBranches(git);
        assertTrue(branches.contains("develop"));
        assertTrue(branches.contains("feature-x"));
    }

    // ---- gitCheckout ----

    @Test
    public void testGitCheckout_createAndSwitchBranch() throws Exception {
        git.branchCreate().setName("new-branch").call();
        AppDevUtil.gitCheckout(git, "new-branch");
        assertEquals("new-branch", git.getRepository().getBranch());
    }

    @Test
    public void testGitCheckout_alreadyOnBranch_noop() throws Exception {
        String currentBranch = git.getRepository().getBranch();
        AppDevUtil.gitCheckout(git, currentBranch);
        assertEquals(currentBranch, git.getRepository().getBranch());
    }

    // ---- gitRenameBranch ----

    @Test
    public void testGitRenameBranch() throws Exception {
        AppDevUtil.gitRenameBranch(git, "renamed-branch");
        assertEquals("renamed-branch", git.getRepository().getBranch());
    }

    // ---- gitAdd ----

    @Test
    public void testGitAdd_stagesFile() throws Exception {
        File newFile = new File(tempDir, "test.json");
        FileUtils.writeStringToFile(newFile, "{\"key\":\"value\"}", "UTF-8");

        AppDevUtil.gitAdd(git, "test.json");

        // Verify file is staged (appears in diff --cached)
        List<org.eclipse.jgit.diff.DiffEntry> staged = git.diff().setCached(true).call();
        boolean found = false;
        for (org.eclipse.jgit.diff.DiffEntry entry : staged) {
            if ("test.json".equals(entry.getNewPath())) {
                found = true;
                break;
            }
        }
        assertTrue("File should be staged after gitAdd", found);
    }

    // ---- gitRemove ----

    @Test
    public void testGitRemove_removesTrackedFile() throws Exception {
        // README.md is already committed
        AppDevUtil.gitRemove(git, "README.md");

        // Verify file is staged for removal
        List<org.eclipse.jgit.diff.DiffEntry> staged = git.diff().setCached(true).call();
        boolean removed = false;
        for (org.eclipse.jgit.diff.DiffEntry entry : staged) {
            if (entry.getChangeType() == org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE
                    && "README.md".equals(entry.getOldPath())) {
                removed = true;
                break;
            }
        }
        assertTrue("File should be staged for removal", removed);
    }

    // ---- gitDiff ----

    @Test
    public void testGitDiff_detectsUnstagedChanges() throws Exception {
        // Modify an existing tracked file (unstaged change)
        FileUtils.writeStringToFile(new File(tempDir, "README.md"), "changed content", "UTF-8");

        List<String> paths = AppDevUtil.gitDiff(git, null);
        assertTrue("Should detect changed README.md", paths.contains("README.md"));
    }

    @Test
    public void testGitDiff_filtersExtensions() throws Exception {
        // Add and commit a .json file, then modify it
        File jsonFile = new File(tempDir, "data.json");
        FileUtils.writeStringToFile(jsonFile, "{}", "UTF-8");
        git.add().addFilepattern("data.json").call();
        git.commit().setAuthor("test", "test@test.com").setMessage("add json").call();
        FileUtils.writeStringToFile(jsonFile, "{\"updated\": true}", "UTF-8");

        // Also modify README.md
        FileUtils.writeStringToFile(new File(tempDir, "README.md"), "changed", "UTF-8");

        List<String> jsonOnly = AppDevUtil.gitDiff(git, new String[]{"json"});
        assertTrue("Should include .json file", jsonOnly.contains("data.json"));
        assertFalse("Should exclude .md file", jsonOnly.contains("README.md"));
    }

    @Test
    public void testGitDiff_noChanges_returnsEmpty() throws Exception {
        List<String> paths = AppDevUtil.gitDiff(git, null);
        assertTrue("No changes should yield empty list", paths.isEmpty());
    }

    // ---- gitFileDiff ----

    @Test
    public void testGitFileDiff_changedFile_returnsTrue() throws Exception {
        FileUtils.writeStringToFile(new File(tempDir, "README.md"), "changed", "UTF-8");
        assertTrue(AppDevUtil.gitFileDiff(git, "README.md"));
    }

    @Test
    public void testGitFileDiff_unchangedFile_returnsFalse() throws Exception {
        assertFalse(AppDevUtil.gitFileDiff(git, "README.md"));
    }

    @Test
    public void testGitFileDiff_nonexistentFile_returnsFalse() throws Exception {
        assertFalse(AppDevUtil.gitFileDiff(git, "nonexistent.txt"));
    }

    // ---- gitDeletedDiff ----

    @Test
    public void testGitDeletedDiff_detectsDeletedFiles() throws Exception {
        // Delete a tracked file without staging
        new File(tempDir, "README.md").delete();

        List<String> deleted = AppDevUtil.gitDeletedDiff(git, null);
        assertTrue("Should detect deleted README.md", deleted.contains("README.md"));
    }

    @Test
    public void testGitDeletedDiff_filtersExtensions() throws Exception {
        // Add and commit a .json file
        File jsonFile = new File(tempDir, "data.json");
        FileUtils.writeStringToFile(jsonFile, "{}", "UTF-8");
        git.add().addFilepattern("data.json").call();
        git.commit().setAuthor("test", "test@test.com").setMessage("add json").call();

        // Delete both files
        new File(tempDir, "README.md").delete();
        jsonFile.delete();

        List<String> deletedJson = AppDevUtil.gitDeletedDiff(git, new String[]{"json"});
        assertTrue("Should include deleted .json", deletedJson.contains("data.json"));
        assertFalse("Should exclude deleted .md", deletedJson.contains("README.md"));
    }

    @Test
    public void testGitDeletedDiff_noDeletes_returnsEmpty() throws Exception {
        List<String> deleted = AppDevUtil.gitDeletedDiff(git, null);
        assertTrue("No deletions should yield empty list", deleted.isEmpty());
    }

    // ---- gitLog ----

    @Test
    public void testGitLog_doesNotThrow() throws Exception {
        // gitLog only logs to debug output; verify it doesn't throw
        AppDevUtil.gitLog(git, 0, 10);
    }

    @Test
    public void testGitLog_multipleCommits() throws Exception {
        // Add more commits
        for (int i = 0; i < 3; i++) {
            File f = new File(tempDir, "file" + i + ".txt");
            FileUtils.writeStringToFile(f, "content" + i, "UTF-8");
            git.add().addFilepattern(f.getName()).call();
            git.commit().setAuthor("test", "test@test.com").setMessage("commit " + i).call();
        }
        // Should not throw even with skip and count
        AppDevUtil.gitLog(git, 1, 2);
    }

    // ---- fileMergeOurs ----

    @Test
    public void testFileMergeOurs_keepsOurSide() throws Exception {
        String conflictContent =
                "line1\n" +
                "<<<<<<< HEAD\n" +
                "our content\n" +
                "=======\n" +
                "their content\n" +
                ">>>>>>> branch\n" +
                "line2";
        File conflictFile = new File(tempDir, "test.json");
        FileUtils.writeStringToFile(conflictFile, conflictContent, "UTF-8");

        AppDevUtil.fileMergeOurs(tempDir, "test.json");

        String result = FileUtils.readFileToString(conflictFile, "UTF-8");
        assertTrue("Should contain our content", result.contains("our content"));
        assertFalse("Should not contain their content", result.contains("their content"));
        assertFalse("Should not contain conflict markers", result.contains("<<<<<<<"));
        assertFalse("Should not contain conflict markers", result.contains(">>>>>>>"));
        assertFalse("Should not contain separator", result.contains("======="));
    }

    @Test
    public void testFileMergeOurs_multipleConflicts() throws Exception {
        String conflictContent =
                "before\n" +
                "<<<<<<< HEAD\n" +
                "ours1\n" +
                "=======\n" +
                "theirs1\n" +
                ">>>>>>> branch\n" +
                "middle\n" +
                "<<<<<<< HEAD\n" +
                "ours2\n" +
                "=======\n" +
                "theirs2\n" +
                ">>>>>>> branch\n" +
                "after";
        File conflictFile = new File(tempDir, "multi.json");
        FileUtils.writeStringToFile(conflictFile, conflictContent, "UTF-8");

        AppDevUtil.fileMergeOurs(tempDir, "multi.json");

        String result = FileUtils.readFileToString(conflictFile, "UTF-8");
        assertTrue(result.contains("ours1"));
        assertTrue(result.contains("ours2"));
        assertFalse(result.contains("theirs1"));
        assertFalse(result.contains("theirs2"));
        assertTrue(result.contains("before"));
        assertTrue(result.contains("middle"));
        assertTrue(result.contains("after"));
    }

    @Test
    public void testFileMergeOurs_appDefinitionXml_packageDefinitionList_keepsTheirs() throws Exception {
        // Special case: appDefinition.xml with <packageDefinitionList/> on our side should keep theirs
        String conflictContent =
                "<?xml version=\"1.0\"?>\n" +
                "<<<<<<< HEAD\n" +
                "<packageDefinitionList/>\n" +
                "=======\n" +
                "<packageDefinitionList><item>real data</item></packageDefinitionList>\n" +
                ">>>>>>> branch\n" +
                "</app>";
        File conflictFile = new File(tempDir, "appDefinition.xml");
        FileUtils.writeStringToFile(conflictFile, conflictContent, "UTF-8");

        AppDevUtil.fileMergeOurs(tempDir, "appDefinition.xml");

        String result = FileUtils.readFileToString(conflictFile, "UTF-8");
        assertTrue("Should keep theirs for packageDefinitionList placeholder",
                result.contains("<packageDefinitionList><item>real data</item></packageDefinitionList>"));
        assertFalse("Should not keep empty packageDefinitionList",
                result.contains("<packageDefinitionList/>"));
    }

    @Test
    public void testFileMergeOurs_noConflicts_unchanged() throws Exception {
        String content = "normal file content\nno conflicts here";
        File file = new File(tempDir, "clean.json");
        FileUtils.writeStringToFile(file, content, "UTF-8");

        AppDevUtil.fileMergeOurs(tempDir, "clean.json");

        String result = FileUtils.readFileToString(file, "UTF-8");
        // Content should be preserved (may use \r\n line endings from the method)
        assertTrue(result.contains("normal file content"));
        assertTrue(result.contains("no conflicts here"));
    }

    // ---- dirSetup ----

    @Test
    public void testDirSetup_createsDirectory() throws Exception {
        File baseDir = createTempDir("dir-setup-base");
        try {
            File result = AppDevUtil.dirSetup(baseDir.getAbsolutePath(), "myProject");
            assertTrue(result.exists());
            assertTrue(result.isDirectory());
            assertEquals("myProject", result.getName());
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    @Test
    public void testDirSetup_existingDirectory_returnsIt() throws Exception {
        File baseDir = createTempDir("dir-setup-base2");
        try {
            File result1 = AppDevUtil.dirSetup(baseDir.getAbsolutePath(), "existing");
            File result2 = AppDevUtil.dirSetup(baseDir.getAbsolutePath(), "existing");
            assertEquals(result1.getAbsolutePath(), result2.getAbsolutePath());
            assertTrue(result2.exists());
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    // ---- cleanGitTempDirectories ----

    @Test
    public void testCleanGitTempDirectories_removesStaleTempDirs() throws Exception {
        File baseDir = createTempDir("clean-test-base");
        try {
            // Simulate app_src/{appId}/ structure
            File appIdDir = new File(baseDir, "myApp");
            appIdDir.mkdirs();

            // Permanent directory: myApp_1 (version suffix)
            File permanentDir = new File(appIdDir, "myApp_1");
            permanentDir.mkdirs();

            // Temp directory: some random suffix (stale)
            File tempGitDir = new File(appIdDir, "myApp_abc123");
            tempGitDir.mkdirs();
            // Make it old enough to be cleaned
            tempGitDir.setLastModified(System.currentTimeMillis() - AppDevUtil.GIT_TEMP_EXPIRES_MS - 1000);

            AppDevUtil.cleanGitTempDirectories(baseDir.getAbsolutePath());

            assertTrue("Permanent dir should remain", permanentDir.exists());
            assertFalse("Stale temp dir should be deleted", tempGitDir.exists());
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    @Test
    public void testCleanGitTempDirectories_keepsRecentTempDirs() throws Exception {
        File baseDir = createTempDir("clean-test-recent");
        try {
            File appIdDir = new File(baseDir, "myApp");
            appIdDir.mkdirs();

            // Recent temp directory â should NOT be cleaned
            File recentTempDir = new File(appIdDir, "myApp_tempXYZ");
            recentTempDir.mkdirs();
            // Last modified is now (recent)

            AppDevUtil.cleanGitTempDirectories(baseDir.getAbsolutePath());

            assertTrue("Recent temp dir should NOT be deleted", recentTempDir.exists());
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    @Test
    public void testCleanGitTempDirectories_nonexistentDir_noop() {
        // Should not throw
        AppDevUtil.cleanGitTempDirectories("/nonexistent/path/that/does/not/exist");
    }

    @Test
    public void testCleanGitTempDirectories_emptyAppDir_noop() throws Exception {
        File baseDir = createTempDir("clean-test-empty");
        try {
            // No app directories inside â should just return without error
            AppDevUtil.cleanGitTempDirectories(baseDir.getAbsolutePath());
        } finally {
            FileUtils.deleteDirectory(baseDir);
        }
    }

    // ---- formatJson ----

    @Test
    public void testFormatJson_prettyPrints() {
        String compact = "{\"name\":\"test\",\"value\":123}";
        String result = AppDevUtil.formatJson(compact);
        assertNotNull(result);
        // Pretty-printed JSON should contain newlines and indentation
        assertTrue("Should contain newlines", result.contains("\n"));
        assertTrue("Should contain key", result.contains("\"name\""));
        assertTrue("Should contain value", result.contains("\"test\""));
    }

    @Test
    public void testFormatJson_invalidJson_returnsOriginal() {
        String invalid = "not valid json {{{";
        String result = AppDevUtil.formatJson(invalid);
        assertEquals("Invalid JSON should be returned as-is", invalid, result);
    }

    @Test
    public void testFormatJson_null_returnsNull() {
        assertNull(AppDevUtil.formatJson(null));
    }

    @Test
    public void testFormatJson_empty_returnsEmpty() {
        assertEquals("", AppDevUtil.formatJson(""));
    }

    // ---- Integration: add + diff + commit flow ----

    @Test
    public void testGitWorkflow_addDiffCommit() throws Exception {
        // Create a new file
        File newFile = new File(tempDir, "form.json");
        FileUtils.writeStringToFile(newFile, "{\"id\": \"form1\"}", "UTF-8");

        // Add to staging
        AppDevUtil.gitAdd(git, "form.json");

        // Commit directly via JGit (gitCommit needs Spring context)
        git.commit().setAuthor("test", "test@test.com").setMessage("add form").call();

        // Modify the file
        FileUtils.writeStringToFile(newFile, "{\"id\": \"form1\", \"updated\": true}", "UTF-8");

        // Should show up in diff
        List<String> diff = AppDevUtil.gitDiff(git, new String[]{"json"});
        assertTrue("Modified json should appear in diff", diff.contains("form.json"));
        assertTrue("gitFileDiff should detect change", AppDevUtil.gitFileDiff(git, "form.json"));

        // Stage and commit the update
        AppDevUtil.gitAdd(git, "form.json");
        git.commit().setAuthor("test", "test@test.com").setMessage("update form").call();

        // Diff should now be empty
        List<String> diffAfter = AppDevUtil.gitDiff(git, null);
        assertTrue("No changes after commit", diffAfter.isEmpty());

        // Verify commit log has our commits
        Iterable<RevCommit> log = git.log().setMaxCount(3).call();
        int count = 0;
        for (@SuppressWarnings("unused") RevCommit ignored : log) {
            count++;
        }
        assertEquals("Should have 3 commits (initial + add + update)", 3, count);
    }

    // ---- gitPullLocal ----

    @Test
    public void testGitPullLocal_pullsChangesFromLocalRemote() throws Exception {
        // Set up a "source" repo that acts as the local bare repo
        File sourceDir = createTempDir("source-repo");
        Git sourceGit = Git.init().setDirectory(sourceDir).call();
        String branchName = "testApp_1";

        // Create initial commit and rename branch to match what getGitBranchName returns
        File sourceFile = new File(sourceDir, "data.json");
        FileUtils.writeStringToFile(sourceFile, "{\"v\":1}", "UTF-8");
        sourceGit.add().addFilepattern("data.json").call();
        sourceGit.commit().setAuthor("test", "test@test.com").setMessage("init source").call();
        sourceGit.branchRename().setNewName(branchName).call();

        // Set up working repo with same branch
        File workDir = createTempDir("work-repo");
        Git workGit = Git.init().setDirectory(workDir).call();
        File workReadme = new File(workDir, "README.md");
        FileUtils.writeStringToFile(workReadme, "init", "UTF-8");
        workGit.add().addFilepattern("README.md").call();
        workGit.commit().setAuthor("test", "test@test.com").setMessage("init work").call();
        workGit.branchRename().setNewName(branchName).call();

        // Add source as "local" remote
        workGit.remoteAdd().setName("local").setUri(new URIish(sourceDir.getAbsolutePath())).call();

        // Create AppDefinition: getGitBranchName returns SecurityUtil.normalizedFileName("testApp_1") = "testApp_1"
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        // gitPullLocal catches all exceptions internally, so verify by checking the file
        AppDevUtil.gitPullLocal(appDef, workGit, workDir);

        assertTrue("data.json should be pulled from local remote", new File(workDir, "data.json").exists());
        String content = FileUtils.readFileToString(new File(workDir, "data.json"), "UTF-8");
        assertEquals("{\"v\":1}", content);

        sourceGit.close();
        workGit.close();
        FileUtils.deleteDirectory(sourceDir);
        FileUtils.deleteDirectory(workDir);
    }

    @Test
    public void testGitPullLocal_noLocalRemote_doesNotThrow() throws Exception {
        // gitPullLocal should catch the exception when "local" remote doesn't exist
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        // No "local" remote configured on this.git â should not throw
        AppDevUtil.gitPullLocal(appDef, git, tempDir);
    }

    // ---- gitPullAndCommit ----

    @Test
    public void testGitPullAndCommit_pullsThenCommits() throws Exception {
        // Set up source repo
        File sourceDir = createTempDir("source-pullcommit");
        Git sourceGit = Git.init().setDirectory(sourceDir).call();
        String branchName = "testApp_1";

        File sourceFile = new File(sourceDir, "pulled.json");
        FileUtils.writeStringToFile(sourceFile, "{}", "UTF-8");
        sourceGit.add().addFilepattern("pulled.json").call();
        sourceGit.commit().setAuthor("test", "test@test.com").setMessage("init source").call();
        sourceGit.branchRename().setNewName(branchName).call();

        // Set up working repo
        File workDir = createTempDir("work-pullcommit");
        Git workGit = Git.init().setDirectory(workDir).call();
        FileUtils.writeStringToFile(new File(workDir, "README.md"), "init", "UTF-8");
        workGit.add().addFilepattern("README.md").call();
        workGit.commit().setAuthor("test", "test@test.com").setMessage("init work").call();
        workGit.branchRename().setNewName(branchName).call();
        workGit.remoteAdd().setName("local").setUri(new URIish(sourceDir.getAbsolutePath())).call();

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        // Add a new file to working dir to be committed
        File newFile = new File(workDir, "form.json");
        FileUtils.writeStringToFile(newFile, "{\"id\":\"f1\"}", "UTF-8");
        workGit.add().addFilepattern("form.json").call();

        // Mock Spring context for gitCommit (needs WorkflowUserManager)
        WorkflowUserManager mockWum = mock(WorkflowUserManager.class);
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        when(mockWum.getCurrentUser()).thenReturn(mockUser);

        ApplicationContext mockAppCtx = mock(ApplicationContext.class);
        when(mockAppCtx.getBean("workflowUserManager")).thenReturn(mockWum);

        // Mock SetupManager so getAppDevBaseDirectory points to a temp location
        File appDevBase = createTempDir("appdev-base");

        try (MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class);
             MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class)) {

            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);
            wfMock.when(WorkflowUtil::getApplicationContext).thenReturn(mockAppCtx);
            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());

            AppDevUtil.gitPullAndCommit(appDef, workGit, workDir, "test commit");
        }

        // Verify pulled file arrived
        assertTrue("pulled.json should exist after pull", new File(workDir, "pulled.json").exists());

        // Verify commit was created with the staged file
        Iterable<RevCommit> log = workGit.log().setMaxCount(1).call();
        RevCommit latestCommit = log.iterator().next();
        assertEquals("test commit", latestCommit.getFullMessage());
        assertEquals("testuser", latestCommit.getAuthorIdent().getName());

        sourceGit.close();
        workGit.close();
        FileUtils.deleteDirectory(sourceDir);
        FileUtils.deleteDirectory(workDir);
        FileUtils.deleteDirectory(appDevBase);
    }

    // ---- fileInitCommit ----

    @Test
    public void testFileInitCommit_createsLocalAndWorkingRepos() throws Exception {
        File appDevBase = createTempDir("fileinit-base");

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("myapp");
        appDef.setVersion(1L);

        try (MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class);
             MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class)) {

            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);

            Map<String, GitCommitHelper> result = AppDevUtil.fileInitCommit(appDef, "init commit");

            assertNotNull("Should return a non-null commit map", result);
            assertTrue("Map should contain entry for app id", result.containsKey("myapp"));

            GitCommitHelper helper = result.get("myapp");
            assertNotNull("GitCommitHelper should not be null", helper);
            
            AppDevUtil.initGitCommitHelper(helper);
            
            assertNotNull("Local git should be set", helper.getLocalGit());
            assertNotNull("Working git should be set", helper.getGit());
            assertNotNull("Working dir should be set", helper.getWorkingDir());
            assertEquals("Commit message should be set", "init commit" + ". \n", helper.getCommitMessage());

            // Verify local repo was created at expected path: appDevBase/app_src/myapp/myapp_1
            File expectedLocalDir = new File(appDevBase, "app_src" + File.separator + "myapp" + File.separator + "myapp_1");
            assertTrue("Local repo directory should exist", expectedLocalDir.exists());
            assertTrue("Local repo should have .git", new File(expectedLocalDir, ".git").exists());

            // Verify working dir was created under appDevBase/app_src/myapp/
            File workingDir = helper.getWorkingDir();
            assertTrue("Working dir should exist", workingDir.exists());
            assertTrue("Working dir should have .git", new File(workingDir, ".git").exists());
            assertTrue("Working dir should be under app_src/myapp/",
                    workingDir.getAbsolutePath().contains("myapp"));

            // Clean up git handles
            helper.getLocalGit().close();
            helper.getGit().close();
        } finally {
            FileUtils.deleteDirectory(appDevBase);
        }
    }

    @Test
    public void testFileInitCommit_withExistingLocalRepo_fetchesContent() throws Exception {
        File appDevBase = createTempDir("fileinit-existing");

        // Pre-create a local repo with content at the expected path
        File localRepoDir = new File(appDevBase, "app_src" + File.separator + "myapp" + File.separator + "myapp_1");
        localRepoDir.mkdirs();
        Git localGit = Git.init().setDirectory(localRepoDir).call();
        FileUtils.writeStringToFile(new File(localRepoDir, "form.json"), "{\"id\":\"f1\"}", "UTF-8");
        localGit.add().addFilepattern("form.json").call();
        localGit.commit().setAuthor("test", "test@test.com").setMessage("pre-existing").call();
        localGit.branchRename().setNewName("myapp_1").call();
        localGit.close();

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("myapp");
        appDef.setVersion(1L);

        try (MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class);
             MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class)) {

            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);

            Map<String, GitCommitHelper> result = AppDevUtil.fileInitCommit(appDef, "fetch test");

            assertNotNull(result);
            GitCommitHelper helper = result.get("myapp");
            assertNotNull(helper);
            
            AppDevUtil.initGitCommitHelper(helper);

            // The working dir should have the file fetched from local repo
            File workingDir = helper.getWorkingDir();
            File fetchedFile = new File(workingDir, "form.json");
            assertTrue("Working dir should have form.json fetched from local repo", fetchedFile.exists());
            assertEquals("{\"id\":\"f1\"}", FileUtils.readFileToString(fetchedFile, "UTF-8"));

            helper.getLocalGit().close();
            helper.getGit().close();
        } finally {
            FileUtils.deleteDirectory(appDevBase);
        }
    }

    @Test
    public void testFileInitCommit_shallowFetch_allFilesPresent_onlyOneCommit() throws Exception {
        File appDevBase = createTempDir("fileinit-shallow");

        // Pre-create a local repo with different files added across multiple commits
        File localRepoDir = new File(appDevBase, "app_src" + File.separator + "myapp" + File.separator + "myapp_1");
        localRepoDir.mkdirs();
        Git localGit = Git.init().setDirectory(localRepoDir).call();

        // Commit 1: add form.json
        FileUtils.writeStringToFile(new File(localRepoDir, "form.json"), "{\"id\":\"form1\"}", "UTF-8");
        localGit.add().addFilepattern("form.json").call();
        localGit.commit().setAuthor("test", "test@test.com").setMessage("add form").call();

        // Commit 2: add list.json
        FileUtils.writeStringToFile(new File(localRepoDir, "list.json"), "{\"id\":\"list1\"}", "UTF-8");
        localGit.add().addFilepattern("list.json").call();
        localGit.commit().setAuthor("test", "test@test.com").setMessage("add list").call();

        // Commit 3: add appDefinition.xml and update form.json
        FileUtils.writeStringToFile(new File(localRepoDir, "appDefinition.xml"), "<app/>", "UTF-8");
        FileUtils.writeStringToFile(new File(localRepoDir, "form.json"), "{\"id\":\"form1\",\"updated\":true}", "UTF-8");
        localGit.add().addFilepattern("appDefinition.xml").call();
        localGit.add().addFilepattern("form.json").call();
        localGit.commit().setAuthor("test", "test@test.com").setMessage("add xml and update form").call();

        // Commit 4: add package.xpdl in a subdirectory
        new File(localRepoDir, "processes").mkdirs();
        FileUtils.writeStringToFile(new File(localRepoDir, "processes" + File.separator + "package.xpdl"), "<xpdl/>", "UTF-8");
        localGit.add().addFilepattern("processes/package.xpdl").call();
        localGit.commit().setAuthor("test", "test@test.com").setMessage("add xpdl").call();

        localGit.branchRename().setNewName("myapp_1").call();
        localGit.close();

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("myapp");
        appDef.setVersion(1L);

        try (MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class);
             MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class)) {

            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);

            Map<String, GitCommitHelper> result = AppDevUtil.fileInitCommit(appDef, "shallow test");

            assertNotNull(result);
            GitCommitHelper helper = result.get("myapp");
            assertNotNull(helper);
            
            AppDevUtil.initGitCommitHelper(helper);

            File workingDir = helper.getWorkingDir();

            // All files from all 4 commits should be present in the working dir
            assertTrue("form.json should exist", new File(workingDir, "form.json").exists());
            assertTrue("list.json should exist", new File(workingDir, "list.json").exists());
            assertTrue("appDefinition.xml should exist", new File(workingDir, "appDefinition.xml").exists());
            assertTrue("processes/package.xpdl should exist",
                    new File(workingDir, "processes" + File.separator + "package.xpdl").exists());

            // Files should have the latest content (form.json was updated in commit 3)
            assertEquals("{\"id\":\"form1\",\"updated\":true}",
                    FileUtils.readFileToString(new File(workingDir, "form.json"), "UTF-8"));
            assertEquals("{\"id\":\"list1\"}",
                    FileUtils.readFileToString(new File(workingDir, "list.json"), "UTF-8"));
            assertEquals("<app/>",
                    FileUtils.readFileToString(new File(workingDir, "appDefinition.xml"), "UTF-8"));
            assertEquals("<xpdl/>",
                    FileUtils.readFileToString(new File(workingDir, "processes" + File.separator + "package.xpdl"), "UTF-8"));

            // Despite all files being present, only 1 commit should exist (shallow fetch depth=1)
            Git workGit = helper.getGit();
            int commitCount = 0;
            for (@SuppressWarnings("unused") RevCommit ignored : workGit.log().call()) {
                commitCount++;
            }
            assertEquals("Shallow fetch should result in only 1 commit in working dir", 1, commitCount);

            helper.getLocalGit().close();
            helper.getGit().close();
        } finally {
            FileUtils.deleteDirectory(appDevBase);
        }
    }

    @Test
    public void testGitPushLocal_shallowFetch_handlesNonFastForwardAndPushes() throws Exception {
        File appDevBase = createTempDir("pushlocal-conflict");

        // Set up local repo with initial content (2 commits)
        File localRepoDir = new File(appDevBase, "app_src" + File.separator + "myapp" + File.separator + "myapp_1");
        localRepoDir.mkdirs();
        Git localGitSetup = Git.init().setDirectory(localRepoDir).call();

        FileUtils.writeStringToFile(new File(localRepoDir, "form.json"), "{\"id\":\"form1\"}", "UTF-8");
        localGitSetup.add().addFilepattern("form.json").call();
        localGitSetup.commit().setAuthor("test", "test@test.com").setMessage("commit A").call();

        FileUtils.writeStringToFile(new File(localRepoDir, "list.json"), "{\"id\":\"list1\"}", "UTF-8");
        localGitSetup.add().addFilepattern("list.json").call();
        localGitSetup.commit().setAuthor("test", "test@test.com").setMessage("commit B").call();

        localGitSetup.branchRename().setNewName("myapp_1").call();
        localGitSetup.close();

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("myapp");
        appDef.setVersion(1L);

        try (MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class);
             MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class)) {

            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());

            // Step 1: fileInitCommit with request=null to get the commit map
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);
            Map<String, GitCommitHelper> commitMap = AppDevUtil.fileInitCommit(appDef, "init");

            assertNotNull(commitMap);
            GitCommitHelper helper = commitMap.get("myapp");
            assertNotNull(helper);
            
            AppDevUtil.initGitCommitHelper(helper);

            File workingDir = helper.getWorkingDir();
            Git workGit = helper.getGit();
            Git localGit = helper.getLocalGit();

            // Verify shallow fetch: working dir has all files but only 1 commit
            assertTrue("form.json should be fetched", new File(workingDir, "form.json").exists());
            assertTrue("list.json should be fetched", new File(workingDir, "list.json").exists());
            int initialCommitCount = 0;
            for (@SuppressWarnings("unused") RevCommit ignored : workGit.log().call()) {
                initialCommitCount++;
            }
            assertEquals("Working dir should have 1 commit (shallow)", 1, initialCommitCount);

            // Step 2: Add a new file in working dir and commit (commit D)
            FileUtils.writeStringToFile(new File(workingDir, "userview.json"), "{\"id\":\"uv1\"}", "UTF-8");
            workGit.add().addFilepattern("userview.json").call();
            workGit.commit().setAuthor("dev1", "dev1@test.com").setMessage("commit D: add userview").call();

            // Step 3: Meanwhile, add a different file directly to local repo (commit E)
            // This creates a divergence: local has BâE, working dir has B'âD
            AppDevUtil.gitCheckout(localGit, "myapp_1");
            FileUtils.writeStringToFile(new File(localRepoDir, "datalist.json"), "{\"id\":\"dl1\"}", "UTF-8");
            localGit.add().addFilepattern("datalist.json").call();
            localGit.commit().setAuthor("dev2", "dev2@test.com").setMessage("commit E: add datalist").call();

            // Step 4: Set up mock request so gitPushLocal proceeds (it returns early if request==null)
            jakarta.servlet.http.HttpServletRequest mockRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
            when(mockRequest.getAttribute(AppDevUtil.ATTRIBUTE_GIT_COMMIT_REQUEST)).thenReturn(commitMap);
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(mockRequest);

            // Step 5: Push from working dir â REJECTED_NONFASTFORWARD â pull â merge â retry push
            AppDevUtil.gitPushLocal(appDef, workGit, workingDir);

            // Step 6: Verify working dir did NOT fetch full history during conflict resolution.
            // Local repo full history: A â B â E (3 commits).
            // Working dir: B' (shallow) â D â pull fetches E â merge commit = 4 commits.
            // If gitPullLocal had fetched full history, commit A would also appear (5+ commits).
            int workingDirCommitCount = 0;
            for (@SuppressWarnings("unused") RevCommit ignored : workGit.log().call()) {
                workingDirCommitCount++;
            }
            assertEquals("Working dir should have 4 commits (shallow B + D + fetched E + merge), not full history",
                    4, workingDirCommitCount);

            // Step 7: Verify local repo has ALL files merged
            // gitPushLocal checks out myapp_1 on local repo at the end
            AppDevUtil.gitCheckout(localGit, "myapp_1");

            assertTrue("Local repo should have form.json (original)",
                    new File(localRepoDir, "form.json").exists());
            assertTrue("Local repo should have list.json (original)",
                    new File(localRepoDir, "list.json").exists());
            assertTrue("Local repo should have userview.json (pushed from working dir)",
                    new File(localRepoDir, "userview.json").exists());
            assertTrue("Local repo should have datalist.json (committed directly to local)",
                    new File(localRepoDir, "datalist.json").exists());

            // Verify content correctness
            assertEquals("{\"id\":\"form1\"}", FileUtils.readFileToString(new File(localRepoDir, "form.json"), "UTF-8"));
            assertEquals("{\"id\":\"uv1\"}", FileUtils.readFileToString(new File(localRepoDir, "userview.json"), "UTF-8"));
            assertEquals("{\"id\":\"dl1\"}", FileUtils.readFileToString(new File(localRepoDir, "datalist.json"), "UTF-8"));

            helper.getLocalGit().close();
            helper.getGit().close();
        } finally {
            FileUtils.deleteDirectory(appDevBase);
        }
    }

    // ---- Shallow Fetch: working dir size smaller than local repo ----

    @Test
    public void testFileInitCommit_shallowFetch_workingDirSmallerThanLocalRepo() throws Exception {
        File appDevBase = createTempDir("fileinit-shallow-size");

        // Pre-create a local repo with many commits to build up git history
        File localRepoDir = new File(appDevBase, "app_src" + File.separator + "myapp" + File.separator + "myapp_1");
        localRepoDir.mkdirs();
        Git localGit = Git.init().setDirectory(localRepoDir).call();

        // Create 20 commits with different files to accumulate git object history
        for (int i = 0; i < 20; i++) {
            FileUtils.writeStringToFile(new File(localRepoDir, "file" + i + ".json"),
                    "{\"id\":\"file" + i + "\",\"data\":\"" + "x".repeat(500) + "\"}", "UTF-8");
            localGit.add().addFilepattern("file" + i + ".json").call();
            localGit.commit().setAuthor("test", "test@test.com").setMessage("commit " + i).call();
        }

        localGit.branchRename().setNewName("myapp_1").call();
        localGit.close();

        long localRepoGitSize = FileUtils.sizeOfDirectory(new File(localRepoDir, ".git"));

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("myapp");
        appDef.setVersion(1L);

        try (MockedStatic<SetupManager> smMock = Mockito.mockStatic(SetupManager.class);
             MockedStatic<WorkflowUtil> wfMock = Mockito.mockStatic(WorkflowUtil.class)) {

            smMock.when(SetupManager::getBaseDirectory).thenReturn(appDevBase.getAbsolutePath());
            wfMock.when(WorkflowUtil::getHttpServletRequest).thenReturn(null);

            Map<String, GitCommitHelper> result = AppDevUtil.fileInitCommit(appDef, "size test");

            assertNotNull(result);
            GitCommitHelper helper = result.get("myapp");
            assertNotNull(helper);

            AppDevUtil.initGitCommitHelper(helper);

            File workingDir = helper.getWorkingDir();

            // All files should be present in the working dir
            for (int i = 0; i < 20; i++) {
                assertTrue("file" + i + ".json should exist", new File(workingDir, "file" + i + ".json").exists());
            }

            // Working dir .git should be smaller than local repo .git due to shallow fetch (depth=1)
            long workingDirGitSize = FileUtils.sizeOfDirectory(new File(workingDir, ".git"));
            assertTrue("Working dir .git (" + workingDirGitSize + " bytes) should be smaller than local repo .git ("
                            + localRepoGitSize + " bytes) due to shallow fetch",
                    workingDirGitSize < localRepoGitSize);

            helper.getLocalGit().close();
            helper.getGit().close();
        } finally {
            FileUtils.deleteDirectory(appDevBase);
        }
    }

    // ---- dirLastModified ----

    @Test
    public void testDirLastModified_returnsLatestDate() throws Exception {
        // Create a temp directory with files of various extensions
        File dirForScan = createTempDir("dir-lastmod");
        File jsonFile = new File(dirForScan, "form.json");
        File xmlFile = new File(dirForScan, "app.xml");
        File txtFile = new File(dirForScan, "notes.txt");

        FileUtils.writeStringToFile(jsonFile, "{}", "UTF-8");
        Thread.sleep(50);
        FileUtils.writeStringToFile(xmlFile, "<app/>", "UTF-8");
        Thread.sleep(50);
        FileUtils.writeStringToFile(txtFile, "ignore me", "UTF-8");

        // xmlFile should be the latest among the tracked extensions (.json, .xml, .xpdl, .jar)
        long expectedMillis = xmlFile.lastModified();

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        try (MockedStatic<DynamicDataSourceManager> ddsmMock = Mockito.mockStatic(DynamicDataSourceManager.class);
             MockedStatic<AppUtil> appUtilMock = Mockito.mockStatic(AppUtil.class);
             MockedStatic<AppDevUtil> appDevUtilMock = Mockito.mockStatic(AppDevUtil.class, Mockito.CALLS_REAL_METHODS)) {

            ddsmMock.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("test");
            appUtilMock.when(() -> AppUtil.getCache(anyString())).thenReturn(null);
            appDevUtilMock.when(() -> AppDevUtil.fileGetFileObject(any(AppDefinition.class), eq("."), eq(false)))
                    .thenReturn(dirForScan);

            Date result = AppDevUtil.dirLastModified(appDef);

            assertNotNull("Should return a date", result);
            // The method truncates milliseconds, so compare at second granularity
            assertEquals("Should match latest tracked file's modification time (seconds)",
                    expectedMillis / 1000, result.getTime() / 1000);
        } finally {
            FileUtils.deleteDirectory(dirForScan);
        }
    }

    @Test
    public void testDirLastModified_ignoresNonTrackedExtensions() throws Exception {
        File dirForScan = createTempDir("dir-lastmod-ext");

        // Only create files with non-tracked extensions
        FileUtils.writeStringToFile(new File(dirForScan, "readme.txt"), "text", "UTF-8");
        FileUtils.writeStringToFile(new File(dirForScan, "style.css"), "body{}", "UTF-8");

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        try (MockedStatic<DynamicDataSourceManager> ddsmMock = Mockito.mockStatic(DynamicDataSourceManager.class);
             MockedStatic<AppUtil> appUtilMock = Mockito.mockStatic(AppUtil.class);
             MockedStatic<AppDevUtil> appDevUtilMock = Mockito.mockStatic(AppDevUtil.class, Mockito.CALLS_REAL_METHODS)) {

            ddsmMock.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("test");
            appUtilMock.when(() -> AppUtil.getCache(anyString())).thenReturn(null);
            appDevUtilMock.when(() -> AppDevUtil.fileGetFileObject(any(AppDefinition.class), eq("."), eq(false)))
                    .thenReturn(dirForScan);

            Date result = AppDevUtil.dirLastModified(appDef);

            assertNull("Should return null when no tracked files exist", result);
        } finally {
            FileUtils.deleteDirectory(dirForScan);
        }
    }

    @Test
    public void testDirLastModified_emptyDirectory_returnsNull() throws Exception {
        File emptyDir = createTempDir("dir-lastmod-empty");

        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        try (MockedStatic<DynamicDataSourceManager> ddsmMock = Mockito.mockStatic(DynamicDataSourceManager.class);
             MockedStatic<AppUtil> appUtilMock = Mockito.mockStatic(AppUtil.class);
             MockedStatic<AppDevUtil> appDevUtilMock = Mockito.mockStatic(AppDevUtil.class, Mockito.CALLS_REAL_METHODS)) {

            ddsmMock.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("test");
            appUtilMock.when(() -> AppUtil.getCache(anyString())).thenReturn(null);
            appDevUtilMock.when(() -> AppDevUtil.fileGetFileObject(any(AppDefinition.class), eq("."), eq(false)))
                    .thenReturn(emptyDir);

            Date result = AppDevUtil.dirLastModified(appDef);

            assertNull("Empty directory should return null", result);
        } finally {
            FileUtils.deleteDirectory(emptyDir);
        }
    }

    @Test
    public void testDirLastModified_nullDirectory_returnsNull() throws Exception {
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        try (MockedStatic<DynamicDataSourceManager> ddsmMock = Mockito.mockStatic(DynamicDataSourceManager.class);
             MockedStatic<AppUtil> appUtilMock = Mockito.mockStatic(AppUtil.class);
             MockedStatic<AppDevUtil> appDevUtilMock = Mockito.mockStatic(AppDevUtil.class, Mockito.CALLS_REAL_METHODS)) {

            ddsmMock.when(DynamicDataSourceManager::getCurrentProfile).thenReturn("test");
            appUtilMock.when(() -> AppUtil.getCache(anyString())).thenReturn(null);
            appDevUtilMock.when(() -> AppDevUtil.fileGetFileObject(any(AppDefinition.class), eq("."), eq(false)))
                    .thenReturn(null);

            Date result = AppDevUtil.dirLastModified(appDef);

            assertNull("Null directory should return null", result);
        } finally {
            // nothing to clean
        }
    }

    // ---- Helper ----

    private File createTempDir(String prefix) throws IOException {
        File dir = File.createTempFile(prefix, "");
        dir.delete();
        dir.mkdirs();
        return dir;
    }
}
