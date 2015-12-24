package org.tdar.search.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.PluralLocalizable;
import org.tdar.core.bean.resource.IntegratableOptions;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.service.ObfuscationService;
import org.tdar.search.bean.SolrSearchObject;
import org.tdar.search.query.Facet;
import org.tdar.search.query.FacetWrapper;
import org.tdar.search.query.FacetedResultHandler;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResultHandler;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

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
    public SolrSearchObject<I> search(SolrSearchObject<I> query, SearchResultHandler<I> resultHandler, TextProvider provider) throws ParseException, SolrServerException, IOException {
        QueryResponse rsp = template.query(query.getCoreName(), query.getSolrParams());
        query.processResults(rsp);
        convertProjectedResultIntoObjects(resultHandler, query);
        processFacets(rsp, resultHandler, provider);
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
    public void processFacets(QueryResponse rsp, SearchResultHandler<?> handler,TextProvider provider) {
        if (!(handler instanceof FacetedResultHandler) || CollectionUtils.isEmpty(rsp.getFacetFields() )) {
            return;
        }
        // if (resultHandler.getFacetFields() == null) {
        // return;
        // }
        logger.trace("begin adding facets");
        FacetedResultHandler facetHandler = (FacetedResultHandler) handler;

        for (FacetField field : rsp.getFacetFields()) {
            List<Facet> facet = new ArrayList<>();
            String fieldName = field.getName();
            for (Count c : field.getValues()) {
                String name = c.getName();
                String label = null;
                FacetWrapper facetWrapper = facetHandler.getFacetWrapper();
                Class cls = facetWrapper.getFacetClass(fieldName);
                logger.trace("{}", cls.getName());
                if (NumberUtils.isDigits(name)) {
                    Long id = Long.parseLong(name);
                    HasLabel find = (HasLabel)datasetDao.find(cls, id);
                    label = find.getLabel();
                } 
                
                if (cls.isEnum()) {
                    if (cls.equals(IntegratableOptions.class)) {
                        if (name.equalsIgnoreCase("false")) {
                            name ="NO";
                        } else if (name.equalsIgnoreCase("true")) {
                            name = "YES";
                        }
                    }
                    
                    @SuppressWarnings("unchecked")
                    Enum enum1 = Enum.valueOf(cls, name);
                    if (enum1 instanceof PluralLocalizable && c.getCount() > 1) {
                        label = ((PluralLocalizable)enum1).getPluralLocaleKey();
                    } else  {
                        label = ((Localizable)enum1).getLocaleKey();
                    }
                    label = provider.getText(label);
                }
                logger.trace("  {} - {}",c.getCount(), label);
                if (c.getCount() > 0) {
                    facet.add(new Facet(name, label,c.getCount()));
                }
            }
            FacetWrapper wrapper = facetHandler.getFacetWrapper();
            wrapper.getFacetResults().put(fieldName, facet);
        }
        logger.trace("completed adding facets");
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
                case HIBERNATE_DEFAULT:
                    for (SolrDocument doc : results.getDocumentList()) {
                        I obj = null;
                        Long id = (Long) doc.getFieldValue(QueryFieldNames.ID);
                        String cls_ = (String) doc.getFieldValue(QueryFieldNames.CLASS);
                        try {
                            Class<I> cls = (Class<I>) Class.forName(cls_);
                            obj = datasetDao.find(cls, id);
                        } catch (ClassNotFoundException e) {
                            logger.error("error finding {}: {}", cls_, id, e);
                        }
                        toReturn.add(obj);
                    }
                    break;
                case RESOURCE_PROXY_INVALIDATE_CACHE:
                case RESOURCE_PROXY:
                    toReturn.addAll(
                            (Collection<I>) (Collection<?>) datasetDao.findSkeletonsForSearch(false, (Long[]) results.getIdList().toArray(new Long[0])));
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
        results.setResultList(toReturn);
    }

}
