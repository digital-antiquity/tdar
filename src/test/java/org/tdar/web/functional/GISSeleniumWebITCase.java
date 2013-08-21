/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.functional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.Language;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractWebTestCase;

public class GISSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {
    public static HashMap<String, String> docValMap;
    public static HashMap<String, List<String>> docMultiValMap = new LinkedHashMap<String, List<String>>();
    public static HashMap<String, List<String>> docMultiValMapLab = new LinkedHashMap<String, List<String>>();
    // we will assert the presence of these values, but we don't care what order they appear
    public static Map<String, String> docUnorderdValMap = new HashMap<String, String>();
    public static List<String> alternateTextLookup = new ArrayList<String>();
    public static List<String> alternateCodeLookup = new ArrayList<String>();

    public static String REGEX_DATASET_VIEW = ".+\\/geospatial\\/\\d+$";
    public static Pattern PATTERN_DOCUMENT_VIEW = Pattern.compile(REGEX_DATASET_VIEW);
    public static String REGEX_DATASET_EDIT = ".+\\/geospatial\\/\\d+$";
    public static String REGEX_RESOURCE_SAVE = ".+save.action$";
    public static String REGEX_DATASET_COLUMNS = ".+\\/geospatial\\/\\d+/columns$";
    public static Pattern PATTERN_DOCUMENT_EDIT = Pattern.compile(REGEX_DATASET_EDIT);

    public GISSeleniumWebITCase() {
        docValMap = new LinkedHashMap<String, String>();
        // removing inline implementation of HashMap to remove serialization warning
        alternateTextLookup.add(AbstractWebTestCase.RESTRICTED_ACCESS_TEXT);

        docValMap.put("projectId", "-1");
        docValMap.put("geospatial.title", "My Sample Geospatial");
        docValMap.put("authorshipProxies[0].person.firstName", "Jim");
        docValMap.put("authorshipProxies[0].person.lastName", "deVos");
        docValMap.put("authorshipProxies[0].person.email", "testabc123@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "A wholly new institution name");
        docValMap.put("authorshipProxies[0].person.id", "");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.CREATOR.name());
        alternateTextLookup.add(ResourceCreatorRole.CREATOR.getLabel());
        docValMap.put("geospatial.description", "A resource description");
        docValMap.put("geospatial.date", "1923");
        docValMap.put("geospatial.spatialReferenceSystem", "WGS:84");
        docValMap.put("geospatial.projection", "mercator");
        docValMap.put("geospatial.scale", "1:100000");
        docValMap.put("geospatial.currentnessUpdateNotes", "very current; updated daily");
        docValMap.put("geospatial.doi", "doi:10.1016/j.iheduc.2003.11.004");
        alternateTextLookup.add(Language.SPANISH.getLabel());
        docValMap.put("resourceLanguage", Language.SPANISH.name());
        docValMap.put("geospatial.url", "http://www.tdar.org");
    }

    private void prepIndexedFields() {
        prepIndexedFields(docValMap.keySet());
        prepIndexedFields(docMultiValMap.keySet());
        prepIndexedFields(docUnorderdValMap.keySet());
    }

    @Test
    public void testUploadShapefile() {
        gotoPage("/geospatial/add");
        WebElement form = find("#metadataForm").first();
        prepIndexedFields();
        // fill in various text fields
        for (Map.Entry<String, String> entry : docValMap.entrySet()) {
            find(By.name(entry.getKey())).val(entry.getValue());
        }

        // check various keyword checkboxes
        for (String key : docMultiValMap.keySet()) {
            for (String val : docMultiValMap.get(key)) {
                find(By.name(key)).val(val);
            }
        }

        FileAccessRestriction restriction = FileAccessRestriction.PUBLIC;
        File dir = new File(TestConstants.TEST_SHAPEFILE_DIR);
        for (File file : dir.listFiles()) {
            uploadFileAsync(restriction, file);
        }
        submitForm();

        String path = getDriver().getCurrentUrl();

        path = getDriver().getCurrentUrl();
        logger.info(find("body").getText());
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + find("body").getText(), path.matches(REGEX_DATASET_COLUMNS));
        logger.trace(find("body").getText());
        submitForm();
        assertTrue("should be on view page", getCurrentUrl().matches(REGEX_DATASET_VIEW));
        assertFalse("no errors present", getText().toLowerCase().contains("error"));
        assertFalse("no errors present", getText().toLowerCase().contains("exception"));
    }



    @Test
    public void testUploadGeotiff() {
        gotoPage("/geospatial/add");
        WebElement form = find("#metadataForm").first();
        prepIndexedFields();
        // fill in various text fields
        for (Map.Entry<String, String> entry : docValMap.entrySet()) {
            find(By.name(entry.getKey())).val(entry.getValue());
        }

        // check various keyword checkboxes
        for (String key : docMultiValMap.keySet()) {
            for (String val : docMultiValMap.get(key)) {
                find(By.name(key)).val(val);
            }
        }

        FileAccessRestriction restriction = FileAccessRestriction.PUBLIC;
        uploadFileAsync(restriction, new File(TestConstants.TEST_GEOTIFF));
        uploadFileAsync(restriction, new File(TestConstants.TEST_GEOTIFF_TFW));
        submitForm();

        String path = getDriver().getCurrentUrl();

        path = getDriver().getCurrentUrl();
        logger.info(find("body").getText());
        assertFalse("expecting to be on view page. Actual path:" + path + "\n" + find("body").getText(), path.matches(REGEX_DATASET_COLUMNS));
        assertTrue("should be on view page", getCurrentUrl().matches(REGEX_DATASET_VIEW));
        assertFalse("no errors present", getText().toLowerCase().contains("error"));
        assertFalse("no errors present", getText().toLowerCase().contains("exception"));

        String url  =getCurrentUrl();
        logout();
        gotoPage(url);
        find(".media-body a").first().click();
        waitFor("#loginUsername");
        String username = TestConfiguration.getInstance().getAdminUsername();
        String password = TestConfiguration.getInstance().getAdminPassword();
        assertThat(username, not(isEmptyOrNullString()));
        assertThat(password, not(isEmptyOrNullString()));
        find("#loginUsername").val(username);
        find("#loginPassword").val(password);
        find("#btnLogin").click();
        assertTrue(getCurrentUrl().contains("confirm"));

    }

}
