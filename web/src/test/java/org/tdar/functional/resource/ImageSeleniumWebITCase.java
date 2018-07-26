/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.functional.resource;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.functional.AbstractBasicSeleniumWebITCase;
import org.tdar.junit.RunWithTdarConfiguration;
import org.tdar.web.AbstractWebTestCase;

@RunWith(MultipleWebTdarConfigurationRunner.class)
public class ImageSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public HashMap<String, String> docValMap;
    public HashMap<String, List<String>> docMultiValMap = new LinkedHashMap<String, List<String>>();
    public HashMap<String, List<String>> docMultiValMapLab = new LinkedHashMap<String, List<String>>();
    // we will assert the presence of these values, but we don't care what order they appear
    public Map<String, String> docUnorderdValMap = new HashMap<String, String>();
    public List<String> alternateTextLookup = new ArrayList<String>();
    public List<String> alternateCodeLookup = new ArrayList<String>();

    public static String REGEX_DATASET_VIEW = ".+\\/image\\/\\d+\\/(.+)$";
    public static Pattern PATTERN_DOCUMENT_VIEW = Pattern.compile(REGEX_DATASET_VIEW);
    public static String REGEX_DATASET_EDIT = ".+\\/image\\/\\d+$";
    public static String REGEX_RESOURCE_SAVE = ".+save$";
    public static Pattern PATTERN_DOCUMENT_EDIT = Pattern.compile(REGEX_DATASET_EDIT);

    public ImageSeleniumWebITCase() {
        docValMap = new LinkedHashMap<String, String>();
        // removing inline implementation of HashMap to remove serialization warning
        alternateTextLookup.add(AbstractWebTestCase.RESTRICTED_ACCESS_TEXT);

        docValMap.put("projectId", "-1");
        docValMap.put("image.title", "My Sample image");
        docValMap.put("authorshipProxies[0].person.firstName", "Jim");
        docValMap.put("authorshipProxies[0].person.lastName", "deVos");
        docValMap.put("authorshipProxies[0].person.email", "testabc123@test.com");
        docValMap.put("authorshipProxies[0].person.institution.name", "A wholly new institution name");
        docValMap.put("authorshipProxies[0].person.id", "");
        docValMap.put("authorshipProxies[0].role", ResourceCreatorRole.CREATOR.name());
        alternateTextLookup.add(ResourceCreatorRole.CREATOR.getLabel());
        docValMap.put("image.description", "A resource description");
        docValMap.put("image.date", "1923");
        docValMap.put("image.doi", "10.1016/j.iheduc.2003.11.004");
        docValMap.put("image.url", "http://www.tdar.org");
        docValMap.put("publisherName", "test");

        docMultiValMap.put("geographicKeywords", asList("Georgia"));
        docMultiValMap.put("temporalKeywords", asList("before time"));
        docMultiValMap.put("uncontrolledMaterialKeywords", asList("bread", "apples", "very small rocks", "a duck"));
        docMultiValMap.put("uncontrolledCultureKeywords", asList("German"));
        docMultiValMap.put("siteNameKeywords", asList("sandy"));
        docMultiValMap.put("uncontrolledSiteTypeKeywords", asList("uncontrolled"));
        docMultiValMap.put("otherKeywords", asList("other"));
    }

    private void prepIndexedFields() {
        prepIndexedFields(docValMap.keySet());
        prepIndexedFields(docMultiValMap.keySet());
        prepIndexedFields(docUnorderdValMap.keySet());
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.SELECT2 })
    @Ignore
    public void testCreateImageEditSavehasResource() {
        gotoPage("/image/add");
        WebElement form = find("#metadataForm").first();
        expandAllTreeviews();
        prepIndexedFields();
        // uploadFileAsync(FileAccessRestriction.PUBLIC, TestConstants.getFile(TestConstants.TEST_IMAGE));
        // fill in various text fields
        for (Map.Entry<String, String> entry : docValMap.entrySet()) {
            logger.debug("{}", entry);
            find(By.name(entry.getKey())).val(entry.getValue());
        }

        // fill in uncontrolled keywords
        for (String key : docMultiValMap.keySet()) {
            select2val(find(By.name(key)), docMultiValMap.get(key));
        }
        submitForm();

        String path = getDriver().getCurrentUrl();
        logger.trace(find("body").getText());
        assertTrue("should be on view page", getCurrentUrl().matches(REGEX_DATASET_VIEW));
        logger.debug(getText());
        assertFalse("no errors present", getText().toLowerCase().contains("exception"));
    }

    @Test
    public void testKeywordsRemoved() {
        testCreateImageEditSavehasResource();
        find(".toolbar-edit").click();
        waitForPageload();
        select2Clear(find("#uncontrolledMaterialKeywordsRepeatable"));
        assertThat(getText(), not(containsString("very small rocks")));
    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}
