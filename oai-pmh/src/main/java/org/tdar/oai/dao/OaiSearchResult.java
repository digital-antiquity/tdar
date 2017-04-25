package org.tdar.oai.dao;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.OaiDcProvider;
import org.tdar.oai.bean.Token;

public class OaiSearchResult implements Serializable {

	private static final long serialVersionUID = -2232337420440578402L;
    private int resultSize = 0;
    private Token cursor = new Token();
    private int recordsPerPage = 25;
    private List<OaiDcProvider> results;

    public int getResultSize() {
        return resultSize;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
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
        return  recordsPerPage;
    }

    public Token getCursor() {
        return cursor;
    }

    public void setCursor(Token cursor) {
        this.cursor = cursor;
    }
}
