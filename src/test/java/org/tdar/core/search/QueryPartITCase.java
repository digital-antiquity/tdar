package org.tdar.core.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.FullTextQuery;
import org.junit.Before;
import org.junit.Test;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FreetextQueryPart;
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

    @Test
    public void testFulltextQueryPart() throws ParseException, InstantiationException, IllegalAccessException {
        // this is really brittle, but a good test of our builder actually working
        Document dc = createAndSaveNewInformationResource(Document.class);
        String title = "asdsadfwqradsasfr132";
        dc.setTitle(title);
        genericService.saveOrUpdate(dc);
        searchIndexService.index(dc);
        FreetextQueryPart ftqp = new FreetextQueryPart();
        ftqp.setFieldValues(Arrays.asList("archaeology"));
        ResourceQueryBuilder rcbBuilder = new ResourceQueryBuilder();
        rcbBuilder.append(ftqp);
        logger.info(rcbBuilder.generateQueryString());
        FullTextQuery search = searchService.search(rcbBuilder, null);
        logger.info("{}", search.list());
        assertNotNull(search.list());
    }
}
