package org.tdar.core.service.processes;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
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
import org.tdar.core.service.XmlService;
import org.tdar.core.service.external.EmailService;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.ibm.icu.impl.ICUService.Key;

@Component
public class PersonAnalysisProcess extends ScheduledBatchProcess<Person> {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private XmlService xmlService;

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

            PersonInfoLog log = new PersonInfoLog();
            log.setPerson(person);
            for (Entry<Creator, Long> entrySet : collaborators.entrySet()) {
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue());
                Creator key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(key.getClass().getSimpleName());
                part.setName(key.getProperName());
                log.getCollaboratorLogPart().add(part);
            }
            for (Entry<Keyword, Long> entrySet : keywords.entrySet()) {
                LogPart part = new LogPart();
                part.setCount(entrySet.getValue());
                Keyword key = entrySet.getKey();
                part.setId(key.getId());
                part.setSimpleClassName(key.getClass().getSimpleName());
                part.setName(key.getLabel());
                log.getKeywordLogPart().add(part);
            }
            
            logger.info("~~~~~ " + person + " ~~~~~~");
            try {
                File dir = new File(TdarConfiguration.getInstance().getPersonalFileStoreLocation(),"creatorInfo");
                dir.mkdir();
                FileWriter writer = new FileWriter(new File(dir, person.getId() + ".xml"));
                xmlService.convertToXML(log,writer);
                IOUtils.closeQuietly(writer);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "personInfoLog")
    public static class PersonInfoLog {
        private List<LogPart> collaboratorLogPart = new ArrayList<>();
        private List<LogPart> keywordLogPart = new ArrayList<>();
        private Person person;

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
        public Person getPerson() {
            return person;
        }

        public void setPerson(Person person) {
            this.person = person;
        }

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "personLogPart")
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
