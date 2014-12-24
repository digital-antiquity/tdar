package org.tdar.core.service.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.FileAction;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.ResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.workflow.WorkflowResult;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.Filestore.BaseFilestore;
import org.tdar.filestore.Filestore.ObjectType;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.utils.Pair;
import org.tdar.utils.PersistableUtils;

/**
 * $Id: AbstractInformationResourceService.java 1466 2011-01-18 20:32:38Z abrin$
 * 
 * Provides basic InformationResource services including file management (via FileProxyS).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

public abstract class AbstractInformationResourceService<T extends InformationResource, R extends ResourceDao<T>> extends ServiceInterface.TypedDaoBase<T, R> {

    // FIXME: this should be injected
    private static final TdarConfiguration config = TdarConfiguration.getInstance();

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;
    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    private FileAnalyzer analyzer;

    // private MessageService messageService;

    // Martin: according to the Spring documentation all these private methods marked as @Transactional totally ignored.
    // They must be public to have the annotation recognised?
    // See: http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/transaction.html#transaction-declarative-annotations
    /*
     * Adds a @link InformationResourceFile to a resource given a file proxy. This method handles the full process of creating the metadata and
     * InformationResourceFileVersions to a file when processing it
     */
    private void addInformationResourceFile(InformationResource resource, FileProxy proxy) throws IOException {
        // always set the download/version info and persist the relationships between the InformationResource and its IRFile.
        InformationResourceFile irFile = proxy.getInformationResourceFile();
        incrementVersionNumber(irFile);
        // genericDao.saveOrUpdate(resource);
        getDao().saveOrUpdate(resource);
        irFile.setInformationResource(resource);
        proxy.setInformationResourceFileVersion(createVersionMetadataAndStore(proxy));
        setInformationResourceFileMetadata(proxy);
        for (FileProxy additionalVersion : proxy.getAdditionalVersions()) {
            getLogger().debug("Creating new version {}", additionalVersion);
            additionalVersion.setInformationResourceFile(proxy.getInformationResourceFile());
            createVersionMetadataAndStore(additionalVersion);
        }
        getDao().saveOrUpdate(irFile);
        resource.add(irFile);
        getLogger().debug("all versions for {}", irFile);
    }

    /*
     * Finds an @link InformationResourceFile based on the file id information associated with the @link FileProxy
     */
    private InformationResourceFile findInformationResourceFile(FileProxy proxy) {
        InformationResourceFile irFile = getDao().find(InformationResourceFile.class, proxy.getFileId());
        if (irFile == null) {
            getLogger().error("{} had no findable InformationResourceFile.id set on it", proxy);
            // FIXME: throw an exception?
        }
        return irFile;
    }

    /*
     * Given a @link Resource and list of @link FileProxy objects, process the files and report any errors to the @link ActionMessageErrorSupport listener,
     * which is likely a controller.
     */
    @Transactional
    public ErrorTransferObject importFileProxiesAndProcessThroughWorkflow(T resource, TdarUser user, Long ticketId,
            List<FileProxy> fileProxiesToProcess) throws IOException {
        if (CollectionUtils.isEmpty(fileProxiesToProcess)) {
            getLogger().debug("Nothing to process, returning.");
            return null;
        }

        // prepare the metadata
        List<FileProxy> cleanedProxies = processMetadataForFileProxies(resource, fileProxiesToProcess.toArray(new FileProxy[0]));
        Pair<List<InformationResourceFile>, List<InformationResourceFileVersion>> filesAndVersions = convertProxiesToFilesAndVersions(cleanedProxies);
        List<InformationResourceFileVersion> filesToProcess = filesAndVersions.getSecond();
        List<InformationResourceFile> irFiles = filesAndVersions.getFirst();

        // make sure we're only doing this if we have files to process
        if (irFiles.size() > 0) {
            addExistingCompositeFilesForProcessing(resource, filesToProcess, irFiles);
        }

        processFiles(filesToProcess, resource.getResourceType().isCompositeFilesEnabled());

        /*
         * FIXME: When we move to an asynchronous model, this section and below will need to be moved into their own dedicated method
         */
        WorkflowResult workflowResult = new WorkflowResult(fileProxiesToProcess);
        ErrorTransferObject errorsAndMessages = workflowResult.getActionErrorsAndMessages();

        // If successful and no errors:
        // purge the filestore
        // mark the uploaded files as "read only"
        if (workflowResult.isSuccess()) {
            List<FileStoreFileProxy> proxies = new ArrayList<>();
            for (InformationResourceFileVersion file : filesToProcess) {
                proxies.add(file);
            }
            config.getFilestore().markReadOnly(ObjectType.RESOURCE, proxies);
        }
        if (ticketId != null) {
            PersonalFilestore personalFilestore = personalFilestoreService.getPersonalFilestore(user);
            personalFilestore.purge(getDao().find(PersonalFilestoreTicket.class, ticketId));
        }
        return errorsAndMessages;
    }

    private Pair<List<InformationResourceFile>, List<InformationResourceFileVersion>> convertProxiesToFilesAndVersions(List<FileProxy> cleanedProxies) {
        List<InformationResourceFile> irFiles = new ArrayList<>();
        List<InformationResourceFileVersion> filesToProcess = new ArrayList<>();
        /*
         * For each file Proxy, if it needs to be run through a workflow (eg. it's not just a MODIFY_METADATA call), then do it, otherwise, skip it
         */
        for (FileProxy proxy : cleanedProxies) {
            if (!proxy.getAction().requiresWorkflowProcessing()) {
                continue;
            }
            logger.debug("PROCESSING: {}", proxy);
            InformationResourceFile irFile = proxy.getInformationResourceFile();
            irFiles.add(irFile);
            InformationResourceFileVersion version = proxy.getInformationResourceFileVersion();
            getLogger().trace("version: {} proxy: {} ", version, proxy);
            getDao().saveOrUpdate(irFile);
            switch (version.getFileVersionType()) {
                case UPLOADED:
                case UPLOADED_ARCHIVAL:
                    irFile.setInformationResourceFileType(analyzer.analyzeFile(version));
                    filesToProcess.add(version);
                    break;
                default:
                    getLogger().debug("Not setting file type on irFile {} for VersionType {}", irFile, proxy.getVersionType());
            }
        }
        return Pair.create(irFiles, filesToProcess);
    }

    /*
     * When processing informationResourceFileVersions, make sure that we have ALL of the files available
     * This may mean trolling through the active files on the resource and adding them to the proxy
     */
    private void addExistingCompositeFilesForProcessing(T resource, List<InformationResourceFileVersion> filesToProcess, List<InformationResourceFile> irFiles)
            throws FileNotFoundException {
        if (!resource.getResourceType().isCompositeFilesEnabled()) {
            return;
        }

        for (InformationResourceFile file : resource.getActiveInformationResourceFiles()) {
            if (!irFiles.contains(file) && !file.isDeleted()) {
                InformationResourceFileVersion latestUploadedVersion = file.getLatestUploadedVersion();
                latestUploadedVersion.setTransientFile(config.getFilestore().retrieveFile(ObjectType.RESOURCE, latestUploadedVersion));
                filesToProcess.add(latestUploadedVersion);
            }
        }
    }

    /*
     * Process the files based on whether the @link ResourceType is a composite (like a @link Dataset where all of the files are necessary) or not where each
     * file is processed separately
     */
    private void processFiles(List<InformationResourceFileVersion> filesToProcess, boolean compositeFilesEnabled) throws IOException {
        if (CollectionUtils.isEmpty(filesToProcess)) {
            return;
        }

        if (compositeFilesEnabled) {
            analyzer.processFile(filesToProcess.toArray(new InformationResourceFileVersion[0]));
        } else {
            for (InformationResourceFileVersion version : filesToProcess) {
                if ((version.getTransientFile() == null) || (!version.getTransientFile().exists())) {
                    // If we are re-processing, the transient file might not exist.
                    version.setTransientFile(config.getFilestore().retrieveFile(ObjectType.RESOURCE, version));
                }
                analyzer.processFile(version);
            }
        }
    }

    /*
     * Unmaps all data-tables for a given @link InformationResourceFile. Handles special cases where you have TAB, CSV, or Text files where the table name is
     * not specified by the file itself
     */
    @Transactional(readOnly = true)
    private void unmapDataTablesForFile(Dataset dataset, InformationResourceFile irFile) {
        String fileName = irFile.getFilename();
        switch (FilenameUtils.getExtension(fileName).toLowerCase()) {
            case "tab":
            case "csv":
            case "txt":
                String name = FilenameUtils.getBaseName(fileName);
                name = datasetDao.normalizeTableName(name);
                DataTable dt = dataset.getDataTableByGenericName(name);
                getLogger().info("removing {}", dt);
                cleanupUnusedTablesAndColumns(dataset, Arrays.asList(dt), null);
                // dataset.getDataTableByGenericName(name)
                break;
            default:
                cleanupUnusedTablesAndColumns(dataset, dataset.getDataTables(), null);
        }
    }

    /*
     * For each proxy, associate it with an @link InformationResourceFile or create one if needed, then execute the appropriate action based on the @link
     * FileProxy action
     */
    @Transactional
    public List<FileProxy> processMetadataForFileProxies(InformationResource informationResource, FileProxy... incomingProxies) throws IOException {
        List<FileProxy> proxies = new ArrayList<FileProxy>(Arrays.asList(incomingProxies));
        proxies = validateFileProxies(proxies, informationResource);
        for (FileProxy proxy : proxies) {
            getLogger().debug("applying {} to {}", proxy, informationResource);

            switch (proxy.getAction()) {
                case MODIFY_METADATA:
                    // set sequence number and confidentiality
                    setInformationResourceFileMetadata(proxy);
                    getDao().update(proxy.getInformationResourceFile());
                    break;
                case REPLACE:
                    // explicit fall through to ADD after loading the existing irFile to be replaced.
                case ADD:
                    addInformationResourceFile(informationResource, proxy);
                    break;
                case ADD_DERIVATIVE:
                    createVersionMetadataAndStore(proxy);
                    break;
                case DELETE:
                    proxy.getInformationResourceFile().setDeleted(true);
                    getDao().update(proxy.getInformationResourceFile());
                    if (informationResource instanceof Dataset) {
                        unmapDataTablesForFile((Dataset) informationResource, proxy.getInformationResourceFile());
                    }
                    break;
                case NONE:
                    getLogger().debug("Taking no action on {} with proxy {}", informationResource, proxy);
                    break;
                default:
                    break;
            }
        }
        return proxies;
    }

    private List<FileProxy> validateFileProxies(List<FileProxy> proxies, InformationResource informationResource) {
        List<FileProxy> serverCreated = new ArrayList<>();
        List<FileProxy> rollbackIssues = new ArrayList<>();

        for (FileProxy proxy : proxies) {
            getLogger().debug("applying {} to {}", proxy, informationResource);
            if (proxy.isCreatedByServer()) {
                serverCreated.add(proxy);
            }

            // will be reassigned in a REPLACE or ADD_DERIVATIVE
            InformationResourceFile irFile = new InformationResourceFile();
            if (proxy.getAction().requiresExistingIrFile()) {
                irFile = findInformationResourceFile(proxy);

                if (irFile == null) {
                    // handling error case user is on the input page (rolled-back transaction for upload) we have a sequence # for an IRFile, but that file
                    // does not exist
                    // case: id == -1 -- cause, likely that there was a validation error caught in the WorkflowContext
                    // case: id > -1, but not in DB -- cause, likely that the transaction failed in the workflow context (access database died in tDAR database
                    // creation)
                    if (PersistableUtils.isNotNullOrTransient(proxy.getFileId())) {
                        logger.debug("resetting: {} {}", proxy, proxy.getAction());
                        rollbackIssues.add(proxy);
                        if (proxy.getAction() == FileAction.REPLACE) {
                            irFile = new InformationResourceFile();
                            proxy.setAction(FileAction.ADD);
                        }
                        if (proxy.getAction() == FileAction.DELETE) {
                            proxy.setAction(FileAction.NONE);
                        }
                    } else {
                        throw new TdarRecoverableRuntimeException("abstractInformationResourceService.bad_proxy", Arrays.asList(proxy.getFilename(),
                                proxy.getAction(), proxy.getFileId()));
                    }
                }
            }

            if (proxy.getAction() == FileAction.REPLACE || proxy.getAction() == FileAction.ADD) {
                irFile.setFilename(proxy.getFilename());
            }

            proxy.setInformationResourceFile(irFile);
        }
        // if we have a 1:1 relationship between server created proxies and rollback issues (proxies w/ids that don't exist), remove the server-created files
        // as they're more than likely hanging out in the personal filestore and not being cleaned out
        logger.debug("rollbackIssues: {}", rollbackIssues);
        if (CollectionUtils.isNotEmpty(serverCreated) && Objects.equals(serverCreated.size(), rollbackIssues.size())) {
            logger.debug("removing: {}", serverCreated);
            proxies.removeAll(serverCreated);
            logger.debug("after: {}", proxies);
        }
        return proxies;
    }

    /*
     * Provides a method to clear all mappings for a @link Dataset. This is called when the @link Dataset is re-mapped on the DataTable have been removed and
     * not replaced.
     */
    @Transactional(readOnly = true)
    public void cleanupUnusedTablesAndColumns(Dataset dataset, Collection<DataTable> tablesToRemove, Collection<DataTableColumn> columnsToRemove) {
        getLogger().info("deleting unmerged tables: {}", tablesToRemove);
        ArrayList<DataTableColumn> columnsToUnmap = new ArrayList<DataTableColumn>();
        if (CollectionUtils.isNotEmpty(columnsToRemove)) {
            for (DataTableColumn column : columnsToRemove) {
                columnsToUnmap.add(column);
            }
        }
        Set<DataTableRelationship> relationshipsToRemove = new HashSet<>();
        for (DataTable table : tablesToRemove) {
            if ((table != null) && CollectionUtils.isNotEmpty(table.getDataTableColumns())) {
                columnsToUnmap.addAll(table.getDataTableColumns());
            }
            relationshipsToRemove.addAll(table.getRelationships());
        }

        // first unmap all columns from the removed tables
        datasetDao.unmapAllColumnsInProject(dataset.getProject().getId(), PersistableUtils.extractIds(columnsToUnmap));

        // remove affected relationships prior to deleting columns
        dataset.getRelationships().removeAll(relationshipsToRemove);
        getDao().delete(relationshipsToRemove);

        getDao().delete(columnsToRemove);
        dataset.getDataTables().removeAll(tablesToRemove);
    }

    /*
     * Copies all of the appropriate metadata from a @link FileProxy to an @link InformationResourceFile . This includes confidentiality settings, embargo
     * settings description, and date.
     */
    private void setInformationResourceFileMetadata(FileProxy fileProxy) {
        InformationResourceFile irFile = fileProxy.getInformationResourceFile();
        irFile.setRestriction(fileProxy.getRestriction());
        Integer sequenceNumber = fileProxy.getSequenceNumber();
        if (fileProxy.getRestriction().isEmbargoed()) {
            if (irFile.getDateMadePublic() == null) {
                DateTime currentDate = new DateTime();
                DateTime embargoDate = currentDate.plusDays(fileProxy.getRestriction().getEmbargoPeriod());
                irFile.setDateMadePublic(embargoDate.toDate());
            }
        } else {
            irFile.setDateMadePublic(null);
        }

        if (fileProxy.getAction().updatesMetadata()) {
            irFile.setDescription(fileProxy.getDescription());
            irFile.setFileCreatedDate(fileProxy.getFileCreatedDate());
        }

        if (sequenceNumber == null) {
            getLogger().warn("No sequence number set on file proxy {}, existing sequence number was {}", fileProxy, irFile.getSequenceNumber());
        }
        else {
            irFile.setSequenceNumber(sequenceNumber);
        }
    }

    /*
     * Utility method for incrementing the version number of an @link InformationResourceFile when it's replaced
     */
    private void incrementVersionNumber(InformationResourceFile irFile) {
        irFile.incrementVersionNumber();
        irFile.clearStatus();
        getLogger().info("incremented version number and reset download and status for irfile: {}", irFile, irFile.getLatestVersion());
    }

    /*
     * Given an @link InformationResource, find all of the latest versions and reprocess them.
     */
    @Transactional(readOnly = false)
    public ErrorTransferObject reprocessInformationResourceFiles(T ir) throws Exception {
        List<InformationResourceFileVersion> latestVersions = new ArrayList<>();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            if (irFile.isDeleted()) {
                continue;
            }
            InformationResourceFileVersion original = irFile.getLatestUploadedVersion();
            original.setTransientFile(config.getFilestore().retrieveFile(ObjectType.RESOURCE, original));
            latestVersions.add(original);
            Iterator<InformationResourceFileVersion> iterator = irFile.getInformationResourceFileVersions().iterator();
            while (iterator.hasNext()) {
                InformationResourceFileVersion version = iterator.next();
                if (!version.equals(original) && !version.isUploaded() && !version.isArchival()) {
                    iterator.remove();
                    informationResourceFileDao.deleteVersionImmediately(version);
                }
            }
        }
        processFiles(latestVersions, ir.getResourceType().isCompositeFilesEnabled());
        // this is a known case where we need to purge the session
        // getDao().synchronize();

        ErrorTransferObject eto = null;
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            final WorkflowContext workflowContext = irFile.getWorkflowContext();
            // may be null for "skipped" or composite file
            if ((workflowContext != null) && !workflowContext.isProcessedSuccessfully()) {
                WorkflowResult workflowResult = new WorkflowResult(workflowContext);
                eto = workflowResult.getActionErrorsAndMessages();
            }
        }
        return eto;
    }

    /*
     * Creates an @link InformationResourceFile and adds appropriate metadata and stores the file in the filestore.
     */
    @Transactional(readOnly = false)
    private InformationResourceFileVersion createVersionMetadataAndStore(FileProxy fileProxy) throws IOException {
        InformationResourceFile irFile = fileProxy.getInformationResourceFile();
        String filename = BaseFilestore.sanitizeFilename(fileProxy.getFilename());
        if ((fileProxy.getFile() == null) || !fileProxy.getFile().exists()) {
            throw new TdarRecoverableRuntimeException("fileprocessing.error.not_found", Arrays.asList(fileProxy.getFilename()));
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(fileProxy.getVersionType(), filename, irFile);
        if (irFile.isTransient()) {
            getDao().saveOrUpdate(irFile);
        }

        irFile.addFileVersion(version);
        config.getFilestore().store(ObjectType.RESOURCE, fileProxy.getFile(), version);
        version.setTransientFile(fileProxy.getFile());
        getDao().save(version);
        getDao().saveOrUpdate(irFile);
        return version;
    }

    /*
     * Returns all enabled Languages in tDAR. Masks Enum.values() as this may become a database value over time
     */
    public List<Language> findAllLanguages() {
        return Arrays.asList(Language.values());
    }

    /**
     * We autowire the setter to help with autowiring issues
     * 
     * @param analyzer
     *            the analyzer to set
     */
    @Autowired
    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public FileAnalyzer getAnalyzer() {
        return analyzer;
    }
}
