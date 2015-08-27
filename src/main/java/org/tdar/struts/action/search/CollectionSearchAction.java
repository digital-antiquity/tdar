package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.FacetGroup;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceCollectionQueryBuilder;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class CollectionSearchAction extends AbstractLookupController<ResourceCollection> {

    private static final long serialVersionUID = -4107940683143336985L;

    private List<SortOption> sortOptions = SortOption.getOptionsForContext(ResourceCollection.class);

    @Autowired
    private transient SearchService searchService;

    private String query;

    @Action(value = "collections", results = {
            @Result(name = SUCCESS, location = "collections.ftl"),
            @Result(name = INPUT, location = "collection.ftl") })
    public String searchCollections() throws TdarActionException {
        setSortOptions(SortOption.getOptionsForContext(ResourceCollection.class));
        try {
            return collectionSearch();
        } catch (TdarRecoverableRuntimeException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
    }

    private String collectionSearch() {
        setLookupSource(LookupSource.COLLECTION);
        setMode("COLLECTION SEARCH:");
        determineCollectionSearchTitle();
        QueryBuilder queryBuilder = new ResourceCollectionQueryBuilder();
        searchService.buildResourceCollectionQuery(queryBuilder, getAuthenticatedUser(), Arrays.asList(getQuery()));

        try {
            getLogger().trace("queryBuilder: {}", queryBuilder);
            searchService.handleSearch(queryBuilder, this, this);
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception: {}", tdre.getMessage());
            addActionError(tdre.getMessage());
        } catch (ParseException e) {
            getLogger().warn("search parse exception: {}", e.getMessage());
            addActionErrorWithException(getText("advancedSearchController.error_parsing_failed"), e);
        }

        if (getActionErrors().isEmpty()) {
            return SUCCESS;
        } else {
            return INPUT;
        }
    }

    protected void determineCollectionSearchTitle() {
        if (StringUtils.isEmpty(getQuery())) {
            setSearchTitle(getText("advancedSearchController.title_all_collections"));
        } else {
            setSearchTitle(getQuery());
        }
    }

    public List<SortOption> getSortOptions() {
        sortOptions.remove(SortOption.RESOURCE_TYPE);
        sortOptions.remove(SortOption.RESOURCE_TYPE_REVERSE);
        return sortOptions;
    }

    public void setSortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchSubtitle() {
        return getSearchTitle();
    }
}
