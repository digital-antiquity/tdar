package org.tdar.web.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class BillingAccountSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {
    @Before
    public void setup() {
        reindexOnce();
    }

    @Test
    public void testAddRemoveAccount() {
        gotoPage("/billing/1/edit");
        assertFalse(getSource().toLowerCase().contains("kintigh") && getText().toLowerCase().contains("keith"));
        find(".addanother").click();
        WebElement last = find(".userAutoComplete").last();
        selectAutocompleteValue(last, "Kintigh", "Kintigh","person-6");
        submitForm();
        assertTrue(getText().contains("Kintigh"));
        gotoPage("/billing/1/edit");
        clearPageCache();
        logger.debug(getSource());
        assertTrue(getSource().toLowerCase().contains("kintigh") || getText().toLowerCase().contains("keith"));
        find(".repeat-row-delete").click();
        submitForm();
        assertFalse(getText().contains("Kintigh"));
        

    }

}
