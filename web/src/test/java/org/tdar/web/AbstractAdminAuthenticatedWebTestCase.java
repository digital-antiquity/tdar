/**
 * 
 */
package org.tdar.web;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
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
        gotoPage("/share/add");
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

    private Document createDoc(Long id, String title, int dateCreated, DocumentType documentType, Status status) {
    	Document doc  = new Document();
    	doc.setTitle(title);
    	doc.setDescription(title);
    	doc.setDate(dateCreated);
    	doc.setDocumentType(documentType);
    	doc.setStatus(status);
    	doc.setId(id);
    	return doc;
    }
    
    protected List<? extends Resource> getSomeResources() {
    	List<Document> someDocs = new ArrayList<>();
    	someDocs.add(createDoc(4287L,"Archeological Survey and Architectural Study of Montezuma Castle National Monument",  2010,  DocumentType.BOOK, Status.ACTIVE));
    	someDocs.add(createDoc(3794L, "Faunal Coding Key", 1988, DocumentType.OTHER, Status.ACTIVE));
    	someDocs.add(createDoc(4230L,"2008 New Philadelphia Archaeology Report, Chapter 2, An Investigation of New Philadelphia Using Thermal Infrared Remote Sensing", 2008, DocumentType.OTHER, Status.ACTIVE));
    	someDocs.add(createDoc(4231L,"2008 New Philadelphia Archaeology Report, Chapter 3, Block 3, Lot 4", 2008, DocumentType.OTHER, Status.ACTIVE));
    	someDocs.add(createDoc(4232L,"2008 New Philadelphia Archaeology Report, Chapter 4, Block 7, Lot 1", 2008, DocumentType.OTHER, Status.ACTIVE));
        return someDocs;
    }

    protected List<TdarUser> getSomeUsers() {
        // let's only get authorized users
    	List<TdarUser> users = new ArrayList<>();
    	users.add(new TdarUser("Allen", "Lee", "allen.lee@dsu.edu","allen.lee", 1L));
    	users.add(new TdarUser("Keith","Kintigh","kintigh@dsu.edu","kintigh", 6L));
    	users.add(new TdarUser("Mallorie", "Hatch","mallorie.hatch@dsu.edu","mallorie.hatch", 38L));
    	users.add(new TdarUser("Matthew","Peeples", "matthew.peeples@dsu.edu","matthew.peeples", 60L));
    	users.add(new TdarUser("Michelle","Elliott","michelle.elliott@dsu.edu","michelle.elliott", 121L));
        return users;
    }

    protected List<Person> getSomePeople() {
    	List<Person> users = new ArrayList<>();
    	users.add(new Person("Tiffany", "Clark", "tiffany.clark@dsu.edu", 100L));
    	users.add(new Person("Patty Jo", "Watson", "pjwatson@dsu.edu", 8007L));
    	users.add(new Person("Charles", "Redman", "charles.redman@dsu.edu", 8008L));
    	users.add(new Person("Steven", "LeBlanc", null, 8009L));
        return users;
    }

    @SuppressWarnings("deprecation")
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

        setInput("shares[0].name", TEST_SECURITY_COLLECTION);
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
