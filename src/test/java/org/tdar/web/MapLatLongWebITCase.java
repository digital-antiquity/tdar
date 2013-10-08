package org.tdar.web;

import java.io.File;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;

public class MapLatLongWebITCase extends AbstractAdminAuthenticatedWebTestCase{

    private static final String TEST_SECURITY_COLLECTION = "test security collection";
    private static final String PROJECT_NAME = "changing project test";
    public static String REGEX_DOCUMENT_VIEW = "/document/(\\d+)$";

    @Test
    public void testAddingInformationResourceToProject() {
        String resourceName = "newresource";
        gotoPage("/collection/add");
        setInput("resourceCollection.name", TEST_SECURITY_COLLECTION);
        setInput("resourceCollection.description", "test for map secuity");
        setInput("resourceCollection.orientation", DisplayOrientation.MAP.name());
        setInput("resourceCollection.visible", "true");
        submitForm();
        String url = getCurrentUrlPath();
        LatitudeLongitudeBox latLong = new LatitudeLongitudeBox();        
        LatitudeLongitudeBox detailedLatLong = new LatitudeLongitudeBox();
        File file = new File(TestConstants.TEST_DOCUMENT_DIR, TestConstants.TEST_DOCUMENT_NAME);
        Long confidentialFile =setupDocumentWithProject("resource with normal latLong and confidential file",latLong, Status.ACTIVE, file, FileAccessRestriction.CONFIDENTIAL);
        Long obfuscatedMap = setupDocumentWithProject("resource with obfuscated latLong",detailedLatLong, Status.ACTIVE, null, null);
        Long draft = setupDocumentWithProject("resource with draft",latLong, Status.DRAFT,null, null);
        Long normal = setupDocumentWithProject("resource with normal ",latLong, Status.ACTIVE,null, null);

        gotoPage(url);
        // make assertions
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
