package org.tdar.core.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.processes.FilestoreWeeklyLoggingProcess;
import org.tdar.core.service.processes.OccurranceStatisticsUpdateProcess;
import org.tdar.core.service.processes.OverdrawnAccountUpdate;
import org.tdar.core.service.processes.PersonAnalysisProcess;
import org.tdar.core.service.processes.SitemapGeneratorProcess;
import org.tdar.core.service.processes.WeeklyStatisticsLoggingProcess;

import static org.junit.Assert.*;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class ScheduledProcessITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ScheduledProcessService scheduledProcessService;
    private static final int MOCK_NUMBER_OF_IDS = 2000;

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
    FilestoreWeeklyLoggingProcess fsp;

    @Test
    public void testVerifyProcess() {
        fsp.execute();
        SimpleMailMessage received = mockMailSender.getMessages().get(0);
        assertTrue(received.getSubject().contains(FilestoreWeeklyLoggingProcess.PROBLEM_FILES_REPORT));
        assertTrue(received.getText().contains("not found"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], getTdarConfiguration().getSystemAdminEmail());
    }

    @Test
    @Rollback
    public void testAccountEmail() {
        Account account = setupAccountForPerson(getBasicUser());
        account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
        genericService.saveOrUpdate(account);
        oau.execute();
        SimpleMailMessage received = mockMailSender.getMessages().get(0);
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

    @Autowired
    private SitemapGeneratorProcess sitemap;

    @Test
    public void testSitemapGen() {
        sitemap.execute();
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

    @Autowired
    WeeklyStatisticsLoggingProcess processingTask;

    
    @Autowired
    PersonAnalysisProcess pap;
    
    @Test
    @Rollback(true)
    public void testPersonAnalytics() throws InstantiationException, IllegalAccessException {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class, Person.class, Institution.class, ResourceCollection.class);

        pap.execute();
    }

    @Autowired
    OccurranceStatisticsUpdateProcess ocur;
    
    @Test
    @Rollback(true)
    public void testOccurranceStats() throws InstantiationException, IllegalAccessException {
        ocur.execute();
    }

    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testStats() throws InstantiationException, IllegalAccessException {
        Number docs = resourceService.countActiveResources(ResourceType.DOCUMENT);
        Number datasets = resourceService.countActiveResources(ResourceType.DATASET);
        Number images = resourceService.countActiveResources(ResourceType.IMAGE);
        Number sheets = resourceService.countActiveResources(ResourceType.CODING_SHEET);
        Number ontologies = resourceService.countActiveResources(ResourceType.ONTOLOGY);
        Number sensory = resourceService.countActiveResources(ResourceType.SENSORY_DATA);
        Number people = entityService.findAllRegisteredUsers(null).size();
        createAndSaveNewInformationResource(Document.class);
        createAndSaveNewInformationResource(Dataset.class);
        createAndSaveNewInformationResource(Image.class);
        createAndSaveNewInformationResource(CodingSheet.class);
        createAndSaveNewInformationResource(Ontology.class);
        createAndSaveNewInformationResource(SensoryData.class, createAndSaveNewPerson());
        InformationResource generateInformationResourceWithFile = generateDocumentWithFileAndUser();
        processingTask.execute();
        flush();
        List<AggregateStatistic> allStats = genericService.findAll(AggregateStatistic.class);
        Map<AggregateStatistic.StatisticType, AggregateStatistic> map = new HashMap<AggregateStatistic.StatisticType, AggregateStatistic>();
        for (AggregateStatistic stat : allStats) {
            logger.info(stat.getRecordedDate() + " " + stat.getValue() + " " + stat.getStatisticType());
            map.put(stat.getStatisticType(), stat);
        }
        Date current = new Date();

        Date date = map.get(StatisticType.NUM_CODING_SHEET).getRecordedDate();
        Calendar cal = new GregorianCalendar(current.getYear(), current.getMonth(), current.getDay());
        Calendar statDate = new GregorianCalendar(date.getYear(), date.getMonth(), date.getDay());
        assertEquals(cal, statDate);
        // assertEquals(11L, map.get(StatisticType.NUM_PROJECT).getValue().longValue());
        assertEquals(datasets.longValue() + 1, map.get(StatisticType.NUM_DATASET).getValue().longValue());
        assertEquals(docs.longValue() + 2, map.get(StatisticType.NUM_DOCUMENT).getValue().longValue());
        assertEquals(images.longValue() + 1, map.get(StatisticType.NUM_IMAGE).getValue().longValue());
        assertEquals(sheets.longValue() + 1, map.get(StatisticType.NUM_CODING_SHEET).getValue().longValue());
        assertEquals(sensory.longValue() + 1, map.get(StatisticType.NUM_SENSORY_DATA).getValue().longValue());
        assertEquals(ontologies.longValue() + 1, map.get(StatisticType.NUM_ONTOLOGY).getValue().longValue());
        assertEquals(people.longValue() + 1, map.get(StatisticType.NUM_USERS).getValue().longValue());
        assertFalse(map.get(StatisticType.REPOSITORY_SIZE).getValue().longValue() == 0);
    }
}
