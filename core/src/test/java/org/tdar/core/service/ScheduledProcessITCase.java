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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.InformationResourceFileVersionProxy;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.external.MockMailSender;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.ScheduledProcess;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.processes.daily.DailyEmailProcess;
import org.tdar.core.service.processes.daily.EmbargoedFilesUpdateProcess;
import org.tdar.core.service.processes.daily.OverdrawnAccountUpdate;
import org.tdar.core.service.processes.daily.SalesforceSyncProcess;
import org.tdar.core.service.processes.weekly.FilestoreVerificationTask;
import org.tdar.core.service.processes.weekly.WeeklyFilestoreLoggingProcess;
import org.tdar.core.service.resource.InformationResourceFileService;
import org.tdar.filestore.FileStoreFileProxy;

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
    private SendEmailProcess sendEmailProcess;
    @Autowired
    private DailyEmailProcess dailyEmailProcess;

    @Autowired
    private SalesforceSyncProcess salesforce;

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
    OverdrawnAccountUpdate oau;

    @Autowired
    WeeklyFilestoreLoggingProcess fsp;

    @Autowired
    ScheduledProcessService scheduledProcessService;


    @Test
    @Rollback
    public void testDailyEmailProcess() {
        TdarUser user = new TdarUser();
        user.setEmail("a@badfdsf.com");
        user.setUsername(user.getEmail());
        user.setFirstName("first");
        user.setLastName("last");
        user.setDateUpdated(new Date());
        user.setAffiliation(UserAffiliation.GENERAL_PUBLIC);
        user.setContributorReason("I really like contributing things.  What is that a crime or something?");
        genericService.saveOrUpdate(user);

        // fixme: I'm not sure why this works like it works (w/ seemingly duplicated calls), but it's required for checkMailAndGetLatest() to work
        scheduledProcessService.queue(DailyEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        assertTrue(dailyEmailProcess.isCompleted());
        scheduledProcessService.queue(SendEmailProcess.class);

        scheduledProcessService.runNextScheduledProcessesInQueue();
//        assertTrue(dailyEmailProcess.isCompleted());
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        scheduledProcessService.queue(SendEmailProcess.class);
        scheduledProcessService.runNextScheduledProcessesInQueue();
        logger.debug("//");
        scheduledProcessService.runNextScheduledProcessesInQueue();

        SimpleMailMessage message = checkMailAndGetLatest("The following users registered with");
        assertThat(message, is( not( nullValue())));
//        assertTrue(dailyEmailProcess.isCompleted());
    }
        
    @Autowired
    SendEmailProcess sep;

    @Test
    @Rollback
    public void testVerifyProcess() throws InstantiationException, IllegalAccessException {
        Document document = generateDocumentWithFileAndUseDefaultUser();
        fsp.execute();
        setupQueue(SendEmailProcess.class, sep);
        scheduledProcessService.queue(SendEmailProcess.class);
        int count = 0;
        while (!scheduledProcessService.getScheduledProcessQueue().isEmpty() && count < 100) {
        	scheduledProcessService.runNextScheduledProcessesInQueue();
        	count++;
        };
        SimpleMailMessage received = checkMailAndGetLatest("reporting on files with issues");
        assertTrue(received.getSubject().contains(WeeklyFilestoreLoggingProcess.PROBLEM_FILES_REPORT));
        assertTrue(received.getText().contains("not found"));
        assertFalse(received.getText().contains(document.getInformationResourceFiles().iterator().next().getFilename()));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], getTdarConfiguration().getSystemAdminEmail());
    }

    @Autowired
    EmbargoedFilesUpdateProcess efup;
    
    @Test
    @Rollback
    public void testEmbargo() throws InstantiationException, IllegalAccessException {
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
    public void testAccountEmail() {
        BillingAccount account = setupAccountForPerson(getBasicUser());
        account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
        account.markUpdated(getAdminUser());
        account.setLastModified(DateTime.now().minusDays(1).toDate());
        genericService.saveOrUpdate(account);
        oau.execute();
        sendEmailProcess.execute();
        ArrayList<SimpleMailMessage> messages = ((MockMailSender) emailService.getMailSender()).getMessages();
        SimpleMailMessage received = messages.get(0);
        assertTrue(received.getSubject().contains(OverdrawnAccountUpdate.SUBJECT));
        assertTrue(received.getText().contains("Flagged Items"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], getTdarConfiguration().getSystemAdminEmail());
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

    
    @Test
    @Rollback(true)
    public void testAccountUsageHistory() {
        scheduledProcessService.cronUpdateAccountUsageHistory();
    }

    @Autowired
    OccurranceStatisticsUpdateProcess ocur;

    @Test
    @Rollback(true)
    public void testOccurranceStats() throws InstantiationException, IllegalAccessException {
        ocur.execute();
    }
    
    @Test
    @Ignore("useful for testing")
    public void testSalesforce() {
        if (salesforce.isEnabled()) {
            createAndSaveNewPerson("test-user@tdar.org", "-tdar2");
            salesforce.execute();
        }
    }
    
}
