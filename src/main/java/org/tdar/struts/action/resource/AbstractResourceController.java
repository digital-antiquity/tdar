package org.tdar.struts.action.resource;

import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_DELETED_RECORDS;
import static org.tdar.core.service.external.auth.InternalTdarRights.SEARCH_FOR_FLAGGED_RECORDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
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
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;
import org.tdar.core.service.ObfuscationService;

import edu.asu.lib.dc.DublinCoreDocument;
import edu.asu.lib.mods.ModsDocument;

/**
 * $Id$
 * 
 * Provides basic metadata support for controllers that manage subtypes of
 * Resource.
 * 
 * Don't extend this class unless you need this metadata to be set.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public abstract class AbstractResourceController<R extends Resource> extends AbstractPersistableController<R> {

    private static final long serialVersionUID = 8620875853247755760L;

    private static final TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

    private List<MaterialKeyword> allMaterialKeywords;
    private List<InvestigationType> allInvestigationTypes;

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private KeywordNode<CultureKeyword> approvedCultureKeywords;

    private List<ResourceCollection> resourceCollections = new ArrayList<ResourceCollection>();
    private List<ResourceCollection> effectiveResourceCollections = new ArrayList<ResourceCollection>();

    // containers for submitted data.
    private List<String> siteNameKeywords;

    private List<Long> materialKeywordIds;
    private List<Long> investigationTypeIds;

    private List<Long> approvedSiteTypeKeywordIds;
    private List<Long> approvedCultureKeywordIds;

    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> uncontrolledCultureKeywords;

    private List<String> otherKeywords;

    private ModsDocument modsDocument;
    private DublinCoreDocument dcDocument;
    private List<String> temporalKeywords;
    private List<String> geographicKeywords;
    private List<LatitudeLongitudeBox> latitudeLongitudeBoxes;
    private List<CoverageDate> coverageDates;
    private boolean confidential;
    private Status status;

    // citation data.
    // private List<String> sourceCitations;
    private List<SourceCollection> sourceCollections;
    // private List<String> relatedCitations;
    private List<RelatedComparativeCollection> relatedComparativeCollections;

    // protected R resource;
    // private Long resourceId;

    private List<ResourceNote> resourceNotes;

    // private ResourceType resourceType;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;

    private List<ResourceAnnotation> resourceAnnotations;
    private Long activeResourceCount;

    @Autowired
    private ObfuscationService obfuscationService;
    
    // protected abstract R loadResourceFromId(final Long resourceId);

    private void initializeResourceCreatorProxyLists() {
        if (getPersistable().getResourceCreators() == null)
            return;
        authorshipProxies = new ArrayList<ResourceCreatorProxy>();
        creditProxies = new ArrayList<ResourceCreatorProxy>();

        for (ResourceCreator rc : getPersistable().getResourceCreators()) {
        	if(rc.getCreatorType() == CreatorType.PERSON && !isAuthenticated()){
        		obfuscationService.obfuscate(rc.getCreator());
        	}
            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }
        }
    }

       
    protected void loadCustomMetadata() {
    };

    @Override
    protected void loadListData() {
        setActiveResourceCount(getResourceService().countResourcesForUserAccess(getAuthenticatedUser()));
    }

    public void setActiveResourceCount(Long countResourcesForUserAccess) {
        this.activeResourceCount = countResourcesForUserAccess;
    }

    public Long getActiveResourceCount() {
        return activeResourceCount;
    }

    @Override
    public String loadMetadata() {
        if (getResource() == null)
            return ERROR;
        loadBasicMetadata();
        loadCustomMetadata();
        getResourceService().incrementAccessCounter(getPersistable());
        return SUCCESS;
    }

    public void delete(R resource) {
        getResourceService().saveRecordToFilestore(resource);

        String logMessage = String.format("%s id:%s deleted by:%s \ttitle:[%s]", resource.getResourceType().getLabel(), resource.getId(),
                getAuthenticatedUser(), StringUtils.abbreviate(resource.getTitle(), 100));

        getResourceService().logResourceModification(resource, getAuthenticatedUser(), logMessage);
    }

    protected void preSaveCallback() {
        if (status == null) {
            status = Status.ACTIVE;
        }
        getPersistable().setStatus(status);
    }

    /**
     * override if needed
     */
    protected void postSaveCleanup() {
    }

    protected void postSaveCallback() {
        if (shouldSaveResource() && getResource() != null) {
            getResourceService().saveRecordToFilestore(getPersistable());
        }

        if (getResource() != null) { // this will happen with the bulk uploader
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", getResource().getResourceType().getLabel(),
                    getAuthenticatedUser(), getResource().getId(), StringUtils.left(getResource().getTitle(), 100));
            logModification(logMessage);
        }
    }

    @Override
    public boolean isCreatable() throws TdarActionException {
        if (getAuthenticatedUser().getContributor()) {
            return true;
        }
        return false;
    }

    // return a persisted annotation based on incoming pojo
    private void resolveAnnotations(Collection<ResourceAnnotation> incomingAnnotations) {
        for (ResourceAnnotation incomingAnnotation : incomingAnnotations) {
            if (incomingAnnotation == null)
                continue;
            ResourceAnnotationKey incomingKey = incomingAnnotation.getResourceAnnotationKey();
            ResourceAnnotationKey resolvedKey = getGenericService().findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
                    .get(0);
            incomingAnnotation.setResourceAnnotationKey(resolvedKey);
        }
    }

    public boolean isEditable() {
        if (isNullOrNew())
            return false;
        return getAuthenticationAndAuthorizationService().canEditResource(getAuthenticatedUser(), getPersistable());
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        if (getResource().isActive()
                || userCan(InternalTdarRights.VIEW_ANYTHING)
                || isEditable()) {
            logger.trace("{} is viewable: {}", getId(), getPersistableClass().getSimpleName());
            return true;
        }

        if (getResource().isDeleted()) {
            logger.debug("resource not viewable because it is deleted: {}", getPersistable());
            throw new TdarActionException(StatusCode.GONE, "this record has been deleted");
        }
        // don't judge me I hate this code too.
        if (getResource().isDraft()) {
            logger.trace("resource not viewable because it is draft: {}", getPersistable());
            throw new TdarActionException(StatusCode.OK.withResultName("draft"), "this record is in draft and is only available to authorized users");
        }

        return false;
    }

    protected void saveKeywords() {
        logger.debug("siteNameKeywords=" + siteNameKeywords);
        logger.debug("materialKeywords=" + materialKeywordIds);
        logger.debug("otherKeywords=" + otherKeywords);
        logger.debug("investigationTypes=" + investigationTypeIds);

        getPersistable().setSiteNameKeywords(getGenericKeywordService().findOrCreateByLabels(SiteNameKeyword.class, siteNameKeywords));

        Set<CultureKeyword> culKeys = getGenericKeywordService().findOrCreateByLabels(CultureKeyword.class, uncontrolledCultureKeywords);
        culKeys.addAll(getGenericKeywordService().findByIds(CultureKeyword.class, approvedCultureKeywordIds));
        getPersistable().setCultureKeywords(culKeys);

        Set<SiteTypeKeyword> siteTypeKeys = getGenericKeywordService().findOrCreateByLabels(SiteTypeKeyword.class, uncontrolledSiteTypeKeywords);
        siteTypeKeys.addAll(getGenericKeywordService().findByIds(SiteTypeKeyword.class, approvedSiteTypeKeywordIds));
        getPersistable().setSiteTypeKeywords(siteTypeKeys);

        getPersistable().setOtherKeywords(getGenericKeywordService().findOrCreateByLabels(OtherKeyword.class, otherKeywords));
        getPersistable().setMaterialKeywords(getGenericKeywordService().findByIds(MaterialKeyword.class, materialKeywordIds));
        getPersistable().setInvestigationTypes(getGenericKeywordService().findByIds(InvestigationType.class, investigationTypeIds));
    }

    protected void saveTemporalContext() {
        // calendar and radiocarbon dates are null for Ontologies
        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, coverageDates,
                getResource().getCoverageDates(), CoverageDate.class);
        getPersistable().setTemporalKeywords(getGenericKeywordService().findOrCreateByLabels(TemporalKeyword.class, temporalKeywords));
    }

    protected void saveSpatialContext() {
        // it won't add a null or incomplete lat-long box.

        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, latitudeLongitudeBoxes,
                getResource().getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);
        getPersistable().setGeographicKeywords(getGenericKeywordService().findOrCreateByLabels(GeographicKeyword.class, geographicKeywords));
        getPersistable().getManagedGeographicKeywords().clear();
        getResourceService().processManagedKeywords(getPersistable(), getPersistable().getLatitudeLongitudeBoxes());
    }

    protected void saveCitations() {
        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS,
                relatedComparativeCollections,
                getResource().getRelatedComparativeCollections(), RelatedComparativeCollection.class);
        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, sourceCollections,
                getResource().getSourceCollections(), SourceCollection.class);

    }

    /**
     * Saves keywords, full / read user access, and confidentiality.
     */
    protected void saveBasicResourceMetadata() {
        if (shouldSaveResource()) {
            getResourceService().saveOrUpdate(getPersistable());
        }

        getResourceCollectionService().saveAuthorizedUsersForResource(getResource(), getAuthorizedUsers(), shouldSaveResource());
        saveKeywords();
        saveTemporalContext();
        saveSpatialContext();
        saveCitations();

        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, resourceNotes,
                getResource().getResourceNotes(), ResourceNote.class);
        saveResourceCreators();

        resolveAnnotations(getResourceAnnotations());

        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, getResourceAnnotations(),
                getResource().getResourceAnnotations(), ResourceAnnotation.class);
        getResourceCollectionService().saveSharedResourceCollections(getResource(), resourceCollections, getResource().getResourceCollections(),
                getAuthenticatedUser(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS);
    }

    protected void logModification(String message) {
        logResourceModification(getPersistable(), message, null);
    }

    protected void saveResourceCreators() {
        List<ResourceCreatorProxy> allProxies = new ArrayList<ResourceCreatorProxy>();
        if (authorshipProxies != null)
            allProxies.addAll(authorshipProxies);
        if (creditProxies != null)
            allProxies.addAll(creditProxies);

        int sequence = 0;
        List<ResourceCreator> incomingResourceCreators = new ArrayList<ResourceCreator>();
        // convert the list of proxies to a list of resource creators
        for (ResourceCreatorProxy proxy : allProxies) {
            if (proxy != null && proxy.isValid()) {
                ResourceCreator resourceCreator = proxy.resolveResourceCreator();
                resourceCreator.setSequenceNumber(sequence++);
                logger.trace("{} - {}", resourceCreator, resourceCreator.getCreatorType());

                getEntityService().findOrSaveResourceCreator(resourceCreator);
                incomingResourceCreators.add(resourceCreator);
                logger.trace("{} - {}", resourceCreator, resourceCreator.getCreatorType());
            } else {
                getLogger().debug("can't create creator from proxy {}", proxy);
            }
        }

        // FIXME: Should this throw errors?
        getResourceService().saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, incomingResourceCreators,
                getResource().getResourceCreators(), ResourceCreator.class);
    }

    public final void loadBasicMetadata() {
        // load all keywords

        setMaterialKeywordIds(toIdList(getResource().getMaterialKeywords()));
        setInvestigationTypeIds(toIdList(getResource().getInvestigationTypes()));

        setUncontrolledCultureKeywords(toSortedStringList(getResource().getUncontrolledCultureKeywords()));
        setApprovedCultureKeywordIds(toIdList(getResource().getApprovedCultureKeywords()));

        setUncontrolledSiteTypeKeywords(toSortedStringList(getResource().getUncontrolledSiteTypeKeywords()));
        setApprovedSiteTypeKeywordIds(toIdList(getResource().getApprovedSiteTypeKeywords()));

        setOtherKeywords(toSortedStringList(getResource().getOtherKeywords()));
        setSiteNameKeywords(toSortedStringList(getResource().getSiteNameKeywords()));
        // load temporal / geographic terms
        setTemporalKeywords(toSortedStringList(getResource().getTemporalKeywords()));
        setGeographicKeywords(toSortedStringList(getResource().getGeographicKeywords()));
        // load spatial context
        getLatitudeLongitudeBoxes().addAll(getResource().getLatitudeLongitudeBoxes());

        // load radiocarbon / calendar dates
        getCoverageDates().addAll(getResource().getCoverageDates());
        // load full access users

        getResourceNotes().addAll(getResource().getResourceNotes());
        getSourceCollections().addAll(getResource().getSourceCollections());
        getRelatedComparativeCollections().addAll(getResource().getRelatedComparativeCollections());
        getAuthorizedUsers().addAll(getResourceCollectionService().getAuthorizedUsersForResource(getResource()));
        getResourceCollections().addAll(getResource().getSharedResourceCollections());
        initializeResourceCreatorProxyLists();
        getResourceAnnotations().addAll(getResource().getResourceAnnotations());
        Set<ResourceCollection> tempSet = new HashSet<ResourceCollection>();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection != null && CollectionUtils.isNotEmpty(collection.getAuthorizedUsers())) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }
        if (getResource().getInternalResourceCollection() != null &&
                CollectionUtils.isNotEmpty(getResource().getInternalResourceCollection().getAuthorizedUsers())) {
            tempSet.add(getResource().getInternalResourceCollection());
        }
        getEffectiveResourceCollections().addAll(tempSet);
    }

    public List<String> getSiteNameKeywords() {
        if (CollectionUtils.isEmpty(siteNameKeywords)) {
            siteNameKeywords = new ArrayList<String>();
        }
        return siteNameKeywords;
    }

    public List<String> getOtherKeywords() {
        if (CollectionUtils.isEmpty(otherKeywords)) {
            otherKeywords = new ArrayList<String>();
        }
        return otherKeywords;
    }

    public List<String> getTemporalKeywords() {
        if (CollectionUtils.isEmpty(temporalKeywords)) {
            temporalKeywords = new ArrayList<String>();
        }
        return temporalKeywords;
    }

    public List<String> getGeographicKeywords() {
        if (CollectionUtils.isEmpty(geographicKeywords)) {
            geographicKeywords = new ArrayList<String>();
        }
        return geographicKeywords;
    }

    public List<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        if (latitudeLongitudeBoxes == null) {
            latitudeLongitudeBoxes = new ArrayList<LatitudeLongitudeBox>();
        }
        return latitudeLongitudeBoxes;
    }

    public void setLatitudeLongitudeBoxes(List<LatitudeLongitudeBox> longitudeLatitudeBox) {
        this.latitudeLongitudeBoxes = longitudeLatitudeBox;
    }

    public List<Long> getMaterialKeywordIds() {
        if (CollectionUtils.isEmpty(materialKeywordIds)) {
            materialKeywordIds = new ArrayList<Long>();
        }
        return materialKeywordIds;
    }

    public R getResource() {
        return getPersistable();
    }

    public void setResource(R resource) {
        logger.debug("setResource: {}", resource);
        setPersistable(resource);
    }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getGenericKeywordService().findAll(MaterialKeyword.class);
            Collections.sort(allMaterialKeywords);
        }
        return allMaterialKeywords;
    }

    public List<CoverageDate> getCoverageDates() {
        if (CollectionUtils.isEmpty(coverageDates)) {
            coverageDates = new ArrayList<CoverageDate>();
        }
        return coverageDates;
    }

    public void setCoverageDates(List<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }

    public List<SourceCollection> getSourceCollections() {
        if (sourceCollections == null) {
            sourceCollections = new ArrayList<SourceCollection>();
        }
        return sourceCollections;
    }

    public List<RelatedComparativeCollection> getRelatedComparativeCollections() {
        if (relatedComparativeCollections == null) {
            relatedComparativeCollections = new ArrayList<RelatedComparativeCollection>();
        }
        return relatedComparativeCollections;
    }

    public TdarConfiguration getTdarConfiguration() {
        return tdarConfiguration;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public boolean isAbleToViewConfidentialFiles() {
        return getEntityService().canViewConfidentialInformation(getAuthenticatedUser(), getPersistable());
    }

    public List<InvestigationType> getAllInvestigationTypes() {
        if (CollectionUtils.isEmpty(allInvestigationTypes)) {
            allInvestigationTypes = getGenericKeywordService().findAll(InvestigationType.class);
            Collections.sort(allInvestigationTypes);
        }
        return allInvestigationTypes;
    }

    public List<Long> getInvestigationTypeIds() {
        if (CollectionUtils.isEmpty(investigationTypeIds)) {
            investigationTypeIds = createListWithSingleNull();
        }
        return investigationTypeIds;
    }

    public KeywordNode<SiteTypeKeyword> getApprovedSiteTypeKeywords() {
        if (approvedSiteTypeKeywords == null) {
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(getGenericKeywordService().findAllApproved(SiteTypeKeyword.class));
        }
        return approvedSiteTypeKeywords;
    }

    public KeywordNode<CultureKeyword> getApprovedCultureKeywords() {
        if (approvedCultureKeywords == null) {
            approvedCultureKeywords = KeywordNode.organizeKeywords(getGenericKeywordService().findAllApproved(CultureKeyword.class));
        }
        return approvedCultureKeywords;
    }

    public List<Long> getApprovedSiteTypeKeywordIds() {
        if (CollectionUtils.isEmpty(approvedSiteTypeKeywordIds)) {
            approvedSiteTypeKeywordIds = createListWithSingleNull();
        }
        return approvedSiteTypeKeywordIds;
    }

    public List<Long> getApprovedCultureKeywordIds() {
        if (CollectionUtils.isEmpty(approvedCultureKeywordIds)) {
            approvedCultureKeywordIds = createListWithSingleNull();
        }
        return approvedCultureKeywordIds;
    }

    public List<String> getUncontrolledSiteTypeKeywords() {
        if (CollectionUtils.isEmpty(uncontrolledSiteTypeKeywords)) {
            uncontrolledSiteTypeKeywords = createListWithSingleNull();
        }
        return uncontrolledSiteTypeKeywords;
    }

    public List<String> getUncontrolledCultureKeywords() {
        if (CollectionUtils.isEmpty(uncontrolledCultureKeywords)) {
            uncontrolledCultureKeywords = createListWithSingleNull();
        }
        return uncontrolledCultureKeywords;
    }

    public ModsDocument getModsDocument() {
        if (modsDocument == null) {
            modsDocument = ModsTransformer.transformAny(getResource());
        }
        return modsDocument;
    }

    @SkipValidation
    @Action(value = "mods", interceptorRefs = { @InterceptorRef("unauthenticatedStack") }, results = {
            @Result(name = "success", type = "jaxbdocument", params = { "documentName", "modsDocument", "formatOutput", "true" })
    })
    public String viewMods() throws TdarActionException {
        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        // checkValidRequest(UserIs.ANONYMOUS, UsersCanModify.NONE, isEditable(), InternalTdarRights.VIEW_ANYTHING);
        return SUCCESS;
    }

    public DublinCoreDocument getDcDocument() {
        if (dcDocument == null) {
            dcDocument = DcTransformer.transformAny(getResource());
        }
        return dcDocument;
    }

    @SkipValidation
    @Action(value = "dc", interceptorRefs = { @InterceptorRef("unauthenticatedStack") }, results = {
            @Result(name = "success", type = "jaxbdocument", params = { "documentName", "dcDocument", "formatOutput", "true" })
    })
    public String viewDc() throws TdarActionException {
        checkValidRequest(RequestType.VIEW, this, InternalTdarRights.VIEW_ANYTHING);
        // checkValidRequest(UserIs.ANONYMOUS, UsersCanModify.NONE, isEditable(), InternalTdarRights.VIEW_ANYTHING);
        return SUCCESS;
    }

    public Status getStatus() {
        return getPersistable().getStatus();
    }

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }

    public List<Status> getStatuses() {
        List<Status> toReturn = new ArrayList<Status>(getResourceService().findAllStatuses());
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(toReturn, Status.DELETED, SEARCH_FOR_DELETED_RECORDS, getAuthenticatedUser());
        getAuthenticationAndAuthorizationService().removeIfNotAllowed(toReturn, Status.FLAGGED, SEARCH_FOR_FLAGGED_RECORDS, getAuthenticatedUser());
        return toReturn;
    }

    public List<CreatorType> getCreatorTypes() {
        // FIXME: move impl to service layer
        return Arrays.asList(CreatorType.values());
    }

    public List<ResourceNote> getResourceNotes() {
        if (resourceNotes == null) {
            resourceNotes = new ArrayList<ResourceNote>();
        }
        Collections.sort(resourceNotes);
        return resourceNotes;
    }

    public void setResourceNotes(List<ResourceNote> resourceNotes) {
        this.resourceNotes = resourceNotes;
    }

    public ResourceNote getBlankResourceNote() {
        return new ResourceNote();
    }

    public List<ResourceNoteType> getNoteTypes() {
        return Arrays.asList(ResourceNoteType.values());
    }

    public List<ResourceCreatorRole> getAllResourceCreatorRoles() {
        // FIXME: move impl to service
        // FIXME: change to SortedSet
        return ResourceCreatorRole.getAll();
    }

    public List<CoverageType> getAllCoverageTypes() {
        List<CoverageType> coverageTypes = new ArrayList<CoverageType>();
        coverageTypes.add(CoverageType.CALENDAR_DATE);
        coverageTypes.add(CoverageType.RADIOCARBON_DATE);
        return coverageTypes;
    }

    public void setSiteNameKeywords(List<String> siteNameKeywords) {
        this.siteNameKeywords = siteNameKeywords;
    }

    public void setApprovedSiteTypeKeywordIds(List<Long> approvedSiteTypeKeywordIds) {
        this.approvedSiteTypeKeywordIds = approvedSiteTypeKeywordIds;
    }

    public void setApprovedCultureKeywordIds(List<Long> approvedCultureKeywordIds) {
        this.approvedCultureKeywordIds = approvedCultureKeywordIds;
    }

    public void setUncontrolledSiteTypeKeywords(List<String> uncontrolledSiteTypeKeywords) {
        this.uncontrolledSiteTypeKeywords = uncontrolledSiteTypeKeywords;
    }

    public void setUncontrolledCultureKeywords(List<String> uncontrolledCultureKeywords) {
        this.uncontrolledCultureKeywords = uncontrolledCultureKeywords;
    }

    public void setOtherKeywords(List<String> otherKeywords) {
        this.otherKeywords = otherKeywords;
    }

    public void setTemporalKeywords(List<String> temporalKeywords) {
        this.temporalKeywords = temporalKeywords;
    }

    public void setGeographicKeywords(List<String> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }

    public void setMaterialKeywordIds(List<Long> materialKeywordIds) {
        this.materialKeywordIds = materialKeywordIds;
    }

    public void setInvestigationTypeIds(List<Long> investigationTypeIds) {
        this.investigationTypeIds = investigationTypeIds;
    }

    public void setSourceCollections(List<SourceCollection> sourceCollections) {
        this.sourceCollections = sourceCollections;
    }

    public void setRelatedComparativeCollections(List<RelatedComparativeCollection> relatedComparativeCitations) {
        this.relatedComparativeCollections = relatedComparativeCitations;
    }

    public List<ResourceCreatorProxy> getAuthorshipProxies() {
        if (CollectionUtils.isEmpty(authorshipProxies)) {
            authorshipProxies = new ArrayList<ResourceCreatorProxy>();
        }
        return authorshipProxies;
    }

    public ResourceCreatorProxy getBlankCreatorProxy() {
        return new ResourceCreatorProxy();
    }

    public CoverageDate getBlankCoverageDate() {
        return new CoverageDate(CoverageType.CALENDAR_DATE);
    }

    public void setAuthorshipProxies(List<ResourceCreatorProxy> authorshipProxies) {
        this.authorshipProxies = authorshipProxies;
    }

    public List<ResourceCreatorProxy> getCreditProxies() {
        if (CollectionUtils.isEmpty(creditProxies)) {
            creditProxies = new ArrayList<ResourceCreatorProxy>();
        }
        return creditProxies;
    }

    public void setCreditProxies(List<ResourceCreatorProxy> creditProxies) {
        this.creditProxies = creditProxies;
    }

    public List<ResourceCreatorRole> getInstitutionAuthorshipRoles() {
        return ResourceCreatorRole.getAuthorshipRoles(CreatorType.INSTITUTION, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getInstitutionCreditRoles() {
        return ResourceCreatorRole.getCreditRoles(CreatorType.INSTITUTION, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getPersonAuthorshipRoles() {
        return ResourceCreatorRole.getAuthorshipRoles(CreatorType.PERSON, getResource().getResourceType());
    }

    public List<ResourceCreatorRole> getPersonCreditRoles() {
        return ResourceCreatorRole.getCreditRoles(CreatorType.PERSON, getResource().getResourceType());
    }

    public List<ResourceAnnotation> getResourceAnnotations() {
        if (resourceAnnotations == null)
            resourceAnnotations = new ArrayList<ResourceAnnotation>();
        return resourceAnnotations;
    }

    public ResourceAnnotation getBlankResourceAnnotation() {
        return new ResourceAnnotation(new ResourceAnnotationKey(), "");
    }

    public ResourceCollection getBlankResourceCollection() {
        return new ResourceCollection(CollectionType.SHARED);
    }

    public SourceCollection getBlankSourceCollection() {
        return new SourceCollection();
    }

    public RelatedComparativeCollection getBlankRelatedComparativeCollection() {
        return new RelatedComparativeCollection();
    }

    public void setResourceAnnotations(List<ResourceAnnotation> resourceAnnotations) {
        this.resourceAnnotations = resourceAnnotations;
    }

    /**
     * @param resourceCollections
     *            the resourceCollections to set
     */
    public void setResourceCollections(List<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    public List<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    /**
     * @return the effectiveResourceCollections
     */
    public List<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    /**
     * @param effectiveResourceCollections
     *            the effectiveResourceCollections to set
     */
    public void setEffectiveResourceCollections(List<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

}
