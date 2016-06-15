package org.tdar.core.service.processes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.processes.relatedInfoLog.RelatedInfoLog;
import org.tdar.core.service.processes.relatedInfoLog.RelatedInfoLogPart;
import org.tdar.utils.PersistableUtils;

import com.google.common.primitives.Doubles;

public abstract class AbstractAnalysisTask<P extends Persistable> extends AbstractScheduledBatchProcess<P> {

    private static final long serialVersionUID = 3903831600832839879L;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient SerializationService serializationService;

    protected void generateLogEntry(Set<Long> resourceIds, Persistable creator, int total, List<Long> idsToIgnoreInLargeTasks) {
        if (CollectionUtils.isEmpty(resourceIds )) {
            return;
        }
        Map<Keyword, Integer> keywords = incrementKeywords(resourceIds);
        Map<Creator, Integer> collaborators = incrementCreators(resourceIds, idsToIgnoreInLargeTasks);

        RelatedInfoLog log = new RelatedInfoLog();
        log.setAbout(creator);
        log.setTotalRecords(total);

        // refactor to remove dups
        Mean creatorMean = new Mean();
        Median creatorMedian = new Median();
        double[] collbValues = Doubles.toArray(collaborators.values());
        log.setCreatorMean(creatorMean.evaluate(collbValues));
        log.setCreatorMedian(creatorMedian.evaluate(collbValues));

        Mean keywordMean = new Mean();
        Median keywordMedian = new Median();
        double[] kwdValues = Doubles.toArray(keywords.values());
        log.setKeywordMean(keywordMean.evaluate(kwdValues));
        log.setKeywordMedian(keywordMedian.evaluate(kwdValues));

        for (Entry<Creator, Integer> entrySet : collaborators.entrySet()) {
            RelatedInfoLogPart part = new RelatedInfoLogPart();
            part.setCount(entrySet.getValue().longValue());
            Creator key = entrySet.getKey();
            if (PersistableUtils.isNullOrTransient(key)) {
                continue;
            }
            part.setId(key.getId());
            part.setSimpleClassName(getClass(key));
            part.setName(key.getProperName());
            log.getCollaboratorLogPart().add(part);
        }

        for (Entry<Keyword, Integer> entrySet : keywords.entrySet()) {
            RelatedInfoLogPart part = new RelatedInfoLogPart();
            part.setCount(entrySet.getValue().longValue());
            Keyword key = entrySet.getKey();
            if (PersistableUtils.isNullOrTransient(key)) {
                continue;
            }
            part.setId(key.getId());
            part.setSimpleClassName(getClass(key));
            part.setName(key.getLabel());
            log.getKeywordLogPart().add(part);
        }

        Collections.sort(log.getCollaboratorLogPart());
        Collections.sort(log.getKeywordLogPart());

        try {
            if (creator instanceof Creator) {
                serializationService.generateFOAF((Creator) creator, log);
            }
            serializationService.generateRelatedLog(creator, log);
        } catch (Exception e) {
            getLogger().error("exception: ", e);
        }
    }

    public List<Long> findEverything() {
        /*
         * Theoretically, we could use the DatasetDao.findRecentlyUpdatedItemsInLastXDays to find all resources modified in the
         * last wwek, and then use those resources to grab all associated creators, and then process those
         */
        List<Long> results = genericDao.findAllIds(getPersistentClass());
        if (CollectionUtils.isNotEmpty(results)) {
            return results;
        }
        return null;
    }

    private String getClass(Object object_) {
        Object object = object_;
        if (HibernateProxy.class.isAssignableFrom(object.getClass())) {
            object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object.getClass().getSimpleName();
    }

    private Map<Creator, Integer> incrementCreators(Set<Long> resourceIds, List<Long> userIdsToIgnoreInLargeTasks) {
        Map<Creator, Integer> counts = entityService.getRelatedCreatorCounts(resourceIds);
        return counts;
    }

    private Map<Keyword, Integer> incrementKeywords(Set<Long> resourceIds) {
        return genericKeywordService.getRelatedKeywordCounts(resourceIds);
    }

}
