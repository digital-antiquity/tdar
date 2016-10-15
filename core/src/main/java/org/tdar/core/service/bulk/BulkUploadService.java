package org.tdar.core.service.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.InformationResourceService;
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
    private InformationResourceService informationResourceService;

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
     * (a) looks at the excel manifest proxy, tries to parse it, validate it, and fails if there are errors.
     * (b) loads the resourceTemplate (image) record and clones it to each resource type as needed one per file
     * (c) clones the resourceTemplate and copies
     * (d) copies the values from the excel manifest proxy onto the clone for each file
     * (e) processes each file through the workflow
     * (f) reconcile account issues
     * (g) save records to XML
     * (h) reindex if needed
     */
    @Transactional
    public void save(final InformationResource resourceTemplate_, final Long submitterId, final Long ticketId,
            final Collection<FileProxy> fileProxies,
            final Long accountId) {
        genericDao.clearCurrentSession();
        BulkUpdateReceiver asyncUpdateReceiver = new BulkUpdateReceiver();
        asyncStatusMap.put(ticketId, asyncUpdateReceiver);
        TdarUser submitter = genericDao.find(TdarUser.class, submitterId);

        InformationResource resourceTemplate = resourceTemplate_;
        logger.debug("BEGIN ASYNC: " + resourceTemplate + fileProxies);
        // in an async method the image's persistent associations will have
        // become detached from the hibernate session that loaded them, and
        // their lazy-init
        // fields will be unavailable. So we reload them to make them part of
        // the current session and regain access to any lazy-init associations.

        // merge complex, shared & pre-persisted objects at the beginning (Project, ResourceCollections, Submitter); do this so that their child objects are
        // brought onto session and not causing conflicts or duplicated
        try {
            prepareTemplateAndBringSharedObjectsOnSession(submitter, resourceTemplate);

            logger.debug("ticketID:" + ticketId);
            Activity activity = registerActivity(fileProxies, submitter);

            if (CollectionUtils.isEmpty(fileProxies)) {
                TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("bulkUploadService.the_system_has_not_received_any_files");
                throw throwable;
            }

            logger.info("bulk: processing files, and then persisting");
            ResourceCollection resources = processFileProxiesIntoResources(fileProxies, resourceTemplate, submitter, asyncUpdateReceiver);
            genericDao.saveOrUpdate(resources);
            updateAccountQuotas(accountId, resources, asyncUpdateReceiver, submitter);

            logAndPersist(asyncUpdateReceiver, resources, submitterId, accountId);
            completeBulkUpload(accountId, asyncUpdateReceiver, activity, ticketId);
            asyncUpdateReceiver.setCollectionId(resources.getId());
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
    private Long prepareTemplateAndBringSharedObjectsOnSession(TdarUser authorizedUser, InformationResource resourceTemplate) {
        ;
        // set the account to null as it is transient and not hibernate managed and thus, will not respond to merges.
        resourceTemplate.setAccount(null);

        resourceTemplate.setDescription("");
        resourceTemplate.setDate(-1);

        List<ResourceCollection> shared = new ArrayList<>();
        Iterator<ResourceCollection> iter = resourceTemplate.getResourceCollections().iterator();
        while (iter.hasNext()) {
            ResourceCollection collection = iter.next();
            if (collection.isInternal()) {
                continue;
            }
            iter.remove();
            shared.add(genericDao.merge(collection));
        }
        resourceTemplate.getResourceCollections().addAll(shared);

        Project project = resourceTemplate.getProject();
        if ((project != null) && !project.equals(Project.NULL)) {
            Project p = genericDao.find(Project.class, project.getId());
            resourceTemplate.setProject(p);
            return p.getId();
        }

        return null;
    }

    /**
     * Take the ManifestProxy and iterate through attached FileProxies and make resources out of them
     * 
     */
    @Transactional
    public ResourceCollection processFileProxiesIntoResources(Collection<FileProxy> fileProxies, InformationResource image, TdarUser authenticatedUser,
            AsyncUpdateReceiver asyncUpdateReceiver) {
        int count = 0;
        String title = "Bulk Upload:" + DateTime.now().toString( DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
        ResourceCollection collection = new ResourceCollection(title, title, SortOption.TITLE, CollectionType.SHARED, false, authenticatedUser);
        collection.markUpdated(authenticatedUser);
        collection.setSystemManaged(true);
        image.setSubmitter(authenticatedUser);
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
                InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(image, suggestTypeForFile.getResourceClass(),
                        false);
                informationResource.setTitle(fileName);
                informationResource.setDescription("add description");
                informationResource.setDate(DateTime.now().getYear());
                informationResource.markUpdated(authenticatedUser);
                genericDao.saveOrUpdate(informationResource);

                // bring the children of the resource onto the session, generate new @OneToMany relationships, etc.
                informationResource = importService.reconcilePersistableChildBeans(authenticatedUser, informationResource);
                // merge everything onto session and persist (needed for thread issues)
                informationResource = genericDao.merge(informationResource);
                genericDao.saveOrUpdate(informationResource);
                collection.getResources().add(informationResource);
                informationResource.getResourceCollections().add(collection);
                // process files
                ErrorTransferObject listener = informationResourceService.importFileProxiesAndProcessThroughWorkflow(informationResource,
                        authenticatedUser, null, Arrays.asList(fileProxy));

                // make sure we're up-to-date (needed for thread issues)
                informationResource = genericDao.find(informationResource.getClass(), informationResource.getId());
                asyncUpdateReceiver.getDetails().add(new Pair<Long, String>(informationResource.getId(), fileName));
                if (CollectionUtils.isNotEmpty(listener.getActionErrors())) {
                    asyncUpdateReceiver.addError(new Exception(String.format("Errors: %s", listener)));
                }
            } catch (Exception e) {
                logger.warn("something happend  while processing file proxy", e);
                asyncUpdateReceiver.addError(e);
            }
        }
        return collection;
    }

    /**
     * Update the billing quotas and accounts as needed
     * 
     * @param updateReciever
     */
    private void updateAccountQuotas(Long accountId, ResourceCollection resources, AsyncUpdateReceiver updateReciever, TdarUser user) {
        try {
            logger.info("bulk: finishing quota work");
            if (TdarConfiguration.getInstance().isPayPerIngestEnabled()) {
                BillingAccount account = genericDao.find(BillingAccount.class, accountId);
                accountService.updateQuota(account, resources.getResources(), user);
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
    private void logAndPersist(AsyncUpdateReceiver receiver, ResourceCollection resources, Long submitterId, Long accountId) {
        logger.info("bulk: setting final statuses and logging");
        try {
            Person submitter = genericDao.find(Person.class, submitterId);

            Iterator<Resource> iterator = resources.getResources().iterator();
            while (iterator.hasNext()) {
                try {
                    Resource resource = iterator.next();
                    genericDao.refresh(resource);
                } catch (Exception e) {
                    logger.error("could not bring resource onto session: {} ", e);
                }
            }

            for (Resource resource : resources.getResources()) {
                receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
                String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]",
                        resource.getResourceType(), submitter, resource.getId(), StringUtils.left(resource.getTitle(), 100));

                try {
                    resourceService.logResourceModification(resource, resource.getSubmitter(), logMessage, RevisionLogType.CREATE);
                    genericDao.saveOrUpdate(resource);
                } catch (TdarRecoverableRuntimeException trex) {
                    receiver.addError(trex);
                    receiver.setCompleted();
                    throw trex;
                }
            }
            logger.info("bulk: completing");
        } catch (Throwable t) {
            logger.error("log and persist error happened", t);
            receiver.addError(t);
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