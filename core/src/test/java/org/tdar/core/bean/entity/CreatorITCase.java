package org.tdar.core.bean.entity;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.service.GenericService;

public class CreatorITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private PersonDao personDao;

    @Autowired
    private InstitutionDao institutionDao;

    
    @Test
    @Rollback
    public void testInstitutionMerge() {
        Institution master = new Institution("master");
        Institution dup = new Institution("dup");
        genericService.saveOrUpdate(master,dup);
        dup.setStatus(Status.DUPLICATE);
        master.getSynonyms().add(dup);
        genericService.saveOrUpdate(dup,master);
        genericService.synchronize();
        assertEquals(master,institutionDao.findAuthorityFromDuplicate(dup));
    }

    
    @Test
    @Rollback
    public void testPersonMerge() {
        Person master = createAndSaveNewPerson("aa@bb.cc", "master");
        Person dup = createAndSaveNewPerson("aa@bb.ccd", "dup");

        genericService.saveOrUpdate(master,dup);
        dup.setStatus(Status.DUPLICATE);
        master.getSynonyms().add(dup);
        genericService.saveOrUpdate(dup,master);
        genericService.synchronize();

        assertEquals(master,personDao.findAuthorityFromDuplicate(dup));
    }

    
    
}
