package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.tdar.TestConstants.DEFAULT_BASE_URL;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
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
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.collect.Lists;
import com.threelevers.css.Selector;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractWebTestCase extends AbstractIntegrationTestCase {

    public static final String RESTRICTED_ACCESS_TEXT = "This resource is restricted from general view";

    private static final String ELIPSIS = "<!-- ==================== ... ======================= -->";
    private static final String BEGIN_PAGE_HEADER = "<!-- BEGIN-PAGE-HEADER -->";
    private static final String BEGIN_TDAR_CONTENT = "<!-- BEGIN-TDAR-CONTENT -->";
    private static final String BEGIN_TDAR_FOOTER = "<!-- BEGIN-TDAR-FOOTER -->";
    public static final String TABLE_METADATA = "table metadata";
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
    protected Page internalPage;
    protected HtmlPage htmlPage;
    private HtmlForm _internalForm;
    private HtmlElement documentElement;
    public static String PROJECT_ID_FIELDNAME = "projectId";
    protected static final String MY_TEST_ACCOUNT = "my test account";
    protected static final String THIS_IS_A_TEST_DESCIPTION = "this is a test desciption";

    protected Set<String> encodingErrorExclusions = new HashSet<String>();

    @SuppressWarnings("serial")
    private Map<String, Pattern> encodingErrorPatterns = new LinkedHashMap<String, Pattern>() {
        {
            // note that braces are java regex meta instructions and must be escaped (oh and don't forget to escape the escape character... man I hate you java)
            put("possible html encoding inside json, open-brace", Pattern.compile("\\{&quot;"));
            put("possible html encoding inside json, close-brace", Pattern.compile("&quot;\\}"));
            put("possible html encoding inside json, quoted-key", Pattern.compile("\\{&quot;:"));
            put("double-encoded html tag", Pattern.compile("&lt;(.+?)&gt;"));
            put("double-encoded html attribute pair", Pattern.compile("\\w+\\s?=\\s?&quot;\\w+&quot;"));
        }
    };

    // disregard an encoding error if it's in the exclusions set;

    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public static String getBaseUrl() {
        return System.getProperty("tdar.baseurl", DEFAULT_BASE_URL);
    }

    public Page getPage(String localPath) {
        try {
            if (localPath.startsWith("http")) {
                return webClient.getPage(localPath);
            } else {
                String prefix = getBaseUrl();
                try {
                    URL current = internalPage.getUrl();
                    prefix = String.format("%s://%s:%s", current.getProtocol(), current.getHost(), current.getPort());
                    logger.info("SETTING URL TO {}{}" , prefix , localPath);
                } catch (Exception e) {
                    logger.trace("{}", e);
                }
                return webClient.getPage(prefix + localPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("couldn't find page at " + localPath, e);
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
            input = page.getElementByName(name);
        } catch (Exception e) {
            logger.trace("no element found: " + name);
        }

        if (input == null && overrideCreate) {
            // test for duplicating fields with the two cases we have (a) struts, or
            // (b) the non-standard file-upload
            if (name.matches(indexedNamePattern)) {
                // clone zeroth collection item (e.g. if we want to create element named 'person[3].firstName' we clone element named 'person[0].firstName')
                String zerothFieldName = name.replaceAll(indexedNamePattern, "$1[0]$3");
                if (!name.equals(zerothFieldName)) {
                    duplicateInputByName(zerothFieldName, name);
                }
            }
            input = page.getElementByName(name);
        }
        assertTrue("could not find input for name: " + name, input != null);

        if (input instanceof HtmlTextArea) {
            HtmlTextArea txt = (HtmlTextArea) input;
            id = txt.getId();
            txt.setText(value);
        } else if (input instanceof HtmlSelect) {
            HtmlSelect sel = (HtmlSelect) input;
            HtmlOption option = null;
            for (HtmlOption option_ : sel.getSelectedOptions()) {
                option_.setSelected(false);

            }
            try {
                option = sel.getOptionByValue(value);
            } catch (ElementNotFoundException enfe) {

            }
            if (option == null) {
                logger.warn("option value " + value + " did not exist, creating it");
                option = (HtmlOption) ((HtmlPage) internalPage).createElement("option");
                option.setValueAttribute(value);
                sel.appendChild(option);
                option.setSelected(true);
            }
            sel.setSelectedAttribute(value, true);
            // assertTrue(sel.getSelectedOptions().get(0).getAttributes().getNamedItem("value").getTextContent() + " should equal " +
            // value,sel.getSelectedOptions().get(0).getAttributes().getNamedItem("value").getTextContent().equals(value));
            id = sel.getId();
        } else if (input instanceof HtmlCheckBoxInput) {
            // if the checkbox's value attribute matches the supplied value, check the box. Otherwise uncheck it.
            HtmlCheckBoxInput chk = (HtmlCheckBoxInput) input;
            id = chk.getId();
            chk.setChecked(chk.getValueAttribute().equalsIgnoreCase(value));
        } else if (input instanceof HtmlRadioButtonInput) {
            // we have a collection of elements with the same name
            id = checkRadioButton(value, page.getElementsByName(name));
        } else {
            HtmlInput inp = (HtmlInput) input;
            id = inp.getId();
            inp.setValueAttribute(value);
        }
        assertTrue("could not find field: " + name, id != null);
        updateMainFormIfNull(id);
    }

    private String checkRadioButton(String value, List<DomElement> radioButtons) {
        List<HtmlInput> buttonsFound = new ArrayList<HtmlInput>();
        for (DomElement radioButton : radioButtons) {
            if (radioButton.getId().toLowerCase().endsWith(value.toLowerCase())) {
                buttonsFound.add((HtmlInput) radioButton);
            }
        }
        assertTrue("found more than one candidate radiobutton for value " + value, buttonsFound.size() == 1);
        HtmlInput radioButton = buttonsFound.get(0);
        radioButton.setChecked(true);
        return radioButton.getId();
    }

    public void createInput(String inputName, String name, String value) {
        HtmlElement createdElement = (HtmlElement) ((HtmlPage) internalPage).createElement("input");
        createdElement.setAttribute("type", inputName);
        createdElement.setAttribute("name", name);
        createdElement.setAttribute("value", value);
        if (getForm() != null) {
            getForm().appendChild(createdElement);
        }
    }

    public void createInput(String inputName, String name, Number value) {
        createInput(inputName, name, value.toString());
    }

    public <T> void createTextInput(String name, T value) {
        // treat null as empty string
        String strValue = "" + value;
        createInput("text", name, strValue);
    }

    // create several text inputs. element name will be String.format(nameFormat, listIndex);
    public <T> void createTextInput(String nameFormat, List<T> values) {
        createTextInputs(nameFormat, values, 0);
    }

    public <T> void createTextInputs(String nameFormat, List<T> values, int startingIndex) {
        for (int i = startingIndex; i < startingIndex + values.size(); i++) {
            T value = values.get(i);
            String name = String.format(nameFormat, i);
            createTextInput(name, value);
        }
    }

    public boolean removeElementsByName(String elementName) {
        if (htmlPage == null)
            return false;
        List<DomElement> elements = htmlPage.getElementsByName(elementName);
        int count = 0;
        for (DomElement element : elements) {
            element.remove();
            count++;
        }
        return count > 0;
    }

    public boolean checkInput(String name, String val) {
        List<DomElement> els = getHtmlPage().getElementsByName(name);
        for (DomElement el : els) {
            logger.trace(String.format("checkinput[%s --> %s] %s", name, val, el.asXml()));
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
        for (DomElement input : page.getElementsByName(name)) {
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
        updateMainFormIfNull(id);
    }

    public void assertButtonPresentWithText(String buttonText) {
        HtmlElement input = getButtonWithName(buttonText);
        assertNotNull(String.format("button with text [%s] not found in form [%s]", buttonText, getForm()), input);
        assertTrue(input.getAttribute("type").equalsIgnoreCase("submit"));
    }

    public int submitForm() {
        String defaultEditButton = "submitAction";
        HtmlElement buttonWithName = getButtonWithName(defaultEditButton);
        if (buttonWithName == null) {
            return submitForm("Save");
        }
        return submitForm(defaultEditButton);
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
            HtmlElement buttonByName = getButtonWithName(buttonText);
            changePage(buttonByName.click());
        } catch (IOException iox) {
            logger.error("exception while trying to submit from via button labeled " + buttonText, iox);
        }
    }

    private HtmlElement getButtonWithName(String buttonText) {
        // get all the likely suspects we consider to be a "button" and return the best match
        logger.trace("get button by name, form {}", _internalForm);
        List<HtmlElement> elements = new ArrayList<HtmlElement>();
        elements.addAll(getForm().getButtonsByName(buttonText));
        elements.addAll(getForm().getInputsByValue(buttonText));
        for (DomElement el : getHtmlPage().getElementsByName(buttonText)) {
            elements.add((HtmlElement) el);
        }

        if (elements.isEmpty()) {
            logger.error("could not find button or element with name or value '{}'", buttonText);
            return null;
        } else {
            return elements.iterator().next();
        }
    }

    public void assertErrorsPresent() {
        assertTextPresent("the following problems with your submission");
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
            logger.trace("text " + text + " found in " + contents);
        }
        assertFalse("text should not be present [" + text + "] in page:" + internalPage.getUrl(), contents.contains(text));
    }

    /**
     * Assert that the page is not an error page and does or contain any inline stacktraces
     */
    public void assertNoErrorTextPresent() {
        assertTextNotPresent("Exception stack trace: " + getCurrentUrlPath() + ":" + getPageText()); // inline stacktrace (ftl compiles but dies partway through
                                                                                                     // rendering)
        assertTextNotPresentIgnoreCase("HTTP ERROR");
        assertTextNotPresentIgnoreCase("Exception " + getCurrentUrlPath() + ":" + getPageText()); // inline stacktrace (ftl compiles but dies partway through
                                                                                                  // rendering)
        assertFalse("page shouldn't contain action errors " + getCurrentUrlPath() + ":" + getPageText(), getPageCode().contains("class=\"action-error\""));
    }

    public void assertNoEscapeIssues() {
        String html = getPageCode().toLowerCase();
        for (Map.Entry<String, Pattern> entry : encodingErrorPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(html);
            if (matcher.find()) {
                String msg = "encoding issue \"%s\" found at pos[%s,%s] : '%s'";
                int start = matcher.start() - 100;
                int end = matcher.end() + 100;
                int max = getPageCode().length();
                if (start < 0) {
                    start = 0;
                }
                if (end > max) {
                    end = max;
                }
                String matchAndContext = getPageCode().subSequence(start, end).toString();
                String exactMatch = getPageCode().subSequence(matcher.start(), matcher.end()).toString();

                if (!encodingErrorExclusions.contains(exactMatch)) {
                    Assert.fail(String.format(msg, entry.getKey(), matcher.start(), matcher.end(), matchAndContext));
                }
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
        assertNotNull(String.format("link with text [%s] not found on page %s", text, getPageCode()), anchor);
        return anchor;
    }

    public void clickLinkWithText(String text) {
        clickLinkOnPage(text);
    }

    public void changePage(Page page) {
        if (page == null) {
            fail("changed to a null page for some reason");
            return;
        }
        internalPage = page;
        _internalForm = null;
        logger.info("CHANGING url TO: " + internalPage.getUrl());
        if (internalPage instanceof HtmlPage) {
            htmlPage = (HtmlPage) internalPage;
            documentElement = htmlPage.getDocumentElement();
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

    // click on element that ostensibly should trigger a request (links, submit inputs, submit guttons)
    public void clickElementWithId(String id) {
        HtmlElement el = (HtmlElement) getHtmlPage().getElementById(id);
        try {
            changePage(el.click());
        } catch (IOException e) {
            Assert.fail("click failed:" + e);
        }
    }

    public String getPageText() {
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            return page.asText();
        }
        if (internalPage instanceof TextPage) {
            return ((TextPage) internalPage).getContent();
        }

        return internalPage.getWebResponse().getContentAsString();
    }
    
    
    //return a fun-sized version of the response string ( title section, the error section and h1 through to the footer);
    //FIXME:  too much expurgation!!!
    public String getPageBodyCode() {
        String content = getPageCode();
        String out = "";
        try {
            if (content.indexOf(BEGIN_PAGE_HEADER) != -1 && content.indexOf(BEGIN_TDAR_CONTENT) != -1 && content.indexOf(BEGIN_TDAR_FOOTER) != -1) {
                out = content.substring(0, content.indexOf(BEGIN_PAGE_HEADER)) + ELIPSIS;
                out += content.subSequence(content.indexOf(BEGIN_TDAR_CONTENT) + BEGIN_TDAR_CONTENT.length(), content.indexOf(BEGIN_TDAR_FOOTER)) + ELIPSIS;
                return out;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
   

    public String getPageCode() {
        String content = internalPage.getWebResponse().getContentAsString();
        return content;
    }

    public String getCurrentUrlPath() {
        return internalPage.getUrl().getPath() + "?" + internalPage.getUrl().getQuery();
    }

    @Before
    public void prepare() {
        // FIXME: This is far less than ideal, but there's a problem with how
        // the MAC is handling memory
        // and appears to be 'leaking' with jwebunit and gooogle maps. Hence, we
        // need to disable javascript
        // testing on the mac :(
        // if (System.getProperty("os.name").toLowerCase().contains("mac os x"))
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(0);
        // webClient.getOptions().setSSLClientCertificate(certificateUrl, certificatePassword, certificateType)
        webClient.setJavaScriptTimeout(0);

        // reset encoding error exclusions for each test
        encodingErrorExclusions = new HashSet<String>();
        // <generated> gets emitted by cglib methods in stacktrace, let's not consider it to be a double encoding error.
        encodingErrorExclusions.add("&lt;generated&gt;");
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
        logger.trace(getPageText());
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
        logger.trace("content of dataset view page: {}", getPageText());
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
        assertTextPresentInPage("Limit by geographic region");
        assertTextPresentInPage("Choose Search Terms");
        assertTextPresentInPage("All Fields");
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
            if (StringUtils.isNotBlank(part) && StringUtils.isNumeric(part)) {
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

    // get the "main" form. it's pretty much a guess, so if you encounter a page w/ multiple forms you might wanna specify it outright
    public HtmlForm getForm() {
        logger.trace("FORM{} OTHERS: {}", _internalForm, getHtmlPage().getForms());
        if (_internalForm == null) {
            HtmlForm htmlForm = null;
            if (getHtmlPage().getForms().size() == 1) {
                htmlForm = getHtmlPage().getForms().get(0);
                logger.trace("only one form: " + htmlForm.getNameAttribute());
            } else {
                for (HtmlForm form : getHtmlPage().getForms()) {
                    if (StringUtils.isNotBlank(form.getActionAttribute()) && !form.getNameAttribute().equalsIgnoreCase("autosave") &&
                            !form.getNameAttribute().equalsIgnoreCase("searchheader")) {
                        htmlForm = form;
                        logger.trace("using form: " + htmlForm.getNameAttribute());
                        break;
                    }
                }
            }
            _internalForm = htmlForm;
        }

        return _internalForm;
    }

    public HtmlForm getForm(String formName) {
        return getHtmlPage().getFormByName(formName);
    }

    protected void setMainForm(HtmlForm form) {
        _internalForm = form;
    }

    protected void setMainForm(String formName) {
        _internalForm = getHtmlPage().getFormByName(formName);
    }

    // set the main form to be first form that contains a child element with the specified id
    private void updateMainFormIfNull(String id) {
        if (_internalForm != null || StringUtils.isBlank(id))
            return;
        for (HtmlForm form : getHtmlPage().getForms()) {
            if (form.getFirstByXPath("descendant-or-self::*[contains(@id,'" + id + "')]") != null) {
                logger.info("updating main for for id: " + id + " to form: " + form);
                setMainForm(form);
                return;
            }
        }
        logger.warn("No form found containing id '{}'", id);
    }

    protected List<Element> querySelectorAll(String cssSelector) {
        Iterable<Element> elements = Selector.from(documentElement).select(cssSelector);
        List<Element> elementList = Lists.newArrayList(elements);
        return elementList;
    }

    protected Element querySelector(String cssSelector) {
        return Selector.from(documentElement).select(cssSelector).iterator().next();
    }

    public String getPersonalFilestoreTicketId() {
        gotoPageWithoutErrorCheck("/upload/grab-ticket");
        TextPage textPage = (TextPage) internalPage;
        String json = textPage.getContent();
        logger.debug("ticket json::" + json.trim());
        JSONObject jsonObject = JSONObject.fromObject(json);
        String ticketId = jsonObject.getString("id");
        logger.debug("ticket id::" + ticketId);
        return ticketId;
    }

    /**
     * upload the specified file to the personal filestore. Note this will change the current page of the webclient
     * 
     * @param ticketId
     * @param path
     */
    public void uploadFileToPersonalFilestore(String ticketId, String path) {
        uploadFileToPersonalFilestore(ticketId, path, true);
    }

    public void addFileProxyFields(int rowNum, FileAccessRestriction restriction, String filename) {
        createInput("hidden", "fileProxies[" + rowNum + "].restriction", restriction.name());
        createInput("hidden", "fileProxies[" + rowNum + "].action", FileAction.ADD.name());
        createInput("hidden", "fileProxies[" + rowNum + "].fileId", "-1");
        createInput("hidden", "fileProxies[" + rowNum + "].filename", filename);
        createInput("hidden", "fileProxies[" + rowNum + "].sequenceNumber", Integer.toString(rowNum));
    }

    public int uploadFileToPersonalFilestoreWithoutErrorCheck(String ticketId, String path) {
        return uploadFileToPersonalFilestore(ticketId, path, false);
    }

    private int uploadFileToPersonalFilestore(String ticketId, String path, boolean assertNoErrors) {
        int code = 0;
        WebClient client = getWebClient();
        String url = getBaseUrl() + "/upload/upload";
        try {
            WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            parms.add(nameValuePair("ticketId", ticketId));
            File file = null;
            if (path != null) {
                file = new File(path);
                parms.add(nameValuePair("uploadFile", file));
            }
            webRequest.setRequestParameters(parms);
            webRequest.setEncodingType(FormEncodingType.MULTIPART);
            Page page = client.getPage(webRequest);
            code = page.getWebResponse().getStatusCode();
            Assert.assertTrue(assertNoErrors && code == HttpStatus.OK.value());
            if (file != null) {
                assertFileSizes(page, Arrays.asList(new File[] { file }));
            }
        } catch (MalformedURLException e) {
            Assert.fail("mailformed URL: are you sure you specified the right page in your test?");
        } catch (IOException iox) {
            Assert.fail("IO exception occured during test");
        } catch (FailingHttpStatusCodeException httpEx) {
            if (assertNoErrors) {
                Assert.fail("upload request returned code" + httpEx.getStatusCode());
            }
            code = httpEx.getStatusCode();
        }
        return code;
    }

    protected void assertFileSizes(Page page, List<File> files) {
        JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(page.getWebResponse().getContentAsString());
        for (int i = 0; i < files.size(); i++) {
            Assert.assertEquals("file size reported from server should be same as original", files.get(i).length(), jsonArray.getJSONObject(i).getLong("size"));
        }
    }

    public NameValuePair nameValuePair(String name, String value) {
        return new NameValuePair(name, value);
    }

    private NameValuePair nameValuePair(String name, File file) {
        // FIXME:is it safe to specify text/plain even when we know it isn't?? It happens to 'work' for these tests, not sure of potential side effects...
        return nameValuePair(name, file, "text/plain");
    }

    private NameValuePair nameValuePair(String name, File file, String contentType) {
        KeyDataPair keyDataPair = new KeyDataPair(name, file, contentType, "utf8");
        return keyDataPair;
    }

    public void createDocumentAndUploadFile(String title) {
        clickLinkWithText("UPLOAD");
        gotoPage("/resource/add");
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TestConstants.TEST_DOCUMENT);
        // gotoPage("/document/add");
        // assume that you're at /project/list
        // logger.info(getPageText());
        gotoPage("/");

        clickLinkWithText("UPLOAD");
        // logger.info(getPageCode());
        clickLinkWithText(ResourceType.DOCUMENT.getLabel());
        assertTextPresentInPage("Create a new Document");
        setInput("document.title", title);
        setInput("document.description", title + " (ABSTRACT)");
        setInput("document.date", "1934");
        setInput("ticketId", ticketId);
        setInput("projectId", Long.toString(TestConstants.ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
//            setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        addFileProxyFields(0, FileAccessRestriction.PUBLIC, TestConstants.TEST_DOCUMENT_NAME);
        // logger.info(getPageCode());
        submitForm();
        HtmlPage page = (HtmlPage) internalPage;
        // make sure we're on the view page
        assertPageTitleEquals(title);
        assertTextPresentInPage(title + " (ABSTRACT)");
        assertTextPresentInPage(TestConstants.TEST_DOCUMENT_NAME);
    }

    protected String testAccountPollingResponse(String total, TransactionStatus expectedResponse) throws MalformedURLException {
        assertCurrentUrlContains("/simple");

        String invoiceid = getInput("id").getAttribute("value");
        submitForm();
        assertCurrentUrlContains("process-payment-request");
        clickLinkWithText("click here");
        URL polingUrl = new URL(getBaseUrl() + "/cart/polling-check?id=" + invoiceid);
        String response = getAccountPollingRequest(polingUrl);
        assertTrue(response.contains(TransactionStatus.PENDING_TRANSACTION.name()));
        checkInput(NelnetTransactionItem.getInvoiceIdKey(), invoiceid);
        checkInput(NelnetTransactionItem.getUserIdKey(), Long.toString(getUserId()));
//        logger.info(getPageBodyCode());
        checkInput(NelnetTransactionItem.AMOUNT_DUE.name(), total);
        clickElementWithId("process-payment_0");
        response = getAccountPollingRequest(polingUrl);
        assertTrue(response.contains(expectedResponse.name()));
        return invoiceid;
    }

    protected String getAccountPollingRequest(URL polingUrl) {
        WebWindow openWindow = webClient.openWindow(polingUrl, "polling" + System.currentTimeMillis());
        return openWindow.getEnclosedPage().getWebResponse().getContentAsString();
    }

    /*
     * add new account, add another, make sure account names are all ok
     */
    protected String addInvoiceToNewAccount(String invoiceId, String accountId, String accountName) {
        if (accountName == null) {
            accountName = MY_TEST_ACCOUNT;
        }
        if (accountId != null) {
            gotoPage("/billing/choose?invoiceId=" + invoiceId + "&accountId=" + accountId);
            setInput("id", accountId);
        } else {
            gotoPage("/billing/add?invoiceId=" + invoiceId);
            setInput("account.name", accountName);
            setInput("account.description", THIS_IS_A_TEST_DESCIPTION);
        }
        List<Person> users = entityService.findAllRegisteredUsers(3);
        List<Long> userIds = Persistable.Base.extractIds(users);
        for (int i = 0; i < userIds.size(); i++) {
            setInput("authorizedMembers[" + i + "].id", Long.toString(userIds.get(i)));
        }
        submitForm();
        assertAccountPageCorrect(users, userIds, accountName);
        clickLinkOnPage("edit");
        String id = getInput("id").getAttribute("value");
        submitForm();
        assertAccountPageCorrect(users, userIds, accountName);
        return id;
    }

    protected void assertAccountPageCorrect(List<Person> users, List<Long> userIds, String title) {
        assertTextPresent(title);
        assertTextPresent(THIS_IS_A_TEST_DESCIPTION);
        for (int i = 0; i < userIds.size(); i++) {
            assertTextPresent(users.get(i).getProperName());
        }
        assertTextPresent(getSessionUser().getProperName());
    }

    public void login(String user, String pass) {
        login(user, pass, false);
    }

    public int login(String user, String pass, boolean expectingErrors) {
        gotoPage("/");
        clickLinkOnPage("Log In");
        setMainForm("loginForm");
        user = System.getProperty("tdar.user", user);
        pass = System.getProperty("tdar.pass", pass);
        // logger.info(user + ":" + pass);
        setInput("loginUsername", user);
        setInput("loginPassword", pass);
        if (expectingErrors) {
            webClient.setThrowExceptionOnFailingStatusCode(false);
            submitFormWithoutErrorCheck("Login");
            webClient.setThrowExceptionOnFailingStatusCode(true);
        } else {
            clickElementWithId("btnLogin");
        }
        return internalPage.getWebResponse().getStatusCode();
    }

    public void logout() {
        webClient.setJavaScriptEnabled(false);
        gotoPage("/logout");
    }

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    public void testLogin(Map<String, String> values, boolean deleteFirst) {

        String username = values.get("person.username");
        if (deleteFirst) {
            Person p = new Person();
            p.setUsername(username);
            authService.getAuthenticationProvider().deleteUser(p);
        }
        gotoPage("/");
        clickLinkOnPage("Sign Up");
        for (String key : values.keySet()) {
            setInput(key, values.get(key));
        }
        setInput("timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        submitForm("Save");
        genericService.synchronize();
        setSessionUser(entityService.findByUsername(username));
    }

    public void setupBasicUser(Map<String, String> personmap, String prefix) {
        personmap.put("person.firstName", prefix + "firstName");
        personmap.put("person.lastName", prefix + "lastName");
        personmap.put("person.email", prefix + "aaaaa@bbbbb.com");
        personmap.put("confirmEmail", prefix + "aaaaa@bbbbb.com");
        personmap.put("person.username", prefix + "aaaaa@bbbbb.com");
        personmap.put("password", "secret");
        personmap.put("confirmPassword", "secret");
        personmap.put("institutionName", "institution");
        personmap.put("person.phone", "1234567890");
        personmap.put("person.contributorReason", "there is a reason");
        // personmap.put("person.rpaNumber", "1234567890");
        personmap.put("requestingContributorAccess", "true");
    }
    
    @Override
    public void onFail(Throwable e, Description description) {
       //FIXME:  need to get this to fire *before* the @After method logs out.  otherwise the pageCode will always be the tdar login screen.
       // logger.error("{} failed. server response below:\n\n {}", description.getDisplayName(), getPageCode());
    }

}
