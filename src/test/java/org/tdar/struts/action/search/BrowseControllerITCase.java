package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.TdarActionSupport;

public class BrowseControllerITCase extends AbstractSearchControllerITCase {

    @Autowired
    private BrowseController controller;
    private Logger log = Logger.getLogger(getClass());

   
    @Before
    public void initController() {
        controller = generateNewInitializedController(BrowseController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback
    public void testBrowseControllerPeople() throws InstantiationException, IllegalAccessException, ParseException {
        Document doc = genericService.find(Document.class, setupDatedDocument());
        ResourceCreator rc = new ResourceCreator(doc, getAdminUser(), ResourceCreatorRole.AUTHOR);
        assertTrue(rc.isValid());
        genericService.saveOrUpdate(rc);
        doc.getResourceCreators().add(rc);
        genericService.saveOrUpdate(doc);
        boolean ex = false;
        // try {
        // searchIndexService.index( doc);
        // } catch (TdarRecoverableRuntimeException e) {
        // ex = true;
        // }
        // assertTrue("exception was thrown", ex);
        searchIndexService.index(doc);
        initController();
        controller.setId(getAdminUserId());
        assertEquals(TdarActionSupport.SUCCESS, controller.browseCreators());
        assertEquals(getAdminUser(), controller.getCreator());
        log.info(controller.getResults());
        assertTrue(controller.getResults().size() > 0);
    }

    @Override
    public Person getSessionUser() {
        return getBasicUser();
    }

}
