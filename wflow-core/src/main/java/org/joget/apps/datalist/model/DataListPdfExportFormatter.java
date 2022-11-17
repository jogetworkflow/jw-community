package org.joget.apps.datalist.model;

/**
 * Interface for DataListColumnFormatDefault to customize the datalist pdf 
 * export result before and after each row of record
 * 
 */
public interface DataListPdfExportFormatter {
    /**
     * A flag to indicate pdfBeforeRow method should be executed before each row is 
     * generate in PDF
     * 
     * @return 
     */
    public boolean isPdfBeforeRow(); 
    
    /**
     * A flag to indicate pdfAfterRow method should be executed after each row is 
     * generate in PDF
     * 
     * @return 
     */
    public boolean isPdfAfterRow();
    
    /**
     * Add content to the PDF table before each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void pdfBeforeRow(DataList datalist, Object row, Object value, DataListPdfWriter writer);
    
    /**
     * Add content to the PDF table after each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void pdfAfterRow(DataList datalist, Object row, Object value, DataListPdfWriter writer);
}
