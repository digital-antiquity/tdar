package org.tdar.struts.action.api;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts.action.TestResourceCollectionHelper;
import org.tdar.struts.action.api.collection.ListCollectionApiAction;

public class ListCollectionApiITCase extends AbstractAdminControllerITCase implements TestResourceCollectionHelper  {

    @Test
    @Rollback
    public void testBasicList() throws Exception {
        SharedCollection collection = generateResourceCollection("parent", "parent", true, null, null, null);
        SharedCollection child = generateResourceCollection("c1", "c1",  true, null, null, collection.getId());
        SharedCollection child2 = generateResourceCollection("c2", "c1",  true, null, null, collection.getId());
        SharedCollection child3 = generateResourceCollection("c3", "c1",  true, null, null, collection.getId());
        SharedCollection child_1 = generateResourceCollection("c1.1", "c1",  true, null, null, child.getId());
        
        ListCollectionApiAction action = generateNewInitializedController(ListCollectionApiAction.class);
        action.prepare();
        action.validate();
        action.execute();
        String result = IOUtils.toString(action.getJsonInputStream());
        logger.debug(result);
    }
}
