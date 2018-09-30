/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.pid.DataCiteDao;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.processes.daily.DoiProcess;
import org.tdar.transform.DataCiteTransformer;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import edu.asu.lib.datacite.DataCiteDocument;
import edu.asu.lib.jaxb.JaxbDocumentWriter;

/**
 * @author Adam Brin
 * 
 */
public class DoiRegistrationITCase extends AbstractIntegrationTestCase {
    // public static final String EZID_URL = "https://n2t.net/ezid";
    // public static final String SHOULDER = "doi:10.5072/FK2";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(12301); 


    public static final String TEST_USER = "apitest";
    public static final String TEST_PASSWORD = "apitest";

    @Autowired
    DataCiteDao ezidDao;

    @Autowired
    UrlService urlService;

    @Autowired
    DoiProcess doiProcess;

    @Test
    public void testDoiList() {
        boolean seenProject = false;
        boolean seenResource = false;
        for (Long id : doiProcess.findAllIds()) {
            Resource r = genericService.find(Resource.class, id);
            if (r.getResourceType().isProject()) {
                seenProject = true;
            }
            if (r instanceof InformationResource) {
                InformationResource ir = (InformationResource) r;
                if (ir.getInformationResourceFiles().size() > 0) {
                    seenResource = true;
                }
            }
        }
        assertTrue("should see at least one project", seenProject);
        assertTrue("should see at least one resource with file", seenResource);
    }

    @Test
    public void testLogin() {
        try {
            assertTrue(ezidDao.connect());
        } catch (ClientProtocolException e) {
            logger.error("{}", e);
            fail(e.getMessage());
        } catch (SSLPeerUnverifiedException spe) {
            logger.error("{}", spe);
            fail(spe.getMessage());
        } catch (IOException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testLogout() {
        try {
            assertTrue(ezidDao.connect());
            assertTrue(ezidDao.logout());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndDelete() {
        try {
            Resource r = resourceService.find(TestConstants.DOCUMENT_INHERITING_CULTURE_ID);
            setupTrivialResponses(r);
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            r.setExternalId("");
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            assertEquals(1, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            assertTrue(StringUtils.isNotBlank(doi));

            Map<String, String> metadata = ezidDao.getMetadata(doi);
            assertTrue(metadata.get("xml").contains("<title>"+ r.getTitle() + "</title>"));

            r.setTitle("test");
            ezidDao.modify(r, absoluteUrl, doi);
            // resetting responses with new title
            setupTrivialResponses(r);
            metadata = ezidDao.getMetadata(doi);
            assertTrue(metadata.get("xml").contains("<title>test</title>"));

            r.setStatus(Status.DELETED);
            ezidDao.delete(r, absoluteUrl, doi);

            metadata = ezidDao.getMetadata(doi);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateImage() {
        try {
            Resource r = genericService.findRandom(Image.class, 1).get(0);
            setupTrivialResponses(r);
            r.setStatus(Status.ACTIVE);
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            logger.debug("createIds: {}", createdIDs);
            assertEquals(1, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            assertTrue(StringUtils.isNotBlank(doi));

            Map<String, String> metadata = ezidDao.getMetadata(doi);
            logger.debug("{}",metadata);
            assertTrue(metadata.get("xml").contains("<title>"+ r.getTitle() + "</title>"));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAll() {
        List<Resource> findAll = genericService.findAll(Resource.class);
        for (Resource r : findAll) {
            DataCiteDocument transformAny = DataCiteTransformer.transformAny(r);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                JaxbDocumentWriter.write(transformAny, bos, true);
                logger.debug(bos.toString());
            } catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    
    
    @Test
    public void testCreateSensoryData() {

        try {
            SensoryData r = new SensoryData();
            r.setTitle("test sensory object");
            r.setDescription("test sensory object");
            r.setDate(1234);
            r.markUpdated(getAdminUser());
            r.setStatus(Status.ACTIVE);
            setupTrivialResponses(r);
            genericService.saveOrUpdate(r);
            logger.debug("connect");
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            logger.debug("create");
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            assertEquals(1, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            assertTrue(StringUtils.isNotBlank(doi));

            logger.debug("get metadata");
            Map<String, String> metadata = ezidDao.getMetadata(doi);
//            assertEquals(ark, metadata.get(EZIDDao._SHADOWED_BY));
//            assertEquals(r.getTitle(), metadata.get(EZIDDao.DATACITE_TITLE));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTrivialResponses(Resource r) {
        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/metadata/10.5072/.*"))
                .willReturn(WireMock.aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "text/xml")
                    .withBody("<response>Some content</response>" + "<title>" + r.getTitle() + "</title>")));

        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/metadata/10.5072/.*"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBody("<response>Some content</response>"+ "<title>" + r.getTitle() + "</title>")));

        WireMock.stubFor(WireMock.put(WireMock.urlMatching("/doi/10.5072/.*"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/xml")
                    .withBody("<response>Some content</response>"+ "<title>" + r.getTitle() + "</title>")));

        WireMock.stubFor(WireMock.delete(WireMock.urlMatching("/metadata/10.5072/.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("<response>Some content</response>"+ "<title>" + r.getTitle() + "</title>")));
    }
}
