package org.tdar.search.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.slf4j.MDC;
import org.tdar.core.LoggingConstants;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;

/**
 * This is a wrapper around the SOLRJ request
 * 
 * @author abrin
 *
 * @param <I>
 */
public class SolrSearchObject<I extends Indexable> {

    private String sortParam = "";
    private int resultSize;
    private int startRecord;
    private String coreName;

    private Long searchStartTime;
    private Long hydrationStartTime;
    private Long facetStartTime;
    private Long endSearchTime;

    private QueryBuilder builder;
    // solr documents returned
    private SolrDocumentList documentList;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // final solr/lucene query string
    private String queryString;
    // fields to facet on
    private List<String> facetFields = new ArrayList<>();
    private List<String> pivotFields = new ArrayList<>();
    private List<String> statsFields = new ArrayList<>();
    private List<String> filters = new ArrayList<>();
    private Integer totalResults = 0;
    private boolean deemphasizeSupportingTypes = false;
    private ResourceType boostType = null;
    // max # of facets
    private Integer facetLimit;
    // min # of items to show in a facet
    private Integer facetMinCount;

    // a list of the (persistable) ids returned by the query in the appropriate
    // order
    private List<Long> idList = new ArrayList<>();
    // a map of the class (as string) and list of ids grouped, lists are in
    // order. Theoretically
    // this map should only ever have one key
    private Map<String, List<Long>> searchByMap = new HashMap<>();
    private StringBuilder facetText = new StringBuilder();
    private ProjectionModel projection;
    private List<String> boosts = new ArrayList<>();
    public SolrSearchObject(QueryBuilder queryBuilder, LuceneSearchResultHandler<I> handler) {
        this.builder = queryBuilder;
        this.coreName = queryBuilder.getCoreName();
        this.setMaxResults(handler.getRecordsPerPage());
        this.setFirstResult(handler.getStartRecord());
        this.projection = handler.getProjectionModel();

        List<String> sort = new ArrayList<>();
        if (handler.getSortField() != null) {
            addSortField(handler.getSortField(), sort);
        }
        if (handler.getSecondarySortField() != null) {
            addSortField(handler.getSecondarySortField(), sort);
        }
        if (queryBuilder instanceof ResourceQueryBuilder && handler.getSortField() == SortOption.RELEVANCE) {
            ResourceQueryBuilder qb = (ResourceQueryBuilder) queryBuilder;
//            if (qb.getBoostType() != null) {
//                boosts.add(String.format("{!boost b=\"if(exists(query({!v='resourceType:(%s)'})),10,1)\"}", StringUtils.join(qb.getBoostType(), " ")));
//            }
            if (qb.isDeemphasizeSupporting()) {
                boosts.add("{!boost b=\"if(exists(query({!v='resourceType:(ONTOLOGY CODING_SHEET)'})),-10,1)\"} ");
            }
            
        }
        if (CollectionUtils.isNotEmpty(sort)) {
            setSortParam(StringUtils.join(sort, ","));
        }
        handleFacets(handler);
    }

    private void handleFacets(SearchResultHandler<I> handler) {
        if (handler instanceof FacetedResultHandler) {
            FacetedResultHandler<I> facetedResultHandler = (FacetedResultHandler<I>) handler;
            FacetWrapper wrap = facetedResultHandler.getFacetWrapper();
            if (wrap != null) {
                Set<String> facetFieldNames = new HashSet<>(wrap.getFacetFieldNames());
                if (wrap.isMapFacet()) {
                    this.pivotFields = Arrays.asList(QueryFieldNames.ACTIVE_GEOGRAPHIC_ISO, QueryFieldNames.RESOURCE_TYPE);
                    this.statsFields = Arrays.asList(QueryFieldNames.ACTIVE_GEOGRAPHIC_ISO, QueryFieldNames.RESOURCE_TYPE);
                    facetLimit = 10000;
                    facetFieldNames.add(QueryFieldNames.RESOURCE_TYPE);
                }
                for (String facet : facetFieldNames) {
                    if (StringUtils.isNotBlank(facet)) {
                        if (facetText.length() > 0) {
                            facetText.append(", ");
                        }
                        String exclude = "";
                        String filter = wrap.getFilter(facet);
                        if (StringUtils.isNotBlank(filter)) {
                            filters.add(String.format("{!tag=%s}%s:(%s)", facet, facet, filter));
                            exclude = String.format(", domain:{excludeTags:%s}", facet);
                        }
                        facetText.append(String.format("%s:{field:%s, type:terms %s}", facet, facet, exclude));
                    }
                }
                if (facetText.length() > 0) {
                    facetText.insert(0, "{");
                    facetText.append("}");
                }

            }
        }
    }

