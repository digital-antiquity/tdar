/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

@RunWith(MultipleWebTdarConfigurationRunner.class)
public class CompleteImageWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String TITLE = "My Sample Image";
    private static final String COPYRIGHT_HOLDER_PERSON_FIRST_NAME = "copyrightHolderProxies.person.firstName";
    private static final String COPYRIGHT_HOLDER_PERSON_LAST_NAME = "copyrightHolderProxies.person.lastName";
    private static final List<String> VALUES_NOT_SHOWN_ON_OVERVIEW_PAGE = Arrays.asList(new String[] { COPYRIGHT_HOLDER_PERSON_FIRST_NAME,
            COPYRIGHT_HOLDER_PERSON_LAST_NAME });
    // , TestConstants.COPYRIGHT_HOLDER_TYPE
    public static HashMap<String, String> docValMap = new HashMap<String, String>();
    public static HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();
    public static final String TEST_IMAGE_NAME = "5127663428_42ef7f4463_b.jpg";
    public static final String TEST_IMAGE = TestConstants.TEST_IMAGE_DIR + TEST_IMAGE_NAME;

    public CompleteImageWebITCase() {
        docValMap.put("projectId", "1");
        docValMap.put("image.title", TITLE);
        docValMap.put("image.description", "A resource description");
        docValMap.put("image.date", "1923");
        docMultiValMap.put("investigationTypeIds", Arrays.asList(new String[] { "1", "2", "3", "5" }));
        docMultiValMap.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "273", "312" }));
        docMultiValMap.put("approvedMaterialKeywordIds", Arrays.asList(new String[] { "15", "12", "5" }));
        docMultiValMap.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "12", "15", "26" }));
        docValMap.put("authorizedUsers[0].user.id", "121");
        docValMap.put("authorizedUsers[1].user.id", "5349");
        docValMap.put("authorizedUsers[0].generalPermission", GeneralPermissions.MODIFY_RECORD.name());
        docValMap.put("authorizedUsers[1].generalPermission", GeneralPermissions.VIEW_ALL.name());
        docValMap.put("authorizedUsersFullNames[0]", "Michelle Elliott");
        docValMap.put("authorizedUsersFullNames[1]", "Joshua Watts");

        docValMap.put("authorshipProxies[0].person.firstName", "Adam");
        docValMap.put("authorshipProxies[0].person.lastName", "Brin");
        docValMap.put("authorshipProxies[0].person.email", "aest@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "Digital Antiquity1");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.CREATOR.name());
        docValMap.put("authorshipProxies[0].person.id", "");

        // FIXME: this is brittle
        docValMap.put("authorshipProxies[1].person.firstName", "Shelby");
        docValMap.put("authorshipProxies[1].person.lastName", "Manney");
        docValMap.put("authorshipProxies[1].person.email", "test2@test.com");
        docValMap.put("authorshipProxies[1].role", ResourceCreatorRole.CREATOR.name());
        docValMap.put("authorshipProxies[1].person.id", "");
        docValMap.put("authorshipProxies[1].person.institution.name", "Digital Antiquity2");
        // FIXME: this is brittle
        docValMap.put("creditProxies[1].institution.name", "Digital Antiquity3");
        docValMap.put("creditProxies[1].role", ResourceCreatorRole.COLLABORATOR.name());

        docValMap.put("creditProxies[0].institution.name", "Othr Institution");
        docValMap.put("creditProxies[0].role", ResourceCreatorRole.SPONSOR.name());
        docValMap.put("image.url", "http://zombo.com");

        docValMap.put("sourceCollections[0].text", "ASU Museum Collection1");
        docValMap.put("sourceCollections[1].text", "test Museum Collection1");
        docValMap.put("relatedComparativeCollections[0].text", "ASU Museum Collection2");
        docValMap.put("relatedComparativeCollections[1].text", "test Museum Collection2");
        docValMap.put("siteNameKeywords[0]", "sandy");
        docValMap.put("uncontrolledSiteTypeKeywords[0]", "uncontrolled");
        docValMap.put("otherKeywords[0]", "other kwd");
        docValMap.put("uncontrolledCultureKeywords[0]", "German");
        docValMap.put("geographicKeywords[0]", "Georgia");
        docValMap.put("latitudeLongitudeBoxes[0].north", "41.83228739643032");
        docValMap.put("latitudeLongitudeBoxes[0].east", "-71.39860153198242");
        docValMap.put("latitudeLongitudeBoxes[0].south", "41.82608370627639");
        docValMap.put("latitudeLongitudeBoxes[0].west", "-71.41018867492676");
        docValMap.put("temporalKeywords[0]", "before time");
        docValMap.put("coverageDates[0].startDate", "1200");
        docValMap.put("coverageDates[0].endDate", "1500");
        docValMap.put("coverageDates[0].dateType", CoverageType.CALENDAR_DATE.name());
        docValMap.put("coverageDates[1].startDate", "1200");
        docValMap.put("coverageDates[1].endDate", "1000");
        docValMap.put("coverageDates[1].dateType", CoverageType.RADIOCARBON_DATE.name());

        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // docValMap.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Person");
            docValMap.put(COPYRIGHT_HOLDER_PERSON_LAST_NAME, "Disney");
            docValMap.put(COPYRIGHT_HOLDER_PERSON_FIRST_NAME, "Walt");
            // docValMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Disney Corp.");
        }

        if (TdarConfiguration.getInstance().getLicenseEnabled()) {
            docValMap.put("resource.licenseType", "" + LicenseType.CREATIVE_COMMONS_NONCOMMERCIAL.name());
        }

        docMultiValMapLab.put("investigationTypeIds",
                Arrays.asList(new String[] { "Archaeological Overview", "Architectural Survey", "Collections Research", "Data Recovery / Excavation" }));
        docMultiValMapLab.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "Agricultural or Herding", "Ancient church / religious structure" }));
        docMultiValMapLab.put("approvedMaterialKeywordIds", Arrays.asList(new String[] { "Fire Cracked Rock", "Mineral", "Wood" }));
        docMultiValMapLab.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "Hopewell", "Middle Woodland", "African American" }));

        docValMap.put("resourceProviderInstitutionName", "Digital Antiquity4");
        docValMap.put("image.copyLocation", "test");
    }

    
    @Test
    @Rollback(true)
    public void testImageAuthorizedRightsIssue() {
        gotoPage("/image/add");
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        for (String key : docMultiValMap.keySet()) {
            setInput(key, docMultiValMap.get(key).toArray(new String[0]));
        }
        logger.trace(getPageText());
        submitForm();
        clickLinkWithText("edit");
        setInput("authorizedUsers[0].generalPermission", GeneralPermissions.VIEW_ALL.name());
        setInput("authorizedUsers[1].generalPermission", GeneralPermissions.VIEW_ALL.name());
        submitForm();
        assertFalse(getCurrentUrlPath().contains("edit"));
        assertFalse(getPageCode().contains(GeneralPermissions.MODIFY_METADATA.name()));
        assertTextPresentInPage(TITLE);
    }
    
    @Test
    @Rollback(true)
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
    public void testCreateImageRecord() {
        // upload a file ahead of submitting the form
        String ticketId = getPersonalFilestoreTicketId();
        uploadFileToPersonalFilestore(ticketId, TEST_IMAGE);

        gotoPage("/image/add");
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.EMBARGOED_FIVE_YEARS, TEST_IMAGE_NAME);
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        for (String key : docMultiValMap.keySet()) {
            setInput(key, docMultiValMap.get(key).toArray(new String[0]));
        }
        logger.trace(getPageText());
        submitForm();
        // logger.info(getPageText());
        nextKey: for (String key : docValMap.keySet()) {
            if (VALUES_NOT_SHOWN_ON_OVERVIEW_PAGE.contains(key)) {
                // We are not showing the radio button selection result.
                continue nextKey;
            }
            // avoid the issue of the fuzzy distances or truncation... use just the
            // top of the lat/long
            if (key.startsWith("latitudeLongitudeBox")) {
                assertTextPresentInPage(docValMap.get(key).substring(0, docValMap.get(key).indexOf(".")));
                // these are displayed by "type" or not "displayed"
            } else if (key.equals("resource.licenseType")) {
                assertTextPresent(LicenseType.CREATIVE_COMMONS_NONCOMMERCIAL.getLicenseName());
            } else if (key.equals("resourceLanguage")) {
                assertTextPresentInPage(docValMap.get(key), false);
            } else if (!key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.startsWith("individualInstitutions") && !key.contains("Email")
                    && !key.contains(".ids") && !key.contains(".email") && !key.contains(".id") && !key.contains(".dateType")
                    && !key.contains("generalPermission")
                    && !key.contains(".type") && !key.contains("Role") && !key.contains("person.institution.name") && !key.contains("person.id")) {
                assertTextPresentInPage(docValMap.get(key), false);
            }
        }
        assertTextPresent("will be released");
        for (String key : docMultiValMapLab.keySet()) {
            for (String val : docMultiValMapLab.get(key)) {
                assertTextPresent(val);
            }
        }

        webClient.getCache().clear();
        clickLinkWithText("edit");
        logger.trace(getPageText());

        // FIXME: the order here is arbitrary, mainly from the fact that
        // we're not setting ids and using them, or maintaining an order
        List<String> unorderedCheck = new ArrayList<String>();
        for (String key : docValMap.keySet()) {

            String val = docValMap.get(key);
            if (key.contains("Ids") || key.contains(PROJECT_ID_FIELDNAME) || key.contains("upload") || key.contains(".id") || val.toUpperCase().equals(val)) {
                continue;
            }

            if (key.startsWith("individual") || key.startsWith("sourceC") || key.startsWith("relatedC") || key.startsWith("institution") ||
                    key.contains("authorized")) {
                unorderedCheck.add(val);
                continue;
            }

            if (key.contains("dateType")) {
                continue;
            }

            HtmlElement input = getInput(key);
            assertTrue(String.format("element: %s should be set to: %s but was", key, val, input.asText()), checkInput(key, val));
        }

        for (String val : unorderedCheck) {
            assertTextPresent(val);
        }

        logger.trace(getPageCode());
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
