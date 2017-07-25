package org.tdar.web.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
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
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceNote;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao.FindOptions;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.FileProxyService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.struts.data.AuthWrapper;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Service
public class ResourceSaveControllerService {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


    private static final String TXT = ".txt";

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private InformationResourceService informationResourceService;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    public ResourceCollectionService resourceCollectionService;


    @Autowired
    private GenericKeywordService genericKeywordService;
    @Autowired
    private GenericService genericService;

    @Autowired
    private transient FileProxyService fileProxyService;

    /**
     * Throw an extension if any of the provided proxies describe a file that is not contained in the list of accepted file types.
     * 
     * @param proxies
     * @throws TdarActionException
     */
    private void validateFileExtensions(List<FileProxy> proxies, Collection<String> validFileExtensions, TextProvider provider) throws TdarActionException {
        List<FileProxy> invalidFiles = new ArrayList<>();
        for (FileProxy proxy : proxies) {
            if (!validFileExtensions.contains(proxy.getExtension().toLowerCase()) && proxy.getAction() != FileAction.DELETE) {
                logger.info("Rejecting file:{} - extension not allowed.  Allowed types:{}", proxy.getExtension(), validFileExtensions);
                invalidFiles.add(proxy);
            }
        }
        if (!invalidFiles.isEmpty()) {
            throw new TdarValidationException(provider.getText("abstractResourceController.bad_extension"));
        }
    }
    
    protected InformationResourceFileVersion getLatestUploadedTextVersion(InformationResource persistable) {
        InformationResourceFileVersion version = null;
        Collection<InformationResourceFileVersion> versions = persistable.getLatestVersions(VersionType.UPLOADED_TEXT);
        if (!versions.isEmpty()) {
            version = persistable.getLatestVersions(VersionType.UPLOADED_TEXT).iterator().next();

        }
        return version;
    }


    public FileProxy processTextInput(TextProvider provider, String fileTextInput, InformationResource persistable) {
        InformationResourceFileVersion latestUploadedTextVersion = getLatestUploadedTextVersion(persistable);
        if ((latestUploadedTextVersion != null)
                && (latestUploadedTextVersion.getInformationResourceFile().getStatus() != FileStatus.PROCESSING_ERROR)) {
            if (Objects.equals(fileTextInput, getLatestUploadedTextVersionText(persistable))) {
                logger.info("incoming and current file input text is the same, skipping further actions");
                return null;
            } else {
                logger.info("processing updated text input for {}", persistable);
            }
        }

        try {
            // process the String uploaded via the fileTextInput box verbatim as the UPLOADED_TEXT version
            // 2013-22-04 AB: if our validation rules for Struts are working, this is not needed as the title already is checked way before this
            // if (StringUtils.isBlank(getPersistable().getTitle())) {
            // getLogger().error("Resource title was empty, client side validation failed for {}", getPersistable());
            // addActionError("Please enter a title for your " + getPersistable().getResourceType().getLabel());
            // return null;
            // }
            String uploadedTextFilename = persistable.getTitle() + TXT;

            FileProxy uploadedTextFileProxy = new FileProxy(uploadedTextFilename, FileProxy.createTempFileFromString(fileTextInput),
                    VersionType.UPLOADED_TEXT);

            // next, generate "uploaded" version of the file. In this case the VersionType.UPLOADED isn't entirely accurate
            // as this is UPLOADED_GENERATED, but it's the file that we want to process in later parts of our code.
            FileProxy primaryFileProxy = createUploadedFileProxy(provider, fileTextInput, persistable);
            primaryFileProxy.addVersion(uploadedTextFileProxy);
            setFileProxyAction(persistable, primaryFileProxy);
            return primaryFileProxy;
        } catch (IOException e) {
            logger.error("unable to create temp file or write " + fileTextInput + " to temp file", e);
            throw new TdarRecoverableRuntimeException(e);
        }
    };

    /**
     * @param persistable 
     * @throws IOException
     *             If there was an IO error
     */
    protected FileProxy createUploadedFileProxy(TextProvider provider, String fileTextInput, InformationResource persistable) throws IOException {
        if (persistable instanceof CodingSheet) {
            String filename = persistable.getTitle() + ".csv";
            // ensure csv conversion
            return new FileProxy(filename, FileProxy.createTempFileFromString(fileTextInput), VersionType.UPLOADED);
        }
        if (persistable instanceof Ontology) {
            String filename = persistable.getTitle() + ".owl";
            // convert text input to OWL XML text and use that as our archival version
            String owlXml = ontologyService.toOwlXml(persistable.getId(), fileTextInput);
            logger.info("owl xml is: \n{}", owlXml);
            return new FileProxy(filename, FileProxy.createTempFileFromString(owlXml), VersionType.UPLOADED);

        }
        throw new UnsupportedOperationException(provider.getText("abstractInformationResourceController.didnt_override", getClass().getSimpleName()));
    }




