package org.tdar.search.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.QueryBuilder;

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
    private String filterString;
    private Integer totalResults = 0;

    public SolrSearchObject(QueryBuilder queryBuilder, SortOption[] sortOptions, SearchResultHandler<I> handler) {
        this.builder = queryBuilder;
        this.coreName = queryBuilder.getCoreName();
        this.setMaxResults(handler.getRecordsPerPage());
        this.setFirstResult(handler.getStartRecord());

        List<String> sort = new ArrayList<>();
        if (!ArrayUtils.isEmpty(sortOptions)) {
            for (SortOption option : sortOptions) {
                String sortName = getSortFieldName(option);
                logger.trace("{} - {}", option, sortName);
                if (sortName != null) {
                    sort.add( sortName+ " " + option.getSortOrder());
                }
            }
            if (CollectionUtils.isNotEmpty(sort)) {
                setSortParam(StringUtils.join(sort, ","));
            }
        }
        this.filterString = StringUtils.join(queryBuilder.getFilters(), " ");
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
            logger.debug(filterString);
        }
        if (StringUtils.isNotBlank(sortParam)) {
            solrQuery.setParam("sort", sortParam);
        }
        solrQuery.setParam("fl", StringUtils.join(Arrays.asList(QueryFieldNames._ID, QueryFieldNames.ID, QueryFieldNames.CLASS, "score"), ","));

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

    public void processResults(SolrDocumentList results) {
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

}
