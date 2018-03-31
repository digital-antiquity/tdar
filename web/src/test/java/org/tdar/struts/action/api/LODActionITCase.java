package org.tdar.struts.action.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.junit.IgnoreActionErrors;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.lod.CollectionLinkedOpenDataAction;
import org.tdar.struts.action.api.lod.CreatorLinkedOpenDataAction;
import org.tdar.struts.action.api.lod.KeywordLinkedOpenDataAction;
import org.tdar.struts.action.api.lod.ResourceLinkedOpenDataAction;

public class LODActionITCase extends AbstractControllerITCase {

    @Test
    @IgnoreActionErrors
    public void testLodKeywordAPIInvalid() throws Exception {
        KeywordLinkedOpenDataAction controller = generateNewInitializedController(KeywordLinkedOpenDataAction.class);
        GeographicKeyword gk = genericService.findRandom(GeographicKeyword.class, 1).get(0);
        controller.setId(gk.getId());
        controller.prepare();
        String body = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("not exist"));

    }

    @Test
    public void testLodKeywordAPI() throws Exception {
        KeywordLinkedOpenDataAction controller = generateNewInitializedController(KeywordLinkedOpenDataAction.class);
        GeographicKeyword gk = genericService.findRandom(GeographicKeyword.class, 1).get(0);
        controller.setId(gk.getId());
        controller.setType(KeywordType.GEOGRAPHIC_KEYWORD);
        prepareAndValid(controller, gk.getLabel());

    }

    @Test
    @IgnoreActionErrors
    public void testLodCreatorAPIInvalid() throws Exception {
        CreatorLinkedOpenDataAction controller = generateNewInitializedController(CreatorLinkedOpenDataAction.class);
        controller.setId(9999l);
        controller.prepare();
        String body = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("not exist"));

    }

    @Test
    public void testLodCreatorAPI() throws Exception {
        CreatorLinkedOpenDataAction controller = generateNewInitializedController(CreatorLinkedOpenDataAction.class);
        Person gk = genericService.findRandom(Person.class, 1).get(0);
        controller.setId(gk.getId());
        prepareAndValid(controller, gk.getFirstName());

    }

    @Test
    @IgnoreActionErrors
    public void testLodCollectionAPIInvalid() throws Exception {
        CollectionLinkedOpenDataAction controller = generateNewInitializedController(CollectionLinkedOpenDataAction.class);
        controller.setId(9999l);
        controller.prepare();
        String body = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("not exist"));

    }

    @Test
    public void testLodCollectionAPI() throws Exception {
        CollectionLinkedOpenDataAction controller = generateNewInitializedController(CollectionLinkedOpenDataAction.class);
        ResourceCollection gk = genericService.findRandom(ResourceCollection.class, 1).get(0);
        controller.setId(gk.getId());
        prepareAndValid(controller, "/collection/");

    }

    @Test
    @IgnoreActionErrors
    public void testLodResourceAPIInvalid() throws Exception {
        ResourceLinkedOpenDataAction controller = generateNewInitializedController(ResourceLinkedOpenDataAction.class);
        controller.setId(99999l);
        controller.prepare();
        String body = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("not exist"));

    }

    @Test
    public void testLodResourceAPI() throws Exception {
        ResourceLinkedOpenDataAction controller = generateNewInitializedController(ResourceLinkedOpenDataAction.class);
        Resource gk = genericService.findRandom(Resource.class, 1).get(0);
        controller.setId(gk.getId());
        prepareAndValid(controller, gk.getTitle());

    }

    private void prepareAndValid(AbstractJsonApiAction controller, String label) throws Exception, IOException {
        controller.prepare();
        String body = IOUtils.toString(controller.getJsonInputStream());
        logger.debug(body);
        assertFalse(body.contains("not exist"));
        assertTrue(body.contains(label));
    }

}
