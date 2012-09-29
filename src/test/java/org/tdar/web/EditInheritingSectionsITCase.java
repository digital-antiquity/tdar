package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

public class EditInheritingSectionsITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static long PARENT_PROJECT_ID = 3805;
    // private static String PARENT_PROJECT_TITLE = "New Philadelphia Archaeology Project";
    private static String[] PARENT_PROJECT_CULTURE_KEYWORDS = { "Archaic", "Historic" };
    private static String DOCUMENT_EDIT_URL = "/document/4230/edit";
    private static String INHERITING_CULTURAL_INFORMATION_FIELDNAME = "resource.inheritingCulturalInformation";
    private static String INHERITING_INVESTIGATION_INFORMATION_FIELDNAME = "resource.inheritingInvestigationInformation";
    private static String INHERITING_SITE_INFORMATION_FIELDNAME = "resource.inheritingSiteInformation";
    private static String INHERITING_MATERIAL_INFORMATION_FIELDNAME = "resource.inheritingMaterialInformation";
    private static String INHERITING_TEMPORAL_INFORMATION_FIELDNAME = "resource.inheritingTemporalInformation";
    private static String INHERITING_OTHER_INFORMATION_FIELDNAME = "resource.inheritingOtherInformation";
    private static String INHERITING_SPATIAL_INFORMATION_FIELDNAME = "resource.inheritingSpatialInformation";

    private Map<String, String> docValMap = new HashMap<String, String>();

    public EditInheritingSectionsITCase() {
        docValMap.put(INHERITING_CULTURAL_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_INVESTIGATION_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_SITE_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_INVESTIGATION_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_SITE_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_MATERIAL_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_TEMPORAL_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_OTHER_INFORMATION_FIELDNAME, "true");
        docValMap.put(INHERITING_SPATIAL_INFORMATION_FIELDNAME, "true");
    }

    @Test
    @Rollback(true)
    public void testCulturalSectionChecked() {
        gotoPage(DOCUMENT_EDIT_URL);
        String key = INHERITING_CULTURAL_INFORMATION_FIELDNAME;
        String val = "true";
        assertTrue("section checkbox :" + key + " is set to:" + val, checkInput(key, val));
        assertFalse("section checkbox should not be set", checkInput(INHERITING_INVESTIGATION_INFORMATION_FIELDNAME, "true"));
    }

    @Test
    @Rollback(true)
    public void testCheckboxStateRetained() {
        gotoPage(DOCUMENT_EDIT_URL);
        // check all the inheritance sections, save, then edit again and confirm
        // they are still checked
        for (String key : docValMap.keySet()) {
            setInput(key, docValMap.get(key));
        }
        submitForm();
        logger.trace(getPageText());
        webClient.getCache().clear();
        clickLinkWithText("edit");
        logger.trace(getPageText());
        for (String key : docValMap.keySet()) {
            String val = docValMap.get(key);
            assertTrue("element:" + key + " is set to:" + val, checkInput(key, val));
        }
    }

    @Test
    @Rollback(true)
    public void testProjectJson() {
        gotoPageWithoutErrorCheck("/project/" + PARENT_PROJECT_ID + "/json");
        String json = getPageCode();
        logger.debug("page json:" + json);
        assertTrue("json is not empty", json.length() > 0);
        JSONObject jsonObj = JSONObject.fromObject(json);
        if (jsonObj == null)
            fail("failed to parse json result");
        long actualId = jsonObj.getLong("id");
        assertEquals(PARENT_PROJECT_ID, actualId);
        testKeywords(PARENT_PROJECT_CULTURE_KEYWORDS, jsonObj, "cultureKeywords");

    }

    private void testKeywords(String[] keywordLabels, JSONObject projectJson, String field) {
        List<String> actualKeywords = new ArrayList<String>();
        List<String> expectedKeywords = Arrays.asList(keywordLabels);
        JSONArray arr = projectJson.getJSONArray(field);
        for (Object obj : arr) {
            JSONObject jsonObj = JSONObject.fromObject(obj);
            actualKeywords.add(jsonObj.getString("label"));
        }

        for (String expectedKeyword : keywordLabels) {
            if (!actualKeywords.contains(expectedKeyword)) {
                fail("expected keyword not found in results:" + expectedKeyword);
            }
        }

        for (String actualKeyword : actualKeywords) {
            if (!expectedKeywords.contains(actualKeyword)) {
                fail("result keyword not expected:" + actualKeyword);
            }
        }
    }

}
