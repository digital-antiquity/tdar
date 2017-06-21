package org.tdar.struts.action.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.FileProxyService;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * <p>
 * Provides common functionality for controllers that manage requests for information resources.
 * </p>
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <R>
 */

public abstract class AbstractInformationResourceController<R extends InformationResource> extends AbstractResourceController<R> {

    public static final String FILE_INPUT_METHOD = "text";

    private static final long serialVersionUID = -200666002871956655L;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient FileProxyService fileProxyService;

    @Autowired
    private transient InformationResourceFileService informationResourceFileService;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    @Autowired
    private transient InformationResourceService informationResourceService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient ProjectService projectService;

    @Autowired
    private transient ObfuscationService obfuscationService;

    private List<CategoryVariable> allDomainCategories;

    private Project project = Project.NULL;
    private List<Resource> potentialParents;
    // incoming data
    private List<File> uploadedFiles;
    private List<String> uploadedFileContentTypes; // unused I think (hope)
    private List<String> uploadedFilesFileNames;
    private Language resourceLanguage;
    private Language metadataLanguage;
    private List<Language> languages;
    private List<FileProxy> fileProxies = new ArrayList<>();
    private String json = "{}";
    private Long projectId;

    // previously uploaded files list in json format, needed by blueimp jquery file upload
    private String filesJson = null;

    private Boolean isAbleToUploadFiles = null;

    // private List<PersonalFilestoreFile> pendingFiles;

    private Long ticketId;

    // resource provider institution and contacts
    private String resourceProviderInstitutionName;
    private String publisherName;

    // resource availability
    // private String resourceAvailability;
    private boolean allowedToViewConfidentialFiles;
    private FileAnalyzer analyzer;
    private boolean hasDeletedFiles = false;
    // protected PersonalFilestoreTicket filestoreTicket;
    private ResourceCreatorProxy copyrightHolderProxies = new ResourceCreatorProxy();

    /**
     * This should be overridden when InformationResource content is entered from a text area in the web form.
     * Currently the only InformationResourceS that employ this method of content/data entry are CodingSheetS and OntologyS.
     * 
     * Returns a FileProxy representing the content that was entered.
     */
    protected FileProxy processTextInput() {
        return null;
    }

