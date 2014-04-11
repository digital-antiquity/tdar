package org.tdar.struts.action;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.struts.data.ResultMetadataWrapper;

@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/datatable")
public class DataTableBrowseController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -2570627983412022974L;

    private Long id;
    private Integer recordsPerPage = 50;
    private Integer startRecord = 0;
    private List<List<String>> results = Collections.emptyList();
    private String callback;
    private int totalRecords;
    private ResultMetadataWrapper resultsWrapper;

    @Action(value = "browse",
            results = { @Result(name = "success", location = "browse.ftl", type = "freemarker", params = { "contentType", "application/json" }) })
    public String getDataResults() {
        if (Persistable.Base.isNullOrTransient(id)) {
            return ERROR;
        }
        DataTable dataTable = getDataTableService().find(getId());
        Dataset dataset = dataTable.getDataset();
        if (dataset.isPublicallyAccessible() || getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), dataset)) {
            ResultMetadataWrapper selectAllFromDataTable = ResultMetadataWrapper.NULL;
            try {
                selectAllFromDataTable = getDatasetService().selectAllFromDataTable(dataTable, getStartRecord(), getRecordsPerPage(), true,
                        isViewRowSupported());
            } catch (BadSqlGrammarException ex) {
                getLogger().error("Failed to pull datatable results for '{}' (perhaps the table is missing from tdardata schema?)", dataTable.getName());
            }
            setResultsWrapper(selectAllFromDataTable);
            setResults(getResultsWrapper().getResults());
        }
        return SUCCESS;
    }

    public List<List<String>> getResults() {
        return results;
    }

    public void setResults(List<List<String>> results) {
        this.results = results;
    }

    public Integer getStartRecord() {
        return startRecord;
    }

    public void setStartRecord(Integer startRecord) {
        this.startRecord = startRecord;
    }

    public Integer getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(Integer recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResultMetadataWrapper getResultsWrapper() {
        return resultsWrapper;
    }

    public void setResultsWrapper(ResultMetadataWrapper resultsWrapper) {
        this.resultsWrapper = resultsWrapper;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

}
