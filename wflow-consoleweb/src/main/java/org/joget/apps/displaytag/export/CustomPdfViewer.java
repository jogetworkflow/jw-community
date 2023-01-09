package org.joget.apps.displaytag.export;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.Messages;
import org.displaytag.exception.BaseNestableJspTagException;
import org.displaytag.exception.SeverityEnum;
import org.displaytag.export.BinaryExportView;
import org.displaytag.export.PdfView;
import org.displaytag.model.Column;
import org.displaytag.model.ColumnIterator;
import org.displaytag.model.HeaderCell;
import org.displaytag.model.Row;
import org.displaytag.model.RowIterator;
import org.displaytag.model.TableModel;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.model.TableModelWrapper;
import org.displaytag.util.TagConstants;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListPdfExportFormatter;
import org.joget.apps.datalist.model.DataListPdfWriter;

public class CustomPdfViewer implements BinaryExportView {
    /* split up the large pdf report into smaller parts */

    private static final int FRAGMENT_SIZE = 512;
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
     * This is a utility class to create table
     */
    private DataListPdfWriter writer;
    /**
     * The datalist object of the current table
     */
    private DataList datalist;
    /**
     * A flag that indicate there is formatter to execute before row
     */
    private boolean isBeforeRow = false;
    /**
     * A flag that indicate there is formatter to execute after row
     */
    private boolean isAfterRow = false;
    /**
     * A map hold the column number and its formatter
     */
    private Map<Integer, DataListPdfExportFormatter> formatter = new HashMap<Integer, DataListPdfExportFormatter>();

    private ResourceBundle bundle = null;

