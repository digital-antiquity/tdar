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
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
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

    PeriodFormatter periodFormatter;

    List<Level> oldLevels = new LinkedList<>();
    List<Logger> loggers = new LinkedList<>();
    //turn off *all* loggers.  revert state with tron(). Careful: if you don't call tron() or if an unhandled
    //exception occurs the loggers will remain off for the remainder of your unit tests.
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

    void logstats(String title, SummaryStatistics stats) {
        String fmt = "[{}::\tn:{}\tsum:{}\tmin:{}\tmax:{}\tavg:{}\tsdv:±{}]";
        Object[] args = new Object[]{
                title,
                stats.getN(),
                fmtnano(stats.getSum()),
                fmtnano(stats.getMin()),
                fmtnano(stats.getMax()),
                fmtnano(stats.getMean()),
                fmtnano(stats.getStandardDeviation())};

        logger.debug(fmt, args);
    }

    //format nanotime  into readable format e.g. "1m 2s 345ms"
    String  fmtnano(double nano) {
        long ns = Math.round(nano);
        long ms = ns / 1_000_000;
        Duration duration = new Duration(ms);
        String period = periodFormatter.print(duration.toPeriod());
        return period;
    }

    //obliterate hibernate caches(1st level, 2nd level, and query cache), then return the current session
    Session cleanSession() {
        Cache cache = sessionFactory.getCache();
        cache.evictEntityRegions();
        cache.evictCollectionRegions();
        cache.evictDefaultQueryRegion();
        cache.evictQueryRegions();
        Session session = sessionFactory.getCurrentSession();
        session.clear();
        return session;
    }

    public ResourceServiceITCase() {
        PeriodFormatterBuilder b = new PeriodFormatterBuilder();
        periodFormatter = b
                .minimumPrintedDigits(1)
                .printZeroNever()
                .appendMinutes()
                .appendSuffix("m")
                .printZeroAlways()
                .appendSeconds()
                .appendSuffix("s")
                .appendSeparatorIfFieldsBefore(" ")
                .minimumPrintedDigits(3)
                .appendMillis()
                .appendSuffix("ms")
                .toFormatter();
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
    public void testFindSimple3() throws InterruptedException, InstantiationException, IllegalAccessException {
        List<Long> idScript = genericService.findAllIds(Resource.class);
        final Long[] ids = new Long[idScript.size()];
        final StopWatch stopwatch = new StopWatch();
        int total = 50;
        Runnable[] strategies = new Runnable[] {
            new Runnable() {public void run() {newWay(false, ids);}},
            new Runnable() {public void run() {newWay(true, ids);}},
            new Runnable() {public void run() {oldWay(ids);}}
        };
        SummaryStatistics newstats1 = new SummaryStatistics();
        SummaryStatistics newstats2 = new SummaryStatistics();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics[] strategyStats = new SummaryStatistics[] {newstats1, newstats2, oldstats};
        int nextStrategy = 0;

        try {
            troff();
            for (int i=0; i < total; i++) {
                //mix up the load order
                Collections.shuffle(idScript);
                idScript.toArray(ids);
                //obliterate cache
                cleanSession();
                //The first call in each pass may run slower than subsequent calls (due to hibernate caching, jvm caching, cpu caching, who knows)
                //so, choose a different strategy to go first in each pass to distribute the first-mover penalty more evenly across all participants
                nextStrategy++;
                for(int j = 0; j < strategies.length;  j++) {
                    nextStrategy = nextStrategy % strategies.length;
                    stopwatch.start();
                    strategies[nextStrategy].run();
                    stopwatch.stop();
                    strategyStats[nextStrategy].addValue(stopwatch.getNanoTime());
                    stopwatch.reset();
                    nextStrategy++;
                }
            }
        } finally {
            tron();
        }
        logger.debug("Perf Test Complete\n\n");
        logger.debug("timing complete: {} trials", total);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
        logstats("new2 way", newstats2);
    }

    @Test
    public void testFindSimple2() throws InterruptedException, InstantiationException, IllegalAccessException {
        List<Long> idScript = genericService.findAllIds(Resource.class);
        StopWatch stopwatch = new StopWatch();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics newstats1 = new SummaryStatistics();
        SummaryStatistics newstats2 = new SummaryStatistics();
        int total = 51;

        for (int i=0; i < total; i++) {
            genericService.synchronize();
            genericService.clearCurrentSession();
            stopwatch.start();

            newWay(false,idScript.toArray(new Long[0]));
            stopwatch.stop();
            if (i != 0) {
                newstats1.addValue(stopwatch.getNanoTime());
            }

            genericService.synchronize();
            genericService.clearCurrentSession();
            stopwatch.reset();
            stopwatch.start();
            newWay(true,idScript.toArray(new Long[0]));
            stopwatch.stop();
            if (i != 0) {
                newstats2.addValue(stopwatch.getNanoTime());
            }

            genericService.synchronize();
            genericService.clearCurrentSession();
            stopwatch.reset();
            stopwatch.start();

            oldWay(idScript.toArray(new Long[0]));
            stopwatch.stop();
            if (i != 0) {
                oldstats.addValue(stopwatch.getNanoTime());
            }

            stopwatch.reset();
        }

        logger.debug("timing complete: {} trials", total);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
        logstats("new2 way", newstats2);
    }

    @Test
    @Rollback
    public void testFindSimple() throws InterruptedException, InstantiationException, IllegalAccessException {
        int trials = 10;

        StopWatch stopwatch = new StopWatch();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics newstats1 = new SummaryStatistics();
        SummaryStatistics newstats2 = new SummaryStatistics();

        for(int i = 0; i < trials; i++) {
            Long id = setupDoc();
            Long id2 = setupDoc();
            Long id3 = setupDoc();

            stopwatch.start();

            newWay(false, id);
            stopwatch.stop();
            newstats1.addValue(stopwatch.getNanoTime());

            stopwatch.reset();
            stopwatch.start();

            newWay(true, id3);
            stopwatch.stop();
            newstats2.addValue(stopwatch.getNanoTime());

            stopwatch.reset();
            stopwatch.start();

            oldWay(id2);
            stopwatch.stop();
            oldstats.addValue(stopwatch.getNanoTime());

            stopwatch.reset();
        }

        logger.debug("timing complete: {} trials", trials);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
        logstats("new2 way", newstats2);
    }

    public double seconds(double nano) {
        return nano / 1000000000.0;
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

    private void newWay(boolean include, Long ... id) {
        List<Resource> docs = resourceService.findSkeletonsForSearch(include, id);
        for (Resource rec : docs) {
            rec.logForTiming();
        }
    }

    private void oldWay(Long ... id) {
        long time = System.currentTimeMillis();
        List<Resource> recs = resourceService.findOld(id);
        for (Resource rec : recs) {
            rec.logForTiming();
        }
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
