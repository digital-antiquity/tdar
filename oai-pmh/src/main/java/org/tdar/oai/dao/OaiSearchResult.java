package org.tdar.oai.dao;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.OaiDcProvider;

public class OaiSearchResult implements Serializable {

	private static final long serialVersionUID = -2232337420440578402L;
    private int resultSize = 0;
    private int totalRecords = 0;
    private int startRecord = 0;
    private int recordsPerPage = 25;
    private List<OaiDcProvider> results;

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
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

    public List<OaiDcProvider> getResults() {
        return results;
    }

    public void setResults(List<OaiDcProvider> results) {
        this.results = results;
    }

    public int getNextPageStartRecord() {
        return startRecord + recordsPerPage;
    }
}
