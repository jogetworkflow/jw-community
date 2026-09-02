package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.joget.plugin.base.PluginProperty;
import org.junit.Test;

/**
 * Regression test for GitLab issue #2271 Issue 7 (NestedDatalistFormatter): the fix does not
 * restrict which action classes may dispatch (a designer-configured action, including a mutating
 * one like Delete, must keep working exactly as configured). Instead it validates that the
 * selected key(s) a caller submits are actually part of THIS list's own current query result -
 * because {@code FormRowDeleteDataListAction} (and potentially other {@link DataListAction}
 * implementations) act on a selected primary key directly, with no check of their own that the
 * key belongs to the calling list's own scope. Without this check, an anonymous caller who
 * legitimately obtained a nonce for list A (by viewing a public page containing it) could still
 * submit a primary key belonging to a completely different, unrelated record in the same
 * underlying table and have the action performed on it.
 */
public class TestDataListSelectionScope {

    /** Minimal binder returning a fixed, fake row set - no DB required. */
    public static class FakeBinder extends DataListBinderDefault {
        private final List<Map<String, Object>> rows;

        public FakeBinder(List<Map<String, Object>> rows) {
            this.rows = rows;
        }

        public String getName() { return "FakeBinder"; }
        public String getVersion() { return "1.0"; }
        public String getDescription() { return ""; }
        public String getLabel() { return "Fake Binder"; }
        public String getPropertyOptions() { return "[]"; }
        public String getPrimaryKeyColumnName() { return "id"; }
        public DataListColumn[] getColumns() { return new DataListColumn[0]; }

        public DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows2) {
            DataListCollection<Map<String, Object>> result = new DataListCollection<Map<String, Object>>();
            result.addAll(rows);
            return result;
        }

        public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
            return rows.size();
        }
    }

    private DataList newDataListWithRows(String... rowIds) {
        DataList dataList = new DataList();
        dataList.setId("nestedlist");

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String id : rowIds) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("id", id);
            rows.add(row);
        }
        dataList.setBinder(new FakeBinder(rows));
        return dataList;
    }

    @Test
    public void nullOrEmptySelectionIsAlwaysWithinScope() {
        DataList dataList = newDataListWithRows("row1", "row2");
        Assert.assertTrue(dataList.isSelectionWithinScope(null));
        Assert.assertTrue(dataList.isSelectionWithinScope(new String[0]));
    }

    @Test
    public void selectionMatchingTheListsOwnRowsIsWithinScope() {
        DataList dataList = newDataListWithRows("row1", "row2", "row3");
        Assert.assertTrue("keys that are genuinely part of this list's own rows must be in scope",
                dataList.isSelectionWithinScope(new String[]{"row1", "row3"}));
    }

    @Test
    public void selectionWithAKeyOutsideTheListsOwnRowsIsNotWithinScope() {
        DataList dataList = newDataListWithRows("row1", "row2");

        // "attackerRow" is not part of this list's own query result - eg. a record from the same
        // underlying table that this specific list's filter/scope never returned
        Assert.assertFalse("a key not among this list's own rows must not be in scope, even if "
                + "some of the other submitted keys are genuine",
                dataList.isSelectionWithinScope(new String[]{"row1", "attackerRow"}));
    }

    /**
     * The rest of the framework (DataListTemplate, DataListDecorator, DataListRow itself) reads a
     * column value through DataListService.evaluateColumnValueFromRow, which additionally handles
     * a DataListRow wrapper, a lowercase name fallback and "t__" prefixed fields.
     * LookupUtil.getBeanProperty alone sees a DataListRow only as the plain HashMap it extends
     * and returns null, so validKeys came out empty and this guard failed CLOSED - silently
     * rejecting every legitimate row action on such a list. GitLab issue #2271, finding F18.
     */
    @Test
    public void selectionIsResolvedTheSameWayTheRestOfTheFrameworkReadsAColumn() {
        DataList dataList = new DataList();
        dataList.setId("wrappedrowlist");

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String id : new String[]{"row1", "row2"}) {
            Map<String, Object> underlying = new HashMap<String, Object>();
            underlying.put("id", id);
            //a DataListRow only exposes the underlying row through its own get(String) override,
            //which a plain bean property lookup never calls
            rows.add(new DataListRow(underlying));
        }
        dataList.setBinder(new FakeBinder(rows));

        Assert.assertTrue("a key that this list genuinely returns must be in scope even when the "
                + "binder wraps its rows in a DataListRow",
                dataList.isSelectionWithinScope(new String[]{"row1"}));
        Assert.assertFalse("a key this list never returned must still be rejected",
                dataList.isSelectionWithinScope(new String[]{"attackerRow"}));
    }

    @Test
    public void noBinderFailsClosed() {
        DataList dataList = new DataList();
        dataList.setId("nolist");
        Assert.assertFalse("with no binder to check against, a non-empty selection must fail closed",
                dataList.isSelectionWithinScope(new String[]{"row1"}));
    }

    /** Minimal action stub that only records whether it was invoked. */
    public static class RecordingAction extends DataListActionDefault {
        boolean executed = false;

        public String getName() { return "RecordingAction"; }
        public String getVersion() { return "1.0"; }
        public String getDescription() { return ""; }
        public String getLinkLabel() { return "Delete"; }
        public String getHref() { return null; }
        public String getTarget() { return null; }
        public String getHrefParam() { return null; }
        public String getHrefColumn() { return null; }
        public String getConfirmation() { return null; }
        public PluginProperty[] getPluginProperties() { return null; }
        public String getPropertyOptions() { return "[]"; }
        public String getLabel() { return "Recording Action"; }

        public DataListActionResult executeAction(DataList dataList, String[] rowKeys) {
            executed = true;
            return null;
        }
    }

    /**
     * End-to-end proof that a designer-configured mutating action (eg. Delete) still dispatches
     * normally for a key that is genuinely part of the list - this fix must not break configured
     * functionality, only reject out-of-scope keys.
     */
    @Test
    public void configuredActionStillDispatchesForAnInScopeKey() {
        DataList dataList = newDataListWithRows("row1", "row2");
        // a DataList parsed from its stored JSON always has "actions" initialized (even to an
        // empty array) - only a hand-built test DataList would otherwise leave it null
        dataList.setActions(new DataListAction[0]);
        RecordingAction rowAction = new RecordingAction();
        rowAction.setProperty("id", "delete");
        dataList.setRowActions(new DataListAction[]{rowAction});

        Map<String, String[]> requestParamMap = new HashMap<String, String[]>();
        requestParamMap.put(dataList.getDataListEncodedParamName(DataList.PARAMETER_ACTION), new String[]{"delete"});
        requestParamMap.put(dataList.getDataListEncodedParamName(DataList.CHECKBOX_PREFIX + "id"), new String[]{"row1"});
        dataList.setRequestParamMap(requestParamMap);

        Assert.assertTrue("row1 is genuinely part of this list, the caller-side scope check must pass",
                dataList.isSelectionWithinScope(dataList.getSelectedKeys()));

        dataList.getActionResult();
        Assert.assertTrue("a configured action must still dispatch normally for an in-scope key",
                rowAction.executed);
    }
}
