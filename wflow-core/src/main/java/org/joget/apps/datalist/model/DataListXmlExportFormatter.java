package org.joget.apps.datalist.model;

/**
 * Interface for DataListColumnFormatDefault to customize the datalist XML 
 * export result before and after each row of record
 * 
 */
public interface DataListXmlExportFormatter {
    /**
     * A flag to indicate xmlBeforeRow method should be executed before each row is 
     * generate in XML
     * 
     * @return 
     */
    public boolean isXmlBeforeRow(); 
    
    /**
     * A flag to indicate xmlAfterRow method should be executed after each row is 
     * generate in XML
     * 
     * @return 
     */
    public boolean isXmlAfterRow();
    
    /**
     * Add content to the XML before each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void xmlBeforeRow(DataList datalist, Object row, Object value, DataListXmlWriter writer);
    
    /**
     * Add content to the XML after each row through the writer
     * 
     * @param datalist
     * @param row
     * @param value
     * @param writer 
     */
    public void xmlAfterRow(DataList datalist, Object row, Object value, DataListXmlWriter writer);
}
