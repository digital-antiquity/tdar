package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.dao.GenericDao.FindOptions;

public class GenericServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    private static final Long TRANSIENT_PERSON_ID = -1L;

    @Before
    public void init() {

    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testFindAllWithCache() {
        for (Class<? extends Keyword> cls : Arrays.asList(InvestigationType.class, MaterialKeyword.class)) {
            assertEquals(new HashSet<Keyword>(genericService.findAll(cls)), new HashSet<Keyword>(genericService.findAllWithCache(cls)));
        }
    }

    @Test
    @Rollback
    public void testFindAllWithIds() {
        // iota
        List<Long> ids = new ArrayList<Long>();
        int numberOfIds = 20;
        for (int id = 0; id < numberOfIds; id++) {
            ids.add(Long.valueOf(id + 1));
        }
        List<InvestigationType> investigationTypes = genericService.findAll(InvestigationType.class, ids);
        assertEquals(numberOfIds, investigationTypes.size());
        Collections.sort(investigationTypes, new Comparator<InvestigationType>() {
            @Override
            public int compare(InvestigationType a, InvestigationType b) {
                return a.getId().compareTo(b.getId());
            }
        });
        assertEquals(investigationTypes, genericService.findAllSorted(InvestigationType.class));
    }

    @Test
    @Rollback
    public void testFindRandom() {
        List<InvestigationType> randomInvestigationTypes = genericService.findRandom(InvestigationType.class, 10);
        assertEquals(10, randomInvestigationTypes.size());
        logger.debug("{}", randomInvestigationTypes);
    }

    @Test
    @Rollback
    public void testFindActiveRandomResources() {
        List<Document> documents = genericService.findRandom(Document.class, 10);
        int activeDocuments = resourceService.countActiveResources(ResourceType.DOCUMENT).intValue();
        for (Document doc : documents) {
            assertTrue(doc.isActive());
        }
        assertTrue(activeDocuments + " active documents", activeDocuments >= documents.size());
    }

    @Test
    @Rollback
    public void testFindOrCreateByPersistableLong() {
        // save a transient person
        Person bum = new Person();
        bum.setId(TRANSIENT_PERSON_ID);
        bum.setName("Brand Newperson");
        Person blessed = genericService.findByExample(Person.class, bum, FindOptions.FIND_FIRST_OR_CREATE).get(0);
        flush();
        assertFalse(blessed.getId().equals(TRANSIENT_PERSON_ID));
        assertEquals("name should be the same", bum.getName(), blessed.getName());
    }

}
