package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.MockMailSender;
import org.tdar.core.service.processes.CreatorAnalysisProcess;
import org.tdar.core.service.processes.DailyEmailProcess;
import org.tdar.core.service.processes.EmbargoedFilesUpdateProcess;
import org.tdar.core.service.processes.LegacyObfuscateLatLongProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.OverdrawnAccountUpdate;
import org.tdar.core.service.processes.RebuildHomepageCache;
import org.tdar.core.service.processes.ScheduledBatchProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.WeeklyFilestoreLoggingProcess;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class ScheduledProcessITCase extends AbstractIntegrationTestCase {

    @Autowired
    // private ScheduledProcessService scheduledProcessService;
    private static final int MOCK_NUMBER_OF_IDS = 2000;

    @Autowired
    RebuildHomepageCache homepage;

    @Autowired
    private SendEmailProcess sendEmailProcess;
    @Autowired
    private DailyEmailProcess dailyEmailProcess;

    @Autowired
    private CreatorAnalysisProcess pap;

    @Autowired
    private LegacyObfuscateLatLongProcess llbprocess;

    private class MockScheduledProcess extends ScheduledBatchProcess<Dataset> {

        private static final long serialVersionUID = 1L;

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
    OverdrawnAccountUpdate oau;

    @Autowired
    WeeklyFilestoreLoggingProcess fsp;

    @Autowired
    ScheduledProcessService scheduledProcessService;

    @Test
    @Rollback
    public void testOptimize() {
        searchIndexService.optimizeAll();
    }

    @Test
    @Rollback
    public void testDailyEmailProcess() {
        TdarUser user = new TdarUser();
        user.setEmail("a@badfdsf.com");
        user.setUsername(user.getEmail());
        user.setFirstName("first");
        user.setLastName("last");
        user.setDateUpdated(new Date());
        genericService.saveOrUpdate(user);

        dailyEmailProcess.execute();

    }

    @Test
    @Rollback
    public void testVerifyProcess() throws InstantiationException, IllegalAccessException {
        scheduledProcessService.getScheduledProcessQueue().clear();
        Document document = generateDocumentWithFileAndUseDefaultUser();
        fsp.execute();
        scheduledProcessService.queueTask(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        SimpleMailMessage received = ((MockMailSender) emailService.getMailSender()).getMessages().get(0);
        assertTrue(received.getSubject().contains(WeeklyFilestoreLoggingProcess.PROBLEM_FILES_REPORT));
        assertTrue(received.getText().contains("not found"));
        assertFalse(received.getText().contains(document.getInformationResourceFiles().iterator().next().getFilename()));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], getTdarConfiguration().getSystemAdminEmail());
    }

    @Test
    @Rollback
    public void testEmbargo() throws InstantiationException, IllegalAccessException {
        // queue the embargo task
        Document doc = generateDocumentWithFileAndUser();
        long id = doc.getId();

        InformationResourceFile irf = doc.getFirstInformationResourceFile();
        irf.setRestriction(FileAccessRestriction.EMBARGOED_SIX_MONTHS);
        irf.setDateMadePublic(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(irf);
        irf = null;
        doc = null;
        scheduledProcessService.queueTask(EmbargoedFilesUpdateProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        // queue the email task
        doc = genericService.find(Document.class, id);
        assertTrue(doc.getFirstInformationResourceFile().getRestriction() == FileAccessRestriction.PUBLIC);

        // FIXME: add tests for checking email
        scheduledProcessService.queueTask(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

        doc.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        doc.getFirstInformationResourceFile().setDateMadePublic(DateTime.now().toDate());
        genericService.saveOrUpdate(doc.getFirstInformationResourceFile());
        scheduledProcessService.queueTask(EmbargoedFilesUpdateProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        // queue the email task
        doc = genericService.find(Document.class, id);
        assertTrue(doc.getFirstInformationResourceFile().getRestriction() == FileAccessRestriction.EMBARGOED_FIVE_YEARS);
        scheduledProcessService.queueTask(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();

    }

    @Test
    @Rollback
    public void testAccountEmail() {
        BillingAccount account = setupAccountForPerson(getBasicUser());
        account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
        genericService.saveOrUpdate(account);
        oau.execute();
        sendEmailProcess.execute();
        SimpleMailMessage received = ((MockMailSender) emailService.getMailSender()).getMessages().get(0);
        assertTrue(received.getSubject().contains(OverdrawnAccountUpdate.SUBJECT));
        assertTrue(received.getText().contains("Flagged Items"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], getTdarConfiguration().getSystemAdminEmail());
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
    public void testHomepageGen() {
        homepage.execute();
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

    @Test
    @Rollback
    public void testLLB() {
        while (CollectionUtils.isNotEmpty(llbprocess.getNextBatch())) {
            llbprocess.execute();
        }
    }

    @Test
    @Rollback(true)
    public void testPersonAnalytics() throws InstantiationException, IllegalAccessException {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class, Person.class, Institution.class, ResourceCollection.class);
        pap.setDaysToRun(3000);
        pap.execute();
        pap.cleanup();
        pap.setAllIds(null);
        // resetting
        pap.execute();
    }

    @Autowired
    OccurranceStatisticsUpdateProcess ocur;

    @Test
    @Rollback(true)
    public void testOccurranceStats() throws InstantiationException, IllegalAccessException {
        ocur.execute();
    }

}
