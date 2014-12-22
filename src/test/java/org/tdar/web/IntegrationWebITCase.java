package org.tdar.web;

import net.sf.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by jimdevos on 12/1/14.
 */
public class IntegrationWebITCase extends AbstractAdminAuthenticatedWebTestCase {
    @Test
    public void testWorkspaceAdd() {
        //angular templates will likely break all html validation
        skipHtmlValidation = true;
        gotoPage("/workspace/add-angular");
    }

    @Test
    public void testFindOntologyDefault() {
        //perform ontology search with all default filters
        String json = gotoJson("/workspace/ajax/find-ontologies?incompatible=false&recordsPerPage=500&title=&unbookmarked=false");
        JSONArray obj = JSONArray.fromObject(json);
        assertNotNull(obj);
    }
}
