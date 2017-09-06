package org.tdar.web.service;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.query.facet.FacetedResultHandler;

import com.opensymphony.xwork2.TextProvider;

public interface HomepageService {

    HomepageDetails getSearchAndHomepageGraphs(TdarUser authenticatedUser, AdvancedSearchQueryObject advancedSearchQueryObject,
            FacetedResultHandler<Resource> result, TextProvider provider);

    HomepageDetails generateDetails(FacetedResultHandler<Resource> result);

    void setupResultForMapSearch(FacetedResultHandler<Resource> result);

    HomepageDetails getHomepageGraphs(TdarUser authenticatedUser, Long collectionId, TextProvider provider);

    Set<Resource> featuredItems(TdarUser authenticatedUser);

    List<HomepageResourceCountCache> resourceStats();

    String getMapJson();

    String getResourceCountsJson();

}