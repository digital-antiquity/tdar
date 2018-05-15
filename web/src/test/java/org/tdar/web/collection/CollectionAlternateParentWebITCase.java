package org.tdar.web.collection;

import java.util.ArrayList;

import org.junit.Test;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class CollectionAlternateParentWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    @Test
    // crate a collection with some resources, then edit it by adding some authorized users and removing a few resources
    public void testCreateRemoveAlternateParent() {
        String name = "my parent collection: " + System.currentTimeMillis();
        String desc = "description goes here: " + System.currentTimeMillis();
        createTestCollection(CollectionResourceSection.MANAGED, name, desc, new ArrayList<>());
        assertTextPresent(name);
        assertTextPresent(desc);
        Long parentId = extractTdarIdFromCurrentURL();

        String namea = "my alternate collection: " + System.currentTimeMillis();
        String desca = "description goes here: " + System.currentTimeMillis();
        createTestCollection(CollectionResourceSection.MANAGED, namea, desca, new ArrayList<>());
        Long altId = extractTdarIdFromCurrentURL();

        String altUrl = getCurrentUrlPath();

        String namec = "my child collection: " + System.currentTimeMillis();
        String descc = "description goes here: " + System.currentTimeMillis();
        createTestCollection(CollectionResourceSection.MANAGED, namec, descc, new ArrayList<>());
        Long childId = extractTdarIdFromCurrentURL();
        String childUrl = getCurrentUrlPath();
        clickLinkWithText("edit");
        logger.debug("p:{} - {}", name, parentId);
        logger.debug("a:{} - {}", namea, altId);
        setInput("parentId", parentId.longValue());
        setInput("parentCollectionName", name);
        setInput("alternateParentId", altId.longValue());
        setInput("alternateParentCollectionName", namea);
        submitForm();
        assertTextPresentInPage(namea);
        gotoPage(altUrl);
        assertTextPresentInPage(namec);

        gotoPage(childUrl);
        clickLinkWithText("edit");
        assertTextPresentInPage(namea);
        setInput("parentId", "");
        setInput("parentCollectionName", "");
        setInput("alternateParentId", "");
        setInput("alternateParentCollectionName", "");
        submitForm();
        assertTextNotPresent(namea);
        assertTextNotPresent(name);

        logout();

    }

}
