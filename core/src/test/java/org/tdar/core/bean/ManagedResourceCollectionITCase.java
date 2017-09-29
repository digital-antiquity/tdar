package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.resource.ErrorHandling;

public class ManagedResourceCollectionITCase extends AbstractIntegrationTestCase {

	@Autowired
	ResourceCollectionService resourceCollectionService;

	@Test
	@Rollback(true)
	public void testUnmanagedConfidentialResource()
			throws InstantiationException, IllegalAccessException, FileNotFoundException {
		// create confidential document with owner of BasicUser
		Document document = createAndSaveDocumentWithFileAndUseDefaultUser();
		document.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.CONFIDENTIAL);
		genericService.saveOrUpdate(document);

		TdarUser tdarUser = createAndSaveNewUser();

		// create a collection
		ResourceCollection collection = createAndSaveNewResourceCollection("test umanaged");
		collection.getAuthorizedUsers()
				.add(new AuthorizedUser(getAdminUser(), tdarUser, Permissions.ADMINISTER_COLLECTION));
		genericService.saveOrUpdate(collection.getAuthorizedUsers());
		genericService.saveOrUpdate(collection);

		resourceCollectionService.addResourceCollectionToResource(document, document.getManagedResourceCollections(),
				getBasicUser(), true, ErrorHandling.NO_VALIDATION, collection, CollectionResourceSection.UNMANGED);

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

//	@Ignore
	@Test
	@Rollback
	public void testManagedCollections()
			throws InstantiationException, IllegalAccessException, FileNotFoundException {

		// Create some other user's collection and add a public and confidential
		// resource to it.
		TdarUser someOtherOwner = createAndSaveNewUser();

		Document someoneElsesPublicDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
		someoneElsesPublicDocument.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.PUBLIC);
		//someoneElsesPublicDocument.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner, someOtherOwner, GeneralPermissions.ADMINISTER_COLLECTION));
		genericService.saveOrUpdate(someoneElsesPublicDocument);
		//genericService.saveOrUpdate(someoneElsesPublicDocument.getAuthorizedUsers());

