package org.tdar.core.service;

import java.util.Set;

import org.apache.commons.collections.SetUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class ImportServiceITCase extends AbstractDataIntegrationTestCase {

    @Autowired
    ImportService importService;
    
    @Autowired
    XmlService xmlService;
    
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
        logger.debug(xmlService.convertToXML(newDoc));
    }

    @Test
    @Rollback
    public void testDataset() throws Exception {
        Dataset dataset = setupAndLoadResource(AbstractDataIntegrationTestCase.SPITAL_DB_NAME, Dataset.class);
        Long id = dataset.getId();
        genericService.synchronize();
        Dataset newDoc = importService.cloneResource(dataset, getAdminUser());
        genericService.synchronize();
        logger.debug(xmlService.convertToXML(newDoc));



    }
}
