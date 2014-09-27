package org.tdar.struts.action.resource;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Persistable.Sequence;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.billing.AccountService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.transform.MetaTag;
import org.tdar.transform.OpenUrlFormatter;
import org.tdar.transform.ScholarMetadataTransformer;
import org.tdar.utils.EmailMessageType;

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
@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/resource")
public class AbstractResourceViewAction<R> extends AbstractPersistableViewableAction<Resource>  {

    private static final long serialVersionUID = 896347341133309643L;

//    public static final String RESOURCE_EDIT_TEMPLATE = "../resource/edit-template.ftl";

    private List<MaterialKeyword> allMaterialKeywords;
    private List<InvestigationType> allInvestigationTypes;
    private List<EmailMessageType> emailTypes = EmailMessageType.valuesWithoutConfidentialFiles();
    private String submitterProperName = "";

    @Autowired
    private transient DatasetService datasetService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    private boolean hasDeletedFiles = false;

    @Autowired
    private XmlService xmlService;

    @Autowired
    private BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private ObfuscationService obfuscationService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    public ResourceCollectionService resourceCollectionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private ResourceService resourceService;

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private KeywordNode<CultureKeyword> approvedCultureKeywords;

    private List<ResourceCollection> resourceCollections = new ArrayList<>();
    private List<ResourceCollection> effectiveResourceCollections = new ArrayList<>();

    private List<ResourceRelationship> resourceRelationships = new ArrayList<>();

    // containers for submitted data.
    private List<String> siteNameKeywords;

    private List<Long> materialKeywordIds;
    private List<Long> investigationTypeIds;

    private List<Long> approvedSiteTypeKeywordIds;
    private List<Long> approvedCultureKeywordIds;

    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> uncontrolledCultureKeywords;

    private List<String> otherKeywords;

    private Person submitter;
    private List<String> temporalKeywords;
    private List<String> geographicKeywords;
    private List<LatitudeLongitudeBox> latitudeLongitudeBoxes;
    private List<CoverageDate> coverageDates;
    // citation data.
    // private List<String> sourceCitations;
    private List<SourceCollection> sourceCollections;
    // private List<String> relatedCitations;
    private List<RelatedComparativeCollection> relatedComparativeCollections;
    private Long accountId;
    private Set<Account> activeAccounts;

    private List<ResourceNote> resourceNotes;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;
    private List<ResourceCreatorProxy> contactProxies;

    private List<ResourceAnnotation> resourceAnnotations;

    private List<ResourceCollection> viewableResourceCollections;

    private Project project;

