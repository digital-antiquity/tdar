package org.tdar.core.bean;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
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
				.add(new AuthorizedUser(getAdminUser(), tdarUser, GeneralPermissions.ADMINISTER_SHARE));
		genericService.saveOrUpdate(collection.getAuthorizedUsers());
		genericService.saveOrUpdate(collection);

		resourceCollectionService.addResourceCollectionToResource(document, document.getSharedResourceCollections(),
				getBasicUser(), true, ErrorHandling.NO_VALIDATION, collection, CollectionType.LIST);

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
		assertFalse(collection.getResources().contains(document));
	}

	@Test
	@Rollback
	public void testManagedCollections()
			throws InstantiationException, IllegalAccessException, FileNotFoundException {

		// Create some other user's collection and add a public and confidential
		// resource to it.
		TdarUser someOtherOwner = createAndSaveNewUser();

		Document someoneElsesPublicDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
		someoneElsesPublicDocument.getFirstInformationResourceFile().setRestriction(FileAccessRestriction.PUBLIC);
		//someoneElsesPublicDocument.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner, someOtherOwner, GeneralPermissions.ADMINISTER_SHARE));
		genericService.saveOrUpdate(someoneElsesPublicDocument);
		//genericService.saveOrUpdate(someoneElsesPublicDocument.getAuthorizedUsers());

		Document someoneElsesConfidentialDocument = createAndSaveDocumentWithFileAndUseDefaultUser();
		someoneElsesConfidentialDocument.getFirstInformationResourceFile()
				.setRestriction(FileAccessRestriction.CONFIDENTIAL);
		someoneElsesConfidentialDocument.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner, someOtherOwner, GeneralPermissions.ADMINISTER_SHARE));
		genericService.saveOrUpdate(someoneElsesConfidentialDocument);
		genericService.saveOrUpdate(someoneElsesConfidentialDocument.getAuthorizedUsers());

		ResourceCollection someoneElsesCollection = createAndSaveNewResourceCollection(
				"Someone else's resource collection");
		someoneElsesCollection.getAuthorizedUsers().add(new AuthorizedUser(someOtherOwner,someOtherOwner,GeneralPermissions.ADMINISTER_SHARE));
		
		someoneElsesCollection.getResources().add(someoneElsesConfidentialDocument);
		someoneElsesCollection.getUnmanagedResources().add(someoneElsesPublicDocument);
		genericService.saveOrUpdate(someoneElsesCollection);
		genericService.saveOrUpdate(someoneElsesCollection.getResources());
		genericService.saveOrUpdate(someoneElsesCollection.getUnmanagedResources());
		genericService.saveOrUpdate(someoneElsesCollection.getAuthorizedUsers());

		// Create an owner and their own resources
		TdarUser owner = createAndSaveNewUser();
		Dataset ownersConfidentialDataset = createAndSaveNewResource(Dataset.class, owner, "Owner's dataset");
		ownersConfidentialDataset.getAuthorizedUsers()
				.add(new AuthorizedUser(owner, owner, GeneralPermissions.ADMINISTER_SHARE));

		Dataset ownersPublicDataset = createAndSaveNewDataset();
		ownersPublicDataset.getAuthorizedUsers()
				.add(new AuthorizedUser(owner, owner, GeneralPermissions.ADMINISTER_SHARE));

		genericService.save(ownersConfidentialDataset);
		genericService.save(ownersConfidentialDataset.getAuthorizedUsers());
		
		ResourceCollection ownersCollection = createAndSaveNewResourceCollection("Owner's resource collection");
		ownersCollection.getAuthorizedUsers().add(new AuthorizedUser(owner,owner,GeneralPermissions.ADMINISTER_SHARE));
		genericService.save(ownersCollection.getAuthorizedUsers());
		
		// TODO: How to I set the permissions for this collection as private for
		// others?

		// Attempt to add a document the user doesn't have permission to as a
		// managed resource.
		ownersCollection.getResources().add(someoneElsesConfidentialDocument);
		ownersCollection.getResources().add(ownersConfidentialDataset);
		
		ownersCollection.getUnmanagedResources().add(ownersPublicDataset);
		ownersCollection.getUnmanagedResources().add(someoneElsesPublicDocument);
		genericService.save(ownersCollection);
		
		

		TdarUser user = createAndSaveNewUser();
		//Add permission for the user to add/remove resources to the owner's collection.
		ownersCollection.getAuthorizedUsers().add(new AuthorizedUser(user, user, GeneralPermissions.ADMINISTER_SHARE));
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
		assertTrue(ownersCollection.getResources().contains(someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(owner, someoneElsesConfidentialDocument));
		
		//Test that someone elses public document can be added as a resouce, can be viewed, but not edited. 
		assertTrue(ownersCollection.getUnmanagedResources().contains(someoneElsesPublicDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(owner, someoneElsesPublicDocument));
		assertTrue(authenticationAndAuthorizationService.canView(owner, someoneElsesPublicDocument));
		
		
		// test the user
		// test if the user can access the resources on the owner's collection.
		assertFalse(authenticationAndAuthorizationService.canView(user, someoneElsesConfidentialDocument));
		assertTrue(authenticationAndAuthorizationService.canEdit(user, ownersCollection));
		assertFalse(authenticationAndAuthorizationService.canView(user, ownersConfidentialDataset));
		assertTrue(authenticationAndAuthorizationService.canView(user, ownersPublicDataset));
		
		// create a viewer
		// test if the user has permission to each of the resources
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, someoneElsesConfidentialDocument));
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, ownersPublicDataset));
		assertFalse(authenticationAndAuthorizationService.canEdit(viewer, ownersConfidentialDataset));

		assertFalse(authenticationAndAuthorizationService.canView(viewer, someoneElsesConfidentialDocument));
		assertTrue(authenticationAndAuthorizationService.canView(viewer, someoneElsesPublicDocument));
		assertTrue(authenticationAndAuthorizationService.canView(viewer, ownersPublicDataset));
		assertFalse(authenticationAndAuthorizationService.canView(viewer, ownersConfidentialDataset));
	}
}
