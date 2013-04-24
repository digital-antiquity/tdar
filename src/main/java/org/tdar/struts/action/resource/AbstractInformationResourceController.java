package org.tdar.struts.action.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResourceCreatorProxy;
import org.tdar.utils.HashQueue;

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

    public static final String WE_WERE_UNABLE_TO_PROCESS_THE_UPLOADED_CONTENT = "We were unable to process the uploaded content.";

    public static final String FILE_INPUT_METHOD = "text";

    private static final long serialVersionUID = -200666002871956655L;

    private List<CategoryVariable> allDomainCategories;

    private Project project;
    private List<Resource> potentialParents;

    // incoming data
    private List<File> uploadedFiles;
    private List<String> uploadedFileContentTypes; // unused I think (hope)
    private List<String> uploadedFilesFileNames;
    private Language resourceLanguage;
    private Language metadataLanguage;
    private List<Language> languages;
    private List<FileProxy> fileProxies = new ArrayList<FileProxy>();

    private String fileInputMethod;
    private String fileTextInput;
    private Boolean isAbleToUploadFiles = null;

    private List<PersonalFilestoreFile> pendingFiles;

    private Long ticketId;

    // resource provider institution and contacts
    private String resourceProviderInstitutionName;
    private String publisherName;

    // resource availability
    // private String resourceAvailability;
    private boolean allowedToViewConfidentialFiles;
    protected FileAnalyzer analyzer;
    private boolean hasDeletedFiles = false;
    // protected PersonalFilestoreTicket filestoreTicket;
    private ResourceCreatorProxy copyrightHolderProxies = new ResourceCreatorProxy();

    @Autowired
    protected PersonalFilestoreService filestoreService;

    private boolean resourceFilesHaveChanged = false;

    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        return;
    }

    /**
     * This should be overridden when InformationResource content is entered from a text area in the web form.
     * Currently the only InformationResourceS that employ this method of content/data entry are CodingSheetS and OntologyS.
     * 
     * Returns a FileProxy representing the content that was entered.
     */
    protected FileProxy processTextInput() {
        if (StringUtils.isBlank(fileTextInput)) {
            addActionError("Please enter your " + getPersistable().getResourceType().getLabel() + " into the text area.");
            return null;
        }
        if (fileTextInput.equals(getLatestUploadedTextVersionText())) {
            logger.info("incoming and current file input text is the same, skipping further actions");
            return null;
        } else {
            logger.info("processing updated text input for {}", getPersistable());
        }

        try {
            // process the String uploaded via the fileTextInput box verbatim as the UPLOADED_TEXT version
            if (StringUtils.isBlank(getPersistable().getTitle())) {
                logger.error("Resource title was empty, client side validation failed for {}", getPersistable());
                addActionError("Please enter a title for your " + getPersistable().getResourceType().getLabel());
                return null;
            }
            String uploadedTextFilename = getPersistable().getTitle() + ".txt";

            FileProxy uploadedTextFileProxy = new FileProxy(uploadedTextFilename,
                    FileProxy.createTempFileFromString(fileTextInput),
                    VersionType.UPLOADED_TEXT);

            // next, generate "uploaded" version of the file. In this case the VersionType.UPLOADED isn't entirely accurate
            // as this is UPLOADED_GENERATED, but it's the file that we want to process in later parts of our code.
            FileProxy primaryFileProxy = createUploadedFileProxy(fileTextInput);
            primaryFileProxy.addVersion(uploadedTextFileProxy);
            setFileProxyAction(primaryFileProxy);
            return primaryFileProxy;
        } catch (IOException e) {
            getLogger().error("unable to create temp file or write " + fileTextInput + " to temp file", e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        throw new UnsupportedOperationException(getClass() + " didn't override properly");
    }

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, Person p) {
        getAuthenticationAndAuthorizationService().setTransientViewableStatus(ir, p);
        for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
            getInformationResourceFileService().updateTransientDownloadCount(irf);
            if (irf.isDeleted()) {
                setHasDeletedFiles(true);
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
        cullInvalidProxies(fileProxies);
        pendingFiles = filestoreService.retrieveAllPersonalFilestoreFiles(ticketId);
        ArrayList<FileProxy> finalProxyList = new ArrayList<FileProxy>(fileProxies);

        // subset of proxy list, hashed into queues.
        HashQueue<String, FileProxy> proxiesNeedingFiles = buildProxyQueue(fileProxies);

        // FIXME: trying to handle duplicate filenames more gracefully by using hashqueue instead of hashmap, but this assumes that the sequence of pending
        // files
        // is *similar* to sequence of incoming file proxies. probably a dodgy assumption, but arguably better than obliterating proxies w/ dupe filenames
        logger.info("{}", pendingFiles);
        logger.info("{}", fileProxies);
        // associates InputStreams with all FileProxy objects that need to create a new version.
        for (PersonalFilestoreFile pendingFile : pendingFiles) {
            File file = pendingFile.getFile();
            FileProxy proxy = proxiesNeedingFiles.poll(file.getName());
            // if we encounter file that has no matching proxy, we create a new proxy and add it to the final list
            // we assume this happens when proxy fields in form were submitted in state that struts could not type-convert into a proxy instance
            if (proxy == null) {
                logger.warn("something bad happened in the JS side of things, there should always be a FileProxy resulting from the upload callback {}",
                        file.getName());
                proxy = new FileProxy(file.getName(), VersionType.UPLOADED, FileAccessRestriction.PUBLIC);
                finalProxyList.add(proxy);
            }
            proxy.setFile(file);
        }

        Collections.sort(finalProxyList);
        return finalProxyList;
    }

    // build a priorityqueue of proxies that expect files.
    private HashQueue<String, FileProxy> buildProxyQueue(List<FileProxy> proxies) {
        HashQueue<String, FileProxy> hashQueue = new HashQueue<String, FileProxy>();
        for (FileProxy proxy : proxies) {
            if (proxy == null)
                continue;
            if (proxy.getAction() == null) {
                logger.error("null proxy action on '{}'", proxy);
                proxy.setAction(FileAction.NONE);
            }
            if (proxy.getAction().shouldExpectFileHandle()) {
                hashQueue.push(proxy.getFilename(), proxy);
            }
        }
        return hashQueue;
    }

    // return a list of fileProxies, culling null and invalid instances
    private void cullInvalidProxies(List<FileProxy> proxies) {
        logger.debug("file proxies: {} ", proxies);
        ListIterator<FileProxy> iterator = proxies.listIterator();
        while (iterator.hasNext()) {
            FileProxy proxy = iterator.next();
            if (proxy == null) {
                logger.debug("fileProxy[{}] is null - culling", iterator.previousIndex());
                iterator.remove();
            } else if (StringUtils.isEmpty(proxy.getFilename())) {
                logger.debug("fileProxy[{}].fileName is blank - culling (value: {})", iterator.previousIndex(), proxy);
                iterator.remove();
            }
        }
    }

    /**
     * One-size-fits-all method for handling uploaded InformationResource files.
     * 
     * Handles text input files for coding sheets and ontologies,
     * async uploads, and single-file dataset uploads.
     */
    protected void handleUploadedFiles() {
        getLogger().debug("handling uploaded files for {}", getPersistable());
        List<FileProxy> fileProxiesToProcess = getFileProxiesToProcess();
        if (CollectionUtils.isEmpty(fileProxiesToProcess)) {
            logger.debug("Nothing to process, returning.");
            return;
        }
        logger.debug("Final proxy set: {}", fileProxiesToProcess);
        ArrayList<InformationResourceFile> modifiedFiles = new ArrayList<InformationResourceFile>();
        for (FileProxy fileProxy : fileProxiesToProcess) {
            try {
                InformationResourceFile file = getInformationResourceService()
                        .processFileProxy(getPersistable(), fileProxy);
                if (file != null) {
                    modifiedFiles.add(file);
                    if (file.getWorkflowContext() != null) {
                        List<String> exceptions = file.getWorkflowContext().getExceptions();
                        logger.info("EXCEPTIONS: {}", exceptions);
                        logger.info("STACK TRACES: {}", file.getWorkflowContext().getStackTraces());
                        if (CollectionUtils.isNotEmpty(exceptions)) {
                            for (String except : exceptions) {
                                addActionError(except);
                            }
                            getStackTraces().addAll(file.getWorkflowContext().getStackTraces());
                        }
                    }
                }
            } catch (IOException exception) {
                addActionErrorWithException("Unable to process file " + fileProxy.getFilename(), exception);
            }
        }
        // FIXME: should be refactored to take in a Set<FileProxy> files to be processed?
        try {
            setResourceFilesHaveChanged(true);
            processUploadedFiles(modifiedFiles);
        } catch (IOException e) {
            addActionErrorWithException(WE_WERE_UNABLE_TO_PROCESS_THE_UPLOADED_CONTENT, e);
        }
        getInformationResourceService().saveOrUpdate(getPersistable());
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
        List<FileProxy> fileProxiesToProcess = new ArrayList<FileProxy>();
        // Possible scenarios:
        if (isTextInput()) {
            // 1. text input for CodingSheet or Ontology (everything in a String, needs preprocessing to convert to a FileProxy)
            FileProxy textInputFileProxy = processTextInput();
            if (textInputFileProxy != null) {
                fileProxiesToProcess.add(textInputFileProxy);
            }
        } else if (isMultipleFileUploadEnabled()) {
            // 2. async uploads for Image or Document or ...
            fileProxiesToProcess = handleAsyncUploads();
        } else {
            // 3. single file upload (dataset|coding sheet|ontology)
            // there could be an incoming file payload, or just a metadata change.

            FileProxy singleFileProxy = CollectionUtils.isEmpty(fileProxies) ? new FileProxy() : fileProxies.get(0);
            if (CollectionUtils.isEmpty(uploadedFiles)) {
                // check for metadata change iff this resource has an existing file.
                InformationResourceFile file = getPersistable().getFirstInformationResourceFile();
                if (file != null && (file.getRestriction() != singleFileProxy.getRestriction())) {
                    singleFileProxy.setAction(FileAction.MODIFY_METADATA);
                    singleFileProxy.setFileId(file.getId());
                    fileProxiesToProcess.add(singleFileProxy);
                }
            } else {
                // process a new uploaded file (either ADD or REPLACE)
                setFileProxyAction(singleFileProxy);
                singleFileProxy.setFilename(uploadedFilesFileNames.get(0));
                singleFileProxy.setFile(uploadedFiles.get(0));
                fileProxiesToProcess.add(singleFileProxy);
            }
        }
        return fileProxiesToProcess;
    }

    @Override
    /**
     * safely remove/destroy resources created during the course of a save action (including files in the personal filestore).  If you need
     * access to resources outside of request scope you should override this method and assume responsibility for file management).
     */
    protected void postSaveCleanup(String returnString) {
        try {
            if (ticketId != null) {
                PersonalFilestore filestore = filestoreService.getPersonalFilestore(getAuthenticatedUser());
                filestore.purge(getGenericService().find(PersonalFilestoreTicket.class, ticketId));
            }
        } catch (Exception e) {
            logger.warn("an error occured when trying to cleanup the filestore: {} for {} ", ticketId, getAuthenticatedUser());
            logger.debug("exception:", e);
        }
    }

    protected void loadResourceProviderInformation() {
        // load resource provider institution and publishers
        setResourceProviderInstitution(getResource().getResourceProviderInstitution());
        setPublisherName(getResource().getPublisherName());
        if (isCopyrightMandatory() && Persistable.Base.isNotNullOrTransient(getResource().getCopyrightHolder())) {
            copyrightHolderProxies = new ResourceCreatorProxy(getResource().getCopyrightHolder(), ResourceCreatorRole.COPYRIGHT_HOLDER);
        }
    }

    protected void saveResourceProviderInformation() {
        getLogger().debug("Saving resource provider information: {}", resourceProviderInstitutionName);
        // save resource provider institution and contact information
        // TODO: use findOrSaveInstitution()
        if (StringUtils.isNotBlank(resourceProviderInstitutionName)) {
            getResource().setResourceProviderInstitution(getEntityService().findOrSaveCreator(new Institution(resourceProviderInstitutionName)));
        }

        if (StringUtils.isNotBlank(publisherName)) {
            getResource().setPublisher(getEntityService().findOrSaveCreator(new Institution(publisherName)));
        } else {
            getResource().setPublisher(null);
        }

        if (isCopyrightMandatory() && copyrightHolderProxies != null) {
            ResourceCreator transientCreator = copyrightHolderProxies.getResourceCreator();
            logger.debug("setting copyright holder to:  {} ", transientCreator);
            getResource().setCopyrightHolder(getEntityService().findOrSaveCreator(transientCreator.getCreator()));
        }
    }

    public void setPublisher(String publisherName) {
        this.publisherName = publisherName;
    }

    public ArrayList<LicenseType> getLicenseTypesList() {
        return new ArrayList<LicenseType>(Arrays.asList(LicenseType.values()));
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
            allDomainCategories = getCategoryVariableService().findAllCategoriesSorted();
        }
        return allDomainCategories;
    }

    public List<String> getUploadedFilesFileName() {
        if (uploadedFilesFileNames == null) {
            uploadedFilesFileNames = new ArrayList<String>();
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

    @Override
    public String loadAddMetadata() {
        String toReturn = super.loadAddMetadata();
        return toReturn;
    }

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        super.loadCustomMetadata();
        loadInformationResourceProperties();
        loadResourceProviderInformation();
        setTransientViewableStatus(getResource(), getAuthenticatedUser());
    }

    protected void loadInformationResourceProperties() {
        setResourceLanguage(getResource().getResourceLanguage());
        setMetadataLanguage(getResource().getMetadataLanguage());
        loadResourceProviderInformation();
        setAllowedToViewConfidentialFiles(getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), getPersistable()));
        initializeFileProxies();
        getDatasetService().assignMappedDataForInformationResource(getResource());
    }

    private void initializeFileProxies() {
        fileProxies = new ArrayList<FileProxy>();
        for (InformationResourceFile informationResourceFile : getPersistable().getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }
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
        if (project == null) {
            project = Project.NULL;
        }
        return project;
    }

    protected void setProject(Project project) {
        this.project = project;
    }

    /**
     * Used to set the parent project for this information resource.
     */
    public void setProjectId(Long projectId) {
        if (projectId == null) {
            logger.warn("Tried to set null project id, no-op.");
            return;
        }
        // look up the Project with the given projectId if either of the
        // following conditions hold:
        // 1. the existing project is null
        // 2. the existing project's id is different from the incoming project's
        // id.
        if (project == null || !projectId.equals(project.getId())) {
            project = getProjectService().find(projectId);
        }
    }

    /**
     * returns list of parent projects the that the system can assign to a
     * resource - Project.NULL - authuser's projects - projects for which
     * authuser is a fulluser - resource's current parent project
     * 
     * The return list is mostly sorted, with the exception of Project.NULL
     * which is always the first item in the list
     */
    public List<Resource> getPotentialParents() {
        logger.info("get potential parents");
        if (potentialParents == null) {
            Person submitter = getAuthenticatedUser();
            potentialParents = new ArrayList<Resource>();
            boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            potentialParents.addAll(getProjectService().findSparseTitleIdProjectListByPerson(submitter, canEditAnything));
            if (!getProject().equals(Project.NULL) && !potentialParents.contains(getProject())) {
                potentialParents.add(getProject());
            }
            // tack the null project at the top of the sorted list
            Collections.sort(potentialParents);
            potentialParents.add(0, Project.NULL);
        }
        logger.trace("Returning all editable projects: {}", potentialParents);
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
            setMaterialKeywordIds(null);
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
        // FIXME: we need to set the project at this point to avoid getProjectId() being indexed too early
        // see TDAR-2001
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
        if (languages == null)
            languages = getInformationResourceService().findAllLanguages();
        return languages;
    }

    public String getProjectAsJson() {
        return getProject().toJSON().toString();
        // return json;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    protected String getLatestUploadedTextVersionText() {
        // in order for this to work we need to be generating text versions
        // of these files for both text input and file uploads
        for (InformationResourceFileVersion version : getPersistable().getLatestVersions(VersionType.UPLOADED_TEXT)) {
            try {
                return FileUtils.readFileToString(version.getFile());
            } catch (Exception e) {
                logger.debug("an error occurred when trying to load the text version of a file", e);
            }
        }
        return "";
    }

    private void setFileProxyAction(FileProxy proxy) {
        if (getPersistable().hasFiles()) {
            logger.debug("Replacing existing files {} for {}", getPersistable().getInformationResourceFiles(), getPersistable());
            proxy.setAction(FileAction.REPLACE);
            proxy.setFileId(getPersistable().getFirstInformationResourceFile().getId());
            logger.debug("set primary file proxy irf id to {}", proxy.getFileId());
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

    private boolean isTextInput() {
        return FILE_INPUT_METHOD.equals(fileInputMethod);
    }

    public String getFileInputMethod() {
        return fileInputMethod;
    }

    public void setFileInputMethod(String fileInputMethod) {
        this.fileInputMethod = fileInputMethod;
    }

    public String getFileTextInput() {
        return fileTextInput;
    }

    public void setFileTextInput(String fileTextInput) {
        this.fileTextInput = fileTextInput;
    }

    public void setAllowedToViewConfidentialFiles(boolean allowedToViewConfidentialFiles) {
        this.allowedToViewConfidentialFiles = allowedToViewConfidentialFiles;
    }

    public boolean isAllowedToViewConfidentialFiles() {
        return allowedToViewConfidentialFiles;
    }

    @Override
    public void prepare() {
        super.prepare();
        if (getPersistable() == null)
            return;
        setProject(getPersistable().getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#validate()
     */
    @Override
    public void validate() {
        super.validate();
        if (getPersistable().getDate() == null) {
            logger.debug("Invalid date created for {}", getPersistable());
            String resourceTypeLabel = getPersistable().getResourceType().getLabel();
            addActionError("Please enter a valid creation year for " + resourceTypeLabel);
        }
        if (isCopyrightMandatory()) {
            if (copyrightHolderProxies != null && copyrightHolderProxies.getActualCreatorType() != null) {
                ResourceCreator transientCreator = copyrightHolderProxies.getResourceCreator();
                logger.info("{} {}", copyrightHolderProxies, transientCreator);
                if (transientCreator != null && StringUtils.isEmpty(transientCreator.getCreator().getProperName().trim())) {
                    logger.debug("No copyright holder set for {}", getPersistable());
                    addActionError("Please enter a copyright holder!");
                }
            } else {
                addActionError("Please enter a copyright holder!");
            }
        }
    }

    @Action(value = "reprocess", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}") })
    public String retranslate() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        // FIXME: trying to avoid concurrent modification exceptions
        // NOTE: this processes deleted ones again too
        // NOTE2: this is ignored in the quota on purpose -- it's on us
        getInformationResourceService().reprocessInformationResourceFiles(new ArrayList<InformationResourceFile>(getResource().getInformationResourceFiles()));

        return SUCCESS;
    }

    public boolean isResourceFilesHaveChanged() {
        return resourceFilesHaveChanged;
    }

    public void setResourceFilesHaveChanged(boolean resourceFilesHaveChanged) {
        this.resourceFilesHaveChanged = resourceFilesHaveChanged;
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

    public boolean supportsMultipleFileUpload() {
        return true;
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
        if(isAbleToUploadFiles == null) {
            isAbleToUploadFiles = getAuthenticationAndAuthorizationService().canUploadFiles(getAuthenticatedUser(), getPersistable());
        }
        return isAbleToUploadFiles;
    }


}
