package org.tdar.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.Facet;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.search.query.facet.FacetedResultHandler;
import org.tdar.search.service.query.ResourceSearchService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.TextProvider;

@Transactional
@Service
public class HomepageServiceImpl implements HomepageService {

    private transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final String _PLURAL = "_PLURAL";
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient ResourceSearchService resourceSearchService;
    @Autowired
    private transient InformationResourceService informationResourceService;
    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient SerializationService serializationService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private GenericService genericService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#getSearchAndHomepageGraphs(org.tdar.core.bean.entity.TdarUser, org.tdar.search.bean.AdvancedSearchQueryObject,
     * org.tdar.search.query.facet.FacetedResultHandler, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized HomepageDetails getSearchAndHomepageGraphs(TdarUser authenticatedUser, AdvancedSearchQueryObject advancedSearchQueryObject,
            FacetedResultHandler<Resource> result, TextProvider provider) {
        setupResultForMapSearch(result);

        try {
            resourceSearchService.buildAdvancedSearch(advancedSearchQueryObject, authenticatedUser, result, provider);
        } catch (SearchException | IOException e1) {
            logger.error("issue generating map search", e1);
        }
        return generateDetails(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#generateDetails(org.tdar.search.query.facet.FacetedResultHandler)
     */
    @Override
    @Transactional(readOnly = true)
    public HomepageDetails generateDetails(FacetedResultHandler<Resource> result) {
        HomepageDetails details = new HomepageDetails();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> locales = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            locales.put(type.name() + _PLURAL, type.getPlural());
            locales.put(type.name(), type.getLabel());
        }
        try {
            details.setLocalesJson(mapper.writeValueAsString(locales));
        } catch (JsonProcessingException e2) {
            logger.error("issue generating locales json", e2);
        }

        List<Map<String, Object>> rtypes = new ArrayList<>();
        try {
            details.setMapJson(result.getFacetWrapper().getFacetPivotJson());

            List<Facet> list = result.getFacetWrapper().getFacetResults().get(QueryFieldNames.RESOURCE_TYPE);
            if (list != null) {
                for (Facet f : list) {
                    Map<String, Object> rtype = new HashMap<>();
                    rtype.put("count", f.getCount());
                    rtype.put("key", f.getRaw());
                    rtype.put("label", locales.get(f.getRaw() + _PLURAL));
                    rtypes.add(rtype);
                }
            }
        } catch (Exception e1) {
            logger.error("issue generating map json", e1);
        }
        try {
            details.setResourceTypeJson(mapper.writeValueAsString(rtypes));
        } catch (JsonProcessingException e) {
            logger.error("issue generating resourceType json", e);
        }
        return details;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#setupResultForMapSearch(org.tdar.search.query.facet.FacetedResultHandler)
     */
    @Override
    @Transactional(readOnly = true)
    public void setupResultForMapSearch(FacetedResultHandler<Resource> result) {
        result.getFacetWrapper().facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class);
        result.getFacetWrapper().setMapFacet(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#getHomepageGraphs(org.tdar.core.bean.entity.TdarUser, java.lang.Long, com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized HomepageDetails getHomepageGraphs(TdarUser authenticatedUser, Long collectionId, boolean isBot, TextProvider provider) {
        AdvancedSearchQueryObject advancedSearchQueryObject = new AdvancedSearchQueryObject();
        if (collectionId != null) {
            SearchParameters sp = new SearchParameters();
            sp.getCollections().add(genericService.find(ResourceCollection.class, collectionId));
            advancedSearchQueryObject.getSearchParameters().add(sp);
        }
        SearchResult<Resource> result = new SearchResult<>();
        result.setFacetWrapper(new FacetWrapper());
        result.setBot(isBot);
        result.setRecordsPerPage(0);

        HomepageDetails details = getSearchAndHomepageGraphs(authenticatedUser, advancedSearchQueryObject, result, provider);
        return details;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#featuredItems(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized Set<Resource> featuredItems(TdarUser authenticatedUser) {
        Set<Resource> featuredResources = new HashSet<>();
        try {
            for (Resource key : informationResourceService.getFeaturedItems()) {
                // perhaps overkill
                genericService.markReadOnly(key);
                if (key instanceof InformationResource) {
                    authorizationService.applyTransientViewableFlag(key, null);
                }
                if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled()) {
                    obfuscationService.obfuscate(key, authenticatedUser);
                }
                featuredResources.add(key);
            }
        } catch (IndexOutOfBoundsException ioe) {
            logger.debug("no featured resources found");
        }
        return featuredResources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#resourceStats()
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized List<HomepageResourceCountCache> resourceStats() {
        List<HomepageResourceCountCache> homepageResourceCountCache = genericService.findAllWithL2Cache(HomepageResourceCountCache.class);
        Iterator<HomepageResourceCountCache> iterator = homepageResourceCountCache.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getResourceType().isSupporting()) {
                iterator.remove();
            }
        }
        return homepageResourceCountCache;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#getMapJson()
     */
    @Override
    @Transactional(readOnly = true)
    public synchronized String getMapJson() {
        List<HomepageGeographicCache> isoGeographicCounts = resourceService.getISOGeographicCounts();
        try {
            return serializationService.convertToJson(isoGeographicCounts);
        } catch (IOException e) {
            logger.error("error creating mapJson", e);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.web.service.HomepageService#getResourceCountsJson()
     */
    @Override
    public synchronized String getResourceCountsJson() {
        try {
            return serializationService.convertToJson(resourceService.getResourceCounts());
        } catch (IOException e) {
            logger.error("error creating mapJson", e);
        }
        return null;
    }

}
