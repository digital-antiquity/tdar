/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.resource;

import static org.junit.Assert.assertTrue;
import static org.tdar.TestConstants.TEST_DOCUMENT;
import static org.tdar.TestConstants.TEST_DOCUMENT_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.core.bean.coverage.CoverageType;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.core.bean.resource.ResourceNoteType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;
import org.tdar.web.collection.CollectionWebITCase;

@RunWith(MultipleWebTdarConfigurationRunner.class)
@RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TDAR, RunWithTdarConfiguration.FAIMS })
public class CompleteDocumentWebITCase extends AbstractAdminAuthenticatedWebTestCase {
    private static final String ASSIGN_RIGHTS = "Assign Rights";
    public HashMap<String, String> docValMap;
    public HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();
    // we will assert the presence of these values, but we don't care what order they appear
    public Map<String, String> docUnorderdValMap = new HashMap<String, String>();
    public List<String> alternateTextLookup = new ArrayList<String>();
    public List<String> alternateCodeLookup = new ArrayList<String>();
    public static String REGEX_DOCUMENT_VIEW = "\\/document\\/\\d+\\/(.+)$";

    public CompleteDocumentWebITCase() {
        docValMap = new HashMap<String, String>();
        // removing inline implementation of HashMap to remove serialization warning
        alternateTextLookup.add(RESTRICTED_ACCESS_TEXT);

        docValMap.put("projectId", "1");
        docValMap.put("document.title", "My Sample Document");
        docValMap.put("document.documentType", "OTHER");
        docValMap.put("authorshipProxies[0].person.firstName", "Jim");
        docValMap.put("authorshipProxies[0].person.lastName", "deVos");
        docValMap.put("authorshipProxies[0].person.email", "testabc123@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "A wholly new institution name");
        docValMap.put("authorshipProxies[0].person.id", "");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.AUTHOR.name());
        alternateTextLookup.add(ResourceCreatorRole.AUTHOR.getLabel());
        // docValMap.put("authorshipProxies[1].person.institution.name", "SOME INSTITUTION");
        // docValMap.put("authorshipProxies[1].person.firstName", "test");
        // docValMap.put("authorshipProxies[1].person.lastName", "test");
        // docValMap.put("authorshipProxies[1].personRole", "AUTHOR");
        // docValMap.put("authorshipProxies[1].person.email", "testabc1233@test.com");
        // docValMap.put("authorshipProxies[1].person.id", "");
        docValMap.put("document.description", "A resource description");
        docValMap.put("document.date", "1923");
        // authorizedUsers[0].user.id
        docValMap.put("document.doi", "doi:10.1016/j.iheduc.2003.11.004");
        docValMap.put("document.isbn", "9780385483995");
        alternateTextLookup.add(Language.SPANISH.getLabel());
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
            alternateTextLookup.add(LicenseType.OTHER.getLabel());
            docValMap.put("resource.licenseText", "my custom license");
        }

        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            // docValMap.put(TestConstants.COPYRIGHT_HOLDER_TYPE, "Institution");
            docValMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }
        docValMap.put("siteNameKeywords[0]", "sandy");
        docValMap.put("uncontrolledSiteTypeKeywords[0]", "uncontrolled");
        docValMap.put("otherKeywords[0]", "other");
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
        alternateTextLookup.add(CoverageType.CALENDAR_DATE.getLabel());
        docValMap.put("coverageDates[1].startDate", "1200");
        docValMap.put("coverageDates[1].endDate", "1000");
        docValMap.put("coverageDates[1].dateType", CoverageType.RADIOCARBON_DATE.name());
        alternateTextLookup.add(CoverageType.RADIOCARBON_DATE.getLabel());
        docValMap.put("resourceProviderInstitutionName", "Digital Antiquity");
        // FIXME: notes not maintaining order
        docValMap.put("resourceNotes[0].type", ResourceNoteType.GENERAL.name());
        alternateTextLookup.add(ResourceNoteType.GENERAL.getLabel());
        docValMap.put("resourceNotes[0].note", "A Moose once bit my sister...");
        // introduce a gap in the list (e.g. a user adds notes and then deleted the middle item)
        docValMap.put("resourceNotes[1].type", ResourceNoteType.REDACTION.name());
        alternateTextLookup.add(ResourceNoteType.REDACTION.getLabel());
        docValMap.put("resourceNotes[1].note", "We apologise for the fault in the subtitles. Those responsible have been sacked.");

        docMultiValMap.put("investigationTypeIds", Arrays.asList(new String[] { "1", "2", "3", "5" }));
        docMultiValMap.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "273", "312" }));
        docMultiValMap.put("approvedMaterialKeywordIds", Arrays.asList(new String[] { "15", "12", "5" }));
        docMultiValMap.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "12", "15", "26" }));

        docMultiValMapLab.put("investigationTypeIds",
                Arrays.asList(new String[] { "Archaeological Overview", "Architectural Survey", "Collections Research", "Data Recovery / Excavation" }));
        docMultiValMapLab.put("approvedSiteTypeKeywordIds", Arrays.asList(new String[] { "Agricultural or Herding", "Ancient church / religious structure" }));
        docMultiValMapLab.put("approvedMaterialKeywordIds", Arrays.asList(new String[] { "Fire Cracked Rock", "Mineral", "Wood" }));
        docMultiValMapLab.put("approvedCultureKeywordIds", Arrays.asList(new String[] { "Hopewell", "Middle Woodland", "African American" }));

        // look for these values in end product (and the edit form), but the system may alter the form field names (e.g. due to culling nulls in lists)
        docUnorderdValMap.put("resourceNotes[5].type", ResourceNoteType.RIGHTS_ATTRIBUTION.name());
        docUnorderdValMap.put("resourceNotes[5].note", "I'm not internationally known, but I'm known to rock a microphone.");

    }

    @Test
    @Rollback(true)
    public void testCreateDocumentEditSavehasResource() {

        gotoPage("/document/add");

        HashMap<String, String> docValMap2 = new HashMap<String, String>();
        docValMap2.put("document.title", "My Sample Document");
        docValMap2.put("document.documentType", "OTHER");
        docValMap2.put("document.description", "A resource description");
        docValMap2.put("document.date", "1923");
        docValMap2.put("projectId", "1");
        String ORIGINAL_START_DATE = "1200";
        String COVERAGE_START = "coverageDates[0].startDate";
        docValMap2.put(COVERAGE_START, ORIGINAL_START_DATE);
        String ORIGINAL_END_DATE = "1500";
        docValMap2.put("coverageDates[0].endDate", ORIGINAL_END_DATE);
        docValMap2.put("coverageDates[0].dateType", CoverageType.CALENDAR_DATE.name());

        if (TdarConfiguration.getInstance().getLicenseEnabled()) {
            docValMap2.put("resource.licenseType", LicenseType.OTHER.name());
            docValMap2.put("resource.licenseText", "my custom license");
        }

        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            docValMap2.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        }

        for (String key : docValMap2.keySet()) {
            setInput(key, docValMap2.get(key));
        }

        submitForm();

        String path = internalPage.getUrl().getPath().toLowerCase();
        logger.trace(getPageText());
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + getPageText(), path.matches(REGEX_DOCUMENT_VIEW));

        assertTextPresent(ORIGINAL_START_DATE);
        assertTextPresent(ORIGINAL_END_DATE);

        clickLinkWithText("edit");
        String NEW_START_DATE = "100";
        setInput(COVERAGE_START, NEW_START_DATE);
        submitForm();

        path = internalPage.getUrl().getPath().toLowerCase();
        logger.trace(getPageText());
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + getPageText(), path.matches(REGEX_DOCUMENT_VIEW));
        assertTextPresent(NEW_START_DATE);
        assertTextNotPresent(ORIGINAL_START_DATE);
        assertTextPresent(ORIGINAL_END_DATE);
        logger.trace(getPageText());
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

        docValMap.putAll(docUnorderdValMap);

        setInputs(docValMap, docMultiValMap);

        submitForm();

        String path = internalPage.getUrl().getPath().toLowerCase();
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + getPageText(), path.matches(REGEX_DOCUMENT_VIEW));

        // try {
        // FileUtils.writeStringToFile(new File("post-save.html"), getPageCode());
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        assertViewPage(docValMap, docMultiValMapLab);

        webClient.getCache().clear();

        // go to the edit page and ensure (some) of the form fields and values that we originally created are still present
        clickLinkWithText("edit");
        logger.debug("----now on edit page----");
        logger.trace(getPageText());
        // try {
        // FileUtils.writeStringToFile(new File("pre-save.html"), getPageCode());
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        assertEditPage(docValMap, docMultiValMap, docUnorderdValMap);

        // FIXME: need assert for 'friendly' role name
        // FIXME: need assert for 'friendly' creatorType

        // make sure our 'async' file was added to the resource
        assertTextPresentInPage(TEST_DOCUMENT_NAME);
        submitForm();

        clickLinkOnPage(CollectionWebITCase.PERMISSIONS);
        alternateCodeLookup.clear();
        Map<String,String> authMap = new HashMap<>();
        authMap.put("proxies[0].id", "121");
        authMap.put("proxies[1].id", "5349");
        authMap.put("proxies[0].permission", GeneralPermissions.MODIFY_RECORD.name());
        authMap.put("proxies[1].permission", GeneralPermissions.VIEW_ALL.name());
        authMap.put("proxies[0].displayName", "Michelle Elliott");
        authMap.put("proxies[1].displayName", "Joshua Watts");
        alternateCodeLookup.add(GeneralPermissions.MODIFY_RECORD.name());
        alternateCodeLookup.add(GeneralPermissions.VIEW_ALL.name());

        
        setInputs(authMap, new HashMap<>());
        submitForm();
        assertViewPage(authMap, new HashMap<>());
        clickLinkOnPage(CollectionWebITCase.PERMISSIONS);
        assertEditPage(authMap, new HashMap<String,List<String>>(), authMap);

    }

    private void assertEditPage(Map<String, String> _docValMap, Map<String, List<String>> _docMultiValMap, Map<String, String> _docUnorderdValMap) {
        for (String key : _docValMap.keySet()) {
            String val = _docValMap.get(key);

            // ignore id fields, file uploads, and fields with UPPER CASE VALUES (huh?)
            if (key.contains("Ids") || key.contains("upload") || val.toUpperCase().equals(val)) {
                continue;
            }

            if (_docUnorderdValMap.containsKey(key)) {
                assertTextPresent(_docValMap.get(key));
            } else {
                assertTrue("element:" + key + " should be set to:" + val, checkInput(key, val));
            }
        }

        for (String key : _docMultiValMap.keySet()) {
            for (String val : _docMultiValMap.get(key)) {
                assertTrue("element:" + key + " should be set to:" + val, checkInput(key, val));
            }
        }
    }

    private void assertViewPage(Map<String,String> _docValMap, Map<String,List<String>> _docMultiValMapLab) {
        logger.trace(getPageText());
        for (String key : _docValMap.keySet()) {
            // avoid the issue of the fuzzy distances or truncation... use just
            // the top of the lat/long
            if (key.startsWith("latitudeLongitudeBox")) {
                assertTextPresentInPage(_docValMap.get(key).substring(0, _docValMap.get(key).indexOf(".")));
                // these are displayed by "type" or not "displayed"
            } else if (key.equals("document.documentType") || key.equals("resourceLanguage")) {
                assertTextPresentInPage(_docValMap.get(key), false);
            } else if (!key.equals("document.journalName") && !key.equals("document.bookTitle") && !key.startsWith("authorInstitutions")
                    && !key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.contains("Email") && !key.equals("ticketId")
                    && !key.contains("permission")
                    && !key.contains(".id") && !key.contains(".email") && !key.contains(".type") && !key.contains(".dateType") && !key.contains(".licenseType")
                    && !key.contains("role")
                    && !key.contains("person.institution.name")) {
                assertTextPresentInPage(_docValMap.get(key));
            }
        }
        for (String alt : alternateTextLookup) {
            assertTextPresent(alt);
        }
        for (String alt : alternateCodeLookup) {
            assertTextPresentInCode(alt);
        }

        assertTextNotPresent("embargo");
        for (String key : _docMultiValMapLab.keySet()) {
            for (String val : _docMultiValMapLab.get(key)) {
                assertTextPresent(val);
            }
        }
    }

    private void setInputs(Map<String,String> valMap, Map<String, List<String>>  multiValMap) {
        for (String key : valMap.keySet()) {
            setInput(key, valMap.get(key));
        }
        for (String key : multiValMap.keySet()) {
            setInput(key, multiValMap.get(key).toArray(new String[0]));
        }
    }

}