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

import java.io.IOException;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.pid.EZIDDao;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.processes.daily.DoiProcess;

/**
 * @author Adam Brin
 * 
 */
public class EZIDITCase extends AbstractIntegrationTestCase {
    // public static final String EZID_URL = "https://n2t.net/ezid";
    // public static final String SHOULDER = "doi:10.5072/FK2";

    public static final String TEST_USER = "apitest";
    public static final String TEST_PASSWORD = "apitest";

    @Autowired
    EZIDDao ezidDao;

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
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            assertEquals(2, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            String ark = createdIDs.get("ARK").trim();
            assertTrue(StringUtils.isNotBlank(doi));
            assertTrue(StringUtils.isNotBlank(ark));

            Map<String, String> metadata = ezidDao.getMetadata(doi);
            assertEquals(ark, metadata.get(EZIDDao._SHADOWED_BY));
            assertEquals(r.getTitle(), metadata.get(EZIDDao.DATACITE_TITLE));

            r.setTitle("test");
            ezidDao.modify(r, absoluteUrl, doi);

            metadata = ezidDao.getMetadata(doi);
            assertEquals(ark, metadata.get("_shadowedby"));
            assertEquals(r.getTitle(), metadata.get(EZIDDao.DATACITE_TITLE));

            r.setStatus(Status.DELETED);
            ezidDao.delete(r, absoluteUrl, doi);

            metadata = ezidDao.getMetadata(doi);
            assertEquals(ark, metadata.get("_shadowedby"));
            // should now be blank
            assertTrue(r.getTitle().equals(metadata.get(EZIDDao.DATACITE_TITLE)));
            assertTrue(EZIDDao._STATUS_UNAVAILABLE.equals(metadata.get(EZIDDao._STATUS)));

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
            r.setStatus(Status.ACTIVE);
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            assertEquals(2, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            String ark = createdIDs.get("ARK").trim();
            assertTrue(StringUtils.isNotBlank(doi));
            assertTrue(StringUtils.isNotBlank(ark));

            Map<String, String> metadata = ezidDao.getMetadata(doi);
            assertEquals(ark, metadata.get(EZIDDao._SHADOWED_BY));
            assertEquals(r.getTitle(), metadata.get(EZIDDao.DATACITE_TITLE));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
            genericService.saveOrUpdate(r);
            ezidDao.connect();
            String absoluteUrl = UrlService.absoluteUrl(r);
            Map<String, String> createdIDs = ezidDao.create(r, absoluteUrl);
            assertEquals(2, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            String ark = createdIDs.get("ARK").trim();
            assertTrue(StringUtils.isNotBlank(doi));
            assertTrue(StringUtils.isNotBlank(ark));

            Map<String, String> metadata = ezidDao.getMetadata(doi);
            assertEquals(ark, metadata.get(EZIDDao._SHADOWED_BY));
            assertEquals(r.getTitle(), metadata.get(EZIDDao.DATACITE_TITLE));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
