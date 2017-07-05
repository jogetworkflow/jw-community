package org.joget.apps.displaytag.export;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.displaytag.Messages;
import org.displaytag.exception.BaseNestableJspTagException;
import org.displaytag.exception.SeverityEnum;
import org.displaytag.export.BinaryExportView;
import org.displaytag.export.excel.ExcelHssfView;
import org.displaytag.model.Column;
import org.displaytag.model.ColumnIterator;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.RowIterator;
import org.displaytag.model.TableModel;
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
     * @see org.displaytag.export.ExportView#setParameters(TableModel, boolean, boolean, boolean)
     */
    public void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader,
            boolean decorateValues) {
        this.model = tableModel;
        this.exportFull = exportFullList;
        this.header = includeHeader;
        this.decorated = decorateValues;
    }

    /**
     * @return "application/vnd.ms-excel"
     * @see org.displaytag.export.BaseExportView#getMimeType()
     */
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    public void doExport(OutputStream out) throws JspException {
        XSSFWorkbook wb_template = null;
        SXSSFWorkbook wb = null;
        try {
            wb_template = new XSSFWorkbook();
            wb = new SXSSFWorkbook(wb_template, 100, true); 
            SXSSFSheet sheet = (SXSSFSheet) wb.createSheet("-");
            sheet.setRandomAccessWindowSize(100);
            sheet.trackAllColumnsForAutoSizing();

            int rowNum = 0;
            int colNum = 0;

            SXSSFRow xlsRow = null;
            CellStyle style = null;
            Font font = null;
            Iterator iterator = null;
            HeaderCell headerCell = null;
            String columnHeader = null;
            Cell cell = null;
            if (this.header) {
                // Create an header row
                xlsRow = (SXSSFRow) sheet.createRow(rowNum++);

                style = wb.createCellStyle();
                style.setFillPattern(CellStyle.FINE_DOTS);
                style.setFillBackgroundColor(HSSFColor.BLUE_GREY.index);
                font = wb.createFont();
                font.setBoldweight(Font.BOLDWEIGHT_BOLD);
                font.setColor(HSSFColor.WHITE.index);
                style.setFont(font);

                iterator = this.model.getHeaderCellList().iterator();

                while (iterator.hasNext()) {
                    headerCell = (HeaderCell) iterator.next();

                    columnHeader = headerCell.getTitle();

                    if (columnHeader == null) {
                        columnHeader = StringUtils.capitalize(headerCell.getBeanPropertyName());
                    }

                    cell = xlsRow.createCell(colNum++);
                    cell.setCellValue(new XSSFRichTextString(columnHeader));
                    cell.setCellStyle(style);
                }
            }

            // get the correct iterator (full or partial list according to the exportFull field)
            RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
            // iterator on rows
            
            Row row = null;
            ColumnIterator columnIterator = null;
            Column column = null;
            Object value = null;
            int colCount = 0;
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                xlsRow = (SXSSFRow) sheet.createRow(rowNum++);
                colNum = 0;

                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());

                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();

                    // Get the value to be displayed for the column
                    value = column.getValue(this.decorated);

                    cell = xlsRow.createCell(colNum++);

                    writeCell(value, cell);
                }
            }

            // adjust the column widths
            colCount = 0;
            while (colCount <= colNum) {
                sheet.autoSizeColumn((short) colCount++);
            }

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
     * Write the value to the cell. Override this method if you have complex data types that may need to be exported.
     * @param value the value of the cell
     * @param cell the cell to write it to
     */
    protected void writeCell(Object value, Cell cell) {
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
        } else {
            cell.setCellValue(new XSSFRichTextString(escapeColumnValue(value)));
        }
    }

    // patch from Karsten Voges
    /**
     * Escape certain values that are not permitted in excel cells.
     * @param rawValue the object value
     * @return the escaped value
     */
    protected String escapeColumnValue(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        String returnString = ObjectUtils.toString(rawValue);
        // escape the String to get the tabs, returns, newline explicit as \t \r \n
        returnString = StringEscapeUtils.escapeJava(StringUtils.trimToEmpty(returnString));
        // remove tabs, insert four whitespaces instead
        returnString = StringUtils.replace(StringUtils.trim(returnString), "\\t", "    ");
        // remove the return, only newline valid in excel
        returnString = StringUtils.replace(StringUtils.trim(returnString), "\\r", " ");
        // unescape so that \n gets back to newline
        returnString = StringEscapeUtils.unescapeJava(returnString);
        return returnString;
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
            super(ExcelHssfView.class, Messages.getString("ExcelView.errorexporting"), cause); //$NON-NLS-1$
        }

        /**
         * @see org.displaytag.exception.BaseNestableJspTagException#getSeverity()
         */
        public SeverityEnum getSeverity() {
            return SeverityEnum.ERROR;
        }
    }
}
