package org.tdar.web;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class LookupWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private void assertJsonResult(String url, String resultField, int minResults) {
        int status = gotoPageWithoutErrorCheck(url);
        Assert.assertEquals("http result should be 200 OK", HttpServletResponse.SC_OK, status);
        String json = getPageCode();
        logger.trace("json is:{}", json);
        JSONObject jso = JSONObject.fromObject(json);
        Assert.assertNotNull("expecting parseable json", jso);
        JSONArray jarr = jso.getJSONArray(resultField);
        Assert.assertTrue(resultField + " array list should not be empty", JSONArray.toCollection(jarr).size() >= minResults);
    }

    private void assertJsonResult(String url, String resultField) {
        assertJsonResult(url, resultField, 1);
    }

    private static int indexCount = 0;

    @Before
    public void lookupPrep() {
        if (indexCount++ < 1) {
            reindex();
        }
    }

    @Test
    // TODO: These tests should assert more than just valid json and result counts.
    public void testValidPersonLookup() {
        assertJsonResult("/api/lookup/person?minLookupLength=0", "people");
    }

    @Test
    public void testValidInstitutionLookup() {
        assertJsonResult("/api/lookup/institution?minLookupLength=0", "institutions");
        logger.debug(getPageCode());
    }

    @Test
    public void testValidResourceLookup() {
        assertJsonResult("/api/lookup/resource?minLookupLength=0", "resources");
    }

    @Test
    public void testValidKeywordLookup() {
        assertJsonResult("/api/lookup/keyword?minLookupLength=0&keywordType=TemporalKeyword", "items");
    }

    @Test
    public void testValidCollectionLookup() {
        // TODO: put some annotation keys in the test dataset or have this create some resources w/ resourceAnnotations
        assertJsonResult("/api/lookup/collection?minLookupLength=0", "collections", 0);
    }

    @Test
    public void testValidAnnotationKeyLookup() {
        // TODO: put some annotation keys in the test dataset or have this create some resources w/ resourceAnnotations
        assertJsonResult("/api/lookup/annotationkey?minLookupLength=0", "items", 0);

    }

}
