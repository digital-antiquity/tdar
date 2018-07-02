package org.tdar.struts.action.api.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.struts.action.AbstractAdminControllerITCase;

public class ReindexApiActionITCase extends AbstractAdminControllerITCase {

    private ReindexApiAction controller;

    @Test
    @Rollback
    public void testReindexCollection() throws SearchIndexException, IOException {
        Long id = 1000L;
        ResourceCollection collection = getGenericService().find(ResourceCollection.class, id);

        List<Long> ids = new ArrayList<Long>();// collection.getResourceIds())

        ids.addAll(collection.getUnmanagedResourceIds());
        ids.addAll(collection.getResourceIds());

        getLogger().debug("Resource ids are :{}", ids);
        controller = generateNewInitializedController(ReindexApiAction.class);
        controller.setCollectionId(id);
        controller.setIds(ids);
        controller.execute();
    }

}
