package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.search.query.FacetWrapper;
import org.tdar.search.query.FacetedResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.QueryBuilder;

/**
 * This is a wrapper around the SOLRJ request
 * @author abrin
 *
 * @param <I>
 */
public class SolrSearchObject<I extends Indexable> {

    private List<Long> idList = new ArrayList<>();
    private String sortParam = "";
    private int resultSize;
    private int startRecord;
    private String coreName;

    private QueryBuilder builder;
    private SolrDocumentList documentList;
    private List<I> resultList;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private String queryString;
    private List<String> facetFields = new ArrayList<>();
    private String filterString;
    private Integer totalResults = 0;
    private Integer facetLimit;
    private Integer facetMinCount;

    public SolrSearchObject(QueryBuilder queryBuilder, SearchResultHandler<I> handler) {
        this.builder = queryBuilder;
        this.coreName = queryBuilder.getCoreName();
        this.setMaxResults(handler.getRecordsPerPage());
        this.setFirstResult(handler.getStartRecord());

        List<String> sort = new ArrayList<>();
        if (handler.getSortField() != null) {
            addSortField(handler.getSortField(), sort);
        }
        if (handler.getSecondarySortField() != null) {
            addSortField(handler.getSecondarySortField(), sort);
        }
        if (CollectionUtils.isNotEmpty(sort)) {
            setSortParam(StringUtils.join(sort, ","));
        }
        this.filterString = StringUtils.join(queryBuilder.getFilters(), " ");
        handleFacets(handler);
    }

    private void handleFacets(SearchResultHandler<I> handler) {
        if (handler instanceof FacetedResultHandler) {
            FacetedResultHandler<I> facetedResultHandler = (FacetedResultHandler<I>) handler;
            FacetWrapper wrap = facetedResultHandler.getFacetWrapper();
            if (wrap != null) {
                for (String facet : wrap.getFacetFieldNames()) {
                    if (StringUtils.isNotBlank(facet)) {
                        facetFields.add(facet);
                    }
                }
            }
        }
    }

    private void addSortField(SortOption option, List<String> sort) {
        String sortName = getSortFieldName(option);
        logger.trace("{} - {}", option, sortName);
        if (sortName != null) {
            sort.add( sortName+ " " + option.getSortOrder());
        }
    }

    private String getSortFieldName(SortOption sortField) {
        if (sortField == null) {
            return null;
        }
        switch (sortField) {
            case COLLECTION_TITLE:
            case COLLECTION_TITLE_REVERSE:
            case CREATOR_NAME:
            case CREATOR_NAME_REVERSE:
            case LABEL:
            case LABEL_REVERSE:
            case TITLE:
            case TITLE_REVERSE:
                return QueryFieldNames.NAME_SORT;
            case DATE:
            case DATE_REVERSE:
                return QueryFieldNames.DATE;
            case DATE_UPDATED:
            case DATE_UPDATED_REVERSE:
                return QueryFieldNames.DATE_UPDATED;
            case FIRST_NAME:
            case FIRST_NAME_REVERSE:
                return QueryFieldNames.FIRST_NAME_SORT;
            case ID:
            case ID_REVERSE:
                return QueryFieldNames.ID;
            case LAST_NAME:
            case LAST_NAME_REVERSE:
                return QueryFieldNames.LAST_NAME_SORT;
            case PROJECT:
                return QueryFieldNames.PROJECT_TITLE_SORT;
            case RELEVANCE:
                return "score";
            case RESOURCE_TYPE:
            case RESOURCE_TYPE_REVERSE:
                return QueryFieldNames.RESOURCE_TYPE_SORT;
            default:
                break;
        }
        return null;
    }

    public int getResultSize() {
        return resultSize;
    }

    public void setFirstResult(int startRecord) {
        this.startRecord = startRecord;
    }

    public void setMaxResults(int recordsPerPage) {
        this.resultSize = recordsPerPage;
    }

    public SolrParams getSolrParams() {
        SolrQuery solrQuery = new SolrQuery();
        setQueryString(builder.generateQueryString());
        solrQuery.setParam("q", getQueryString());
        solrQuery.setParam("start", Integer.toString(startRecord));
        solrQuery.setParam("rows", Integer.toString(resultSize));
        if (StringUtils.isNotBlank(filterString)) {
            solrQuery.setParam("fq", filterString);
        }

        if (CollectionUtils.isNotEmpty(facetFields)) {
            solrQuery.addFacetField(facetFields.toArray(new String[0]));
            solrQuery.setParam("facet","on");
        }
        if (facetLimit != null) {
            solrQuery.setFacetLimit(facetLimit);
        }
        if (facetMinCount != null) {
            solrQuery.setFacetMinCount(facetMinCount);
        }

        //        solrQuery.setFacetSort(sort)

        if (StringUtils.isNotBlank(sortParam)) {
            solrQuery.setParam("sort", sortParam);
        }
        Set<String> fieldList = new HashSet<>();
        fieldList.addAll(Arrays.asList(QueryFieldNames._ID, QueryFieldNames.ID, QueryFieldNames.CLASS, "score"));
//        fieldList.addAll(facetFields);
        solrQuery.setParam("fl", StringUtils.join(fieldList, ","));
        if (logger.isTraceEnabled()) {
            logger.trace("{}", solrQuery);
        }
        return solrQuery;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public String getSortParam() {
        return sortParam;
    }

    public void setSortParam(String sortParam) {
        this.sortParam = sortParam;
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    public void processResults(QueryResponse rsp) {
        SolrDocumentList results = rsp.getResults();
        this.setDocumentList(results);
        if (logger.isTraceEnabled()) {
            logger.trace("results:{}", results);
        }
        setTotalResults((int) results.getNumFound());
        for (SolrDocument doc : results) {
            idList.add((Long) doc.get(QueryFieldNames.ID));
        }
    }

    public void setResultList(List<I> resultList) {
        this.resultList = resultList;
    }

    public List<I> getResultList() {
        return resultList;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public SolrDocumentList getDocumentList() {
        return documentList;
    }

    public void setDocumentList(SolrDocumentList documentList) {
        this.documentList = documentList;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<String> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(List<String> facetFields) {
        this.facetFields = facetFields;
    }

    public Integer getFacetMinCount() {
        return facetMinCount;
    }

    public void setFacetMinCount(Integer facetMinCount) {
        this.facetMinCount = facetMinCount;
    }

    public Integer getFacetLimit() {
        return facetLimit;
    }

    public void setFacetLimit(Integer facetLimit) {
        this.facetLimit = facetLimit;
    }

}
