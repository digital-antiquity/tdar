package org.tdar.search.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;

public class AdvancedSearchQueryObject implements Serializable {

    private static final long serialVersionUID = -2556511171217353615L;

    private boolean multiCore = false;
    private ReservedSearchParameters reservedParams;
    private Operator operator = Operator.AND;
    private List<SearchParameters> searchParameters = new ArrayList<>();
    private boolean explore = false;
    private boolean collectionSearchBoxVisible = false;
    private String query;
    private String searchPhrase;
    private String refinedBy;
    private List<String> allGeneralQueryFields = new ArrayList<>();
    
    public String getSearchPhrase() {
        return searchPhrase;
    }

    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }

    public String getRefinedBy() {
        return refinedBy;
    }

    public void setRefinedBy(String refinedBy) {
        this.refinedBy = refinedBy;
    }

    public ReservedSearchParameters getReservedParams() {
        if (reservedParams == null) {
            reservedParams = new ReservedSearchParameters();
        }
        return reservedParams;
    }

    public void setReservedParams(ReservedSearchParameters reservedParams) {
        this.reservedParams = reservedParams;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public List<SearchParameters> getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(List<SearchParameters> searchParameters) {
        this.searchParameters = searchParameters;
    }

    public boolean isExplore() {
        return explore;
    }

    public void setExplore(boolean explore) {
        this.explore = explore;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isCollectionSearchBoxVisible() {
        return collectionSearchBoxVisible;
    }

    public void setCollectionSearchBoxVisible(boolean collectionSearchBoxVisible) {
        this.collectionSearchBoxVisible = collectionSearchBoxVisible;
    }

    public List<String> getAllGeneralQueryFields() {
        return allGeneralQueryFields;
    }

    public void setAllGeneralQueryFields(List<String> getAllGeneralQueryFields) {
        this.allGeneralQueryFields = getAllGeneralQueryFields;
    }

    public boolean isMultiCore() {
        return multiCore;
    }

    public void setMultiCore(boolean multiCore) {
        this.multiCore = multiCore;
    }

}
