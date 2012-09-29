package org.tdar.struts.action.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.LanguageEnum;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.FilestoreService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.PersonalFilestore;
import org.tdar.filestore.PersonalFilestoreFile;
import org.tdar.struts.data.FileProxy;

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

    private static final long serialVersionUID = -200666002871956655L;

    private List<CategoryVariable> allDomainCategories;

    private Project project;
    private List<Resource> potentialParents;

    // incoming data
    private List<File> uploadedFiles;
    private List<String> uploadedFileContentTypes; // unused I think (hope)
    private List<String> uploadedFilesFileNames;
    private LanguageEnum resourceLanguage;
    private LanguageEnum metadataLanguage;
    private List<LanguageEnum> languages;
    // private List<InformationResourceFileProxy> currentVersionFiles;
    // private Collection<InformationResourceFileVersion> accessibleFiles;
    private List<FileProxy> fileProxies = new ArrayList<FileProxy>();

    private String fileInputMethod;
    private String fileTextInput;

    private List<PersonalFilestoreFile> pendingFiles;

    private Long ticketId;

    // resource provider institution and contacts
    private String resourceProviderInstitution;

    // resource availability
    private String resourceAvailability;
    private boolean allowedToViewConfidentialFiles;
    protected FileAnalyzer analyzer;

    protected PersonalFilestoreTicket filestoreTicket;

    @Autowired
    protected FilestoreService filestoreService;

    protected abstract void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException;

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
                    new ByteArrayInputStream(fileTextInput.getBytes("UTF-8")),
                    VersionType.UPLOADED_TEXT);

            // next, generate "uploaded" version of the file. In this case the VersionType.UPLOADED isn't entirely accurate
            // as this is UPLOADED_GENERATED, but it's the file that we want to process in later parts of our code.
            FileProxy primaryFileProxy = createUploadedFileProxy(fileTextInput);
            primaryFileProxy.addVersion(uploadedTextFileProxy);
            setFileProxyAction(primaryFileProxy);
            return primaryFileProxy;
        } catch (UnsupportedEncodingException e) {
            String error = "Error encoding input content as UTF-8: \n" + fileTextInput;
            getLogger().error(error, e);
            throw new TdarRecoverableRuntimeException(error, e);
        }
    }

    protected FileProxy createUploadedFileProxy(String fileTextInput) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException(getClass() + " didn't override properly");
    }

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    /**
     * Returns a List<FileProxy> representing all FileProxy objects to be processed.
     * Unifies the incoming fileProxies from the web layer with the PersonalFilestoreFiles associated with
     * ticketId, generating appropriate InputStreams on the FileProxy objects.
     * 
     * @return a List<FileProxy> representing all fully initialized FileProxy objects to be processed by the service layer.
     */
    protected List<FileProxy> handleAsyncUploads() {
        if (ticketId == null) {
            return fileProxies;
        }
        filestoreTicket = getGenericService().find(PersonalFilestoreTicket.class, ticketId);
        pendingFiles = filestoreService.retrieveAllPersonalFilestoreFiles(filestoreTicket);
        LinkedHashMap<String, FileProxy> filenameProxyMap = new LinkedHashMap<String, FileProxy>();
        logger.debug("file proxies: {} ", fileProxies);
        for (FileProxy fileProxy : fileProxies) {
            if (fileProxy == null) {
                // was pending & deleted, skip
                logger.debug("Skipping null file proxy ostensibly resulting from a deleted pending file.");
                continue;
            }
            if (StringUtils.isEmpty(fileProxy.getFilename())) {
                logger.warn("file proxy with no filename {}", fileProxy);
                continue;
            }
            // FIXME: this logic shouldn't be necessary with the new ftl changes, but the
            // tests aren't updated to deal with this.
            // 1. if there is no existing proxy with the same filename, add it.
            // 2. if there is an existing proxy, replace it if this fileProxy is a REPLACE.
            // FileProxy existingProxy = filenameProxyMap.get(fileProxy.getFilename());
            // if (existingProxy == null || fileProxy.getAction() == FileAction.REPLACE) {
            // filenameProxyMap.put(fileProxy.getFilename(), fileProxy);
            // }
            logger.debug("putting {} into the map", fileProxy);
            filenameProxyMap.put(fileProxy.getFilename(), fileProxy);
        }
        // associates InputStreams with all FileProxy objects that need to create a new version.
        for (PersonalFilestoreFile pendingFile : pendingFiles) {
            File file = pendingFile.getFile();
            FileProxy proxy = filenameProxyMap.get(file.getName());
            if (proxy == null) {
                logger.debug("something bad happened in the JS side of things, there should always be a FileProxy resulting from the upload callback {}",
                        file.getName());
                proxy = new FileProxy(file.getName(), VersionType.UPLOADED, false);
                filenameProxyMap.put(file.getName(), proxy);
            }
            try {
                proxy.setInputStream(new FileInputStream(file));
            } catch (IOException exception) {
                addActionErrorWithException("Unable to get an InputStream to " + file.getName(), exception);
                // remove FileProxy since we won't be able to process it
                filenameProxyMap.remove(file.getName());
                continue;
            }
            // TRYING TO FIX THE CASE WHERE SOMETHING weird HAPPEND & THE CONTROLLER GOT SOME METHOD
            // THAT SAYS MODIFY_METADATA, BUT THERE'S A FILE ATTACHED... WHEN IN DOUBT, ADD THE THING
            if (!proxy.getAction().shouldExpectFileHandle() && proxy.getInputStream() != null) {
                logger.debug("resetting file proxy action to ADD because it has a FILE ATTACHED");
                proxy.setAction(FileAction.ADD);
            }
        }
        ArrayList<FileProxy> finalProxySet = new ArrayList<FileProxy>(filenameProxyMap.values());
        Collections.sort(finalProxySet);
        // FIXME: apply sequence numbering?
        return finalProxySet;

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
                InformationResourceFile file = getInformationResourceService().processFileProxy(getPersistable(), fileProxy);
                modifiedFiles.add(file);
            } catch (IOException exception) {
                addActionErrorWithException("Unable to process file " + fileProxy.getFilename(), exception);
            }
        }
        // FIXME: should be refactored to take in a Set<FileProxy> files to be processed?
        try {
            processUploadedFiles(modifiedFiles);
        } catch (IOException e) {
            addActionErrorWithException("We were unable to process the uploaded content.", e);
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
                if (file != null && (file.isConfidential() != singleFileProxy.isConfidential())) {
                    singleFileProxy.setAction(FileAction.MODIFY_METADATA);
                    singleFileProxy.setFileId(file.getId());
                    fileProxiesToProcess.add(singleFileProxy);
                }
            } else {
                // process a new uploaded file (either ADD or REPLACE)
                try {
                    setFileProxyAction(singleFileProxy);
                    singleFileProxy.setFilename(uploadedFilesFileNames.get(0));
                    singleFileProxy.setInputStream(new FileInputStream(uploadedFiles.get(0)));
                    fileProxiesToProcess.add(singleFileProxy);
                } catch (FileNotFoundException exception) {
                    addActionErrorWithException("Couldn't find file " + uploadedFilesFileNames.get(0), exception);
                }
            }
        }
        return fileProxiesToProcess;
    }

    @Override
    protected void postSaveCleanup() {
        if (isAsync()) {
            return;
        }
        try {
            if (filestoreTicket != null) {
                PersonalFilestore filestore = filestoreService.getPersonalFilestore(getAuthenticatedUser());
                filestore.purge(filestoreTicket);

            }
        } catch (Exception e) {
            logger.warn("an error occured when trying to cleanup the filestore: {} for {} ", filestoreTicket, getAuthenticatedUser());
            e.printStackTrace();
        }
        // getInformationResourceService().updateProjectIndex(getPersistable().getProjectId());
    }

    protected void loadResourceProviderInformation() {
        // load resource provider institution and publishers
        setResourceProviderInstitutionObject(getResource().getResourceProviderInstitution());

    }

    protected void saveResourceProviderInformation() {
        getLogger().debug("Saving resource provider information: {}", resourceProviderInstitution);
        // save resource provider institution and contact information
        // TODO: use findOrSaveInstitution()
        if (StringUtils.isNotBlank(resourceProviderInstitution)) {
            Institution institution = getEntityService().findInstitutionByName(resourceProviderInstitution);
            if (institution == null) {
                institution = new Institution();
                institution.setName(resourceProviderInstitution);
                getEntityService().save(institution);
            }
            getResource().setResourceProviderInstitution(institution);
        }
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

    public String getResourceProviderInstitution() {
        return resourceProviderInstitution;
    }

    public void setResourceProviderInstitution(String resourceProviderInstitution) {
        this.resourceProviderInstitution = resourceProviderInstitution;
    }

    //FIXME: WAS setResourceProvicerInstitution(institution), but was causing issues w/ struts type conversion
    //what we really should do is have the controller/freemarker just use the institution object (e.g.  resourceProvider.name)
    public void setResourceProviderInstitutionObject(Institution resourceProviderInstitution) {
        if (resourceProviderInstitution != null) {
            this.resourceProviderInstitution = resourceProviderInstitution.getName();
        }
    }

    public String getResourceAvailability() {
        return resourceAvailability;
    }

    public void setResourceAvailability(String resourceAvailability) {
        this.resourceAvailability = resourceAvailability;
    }

    protected void loadInformationResourceProperties() {
        setResourceAvailability(getResource().isAvailableToPublic() ? "Public" : "Embargoed");
        setResourceLanguage(getResource().getResourceLanguage());
        setMetadataLanguage(getResource().getMetadataLanguage());
        loadResourceProviderInformation();
        setAllowedToViewConfidentialFiles(getEntityService().canViewConfidentialInformation(getAuthenticatedUser(), getPersistable()));
        initializeFileProxies();
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
        boolean availableToPublic = isResourcePublic();
        getResource().setAvailableToPublic(availableToPublic);
        Calendar calendar = Calendar.getInstance();
        if (!availableToPublic) {
            // set date made public to 5 years now.
            calendar.add(Calendar.YEAR, InformationResource.EMBARGO_PERIOD_YEARS);
        }
        getResource().setDateMadePublic(calendar.getTime());
        getResource().setResourceLanguage(resourceLanguage);
        getResource().setMetadataLanguage(metadataLanguage);

        getResource().setProject(getProject());
        // handle dataset availability + date made public
        saveResourceProviderInformation();
    }
    
    public Integer getEmbargoPeriodInYears() {
        return InformationResource.EMBARGO_PERIOD_YEARS;
    }

    private boolean isResourcePublic() {
        if (StringUtils.isBlank(resourceAvailability)) {
            // FIXME: by default make things public?
            logger.debug("resource availability null/empty.  check if page params are set properly, defaulting to public");
            return true;
        }
        return "public".equalsIgnoreCase(resourceAvailability);
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
            potentialParents.addAll(getProjectService().findSparseTitleIdProjectListByPerson(submitter, isAdministrator()));
            if (!getProject().equals(Project.NULL) && !potentialParents.contains(getProject())) {
                potentialParents.add(getProject());
            }
            // tack the null project at the top of the sorted list
            Collections.sort(potentialParents);
            potentialParents.add(0, Project.NULL);
        }
        logger.debug("Returning all editable projects: {}", potentialParents);
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
        super.saveBasicResourceMetadata();
    }

    @Autowired
    public void setFileAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public Collection<String> getValidFileExtensions() {
        return Collections.emptyList();
    }

    public void setMetadataLanguage(LanguageEnum language) {
        this.metadataLanguage = language;
    }

    public LanguageEnum getMetadataLanguage() {
        return getPersistable().getMetadataLanguage();
    }

    public LanguageEnum getResourceLanguage() {
        return getPersistable().getResourceLanguage();
    }

    public void setResourceLanguage(LanguageEnum language) {
        this.resourceLanguage = language;
    }

    public List<LanguageEnum> getLanguages() {
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
                e.printStackTrace();
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
        return "text".equals(fileInputMethod);
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
    public R loadFromId(Long id) {
        super.loadFromId(id);
        if(getPersistable() != null) {
            setProject(getPersistable().getProject());
        }
        return getPersistable();
    }

    @Override
    public void validate() {
        super.validate();
        if (getPersistable().getDateCreated() == null) {
            logger.debug("Invalid date created for {}", getPersistable());
            String resourceTypeLabel = getPersistable().getResourceType().getLabel();
            addActionError("Please enter a valid creation year for " + resourceTypeLabel);
        }

    }
}
