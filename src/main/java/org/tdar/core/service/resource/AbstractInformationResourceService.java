package org.tdar.core.service.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.InformationResourceFileDao;
import org.tdar.core.dao.resource.ResourceDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.workflow.ActionMessageErrorSupport;
import org.tdar.core.service.workflow.WorkflowResult;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.Filestore.BaseFilestore;
import org.tdar.filestore.WorkflowContext;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.struts.data.FileProxy;

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
    private static final Filestore filestore = TdarConfiguration.getInstance().getFilestore();

    @Autowired
    private InformationResourceFileDao informationResourceFileDao;
    @Autowired
    private DatasetDao datasetDao;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    protected FileAnalyzer analyzer;

    // private MessageService messageService;

    // Martin: according to the Spring documentation all these private methods marked as @Transactional totally ignored.
    // They must be public to have the annotation recognised?
    // See: http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/transaction.html#transaction-declarative-annotations
    @Transactional(readOnly = false)
    private void addInformationResourceFile(InformationResource resource, InformationResourceFile irFile, FileProxy proxy) throws IOException {
        // always set the download/version info and persist the relationships between the InformationResource and its IRFile.
        incrementVersionNumber(irFile);
        // genericDao.saveOrUpdate(resource);
        getDao().saveOrUpdate(resource);
        irFile.setInformationResource(resource);
        proxy.setInformationResourceFileVersion(createVersionMetadataAndStore(irFile, proxy));
        setInformationResourceFileMetadata(irFile, proxy);
        for (FileProxy additionalVersion : proxy.getAdditionalVersions()) {
            logger.debug("Creating new version {}", additionalVersion);
            createVersionMetadataAndStore(irFile, additionalVersion);
        }
        getDao().saveOrUpdate(irFile);
        resource.add(irFile);
        logger.debug("all versions for {}", irFile);
    }

    @Transactional(readOnly = true)
    private InformationResourceFile findInformationResourceFile(FileProxy proxy) {
        InformationResourceFile irFile = getDao().find(InformationResourceFile.class, proxy.getFileId());
        if (irFile == null) {
            logger.error("{} had no findable InformationResourceFile.id set on it", proxy);
            // FIXME: throw an exception?
        }
        return irFile;
    }

    @Transactional
    public void importFileProxiesAndProcessThroughWorkflow(T resource, Person user, Long ticketId, ActionMessageErrorSupport listener,
            List<FileProxy> fileProxiesToProcess) throws IOException {
        if (CollectionUtils.isEmpty(fileProxiesToProcess)) {
            logger.debug("Nothing to process, returning.");
            return;
        }

        processMetadataForFileProxies(resource, fileProxiesToProcess.toArray(new FileProxy[0]));
        List<InformationResourceFileVersion> filesToProcess = new ArrayList<>();
        List<InformationResourceFile> irFiles = new ArrayList<>();

        for (FileProxy proxy : fileProxiesToProcess) {
            if (proxy.getAction().requiresWorkflowProcessing()) {
                InformationResourceFile irFile = proxy.getInformationResourceFile();
                irFiles.add(irFile);
                InformationResourceFileVersion version = proxy.getInformationResourceFileVersion();
                logger.trace("version: {} proxy: {} ", version, proxy);
                getDao().saveOrUpdate(irFile);
                switch (version.getFileVersionType()) {
                    case UPLOADED:
                    case UPLOADED_ARCHIVAL:
                        irFile.setInformationResourceFileType(analyzer.analyzeFile(version));
                        filesToProcess.add(version);
                        break;
                    default:
                        logger.debug("Not setting file type on irFile {} for VersionType {}", irFile, proxy.getVersionType());
                }
            }
        }
        
        // make sure we're only doing this if we have files to process
        if (irFiles.size() > 0) {
            addExistingCompositeFilesForProcessing(resource, filesToProcess, irFiles);
        }
        processFiles(resource, filesToProcess);

        /*
         * FIXME: When we move to an asynchronous model, this section and below will need to be moved into their own dedicated method
         */
        new WorkflowResult(fileProxiesToProcess).addActionErrorsAndMessages(listener);

        // getDao().refreshAll(resource.getInformationResourceFiles());
        if (ticketId != null) {
            PersonalFilestore personalFilestore = personalFilestoreService.getPersonalFilestore(user);
            personalFilestore.purge(getDao().find(PersonalFilestoreTicket.class, ticketId));
        }
    }

    /*
     * When processing informationResourceFileVersions, make sure that we have ALL of the files available
     * This may mean trolling through the active files on the resource and adding them to the proxy
     */
    private void addExistingCompositeFilesForProcessing(T resource, List<InformationResourceFileVersion> filesToProcess, List<InformationResourceFile> irFiles)
            throws FileNotFoundException {
        if (resource.getResourceType().isCompositeFilesEnabled()) {
            for (InformationResourceFile file : resource.getActiveInformationResourceFiles()) {
                if (!irFiles.contains(file) && !file.isDeleted()) {
                    InformationResourceFileVersion latestUploadedVersion = file.getLatestUploadedVersion();
                    latestUploadedVersion.setTransientFile(filestore.retrieveFile(latestUploadedVersion));
                    filesToProcess.add(latestUploadedVersion);
                }
            }
        }
    }

    private void processFiles(T resource, List<InformationResourceFileVersion> filesToProcess) throws IOException {
        if (!CollectionUtils.isEmpty(filesToProcess)) {
            if (resource.getResourceType().isCompositeFilesEnabled()) {
                analyzer.processFile(filesToProcess.toArray(new InformationResourceFileVersion[0]));
            } else {
                for (InformationResourceFileVersion version : filesToProcess) {
                    if ((version.getTransientFile() == null) || (!version.getTransientFile().exists())) {
                        // If we are re-processing, the transient file might not exist.
                        version.setTransientFile(filestore.retrieveFile(version));
                    }
                    analyzer.processFile(version);
                }
            }
        }
    }

    @Transactional
    private void processFileProxyMetadata(InformationResource informationResource, FileProxy proxy) throws IOException {
        logger.debug("applying {} to {}", proxy, informationResource);
        // will be reassigned in a REPLACE or ADD_DERIVATIVE
        InformationResourceFile irFile = new InformationResourceFile();
        if (proxy.getAction().requiresExistingIrFile()) {
            irFile = findInformationResourceFile(proxy);
            if (irFile == null) {
                logger.error("FileProxy {} {} had no InformationResourceFile.id ({}) set on it", proxy.getFilename(), proxy.getAction(), proxy.getFileId());
                return;
            }
        }

        switch (proxy.getAction()) {
            case MODIFY_METADATA:
                // set sequence number and confidentiality
                setInformationResourceFileMetadata(irFile, proxy);
                getDao().update(irFile);
                break;
            case REPLACE:
                // explicit fall through to ADD after loading the existing irFile to be replaced.
            case ADD:
                addInformationResourceFile(informationResource, irFile, proxy);
                break;
            case ADD_DERIVATIVE:
                createVersionMetadataAndStore(irFile, proxy);
                break;
            case DELETE:
                irFile.markAsDeleted();
                getDao().update(irFile);
                if (informationResource instanceof Dataset) {
                    unmapDataTablesForFile((Dataset) informationResource, irFile);
                }
                break;
            case NONE:
                logger.debug("Taking no action on {} with proxy {}", informationResource, proxy);
                break;
            default:
                break;
        }
        proxy.setInformationResourceFile(irFile);
    }

    @Transactional(readOnly = true)
    private void unmapDataTablesForFile(Dataset dataset, InformationResourceFile irFile) {
        String fileName = irFile.getFileName();
        switch (FilenameUtils.getExtension(fileName).toLowerCase()) {
            case "tab":
            case "csv":
            case "txt":
                String name = FilenameUtils.getBaseName(fileName);
                name = datasetDao.normalizeTableName(name);
                DataTable dt = dataset.getDataTableByGenericName(name);
                logger.info("removing {}", dt);
                cleanupUnusedTablesAndColumns(dataset, Arrays.asList(dt));
                // dataset.getDataTableByGenericName(name)
                break;
            default:
                cleanupUnusedTablesAndColumns(dataset, dataset.getDataTables());
        }
    }

    @Transactional
    public void processMetadataForFileProxies(InformationResource informationResource, FileProxy... proxies) throws IOException {
        for (FileProxy proxy : proxies) {
            processFileProxyMetadata(informationResource, proxy);
        }
    }

    @Transactional(readOnly = true)
    public void cleanupUnusedTablesAndColumns(Dataset dataset, Collection<DataTable> tablesToRemove) {
        logger.info("deleting unmerged tables: {}", tablesToRemove);
        ArrayList<DataTableColumn> columnsToUnmap = new ArrayList<DataTableColumn>();
        for (DataTable table : tablesToRemove) {
            columnsToUnmap.addAll(table.getDataTableColumns());
        }
        // first unmap all columns from the removed tables
        datasetDao.unmapAllColumnsInProject(dataset.getProject(), columnsToUnmap);
        dataset.getDataTables().removeAll(tablesToRemove);
    }

    private void setInformationResourceFileMetadata(InformationResourceFile irFile, FileProxy fileProxy) {
        irFile.setRestriction(fileProxy.getRestriction());
        Integer sequenceNumber = fileProxy.getSequenceNumber();
        if (fileProxy.getRestriction() == FileAccessRestriction.EMBARGOED) {
            if (irFile.getDateMadePublic() == null) {
                Calendar calendar = Calendar.getInstance();
                // set date made public to 5 years now.
                calendar.add(Calendar.YEAR, TdarConfiguration.getInstance().getEmbargoPeriod());
                irFile.setDateMadePublic(calendar.getTime());
            }
        } else {
            irFile.setDateMadePublic(null);
        }

        if (fileProxy.getAction() == FileAction.MODIFY_METADATA || fileProxy.getAction() == FileAction.ADD) {
            irFile.setDescription(fileProxy.getDescription());
            irFile.setFileCreatedDate(fileProxy.getFileCreatedDate());
        }

        if (sequenceNumber == null) {
            logger.warn("No sequence number set on file proxy {}, existing sequence number was {}", fileProxy, irFile.getSequenceNumber());
        }
        else {
            irFile.setSequenceNumber(sequenceNumber);
        }
    }

    private void incrementVersionNumber(InformationResourceFile irFile) {
        irFile.incrementVersionNumber();
        irFile.clearStatus();
        logger.info("incremented version number and reset download and status for irfile: {}", irFile, irFile.getLatestVersion());
    }

    @Transactional(readOnly = false)
    public void reprocessInformationResourceFiles(T ir, ActionMessageErrorSupport listener) throws Exception {
        List<InformationResourceFileVersion> latestVersions = new ArrayList<>();
        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            if (irFile.isDeleted()) {
                continue;
            }
            InformationResourceFileVersion original = irFile.getLatestUploadedVersion();
            original.setTransientFile(TdarConfiguration.getInstance().getFilestore().retrieveFile(original));
            latestVersions.add(original);
            Iterator<InformationResourceFileVersion> iterator = irFile.getInformationResourceFileVersions().iterator();
            while (iterator.hasNext()) {
                InformationResourceFileVersion version = iterator.next();
                if (!version.equals(original) && !version.isUploaded() && !version.isArchival()) {
                    iterator.remove();
                    informationResourceFileDao.delete(version);
                }
            }
        }
        processFiles(ir, latestVersions);
        // this is a known case where we need to purge the session
        getDao().synchronize();

        for (InformationResourceFile irFile : ir.getInformationResourceFiles()) {
            final WorkflowContext workflowContext = irFile.getWorkflowContext();
            if (!workflowContext.isProcessedSuccessfully()) {
                new WorkflowResult(workflowContext).addActionErrorsAndMessages(listener);
            }
        }

    }

    @Transactional(readOnly = false)
    private InformationResourceFileVersion createVersionMetadataAndStore(InformationResourceFile irFile, FileProxy fileProxy) throws IOException {
        String filename = BaseFilestore.sanitizeFilename(fileProxy.getFilename());
        if (fileProxy.getFile() == null || !fileProxy.getFile().exists()) {
            throw new TdarRecoverableRuntimeException("something went wrong, file " + fileProxy.getFilename() + " does not exist");
        }
        InformationResourceFileVersion version = new InformationResourceFileVersion(fileProxy.getVersionType(), filename, irFile);
        if (irFile.isTransient()) {
            getDao().saveOrUpdate(irFile);
        }

        irFile.addFileVersion(version);
        filestore.store(fileProxy.getFile(), version);
        version.setTransientFile(fileProxy.getFile());
        getDao().save(version);
        getDao().saveOrUpdate(irFile);
        return version;
    }

    public List<Language> findAllLanguages() {
        return Arrays.asList(Language.values());
    }

    /**
     * @param analyzer
     *            the analyzer to set
     */
    @Autowired
    public void setAnalyzer(FileAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

}
