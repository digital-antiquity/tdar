package org.tdar.struts.action.search;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.resource.Facetable;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@SuppressWarnings("rawtypes")
@Namespace("/scholar")
@ParentPackage("default")
@Component
@Scope("prototype")
@HttpOnlyIfUnauthenticated
public class ScholarController extends AbstractLookupController {

    private static final long serialVersionUID = -4680630242612817779L;
    private int year;

    @Autowired
    private transient ResourceService resourceService;

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
    public String execute() {
        setRecordsPerPage(250);
        setResults(resourceService.findByTdarYear(this, getYear()));
        for (Indexable p : (List<Indexable>)getResults()) {
            getAuthenticationAndAuthorizationService().applyTransientViewableFlag(p, getAuthenticatedUser());
        }
        return SUCCESS;
    }

    @Override
    public List<FacetGroup<? extends Facetable>> getFacetFields() {
        return null;
    }

}
