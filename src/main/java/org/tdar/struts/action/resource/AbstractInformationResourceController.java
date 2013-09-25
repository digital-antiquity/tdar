package org.tdar.struts.action.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.FileProxyService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.XmlService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.ResourceCreatorProxy;

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
    protected FileProxyService fileProxyService;

    @Autowired
    protected XmlService xmlService;

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


    private Long projectId;

    // previously uploaded files list in json format, needed by blueimp jquery file upload
    private String filesJson = "";

    private Boolean isAbleToUploadFiles = null;

    // private List<PersonalFilestoreFile> pendingFiles;

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

    // private boolean resourceFilesHaveChanged = false;

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
        throw new UnsupportedOperationException(getClass() + " didn't override properly");
    }

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    /*
     * Creating a simple transient boolean to handle visibility here instead of freemarker
     */
    public void setTransientViewableStatus(InformationResource ir, Person p) {
        getAuthenticationAndAuthorizationService().applyTransientViewableFlag(ir, p);
        if (Persistable.Base.isNotNullOrTransient(p)) {
            for (InformationResourceFile irf : ir.getInformationResourceFiles()) {
                getInformationResourceFileService().updateTransientDownloadCount(irf);
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
            logger.debug("Final proxy set: {}", proxies);

            for (FileProxy proxy : proxies) {
                if (proxy != null && proxy.getAction() != FileAction.NONE) {
                    setHasFileProxyChanges(true);
                }
            }

        } catch (TdarRecoverableRuntimeException trrc) {
            addActionErrorWithException(WE_WERE_UNABLE_TO_PROCESS_THE_UPLOADED_CONTENT, trrc);
        }

        if (isHasFileProxyChanges()
                && !getAuthenticationAndAuthorizationService().canDo(getAuthenticatedUser(), getResource(), InternalTdarRights.EDIT_ANY_RESOURCE,
                        GeneralPermissions.MODIFY_RECORD)) {
            throw new TdarActionException(StatusCode.FORBIDDEN, "You do not have permissions to upload or modify files");
        }

        try {
            getInformationResourceService().importFileProxiesAndProcessThroughWorkflow(getPersistable(), getAuthenticatedUser(), ticketId, this, proxies);
        } catch (Exception e) {
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
            allDomainCategories = getCategoryVariableService().findAllCategoriesSorted();
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
        if (Persistable.Base.isNullOrTransient(getResource())) {
            return;
        }

        List<FileProxy> fileProxies = new ArrayList<>();
        for (InformationResourceFile informationResourceFile : getResource().getInformationResourceFiles()) {
            if (!informationResourceFile.isDeleted()) {
                fileProxies.add(new FileProxy(informationResourceFile));
            }
        }
        try {
            filesJson = xmlService.convertToJson(fileProxies);
        } catch (IOException e) {
            logger.error("could not convert file list to json", e);
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

    protected void loadInformationResourceProperties() {
        setResourceLanguage(getResource().getResourceLanguage());
        setMetadataLanguage(getResource().getMetadataLanguage());
        loadResourceProviderInformation();
        setAllowedToViewConfidentialFiles(getAuthenticationAndAuthorizationService().canViewConfidentialInformation(getAuthenticatedUser(), getPersistable()));
        initializeFileProxies();
        try {
            getDatasetService().assignMappedDataForInformationResource(getResource());
        } catch (Exception e) {
            logger.error("could not attach additional dataset data to resource", e);
        }
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
        if (Persistable.Base.isNotNullOrTransient(projectId)) {
            project = getGenericService().find(Project.class, projectId);
            return;
        } 
    }

    protected void setProject(Project project) {
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
            logger.warn("Tried to set null project id, no-op.");
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
    public List<Resource> getPotentialParents() {
        logger.info("get potential parents");
        if (potentialParents == null) {
            Person submitter = getAuthenticatedUser();
            potentialParents = new LinkedList<>();
            boolean canEditAnything = getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_ANYTHING, getAuthenticatedUser());
            potentialParents.addAll(getProjectService().findSparseTitleIdProjectListByPerson(submitter, canEditAnything));
            if (!getProject().equals(Project.NULL) && !potentialParents.contains(getProject())) {
                potentialParents.add(getProject());
            }
            // tack the null project at the top of the sorted list
            // Collections.sort(potentialParents);
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
        if (languages == null)
            languages = getInformationResourceService().findAllLanguages();
        return languages;
    }

    public String getProjectAsJson() {
        return getProject().toJSON().toString();
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    protected void setFileProxyAction(FileProxy proxy) {
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
            // first check to see if the form has copyright holders specified
            if (copyrightHolderProxies != null && copyrightHolderProxies.getActualCreatorType() != null) {
                ResourceCreator transientCreator = copyrightHolderProxies.getResourceCreator();
                logger.info("{} {}", copyrightHolderProxies, transientCreator);
                if (transientCreator != null && StringUtils.isEmpty(transientCreator.getCreator().getProperName().trim())) {
                    logger.debug("No copyright holder set for {}", getPersistable());
                    addActionError("Please enter a copyright holder!");
                }
                // and if not on a form (the reprocess below, for example, then check the persistable itself
            } else if (getPersistable().getCopyrightHolder() == null) {
                addActionError("The required copyright holder is missing!");
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
            isAbleToUploadFiles = getAuthenticationAndAuthorizationService().canUploadFiles(getAuthenticatedUser(), getPersistable());
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

}
