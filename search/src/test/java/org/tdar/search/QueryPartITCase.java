package org.tdar.search;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.part.StatusAndRelatedPermissionsQueryPart;

public class QueryPartITCase extends AbstractWithIndexIntegrationTestCase {

    @Override
    public void reindex() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
    }

    @Test
    public void test() {
        // this is really brittle, but a good test of our builder actually working
        StatusAndRelatedPermissionsQueryPart sqp = new StatusAndRelatedPermissionsQueryPart(Arrays.asList(Status.DRAFT), getBasicUser(), TdarGroup.TDAR_USERS);
        assertEquals("( ( status:(DRAFT) AND ( usersWhoCanModify:(8092) OR usersWhoCanView:(8092) )  )  ) ", sqp.generateQueryString());
    }
}
