package org.tdar.core.service.processes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.SearchService;
import org.tdar.core.service.external.EmailService;
import org.tdar.search.query.builder.QueryBuilder;

@Component
public class PersonAnalysisProcess extends ScheduledBatchProcess<Person> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SearchService searchService;

    public String getDisplayName() {
        return "Overdrawn Account Process";
    }

    @Override
    public int getBatchSize() {
        return 100;
    }

    public Class<Person> getPersistentClass() {
        return Person.class;
    }

    @Override
    public List<Long> findAllIds() {
        List<Person> results = genericDao.findAll(getPersistentClass());
        if (CollectionUtils.isNotEmpty(results)) {
            return Persistable.Base.extractIds(results);
        }
        return null;
    }

    @Override
    public void execute() {
        List<Person> people = genericDao.findAll(getPersistentClass(), getNextBatch());
        for (Person person : people) {
            Map<Creator, Long> collaborators = new HashMap<>();
            Map<Keyword, Long> keywords = new HashMap<>();
            QueryBuilder query = searchService.generateQueryForRelatedResources(person, null);
            try {
                FullTextQuery search = searchService.search(query, null);
                ScrollableResults results = search.scroll(ScrollMode.FORWARD_ONLY);
                while (results.next()) {
                    Resource resource = (Resource) results.get()[0];
                    incrementKeywords(keywords, resource);
                    incrementCreators(person, collaborators, resource);
                }
            } catch (Exception e) {
                logger.warn("Exception {}", e);
            }
            logger.info("~~~~~ " + person + " ~~~~~~");
            logger.info("Collaborators: {} ", collaborators);
            logger.info("Keywords: {} ", keywords);
        }
    }

    private void incrementCreators(Person person, Map<Creator, Long> collaborators, Resource resource) {
        for (Creator creator : resource.getRelatedCreators()) {
            if (ObjectUtils.equals(creator, person) || creator == null)
                continue;
            Long count = collaborators.get(creator);
            if (count == null) {
                count = 0L;
            }
            count++;
            collaborators.put(creator, count);
        }
    }

    private void incrementKeywords(Map<Keyword, Long> keywords, Resource resource) {
        for (Keyword kwd : resource.getAllActiveKeywords()) {
            Long count = keywords.get(kwd);
            if (count == null) {
                count = 0L;
            }
            count++;
            keywords.put(kwd, count);
        }
    }

    @Override
    public void process(Person account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
