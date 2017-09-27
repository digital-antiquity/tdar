package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Geospatial;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.bean.statistics.ResourceAccessStatistic;
import org.tdar.core.dao.AggregateStatisticsDao;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.processes.daily.DailyStatisticsUpdate;
import org.tdar.core.service.processes.weekly.WeeklyStatisticsLoggingProcess;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.MessageHelper;


public class StatisticsITCase extends AbstractIntegrationTestCase {

    @Autowired
    private WeeklyStatisticsLoggingProcess processingTask;

    @Autowired
    private DailyStatisticsUpdate dailyTask;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private StatisticService statisticService;
    @Autowired
    private AggregateStatisticsDao aggregateStatisticsDao;

    
    @Test
    public void testContributorStats() {
        Set<Long> findAllContributorIds = entityService.findAllContributorIds();
        logger.debug("{}", findAllContributorIds);
        assertNotEmpty("should have contributor ids", findAllContributorIds);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testBasicStats() {
        Document document = setupDacumentWithStats();
        // should only catch 1 day(today) beacause the rest aren't in the agg stats table yet
        Number count = datasetService.getDao().getAccessCount(document);
        assertEquals(1l, count.longValue());
        dailyTask.execute();
        genericService.synchronize();
        count = datasetService.getDao().getAccessCount(document);
        assertTrue(2L <= count.longValue());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testResourceUsageStatsPage() throws IOException {
        Document document = setupDacumentWithStats();
        // should only catch 1 day(today) beacause the rest aren't in the agg stats table yet
        Number count = datasetService.getDao().getAccessCount(document);
        assertEquals(1l, count.longValue());
        dailyTask.execute();
        genericService.synchronize();
        ResourceStatisticsObject usageStatsForResource = statisticService.getUsageStatsObjectForResource(MessageHelper.getInstance(), document);
         logger.debug("{} {}", StringUtils.join(usageStatsForResource));
        //        assertEquals(3L, usageStatsForResource.getUsageStatsForResource().get(0).getTotal().longValue());

    }

    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testMonthlyInitialization() {
        DateTime withDayOfMonth = DateTime.now().plusMonths(1).withDayOfMonth(1).withHourOfDay(4);
        Document document = setupDacumentWithStats();
        genericService.saveOrUpdate(new ResourceAccessStatistic(withDayOfMonth.toDate(), document, false));
        genericService.synchronize();
        aggregateStatisticsDao.createNewAggregateEntries(withDayOfMonth);
        statisticService.generateMonthlyResourceStats(withDayOfMonth);
        List<AggregateDayViewStatistic> usageStatsForResource = statisticService.getUsageStatsForResource(document);
        logger.debug("{} {}", StringUtils.join(usageStatsForResource));
        assertEquals(1L, usageStatsForResource.get(0).getTotal().longValue());
    }

    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testUsageStatsBilling() {
        Document document = setupDacumentWithStats();
        BillingAccount bas = new BillingAccount("test");
        bas.markUpdated(getAdminUser());
        bas.getResources().add(document);
        genericService.saveOrUpdate(bas);
        dailyTask.execute();
        genericService.synchronize();
        StatsResultObject statsForAccount = statisticService.getStatsForAccount(bas, MessageHelper.getInstance(), DateGranularity.DAY);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
        statsForAccount = statisticService.getStatsForAccount(bas, MessageHelper.getInstance(), DateGranularity.YEAR);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
        statsForAccount = statisticService.getStatsForAccount(bas, MessageHelper.getInstance(), DateGranularity.MONTH);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testUsageStatsCollection() {
        Document document = setupDacumentWithStats();
        SharedCollection col = new SharedCollection();
        col.setName("test");
        col.setDescription("test");
        col.markUpdated(getAdminUser());
        col.getResources().add(document);
        document.getSharedCollections().add(col);
        genericService.saveOrUpdate(col);
        genericService.saveOrUpdate(document);
        dailyTask.execute();
        genericService.synchronize();
        StatsResultObject statsForAccount = statisticService.getStatsForCollection(col, MessageHelper.getInstance(), DateGranularity.DAY);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
        statsForAccount = statisticService.getStatsForCollection(col, MessageHelper.getInstance(), DateGranularity.YEAR);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
        statsForAccount = statisticService.getStatsForCollection(col, MessageHelper.getInstance(), DateGranularity.MONTH);
        logger.debug("{} {}", StringUtils.join(statsForAccount.getTotals()), StringUtils.join(statsForAccount.getRowLabels()));
        assertTrue(statsForAccount.getTotals().contains(3L));
    }


    

    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testWeeklyPopular() {
        Document document = setupDacumentWithStats();
        dailyTask.execute();
        genericService.synchronize();
        List<Resource> resources = resourceService.getWeeklyPopularResources(10);
        logger.debug("{}", resources);
        assertFalse(CollectionUtils.isEmpty(resources));
//        assertTrue(statsForAccount.getTotals().contains(3L));
    }


    private Document setupDacumentWithStats() {
        Document document = new Document();
        document.setTitle("test");
        document.setDescription("test");
        document.setDate(1234);
        document.markUpdated(getAdminUser());
        genericService.saveOrUpdate(document);
        genericService.saveOrUpdate(new ResourceAccessStatistic(DateTime.now().toDate(), document, false));
        genericService.saveOrUpdate(new ResourceAccessStatistic(DateTime.now().minusDays(1).toDate(), document, false));
        genericService.saveOrUpdate(new ResourceAccessStatistic(DateTime.now().minusDays(1).toDate(), document,true));
        genericService.synchronize();
        return document;
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
        createAndSaveNewInformationResource(SensoryData.class, createAndSaveNewUser());
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
