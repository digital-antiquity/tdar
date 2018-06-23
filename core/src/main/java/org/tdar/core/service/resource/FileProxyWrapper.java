package org.tdar.core.service.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.filestore.BaseFilestore;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.utils.PersistableUtils;

public class FileProxyWrapper {

    private List<InformationResourceFile> irFiles = new ArrayList<>();
    private List<InformationResourceFileVersion> filesToProcess = new ArrayList<>();

    private TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    private final DatasetDao datasetDao;
    private InformationResource informationResource;
    private final FileAnalyzer analyzer;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private List<FileProxy> cleanedProxies;

    public FileProxyWrapper(InformationResource resource, FileAnalyzer analyzer2, DatasetDao datasetDao, Collection<FileProxy> proxies) {
        this.informationResource = resource;
        this.analyzer = analyzer2;
        this.datasetDao = datasetDao;
        this.cleanedProxies = new ArrayList<>(proxies);
    }

    /*
     * For each proxy, associate it with an @link InformationResourceFile or create one if needed, then execute the appropriate action based on the @link
     * FileProxy action
     */
    public void processMetadataForFileProxies() throws IOException {
        validateFileProxies();
        for (FileProxy proxy : cleanedProxies) {
            logger.debug("applying {} to {}", proxy, informationResource);

            switch (proxy.getAction()) {
                case MODIFY_METADATA:
                    // set sequence number and confidentiality
                    setInformationResourceFileMetadata(proxy);
                    datasetDao.update(proxy.getInformationResourceFile());
                    break;
                case REPLACE:
                    // explicit fall through to ADD after loading the existing irFile to be replaced.
                case ADD:
                    addInformationResourceFile(proxy);
                    break;
                case ADD_DERIVATIVE:
                    createVersionMetadataAndStore(proxy);
                    break;
                case DELETE:
                    proxy.getInformationResourceFile().setDeleted(true);
                    datasetDao.update(proxy.getInformationResourceFile());
                    if (informationResource instanceof Dataset) {
                        unmapDataTablesForFile((Dataset) informationResource, proxy.getInformationResourceFile());
                    }
                    break;
                case NONE:
                    logger.debug("Taking no action on {} with proxy {}", informationResource, proxy);
                    break;
                default:
                    break;
            }
        }
        for (FileProxy proxy : cleanedProxies) {
            if (!proxy.getAction().requiresWorkflowProcessing()) {
                continue;
            }
            logger.debug("PROCESSING: {}", proxy);
            InformationResourceFile irFile = proxy.getInformationResourceFile();
            getIrFiles().add(irFile);
            InformationResourceFileVersion version = proxy.getInformationResourceFileVersion();
            logger.trace("version: {} proxy: {} ", version, proxy);
            switch (version.getFileVersionType()) {
                case UPLOADED:
                case UPLOADED_ARCHIVAL:
                    irFile.setInformationResourceFileType(analyzer.getFileTypeForExtension(version, informationResource.getResourceType()));
                    getFilesToProcess().add(version);
                    break;
                default:
                    logger.debug("Not setting file type on irFile {} for VersionType {}", irFile, proxy.getVersionType());
            }
            datasetDao.saveOrUpdate(irFile);
        }

        // make sure we're only doing this if we have files to process
        if (getIrFiles().size() > 0 && informationResource.getResourceType().isCompositeFilesEnabled()) {
            for (InformationResourceFile file : informationResource.getActiveInformationResourceFiles()) {
                if (!irFiles.contains(file) && !file.isDeleted()) {
                    InformationResourceFileVersion latestUploadedVersion = file.getLatestUploadedVersion();
                    latestUploadedVersion.setTransientFile(CONFIG.getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, latestUploadedVersion));
                    filesToProcess.add(latestUploadedVersion);
                }
            }
        }
    }

    /*
     * Adds a @link InformationResourceFile to a resource given a file proxy. This method handles the full process of creating the metadata and
     * InformationResourceFileVersions to a file when processing it
     */
    public void addInformationResourceFile(FileProxy proxy) throws IOException {
        // always set the download/version info and persist the relationships between the InformationResource and its IRFile.
        
        InformationResourceFile irFile = proxy.getInformationResourceFile();
        if (proxy.getAction() == FileAction.ADD && !informationResource.getResourceType().allowsMultipleFiles() && !informationResource.getResourceType().isCompositeFilesEnabled()) {
            if (CollectionUtils.isNotEmpty(informationResource.getInformationResourceFiles()) && informationResource.getInformationResourceFiles().size() > 1) {
                throw new TdarRecoverableRuntimeException("informationResourceFile.too.many.files");
            }
            InformationResourceFile firstInformationResourceFile = informationResource.getFirstInformationResourceFile();
            if (firstInformationResourceFile != null) {
                firstInformationResourceFile.setDeleted(true);
            }
        }
        incrementVersionNumber(irFile);
        
        // genericDao.saveOrUpdate(resource);

        
        
        irFile.setInformationResource(informationResource);
        proxy.setInformationResourceFileVersion(createVersionMetadataAndStore(proxy));
        setInformationResourceFileMetadata(proxy);
        for (FileProxy additionalVersion : proxy.getAdditionalVersions()) {
            logger.debug("Creating new version {}", additionalVersion);
            additionalVersion.setInformationResourceFile(proxy.getInformationResourceFile());
            createVersionMetadataAndStore(additionalVersion);
        }
        datasetDao.saveOrUpdate(irFile);
        informationResource.add(irFile);
        logger.debug("all versions for {}", irFile);
    }

    /*
     * Unmaps all data-tables for a given @link InformationResourceFile. Handles special cases where you have TAB, CSV, or Text files where the table name is
     * not specified by the file itself
     */
    private void unmapDataTablesForFile(Dataset dataset, InformationResourceFile irFile) {
        String fileName = irFile.getFilename();
        switch (FilenameUtils.getExtension(fileName).toLowerCase()) {
            case "tab":
            case "csv":
            case "txt":
                String name = FilenameUtils.getBaseName(fileName);
                name = datasetDao.normalizeTableName(name);
                DataTable dt = dataset.getDataTableByGenericName(name);
                logger.info("removing {}", dt);
                removeAllRelationships(dataset, Arrays.asList(dt));
                datasetDao.cleanupUnusedTablesAndColumns(dataset, Arrays.asList(dt), null);
                break;
            default:
                logger.debug(" deleting relationships");
                removeAllRelationships(dataset, dataset.getDataTables());
                logger.debug(" deleting all tables");
                datasetDao.cleanupUnusedTablesAndColumns(dataset, dataset.getDataTables(), null);
                logger.debug(" done");
        }
    }

    /*
     * Creates an @link InformationResourceFile and adds appropriate metadata and stores the file in the filestore.
     */
    public InformationResourceFileVersion createVersionMetadataAndStore(FileProxy proxy) throws IOException {
        InformationResourceFile irFile = proxy.getInformationResourceFile();
        String originalFilename = proxy.getFilename();
        String filename = BaseFilestore.sanitizeFilename(originalFilename);
        File file = proxy.getFile();
        if ((file == null) || !file.exists()) {
            throw new TdarRecoverableRuntimeException("fileprocessing.error.not_found", Arrays.asList(originalFilename));
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(proxy.getVersionType(), filename, irFile);
        if (irFile.isTransient()) {
            if (irFile.getInformationResource().isTransient()) {
                datasetDao.saveOrUpdate(irFile.getInformationResource());
            }

            datasetDao.saveOrUpdate(irFile);
        }

        irFile.addFileVersion(version);
        TdarConfiguration.getInstance().getFilestore().store(FilestoreObjectType.RESOURCE, file, version);
        version.setTransientFile(file);
        datasetDao.save(version);
        datasetDao.saveOrUpdate(irFile);
        return version;
    }

    private void removeAllRelationships(Dataset dataset, Collection<DataTable> tablesToRemove) {

        Set<DataTableRelationship> relationshipsToRemove = new HashSet<>();
        for (DataTable table : tablesToRemove) {
            relationshipsToRemove.addAll(table.getRelationships());
        }
        datasetDao.deleteRelationships(relationshipsToRemove);
        // // remove affected relationships prior to deleting columns
        // dataset.getRelationships().removeAll(relationshipsToRemove);
        // getDao().delete(relationshipsToRemove);
    }

    /*
     * Utility method for incrementing the version number of an @link InformationResourceFile when it's replaced
     */
    private void incrementVersionNumber(InformationResourceFile irFile) {
        irFile.incrementVersionNumber();
        irFile.clearStatus();
        logger.info("incremented version number and reset download and status for irfile: {}", irFile, irFile.getLatestVersion());
    }

    public List<InformationResourceFile> getIrFiles() {
        return irFiles;
    }

    public void setIrFiles(List<InformationResourceFile> irFiles) {
        this.irFiles = irFiles;
    }

    public List<InformationResourceFileVersion> getFilesToProcess() {
        return filesToProcess;
    }

    public void setFilesToProcess(List<InformationResourceFileVersion> filesToProcess) {
        this.filesToProcess = filesToProcess;
    }

    /*
     * Copies all of the appropriate metadata from a @link FileProxy to an @link InformationResourceFile . This includes confidentiality settings, embargo
     * settings description, and date.
     */
    public void setInformationResourceFileMetadata(FileProxy proxy) {
        InformationResourceFile irFile = proxy.getInformationResourceFile();
        Integer sequenceNumber = proxy.getSequenceNumber();
        DateTime currentDate = DateTime.now();

        if (proxy.getRestriction().isEmbargoed()) {
            // calculate initial expiry date if expiry date does not exist
            if (irFile.getDateMadePublic() == null) {
                DateTime embargoDate = currentDate.plusDays(proxy.getRestriction().getEmbargoPeriod());
                irFile.setDateMadePublic(embargoDate.toDate());

                // user may have changed embargo period, so recalculate expiry date
            } else {
                Period currentPeriod = Period.days(irFile.getRestriction().getEmbargoPeriod());
                Period newPeriod = Period.days(proxy.getRestriction().getEmbargoPeriod());
                DateTime currentExpiry = new DateTime(irFile.getDateMadePublic());
                DateTime newExpiry = currentExpiry.plus(newPeriod.minus(currentPeriod));

                // Don't allow new expiry to occur in the past.
                if (newExpiry.isBefore(currentDate)) {
                    throw new TdarRecoverableRuntimeException("abstractInformationResourceService.expiry_occurs_before_today");
                }
                irFile.setDateMadePublic(newExpiry.toDate());
            }
        } else {
            irFile.setDateMadePublic(null);
        }

        if (proxy.getAction().updatesMetadata()) {
            irFile.setDescription(proxy.getDescription());
            irFile.setFileCreatedDate(proxy.getFileCreatedDate());
        }

        if (sequenceNumber == null) {
            logger.warn("No sequence number set on file proxy {}, existing sequence number was {}", this, irFile.getSequenceNumber());
        } else {
            irFile.setSequenceNumber(sequenceNumber);
        }
        irFile.setRestriction(proxy.getRestriction());

    }

    public void validateFileProxies() {
        List<FileProxy> rollbackIssues = new ArrayList<>();

        for (FileProxy proxy : cleanedProxies) {
            logger.debug("applying {} to {}", proxy, informationResource);
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
    }

    /*
     * Finds an @link InformationResourceFile based on the file id information associated with the @link FileProxy
     */
    private InformationResourceFile findInformationResourceFile(FileProxy proxy) {
        InformationResourceFile irFile = datasetDao.find(InformationResourceFile.class, proxy.getFileId());
        if (irFile == null) {
            logger.error("{} had no findable InformationResourceFile.id set on it", proxy);
            // FIXME: throw an exception?
        }
        return irFile;
    }
}
