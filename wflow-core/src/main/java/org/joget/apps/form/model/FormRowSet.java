package org.joget.apps.form.model;

import java.util.ArrayList;

/**
 * Represents a list of rows, each of which is represented by a Property object.
 */
public class FormRowSet extends ArrayList<FormRow> {

    private boolean multiRow = false;
    private String referenceTable;
    private String referenceKey;

    /**
     * @return true indicates that multiple row results are to be expected,
     * even if the actual result is empty or just a single row.
     */
    public boolean isMultiRow() {
        return multiRow;
    }

    public void setMultiRow(boolean multiRow) {
        this.multiRow = multiRow;
    }

    public String getReferenceTable() {
        return referenceTable;
    }

    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    public String getReferenceKey() {
        return referenceKey;
    }

    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }
}
