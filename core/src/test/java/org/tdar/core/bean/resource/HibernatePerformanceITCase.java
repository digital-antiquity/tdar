package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.service.resource.ResourceService;

@Ignore(value = "performance test cases ... ignore")
public class HibernatePerformanceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private transient SessionFactory sessionFactory;

    /**
     * FIXME: Disabled for new log4j 
     */
//    List<Level> oldLevels = new LinkedList<>();
    List<Logger> loggers = new LinkedList<>();

    // turn off *all* loggers. revert state with tron(). Careful: if you don't call tron() or if an unhandled
    // exception occurs the loggers will remain off for the remainder of your unit tests.
    void disableLogging() {
        getVerifyTransactionCallback();
//        loggers.clear();
//        oldLevels.clear();
//        loggers.addAll(Collections.<Logger> list(LogManager.getCurrentLoggers()));
//        loggers.add(LogManager.getRootLogger());
//        for (Logger logger : loggers) {
//            // out("troff:: %s \t was:%s", logger.getName(), logger.getLevel());
//            oldLevels.add(logger.getLevel());
//            logger.setLevel(Level.OFF);
//        }
    }

    void enableLogging() {
//        ListIterator<Logger> itor = loggers.listIterator();
//        for (Level level : oldLevels) {
//            itor.next().setLevel(level);
//        }
    }

    void logstats(String title, SummaryStatistics stats) {
        String fmt = "[{}::\tn:{}\tsum:{}\tmin:{}\tmax:{}\tavg:{}\tsdv:±{}]";
        Object[] args = new Object[] {
                title,
                stats.getN(),
                seconds(stats.getSum()),
                seconds(stats.getMin()),
                seconds(stats.getMax()),
                seconds(stats.getMean()),
                seconds(stats.getStandardDeviation()) };

        logger.debug(fmt, args);
    }

    public double seconds(double nano) {
        return nano / 1000000000.0;
    }

    // obliterate hibernate caches(1st level, 2nd level, and query cache), then return the current session
    Session cleanSession() {
        evictCache();
//        searchIndexService.flushToIndexes();
        Cache cache = sessionFactory.getCache();
        cache.evictEntityRegions();
        cache.evictCollectionRegions();
        cache.evictDefaultQueryRegion();
        cache.evictQueryRegions();
        Session session = sessionFactory.getCurrentSession();
        session.clear();
        return session;
    }

    @Test
    public void testFindSimple3() throws InterruptedException, InstantiationException, IllegalAccessException {
        List<Long> idScript = genericService.findAllIds(Resource.class);
        final Long[] ids = new Long[idScript.size()];
        final StopWatch stopwatch = new StopWatch();
        int total = 50;
        Runnable[] strategies = new Runnable[] {
                new Runnable() {
                    @Override
                    public void run() {
                        newWay(ids);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        oldWay(ids);
                    }
                }
        };
        SummaryStatistics newstats1 = new SummaryStatistics();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics[] strategyStats = new SummaryStatistics[] { newstats1, oldstats };
        int nextStrategy = 0;

        try {
            disableLogging();
            for (int i = 0; i < total; i++) {
                // mix up the load order
                Collections.shuffle(idScript);
                idScript.toArray(ids);
                // obliterate cache
                cleanSession();
                // The first call in each pass may run slower than subsequent calls (due to hibernate caching, jvm caching, cpu caching, who knows)
                // so, choose a different strategy to go first in each pass to distribute the first-mover penalty more evenly across all participants
                nextStrategy++;
                shuffleArray(strategies);
                for (int j = 0; j < strategies.length; j++) {
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
            enableLogging();
        }
        logger.debug("Perf Test Complete\n\n");
        logger.debug("timing complete: {} trials", total);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
    }

    // http://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
    // Implementing Fisher–Yates shuffle
    static void shuffleArray(Runnable[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Runnable a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @Test
    public void testFindSimple2() throws InterruptedException, InstantiationException, IllegalAccessException {
        List<Long> idScript = genericService.findAllIds(Resource.class);
        StopWatch stopwatch = new StopWatch();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics newstats1 = new SummaryStatistics();
        int total = 51;
        disableLogging();
        for (int i = 0; i < total; i++) {
            cleanSession();
            stopwatch.start();

            newWay(idScript.toArray(new Long[0]));
            stopwatch.stop();
            if (i != 0) {
                newstats1.addValue(stopwatch.getNanoTime());
            }

            cleanSession();
            stopwatch.reset();
            stopwatch.start();

            oldWay(idScript.toArray(new Long[0]));
            stopwatch.stop();
            if (i != 0) {
                oldstats.addValue(stopwatch.getNanoTime());
            }

            stopwatch.reset();
        }
        enableLogging();
        logger.debug("timing complete: {} trials", total);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
    }

    @Test
    @Rollback
    public void testFindSimple() throws InterruptedException, InstantiationException, IllegalAccessException {
        int trials = 10;

        StopWatch stopwatch = new StopWatch();
        SummaryStatistics oldstats = new SummaryStatistics();
        SummaryStatistics newstats1 = new SummaryStatistics();
        disableLogging();
        for (int i = 0; i < trials; i++) {
            Long id = setupDoc();
            Long id2 = setupDoc();

            stopwatch.start();

            newWay(id);
            stopwatch.stop();
            newstats1.addValue(stopwatch.getNanoTime());

            stopwatch.reset();
            stopwatch.start();

            oldWay(id2);
            stopwatch.stop();
            oldstats.addValue(stopwatch.getNanoTime());

            stopwatch.reset();
        }
        enableLogging();
        logger.debug("timing complete: {} trials", trials);
        logstats(" old way", oldstats);
        logstats("new1 way", newstats1);
    }

    private Long setupDoc() throws InstantiationException, IllegalAccessException {
        Document doc = generateDocumentWithFileAndUseDefaultUser();
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.AUTHOR));
        doc.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser().getInstitution(), ResourceCreatorRole.CONTACT));
        doc.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.SUBMITTED_TO));
        doc.getLatitudeLongitudeBoxes().add(new LatitudeLongitudeBox(1d, 1d, 4d, 4d));
        genericService.saveOrUpdate(doc);
        cleanSession();
        return doc.getId();
    }

    private void newWay(Long... id) {
        List<Resource> docs = resourceService.findSkeletonsForSearch(false, Arrays.asList(id));
        for (Resource rec : docs) {
            logForTiming(rec);
        }
    }

    private void oldWay(Long... id) {
        List<Resource> recs = resourceService.findOld(id);
        for (Resource rec : recs) {
            logForTiming(rec);
        }
    }

    public void logForTiming(Resource res) {
        if (res instanceof InformationResource) {
            InformationResource ir = (InformationResource) res;
            String msg = String.format("%s %s %s %s %s", res, ir.getLatitudeLongitudeBoxes(), res.getResourceCreators(), res.getSubmitter(), ir.getProject(),
                    ir.getInformationResourceFiles());
            logger.debug(msg);
        } else {
            String msg = String.format("%s %s %s %s", res, res.getLatitudeLongitudeBoxes(), res.getResourceCreators(), res.getSubmitter());
            logger.debug(msg);
        }
    }
}
