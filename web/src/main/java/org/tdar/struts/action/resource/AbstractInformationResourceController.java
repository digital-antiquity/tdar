package org.tdar.struts.action.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ResourceCreatorProxy;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.CategoryVariableService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.FileSaveWrapper;
import org.tdar.web.service.ResourceEditControllerService;
import org.tdar.web.service.ResourceSaveControllerService;
import org.tdar.web.service.ResourceViewControllerService;

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
    private transient ProjectService projectService;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    @Autowired
    private transient ObfuscationService obfuscationService;

    @Autowired
    private transient ResourceViewControllerService resourceViewControllerService;
    @Autowired
    private transient ResourceSaveControllerService resourceSaveControllerService;

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
    private String fileInputMethod;
    private String fileTextInput;
    protected FileSaveWrapper fsw = new FileSaveWrapper();

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

    public boolean isMultipleFileUploadEnabled() {
        return false;
    }

    private boolean hasFileProxyChanges = false;

    @Autowired
    private ResourceEditControllerService resourceEditControllerService;

    @Override
    protected String save(InformationResource document) throws TdarActionException {
        // save basic metadata
        getLogger().debug("save ir");
        saveBasicResourceMetadata();
        
        // We set the project here to avoid getProjectId() being indexed too early (see TDAR-2001 for more info)
        resolveProject();
        getResource().setProject(getProject());
        getLogger().debug("setting projectid: {}", getResource().getProject());

        // handle dataset availability + date made public
        getResource().setResourceLanguage(resourceLanguage);
        getResource().setMetadataLanguage(metadataLanguage);
        // handle dataset availability + date made public

        proxy.setResourceProviderInstitutionName(resourceProviderInstitutionName);
        proxy.setPublisherName(publisherName);
        proxy.setCopyrightHolder(copyrightHolderProxies);
        proxy.setValidFileExtensions(getValidFileExtensions());

        fsw.setBulkUpload(isBulkUpload());
        fsw.setFileProxies(getFileProxies());
        fsw.setFileTextInput(fileTextInput);
        fsw.setTextInput(isTextInput());
        fsw.setMultipleFileUploadEnabled(isMultipleFileUploadEnabled());
        fsw.setTicketId(getTicketId());
        fsw.setUploadedFilesFileName(getUploadedFilesFileName());
        fsw.setUploadedFiles(getUploadedFiles());
        AuthWrapper<InformationResource> authWrapper = new AuthWrapper<InformationResource>(getResource(), isAuthenticated(), getAuthenticatedUser(), isEditor());
        resourceSaveControllerService.setupFileProxiesForSave(proxy, authWrapper, fsw, this);
        setHasFileProxyChanges(fsw.isFileProxyChanges());
        super.save(document);
        return SUCCESS;

    }

    protected void loadResourceProviderInformation() {
        // load resource provider institution and publishers
        setResourceProviderInstitution(getResource().getResourceProviderInstitution());
        setPublisherName(getResource().getPublisherName());
        if (getTdarConfiguration().getCopyrightMandatory() && PersistableUtils.isNotNullOrTransient(getResource().getCopyrightHolder())) {
            copyrightHolderProxies = new ResourceCreatorProxy(getResource().getCopyrightHolder(), ResourceCreatorRole.COPYRIGHT_HOLDER);
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

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        setProject(getPersistable().getProject());
        setProjectId(getPersistable().getProjectId());
        super.loadCustomMetadata();
        loadInformationResourceProperties();
        loadResourceProviderInformation();
        resourceViewControllerService.setTransientViewableStatus(getResource(), getAuthenticatedUser());
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
            potentialParents = resourceEditControllerService.getPotentialParents(getPersistable(), getAuthenticatedUser(), getProject(), this);
        }
        if (getLogger().isTraceEnabled()) {
            getLogger().trace("Returning all editable projects: {}", potentialParents);
        }
        return potentialParents;
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

    /**
     * Verifies if the resource can be allowed to add additional files.
     * The user may not have permission or the billing account may be over limit.
     * 
     * @return boolean
     */
    public boolean isAbleToUploadFiles() {
        if (isAbleToUploadFiles == null) {
            isAbleToUploadFiles = resourceEditControllerService.isAbleToUploadFiles(getAuthenticatedUser(), getPersistable(), getActiveAccounts());
        }
        getLogger().debug("isAbleToUploadFiles: {} , getAccount:{}", isAbleToUploadFiles, getPersistable().getAccount());

        return isAbleToUploadFiles;
    }

    public boolean isHasFileProxyChanges() {
        return hasFileProxyChanges;
    }

    public void setHasFileProxyChanges(boolean hasFileProxyChanges) {
        this.hasFileProxyChanges = hasFileProxyChanges;
    }

    public String getFilesJson() {
        filesJson = resourceEditControllerService.loadFilesJson(getPersistable());
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



    public String getFileInputMethod() {
        return fileInputMethod;
    }

    private boolean isTextInput() {
        return FILE_INPUT_METHOD.equals(fileInputMethod);
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
}
