package org.tdar.search.service.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;

/**
 * A search object for data in a tdar database. this object contains basic info on a search
 * - project id for limiting
 * - operator
 * - a list of params
 * 
 * @author abrin
 *
 */
public class RowSearchObject implements Serializable {

    private static final long serialVersionUID = 4040213909975852656L;

    private Operator operator = Operator.AND;
    private Long projectId;
    private Integer recordsPerPage;
    private Integer startRecord;
    private Integer totalRecords;

    private List<RowSearchParameters> params = new ArrayList<>();

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<RowSearchParameters> getParams() {
        return params;
    }

    public void setParams(List<RowSearchParameters> params) {
        this.params = params;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
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

}
