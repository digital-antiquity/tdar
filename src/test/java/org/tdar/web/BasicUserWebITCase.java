package org.tdar.web;

import java.util.HashMap;

import org.junit.Test;

public class BasicUserWebITCase extends AbstractAuthenticatedWebTestCase {
    private static final String DESCRIPTION = "descriptionthisisauniquetest";
    private static final String TITLE = "title of a test document";
    private static final String TESTCOLLECTIONNAME = "TESTCOLLECTIONNAME";


    @Test
    public void testDraftPermissions() {
        gotoPage("/document/add");
        HashMap<String, String> docValMap = new HashMap<String, String>();
        docValMap.put(PROJECT_ID_FIELDNAME, "1");
        docValMap.put("document.title", TITLE);
        docValMap.put("document.description", DESCRIPTION);
        docValMap.put("document.documentType", "BOOK");
        docValMap.put("resourceCollections[0].name", TESTCOLLECTIONNAME);
        docValMap.put("document.date", "1923");
        docValMap.put("status", "DRAFT");
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        submitForm();
        assertTextPresent(TESTCOLLECTIONNAME);
        clickLinkWithText(TESTCOLLECTIONNAME);
        assertTextPresent(TITLE);
        gotoPage("/search/results?query="+DESCRIPTION);
        assertTextPresent(TITLE);
        assertTextPresent(DESCRIPTION); // should be in the "search description"
        logout();
        gotoPageWithoutErrorCheck("/search/results?includedStatuses=DRAFT&useSubmitterContext=true&query="+DESCRIPTION);
        assertTextNotPresent(TITLE);
        assertErrorsPresent();
        assertTextPresent(DESCRIPTION);

    }

}
