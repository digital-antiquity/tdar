package org.tdar.struts.action.search;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpsOnly
public class InstitutionSearchAction extends AbstractLookupController<Institution> {

    private static final long serialVersionUID = -2102002561399688184L;

    private List<SortOption> sortOptions = SortOption.getOptionsForContext(Institution.class);

    private String query;

    @Autowired
    private CreatorSearchService<Institution> creatorSearchService;

    @Action(value = "institutions", results = {
            @Result(name = SUCCESS, location = "institutions.ftl"),
            @Result(name = INPUT, location = "institution.ftl") })
    public String searchInstitutions() throws TdarActionException, SolrServerException, IOException {
        setSortOptions(SortOption.getOptionsForContext(Institution.class));
        setMinLookupLength(0);
        setLookupSource(LookupSource.INSTITUTION);
        setMode("INSTITUTION");
        try {
            creatorSearchService.searchInstitution(getQuery(), this, this);
        } catch (TdarRecoverableRuntimeException | SearchException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    public List<SortOption> getSortOptions() {
        sortOptions.remove(SortOption.RESOURCE_TYPE);
        return sortOptions;
    }

    public void setSortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean isLeftSidebar() {
        return true;
    }
}
