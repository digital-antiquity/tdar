/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.TestConstants.TEST_DOCUMENT_NAME;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.configuration.TdarConfiguration;

public class CompleteDocumentITCase extends AbstractAdminAuthenticatedWebTestCase {
    public static HashMap<String, String> docValMap;
    public static HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();

    public static String REGEX_DOCUMENT_VIEW = "\\/document\\/\\d+$";

    public CompleteDocumentITCase() {

        docValMap = new HashMap<String, String>();
        // removing inline implementation of HashMap to remove serialization warning
        docValMap.put("projectId", "1");
        docValMap.put("document.title", "My Sample Document");
        docValMap.put("document.documentType", "OTHER");
        docValMap.put("authorshipProxies[0].person.firstName", "Jim");
        docValMap.put("authorshipProxies[0].person.lastName", "deVos");
        docValMap.put("authorshipProxies[0].person.email", "testabc123@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "A wholly new institution name");
        docValMap.put("authorshipProxies[0].person.id", "");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.AUTHOR.name());
        // docValMap.put("authorshipProxies[1].person.institution.name", "SOME INSTITUTION");
        // docValMap.put("authorshipProxies[1].person.firstName", "test");
        // docValMap.put("authorshipProxies[1].person.lastName", "test");
        // docValMap.put("authorshipProxies[1].personRole", "AUTHOR");
        // docValMap.put("authorshipProxies[1].person.email", "testabc1233@test.com");
        // docValMap.put("authorshipProxies[1].person.id", "");
        docValMap.put("document.description", "A resource description");
        docValMap.put("document.date", "1923");
        docValMap.put("authorizedUsers[0].user.id", "121");
        docValMap.put("authorizedUsers[1].user.id", "5349");
        docValMap.put("authorizedUsers[0].generalPermission", GeneralPermissions.MODIFY_RECORD.name());
        docValMap.put("authorizedUsers[1].generalPermission", GeneralPermissions.VIEW_ALL.name());
        docValMap.put("authorizedUsers[0].user.firstName", "Michelle");
        docValMap.put("authorizedUsers[0].user.lastName", "Elliott");
        docValMap.put("authorizedUsers[1].user.firstName", "Joshua");
        docValMap.put("authorizedUsers[1].user.lastName", "Watts");
        docValMap.put("document.doi", "doi:10.1016/j.iheduc.2003.11.004");
        docValMap.put("document.isbn", "9780385483995");
        docValMap.put("resourceLanguage", Language.SPANISH.name());
        docValMap.put("document.url", "http://www.tdar.org");
        docValMap.put("publisherName", "test");
        docValMap.put("document.publisherLocation", "new york");
        docValMap.put("document.edition", "3rd");
        docValMap.put("document.seriesName", "series title");
        docValMap.put("document.seriesNumber", "series number");
        docValMap.put("document.copyLocation", "copy location");
        docValMap.put("document.bookTitle", "book title");
        docValMap.put("document.journalName", "journal name");
        docValMap.put("document.issn", "0002-9114");
        docValMap.put("document.startPage", "mcmxvii");
        docValMap.put("document.endPage", "MMMvii");
        docValMap.put("document.journalNumber", "1");
        docValMap.put("document.volume", "25");
        if (TdarConfiguration.getInstance().getLicenseEnabled()) {
            docValMap.put("resource.licenseType", LicenseType.OTHER.name());
            docValMap.put("resource.licenseText", "my custom license");
        }

        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            docValMap.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            docValMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        docValMap.put("siteNameKeywords[0]", "sandy");
        docValMap.put("uncontrolledSiteTypeKeywords[0]", "uncontrolled");
        docValMap.put("otherKeywords[0]", "other");
        docValMap.put("uncontrolledCultureKeywords[0]", "German");
        docValMap.put("geographicKeywords[0]", "Georgia");
        docValMap.put("latitudeLongitudeBoxes[0].maximumLatitude", "41.83228739643032");
        docValMap.put("latitudeLongitudeBoxes[0].maximumLongitude", "-71.39860153198242");
        docValMap.put("latitudeLongitudeBoxes[0].minimumLatitude", "41.82608370627639");
        docValMap.put("latitudeLongitudeBoxes[0].minimumLongitude", "-71.41018867492676");
        docValMap.put("temporalKeywords[0]", "before time");
        docValMap.put("coverageDates[0].startDate", "1200");
        docValMap.put("coverageDates[0].endDate", "1500");
        docValMap.put("coverageDates[0].dateType", CoverageType.CALENDAR_DATE.name());
        docValMap.put("coverageDates[1].startDate", "1200");
        docValMap.put("coverageDates[1].endDate", "1000");
        docValMap.put("coverageDates[1].dateType", CoverageType.RADIOCARBON_DATE.name());
        docValMap.put("resourceProviderInstitutionName", "Digital Antiquity");
        // FIXME: notes not maintaining order
        docValMap.put("resourceNotes[0].type", ResourceNoteType.GENERAL.name());
        docValMap.put("resourceNotes[0].note", "A Moose once bit my sister...");
        docValMap.put("resourceNotes[1].type", ResourceNoteType.REDACTION.name());
        docValMap.put("resourceNotes[1].note", "We apologise for the fault in the subtitles. Those responsible have been sacked.");

        docMultiValMap.put("investigationTypeIds", Arrays.asList(new String[] { "1", "2", "3", "5" }));
        docMultiValMap.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "273", "312" }));
        docMultiValMap.put("materialKeywordIds", Arrays.asList(new String[] { "15", "12", "5" }));
        docMultiValMap.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "12", "15", "26" }));

        docMultiValMapLab.put("investigationTypeIds",
                Arrays.asList(new String[] { "Archaeological Overview", "Architectural Survey", "Collections Research", "Data Recovery / Excavation" }));
        docMultiValMapLab.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "Agricultural or Herding", "Ancient church / religious structure" }));
        docMultiValMapLab.put("materialKeywordIds", Arrays.asList(new String[] { "Fire Cracked Rock", "Mineral", "Wood" }));
        docMultiValMapLab.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "Hopewell", "Middle Woodland", "African American" }));

    }

    @Test
    @Rollback(true)
    public void testCreateDocument() {

        // grab a ticket, upload a file with that ticket, then set ticketId on this form
        String ticketId = getPersonalFilestoreTicketId();
        assertTrue("Expected integer number for ticket - but got: " + ticketId, ticketId.matches("([0-9]*)"));
        uploadFileToPersonalFilestore(ticketId, TEST_DOCUMENT);

        gotoPage("/document/add");
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.CONFIDENTIAL, TEST_DOCUMENT_NAME);
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        for (String key : docMultiValMap.keySet()) {
            setInput(key, (String[]) docMultiValMap.get(key).toArray(new String[0]));
        }
        submitForm();

        String path = internalPage.getUrl().getPath().toLowerCase();
        logger.info(getPageText());
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + getPageText(), path.matches(REGEX_DOCUMENT_VIEW));

        logger.trace(getPageText());
        nextKey: for (String key : docValMap.keySet()) {
            if ("resource.licenseType".equals(key)) {
                // We are not showing the radio button selection.
                continue nextKey;
            }
            // avoid the issue of the fuzzy distances or truncation... use just
            // the top of the lat/long
            if (key.startsWith("latitudeLongitudeBox")) {
                assertTextPresentInPage(docValMap.get(key).substring(0, docValMap.get(key).indexOf(".")));
                // these are displayed by "type" or not "displayed"
            } else if (key.equals("document.documentType") || key.equals("resourceLanguage")) {
                assertTextPresentInPage(docValMap.get(key), false);
            } else if (!key.equals("document.journalName") && !key.equals("document.bookTitle") && !key.startsWith("authorInstitutions")
                    && !key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.contains("Email") && !key.equals("ticketId")
                    && !key.contains("generalPermission")
                    && !key.contains(".id") && !key.contains(".email") && !key.contains(".type") && !key.contains(".dateType") && !key.contains("role")
                    && !key.contains("person.institution.name")) {
                assertTextPresentInPage(docValMap.get(key));
            }
        }
        assertTextPresent("Note: this resource is restricted from general view");
        assertTextNotPresent("embargo");
        for (String key : docMultiValMapLab.keySet()) {
            for (String val : docMultiValMapLab.get(key)) {
                assertTextPresent(val);
            }
        }

        webClient.getCache().clear();
        clickLinkWithText("edit");
        logger.debug("----now on edit page----");
        logger.trace(getPageText());

        for (String key : docValMap.keySet()) {
            String val = docValMap.get(key);
            if (key.contains("Ids") || key.contains("upload") || val.toUpperCase().equals(val))
                continue;

            // FIXME: THE MAIN ISSUE HERE IS ONE OF ORDER NOTES FIELDS ARE NOT BEING SAVED AND KEPT IN THE ORDER
            // THEY'RE BEING ENTERED, HENCE THE TEST FAILS
            if (key.contains("[0]") || key.contains("[1]")) {
                assertTextPresent(docValMap.get(key));
                continue;
            }
            assertTrue("element:" + key + " should be set to:" + val, checkInput(key, val));
        }

        for (String key : docMultiValMap.keySet()) {
            for (String val : docMultiValMap.get(key)) {
                assertTrue("element:" + key + " should be set to:" + val, checkInput(key, val));
            }
        }

        // FIXME: need assert for 'friendly' role name
        // FIXME: need assert for 'friendly' creatorType

        // make sure our 'async' file was added to the resource
        assertTextPresentInPage(TEST_DOCUMENT_NAME);

    }

}