package org.tdar.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.WebElement;

public class BillingAccountSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private static final String HATCH_PROP = "Hatch";
    private static final String HATCH = "hatch";

    @Test
    public void testAddRemoveAccount() {
        gotoPage("/billing/1/edit");
        assertFalse(getSource().toLowerCase().contains(HATCH) && getText().toLowerCase().contains(HATCH));
        find(".addanother").click();
        WebElement last = find(".userAutoComplete").last();
        selectAutocompleteValue(last, HATCH_PROP, HATCH_PROP, "person-38");
        submitForm();
        assertTrue(getText().contains(HATCH_PROP));
        gotoPage("/billing/1/edit");
        clearPageCache();
        assertTrue(getSource().toLowerCase().contains(HATCH) || getText().toLowerCase().contains(HATCH));
        find(".repeat-row-delete").click();
        submitForm();
        clearPageCache();
        logger.debug(getText());
        assertFalse(getText().contains(HATCH_PROP));
    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}
