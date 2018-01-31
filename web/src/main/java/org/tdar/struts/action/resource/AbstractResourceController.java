package org.tdar.struts.action.resource;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.bulk.BulkUploadController;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.transform.MetaTag;
import org.tdar.transform.OpenUrlFormatter;
import org.tdar.transform.ScholarMetadataTransformer;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.ResourceControllerProxy;
import org.tdar.web.service.ResourceEditControllerService;
import org.tdar.web.service.ResourceSaveControllerService;

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

    public static final String RESOURCE_EDIT_TEMPLATE = "../resource/edit-template.ftl";

    private static final long serialVersionUID = 8620875853247755760L;

    private static final String RIGHTS = "rights";
    private boolean select2Enabled = TdarConfiguration.getInstance().isSelect2Enabled();
    private boolean select2SingleEnabled = TdarConfiguration.getInstance().isSelect2SingleEnabled();
    private List<MaterialKeyword> allMaterialKeywords;
    private List<InvestigationType> allInvestigationTypes;
    private List<EmailType> emailTypes = EmailType.valuesWithoutConfidentialFiles();
    private RevisionLogType revisionType = RevisionLogType.EDIT;
    private String submit;
    protected ResourceControllerProxy<R> proxy = new ResourceControllerProxy<>(this);

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private GenericService genericService;

    @Autowired
    public ResourceCollectionService resourceCollectionService;

    @Autowired
    private BillingAccountService accountService;

    @Autowired
    private ResourceService resourceService;

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private KeywordNode<CultureKeyword> approvedCultureKeywords;

    private List<ResourceCollection> resourceCollections = new ArrayList<>();
    private List<ResourceCollection> effectiveResourceCollections = new ArrayList<>();

    private List<ResourceCollection> shares = new ArrayList<>();
    private List<ResourceCollection> effectiveShares = new ArrayList<>();
    private List<ResourceCollection> retainedSharedCollections = new ArrayList<>();
    private List<ResourceCollection> retainedListCollections = new ArrayList<>();

    private List<ResourceRelationship> resourceRelationships = new ArrayList<>();

    // containers for submitted data.
    private List<String> siteNameKeywords;

    private List<Long> investigationTypeIds;

    private List<Long> approvedMaterialKeywordIds;
    private List<Long> approvedSiteTypeKeywordIds;
    private List<Long> approvedCultureKeywordIds;

    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> uncontrolledCultureKeywords;
    private List<String> uncontrolledMaterialKeywords;

    private List<String> otherKeywords;

    private TdarUser submitter;
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
    private List<BillingAccount> activeAccounts;

    private List<ResourceNote> resourceNotes;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;

    private List<ResourceAnnotation> resourceAnnotations;

    @Autowired
    private ResourceSaveControllerService saveService;
    @Autowired
    private ResourceEditControllerService editService;

    protected void loadCustomMetadata() throws TdarActionException {
    }

    public String getOpenUrl() {
        return OpenUrlFormatter.toOpenURL(getResource());
    }

    public String getGoogleScholarTags() throws Exception {
        ScholarMetadataTransformer trans = new ScholarMetadataTransformer();
        StringWriter sw = new StringWriter();
        for (MetaTag tag : trans.convertResourceToMetaTag(getResource())) {
            serializationService.convertToXMLFragment(MetaTag.class, tag, sw);
            sw.append("\n");
        }
        return sw.toString();
    }


    @Override
    public String save(Resource resource) {
        getLogger().debug("calling save");
        String toReturn = SUCCESS;
        if (resource instanceof InformationResource) {
            try { 
                toReturn = saveInformationResource(getPersistable());
            } catch (TdarActionException e) {
                addActionErrorWithException(e.getMessage(), e);
                return INPUT;
            }
            if (this instanceof BulkUploadController) {
                resolvePostSaveAction(getPersistable());
                return toReturn;
            }
        }

        saveCustomMetadata();
        saveBasicResourceMetadata();
        resolvePostSaveAction(getPersistable());
        return toReturn;
    }
    
    public void saveCustomMetadata() {
        // TODO Auto-generated method stub
        
    }

    public void resolvePostSaveAction(R persistable) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String loadAddMetadata() {
        if (PersistableUtils.isNotNullOrTransient(getResource())) {
            setSubmitter(getResource().getSubmitter());
        } else {
            setSubmitter(getAuthenticatedUser());
        }

        if (getTdarConfiguration().isPayPerIngestEnabled()) {
            accountService.updateTransientAccountInfo(getResource());
            // setActiveAccounts(new ArrayList<>(editService.determineActiveAccounts(getAuthenticatedUser(), getResource())));
            if (PersistableUtils.isNotNullOrTransient(getResource()) && PersistableUtils.isNotNullOrTransient(getResource().getAccount())) {
                setAccountId(getResource().getAccount().getId());
            }
            for (BillingAccount account : getActiveAccounts()) {
                getLogger().trace(" - active accounts to {} files: {} mb: {}", account, account.getAvailableNumberOfFiles(), account.getAvailableSpaceInMb());
            }
        }
        return SUCCESS;
    }

    @Action(value = SAVE,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = SAVE_SUCCESS_PATH),
                    @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
                    @Result(name = INPUT, location = RESOURCE_EDIT_TEMPLATE),
                    @Result(name = RIGHTS, type = TdarActionSupport.REDIRECT, location = "/resource/rights/${persistable.id}")
            })
    @WriteableSession
    @PostOnly
    @HttpsOnly
    @Override
    /**
     * FIXME: appears to only override the INPUT result type compared to AbstractPersistableController's declaration,
     * see if it's possible to do this with less duplicatiousness
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#save()
     */
    public String save() throws TdarActionException {
        setSaveSuccessPath(getResource().getResourceType().getUrlNamespace());
        if (PersistableUtils.isNullOrTransient(getId())) {
            revisionType = RevisionLogType.CREATE;
            getPersistable().getAuthorizedUsers().add(new AuthorizedUser(getAuthenticatedUser(), getAuthenticatedUser(), Permissions.ADMINISTER_COLLECTION));
        }

        String save2 = super.save();
        try {
            if (StringUtils.equals(save2, SUCCESS) && StringUtils.equalsIgnoreCase(getAlternateSubmitAction(), ASSIGN_RIGHTS)) {
                return RIGHTS;
            }
        } catch (Throwable t) {
            getLogger().debug("{}", t, t);
        }
        getLogger().debug("success: {}", getSaveSuccessPath());
        return save2;
    }

    public String saveInformationResource(R persistable) throws TdarActionException {
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = ADD, results = {
            @Result(name = SUCCESS, location = RESOURCE_EDIT_TEMPLATE),
            @Result(name = CONTRIBUTOR, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.MY_PROFILE),
            @Result(name = BILLING, type = TdarActionSupport.TDAR_REDIRECT, location = URLConstants.CART_ADD)
    })
    @HttpsOnly
    @Override
    public String add() throws TdarActionException {
        accountService.assignOrphanInvoicesIfNecessary(getAuthenticatedUser());
        // if user has no invoices/funds to bill against, redirect to cart page
        if (!isAbleToCreateBillableItem()) {
            addActionMessage(getText("resourceController.requires_funds"));
            return BILLING;
        }

        // if user could otherwise create a billable item but isn't a contributor (an unlikely event), redirect to the profile page so that they can change
        // their status.
        if (!isContributor()) {
            addActionMessage(getText("resourceController.must_be_contributor"));
            return CONTRIBUTOR;
        }

        return super.add();
    }

    @SkipValidation
    @Action(value = EDIT, results = {
            @Result(name = SUCCESS, location = RESOURCE_EDIT_TEMPLATE)
    })
    @HttpsOnly
    @Override
    public String edit() throws TdarActionException {
        return super.edit();
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        loadAddMetadata();
        loadBasicMetadata();
        loadCustomMetadata();
        return SUCCESS;
    }

    @Override
    protected void postSaveCallback(String actionMessage) {
        // if user has single billing account, use that (ignore the form);
        setupAccountForSaving();

        if (SUCCESS.equals(actionMessage) || RIGHTS.equals(actionMessage)) {
            if (shouldSaveResource()) {
                if (getResource().getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                    getResource().setStatus(getResource().getPreviousStatus());
                }
                updateQuota(getGenericService().find(BillingAccount.class, getAccountId()), getResource(), getAuthenticatedUser());
            }
        } else {
            loadAddMetadata();
        }

        if (getResource() != null) { // this will happen with the bulk uploader
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", getResource().getResourceType().name(),
                    getAuthenticatedUser(), getResource().getId(), StringUtils.left(getResource().getTitle(), 100));
            logModification(logMessage, revisionType);
        }
    }

    protected void setupAccountForSaving() {
        List<BillingAccount> accounts = getActiveAccounts();
        if (accounts.size() == 1) {
            setAccountId(accounts.get(0).getId());
        }
    }

    /**
     * Returns true if authuser is able to create billable items, contributor status notwithstanding.
     * 
     * @return
     */
    @Override
    public boolean isAbleToCreateBillableItem() {
        return (!getTdarConfiguration().isPayPerIngestEnabled() || accountService.hasSpaceInAnAccount(getAuthenticatedUser(),
                ResourceType.fromClass(getPersistableClass())));
    }

    private Boolean editable = null;

    @Override
    public boolean authorize() {
        if (isNullOrNew()) {
            return true;
        }
        if (editable == null) {
            editable = authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), Permissions.MODIFY_METADATA);
        }
        return editable;
    }

    /**
     * Saves keywords, full / read user access, and confidentiality.
     */
    protected void saveBasicResourceMetadata() {
        AuthWrapper<Resource> authWrapper = new AuthWrapper<Resource>(getPersistable(), isAuthenticated(), getAuthenticatedUser(), isEditor());

        if (CollectionUtils.isEmpty(authWrapper.getItem().getActiveLatitudeLongitudeBoxes()) && !(this instanceof BulkUploadController)
                && !(this instanceof AbstractSupportingInformationResourceController)) {
            addActionMessage(getText("abstractResourceController.no_map", Arrays.asList(authWrapper.getItem().getResourceType().getLabel())));
        }

        proxy.setResource(getPersistable());
        proxy.setAccountId(accountId);
        proxy.setIncomingAnnotations(resourceAnnotations);
        proxy.setLatitudeLongitudeBoxes(latitudeLongitudeBoxes);
        proxy.setGeographicKeywords(geographicKeywords);
        proxy.setRelatedComparativeCollections(relatedComparativeCollections);
        proxy.setSourceCollections(sourceCollections);
        proxy.setSiteNameKeywords(siteNameKeywords);
        proxy.setApprovedCultureKeywordIds(approvedCultureKeywordIds);
        proxy.setApprovedSiteTypeKeywordIds(approvedSiteTypeKeywordIds);
        proxy.setInvestigationTypeIds(investigationTypeIds);
        proxy.setApprovedMaterialKeywordIds(approvedMaterialKeywordIds);
        proxy.setOtherKeywords(otherKeywords);
        proxy.setUncontrolledCultureKeywords(uncontrolledCultureKeywords);
        proxy.setUncontrolledMaterialKeywords(uncontrolledMaterialKeywords);
        proxy.setUncontrolledSiteTypeKeywords(uncontrolledSiteTypeKeywords);
        proxy.setTemporalKeywords(temporalKeywords);
        proxy.setSave(shouldSaveResource());
        proxy.setCoverageDates(coverageDates);
        proxy.setResourceNotes(resourceNotes);
        proxy.setShares(shares);
        proxy.setResourceCollections(resourceCollections);
        proxy.setSubmitter(submitter);
        proxy.setCreditProxies(creditProxies);
        proxy.setAuthorshipProxies(authorshipProxies);
        try {
            ErrorTransferObject eto = saveService.save(authWrapper, proxy);
            processErrorObject(eto);
        } catch (Throwable t) {
            addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), t);
        }

    }

    protected void logModification(String message, RevisionLogType type) {
        resourceService.logResourceModification(getPersistable(), getAuthenticatedUser(), message, null, type, getStartTime());
    }

    public void loadBasicMetadata() {
        // load all keywords
        AuthWrapper<Resource> authWrapper = new AuthWrapper<Resource>(getPersistable(), isAuthenticated(), getAuthenticatedUser(), isEditor());

        setApprovedMaterialKeywordIds(toIdList(getResource().getMaterialKeywords()));
        setInvestigationTypeIds(toIdList(getResource().getInvestigationTypes()));

        setUncontrolledCultureKeywords(toSortedStringList(getResource().getUncontrolledCultureKeywords()));
        setUncontrolledMaterialKeywords(toSortedStringList(getResource().getUncontrolledMaterialKeywords()));
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
        editService.initializeResourceCreatorProxyLists(authWrapper, getAuthorshipProxies(), getCreditProxies());
        getResourceAnnotations().addAll(getResource().getResourceAnnotations());
        editService.updateSharesForEdit(getResource(), getAuthenticatedUser(), effectiveShares, retainedSharedCollections, effectiveResourceCollections,
                retainedListCollections, shares, effectiveResourceCollections);
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

    public List<Long> getApprovedMaterialKeywordIds() {
        if (CollectionUtils.isEmpty(approvedMaterialKeywordIds)) {
            approvedMaterialKeywordIds = new ArrayList<>();
        }
        return approvedMaterialKeywordIds;
    }

    public R getResource() {
        return getPersistable();
    }

    public void setResource(R resource) {
        getLogger().debug("setResource: {}", resource);
        setPersistable(resource);
    }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = genericKeywordService.findAllApproved(MaterialKeyword.class);
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
            allInvestigationTypes = genericService.findAll(InvestigationType.class);
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
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(SiteTypeKeyword.class));
        }
        return approvedSiteTypeKeywords;
    }

    public KeywordNode<CultureKeyword> getApprovedCultureKeywords() {
        if (approvedCultureKeywords == null) {
            approvedCultureKeywords = KeywordNode.organizeKeywords(genericKeywordService.findAllApproved(CultureKeyword.class));
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

    public List<String> getUncontrolledMaterialKeywords() {
        if (CollectionUtils.isEmpty(uncontrolledMaterialKeywords)) {
            uncontrolledMaterialKeywords = createListWithSingleNull();
        }
        return uncontrolledMaterialKeywords;
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

    public void setUncontrolledMaterialKeywords(List<String> uncontrolledMaterialKeywords) {
        this.uncontrolledMaterialKeywords = uncontrolledMaterialKeywords;
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

    public void setApprovedMaterialKeywordIds(List<Long> materialKeywordIds) {
        this.approvedMaterialKeywordIds = materialKeywordIds;
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
        return new ResourceCollection();
    }

    public ResourceCollection getBlankShare() {
        return new ResourceCollection();
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
    public void setShares(List<ResourceCollection> resourceCollections) {
        this.shares = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    public List<ResourceCollection> getShares() {
        return shares;
    }

    /**
     * @return the effectiveResourceCollections
     */
    public List<ResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    /**
     * @param effectiveResourceCollections
     *            the effectiveResourceCollections to set
     */
    public void setEffectiveShares(List<ResourceCollection> effectiveResourceCollections) {
        this.effectiveShares = effectiveResourceCollections;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public List<BillingAccount> getActiveAccounts() {
        if (activeAccounts == null) {
            activeAccounts = new ArrayList<>(editService.determineActiveAccounts(getAuthenticatedUser(), getResource()));
        }
        return activeAccounts;
    }

    public void setActiveAccounts(List<BillingAccount> activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public boolean isBulkUpload() {
        return false;
    }

    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
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

    public void updateQuota(BillingAccount account, Resource resource, TdarUser user) {
        if (getTdarConfiguration().isPayPerIngestEnabled()) {
            accountService.updateQuota(account, user, resource);
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
        return PersistableUtils.extractIds(persistables);
    }

    public List<EmailType> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(List<EmailType> emailTypes) {
        this.emailTypes = emailTypes;
    }

    public String getSubmitterProperName() {
        if (getSubmitter() == null)
            return null;
        return getSubmitter().getProperName();
    }

    public Boolean isSelect2Enabled() {
        return select2Enabled;
    }

    public void setSelect2Enabled(boolean select2Enabled) {
        this.select2Enabled = select2Enabled;
    }

    public Boolean isSelect2SingleEnabled() {
        return select2SingleEnabled;
    }

    public void setSelect2SingleEnabled(boolean select2SingleEnabled) {
        this.select2SingleEnabled = select2SingleEnabled;
    }

    public List<ResourceCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    public void setEffectiveResourceCollections(List<ResourceCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    public List<ResourceCollection> getResourceCollections() {
        return resourceCollections;
    }

    public void setResourceCollections(List<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }
}
