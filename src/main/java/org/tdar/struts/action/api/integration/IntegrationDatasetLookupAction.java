package org.tdar.struts.action.api.integration;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.integration.DatasetSearchFilter;
import org.tdar.core.dao.resource.integration.IntegrationDataTableSearchResult;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.search.query.SimpleSearchResultHandler;
import org.tdar.search.query.SortOption;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationDatasetLookupAction extends AbstractIntegrationAction implements Preparable, SimpleSearchResultHandler {

    private static final long serialVersionUID = 6908759745526760734L;

    private Integer startRecord = 0;
    private Integer recordsPerPage = 100;
    private DatasetSearchFilter searchFilter = new DatasetSearchFilter(recordsPerPage, startRecord);

    @Autowired
    private transient DataTableService dataTableService;
    @Autowired
    private transient SerializationService serializationService;

    @Override
    public void prepare() {
        searchFilter.setAuthorizedUser(getAuthenticatedUser());
        searchFilter.setMaxResults(getRecordsPerPage());
        searchFilter.setFirstResult(getStartRecord());
    }

    @Action(value = "find-datasets")
    public String findDatasets() throws IOException {
        getLogger().debug("find-datasets:: datasetFilter: {}", searchFilter);
        IntegrationDataTableSearchResult findDataTables = dataTableService.findDataTables(searchFilter);
        setJsonObject(findDataTables, JsonIntegrationFilter.class);
        return SUCCESS;
    }

    public DatasetSearchFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(DatasetSearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }

    @Override
    public void setRecordsPerPage(int recordsPerPage) {
        // TODO Auto-generated method stub

    }

    @Override
    public SortOption getSortField() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSortField(SortOption sortField) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getStartRecord() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setStartRecord(int startRecord) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getRecordsPerPage() {
        // TODO Auto-generated method stub
        return 0;
    }

}
