package org.tdar.struts.action.api.search;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.search.SearchInfoObject;
import org.tdar.search.service.query.SearchService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.HttpNotFoundErrorOnly;

@Namespaces(value = {
        @Namespace("/search"),
        @Namespace("/api/search") })
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpNotFoundErrorOnly()
public class SearchInfoAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = -1412305455268856908L;
    @Autowired
    private SearchService searchService;
    private SearchInfoObject searchInfoObject;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        setSearchInfoObject(searchService.getSearchInfoObject(getAuthenticatedUser()));
        setResultObject(searchInfoObject);

    }

    @Override
    @Action(value = "info")
    public String execute() throws Exception {
        return SUCCESS;
    }

    public SearchInfoObject getSearchInfoObject() {
        return searchInfoObject;
    }

    public void setSearchInfoObject(SearchInfoObject searchInfoObject) {
        this.searchInfoObject = searchInfoObject;
    }

}
