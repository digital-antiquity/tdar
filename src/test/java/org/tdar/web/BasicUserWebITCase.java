package org.tdar.web;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.TestConstants;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

@RunWith(MultipleTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { "src/test/resources/tdar.properties", "src/test/resources/tdar.ahad.properties" })
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
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
//            docValMap.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            docValMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
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
    
    
    
    public void assertViewPage() {
        String url = internalPage.getUrl().toString();
        Assert.assertTrue("expecting to be on the view page.  actual page is: " + url, url.matches("^.*\\d+$"));
    }
    
    public void fillOutRequiredfields(String prefix) {
        setInput(prefix + ".title", "minimal test");
        setInput(prefix + ".date", "2002");
        //setInput("accountId", "???");
        setInput(prefix + ".description", "testing");
        setInput("projectId", "-1");
    }

    //create a resource with only required field values.  assert that we land on the view page.  This will hopefully weed out silly
    //mistakes like omitting necessary form field or duplicating a form field.
    public void createMinimalResource(String url, String prefix) {
        gotoPage(url);
        fillOutRequiredfields(prefix);
        submitForm();
        assertViewPage();
    }
    
    public void createMinimalResource(String url, String prefix, String textInput) {
        gotoPage(url);
        fillOutRequiredfields(prefix);
        setInput("fileTextInput", textInput);
        submitForm();
        assertViewPage();
        
    }
    
    @Test
    public void testMinimalCreate() {
        createMinimalResource("/document/add", "document");
        createMinimalResource("/image/add", "image");
        createMinimalResource("/coding-sheet/add", "codingSheet", "doh, a female dear\nfa, a long long way to run\n");
        createMinimalResource("/ontology/add", "ontology", "level1\n\tlevel2\n");
        createMinimalResource("/sensory-data/add", "sensoryData");
        createMinimalResource("/dataset/add", "dataset");
    }
    

}
