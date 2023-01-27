package org.joget.apps.datalist.model;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * A utility class used to create table in XML for datalist XML export
 * 
 */
public class DataListXmlWriter {
    Writer out;
    
    public static final String DOCUMENT_START = "<?xml version=\"1.0\"?>\n<table>\n";
    public static final String DOCUMENT_END = "</table>\n";
    public static final String ROW_START = "<row>\n";
    public static final String ROW_END = "</row>\n";
    public static final String CELL_START = "<column>";
    public static final String CELL_END = "</column>\n";
    
    
    public DataListXmlWriter(Writer out) {
        this.out = out;
    }
    
    /**
     * Create XML document
     */
    public void createXmlDocument() throws IOException {
        write(DOCUMENT_START);
    }
    
    /**
     * Complete XML document
     */
    public void endXmlDocument() throws IOException {
        write(DOCUMENT_END);
    }
    
    /**
     * Create new row in XML
     */
    public void createNewRow() throws IOException {
        write(ROW_START);
    }
    
    /**
     * Complete a row in XML
     */
    public void endRow() throws IOException {
        write(ROW_END);
    }
    
    /**
     * Create new column in XML
     */
    public void addColumn(Object value) throws IOException {
        write(CELL_START + escapeColumnValue(value) + CELL_END);
    }

    /**
     * Get the writer
     * @return 
     */
    public Writer getWriter() {
        return out;
    }

    /**
     * Escaping for xml format.
     */
    protected String escapeColumnValue(Object value)
    {
        return StringEscapeUtils.escapeXml(value.toString());
    }
    
    private void write(String string) throws IOException
    {
        if (string != null){
            out.write(string);
        }
    }
}
