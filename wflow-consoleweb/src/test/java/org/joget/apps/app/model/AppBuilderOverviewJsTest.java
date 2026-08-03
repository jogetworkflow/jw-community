package org.joget.apps.app.model;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class AppBuilderOverviewJsTest {

    @Test
    public void testMoreDetailsInitializesEditorAndRendersContent() throws Exception {
        String source = readAppBuilderJs();

        Assert.assertTrue("More Details should initialize CodeMirror 6 from the popup iframe instance.",
                source.contains("codeEditorField = frameWindow.initCM6Editor($codeDetail[0], content, \"html\", isDark);"));
        Assert.assertFalse("More Details should not depend on the legacy CodeMirror global.",
                source.contains("contentWindow.CodeMirror"));
        Assert.assertTrue("More Details should render the selected overview content.",
                source.contains("initCM6Editor($codeDetail[0], content"));
        Assert.assertTrue("More Details should remain read-only.",
                source.contains("frameWindow.cm.EditorState.readOnly.of(true)"));
        Assert.assertFalse("More Details should remain focusable so CodeMirror search works.",
                source.contains("frameWindow.cm.EditorView.editable.of(false)"));
    }

    @Test
    public void testMoreDetailsRefreshesEditorAfterPopupIsVisibleAndSized() throws Exception {
        String source = readAppBuilderJs();
        int showPopupIndex = source.indexOf("JPopup.dialogboxes[\"overview_data_more_detail\"].show();");
        int adjustPopupIndex = source.indexOf("UI.adjustPopUpDialog(JPopup.dialogboxes[\"overview_data_more_detail\"]);", showPopupIndex);
        int refreshEditorIndex = source.indexOf("codeEditorField.refresh();", adjustPopupIndex);

        Assert.assertTrue("More Details should show the popup before refreshing CodeMirror.",
                showPopupIndex >= 0);
        Assert.assertTrue("More Details should size the visible popup before refreshing CodeMirror.",
                adjustPopupIndex > showPopupIndex);
        Assert.assertTrue("More Details should refresh CodeMirror after the popup is visible and sized.",
                refreshEditorIndex > adjustPopupIndex);
    }

    private String readAppBuilderJs() throws Exception {
        Path path = Paths.get("src/main/webapp/js/abuilder.core.js");
        if (!Files.exists(path)) {
            path = Paths.get("wflow-consoleweb/src/main/webapp/js/abuilder.core.js");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
