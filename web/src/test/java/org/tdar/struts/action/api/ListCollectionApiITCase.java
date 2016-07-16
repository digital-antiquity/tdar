package org.tdar.struts.action.api;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.api.collection.ListCollectionApiAction;

public class ListCollectionApiITCase extends AbstractAdminControllerITCase {

    @Test
    @Rollback
    public void testBasicList() throws Exception {
        ResourceCollection collection = generateResourceCollection("parent", "parent", CollectionType.SHARED, true, null, null, null);
        ResourceCollection child = generateResourceCollection("c1", "c1", CollectionType.SHARED, true, null, null, collection.getId());
        ResourceCollection child2 = generateResourceCollection("c2", "c1", CollectionType.SHARED, true, null, null, collection.getId());
        ResourceCollection child3 = generateResourceCollection("c3", "c1", CollectionType.SHARED, true, null, null, collection.getId());
        ResourceCollection child_1 = generateResourceCollection("c1.1", "c1", CollectionType.SHARED, true, null, null, child.getId());
        
        ListCollectionApiAction action = generateNewInitializedController(ListCollectionApiAction.class);
        action.prepare();
        action.validate();
        action.execute();
        String result = IOUtils.toString(action.getJsonInputStream());
        logger.debug(result);
    }
}
