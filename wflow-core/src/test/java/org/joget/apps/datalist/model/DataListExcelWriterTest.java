package org.joget.apps.datalist.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DataListExcelWriter} numeric auto-detection.
 *
 * <h3>Test matrix</h3>
 * <ol>
 *   <li>No style configured — auto-detect unambiguous numbers; keep text as text</li>
 *   <li>US style, thousand separator ON  (1,234 / 1,234.50)</li>
 *   <li>US style, thousand separator OFF (1234  / 1234.50)</li>
 *   <li>Euro style, thousand separator ON (1.234,50)</li>
 *   <li>Prefix test — "RM 1,234" stays numeric; ISNUMBER true; SUM works</li>
 *   <li>Postfix test — "1,234 %" stays numeric</li>
 *   <li>Leading-zero strings — always text (phone numbers, IDs)</li>
 *   <li>IP addresses — always text</li>
 *   <li>Ambiguity resolution with explicit style preference</li>
 *   <li>Form decimal-point-3 edge case (1.000 ambiguity)</li>
 *   <li>Negative numbers</li>
 *   <li>Edge cases (empty, null, whitespace-only, spaces inside number)</li>
 * </ol>
 */
public class DataListExcelWriterTest {

    private SXSSFWorkbook   wb;
    private SXSSFSheet      sheet;
    private DataListExcelWriter writer;

    @Before
    public void setUp() {
        wb     = new SXSSFWorkbook(100);
        sheet  = wb.createSheet("TestSheet");
        writer = new DataListExcelWriter(wb, sheet);
    }

    @After
    public void tearDown() throws Exception {
        if (wb != null) { wb.dispose(); wb.close(); }
    }

    /**
     * Write {@code input} and assert the cell is numeric with the given value
     * and Excel format code.
     */
    private void writeAndAssertNumeric(String input, double expectedValue, String expectedFormat) {
        writer.createNewRow();
        writer.addCell(input);
        Cell cell = writer.getCurrentCell();

        assertEquals("Cell type mismatch for: " + input,
                      CellType.NUMERIC, cell.getCellType());
        assertEquals("Numeric value mismatch for: " + input,
                      expectedValue, cell.getNumericCellValue(), 0.0001);
        assertEquals("Format code mismatch for: " + input,
                      expectedFormat, cell.getCellStyle().getDataFormatString());
    }

    /**
     * Write {@code input} and assert the cell is a string cell whose value
     * equals {@code input} (after the writer's own escaping).
     */
    private void writeAndAssertText(String input) {
        writer.createNewRow();
        writer.addCell(input);
        Cell cell = writer.getCurrentCell();

        assertEquals("Cell type mismatch for: " + input,
                      CellType.STRING, cell.getCellType());
        // The writer escapes control characters; for printable ASCII input the
        // stored value should equal the input.
        assertEquals("String value mismatch for: " + input,
                      input, cell.getStringCellValue());
    }

    /** Convenience: set a preference on column 0 (the only column used by helpers). */
    private void setStyle(String style) {
        writer.setColumnPreference(0, style);
    }

    // =========================================================================
    // 1. No style — auto-detection
    // =========================================================================

    @Test
    public void autoDetect_SimpleInteger_IsNumeric() {
        writeAndAssertNumeric("1234", 1234.0, "0");
        writeAndAssertNumeric("42", 42.0, "0");
        writeAndAssertNumeric("0", 0.0, "0");
    }

    @Test
    public void autoDetect_USDecimal_IsNumeric() {
        writeAndAssertNumeric("1234.50", 1234.50, "0.00");
        writeAndAssertNumeric("1234.5", 1234.5, "0.0");
        writeAndAssertNumeric("0.00", 0.0, "0.00");
        writeAndAssertNumeric("3.14159", 3.14159, "0.00000");
    }

    @Test
    public void autoDetect_USThousands_IsNumeric() {
        writeAndAssertNumeric("1,234", 1234.0, "#,##0");
        writeAndAssertNumeric("1,234.50", 1234.50, "#,##0.00");
        writeAndAssertNumeric("1,234,567.89", 1234567.89, "#,##0.00");
    }

