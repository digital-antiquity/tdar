package org.tdar.core.service.processes;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.XmlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.google.common.primitives.Doubles;

@Component
public class CreatorAnalysisProcess extends ScheduledBatchProcess<Creator> {

    private static final long serialVersionUID = 581887107336388520L;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private XmlService xmlService;

    public String getDisplayName() {
        return "Creator Analytics Process";
    }

    @Override
    public int getBatchSize() {
        return 100;
    }

    public Class<Creator> getPersistentClass() {
        return Creator.class;
    }

    @Override
    public List<Long> findAllIds() {
        List<Creator> results = genericDao.findAll(getPersistentClass());
        if (CollectionUtils.isNotEmpty(results)) {
            return Persistable.Base.extractIds(results);
        }
        return null;
    }

    @Override
    public void execute() {
        List<Creator> creators = genericDao.findAll(getPersistentClass(), getNextBatch());
        List<Long> userIdsToIgnoreInLargeTasks = getTdarConfiguration().getUserIdsToIgnoreInLargeTasks();
        for (Creator creator : creators) {
            logger.info("~~~~~ " + creator + " ~~~~~~");
            if (userIdsToIgnoreInLargeTasks.contains(creator.getId())) {
                continue;
            }
            Map<Creator, Double> collaborators = new HashMap<Creator, Double>();
            Map<Keyword, Double> keywords = new HashMap<Keyword, Double>();
            int total = 0;
            QueryBuilder query = searchService.generateQueryForRelatedResources(creator, null);
            try {
                FullTextQuery search = searchService.search(query, null);
                ScrollableResults results = search.scroll(ScrollMode.FORWARD_ONLY);
                total = search.getResultSize();
                if (total == 0)
                    continue;
                while (results.next()) {
                    Resource resource = (Resource) results.get()[0];
                    incrementKeywords(keywords, resource);
                    incrementCreators(creator, collaborators, resource,userIdsToIgnoreInLargeTasks);
                }
            } catch (Exception e) {
                logger.warn("Exception {}", e);
            }

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

            for (Entry<Creator, Double> entrySet : collaborators.entrySet()) {
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue().longValue());
                Creator key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(key.getClass().getSimpleName());
                part.setName(key.getProperName());
                log.getCollaboratorLogPart().add(part);
            }
            for (Entry<Keyword, Double> entrySet : keywords.entrySet()) {
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue().longValue());
                Keyword key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(key.getClass().getSimpleName());
                part.setName(key.getLabel());
                log.getKeywordLogPart().add(part);
            }

            Collections.sort(log.getCollaboratorLogPart(), new LogPartComparator());
            Collections.sort(log.getKeywordLogPart(), new LogPartComparator());

            try {
                xmlService.generateFOAF(creator, log);
                xmlService.convertToXML(log, new FileWriter(new File(TdarConfiguration.getInstance().getCreatorFOAFDir() +"/" + creator.getId() + ".xml")));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error("exception: {} ", e);
            }
        }
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

    private void incrementCreators(Creator current, Map<Creator, Double> collaborators, Resource resource, List<Long> userIdsToIgnoreInLargeTasks) {
        for (Creator creator : resource.getRelatedCreators()) {
            if (ObjectUtils.equals(creator, current) || creator == null || StringUtils.isBlank(creator.getProperName()))
                continue;

            if (CollectionUtils.isNotEmpty(userIdsToIgnoreInLargeTasks) && userIdsToIgnoreInLargeTasks.contains(current.getId())) {
                continue;
            }

            Double count = collaborators.get(creator);
            if (count == null) {
                count = 0.0;
            }
            count++;
            collaborators.put(creator, count);
        }
    }

    private void incrementKeywords(Map<Keyword, Double> keywords, Resource resource) {
        for (Keyword kwd : resource.getAllActiveKeywords()) {
            Double count = keywords.get(kwd);
            if (count == null) {
                count = 0.0;
            }
            count++;
            keywords.put(kwd, count);
        }
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
        return true;
    }

}
