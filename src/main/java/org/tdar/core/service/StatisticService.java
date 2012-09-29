package org.tdar.core.service;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.util.Statistic;
import org.tdar.core.bean.util.Statistic.StatisticType;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.StatisticDao;

/*
 * simple service to help handle finding and saving statistics
 */
@Service
public class StatisticService extends ServiceInterface.TypedDaoBase<Statistic, StatisticDao> {

    @Autowired
    GenericDao genericDao;

    @Autowired
    public void setDao(StatisticDao dao) {
        super.setDao(dao);
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    public Date getBeginning() {
        try {
            return format.parse("20080101");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Transactional(readOnly=true)
    public Map<Date, Map<StatisticType, Long>> getResourceStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_CODING_SHEET, StatisticType.NUM_DATASET, StatisticType.NUM_DOCUMENT,
                StatisticType.NUM_IMAGE, StatisticType.NUM_ONTOLOGY, StatisticType.NUM_PROJECT, StatisticType.NUM_SENSORY_DATA);
        return getDao().getStatistics(getBeginning(), new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly=true)
    public Map<Date, Map<StatisticType, Long>> getCollectionStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_COLLECTIONS);
        return getDao().getStatistics(getBeginning(), new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly=true)
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        return getDao().getCurrentResourceStats();
    }

    @Transactional(readOnly=true)
    public Map<Date, Map<StatisticType, Long>> getUserStatistics() {
        List<StatisticType> types = Arrays.asList(StatisticType.NUM_USERS,StatisticType.NUM_ACTUAL_CONTRIBUTORS);
        return getDao().getStatistics(getBeginning(), new Date(), types.toArray(new StatisticType[0]));
    }

    @Transactional(readOnly=true)
    public Map<String, List<Number>> getFileAverageStats() {
        return getDao().getFileAverageStats();
    }
    
}
