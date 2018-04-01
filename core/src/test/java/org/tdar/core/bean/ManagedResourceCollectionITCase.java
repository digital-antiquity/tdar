package org.tdar.core.bean;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.collection.ResourceCollectionService;

@Ignore
public class ManagedResourceCollectionITCase extends AbstractIntegrationTestCase {

    @Autowired
    ResourceCollectionService resourceCollectionService;

    private TdarUser user2;
    private Document user2PublicDocument;
    private Document user2ConfidentialDocument;
    private ResourceCollection user2Collection;

    private TdarUser user1;
    private Dataset user1ConfidentialDataset;
    private Dataset user1PublicDataset;
    private ResourceCollection user1Collection;

    /**
     * private TdarUser viewer;
     * 
     * @BeforeClass
     *              public void setUpClass() throws InstantiationException, IllegalAccessException, FileNotFoundException {
     *              user1 = createAndSaveNewUser();
     *              user1ConfidentialDataset = createAndSaveNewResource(Dataset.class, user1, "Owner's dataset");
     *              // AB: a resource should never have 'ADMINISTER' rights on it
     *              user1ConfidentialDataset.getAuthorizedUsers()
     *              .add(new AuthorizedUser(user1, user1, Permissions.MODIFY_RECORD));
     * 
     *              user1PublicDataset = createAndSaveNewDataset();
     *              user1PublicDataset.getAuthorizedUsers()
     *              .add(new AuthorizedUser(user1, user1, Permissions.MODIFY_RECORD));
     * 
     *              genericService.save(user1ConfidentialDataset);
     *              genericService.save(user1ConfidentialDataset.getAuthorizedUsers());
     * 
     *              user1Collection = createAndSaveNewResourceCollection("Owner's resource collection");
     *              user1Collection.getAuthorizedUsers().add(new AuthorizedUser(user1, user1, Permissions.ADMINISTER_COLLECTION));
     *              genericService.save(user1Collection.getAuthorizedUsers());
     * 
     *              // Create some other user's collection and add a public and confidential
     *              // resource to it.
     *              user2 = createAndSaveNewUser();
     *              user2PublicDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
     *              user2PublicDocument.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.PUBLIC);
     *              genericService.saveOrUpdate(user2PublicDocument);
     * 
     *              user2ConfidentialDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
     *              user2ConfidentialDocument.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.CONFIDENTIAL);
     *              user2ConfidentialDocument.getAuthorizedUsers().add(new AuthorizedUser(user2, user2, Permissions.MODIFY_RECORD));
     *              genericService.saveOrUpdate(user2ConfidentialDocument);
     *              genericService.saveOrUpdate(user2ConfidentialDocument.getAuthorizedUsers());
     * 
     *              user2Collection = createAndSaveNewResourceCollection("Someone else's resource collection");
     *              user2Collection.getAuthorizedUsers().add(new AuthorizedUser(user2, user2, Permissions.ADMINISTER_COLLECTION));
     *              user2Collection.getManagedResources().add(user2ConfidentialDocument);
     *              user2Collection.getUnmanagedResources().add(user2PublicDocument);
     *              genericService.saveOrUpdate(user2Collection);
     *              genericService.saveOrUpdate(user2Collection.getManagedResources());
     *              genericService.saveOrUpdate(user2Collection.getUnmanagedResources());
     *              genericService.saveOrUpdate(user2Collection.getAuthorizedUsers());
     * 
     *              viewer = createAndSaveNewUser();
     *              }
     * 
     * @Test
     * @Rollback(true)
     *                 public void testUnmanagedConfidentialResource()
     *                 throws InstantiationException, IllegalAccessException, FileNotFoundException {
     *                 // create confidential document with owner of BasicUser
     *                 Document document = createAndSaveDocumentWithFileAndUseDefaultUser();
     *                 document.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.CONFIDENTIAL);
     *                 genericService.saveOrUpdate(document);
     * 
     *                 TdarUser tdarUser = createAndSaveNewUser();
     * 
     *                 // create a collection
     *                 ResourceCollection collection = createAndSaveNewResourceCollection("test umanaged");
     *                 collection.getAuthorizedUsers()
     *                 .add(new AuthorizedUser(getAdminUser(), tdarUser, Permissions.ADMINISTER_COLLECTION));
     *                 genericService.saveOrUpdate(collection.getAuthorizedUsers());
     *                 genericService.saveOrUpdate(collection);
     * 
     *                 resourceCollectionService.addResourceCollectionToResource(document, document.getManagedResourceCollections(),
     *                 getBasicUser(), true, ErrorHandling.NO_VALIDATION, collection, CollectionResourceSection.UNMANAGED);
     * 
     *                 // get the Ids
     *                 Long userId = tdarUser.getId();
     *                 Long documentId = document.getId();
     *                 Long collectionId = collection.getId();
     *                 tdarUser = null;
     *                 document = null;
     *                 collection = null;
     * 
     *                 // flush everything into the database
     *                 genericService.synchronize();
     * 
     *                 // find again just in case (clears transient fields/ relationships)
     *                 tdarUser = genericService.find(TdarUser.class, userId);
     *                 document = genericService.find(Document.class, documentId);
     *                 collection = genericService.find(ResourceCollection.class, collectionId);
     *                 assertFalse(authenticationAndAuthorizationService.canEdit(tdarUser, document));
     *                 assertTrue(collection.getUnmanagedResources().contains(document));
     *                 assertFalse(collection.getManagedResources().contains(document));
     *                 }
     * 
     * @Test
     * @Rollback
     *           public void testAddPublicResourceToManagedCollection() {
     * 
     *           }
     * 
     * @Test
     * @Rollback
     *           private void testAddPrivateResourceToManagedCollection() {
     * 
     *           }
     * 
     * @Test
     * @Rollback
     *           private void testAddPrivateResourceToUnmanagedCollection() {
     * 
     *           }
     */
    // @Ignore
    @Test
    @Rollback
    public void testManagedCollections() throws InstantiationException, IllegalAccessException, FileNotFoundException {

        /***
         * 
         * User 1 -- Collection
         * - Managed
         * - Confidentaial Dataset (#1)
         * - Confidential Resource (User 2's, #3)
         * 
         * - Unmanaged
         * - Public Dataset (#2)
         * - Public Resource (User 2's #4)
         * 
         * User 2 -- Collection
         * - Managed
         * - Confidential Resource (#3)
         * - Unmanaged
         * - Public Resource (#4)
         * 
         * 
         * 
         * 
         * 
         */

        /**
         * 
         * // Create an owner and their own resources
         * 
         * 
         * 
         * // TODO: How to I set the permissions for this collection as private for
         * // others?
         * // AB: not sure I follow, do you mean "hidden" as in other's can't see
         * // it?
         * 
         * // Attempt to add a document the user doesn't have permission to as a
         * // managed resource.
         * user1Collection.getManagedResources().add(user2ConfidentialDocument);
         * user1Collection.getManagedResources().add(user1ConfidentialDataset);
         * 
         * user1Collection.getUnmanagedResources().add(user1PublicDataset);
         * user1Collection.getUnmanagedResources().add(someoneElsesPublicDocument);
         * genericService.save(user1Collection);
         * 
         * TdarUser user = createAndSaveNewUser();
         * // Add permission for the user to add/remove resources to the owner's
         * // collection.
         * user1Collection.getAuthorizedUsers().add(new AuthorizedUser(user, user, Permissions.ADMINISTER_COLLECTION));
         * genericService.saveOrUpdate(user1Collection.getAuthorizedUsers());
         * 
         * // Create a viewer who has no association to the owner's collection.
         * 
         * // get the Ids
         * Long ownerId = user1.getId();
         * Long otherUserId = user2.getId();
         * Long userId = user.getId();
         * Long viewerId = viewer.getId();
         * 
         * Long otherCollectionId = someoneElsesCollection.getId();
         * Long ownerCollectionId = user1Collection.getId();
         * 
         * user2 = null;
         * user1 = null;
         * user = null;
         * viewer = null;
         * user1Collection = null;
         * someoneElsesCollection = null;
         * 
         * // flush everything into the database
         * genericService.synchronize();
         * 
         * // find again just in case (clears transient fields/ relationships)
         * user1 = genericService.find(TdarUser.class, ownerId);
         * user2 = genericService.find(TdarUser.class, otherUserId);
         * user = genericService.find(TdarUser.class, userId);
         * viewer = genericService.find(TdarUser.class, viewerId);
         * 
         * user1Collection = genericService.find(ResourceCollection.class, ownerCollectionId);
         * ;
         * someoneElsesCollection = genericService.find(ResourceCollection.class, otherCollectionId);
         * ;
         * 
         * // Test a managed collection the user owns (created).
         * 
         * // Create a colleciton
         * 
         * // create a resource (public)
         * // create a resource (confidential)
         * 
         * // create a second resource (public)
         * // create a third resrouce (confidential)
         * 
         * // add all the resources to the collection
         * 
         * //The 3rd Party can edit their own dowcument.
         * assertTrue(authenticationAndAuthorizationService.canEdit(user2, user2ConfidentialDocument));
         * 
         * // The owner can edit their own document.
         * assertTrue(authenticationAndAuthorizationService.canEdit(user1, user1Collection));
         * 
         * // Test that the confidental document exists in the manages resources,
         * // but the owner can't access it
         * assertTrue(user1Collection.getManagedResources().contains(user2ConfidentialDocument));
         * assertFalse(authenticationAndAuthorizationService.canEdit(user1, user2ConfidentialDocument));
         * 
         * 
         * // Test that someone elses public document can be added as a resouce,
         * // can be viewed, but not edited.
         * assertTrue(user1Collection.getUnmanagedResources().contains(someoneElsesPublicDocument));
         * assertFalse(authenticationAndAuthorizationService.canEdit(user1, someoneElsesPublicDocument));
         * logger.debug("{} {}", user1, someoneElsesPublicDocument);
         * assertTrue(authenticationAndAuthorizationService.canView(user1, someoneElsesPublicDocument));
         * 
         * // test the user
         * // test if the user can access the resources on the owner's collection.
         * assertTrue(authenticationAndAuthorizationService.canView(user, user2ConfidentialDocument));
         * assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(user,
         * user2ConfidentialDocument));
         * 
         * assertTrue(authenticationAndAuthorizationService.canEdit(user, user1Collection));
         * assertFalse(
         * authenticationAndAuthorizationService.canViewConfidentialInformation(user, user1ConfidentialDataset));
         * assertTrue(authenticationAndAuthorizationService.canView(user, user1PublicDataset));
         * 
         * // create a viewer
         * // test if the user has permission to each of the resources
         * assertFalse(authenticationAndAuthorizationService.canEdit(viewer, user2ConfidentialDocument));
         * // AB:dup?
         * assertFalse(authenticationAndAuthorizationService.canEdit(viewer, user2ConfidentialDocument));
         * assertFalse(authenticationAndAuthorizationService.canEdit(viewer, user1PublicDataset));
         * assertFalse(authenticationAndAuthorizationService.canEdit(viewer, user1ConfidentialDataset));
         * 
         * assertFalse(authenticationAndAuthorizationService.canView(viewer, user2ConfidentialDocument));
         * assertTrue(authenticationAndAuthorizationService.canView(viewer, someoneElsesPublicDocument));
         * assertTrue(authenticationAndAuthorizationService.canView(viewer, user1PublicDataset));
         * assertFalse(authenticationAndAuthorizationService.canView(viewer, user1ConfidentialDataset));
         **/
    }
}
