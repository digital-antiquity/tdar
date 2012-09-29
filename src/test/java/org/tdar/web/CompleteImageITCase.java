/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;

public class CompleteImageITCase extends AbstractAdminAuthenticatedWebTestCase {
    public static HashMap<String, String> docValMap = new HashMap<String, String>();
    public static HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();
    public static String PROJECT_ID_FIELDNAME = "projectId";
    public static String DOCUMENT_TITLE_FIELDNAME = "image.title";
    public static String DESCRIPTION_FIELDNAME = "image.description";
    public static final String TEST_IMAGE_NAME = "5127663428_42ef7f4463_b.jpg";
    public static final String TEST_IMAGE = TestConstants.TEST_IMAGE_DIR + TEST_IMAGE_NAME;

    public static String PROJECT_ID = "1";
    public static String IMAGE_TITLE = "My Sample Image";
    public static String DESCRIPTION = "A resource description";

    public CompleteImageITCase() {
        docValMap.put(PROJECT_ID_FIELDNAME, PROJECT_ID);
        docValMap.put(DOCUMENT_TITLE_FIELDNAME, IMAGE_TITLE);
        docValMap.put(DESCRIPTION_FIELDNAME, DESCRIPTION);
        docValMap.put("image.dateCreated", "1923");
        docMultiValMap.put("investigationTypeIds", Arrays.asList(new String[] { "1", "2", "3", "5" }));
        docMultiValMap.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "273", "312" }));
        docMultiValMap.put("materialKeywordIds", Arrays.asList(new String[] { "15", "12", "5" }));
        docMultiValMap.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "12", "15", "26" }));
        docValMap.put("authorizedUsers[0].user.id", "121");
        docValMap.put("authorizedUsers[1].user.id", "5349");
        docValMap.put("authorizedUsers[0].generalPermission", "MODIFY_METADATA");
        docValMap.put("authorizedUsers[1].generalPermission", "VIEW_ALL");
        docValMap.put("authorizedUsers[0].user.firstName", "Michelle");
        docValMap.put("authorizedUsers[0].user.lastName", "Elliott");
        docValMap.put("authorizedUsers[1].user.firstName", "Joshua");
        docValMap.put("authorizedUsers[1].user.lastName", "Watts");

        docValMap.put("authorshipProxies[0].person.firstName", "Adam");
        docValMap.put("authorshipProxies[0].person.lastName", "Brin");
        docValMap.put("authorshipProxies[0].person.email", "aest@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "Digital Antiquity1");
        docValMap.put("authorshipProxies[0].personRole", "CREATOR");
        docValMap.put("authorshipProxies[0].person.id", "");

        docValMap.put("authorshipProxies[1].person.firstName", "Shelby");
        docValMap.put("authorshipProxies[1].person.lastName", "Manney");
        docValMap.put("authorshipProxies[1].person.email", "test2@test.com");
        docValMap.put("authorshipProxies[1].personRole", "CREATOR");
        docValMap.put("authorshipProxies[1].person.id", "");
        docValMap.put("authorshipProxies[1].person.institution.name", "Digital Antiquity2");

        docValMap.put("creditProxies[1].institution.name", "Digital Antiquity3");
        docValMap.put("creditProxies[1].institutionRole", "COLLABORATOR");

        docValMap.put("creditProxies[0].institution.name", "Othr Institution");
        docValMap.put("creditProxies[0].institutionRole", "SPONSOR");

        docValMap.put("sourceCollections[0].text", "ASU Museum Collection1");
        docValMap.put("sourceCollections[1].text", "test Museum Collection1");
        docValMap.put("relatedComparativeCollections[0].text", "ASU Museum Collection2");
        docValMap.put("relatedComparativeCollections[1].text", "test Museum Collection2");
        docValMap.put("siteNameKeywords[0]", "sandy");
        docValMap.put("uncontrolledSiteTypeKeywords[0]", "uncontrolled");
        docValMap.put("otherKeywords[0]", "other kwd");
        docValMap.put("uncontrolledCultureKeywords[0]", "German");
        docValMap.put("geographicKeywords[0]", "Georgia");
        docValMap.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        docValMap.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        docValMap.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        docValMap.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        docValMap.put("temporalKeywords[0]", "before time");
        docValMap.put("coverageDates[0].startDate", "1200");
        docValMap.put("coverageDates[0].endDate", "1500");
        docValMap.put("coverageDates[0].dateType", "CALENDAR_DATE");
        docValMap.put("coverageDates[1].startDate", "1200");
        docValMap.put("coverageDates[1].endDate", "1000");
        docValMap.put("coverageDates[1].dateType", "RADIOCARBON_DATE");

        docMultiValMapLab.put("investigationTypeIds",
                Arrays.asList(new String[] { "Archaeological Overview", "Architectural Survey", "Collections Research", "Data Recovery / Excavation" }));
        docMultiValMapLab.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "Agricultural or Herding", "Ancient church / religious structure" }));
        docMultiValMapLab.put("materialKeywordIds", Arrays.asList(new String[] { "Fire Cracked Rock", "Mineral", "Wood" }));
        docMultiValMapLab.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "Hopewell", "Middle Woodland", "African American" }));

        docValMap.put("resourceAvailability", "Embargoed");
        docValMap.put("resourceProviderInstitution", "Digital Antiquity4");
        docValMap.put("resource.copyLocation", "test");
    }

    @Test
    @Rollback(true)
    public void testCreateImageRecord() {
        // upload a file ahead of submitting the form
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TEST_IMAGE);

        gotoPage("/image/add");
        setInput("ticketId", ticketId);
        addFileProxyFields(0, true, TEST_IMAGE_NAME);
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        for (String key : docMultiValMap.keySet()) {
            setInput(key, (String[]) docMultiValMap.get(key).toArray(new String[0]));
        }
        logger.info(getPageText());
        submitForm();
        // logger.info(getPageText());
        for (String key : docValMap.keySet()) {
            // avoid the issue of the fuzzy distances or truncation... use just the
            // top of the lat/long
            if (key.startsWith("latitudeLongitudeBox")) {
                assertTextPresentInPage(docValMap.get(key).substring(0, docValMap.get(key).indexOf(".")));
                // these are displayed by "type" or not "displayed"
            } else if (key.equals("resourceLanguage")) {
                assertTextPresentInPage(docValMap.get(key), false);
            } else if (key.equals("resourceAvailability")) {
                assertTextPresent("will be released");
            } else if (key.equals("confidential")) {
                assertTextPresent("confidential");
            } else if (!key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.startsWith("individualInstitutions") && !key.contains("Email")
                    && !key.contains(".ids") && !key.contains(".email") && !key.contains(".id") && !key.contains(".dateType") && !key.contains("generalPermission") 
                    && !key.contains(".type") && !key.contains("Role") && !key.contains("person.institution.name") && !key.contains("person.id")) {
                assertTextPresentInPage(docValMap.get(key), false);
            }
        }
        for (String key : docMultiValMapLab.keySet()) {
            for (String val : docMultiValMapLab.get(key)) {
                assertTextPresent(val);
            }
        }

        webClient.getCache().clear();
        clickLinkWithText("edit");
        logger.info(getPageText());

        // FIXME: the order here is arbitrary, mainly from the fact that
        // we're not setting ids and using them, or maintaining an order
        List<String> unorderedCheck = new ArrayList<String>();
        for (String key : docValMap.keySet()) {

            String val = docValMap.get(key);
            if (key.contains("Ids") || key.contains(PROJECT_ID_FIELDNAME) || key.contains("upload") || key.contains(".id") || val.toUpperCase().equals(val))
                continue;

            if (key.startsWith("individual") || key.startsWith("sourceC") || key.startsWith("relatedC") || key.startsWith("institution") || 
                    key.contains("authorized")) {
                unorderedCheck.add(val);
                continue;
            }

            if (key.contains("dateType"))
                continue;

            assertTrue("element:" + key + " is set to:" + val, checkInput(key, val));
        }

        for (String val : unorderedCheck) {
            assertTextPresent(val);
        }

        for (String key : docMultiValMap.keySet()) {
            for (String val : docMultiValMap.get(key)) {
                assertTrue("element:" + key + " is set to:" + val, checkInput(key, val));
            }
        }

        // FIXME: need assert for 'friendly' role name
        // FIXME: need assert for 'friendly' creatorType

        // make sure our 'async' file was added to the resource
        assertTextPresentInPage(TEST_IMAGE_NAME);
    }
}
