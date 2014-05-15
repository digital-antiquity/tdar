package org.tdar.core.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;

public class ImportServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ImportService importService;
    
    @Test
    @Rollback
    public void testClone() {
        Document document  = genericService.find(Document.class, 4287L);
//        Document document  =new Document();
        Long id = document.getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document);
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        
    }
}