    @Test
    public void autoDetect_EuroThousandsWithDecimal_IsNumeric() {
        // Unambiguous because of the trailing comma-decimal
        writeAndAssertNumeric("1.234,50", 1234.50, "#,##0.00");
        writeAndAssertNumeric("1.234.567,89", 1234567.89, "#,##0.00");
    }

    @Test
    public void autoDetect_EuroDecimalOnly_IsNumeric() {
        // No thousands group, comma as decimal separator
        writeAndAssertNumeric("1234,50", 1234.50, "0.00");
        writeAndAssertNumeric("12,34", 12.34, "0.00");
    }

    // =========================================================================
    // 2 & 3. US style — thousand separator ON / OFF
    // =========================================================================

    @Test
    public void usStyle_ThousandSeparatorOn_Numeric() {
        // Thousand separator present: SUM-able, displays grouped
        writeAndAssertNumeric("1,234", 1234.0, "#,##0");
        writeAndAssertNumeric("1,234.50", 1234.50, "#,##0.00");
        writeAndAssertNumeric("-1,234.50", -1234.50, "#,##0.00");
        writeAndAssertNumeric("1,234,567.89", 1234567.89,"#,##0.00");
    }

    @Test
    public void usStyle_ThousandSeparatorOff_Numeric() {
        // No comma grouping, dot decimal — plain numeric format
        writeAndAssertNumeric("1234.50", 1234.50, "0.00");
        writeAndAssertNumeric("1234", 1234.0, "0");
        writeAndAssertNumeric("-1234.50", -1234.50, "0.00");
    }

    // =========================================================================
    // 4. Euro style — thousand separator ON
    // =========================================================================

    @Test
    public void euroStyle_ThousandsWithDecimal_Numeric() {
        // Unambiguous regardless of preference
        writeAndAssertNumeric("1.234,50", 1234.50, "#,##0.00");
        writeAndAssertNumeric("-1.234,50", -1234.50, "#,##0.00");
        writeAndAssertNumeric("1.234.567,89", 1234567.89, "#,##0.00");
    }

    @Test
    public void euroStyle_ThousandsNoDecimal_RequiresPreference() {
        // "1.234.567" without preference → text (ambiguous with IP-like strings)
        writeAndAssertText("1.234.567");

        // With Euro preference → numeric
        setStyle("euro");
        writeAndAssertNumeric("1.234.567", 1234567.0, "#,##0");
    }

    // =========================================================================
    // 5. Prefix test
    // =========================================================================

    @Test
    public void prefix_NumericRemainsNumeric() {
        writeAndAssertNumeric("RM 1,234", 1234.0, "\"RM\"#,##0");
        writeAndAssertNumeric("US$ 1,234.50", 1234.50, "\"US$\"#,##0.00");
        writeAndAssertNumeric("\u20AC 1.234,50", 1234.50,"\"\u20AC\"#,##0.00");
        // Prefix also resolves Euro-millions ambiguity
        writeAndAssertNumeric("RM 123.123.123", 123123123.0, "\"RM\"#,##0");
    }

    // =========================================================================
    // 6. Postfix test
    // =========================================================================

    @Test
    public void postfix_NumericRemainsNumeric() {
        // Postfix is stored as a literal string in the format — NOT percentage scaling
        writeAndAssertNumeric("1,234 %", 1234.0, "#,##0\"%\"");
        writeAndAssertNumeric("1,234.50 %", 1234.50, "#,##0.00\"%\"");
    }

    @Test
    public void prefixAndPostfix_Combined() {
        writeAndAssertNumeric("RM 1,234.50 %", 1234.50, "\"RM\"#,##0.00\"%\"");
    }

    // =========================================================================
    // 7. Leading-zero strings — always text
    // =========================================================================

    @Test
    public void leadingZero_AlwaysText() {
        // Phone numbers and IDs with leading zeros must never become numeric
        writeAndAssertText("0123456789");
        writeAndAssertText("0123");
        writeAndAssertText("-0123");
        writeAndAssertText("ID0123");
        writeAndAssertText("00");
    }

