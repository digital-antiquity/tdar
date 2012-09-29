package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.dao.GenericDao.FindOptions;

public class GenericServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    private static final Long TRANSIENT_PERSON_ID = -1L;

    @Before
    public void init() {

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
        int activeDocuments = resourceService.countActiveResources(Document.class).intValue();
        assertEquals(activeDocuments + " active documents", activeDocuments, documents.size());
    }

    @Test
    @Rollback
    public void testFindOrCreateByPersistableLong() {
        // save a transient person
        Person bum = new Person();
        bum.setId(TRANSIENT_PERSON_ID);
        bum.setName("Brand Newperson");
        Person blessed = genericService.findByExample(Person.class, bum,FindOptions.FIND_FIRST_OR_CREATE).get(0);
        flush();
        assertFalse(blessed.getId().equals(TRANSIENT_PERSON_ID));
        assertEquals("name should be the same", bum.getName(), blessed.getName());
    }

}
