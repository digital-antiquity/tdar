package org.tdar.core.service;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.resource.Document;

public class ImportServiceITCase extends AbstractIntegrationTestCase {

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
    public void testCloneInternalCollection() throws Exception {
        Document document = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        ResourceCollection rc = new ResourceCollection(document,getAdminUser());
        rc.setSortBy(SortOption.TITLE);
        rc.setDescription("test");
        rc.setName("name");
        rc.setType(CollectionType.SHARED);
        rc.markUpdated(getAdminUser());
        genericService.saveOrUpdate(rc);
        document.getResourceCollections().add(rc);
        
        genericService.saveOrUpdate(document);
        logger.debug("IRC:{}",document.getInternalResourceCollection());
        Long ircid = document.getInternalResourceCollection().getId();
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        logger.debug("oldIrId:"  + ircid.longValue() + " newIRID:"+ newDoc.getInternalResourceCollection().getId().longValue());
        Assert.assertNotEquals(ircid.longValue(), newDoc.getInternalResourceCollection().getId().longValue());
        Assert.assertEquals(newDoc.getId().longValue(), newDoc.getInternalResourceCollection().getResources().iterator().next().getId().longValue());
        logger.debug(serializationService.convertToXML(newDoc));
    }
    

    

}
