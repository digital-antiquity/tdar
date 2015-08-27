/**
 * 
 */
package org.tdar.web;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractAdminAuthenticatedWebTestCase extends AbstractAuthenticatedWebTestCase {

    public static final String LAT_LONG_SECURITY_TEST = "latLongSecurityTest";
    public static final String TEST_SECURITY_COLLECTION = "test security collection";

    @Before
    @Override
    public void setUp() {
        loginAdmin();
    }

    public void createTestCollection(String name, String desc, List<? extends Resource> someResources) {
        assertNotNull(genericService);
        gotoPage("/collection/add");
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);

        for (int i = 0; i < someResources.size(); i++) {
            Resource resource = someResources.get(i);
            // FIXME: we don't set id's in the form this way but setInput() doesn't understand 'resources.id' syntax. fix it so that it can.
            String fieldName = "toAdd[" + i + "]";
            String fieldValue = "" + resource.getId();
            logger.debug("setting  fieldName:{}\t value:{}", fieldName, fieldValue);
            createInput("hidden", fieldName, fieldValue);
        }
        submitForm();
    }

    protected List<? extends Resource> getSomeResources() {
        List<? extends Resource> alldocs = genericService.findAll(Document.class);
        List<? extends Resource> somedocs = alldocs.subList(0, Math.min(10, alldocs.size())); // get no more than 10 docs, pls
        return somedocs;
    }

    protected List<TdarUser> getSomeUsers() {
        // let's only get authorized users
        List<TdarUser> allRegisteredUsers = entityService.findAllRegisteredUsers();
        List<TdarUser> someRegisteredUsers = allRegisteredUsers.subList(0, Math.min(10, allRegisteredUsers.size()));
        return someRegisteredUsers;
    }

    protected List<Person> getSomePeople() {
        List<Person> allNonUsers = entityService.findAll();
        allNonUsers.removeAll(entityService.findAllRegisteredUsers());
        List<Person> someNonUsers = allNonUsers.subList(0, Math.min(10, allNonUsers.size()));
        logger.debug("non-users: {}", someNonUsers);
        if (CollectionUtils.isEmpty(someNonUsers)) {
            Assert.fail("expecting users");
        }
        return someNonUsers;
    }

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
            setInput("latitudeLongitudeBoxes[0].maximumLatitude", latLong.getMaximumLatitude());
            setInput("latitudeLongitudeBoxes[0].maximumLongitude", latLong.getMaximumLongitude());
            setInput("latitudeLongitudeBoxes[0].minimumLatitude", latLong.getMinimumLatitude());
            setInput("latitudeLongitudeBoxes[0].minimumLongitude", latLong.getMinimumLongitude());
        }
        if (status != null) {
            setInput("status", status.name());
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
