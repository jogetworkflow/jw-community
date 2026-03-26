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
    Map<String, XSSFCellStyle> formatStyles = new HashMap<>();
    Map<Integer, String> colPreferences = new HashMap<>();
    Integer rowNo = null;
    Integer colNo = null;
    Integer totalColumnNumber = 0;

    public DataListExcelWriter(SXSSFWorkbook wb, SXSSFSheet sheet) {
        this.wb = wb;
        this.sheet = sheet;
        this.sheet.setRandomAccessWindowSize(100);

        headerStyle = (XSSFCellStyle) wb.createCellStyle();
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
        headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        headerStyle.setFont(headerFont);
    }

    /**
     * Set a number-style hint for a column.
     * @param col  zero-based column index
     * @param pref {@code "us"} or {@code "euro"}; {@code null} to clear
     */
    public void setColumnPreference(int col, String pref) {
        colPreferences.put(col, pref);
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
                custom.setFillForegroundColor(new XSSFColor(backgroundColor, wb.getXSSFWorkbook().getStylesSource().getIndexedColors()));
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
        //this should only set before calling autoSizeColumn, else it will create performance issue
        this.sheet.trackAllColumnsForAutoSizing();
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
            String str        = escapeColumnValue(value);
            String preference = colPreferences.get(cell.getColumnIndex());
            Object[] parsed   = parseNumericValue(str, preference);
            if (parsed != null) {
                cell.setCellValue((Double) parsed[0]);
                cell.setCellStyle(getOrCreateFormatStyle((String) parsed[1]));
            } else {
                cell.setCellValue(new XSSFRichTextString(str));
            }
        }
    }

    /**
     * Attempt to interpret a string as a numeric value with an Excel format code.
     *
     * <p>Supported patterns:
     * <ul>
     *   <li><b>US thousands:</b> {@code 1,234} / {@code 1,234.56} / {@code 1,234,567.89}
     *       → format {@code #,##0[.00]}</li>
     *   <li><b>Euro thousands + decimal:</b> {@code 1.234,56} / {@code 1.234.567,89}
     *       → format {@code #,##0[.00]}</li>
     *   <li><b>Euro millions (dots only):</b> {@code 1.234.567}
     *       → numeric only with {@code "euro"} preference or a non-empty prefix/suffix</li>
     *   <li><b>Simple integer / US decimal:</b> {@code 1234} / {@code 1234.56}
     *       → format {@code 0[.00]}</li>
     *   <li><b>Euro decimal (comma decimal, no thousands):</b> {@code 1234,56}
     *       → format {@code 0.00}</li>
     *   <li><b>Ambiguous single-dot three-decimals:</b> {@code 1.234}
     *       → US=1.234 (default); Euro=1000 (with {@code "euro"} preference)</li>
     * </ul>
     *
     * <p>Always returns {@code null} (→ text cell) for:
     * <ul>
     *   <li>Leading-zero strings: {@code 0123}, {@code -0123}, {@code ID0123}</li>
     *   <li>Strings with spaces inside the numeric portion: {@code 123 456}</li>
     *   <li>IP-like patterns without Euro preference: {@code 123.123.123}</li>
     * </ul>
     *
     * @param strValue   the escaped string value of the cell
     * @param preference {@code "us"}, {@code "euro"}, or {@code null}
     * @return {@code Object[]{Double value, String excelFormat}} or {@code null}
     */
    protected Object[] parseNumericValue(String strValue, String preference) {
        if (strValue == null || strValue.trim().isEmpty()) return null;

        String  s = strValue.trim();
        boolean isEuro = "euro".equalsIgnoreCase(preference);

        // Locate the digit span
        int firstDigit = -1, lastDigit = -1;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                if (firstDigit == -1) firstDigit = i;
                lastDigit = i;
            }
        }
        if (firstDigit == -1) return null; // no digits at all

        // Allow a minus sign immediately before the first digit
        int numStart = (firstDigit > 0 && s.charAt(firstDigit - 1) == '-')
                       ? firstDigit - 1 : firstDigit;

        String  prefix = s.substring(0, numStart).trim();
        String  num = s.substring(numStart, lastDigit + 1);
        String  suffix = s.substring(lastDigit + 1).trim();
        boolean hasContext = !prefix.isEmpty() || !suffix.isEmpty();

        // Embedded spaces inside the number part make it unparseable
        if (num.contains(" ")) return null;

        // Leading-zero strings → phone numbers / IDs, never numeric
        if (num.matches("-?0\\d+")) return null;

        // Pattern matchin
        Double value  = null;
        String format = null;

        if (num.matches("-?\\d{1,3}(,\\d{3})+(\\.\\d+)?")) {
            // US thousands:  1,234  /  1,234.56  /  1,234,567.89
            value  = Double.parseDouble(num.replace(",", ""));
            int dec = countDecimalPlaces(num, '.');
            format = "#,##0" + decimalFormatSuffix(dec);

        } else if (num.matches("-?\\d{1,3}(\\.\\d{3})+(,\\d+)")) {
            // Euro thousands + decimal:  1.234,56  /  1.234.567,89
            value  = Double.parseDouble(num.replace(".", "").replace(",", "."));
            int dec = num.length() - num.lastIndexOf(',') - 1;
            format = "#,##0" + decimalFormatSuffix(dec);

        } else if (num.matches("-?\\d{1,3}(\\.\\d{3}){2,}")) {
            // Multiple dot-groups, no decimal:  1.234.567  /  123.123.123
            // Ambiguous with IP-like strings — require an explicit hint or context.
            if (isEuro || hasContext) {
                value  = Double.parseDouble(num.replace(".", ""));
                format = "#,##0";
            } else {
                return null;
            }

        } else if (num.matches("-?\\d+(\\.\\d+)?")) {
            // Simple integer or decimal:  1234  /  1234.56  /  1.234 (ambiguous)
            if ((isEuro || hasContext) && num.matches("-?\\d+\\.\\d{3}")) {
                // Euro interpretation: 1.234 → 1 234 (dot is thousands separator)
                value  = Double.parseDouble(num.replace(".", ""));
                format = "#,##0";
            } else {
                value  = Double.parseDouble(num);
                int dec = countDecimalPlaces(num, '.');
                format = "0" + decimalFormatSuffix(dec);
            }

        } else if (num.matches("-?\\d+(,\\d+)")) {
            // Euro decimal only (no thousands):  1234,56
            value  = Double.parseDouble(num.replace(",", "."));
            int dec = num.length() - num.indexOf(',') - 1;
            format = "0" + decimalFormatSuffix(dec);

        } else {
            return null;
        }

        return new Object[]{ value, buildExcelFormat(format, prefix, suffix) };
    }

    /** 
     * Count the number of characters after {@code separator} in {@code num}. 
     */
    private int countDecimalPlaces(String num, char separator) {
        int idx = num.indexOf(separator);
        return (idx >= 0) ? num.length() - idx - 1 : 0;
    }

    /**
     * Build the {@code .000} tail of an Excel number format.
     * Returns {@code ""} when {@code places <= 0}.
     */
    protected String decimalFormatSuffix(int places) {
        if (places <= 0) return "";
        StringBuilder sb = new StringBuilder(".");
        for (int i = 0; i < places; i++) sb.append('0');
        return sb.toString();
    }

    /**
     * Wrap a bare number format code with Excel literal-text prefix/suffix.
     * Example: {@code buildExcelFormat("#,##0.00", "RM ", " %")}
     * {@code "\"RM \"#,##0.00\" %\""}
     */
    private String buildExcelFormat(String numberFormat, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder();
        if (!prefix.isEmpty()) {
            sb.append('"').append(prefix.replace("\"", "\\\"")).append('"');
        }
        sb.append(numberFormat);
        if (!suffix.isEmpty()) {
            sb.append('"').append(suffix.replace("\"", "\\\"")).append('"');
        }
        return sb.toString();
    }

    /** 
     * Return a cached (or newly created) cell style for the given format code. 
     */
    private XSSFCellStyle getOrCreateFormatStyle(String format) {
        return formatStyles.computeIfAbsent(format, f -> {
            XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
            style.setDataFormat(wb.createDataFormat().getFormat(f));
            return style;
        });
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