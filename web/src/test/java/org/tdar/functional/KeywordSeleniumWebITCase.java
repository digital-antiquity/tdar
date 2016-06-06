package org.tdar.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.tdar.core.bean.RelationType;

public class KeywordSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {
    
    private static final String url = "/browse/temporal-keyword/78/basketmaker-iii";
    private static final String LABEL = "Basketmaker 3";
    private static final String DESCRIPTION = "test description";
    private static final String url1 = "http://www.tdar.org";
    private static final String url2 = "http://www.digitalantiquity.org";

    @Test
    public void testKeywordEdit() {
        gotoPage(url);
        find(By.className("toolbar-edit")).click();
        assertNotEquals("current page is not view page", url , getCurrentUrl());
        setFieldByName("label", LABEL);
        setFieldByName("description", DESCRIPTION);
        find(By.className("addanother")).click();
        setFieldByName("mappings[0].relation", url1);
        setFieldByName("mappings[0].relationType", RelationType.DCTERMS_PART_OF.name());
        setFieldByName("mappings[1].relation", url2);
        setFieldByName("mappings[1].relationType", RelationType.DCTERMS_IS_VERSION_OF.name());
        submitForm();
        assertTrue("looking for new label",getText().contains(LABEL));
        assertTrue("looking for description", getText().contains(DESCRIPTION));
        assertTrue("looking for first url", getText().contains(url1));
        assertTrue("looking for second url", getText().contains(url2));
        assertTrue("looking for part of",getText().contains(RelationType.DCTERMS_PART_OF.getTerm()));
        assertTrue("looking for version of",getText().contains(RelationType.DCTERMS_IS_VERSION_OF.getTerm()));
        find(By.className("toolbar-edit")).click();
        find(By.className("repeat-row-delete")).first().click();
        submitForm();
        assertFalse("looking for first url", getText().contains(url1));
        assertTrue("looking for second url", getText().contains(url2));
    
    }

}