    @Test
    public void singleZero_IsNumeric() {
        // Plain zero is a valid number, not a leading-zero case
        writeAndAssertNumeric("0", 0.0, "0");
        writeAndAssertNumeric("0.00", 0.0, "0.00");
    }

    // =========================================================================
    // 8. IP addresses — always text
    // =========================================================================

    @Test
    public void ipAddresses_AlwaysText() {
        writeAndAssertText("192.168.1.1");
        writeAndAssertText("10.0.0.1");
        writeAndAssertText("123.45.678");
        writeAndAssertText("123.123.123.123");
    }

    @Test
    public void threeGroupDots_TextWithoutPreference_NumericWithEuro() {
        // 123.123.123 — ambiguous (Euro millions vs. IP-like)
        writeAndAssertText("123.123.123");

        setStyle("euro");
        writeAndAssertNumeric("123.123.123", 123123123.0, "#,##0");
    }

    // =========================================================================
    // 9. Ambiguity resolution with explicit style preference
    // =========================================================================

    @Test
    public void ambiguity_SingleDot_USvEuro() {
        // Default (no style): 1.234 treated as decimal 1.234
        writeAndAssertNumeric("1.234", 1.234, "0.000");

        // Explicit Euro: 1.234 treated as 1234 (dot is thousands separator)
        setStyle("euro");
        writeAndAssertNumeric("1.234", 1234.0, "#,##0");

        // Explicit US: 1.234 stays as decimal 1.234
        setStyle("us");
        writeAndAssertNumeric("1.234", 1.234, "0.000");
    }

    @Test
    public void ambiguity_EuroMillions_WithPreference() {
        setStyle("euro");
        writeAndAssertNumeric("1.234.567", 1234567.0, "#,##0");
        writeAndAssertNumeric("123.123.123", 123123123.0, "#,##0");
    }

    // =========================================================================
    // 10. Form decimal-point-3 edge case
    //     A form field with 3 decimal places shows values like "1.000".
    //     This must always be exported as a number, never as text.
    // =========================================================================

    @Test
    public void formDecimalPoint3_NoStyle_IsUSDecimal() {
        // "1.000" without style → decimal number 1.0 with 3 decimal places
        writeAndAssertNumeric("1.000", 1.0, "0.000");
        writeAndAssertNumeric("10.000", 10.0, "0.000");
        writeAndAssertNumeric("100.000", 100.0, "0.000");
        writeAndAssertNumeric("1234.000", 1234.0, "0.000");
    }

    @Test
    public void formDecimalPoint3_EuroStyle_IsThouand() {
        // "1.000" with Euro style → 1000 (dot is the thousands separator)
        setStyle("euro");
        writeAndAssertNumeric("1.000", 1000.0, "#,##0");
        writeAndAssertNumeric("10.000", 10000.0, "#,##0");  // unusual but consistent
    }

    @Test
    public void formDecimalPoint3_EuroStyle_WithContext_IsThouand() {
        // Prefix provides unambiguous Euro context even without explicit preference
        writeAndAssertNumeric("RM 1.000", 1000.0, "\"RM\"#,##0");
    }

    // =========================================================================
    // 11. Negative numbers
    // =========================================================================

    @Test
    public void negativeNumbers_AllStyles() {
        writeAndAssertNumeric("-1234", -1234.0, "0");
        writeAndAssertNumeric("-1234.56", -1234.56, "0.00");
        writeAndAssertNumeric("-1,234", -1234.0, "#,##0");
        writeAndAssertNumeric("-1,234.56", -1234.56, "#,##0.00");
        writeAndAssertNumeric("-1.234,56", -1234.56, "#,##0.00");
    }

    @Test
    public void negativeWithPrefix() {
        // E.g. "RM -1,234"
        writeAndAssertNumeric("RM -1,234", -1234.0, "\"RM\"#,##0");
    }

    // =========================================================================
    // 12. Edge cases
    // =========================================================================

    @Test
    public void nullAndEmpty_AreText() {
        // null → empty string cell
        writer.createNewRow();
        writer.addCell(null);
        Cell cell = writer.getCurrentCell();
        // escapeColumnValue(null) returns null → XSSFRichTextString(null) → empty string
        assertEquals(CellType.BLANK, cell.getCellType());

        writeAndAssertText("");
    }

