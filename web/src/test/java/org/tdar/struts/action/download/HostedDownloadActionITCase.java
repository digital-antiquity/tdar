package org.tdar.struts.action.download;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.REFERER;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.PdfService;
import org.tdar.core.service.download.DownloadService;
import org.tdar.junit.IgnoreActionErrors;
import org.tdar.struts.action.AbstractDataIntegrationTestCase;

import com.opensymphony.xwork2.Action;

public class HostedDownloadActionITCase extends AbstractDataIntegrationTestCase {

    private Document doc;

    @Autowired
    DownloadService downloadService;
    int COVER_PAGE_WIGGLE_ROOM = 155_000;

    @Autowired
    PdfService pdfService;

    @Test
    @Rollback
    public void testValidHostedDownload() throws Exception {

        HostedDownloadAction controller = generateNewController(HostedDownloadAction.class);
        init(controller, null);
        controller.setApiKey("test");
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader("referer", "http://test.tdar.org/blog/this-is-my-test-url");
        controller.setServletRequest(request);
        controller.setInformationResourceFileId(doc.getFirstInformationResourceFile().getId());

        controller.prepare();
        controller.validate();
        assertEquals(Action.SUCCESS, controller.execute());
        assertEquals(TestConstants.TEST_DOCUMENT_NAME, controller.getDownloadTransferObject().getFileName());
        IOUtils.copyLarge(controller.getDownloadTransferObject().getInputStream(), new FileOutputStream(new File("target/out.pdf")));
    }

    @Test
    @Rollback
    @IgnoreActionErrors
    public void testInvalidHostedDownloadReferrer() throws Exception {
        // test bad referrer
        HostedDownloadAction controller = generateNewController(HostedDownloadAction.class);
        init(controller, null);
        controller.setApiKey("test");
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).addHeader(REFERER, "http://tdar.org/blog/this-is-my-test-url");
        controller.setServletRequest(request);
        controller.setInformationResourceFileId(doc.getFirstInformationResourceFile().getId());

        controller.prepare();
        controller.validate();
        assertThat(controller.getActionErrors(), is( not( empty())));
    }

    @Test
    @Rollback
    @IgnoreActionErrors
    public void testMissingHostedDownloadReferrer() throws Exception {
        // test no referrer
        HostedDownloadAction controller = generateNewController(HostedDownloadAction.class);
        init(controller, null);
        controller.setApiKey("test");
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.setServletRequest(request);
        controller.setInformationResourceFileId(doc.getFirstInformationResourceFile().getId());

        controller.prepare();
        controller.validate();
        assertThat(controller.getActionErrors(), is( not( empty())));
    }

    @Test
    @Rollback
    @IgnoreActionErrors
    public void testInvalidApiKeyHostedDownloadReferrer() throws Exception {
        HostedDownloadAction controller = generateNewController(HostedDownloadAction.class);
        init(controller, null);
        controller.setApiKey("testasasfasf");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(REFERER, "http://bobs-file-hut.ru/tdar");

        controller.setServletRequest(request);
        controller.setInformationResourceFileId(doc.getFirstInformationResourceFile().getId());

        controller.prepare();
        controller.validate();
        assertThat(controller.getActionErrors(), is( not( empty())));
    }

    @Test
    @Rollback
    @IgnoreActionErrors
    public void testMissingApiKeyHostedDownloadReferrer() {
        HostedDownloadAction controller = generateNewController(HostedDownloadAction.class);
        init(controller, null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(REFERER, "http://invalid-apikey-exchange.biz/archaeology");

        controller.setServletRequest(request);
        controller.setInformationResourceFileId(doc.getFirstInformationResourceFile().getId());

        controller.prepare();
        controller.validate();
        assertThat(controller.getActionErrors(), is( not( empty())));
    }

    @Before
    public void setup() throws InstantiationException, IllegalAccessException {
        doc = generateDocumentWithFileAndUseDefaultUser();
        ResourceCollection collection = new ResourceCollection(CollectionType.SHARED);
        collection.setName("authorized collection");
        collection.setDescription(collection.getName());
        collection.markUpdated(getAdminUser());
        collection.getResources().add(doc);
        genericService.saveOrUpdate(collection);
        doc.getResourceCollections().add(collection);
        genericService.saveOrUpdate(doc);
        DownloadAuthorization downloadAuthorization = new DownloadAuthorization();
        downloadAuthorization.setApiKey("test");
        downloadAuthorization.setResourceCollection(collection);
        downloadAuthorization.getRefererHostnames().add("test.tdar.org");
        downloadAuthorization.getRefererHostnames().add("whatever.tdar.org");
        genericService.saveOrUpdate(downloadAuthorization);
    }

}