    /**
     * @throws IOException
     *             If there was an IO error
     */
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        throw new UnsupportedOperationException(getText("abstractInformationResourceController.didnt_override", getClass()));
    }

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, TdarUser p) {
        authorizationService.applyTransientViewableFlag(ir, p);
        if (PersistableUtils.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                informationResourceFileService.updateTransientDownloadCount(irf);
                if (irf.isDeleted()) {
                    setHasDeletedFiles(true);
                }
            }
        }
    }

    /**
     * Returns a List<FileProxy> representing all FileProxy objects to be processed.
     * Unifies the incoming fileProxies from the web layer with the PersonalFilestoreFiles associated with
     * ticketId, injecting the appropriate Files on the FileProxy objects.
     * 
     * @return a List<FileProxy> representing all fully initialized FileProxy objects to be processed by the service layer.
     */
    protected List<FileProxy> handleAsyncUploads() {
        return fileProxyService.reconcilePersonalFilestoreFilesAndFileProxies(fileProxies, ticketId);
    }

    private boolean hasFileProxyChanges = false;

    /**
     * Throw an extension if any of the provided proxies describe a file that is not contained in the list of accepted file types.
     * 
     * @param proxies
     * @throws TdarActionException
     */
    private void validateFileExtensions(List<FileProxy> proxies) throws TdarActionException {
        List<FileProxy> invalidFiles = new ArrayList<>();
        for (FileProxy proxy : proxies) {
            if (!getValidFileExtensions().contains(proxy.getExtension().toLowerCase()) && proxy.getAction() != FileAction.DELETE) {
                getLogger().info("Rejecting file:{} - extension not allowed.  Allowed types:{}", proxy.getExtension(), getValidFileExtensions());
                invalidFiles.add(proxy);
            }
        }
        if (!invalidFiles.isEmpty()) {
            throw new TdarRecoverableRuntimeException(getText("abstractResourceController.bad_extension"));
        }
    }

    /**
     * One-size-fits-all method for handling uploaded InformationResource files.
     * 
     * Handles text input files for coding sheets and ontologies,
     * async uploads, and single-file dataset uploads.
     * 
     * @throws TdarActionException
     */
    protected void handleUploadedFiles() throws TdarActionException {

        List<FileProxy> proxies = new ArrayList<>();
        try {
            getLogger().debug("handling uploaded files for {}", getPersistable());
            proxies = getFileProxiesToProcess();
            validateFileExtensions(proxies);
            getLogger().debug("Final proxy set: {}", proxies);

            for (FileProxy proxy : proxies) {
                if (proxy != null && proxy.getAction() != FileAction.NONE) {
                    setHasFileProxyChanges(true);
                }
            }

        } catch (TdarRecoverableRuntimeException trrc) {
            addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), trrc);
        }

        if (isHasFileProxyChanges()
                && !authorizationService.canDo(getAuthenticatedUser(), getResource(), InternalTdarRights.EDIT_ANY_RESOURCE,
                        GeneralPermissions.MODIFY_RECORD)) {
            throw new TdarActionException(StatusCode.FORBIDDEN, "You do not have permissions to upload or modify files");
        }
        // abstractInformationResourceController.didnt_override=%s didn't override properly
        // abstractInformationResourceController.didnt_override=%s didn't override properly

        try {
            ErrorTransferObject errors = informationResourceService.importFileProxiesAndProcessThroughWorkflow(getPersistable(), getAuthenticatedUser(),
                    ticketId, proxies);
            processErrorObject(errors);
        } catch (Exception e) {
            addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), e);
        }
        getGenericService().saveOrUpdate(getPersistable());
        getLogger().trace("done processing upload files");
    }

    /**
     * Returns a List<FileProxy> representing the final set of fully initialized FileProxy objects
     * to be processed by the service layer.
     * 
     * FIXME: conditional logic could use some additional refactoring.
     * 
     * @return a List<FileProxy> representing the final set of fully initialized FileProxy objects
     */
    protected List<FileProxy> getFileProxiesToProcess() {
        List<FileProxy> fileProxiesToProcess = new ArrayList<>();
        // Possible scenarios:
        FileProxy textInputFileProxy = processTextInput();

        // 1. text input for CodingSheet or Ontology (everything in a String, needs preprocessing to convert to a FileProxy)
        if (textInputFileProxy != null) {
            fileProxiesToProcess.add(textInputFileProxy);
        }
        // 2. async uploads for Image or Document or ...
        else if (isMultipleFileUploadEnabled()) {
            fileProxiesToProcess = handleAsyncUploads();
        } else
        // 3. single file upload (dataset|coding sheet|ontology)
        // there could be an incoming file payload, or just a metadata change.
        {
            fileProxiesToProcess = handleSingleFileUpload(fileProxiesToProcess);
        }

        return fileProxiesToProcess;
    }

    protected List<FileProxy> handleSingleFileUpload(List<FileProxy> toProcess) {
        /*
         * FIXME: in Jar, hopefully, this goes away
         */

        FileProxy singleFileProxy = CollectionUtils.isEmpty(fileProxies) ? new FileProxy() : fileProxies.get(0);
        if (CollectionUtils.isEmpty(uploadedFiles)) {
            // check for metadata change iff this resource has an existing file.
            InformationResourceFile file = getPersistable().getFirstInformationResourceFile();
            if (file != null && singleFileProxy.isDifferentFromFile(file)) {
                singleFileProxy.setAction(FileAction.MODIFY_METADATA);
                singleFileProxy.setFileId(file.getId());
                toProcess.add(singleFileProxy);
            }
        } else {
            // process a new uploaded file (either ADD or REPLACE)
            setFileProxyAction(singleFileProxy);
            singleFileProxy.setFilename(uploadedFilesFileNames.get(0));
            singleFileProxy.setFile(uploadedFiles.get(0));
            toProcess.add(singleFileProxy);
        }
        return toProcess;
    }

    protected void loadResourceProviderInformation() {
        // load resource provider institution and publishers
        setResourceProviderInstitution(getResource().getResourceProviderInstitution());
        setPublisherName(getResource().getPublisherName());
        if (getTdarConfiguration().getCopyrightMandatory() && PersistableUtils.isNotNullOrTransient(getResource().getCopyrightHolder())) {
            copyrightHolderProxies = new ResourceCreatorProxy(getResource().getCopyrightHolder(), ResourceCreatorRole.COPYRIGHT_HOLDER);
        }
    }

    protected void saveResourceProviderInformation() {
        getLogger().debug("Saving resource provider information: {}", resourceProviderInstitutionName);
        // save resource provider institution and contact information
        if (StringUtils.isNotBlank(resourceProviderInstitutionName)) {
            getResource().setResourceProviderInstitution(entityService.findOrSaveCreator(new Institution(resourceProviderInstitutionName)));
        } else {
            getResource().setResourceProviderInstitution(null);
        }

        if (StringUtils.isNotBlank(publisherName)) {
            getResource().setPublisher(entityService.findOrSaveCreator(new Institution(publisherName)));
        } else {
            getResource().setPublisher(null);
        }

        if (getTdarConfiguration().getCopyrightMandatory() && copyrightHolderProxies != null) {
            ResourceCreator transientCreator = copyrightHolderProxies.getResourceCreator();
            getLogger().debug("setting copyright holder to:  {} ", transientCreator);
            getResource().setCopyrightHolder(entityService.findOrSaveCreator(transientCreator.getCreator()));
        }
    }

    public void setPublisher(String publisherName) {
        this.publisherName = publisherName;
    }

    public ArrayList<LicenseType> getLicenseTypesList() {
        return new ArrayList<>(Arrays.asList(LicenseType.values()));
    }

    public LicenseType getDefaultLicenseType() {
        return getTdarConfiguration().getDefaultLicenseType();

    }

    public List<File> getUploadedFiles() {
        if (CollectionUtils.isEmpty(uploadedFiles)) {
            uploadedFiles = createListWithSingleNull();
        }
        return uploadedFiles;
    }

    public void setUploadedFiles(List<File> uploadedFile) {
        getLogger().trace("incoming file list {}", uploadedFile);
        this.uploadedFiles = uploadedFile;
    }

    public List<String> getUploadedFilesContentType() {
        return uploadedFileContentTypes;
    }

    public void setUploadedFilesContentType(List<String> uploadedFileContentType) {
        this.uploadedFileContentTypes = uploadedFileContentType;
    }

    public List<CategoryVariable> getAllDomainCategories() {
        if (allDomainCategories == null) {
            allDomainCategories = categoryVariableService.findAllCategoriesSorted();
        }
        return allDomainCategories;
    }

    public List<String> getUploadedFilesFileName() {
        if (uploadedFilesFileNames == null) {
            uploadedFilesFileNames = new ArrayList<>();
        }
        return uploadedFilesFileNames;
    }

    // NOTE: Struts2 reflection is a little off with these, it assumes that,
    // even for an array of files; that the
    // names are singular for the method:
    // http://struts.apache.org/2.x/docs/how-do-we-upload-files.html
    public void setUploadedFilesFileName(List<String> uploadedFileFileName) {
        getLogger().trace("setting file name: {}", uploadedFileFileName);
        this.uploadedFilesFileNames = uploadedFileFileName;
    }

    public String getResourceProviderInstitutionName() {
        return resourceProviderInstitutionName;
    }

    public void setResourceProviderInstitutionName(String name) {
        this.resourceProviderInstitutionName = name;
    }

    public void setResourceProviderInstitution(Institution resourceProviderInstitution) {
        if (resourceProviderInstitution != null) {
            this.resourceProviderInstitutionName = resourceProviderInstitution.getName();
        }
    }

    private void loadFilesJson() {
        if (PersistableUtils.isNullOrTransient(getResource())) {
            return;
        }

        List<FileProxy> fileProxies = new ArrayList<>();
        // FIXME: this is the same logic as the initialization of the fileProxy... could use that instead, but causes a sesion issue
        for (InformationResourceFile informationResourceFile : getResource().getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }

        try {
            filesJson = serializationService.convertToJson(fileProxies);
            getLogger().debug(filesJson);
        } catch (IOException e) {
            getLogger().error("could not convert file list to json", e);
            filesJson = "[]";
        }
    }

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        setProject(getPersistable().getProject());
        setProjectId(getPersistable().getProjectId());
        super.loadCustomMetadata();
        loadInformationResourceProperties();
        loadResourceProviderInformation();
        setTransientViewableStatus(getResource(), getAuthenticatedUser());
    }

    @Override
    public String loadAddMetadata() {
        String retval = super.loadAddMetadata();
        resolveProject();
        Project obsProj = getGenericService().find(Project.class, getProjectId());
        obfuscationService.obfuscate(obsProj, getAuthenticatedUser());
        json = projectService.getProjectAsJson(obsProj, getAuthenticatedUser(), null);
        return retval;
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        setProjectId(getResource().getProjectId());
        return super.loadEditMetadata();
    }

    protected void loadInformationResourceProperties() {
        setResourceLanguage(getResource().getResourceLanguage());
        setMetadataLanguage(getResource().getMetadataLanguage());
        loadResourceProviderInformation();
        setAllowedToViewConfidentialFiles(authorizationService.canViewConfidentialInformation(getAuthenticatedUser(), getPersistable()));
        initializeFileProxies();
    }

    private void initializeFileProxies() {
        fileProxies = new ArrayList<>();
        for (InformationResourceFile informationResourceFile : getPersistable().getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }
    }

    public FileProxy getBlankFileProxy() {
        return new FileProxy();
    }

    protected void saveInformationResourceProperties() {
        // handle dataset availability + date made public
        getResource().setResourceLanguage(resourceLanguage);
        getResource().setMetadataLanguage(metadataLanguage);
        // handle dataset availability + date made public
        saveResourceProviderInformation();
    }

    public Integer getEmbargoPeriodInYears() {
        return getTdarConfiguration().getEmbargoPeriod();
    }

    public Project getProject() {
        return project;
    }

    protected void resolveProject() {
        project = Project.NULL;
        if (PersistableUtils.isNotNullOrTransient(projectId)) {
            project = getGenericService().find(Project.class, projectId);
        }
        json = projectService.getProjectAsJson(getProject(), getAuthenticatedUser(), null);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getProjectId() {
        return projectId;
    }

    /**
     * Used to set the parent project for this information resource.
     */
    public void setProjectId(Long projectId_) {
        // remove me?
        if (projectId_ == null) {
            getLogger().trace("Tried to set null project id, no-op.");
            return;
        }
        this.projectId = projectId_;
    }

    /**
     * returns list of parent projects the that the system can assign to a
     * resource - Project.NULL - authuser's projects - projects for which
     * authuser is a fulluser - resource's current parent project
     * 
     * The return list is mostly sorted, with the exception of Project.NULL
     * which is always the first item in the list
     */
    @DoNotObfuscate(reason = "always called by edit pages, so it shouldn't matter, also bad if called when user is anonymous")
    public List<Resource> getPotentialParents() {
        getLogger().debug("get potential parents");
        if (potentialParents == null) {
            TdarUser submitter = getAuthenticatedUser();
            potentialParents = new LinkedList<>();
            boolean canEditAnything = authorizationService.can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            potentialParents.addAll(projectService.findSparseTitleIdProjectListByPerson(submitter, canEditAnything));
            if (!getProject().equals(Project.NULL) && !potentialParents.contains(getProject())) {
                potentialParents.add(getProject());
            }
            // Prepend null project so that dropdowns will see "No associated project" at the top of the list.
            Project noAssociatedProject = new Project(-1L, getText("project.no_associated_project"));
            getGenericService().markReadOnly(project);
            potentialParents.add(0, noAssociatedProject);
        }
        getLogger().debug("Returning all editable projects: {}", potentialParents);
        return potentialParents;
    }

    /**
     * Saves keywords, full / read user access, and confidentiality.
     */
    @Override
    protected void saveBasicResourceMetadata() {
        // don't save any values at the resource level that we are inheriting
        // from parent
        if (getResource().isInheritingInvestigationInformation()) {
            setInvestigationTypeIds(null);
        }
        if (getResource().isInheritingSiteInformation()) {
            setSiteNameKeywords(null);
            setApprovedSiteTypeKeywordIds(null);
            setUncontrolledSiteTypeKeywords(null);
        }
        if (getResource().isInheritingMaterialInformation()) {
            setApprovedMaterialKeywordIds(null);
            setUncontrolledCultureKeywords(null);
        }
        if (getResource().isInheritingCulturalInformation()) {
            setApprovedCultureKeywordIds(null);
            setUncontrolledCultureKeywords(null);
        }
        if (getResource().isInheritingSpatialInformation()) {
            getLatitudeLongitudeBoxes().clear();
            setGeographicKeywords(null);
        }
        if (getResource().isInheritingTemporalInformation()) {
            setTemporalKeywords(null);
            getResource().getCoverageDates().clear();
        }
        if (getResource().isInheritingOtherInformation()) {
            setOtherKeywords(null);
        }

        if (getResource().isInheritingIndividualAndInstitutionalCredit()) {
            if (CollectionUtils.isNotEmpty(getCreditProxies())) {
                getCreditProxies().clear();
            }
        }

        if (getResource().isInheritingCollectionInformation()) {
            if (CollectionUtils.isNotEmpty(getRelatedComparativeCollections())) {
                getRelatedComparativeCollections().clear();
            }
            if (CollectionUtils.isNotEmpty(getSourceCollections())) {
                getSourceCollections().clear();
            }
        }

        if (getResource().isInheritingNoteInformation()) {
            if (CollectionUtils.isNotEmpty(getResourceNotes())) {
                getResourceNotes().clear();
            }
        }

        if (getResource().isInheritingIdentifierInformation()) {
            if (CollectionUtils.isNotEmpty(getResourceAnnotations())) {
                getResourceAnnotations().clear();
            }
        }

        // We set the project here to avoid getProjectId() being indexed too early (see TDAR-2001 for more info)
        resolveProject();
        getResource().setProject(getProject());
        super.saveBasicResourceMetadata();
    }

    @Autowired
    public void setFileAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Collection<String> getValidFileExtensions() {
        return Collections.emptyList();
    }

    public void setMetadataLanguage(Language language) {
        this.metadataLanguage = language;
    }

    public Language getMetadataLanguage() {
        return getPersistable().getMetadataLanguage();
    }

    public Language getResourceLanguage() {
        return getPersistable().getResourceLanguage();
    }

    public void setResourceLanguage(Language language) {
        this.resourceLanguage = language;
    }

    public List<Language> getLanguages() {
        if (languages == null) {
            languages = Arrays.asList(Language.values());
            languages.remove(Language.ENGLISH);
            languages.add(0, Language.ENGLISH);
        }
        return languages;
    }

    public String getProjectAsJson() {
        return json;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    protected void setFileProxyAction(FileProxy proxy) {
        if (getPersistable().hasFiles()) {
            getLogger().debug("Replacing existing files {} for {}", getPersistable().getInformationResourceFiles(), getPersistable());
            proxy.setAction(FileAction.REPLACE);
            proxy.setFileId(getPersistable().getFirstInformationResourceFile().getId());
            getLogger().debug("set primary file proxy irf id to {}", proxy.getFileId());
        } else {
            proxy.setAction(FileAction.ADD);
        }
    }

    public List<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public void setFileProxies(List<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    public void setAllowedToViewConfidentialFiles(boolean allowedToViewConfidentialFiles) {
        this.allowedToViewConfidentialFiles = allowedToViewConfidentialFiles;
    }

    public boolean isAllowedToViewConfidentialFiles() {
        return allowedToViewConfidentialFiles;
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        if (getPersistable() == null)
            return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#validate()
     */
    @Override
    public void validate() {
        super.validate();
        // parent should catch and report action errors for null persistable
        if (getPersistable() == null) {
            return;
        }

        if (getPersistable().getDate() == null) {
            getLogger().debug("Invalid date created for {}", getPersistable());
            String resourceTypeLabel = getText(getPersistable().getResourceType().name());
            addActionError("Please enter a valid creation year for " + resourceTypeLabel);
        }
        if (getTdarConfiguration().getCopyrightMandatory()) {
            // first check to see if the form has copyright holders specified
            if (copyrightHolderProxies != null && copyrightHolderProxies.getActualCreatorType() != null) {
                ResourceCreator transientCreator = copyrightHolderProxies.getResourceCreator();
                getLogger().info("{} {}", copyrightHolderProxies, transientCreator);
                if (transientCreator != null && StringUtils.isEmpty(transientCreator.getCreator().getProperName().trim())) {
                    getLogger().debug("No copyright holder set for {}", getPersistable());
                    addActionError(getText("abstractInformationResourceController.add_copyright_holder"));
                }
                // and if not on a form (the reprocess below, for example, then check the persistable itself
            } else if (getPersistable().getCopyrightHolder() == null) {
                addActionError(getText("abstractInformationResourceController.copyright_holder_missing"));
            }
        }
    }

    public boolean isHasDeletedFiles() {
        return hasDeletedFiles;
    }

    public void setHasDeletedFiles(boolean hasDeletedFiles) {
        this.hasDeletedFiles = hasDeletedFiles;
    }

    public void setCopyrightHolderProxies(ResourceCreatorProxy copyrightHolderProxy) {
        this.copyrightHolderProxies = copyrightHolderProxy;
    }

    public ResourceCreatorProxy getCopyrightHolderProxies() {
        return copyrightHolderProxies;
    }

    public List<FileAccessRestriction> getFileAccessRestrictions() {
        return Arrays.asList(FileAccessRestriction.values());
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public boolean isAbleToUploadFiles() {
        if (isAbleToUploadFiles == null) {
            isAbleToUploadFiles = authorizationService.canUploadFiles(getAuthenticatedUser(), getPersistable());
        }
        return isAbleToUploadFiles;
    }

    public boolean isHasFileProxyChanges() {
        return hasFileProxyChanges;
    }

    public void setHasFileProxyChanges(boolean hasFileProxyChanges) {
        this.hasFileProxyChanges = hasFileProxyChanges;
    }

    public String getFilesJson() {
        loadFilesJson();
        return filesJson;
    }

    public FileAnalyzer getAnalyzer() {
        return analyzer;
    }

    public boolean isResourceEditPage() {
        return true;
    }

    public List<Pair<InformationResourceFile, ExceptionWrapper>> getHistoricalFileErrors() {
        List<Pair<InformationResourceFile, ExceptionWrapper>> toReturn = new ArrayList<>();
        try {
            if (isHasFileProxyChanges()) {
                return toReturn;
            }

            if (getPersistable() == null || CollectionUtils.isEmpty(getPersistable().getFilesWithFatalProcessingErrors())) {
                return toReturn;
            }

            for (InformationResourceFile file : getPersistable().getFilesWithProcessingErrors()) {
                if (file.isDeleted()) {
                    continue;
                }
                String message = file.getErrorMessage();
                String stackTrace = file.getErrorMessage();
                if (StringUtils.contains(message, ExceptionWrapper.SEPARATOR)) {
                    message = message.substring(0, message.indexOf(ExceptionWrapper.SEPARATOR));
                    stackTrace = stackTrace.substring(stackTrace.indexOf(ExceptionWrapper.SEPARATOR) + 2);
                }
                Pair<InformationResourceFile, ExceptionWrapper> pair = Pair.create(file, new ExceptionWrapper(message, stackTrace));
                toReturn.add(pair);
            }
        } catch (LazyInitializationException lae) {
            getLogger().trace("lazy initializatione exception -- ignore in this case, likely session has been actively closed by SessionSecurityInterceptor");
        } catch (Exception e) {
            getLogger().error("got an exception while evaluating whether we should show one, should we?", e);
        }
        return toReturn;
    }

    @Override
    public List<EmailMessageType> getEmailTypes() {
        List<EmailMessageType> types = new ArrayList<>(super.getEmailTypes());
        if (getPersistable().hasConfidentialFiles()) {
            types.add(EmailMessageType.REQUEST_ACCESS);
        }
        return types;
    }

}
