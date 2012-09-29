package org.tdar.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.opensymphony.module.sitemesh.HTMLPage;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractWebTestCase {

    final Logger logger = LoggerFactory.getLogger(AbstractAnonymousWebTestCase.class);

    final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
    protected Page internalPage;
    private HtmlForm internalForm;

    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public String getBaseUrl() {
        return System.getProperty("tdar.baseurl", DEFAULT_BASE_URL);
    }

    public Page getPage(String localPath) {
        try {
            if (localPath.startsWith("http")) {
                return webClient.getPage(localPath);
            } else {
                return webClient.getPage(getBaseUrl() + localPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected WebClient getWebClient() {
        return webClient;
    }

    /**
     * Go to the specified page, with explicit assertions that the server did not return with a 500 error or contain any inline exception text
     * 
     * @param path
     * @return http return code (if no errors found, otherwise assertions fail and method does not return);
     */
    public int gotoPage(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertFalse(statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertNoErrorTextPresent();
        return statusCode;
    }

    /**
     * Same as gotoPage(), but does not perform any assertions on the server response
     * 
     * @param path
     * @return http return code
     */
    public int gotoPageWithoutErrorCheck(String path) {
        webClient.setThrowExceptionOnFailingStatusCode(false);
        changePage(getPage(path));
        webClient.setThrowExceptionOnFailingStatusCode(true);
        return internalPage.getWebResponse().getStatusCode();
    }

    public void assertTextPresentInPage(String text) {
        assertTextPresentInPage(text, true);
    }

    public void assertTextPresentInPage(String text, boolean sensitive) {
        HtmlPage page = (HtmlPage) internalPage;
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl(), page.asText().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl(), page.asText().toLowerCase().contains(text.toLowerCase()));
        }
    }

    public void assertTextPresentInCode(String text) {
        assertTextPresentInCode(text, true);
    }

    public void assertTextPresentInCode(String text, boolean sensitive) {
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl(), getPageCode().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl(), getPageCode().toLowerCase().contains(text));
        }
    }

    public void duplicateInputByName(String name, String newName) {
        HtmlPage page = (HtmlPage) internalPage;
        HtmlElement elementByName = page.getElementByName(name);
        HtmlElement clone = (HtmlElement) elementByName.cloneNode(true);
        clone.setAttribute("name", newName);
        elementByName.getParentNode().appendChild(clone);
    }

    public void assertTextPresent(String text) {
        assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl(), internalPage.getWebResponse().getContentAsString().contains(text));
    }

    public HtmlElement getInput(String name) {
        HtmlPage page = (HtmlPage) internalPage;
        return page.getElementByName(name);
    }

    public void setInput(String name, String value) {
        HtmlPage page = (HtmlPage) internalPage;
        String id = null;

        // a pattern that describes struts "indexed" form field names (e.g.
        // <input name="mybean[2].myBeanField" /> or <input name="myvar[0]" />)
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        HtmlElement input = null;
        try {
            page.getElementByName(name);
        } catch (Exception e) {
            logger.info("no element found: " + name);
        }

        if (input == null) {
            // test for duplicating fields with the two cases we have (a) struts, or
            // (b) the non-standard file-upload
            if (name.matches(indexedNamePattern)) {
                String zerothFieldName = name.replaceAll(indexedNamePattern, "$1[0]$3"); // $3
                                                                                         // may
                                                                                         // be
                                                                                         // blank
                if (!name.equals(zerothFieldName)) {
                    duplicateInputByName(zerothFieldName, name);
                }
            } else if (name.equalsIgnoreCase("uploadedFiles")) {
                duplicateInputByName(name, name);
            }
            input = page.getElementByName(name);
        }

        if (input instanceof HtmlTextArea) {
            HtmlTextArea txt = (HtmlTextArea) input;
            id = txt.getId();
            txt.setText(value);
        } else if (input instanceof HtmlSelect) {
            HtmlSelect sel = (HtmlSelect) input;
            sel.setSelectedAttribute(value, true);
            id = sel.getId();
        } else if (input instanceof HtmlCheckBoxInput) {
            // if the checkbox's value attribute matches the supplied value, check the box. Otherwise uncheck it.
            HtmlCheckBoxInput chk = (HtmlCheckBoxInput) input;
            id = chk.getId();
            chk.setChecked(chk.getValueAttribute().equalsIgnoreCase(value));
        } else {
            HtmlInput inp = (HtmlInput) input;
            id = inp.getId();
            inp.setValueAttribute(value);
        }
        assertTrue("could not find field: " + name, id != null);
        for (HtmlForm form : page.getForms()) {
            if (form.hasHtmlElementWithId(id)) {
                internalForm = form;
            }
        }
    }

    public void createInput(String inputName, String name, String value) {
        HtmlElement createdElement = ((HtmlPage) internalPage).createElement("input");
        createdElement.setAttribute("type", inputName);
        createdElement.setAttribute("name", name);
        createdElement.setAttribute("value", value);
        internalForm.appendChild(createdElement);
    }

    public boolean checkInput(String name, String val) {
        List<HtmlElement> els = getHtmlPage().getElementsByName(name);
        for (HtmlElement el : els) {
            if (el instanceof HtmlTextArea && ((HtmlTextArea) el).getText().equals(val)) {
                return true;
            } else if (el instanceof HtmlSelect) {
                HtmlSelect sel = (HtmlSelect) el;
                for (HtmlOption o : sel.getSelectedOptions()) {
                    if (o.getValueAttribute().equalsIgnoreCase(val))
                        return true;
                }
            } else if (el instanceof HtmlCheckBoxInput || el instanceof HtmlRadioButtonInput) {
                if (els.size() == 1 && val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
                    if (el.getAttribute("value").equalsIgnoreCase(val)) {
                        if (el.hasAttribute("checked") && val.equalsIgnoreCase("true"))
                            return true;
                        if (!el.hasAttribute("checked") && val.equalsIgnoreCase("false"))
                            return true;
                    }
                } else if (el.getAttribute("value").equalsIgnoreCase(val)
                        && (el instanceof HtmlCheckBoxInput && ((HtmlCheckBoxInput) el).isChecked() || el instanceof HtmlRadioButtonInput
                                && ((HtmlRadioButtonInput) el).isChecked()))
                    return true;
            } else if ((el instanceof HtmlTextInput || el instanceof HtmlHiddenInput || el instanceof HtmlPasswordInput)
                    && el.getAttribute("value").equals(val)) {
                return true;
            }
        }
        return false;
    }

    public void setInput(String name, String... values) {
        HtmlPage page = (HtmlPage) internalPage;
        String id = null;
        for (HtmlElement input : page.getElementsByName(name)) {
            if (input instanceof HtmlCheckBoxInput) {
                HtmlCheckBoxInput chk = (HtmlCheckBoxInput) input;
                for (String val : values) {
                    if (chk.getValueAttribute().equalsIgnoreCase(val)) {
                        id = chk.getId();
                        chk.setChecked(true);
                        continue;
                    }
                }
            }
        }
        for (HtmlForm form : page.getForms()) {
            if (form.hasHtmlElementWithId(id)) {
                internalForm = form;
            }
        }
    }

    public void assertButtonPresentWithText(String buttonText) {
        if (internalForm == null && getHtmlPage().getForms().size() > 0) {
            internalForm = getHtmlPage().getForms().get(0);
        }
        assertNotNull("button with text [" + buttonText + "] not found", internalForm.getInputByValue(buttonText));
        assertTrue(internalForm.getInputByValue(buttonText).getTypeAttribute().equalsIgnoreCase("submit"));
    }

    public int submitForm() {
        return submitForm("Save");
    }

    public int submitForm(String buttonText) {
        submitFormWithoutErrorCheck(buttonText);
        int statusCode = internalPage.getWebResponse().getStatusCode();
        assertFalse(statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertNoErrorTextPresent();
        return statusCode;
    }

    /**
     * similar to submitForm but does not perform any assertions on the server response
     * 
     * @param buttonText
     */
    public void submitFormWithoutErrorCheck(String buttonText) {
        assertButtonPresentWithText(buttonText);
        try {
            changePage(internalForm.getInputByValue(buttonText).click());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assertTextNotPresent(String text) {
        String contents = "";
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            contents = page.asText();
        }
        if (internalPage instanceof UnexpectedPage) {
            UnexpectedPage page = (UnexpectedPage) internalPage;
            contents = page.getWebResponse().getContentAsString();
        }
        assertFalse("text should not be present [" + text + "] in page:" + internalPage.getUrl(), contents.contains(text));
    }

    /**
     * Assert that the page is not an error page and does or contain any inline stacktraces
     */
    public void assertNoErrorTextPresent() {
        assertTextNotPresent("Exception stack trace:" + getPageText()); // inline stacktrace (ftl compiles but dies partway through rendering)
        Assert.assertFalse("page shouldn't contain action errors" + getPageText(), getPageCode().contains("class=\"action-error\""));
    }

    public HtmlPage getHtmlPage() {
        assertTrue("page is not a HtmlPage", internalPage instanceof HtmlPage);
        HtmlPage page = (HtmlPage) internalPage;
        return page;
    }

    public HtmlAnchor findPageLink(String text) {
        HtmlAnchor anchor = getHtmlPage().getAnchorByText(text);
        assertNotNull("link with text [" + text + "] not found", anchor);
        return anchor;
    }

    public void clickLinkWithText(String text) {
        clickLinkOnPage(text);
    }

    public void changePage(Page page) {
        internalPage = page;
        internalForm = null;
        logger.info("CHANGING url TO: " + internalPage.getUrl());
    }

    public void clickLinkOnPage(String text) {
        HtmlAnchor anchor = findPageLink(text);
        assertNotNull(anchor);
        try {
            changePage(anchor.click());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPageText() {
        if (internalPage instanceof HTMLPage) {
            HtmlPage page = (HtmlPage) internalPage;
            return page.asText();
        }
        if (internalPage instanceof TextPage) {
            return ((TextPage) internalPage).getContent();
        }

        return internalPage.getWebResponse().getContentAsString();
    }

    public String getPageCode() {
        return internalPage.getWebResponse().getContentAsString();
    }

    public String getCurrentUrlPath() {
        return internalPage.getUrl().getPath();
    }

    @Before
    public void prepare() {

        // FIXME: This is far less than ideal, but there's a problem with how
        // the MAC is handling memory
        // and appears to be 'leaking' with jwebunit and gooogle maps. Hence, we
        // need to disable javascript
        // testing on the mac :(
        // if (System.getProperty("os.name").toLowerCase().contains("mac os x"))
        webClient.setJavaScriptEnabled(false);
        webClient.setTimeout(0);
        webClient.setJavaScriptTimeout(0);
    }

    public void testOntologyView() {
        gotoPage("/ontology/3479");
        assertTextPresentInPage("TAG Fauna Ontology - Taxon");
        assertTextPresentInPage("AVES");
        assertTextPresentInCode("Canis familiaris (Dog)"); // this may be hidden in the 'expanded' area, so look in html source
    }

    public void testCodingSheetView() {
        gotoPage("/coding-sheet/449");
        assertTextPresentInPage("CARP Fauna Proximal-Distal");
        assertTextPresentInPage("Subcategory: Proximal/Distal");
    }

    public void testProjectView() {
        gotoPage("/project/3805");
        logger.trace(getPageText());
        assertTextPresentInPage("New Philadelphia Archaeology Project");
        assertTextPresentInPage("Block 3, Lot 4");
    }

    public void testDocumentView() {
        gotoPage("/document/4232");
        assertTextPresentInPage("2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1");
        assertTextPresentInPage("a2008reportchap4.pdf");
        assertTextPresentInPage("New Philadelphia Archaeology Project");
        assertTextPresentInPage("17");
    }

    public void testDatasetView() {
        gotoPage("/dataset/3088");
        assertTextPresentInPage("Knowth Stage 8 Fauna Dataset");
        assertTextPresentInPage("Dataset");
        assertTextPresentInPage("dataset_3088_knowthstage8.xls");
    }

    public void testBasicSearchView() {
        gotoPage("/search/basic");
        assertTextPresentInPage("Limit by");
    }

    public void testAdvancedSearchView() {
        gotoPage("/search/advanced");
        assertTextPresentInPage("TDAR id:");
        assertTextPresentInPage("Temporal Limits");
        assertTextPresentInPage("Investigation Types");
        assertTextPresentInPage("Material Type(s)");
        assertTextPresentInPage("Cultural Term(s)");
    }

    @After
    public void cleanup() {
        webClient.closeAllWindows();
    }

}
