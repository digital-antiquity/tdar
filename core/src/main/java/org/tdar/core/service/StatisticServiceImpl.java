package org.tdar.core.service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.bean.statistics.AggregateDayViewStatistic;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.dao.AggregateStatisticsDao;
import org.tdar.core.dao.StatisticDao;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.utils.Pair;

import com.ibm.icu.util.GregorianCalendar;
import com.opensymphony.xwork2.TextProvider;

/**
 * Helper class for running statistics and working with @link AggregatedStatistic objects
 * 
 * @author abrin
 * 
 */
@Service
public class StatisticServiceImpl  extends ServiceInterface.TypedDaoBase<AggregateStatistic, StatisticDao> implements StatisticsService {

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private AggregateStatisticsDao aggregateStatisticsDao;

    private final Date startDate = new GregorianCalendar(2008, 1, 1).getTime();

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getResourceStatistics()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getResourceStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_CODING_SHEET, StatisticType.NUM_DATASET, StatisticType.NUM_DOCUMENT,
                StatisticType.NUM_IMAGE, StatisticType.NUM_ONTOLOGY, StatisticType.NUM_PROJECT, StatisticType.NUM_SENSORY_DATA, StatisticType.NUM_VIDEO,
                StatisticType.NUM_ARCHIVES, StatisticType.NUM_GIS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getCollectionStatistics()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getCollectionStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_COLLECTIONS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getCurrentResourceStats()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        return getDao().getCurrentResourceStats();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getUserStatistics()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getUserStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_USERS, StatisticType.NUM_ACTUAL_CONTRIBUTORS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getFileAverageStats(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, List<Number>> getFileAverageStats(List<VersionType> types) {
        return getDao().getFileAverageStats(types);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getRepositorySizes()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getRepositorySizes() {
        List<StatisticType> types = Arrays.asList(StatisticType.REPOSITORY_SIZE);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getResourceStatisticsWithFiles()
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getResourceStatisticsWithFiles() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_CODING_SHEET_WITH_FILES, StatisticType.NUM_DATASET_WITH_FILES,
                StatisticType.NUM_DOCUMENT_WITH_FILES,
                StatisticType.NUM_IMAGE_WITH_FILES, StatisticType.NUM_ONTOLOGY_WITH_FILES, StatisticType.NUM_PROJECT,
                StatisticType.NUM_SENSORY_DATA_WITH_FILES, StatisticType.NUM_VIDEO_WITH_FILES,
                StatisticType.NUM_GIS_WITH_FILES, StatisticType.NUM_ARCHIVES_WITH_FILES);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getUserLoginStats()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pair<Long, Long>> getUserLoginStats() {
        return getDao().getUserLoginStats();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getFileStats(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getFileStats(List<VersionType> types) {
        return getDao().getFileStats(types);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#cleanupOldDailyStats(java.util.Date)
     */
    @Override
    @Transactional(readOnly=false)
    public void cleanupOldDailyStats(Date date) {
        aggregateStatisticsDao.cleanupOldDailyStats(date);
        
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#generateAggregateDailyDownloadData(java.util.Date)
     */
    @Override
    @Transactional
    public void generateAggregateDailyDownloadData(Date date) {
        aggregateStatisticsDao.generateAggregateDailyDownloadData(date);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#countWeeklyEmails()
     */
    @Override
    @Transactional
    public Number countWeeklyEmails() {
        return getDao().countWeeklyEmails();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getStatsForCollection(org.tdar.core.bean.collection.SharedCollection, com.opensymphony.xwork2.TextProvider, org.tdar.core.dao.resource.stats.DateGranularity)
     */
    @Override
    @Transactional(readOnly = true)
    public StatsResultObject getStatsForCollection(ResourceCollection collection, TextProvider provider, DateGranularity granularity) {
        if (collection != null) {
            return getStats(collection, provider, granularity);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getStatsForAccount(org.tdar.core.bean.billing.BillingAccount, com.opensymphony.xwork2.TextProvider, org.tdar.core.dao.resource.stats.DateGranularity)
     */
    @Override
    @Transactional(readOnly = true)
    public StatsResultObject getStatsForAccount(BillingAccount account, TextProvider provider, DateGranularity granularity) {
        if (account != null && CollectionUtils.isNotEmpty(account.getResources())) {
            return getStats(account, provider, granularity);
        }
        return null;
    }

    private StatsResultObject getStats(Persistable p, TextProvider provider, DateGranularity granularity) {
        switch (granularity) {
            case DAY:
                return aggregateStatisticsDao.getDailyStats(p, provider);
            case MONTH:
                return aggregateStatisticsDao.getMonthlyStats(p, provider);
            case YEAR:
                return aggregateStatisticsDao.getAnnualStats(p, provider);
            default:
                return null;
        }
    }


    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#generateMonthlyResourceStats(org.joda.time.DateTime)
     */
    @Override
    @Transactional(readOnly=false)
    public void generateMonthlyResourceStats(DateTime date) {
        aggregateStatisticsDao.updateMonthly(date);
        
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#initializeNewAggregateEntries(org.joda.time.DateTime)
     */
    @Override
    @Transactional(readOnly=false)
    public void initializeNewAggregateEntries(DateTime date) {
        aggregateStatisticsDao.createNewAggregateEntries(date);
//        aggregateStatisticsDao.resetAnnualTable(date);
    }



    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getAggregateDownloadStatsForFile(org.tdar.core.dao.resource.stats.DateGranularity, java.util.Date, java.util.Date, java.lang.Long, java.lang.Long)
     */
    @Override
    @Transactional(readOnly=true)
    public List<AggregateDownloadStatistic> getAggregateDownloadStatsForFile(DateGranularity granularity, Date start, Date end, Long minCount, Long iRFileId) {
        return aggregateStatisticsDao.getDownloadStatsForFile(granularity, start, end, minCount, iRFileId);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.StatisticsService#getUsageStatsForResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly=true)
    public List<AggregateDayViewStatistic> getUsageStatsForResource(Resource resource) {
        return aggregateStatisticsDao.getUsageStatsForResource(resource);
    }
    
    @Transactional(readOnly=true)
    @Override
    public ResourceStatisticsObject getUsageStatsObjectForResource(TextProvider provider, Resource resource) throws IOException {
        Map<String, List<AggregateDownloadStatistic>> downloadStats = new HashMap<String, List<AggregateDownloadStatistic>>();
        if (resource instanceof InformationResource) {
            for (InformationResourceFile file : ((InformationResource) resource).getInformationResourceFiles()) {
                downloadStats.put(file.getFilename(), getAggregateDownloadStatsForFile(DateGranularity.WEEK, new Date(0L), new Date(), 1L, file.getId()));
            }
        }

        ResourceStatisticsObject rso = new ResourceStatisticsObject(provider, getUsageStatsForResource(resource), downloadStats, resource);
        return rso;
    }

    
}
