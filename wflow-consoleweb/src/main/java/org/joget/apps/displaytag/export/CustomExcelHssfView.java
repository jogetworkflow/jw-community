package org.joget.apps.displaytag.export;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.displaytag.Messages;
import org.displaytag.exception.BaseNestableJspTagException;
import org.displaytag.exception.SeverityEnum;
import org.displaytag.export.BinaryExportView;
import org.displaytag.model.Column;
import org.displaytag.model.ColumnIterator;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.RowIterator;
import org.displaytag.model.TableModel;
import org.displaytag.model.TableModelWrapper;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListExcelExportFormatter;
import org.joget.apps.datalist.model.DataListExcelWriter;
import org.joget.commons.util.LogUtil;

public class CustomExcelHssfView implements BinaryExportView {

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
    private DataListExcelWriter writer;
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
    private Map<Integer, DataListExcelExportFormatter> formatter = new HashMap<Integer, DataListExcelExportFormatter>();

    /**
     * @see org.displaytag.export.ExportView#setParameters(TableModel, boolean, boolean, boolean)
     */
    public void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader,
            boolean decorateValues) {
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
                DataListExcelExportFormatter ef;
                for (int i = 0; i < columns.length; i++) {
                    formats = columns[i].getFormats();
                    if (formats != null && !formats.isEmpty()) {
                        for (DataListColumnFormat f : formats) {
                            if (f instanceof DataListExcelExportFormatter) {
                                ef = (DataListExcelExportFormatter) f;
                                if (ef.isExcelBeforeRow()) {
                                    isBeforeRow = true;
                                }
                                if (ef.isExcelAfterRow()) {
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

    /**
     * @return "application/vnd.ms-excel"
     * @see org.displaytag.export.BaseExportView#getMimeType()
     */
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8";
    }

    public void doExport(OutputStream out) throws JspException {
        XSSFWorkbook wb_template = null;
        SXSSFWorkbook wb = null;
        try {
            wb_template = new XSSFWorkbook();
            wb = new SXSSFWorkbook(wb_template, 100, true); 
            SXSSFSheet sheet = (SXSSFSheet) wb.createSheet("-");
            writer = new DataListExcelWriter(wb, sheet);
            
            Iterator iterator = null;
            HeaderCell headerCell = null;
            String columnHeader = null;
            if (this.header) {
                // Create an header row
                writer.createNewRow();
                
                iterator = this.model.getHeaderCellList().iterator();
                while (iterator.hasNext()) {
                    headerCell = (HeaderCell) iterator.next();

                    columnHeader = StringEscapeUtils.unescapeHtml(headerCell.getTitle());

                    if (columnHeader == null) {
                        columnHeader = StringUtils.capitalize(headerCell.getBeanPropertyName());
                    }
                    writer.addHeaderCell(columnHeader, null);
                }
            }

            // get the correct iterator (full or partial list according to the exportFull field)
            RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
            Row row = null;
            ColumnIterator columnIterator = null;
            Column column = null;
            Object value = null;
            int col;
            DataListExcelExportFormatter ef;
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                
                if (isBeforeRow) {
                    // iterator on columns
                    columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                    col = 0;
                    while (columnIterator.hasNext()) {
                        column = columnIterator.nextColumn();
                        ef = formatter.get(col);
                        if (ef != null && ef.isExcelBeforeRow()) {
                            value = column.getValue(this.decorated);
                            ef.excelBeforeRow(datalist, row.getObject(), value, writer);
                        }
                        col++;
                    }
                }
                
                writer.createNewRow();
                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();

                    // Get the value to be displayed for the column
                    value = column.getValue(this.decorated);
                    writer.addCell(value);
                }
                
                if (isAfterRow) {
                    // iterator on columns
                    columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                    col = 0;
                    while (columnIterator.hasNext()) {
                        column = columnIterator.nextColumn();
                        ef = formatter.get(col);
                        if (ef != null && ef.isExcelAfterRow()) {
                            value = column.getValue(this.decorated);
                            ef.excelAfterRow(datalist, row.getObject(), value, writer);
                        }
                        col++;
                    }
                }
            }

            writer.adjustColumnWidth();
            wb.write(out);
        } catch (Exception e) {
            LogUtil.error(CustomExcelHssfView.class.getName(), e, "");
            throw new RuntimeException(e.getLocalizedMessage());
        } finally {
            if (wb_template != null) {
                try {
                    wb_template.close();
                } catch (Exception e) {}
            }
            if (wb != null) {
                try {
                    wb.close();
                } catch (Exception e) {}
            }
        }
    }
    
    /**
     * Wraps IText-generated exceptions.
     * @author Fabrizio Giustina
     * @version $Revision: 1143 $ ($Author: fgiust $)
     */
    static class ExcelGenerationException extends BaseNestableJspTagException {

        /**
         * D1597A17A6.
         */
        private static final long serialVersionUID = 899149338534L;

        /**
         * Instantiate a new PdfGenerationException with a fixed message and the given cause.
         * @param cause Previous exception
         */
        public ExcelGenerationException(Throwable cause) {
            super(CustomExcelHssfView.class, Messages.getString("ExcelView.errorexporting"), cause); //$NON-NLS-1$
        }

        /**
         * @see org.displaytag.exception.BaseNestableJspTagException#getSeverity()
         */
        public SeverityEnum getSeverity() {
            return SeverityEnum.ERROR;
        }
    }
}
