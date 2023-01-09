package org.displaytag.model;

import javax.servlet.jsp.PageContext;

/**
 * This is a wrapper class of org.displaytag.model.TableModel to retrieve 
 * pageContext
 * 
 */
public class TableModelWrapper {
    TableModel model;
    
    public TableModelWrapper(TableModel model) {
        this.model = model;
    }
    
    /**
     * Return the page context of the table model
     * @return 
     */
    public PageContext getPageContext() {
        return model.getPageContext();
    }
}
