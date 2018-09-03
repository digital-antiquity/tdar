package org.tdar.core.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.statistics.AggregateStatistic;
import org.tdar.core.bean.statistics.AggregateStatistic.StatisticType;
import org.tdar.core.dao.base.HibernateBase;
import org.tdar.filestore.VersionType;
import org.tdar.utils.Pair;

@Component
public class StatisticDao extends HibernateBase<AggregateStatistic> {

    public StatisticDao() {
        super(AggregateStatistic.class);
    }

    public Map<Date, Map<StatisticType, Long>> getStatistics(Date fromDate, Date toDate, StatisticType... types) {
        Query<AggregateStatistic> query = getNamedQuery(QUERY_USAGE_STATS, AggregateStatistic.class);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("statTypes", Arrays.asList(types));
        Map<Date, Map<StatisticType, Long>> toReturn = new HashMap<Date, Map<StatisticType, Long>>();
        for (AggregateStatistic result : query.getResultList()) {
            Date date = result.getRecordedDate();
            if (!toReturn.containsKey(date)) {
                toReturn.put(date, new HashMap<StatisticType, Long>());
                for (StatisticType type : types) {
                    toReturn.get(date).put(type, 0L);
                }
            }
            Map<StatisticType, Long> stat = toReturn.get(date);
            stat.put(result.getStatisticType(), result.getValue());
        }

        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<ResourceType, List<BigInteger>> getCurrentResourceStats() {
        Query query = getCurrentSession().createNativeQuery(QUERY_SQL_RAW_RESOURCE_STAT_LOOKUP);
        Map<ResourceType, List<BigInteger>> toReturn = new HashMap<ResourceType, List<BigInteger>>();
        for (Object[] result_ : (List<Object[]>) query.getResultList()) {
            List<BigInteger> stat = new ArrayList<BigInteger>();
            toReturn.put(ResourceType.valueOf((String) result_[0]), stat);
            stat.add((BigInteger) result_[1]);
            stat.add((BigInteger) result_[2]);
            stat.add((BigInteger) result_[3]);
            stat.add((BigInteger) result_[4]);
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<Number>> getFileAverageStats(List<VersionType> types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_STATS);
        query.setParameter("types", types);
        Map<String, List<Number>> toReturn = new HashMap<String, List<Number>>();
        for (Object[] result_ : (List<Object[]>) query.getResultList()) {
            List<Number> stat = new ArrayList<Number>();
            toReturn.put((String) result_[0], stat);
            stat.add((Double) result_[1]); // average
            stat.add((Long) result_[2]); // min
            stat.add((Long) result_[3]); // max
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public List<Pair<Long, Long>> getUserLoginStats() {
        Query query = getCurrentSession().getNamedQuery(QUERY_LOGIN_STATS);
        List<Pair<Long, Long>> toReturn = new ArrayList<Pair<Long, Long>>();
        for (Object[] result_ : (List<Object[]>) query.getResultList()) {
            Number total = (Number) result_[0];
            Number count = (Number) result_[1];
            toReturn.add(Pair.create(total.longValue(), count.longValue()));
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Long> getFileStats(List<VersionType> types) {
        Query query = getCurrentSession().getNamedQuery(QUERY_FILE_SIZE_TOTAL);
        query.setParameter("types", types);
        Map<String, Long> toReturn = new HashMap<>();
        for (Object[] result_ : (List<Object[]>) query.getResultList()) {
            String txt = StringUtils.upperCase((String) result_[0]);
            switch (txt) {
                case "JPEG":
                    txt = "JPG";
                    break;
                case "TIFF":
                    txt = "TIF";
                default:
                    break;
            }
            Long val = toReturn.get(txt);
            if (val == null) {
                val = 0L;
            }
            toReturn.put(txt, val + (Long) result_[1]);
        }
        return toReturn;
    }

    public Number countWeeklyEmails() {
        Query<Number> query = getNamedQuery(TdarNamedQueries.WEEKLY_EMAIL_STATS, Number.class);
        query.setParameter("date", DateTime.now().minusDays(7).toDate());
        return query.getSingleResult();
    }
}
