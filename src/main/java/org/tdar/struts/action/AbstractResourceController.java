package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.entity.FullUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ReadUser;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.ResourceUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ResourceService.ErrorHandling;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;
import org.tdar.utils.entity.ResourceCreatorProxy;
import org.tdar.utils.keyword.KeywordNode;

import com.opensymphony.xwork2.Preparable;

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
@Results({ @Result(name = AbstractResourceController.REDIRECT_HOME, type = "redirect", location = AbstractResourceController.HOME_URL),
        @Result(name = AbstractResourceController.REDIRECT_PROJECT_LIST, type = "redirect", location = AbstractResourceController.PROJECT_LIST_URL) })
public abstract class AbstractResourceController<R extends Resource> extends AuthenticationAware.Base implements Preparable {

    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String HOME_URL = "/";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    public final static String PROJECT_LIST_URL = "/project/list";
    private static final long serialVersionUID = 8620875853247755760L;

    private static final TdarConfiguration tdarConfiguration = TdarConfiguration.getInstance();

    // beans that need to be made available to the page
    private List<Project> fullUserProjects;

    private List<MaterialKeyword> allMaterialKeywords;
    private List<InvestigationType> allInvestigationTypes;

    private KeywordNode<SiteTypeKeyword> approvedSiteTypeKeywords;
    private KeywordNode<CultureKeyword> approvedCultureKeywords;

    private List<Person> fullUsers;
    private List<Person> readOnlyUsers;

    private List<AuthorizedUser> authorizedUsers;
    private List<ResourceCollection> resourceCollections;

    private List<Long> fullUserIds;
    private List<Long> readUserIds;

    // containers for submitted data.
    private List<String> siteNameKeywords;

    private List<Long> materialKeywordIds;
    private List<Long> investigationTypeIds;

    private List<Long> approvedSiteTypeKeywordIds;
    private List<Long> approvedCultureKeywordIds;

    private List<String> uncontrolledSiteTypeKeywords;
    private List<String> uncontrolledCultureKeywords;

    private List<String> otherKeywords;

    private String delete;
    private ModsDocument modsDocument;
    private DublinCoreDocument dcDocument;
    private List<String> temporalKeywords;
    private List<String> geographicKeywords;
    private List<LatitudeLongitudeBox> latitudeLongitudeBoxes;
    private List<CoverageDate> coverageDates;
    private boolean confidential;
    private Status status;
    private boolean asyncSave = true;

    // citation data.
    private List<String> sourceCitations;
    private List<String> sourceCollections;
    private List<String> relatedCitations;
    private List<String> relatedComparativeCitations;

    protected R resource;
    private Long resourceId;

    private List<ResourceNote> resourceNotes;

    // private ResourceType resourceType;
    private List<ResourceCreatorProxy> authorshipProxies;
    private List<ResourceCreatorProxy> creditProxies;

    private List<ResourceAnnotation> resourceAnnotations;
    private Long activeResourceCount;
    private List<Project> filteredFullUserProjects;

    protected abstract R loadResourceFromId(final Long resourceId);

    private void initializeResourceCreatorProxyLists() {
        if (resource.getResourceCreators() == null)
            return;
        authorshipProxies = new ArrayList<ResourceCreatorProxy>();
        creditProxies = new ArrayList<ResourceCreatorProxy>();

        for (ResourceCreator rc : resource.getResourceCreators()) {
            ResourceCreatorProxy proxy = new ResourceCreatorProxy(rc);
            if (ResourceCreatorRole.getAuthorshipRoles().contains(rc.getRole())) {
                authorshipProxies.add(proxy);
            } else {
                creditProxies.add(proxy);
            }
        }
        //
        // resource creators should be sorted by sequence number
        // Collections.sort(authorshipProxies);
        // Collections.sort(creditProxies);
    }

    /**
     * Override to perform custom save logic for the specific subtype of
     * Resource.
     * 
     * @param resource
     * @return the String result code to use.
     */
    protected abstract String save(R resource);

