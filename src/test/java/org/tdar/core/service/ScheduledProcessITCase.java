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
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.util.ScheduledProcess;
import org.tdar.core.bean.util.Statistic;
import org.tdar.core.bean.util.Statistic.StatisticType;

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
    
    private class MockScheduledProcess extends ScheduledProcess.Base<Dataset> {

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
        public List<Long> getPersistableIdQueue() {
            return new LinkedList<Long>(Collections.nCopies(MOCK_NUMBER_OF_IDS, Long.valueOf(37)));
        }

        @Override
        public void processBatch(List<Long> batch) throws Exception {
            // FIXME: this is dependent on TdarConfiguration's batch size being an even multiple of MOCK_NUMBER_OF_IDS
            assertEquals(batch.size(), getTdarConfiguration().getScheduledProcessBatchSize());
        }
        @Override
        public void process(Dataset persistable) {
            fail("this should not be invoked");
        }
    }
    
    @Test
    @Rollback
    public void testGetNextBatch() {
        ScheduledProcess<Dataset> mock = new MockScheduledProcess();
        List<Long> batch = scheduledProcessService.getNextBatch(mock);
        assertEquals(MOCK_NUMBER_OF_IDS, scheduledProcessService.getPersistableIdQueue().size() + batch.size());
        int numberOfRuns = MOCK_NUMBER_OF_IDS / getTdarConfiguration().getScheduledProcessBatchSize();
        assertNotSame(numberOfRuns, 0);
        while (CollectionUtils.isNotEmpty(scheduledProcessService.getPersistableIdQueue())) {
            int initialSize = scheduledProcessService.getPersistableIdQueue().size();
            batch = scheduledProcessService.getNextBatch(mock);
            assertEquals(initialSize, scheduledProcessService.getPersistableIdQueue().size() + batch.size());
            numberOfRuns--;
            if (numberOfRuns < 0) {
                fail("MockScheduledProcess should have been batched " + numberOfRuns + " times but didn't.");
            }
        }
        assertEquals("id queue should be empty", 0, scheduledProcessService.getPersistableIdQueue().size());
        assertSame("number of runs should be 1 now", 1, numberOfRuns);
        
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testStats() throws InstantiationException, IllegalAccessException {
        Number docs = resourceService.countActiveResources(Document.class);
        Number datasets = resourceService.countActiveResources(Dataset.class);
        Number images = resourceService.countActiveResources(Image.class);
        Number sheets = resourceService.countActiveResources(CodingSheet.class);
        Number ontologies = resourceService.countActiveResources(Ontology.class);
        Number sensory = resourceService.countActiveResources(SensoryData.class);
        Number people = entityService.findAllRegisteredUsers(null).size();
        createAndSaveNewInformationResource(Document.class, false);
        createAndSaveNewInformationResource(Dataset.class, false);
        createAndSaveNewInformationResource(Image.class, false);
        createAndSaveNewInformationResource(CodingSheet.class, false);
        createAndSaveNewInformationResource(Ontology.class, false);
        createAndSaveNewInformationResource(SensoryData.class,true);

        scheduledProcessService.generateWeeklyStats();
        flush();
        List<Statistic> allStats = genericService.findAll(Statistic.class);
        Map<Statistic.StatisticType, Statistic> map = new HashMap<Statistic.StatisticType, Statistic>();
        for (Statistic stat : allStats) {
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
        assertEquals(docs.longValue() + 1, map.get(StatisticType.NUM_DOCUMENT).getValue().longValue());
        assertEquals(images.longValue() + 1, map.get(StatisticType.NUM_IMAGE).getValue().longValue());
        assertEquals(sheets.longValue() + 1, map.get(StatisticType.NUM_CODING_SHEET).getValue().longValue());
        assertEquals(sensory.longValue() + 1, map.get(StatisticType.NUM_SENSORY_DATA).getValue().longValue());
        assertEquals(ontologies.longValue() + 1, map.get(StatisticType.NUM_ONTOLOGY).getValue().longValue());
        assertEquals(people.longValue() + 1, map.get(StatisticType.NUM_USERS).getValue().longValue());
    }
}
