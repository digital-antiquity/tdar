package org.tdar.web;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.TestConfiguration;

import com.gargoylesoftware.htmlunit.html.DomNode;

public class MapLatLongWebITCase extends AbstractAdminAuthenticatedWebTestCase{

    private static final String LAT_LONG_SECURITY_TEST = "latLongSecurityTest";
    private static final String RESOURCE_WITH_NORMAL = "resource with normal";
    private static final String RESOURCE_WITH_DRAFT = "resource with draft";
    private static final String RESOURCE_WITH_OBFUSCATED_LAT_LONG = "resource with obfuscated latLong";
    private static final String RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE = "resource with normal latLong and confidential file";
    private static final String TEST_SECURITY_COLLECTION = "test security collection";
    public static String REGEX_DOCUMENT_VIEW = "/document/(\\d+)$";

    @Test
    public void testAddingInformationResourceToProject() {
        TestConfiguration config = TestConfiguration.getInstance();

        gotoPage("/collection/add");
        setInput("resourceCollection.name", TEST_SECURITY_COLLECTION);
        setInput("resourceCollection.description", "test for map secuity");
        setInput("resourceCollection.orientation", DisplayOrientation.MAP.name());
        setInput("resourceCollection.visible", "true");
        createUserWithPermissions(0, getBasicUser(), GeneralPermissions.MODIFY_RECORD);
        submitForm();
        String url = getCurrentUrlPath();
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();    
        latLong.setMaximumLatitude(45.336701909968106);
        latLong.setMinimumLatitude(32.175612478499325);
        latLong.setMaximumLongitude(-83.0126953125);
        latLong.setMinimumLongitude(-114.7412109375);
        LatitudeLongitudeBox detailedLatLong = new LatitudeLongitudeBox();
        detailedLatLong.setMaximumLatitude(40.21362051996706);
        detailedLatLong.setMinimumLatitude(40.2115886265213);
        detailedLatLong.setMaximumLongitude(-106.38383388519287);
        detailedLatLong.setMinimumLongitude(-106.38091564178467);
        File file = new File(TestConstants.TEST_DOCUMENT_DIR, TestConstants.TEST_DOCUMENT_NAME);
        Long confidentialFile =setupDocumentWithProject(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE,latLong, Status.ACTIVE, file, FileAccessRestriction.CONFIDENTIAL);
        Long obfuscatedMap = setupDocumentWithProject(RESOURCE_WITH_OBFUSCATED_LAT_LONG,detailedLatLong, Status.ACTIVE, null, null);
        Long draft = setupDocumentWithProject(RESOURCE_WITH_DRAFT,latLong, Status.DRAFT,null, null);
        Long normal = setupDocumentWithProject(RESOURCE_WITH_NORMAL,latLong, Status.ACTIVE,null, null);

        gotoPage(url);
        
        assertTextPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
        assertNodeDoesNotLatLongAttributes(confidentialFile);

        // should do the same thing with urls:
        gotoPage("/search/results?query="+LAT_LONG_SECURITY_TEST+"&orientation=MAP");
        
        logout();
        gotoPage(url);
        // anonymous user
        assertTextNotPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
        assertNodeDoesNotLatLongAttributes(confidentialFile);

        login(config.getUsername(), config.getPassword());
        gotoPage(url);
        
        assertTextNotPresent(RESOURCE_WITH_DRAFT);
        assertNodeHasLatLongAttributes(draft);
        assertTextPresent(RESOURCE_WITH_NORMAL);
        assertNodeHasLatLongAttributes(normal);
        assertTextPresent(RESOURCE_WITH_OBFUSCATED_LAT_LONG);
        assertNodeDoesNotLatLongAttributes(obfuscatedMap);
        assertTextPresent(RESOURCE_WITH_NORMAL_LAT_LONG_AND_CONFIDENTIAL_FILE);
        assertNodeDoesNotLatLongAttributes(confidentialFile);

        // make assertions
    }


    private void assertNodeHasLatLongAttributes(Long confidentialFile) {
        DomNode node = getPageContentsWithId(confidentialFile);
        Assert.assertNotNull("should have lat-long", node.getAttributes().getNamedItem("data-lat"));
        Assert.assertNotNull("should have lat-long", node.getAttributes().getNamedItem("data-long"));
    }

    private void assertNodeDoesNotLatLongAttributes(Long confidentialFile) {
        DomNode node = getPageContentsWithId(confidentialFile);
        Assert.assertNull("should not have lat-long", node.getAttributes().getNamedItem("data-lat"));
        Assert.assertNull("should not have lat-long", node.getAttributes().getNamedItem("data-long"));
    }


    private DomNode getPageContentsWithId(Long confidentialFile) {
        for (DomNode element_ : htmlPage.getDocumentElement().querySelectorAll("#resource-"+confidentialFile)) {
            return element_;
        }
        return null;
    }


    private Long setupDocumentWithProject(String resourceName, LatitudeLongitudeBox latLong, Status status, File file, FileAccessRestriction access) {
        String ticketId = getPersonalFilestoreTicketId();
        if (file != null) {
            uploadFileToPersonalFilestore(ticketId, file.getAbsolutePath());
        }
        
        gotoPage("/document/add");
        setInput("document.title", resourceName);
        setInput("document.description",  "hi mom");
        setInput("document.date", "1999");
        setInput("projectId", TestConstants.PARENT_PROJECT_ID.toString());
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            setInput(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        setInput("uncontrolledSiteTypeKeywords[0]", LAT_LONG_SECURITY_TEST);
        if (latLong != null) {
            setInput("latitudeLongitudeBoxes[0].maximumLatitude", latLong.getMaximumLatitude());
            setInput("latitudeLongitudeBoxes[0].maximumLongitude", latLong.getMaximumLongitude());
            setInput("latitudeLongitudeBoxes[0].minimumLatitude", latLong.getMinimumLatitude());
            setInput("latitudeLongitudeBoxes[0].minimumLongitude",latLong.getMinimumLongitude());
        }
        if (status != null) {
            setInput("status",status.name());
        }

        setInput("resourceCollections[0].name", TEST_SECURITY_COLLECTION);
        if (file != null) {
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.CONFIDENTIAL, file.getName());
        }
        submitForm();
        return extractTdarIdFromCurrentURL();
    }

}
