package org.tdar.core.service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.dao.StatisticDao;
import org.tdar.utils.Pair;

import com.ibm.icu.util.GregorianCalendar;

/*
 * simple service to help handle finding and saving statistics
 */
@Service
public class StatisticService extends ServiceInterface.TypedDaoBase<AggregateStatistic, StatisticDao> {

    private final Date startDate = new GregorianCalendar(2008, 1, 1).getTime();

    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getResourceStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_CODING_SHEET, StatisticType.NUM_DATASET, StatisticType.NUM_DOCUMENT,
                StatisticType.NUM_IMAGE, StatisticType.NUM_ONTOLOGY, StatisticType.NUM_PROJECT, StatisticType.NUM_SENSORY_DATA, StatisticType.NUM_VIDEO,
                StatisticType.NUM_ARCHIVES, StatisticType.NUM_GIS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getCollectionStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_COLLECTIONS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly = true)
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        return getDao().getCurrentResourceStats();
    }

    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getUserStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_USERS, StatisticType.NUM_ACTUAL_CONTRIBUTORS);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly = true)
    public Map<String, List<Number>> getFileAverageStats(List<VersionType> types) {
        return getDao().getFileAverageStats(types);
    }

    @Transactional(readOnly = true)
    public Map<Date, Map<StatisticType, Long>> getRepositorySizes() {
        List<StatisticType> types = Arrays.asList(StatisticType.REPOSITORY_SIZE);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    public Map<Date, Map<StatisticType, Long>> getResourceStatisticsWithFiles() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_CODING_SHEET_WITH_FILES, StatisticType.NUM_DATASET_WITH_FILES,
                StatisticType.NUM_DOCUMENT_WITH_FILES,
                StatisticType.NUM_IMAGE_WITH_FILES, StatisticType.NUM_ONTOLOGY_WITH_FILES, StatisticType.NUM_PROJECT,
                StatisticType.NUM_SENSORY_DATA_WITH_FILES, StatisticType.NUM_VIDEO_WITH_FILES,
                StatisticType.NUM_GIS_WITH_FILES, StatisticType.NUM_ARCHIVES_WITH_FILES);
        return getDao().getStatistics(startDate, new Date(), types.toArray(new StatisticType[0]));
    }

    public List<Pair<Long, Long>> getUserLoginStats() {
        return getDao().getUserLoginStats();
    }

}
