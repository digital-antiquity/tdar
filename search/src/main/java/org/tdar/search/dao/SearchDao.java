package org.tdar.search.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.ObfuscationService;
import org.tdar.search.bean.SolrSearchObject;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.util.SolrMapConverter;
import org.tdar.utils.PersistableUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.TextProvider;

@Component
public class SearchDao<I extends Indexable> {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    @Autowired
    private SolrClient template;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private ObfuscationService obfuscationService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private boolean groupedSearchMode = true;

    /**
     * Perform a search based on the @link QueryBuilder and @link SortOption
     * array.
     *
     * @param queryBuilder
     * @param sortOptions
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws SolrServerException
     */
    public SolrSearchObject<I> search(SolrSearchObject<I> query, LuceneSearchResultHandler<I> resultHandler,
            TextProvider provider) throws ParseException, SolrServerException, IOException {
        query.markStartSearch();
        SolrParams solrParams = query.getSolrParams();
        if (logger.isTraceEnabled()) {
            logger.trace(solrParams.toQueryString());
        }
        QueryResponse rsp = template.query(query.getCoreName(), solrParams);
        query.processResults(rsp);
        if (logger.isTraceEnabled()) {
            logger.trace(rsp.toString());
        }
        convertProjectedResultIntoObjects(resultHandler, query);
        query.markStartFacetSearch();
        processFacets(rsp, resultHandler, provider);
        logger.trace("completed fulltextquery setup");
        query.markEndSearch();
        return query;
    }

