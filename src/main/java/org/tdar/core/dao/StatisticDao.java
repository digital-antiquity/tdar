package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;

@Component
public class StatisticDao extends Dao.HibernateBase<AggregateStatistic> {

    public StatisticDao() {
        super(AggregateStatistic.class);
    }

    @SuppressWarnings("unchecked")
    public Map<Date, Map<StatisticType, Long>> getStatistics(Date fromDate, Date toDate, StatisticType ... types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_USAGE_STATS);
        query.setDate("fromDate", fromDate);
        query.setDate("toDate", toDate);
        query.setParameterList("statTypes", types);
        Map<Date, Map<StatisticType,Long>> toReturn = new HashMap<Date, Map<StatisticType,Long>>();
        for (AggregateStatistic result : (List<AggregateStatistic>) query.list()) {
            Date date = result.getRecordedDate();
            if (!toReturn.containsKey(date)) {
                toReturn.put(date, new HashMap<StatisticType, Long>());
                for (StatisticType type : types) {
                    toReturn.get(date).put(type, 0L);
                }
            }
            Map<StatisticType,Long> stat = toReturn.get(date);
            stat.put(result.getStatisticType(), result.getValue());
        }

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        Query query = getCurrentSession().createSQLQuery(QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP);
        Map<ResourceType, List<BigInteger>> toReturn = new HashMap<ResourceType, List<BigInteger>>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            List<BigInteger> stat = new ArrayList<BigInteger>();
            toReturn.put(ResourceType.valueOf((String) result_[3]), stat);
            stat.add((BigInteger) result_[0]);
            stat.add((BigInteger) result_[1]);
            stat.add((BigInteger) result_[2]);
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Number>> getFileAverageStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_STATS);
        Map<String, List<Number>> toReturn = new HashMap<String, List<Number>>();
        for (Object[] result_ : (List<Object[]>) query.list()) {
            List<Number> stat = new ArrayList<Number>();
            toReturn.put((String) result_[0], stat);
            stat.add((Double) result_[1]); //average
            stat.add((Long) result_[2]); //min
            stat.add((Long) result_[3]); //max
        }
        return toReturn;
    }
    
}
