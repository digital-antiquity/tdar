package org.tdar.struts.action.entity;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;
import org.tdar.struts.action.search.FacetGroup;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PaginationHelper;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/keyword")
public class SimpleKeywordController extends AbstractKeywordController implements SearchResultHandler<Resource> {

    private static final long serialVersionUID = 8576078075798508582L;

    @Autowired
    private transient SearchService searchService;

    private int startRecord = DEFAULT_START;
    private int recordsPerPage = getDefaultRecordsPerPage();
    private int totalRecords;
    private List<Resource> results;
    private SortOption secondarySortField;
    private SortOption sortField;
    private String mode = "KeywordBrowse";
    private PaginationHelper paginationHelper;

    @Action("edit")
    @HttpsOnly
    @RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
    public String edit() {
        return SUCCESS;
    }

    @Action(value = "view", interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String view() {
        if (getKeyword().getStatus() != Status.ACTIVE && !isEditor()) {
            return NOT_FOUND;
        }
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(new HydrateableKeywordQueryPart<Keyword>(getKeywordType(), Arrays.asList(getKeyword())));
        rqb.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        try {
            setSortField(SortOption.TITLE);
            searchService.handleSearch(rqb, this, this);
        } catch (Exception e) {
            addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
        }

        setResults(results);
        return SUCCESS;
    }

    public int getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(int startRecord) {
        this.startRecord = startRecord;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<Resource> getResults() {
        return results;
    }

    public void setResults(List<Resource> results) {
        this.results = results;
    }

    public SortOption getSecondarySortField() {
        return secondarySortField;
    }

    public void setSecondarySortField(SortOption secondarySortField) {
        this.secondarySortField = secondarySortField;
    }

    public SortOption getSortField() {
        return sortField;
    }

    public void setSortField(SortOption sortField) {
        this.sortField = sortField;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    @Override
    public ProjectionModel getProjectionModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isShowAll() {
        return false;
    }

    @Override
    public String getSearchTitle() {
        return null;
    }

    @Override
    public String getSearchDescription() {
        return null;
    }

    @Override
    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }

    @Override
    public int getPrevPageStartRecord() {
        return startRecord - recordsPerPage;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getDefaultRecordsPerPage() {
        return 100;
    }
}