    private void addSortField(SortOption option, List<String> sort) {
        String sortName = getSortFieldName(option);
        logger.trace("{} - {}", option, sortName);
        if (sortName != null) {
            sort.add(sortName + " " + option.getSortOrder());
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

    public void setFirstResult(int startRecord) {
        this.startRecord = startRecord;
    }

    public void setMaxResults(int recordsPerPage) {
        this.resultSize = recordsPerPage;
    }

    public SolrParams getSolrParams() {
        SolrQuery solrQuery = new SolrQuery();
        setQueryString(StringUtils.join(boosts,"") + builder.generateQueryString());
        solrQuery.setParam("q", getQueryString());
        solrQuery.setParam("start", Integer.toString(startRecord));
        solrQuery.setParam("rows", Integer.toString(resultSize));
        if (!CollectionUtils.isEmpty(statsFields)) {
            solrQuery.setParam("stats", true);
            solrQuery.setParam("stats.field", statsFields.toArray(new String[0]));
        }

        if (!CollectionUtils.isEmpty(pivotFields)) {
            solrQuery.setParam("stats", true);
            solrQuery.setParam("facet.pivot", StringUtils.join(pivotFields,","));
        }

        
        String tag = String.format("p:%s u:%s", MDC.get(LoggingConstants.TAG_PATH), MDC.get(LoggingConstants.TAG_AGENT));
        solrQuery.setParam("_logtag", tag);
        if (facetText.length() > 0 || !CollectionUtils.isEmpty(pivotFields)) {
            solrQuery.setParam("json.facet", facetText.toString());
            solrQuery.setParam("facet", "on");
            // solrQuery.setParam("facet.method", "enum");
        }
        if (CollectionUtils.isNotEmpty(filters)) {
            solrQuery.setParam("fq", filters.toArray(new String[0]));
        }
        if (facetLimit != null) {
            solrQuery.setFacetLimit(facetLimit);
        }
        if (facetMinCount != null) {
            solrQuery.setFacetMinCount(facetMinCount);
        }

        // solrQuery.setFacetSort(sort)

        if (StringUtils.isNotBlank(sortParam)) {
            solrQuery.setParam("sort", sortParam);
        }
        Set<String> fieldList = new HashSet<>(ProjectionModel.getDefaultProjections());
        if (projection != null) {
            fieldList.addAll(projection.getProjections());
        }
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
            Long id = (Long) doc.get(QueryFieldNames.ID);
            String key = (String) doc.get(QueryFieldNames.CLASS);
            if (searchByMap.get(key) == null) {
                searchByMap.put(key, new ArrayList<>());
            }
            idList.add(id);
            searchByMap.get(key).add(id);
        }
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

    public Map<String, List<Long>> getSearchByMap() {
        return this.searchByMap;
    }

    public void markStartHydration() {
        hydrationStartTime = System.currentTimeMillis();
    }

    public void markStartSearch() {
        searchStartTime = System.currentTimeMillis();

    }

    public void markEndSearch() {
        endSearchTime = System.currentTimeMillis();
    }

    public void markStartFacetSearch() {
        facetStartTime = System.currentTimeMillis();
    }

    public Long getLuceneTime() {
        return hydrationStartTime - searchStartTime;
    }

    public Long getHydrationTime() {
        return facetStartTime - hydrationStartTime;
    }

    public Long getFacetTime() {
        return endSearchTime - facetStartTime;
    }

    public Long getTotalSearchTime() {
        return endSearchTime - searchStartTime;
    }

    public List<String> getPivotFields() {
        return pivotFields;
    }

    public void setPivotFields(List<String> pivotFields) {
        this.pivotFields = pivotFields;
    }

    public List<String> getStatsFields() {
        return statsFields;
    }

    public void setStatsFields(List<String> statsFields) {
        this.statsFields = statsFields;
    }

    public ResourceType getBoostType() {
        return boostType;
    }

    public void setBoostType(ResourceType boostType) {
        this.boostType = boostType;
    }

    public boolean isDeemphasizeSupportingTypes() {
        return deemphasizeSupportingTypes;
    }

    public void setDeemphasizeSupportingTypes(boolean deemphasizeSupportingTypes) {
        this.deemphasizeSupportingTypes = deemphasizeSupportingTypes;
    }

    
}