    /**
     * Generate Facet Requests based on those specified on the @link
     * SearchResultHandler
     *
     * @param ftq
     * @param resultHandler
     */
    @SuppressWarnings("rawtypes")
    public void processFacets(QueryResponse rsp, SearchResultHandler<?> handler, TextProvider provider) {
        // the JSON faceting API is not supported by solrJ -- supporting here
        SimpleOrderedMap facetMap = (SimpleOrderedMap) rsp.getResponse().get("facets");
        if (!(handler instanceof FacetedResultHandler)
                || (CollectionUtils.isEmpty(rsp.getFacetFields()) && facetMap == null)) {
            return;
        }
        logger.trace("begin adding facets");
        FacetedResultHandler facetHandler = (FacetedResultHandler) handler;
        FacetWrapper wrapper = facetHandler.getFacetWrapper();
        handleJsonFacetingApi(rsp, facetMap, wrapper);
        SimpleOrderedMap pivot = (SimpleOrderedMap) rsp.getResponse().findRecursive("facet_counts", "facet_pivot");
        Object map = SolrMapConverter.toMap(pivot);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String writeValueAsString = mapper.writeValueAsString(map);
            logger.trace(writeValueAsString);
            wrapper.setFacetPivotJson(writeValueAsString);
        } catch (JsonProcessingException e) {
            logger.error("{}", e, e);
        }
        Map<String, List<Facet>> facetResults = wrapper.getFacetResults();
        for (FacetField field : rsp.getFacetFields()) {
            String fieldName = field.getName();
            Class facetClass = facetHandler.getFacetWrapper().getFacetClass(fieldName);
            if (Indexable.class.isAssignableFrom(facetClass)) {
                facetResults.put(fieldName, hydratePersistableFacets(field, facetClass));
            }

            if (facetClass.isEnum()) {
                facetResults.put(fieldName, hydrateEnumFacets(provider, field, facetClass));
            }
        }
        logger.trace("completed adding facets");
    }

    // http://yonik.com/multi-select-faceting/
    private void handleJsonFacetingApi(QueryResponse rsp, SimpleOrderedMap<?> facetMap, FacetWrapper wrapper) {
        if (facetMap != null) {
            for (String field : wrapper.getFacetFieldNames()) {
                SimpleOrderedMap<?> object = (SimpleOrderedMap<?>) facetMap.get(field);
                if (object == null || object.get("buckets") == null) {
                    continue;
                }
                List<?> list = (List<?>) object.get("buckets");
                FacetField fld = new FacetField(field);
                for (Object obj : list) {
                    SimpleOrderedMap<?> f = (SimpleOrderedMap<?>) obj;
                    fld.add(f.get("val").toString(), ((Number) f.get("count")).longValue());

                }
                rsp.getFacetFields().add(fld);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    protected List<Facet> hydrateEnumFacets(TextProvider provider, FacetField field, Class facetClass) {
        List<Facet> facet = new ArrayList<>();
        for (Count c : field.getValues()) {
            String name = c.getName();
            String label = null;
            if (facetClass.equals(IntegratableOptions.class)) {
                // issue with how solr handles Yes/no values, it treats them as
                // booleans
                if (name.equalsIgnoreCase("false")) {
                    name = IntegratableOptions.NO.name();
                } else if (name.equalsIgnoreCase("true")) {
                    name = IntegratableOptions.YES.name();
                }
            }

            @SuppressWarnings("unchecked")
            Enum enum1 = Enum.valueOf(facetClass, name);
            if (enum1 instanceof PluralLocalizable && c.getCount() > 1) {
                label = ((PluralLocalizable) enum1).getPluralLocaleKey();
            } else {
                label = ((Localizable) enum1).getLocaleKey();
            }
            label = provider.getText(label);
            logger.trace("  {} - {}", c.getCount(), label);
            if (c.getCount() > 0) {
                facet.add(new Facet(name, label, c.getCount(), facetClass));
            }
        }
        return facet;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<Facet> hydratePersistableFacets(FacetField field, Class facetClass) {
        List<Long> ids = new ArrayList<>();
        List<Facet> facet = new ArrayList<>();
        for (Count c : field.getValues()) {
            if (c.getCount() > 0) {
                ids.add(Long.parseLong(c.getName()));
            }
        }

        Map<Long, Persistable> idMap = PersistableUtils.createIdMap((Collection<Persistable>) datasetDao.findAll(facetClass, ids));
        for (Count c : field.getValues()) {
            if (c.getCount() > 0) {
                HasLabel persistable = (HasLabel) idMap.get(Long.parseLong(c.getName()));
                String label = persistable.getLabel();
                logger.trace("  {} - {}", c.getCount(), label);
                Facet f = new Facet(c.getName(), label, c.getCount(), facetClass);
                if (persistable instanceof Addressable) {
                    f.setDetailUrl(((Addressable) persistable).getDetailUrl());
                }
                facet.add(f);
            }
        }
        return facet;
    }

    /**
     * Taking the projected List<Object[]> and converting them back into
     * something we can use; if using projection, we hydrate those field we use
     * "projection" to add the score and possibly the explanation in... but we
     * also use it to get back simpler results so we can control things like the
     * JSON lookups to make them superfast because we just need "certain"
     * fields, most of these will just have an "ID" that we hydrate later on
     *
     * @param resultHandler
     * @param projections
     * @param list
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    private void convertProjectedResultIntoObjects(LuceneSearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
        List<I> toReturn = new ArrayList<>();
        results.markStartHydration();
        ProjectionModel projectionModel = resultHandler.getProjectionModel();
        if (projectionModel == null) {
            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
        }
        // iterate over all of the objects and create an objectMap if needed
        if (CollectionUtils.isNotEmpty(results.getIdList())) {
            switch (projectionModel) {
                case LUCENE:
                    for (SolrDocument doc : results.getDocumentList()) {
                        I obj = null;
                        Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
                        String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
                        try {
                            Class<I> cls = (Class<I>) Class.forName(cls_);
                            I r = cls.newInstance();
                            r.setId(id);
                        } catch (ClassNotFoundException e) {
                            logger.error("error finding {}: {}", cls_, id, e);
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error("error finding {}: {}", cls_, id, e);
                        }
                        toReturn.add(obj);
                    }
                    break;
                case LUCENE_EXPERIMENTAL:
                    hydrateExperimental(resultHandler, results, toReturn);
                    break;

                case HIBERNATE_DEFAULT:
                    if (groupedSearchMode) {
                        // try to group the results together to improve the DB query
                        // performance by removing multiple connections,
                        // theoretically, this should be faster.
                        toReturn = processGroupSearch(resultHandler, results);
                    } else {
                        // serial queries
                        toReturn = processSerialSearch(resultHandler, results);
                    }
                    break;
                case RESOURCE_PROXY:
                    for (I i : (List<I>) datasetDao.findSkeletonsForSearch(false, results.getIdList())) {
                        obfuscateAndMarkViewable(resultHandler, i);
                        toReturn.add((I) i);
                    }
                    break;
                default:
                    break;
            }
        }
        resultHandler.setResults(toReturn);
    }

    @Autowired
    ProjectionTransformer<I> projectionTransformer;

    @SuppressWarnings("unchecked")
    private void hydrateExperimental(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results, List<I> toReturn) {
        for (SolrDocument doc : results.getDocumentList()) {
            Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
            String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
            try {
                Class<I> cls = (Class<I>) Class.forName(cls_);
                I r = cls.newInstance();
                r.setId(id);
                if (r instanceof Resource) {
                    logger.trace("{}", doc);
                    r = projectionTransformer.transformResource(resultHandler, doc, r, obfuscationService);

                }
                toReturn.add(r);
            } catch (ClassNotFoundException e) {
                logger.error("error finding {}: {}", cls_, id, e);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("error finding {}: {}", cls_, id, e);
            }
        }
    }

    private void obfuscateAndMarkViewable(SearchResultHandler<I> resultHandler, I p) {
        if (PersistableUtils.isNullOrTransient(p)) {
            return;
        }
        if (CONFIG.obfuscationInterceptorDisabled()
                && PersistableUtils.isNullOrTransient(resultHandler.getAuthenticatedUser())) {
            obfuscationService.obfuscate((Obfuscatable) p, resultHandler.getAuthenticatedUser());
        }
        obfuscationService.getAuthenticationAndAuthorizationService().applyTransientViewableFlag(p,
                resultHandler.getAuthenticatedUser());
    }

    @SuppressWarnings("unchecked")
    private List<I> processSerialSearch(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
        List<I> toReturn = new ArrayList<>();
        for (SolrDocument doc : results.getDocumentList()) {
            I obj = null;
            Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
            String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
            try {
                Class<I> cls = (Class<I>) Class.forName(cls_);
                obj = datasetDao.find(cls, id);
                obfuscateAndMarkViewable(resultHandler, obj);
            } catch (ClassNotFoundException e) {
                logger.error("error finding {}: {}", cls_, id, e);
            }
            toReturn.add(obj);
        }
        return toReturn;
    }

    /**
     * For the results... group the results into a Map<Indexable class, List
     * <Long>>. Then, group the queries in the database together to get a group
     * of results.
     * 
     * With those groups of results, insert them into the appropriate positions
     * in a static array (so we don't have to worry about initial order or
     * sorting), and return that.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List<I> processGroupSearch(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
        Map<String, List<Long>> coalesce = results.getSearchByMap();
        List<Long> idList = results.getIdList();
        Object[] elements = new Object[idList.size()];
        for (String cls : coalesce.keySet()) {
            try {
                Class<I> cls_ = (Class<I>) Class.forName(cls);
                for (I i : datasetDao.findAll(cls_, coalesce.get(cls))) {
                    Long id = i.getId();
                    obfuscateAndMarkViewable(resultHandler, i);
                    elements[idList.indexOf(id)] = i;
                }
            } catch (Exception e) {
                logger.error("exception in searching", e);
            }
        }
        return (List<I>) (List) Arrays.asList(elements);
    }

}
