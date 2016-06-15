package org.tdar.core.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;

@Transactional
@Service
public class HomepageService {

    private transient Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private transient ResourceService resourceService;
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
    
    @Transactional(readOnly=true)
    public synchronized Set<Resource> featuredItems(TdarUser authenticatedUser) {
        Set<Resource> featuredResources = new HashSet<>(); 
        try {
            for (Resource key : informationResourceService.getFeaturedItems()) {
                //perhaps overkill
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

    @Transactional(readOnly=true)
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


    @Transactional(readOnly=true)
    public synchronized String getMapJson() {
        List<HomepageGeographicCache> isoGeographicCounts = resourceService.getISOGeographicCounts();
        try {
            return serializationService.convertToJson(isoGeographicCounts);
        } catch (IOException e) {
            logger.error("error creating mapJson",e);
        }
        return null;
    }


    public synchronized String getResourceCountsJson() {
        try {
            return serializationService.convertToJson(resourceService.getResourceCounts());
        } catch (IOException e) {
            logger.error("error creating mapJson",e);
        }
        return null;
    }

}
