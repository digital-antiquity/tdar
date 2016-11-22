package org.tdar.web;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.tdar.TestConstants;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.junit.WebTestCase;
import org.tdar.utils.TestConfiguration;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.InteractivePage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptException;
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
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * @author Adam Brin
 * 
 */
//@ContextConfiguration(classes = TdarAppConfiguration.class)
public abstract class AbstractWebTestCase  implements WebTestCase {

    private static final String CART_REVIEW = "/cart/review";

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    public static final String RESTRICTED_ACCESS_TEXT = "This resource is restricted from general view";

    // formats for form element names
    public static final String FMT_AUTHUSERS_ID = "authorizedUsers[%s].user.id";
    public static final String FMT_AUTHUSERS_LASTNAME = "authorizedUsers[%s].user.lastName";
    public static final String FMT_AUTHUSERS_FIRSTNAME = "authorizedUsers[%s].user.firstName";
    public static final String FMT_AUTHUSERS_EMAIL = "authorizedUsers[%s].user.email";
    public static final String FMT_AUTHUSERS_INSTITUTION = "authorizedUsers[%s].user.institution.name";
    public static final String FMT_AUTHUSERS_PERMISSION = "authorizedUsers[%s].generalPermission";
    public static List<String> errorPatterns = Arrays.asList("http error", "server error", "{0}", "{1}", "{2}", "{3}", "{4}", ".exception.", "caused by",
            "problems with this submission", "TDAR:500", "TDAR:404", "TDAR:509");

    private static final String ELIPSIS = "<!-- ==================== ... ======================= -->";
    private static final String BEGIN_PAGE_HEADER = "<!-- BEGIN-PAGE-HEADER -->";
    private static final String BEGIN_TDAR_CONTENT = "<!-- BEGIN-TDAR-CONTENT -->";
    private static final String BEGIN_TDAR_FOOTER = "<!-- BEGIN-TDAR-FOOTER -->";
    public static final String TABLE_METADATA = "table metadata";
    public static final String ACCOUNT_ID = "accountId";
    public static final String INVOICE_ID = "invoiceId";
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
    protected Page internalPage;
    protected HtmlPage htmlPage;
    private HtmlForm _internalForm;
    public static String PROJECT_ID_FIELDNAME = "projectId";
    protected static final String MY_TEST_ACCOUNT = "my test account";
    protected static final String THIS_IS_A_TEST_DESCIPTION = "this is a test desciption";
    // "link isn't allowed in <div> elements",

    // "unescaped & or unknown entity" /*add back later */,

    protected Set<String> encodingErrorExclusions = new HashSet<>();

    
    
    @Rule
    public TestName testName = new TestName();

