package org.tdar.core.service.processes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.search.FullTextQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.search.SearchService;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.google.common.primitives.Doubles;

@Component
public class CreatorAnalysisProcess extends ScheduledBatchProcess<Creator> {

    private static final long serialVersionUID = 581887107336388520L;

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient SearchService searchService;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Autowired
    private transient EntityService entityService;

    @Autowired
    private transient DatasetDao datasetDao;

    @Autowired
    private transient ProjectDao projectDao;

    @Autowired
    private transient SerializationService serializationService;

    private int daysToRun = TdarConfiguration.getInstance().getDaysForCreatorProcess();

    private boolean findRecent = true;

    @Override
    public String getDisplayName() {
        return "Creator Analytics Process";
    }

    @Override
    public int getBatchSize() {
        return 100;
    }

    @Override
    public Class<Creator> getPersistentClass() {
        return Creator.class;
    }

    @Override
    public List<Long> findAllIds() {
        /*
         * We could use the DatasetDao.findRecentlyUpdatedItemsInLastXDays to find all resources modified in the
         * last wwek, and then use those resources to grab all associated creators, and then process those
         */
        if (findRecent) {
            return findCreatorsOfRecentlyModifiedResources();
        } else {
            return findEverything();
        }

    }

    private List<Long> findCreatorsOfRecentlyModifiedResources() {
        // this could be optimized to get the list of creator ids from the database
        List<Resource> results = datasetDao.findRecentlyUpdatedItemsInLastXDays(getDaysToRun());
        Set<Long> ids = new HashSet<>();
        getLogger().debug("dealing with {} resource(s) updated in the last {} days", results.size(), getDaysToRun());
        while (!results.isEmpty()) {
            Resource resource = results.remove(0);
            // add all children of project if project was modified (inheritance check)
            if (resource instanceof Project) {
                results.addAll(projectDao.findAllResourcesInProject((Project) resource));
            }
            getLogger().trace(" - adding {} creators", resource.getRelatedCreators().size());
            for (Creator creator : resource.getRelatedCreators()) {
                if (creator == null) {
                    continue;
                }
                if (creator.isDuplicate()) {
                    creator = entityService.findAuthorityFromDuplicate(creator);
                }
                if ((creator == null) || !creator.isActive()) {
                    continue;
                }
                ids.add(creator.getId());
            }
        }
        return new ArrayList<>(ids);
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

    @Override
    public void execute() {
        List<Creator> creators = genericDao.findAll(getPersistentClass(), getNextBatch());
        List<Long> userIdsToIgnoreInLargeTasks = getTdarConfiguration().getUserIdsToIgnoreInLargeTasks();
        boolean seen = false;
        for (Creator creator : creators) {
            getLogger().trace("~~~~~ {} ~~~~~~", creator);
            if (!seen) {
                getLogger().debug("~~~~~ {} ~~~~~~", creator);
                seen = true;
            }
            if (userIdsToIgnoreInLargeTasks.contains(creator.getId())) {
                continue;
            }
            int total = 0;
            if (!creator.isActive()) {
                continue;
            }
            QueryBuilder query = searchService.generateQueryForRelatedResources(creator, null, MessageHelper.getInstance());
            Set<Long> resourceIds = new HashSet<>();
            try {
                FullTextQuery search = searchService.search(query);
                // change to ID only projection
//                search.setProjection(arg0)
                ScrollableResults results = search.scroll(ScrollMode.FORWARD_ONLY);
                total = search.getResultSize();
                if (total == 0) {
                    continue;
                }
                while (results.next()) {
                    Resource resource = (Resource) results.get()[0];
                    resourceIds.add(resource.getId());
                }
            } catch (Exception e) {
                getLogger().warn("Exception", e);
            }

            Map<Keyword, Integer> keywords = incrementKeywords(resourceIds);
            Map<Creator, Integer> collaborators = incrementCreators(resourceIds, userIdsToIgnoreInLargeTasks);

            CreatorInfoLog log = new CreatorInfoLog();
            log.setPerson(creator);
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
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue().longValue());
                Creator key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(getClass(key));
                part.setName(key.getProperName());
                log.getCollaboratorLogPart().add(part);
            }

            for (Entry<Keyword, Integer> entrySet : keywords.entrySet()) {
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue().longValue());
                Keyword key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(getClass(key));
                part.setName(key.getLabel());
                log.getKeywordLogPart().add(part);
            }

