package org.tdar.core.bean.resource;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;

public class ResourceServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private transient SessionFactory sessionFactory;


    List<Level> oldLevels = new LinkedList<>();
    List<Logger> loggers = new LinkedList<>();

    //turn off all loggers.  revert state with tron().
    void troff() {
        getVerifyTransactionCallback();
        loggers.clear();
        oldLevels.clear();
        loggers.addAll(Collections.<Logger>list(LogManager.getCurrentLoggers()));
        loggers.add(LogManager.getRootLogger());
        for ( Logger logger : loggers ) {
            //out("troff:: %s \t was:%s", logger.getName(), logger.getLevel());
            oldLevels.add(logger.getLevel());
            logger.setLevel(Level.OFF);
        }
    }

    void tron() {
        ListIterator<Logger> itor = loggers.listIterator();
        for(Level level : oldLevels) {
            itor.next().setLevel(level);
        }
    }

    void out(String fmt, Object ... args) {
        System.out.println(String.format(fmt, args));
    }

    long nano() {
        return System.nanoTime();
    }

    @Test
    @Rollback
    public void testSaveHasResource() throws InstantiationException, IllegalAccessException {
        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());
    }

    @Test
    //fixme: this is just an example of how to get more accurate timings - if useful, we should pull out this timing harness to work with any test.
    public void testFindSimple() throws InterruptedException, InstantiationException, IllegalAccessException {
//        List<Long> idScript = genericService.findAllIds(Resource.class);
//        assertThat(idScript, not(empty()));
        int trials = 3;

        StopWatch stopwatch = new StopWatch();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics newstats1 = new SummaryStatistics();
        SummaryStatistics newstats2 = new SummaryStatistics();

        for(int i = 0; i < trials; i++) {
                
            Long id = setupDoc();

            stopwatch.start();

            newWay(false, id);
            stopwatch.stop();
            newstats1.addValue(stopwatch.getNanoTime());

            id = setupDoc();
            stopwatch.reset();
            stopwatch.start();

            oldWay(id);
            stopwatch.stop();
            oldstats.addValue(stopwatch.getNanoTime());

            id = setupDoc();
            stopwatch.reset();
            stopwatch.start();
            newWay(true, id);
            stopwatch.stop();
            newstats2.addValue(stopwatch.getNanoTime());
            stopwatch.reset();
        }

        logger.debug("timing complete: {} trials:  values:{}", trials, oldstats.getN());
        logger.debug(" old way::  total:{}   avg:{}   stddev:{}", oldstats.getSum(), oldstats.getMean(), oldstats.getStandardDeviation());
        logger.debug("new way1::  total:{}   avg:{}   stddev:{}", newstats1.getSum(), newstats1.getMean(), newstats1.getStandardDeviation());
        logger.debug("new way2::  total:{}   avg:{}   stddev:{}", newstats2.getSum(), newstats2.getMean(), newstats2.getStandardDeviation());
    }

    private Long setupDoc() throws InstantiationException, IllegalAccessException {
        Document doc = generateDocumentWithFileAndUser();
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.AUTHOR));
        doc.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser().getInstitution(), ResourceCreatorRole.CONTACT));
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.SUBMITTED_TO));
        doc.getLatitudeLongitudeBoxes().add(new LatitudeLongitudeBox(1d, 1d, 4d, 4d));
        genericService.saveOrUpdate(doc);
        genericService.synchronize();
        searchIndexService.flushToIndexes();
        genericService.clearCurrentSession();
        return doc.getId();
    }

    private void newWay(boolean include, long id) {
        List<Resource> docs = resourceService.findSkeletonsForSearch(include, id);
        Resource doc = docs.get(0);
        logger.debug("retrieved {}", doc);
    }

    private void oldWay(long id) {
        long time = System.currentTimeMillis();
        List<Resource> recs = resourceService.findOld(id);
        Resource rec2 = recs.get(0);
        logger.debug("retrieved {}", rec2);
    }
    
    @Test
    @Rollback
    public void testSaveHasResourceExistingNull() throws InstantiationException, IllegalAccessException {

        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        doc.setCoverageDates(null);
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());

    }

    @Test
    @Rollback
    public void testSaveHasResourceIncomingNulLCase() throws InstantiationException, IllegalAccessException {

        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = null;
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());

    }
}
