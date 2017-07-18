/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hibernate.stat.Statistics;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.GenericService;

/**
 * @author Adam Brin
 * 
 */
public class GenericITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;
    @Autowired
    private GenericDao genericDao;

    public static Integer INVESTIATION_TYPE_COUNT = 20;

    @Test
    public void testCount() {
        Number count = genericService.count(InvestigationType.class);
        assertEquals(Integer.valueOf(count.intValue()), INVESTIATION_TYPE_COUNT);
        List<InvestigationType> allInvestigationTypes = genericService.findAll(InvestigationType.class);
        assertEquals(INVESTIATION_TYPE_COUNT, Integer.valueOf(allInvestigationTypes.size()));
    }

    @Test
    public void testCache() {
        long id = Long.parseLong(TestConstants.TEST_DOCUMENT_ID);
        logger.debug("cache contains?:{}", genericDao.cacheContains(Document.class, id));
        Document document = genericService.find(Document.class, id);
        document.getFirstActiveLatitudeLongitudeBox();
        logger.debug("done {}", document);
        Statistics stats = genericService.getSessionStatistics();
        logger.debug("stats: {}h {}m {}p", stats.getSecondLevelCacheHitCount(), stats.getSecondLevelCacheMissCount(), stats.getSecondLevelCachePutCount());
        logger.debug("cache contains?:{}", genericDao.cacheContains(Document.class, id));
        document = genericService.find(Document.class, id);
        document.getFirstActiveLatitudeLongitudeBox();
        logger.debug("done {}", document);
        logger.debug("stats: {}h {}m {}p", stats.getSecondLevelCacheHitCount(), stats.getSecondLevelCacheMissCount(), stats.getSecondLevelCachePutCount());
        logger.debug("cache contains?:{}", genericDao.cacheContains(Document.class, id));
    }
}
