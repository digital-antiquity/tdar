package org.tdar.core.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.TestBillingAccountHelper;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.service.email.AwsEmailSender;
import org.tdar.core.service.email.MockAwsEmailSenderServiceImpl;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.core.service.processes.daily.DailyTimedAccessRevokingProcess;
import org.tdar.core.service.processes.daily.EmbargoedFilesUpdateProcess;
import org.tdar.core.service.processes.daily.OverdrawnAccountUpdate;
import org.tdar.core.service.processes.daily.SalesforceSyncProcess;
import org.tdar.core.service.processes.weekly.WeeklyFilestoreLoggingProcess;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class ScheduledProcessITCase extends AbstractIntegrationTestCase implements TestBillingAccountHelper {

    @Autowired
    // private ScheduledProcessService scheduledProcessService;
    private static final int MOCK_NUMBER_OF_IDS = 2000;

    @Autowired
    private AwsEmailSender awsEmailService;

    private class MockScheduledProcess extends AbstractScheduledBatchProcess<Dataset> {

        private static final long serialVersionUID = -3861909608332571409L;

        @Override
        public String getDisplayName() {
            return "Mock scheduled dataset process";
        }

        @Override
        public Class<Dataset> getPersistentClass() {
            return Dataset.class;
        }

        @Override
        public List<Long> findAllIds() {
            return new LinkedList<Long>(Collections.nCopies(MOCK_NUMBER_OF_IDS, Long.valueOf(37)));
        }

        @Override
        public void processBatch(List<Long> batch) {
            // FIXME: this is dependent on TdarConfiguration's batch size being an even multiple of MOCK_NUMBER_OF_IDS
            assertEquals(batch.size(), getTdarConfiguration().getScheduledProcessBatchSize());
        }

        @Override
        public void process(Dataset persistable) {
            fail("this should not be invoked");
        }
    }

    @Autowired
    ScheduledProcessService scheduledProcessService;

    @Test
    @Rollback
    public void testDailyEmailProcess() {
        TdarUser user1 = new TdarUser();
        user1.setEmail("a@badfdsf.com");
        user1.setUsername(user1.getEmail());
        user1.setFirstName("first");
        user1.setLastName("last");
        user1.setDateUpdated(new Date());
        user1.setAffiliation(UserAffiliation.GENERAL_PUBLIC);
        user1.setContributorReason("I really like contributing things.  What is that a crime or something?");
        genericService.saveOrUpdate(user1);

        TdarUser user2 = new TdarUser();
        user2.setEmail("2@testuser.com");
        user2.setUsername(user2.getEmail());
        user2.setFirstName("first");
        user2.setLastName("last");
        user2.setDateUpdated(new Date());
        user2.setAffiliation(UserAffiliation.GENERAL_PUBLIC);
        user2.setContributorReason(" ");
        genericService.saveOrUpdate(user2);

        TdarUser user3 = new TdarUser();
        user3.setEmail("3@testuser.com");
        user3.setUsername(user3.getEmail());
        user3.setFirstName("first");
        user3.setLastName("last");
        user3.setDateUpdated(new Date());
        user3.setAffiliation(UserAffiliation.GENERAL_PUBLIC);
        user3.setContributorReason(null);
        genericService.saveOrUpdate(user3);

        // fixme: I'm not sure why this works like it works (w/ seemingly duplicated calls), but it's required for checkMailAndGetLatest() to work
        scheduledProcessService.queue(DailyEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        // assertTrue(dailyEmailProcess.isCompleted());
        scheduledProcessService.queue(SendEmailProcess.class);

        scheduledProcessService.runNextScheduledProcessesInQueue();
        // assertTrue(dailyEmailProcess.isCompleted());
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        logger.debug("//");
        scheduledProcessService.runNextScheduledProcessesInQueue();

        Email message = checkMailAndGetLatest("The following users registered with");
        assertThat(message, is(not(nullValue())));
        assertTrue(message.getMessage().contains("contributor reason: None"));
        // assertTrue(dailyEmailProcess.isCompleted());
    }

    @Autowired
    SendEmailProcess sep;

    @Test
    @Rollback
    public void testVerifyProcess() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        Document document = createAndSaveDocumentWithFileAndUseDefaultUser();
        scheduledProcessService.queue(WeeklyFilestoreLoggingProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        Number totalFiles = genericService.count(InformationResourceFileVersion.class);
        setupQueue(SendEmailProcess.class, sep);
        scheduledProcessService.queue(SendEmailProcess.class);
        int count = 0;
        while (!scheduledProcessService.getScheduledProcessQueue().isEmpty() && count < 100) {
            scheduledProcessService.runNextScheduledProcessesInQueue();
            count++;
        }
        ;
        Email received = checkMailAndGetLatest("reporting on files with issues");
        assertTrue(received.getSubject().contains(WeeklyFilestoreLoggingProcess.PROBLEM_FILES_REPORT));
        assertTrue(received.getMessage().contains("not found"));
        assertTrue("should find " + totalFiles.intValue(), received.getMessage().contains("Total Files:</b> " + totalFiles.intValue()));
        assertFalse(received.getMessage().contains(document.getInformationResourceFiles().iterator().next().getFilename()));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo(), getTdarConfiguration().getSystemAdminEmail());
    }

    


    @Autowired
    EmbargoedFilesUpdateProcess efup;

    @Test
    @Rollback
    public void testEmbargo() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        // queue the embargo task
        Document doc = generateDocumentWithFileAndUser();
        long id = doc.getId();

        setupQueue(EmbargoedFilesUpdateProcess.class, efup);

        InformationResourceFile irf = doc.getFirstInformationResourceFile();
        irf.setRestriction(FileAccessRestriction.EMBARGOED_SIX_MONTHS);
        irf.setDateMadePublic(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(irf);
        irf = null;
        doc = null;
        scheduledProcessService.queue(EmbargoedFilesUpdateProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        // queue the email task
        doc = genericService.find(Document.class, id);
        assertTrue(doc.getFirstInformationResourceFile().getRestriction() == FileAccessRestriction.PUBLIC);

        // FIXME: add tests for checking email
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

        doc.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        doc.getFirstInformationResourceFile().setDateMadePublic(DateTime.now().toDate());
        genericService.saveOrUpdate(doc.getFirstInformationResourceFile());
        scheduledProcessService.queue(EmbargoedFilesUpdateProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        // queue the email task
        doc = genericService.find(Document.class, id);
        assertTrue(doc.getFirstInformationResourceFile().getRestriction() == FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

    }

    private void setupQueue(Class<? extends ScheduledProcess> cls, ScheduledProcess proc) {
        scheduledProcessService.getManager().reset();
        scheduledProcessService.getManager().addProcess(cls);
    }

    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.CREDIT_CARD })
    public void testAccountEmail() {
        BillingAccount account = setupAccountForPerson(getBasicUser());
        account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
        account.markUpdated(getAdminUser());
        account.setLastModified(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(account);

        scheduledProcessService.queue(OverdrawnAccountUpdate.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        List<Email> messages = ((MockAwsEmailSenderServiceImpl) awsEmailService).getMessages();
        logger.debug("Messages are {}", messages);
        Email received = messages.get(0);
        assertTrue(received.getSubject().contains(OverdrawnAccountUpdate.SUBJECT));
        assertTrue(received.getMessage().contains("Flagged Items"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo(), getTdarConfiguration().getSystemAdminEmail());
        assertNotNull(messages.get(1));
    }

    @Test
    public void testCleanup() throws Exception {
        MockScheduledProcess mock = new MockScheduledProcess();
        do {
            mock.execute();
        } while (!mock.isCompleted());
        assertNotNull(mock.getLastId());
        assertTrue(mock.getNextBatch().isEmpty());
        assertTrue(mock.getBatchIdQueue().isEmpty());
        mock.cleanup();
        assertFalse("ScheduledBatchProcess should be reset now", mock.isCompleted());
    }

    @Test
    public void testBatchProcessing() {
        MockScheduledProcess mock = new MockScheduledProcess();
        List<Long> batch = mock.getNextBatch();
        assertEquals(MOCK_NUMBER_OF_IDS, mock.getBatchIdQueue().size() + batch.size());
        int numberOfRuns = MOCK_NUMBER_OF_IDS / getTdarConfiguration().getScheduledProcessBatchSize();
        assertNotSame(numberOfRuns, 0);
        while (CollectionUtils.isNotEmpty(mock.getBatchIdQueue())) {
            int initialSize = mock.getBatchIdQueue().size();
            batch = mock.getNextBatch();
            assertEquals(initialSize, mock.getBatchIdQueue().size() + batch.size());
            numberOfRuns--;
            if (numberOfRuns < 0) {
                fail("MockScheduledProcess should have been batched " + numberOfRuns + " times but didn't.");
            }
        }
        assertEquals("id queue should be empty", 0, mock.getBatchIdQueue().size());
        assertSame("number of runs should be 1 now", 1, numberOfRuns);

    }

    OccurranceStatisticsUpdateProcess ocur;

    @Test
    @Rollback(true)
    public void testOccurranceStats() throws InstantiationException, IllegalAccessException {
        scheduledProcessService.queue(OccurranceStatisticsUpdateProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

    }

    @Test
    @Ignore("useful for testing")
    public void testSalesforce() {
        createAndSaveNewPerson("test-user@tdar.org", "-tdar2");
        scheduledProcessService.queue(SalesforceSyncProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
    }

    @Test
    @Rollback(false)
    public void testDailyTimedAccessRevokingProcess() {
        Dataset dataset = createAndSaveNewDataset();
        ResourceCollection collection = createSharedCollection(DateTime.now().plusDays(1).toDate(), dataset);
        final Long cid = collection.getId();
        Date expires = DateTime.now().minusDays(2).toDate();
        ResourceCollection expired = createSharedCollection(expires, dataset);
        final Long eid = expired.getId();
        // genericService.saveOrUpdate(e)
        // dataset.getResourceCollections().add(collection);

        final int aus = expired.getAuthorizedUsers().size();
        collection = null;
        expired = null;
        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                scheduledProcessService.queue(DailyTimedAccessRevokingProcess.class);
                scheduledProcessService.runNextScheduledProcessesInQueue();
                // dtarp.execute();
                // dtarp.cleanup();
                ResourceCollection rcn = genericService.find(ResourceCollection.class, cid);
                logger.debug("{}", rcn);
                logger.debug("au: {}", rcn.getAuthorizedUsers());
                assertEquals(aus, rcn.getAuthorizedUsers().size());

                ResourceCollection rce = genericService.find(ResourceCollection.class, eid);
                logger.debug("{}", rce);
                logger.debug("au: {}", rce.getAuthorizedUsers());
                assertEquals(aus - 1, rce.getAuthorizedUsers().size());
                rce.setStatus(Status.DELETED);
                rcn.setStatus(Status.DELETED);
                genericService.saveOrUpdate(rcn);
                genericService.saveOrUpdate(rce);
                return null;
            }
        });
    }

    

    @Test
    @Rollback(false)
    public void testDailyTimedAccessRevokingProcessForResource() {
        Dataset dataset = createAndSaveNewDataset();
        Date expires = DateTime.now().minusDays(2).toDate();
        TdarUser user = createAndSaveNewUser();
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), user, Permissions.VIEW_ALL);
        authorizedUser.setDateExpires(expires);
        dataset.getAuthorizedUsers().add(authorizedUser);
        genericService.saveOrUpdate(dataset);
        genericService.saveOrUpdate(authorizedUser);
        final Long eid = dataset.getId();
        Long userId = user.getId();
        user = null;
        // genericService.saveOrUpdate(e)
        // dataset.getResourceCollections().add(collection);
        authorizedUser = null;
        final int aus = dataset.getAuthorizedUsers().size();
        dataset = null;
        setVerifyTransactionCallback(new TransactionCallback<Image>() {
            @Override
            public Image doInTransaction(TransactionStatus status) {
                scheduledProcessService.queue(DailyTimedAccessRevokingProcess.class);
                scheduledProcessService.runNextScheduledProcessesInQueue();
                // dtarp.execute();
                // dtarp.cleanup();
                Dataset ds = genericService.find(Dataset.class, eid);
                logger.debug("{}", ds);
                logger.debug("au: {}", ds.getAuthorizedUsers());
//                assertEquals(aus, ds.getAuthorizedUsers().size());
                for (AuthorizedUser au : ds.getAuthorizedUsers()) {
                    assertNotEquals(userId, au.getUser().getId());
                }
                assertEquals(aus - 1, ds.getAuthorizedUsers().size());
                ds.setStatus(Status.DELETED);
                genericService.saveOrUpdate(ds);
                genericService.delete(genericService.find(TdarUser.class, userId));
                return null;
            }
        });
    }

    private ResourceCollection createSharedCollection(Date date, Dataset dataset) {
        ResourceCollection collection = new ResourceCollection();
        collection.getManagedResources().add(dataset);
        dataset.getManagedResourceCollections().add(collection);
        collection.markUpdated(getAdminUser());
        collection.setName("test " + date);
        collection.setDescription("test");
        collection.markUpdated(getAdminUser());
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), getBasicUser(), Permissions.VIEW_ALL);

        authorizedUser.setDateExpires(date);
        collection.getAuthorizedUsers().add(authorizedUser);
        collection.getManagedResources().add(dataset);
        // dataset.getSharedCollections().add(collection);
        genericService.saveOrUpdate(collection);
        genericService.saveOrUpdate(authorizedUser);
        // genericService.saveOrUpdate(dataset);
        return collection;
    }

}
