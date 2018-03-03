package org.tdar.web.resource;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class ResourceRightsWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String YAGER = "Yager";
    private static final String PERMISSIONS = "permissions";

    @Test
    @Rollback(true)
    /**
     * Test that the form takes entries properly with repeats
     */
    public void testImageAuthorizedRightsIssue() {
        Map<String,String> docValMap = new HashMap<>();
        docValMap .put("projectId", "1");
        docValMap.put("image.title", "test title");
        docValMap.put("image.description", "A resource description");
        docValMap.put("image.date", "1923");

        gotoPage("/image/add");
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        submitForm();
        clickLinkOnPage(PERMISSIONS);
        Map<String,String> docUnorderdValMap = new HashMap<>();
        docUnorderdValMap.put("proxies[0].id", "121");
        docUnorderdValMap.put("proxies[1].id", "5349");
        docUnorderdValMap.put("proxies[0].permission", Permissions.MODIFY_RECORD.name());
        docUnorderdValMap.put("proxies[1].permission", Permissions.VIEW_ALL.name());
        docUnorderdValMap.put("proxies[0].displayName", "Michelle Elliott");
        docUnorderdValMap.put("proxies[1].displayName", "Joshua Watts");

        for (String key : docUnorderdValMap.keySet()) {
            setInput(key, docUnorderdValMap.get(key));
        }
        logger.trace(getPageText());
        submitForm();
        assertTextPresentInPage(docValMap.get("image.title"));
        clickLinkWithText(PERMISSIONS);
        setInput("proxies[0].permission", Permissions.VIEW_ALL.name());
        setInput("proxies[1].permission", Permissions.VIEW_ALL.name());
        submitForm();
        assertFalse(getCurrentUrlPath().contains(PERMISSIONS));
        assertFalse(getPageCode().contains(Permissions.MODIFY_METADATA.name()));
        assertTextPresentInPage(docValMap.get("image.title"));
    }

    

    @Test
    @Rollback(true)
    /**
     * Test that the form takes entries properly with repeats
     */
    public void testDatasetInviteRights() {
        Map<String,String> docValMap = new HashMap<>();
        docValMap .put("projectId", "1");
        docValMap.put("dataset.title", "test title");
        docValMap.put("dataset.description", "A resource description");
        docValMap.put("dataset.date", "1923");

        gotoPage("/dataset/add");
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        submitForm();
        clickLinkOnPage(PERMISSIONS);
        Map<String,String> docUnorderdValMap = new HashMap<>();
        docUnorderdValMap.put("invites[0].permission", Permissions.MODIFY_RECORD.name());
        docUnorderdValMap.put("invites[0].firstName", "Bert");
        docUnorderdValMap.put("invites[0].lastName", YAGER);
        docUnorderdValMap.put("invites[0].email", "Yager@riverschool.net");

        for (String key : docUnorderdValMap.keySet()) {
            setInput(key, docUnorderdValMap.get(key));
        }
        logger.trace(getPageText());
        submitForm();
        assertTextPresentInPage(docValMap.get("dataset.title"));
        assertTextPresentInPage(YAGER);
        clickLinkWithText(PERMISSIONS);
        assertTextPresentInPage(YAGER);
        submitForm();
        assertFalse(getCurrentUrlPath().contains(PERMISSIONS));
        assertTextPresentInPage(YAGER);
        assertTextPresentInPage(docValMap.get("dataset.title"));

        clickLinkWithText(PERMISSIONS);
        assertTextPresentInPage(YAGER);
        setInput("proxies[1].permission", "");
        setInput("proxies[1].displayName", "");
        setInput("proxies[1].inviteId", "");
        setInput("proxies[1].permission", "");
        

        submitForm();
        assertFalse(getCurrentUrlPath().contains(PERMISSIONS));
        assertTextNotPresent(YAGER);
        assertTextPresentInPage(docValMap.get("dataset.title"));
}

    
}
