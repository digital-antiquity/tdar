package org.tdar.core.search;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.search.query.part.StatusQueryPart;

public class QueryPartITCase extends AbstractIntegrationTestCase {

    @Before
    public void before() {
        searchIndexService.purgeAll();
        searchIndexService.indexAll(getAdminUser(), Resource.class);
    }

    @Test
    public void test() {
        // this is really brittle, but a good test of our builder actually working
        StatusQueryPart sqp = new StatusQueryPart(Arrays.asList(Status.DRAFT), getBasicUser(), TdarGroup.TDAR_USERS);
        assertEquals("( ( status:(DRAFT) AND ( usersWhoCanModify:(8092) OR usersWhoCanView:(8092) )  )  ) ", sqp.generateQueryString());
    }
}
