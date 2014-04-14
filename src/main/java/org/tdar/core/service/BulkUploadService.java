package org.tdar.core.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.bulk.BulkFileProxy;
import org.tdar.core.service.bulk.BulkManifestProxy;
import org.tdar.core.service.bulk.BulkUploadTemplate;
import org.tdar.core.service.bulk.CellMetadata;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.workflow.ActionMessageErrorListener;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.struts.data.FileProxy;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.Activity;

/**
 * The BulkUploadService support the bulk loading of resources into tDAR through
 * the user interface
 * 
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class BulkUploadService {

    @Autowired
    private EntityService entityService;

    @Autowired
    private ImportService importService;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SearchIndexService searchIndexService;

    @Autowired
    private FileAnalyzer analyzer;

    @Autowired
    private XmlService xmlService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private BulkUploadTemplateService bulkUploadTemplateService;

    @Autowired
    private ReflectionService reflectionService;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Long, AsyncUpdateReceiver> asyncStatusMap = new WeakHashMap<Long, AsyncUpdateReceiver>();

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously, and one without. Note, the @Async
     * annotation does not work in the Spring testing framework
     */
    @Async
    public void saveAsync(final InformationResource image, final Long submitterId, final Long ticketId, final File excelManifest,
            final Collection<FileProxy> fileProxies, Long accountId) {
        save(image, submitterId, ticketId, excelManifest, fileProxies, accountId);
    }

    /**
     * Load the Excel manifest if it exists
     */
    public BulkManifestProxy loadExcelManifest(BulkFileProxy wrapper, InformationResource resourceTemplate, Person submitter, Collection<FileProxy> fileProxies,
            Long ticketId) {
        BulkManifestProxy manifestProxy = null;
        File excelManifest = wrapper.getFile();
        if ((excelManifest != null) && excelManifest.exists()) {
            logger.debug("processing manifest:" + excelManifest.getName());
            try {
                wrapper.setStream(new FileInputStream(excelManifest));
                Workbook workbook = WorkbookFactory.create(wrapper.getStream());
                manifestProxy = validateManifestFile(workbook.getSheetAt(0), resourceTemplate, submitter, fileProxies, ticketId);
            } catch (Exception e) {
                logger.debug("exception happened when reading excel file", e);
                manifestProxy = new BulkManifestProxy(null, null, null, excelService);
                manifestProxy.getAsyncUpdateReceiver().addError(e);
                if (Persistable.Base.isNotNullOrTransient(ticketId)) {
                    getAsyncStatusMap().put(ticketId, manifestProxy.getAsyncUpdateReceiver());
                }
            } finally {
                IOUtils.closeQuietly(wrapper.getStream());
            }
        }
        return manifestProxy;
    }

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously. This method: (a) looks at the
     * excel manifest proxy, tries to parse it, fails fast (b) loads the proxy
     * image record and clones it to each resource type as needed one per file
     * being imported (c) applies custom metadata from the manifest (d) returns
     */
    @Transactional
    public void save(final InformationResource resourceTemplate_, final Long submitterId, final Long ticketId, final File excelManifest_, final Collection<FileProxy> fileProxies,
            final Long accountId) {
        Person submitter = genericDao.find(Person.class, submitterId);
        // enforce that we're entirely on the session
        InformationResource resourceTemplate = resourceTemplate_;
        logger.debug("BEGIN ASYNC: " + resourceTemplate + fileProxies);

        resourceTemplate.setAccount(null);

        // in an async method the image's persistent associations will have
        // become detached from the hibernate session that loaded them, and
        // their lazy-init
        // fields will be unavailable. So we reload them to make them part of
        // the current session and regain access to any lazy-init associations.

        Project project = resourceTemplate.getProject();
        if ((project != null) && !project.equals(Project.NULL)) {
            Project p = genericDao.find(Project.class, project.getId());
            resourceTemplate.setProject(p);
        }

        logger.debug("ticketID:" + ticketId);
        Activity activity = registerActivity(fileProxies, submitter);
        BulkFileProxy excelManifest = new BulkFileProxy(excelManifest_, activity);
        float count = 0f;
        resourceTemplate.setDescription("");
        resourceTemplate.setDate(-1);
        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("bulkUploadService.the_system_has_not_received_any_files");
            throw throwable;
        }
        logger.debug("mapping metadata with excelManifest:" + excelManifest);
        BulkManifestProxy manifestProxy = loadExcelManifest(excelManifest, resourceTemplate, submitter, fileProxies, ticketId);

        if (manifestProxy == null) {
            manifestProxy = new BulkManifestProxy(null, null, null, excelService);
        }
        manifestProxy.setFileProxies(fileProxies);
        // If there are errors, then stop...
        AsyncUpdateReceiver updateReciever = manifestProxy.getAsyncUpdateReceiver();
        List<String> asyncErrors = updateReciever.getAsyncErrors();
        if (CollectionUtils.isNotEmpty(asyncErrors)) {
            logger.debug("not moving further because of async validation errors: {}", asyncErrors);
            completeBulkUpload(resourceTemplate, accountId, updateReciever, excelManifest, ticketId);
            return;
        }

        logger.info("bulk: processing files, and then persisting");
        count = processFileProxiesIntoResources(manifestProxy, count);

        List<Resource> remainingResources = new ArrayList<>();
        try {
            manifestProxy.setSubmitter(null);
            submitter = genericDao.merge(submitter);
            resourceTemplate = null;
            for (Resource resource : manifestProxy.getResourcesCreated().values()) {
                remainingResources.add(resource);
            }
            manifestProxy.getResourcesCreated().clear();
        } catch (Throwable t) {
            logger.error("{}", t);
        }
        logger.info("bulk: applying manifest file data to resources");
        try {
            updateAccountQuotas(accountId, remainingResources);
            logger.info("bulk: finishing quota work");
        } catch (Throwable t) {
            logger.error("quota error happend", t);
            updateReciever.addError(t);
        }

        try {
            logAndPersist(manifestProxy.getAsyncUpdateReceiver(), remainingResources, submitterId, accountId);
            logger.info("bulk: completing");
        } catch (Throwable t) {
            logger.error("log and persist error happened", t);
            updateReciever.addError(t);
        }

        completeBulkUpload(resourceTemplate, accountId, manifestProxy.getAsyncUpdateReceiver(), excelManifest, ticketId);
        logger.info("bulk: done");
    }

    /**
     * Take the ManifestProxy and iterate through attached FileProxies and make resources out of them
     * 
     */
    @Transactional
    public float processFileProxiesIntoResources(BulkManifestProxy manifestProxy, float count) {
        for (FileProxy fileProxy : manifestProxy.getFileProxies()) {
            logger.trace("processing: {}", fileProxy);
            try {
                if ((fileProxy == null) || (fileProxy.getAction() != FileAction.ADD)) {
                    continue;
                }
                logger.debug("processing: {} | {}", fileProxy, fileProxy.getAction());
                String fileName = fileProxy.getFilename();
                // if there is not an exact match in the manifest file then,
                // skip it. If there is no manifest file, then go merrily along
                if ((manifestProxy != null) && !manifestProxy.containsFilename(fileName)) {
                    logger.info("skipping {} filenames: {} ", fileName, manifestProxy.listFilenames());
                    continue;
                }
                ActionMessageErrorListener listener = new ActionMessageErrorListener();
                InformationResource informationResource = (InformationResource) manifestProxy.getResourcesCreated().get(fileName);
                // createInternalResourceCollectionWithResource
                importService.reconcilePersistableChildBeans(manifestProxy.getSubmitter(), informationResource);
                manifestProxy.getResourcesCreated().put(fileName, informationResource);
                // informationResource = genericDao.merge(informationResource);
                informationResourceService.importFileProxiesAndProcessThroughWorkflow(informationResource, manifestProxy.getSubmitter(), null, listener,
                        Arrays.asList(fileProxy));
                genericDao.saveOrUpdate(informationResource);
                manifestProxy.getAsyncUpdateReceiver().getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                if (listener.hasActionErrors()) {
                    manifestProxy.getAsyncUpdateReceiver().addError(new Exception(String.format("Errors: %s", listener)));
                }
            } catch (Exception e) {
                logger.warn("something happend  while processing file proxy", e);
                manifestProxy.getAsyncUpdateReceiver().addError(e);
            }
        }
        return count;
    }

    /**
     * Update the billing quotas and accounts as needed
     */
    private void updateAccountQuotas(Long accountId, Collection<Resource> resources) {
        if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
            final Account account = genericDao.find(Account.class, accountId);
            accountService.updateQuota(account, resources);
        }
    }

    /**
     * Log each record to XML and put in the filestore, and persist the record
     * as needed, then let the @link AsyncUpdateReceiver know we're done
     */
    @Transactional
    public void logAndPersist(AsyncUpdateReceiver receiver, List<Resource> resources, Long submitterId, Long accountId) {
        logger.info("bulk: setting final statuses and logging");

        ResourceCollection bulkUpload = new ResourceCollection(CollectionType.SHARED);
        Person submitter = genericDao.find(Person.class, submitterId);
        bulkUpload.markUpdated(submitter);
        bulkUpload.setVisible(false);
        bulkUpload.setName(String.format("bulk upload for %s on %s", bulkUpload.getSubmitter().getProperName(), new Date()));
        genericDao.saveOrUpdate(bulkUpload);
        Iterator<Resource> iterator = resources.iterator();
        while (iterator.hasNext()) {
            try {
                Resource resource = iterator.next();
                genericDao.refresh(resource);
                bulkUpload.getResources().add(resource);
                resource.getResourceCollections().add(bulkUpload);
            } catch (Exception e) {
                logger.error("could not bring resource onto session: {} ", e);
            }
        }
        resources.clear();
        genericDao.saveOrUpdate(bulkUpload);

        Set<ResourceCollection> cols = new HashSet<>();
        for (Resource resource : bulkUpload.getResources()) {
            receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
            resource.getResourceCollections().add(bulkUpload);
            String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]",
                    resource.getResourceType(), submitter, resource.getId(), StringUtils.left(resource.getTitle(), 100));

            try {
                xmlService.logRecordXmlToFilestore(resource);
                logger.debug("resource: {} {}", resource, resource.getResourceCollections());
                cols.addAll(resource.getResourceCollections());
                // genericDao.refresh(submitter);
                resourceService.logResourceModification(resource, resource.getSubmitter(), logMessage);
                // FIXME: saveRecordToFilestore doesn't distinguish 'recoverable' from 'disastrous' exceptions. Until it does we just have to assume the worst.
                resource.setReadyToIndex(true);
                genericDao.saveOrUpdate(resource);
            } catch (TdarRecoverableRuntimeException trex) {
                receiver.addError(trex);
                receiver.setCompleted();
                throw trex;
            }
        }
    }

    /**
     * Setup the @link Activity so we can track it
     * 
     * @param fileProxies
     * @param submitter
     * @return
     */
    private Activity registerActivity(final Collection<FileProxy> fileProxies, Person submitter) {
        Activity activity = new Activity();
        activity.setName(String.format("bulk upload for %s of %s resources", submitter.getName(), fileProxies.size()));
        activity.start();
        ActivityManager.getInstance().addActivityToQueue(activity);
        return activity;
    }

    /**
     * Close all of our @link InputStream entries, purge the @link
     * PersonalFilestore , delete the @link Image entry, and return
     * 
     * @param image
     * @param accountId
     * @param receiver
     * @param wrapper
     * @param ticketId
     */
    private void completeBulkUpload(InformationResource image, Long accountId, AsyncUpdateReceiver receiver, BulkFileProxy wrapper, Long ticketId) {

        try {
            PersonalFilestoreTicket findPersonalFilestoreTicket = filestoreService.findPersonalFilestoreTicket(ticketId);
            filestoreService.getPersonalFilestore(findPersonalFilestoreTicket).purgeQuietly(findPersonalFilestoreTicket);
        } catch (Exception e) {
            receiver.addError(e);
        }

        receiver.setCompleted();
        logger.info("bulk upload complete");
        // logger.info("remaining: " + image.getInternalResourceCollection());
        wrapper.getActivity().end();
        // image.setStatus(Status.DELETED);
        try {
            IOUtils.closeQuietly(wrapper.getStream());
        } catch (Exception e) {
            logger.debug("an exception occurred when closing import stream", e);
        }

    }

    /**
     * Reads through the Excel Sheet and validates it. Looks for required fields
     * that are missing, and also parses all files to figure out what files we
     * have and whether to evaluate the entire sheet as case-sensitive filenames
     * or not.
     * 
     * @param sheet
     * @param fileProxies
     * @return
     */
    @Transactional(readOnly = true)
    public BulkManifestProxy validateManifestFile(Sheet sheet, InformationResource image, Person submitter, Collection<FileProxy> fileProxies, Long ticketId) {

        Iterator<Row> rowIterator = sheet.rowIterator();

        LinkedHashSet<CellMetadata> allValidFields = bulkUploadTemplateService.getAllValidFieldNames();
        Map<String, CellMetadata> cellLookupMap = bulkUploadTemplateService.getCellLookupMapByName(allValidFields);
        BulkManifestProxy proxy = new BulkManifestProxy(sheet, allValidFields, cellLookupMap, excelService);
        if (Persistable.Base.isNotNullOrTransient(ticketId)) {
            getAsyncStatusMap().put(ticketId, proxy.getAsyncUpdateReceiver());
        }
        proxy.setSubmitter(submitter);
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        proxy.setColumnNamesRow(sheet.getRow(ExcelService.FIRST_ROW));

        // capture all of the column names and make sure they're valid in general
        proxy.initializeColumnMetadata(cellLookupMap, evaluator);
        if (CollectionUtils.isEmpty(fileProxies)) {
            proxy.createFakeFileProxies(sheet);
        } else {
            proxy.setFileProxies(fileProxies);
        }
        List<String> requiredErrors = new ArrayList<String>();
        for (CellMetadata field : allValidFields) {
            if (field.isRequired() && !proxy.getRequired().contains(field)) {
                requiredErrors.add(field.getDisplayName());
            }
        }

        if (proxy.getColumnNames().isEmpty()) {
            logger.info("the manifest file uploaded appears to be empty");
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_manifest_file_uploaded_appears_to_be_empty_no_columns_found");
        }

        if (!proxy.getColumnNames().get(ExcelService.FIRST_COLUMN).equals(BulkUploadTemplate.FILENAME)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_first_column_must_be_the_filename");
        }

        if (CollectionUtils.isNotEmpty(requiredErrors)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_following_columns_are_required_s",
                    Arrays.asList(StringUtils.join(requiredErrors.toArray(), ", ")));
        }

        proxy.testFilenameCaseAndAddFiles(rowIterator);
        float count = 0;

        count = processFileProxiesIntoResources(image, proxy, count);
        try {
            proxy.readExcelFile(bulkUploadTemplateService, entityService, reflectionService);
        } catch (InvalidFormatException | IOException e) {
            proxy.getAsyncUpdateReceiver().addError(e);
            logger.error(e.getMessage(), e);
        }

        return proxy;
    }

    /**
     * Given the template @link Image (@link InformationResource) process each
     * of the @link FileProxy entries (Excel template excluded) into it's own
     * resource
     * 
     * @param image
     * @param submitter
     * @param proxy
     * @param fileProxies
     * @param receiver
     * @param count
     * @return
     */
    private float processFileProxiesIntoResources(final InformationResource image, final BulkManifestProxy proxy, float count) {

        if (StringUtils.isBlank(image.getTitle())) {
            image.setTitle(BulkUploadTemplate.BULK_TEMPLATE_TITLE);
        }

        if (proxy == null) {
            return count;
        }

        for (FileProxy fileProxy : proxy.getFileProxies()) {
            logger.trace("processing: {}", fileProxy);
            try {
                if ((fileProxy == null) || (fileProxy.getAction() != FileAction.ADD)) {
                    continue;
                }
                logger.debug("processing: {} | {}", fileProxy, fileProxy.getAction());
                String fileName = fileProxy.getFilename();
                // if there is not an exact match in the manifest file then,
                // skip it. If there is no manifest file, then go merrily along
                if ((proxy != null) && !proxy.containsFilename(fileName)) {
                    logger.info("skipping {} filenames: {} ", fileName, proxy.listFilenames());
                    continue;
                }

                logger.info("inspecting ... {}", fileName);
                count++;
                float percent = (count / Float.valueOf(proxy.getFileProxies().size())) * 50;
                proxy.getAsyncUpdateReceiver().update(percent, " processing " + fileName);
                ResourceType suggestTypeForFile = analyzer.suggestTypeForFileName(fileName, getResourceTypesSupportingBulkUpload());
                if (suggestTypeForFile == null) {
                    logger.debug("skipping because cannot figure out extension for file {}", fileName);
                    proxy.getAsyncUpdateReceiver().addError(
                            new TdarRecoverableRuntimeException("bulkUploadService.skipping_line_filename_not_found", Arrays.asList(fileName)));
                    continue;
                }

                createResourceAndAddToProxyList(image, proxy, fileName, suggestTypeForFile);
            } catch (Exception e) {
                logger.error("something happend", e);
                proxy.getAsyncUpdateReceiver().addError(e);
            }
        }
        return count;
    }

    /**
     * Create the actual resource and add it to the proxy list
     * 
     * @param image
     * @param proxy
     * @param fileName
     * @param suggestTypeForFile
     */
    private void createResourceAndAddToProxyList(final InformationResource image, final BulkManifestProxy proxy, String fileName,
            ResourceType suggestTypeForFile) {
        ActionMessageErrorListener listener = new ActionMessageErrorListener();
        Class<? extends Resource> resourceClass = suggestTypeForFile.getResourceClass();
        if (InformationResource.class.isAssignableFrom(resourceClass)) {
            logger.info("saving " + fileName + "..." + suggestTypeForFile);
            InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(image, resourceClass, false);
            informationResource.setReadyToIndex(false);
            informationResource.setTitle(fileName);
            informationResource.markUpdated(proxy.getSubmitter());
            informationResource.setDescription(" ");
            // informationResource.setStatus(Status);
            // make sure we're not on the session, period
            genericDao.detachFromSession(informationResource);
            proxy.getResourcesCreated().put(fileName, informationResource);
            if (listener.hasActionErrors()) {
                proxy.getAsyncUpdateReceiver().addError(new Exception(String.format("Errors: %s", listener)));
            }
        }
    }

    /**
     * get the set of @link ResourceType enums that support BulkUpload
     * 
     * @return
     */
    public ResourceType[] getResourceTypesSupportingBulkUpload() {
        return ResourceType.getTypesSupportingBulkUpload();

    }

    /**
     * Expose the AsyncStatus Reciever
     * 
     * @param ticketId
     * @return
     */
    public synchronized AsyncUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.get(ticketId);
    }

    public synchronized Map<Long, AsyncUpdateReceiver> getAsyncStatusMap() {
        return asyncStatusMap;
    }
}