    /**
     * @see org.displaytag.export.ExportView#setParameters(TableModel, boolean, boolean, boolean)
     */
    public void setParameters(TableModel tableModel, boolean exportFullList, boolean includeHeader,
            boolean decorateValues) {
        this.model = tableModel;
        this.exportFull = exportFullList;
        this.header = includeHeader;
        this.decorated = decorateValues;
        
        PageContext pageContext = (new TableModelWrapper(tableModel)).getPageContext();
        if (pageContext != null) {
            datalist = (DataList) pageContext.findAttribute("dataList");
            
            if (datalist != null) {
                DataListColumn[] columns = datalist.getColumns();
                Collection<DataListColumnFormat> formats;
                DataListPdfExportFormatter ef;
                for (int i = 0; i < columns.length; i++) {
                    formats = columns[i].getFormats();
                    if (formats != null && !formats.isEmpty()) {
                        for (DataListColumnFormat f : formats) {
                            if (f instanceof DataListPdfExportFormatter) {
                                ef = (DataListPdfExportFormatter) f;
                                if (ef.isPdfBeforeRow()) {
                                    isBeforeRow = true;
                                }
                                if (ef.isPdfAfterRow()) {
                                    isAfterRow = true;
                                }
                                formatter.put(i, ef);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize the main info holder table.
     * @throws BadElementException for errors during table initialization
     */
    protected void initTable() throws BadElementException, DocumentException, IOException{
        writer = new DataListPdfWriter(this.model.getNumberOfColumns());
        
        if (bundle == null) {
            //get displaytag ressource bundle
            bundle = ResourceBundle.getBundle(org.displaytag.properties.TableProperties.LOCAL_PROPERTIES, Locale.getDefault());
        }
    }

    /**
     * @see org.displaytag.export.BaseExportView#getMimeType()
     * @return "application/pdf"
     */
    public String getMimeType() {
        return "application/pdf;charset=UTF-8"; //$NON-NLS-1$
    }

    /**
     * The overall PDF table generator.
     * @throws JspException for errors during value retrieving from the table model
     * @throws BadElementException IText exception
     */
    protected void generatePDFTable(Document document) throws JspException, BadElementException, DocumentException, IOException {
        if (this.header) {
            generateHeaders();
        }
        writer.endHeaders();
        generateRows(document);
    }

    /**
     * @see org.displaytag.export.BinaryExportView#doExport(OutputStream)
     */
    public void doExport(OutputStream out) throws JspException {
        
        try {
            // Initialize the table with the appropriate number of columns
            initTable();

            // Initialize the Document and register it with PdfWriter listener and the OutputStream
            Document document = new Document(PageSize.A4.rotate(), 60, 60, 40, 40);
            document.addCreationDate();

            HeaderFooter footer = new HeaderFooter(writer.getSelector().process(TagConstants.EMPTY_STRING), true);
            footer.setBorder(Rectangle.NO_BORDER);
            footer.setAlignment(Element.ALIGN_CENTER);

            PdfWriter.getInstance(document, out);
            document.setFooter(footer);

            // Fill the virtual PDF table with the necessary data
            document.open();
            generatePDFTable(document);
            document.close();
        } catch (Exception e) {
            throw new PdfGenerationException(e);
        }
    }

    /**
     * Generates the header cells, which persist on every page of the PDF document.
     * @throws BadElementException IText exception
     */
    protected void generateHeaders() throws BadElementException {
        Iterator<HeaderCell> iterator = this.model.getHeaderCellList().iterator();

        while (iterator.hasNext()) {
            HeaderCell headerCell = iterator.next();
            String columnHeader = StringEscapeUtils.unescapeHtml(headerCell.getTitle());
            if (columnHeader == null) {
                columnHeader = StringUtils.capitalize(headerCell.getBeanPropertyName());
            }
            writer.addHeaderCell(columnHeader, new Color(210, 221, 231));
        }
    }

    /**
     * Generates all the row cells.
     * @throws JspException for errors during value retrieving from the table model
     * @throws BadElementException errors while generating content
     */
    protected void generateRows(Document document) throws JspException, BadElementException, DocumentException, IOException {
        // get the correct iterator (full or partial list according to the exportFull field)
        RowIterator rowIterator = this.model.getRowIterator(this.exportFull);
        int rowCnt = 0;
        Column column = null;
        Object value = null;
        Cell cell = null;
        Row row = null;
        DataListPdfExportFormatter ef;
        int col;
        ColumnIterator columnIterator;
        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            
            if (isBeforeRow) {
                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                col = 0;
                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();
                    ef = formatter.get(col);
                    if (ef != null && ef.isPdfBeforeRow()) {
                        value = column.getValue(this.decorated);
                        ef.pdfBeforeRow(datalist, row.getObject(), value, writer);
                    }
                    col++;
                }
            }

            // iterator on columns
            columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
            while (columnIterator.hasNext()) {
                column = columnIterator.nextColumn();

                // Get the value to be displayed for the column
                value = column.getValue(this.decorated);
                
                /* some eyecandy stuff */
                if (rowCnt % 2 == 1) {
                    writer.addCell(ObjectUtils.toString(value), new Color(220, 223, 225));
                } else {
                    writer.addCell(ObjectUtils.toString(value), null);
                }
            }
            
            if (isAfterRow) {
                // iterator on columns
                columnIterator = row.getColumnIterator(this.model.getHeaderCellList());
                col = 0;
                while (columnIterator.hasNext()) {
                    column = columnIterator.nextColumn();
                    ef = formatter.get(col);
                    if (ef != null && ef.isPdfAfterRow()) {
                        value = column.getValue(this.decorated);
                        ef.pdfAfterRow(datalist, row.getObject(), value, writer);
                    }
                    col++;
                }
            }

            /* split up the pdf document to prevent OutOfMemoryExceptions */
            if (rowCnt % FRAGMENT_SIZE == FRAGMENT_SIZE - 1) {
                document.add(writer.getTable());
                initTable();
            }

            rowCnt++;
        }

        /* add the rest of the table */
        document.add(writer.getTable());
    }

    /**
     * Wraps IText-generated exceptions.
     * @author Fabrizio Giustina
     * @version $Revision: 1.2 $ ($Author: mvo $)
     */
    static class PdfGenerationException extends BaseNestableJspTagException {

        /**
         * D1597A17A6.
         */
        private static final long serialVersionUID = 899149338534L;

        /**
         * Instantiate a new PdfGenerationException with a fixed message and the given cause.
         * @param cause Previous exception
         */
        public PdfGenerationException(Throwable cause) {
            super(PdfView.class, Messages.getString("PdfView.errorexporting"), cause); //$NON-NLS-1$
        }

        /**
         * @see org.displaytag.exception.BaseNestableJspTagException#getSeverity()
         */
        public SeverityEnum getSeverity() {
            return SeverityEnum.ERROR;
        }
    }
}