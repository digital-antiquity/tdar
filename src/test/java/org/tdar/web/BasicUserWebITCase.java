package org.tdar.web;

import java.util.HashMap;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

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
            // docValMap.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            docValMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        submitForm();
        assertTextPresent(TESTCOLLECTIONNAME);
        clickLinkWithText(TESTCOLLECTIONNAME);
        assertTextPresent(TITLE);
        gotoPage("/search/results?query=" + DESCRIPTION);
        assertTextPresent(TITLE);
        assertTextPresent(DESCRIPTION); // should be in the "search description"
        logout();
        gotoPageWithoutErrorCheck("/search/results?includedStatuses=DRAFT&useSubmitterContext=true&query=" + DESCRIPTION);
        assertTextNotPresent(TITLE);
        assertErrorsPresent();
        assertTextPresent(DESCRIPTION);

    }

    private void chooseFirstBillingAccount() {
        HtmlElement input = null;
        if (getTdarConfiguration().getInstance().isPayPerIngestEnabled()) {
            try {
                input = getInput("accountId");
            } catch (ElementNotFoundException ex) {
            }
        } else {
            Assert.assertTrue("there should be no accountId input if pay-per-ingest is enabled", input == null);
        }
        if (input == null)
            return;

        // get the 2nd option which is the first billing account
        Iterator<DomElement> options = input.getChildElements().iterator();
        options.next(); // skip the blank
        DomElement option = options.next();
        String accountId = option.getAttribute("value");
        setInput("accountId", accountId);
    }

    public void assertViewPage() {
        String url = internalPage.getUrl().toString();
        Assert.assertTrue("expecting to be on the view page.  actual page is: " + url, url.matches("^.*\\d+$"));
    }

    public void fillOutRequiredfields(ResourceType resourceType) {
        String prefix = resourceType.getFieldName();
        setInput(prefix + ".title", "minimal test");
        if (!resourceType.isProject()) {
            if (getInput("projectId") == null) {
                logger.info(getPageBodyCode());
            }
            setInput("projectId", "-1");
            setInput(prefix + ".date", "2002");
            if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
                setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
            }
        } else {

//            if (TdarConfiguration.getInstance().getLicenseEnabled()) {
//                setInput("resource.licenseType", LicenseType.OTHER.name());
//                setInput("resource.licenseText", "my custom license");
//            }
        }
        setInput(prefix + ".description", "testing");
    }

    // create a resource with only required field values. assert that we land on the view page. This will hopefully weed out silly
    // mistakes like omitting necessary form field or duplicating a form field.
    public void createMinimalResource(ResourceType resourceType) {
        createMinimalResource(resourceType, null);
    }

    public void createMinimalResource(ResourceType resourceType, String textInput) {
        String uri = String.format("/%s/add", resourceType.getUrlNamespace());
        gotoPage(uri);
        chooseFirstBillingAccount();
        fillOutRequiredfields(resourceType);
        if (textInput != null) {
            setInput("fileTextInput", textInput);
        }
        submitForm();
        assertViewPage();

    }

    @Test
    public void testMinimalCreate() {
        for (ResourceType resourceType : ResourceType.values()) {
            if (resourceType.isSupporting()) {
                if (resourceType.isCodingSheet()) {
                    createMinimalResource(resourceType, "doh, a female dear\nfa, a long long way to run\n");
                }
                if (resourceType.isOntology()) {
                    createMinimalResource(resourceType, "level1\n\tlevel2\n");
                }
            } else {
                createMinimalResource(resourceType);
            }
        }
    }

}