		Document someoneElsesConfidentialDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
		someoneElsesConfidentialDocument.getFirstInformationResourceFile()
				.setRestriction(FileAccessRestriction.CONFIDENTIAL);
		someoneElsesConfidentialDocument.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner, someOtherOwner, Permissions.ADMINISTER_COLLECTION));
		genericService.saveOrUpdate(someoneElsesConfidentialDocument);
		genericService.saveOrUpdate(someoneElsesConfidentialDocument.getAuthorizedUsers());

		ResourceCollection someoneElsesCollection = createAndSaveNewResourceCollection(
				"Someone else's resource collection");
		someoneElsesCollection.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner,someOtherOwner,Permissions.ADMINISTER_COLLECTION));
		
		someoneElsesCollection.getManagedResources().add(someoneElsesConfidentialDocument);
		someoneElsesCollection.getUnmanagedResources().add(someoneElsesPublicDocument);
		genericService.saveOrUpdate(someoneElsesCollection);
		genericService.saveOrUpdate(someoneElsesCollection.getManagedResources());
		genericService.saveOrUpdate(someoneElsesCollection.getUnmanagedResources());
		genericService.saveOrUpdate(someoneElsesCollection.getAuthorizedUsers());

		// Create an owner and their own resources
		TdarUser owner = createAndSaveNewUser();
		Dataset ownersConfidentialDataset = createAndSaveNewResource(Dataset.class, owner, "Owner's dataset");
		//AB: a resource should never have 'ADMINISTER' rights on it
		ownersConfidentialDataset.getAuthorizedUsers()
				.add(new AuthorizedUser(owner, owner, Permissions.ADMINISTER_COLLECTION));

		Dataset ownersPublicDataset = createAndSaveNewDataset();
		ownersPublicDataset.getAuthorizedUsers()
				.add(new AuthorizedUser(owner, owner, Permissions.ADMINISTER_COLLECTION));

		genericService.save(ownersConfidentialDataset);
		genericService.save(ownersConfidentialDataset.getAuthorizedUsers());
		
		ResourceCollection ownersCollection = createAndSaveNewResourceCollection("Owner's resource collection");
		ownersCollection.getAuthorizedUsers().add(new AuthorizedUser(owner,owner,Permissions.ADMINISTER_COLLECTION));
		genericService.save(ownersCollection.getAuthorizedUsers());
		
		// TODO: How to I set the permissions for this collection as private for
		// others?
		//AB: not sure I follow, do you mean "hidden" as in other's can't see it?
		
		// Attempt to add a document the user doesn't have permission to as a
		// managed resource.
		ownersCollection.getManagedResources().add(someoneElsesConfidentialDocument);
		ownersCollection.getManagedResources().add(ownersConfidentialDataset);
		
		ownersCollection.getUnmanagedResources().add(ownersPublicDataset);
		ownersCollection.getUnmanagedResources().add(someoneElsesPublicDocument);
		genericService.save(ownersCollection);
		
		

		TdarUser user = createAndSaveNewUser();
		//Add permission for the user to add/remove resources to the owner's collection.
		ownersCollection.getAuthorizedUsers().add(new AuthorizedUser(user, user, Permissions.ADMINISTER_COLLECTION));
		genericService.saveOrUpdate(ownersCollection.getAuthorizedUsers());

		// Create a viewer who has no association to the owner's collection.
		TdarUser viewer = createAndSaveNewUser();

		// get the Ids
		Long ownerId = owner.getId();
		Long otherUserId = someOtherOwner.getId();
		Long userId = user.getId();
		Long viewerId = viewer.getId();

		
		Long otherCollectionId = someoneElsesCollection.getId();
		Long ownerCollectionId = ownersCollection.getId();
		
		someOtherOwner = null;
		owner = null;
		user = null;
		viewer = null;
		ownersCollection = null;
		someoneElsesCollection = null;
		
		// flush everything into the database
		genericService.synchronize();

		// find again just in case (clears transient fields/ relationships)
		owner = genericService.find(TdarUser.class, ownerId);
		someOtherOwner = genericService.find(TdarUser.class, otherUserId);
		user = genericService.find(TdarUser.class, userId);
		viewer = genericService.find(TdarUser.class, viewerId);
		
		
		ownersCollection = genericService.find(ResourceCollection.class, ownerCollectionId);;
		someoneElsesCollection = genericService.find(ResourceCollection.class, otherCollectionId);;
		


		// Test a managed collection the user owns (created).

		// Create a colleciton

		// create a resource (public)
		// create a resource (confidential)

		// create a second resource (public)
		// create a third resrouce (confidential)

		// add all the resources to the collection

		
		assertTrue(authenticationAndAuthorizationService.canEdit(someOtherOwner, someoneElsesConfidentialDocument));
		
		// test the owner
		assertTrue(authenticationAndAuthorizationService.canEdit(owner, ownersCollection));

		//Test that the confidental document exists in the manages resources, but hte owner can't access it
		assertTrue(ownersCollection.getManagedResources().contains(someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(owner, someoneElsesConfidentialDocument));
		
		//Test that someone elses public document can be added as a resouce, can be viewed, but not edited. 
		assertTrue(ownersCollection.getUnmanagedResources().contains(someoneElsesPublicDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(owner, someoneElsesPublicDocument));
		logger.debug("{} {}", owner, someoneElsesPublicDocument);
		assertTrue(authenticationAndAuthorizationService.canView(owner, someoneElsesPublicDocument));
		
		
		// test the user
		// test if the user can access the resources on the owner's collection.
		assertTrue(authenticationAndAuthorizationService.canView(user, someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(user, someoneElsesConfidentialDocument));
		
		assertTrue(authenticationAndAuthorizationService.canEdit(user, ownersCollection));
		assertFalse(authenticationAndAuthorizationService.canViewConfidentialInformation(user, ownersConfidentialDataset));
		assertTrue(authenticationAndAuthorizationService.canView(user, ownersPublicDataset));
		
		// create a viewer
		// test if the user has permission to each of the resources
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, someoneElsesConfidentialDocument));
		//AB:dup?
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, ownersPublicDataset));
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, ownersConfidentialDataset));

		assertFalse(authenticationAndAuthorizationService.canView(viewer, someoneElsesConfidentialDocument));
		assertTrue(authenticationAndAuthorizationService.canView(viewer, someoneElsesPublicDocument));
		assertTrue(authenticationAndAuthorizationService.canView(viewer, ownersPublicDataset));
		assertFalse(authenticationAndAuthorizationService.canView(viewer, ownersConfidentialDataset));
	}
}