            Collections.sort(log.getCollaboratorLogPart(), new LogPartComparator());
            Collections.sort(log.getKeywordLogPart(), new LogPartComparator());

            try {
                serializationService.generateFOAF(creator, log);
                serializationService.generateCreatorLog(creator, log);
            } catch (Exception e) {
                getLogger().error("exception: ", e);
            }
        }
    }

    private String getClass(Object object_) {
        Object object = object_;
        if (HibernateProxy.class.isAssignableFrom(object.getClass())) {
            object = ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object.getClass().getSimpleName();
    }


    public static class LogPartComparator implements Comparator<LogPart> {

        @Override
        public int compare(LogPart o1, LogPart o2) {
            return ObjectUtils.compare(o1.getCount(), o2.getCount());
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "creatorInfoLog")
    public static class CreatorInfoLog {
        private List<LogPart> collaboratorLogPart = new ArrayList<LogPart>();
        private List<LogPart> keywordLogPart = new ArrayList<LogPart>();
        private Creator creator;
        private int totalRecords;
        private Double creatorMean;
        private Double creatorMedian;
        private Double keywordMean;
        private Double keywordMedian;

        @XmlAttribute
        public Double getCreatorMean() {
            return creatorMean;
        }

        public void setCreatorMean(Double creatorMean) {
            this.creatorMean = creatorMean;
        }

        @XmlAttribute
        public Double getCreatorMedian() {
            return creatorMedian;
        }

        public void setCreatorMedian(Double creatorMedian) {
            this.creatorMedian = creatorMedian;
        }

        @XmlAttribute
        public Double getKeywordMean() {
            return keywordMean;
        }

        public void setKeywordMean(Double keywordMean) {
            this.keywordMean = keywordMean;
        }

        @XmlAttribute
        public Double getKeywordMedian() {
            return keywordMedian;
        }

        public void setKeywordMedian(Double keywordMedian) {
            this.keywordMedian = keywordMedian;
        }

        @XmlElementWrapper(name = "collaborators")
        @XmlElement(name = "collaborator")
        public List<LogPart> getCollaboratorLogPart() {
            return collaboratorLogPart;
        }

        public void setCollaboratorLogPart(List<LogPart> collaboratorLogPart) {
            this.collaboratorLogPart = collaboratorLogPart;
        }

        @XmlElementWrapper(name = "keywords")
        @XmlElement(name = "keyword")
        public List<LogPart> getKeywordLogPart() {
            return keywordLogPart;
        }

        public void setKeywordLogPart(List<LogPart> keywordLogPart) {
            this.keywordLogPart = keywordLogPart;
        }

        @XmlAttribute(name = "submitterRef")
        @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
        public Creator getCreator() {
            return creator;
        }

        public void setPerson(Creator creator) {
            this.creator = creator;
        }

        @XmlAttribute
        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "creatorLogPart")
    public static class LogPart {

        private Long id;
        private Long count;
        private String name;
        private String simpleClassName;

        @XmlAttribute(name = "id")
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @XmlAttribute
        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute
        public String getSimpleClassName() {
            return simpleClassName;
        }

        public void setSimpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
        }

    }

    private Map<Creator, Integer> incrementCreators(Set<Long> resourceIds, List<Long> userIdsToIgnoreInLargeTasks) {
        Map<Creator, Integer> counts = entityService.getRelatedCreatorCounts(resourceIds);
        return counts;
    }

    private Map<Keyword, Integer> incrementKeywords(Set<Long> resourceIds) {
        return genericKeywordService.getRelatedKeywordCounts(resourceIds);
    }

    @Override
    public void process(Creator account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    public void setDaysToRun(int i) {
        this.daysToRun = i;
    }

    private int getDaysToRun() {
        return daysToRun;
    }

}
