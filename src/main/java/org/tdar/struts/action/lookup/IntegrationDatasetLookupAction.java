package org.tdar.struts.action.lookup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.BookmarkQueryPart;
import org.tdar.search.query.part.CategoryTermQueryPart;
import org.tdar.search.query.part.ProjectIdLookupQueryPart;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.struts.data.FacetGroup;
import org.tdar.utils.json.JsonLookupFilter;

/**
 * $Id$
 * <p>
 * Handles ajax requests for people
 * 
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationDatasetLookupAction extends AbstractLookupController<Resource> {

    
    private static final long serialVersionUID = -7961710740717444794L;

    public static final String SELECTED_RESULTS = "selectedResults";
    
    private Long projectId;
    private Long collectionId;
    private String term;
    private Long sortCategoryId;
    private boolean bookmarked;

    @Action(value = "dataset",
            results = {
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
            })
    public String lookupResource() {
        QueryBuilder q = new ResourceQueryBuilder();
        setLookupSource(LookupSource.RESOURCE);
        setMode("resourceLookup");
        
        if (isBookmarked()) {
            BookmarkQueryPart bqp = new BookmarkQueryPart();
            bqp.add(getAuthenticatedUser());
            q.append(bqp);
        }
        q.append(new CategoryTermQueryPart(getTerm(), getSortCategoryId()));

        if (Persistable.Base.isNotNullOrTransient(getProjectId())) {
            q.append(new ProjectIdLookupQueryPart(getProjectId()));
        }

        appendIf(Persistable.Base.isNotNullOrTransient(getCollectionId()), q, QueryFieldNames.RESOURCE_COLLECTION_SHARED_IDS, getCollectionId());

        if (getSortField() != SortOption.RELEVANCE) {
            setSecondarySortField(SortOption.TITLE);
        }

        
        q.append(processReservedTerms(this));
        try {
            handleSearch(q);
            getLogger().trace("jsonResults: {}", getResults());
        } catch (ParseException e) {
            addActionErrorWithException(getText("abstractLookupController.invalid_syntax"), e);
            return ERROR;
        }

        jsonifyResult(JsonLookupFilter.class);
        return SUCCESS;
    }
    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    public Long getCollectionId() {
        return collectionId;
    }
    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
    public Long getSortCategoryId() {
        return sortCategoryId;
    }
    public void setSortCategoryId(Long sortCategoryId) {
        this.sortCategoryId = sortCategoryId;
    }
    public boolean isBookmarked() {
        return bookmarked;
    }
    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

}