    public String getLatestUploadedTextVersionText(InformationResource persistable) {
        // in order for this to work we need to be generating text versions
        // of these files for both text input and file uploads
        String versionText = "";
        InformationResourceFileVersion version = getLatestUploadedTextVersion(persistable);
        if (version != null) {
            try {
                versionText = FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, version));
            } catch (IOException e) {
                logger.debug("an error occurred when trying to load the text version of a file", e);
            }
        }
        return versionText;
    }
    /**
     * Returns a List<FileProxy> representing the final set of fully initialized FileProxy objects
     * to be processed by the service layer.
     * 
     * FIXME: conditional logic could use some additional refactoring.
     * 
     * @return a List<FileProxy> representing the final set of fully initialized FileProxy objects
     */
    @Transactional(readOnly=false)
    public List<FileProxy> getFileProxiesToProcess(AuthWrapper<InformationResource> auth, TextProvider provider,Long ticketId, boolean multipleFileUploadEnabled, List<FileProxy> fileProxies, FileProxy textInputFileProxy,
            List<String> filenames, List<File> files) {
        List<FileProxy> fileProxiesToProcess = new ArrayList<>();

        // 1. text input for CodingSheet or Ontology (everything in a String, needs preprocessing to convert to a FileProxy)
        if (textInputFileProxy != null) {
            fileProxiesToProcess.add(textInputFileProxy);
        }
        // 2. async uploads for Image or Document or ...
        else if (multipleFileUploadEnabled) {
            fileProxiesToProcess =  fileProxyService.reconcilePersonalFilestoreFilesAndFileProxies(fileProxies, ticketId);

        } else
        // 3. single file upload (dataset|coding sheet|ontology)
        // there could be an incoming file payload, or just a metadata change.
        {
            fileProxiesToProcess = handleSingleFileUpload(fileProxiesToProcess, auth.getItem(), filenames, files, fileProxies);
        }

        return fileProxiesToProcess;
    }


    /**
     * One-size-fits-all method for handling uploaded InformationResource files.
     * 
     * Handles text input files for coding sheets and ontologies,
     * async uploads, and single-file dataset uploads.
     * @return 
     * 
     * @throws TdarActionException
     * @throws IOException 
     */
    @Transactional(readOnly=false)
    public ErrorTransferObject handleUploadedFiles(AuthWrapper<InformationResource> auth, TextProvider provider,Collection<String> validFileNames, Long ticketId, List<FileProxy> proxies) throws TdarActionException, IOException {
        boolean hasProxyChanges = false; // need to return
        // need to call getFileProxiesToProcess() before
            logger.debug("handling uploaded files for {}", auth.getItem());
            validateFileExtensions(proxies, validFileNames, provider);
            logger.debug("Final proxy set: {}", proxies);

            for (FileProxy proxy : proxies) {
                if (proxy != null && proxy.getAction() != FileAction.NONE) {
                    hasProxyChanges = true;
                }
            }

            if (!hasProxyChanges) {
                return null;
            }
            
        if (hasProxyChanges
                && !authorizationService.canDo(auth.getAuthenticatedUser(), auth.getItem(), InternalTdarRights.EDIT_ANY_RESOURCE,
                        GeneralPermissions.MODIFY_RECORD)) {
            throw new TdarActionException(StatusCode.FORBIDDEN, "You do not have permissions to upload or modify files");
        }
        // abstractInformationResourceController.didnt_override=%s didn't override properly
        // abstractInformationResourceController.didnt_override=%s didn't override properly

            ErrorTransferObject errors = informationResourceService.importFileProxiesAndProcessThroughWorkflow(auth.getItem(), auth.getAuthenticatedUser(),
                    ticketId, proxies);
        genericService.saveOrUpdate(auth.getItem());
        logger.trace("done processing upload files");
        return errors;
    }
    
    

    protected List<FileProxy> handleSingleFileUpload(List<FileProxy> toProcess, InformationResource persistable, List<String> uploadedFilesFileNames, List<File> uploadedFiles, List<FileProxy> fileProxies) {
        /*
         * FIXME: in Jar, hopefully, this goes away
         */

        FileProxy singleFileProxy = CollectionUtils.isEmpty(fileProxies) ? new FileProxy() : fileProxies.get(0);
        if (CollectionUtils.isEmpty(uploadedFiles)) {
            // check for metadata change iff this resource has an existing file.
            InformationResourceFile file = persistable.getFirstInformationResourceFile();
            if (file != null && singleFileProxy.isDifferentFromFile(file)) {
                singleFileProxy.setAction(FileAction.MODIFY_METADATA);
                singleFileProxy.setFileId(file.getId());
                toProcess.add(singleFileProxy);
            }
        } else {
            // process a new uploaded file (either ADD or REPLACE)
            setFileProxyAction(persistable, singleFileProxy);
            singleFileProxy.setFilename(uploadedFilesFileNames.get(0));
            singleFileProxy.setFile(uploadedFiles.get(0));
            toProcess.add(singleFileProxy);
        }
        return toProcess;
    }
    

    protected void setFileProxyAction(InformationResource persistable, FileProxy proxy) {
        if (persistable.hasFiles()) {
            logger.debug("Replacing existing files {} for {}", persistable.getInformationResourceFiles(), persistable);
            proxy.setAction(FileAction.REPLACE);
            proxy.setFileId(persistable.getFirstInformationResourceFile().getId());
            logger.debug("set primary file proxy irf id to {}", proxy.getFileId());
        } else {
            proxy.setAction(FileAction.ADD);
        }
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

    public void save(AuthWrapper<Resource> authWrapper, ResourceControllerProxy rcp) {
        
        if (rcp.shouldSaveResource()) {
            genericService.saveOrUpdate(authWrapper.getItem());
        }

        if (PersistableUtils.isNotNullOrTransient(rcp.getSubmitter())) {
            TdarUser uploader = genericService.find(TdarUser.class, rcp.getSubmitter().getId());
            authWrapper.getItem().setSubmitter(uploader);
        }

        saveKeywords(authWrapper, rcp);
        saveTemporalContext(authWrapper,rcp);
        saveSpatialContext(authWrapper,rcp);
        saveCitations(authWrapper,rcp);

        prepSequence(rcp.getResourceNotes());
        
        resourceService.saveHasResources(authWrapper.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getResourceNotes(),
                authWrapper.getItem().getResourceNotes(), ResourceNote.class);

        resourceService.saveResourceCreatorsFromProxies(rcp.getResourceCreatorProxies(), authWrapper.getItem(), rcp.shouldSaveResource());

        resolveAnnotations(authWrapper, rcp);
        List<SharedCollection> retainedSharedCollections = new ArrayList<>();
        List<ListCollection> retainedListCollections = new ArrayList<>();
        List<SharedCollection> shares = rcp.getShares();
        List<ListCollection> resourceCollections = rcp.getResourceCollections();

        loadEffectiveResourceCollectionsForSave(authWrapper, retainedSharedCollections, retainedListCollections);
        logger.debug("retained collections:{}", retainedSharedCollections);
        logger.debug("retained list collections:{}", retainedListCollections);
        shares.addAll(retainedSharedCollections);
        resourceCollections.addAll(retainedListCollections);
        
        if (authorizationService.canDo(authWrapper.getAuthenticatedUser(), authWrapper.getItem(), InternalTdarRights.EDIT_ANY_RESOURCE,
                GeneralPermissions.MODIFY_RECORD)) {
            resourceCollectionService.saveResourceCollections(authWrapper.getItem(), shares, authWrapper.getItem().getSharedCollections(),
                    authWrapper.getAuthenticatedUser(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, SharedCollection.class);

            if (!authorizationService.canEdit(authWrapper.getAuthenticatedUser(), authWrapper.getItem())) {
//                addActionError("abstractResourceController.cannot_remove_collection");
                logger.error("user is trying to remove themselves from the collection that granted them rights");
//                addActionMessage("abstractResourceController.collection_rights_remove");
            }
        } else {
            logger.debug("ignoring changes to rights as user doesn't have sufficient permissions");
        }
        resourceCollectionService.saveResourceCollections(authWrapper.getItem(), resourceCollections, authWrapper.getItem().getUnmanagedResourceCollections(),
                authWrapper.getAuthenticatedUser(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, ListCollection.class);


        
        
    }
    
    
    private void loadEffectiveResourceCollectionsForSave(AuthWrapper<Resource> auth, List<SharedCollection> retainedSharedCollections, List<ListCollection> retainedListCollections) {
        logger.debug("loadEffective...");
        for (SharedCollection rc : auth.getItem().getSharedCollections()) {
            if (!authorizationService.canViewCollection(auth.getAuthenticatedUser(),rc)) {
                retainedSharedCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        for (ListCollection rc : auth.getItem().getUnmanagedResourceCollections()) {
            if (!authorizationService.canViewCollection(auth.getAuthenticatedUser(),rc)) {
                retainedListCollections.add(rc);
                logger.debug("adding: {} to retained collections", rc);
            }
        }
        //effectiveResourceCollections.addAll(resourceCollectionService.getEffectiveResourceCollectionsForResource(auth.getItem()));
    }


    private void saveTemporalContext(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        // calendar and radiocarbon dates are null for Ontologies
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getCoverageDates(),
                auth.getItem().getCoverageDates(), CoverageDate.class);
        PersistableUtils.reconcileSet(auth.getItem().getTemporalKeywords(),
                genericKeywordService.findOrCreateByLabels(TemporalKeyword.class, rcp.getTemporalKeywords()));
    }


    // return a persisted annotation based on incoming pojo
    private void resolveAnnotations(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        
        
        List<ResourceAnnotation> toAdd = new ArrayList<>();
        for (ResourceAnnotation incomingAnnotation : rcp.getIncomingAnnotations()) {
            if (incomingAnnotation == null) {
                continue;
            }
            ResourceAnnotationKey incomingKey = incomingAnnotation.getResourceAnnotationKey();
            ResourceAnnotationKey resolvedKey = genericService.findByExample(ResourceAnnotationKey.class, incomingKey, FindOptions.FIND_FIRST_OR_CREATE)
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
        rcp.getIncomingAnnotations().addAll(toAdd);
        
        resourceService.saveHasResources((Resource) auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getIncomingAnnotations(),
                auth.getItem().getResourceAnnotations(), ResourceAnnotation.class);

    }

    protected void saveSpatialContext(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        // it won't add a null or incomplete lat-long box.

        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getLatitudeLongitudeBoxes(),
                auth.getItem().getLatitudeLongitudeBoxes(), LatitudeLongitudeBox.class);

        PersistableUtils.reconcileSet(auth.getItem().getGeographicKeywords(),
                genericKeywordService.findOrCreateByLabels(GeographicKeyword.class, rcp.getGeographicKeywords()));

        resourceService.processManagedKeywords(auth.getItem(), auth.getItem().getLatitudeLongitudeBoxes());
    }

    protected void saveCitations(AuthWrapper<Resource> auth, ResourceControllerProxy rcp) {
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS,
                rcp.getRelatedComparativeCollections(),
                auth.getItem().getRelatedComparativeCollections(), RelatedComparativeCollection.class);
        resourceService.saveHasResources(auth.getItem(), rcp.shouldSaveResource(), ErrorHandling.VALIDATE_SKIP_ERRORS, rcp.getSourceCollections(),
                auth.getItem().getSourceCollections(), SourceCollection.class);

    }
    private void saveKeywords(AuthWrapper<Resource> authWrapper,ResourceControllerProxy rcp) {

            logger.debug("siteNameKeywords=" + rcp.getSiteNameKeywords());
            logger.debug("materialKeywords=" + rcp.getApprovedMaterialKeywordIds());
            logger.debug("otherKeywords=" + rcp.getOtherKeywords());
            logger.debug("investigationTypes=" + rcp.getInvestigationTypeIds());
            Resource res = authWrapper.getItem();

            cleanupKeywords(rcp.getUncontrolledCultureKeywords());
            cleanupKeywords(rcp.getUncontrolledMaterialKeywords());
            cleanupKeywords(rcp.getUncontrolledSiteTypeKeywords());
            cleanupKeywords(rcp.getSiteNameKeywords());
            cleanupKeywords(rcp.getOtherKeywords());
            cleanupKeywords(rcp.getTemporalKeywords());

            Set<CultureKeyword> culKeys = genericKeywordService.findOrCreateByLabels(CultureKeyword.class, rcp.getUncontrolledCultureKeywords());
            culKeys.addAll(genericService.findAll(CultureKeyword.class, rcp.getApprovedCultureKeywordIds()));
            Set<MaterialKeyword> matKeys = genericKeywordService.findOrCreateByLabels(MaterialKeyword.class, rcp.getUncontrolledMaterialKeywords());
            matKeys.addAll(genericService.findAll(MaterialKeyword.class, rcp.getApprovedMaterialKeywordIds()));

            Set<SiteTypeKeyword> siteTypeKeys = genericKeywordService.findOrCreateByLabels(SiteTypeKeyword.class, rcp.getUncontrolledSiteTypeKeywords());
            siteTypeKeys.addAll(genericService.findAll(SiteTypeKeyword.class, rcp.getApprovedSiteTypeKeywordIds()));

            PersistableUtils.reconcileSet(res.getSiteNameKeywords(), genericKeywordService.findOrCreateByLabels(SiteNameKeyword.class, rcp.getSiteNameKeywords()));
            PersistableUtils.reconcileSet(res.getOtherKeywords(), genericKeywordService.findOrCreateByLabels(OtherKeyword.class, rcp.getOtherKeywords()));
            PersistableUtils.reconcileSet(res.getInvestigationTypes(), genericService.findAll(InvestigationType.class, rcp.getInvestigationTypeIds()));

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

}
