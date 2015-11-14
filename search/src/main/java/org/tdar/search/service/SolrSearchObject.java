package org.tdar.search.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.QueryBuilder;

public class SolrSearchObject {

    private List<Long> idList = new ArrayList<>();
    private Class<? extends Indexable> objectClass;
    private String sortParam = "";
    private int resultSize;
    private int startRecord;
    private String coreName;
    
    private QueryBuilder builder;
    private SolrDocumentList documentList;
    private List<Indexable> resultList;
    /*
     *         Query query = new MatchAllDocsQuery();
            if (!queryBuilder.isEmpty()) {
                query = queryBuilder.buildQuery();
            }

     */
    public SolrSearchObject(QueryBuilder queryBuilder, SortOption[] sortOptions) {
        this.builder = queryBuilder;
        this.coreName = queryBuilder.getCoreName();
        List<String> sort = new ArrayList<>();
        for (SortOption option : sortOptions) {
            sort.add(option.getSortField() + " " + option.getSortOrder());
        }
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
        SolrQuery solrQuery = new  SolrQuery();
        solrQuery.add("q", builder.generateQueryString());
        solrQuery.add("start", Integer.toString(startRecord));
        solrQuery.add("rows", Integer.toString(resultSize));
        solrQuery.add("sort", sortParam);
        solrQuery.add("fl","id,score");
        
        solrQuery.setParam("q",builder.generateQueryString());
        return solrQuery;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public Class<? extends Indexable> getObjectClass() {
        return objectClass;
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
        this.documentList = results;
        for (SolrDocument doc : results) {
            idList.add((Long)doc.get(QueryFieldNames.ID));
        }
    }

    public void setResultList(List<Indexable> resultList) {
        this.resultList = resultList;
    }

    public List<? extends Indexable> getResultList() {
        return resultList;
    }

}