    private void initializeResourceCreatorProxyLists(boolean isViewPage) {
        Set<ResourceCreator> resourceCreators = getPersistable().getResourceCreators();
        if (isViewPage) {
            resourceCreators = getPersistable().getActiveResourceCreators();
        }
        if (resourceCreators == null) {
            return;
        }
        authorshipProxies = new ArrayList<>();
        creditProxies = new ArrayList<>();

        // this may be duplicative... check
        for (ResourceCreator rc : resourceCreators) {
            if (getTdarConfiguration().obfuscationInterceptorDisabled()) {
                if ((rc.getCreatorType() == CreatorType.PERSON) && !isAuthenticated()) {
                    obfuscationService.obfuscate(rc.getCreator(), getAuthenticatedUser());
                }
            }

            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }

            if (ResourceCreatorRole.CONTACT == proxy.getRole()) {
                getContactProxies().add(proxy);
            }
        }
    }

    
    @HttpOnlyIfUnauthenticated
    @Override
    @Action(value = VIEW,
            results = {
                    @Result(name = SUCCESS, location = "../resource/view-template.ftl"),
                    @Result(name = INPUT, type = HTTPHEADER, params = { "error", "404" }),
                    @Result(name = DRAFT, location = "/WEB-INF/content/errors/resource-in-draft.ftl")
            })
    /**
     * FIXME: appears to only override the SUCCESS result type compared to AbstractPersistableController's declaration,
     * see if it's possible to do this with less duplicatiousness
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#save()
     */
    public String view() throws TdarActionException {
        return super.view();
    }

    protected void loadCustomMetadata() throws TdarActionException {
    }

    public String getOpenUrl() {
        return OpenUrlFormatter.toOpenURL(getResource());
    }

    public String getGoogleScholarTags() throws Exception {
        ScholarMetadataTransformer trans = new ScholarMetadataTransformer();
        StringWriter sw = new StringWriter();
        for (MetaTag tag : trans.convertResourceToMetaTag(getResource())) {
            xmlService.convertToXMLFragment(MetaTag.class, tag, sw);
            sw.append("\n");
        }
        return sw.toString();
    }

    // Return list of acceptable billing accounts. If the resource has an account, this method will include it in the returned list even
    // if the user does not have explicit rights to the account (e.g. so that a user w/ edit rights on the resource can modify the resource
    // and maintain original billing account).
    protected List<Account> determineActiveAccounts() {
        List<Account> accounts = new LinkedList<>(accountService.listAvailableAccountsForUser(getAuthenticatedUser()));
        if (getResource() != null) {
            Account resourceAccount = getResource().getAccount();
            if ((resourceAccount != null) && !accounts.contains(resourceAccount)) {
                accounts.add(0, resourceAccount);
            }
        }
        return accounts;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String loadViewMetadata() throws TdarActionException {
        if (getResource() == null) {
            return ERROR;
        }
        loadBasicViewMetadata();
        loadCustomViewMetadata();
        resourceService.updateTransientAccessCount(getResource());
        // don't count if we're an admin
        if (!Persistable.Base.isEqual(getPersistable().getSubmitter(), getAuthenticatedUser()) && !isEditor()) {
            resourceService.incrementAccessCounter(getPersistable());
        }
        accountService.updateTransientAccountInfo((List<Resource>) Arrays.asList(getResource()));
        bookmarkedResourceService.applyTransientBookmarked(Arrays.asList(getResource()), getAuthenticatedUser());
        if (isEditor()) {
            if (getPersistableClass().equals(Project.class)) {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(null, null, null, Arrays.asList(getId()), null));
            } else {
                setUploadedResourceAccessStatistic(resourceService.getResourceSpaceUsageStatistics(null, Arrays.asList(getId()), null, null, null));
            }
        }

        return SUCCESS;
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        boolean result = authorizationService.isResourceViewable(getAuthenticatedUser(), getResource());
        getLogger().debug("is viewable: {}, status: {}",result, getResource().getStatus());
        if (result == false) {
            if (getResource().isDeleted()) {
                getLogger().debug("resource not viewable because it is deleted: {}", getResource());
                throw new TdarActionException(StatusCode.GONE, getText("abstractResourceController.resource_deleted"));
            }

            if (getResource().isDraft()) {
                getLogger().trace("resource not viewable because it is draft: {}", getResource());
                throw new TdarActionException(StatusCode.OK.withResultName(DRAFT),
                        getText("abstractResourceController.this_record_is_in_draft_and_is_only_available_to_authorized_users"));
            }

        }
        return result;
    }

    public <T extends Sequenceable<T>> void prepSequence(List<T> list) {
        if (list == null) {
            return;
        }
        if (list.isEmpty()) {
            return;
        }
        list.removeAll(Collections.singletonList(null));
        Sequence.applySequence(list);
    }

    public void loadBasicMetadata() {
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
        Collections.sort(getResourceNotes());
        getSourceCollections().addAll(getResource().getSourceCollections());
        getRelatedComparativeCollections().addAll(getResource().getRelatedComparativeCollections());
        getAuthorizedUsers().addAll(resourceCollectionService.getAuthorizedUsersForResource(getResource(), getAuthenticatedUser()));
        for (AuthorizedUser au : getAuthorizedUsers()) {
            String name = null;
            if (au != null && au.getUser() != null) {
                name = au.getUser().getProperName();
            }
            getAuthorizedUsersFullNames().add(name);
        }
        initializeResourceCreatorProxyLists(false);
        getResourceAnnotations().addAll(getResource().getResourceAnnotations());
        loadEffectiveResourceCollections();
    }

    public void loadBasicViewMetadata() {
        getAuthorizedUsers().addAll(resourceCollectionService.getAuthorizedUsersForResource(getResource(), getAuthenticatedUser()));
        initializeResourceCreatorProxyLists(true);
        loadEffectiveResourceCollections();
    }

    private void loadEffectiveResourceCollections() {
        getResourceCollections().addAll(getResource().getSharedResourceCollections());
        getEffectiveResourceCollections().addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(getResource()));
    }

    public List<String> getSiteNameKeywords() {
        if (CollectionUtils.isEmpty(siteNameKeywords)) {
            siteNameKeywords = new ArrayList<>();
        }
        return siteNameKeywords;
    }

    public List<String> getOtherKeywords() {
        if (CollectionUtils.isEmpty(otherKeywords)) {
            otherKeywords = new ArrayList<>();
        }
        return otherKeywords;
    }

    public List<String> getTemporalKeywords() {
        if (CollectionUtils.isEmpty(temporalKeywords)) {
            temporalKeywords = new ArrayList<>();
        }
        return temporalKeywords;
    }

    public List<String> getGeographicKeywords() {
        if (CollectionUtils.isEmpty(geographicKeywords)) {
            geographicKeywords = new ArrayList<>();
        }
        return geographicKeywords;
    }

    public List<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        if (latitudeLongitudeBoxes == null) {
            latitudeLongitudeBoxes = new ArrayList<>();
        }
        return latitudeLongitudeBoxes;
    }

    public void setLatitudeLongitudeBoxes(List<LatitudeLongitudeBox> longitudeLatitudeBox) {
        this.latitudeLongitudeBoxes = longitudeLatitudeBox;
    }

    public List<Long> getMaterialKeywordIds() {
        if (CollectionUtils.isEmpty(materialKeywordIds)) {
            materialKeywordIds = new ArrayList<>();
        }
        return materialKeywordIds;
    }

    public Resource getResource() {
        return getPersistable();
    }

    public void setResource(Resource resource) {
        getLogger().debug("setResource: {}", resource);
        setPersistable(resource);
    }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = genericKeywordService.findAllWithCache(MaterialKeyword.class);
            Collections.sort(allMaterialKeywords);
        }
        return allMaterialKeywords;
    }

    public List<CoverageDate> getCoverageDates() {
        if (CollectionUtils.isEmpty(coverageDates)) {
            coverageDates = new ArrayList<>();
        }
        return coverageDates;
    }

    public void setCoverageDates(List<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }

    public List<SourceCollection> getSourceCollections() {
        if (sourceCollections == null) {
            sourceCollections = new ArrayList<>();
        }
        return sourceCollections;
    }

    public List<RelatedComparativeCollection> getRelatedComparativeCollections() {
        if (relatedComparativeCollections == null) {
            relatedComparativeCollections = new ArrayList<>();
        }
        return relatedComparativeCollections;
    }

    public boolean isAbleToViewConfidentialFiles() {
        return authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getPersistable());
    }

    public List<InvestigationType> getAllInvestigationTypes() {
        if (CollectionUtils.isEmpty(allInvestigationTypes)) {
            allInvestigationTypes = genericKeywordService.findAllWithCache(InvestigationType.class);
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
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApprovedWithCache(SiteTypeKeyword.class));
        }
        return approvedSiteTypeKeywords;
    }

    public KeywordNode<CultureKeyword> getApprovedCultureKeywords() {
        if (approvedCultureKeywords == null) {
            approvedCultureKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApprovedWithCache(CultureKeyword.class));
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

    public List<CreatorType> getCreatorTypes() {
        // FIXME: move impl to service layer
        return Arrays.asList(CreatorType.values());
    }

    public List<ResourceNote> getResourceNotes() {
        if (resourceNotes == null) {
            resourceNotes = new ArrayList<>();
        }
        return resourceNotes;
    }

    // FIXME: JTd: I think we should make all controller collection setters protected unless service layer absolutely. Confirm w/ others and change signatures
    // or revert this method back to public if there are objections.
    protected void setResourceNotes(List<ResourceNote> resourceNotes) {
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

    public Set<ResourceAnnotationKey> getAllResourceAnnotationKeys() {
        Set<ResourceAnnotationKey> keys = new HashSet<>();
        if ((getPersistable() != null) && CollectionUtils.isNotEmpty(getPersistable().getActiveResourceAnnotations())) {
            for (ResourceAnnotation ra : getPersistable().getActiveResourceAnnotations()) {
                keys.add(ra.getResourceAnnotationKey());
            }
        }
        return keys;
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
            authorshipProxies = new ArrayList<>();
        }
        return authorshipProxies;
    }

    public List<ResourceCreatorProxy> getContactProxies() {
        if (CollectionUtils.isEmpty(contactProxies)) {
            contactProxies = new ArrayList<>();
        }
        return contactProxies;
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
            creditProxies = new ArrayList<>();
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
        if (resourceAnnotations == null) {
            resourceAnnotations = new ArrayList<>();
        }
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

    public List<ResourceType> getAllResourceTypes() {
        return Arrays.asList(ResourceType.values());
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

    // return all of the collections that the currently-logged-in user is allowed to view. We define viewable as either shared+visible, or
    // shared+invisible+canEdit
    public List<ResourceCollection> getViewableResourceCollections() {
        if (viewableResourceCollections != null) {
            return viewableResourceCollections;
        }

        // if nobody logged in, just get the shared+visible collections
        Set<ResourceCollection> collections = new HashSet<>(getResource().getSharedVisibleResourceCollections());

        // if authenticated, also add the collections that the user can modify
        if (isAuthenticated()) {
            for (ResourceCollection resourceCollection : getResource().getSharedResourceCollections()) {
                if (authorizationService.canViewCollection(resourceCollection, getAuthenticatedUser())) {
                    collections.add(resourceCollection);
                }
            }
        }

        viewableResourceCollections = new ArrayList<>(collections);
        return viewableResourceCollections;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Set<Account> getActiveAccounts() {
        if (activeAccounts == null) {
            activeAccounts = new HashSet<>(determineActiveAccounts());
        }
        return activeAccounts;
    }

    public void setActiveAccounts(Set<Account> activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public boolean isBulkUpload() {
        return false;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public List<ResourceRelationship> getResourceRelationships() {
        return resourceRelationships;
    }

    public void setResourceRelationships(List<ResourceRelationship> resourceRelationships) {
        this.resourceRelationships = resourceRelationships;
    }

    public boolean isUserAbleToReTranslate() {
        if (authorizationService.canEdit(getAuthenticatedUser(), getPersistable())) {
            return true;
        }
        return false;
    }

    public boolean isUserAbleToViewDeletedFiles() {
        return isEditor();
    }

    public boolean isUserAbleToViewUnobfuscatedMap() {
        return isEditor();
    }

    public void updateQuota(Account account, Resource resource) {
        if (getTdarConfiguration().isPayPerIngestEnabled()) {
            accountService.updateQuota(account, resource);
        }
    }

    public List<CoverageType> getAllCoverageTypes() {
        List<CoverageType> coverageTypes = new ArrayList<>();
        coverageTypes.add(CoverageType.CALENDAR_DATE);
        coverageTypes.add(CoverageType.RADIOCARBON_DATE);
        return coverageTypes;
    }

    /**
     * Returns a list of Strings resulting from applying toString to each
     * element of the incoming Collection.
     * 
     * Basically, map( toString, collection ), but since Java doesn't support
     * closures yet...
     * 
     * @param collection
     * @return
     */
    protected List<String> toSortedStringList(Collection<?> collection) {
        ArrayList<String> stringList = new ArrayList<>(collection.size());
        for (Object o : collection) {
            stringList.add(o.toString());
        }
        Collections.sort(stringList);
        return stringList;
    }

    protected <P extends Persistable> List<Long> toIdList(Collection<P> persistables) {
        return Persistable.Base.extractIds(persistables);
    }

    protected void loadCustomViewMetadata() throws TdarActionException {
        if (getResource() instanceof InformationResource) {
            InformationResource informationResource = (InformationResource) getResource();
            try {
                datasetService.assignMappedDataForInformationResource(informationResource);
            } catch (Exception e) {
                getLogger().error("could not attach additional dataset data to resource", e);
            }
            setTransientViewableStatus(informationResource, getAuthenticatedUser());
        }

    }

    public List<EmailMessageType> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(List<EmailMessageType> emailTypes) {
        this.emailTypes = emailTypes;
    }

    public String getSubmitterProperName() {
        return submitterProperName;
    }

    public void setSubmitterProperName(String submitterProperName) {
        this.submitterProperName = submitterProperName;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    public Project getProject() {
        return project;
    }

    protected void resolveProject() {
        project = Project.NULL;
        if (getResource() instanceof InformationResource) {
            InformationResource ir = (InformationResource) getResource();
            project = ir.getProject();
        }
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
        if (Persistable.Base.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                informationResourceFileService.updateTransientDownloadCount(irf);
                if (irf.isDeleted()) {
                    setHasDeletedFiles(true);
                }
            }
        }
    }

    public boolean isHasDeletedFiles() {
        return hasDeletedFiles;
    }

    public void setHasDeletedFiles(boolean hasDeletedFiles) {
        this.hasDeletedFiles = hasDeletedFiles;
    }

    private Boolean editable = null;

    public boolean isEditable() {
        if (isNullOrNew()) {
            return false;
        }
        if (editable == null) {
            editable = authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_METADATA);
        }
        return editable;
    }

}
