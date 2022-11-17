package org.joget.apps.displaytag.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.export.TextExportView;
import org.displaytag.model.Column;
import org.displaytag.model.ColumnIterator;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.RowIterator;
import org.displaytag.model.TableModel;
import org.displaytag.model.TableModelWrapper;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListCsvExportFormatter;
import org.joget.apps.datalist.model.DataListCsvWriter;
import org.w3c.www.mime.Utils;

public class CustomCsvViewer implements TextExportView {
    /**
     * TableModel to render.
     */
    private TableModel model;

    /**
     * export full list?
     */
    private boolean exportFull;

    /**
     * include header in export?
     */
    private boolean header;

    /**
     * decorate export?
     */
    private boolean decorated;
    /**
     * This is a utility class to create table
     */
    private DataListCsvWriter writer;
    /**
     * The datalist object of the current table
     */
    private DataList datalist;
    /**
     * A flag that indicate there is formatter to execute before row
     */
    private boolean isBeforeRow = false;
    /**
     * A flag that indicate there is formatter to execute after row
     */
    private boolean isAfterRow = false;
    /**
     * A map hold the column number and its formatter
     */
    private Map<Integer, DataListCsvExportFormatter> formatter = new HashMap<Integer, DataListCsvExportFormatter>();
    
    @Override
    public void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader,
        boolean decorateValues)
    {
        this.model = tableModel;
        this.exportFull = exportFullList;
        this.header = includeHeader;
        this.decorated = decorateValues;
        
        PageContext pageContext = (new TableModelWrapper(tableModel)).getPageContext();
        if (pageContext != null) {
            datalist = (DataList) pageContext.findAttribute("dataList");
            
            if (datalist != null) {
                DataListColumn[] columns = datalist.getColumns();
                Collection<DataListColumnFormat> formats;
                DataListCsvExportFormatter ef;
                for (int i = 0; i < columns.length; i++) {
                    formats = columns[i].getFormats();
                    if (formats != null && !formats.isEmpty()) {
                        for (DataListColumnFormat f : formats) {
                            if (f instanceof DataListCsvExportFormatter) {
                                ef = (DataListCsvExportFormatter) f;
                                if (ef.isCsvBeforeRow()) {
                                    isBeforeRow = true;
                                }
                                if (ef.isCsvAfterRow()) {
                                    isAfterRow = true;
                                }
                                formatter.put(i, ef);
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public String getMimeType() {
        return "text/csv;charset=UTF-8"; //$NON-NLS-1$
    }
    
    public void doExport(Writer out) throws IOException, JspException
    {
        writer = new DataListCsvWriter(out);
        if (this.header)
        {
            Iterator iterator = this.model.getHeaderCellList().iterator();
            HeaderCell headerCell;
            String columnHeader;
            writer.createNewRow();
            while (iterator.hasNext()) {
                headerCell = (HeaderCell) iterator.next();
                columnHeader = StringEscapeUtils.unescapeHtml(headerCell.getTitle());

                if (columnHeader == null) {
                    columnHeader = StringUtils.capitalize(headerCell.getBeanPropertyName());
                }
                writer.addCell(columnHeader);
            }
        }

        // get the correct iterator (full or partial list according to the exportFull field)
        RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
        Column column = null;
        Object value = null;
        DataListCsvExportFormatter ef;
        Row row = null;
        int col = 0;
        ColumnIterator columnIterator;
        // iterator on rows
        while (rowIterator.hasNext())
        {
            row = rowIterator.next();
            
            if (isBeforeRow) {
                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                col = 0;
                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();
                    ef = formatter.get(col);
                    if (ef != null && ef.isCsvBeforeRow()) {
                        value = column.getValue(this.decorated);
                        ef.csvBeforeRow(datalist, row.getObject(), value, writer);
                    }
                    col++;
                }
            }

            writer.createNewRow();
            columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
            while (columnIterator.hasNext()) {
                column = columnIterator.nextColumn();

                // Get the value to be displayed for the column
                value =  column.getValue(this.decorated);
                writer.addCell(value);
            }
            
            if (isAfterRow) {
                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                col = 0;
                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();
                    ef = formatter.get(col);
                    if (ef != null && ef.isCsvAfterRow()) {
                        value = column.getValue(this.decorated);
                        ef.csvAfterRow(datalist, row.getObject(), value, writer);
                    }
                    col++;
                }
            }
        }
    }

    public boolean outputPage() {
        return false;
    }
}