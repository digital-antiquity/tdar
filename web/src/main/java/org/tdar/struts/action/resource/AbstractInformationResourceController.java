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
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Document;
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
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.DoNotObfuscate;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.ExceptionWrapper;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;
import org.tdar.web.service.ResourceEditControllerService;
import org.tdar.web.service.ResourceSaveControllerService;
import org.tdar.web.service.ResourceViewControllerService;

import com.opensymphony.xwork2.TextProvider;

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
    private transient SerializationService serializationService;

    @Autowired
    private transient CategoryVariableService categoryVariableService;

    @Autowired
    private transient EntityService entityService;


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
        super.saveBasicResourceMetadata();
        saveInformationResourceProperties();
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

    protected void saveInformationResourceProperties() {
        // handle dataset availability + date made public
        getResource().setResourceLanguage(resourceLanguage);
        getResource().setMetadataLanguage(metadataLanguage);
        // handle dataset availability + date made public
        saveResourceProviderInformation();
        try {

            FileProxy processTextInput = null;
            
            AuthWrapper<InformationResource> auth = new AuthWrapper<InformationResource>(getResource(), isAuthenticated(), getAuthenticatedUser(), isEditor());
            
            if (!isMultipleFileUploadEnabled()) {
                if (!isTextInput()) {
                    return;
                }
                if (StringUtils.isBlank(getFileTextInput())) {
                    addActionError(this.getText("abstractSupportingInformationResourceController.please_enter"));
                    return;
                }
                processTextInput = resourceSaveControllerService.processTextInput(this, getFileTextInput(), getPersistable());

            }
            
            List<FileProxy> fileProxiesToProcess = resourceSaveControllerService.getFileProxiesToProcess(auth, this,getTicketId(), isMultipleFileUploadEnabled(), getFileProxies(), processTextInput, getUploadedFilesFileName(), getUploadedFiles());
            
            ErrorTransferObject eto = resourceSaveControllerService.handleUploadedFiles(auth, this , getValidFileExtensions(), getTicketId(), fileProxiesToProcess);
            processErrorObject(eto);
        } catch (Exception e) {
            addActionErrorWithException(getText("abstractResourceController.we_were_unable_to_process_the_uploaded_content"), e);
        }

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
            potentialParents = resourceEditControllerService.getPotentialParents(getPersistable(),getAuthenticatedUser(), getProject(), this);
        }
        if (getLogger().isTraceEnabled()) {
            getLogger().trace("Returning all editable projects: {}", potentialParents);
        }
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
     * @return boolean
     */
    public boolean isAbleToUploadFiles() {
        if (isAbleToUploadFiles == null) {
            isAbleToUploadFiles = authorizationService.canUploadFiles(getAuthenticatedUser(), getPersistable());
            getLogger().debug("isAbleToUploadFiles: {} , getAccount:{}", isAbleToUploadFiles, getPersistable().getAccount());
            if(PersistableUtils.isNotNullOrTransient(getPersistable()) && getPersistable().getAccount()!=null){
            	List<BillingAccount> _activeAccounts = getActiveAccounts();
            	getLogger().debug("_activeAccounts:{}", _activeAccounts);
            	//BillingAccount account = _activeAccounts.stream().filter(a -> ObjectUtils.equals(a,getPersistable().getAccount())).collect(Collectors.toList()).get(0);
            	if(!getPersistable().getAccount().isActive()){
            		isAbleToUploadFiles = false;
            	}
            }
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
