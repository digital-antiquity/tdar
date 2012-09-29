package org.tdar.struts.action;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.citation.Citation;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
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
import org.tdar.core.bean.resource.Status;
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
    private List<Project> potentialParents;

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

    protected abstract void processUploadedFile() throws IOException;

    /**
     * FIXME: should consider doing FileProxy reconciliation here instead of in getFileProxiesToProcess.
     * check for any pending uploads
     */
    protected void handleAsyncUploads() {
        if (ticketId == null) {
            return;
        }
        PersonalFilestore filestore = filestoreService.getPersonalFilestore(getAuthenticatedUser());
        filestoreTicket = getGenericService().find(PersonalFilestoreTicket.class, ticketId);
        this.pendingFiles = filestore.retrieveAll(filestoreTicket);
        if (pendingFiles.size() > 0) {
            uploadedFiles = new ArrayList<File>();
            uploadedFilesFileNames = new ArrayList<String>();

            for (PersonalFilestoreFile pendingFile : pendingFiles) {
                uploadedFiles.add(pendingFile.getFile());
                uploadedFilesFileNames.add(pendingFile.getFile().getName());
                // TODO: do something w/ md5
            }
        }
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
    }

    /**
     * This should be overridden when InformationResource content is entered from a text area in the web form.
     * Currently the only InformationResourceS that employ this method of content/data entry are CodingSheetS and OntologyS.
     * 
     * Returns a FileProxy representing the content that was entered.
     */
    protected FileProxy processTextInput() {
        if (!isTextInput()) {
            return null;
        }
        if (resource.hasFiles() && StringUtils.isEmpty(fileTextInput)) {
            // this resource already has associated files and there's blank incoming input, ignore
            return null;
        }
        if (StringUtils.isEmpty(fileTextInput)) {
            addFieldError("fileTextInput", "Please enter your " + resource.getResourceType().getLabel() + " into the text area.");
            return null;
        }
        if (fileTextInput.equals(getLatestUploadedTextVersionText())) {
            logger.info("incoming and current file input text is the same, skipping further actions");
            return null;
        } else {
            logger.info("found new or changed data for {}", resource);
        }

        try {
            // first process the String uploaded via file the fileTextInput box verbatim as the UPLOADED_TEXT version
            if (StringUtils.isEmpty(resource.getTitle())) {
                logger.error("Resource title was empty, client side validation failed for {}", resource);
                addActionError("Please enter a title for your " + resource.getResourceType().getLabel());
                // FIXME: should we create the FileProxy anyways with a dummy name?
                return null;
            }
            String uploadedFileName = resource.getTitle() + getFileTextArchivalExtension();
            FileProxy uploadedTextFileProxy = new FileProxy(uploadedFileName,
                    new ByteArrayInputStream(fileTextInput.getBytes("UTF-8")),
                    VersionType.UPLOADED_TEXT);

            // next, generate "uploaded" version of the file. In this case the VersionType isn't quite accurate or at least
            // isn't capturing how we want to use it in calling code, e.g., a DatasetService or CodingSheetService wants the latest File
            // in a format that it know how to process.
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
        throw new UnsupportedOperationException(getClass() + " didn't override");
    }

    // FIXME: inline this method if not needed.
    protected String getFileTextArchivalExtension() {
        return ".txt";
    }

    @Action("upload")
    @Override
    public String execute() {
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
        loadCustomMetadata();
        return SUCCESS;
    }

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    protected String handleUploadedFiles() {
        getLogger().debug("handling uploaded files for {}", resource);
        // The two special case scenarios are the text input area and the async uploads.
        // first check if there was text input
        FileProxy textInputFileProxy = processTextInput();
        if (textInputFileProxy != null) {
            fileProxies.add(textInputFileProxy);
        }
        handleAsyncUploads();
        logger.debug("uploaded file filenames: {}", uploadedFilesFileNames);
        // both processTextInput and handleAsyncUploads modify uploadedFilesFileNames when
        // there are files to process. If it is empty, just return.
        if (CollectionUtils.isEmpty(uploadedFilesFileNames) && CollectionUtils.isEmpty(fileProxies)) {
            logger.debug("No filenames in uploadedFilesFileNames and no file proxies to process.  Returning.");
            return SUCCESS;

        }
        Collection<FileProxy> fileProxiesToProcess = getFileProxiesToProcess();
        // XXX: could also apply sequencing here via Persistable.Sequence.applySequence(fileProxySet) instead of setting the sequence number
        // manually
        logger.debug("Final proxy set: {}", fileProxiesToProcess);
        for (FileProxy fileProxy : fileProxiesToProcess) {
            try {
                getInformationResourceService().processFileProxy(resource, fileProxy);
            } catch (IOException exception) {
                addActionErrorWithException("Unable to process file " + fileProxy.getFilename(), exception);
            }
        }
        try {
            processUploadedFile();
        } catch (IOException e) {
            addActionErrorWithException("We were unable to process the uploaded content.", e);
        }
        getInformationResourceService().saveOrUpdate(resource);

        getLogger().trace("done processing upload files");
        return SUCCESS;
    }

    protected void loadResourceProviderInformation() {
        // load resource provider institution and publishers
        setResourceProviderInstitution(getResource().getResourceProviderInstitution());

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

    @Override
    protected void delete(R informationResource) {
        resource.setStatus(Status.DELETED);
        getInformationResourceService().update(resource);
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

    public void setResourceProviderInstitution(Institution resourceProviderInstitution) {
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
        // load citations
        setSourceCollections(toStringList(getResource().getSourceCollections()));
        setRelatedComparativeCitations(toStringList(getResource().getRelatedComparativeCollections()));
        loadResourceProviderInformation();
        setAllowedToViewConfidentialFiles(getEntityService().canViewConfidentialInformation(getAuthenticatedUser(), resource));
        initializeFileProxies();
    }

    private void initializeFileProxies() {
        fileProxies = new ArrayList<FileProxy>();
        for (InformationResourceFile informationResourceFile : resource.getInformationResourceFiles()) {
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
            calendar.add(Calendar.YEAR, 5);
        }
        getResource().setDateMadePublic(calendar.getTime());
        getResource().setResourceLanguage(resourceLanguage);
        getResource().setMetadataLanguage(metadataLanguage);

        getResource().setProject(getProject());
        // handle dataset availability + date made public
        saveResourceProviderInformation();
        getInformationResourceService().delete(resource.getSourceCollections());
        resource.setSourceCollections(toCitationSet(getSourceCollections(), SourceCollection.class));

        getInformationResourceService().delete(resource.getRelatedComparativeCollections());
        resource.setRelatedComparativeCollections(toCitationSet(getRelatedComparativeCitations(), RelatedComparativeCollection.class));
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
    public List<Project> getPotentialParents() {
        logger.info("get potential parents");
        if (potentialParents == null) {
            Person submitter = getAuthenticatedUser();
            Set<Project> parents = new HashSet<Project>();
            parents.addAll(getProjectService().findBySubmitter(submitter));
            parents.addAll(getProjectService().findSparseTitleIdProjectListByPerson(submitter));
            if (!getProject().equals(Project.NULL)) {
                parents.add(getProject());
            }
            // tack the null project at the top of the sorted list
            List<Project> sortedParents = new ArrayList<Project>(parents);
            Collections.sort(sortedParents);
            potentialParents = new ArrayList<Project>();
            potentialParents.add(Project.NULL);
            potentialParents.addAll(sortedParents);
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
        return resource.getMetadataLanguage();
    }

    public LanguageEnum getResourceLanguage() {
        return resource.getResourceLanguage();
    }

    public void setResourceLanguage(LanguageEnum language) {
        this.resourceLanguage = language;
    }

    public List<LanguageEnum> getLanguages() {
        if (languages == null)
            languages = getInformationResourceService().findAllLanguages();
        return languages;
    }

    protected <C extends Citation> Set<C> toCitationSet(Collection<String> citationStrings, Class<C> citationClass) {
        if (citationStrings == null) {
            return Collections.emptySet();
        }
        HashSet<C> citationSet = new HashSet<C>();
        for (String citationString : citationStrings) {
            // ignore empty citations
            if (StringUtils.isBlank(citationString)) {
                continue;
            }
            try {
                C citation = citationClass.newInstance();
                citation.setText(citationString);
                citation.setResource(resource);
                citationSet.add(citation);
            } catch (Exception exception) {
                logger.error("Couldn't instantiate citation of class: " + citationClass + " citations: " + citationStrings, exception);
            }
        }
        return citationSet;
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
        for (InformationResourceFileVersion version : resource.getLatestVersions(VersionType.UPLOADED_TEXT)) {
            try {
                return FileUtils.readFileToString(version.getFile());
            } catch (IOException e) {
                e.printStackTrace();
                logger.debug("an error occurred when trying to load the text version of a file", e);
            }
        }
        return "";
    }

    private void setFileProxyAction(FileProxy proxy) {
        if (resource.hasFiles()) {
            logger.debug("Replacing existing files {} for {}", resource.getInformationResourceFiles(), resource);
            proxy.setAction(FileAction.REPLACE);
            proxy.setFileId(resource.getFirstInformationResourceFile().getId());
            logger.debug("set primary file proxy irf id to {}", proxy.getFileId());
        } else {
            proxy.setAction(FileAction.ADD);
        }
    }

    /**
     * Returns a Map of FileProxyS from the original incoming set of FileProxies and uploadedFilesFileNames.
     * 
     * FIXME: change to return the values() as a LinkedHashSet?
     * 
     * @return
     */
    protected Collection<FileProxy> getFileProxiesToProcess() {
        logger.debug("Generating file proxy map from existing proxies {} and uploaded filenames {}", fileProxies, uploadedFilesFileNames);
        LinkedHashMap<String, FileProxy> proxiesToProcess = new LinkedHashMap<String, FileProxy>();
        for (FileProxy fileProxy : fileProxies) {
            if (fileProxy == null) {
                // was pending & deleted, skip
                logger.debug("Skipping null file proxy resulting from a deleted pending file.");
                continue;
            }
            if (StringUtils.isEmpty(fileProxy.getFilename())) {
                logger.warn("file proxy with no filename {}", fileProxy);
            }
            // if (fileProxy.shouldProcess()) {
            FileProxy existingProxy = proxiesToProcess.get(fileProxy.getFilename());
            // only add the FileProxy to the proxies to process map if the one we're looking at is a replace.
            // this is a hack to work around the
            if (existingProxy == null || fileProxy.getAction() == FileAction.REPLACE) {
                fileProxy.setSequenceNumber(proxiesToProcess.size() + 1);
                logger.debug("Replacing existing proxy {} with {}", existingProxy, fileProxy);
                proxiesToProcess.put(fileProxy.getFilename(), fileProxy);
            }
            // }
        }
        // FIXME: clarify logic in these subsequent sections
        boolean singleFileUpload = !isMultipleFileUploadEnabled();
        // deal with confidentiality metadata change if and only if this is
        // 1. a single file upload
        // 2. there are no replacement uploads incoming
        // 3. the resource has an existing file
        // 4. there are no other file proxies to process (generated via processTextInput)
        if (singleFileUpload && getUploadedFilesFileName().isEmpty() && resource.hasFiles() && proxiesToProcess.isEmpty()) {
            LinkedHashSet<FileProxy> proxySet = new LinkedHashSet<FileProxy>();
            InformationResourceFile file = resource.getFirstInformationResourceFile();
            FileProxy proxy = new FileProxy(file, isConfidential(), FileAction.MODIFY_METADATA);
            // FIXME: could push confidential check into FileProxy constructor to compare incoming confidentiality with existing
            // file confidentiality and set FileAction appropriately.
            if (isConfidential() != file.isConfidential()) {
                proxySet.add(proxy);
            }
            return proxySet;
        }
        for (int index = 0; index < getUploadedFilesFileName().size(); index++) {
            String uploadedFilename = uploadedFilesFileNames.get(index);
            FileProxy proxy = proxiesToProcess.get(uploadedFilename);
            logger.debug("populating file proxies via uploadedFilesFileName: {}, associated FileProxy {}", uploadedFilename, proxy);
            if (singleFileUpload) {
                // Generated via coding sheet, dataset, or ontology.
                // In the case of a single file upload, always generate a new proxy for the incoming filename since
                // async uploading is not enabled and uploadedFilesFileName is set by Struts, not via handleAsyncUpload.
                // FIXME: get rid of this corner case if possible.
                if (proxy != null) {
                    logger.info("FileProxy found even though we are processing single-file upload. Assuming replacement had same name and reassigning anyway. Filename:{}\t proxy:{}", 
                            uploadedFilename, proxy);
                }
                proxy = new FileProxy();
                setFileProxyAction(proxy);
                proxy.setFilename(uploadedFilename);
                proxy.setConfidential(isConfidential());
                proxy.setSequenceNumber(1);
                proxiesToProcess.put(uploadedFilename, proxy);
            } else if (proxy == null) {
                // logger.debug("null proxy for {}, should have been a deleted pending file.", uploadedFilename);
                logger.debug("something bad happened in the JS side of things, there should always be a FileProxy resulting from the upload callback {}", 
                                                uploadedFilename);
                proxy = new FileProxy(uploadedFilename, VersionType.UPLOADED, false);
                proxiesToProcess.put(uploadedFilename, proxy);
            }
            // assign the appropriate InputStream to the FileProxy.
            // FIXME: null check shouldn't be necessary since the only FileProxyS
            // with non-null InputStreams are added directly via processTextInput.
            logger.info("Assuming no input stream on proxy {}, setting to uploadedFiles FileInputStream", proxy);
            try {
                proxy.setInputStream(new FileInputStream(uploadedFiles.get(index)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                addActionErrorWithException("Unable to get an InputStream to " + uploadedFilename, e);
                continue;
            }

        }
        return proxiesToProcess.values();
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

}
