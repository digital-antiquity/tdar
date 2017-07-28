package org.tdar.struts.action.scholar;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.exception.SearchException;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.utils.PaginationHelper;

@Namespace("/scholar")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpsOnly
public class ScholarController extends AbstractLookupController<Resource> {

    private static final long serialVersionUID = -4680630242612817779L;
    private int year;
    private PaginationHelper paginationHelper;
    
    
    
    @Autowired
    private transient ResourceSearchService resourceSearchService;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    @Actions({
            @Action(value = "scholar", results = { @Result(name = SUCCESS, location = "scholar.ftl") }),
    })
    public String execute() throws SearchException, IOException {
        setRecordsPerPage(250);
        setResults(resourceSearchService.findByTdarYear(year,this, this).getResults());
        return SUCCESS;
    }

    public PaginationHelper getPaginationHelper() {
        if (paginationHelper == null) {
            paginationHelper = PaginationHelper.withSearchResults(this);
        }
        return paginationHelper;
    }

    public void setPaginationHelper(PaginationHelper paginationHelper) {
        this.paginationHelper = paginationHelper;
    }

}
