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
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.SensoryData;
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
    public void testStats() throws InstantiationException, IllegalAccessException {
        Number docs = resourceService.countActiveResources(Document.class);
        Number datasets = resourceService.countActiveResources(Dataset.class);
        Number images = resourceService.countActiveResources(Image.class);
        Number sheets = resourceService.countActiveResources(CodingSheet.class);
        Number ontologies = resourceService.countActiveResources(Ontology.class);
        Number sensory = resourceService.countActiveResources(SensoryData.class);

        createAndSaveNewInformationResource(Document.class,false);
        createAndSaveNewInformationResource(Dataset.class,false);
        createAndSaveNewInformationResource(Image.class,false);
        createAndSaveNewInformationResource(CodingSheet.class,false);
        createAndSaveNewInformationResource(Ontology.class,false);
        createAndSaveNewInformationResource(SensoryData.class,false);

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
//        assertEquals(11L, map.get(StatisticType.NUM_PROJECT).getValue().longValue());
        assertEquals(datasets.longValue() + 1, map.get(StatisticType.NUM_DATASET).getValue().longValue());
        assertEquals(docs.longValue() + 1, map.get(StatisticType.NUM_DOCUMENT).getValue().longValue());
        assertEquals(images.longValue() + 1, map.get(StatisticType.NUM_IMAGE).getValue().longValue());
        assertEquals(sheets.longValue() + 1, map.get(StatisticType.NUM_CODING_SHEET).getValue().longValue());
        assertEquals(sensory.longValue() + 1, map.get(StatisticType.NUM_SENSORY_DATA).getValue().longValue());
        assertEquals(ontologies.longValue() + 1, map.get(StatisticType.NUM_ONTOLOGY).getValue().longValue());
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
