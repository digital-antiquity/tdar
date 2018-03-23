package org.tdar.search;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.part.StatusAndRelatedPermissionsQueryPart;

public class QueryPartITCase extends AbstractWithIndexIntegrationTestCase {


    @Test
    public void test() {
        // this is really brittle, but a good test of our builder actually working
        StatusAndRelatedPermissionsQueryPart sqp = new StatusAndRelatedPermissionsQueryPart(Arrays.asList(Status.DRAFT, Status.ACTIVE), getBasicUser(), TdarGroup.TDAR_USERS);
        logger.debug(sqp.toString());
        assertEquals("( effectivelyPublic:(true) OR ( ( usersWhoCanModify:(8092) OR usersWhoCanView:(8092) )  AND ( status:(DRAFT) OR hidden:(true) )  )  ) ", sqp.generateQueryString());
    }
}
