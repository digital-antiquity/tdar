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

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.pid.AndsPidsDao;
import org.tdar.core.service.UrlService;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;

/**
 * @author Adam Brin
 * 
 */
@Ignore("The ANDS pid test service seems to have stopped working a while back. we now have a ticket: https://jira.ands.org.au/browse/SD-4419")
public class AndsPidsITCase extends AbstractSearchControllerITCase {

    @Autowired
    AndsPidsDao pidsDao;

    @Autowired
    UrlService urlService;

    @Test
    public void testLogin() {
        assertTrue(pidsDao.connect());
    }

    @Test
    public void testLogout() {
        assertTrue(pidsDao.connect());
        assertTrue(pidsDao.logout());
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
            // assertEquals(r.getTitle(), metadata.get(AndsPidsDao.DATACITE_TITLE));

            r.setStatus(Status.DELETED);
            pidsDao.delete(r, absoluteUrl, doi);

            metadata = pidsDao.getMetadata(doi);
            // should no longer exist
            // assertFalse(metadata.containsKey(AndsPidsDao.DATACITE_TITLE));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
