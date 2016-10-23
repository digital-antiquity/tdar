package org.tdar.core.service.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AsyncUpdateReceiver;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FileAnalyzer;
import org.tdar.utils.Pair;
import org.tdar.utils.activity.Activity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
    private ImportService importService;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private BillingAccountService accountService;

    @Autowired
    private FileAnalyzer analyzer;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private Cache<Long, BulkUpdateReceiver> asyncStatusMap;

    public BulkUploadService() {
        asyncStatusMap = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously, and one without. Note, the @Async
     * annotation does not work in the Spring testing framework
     */
    @Async
    public void saveAsync(final InformationResource image, final Long submitterId, final Long ticketId,
            final Collection<FileProxy> fileProxies, Long accountId) {
        save(image, submitterId, ticketId, fileProxies, accountId);
    }

    /**
     * The Save method needs to endpoints, one with the @Async annotation to
     * allow Spring to run it asynchronously. This method:
     * (e) processes each file through the workflow
     * (f) reconcile account issues
     * (g) save records to XML
     * (h) reindex if needed
     */
    @Transactional
    public void save(final InformationResource resourceTemplate_, final Long submitterId, final Long ticketId,
            final Collection<FileProxy> fileProxies, final Long accountId) {

        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("bulkUploadService.the_system_has_not_received_any_files");
            throw throwable;
        }
        
        genericDao.clearCurrentSession();
        BulkUpdateReceiver asyncUpdateReceiver = new BulkUpdateReceiver();
        asyncStatusMap.put(ticketId, asyncUpdateReceiver);
        TdarUser submitter = genericDao.find(TdarUser.class, submitterId);

        // it is assumed that the the resourceTemplate is off the session when passed in, but make sure.
        InformationResource resourceTemplate = resourceTemplate_;
        genericDao.detachFromSession(resourceTemplate_);
        logger.debug("BEGIN ASYNC: " + resourceTemplate + fileProxies);
        List<Resource> resources = new ArrayList<>();

        try {
            prepareTemplate(submitter, resourceTemplate);

            logger.debug("ticketID:" + ticketId);
            Activity activity = registerActivity(fileProxies, submitter);


        try {
        logAndPersist(manifestProxy.getAsyncUpdateReceiver(), remainingResources, submitterId, accountId);
        } catch (Throwable t) {
            logger.error("erorr in persist", t);
            logger.error("erorr in persist", t.getCause());
            
        }
            logger.info("bulk: processing files, and then persisting");
            processFileProxiesIntoResources(fileProxies, resourceTemplate, submitter, asyncUpdateReceiver, resources);
            updateAccountQuotas(accountId, resources, asyncUpdateReceiver, submitter);

            ListCollection collection = logAndPersist(asyncUpdateReceiver, resources, submitterId, accountId);
            completeBulkUpload(accountId, asyncUpdateReceiver, activity, ticketId);
            asyncUpdateReceiver.setCollectionId(collection.getId());
        } catch (Throwable t) {
            logger.error("exception in BulkUploadService: {}", t, t);
        }

        logger.info("bulk: done");
    }

    /**
     * The account will be set by controller, but future calls to "merge" will not pass through and thus cause issues
     * because account is a transient object on Resource. Same with shared collections, submitter, and projects.
     * calling merge now so we can only call it once.
     * 
     * @param authorizedUser
     * @param resourceTemplate
     * @return
     */
    private void prepareTemplate(TdarUser authorizedUser, InformationResource resourceTemplate) {
        ;
        // set the account to null as it is transient and not hibernate managed and thus, will not respond to merges.
        resourceTemplate.setAccount(null);

        resourceTemplate.setDescription("");
        resourceTemplate.setDate(-1);

        resourceService.clearOneToManyIds(resourceTemplate);
    }

    /**
     * Take the ManifestProxy and iterate through attached FileProxies and make resources out of them
     * 
     */
    @Transactional
    public void processFileProxiesIntoResources(Collection<FileProxy> fileProxies, InformationResource image, TdarUser authenticatedUser,
            AsyncUpdateReceiver asyncUpdateReceiver, List<Resource> resources) {
        int count = 0;
        image.setSubmitter(authenticatedUser);
        Map<String,FileProxy> map = new HashMap<>();
        Map<String,InformationResource> rMap = new HashMap<>();
        for (FileProxy fileProxy : fileProxies) {
            logger.trace("processing: {}", fileProxy);
            try {
                if ((fileProxy == null) || (fileProxy.getAction() != FileAction.ADD)) {
                    continue;
                }

                String fileName = fileProxy.getFilename();

                logger.info("inspecting ... {}", fileName);
                count++;
                float percent = (count / Float.valueOf(fileProxies.size())) * 95;
                asyncUpdateReceiver.update(percent, " processing " + fileName);
                ResourceType suggestTypeForFile = analyzer.suggestTypeForFileName(fileName, getResourceTypesSupportingBulkUpload());
                if (suggestTypeForFile == null) {
                    logger.debug("skipping because cannot figure out extension for file {}", fileName);
                    asyncUpdateReceiver.addError(
                            new TdarRecoverableRuntimeException("bulkUploadService.skipping_line_filename_not_found", Arrays.asList(fileName)));
                    continue;
                }
                InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(image, suggestTypeForFile.getResourceClass(), true);
                informationResource.setTitle(fileName);
                informationResource.setId(null);
                informationResource.setDescription("add description");
                informationResource.setDate(DateTime.now().getYear());
                informationResource.markUpdated(authenticatedUser);
                rMap.put(fileName, informationResource);
                map.put(fileName, fileProxy);
                informationResource = importService.bringObjectOntoSession(informationResource, authenticatedUser, Arrays.asList(fileProxy), null, false);
                genericDao.saveOrUpdate(informationResource);
                informationResource = genericDao.find(informationResource.getClass(), informationResource.getId());
                asyncUpdateReceiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileProxy.getFilename()));
                resources.add(informationResource);
            } catch (Exception e) {
                logger.warn("something happend  while creating file", e);
                asyncUpdateReceiver.addError(e);
            }
        }
    }

    /**
     * Update the billing quotas and accounts as needed
     * 
     * @param updateReciever
     */
    private void updateAccountQuotas(Long accountId, List<Resource> resources, AsyncUpdateReceiver updateReciever, TdarUser user) {
        try {
            logger.info("bulk: finishing quota work");
            if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
                BillingAccount account = genericDao.find(BillingAccount.class, accountId);
                accountService.updateQuota(account, resources, user);
            }
        } catch (Throwable t) {
            logger.error("quota error happend", t);
            updateReciever.addError(t);
        }
    }

    /**
     * Log each record to XML and put in the filestore, and persist the record
     * as needed, then let the @link AsyncUpdateReceiver know we're done
     */
    private ResourceCollection logAndPersist(AsyncUpdateReceiver receiver, List<Resource> resources, Long submitterId, Long accountId) {
        logger.info("bulk: setting final statuses and logging");
        TdarUser submitter = genericDao.find(TdarUser.class, submitterId);
        String title = "Bulk Upload:" + DateTime.now().toString( DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
        ListCollection collection = new ListCollection(title, title, SortOption.TITLE, false, submitter);
        try {
            collection.markUpdated(submitter);
            collection.setSystemManaged(true);
            genericDao.saveOrUpdate(collection);
            for (Resource resource : resources) {
                receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
                String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]",
                        resource.getResourceType(), submitter, resource.getId(), StringUtils.left(resource.getTitle(), 100));

                try {
                    collection.getResources().add(resource);
                    resource.getResourceCollections().add(collection);
                    resourceService.logResourceModification(resource, resource.getSubmitter(), logMessage, RevisionLogType.CREATE);
                    genericDao.saveOrUpdate(resource);
                } catch (TdarRecoverableRuntimeException trex) {
                    receiver.addError(trex);
                    receiver.setCompleted();
                    throw trex;
                }
            }
            genericDao.saveOrUpdate(collection);
            logger.info("bulk: completing");
        } catch (Throwable t) {
            logger.error("log and persist error happened", t);
            receiver.addError(t);
        }
        return collection;
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
     */
    private void completeBulkUpload(Long accountId, AsyncUpdateReceiver receiver, Activity activity, Long ticketId) {

        try {
            PersonalFilestoreTicket findPersonalFilestoreTicket = filestoreService.findPersonalFilestoreTicket(ticketId);
            filestoreService.getPersonalFilestore(findPersonalFilestoreTicket).purgeQuietly(findPersonalFilestoreTicket);
        } catch (Exception e) {
            receiver.addError(e);
        }

        receiver.setCompleted();
        logger.info("bulk upload complete");
        // logger.info("remaining: " + image.getInternalResourceCollection());
<<<<<<< mine
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
    public BulkManifestProxy validateManifestFile(Sheet sheet, InformationResource resourceTemplate, TdarUser submitter, Collection<FileProxy> fileProxies,
            Long ticketId) {

        Iterator<Row> rowIterator = sheet.rowIterator();

        LinkedHashSet<CellMetadata> allValidFields = bulkUploadTemplateService.getAllValidFieldNames();
        Map<String, CellMetadata> cellLookupMap = bulkUploadTemplateService.getCellLookupMapByName(allValidFields);
        BulkManifestProxy proxy = new BulkManifestProxy(sheet, allValidFields, cellLookupMap, bulkUploadTemplateService, entityService,
                reflectionService);
        if (PersistableUtils.isNotNullOrTransient(ticketId)) {
            asyncStatusMap.put(ticketId, proxy.getAsyncUpdateReceiver());
        }
        proxy.setSubmitter(submitter);
        FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        proxy.setColumnNamesRow(sheet.getRow(ExcelWorkbookWriter.FIRST_ROW));

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

        if (!proxy.getColumnNames().get(ExcelWorkbookWriter.FIRST_COLUMN).equals(BulkUploadTemplate.FILENAME)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_first_column_must_be_the_filename");
        }

        if (CollectionUtils.isNotEmpty(requiredErrors)) {
            throw new TdarRecoverableRuntimeException("bulkUploadService.the_following_columns_are_required_s",
                    Arrays.asList(StringUtils.join(requiredErrors.toArray(), ", ")));
        }

        proxy.testFilenameCaseAndAddFiles(rowIterator);

        createResourceFromFileProxies(resourceTemplate, proxy);
        try {
            proxy.readExcelFile();
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
    private void createResourceFromFileProxies(final InformationResource image, final BulkManifestProxy proxy) {

        if (StringUtils.isBlank(image.getTitle())) {
            image.setTitle(BulkUploadTemplate.BULK_TEMPLATE_TITLE);
        }

        if (proxy == null) {
            return;
        }
        float count = 0f;
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
                logger.error("something happend", e.getCause());
                proxy.getAsyncUpdateReceiver().addError(e);
            }
        }
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
        Class<? extends Resource> resourceClass = suggestTypeForFile.getResourceClass();
        if (InformationResource.class.isAssignableFrom(resourceClass)) {
            logger.info("saving " + fileName + "..." + suggestTypeForFile);
            InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(image, resourceClass, false);
            informationResource.setTitle(fileName);
            informationResource.markUpdated(proxy.getSubmitter());
            informationResource.setDescription(" ");

            proxy.getResourcesCreated().put(fileName, informationResource);
        }
        activity.end();
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
    public BulkUpdateReceiver checkAsyncStatus(Long ticketId) {
        return asyncStatusMap.getIfPresent(ticketId);
    }

}