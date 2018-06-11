package org.tdar.struts.action.api.search;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Image;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;

public class SaveSearchResultActionITCase extends AbstractSearchControllerITCase {

    @Test
    @Rollback
    public void testRunSeachWithExpectedResults() throws Exception {
        Image resource = createAndSaveNewInformationResource(Image.class);
        resource.setTitle("TEST RESOURCE 123::FOR RESULT SAVE");
        getGenericService().saveOrUpdate(resource);
        ResourceCollection resourceCollection = createAndSaveNewResourceCollection("Test Result Collection");
        Long collectionId = resourceCollection.getId();
        Long resourceId = resource.getId();
        getLogger().debug("The new collection id is {}", resourceCollection.getId());

        SaveSearchResultAction controller = generateNewInitializedController(SaveSearchResultAction.class, getUser());
        controller.setCollectionId(resourceCollection.getId());
        controller.setQuery("TEST RESOURCE 123::FOR RESULT SAVE");
        controller.validate();
        controller.prepare();
        // controller.setAsync(false);
        controller.saveSearchResultsToCollection();

        resource = null;
        resourceCollection = null;
        resourceCollection = getGenericService().find(ResourceCollection.class, collectionId);
        resource = getGenericService().find(Image.class, resourceId);
        getLogger().debug("Unmanaged collection is : {}", resourceCollection.getUnmanagedResources());

        assertNotEmpty("The collection is not empty", resourceCollection.getUnmanagedResources());
        assertTrue("The resoruce exists in the unmanaged resource", resourceCollection.getUnmanagedResources().contains(resource));

    }
}
