/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.AndsPidsDao;
import org.tdar.core.dao.EZIDDao;
import org.tdar.core.service.UrlService;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;

/**
 * @author Adam Brin
 * 
 */
public class AndsPidsITCase extends AbstractSearchControllerITCase {

    @Autowired
    AndsPidsDao pidsDao;

    @Autowired
    UrlService urlService;

    @Test 
    public void testLogin() {
        try {
            assertTrue(pidsDao.connect());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLogout() {
        try {
            assertTrue(pidsDao.connect());
            assertTrue(pidsDao.logout());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateAndDelete() {
        try {
            Resource r = resourceService.find(DOCUMENT_INHERITING_CULTURE_ID);
            pidsDao.connect();
            String absoluteUrl = urlService.absoluteUrl(r);
            Map<String, String> createdIDs = pidsDao.create(r, absoluteUrl);
            assertEquals(1, createdIDs.size());
            String doi = createdIDs.get("DOI").trim();
            assertTrue(StringUtils.isNotBlank(doi));

            Map<String, String> metadata = pidsDao.getMetadata(doi);
            assertEquals(r.getTitle(), metadata.get(AndsPidsDao.DATACITE_TITLE));

            r.setTitle("test");
            pidsDao.modify(r, absoluteUrl, doi);

            metadata = pidsDao.getMetadata(doi);
            //assertEquals(r.getTitle(), metadata.get(AndsPidsDao.DATACITE_TITLE));

            r.setStatus(Status.DELETED);
            pidsDao.delete(r, absoluteUrl, doi);

            metadata = pidsDao.getMetadata(doi);
            // should no longer exist            
            //assertFalse(metadata.containsKey(AndsPidsDao.DATACITE_TITLE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
