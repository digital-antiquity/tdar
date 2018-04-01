package org.tdar.web.resource;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.tdar.web.AbstractAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CollectionApiWebITCase extends AbstractAuthenticatedWebTestCase {

    @Test
    public void testCreateNewCollection() {
        String url = "/api/collection/newcollection";
        Map<String, String> params = new HashMap<String, String>();

        params.put("collectionName", "Test Collection Name");
        Page page = createWebRequest(url, params, false);
        JSONObject response = getJsonResponse(page);
        assertEquals(response.getString("status"), "success");
    }

    @Test
    public void testAddResourceToManagedCollection() {
        String url = "/api/collection/addtocollection";
        Map<String, String> params = new HashMap<String, String>();

        params.put("resourceId", "3");
        params.put("collectionId", "1000");
        params.put("addAsManagedResource", "true");
        Page page = createWebRequest(url, params, false);
        JSONObject response = getJsonResponse(page);
        assertEquals(response.getString("status"), "success");
    }

    @Test
    public void testAddResourceToUnmanagedCollection() {
        String url = "/api/collection/addtocollection";
        Map<String, String> params = new HashMap<String, String>();

        params.put("resourceId", "1");
        params.put("collectionId", "1000");
        params.put("type", "UNMANAGED");
        Page page = createWebRequest(url, params, false);
        JSONObject response = getJsonResponse(page);
        assertEquals(response.getString("status"), "success");
    }

    @Test
    public void testRemoveResourceFromManagedCollection() {
        String url = "/api/collection/removefromcollection";
        Map<String, String> params = new HashMap<String, String>();
        params.put("resourceId", "3");
        params.put("collectionId", "1000");
        params.put("type", "MANAGED");

        Page page = createWebRequest(url, params, false);
        assertEquals(page.getWebResponse().getStatusCode(), HttpStatus.OK.value());
    }

    @Test
    public void testRemoveResourceFromUnmanagedCollection() {
        String url = "/api/collection/removefromcollection";
        Map<String, String> params = new HashMap<String, String>();
        params.put("resourceId", "5000");
        params.put("collectionId", "1000");
        params.put("type", "UNMANAGED");

        Page page = createWebRequest(url, params, false);
        assertEquals(page.getWebResponse().getStatusCode(), HttpStatus.OK.value());
    }

    @Test
    public void testListCollectionsForResource() {
        String url = "/api/collection/resourcecollections";
        Map<String, String> params = new HashMap<String, String>();
        params.put("resourceId", "42940");

        Page page = createWebRequest(url, params, false);
        assertEquals(page.getWebResponse().getStatusCode(), HttpStatus.OK.value());
    }

    private Page createWebRequest(String path, Map<String, String> params, boolean assertNoErrors) {
        int code = 0;
        WebClient client = getWebClient();
        String url = getBaseSecureUrl() + path;
        Page page = null;

        logger.debug("Request URL is {}", path);

        try {
            WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
            List<NameValuePair> parmsList = new ArrayList<NameValuePair>();

            for (String key : params.keySet()) {
                parmsList.add(nameValuePair(key, params.get(key)));
            }

            webRequest.setRequestParameters(parmsList);
            webRequest.setEncodingType(FormEncodingType.MULTIPART);
            page = client.getPage(webRequest);
            code = page.getWebResponse().getStatusCode();

            logger.debug("errors: {} ; code: {} ; content: {}", assertNoErrors, code,
                    page.getWebResponse().getContentAsString());

        } catch (MalformedURLException e) {
            Assert.fail("mailformed URL: are you sure you specified the right page in your test?");
        } catch (IOException iox) {
            Assert.fail("IO exception occured during test");
        } catch (FailingHttpStatusCodeException httpEx) {
            if (assertNoErrors) {
                Assert.fail("Failed HTTP requested: " + httpEx.getStatusCode());
            } else {
                logger.debug("The HTTP request failed: {} ({})", httpEx.getMessage(), httpEx);
            }
        }

        return page;
    }

    protected JSONObject getJsonResponse(Page page) {
        JSONObject json = toJson(page.getWebResponse().getContentAsString());
        return json;
    }

    protected void assertFileSizes(Page page, List<File> files) {
        JSONObject json = getJsonResponse(page);
        JSONArray jsonArray = json.getJSONArray("files");

        logger.info("{}", jsonArray);
        for (int i = 0; i < files.size(); i++) {
            Assert.assertEquals("file size reported from server should be same as original", files.get(i).length(),
                    jsonArray.getJSONObject(i).getLong("size"));
        }
    }
}
