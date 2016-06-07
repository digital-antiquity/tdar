package org.tdar.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tdar.utils.TestConfiguration;

import net.sf.json.JSONObject;

/**
 * Created by jimdevos on 12/1/14.
 */
public class IntegrationWebITCase extends AbstractAdminAuthenticatedWebTestCase {
    @Test
    public void testWorkspaceAdd() {
        skipHtmlValidation = true;
        gotoPage("/workspace/list");
        gotoPage("/workspace/integrate");
    }

    @Test
    public void testFindOntologyDefault() {
        String json = gotoJson("/api/integration/find-ontologies?incompatible=false&recordsPerPage=500&title=&unbookmarked=false");
        JSONObject obj = JSONObject.fromObject(json);
        assertNotNull(obj);
    }
    
    @Test
    public void testSettings() {
        logout();
        TestConfiguration instance = TestConfiguration.getInstance();
        login(instance.getUsername(), instance.getPassword());
        gotoPage("/workspace/list");
        logger.debug(getPageText());
        gotoPage("/workspace/settings/1000/edit");
        logger.debug(getPageText());
        assertTrue(getPageText().contains("Test Blank Integration"));
        
    }
}
