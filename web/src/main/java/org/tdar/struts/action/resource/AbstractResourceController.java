package org.tdar.struts.action.resource;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
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
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao.FindOptions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.bulk.BulkUploadController;
import org.tdar.struts.data.KeywordNode;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.transform.MetaTag;
import org.tdar.transform.OpenUrlFormatter;
import org.tdar.transform.ScholarMetadataTransformer;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.PersistableUtils;

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
    private List<EmailMessageType> emailTypes = EmailMessageType.valuesWithoutConfidentialFiles();
    private RevisionLogType revisionType = RevisionLogType.EDIT;
    private String submit;
    
    @Autowired
    private SerializationService serializationService;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private ObfuscationService obfuscationService;

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

    private List<ListCollection> resourceCollections = new ArrayList<>();
    private List<ListCollection> effectiveResourceCollections = new ArrayList<>();

    private List<SharedCollection> shares = new ArrayList<>();
    private List<RightsBasedResourceCollection> effectiveShares = new ArrayList<>();
    private List<SharedCollection> retainedSharedCollections = new ArrayList<>();
    private List<ListCollection> retainedListCollections = new ArrayList<>();

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
    private List<BillingAccount> activeAccounts;

    private List<ResourceNote> resourceNotes;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;

    private List<ResourceAnnotation> resourceAnnotations;

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
        }
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
            serializationService.convertToXMLFragment(MetaTag.class, tag, sw);
            sw.append("\n");
        }
        return sw.toString();
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
            setActiveAccounts(new ArrayList<>(determineActiveAccounts()));
            if (PersistableUtils.isNotNullOrTransient(getResource()) && PersistableUtils.isNotNullOrTransient(getResource().getAccount())) {
                setAccountId(getResource().getAccount().getId());
            }
            for (BillingAccount account : getActiveAccounts()) {
                getLogger().trace(" - active accounts to {} files: {} mb: {}", account, account.getAvailableNumberOfFiles(), account.getAvailableSpaceInMb());
            }
        }
        return SUCCESS;
    }

    // Return list of acceptable billing accounts. If the resource has an account, this method will include it in the returned list even
    // if the user does not have explicit rights to the account (e.g. so that a user w/ edit rights on the resource can modify the resource
    // and maintain original billing account).
    protected List<BillingAccount> determineActiveAccounts() {
    	//Get all available active accounts for the user. If the resource is being edited, and its associated account is over-limit, this list will 
    	//not contain that billing account.
        List<BillingAccount> accounts = new LinkedList<>(accountService.listAvailableAccountsForUser(getAuthenticatedUser(), Status.ACTIVE));

        //If the resource has been created, e.g., not null, then check to see if the billing account needs to be added in. 
        if (getResource() != null) {

            accountService.updateTransientAccountInfo(getResource());
        	
            BillingAccount resourceAccount     = getResource().getAccount();
            boolean resourceAccountIsNotNull   = resourceAccount !=null;
            boolean resourceAccountNotInList   = !accounts.contains(resourceAccount);
            boolean hasInheritedEditPermission = authorizationService.isAllowedToEditInherited(getAuthenticatedUser(), getResource());
            
            //If the billing account is not in the list, but should be, then move it to the front of the list.
            if (resourceAccountIsNotNull && resourceAccountNotInList &&
            	(isEditor() || hasInheritedEditPermission)) {
                accounts.add(0, resourceAccount);
            }
        }
        return accounts;
    }

    @Action(value = SAVE,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = SAVE_SUCCESS_PATH),
                    @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
                    @Result(name = INPUT, location = RESOURCE_EDIT_TEMPLATE),
                    @Result(name = RIGHTS, type = TdarActionSupport.REDIRECT,  location = "/resource/rights/${persistable.id}")
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
            getPersistable().getAuthorizedUsers().add(new AuthorizedUser(getAuthenticatedUser(), getAuthenticatedUser(), GeneralPermissions.ADMINISTER_SHARE));
        }
    
        String save2 = super.save();
        try {
	        if (StringUtils.equals(save2, SUCCESS) && StringUtils.equalsAnyIgnoreCase(getAlternateSubmitAction(), ASSIGN_RIGHTS)) {
	            return RIGHTS;
	        }
        } catch (Throwable t) {
        	getLogger().debug("{}",t,t);
        }
        return save2;
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
        List<BillingAccount> accounts = determineActiveAccounts();
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

    // return a persisted annotation based on incoming pojo
    private void resolveAnnotations(Collection<ResourceAnnotation> incomingAnnotations) {
        List<ResourceAnnotation> toAdd = new ArrayList<>();
        for (ResourceAnnotation incomingAnnotation : incomingAnnotations) {
            if (incomingAnnotation == null) {
                continue;
            }
            ResourceAnnotationKey incomingKey = incomingAnnotation.getResourceAnnotationKey();
            ResourceAnnotationKey resolvedKey = getGenericService().findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
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
        incomingAnnotations.addAll(toAdd);
    }

    private Boolean editable = null;

    @Override
    public boolean authorize() {
        if (isNullOrNew()) {
            return true;
        }
        if (editable == null) {
            editable = authorizationService.canEditResource(getAuthenticatedUser(), getPersistable(), GeneralPermissions.MODIFY_METADATA);
        }
        return editable;
    }

    protected void saveKeywords() {
        getLogger().debug("siteNameKeywords=" + siteNameKeywords);
        getLogger().debug("materialKeywords=" + approvedMaterialKeywordIds);
        getLogger().debug("otherKeywords=" + otherKeywords);
        getLogger().debug("investigationTypes=" + investigationTypeIds);
        Resource res = getPersistable();

        cleanupKeywords(uncontrolledCultureKeywords);
        cleanupKeywords(uncontrolledMaterialKeywords);
        cleanupKeywords(uncontrolledSiteTypeKeywords);
        cleanupKeywords(siteNameKeywords);
        cleanupKeywords(otherKeywords);
        cleanupKeywords(temporalKeywords);

        Set<CultureKeyword> culKeys = genericKeywordService.findOrCreateByLabels(CultureKeyword.class, uncontrolledCultureKeywords);
        culKeys.addAll(genericService.findAll(CultureKeyword.class, approvedCultureKeywordIds));
        Set<MaterialKeyword> matKeys = genericKeywordService.findOrCreateByLabels(MaterialKeyword.class, uncontrolledMaterialKeywords);
        matKeys.addAll(genericService.findAll(MaterialKeyword.class, approvedMaterialKeywordIds));

        Set<SiteTypeKeyword> siteTypeKeys = genericKeywordService.findOrCreateByLabels(SiteTypeKeyword.class, uncontrolledSiteTypeKeywords);
        siteTypeKeys.addAll(genericService.findAll(SiteTypeKeyword.class, approvedSiteTypeKeywordIds));

        PersistableUtils.reconcileSet(res.getSiteNameKeywords(), genericKeywordService.findOrCreateByLabels(SiteNameKeyword.class, siteNameKeywords));
        PersistableUtils.reconcileSet(res.getOtherKeywords(), genericKeywordService.findOrCreateByLabels(OtherKeyword.class, otherKeywords));
        PersistableUtils.reconcileSet(res.getInvestigationTypes(), genericService.findAll(InvestigationType.class, investigationTypeIds));

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

    protected void saveTemporalContext() {
        // calendar and radiocarbon dates are null for Ontologies
        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, coverageDates,
                getResource().getCoverageDates(), CoverageDate.class);
        PersistableUtils.reconcileSet(getPersistable().getTemporalKeywords(),
                genericKeywordService.findOrCreateByLabels(TemporalKeyword.class, temporalKeywords));
    }

    protected void saveSpatialContext() {
        // it won't add a null or incomplete lat-long box.

        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, latitudeLongitudeBoxes,
                getResource().getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);

        if (CollectionUtils.isEmpty(getPersistable().getActiveLatitudeLongitudeBoxes()) && !(this instanceof BulkUploadController)
                && !(this instanceof AbstractSupportingInformationResourceController)) {
            addActionMessage(getText("abstractResourceController.no_map", Arrays.asList(getResource().getResourceType().getLabel())));
        }
        PersistableUtils.reconcileSet(getPersistable().getGeographicKeywords(),
                genericKeywordService.findOrCreateByLabels(GeographicKeyword.class, geographicKeywords));

        resourceService.processManagedKeywords(getPersistable(), getPersistable().getLatitudeLongitudeBoxes());
    }

    protected void saveCitations() {
        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS,
                relatedComparativeCollections,
                getResource().getRelatedComparativeCollections(), RelatedComparativeCollection.class);
        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, sourceCollections,
                getResource().getSourceCollections(), SourceCollection.class);

    }

    /**
     * Saves keywords, full / read user access, and confidentiality.
     */
    protected void saveBasicResourceMetadata() {

        if (shouldSaveResource()) {
            genericService.saveOrUpdate(getPersistable());
        }

        if (PersistableUtils.isNotNullOrTransient(getSubmitter())) {
            TdarUser uploader = getGenericService().find(TdarUser.class, getSubmitter().getId());
            getPersistable().setSubmitter(uploader);
        }


        saveKeywords();
        saveTemporalContext();
        saveSpatialContext();
        saveCitations();

        prepSequence(resourceNotes);
        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, resourceNotes,
                getResource().getResourceNotes(), ResourceNote.class);
        saveResourceCreators();

        resolveAnnotations(getResourceAnnotations());

        resourceService.saveHasResources((Resource) getPersistable(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, getResourceAnnotations(),
                getResource().getResourceAnnotations(), ResourceAnnotation.class);

        loadEffectiveResourceCollectionsForSave();
        getLogger().debug("retained collections:{}", retainedSharedCollections);
        getLogger().debug("retained list collections:{}", retainedListCollections);
        shares.addAll(retainedSharedCollections);
        resourceCollections.addAll(retainedListCollections);
        
        if (authorizationService.canDo(getAuthenticatedUser(), getResource(), InternalTdarRights.EDIT_ANY_RESOURCE,
                GeneralPermissions.MODIFY_RECORD)) {
            resourceCollectionService.saveResourceCollections(getResource(), shares, getResource().getSharedCollections(),
                    getAuthenticatedUser(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);

            if (!authorizationService.canEdit(getAuthenticatedUser(), getResource())) {
//                addActionError("abstractResourceController.cannot_remove_collection");
                getLogger().error("user is trying to remove themselves from the collection that granted them rights");
                addActionMessage("abstractResourceController.collection_rights_remove");
            }
        } else {
            getLogger().debug("ignoring changes to rights as user doesn't have sufficient permissions");
        }
        resourceCollectionService.saveResourceCollections(getResource(), resourceCollections, getResource().getUnmanagedResourceCollections(),
                getAuthenticatedUser(), shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, ListCollection.class);

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

    protected void logModification(String message, RevisionLogType type) {
        resourceService.logResourceModification(getPersistable(), getAuthenticatedUser(), message, null, type, getStartTime());
    }

    protected void saveResourceCreators() {
        List<ResourceCreatorProxy> allProxies = new ArrayList<>();
        if (authorshipProxies != null) {
            allProxies.addAll(authorshipProxies);
        }
        if (creditProxies != null) {
            allProxies.addAll(creditProxies);
        }
        resourceService.saveResourceCreatorsFromProxies(allProxies, getPersistable(), shouldSaveResource());
    }

    public void loadBasicMetadata() {
        // load all keywords

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
        initializeResourceCreatorProxyLists(false);
        getResourceAnnotations().addAll(getResource().getResourceAnnotations());
        loadEffectiveResourceCollectionsForEdit();
    }

    private void loadEffectiveResourceCollectionsForEdit() {
        getEffectiveShares().addAll(resourceCollectionService.getEffectiveSharesForResource(getResource()));

        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedResourceCollections()) {
            if (authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                getShares().add(rc);
            } else {
                retainedSharedCollections.add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
        for (ListCollection rc : getResource().getUnmanagedResourceCollections()) {
            if (authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                getResourceCollections().add(rc);
            } else {
                retainedListCollections.add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
        getEffectiveResourceCollections().addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(getResource()));
    }

    
    private void loadEffectiveResourceCollectionsForSave() {
        getLogger().debug("loadEffective...");
        for (SharedCollection rc : getResource().getSharedCollections()) {
            if (!authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                retainedSharedCollections.add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
        for (ListCollection rc : getResource().getUnmanagedResourceCollections()) {
            if (!authorizationService.canViewCollection(getAuthenticatedUser(),rc)) {
                retainedListCollections.add(rc);
                getLogger().debug("adding: {} to retained collections", rc);
            }
        }
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

    public ListCollection getBlankResourceCollection() {
        return new ListCollection();
    }

    public SharedCollection getBlankShare() {
        return new SharedCollection();
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
    public void setShares(List<SharedCollection> resourceCollections) {
        this.shares = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    public List<SharedCollection> getShares() {
        return shares;
    }

    /**
     * @return the effectiveResourceCollections
     */
    public List<RightsBasedResourceCollection> getEffectiveShares() {
        return effectiveShares;
    }

    /**
     * @param effectiveResourceCollections
     *            the effectiveResourceCollections to set
     */
    public void setEffectiveShares(List<RightsBasedResourceCollection> effectiveResourceCollections) {
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
            activeAccounts = new ArrayList<>(determineActiveAccounts());
        }
        return activeAccounts;
    }

    public void setActiveAccounts(List<BillingAccount> activeAccounts) {
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

    public List<EmailMessageType> getEmailTypes() {
        return emailTypes;
    }

    public void setEmailTypes(List<EmailMessageType> emailTypes) {
        this.emailTypes = emailTypes;
    }

    public String getSubmitterProperName() {
        if(getSubmitter() == null) return null;
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

    public List<ListCollection> getEffectiveResourceCollections() {
        return effectiveResourceCollections;
    }

    public void setEffectiveResourceCollections(List<ListCollection> effectiveResourceCollections) {
        this.effectiveResourceCollections = effectiveResourceCollections;
    }

    public List<ListCollection> getResourceCollections() {
        return resourceCollections;
    }

    public void setResourceCollections(List<ListCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }
}
