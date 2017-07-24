package org.tdar.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao.FindOptions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.utils.PersistableUtils;

@Service
public class ResourceEditControllerService {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ObfuscationService obfuscationService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    public ResourceCollectionService resourceCollectionService;


    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private GenericService genericService;

    

    @Transactional(readOnly = true)
    public void initializeResourceCreatorProxyLists(AuthWrapper<Resource> auth, List<ResourceCreatorProxy> authorshipProxies,
            List<ResourceCreatorProxy> creditProxies) {
        Set<ResourceCreator> resourceCreators = auth.getItem().getResourceCreators();
        if (resourceCreators == null) {
            return;
        }

        // this may be duplicative... check
        for (ResourceCreator rc : resourceCreators) {
            if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled()) {
                if ((rc.getCreatorType() == CreatorType.PERSON) && !auth.isAuthenticated()) {
                    obfuscationService.obfuscate(rc.getCreator(), auth.getAuthenticatedUser());
                }
            }

            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }
        }
    }

    public <T extends Sequenceable<T>> void prepSequence(List<T> list) {
        if (list == null) {
            return;
        }
        if (list.isEmpty()) {
            return;
        }
        list.removeAll(Collections.singletonList(null));
        AbstractSequenced.applySequence(list);
    }

    public void save(AuthWrapper<Resource> authWrapper, ResourceControllerProxy rcp) {
        
        if (rcp.shouldSaveResource()) {
            genericService.saveOrUpdate(authWrapper.getItem());
        }

        if (PersistableUtils.isNotNullOrTransient(rcp.getSubmitter())) {
            TdarUser uploader = genericService.find(TdarUser.class, rcp.getSubmitter().getId());
            authWrapper.getItem().setSubmitter(uploader);
        }

        saveKeywords(authWrapper, rcp);
        saveTemporalContext(authWrapper,rcp);
        saveSpatialContext(authWrapper,rcp);
        saveCitations(authWrapper,rcp);

        prepSequence(rcp.getResourceNotes());
        
        resourceService.saveHasResources(authWrapper.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getResourceNotes(),
                authWrapper.getItem().getResourceNotes(), ResourceNote.class);

        resourceService.saveResourceCreatorsFromProxies(rcp.getResourceCreatorProxies(), authWrapper.getItem(), rcp.shouldSaveResource());

        resolveAnnotations(authWrapper, rcp);
        List<SharedCollection> retainedSharedCollections = new ArrayList<>();
        List<ListCollection> retainedListCollections = new ArrayList<>();
        List<SharedCollection> shares = rcp.getShares();
        List<ListCollection> resourceCollections = rcp.getResourceCollections();

        loadEffectiveResourceCollectionsForSave(authWrapper, retainedSharedCollections, retainedListCollections);
        logger.debug("retained collections:{}", retainedSharedCollections);
        logger.debug("retained list collections:{}", retainedListCollections);
        shares.addAll(retainedSharedCollections);
        resourceCollections.addAll(retainedListCollections);
        
        if (authorizationService.canDo(authWrapper.getAuthenticatedUser(), authWrapper.getItem(), InternalTdarRights.EDIT_ANY_RESOURCE,
                GeneralPermissions.MODIFY_RECORD)) {
            resourceCollectionService.saveResourceCollections(authWrapper.getItem(), shares, authWrapper.getItem().getSharedCollections(),
                    authWrapper.getAuthenticatedUser(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);

            if (!authorizationService.canEdit(authWrapper.getAuthenticatedUser(), authWrapper.getItem())) {
//                addActionError("abstractResourceController.cannot_remove_collection");
                logger.error("user is trying to remove themselves from the collection that granted them rights");
//                addActionMessage("abstractResourceController.collection_rights_remove");
            }
        } else {
            logger.debug("ignoring changes to rights as user doesn't have sufficient permissions");
        }
        resourceCollectionService.saveResourceCollections(authWrapper.getItem(), resourceCollections, authWrapper.getItem().getUnmanagedResourceCollections(),
                authWrapper.getAuthenticatedUser(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, ListCollection.class);


        
        
    }
    
    
    private void loadEffectiveResourceCollectionsForSave(AuthWrapper<Resource> auth, List<SharedCollection> retainedSharedCollections, List<ListCollection> retainedListCollections) {
        logger.debug("loadEffective...");
        for (SharedCollection rc : auth.getItem().getSharedCollections()) {
            if (!authorizationService.canViewCollection(auth.getAuthenticatedUser(),rc)) {
                retainedSharedCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        for (ListCollection rc : auth.getItem().getUnmanagedResourceCollections()) {
            if (!authorizationService.canViewCollection(auth.getAuthenticatedUser(),rc)) {
                retainedListCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        //effectiveResourceCollections.addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(auth.getItem()));
    }


    private void saveTemporalContext(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        // calendar and radiocarbon dates are null for Ontologies
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getCoverageDates(),
                auth.getItem().getCoverageDates(), CoverageDate.class);
        PersistableUtils.reconcileSet(auth.getItem().getTemporalKeywords(),
                genericKeywordService.findOrCreateByLabels(TemporalKeyword.class, rcp.getTemporalKeywords()));
    }


    // return a persisted annotation based on incoming pojo
    private void resolveAnnotations(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        
        
        List<ResourceAnnotation> toAdd = new ArrayList<>();
        for (ResourceAnnotation incomingAnnotation : rcp.getIncomingAnnotations()) {
            if (incomingAnnotation == null) {
                continue;
            }
            ResourceAnnotationKey incomingKey = incomingAnnotation.getResourceAnnotationKey();
            ResourceAnnotationKey resolvedKey = genericService.findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
                    .get(0);
            incomingAnnotation.setResourceAnnotationKey(resolvedKey);

            if (incomingAnnotation.isTransient()) {
                List<String> vals = new ArrayList<>();
                vals.add(incomingAnnotation.getValue());
                cleanupKeywords(vals);

                if (vals.size() > 1) {
                    incomingAnnotation.setValue(vals.get(0));
                    for (int i = 1; i < vals.size(); i++) {
                        toAdd.add(new ResourceAnnotation(resolvedKey, vals.get(i)));
                    }
                }
            }
        }
        rcp.getIncomingAnnotations().addAll(toAdd);
        
        resourceService.saveHasResources((Resource) auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getIncomingAnnotations(),
                auth.getItem().getResourceAnnotations(), ResourceAnnotation.class);

    }

    protected void saveSpatialContext(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        // it won't add a null or incomplete lat-long box.

        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getLatitudeLongitudeBoxes(),
                auth.getItem().getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);

        PersistableUtils.reconcileSet(auth.getItem().getGeographicKeywords(),
                genericKeywordService.findOrCreateByLabels(GeographicKeyword.class, rcp.getGeographicKeywords()));

        resourceService.processManagedKeywords(auth.getItem(), auth.getItem().getLatitudeLongitudeBoxes());
    }

    protected void saveCitations(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS,
                rcp.getRelatedComparativeCollections(),
                auth.getItem().getRelatedComparativeCollections(), RelatedComparativeCollection.class);
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getSourceCollections(),
                auth.getItem().getSourceCollections(), SourceCollection.class);

    }
    private void saveKeywords(AuthWrapper<Resource> authWrapper,ResourceControllerProxy rcp) {

            logger.debug("siteNameKeywords=" + rcp.getSiteNameKeywords());
            logger.debug("materialKeywords=" + rcp.getApprovedMaterialKeywordIds());
            logger.debug("otherKeywords=" + rcp.getOtherKeywords());
            logger.debug("investigationTypes=" + rcp.getInvestigationTypeIds());
            Resource res = authWrapper.getItem();

            cleanupKeywords(rcp.getUncontrolledCultureKeywords());
            cleanupKeywords(rcp.getUncontrolledMaterialKeywords());
            cleanupKeywords(rcp.getUncontrolledSiteTypeKeywords());
            cleanupKeywords(rcp.getSiteNameKeywords());
            cleanupKeywords(rcp.getOtherKeywords());
            cleanupKeywords(rcp.getTemporalKeywords());

            Set<CultureKeyword> culKeys = genericKeywordService.findOrCreateByLabels(CultureKeyword.class, rcp.getUncontrolledCultureKeywords());
            culKeys.addAll(genericService.findAll(CultureKeyword.class, rcp.getApprovedCultureKeywordIds()));
            Set<MaterialKeyword> matKeys = genericKeywordService.findOrCreateByLabels(MaterialKeyword.class, rcp.getUncontrolledMaterialKeywords());
            matKeys.addAll(genericService.findAll(MaterialKeyword.class, rcp.getApprovedMaterialKeywordIds()));

            Set<SiteTypeKeyword> siteTypeKeys = genericKeywordService.findOrCreateByLabels(SiteTypeKeyword.class, rcp.getUncontrolledSiteTypeKeywords());
            siteTypeKeys.addAll(genericService.findAll(SiteTypeKeyword.class, rcp.getApprovedSiteTypeKeywordIds()));

            PersistableUtils.reconcileSet(res.getSiteNameKeywords(), genericKeywordService.findOrCreateByLabels(SiteNameKeyword.class, rcp.getSiteNameKeywords()));
            PersistableUtils.reconcileSet(res.getOtherKeywords(), genericKeywordService.findOrCreateByLabels(OtherKeyword.class, rcp.getOtherKeywords()));
            PersistableUtils.reconcileSet(res.getInvestigationTypes(), genericService.findAll(InvestigationType.class, rcp.getInvestigationTypeIds()));

            PersistableUtils.reconcileSet(res.getCultureKeywords(), culKeys);
            PersistableUtils.reconcileSet(res.getSiteTypeKeywords(), siteTypeKeys);
            PersistableUtils.reconcileSet(res.getMaterialKeywords(), matKeys);
    }
    

    private void cleanupKeywords(List<String> kwds) {

        if (CollectionUtils.isEmpty(kwds)) {
            return;
        }
        String delim = "||";
        Iterator<String> iter = kwds.iterator();
        Set<String> toAdd = new HashSet<>();
        while (iter.hasNext()) {
            String keyword = iter.next();
            if (StringUtils.isBlank(keyword)) {
                continue;
            }

            if (keyword.contains(delim)) {
                for (String sub : StringUtils.split(keyword, delim)) {
                    sub = StringUtils.trim(sub);
                    if (StringUtils.isNotBlank(sub)) {
                        toAdd.add(sub);
                    }
                }
                iter.remove();
            }
        }
        kwds.addAll(toAdd);
    }


}
