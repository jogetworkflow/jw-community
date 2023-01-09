package org.joget.apps.datalist.model;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontSelector;
import com.lowagie.text.pdf.ITextCustomOutputDevice;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;

/**
 * A utility class used to create table in PDF for datalist pdf export
 * 
 */
public class DataListPdfWriter {
    /**
     * This is the table, added as an Element to the PDF document. It contains all the data, needed to represent the
     * visible table into the PDF
     */
    private PdfPTable mainTable;
    /**
     * This is the current editing table, it is null unless a inner table is created. 
     */
    private PdfPTable innerTable;
    
    /**
     * The default font used in the document.
     */
    private FontSelector selector;
    
    public DataListPdfWriter(int numOfCols) throws BadElementException, DocumentException, IOException {
        mainTable = new PdfPTable(numOfCols);
        mainTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
        mainTable.setWidthPercentage(100);
        
        selector = new FontSelector();
        selector.addFont(FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("MSung-Light", "UniCNS-UCS2-H", BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("HeiseiMin-W3", "UniJIS-UCS2-H", BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        
        String path = SetupManager.getBaseDirectory() + File.separator + "fonts" + File.separator;
        File fontsFile = new File(path + "fonts.csv");
        if (fontsFile.exists()) {
            BufferedReader br = null;
            String line, name, fontPath, encoding = "";
            String[] parts = null;
            File fontFile = null;
            try {
                br = new BufferedReader(new FileReader(path + "fonts.csv"));
                while ((line = br.readLine()) != null) {
                    parts = line.split(",");
                    name = parts[0].trim();
                    fontPath = SecurityUtil.normalizedFileName(parts[1].trim());
                    
                    encoding = parts[2].trim();
                    fontFile = new File(path + fontPath);
                    if (fontFile.exists()) {
                        selector.addFont(new Font(BaseFont.createFont(path + fontPath, encoding, BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
                    }
                }
            } catch (Exception e) {
                LogUtil.error(DataListPdfWriter.class.getName(), e, "");
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {}
                }
            }
        }
        selector.addFont(new Font(BaseFont.createFont("fonts/NotoNaskhArabic/NotoNaskhArabic-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("fonts/Droid-Sans/DroidSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 7, Font.NORMAL, new Color(0, 0, 0)));
        selector.addFont(new Font(BaseFont.createFont("fonts/THSarabun/THSarabun.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 10, Font.NORMAL, new Color(0, 0, 0)));
    }
    
    /**
     * Add header cell to the main table or current working inner table
     * @param text
     * @param color
     * @throws BadElementException 
     */
    public void addHeaderCell(String text, Color color) throws BadElementException {
        PdfPCell hdrCell = getCell(text);
        if (color != null) {
            hdrCell.setBackgroundColor(color);
        }
        getCurrentTable().addCell(hdrCell);
    }
    
    /**
     * Complete the process of adding header cell to the main table or current working inner table
     */
    public void endHeaders() {
        getCurrentTable().setHeaderRows(0);
    }
    
    /**
     * Add cell to the main table or current working inner table
     * @param text
     * @param color
     * @throws BadElementException 
     */
    public void addCell(String text, Color color) throws BadElementException {
        PdfPCell cell = getCell(text);
        if (color != null) {
            cell.setBackgroundColor(color);
        }
        getCurrentTable().addCell(cell);
    }
    
    /**
     * Create a inner table
     * @param numOfCols
     * @throws BadElementException 
     */
    public void createInnerTable(int numOfCols) throws BadElementException {
        if (innerTable == null) {
            innerTable = new PdfPTable(numOfCols);
            innerTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            innerTable.setWidthPercentage(100);
        }
    }
    
    /**
     * Complete the inner table creation and add it to main table
     */
    public void endInnerTable() {
        if (innerTable != null) {
            PdfPCell cell = new PdfPCell(innerTable);
            cell.setColspan(mainTable.getNumberOfColumns());
            mainTable.addCell(cell);
            innerTable = null;
        }
    }
    
    /**
     * Get the cell object with configuration
     * @param value
     * @return
     * @throws BadElementException 
     */
    protected PdfPCell getCell(String value) throws BadElementException {
        value = StringUtils.trimToEmpty(value);
        if (ITextCustomOutputDevice.textIsRTL(value)) {
            value = ITextCustomOutputDevice.transformRTL(value);
        }
        PdfPCell cell = new PdfPCell(this.selector.process(value));
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setLeading(8, 0);
        return cell;
    }
    
    /**
     * Get the current working table, it inner table is created and not yet added to main table,
     * inner table will be return, else main table will return
     * @return 
     */
    protected PdfPTable getCurrentTable() {
        if (innerTable != null) {
            return innerTable;
        } else {
            return mainTable;
        }
    }

    /**
     * Retrieve the resulted table
     * @return 
     */
    public PdfPTable getTable() {
        return mainTable;
    }

    /**
     * Retrieve the font selector of the PDF
     * @return 
     */
    public FontSelector getSelector() {
        return selector;
    }
}
