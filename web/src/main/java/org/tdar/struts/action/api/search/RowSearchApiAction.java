package org.tdar.struts.action.api.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.search.service.RowSearchService;
import org.tdar.search.service.query.RowSearchObject;
import org.tdar.struts.action.api.AbstractJsonApiAction;

public class RowSearchApiAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = -5744108280813660844L;

    @Autowired
    private RowSearchService rowSearchService;
    
    private RowSearchObject rowSearchObect;
    
    @Override
    public void prepare() throws Exception {
        // TODO Auto-generated method stub
        super.prepare();
    }
    
    @Override
    public String execute() throws Exception {
        setResultObject(rowSearchService.search(rowSearchObect, getAuthenticatedUser()));
        return SUCCESS;
    }

    public RowSearchObject getRowSearchObect() {
        return rowSearchObect;
    }

    public void setRowSearchObect(RowSearchObject rowSearchObect) {
        this.rowSearchObect = rowSearchObect;
    }

}
