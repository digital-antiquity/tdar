/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.functional.resource;

import static org.junit.Assert.assertFalse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Language;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.functional.AbstractBasicSeleniumWebITCase;
import org.tdar.functional.util.WebElementSelection;
import org.tdar.web.AbstractWebTestCase;

public class DatasetSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public HashMap<String, String> docValMap;
    public HashMap<String, List<String>> docMultiValMap = new LinkedHashMap<String, List<String>>();
    public HashMap<String, List<String>> docMultiValMapLab = new LinkedHashMap<String, List<String>>();
    // we will assert the presence of these values, but we don't care what order they appear
    public Map<String, String> docUnorderdValMap = new HashMap<String, String>();
    public List<String> alternateTextLookup = new ArrayList<String>();
    public List<String> alternateCodeLookup = new ArrayList<String>();

    public static String REGEX_DATASET_VIEW = ".+\\/dataset\\/\\d+\\/(.+)$";
    public static Pattern PATTERN_DOCUMENT_VIEW = Pattern.compile(REGEX_DATASET_VIEW);
    public static String REGEX_DATASET_EDIT = ".+\\/dataset\\/\\d+$";
    public static String REGEX_RESOURCE_SAVE = ".+save$";
    public static String REGEX_DATASET_COLUMNS = ".+\\/dataset\\/columns/\\d+$";
    public static Pattern PATTERN_DOCUMENT_EDIT = Pattern.compile(REGEX_DATASET_EDIT);

    public DatasetSeleniumWebITCase() {
        docValMap = new LinkedHashMap<String, String>();
        // removing inline implementation of HashMap to remove serialization warning
        alternateTextLookup.add(AbstractWebTestCase.RESTRICTED_ACCESS_TEXT);

        docValMap.put("projectId", "-1");
        docValMap.put("dataset.title", "My Sample Dataset");
        docValMap.put("authorshipProxies[0].person.firstName", "Jim");
        docValMap.put("authorshipProxies[0].person.lastName", "deVos");
        docValMap.put("authorshipProxies[0].person.email", "testabc123@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "A wholly new institution name");
        docValMap.put("authorshipProxies[0].person.id", "");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.CREATOR.name());
        alternateTextLookup.add(ResourceCreatorRole.CREATOR.getLabel());
        docValMap.put("dataset.description", "A resource description");
        docValMap.put("dataset.date", "1923");
        docValMap.put("dataset.doi", "10.1016/j.iheduc.2003.11.004");
        alternateTextLookup.add(Language.SPANISH.getLabel());
        docValMap.put("resourceLanguage", Language.SPANISH.name());
        docValMap.put("dataset.url", "http://www.tdar.org");
        docValMap.put("publisherName", "test");
    }

    private void prepIndexedFields() {
        prepIndexedFields(docValMap.keySet());
        prepIndexedFields(docMultiValMap.keySet());
        prepIndexedFields(docUnorderdValMap.keySet());
    }

    @SuppressWarnings("unused")
    @Test
    public void testCreateDatasetEditSavehasResource() {
        gotoPage("/dataset/add");
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
        File uploadFile = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "too_many_columns.tab");

        uploadFileAsync(restriction, uploadFile);

        submitForm();

        String path = getDriver().getCurrentUrl();
        logger.trace(find("body").getText());
        assertTrue("expecting to be on edit page. Actual path:" + path + "\n" + find("body").getText(), path.matches(REGEX_RESOURCE_SAVE));

        // assertEquals("count of edit buttons", 1, find(By.partialLinkText("EDIT")).size());
        // find(By.partialLinkText("EDIT")).click();

        WebElementSelection sel = find(".replace-file");
        File replaceFile = new File(TestConstants.TEST_DATA_INTEGRATION_DIR, "tab_mapping_dataset.tab");
        sel.sendKeys(replaceFile.getAbsolutePath());
        waitFor(".undo-replace-button");

        submitForm();

        path = getDriver().getCurrentUrl();
        logger.trace(find("body").getText());
        assertTrue("expecting to be on view page. Actual path:" + path + "\n" + find("body").getText(), path.matches(REGEX_DATASET_COLUMNS));
        logger.trace(find("body").getText());
        submitForm("#submitButton");
        assertTrue("should be on view page", getCurrentUrl().matches(REGEX_DATASET_VIEW));
        logger.debug(getText());
        assertFalse("no errors present", getText().toLowerCase().contains("exception"));
        // assertFalse("no errors present", getText().toLowerCase().contains("error"));
        // doesn't work because -- Error setting expression 'submitAction' may occur

    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}
