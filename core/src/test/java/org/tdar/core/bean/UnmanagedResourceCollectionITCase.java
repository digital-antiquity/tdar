package org.tdar.core.bean;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.resource.ErrorHandling;

public class UnmanagedResourceCollectionITCase extends AbstractIntegrationTestCase {
    
    @Autowired
    ResourceCollectionService resourceCollectionService;
    
    @Test
    @Rollback(true)
    public void testUnmanagedConfidentialResource() throws InstantiationException, IllegalAccessException, FileNotFoundException {
        //create confidential document with owner of BasicUser
        Document document = createAndSaveDocumentWithFileAndUseDefaultUser();
        document.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.CONFIDENTIAL);
        genericService.saveOrUpdate(document);

        TdarUser tdarUser = createAndSaveNewUser();

        // create a collection
        ResourceCollection collection = createAndSaveNewResourceCollection("test umanaged");
        collection.getAuthorizedUsers().add(new AuthorizedUser(getAdminUser(), tdarUser, GeneralPermissions.ADMINISTER_SHARE));
        genericService.saveOrUpdate(collection.getAuthorizedUsers());
        genericService.saveOrUpdate(collection);
      
        resourceCollectionService.addResourceCollectionToResource(document, document.getManagedResourceCollections(), getBasicUser(), true, 
                ErrorHandling.NO_VALIDATION, collection, CollectionResourceSection.UNMANGED);
        
        // get the Ids
        Long userId = tdarUser.getId();
        Long documentId = document.getId();
        Long collectionId = collection.getId();
        tdarUser = null;
        document = null;
        collection = null;
        // flush everything into the database
        genericService.synchronize();

        // find again just in case (clears transient fields/ relationships)
        tdarUser = genericService.find(TdarUser.class, userId);
        document = genericService.find(Document.class, documentId);
        collection = genericService.find(ResourceCollection.class, collectionId);
        assertFalse(authenticationAndAuthorizationService.canEdit(tdarUser, document));
        assertTrue(collection.getUnmanagedResources().contains(document));
        assertFalse(collection.getManagedResources().contains(document));
    }
    
    @Test
    @Ignore
    @Rollback(true)
    public void testUnmanagedDraftResource() {
        
    }

}
