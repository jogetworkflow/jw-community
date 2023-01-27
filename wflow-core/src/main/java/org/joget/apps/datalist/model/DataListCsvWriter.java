package org.joget.apps.datalist.model;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang.StringUtils;

/**
 * A utility class used to create table in CSV for datalist CSV export
 * 
 */
public class DataListCsvWriter {
    
    Writer out;
    Integer rowNo = null;
    Integer colNo = null;
    
    public static final String ROW_END = "\n";
    public static final String CELL_END = ",";
    
    
    public DataListCsvWriter(Writer out) {
        this.out = out;
    }
    
    /**
     * Create new row in CSV
     */
    public void createNewRow() throws IOException {
        if (rowNo == null) {
            rowNo = 0;
        } else {
            write(ROW_END);
            rowNo++;
            colNo = null; //reset column count
        }
    }
    
    /**
     * Create new row in CSV
     */
    public void addCell(Object value) throws IOException {
        if (colNo == null) {
            colNo = 0;
        } else {
            write(CELL_END);
            colNo++;
        }
        write(escapeColumnValue(value));
    }
    
    /**
     * Get the writer
     * @return 
     */
    public Writer getWriter() {
        return out;
    }
    
    /**
     * Escaping for csv format.
     * <ul>
     * <li>Quotes inside quoted strings are escaped with a /</li>
     * <li>Fields containings newlines or , are surrounded by "</li>
     * </ul>
     * Note this is the standard CVS format and it's not handled well by excel.
     * @see org.displaytag.export.BaseExportView#escapeColumnValue(java.lang.Object)
     */
    protected String escapeColumnValue(Object value)
    {
        String stringValue = StringUtils.trim(value.toString());
        if (!StringUtils.containsNone(stringValue, new char[]{'\n', ','}))
        {
            return "\"" + //$NON-NLS-1$
                StringUtils.replace(stringValue, "\"", "\\\"") + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        return stringValue;
    }
    
    private void write(String string) throws IOException
    {
        if (string != null){
            out.write(string);
        }
    }
}