    @Test
    public void spacesInsideNumber_AreText() {
        writeAndAssertText("123 456");
        writeAndAssertText("1 234.56");
        writeAndAssertText("123 abc 456");
    }

    @Test
    public void pureAlpha_IsText() {
        writeAndAssertText("abc");
        writeAndAssertText("Hello World");
        writeAndAssertText("N/A");
    }

    @Test
    public void invalidGrouping_IsText() {
        // Invalid US grouping (wrong digit counts)
        writeAndAssertText("1,2,3");
    }

    @Test
    public void nativeNumericType_WrittenDirectly() {
        // Number objects bypass string parsing entirely
        writer.createNewRow();
        writer.addCell(1234.56);
        Cell cell = writer.getCurrentCell();
        assertEquals(CellType.NUMERIC, cell.getCellType());
        assertEquals(1234.56, cell.getNumericCellValue(), 0.0001);
    }

    // =========================================================================
    // 13. decimalFormatSuffix helper
    // =========================================================================

    @Test
    public void decimalFormatSuffix_VariousPlaces() {
        assertEquals("", writer.decimalFormatSuffix(0));
        assertEquals(".0", writer.decimalFormatSuffix(1));
        assertEquals(".00", writer.decimalFormatSuffix(2));
        assertEquals(".000", writer.decimalFormatSuffix(3));
        assertEquals(".0000", writer.decimalFormatSuffix(4));
        assertEquals("", writer.decimalFormatSuffix(-1));
    }

    // =========================================================================
    // 14. parseNumericValue — direct unit tests
    // =========================================================================

    @Test
    public void parseNumericValue_NullAndBlank_ReturnsNull() {
        assertNull(writer.parseNumericValue(null, null));
        assertNull(writer.parseNumericValue("",   null));
        assertNull(writer.parseNumericValue("   ", null));
    }

    @Test
    public void parseNumericValue_USThousands_CorrectValueAndFormat() {
        Object[] r = writer.parseNumericValue("1,234.56", null);
        assertNotNull(r);
        assertEquals(1234.56, (Double) r[0], 0.0001);
        assertEquals("#,##0.00", r[1]);
    }

    @Test
    public void parseNumericValue_EuroThousandsWithDecimal_CorrectValueAndFormat() {
        Object[] r = writer.parseNumericValue("1.234,56", null);
        assertNotNull(r);
        assertEquals(1234.56, (Double) r[0], 0.0001);
        assertEquals("#,##0.00", r[1]);
    }

    @Test
    public void parseNumericValue_EuroMillions_NullWithoutPreference() {
        assertNull("Should be null without Euro preference",
                   writer.parseNumericValue("1.234.567", null));
    }

    @Test
    public void parseNumericValue_EuroMillions_NumericWithPreference() {
        Object[] r = writer.parseNumericValue("1.234.567", "euro");
        assertNotNull(r);
        assertEquals(1234567.0, (Double) r[0], 0.0001);
        assertEquals("#,##0", r[1]);
    }

    @Test
    public void parseNumericValue_Prefix_IncludedInFormat() {
        Object[] r = writer.parseNumericValue("RM 1,234", null);
        assertNotNull(r);
        assertEquals(1234.0, (Double) r[0], 0.0001);
        assertEquals("\"RM\"#,##0", r[1]);
    }

    @Test
    public void parseNumericValue_Suffix_IncludedInFormat() {
        Object[] r = writer.parseNumericValue("1,234 %", null);
        assertNotNull(r);
        assertEquals(1234.0, (Double) r[0], 0.0001);
        assertEquals("#,##0\"%\"", r[1]);
    }

    @Test
    public void parseNumericValue_LeadingZero_ReturnsNull() {
        assertNull(writer.parseNumericValue("0123", null));
        assertNull(writer.parseNumericValue("-0123", null));
    }

    @Test
    public void parseNumericValue_IP_ReturnsNull() {
        assertNull(writer.parseNumericValue("192.168.1.1", null));
        assertNull(writer.parseNumericValue("10.0.0.1",  null));
    }
}