/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.web.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleWebTdarConfigurationRunner.class)
public class DatasetWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String DATA_DEFAULT_DATA_TABLE_ID = "data-default-data-table-id=\"";
	// FIXME: add datatable controller browse tests. See EditInheritingSectionsWebITCase#testProjectJson on how to parse/inspect.

    private static final String AZ_PALEOINDIAN_POINT_SURVEY_MDB = "az-paleoindian-point-survey.mdb";
    private static final String WEST_COAST_CITIES = "West Coast Cities";
    private static final String EAST_COAST_CITIES = "East Coast Cities";
    private static final String WASHINGTON_3 = "washington";
    private static final String SAN_FRANCISCO_2 = "san francisco";
    private static final String NEW_YORK_1 = "new york";
    // http://localhost:8080/dataset/3513
    public static HashMap<String, String> docValMap = new HashMap<String, String>();
    public static HashMap<String, List<String>> docMultiValMap = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new HashMap<String, List<String>>();
    public static final String TEST_DATASET_NAME = "dataset_with_ints.xls";

    public static String PROJECT_ID = "1";
    public static String TITLE = "My Sample Dataset";
    public static String DESCRIPTION = "A resource description";
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";

    private void addCopyrightHolder(final HashMap<String, String> valueMap) {
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            valueMap.put(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME, "Elsevier");
        } else {
            valueMap.remove(TestConstants.COPYRIGHT_HOLDER_PROXY_INSTITUTION_NAME);
        }
    }

    public DatasetWebITCase() {
        docValMap.put(PROJECT_ID_FIELDNAME, PROJECT_ID);
        docValMap.put("dataset.title", TITLE);
        docValMap.put("dataset.description", DESCRIPTION);
        docValMap.put("resourceCollections[0].name", "TESTCOLLECTIONNAME");
        docValMap.put("dataset.date", "1923");
        docValMap.put("uploadedFiles", TestConstants.TEST_DATA_INTEGRATION_DIR + TEST_DATASET_NAME);
    }

    @Test
    @Rollback
    public void testReplaceWithLBNLTableIssue() {
        docValMap.put(PROJECT_ID_FIELDNAME, "3805");
        docValMap.put("uploadedFiles", TestConstants.TEST_DATA_INTEGRATION_DIR + "replace_prob/" + "LBNL_CA.xlsx");
        uploadDataset();
        Long datasetId = extractTdarIdFromCurrentURL();

        String filename = TestConstants.TEST_DATA_INTEGRATION_DIR + "replace_prob/" + "LBNL_CA_v2.xls";
        String ticketId = getPersonalFilestoreTicketId();
        assertTrue("Expected integer number for ticket - but got: " + ticketId, ticketId.matches("([0-9]*)"));
        docValMap.remove("uploadedFiles");
        uploadFileToPersonalFilestore(ticketId, filename);
        gotoPage("/dataset/" + datasetId + "/edit");
        setInput("ticketId", ticketId);
        Long fileId = Long.parseLong(getInput("fileProxies[0].fileId").getAttribute("value"));
        addFileProxyFields(1, FileAccessRestriction.PUBLIC, filename, fileId, FileAction.REPLACE);
        submitForm();
        assertFalse(getCurrentUrlPath().contains("dataset/save"));

    }

    @Test
    @Rollback
    public void testReplaceSpitalfields() {
        docValMap.put(PROJECT_ID_FIELDNAME, "3805");
        String filename = TestConstants.TEST_DATA_INTEGRATION_DIR + SPITAL_DB_NAME;
        docValMap.put("uploadedFiles", filename);
        uploadDataset();
        Long datasetId = extractTdarIdFromCurrentURL();

        String ticketId = getPersonalFilestoreTicketId();
        assertTrue("Expected integer number for ticket - but got: " + ticketId, ticketId.matches("([0-9]*)"));
        docValMap.remove("uploadedFiles");
        filename = TestConstants.TEST_DATA_INTEGRATION_DIR + AZ_PALEOINDIAN_POINT_SURVEY_MDB;
        uploadFileToPersonalFilestore(ticketId, filename);
        gotoPage("/dataset/" + datasetId + "/edit");
        setInput("ticketId", ticketId);
        Long fileId = Long.parseLong(getInput("fileProxies[0].fileId").getAttribute("value"));
        addFileProxyFields(1, FileAccessRestriction.PUBLIC, filename, fileId, FileAction.REPLACE);
        submitForm();
        assertFalse(getCurrentUrlPath().contains("dataset/save"));

    }

    @Test
    @Rollback
    public void testDeleteIssue() {
        docValMap.put(PROJECT_ID_FIELDNAME, "3805");
        docValMap.put("uploadedFiles", TestConstants.TEST_DATA_INTEGRATION_DIR + SPITAL_DB_NAME);
        uploadDataset();
        Long datasetId = extractTdarIdFromCurrentURL();

        gotoPage("/dataset/" + datasetId + "/edit");
        setInput("fileProxies[0].action", FileAction.DELETE);
        submitForm();
        assertFalse(getCurrentUrlPath().contains("dataset/save"));
        assertFalse(getPageText().contains(SPITAL_DB_NAME));

    }

    @Test
    @Rollback(true)
    public void testCreateDatasetRecordSpitalfields() {
        // upload a file ahead of submitting the form
        docValMap.put("uploadedFiles", TestConstants.TEST_DATA_INTEGRATION_DIR + SPITAL_DB_NAME);
        uploadDataset();
        Long datasetId = extractTdarIdFromCurrentURL();
        String datasetUrl = "/dataset/" + datasetId;
        String browseDataUrl = String.format("/dataset/row/%s/%s/1", datasetId, getDataTableId());
        gotoPage(browseDataUrl);
        assertTextNotPresent("Expression dataset is undefined");
        if (TdarConfiguration.getInstance().isViewRowSupported()) {
            assertTextPresentIgnoreCase("Row number 1");
        } else {
            assertTextNotPresentIgnoreCase("Row number 1");
        }
        gotoPage(datasetUrl);
        clickLinkOnPage("duplicate");
        submitForm("duplicate");
        assertPageTitleContains(" (Copy)");
        assertNotEquals(datasetUrl, getCurrentUrlPath());
        assertNoErrorTextPresent();
    }

    @Test
    @Rollback(true)
    public void testCreateDatasetRecord() {

        // upload a file ahead of submitting the form

        docValMap.put("fileProxies[0].restriction", FileAccessRestriction.CONFIDENTIAL.name());
        uploadDataset();

        assertTextPresentInPage(TEST_DATASET_NAME);
        Long datasetId = extractTdarIdFromCurrentURL();
        Long ontologyId = testOntologyCreation();
        Long codingSheetId = testCodingSheetCreation(ontologyId);

        gotoPage("/dataset/" + datasetId);
        assertTextPresentIgnoreCase(RESTRICTED_ACCESS_TEXT);

        clickLinkWithText(TABLE_METADATA);

        assertTextPresentInCode(datasetId.toString());

        assertTrue("Column1 should not be blank", checkInput("dataTableColumns[1].columnEncodingType", DataTableColumnEncodingType.UNCODED_VALUE.name()));
        setInput("dataTableColumns[0].columnEncodingType", DataTableColumnEncodingType.UNCODED_VALUE.name(), false);

        setInput("dataTableColumns[1].columnEncodingType", DataTableColumnEncodingType.CODED_VALUE.name(), false);
        setInput("dataTableColumns[1].categoryVariable.id", "1", false); // ARCHITECTURE
        setInput("dataTableColumns[1].tempSubCategoryVariable.id", "27", false); // MATERIAL
        setInput("dataTableColumns[1].description", "column description for city", false);
        setInput("dataTableColumns[1].defaultCodingSheet.id", Long.toString(codingSheetId), false);
        setInput("dataTableColumns[1].transientOntology.id", Long.toString(ontologyId), false);
        // setInput("postSaveAction", "SAVE_MAP_NEXT");
        logger.debug("coding sheet id: {} ", codingSheetId);
        logger.debug("ontology id: {} ", ontologyId);
        submitForm("Save");
        assertFalse(internalPage.getUrl().toString().contains("save-column-metadata"));

        assertTrue("url:" + internalPage.getUrl().toString() + "should have /dataset/", internalPage.getUrl().toString().contains("/dataset/" + datasetId));
        assertTextPresentIgnoreCase("translated");

        // ensure that changing column metadata didn't implicitly change file access rights
        assertTextPresentIgnoreCase(RESTRICTED_ACCESS_TEXT);
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void testConfidentialDatatableView() {
        testCreateDatasetRecord();
        String viewPageUrl = internalPage.getUrl().toString();
        Long datasetId = extractTdarIdFromCurrentURL();
        String browseDataUrl = "/datatable/browse?id=" + getDataTableId();
        gotoPage(browseDataUrl);
        // does this look like json?
        assertTextPresentInCode("columnEncodingType");
        assertFalse("response should be json, not html", getPageCode().contains("<html"));

        // currently logged in as user, log out and then log in as basic user (without view rights)
        logout();
        login();

        gotoPage(browseDataUrl);
        logger.debug("JSON:{}", getPageBodyCode());
        assertTrue(getPageBodyCode().matches("(?s)(.*)\"results\"(\\s*):(\\s*)[(\\s*)](.*)"));
    }

	private String getDataTableId() {
        Long datasetId = extractTdarIdFromCurrentURL();
        gotoPage("/dataset/" + datasetId);
//        logger.debug(getPageCode());
		int indexOf = getPageCode().indexOf(DATA_DEFAULT_DATA_TABLE_ID) + DATA_DEFAULT_DATA_TABLE_ID.length();
		logger.debug("index:{}", indexOf);
		String substring = StringUtils.substring(getPageCode(), indexOf,indexOf + 100);
		logger.debug("index:{}", substring);
        substring = StringUtils.substring(substring,0, substring.indexOf("\""));
        return substring;
	}

    @SuppressWarnings("unused")
    @Test
    @Rollback
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.FAIMS })
    public void testXmlDatatableView() {
        testCreateDatasetRecord();
        Long datasetId = extractTdarIdFromCurrentURL();
        String browseDataUrl = "/dataset/xml?dataTableId=" + getDataTableId();
        gotoPage(browseDataUrl);
        // does this look like xml?
        final String pageCode = getPageCode();
        assertTrue("response should be xml, not html", pageCode.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""));
    }

    private void uploadDataset() {

        String ticketId = getPersonalFilestoreTicketId();
        assertTrue("Expected integer number for ticket - but got: " + ticketId, ticketId.matches("([0-9]*)"));
        String filename = docValMap.get("uploadedFiles");
        docValMap.remove("uploadedFiles");
        uploadFileToPersonalFilestore(ticketId, filename);

        gotoPage("/dataset/add");
        addCopyrightHolder(docValMap);
        setInput("ticketId", ticketId);
        addFileProxyFields(0, FileAccessRestriction.PUBLIC, filename);

        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        for (String key : docMultiValMap.keySet()) {
            setInput(key, docMultiValMap.get(key).toArray(new String[0]));
        }
        logger.trace(getPageText());
        submitForm();
        assertCurrentUrlContains("columns");
        gotoPage("/dataset/" + extractTdarIdFromCurrentURL());
        logger.trace(getPageText());
        for (String key : docValMap.keySet()) {
            // avoid the issue of the fuzzy distances or truncation... use just the
            // top of the lat/long
            if (key.equals(PROJECT_ID_FIELDNAME) || !key.contains("Ids") && !key.startsWith("individualInstitutions") && !key.contains("Email")
                    && !key.contains(".ids") && !key.contains(".email") && !key.contains(".id") && !key.contains(".dateType")
                    && !key.contains("generalPermission")
                    && !key.contains(".type") && !key.contains("Role") && !key.contains("person.institution.name") && !key.contains("person.id")) {
                continue;
            }
            assertTextPresentInPage(docValMap.get(key), false);

        }
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
    }

    public Long testCodingSheetCreation(Long ontologyId) {
        gotoPage("/coding-sheet/add");
        HashMap<String, String> codingMap = new HashMap<String, String>();
        codingMap.put(PROJECT_ID_FIELDNAME, PROJECT_ID);
        codingMap.put("codingSheet.title", TITLE);
        codingMap.put("codingSheet.description", DESCRIPTION);
        codingMap.put("codingSheet.date", "1923");
        codingMap.put("categoryId", "1"); // ARCHITECTURE
        codingMap.put("subcategoryId", "27"); // MATERIAL
        if (ontologyId != null) {
            codingMap.put("ontology.id", ontologyId.toString());
        }
        addCopyrightHolder(codingMap);

        String codingText = "1," + NEW_YORK_1 + ",NY\r\n" +
                "2," + SAN_FRANCISCO_2 + ",CA\r\n" +
                "3," + WASHINGTON_3 + ",DC\r\n";
        setInput("fileTextInput", codingText);
        for (String key : codingMap.keySet()) {
            setInput(key, codingMap.get(key));
        }
        logger.info(getInput("subcategoryId").asXml());
        logger.trace(getPageText());
        submitForm();
        // logger.info(getPageText());
        for (String key : codingMap.keySet()) {
            // avoid the issue of the fuzzy distances or truncation... use just the
            // top of the lat/long
            if (!key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.startsWith("individualInstitutions") && !key.contains("Email")
                    && !key.contains(".ids") && !key.contains(".email") && !key.contains(".id") && !key.contains(".dateType")
                    && !key.contains("generalPermission")
                    && !key.contains(".type") && !key.contains("Role") && !key.contains("person.institution.name") && !key.contains("person.id")) {
                continue;
            }
            if (!key.contains("ontology.id")) {
                assertTextPresentInPage(codingMap.get(key), false);
            }
        }
        assertTextPresent(NEW_YORK_1);
        assertTextPresent("Architecture");
        assertTextPresent("Style/Type");
        assertTextPresent(WASHINGTON_3);
        assertTextPresent(SAN_FRANCISCO_2);
        assertTextPresent("NY");
        assertTextPresent("CA");
        assertTextPresent("DC");
        assertTextNotPresentIgnoreCase("translated");

        clickLinkOnPage("map ontology");

        // assertTextPresent(EAST_COAST_CITIES);
        // assertTextPresent(WEST_COAST_CITIES);

        int indexOfOntology = getPageCode().indexOf("var ontology");
        String ontologyNodeInfo = getPageCode().substring(indexOfOntology, getPageCode().indexOf("];", indexOfOntology));
        logger.debug("ONTOLOGY NODE TEXT: {}", ontologyNodeInfo);

        String regex = "id: \\\"(\\d+)\\\",(?:\\s+)name: \\\"(.+)\\\"";
        logger.info("regex is: {}", regex);
        Pattern p = Pattern.compile(regex);

        HashMap<String, Long> ontologyMap = new HashMap<String, Long>();
        for (String line : ontologyNodeInfo.split("([{]|(}\\s?,?))")) {
            Matcher matcher = p.matcher(line);
            logger.info("line:{} \t matches:{}", line, matcher.matches());
            if (matcher.matches()) {
                String key = matcher.group(2).replaceAll("[\\|\\-]", "").trim().toLowerCase();
                logger.info("{} : {}", matcher.group(1), key);
                ontologyMap.put(key, Long.parseLong(matcher.group(1)));
            }
        }
        assertTrue(ontologyMap.containsKey("united states"));
        setInput("codingRules[0].ontologyNode.id", ontologyMap.get(NEW_YORK_1).toString(), false);
        setInput("codingRules[1].ontologyNode.id", ontologyMap.get(SAN_FRANCISCO_2).toString(), false);
        setInput("codingRules[2].ontologyNode.id", ontologyMap.get(WASHINGTON_3).toString(), false);
        submitForm("Save");

        // assertTextPresent(EAST_COAST_CITIES);
        // assertTextPresent(WEST_COAST_CITIES);
        assertTextPresent(NEW_YORK_1);
        assertTextPresent(WASHINGTON_3);
        assertTextPresent(SAN_FRANCISCO_2);
        return extractTdarIdFromCurrentURL();
    }

    public Long testOntologyCreation() {
        gotoPage("/ontology/add");
        HashMap<String, String> codingMap = new HashMap<String, String>();
        codingMap.put(PROJECT_ID_FIELDNAME, PROJECT_ID);
        codingMap.put("ontology.title", TITLE);
        codingMap.put("ontology.description", DESCRIPTION);
        codingMap.put("ontology.date", "1923");
        codingMap.put("categoryId", "1"); // ARCHITECTURE
        codingMap.put("subcategoryId", "27"); // MATERIAL
        String ontology = "North America\r\n" +
                "\tUnited States\r\n" +
                "\t\t" + EAST_COAST_CITIES + "\r\n" +
                "\t\t\t" + WASHINGTON_3 + "\r\n" +
                "\t\t\t" + NEW_YORK_1 + "\r\n" +
                "\t\t" + WEST_COAST_CITIES + "\r\n" +
                "\t\t\t" + SAN_FRANCISCO_2;
        addCopyrightHolder(codingMap);

        setInput("fileTextInput", ontology);
        for (String key : codingMap.keySet()) {
            setInput(key, codingMap.get(key));
        }
        logger.info(getPageText());
        submitForm();
        // logger.info(getPageText());
        for (String key : codingMap.keySet()) {
            // avoid the issue of the fuzzy distances or truncation... use just the
            // top of the lat/long
            if (!key.equals(PROJECT_ID_FIELDNAME) && !key.contains("Ids") && !key.startsWith("individualInstitutions") && !key.contains("Email")
                    && !key.contains(".ids") && !key.contains(".email") && !key.contains(".id") && !key.contains(".dateType")
                    && !key.contains("generalPermission")
                    && !key.contains(".type") && !key.contains("Role") && !key.contains("person.institution.name") && !key.contains("person.id")) {
                continue;
            }
            assertTextPresentInPage(codingMap.get(key), false);
        }
        assertTextPresent(NEW_YORK_1);
        assertTextPresent(WASHINGTON_3);
        assertTextPresent(SAN_FRANCISCO_2);
        assertTextNotPresentIgnoreCase("translated");

        Long tdarId = extractTdarIdFromCurrentURL();

        return tdarId;
    }

}
