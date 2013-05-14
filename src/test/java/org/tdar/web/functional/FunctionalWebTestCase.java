package org.tdar.web.functional;

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
import java.util.Collections;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.external.payment.nelnet.NelNetTransactionRequestTemplate.NelnetTransactionItem;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.w3c.dom.Element;

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

public abstract class FunctionalWebTestCase{

    WebDriver driver;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Before 
    public void before() {
        driver = new FirefoxDriver();
    }
   
    @After 
    public void after() {
        logger.debug("after");
        try {
            driver.close();
            driver = null;
        }catch (Exception ex) {
            logger.error("Could not close selenium driver: {}", ex);
        }
    }

    public String url(String path) {
        return String.format("%s%s", DEFAULT_BASE_URL, path);
    }
    
    public void gotoPage(String path) {
        String url = url(path);
        logger.debug("going to {}", url);
        driver.get(url(path));
    }
    
    //TODO: find out if this is necessary for repeatrow buttons.  Supposedly selenium will wait until domready is complete.
    public WebElement waitFor(String selector) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(selector)));
        WebElement result = null;
        if(!elements.isEmpty()) {
            result = elements.get(0);
        }
        return result;
    }
    
    public WebElementSelection find(String selector) {
        WebElementSelection selection = new WebElementSelection(driver.findElements(By.cssSelector(selector)));
        logger.debug("selector:{}\t size:{}", selector, selection.size());
        return selection;
    }
    
    public WebElement findOne(String selector) {
        return find(selector).iterator().next();
    }
    
    public void assertSelector(String selector) {
        if(find(selector).isEmpty()) {
            fail("could not find content on page with selector:" + selector);
        }
    }
    
    public WebDriver getDriver() {
        return driver;
    }
    
    public void login() {
        login(TestConstants.USERNAME, TestConstants.PASSWORD);
    }
    
    public void login(String username, String password) {
        gotoPage("/login");
        find("#loginUsername").sendKeys(username);
        find("#loginPassword").sendKeys(password);
        find("#btnLogin").click();
    }
    
    public void logout() {
        gotoPage("/logout");
    }
    
    //case-insensitive search for body.innerText.
    public boolean textContains(String expected) {
        String text = find("body").getText().toLowerCase();
        String _expected = expected.toLowerCase();
        return text.contains(_expected);
    }
}
