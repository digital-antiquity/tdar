package org.tdar.web;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.collection.CollectionWebITCase;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.DomNode;

public class MapLatLongWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String RESOURCE_WITH_NORMAL = "resource with normal";
    private static final String RESOURCE_WITH_DRAFT = "resource with draft";
    private static final String RESOURCE_WITH_OBFUSCATED_LAT_LONG = "resource with obfuscated latLong";
    private static final String RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE = "resource with normal latLong and confidential file";
    private static final String TEST_SECURITY_SHARE = "test security share";
    public static String REGEX_DOCUMENT_VIEW = "/document/(\\d+)$";

    // FIXME:Break into two tests
    @Test
    public void testAddingInformationResourceToProject() throws FileNotFoundException {
        TestConfiguration config = TestConfiguration.getInstance();
        String collectionUrl = null;
        if (TdarConfiguration.getInstance().isListCollectionsEnabled()) {
            gotoPage("/listcollection/add");
            setInput("resourceCollection.name", TEST_SECURITY_COLLECTION);
            setInput("resourceCollection.description", "test for map secuity");
            setInput("resourceCollection.orientation", DisplayOrientation.MAP.name());
            setInput("resourceCollection.hidden", "false");
            submitForm();
            clickLinkWithText("Rights");
            setInput("authorizedUsers[0].user.id", CONFIG.getUserId());
            setInput("authorizedUsers[0].generalPermission", Permissions.MODIFY_RECORD.name());
            submitForm();
            collectionUrl = getCurrentUrlPath();
        }
        gotoPage("/collection/add");
        setInput("resourceCollection.name", TEST_SECURITY_SHARE);
        setInput("resourceCollection.description", "test for map secuity");
        setInput("resourceCollection.orientation", DisplayOrientation.MAP.name());
        setInput("resourceCollection.hidden", "false");
        submitForm();
        if (!TdarConfiguration.getInstance().isListCollectionsEnabled()) {
            collectionUrl = getCurrentUrlPath();
        }
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        setInput("proxies[0].id", CONFIG.getUserId());
        setInput("proxies[0].permission", Permissions.MODIFY_RECORD.name());
        submitForm();
        String shareUrl = getCurrentUrlPath() + "?type=1&orientation=" + DisplayOrientation.MAP.name();
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();
        latLong.setNorth(45.336701909968106);
        latLong.setSouth(32.175612478499325);
        latLong.setEast(-83.0126953125);
        latLong.setWest(-114.7412109375);
        LatitudeLongitudeBox detailedLatLong = new LatitudeLongitudeBox();
        detailedLatLong.setNorth(40.21362051996706);
        detailedLatLong.setSouth(40.2115886265213);
        detailedLatLong.setEast(-106.38383388519287);
        detailedLatLong.setWest(-106.38091564178467);
        File file = TestConstants.getFile(TestConstants.TEST_DOCUMENT_DIR, TestConstants.TEST_DOCUMENT_NAME);
        Long confidentialFile = setupDocumentWithProject(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE, latLong, Status.ACTIVE, file,
                FileAccessRestriction.CONFIDENTIAL);
        Long obfuscatedMap = setupDocumentWithProject(RESOURCE_WITH_OBFUSCATED_LAT_LONG, detailedLatLong, Status.ACTIVE, null, null);
        Long draft = setupDocumentWithProject(RESOURCE_WITH_DRAFT, latLong, Status.DRAFT, null, null);
        Long normal = setupDocumentWithProject(RESOURCE_WITH_NORMAL, latLong, Status.ACTIVE, null, null);

        // test that the collection is ok
        gotoPage(collectionUrl);
        logger.info(getPageText());
        assertTextPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);

        
        // test that the share is ok
        gotoPage(shareUrl);
        logger.info(getPageBodyCode());
        assertTextPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
//        assertNodeDoesNotLatLongAttributes(confidentialFile);

        // should do the same thing with urls:
        gotoPage("/search/results?query=" + LAT_LONG_SECURITY_TEST + "&orientation=MAP");

        logout();
        gotoPage(collectionUrl);
        // anonymous user
//        assertTextNotPresent(RESOURCE_WITH_DRAFT);
//        assertNodeDoesNotLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
        assertNodeDoesNotLatLongAttributes(confidentialFile);

        login(config.getUsername(), config.getPassword());
        gotoPage(collectionUrl);

        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
        assertNodeDoesNotLatLongAttributes(confidentialFile);

        // test that the collection is ok
        gotoPage(shareUrl);
        logger.info(getPageBodyCode());
        assertTextPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);

        // make assertions
    }
    
    private void assertNodeHasLatLongAttributes(Long confidentialFile) {
        DomNode node = getPageContentsWithId(confidentialFile);
        Assert.assertNotNull("should have lat-long", node.getAttributes().getNamedItem("data-lat"));
        Assert.assertNotNull("should have lat-long", node.getAttributes().getNamedItem("data-long"));
    }

    private void assertNodeDoesNotLatLongAttributes(Long confidentialFile) {
        DomNode node = null;
        try {
            node = getPageContentsWithId(confidentialFile);
        } catch (ElementNotFoundException ene) {
            node = null;
        }
        if (node != null) {
            Assert.assertNull("should not have lat-long", node.getAttributes().getNamedItem("data-lat"));
            Assert.assertNull("should not have lat-long", node.getAttributes().getNamedItem("data-long"));
        }
    }

    private DomNode getPageContentsWithId(Long confidentialFile) {
        return htmlPage.getElementById("resource-" + confidentialFile);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Long setupDocumentWithProject(String resourceName, LatitudeLongitudeBox latLong, Status status, File file, FileAccessRestriction access) {
        String ticketId = getPersonalFilestoreTicketId();
        if (file != null) {
            uploadFileToPersonalFilestore(ticketId, file.getAbsolutePath());
        }

        gotoPage("/document/add");
        setInput("document.title", resourceName);
        setInput("document.description", "hi mom");
        setInput("document.date", "1999");
        setInput("document.documentType", "OTHER");
        setInput("projectId", TestConstants.PARENT_PROJECT_ID.toString());
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        setInput("uncontrolledSiteTypeKeywords[0]", LAT_LONG_SECURITY_TEST);
        if (latLong != null) {
            setInput("latitudeLongitudeBoxes[0].north", latLong.getNorth());
            setInput("latitudeLongitudeBoxes[0].east", latLong.getEast());
            setInput("latitudeLongitudeBoxes[0].south", latLong.getSouth());
            setInput("latitudeLongitudeBoxes[0].west", latLong.getWest());
        }
        if (status != null) {
            setInput("status", status.name());
        }

        setInput("shares[0].name", TEST_SECURITY_SHARE);
        if (TdarConfiguration.getInstance().isListCollectionsEnabled()) {
            setInput("resourceCollections[0].name", TEST_SECURITY_COLLECTION);
        }
        if (file != null) {
            setInput("ticketId", ticketId);
            FileAccessRestriction access_ = FileAccessRestriction.PUBLIC;
            if (access != null) {
                access_ = access;
            }
            addFileProxyFields(0, access_, file.getName());
        }
        submitForm();
        return extractTdarIdFromCurrentURL();
    }
}
