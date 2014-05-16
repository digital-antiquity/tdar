package org.tdar.core.service;

import java.util.Set;

import org.apache.commons.collections.SetUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;

public class ImportServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ImportService importService;
    
    @Test
    @Rollback
    public void testClone() throws Exception {
        Document document  = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        Set<CoverageDate> coverageDates = newDoc.getCoverageDates();
        assertNotEmpty(coverageDates);
        document  = genericService.find(Document.class, 4287L);
        Set<CoverageDate> coverageDates2 = document.getCoverageDates();
        assertNotEmpty(coverageDates2);
        assertNotEquals(coverageDates.iterator().next().getId(), coverageDates2.iterator().next().getId());
        
    }
}
