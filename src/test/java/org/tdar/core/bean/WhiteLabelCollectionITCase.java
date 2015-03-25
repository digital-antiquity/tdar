package org.tdar.core.bean;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.service.GenericService;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by jimdevos on 3/23/15.
 */
public class WhiteLabelCollectionITCase extends AbstractIntegrationTestCase {


    @Autowired
    GenericService genericService;

    /**
     * Try to save a new 'white label' collection with all default values;
     */
    @Test
    @Rollback
    public void testSave() {
        ResourceCollection rc = new WhiteLabelCollection();
        rc.setName("default white label collection");
        rc.markUpdated(getAdminUser());
        genericService.save(rc);
        assertThat(rc.getId(), not( nullValue()));
    }


}