    @Rule
    public TestWatcher failWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            AbstractWebTestCase.this.onFail(e, description);
        }

    };

    @Before
    public void announceTestStarting() {
        String fmt = " ***   RUNNING TEST: {}.{}() ***";
        logger.info(fmt, getClass().getSimpleName(), testName.getMethodName());
    }    

    @After
    public void announceTestOver() {
        String fmt = " *** COMPLETED TEST: {}.{}() ***";
        logger.info(fmt, getClass().getCanonicalName(), testName.getMethodName());
    }

    
    
    
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

    @SuppressWarnings("unused")
    private HtmlElement documentElement;
    protected boolean skipHtmlValidation = false;

    private boolean validateViaNu = false;

    // disregard an encoding error if it's in the exclusions set;

    /*
     * override to test with different URL can use this to point at another
     * instance of tDAR instead of running "integration" tests.
     */
    public String getBaseUrl() {
        return CONFIG.getBaseUrl();
    }

    public static String getBaseSecureUrl() {
        return CONFIG.getBaseSecureUrl();
    }

    public Page getPage(String localPath) {
        try {
            if (localPath.startsWith("http")) {
                return webClient.getPage(localPath);
            } else {
                String url = pathToUrl(localPath);
                return webClient.getPage(url);
            }
        } catch (Exception e) {
            logger.error("couldn't find page at {}", localPath, e);
        }
        return null;
    }

    public String pathToUrl(String localPath_) {
        String localPath = localPath_;
        String prefix = getBaseUrl();
        try {
            URL current = internalPage.getUrl();
            prefix = String.format("%s://%s:%s", current.getProtocol(), current.getHost(), current.getPort());
            logger.info("SETTING URL TO {}{}", prefix, localPath);
        } catch (Exception e) {
            logger.trace("{}", e);
        }

        if (localPath.startsWith("//")) {
            localPath = localPath.substring(1);
        }

        if (prefix.endsWith("/")) {
            while (localPath.startsWith("/")) {
                localPath = localPath.substring(1);
            }
        }

        String url = prefix + localPath;
        return url;
    }

    protected WebClient getWebClient() {
        return webClient;
    }

    // TODO: implemnent this, if viable (not sure yet if it will be easy or crazy hard).
    /**
     * asserts whether current web page contains text that matches regex based on the provided reges. So, if format string is
     * 
     * "Hello %s,  how are you doing this fine %s?"
     * 
     * becomes...
     * 
     * /.*Hello (.*?), how are you doing this fine (.*?)\?/i
     * 
     * @param formatString
     */
    public void assertContainsFormat(String formatString) {
        fail("not implemented");
    }

    /**
     * Go to the specified page, with explicit assertions that the server did not return with a 500 error or contain any inline exception text
     * 
     * @param path
     * @return http return code (if no errors found, otherwise assertions fail and method does not return);
     */
    public int gotoPage(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertThat(statusCode, not(anyOf(is(SC_INTERNAL_SERVER_ERROR), is(SC_BAD_REQUEST))));
        assertNoEscapeIssues();
        assertNoErrorTextPresent();
        assertNoAccessibilityErrors();
        return statusCode;
    }

    /**
     * Request a page similar to gotoPage(), sans html validaton
     * 
     * @param path
     * @return
     */
    public String gotoJson(String path) {
        int statusCode = gotoPageWithoutErrorCheck(path);
        assertThat(statusCode, not(anyOf(is(SC_INTERNAL_SERVER_ERROR), is(SC_BAD_REQUEST))));
        return internalPage.getWebResponse().getContentAsString();
    }

    private void assertNoAccessibilityErrors() {
        Pattern p = Pattern.compile("<img([^>]+)");
        Matcher matcher = p.matcher(getPageCode());
        List<String> errors = new ArrayList<>();
        while (matcher.find()) {
            boolean missingAlt = false;
            boolean missingTitle = false;
            String group = matcher.group(1);
            if (!group.contains(" alt=") && !group.contains(" alt =")) {
                missingAlt = true;
            }
            if (!group.contains(" title=") && !group.contains(" title =")) {
                missingTitle = true;
            }
            if (missingAlt || missingTitle) {
                errors.add(String.format("%s -- missing alt(%s) title(%s)", group, missingAlt, missingTitle));
            }
        }
        if (CollectionUtils.isNotEmpty(errors)) {
            fail(StringUtils.join(errors.toArray()));
        }

    }

    public Long createResourceFromType(ResourceType rt, String title) {
        final String path = "/" + rt.getUrlNamespace() + "/add";
        gotoPage(path);
        setInput(String.format("%s.%s", rt.getFieldName(), "title"), title);
        setInput(String.format("%s.%s", rt.getFieldName(), "description"), title + "::description");
        if (!rt.isProject()) {
            if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
                // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
                setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
            }
            setInput(String.format("%s.%s", rt.getFieldName(), "date"), "2134");
        }
        if (rt.isSupporting()) {
            setInput("fileInputMethod", "text");
            if (rt == ResourceType.ONTOLOGY) {
                setInput("fileTextInput", "text\ttext\r\n");
            } else {
                setInput("fileTextInput", "text,text\r\n");
            }
        }
        submitForm();
        return extractTdarIdFromCurrentURL();
    }

    protected void assertPageValidHtml() {
        if (skipHtmlValidation)
            return;
        if (internalPage.getWebResponse().getContentType().contains("json")) {
            try {
                JSONObject.fromObject(getPageCode());
            } catch (Exception e) {
                Assert.fail(String.format("%s : %s: %s", e.getMessage(), ExceptionUtils.getRootCauseStackTrace(e), getPageCode()));
            }
        }
        if (internalPage.getWebResponse().getContentType().toLowerCase().contains("html")) {
            HtmlValidator validator = new HtmlValidator();
            if (validateViaNu) {
                validator.validateHtmlViaNuValidator(internalPage);
            } else {
                validator.validateViaTidy(internalPage);
            }
        }
    }

    /**
     * Same as gotoPage(), but does not perform any assertions on the server response
     * 
     * @param path
     * @return http return code
     */
    public int gotoPageWithoutErrorCheck(String path) {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        changePage(getPage(path));
        assertNoEscapeIssues();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
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

    public void assertPageTitleContains(String expectedTitle) {
        if (internalPage instanceof HtmlPage) {
            HtmlPage page = (HtmlPage) internalPage;
            assertTrue(page.getTitleText().toLowerCase().contains(expectedTitle.toLowerCase()));
        }
        else {
            Assert.fail(String.format("was looking for <title>%s</title> but server response was not a valid html page", expectedTitle));
        }
    }

    public HtmlElement getInput(String name) {
        HtmlPage page = (HtmlPage) internalPage;
        try {
            return page.getElementByName(name);
        } catch (ElementNotFoundException nse) {
            throw new NoSuchElementException(String.format("Input * with Name %s on page %s cannot be found", name, internalPage.getUrl().toString()));
        }
    }

    public void setInput(String name, String value) {
        setInput(name, value, true);
    }

    public void setInput(String name, Number value) {
        setInput(name, String.format("%s", value));
    }

    public <T extends Enum<T>> void setInput(String name, T value) {
        setInput(name, String.format("%s", value.name()));
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
            logger.trace("no element found: {}", name, e);
        }

        if ((input == null) && overrideCreate) {
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
                logger.warn("option value {} did not exist, creating it", value);
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
            if (radioButton.getAttribute("value").equals(value)) {
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
        for (int i = startingIndex; i < (startingIndex + values.size()); i++) {
            T value = values.get(i);
            String name = String.format(nameFormat, i);
            createTextInput(name, value);
        }
    }

    public boolean removeElementsByName(String elementName) {
        if (htmlPage == null) {
            return false;
        }
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
            if ((el instanceof HtmlTextArea) && ((HtmlTextArea) el).getText().equals(val)) {
                return true;
            } else if (el instanceof HtmlSelect) {
                HtmlSelect sel = (HtmlSelect) el;
                for (HtmlOption o : sel.getSelectedOptions()) {
                    if (o.getValueAttribute().equalsIgnoreCase(val)) {
                        return true;
                    }
                }
            } else if ((el instanceof HtmlCheckBoxInput) || (el instanceof HtmlRadioButtonInput)) {
                if (((els.size() == 1) && val.equalsIgnoreCase("true")) || val.equalsIgnoreCase("false")) {
                    if (el.getAttribute("value").equalsIgnoreCase(val)) {
                        if (el.hasAttribute("checked") && val.equalsIgnoreCase("true")) {
                            return true;
                        }
                        if (!el.hasAttribute("checked") && val.equalsIgnoreCase("false")) {
                            return true;
                        }
                    }
                } else if (el.getAttribute("value").equalsIgnoreCase(val)
                        && (((el instanceof HtmlCheckBoxInput) && ((HtmlCheckBoxInput) el).isChecked()) || ((el instanceof HtmlRadioButtonInput)
                        && ((HtmlRadioButtonInput) el).isChecked()))) {
                    return true;
                }
            } else if (((el instanceof HtmlTextInput) || (el instanceof HtmlHiddenInput) || (el instanceof HtmlPasswordInput))
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

    public void submitFormWithoutErrorCheck() {
        String defaultEditButton = "submitAction";
        HtmlElement buttonWithName = getButtonWithName(defaultEditButton);
        if (buttonWithName == null) {
            defaultEditButton = "Save";
        }
        submitFormWithoutErrorCheck(defaultEditButton);
    }

    public int submitForm(String buttonText) {
        submitFormWithoutErrorCheck(buttonText);
        int statusCode = internalPage.getWebResponse().getStatusCode();
        assertFalse(statusCode == SC_INTERNAL_SERVER_ERROR);
        assertNoErrorTextPresent();
        assertNoEscapeIssues();
        assertPageValidHtml();
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
            changePage(buttonByName.click(), true);
        } catch (FailingHttpStatusCodeException | IOException iox) {
            logger.error("exception while trying to submit from via button labeled {}", buttonText, iox);
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
        assertTextPresent("the following problems with this submission");
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
            logger.trace("text {} found in {}", text, contents);
        }
        assertFalse("text should not be present [" + text + "] in page:" + internalPage.getUrl() + "\r\n" + getPageText(), contents.contains(text));
    }

    /**
     * Assert that the page is not an error page and does or contain any inline stacktraces
     */
    public void assertNoErrorTextPresent() {
        checkForFreemarkerExceptions();
        for (String err : errorPatterns) {
            assertTextNotPresentIgnoreCase(err);
        }
    }

    public void checkForFreemarkerExceptions() {
        assertTextNotPresent("Exception stack trace: " + getCurrentUrlPath() + ":" + getPageText()); // inline stacktrace (ftl compiles but dies partway through
                                                                                                     // rendering)
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
                    logger.debug(getPageCode());
                    Assert.fail(String.format(msg, entry.getKey(), matcher.start(), matcher.end(), matchAndContext));
                }
            }
        }
    }

    public HtmlPage getHtmlPage() {
        assertTrue("page is not a HtmlPage", internalPage instanceof HtmlPage);
        return (HtmlPage) internalPage;
    }

    public HtmlAnchor findPageLink(String text) {
        try {
            return getHtmlPage().getAnchorByText(text);
        } catch (ElementNotFoundException exception) {
            fail(String.format("link with text [%s] not found on page %s", text, getPageCode()));
            return null;
        }

    }

    public void clickLinkWithText(String text) {
        clickLinkOnPage(text);
    }

    public void changePage(Page page) {
        changePage(page, false);
    }

    public void changePage(Page page, boolean expectErrors) {
        if (page == null) {
            fail("changed to a null page for some reason");
            return;
        }
        internalPage = page;
        _internalForm = null;
        logger.info("CHANGING url TO: {}", internalPage.getUrl());
        if (internalPage instanceof HtmlPage) {
            htmlPage = (HtmlPage) internalPage;
            documentElement = htmlPage.getDocumentElement();
            if (!expectErrors) {
                assertNoEscapeIssues();
                assertPageValidHtml();
            }
        }
    }

    public void clickLinkByHref(String href) throws IOException {
        changePage(getHtmlPage().getAnchorByHref(href).click());
    }

    public void clickLinkOnPage(String text) {
        try {
            changePage(findPageLink(text).click());
        } catch (IOException e) {
            getLogger().warn("Couldn't click anchor {}", e);
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
            return getHtmlPage().asText();
        }
        if (internalPage instanceof TextPage) {
            return ((TextPage) internalPage).getContent();
        }

        return internalPage.getWebResponse().getContentAsString();
    }

    // return a fun-sized version of the response string ( title section, the error section and h1 through to the footer);
    // FIXME: too much expurgation!!!
    public String getPageBodyCode() {
        String content = getPageCode();
        String out = "";
        try {
            if ((content.indexOf(BEGIN_PAGE_HEADER) != -1) && (content.indexOf(BEGIN_TDAR_CONTENT) != -1) && (content.indexOf(BEGIN_TDAR_FOOTER) != -1)) {
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
        initializeAndConfigureWebClient();
        // reset encoding error exclusions for each test
        encodingErrorExclusions = new HashSet<String>();
        // <generated> gets emitted by cglib methods in stacktrace, let's not consider it to be a double encoding error.
        encodingErrorExclusions.add("&lt;generated&gt;");
        skipHtmlValidation = false;
    }

    protected void initializeAndConfigureWebClient() {
        webClient.getOptions().setUseInsecureSSL(true);
        // webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getCurrentWindow().setInnerWidth(1024);
        // if you enable css, then we need to figure out how to deal with CSS Responsive issues (hidden-phone-portrait) on UPLOAD Button 
        webClient.getOptions().setCssEnabled(false);
        CookieManager cookieMan = new CookieManager();
        cookieMan = webClient.getCookieManager();
        cookieMan.setCookiesEnabled(true);
        webClient.getOptions().setTimeout(0);
        // webClient.getOptions().setSSLClientCertificate(certificateUrl, certificatePassword, certificateType)
        webClient.setJavaScriptTimeout(0);
        
        webClient.setAlertHandler(new AlertHandler() {
            
            @Override
            public void handleAlert(Page page, String message) {
                logger.error("ALERT ON " + page.getUrl() + " : " + message);                
            }
        });

        webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

            @Override
            public void scriptException(InteractivePage page, ScriptException scriptException) {
                logger.error("JS load exception: {}({}:{}):: {}\n {}", page.getUrl(), scriptException.getFailingLineNumber(), scriptException.getFailingColumnNumber(), scriptException.getFailingLine(), scriptException, scriptException.getScriptSourceCode());
            }

            @Override
            public void timeoutError(InteractivePage page, long allowedTime, long executionTime) {
                logger.error("timeout exception: {} {}", page.getUrl(), allowedTime);
                
            }

            @Override
            public void malformedScriptURL(InteractivePage page, String url, MalformedURLException malformedURLException) {
                logger.error("malformed script URL exception: {} {}", page.getUrl(), malformedURLException);
                
            }

            @Override
            public void loadScriptError(InteractivePage page, URL scriptUrl, Exception exception) {
                logger.error("load script Error: {} {}", scriptUrl, exception);
                
            }
        });
        webClient.setCssErrorHandler(new ErrorHandler() {
            @Override
            public void warning(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.trace("CSS Warning:", exception);
                }
            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("/css/")) {
                    logger.warn("CSS Fatal Error:", exception);
                }
            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
                String uri = exception.getURI();
                if (uri.contains(getBaseUrl()) && uri.contains("tdar")) {
                    String msg = String.format("CSS Error: %s ; message: %s line: %s ", exception.getURI(), exception.getMessage(), exception.getLineNumber());
                    logger.error(msg);
                    fail(msg);
                }
            }
        });
    }

    public void testOntologyView() {
        gotoPage("/ontology/3029");
        assertTextPresentInPage("Fauna Pathologies - Default Ontology Draft");
        assertTextPresentInPage("Indeterminate");
        assertTextPresentInPage("Fauna");
        clickLinkWithText("Present (Present)");
        assertTextPresent("Synonyms");
        assertTextPresent("Parent");
        assertTextPresent("Datasets that use Present");
        clickLinkWithText("Fauna Pathologies - Default Ontology Draft (ontology root)");
    }

    public void testCodingSheetView() {
        gotoPage("/coding-sheet/449");
        logger.trace("\n----------- page begin--------\n{}\n----------- page begin--------\n", this);
        assertTextPresentInPage("CARP Fauna Proximal-Distal");
        assertTextPresentInPage("Subcategory: Portion/Proximal/Distal");
    }

    public void testProjectView() {
        // this should probably be done @before every test but it would slow things down even more
//        logger.debug(getCurrentUrlPath());
        gotoPage("/project/3805");
        logger.trace("{}", this);
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
        logger.trace("content of dataset view page: {}", this);
        assertTextPresentInPage("Knowth Stage 8 Fauna Dataset");
        assertTextPresentInPage("Dataset");
        assertTextPresentInPage("dataset_3088_knowthstage8.xls");
    }


    public void testAdvancedSearchView() {
        gotoPage("/search/advanced");
        assertTextPresentInPage("Limit by geographic region");
        assertTextPresentInPage("Choose Search Terms");
        assertTextPresentInPage("All Fields");
    }

    @After
    public void cleanup() {
        webClient.close();;
        webClient.getCookieManager().clearCookies();
        webClient.getCache().clear();
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


    //fixme: replace this madness with css selector-based methods
    /**
     * Get the "main" form. It's pretty much a guess, so if you encounter a page w/ multiple forms you might wanna
     * specify it outright.
     */
    public HtmlForm getForm() {
        logger.trace("FORM{} OTHERS: {}", _internalForm, getHtmlPage().getForms());
        if (_internalForm == null) {
            HtmlForm htmlForm = null;
            if (getHtmlPage().getForms().size() == 1) {
                htmlForm = getHtmlPage().getForms().get(0);
                logger.trace("only one form: {}", htmlForm.getNameAttribute());
            } else {
                for (HtmlForm form : getHtmlPage().getForms()) {
                    if (StringUtils.isNotBlank(form.getActionAttribute()) && 
                            !StringUtils.containsAny(form.getNameAttribute().toLowerCase(), "autosave","logoutform","searchheader","logoutformmenu")) {
                        htmlForm = form;
                        logger.trace("using form: {}", htmlForm.getNameAttribute());
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
        if ((_internalForm != null) || StringUtils.isBlank(id)) {
            return;
        }
        for (HtmlForm form : getHtmlPage().getForms()) {
            if (form.getFirstByXPath("descendant-or-self::*[contains(@id,'" + id + "')]") != null) {
                logger.info("updating main for for id: {} to form: {}", id, form);
                setMainForm(form);
                return;
            }
        }
        logger.warn("No form found containing id '{}'", id);
    }

    public String getPersonalFilestoreTicketId() {
        gotoPageWithoutErrorCheck("/upload/grab-ticket");
        assertTrue("internalPage is not TextPage. It is: " + internalPage.getClass().getName(), internalPage.getWebResponse().getContentType().contains("json"));
        String json = getPageCode();
        logger.debug("ticket json:: {}", json.trim());
        JSONObject jsonObject = JSONObject.fromObject(json);
        String ticketId = jsonObject.getString("id");
        logger.debug("ticket id::{}", ticketId);
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

    public void addFileProxyFields(int rowNum, FileAccessRestriction restriction, String filename, Long fileId, FileAction action) {
        createInput("hidden", "fileProxies[" + rowNum + "].restriction", restriction.name());
        createInput("hidden", "fileProxies[" + rowNum + "].action", action.name());
        createInput("hidden", "fileProxies[" + rowNum + "].fileId", Long.toString(fileId));
        createInput("hidden", "fileProxies[" + rowNum + "].filename", FilenameUtils.getName(filename));
        createInput("hidden", "fileProxies[" + rowNum + "].sequenceNumber", Integer.toString(rowNum));

    }

    public void addFileProxyFields(int rowNum, FileAccessRestriction restriction, String filename) {
        addFileProxyFields(rowNum, restriction, filename, -1L, FileAction.ADD);
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
            logger.debug("errors: {} ; code: {} ; content: {}", assertNoErrors, code, page.getWebResponse().getContentAsString());
            Assert.assertTrue(assertNoErrors && (code == HttpStatus.OK.value()));
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

    public void assertValidJson(String json) {
        try {
            JSONSerializer.toJSON(json);
        } catch (JSONException jex) {
            fail("Invalid json string: >>" + json + "<<");
        }
    }

    /**
     * assert provided string is valid json and return a JSONObject, otherwise call fail()
     * 
     * @param json
     * @return
     */
    protected JSONObject toJson(String json) {
        assertValidJson(json);
        JSONObject jso = (JSONObject) JSONSerializer.toJSON(json);
        return jso;
    }

    protected void assertFileSizes(Page page, List<File> files) {
        JSONObject json = toJson(page.getWebResponse().getContentAsString());
        JSONArray jsonArray = json.getJSONArray("files");
        logger.info("{}", jsonArray);
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
        //(name, file, contentType, "utf8");
        KeyDataPair keyDataPair = new KeyDataPair(name, file, file.getName(), contentType, "utf8");
        return keyDataPair;
    }

    public void createDocumentAndUploadFile(String title) {
        createDocumentAndUploadFile(title, null);
    }

    public void createDocumentAndUploadFile(String title, Long accountId) {
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
        if (accountId != null) {
            setInputIfExists(ACCOUNT_ID, accountId.toString());
        }
        setInput("document.date", "1934");
        setInput("ticketId", ticketId);
        setInput("projectId", Long.toString(TestConstants.ADMIN_PROJECT_ID));
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // setInput(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        addFileProxyFields(0, FileAccessRestriction.PUBLIC, TestConstants.TEST_DOCUMENT_NAME);
        // logger.info(getPageCode());
        submitForm();
        // make sure we're on the view page
        assertPageTitleEquals(title);
        assertTextPresentInPage(title + " (ABSTRACT)");
        assertTextPresentInPage(TestConstants.TEST_DOCUMENT_NAME);
    }

    public void setInputIfExists(String name, String value) {
        try {
            getInput(name);
            setInput(name, value);
        } catch (Exception e) {
        }
    }

    protected Map<String, String> testAccountPollingResponse(String total, TransactionStatus expectedResponse) throws MalformedURLException {
        // assertCurrentUrlContains("/simple");
        Map<String, String> toReturn = new HashMap<>();
        toReturn.put(ACCOUNT_ID, getValue(ACCOUNT_ID));
        toReturn.put(INVOICE_ID, getValue(INVOICE_ID));

        if (!getCurrentUrlPath().contains("process-payment-request")) {
            gotoPage(CART_REVIEW);
            submitForm("Next Step: Payment");
        }
        logger.debug("{}", toReturn);
        logger.info("TOTAL::: " + total);
        if (!total.equals("0")) {
            toReturn.put(ACCOUNT_ID, getValue(ACCOUNT_ID));
            toReturn.put(INVOICE_ID, getValue(INVOICE_ID));
            assertCurrentUrlContains("process-payment-request");
            clickLinkWithText("Click Here To Begin Payment Process");
            URL polingUrl = new URL(getBaseUrl() + "/api/cart/" + toReturn.get(INVOICE_ID) + "/polling-check");
            String response = getAccountPollingRequest(polingUrl);
            assertTrue(response.contains(TransactionStatus.PENDING_TRANSACTION.name()));
            checkInput(NelnetTransactionItem.getInvoiceIdKey(), toReturn.get(INVOICE_ID));
            checkInput(NelnetTransactionItem.getUserIdKey(), Long.toString(CONFIG.getUserId()));
            // logger.info(getPageBodyCode());
            checkInput(NelnetTransactionItem.AMOUNT_DUE.name(), total);
            clickElementWithId("process-payment_0");
            response = getAccountPollingRequest(polingUrl);
            assertTrue(response.contains(expectedResponse.name()));
            gotoPage("/dashboard");
        }
        return toReturn;
    }

    private String getValue(String key) {
        try {
            return getInput(key).getAttribute("value");
        } catch (Exception e) {
        }
        return null;
    }

    protected String getAccountPollingRequest(URL polingUrl) {
        try {
            WebWindow openWindow = webClient.openWindow(null, "polling" + System.currentTimeMillis());
            Page page = openWindow.getWebClient().getPage(new WebRequest(polingUrl, HttpMethod.POST));
            logger.debug(page.toString() + "--\n" + page.getWebResponse().getContentAsString());
            return page.getWebResponse().getContentAsString();
        } catch (Exception e) {
            logger.error("error in polling", e);
            return null;
        }
    }

    /*
     * add new account, add another, make sure account names are all ok
     */
    protected String addInvoiceToNewAccount(String invoiceId, String accountId, String _accountName) {
        String accountName = _accountName;
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
        List<String> users = Arrays.asList("editor user", "K. Selcuk Candan", "Keith Kintigh");
        List<Long> userIds = Arrays.asList(8067L, 8094L, 8389L);
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

    protected void assertAccountPageCorrect(List<String> users, List<Long> userIds, String title) {
        assertTextPresent(title);
        assertTextPresent(THIS_IS_A_TEST_DESCIPTION);
        for (int i = 0; i < userIds.size(); i++) {
            assertTextPresent(users.get(i));
        }
        assertTextPresent("test user");
    }

    public void login(String user, String pass) {
        login(user, pass, false);
    }

    public int login(String user, String pass, boolean expectingErrors) {
        gotoPage("/");
        clickLinkOnPage("Log In");
        completeLoginForm(user, pass, expectingErrors);
        return internalPage.getWebResponse().getStatusCode();
    }

    public void completeLoginForm(String user_, String pass_, boolean expectingErrors) {
        setMainForm("loginForm");
        String user = CONFIG.getUsername(user_);
        String pass = CONFIG.getPassword(pass_);
        // logger.info(user + ":" + pass);
        setInput("userLogin.loginUsername", user);
        setInput("userLogin.loginPassword", pass);
        if (expectingErrors) {
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            submitFormWithoutErrorCheck("_tdar.Login");
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        } else {
            clickElementWithId("btnLogin");
        }
    }

    public void logout() {
        webClient.getOptions().setJavaScriptEnabled(false);
        if (internalPage instanceof HtmlPage && 
                getHtmlPage().getElementById("logout-button") != null) {
            clickElementWithId("logout-button");
        } else {
            // go to homepage
            gotoPage("/login");
            // if logout-button is not present, then we're logged-out
            if (getHtmlPage().getElementById("logout-button") != null) {
                clickElementWithId("logout-button");
            }
        }
        webClient.getCookieManager().clearCookies();
    }


    public enum TERMS {
        TOS,
        CONTRIB,
        BOTH;
    }

    public void testRegister(Map<String, String> values, TERMS terms, boolean valid) {

        @SuppressWarnings("unused")
        String username = values.get("registration.person.username");
        gotoPage("/");
        logger.trace(getPageText());

        clickLinkOnPage("Sign Up");
        logger.trace(getPageCode());
        for (String key : values.keySet()) {
            setInput(key, values.get(key));
        }

        if (terms != null) {
            switch (terms) {
                case BOTH:
                    setInput("registration.acceptTermsOfUse", "true");
                    setInput("registration.requestingContributorAccess", "true");
                    break;
                case CONTRIB:
                    setInput("registration.requestingContributorAccess", "true");
                    break;
                case TOS:
                    setInput("registration.acceptTermsOfUse", "true");
                    break;
                default:
                    break;
            }
        }

        if (valid) {
            setInput("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
            submitForm("Register");
        } else {
            submitFormWithoutErrorCheck("Register");
        }
    }


    public void setupBasicUser(Map<String, String> personmap, String prefix) {
        setupBasicUser(personmap, prefix, "registration");
    }

    public void setupBasicUser(Map<String, String> personmap, String prefix, String mapPrefix) {
        personmap.put(mapPrefix + ".person.firstName", prefix + "firstName");
        personmap.put(mapPrefix + ".person.lastName", prefix + "lastName");
        personmap.put(mapPrefix + ".person.email", prefix + "aaaaa@bbbbb.com");
        personmap.put(mapPrefix + ".confirmEmail", prefix + "aaaaa@bbbbb.com");
        personmap.put(mapPrefix + ".person.username", prefix + "aaaaa@bbbbb.com");
        personmap.put(mapPrefix + ".password", "secret");
        personmap.put(mapPrefix + ".confirmPassword", "secret");
        personmap.put(mapPrefix + ".institutionName", "institution");
        personmap.put(mapPrefix + ".person.phone", "1234567890");
        personmap.put(mapPrefix + ".contributorReason", "there is a reason");
        // personmap.put("contributor", "true");
        personmap.put(mapPrefix + ".affiliation", UserAffiliation.GRADUATE_STUDENT.name());
        // personmap.put("person.rpaNumber", "1234567890");
        // personmap.put("registration.acceptTermsOfUse","true");
        // personmap.put("registration.requestingContributorAccess", "true");
    }

    public void onFail(Throwable e, Description description) {
        // FIXME: need to get this to fire *before* the @After method logs out. otherwise the pageCode will always be the tdar login screen.
        // logger.error("{} failed. server response below:\n\n {}", description.getDisplayName(), getPageCode());
    }

    public void reindexUnauthenticated() {
        String url = getCurrentUrlPath();
        logout();
        login(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
        reindex();
        logout();
        gotoPage(url);
    }

    protected void reindex() {
        gotoPage("/admin/searchindex/build");
        gotoPage("/admin/searchindex/buildIndex");
        try {
            URL url = new URL(getBaseSecureUrl() + "/admin/searchindex/checkstatus?userId=" + TestConfiguration.getInstance().getAdminUserId());
            internalPage = webClient.getPage(new WebRequest(url, HttpMethod.POST));

            logger.debug(getPageCode());
            int count = 0;
            while (!getPageCode().contains("\"percentDone\" : 100") && !getPageCode().contains("index all complete")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    fail("InterruptedException during reindex.  sorry.");
                }
                internalPage = webClient.getPage(new WebRequest(url, HttpMethod.POST));
                if ((count % 10) == 5) {
                    logger.info(getPageCode());
                }
                if (count == 100) {
                    fail("we went through 200 iterations of waiting for the search index to build... assuming something is wrong");
                }
                count++;
            }
            Thread.sleep(1000);
            logger.debug(getPageCode());
        } catch (Exception e) {
            logger.error("exception in reindexing",e);
            fail("exception in reindexing");
        }
    }

    public void createUserWithPermissions(int i, Person user, GeneralPermissions viewAll) {
        logger.info("setiting user [{}] to {} {}", i, user, viewAll);
        createInput("hidden", String.format(FMT_AUTHUSERS_ID, i), user.getId());
        createInput("text", String.format(FMT_AUTHUSERS_LASTNAME, i), user.getLastName());
        createInput("text", String.format(FMT_AUTHUSERS_FIRSTNAME, i), user.getFirstName());
        createInput("text", String.format(FMT_AUTHUSERS_EMAIL, i), user.getEmail());
        String inst = user.getInstitutionName();
        if (inst == null) {
            inst = "";
        }
        createInput("text", String.format(FMT_AUTHUSERS_INSTITUTION, i), inst);
        createInput("text", String.format(FMT_AUTHUSERS_PERMISSION, i), viewAll.toString());
    }

    @Override
    public String toString() {
        return getPageText();
    }

    public void selectAnyAccount() {
        gotoPage(CART_REVIEW);

        try {
            HtmlElement input = getInput("id");
            if (input instanceof HtmlSelect) {
                HtmlOption opt = null;
                for (HtmlOption option : ((HtmlSelect) input).getOptions()) {
                    String valueAttribute = option.getValueAttribute();
                    if (StringUtils.isNotBlank(valueAttribute) && Long.parseLong(valueAttribute.trim()) > -1) {
                        logger.debug("accountId: " + valueAttribute);
                        opt = option;
                        break;
                    }
                }
                if (opt != null) {
                    setInput("id", opt.getValueAttribute());
                }
            }
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void assertNotEquals(Object o1, Object o2) {
        assertFalse(Objects.equals(o1, o2));
    }


}
