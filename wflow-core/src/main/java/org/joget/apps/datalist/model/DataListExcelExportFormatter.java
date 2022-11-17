package org.joget.apps.datalist.model;

/**
 * Interface for DataListColumnFormatDefault to customize the datalist Excel 
 * export result before and after each row of record
 * 
 */
public interface DataListExcelExportFormatter {
    /**
     * A flag to indicate excelBeforeRow method should be executed before each row is 
     * generate in Excel
     * 
     * @return 
     */
    public boolean isExcelBeforeRow(); 
    
    /**
     * A flag to indicate excelAfterRow method should be executed after each row is 
     * generate in Excel
     * 
     * @return 
     */
    public boolean isExcelAfterRow();
    
    /**
     * Add content to the Excel before each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void excelBeforeRow(DataList datalist, Object row, Object value, DataListExcelWriter writer);
    
    /**
     * Add content to the Excel after each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void excelAfterRow(DataList datalist, Object row, Object value, DataListExcelWriter writer);
}
