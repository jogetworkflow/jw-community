package org.joget.apps.datalist.model;

/**
 * Interface for DataListColumnFormatDefault to customize the datalist CSV 
 * export result before and after each row of record
 * 
 */
public interface DataListCsvExportFormatter {
    /**
     * A flag to indicate csvBeforeRow method should be executed before each row is 
     * generate in CSV
     * 
     * @return 
     */
    public boolean isCsvBeforeRow(); 
    
    /**
     * A flag to indicate csvAfterRow method should be executed after each row is 
     * generate in CSV
     * 
     * @return 
     */
    public boolean isCsvAfterRow();
    
    /**
     * Add content to the CSV before each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void csvBeforeRow(DataList datalist, Object row, Object value, DataListCsvWriter writer);
    
    /**
     * Add content to the CSV after each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void csvAfterRow(DataList datalist, Object row, Object value, DataListCsvWriter writer);
}
