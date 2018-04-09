package org.tdar.struts.action.datatable;

import java.util.HashMap;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.dataset.ResultMetadataWrapper;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.utils.PersistableUtils;

@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/datatable")
public class DataTableBrowseController extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = -2570627983412022974L;

    private Long id;
    private Integer recordsPerPage = 50;
    private Integer startRecord = 0;
    private String callback;
    private int totalRecords;
    private ResultMetadataWrapper resultsWrapper = new ResultMetadataWrapper();
    private Object resultObject = new HashMap<String, Object>();

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient DatasetService datasetService;

    @Action(value = "browse",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") }, results = {
                    @Result(name = ERROR, type = JSONRESULT, params = { "jsonObject", "resultObject" }),
                    @Result(name = SUCCESS, type = JSONRESULT, params = { "jsonObject", "resultObject" })
            })
    @HttpForbiddenErrorResponseOnly
    public String getDataResults() {
        if (PersistableUtils.isNullOrTransient(id)) {
            return ERROR;
        }
        DataTable dataTable = getGenericService().find(DataTable.class, getId());
        if (PersistableUtils.isNullOrTransient(dataTable)) {
            return ERROR;
        }
        Dataset dataset = dataTable.getDataset();
        if (dataset.isPublicallyAccessible() || authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), dataset)) {
            ResultMetadataWrapper selectAllFromDataTable = ResultMetadataWrapper.NULL;
            try {
                selectAllFromDataTable = datasetService.selectAllFromDataTable(dataTable, getStartRecord(), getRecordsPerPage(), true,
                        getTdarConfiguration().isViewRowSupported());
            } catch (BadSqlGrammarException ex) {
                getLogger().error("Failed to pull datatable results for '{}' (perhaps the table is missing from tdardata schema?)", dataTable.getName());
            }
            setResultsWrapper(selectAllFromDataTable);
            // getLogger().debug("results: {} ", getResultsWrapper().getResults());
        }
        setResultObject(getResultsWrapper());
        return SUCCESS;
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

    public Object getResultObject() {
        return resultObject;
    }

    public void setResultObject(Object resultObject) {
        this.resultObject = resultObject;
    }

}
