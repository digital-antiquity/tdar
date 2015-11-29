package org.tdar.search.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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

    /*
     * Query query = new MatchAllDocsQuery();
     * if (!queryBuilder.isEmpty()) {
     * query = queryBuilder.buildQuery();
     * }
     * 
     */
    public SolrSearchObject(QueryBuilder queryBuilder, SortOption[] sortOptions, SearchResultHandler handler) {
        this.builder = queryBuilder;
        this.coreName = queryBuilder.getCoreName();
        this.setMaxResults(handler.getRecordsPerPage());
        this.setFirstResult(handler.getStartRecord());

        List<String> sort = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sort)) {
            for (SortOption option : sortOptions) {
                sort.add(option.getSortField() + " " + option.getSortOrder());
            }
        }
        this.filterString = StringUtils.join(queryBuilder.getFilters(), " ");
        setSortParam(StringUtils.join(sort, ","));
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
        solrQuery.setParam("fl", StringUtils.join(Arrays.asList(QueryFieldNames._ID, QueryFieldNames.ID,QueryFieldNames.CLASS, "score"), ","));

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
        logger.debug("results:{}", results);
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

}
