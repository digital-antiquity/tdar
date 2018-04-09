package org.tdar.struts.action.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.bean.CollectionSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.service.query.CollectionSearchService;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpsOnly
public class CollectionSearchAction extends AbstractLookupController<ResourceCollection> {

    private static final long serialVersionUID = -4107940683143336985L;

    private List<SortOption> sortOptions = SortOption.getOptionsForContext(ResourceCollection.class);

    @Autowired
    private transient CollectionSearchService collectionSearchService;

    private String query;

    @Action(value = "collections", results = {
            @Result(name = SUCCESS, location = "collections.ftl"),
            @Result(name = INPUT, location = "collection.ftl") })
    public String searchCollections() throws TdarActionException, SolrServerException, IOException {
        setSortOptions(SortOption.getOptionsForContext(ResourceCollection.class));
        try {
            return collectionSearch();
        } catch (TdarRecoverableRuntimeException trex) {
            addActionError(trex.getMessage());
            return INPUT;
        }
    }

    private String collectionSearch() throws SolrServerException, IOException {
        setLookupSource(LookupSource.COLLECTION);
        setMode("COLLECTION SEARCH:");

        try {
            CollectionSearchQueryObject csqo = new CollectionSearchQueryObject();
            csqo.setAllFields(Arrays.asList(getQuery()));
            collectionSearchService.buildResourceCollectionQuery(getAuthenticatedUser(), csqo, this, this);
            determineCollectionSearchTitle();
        } catch (TdarRecoverableRuntimeException tdre) {
            getLogger().warn("search parse exception: {}", tdre.getMessage());
            addActionError(tdre.getMessage());
        } catch (SearchException e) {
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
        if (StringUtils.isBlank(getQuery())) {
            setSearchTitle(getText("advancedSearchController.title_all_collections"));
        } else {
            setSearchTitle(getQuery());
        }
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

    public String getSearchSubtitle() {
        return getSearchTitle();
    }

    @Override
    public boolean isLeftSidebar() {
        return true;
    }
}
