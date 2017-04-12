package org.tdar.web.resource;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class ResourceRightsWebITCase extends AbstractAdminAuthenticatedWebTestCase {

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
        clickLinkOnPage("permissions");
        Map<String,String> docUnorderdValMap = new HashMap<>();
        docUnorderdValMap.put("proxies[0].id", "121");
        docUnorderdValMap.put("proxies[1].id", "5349");
        docUnorderdValMap.put("proxies[0].permission", GeneralPermissions.MODIFY_RECORD.name());
        docUnorderdValMap.put("proxies[1].permission", GeneralPermissions.VIEW_ALL.name());
        docUnorderdValMap.put("proxies[0].displayName", "Michelle Elliott");
        docUnorderdValMap.put("proxies[1].displayName", "Joshua Watts");

        for (String key : docUnorderdValMap.keySet()) {
            setInput(key, docUnorderdValMap.get(key));
        }
        logger.trace(getPageText());
        submitForm();
        assertTextPresentInPage(docValMap.get("image.title"));
        clickLinkWithText("permissios");
        setInput("proxies[0].generalPermission", GeneralPermissions.VIEW_ALL.name());
        setInput("proxies[1].generalPermission", GeneralPermissions.VIEW_ALL.name());
        submitForm();
        assertFalse(getCurrentUrlPath().contains("permissions"));
        assertFalse(getPageCode().contains(GeneralPermissions.MODIFY_METADATA.name()));
        assertTextPresentInPage(docValMap.get("image.title"));
    }
    
    
}
