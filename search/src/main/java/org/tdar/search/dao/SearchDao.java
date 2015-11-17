package org.tdar.search.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.jena.atlas.test.Gen;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ObfuscationService;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.search.service.SolrSearchObject;
import org.tdar.utils.PersistableUtils;

@Component
public class SearchDao<I extends Indexable> {

    @Autowired
    private SolrClient template;

    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private ObfuscationService obfuscationService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Perform a search based on the @link QueryBuilder and @link SortOption array.
     *
     * @param queryBuilder
     * @param sortOptions
     * @return
     * @throws ParseException
     * @throws IOException 
     * @throws SolrServerException 
     */
    public SolrSearchObject<I> search(SolrSearchObject<I> query, SearchResultHandler<I> resultHandler) throws ParseException, SolrServerException, IOException {
        QueryResponse rsp = template.query(query.getCoreName(), query.getSolrParams());
        query.processResults(rsp.getResults());
        convertProjectedResultIntoObjects(resultHandler, query);
        processFacets(query, resultHandler);
        logger.trace("completed fulltextquery setup");
        return query;
    }

    /**
     * Generate Facet Requests based on those specified on the @link SearchResultHandler
     *
     * @param ftq
     * @param resultHandler
     */
    @SuppressWarnings("rawtypes")
    public <F extends FacetValue> void processFacets(SolrSearchObject ftq, SearchResultHandler<?> resultHandler) {
//        if (resultHandler.getFacetFields() == null) {
//            return;
//        }

        // for (FacetGroup<? extends Enum> facet : resultHandler.getFacetFields()) {
        // FacetingRequest facetRequest = queryBuilder.facet().name(facet.getFacetField())
        // .onField(facet.getFacetField()).discrete().orderedBy(FacetSortOrder.COUNT_DESC)
        // .includeZeroCounts(false).createFacetingRequest();
        // ftq.getFacetManager().enableFaceting(facetRequest);
        // for (Facet facetResult : ftq.getFacetManager().getFacets(facet.getFacetField())) {
        // facet.add(facetResult.getValue(), facetResult.getCount());
        // }
        // }
    }

    /**
     * Taking the projected List<Object[]> and converting them back into something we can use; if using projection, we hydrate those field
     * we use "projection" to add the score and possibly the explanation in... but we also use it to get back simpler results
     * so we can control things like the JSON lookups to make them superfast because we just need "certain" fields, most of these
     * will just have an "ID" that we hydrate later on
     *
     * @param resultHandler
     * @param projections
     * @param list
     * @param user
     * @return
     */
    private void convertProjectedResultIntoObjects(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
        List<I> toReturn = new ArrayList<>();
        ProjectionModel projectionModel = resultHandler.getProjectionModel();
        if (projectionModel == null) {
            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
        }
        // iterate over all of the objects and create an objectMap if needed
        if (CollectionUtils.isNotEmpty(results.getIdList())) {
            switch (projectionModel) {
                case HIBERNATE_DEFAULT:
                    for (SolrDocument doc : results.getDocumentList()) {
                        I obj = null;
                        Long id = (Long)doc.getFieldValue(QueryFieldNames.ID);
                        String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
                        try {
                            Class<I> cls = (Class<I>) Class.forName(cls_);
                            obj = datasetDao.find(cls, id);
                        } catch (ClassNotFoundException e) {
                            logger.error("error finding {}: {}", cls_, id,e );
                        }
                        toReturn.add(obj);
                    }
                    break;
                case RESOURCE_PROXY_INVALIDATE_CACHE:
                case RESOURCE_PROXY:
                    toReturn.addAll((Collection<I>)(Collection<?>)datasetDao.findSkeletonsForSearch(false, (Long[])results.getIdList().toArray(new Long[0])));
                    break;
                default:
                    break;
            }

            for (I p : toReturn) {
                if (PersistableUtils.isNullOrTransient(p)) {
                    continue;
                }
                if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled()
                        && PersistableUtils.isNullOrTransient(resultHandler.getAuthenticatedUser())) {
                    obfuscationService.obfuscate((Obfuscatable) p, resultHandler.getAuthenticatedUser());
                }
                obfuscationService.getAuthenticationAndAuthorizationService().applyTransientViewableFlag(p, resultHandler.getAuthenticatedUser());
            }
        }
        resultHandler.setResults(toReturn);
        logger.debug("results: {}",toReturn);
        results.setResultList(toReturn);
    }

//    /**
//     * This method takes the projects from the search handler (if there are any) and adds them to a projection list that we're managing
//     *
//     * @param resultHandler
//     * @param ftq
//     * @return
//     */
//    private List<String> setupProjectionsForSearch(SearchResultHandler<?> resultHandler, SolrSearchObject ftq) {
//        List<String> projections = new ArrayList<>();
//        projections.add(ProjectionConstants.THIS); // Hibernate Object
//        projections.add(ProjectionConstants.OBJECT_CLASS); // class to project
//
//        ProjectionModel projectionModel = resultHandler.getProjectionModel();
//        if (projectionModel == null) {
//            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
//        }
//
//        if (projectionModel != ProjectionModel.HIBERNATE_DEFAULT) { // OVERRIDE CASE, PROJECTIONS SET IN RESULTS HANDLER
//            projections.remove(ProjectionConstants.THIS);
//            projections.addAll(resultHandler.getProjectionModel().getProjections());
//        }
//
//        projections.add(ProjectionConstants.SCORE);
//        if (resultHandler.isDebug()) {
//            logger.debug("debug mode on for results handling");
//            projections.add(ProjectionConstants.EXPLANATION);
//        }
//        return projections;
//    }

//    /**
//     * Takes the projected object and list of projections and turns them back into an object we can use, often just with Id
//     *
//     * @param resultHandler
//     * @param projections
//     * @param ids
//     * @return
//     */
//    private void createSpareObjectFromProjection(SearchResultHandler<I> resultHandler, SolrSearchObject<I> results) {
//        List<I> toReturn = new ArrayList<>();
//        ProjectionModel projectionModel = resultHandler.getProjectionModel();
//        if (projectionModel == null) {
//            projectionModel = ProjectionModel.HIBERNATE_DEFAULT;
//        }
//        for (Long id : results.getIdList()) {
//
//            try {
//                I p = results.getObjectClass().newInstance();
//                p.setId(id);
//                toReturn.add(p);
//            } catch (Exception e) {
//                throw new TdarRecoverableRuntimeException(e);
//            }
//
//        }
//        results.getResultList().addAll(toReturn);
//    }

}
