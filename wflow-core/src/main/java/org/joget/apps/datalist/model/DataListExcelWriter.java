package org.joget.apps.datalist.model;

import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * A utility class used to create table in Excel for datalist excel export
 * 
 */
public class DataListExcelWriter {
    
    SXSSFWorkbook wb;
    SXSSFSheet sheet;
    SXSSFRow currentRow;
    Cell currentCell;
    XSSFCellStyle headerStyle;
    Font headerFont;
    Map<Color, XSSFCellStyle> customStyles = new HashMap<Color, XSSFCellStyle>();
    Integer rowNo = null;
    Integer colNo = null;
    Integer totalColumnNumber = 0;
    
    public DataListExcelWriter(SXSSFWorkbook wb, SXSSFSheet sheet) {
        this.wb = wb;
        this.sheet = sheet;
        this.sheet.setRandomAccessWindowSize(100);
        this.sheet.trackAllColumnsForAutoSizing();
        
        headerStyle = (XSSFCellStyle) wb.createCellStyle();
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
        headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        headerStyle.setFont(headerFont);
    }
    
    /**
     * Create new row in excel sheet
     */
    public void createNewRow() {
        if (rowNo == null) {
            rowNo = 0;
        } else {
            rowNo++;
        }
        colNo = null;
        currentRow = (SXSSFRow) sheet.createRow(rowNo);
    }
    
    /**
     * Add header cell in current row
     * @param text 
     * @param backgroundColor Color name in HSSFColor
     */
    public void addHeaderCell(String text, Color backgroundColor) {
        if (colNo == null) {
            colNo = 0;
        } else {
            colNo++;
        }
        currentCell = getCurrentRow().createCell(colNo);
        currentCell.setCellValue(new XSSFRichTextString(text));
        if (backgroundColor != null) {
            if (customStyles.get(backgroundColor) == null) {
                XSSFCellStyle custom = (XSSFCellStyle) wb.createCellStyle();
                custom.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                custom.setFillForegroundColor(new XSSFColor(backgroundColor));
                custom.setFont(headerFont);
                customStyles.put(backgroundColor, custom);
            }
            currentCell.setCellStyle(customStyles.get(backgroundColor));
        } else {
            currentCell.setCellStyle(headerStyle);
        }
    }
    
    /**
     * Add cell in current row 
     * @param value
     */
    public void addCell(Object value) {
        if (colNo == null) {
            colNo = 0;
        } else {
            colNo++;
        }
        if (colNo > totalColumnNumber) {
            totalColumnNumber = colNo;
        }
        currentCell = getCurrentRow().createCell(colNo);
        writeCell(value, currentCell);
    }
    
    /**
     * Set all columns to auto width
     */
    public void adjustColumnWidth() {
        // adjust the column widths
        int colCount = 0;
        while (colCount <= totalColumnNumber) {
            sheet.autoSizeColumn((short) colCount++);
        }
    }
    
    /**
     * Get current working row in sheet 
     * @return 
     */
    public SXSSFRow getCurrentRow() {
        return currentRow;
    }
    
    /**
     * Get current working cell in sheet 
     * @return 
     */
    public Cell getCurrentCell() {
        return currentCell;
    }

    /**
     * Get Excel Workbook
     * @return 
     */
    public SXSSFWorkbook getWb() {
        return wb;
    }

    /**
     * Get excel sheet
     * @return 
     */
    public SXSSFSheet getSheet() {
        return sheet;
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
}
