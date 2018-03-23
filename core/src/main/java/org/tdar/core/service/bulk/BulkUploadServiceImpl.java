package org.tdar.core.service.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ActivityManager;
import org.tdar.core.service.AsynchronousProcessManager;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.filestore.FileAnalyzer;
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
public class BulkUploadServiceImpl implements BulkUploadService {

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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.bulk.BulkUploadService#saveAsync(org.tdar.core.bean.resource.InformationResource, java.lang.Long, java.lang.Long,
     * java.util.Collection, java.lang.Long)
     */
    @Override
    @Async
    public void saveAsync(final InformationResource image, final Long submitterId, final Long ticketId,
            final Collection<FileProxy> fileProxies, Long accountId) {
        save(image, submitterId, ticketId, fileProxies, accountId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.bulk.BulkUploadService#save(org.tdar.core.bean.resource.InformationResource, java.lang.Long, java.lang.Long,
     * java.util.Collection, java.lang.Long)
     */
    @Override
    @Transactional
    public void save(final InformationResource resourceTemplate_, final Long submitterId, final Long ticketId,
            final Collection<FileProxy> fileProxies, final Long accountId) {

        if (CollectionUtils.isEmpty(fileProxies)) {
            TdarRecoverableRuntimeException throwable = new TdarRecoverableRuntimeException("bulkUploadService.the_system_has_not_received_any_files");
            throw throwable;
        }

        genericDao.clearCurrentSession();
        BulkUpdateReceiver asyncUpdateReceiver = new BulkUpdateReceiver(ticketId.toString());
        AsynchronousProcessManager.getInstance().addActivityToQueue(asyncUpdateReceiver);
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

            logger.info("bulk: processing files, and then persisting");
            processFileProxiesIntoResources(fileProxies, resourceTemplate, submitter, asyncUpdateReceiver, resources);
            updateAccountQuotas(accountId, resources, asyncUpdateReceiver, submitter);

            ResourceCollection collection = logAndPersist(asyncUpdateReceiver, resources, submitterId, accountId);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.bulk.BulkUploadService#processFileProxiesIntoResources(java.util.Collection, org.tdar.core.bean.resource.InformationResource,
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.AsyncUpdateReceiver, java.util.List)
     */
    @Override
    @Transactional
    public void processFileProxiesIntoResources(Collection<FileProxy> fileProxies, InformationResource image, TdarUser authenticatedUser,
            AsyncUpdateReceiver asyncUpdateReceiver, List<Resource> resources) {
        int count = 0;
        image.setSubmitter(authenticatedUser);
        Map<String, FileProxy> map = new HashMap<>();
        Map<String, InformationResource> rMap = new HashMap<>();
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
                InformationResource informationResource = (InformationResource) resourceService.createResourceFrom(authenticatedUser, image,
                        suggestTypeForFile.getResourceClass(), true);
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
        String title = "Bulk Upload:" + DateTime.now().toString(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss"));
        ResourceCollection collection = new ResourceCollection(title, title, submitter);
        try {
            collection.markUpdated(submitter);
            collection.setSystemManaged(true);
            genericDao.saveOrUpdate(collection);

            for (Resource resource : resources) {
                receiver.update(receiver.getPercentComplete(), String.format("saving %s", resource.getTitle()));
                String logMessage = String.format("%s edited and saved by %s:\ttdar id:%s\ttitle:[%s]",
                        resource.getResourceType(), submitter, resource.getId(), StringUtils.left(resource.getTitle(), 100));

                try {
                    collection.getManagedResources().add(resource);
                    resource.getManagedResourceCollections().add(collection);
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
        activity.end();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.bulk.BulkUploadService#getResourceTypesSupportingBulkUpload()
     */
    @Override
    public ResourceType[] getResourceTypesSupportingBulkUpload() {
        return ResourceType.getTypesSupportingBulkUpload();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.bulk.BulkUploadService#checkAsyncStatus(java.lang.Long)
     */
    @Override
    public BulkUpdateReceiver checkAsyncStatus(Long ticketId) {
        return (BulkUpdateReceiver) AsynchronousProcessManager.getInstance().findActivity(BulkUpdateReceiver.BULK_UPLOAD + ticketId.toString());
    }

}
