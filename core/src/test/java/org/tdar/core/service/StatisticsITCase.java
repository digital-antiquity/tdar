package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.processes.daily.DailyStatisticsUpdate;
import org.tdar.core.service.processes.weekly.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.resource.ResourceService;

public class StatisticsITCase extends AbstractIntegrationTestCase {

    @Autowired
    private WeeklyStatisticsLoggingProcess processingTask;

    @Autowired
    private DailyStatisticsUpdate dailyTask;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private EntityService entityService;

    
    @Test
    public void testContributorStats() {
        Set<Long> findAllContributorIds = entityService.findAllContributorIds();
        logger.debug("{}", findAllContributorIds);
        assertNotEmpty(findAllContributorIds);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testBasicStats() {
        Document document = new Document();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        document.markUpdated(getAdminUser());
        genericService.saveOrUpdate(document);
        genericService.saveOrUpdate(new ResourceAccessStatistic(new Date(), document, false));
        Date date = DateTime.now().minusDays(1).toDate();
        genericService.saveOrUpdate(new ResourceAccessStatistic(date, document,true));
        genericService.synchronize();
        Number count = datasetService.getDao().getAccessCount(document);
        assertEquals(1l, count.longValue());
        dailyTask.execute();
        genericService.synchronize();
        count = datasetService.getDao().getAccessCount(document);
        assertEquals(2l, count.longValue());
        List<AggregateViewStatistic> aggregateUsageStats = resourceService.getAggregateUsageStats(DateGranularity.DAY, DateTime.now().minusDays(5).toDate(),
                DateTime.now().toDate(), 1L);
        assertEquals(1, aggregateUsageStats.size());

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
        Number gis = resourceService.countActiveResources(ResourceType.GEOSPATIAL);
        Number people = entityService.findAllRegisteredUsers().size();
        createAndSaveNewInformationResource(Document.class);
        createAndSaveNewInformationResource(Dataset.class);
        createAndSaveNewInformationResource(Image.class);
        createAndSaveNewInformationResource(CodingSheet.class);
        createAndSaveNewInformationResource(Ontology.class);
        createAndSaveNewInformationResource(Geospatial.class);
        createAndSaveNewInformationResource(SensoryData.class, createAndSaveNewPerson());
        generateDocumentWithFileAndUseDefaultUser();
        processingTask.execute();
        genericService.synchronize();

        // flush();
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
        assertEquals(gis.longValue() + 1, map.get(StatisticType.NUM_GIS).getValue().longValue());
        assertEquals(docs.longValue() + 2, map.get(StatisticType.NUM_DOCUMENT).getValue().longValue());
        assertEquals(images.longValue() + 1, map.get(StatisticType.NUM_IMAGE).getValue().longValue());
        assertEquals(sheets.longValue() + 1, map.get(StatisticType.NUM_CODING_SHEET).getValue().longValue());
        assertEquals(sensory.longValue() + 1, map.get(StatisticType.NUM_SENSORY_DATA).getValue().longValue());
        assertEquals(ontologies.longValue() + 1, map.get(StatisticType.NUM_ONTOLOGY).getValue().longValue());
        assertEquals(people.longValue() + 1, map.get(StatisticType.NUM_USERS).getValue().longValue());
        assertFalse(map.get(StatisticType.REPOSITORY_SIZE).getValue().longValue() == 0);
        genericService.synchronize();
    }
}
