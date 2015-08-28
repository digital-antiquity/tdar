package org.tdar.functional;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ArchiveFileUploadSeleniumITCase extends AbstractBasicSeleniumWebITCase {

    private final List<String> REQUIRED_FIELDS = java.util.Arrays.asList(new String[] {
            "resourceRegistrationTitle", "dateCreated", "resourceDescription", "projectId", "resourceRegistrationTitle", "dateCreated",
            "resourceDescription", "projectId" });

    @Test
    public void requiredFieldsAreRequested() {
        gotoPage("/archive/add");
        submitForm();
        assertThat(getDriver().getCurrentUrl(), endsWith("/archive/add"));
        List<WebElement> inputs = getDriver().findElements(By.className("error"));
        List<String> foundElementsFor = new ArrayList<>();
        for (WebElement input : inputs) {
            assertTrue(input.isDisplayed());
            foundElementsFor.add(input.getAttribute("for"));
        }
        assertTrue(foundElementsFor.containsAll(REQUIRED_FIELDS));
    }

}