    /**
     * Used to instantiate and return a new specific subtype of Resource to be
     * used by the Struts action and JSP/FTL page. Must be overridden by any
     * subclass of the AbstractResourceController.
     * 
     * @return a new instance of the specific subtype of Resource for which this
     *         ResourceController is managing requests.
     */
    protected abstract R createResource();

    /**
     * Override to provide custom deletion logic for the specific kind of
     * Resource this ResourceController is managing.
     * 
     * @param resource
     */
    protected abstract void delete(R resource);

    /**
     * Override the following methods as necessary.
     */
    protected String loadAddData() {
        return SUCCESS;
    }

    protected void loadCustomMetadata() {
    };

    protected String deleteCustom() {
        return SUCCESS;
    }

    protected void loadListData() {
        setActiveResourceCount(getResourceService().countResourcesForUserAccess(getAuthenticatedUser()));
    }

    public void setActiveResourceCount(Long countResourcesForUserAccess) {
        this.activeResourceCount = countResourcesForUserAccess;
    }

    public Long getActiveResourceCount() {
        return activeResourceCount;
    }

    @SkipValidation
    @Action(value = "view", interceptorRefs = { @InterceptorRef("unAuthenticatedStack") }, results = { @Result(name = SUCCESS, location = "view.ftl") })
    public String view() {
        if (isNullOrNewResource()) {
            logger.warn("null resource to view, setResourceId must not have been invoked properly.");
            return REDIRECT_HOME;
        }
        /*
         * if the user is not an admin and the thing is deleted, or the user is
         * not the editor and the thing is a draft, then treat it as if it
         * doesn't exist.
         */
        logger.debug(resource.getId() + " : " + resource.getStatus());
        if (!(isAdministrator() || isEditable() && resource.getStatus() == Status.DRAFT || resource.getStatus() == Status.ACTIVE)) {
            logger.warn("don't have the rights to view this item.");
            return REDIRECT_HOME;
        }
        loadBasicMetadata();
        loadCustomMetadata();
        // TODO: should we create a specific viewCountIncrement() at the service
        // level? This seems like overkill.
        getResourceService().incrementAccessCounter(resource);
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "add", results = { @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, type = "redirect", location = "/resource/add") })
    public String add() {
        logger.info(getAuthenticatedUser().getEmail().toUpperCase() + " is CREATING a RECORD");
        return loadAddData();
    }

    @SkipValidation
    @Action(value = "delete", results = { @Result(name = SUCCESS, type = "redirect", location = PROJECT_LIST_URL),
            @Result(name = "confirm", location = "/WEB-INF/content/confirm.ftl") })
    public String delete() {
        if (getServletRequest() != null && getServletRequest().getMethod() != null && getServletRequest().getMethod().equalsIgnoreCase("post")
                && getDelete() != null && getDelete().equals("delete")) {

            if (isNullOrNewResource()) {
                logger.warn("Null resource, turning delete into a no-op");
                return REDIRECT_HOME;
            } else if (!isEditable() && !isAdministrator()) {
                String msg = String.format("user %s does not have the rights to delete this resource %d", getAuthenticatedUser(), getResource().getId());
                logger.warn(msg);
                logModification(msg);
                return REDIRECT_HOME;
            } else {
                logger.info(getAuthenticatedUser().getEmail().toUpperCase() + " is DELETING " + resource.getResourceType() + " " + resource.getTitle() + " ("
                        + resource.getId() + ")");
                if (deleteCustom() != SUCCESS)
                    return ERROR;
                String logMessage = String.format("%s id:%s deleted by:%s \ttitle:[%s]", getResource().getResourceType().getLabel(), getResource().getId(),
                        getAuthenticatedUser(), StringUtils.abbreviate(getResource().getTitle(), 100));

                getResourceService().logResourceModification(getResource(), getAuthenticatedUser(), logMessage);
                delete(resource);
                // purgeFromArchive(resource);
            }
            return SUCCESS;
        }
        return "confirm";
    }

    @SkipValidation
    @Action(value = "list")
    public String list() {
        loadListData();
        return SUCCESS;
    }

    @Action(value = "save", results = { @Result(name = SUCCESS, type = "redirect", location = "view?resourceId=${resource.id}"),
            @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
            @Result(name = INPUT, location = "edit.ftl")
    })
    public String save() {
        String actionReturnStatus = SUCCESS;
        logger.info("{} is SAVING {} ", getAuthenticatedUser().getEmail().toUpperCase(), resource);
        if (resource == null) {
            logger.warn("Trying to save but resource was null, returning INPUT");
            return INPUT;
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
        resource.setStatus(status);
        resource.markUpdated(getAuthenticatedUser());
        try {
            // execute subtype save logic
            actionReturnStatus = save(resource);
            // FIXME: always return INPUT and change signature to Throwable, use instanceOf to test for type and validate better
        } catch (Exception exception) {
            addActionErrorWithException("Sorry, we were unable to save: " + resource, exception);
            return INPUT;
        } finally {
            postSaveCleanup();
        }

        // getResourceService().saveTempVersion(resource);
        if (getResource() != null) { // this will happen with the bulk uploader
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]", getResource().getResourceType().getLabel(),
                    getAuthenticatedUser(), getResource().getId(), StringUtils.left(getResource().getTitle(), 100));
            logModification(logMessage);
        }
        return actionReturnStatus;
    }

    /**
     * override if needed
     */
    protected void postSaveCleanup() {
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

    @SkipValidation
    @Action(value = "edit", results = {
            @Result(name = SUCCESS, location = "edit.ftl"),
            @Result(name = INPUT, location = "add", type = "redirect"),
            @Result(name = UNAUTHORIZED, location = "/WEB-INF/content/.ftl")
            })
    public String edit() {
        if (isNullOrNewResource()) {
            logger.warn("edit() received an invalid resource {}", resource);
            return INPUT;
        }
        // check permissions
        if (getEntityService().canEditResource(getAuthenticatedUser(), resource)) {
            logger.info(getAuthenticatedUser().getEmail().toUpperCase() + " is EDITING " + resource.getResourceType() + " " + resource.getTitle() + " ("
                    + resource.getId() + ")");
            loadBasicMetadata();
            // load any additional custom metadata
            loadCustomMetadata();
            return SUCCESS;
        } else {
            addActionError(String.format("You do not have permissions to edit the resource (ID: %s, Title: %s)", resource.getId(), resource.getTitle()));
            return UNAUTHORIZED;
        }
    }

    public boolean isEditable() {
        if (isNullOrNewResource())
            return false;
        return getEntityService().canEditResource(getAuthenticatedUser(), resource);
    }

    /**
     * This method is invoked when the paramsPrepareParamsInterceptor stack is
     * applied. It allows us to fetch an entity from the database based on the
     * incoming resourceId param, and then re-apply params on that resource.
     */
    public void prepare() {
        if (resourceId == null || resourceId == -1L) {
            resource = createResource();
        } else {
            resource = loadResourceFromId(resourceId);
        }
    }

    protected void saveKeywords() {
        logger.debug("siteNameKeywords=" + siteNameKeywords);
        logger.debug("materialKeywords=" + materialKeywordIds);
        logger.debug("otherKeywords=" + otherKeywords);
        logger.debug("investigationTypes=" + investigationTypeIds);

        if (resource.getId() != -1L) {
            // delete existing keywords, optimize this so that this doesn't
            // occur when
            // no changes occur..

            // do not think we need to do this anymore
            // getProjectService().deleteAllKeywords(resource);

        }
        resource.setSiteNameKeywords(getSiteNameKeywordService().findOrCreateByLabels(siteNameKeywords));

        Set<CultureKeyword> culKeys = getCultureKeywordService().findOrCreateByLabels(uncontrolledCultureKeywords);
        culKeys.addAll(getCultureKeywordService().findByIds(approvedCultureKeywordIds));
        resource.setCultureKeywords(culKeys);

        Set<SiteTypeKeyword> siteTypeKeys = getSiteTypeKeywordService().findOrCreateByLabels(uncontrolledSiteTypeKeywords);
        siteTypeKeys.addAll(getSiteTypeKeywordService().findByIds(approvedSiteTypeKeywordIds));
        resource.setSiteTypeKeywords(siteTypeKeys);

        resource.setOtherKeywords(getOtherKeywordService().findOrCreateByLabels(otherKeywords));

        resource.setMaterialKeywords(getMaterialKeywordService().findByIds(materialKeywordIds));

        resource.setInvestigationTypes(getInvestigationTypeService().findByIds(investigationTypeIds));
    }

    protected void saveTemporalContext() {
        // calendar and radiocarbon dates are null for Ontologies
        getResourceService().saveHasResources((Resource) resource, shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, coverageDates,
                getResource().getCoverageDates(), CoverageDate.class);
        resource.setTemporalKeywords(getTemporalKeywordService().findOrCreateByLabels(temporalKeywords));
    }

    protected void saveSpatialContext() {
        // Resource.add(LongitudeLatitudeBox) makes validity checks to ensure
        // it won't add a null or incomplete lat-long box.

        getResourceService().saveHasResources((Resource) resource, shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, latitudeLongitudeBoxes,
                getResource().getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);
        resource.setGeographicKeywords(getGeographicKeywordService().findOrCreateByLabels(geographicKeywords));
        resource.getManagedGeographicKeywords().clear();
        getResourceService().processManagedKeywords(resource, resource.getLatitudeLongitudeBoxes());
    }

    /*
     * generic method for handling read and full users
     */
    protected <U extends ResourceUser> void saveUsers(Class<U> clas, Set<U> currentUsers, List<Long> incomingUserIds) {
        // if empty, remove all
        if (CollectionUtils.isEmpty(incomingUserIds)) {
            currentUsers.clear();
            return;
        }
        // get rid of bad ids
        incomingUserIds = filterInvalidUsersIds(incomingUserIds);

        // iterate through all current users and remove those not in the list
        for (Iterator<U> iter = currentUsers.iterator(); iter.hasNext();) {
            U user = iter.next();
            Long rUserId = user.getPerson().getId();
            if (incomingUserIds.contains(rUserId)) {
                incomingUserIds.remove(rUserId);
            } else {
                iter.remove();
                logger.debug("removing ... " + user);
            }
        }

        // add the new users
        for (Long personId : incomingUserIds) {
            try {
                U user = clas.newInstance();
                Person person = getEntityService().findPerson(personId);
                user.setPerson(person);
                user.setResource(resource);
                currentUsers.add(user);
                logger.debug("adding ... " + user);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("Unable to map user rights", e);
            }

        }
    }

    /**
     * Saves keywords, full / read user access, and confidentiality.
     */
    protected void saveBasicResourceMetadata() {
        resource.setDateUpdated(new Date());
        resource.setUpdatedBy(getAuthenticatedUser());
        if (shouldSaveResource()) {
            getResourceService().saveOrUpdate(resource);
            // resource = getResourceService().merge(resource);
        }
        saveUsers(ReadUser.class, resource.getReadUsers(), readUserIds);
        saveUsers(FullUser.class, resource.getFullUsers(), fullUserIds);
        getResourceCollectionService().saveAuthorizedUsersForResource(getResource(), getAuthorizedUsers(), shouldSaveResource());
        // logger.info("confidential set to: " + confidential);
        // resource.setConfidential(confidential);
        saveKeywords();
        saveTemporalContext();
        saveSpatialContext();

        getResourceService().saveHasResources((Resource) resource, shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, resourceNotes,
                getResource().getResourceNotes(), ResourceNote.class);
        saveResourceCreators();

        resolveAnnotations(getResourceAnnotations());
        getResourceService().saveHasResources((Resource) resource, shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, getResourceAnnotations(),
                getResource().getResourceAnnotations(), ResourceAnnotation.class);
    }

    protected void logModification(String message) {
        logResourceModification(resource, message, null);
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
                logger.debug("{} - {}", resourceCreator, resourceCreator.getCreatorType());

                getEntityService().findOrSaveResourceCreator(resourceCreator);
                incomingResourceCreators.add(resourceCreator);
                logger.debug("{} - {}", resourceCreator, resourceCreator.getCreatorType());
            } else {
                getLogger().debug("can't create creator from proxy {}", proxy);
            }
        }

        // FIXME: Should this throw errors?
        getResourceService().saveHasResources((Resource) resource, shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, incomingResourceCreators,
                getResource().getResourceCreators(), ResourceCreator.class);
    }

    protected final void loadBasicMetadata() {
        // load all keywords

        setMaterialKeywordIds(toIdList(getResource().getMaterialKeywords()));
        setInvestigationTypeIds(toIdList(getResource().getInvestigationTypes()));

        setUncontrolledCultureKeywords(toStringList(getResource().getUncontrolledCultureKeywords()));
        setApprovedCultureKeywordIds(toIdList(getResource().getApprovedCultureKeywords()));

        setUncontrolledSiteTypeKeywords(toStringList(getResource().getUncontrolledSiteTypeKeywords()));
        setApprovedSiteTypeKeywordIds(toIdList(getResource().getApprovedSiteTypeKeywords()));

        setOtherKeywords(toStringList(getResource().getOtherKeywords()));
        setSiteNameKeywords(toStringList(getResource().getSiteNameKeywords()));
        // load temporal / geographic terms
        setTemporalKeywords(toStringList(getResource().getTemporalKeywords()));
        setGeographicKeywords(toStringList(getResource().getGeographicKeywords()));
        // load spatial context
        getLatitudeLongitudeBoxes().addAll(getResource().getLatitudeLongitudeBoxes());

        // load radiocarbon / calendar dates
        getCoverageDates().addAll(getResource().getCoverageDates());
        // load full access users

        fullUsers = new ArrayList<Person>();
        for (FullUser fullUser : getResource().getFullUsers()) {
            fullUsers.add(fullUser.getPerson());
        }
        // load read only users
        readOnlyUsers = new ArrayList<Person>();
        for (ReadUser readUser : getResource().getReadUsers()) {
            readOnlyUsers.add(readUser.getPerson());
        }

        getResourceNotes().addAll(getResource().getResourceNotes());
        getAuthorizedUsers().addAll(getResourceCollectionService().getAuthorizedUsersForResource(getResource()));
        // setConfidential(resource.isConfidential());
        initializeResourceCreatorProxyLists();
        getResourceAnnotations().addAll(getResource().getResourceAnnotations());
    }

    /**
     * Set by view and edit to load the initial resource (via
     * loadResourceFromId).
     * 
     * @param resourceId
     */
    public void setResourceId(final Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public List<Project> getAllSubmittedProjects() {
        List<Project> allSubmittedProjects = getProjectService().findBySubmitter(getAuthenticatedUser());
        Collections.sort(allSubmittedProjects);
        return allSubmittedProjects;
    }

    public List<Project> getFullUserProjects() {
        if (fullUserProjects == null) {
            fullUserProjects = getEntityService().findSparseTitleIdProjectListByPerson(getAuthenticatedUser());
            fullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return fullUserProjects;
    }

    public List<Project> getFilteredFullUserProjects() {
        if (filteredFullUserProjects == null) {
            filteredFullUserProjects = new ArrayList<Project>(getFullUserProjects());
            filteredFullUserProjects.removeAll(getAllSubmittedProjects());
        }
        return filteredFullUserProjects;
    }

    public List<String> getSiteNameKeywords() {
        if (CollectionUtils.isEmpty(siteNameKeywords)) {
            siteNameKeywords = createListWithSingleNull();
        }
        return siteNameKeywords;
    }

    public List<String> getOtherKeywords() {
        if (CollectionUtils.isEmpty(otherKeywords)) {
            otherKeywords = createListWithSingleNull();
        }
        return otherKeywords;
    }

    public List<String> getTemporalKeywords() {
        if (CollectionUtils.isEmpty(temporalKeywords)) {
            temporalKeywords = createListWithSingleNull();
        }
        return temporalKeywords;
    }

    public List<String> getGeographicKeywords() {
        if (CollectionUtils.isEmpty(geographicKeywords)) {
            geographicKeywords = createListWithSingleNull();
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
            materialKeywordIds = createListWithSingleNull();
        }
        return materialKeywordIds;
    }

    public R getResource() {
        return resource;
    }

    public void setResource(R resource) {
        logger.debug("setResource: {}", resource);
        this.resource = resource;
    }

    public List<Person> getFullUsers() {
        // edit ftl expects at least one person, even when creating new resource
        if (CollectionUtils.isEmpty(fullUsers)) {
            fullUsers = new ArrayList<Person>();
        }
        getLogger().trace("fullUsers:" + fullUsers);
        return fullUsers;
    }

    public List<Person> getBlankUser() {
        getLogger().trace("blank user called");
        Person person = new Person();
        person.setId(-1L);
        List<Person> list = new ArrayList<Person>();
        list.add(person);
        return list;
    }

    public List<MaterialKeyword> getAllMaterialKeywords() {
        if (CollectionUtils.isEmpty(allMaterialKeywords)) {
            allMaterialKeywords = getMaterialKeywordService().findAll();
        }
        return allMaterialKeywords;
    }

    public List<Person> getReadOnlyUsers() {
        if (CollectionUtils.isEmpty(readOnlyUsers)) {
            readOnlyUsers = new ArrayList<Person>();
        }
        getLogger().trace("readOnlyUsers:" + readOnlyUsers);
        return readOnlyUsers;
    }

    public void setReadOnlyUsers(List<Person> readUsers) {
        this.readOnlyUsers = readUsers;
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

    public List<String> getSourceCitations() {
        if (CollectionUtils.isEmpty(sourceCitations)) {
            sourceCitations = createListWithSingleNull();
        }
        return sourceCitations;
    }

    public void setSourceCitations(List<String> sourceCitations) {
        this.sourceCitations = sourceCitations;
    }

    public List<String> getSourceCollections() {
        if (CollectionUtils.isEmpty(sourceCollections)) {
            sourceCollections = createListWithSingleNull();
        }
        return sourceCollections;
    }

    public List<String> getRelatedCitations() {
        if (CollectionUtils.isEmpty(relatedCitations)) {
            relatedCitations = createListWithSingleNull();
        }
        return relatedCitations;
    }

    public List<String> getRelatedComparativeCitations() {
        if (CollectionUtils.isEmpty(relatedComparativeCitations)) {
            relatedComparativeCitations = createListWithSingleNull();
        }
        return relatedComparativeCitations;
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

    public boolean isViewable() {
        return getEntityService().canViewConfidentialInformation(getAuthenticatedUser(), resource);
    }

    protected boolean isNullOrNewResource() {
        return resource == null || resource.getId() == -1L;
    }

    public List<InvestigationType> getAllInvestigationTypes() {
        if (CollectionUtils.isEmpty(allInvestigationTypes)) {
            allInvestigationTypes = getInvestigationTypeService().findAll();
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
            approvedSiteTypeKeywords = KeywordNode.organizeKeywords(getSiteTypeKeywordService().findAllApproved());
        }
        return approvedSiteTypeKeywords;
    }

    public KeywordNode<CultureKeyword> getApprovedCultureKeywords() {
        if (approvedCultureKeywords == null) {
            approvedCultureKeywords = KeywordNode.organizeKeywords(getCultureKeywordService().findAllApproved());
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

    public abstract ModsTransformer<R> getModsTransformer();

    public ModsDocument getModsDocument() {
        if (modsDocument == null) {
            modsDocument = getModsTransformer().transform(getResource());
        }
        return modsDocument;
    }

    @Action(value = "mods", interceptorRefs = { @InterceptorRef("unAuthenticatedStack") }, results = {
            @Result(name = "success", type = "jaxbdocument", params = { "documentName", "modsDocument", "formatOutput", "true" }),
            @Result(name = "notfound", type = "httpheader", params = { "status", "404" }) })
    public String viewMods() {
        if (getResource() == null) {
            return "notfound";
        }
        return SUCCESS;
    }

    public abstract DcTransformer<R> getDcTransformer();

    public DublinCoreDocument getDcDocument() {
        if (dcDocument == null) {
            dcDocument = getDcTransformer().transform(getResource());
        }
        return dcDocument;
    }

    @Action(value = "dc", interceptorRefs = { @InterceptorRef("unAuthenticatedStack") }, results = {
            @Result(name = "success", type = "jaxbdocument", params = { "documentName", "dcDocument", "formatOutput", "true" }),
            @Result(name = "notfound", type = "httpheader", params = { "status", "404" }) })
    public String viewDc() {
        if (getResource() == null) {
            return "notfound";
        }
        return SUCCESS;
    }

    public Status getStatus() {
        return resource.getStatus();
    }

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }

    public List<Status> getStatuses() {
        List<Status> toReturn = new ArrayList<Status>();
        for (Status status : getResourceService().findAllStatuses()) {
            if (!isAdministrator() && (status == Status.FLAGGED ||
                    status == Status.DELETED)) {
                continue;
            }
            toReturn.add(status);

        }
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

    public void setSourceCollections(List<String> sourceCollections) {
        this.sourceCollections = sourceCollections;
    }

    public void setRelatedComparativeCitations(List<String> relatedComparativeCitations) {
        this.relatedComparativeCitations = relatedComparativeCitations;
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

    public void setResourceAnnotations(List<ResourceAnnotation> resourceAnnotations) {
        this.resourceAnnotations = resourceAnnotations;
    }

    public List<Long> getFullUserIds() {
        return fullUserIds;
    }

    public void setFullUserIds(List<Long> fullUserIds) {
        this.fullUserIds = fullUserIds;
    }

    public List<Long> getReadUserIds() {
        return readUserIds;
    }

    public void setReadUserIds(List<Long> readUserIds) {
        this.readUserIds = readUserIds;
    }

    /**
     * Returns true if we need to checkpoint and save the resource at various stages to handle many-to-one relationships
     * properly (due to cascading not working properly)
     * 
     * @return
     */
    public boolean shouldSaveResource() {
        return true;
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
     * @param authorizedUsers
     *            the authorizedUsers to set
     */
    public void setAuthorizedUsers(List<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    /**
     * @return the authorizedUsers
     */
    public List<AuthorizedUser> getAuthorizedUsers() {
        if (authorizedUsers == null) {
            authorizedUsers = new ArrayList<AuthorizedUser>();
        }
        return authorizedUsers;
    }

    public AuthorizedUser getBlankAuthorizedUser() {
        return new AuthorizedUser();
    }

    /**
     * Used to signal confirmation of deletion requests.
     * 
     * @param delete
     *            the delete to set
     */
    public void setDelete(String delete) {
        this.delete = delete;
    }

    /**
     * 
     * @return the delete
     */
    public String getDelete() {
        return delete;
    }

    @Override
    public void validate() {
        logger.debug("validating resource()");
        if (resource == null) {
            logger.warn("Null resource being validated.");
            addActionError("Sorry, we couldn't find the resource you specified.");
            return;
        }
        String resourceTypeLabel = resource.getResourceType().getLabel();
        if (StringUtils.isEmpty(resource.getTitle())) {
            addActionError("Please enter a title for your " + resourceTypeLabel);
        }
        if (StringUtils.isEmpty(resource.getDescription())) {
            addActionError("Please enter a description for your " + resourceTypeLabel);
        }
    }

    public void setAsync(boolean async) {
        this.asyncSave = async;
    }

    public boolean isAsync() {
        return asyncSave;
    }

}
