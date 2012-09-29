/**
 * $Id: CachingServiceITCase.java 1761 2011-03-16 18:34:03Z abrin $
 * 
 * @author $Author: abrin $
 * @version $Revision: 1761 $
 */
package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.util.Statistic;
import org.tdar.core.bean.util.Statistic.StatisticType;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.TdarActionSupport;

/**
 * @author Adam Brin
 * 
 */
public class ScheduledProcessITCase extends AbstractControllerITCase {

    @Autowired
    private ScheduledProcessService scheduledProcessService;

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testStats() {
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
        assertEquals(11L, map.get(StatisticType.NUM_PROJECT).getValue().longValue());
        assertEquals(2L, map.get(StatisticType.NUM_DATASET).getValue().longValue());
//        assertEquals(4L, map.get(StatisticType.NUM_CODING_SHEET).getValue().longValue());
//        assertEquals(0L, map.get(StatisticType.NUM_SENSORY_DATA).getValue().longValue());
        assertEquals(1L, map.get(StatisticType.NUM_ONTOLOGY).getValue().longValue());
        assertEquals(19L, map.get(StatisticType.NUM_USERS).getValue().longValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return null;
    }
}
