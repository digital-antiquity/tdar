package org.tdar.web.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Test;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ResourceAccessWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    //todo: Make this friendlier (e.g.  SeleniumWebITCase#find + WebElementSelection) and then pull it up into the abstract class

    /**
     * Return a list of DomElements matched by the supplied css selector. This method throws ClassCastException
     * if any nodes matched by the selector are not instances of DomElement (However, I'm assuming that a querySelector
     * will only ever match elements.)
     *
     * @param cssSelector
     * @return
     */
    public List<DomElement> find(String cssSelector) {
        if(getHtmlPage() == null) return Collections.emptyList();
        if(getHtmlPage().getDocumentElement() == null) return Collections.emptyList();
        List<DomElement> selection = getHtmlPage().getDocumentElement().querySelectorAll(cssSelector)
                .stream().map(domNode -> (DomElement)domNode).collect(Collectors.toList());
        return selection;
    }

    @Test
    public void testShareAccessSuccess() throws IOException {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", GeneralPermissions.MODIFY_METADATA.name());
        changePage(find("#metadataForm_submit").get(0).click());
        logger.info(getCurrentUrlPath());
        //logger.info(getPageText());
        assertThat(getPageText(), containsString(" has been granted "));
    }

    @Test(expected = FailingHttpStatusCodeException.class)
    public void testShareAccessFailureEmptyPermission() throws IOException {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", "");
        changePage(find("#metadataForm_submit").get(0).click(), true);
        assertThat(getPageText(), containsString("Please specify the level of rights"));
    }

    private void assertStatusCodeNotSuccess() {
        logger.debug("{} {}", internalPage.getWebResponse().getStatusCode(), getCurrentUrlPath());
        assertNotEquals(200, internalPage.getWebResponse().getStatusCode());
    }

    @Test
    public void testShareAccessFailureEmptyUser() {
        gotoPageWithoutErrorCheck("/resource/request-access?resourceId=3088");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFailureEmptyResource() {
        gotoPageWithoutErrorCheck("/resource/request-access");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFalure() {
        logout();
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPageWithoutErrorCheck("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        logger.debug(getPageText());
    }
}
