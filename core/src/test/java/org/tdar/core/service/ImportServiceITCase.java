package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;

public class ImportServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ImportService importService;

    @Autowired
    SerializationService serializationService;

    @SuppressWarnings("deprecation")
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
        assertNotEmpty("should have coverage dates", coverageDates);
        document = genericService.find(Document.class, 4287L);
        Set<CoverageDate> coverageDates2 = document.getCoverageDates();
        assertNotEmpty("should have coverage dates", coverageDates2);
        assertNotEquals(coverageDates.iterator().next().getId(), coverageDates2.iterator().next().getId());
        logger.debug(serializationService.convertToXML(newDoc));
    }

    
    @Test
    @Rollback
    public void testImportWithAuthorizedUser() throws Exception {
        Document document = new Document();
        document.setTitle("test");
        document.setDescription("test description");
        document.setDocumentType(DocumentType.BOOK);
        document.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), new TdarUser(null, null, null, getBillingUser().getUsername()), GeneralPermissions.ADMINISTER_SHARE));
        Document newDoc = importService.bringObjectOntoSession(document, getAdminUser(), null, null, true);
        genericService.synchronize();
        Set<AuthorizedUser> authorizedUsers = newDoc.getAuthorizedUsers();
        logger.debug("AU:{}",authorizedUsers);
        assertEquals(authorizedUsers.iterator().next().getUser(), getBillingUser());
    }


    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testCloneInternalCollection() throws Exception {
        Document document = genericService.find(Document.class, 4287L);
        Long id = document.getId();
        ResourceCollection rc = new ResourceCollection(document,getAdminUser());
        rc.setDescription("test");
        rc.setName("name");
        rc.markUpdated(getAdminUser());
        genericService.saveOrUpdate(rc);
        document.getSharedCollections().add(rc);
        
        genericService.saveOrUpdate(document);
        logger.debug("IRC:{}",document.getAuthorizedUsers());
        genericService.synchronize();
        Document newDoc = importService.cloneResource(document, getAdminUser());
        genericService.synchronize();
        assertNotEquals(id, newDoc.getId());
        logger.debug("oldId: {} newId: {}", id, newDoc.getId());
        Assert.assertNotNull(newDoc.getId());
        logger.debug("{}", document.getAuthorizedUsers());
        logger.debug("{}", newDoc.getAuthorizedUsers());
        // add one for the new authorized user
        Assert.assertEquals(document.getAuthorizedUsers().size() +1, newDoc.getAuthorizedUsers().size());
        logger.debug(serializationService.convertToXML(newDoc));
    }
    

    

}
