package org.tdar.struts.action.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.search.query.SortOption;

public class CollectionLookupControllerITCase extends AbstractIntegrationTestCase {

    private LookupController controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    String[] collectionNames = new String[] { "Kalaupapa National Historical Park, Hawaii", "Kaloko-Honokohau National Historical Park, Hawaii", "Kapsul",
            "KBP Artifact Photographs", "KBP Field Notes", "KBP Level Maps", "KBP Maps", "KBP Profiles", "KBP Reports", "KBP Site Photographs",
            "Kharimkotan 1", "Kienuka", "Kintigh - Carp Fauna Coding Sheets", "Kintigh - Cibola Excavation", "Kintigh - Cibola Research",
            "Kintigh - Cibola Survey Projects", "Kintigh - Context Ontologies", "Kintigh - Fauna Ontologies", "Kintigh - HARP Coding Sheets",
            "Kintigh - Quantitative and Formal Methods Class - Assignments & Data", "Kleis", "Klinko", "Kokina 1", "Kompaneyskyy 1",
            "Kuril Biocomplexity Research", "Kuybyshevskaya 1",
            "Spielmann/Kintigh - Fauna Ontologies - Current" };

    @Test
    @Rollback(true)
    public void testCollectionLookup() throws IOException {
        setupCollections();
        controller.setTerm("Kin");
        controller.lookupResourceCollection();
        for (Indexable collection_ : controller.getResults()) {
            ResourceCollection collection = (ResourceCollection) collection_;
            logger.info("{}", collection);
            if (collection != null) {
                assertFalse(collection.getTitle().equals("Kleis"));
            }
        }
        initController();
        controller.setTerm("Kintigh - C");
        controller.lookupResourceCollection();
        logger.debug(IOUtils.toString(controller.getJsonInputStream()));
        for (Indexable collection_ : controller.getResults()) {
            ResourceCollection collection = (ResourceCollection) collection_;
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    @Test
    @Rollback(true)
    public void testCollectionLookupUnauthenticated() {
        setupCollections();
        controller = generateNewController(LookupController.class);
        initAnonymousUser(controller);
        controller.setTerm("Kintigh - C");
        controller.lookupResourceCollection();
        for (Indexable collection_ : controller.getResults()) {
            ResourceCollection collection = (ResourceCollection) collection_;
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    private void setupCollections() {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        for (String collectionName : collectionNames) {
            ResourceCollection e = new ResourceCollection(collectionName, collectionName, SortOption.TITLE, CollectionType.SHARED, true, getBasicUser());
            collections.add(e);
            e.markUpdated(getBasicUser());

        }
        genericService.save(collections);
        searchIndexService.index(collections.toArray(new ResourceCollection[0]));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicOwner() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        controller.setTerm("test");
        controller.lookupResourceCollection();
        assertTrue(controller.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUser() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        init(controller, getBasicUser());
        controller.setTerm("test");
        controller.lookupResourceCollection();
        assertTrue(controller.getResults().contains(e));
    }


    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUserForModification() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        init(controller, getBasicUser());
        controller.setTerm("test");
        controller.setPermission(GeneralPermissions.ADMINISTER_GROUP);
        controller.lookupResourceCollection();
        assertFalse(controller.getResults().contains(e));
    }

    private ResourceCollection setupResourceCollectionForPermissionsTests(TdarUser owner, boolean visible, TdarUser user, GeneralPermissions permission) {
        assertFalse(getSessionUser().equals(getAdminUser()));
        ResourceCollection e = new ResourceCollection("a test", "a Name", SortOption.TITLE, CollectionType.SHARED, visible, owner);
        e.markUpdated(owner);
        genericService.save(e);
        if (user != null) {
            AuthorizedUser au = new AuthorizedUser(user, permission);
            e.getAuthorizedUsers().add(au);
        }
        searchIndexService.index(e);
        return e;
    }

    @Autowired
    ObfuscationService obfuscationService;

    @Autowired
    ReflectionService reflectionService;


}
