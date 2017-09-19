package org.tdar.struts.action.resource;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.document.DocumentController;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.utils.PersistableUtils;

import net.sf.saxon.pattern.DocumentNodeTest;

public class ResourceRetainITCase extends AbstractControllerITCase {

    @Test
    @Rollback
    public void testResourceCollectionRetain() throws InstantiationException, IllegalAccessException, TdarActionException {
        Document doc  = createAndSaveNewInformationResource(Document.class, true);
        ResourceCollection collection = createAndSaveNewResourceCollection("uneditable");
        doc.getSharedCollections().add(collection);
        collection.getResources().add(doc);
        genericService.saveOrUpdate(doc);
        genericService.saveOrUpdate(collection);
        genericService.synchronize();
        
        TdarUser submitter = doc.getSubmitter();
        assertNotEquals(getUser(), submitter);
        Long id = doc.getId();
        Long collectionId = collection.getId();
        doc = null;
        collection = null;
        DocumentController controller = generateNewInitializedController(DocumentController.class, submitter);
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.getShares().clear();
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        
        doc = genericService.find(Document.class, id);
        assertTrue(PersistableUtils.extractIds(doc.getSharedCollections()).contains(collectionId));

    }
}
