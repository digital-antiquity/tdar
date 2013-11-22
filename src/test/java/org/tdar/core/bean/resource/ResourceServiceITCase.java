package org.tdar.core.bean.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;

public class ResourceServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private ResourceService resourceService;

    @Test
    @Rollback
    public void testSaveHasResource() throws InstantiationException, IllegalAccessException {
        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());
    }

    @Test
    public void testFindSimple() throws InterruptedException {
        Thread.sleep(4000l);
        genericService.findAll(Document.class);
        long id = Long.parseLong(TestConstants.TEST_DOCUMENT_ID);
        newWay(id);
        oldWay(id);
        newWay(id);
        oldWay(id);
    }

    private Resource newWay(long id) {
        long time = System.currentTimeMillis();
        List<Resource> docs = resourceService.findSkeletonsForSearch(false, id);
        Resource doc = docs.get(0);
        doc.logForTiming();
        logger.info("NEW TOTAL TIME: {} ", System.currentTimeMillis() - time);
        return doc;
    }

    private void oldWay(long id) {
        long time = System.currentTimeMillis();
        List<Resource> recs = resourceService.findOld(id);
        Resource rec2 = recs.get(0);
        rec2.logForTiming();
        logger.info("OLD TOTAL TIME: {} ", System.currentTimeMillis() - time);
    }
    
    @Test
    @Rollback
    public void testSaveHasResourceExistingNull() throws InstantiationException, IllegalAccessException {

        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = new ArrayList<CoverageDate>();
        doc.setCoverageDates(null);
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());

    }

    @Test
    @Rollback
    public void testSaveHasResourceIncomingNulLCase() throws InstantiationException, IllegalAccessException {

        Resource doc = (Document) generateDocumentWithUser();
        doc.getCoverageDates().add(new CoverageDate(CoverageType.CALENDAR_DATE, 1390, 1590));
        List<CoverageDate> dates = null;
        resourceService.saveHasResources(doc, false, ErrorHandling.VALIDATE_SKIP_ERRORS, dates,
                doc.getCoverageDates(), CoverageDate.class);
        assertEquals(0, doc.getCoverageDates().size());

    }
}
