package org.tdar.core.service;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

public class ImportServiceITCase extends AbstractDataIntegrationTestCase {

    @Autowired
    ImportService importService;

    @Autowired
    SerializationService serializationService;

    @Test
    @Rollback
    public void testClone() throws Exception {
        Document document = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        Set<CoverageDate> coverageDates = newDoc.getCoverageDates();
        assertNotEmpty(coverageDates);
        document = genericService.find(Document.class, 4287L);
        Set<CoverageDate> coverageDates2 = document.getCoverageDates();
        assertNotEmpty(coverageDates2);
        assertNotEquals(coverageDates.iterator().next().getId(), coverageDates2.iterator().next().getId());
        logger.debug(serializationService.convertToXML(newDoc));
    }

    @Test
    @Rollback
    public void testDataset() throws Exception {
        Dataset dataset = setupAndLoadResource(AbstractDataIntegrationTestCase.SPITAL_DB_NAME, Dataset.class);
        Long id = dataset.getId();
        genericService.synchronize();
        Dataset newDoc = importService.cloneResource(dataset, getAdminUser());
        genericService.synchronize();
        logger.debug(serializationService.convertToXML(newDoc));
    }
    
    @Override
    protected String getTestFilePath() {
        return TestConstants.TEST_DATA_INTEGRATION_DIR;
    }

    @Test
    @Rollback
    public void testCodingSheet() throws Exception {
        Long id = 43000L;
        CodingSheet cs = genericService.find(CodingSheet.class, id);
        genericService.synchronize();
        CodingSheet newDoc = importService.cloneResource(cs, getAdminUser());
        genericService.synchronize();
        logger.debug(serializationService.convertToXML(newDoc));

    }
}
