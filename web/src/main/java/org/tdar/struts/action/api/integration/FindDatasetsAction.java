package org.tdar.struts.action.api.integration;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.integration.search.DatasetSearchFilter;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.utils.json.JsonIntegrationFilter;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class FindDatasetsAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = 6908759745526760734L;

    private DatasetSearchFilter searchFilter = new DatasetSearchFilter();

    private boolean fetchRecordCount = true;

    @Autowired
    private transient DataTableService dataTableService;

    @Override
    public void prepare() {
        searchFilter.setAuthorizedUser(getAuthenticatedUser());
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

    public boolean isFetchRecordCount() {
        return fetchRecordCount;
    }

    public void setFetchRecordCount(boolean fetchRecordCount) {
        this.fetchRecordCount = fetchRecordCount;
    }
}
