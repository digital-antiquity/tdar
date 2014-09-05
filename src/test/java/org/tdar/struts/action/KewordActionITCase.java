package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Status;
import org.tdar.struts.action.search.BrowseKeywordController;

public class KewordActionITCase extends AbstractDataIntegrationTestCase {

    @Test
    public void testBasicKeywordAction() {
        BrowseKeywordController bkc = setupController(1L, KeywordType.CULTURE_KEYWORD);
        bkc.view();
    }

    private BrowseKeywordController setupController(long l, KeywordType cultureKeyword) {
        BrowseKeywordController bkc = generateNewController(BrowseKeywordController.class);
        init(bkc,null);
        bkc.setId(l);
        bkc.setKeywordType(cultureKeyword);
        Exception e = null;
        try {
            bkc.prepare();
        } catch (Exception ex) {
            e = ex;
        }
        assertEquals(null, e);
        return bkc;
    }

    @Test
    public void testKeywordActionInvalidId() {
        BrowseKeywordController bkc = setupController(1000L, KeywordType.CULTURE_KEYWORD);
        String result = bkc.view();
        assertEquals(TdarActionSupport.NOT_FOUND, result);
    }

    @Test
    @Rollback(true)
    public void testKeywordActionStatus() {
        InvestigationType it =new InvestigationType();
        it.setDefinition("this is a test");
        it.setStatus(Status.DELETED);
        it.setLabel("test type");
        genericService.save(it);
        BrowseKeywordController bkc = setupController(it.getId(), KeywordType.INVESTIGATION_TYPE);
        String result = bkc.view();
        assertEquals(TdarActionSupport.NOT_FOUND, result);

        // change to draft
        it.setStatus(Status.DRAFT);
        genericService.saveOrUpdate(it);
        bkc = setupController(it.getId(), KeywordType.INVESTIGATION_TYPE);
        result = bkc.view();
        assertEquals(TdarActionSupport.NOT_FOUND, result);

        // change to active
        it.setStatus(Status.ACTIVE);
        genericService.saveOrUpdate(it);
        bkc = setupController(it.getId(), KeywordType.INVESTIGATION_TYPE);
        result = bkc.view();
        assertEquals(TdarActionSupport.SUCCESS, result);
}

}
