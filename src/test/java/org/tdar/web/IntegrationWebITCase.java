package org.tdar.web;

import static org.junit.Assert.assertNotNull;
import net.sf.json.JSONObject;

import org.junit.Test;

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
}
