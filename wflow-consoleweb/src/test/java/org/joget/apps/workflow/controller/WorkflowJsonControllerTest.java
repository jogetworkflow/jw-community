package org.joget.apps.workflow.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Direct unit coverage for
 * {@link WorkflowJsonController#extractContentDispositionFilename(String)}, which replaced a
 * naive {@code indexOf("filename=") + 9} substring used by {@code installMarketplaceApp(..)} to
 * parse a remote server's {@code Content-Disposition} response header before calling {@code
 * pluginManager.upload(filename, in)}. See that method's own javadoc for the two bugs this fixes.
 */
public class WorkflowJsonControllerTest {

    @Test
    public void extractsUnquotedFilenameWithNoTrailingParameters() {
        assertEquals("plugin.jar", WorkflowJsonController.extractContentDispositionFilename(
                "attachment; filename=plugin.jar"));
    }

    @Test
    public void extractsUnquotedFilenameWithTrailingParameters() {
        assertEquals("plugin.jar", WorkflowJsonController.extractContentDispositionFilename(
                "attachment; filename=plugin.jar; size=1234"));
    }

    /**
     * The standard, RFC 6266 quoted form -- a real marketplace/CDN server is entirely likely to
     * send exactly this. The pre-fix naive substring produced {@code "\"plugin.jar\""} (quotes
     * included), which never satisfies {@code filename.endsWith(".jar")} in the caller, silently
     * routing a plugin jar into the app-import branch instead.
     */
    @Test
    public void extractsQuotedFilenameWithTrailingParameters() {
        assertEquals("plugin.jar", WorkflowJsonController.extractContentDispositionFilename(
                "attachment; filename=\"plugin.jar\"; size=1234"));
    }

    @Test
    public void extractsQuotedFilenameWithNoTrailingParameters() {
        assertEquals("plugin.jar", WorkflowJsonController.extractContentDispositionFilename(
                "attachment; filename=\"plugin.jar\""));
    }

    @Test
    public void extractsFilenameContainingASubdirectorySegmentUnmodified() {
        // Not itself re-validated here -- PluginManager.upload(..)'s own
        // SecurityUtil.normalizedFileName(..) and ancestor-containment check are the actual
        // security boundary for this shape (see this method's own javadoc); this test only proves
        // the parser hands that check the real, unmangled value to evaluate.
        assertEquals("subdir/plugin.jar", WorkflowJsonController.extractContentDispositionFilename(
                "attachment; filename=subdir/plugin.jar"));
    }

    /**
     * Pre-fix, this threw {@code StringIndexOutOfBoundsException}: {@code indexOf("filename=")}
     * returns -1 when absent, and {@code substring(-1 + 9)} on a header shorter than 8 characters
     * (e.g. {@code "inline"}, length 6) is out of bounds. A legitimate response with no filename
     * parameter at all must resolve to "", not crash the request.
     */
    @Test
    public void returnsEmptyStringWhenNoFilenameParameterIsPresent() {
        assertEquals("", WorkflowJsonController.extractContentDispositionFilename("inline"));
    }

    @Test
    public void returnsEmptyStringForNullHeaderValue() {
        assertEquals("", WorkflowJsonController.extractContentDispositionFilename(null));
    }

    @Test
    public void returnsEmptyStringForEmptyHeaderValue() {
        assertEquals("", WorkflowJsonController.extractContentDispositionFilename(""));
    }
}
