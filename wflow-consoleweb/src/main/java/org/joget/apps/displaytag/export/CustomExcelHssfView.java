package org.joget.apps.displaytag.export;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListExcelExportFormatter;
import org.joget.apps.datalist.model.DataListExcelWriter;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
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
     * Column index → style preference ("us" / "euro"), populated from the form
     * element definitions during {@link #setParameters}.
     */
    private Map<Integer, String> colPreferences = new HashMap<>();

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
        if (pageContext == null) return;

        datalist = (DataList) pageContext.findAttribute("dataList");
        if (datalist == null) return;

        DataListColumn[] columns = datalist.getColumns();

        // Read numeric style preferences from the bound form
        loadColumnStylePreferences(columns);

        // Collect per-row Excel formatter hooks
        for (int i = 0; i < columns.length; i++) {
            Collection<DataListColumnFormat> formats = columns[i].getFormats();
            if (formats == null) continue;
            for (DataListColumnFormat f : formats) {
                if (f instanceof DataListExcelExportFormatter) {
                    DataListExcelExportFormatter ef = (DataListExcelExportFormatter) f;
                    if (ef.isExcelBeforeRow()) isBeforeRow = true;
                    if (ef.isExcelAfterRow())  isAfterRow  = true;
                    formatter.put(i, ef);
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
        SXSSFWorkbook wb = null;
        try {
            wb = new SXSSFWorkbook(100); 
            SXSSFSheet sheet = (SXSSFSheet) wb.createSheet("-");
            writer = new DataListExcelWriter(wb, sheet);
            
            Iterator iterator = null;
            HeaderCell headerCell = null;
            String columnHeader = null;
            
            colPreferences.forEach(writer::setColumnPreference);
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
            LogUtil.error(CustomExcelHssfView.class.getName(), e, "Error during Excel export");
            throw new RuntimeException(e.getLocalizedMessage());
        } finally {
            if (wb != null) {
                try {
                    wb.dispose(); //should call this to remove all temp file
                    wb.close();
                } catch (Exception e) {}
            }
        }
    }

    /**
     * Walk each DataList column and look up the matching form element.
     * If the element carries a {@code "style"} property ("us" or "euro"), store
     * that as a column preference so the writer can resolve ambiguous numbers.
     */
    private void loadColumnStylePreferences(DataListColumn[] columns) {
        try {
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            if (appDef == null) return;

            String formDefId = datalist.getBinder() != null
                               ? (String) datalist.getBinder().getProperty("formDefId")
                               : null;
            if (StringUtils.isBlank(formDefId)) return;

            FormDefinitionDao formDefinitionDao =
                (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
            FormService formService =
                (FormService) AppUtil.getApplicationContext().getBean("formService");

            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            if (formDef == null) return;

            Form form = (Form) formService.createElementFromJson(formDef.getJson(), false);

            for (int i = 0; i < columns.length; i++) {
                String  colName = columns[i].getName();
                Element el = FormUtil.findElement(colName, form, null);
                if (el == null) continue;

                String style = el.getPropertyString("style");
                if (StringUtils.isNotBlank(style)) {
                    colPreferences.put(i, style.toLowerCase());
                }
            }
        } catch (Exception e) {
            LogUtil.error(CustomExcelHssfView.class.getName(), e,
                          "Error reading form element styles for Excel export");
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
