package org.tdar.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

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

import static org.junit.Assert.*;
import static org.tdar.TestConstants.*;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractWebTestCase extends AbstractIntegrationTestCase {

    final Logger logger = LoggerFactory.getLogger(AbstractAnonymousWebTestCase.class);

    final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
    protected Page internalPage;
    protected HtmlPage htmlPage;
    private HtmlForm internalForm;
    public static final String TABLE_METADATA = "table metadata";
    public static String PROJECT_ID_FIELDNAME = "projectId";

    String regex = "&lt;(.+?)&gt;";
    Pattern pattern = Pattern.compile(regex);
    
    //using linked hash for consistent execution order
    @SuppressWarnings("serial")
    Map<String,String> encodingErrorRegexes = new LinkedHashMap<String, String>() {{
        //note that braces are java regex meta instructions and must be escaped (oh and don't forget to escape the escape character... man I hate you java)
        put("possible html encoding inside json, open-brace", "\\{&quot;");
        put("possible html encoding inside json, close-brace", "&quot;\\}");
        put("possible html encoding inside json, quoted-key", "\\{&quot;:");
        put("double-encoded html tag", "&lt;(.+?)&gt;");
        put("double-encoded html attribute pair", "\\w+\\s?=\\s?&quot;\\w+&quot;");
    }};
    
    Map<String, Pattern> encodingErrorPatterns = new LinkedHashMap<String, Pattern>();
    
    public AbstractWebTestCase() {
        for(Map.Entry<String, String> entry  : encodingErrorRegexes.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getValue());
            encodingErrorPatterns.put(entry.getKey(), pattern);
        }
    }

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
        assertFalse("An error ocurred" + internalPage.getWebResponse().getContentAsString(), statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertNoEscapeIssues();
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
        assertNoEscapeIssues();
        webClient.setThrowExceptionOnFailingStatusCode(true);
        return internalPage.getWebResponse().getStatusCode();
    }

    public void assertTextPresentInPage(String text) {
        assertTextPresentInPage(text, true);
    }

    public void assertTextPresentInPage(String text, boolean sensitive) {
        HtmlPage page = (HtmlPage) internalPage;
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + page.asText(), page.asText().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + page.asText(),
                    page.asText().toLowerCase().contains(text.toLowerCase()));
        }
    }

    public void assertTextPresentInCode(String text) {
        assertTextPresentInCode(text, true);
    }

    public void assertTextPresentInCode(String text, boolean sensitive) {
        if (sensitive) {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageCode(), getPageCode().contains(text));
        } else {
            assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageCode(), getPageCode().toLowerCase().contains(text));
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
        assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(), internalPage.getWebResponse().getContentAsString()
                .contains(text));
    }

    public void assertTextPresentIgnoreCase(String text) {
        assertTrue("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(),
                StringUtils.containsIgnoreCase(internalPage.getWebResponse().getContentAsString(), text));
    }

    public void assertTextNotPresentIgnoreCase(String text) {
        assertFalse("looking for [" + text + "] in page:" + internalPage.getUrl() + "\n" + getPageText(),
                StringUtils.containsIgnoreCase(internalPage.getWebResponse().getContentAsString(), text));
    }

    public void assertPageTitleEquals(String expectedTitle) {
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            assertEquals(expectedTitle.toLowerCase(), page.getTitleText().toLowerCase());
        }
        else {
            Assert.fail(String.format("was looking for <title>%s</title> but server response was not a valid html page", expectedTitle));
        }
    }

    public HtmlElement getInput(String name) {
        HtmlPage page = (HtmlPage) internalPage;
        return page.getElementByName(name);
    }

    public void setInput(String name, String value) {
        setInput(name, value, true);
    }

    public void setInput(String name, String value, boolean overrideCreate) {
        HtmlPage page = (HtmlPage) internalPage;
        String id = null;

        // a pattern that describes struts "indexed" form field names (e.g.
        // <input name="mybean[2].myBeanField" /> or <input name="myvar[0]" />)
        String indexedNamePattern = "(.+)\\[(\\d+)\\](\\..+)?";
        HtmlElement input = null;
        try {
            page.getElementByName(name);
        } catch (Exception e) {
            logger.trace("no element found: " + name);
        }

        if (input == null && overrideCreate) {
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
        }
        input = page.getElementByName(name);

        if (input instanceof HtmlTextArea) {
            HtmlTextArea txt = (HtmlTextArea) input;
            id = txt.getId();
            txt.setText(value);
        } else if (input instanceof HtmlSelect) {
            HtmlSelect sel = (HtmlSelect) input;
            sel.setSelectedAttribute(value, true);
            // assertTrue(sel.getSelectedOptions().get(0).getAttributes().getNamedItem("value").getTextContent() + " should equal " +
            // value,sel.getSelectedOptions().get(0).getAttributes().getNamedItem("value").getTextContent().equals(value));
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
        if (internalForm != null) {
            internalForm.appendChild(createdElement);
        } else {
            logger.warn("internalPage was null - element will be added to all forms on the page");
            HtmlPage page = (HtmlPage) internalPage;
            List<HtmlForm> forms = page.getForms();
            if (forms.isEmpty()) {
                Assert.fail("cannot create input element because page does not have any forms");
            }
            for (HtmlForm form : page.getForms()) {
                form.appendChild(createdElement);
                logger.debug("appending name:'{}'\t value:'{}'\t to form:'{}'", new Object[] { name, value, form.getId() });
            }
        }
    }

    public void createInput(String inputName, String name, Number value) {
        createInput(inputName, name, value.toString());
    }

    public boolean removeElementsByName(String elementName) {
        if (htmlPage == null)
            return false;
        List<HtmlElement> elements = htmlPage.getElementsByName(elementName);
        int count = 0;
        for (HtmlElement element : elements) {
            element.remove();
            count++;
        }
        return count > 0;
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
        HtmlInput input = null;
        try {
            input = internalForm.getInputByValue(buttonText);
        } catch (Exception ex) {
            logger.error("button element not found", ex);
        }
        assertNotNull("button with text [" + buttonText + "] not found", input);
        assertTrue(input.getTypeAttribute().equalsIgnoreCase("submit"));
    }

    public int submitForm() {
        return submitForm("Save");
    }

    public int submitForm(String buttonText) {
        submitFormWithoutErrorCheck(buttonText);
        int statusCode = internalPage.getWebResponse().getStatusCode();
        assertFalse(statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertNoErrorTextPresent();
        assertNoEscapeIssues();
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
        if (contents.contains(text)) {
            logger.debug("text " + text + " found in " + contents);
        }
        assertFalse("text should not be present [" + text + "] in page:" + internalPage.getUrl(), contents.contains(text));
    }

    /**
     * Assert that the page is not an error page and does or contain any inline stacktraces
     */
    public void assertNoErrorTextPresent() {
        assertTextNotPresent("Exception stack trace:" + getPageText()); // inline stacktrace (ftl compiles but dies partway through rendering)
        assertTextNotPresentIgnoreCase("HTTP ERROR");
        assertTextNotPresentIgnoreCase("Exception" + getPageText()); // inline stacktrace (ftl compiles but dies partway through rendering)
        assertFalse("page shouldn't contain action errors" + getPageText(), getPageCode().contains("class=\"action-error\""));
    }

    public void assertNoEscapeIssues() {
        String html = getPageCode().toLowerCase();
        for(Map.Entry<String, Pattern> entry : encodingErrorPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(html);
            if(matcher.find()) {
                String msg = "encoding issue \"%s\" found at pos[%s,%s] : '%s'";
                Assert.fail(String.format(msg, entry.getKey(), matcher.start(), matcher.end(), matcher.group()));
            }
        }
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
        if (internalPage instanceof HtmlPage) {
            htmlPage = (HtmlPage) internalPage;
            assertNoEscapeIssues();
        }
    }

    public void clickLinkOnPage(String text) {
        HtmlAnchor anchor = findPageLink(text);
        assertNotNull("could not find link with " + text + " on " + getPageText(), anchor);
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
        gotoPage("/ontology/3029");
        assertTextPresentInPage("Fauna Pathologies - Default Ontology Draft");
        assertTextPresentInPage("Indeterminate");
        assertTextPresentInPage("Fauna");
    }

    public void testCodingSheetView() {
        gotoPage("/coding-sheet/449");
        logger.trace("\n----------- page begin--------\n" + getPageText() + "\n----------- page begin--------\n");
        assertTextPresentInPage("CARP Fauna Proximal-Distal");
        assertTextPresentInPage("Subcategory: Portion/Proximal/Distal");
    }

    public void testProjectView() {
        // this should probably be done @before every test but it would slow things down even more
        searchIndexService.indexAll();

        gotoPage("/project/3805");
        logger.debug(getPageText());
        assertTextPresentInPage("New Philadelphia Archaeology Project");
        assertTextPresentInPage("Block 3, Lot 4");
    }

    public void testDocumentView() {
        gotoPage("/document/" + TestConstants.TEST_DOCUMENT_ID);
        assertTextPresentInPage("2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1");
        assertTextPresentInPage("a2008reportchap4.pdf");
        assertTextPresentInPage("New Philadelphia Archaeology project");
        assertTextPresentInPage("17");
    }

    public void testDatasetView() {
        gotoPage("/dataset/3088");
        logger.info("content of dataset view page: {}", getPageText());
        assertTextPresentInPage("Knowth Stage 8 Fauna Dataset");
        assertTextPresentInPage("Dataset");
        assertTextPresentInPage("dataset_3088_knowthstage8.xls");
    }

    public void testBasicSearchView() {
        gotoPage("/search/basic");
        assertTextPresentInPage("Search");
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

    public Long extractTdarIdFromCurrentURL() {
        String url = internalPage.getUrl().toString();
        while (url.indexOf("/") != -1) {
            String part = url.substring(url.lastIndexOf("/"));
            part = part.replace("/", "");
            url = url.substring(0, url.lastIndexOf("/"));
            logger.trace("evaluating {} : {}", url, part);
            if (StringUtils.isNumeric(part)) {
                return Long.parseLong(part);
            }
        }
        throw new TdarRecoverableRuntimeException("could not find tDAR ID in URL" + internalPage.getUrl().toString());
    }

    public void assertCurrentUrlEquals(String url) {
        String msg = String.format("actual page: %s; assumed page: %s; status: %s", internalPage.getUrl(), url, internalPage.getWebResponse().getStatusCode());
        assertEquals(msg, internalPage.getUrl().toString(), url);
    }

    public void assertCurrentUrlContains(String url) {
        String msg = String.format("actual page: %s; assumed page should have in URL: %s; status: %s", internalPage.getUrl(), url, internalPage
                .getWebResponse().getStatusCode());
        assertTrue(msg, internalPage.getUrl().toString().contains(url));
    }

}
