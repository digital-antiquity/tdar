package org.tdar.web.functional;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.*;

public class InheritenceSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {
    @Test
    //create a project, fill out a couple inheritable sections, then inherit
    public void testBasicInheritance() throws InterruptedException {
        gotoPage("/project/add");
        String projectName = "project abc";
        String description = "project abc description";

        find("#resourceRegistrationTitle").val(projectName);
        find("#resourceDescription").val(description);

        //set some map bounds
        //find("#viewCoordinatesCheckbox").click();
        //hack: programmatic click doesn't set elem.checked before firing handler.  need to click label instead.
        find("[for=viewCoordinatesCheckbox]").click();
        Thread.sleep(1000); //wait for coordinates to appear.
        find("#d_maxy").val("36°33′45″N");
        find("#d_maxx").val("095°58′36″W");
        find("#d_miny").val("32°03′50″N");
        find("#d_minx").val("101°07′05″W");
        find("#locate").click();

        //add a keyword
        find("#metadataForm_otherKeywords_0_").val("foobar");

        submitForm();

        //now create a document and inherit everything.
        gotoPage("/document/add");
        logger.debug("expecting to be on document add page: {}", getDriver().getCurrentUrl());
        find("#projectId").toSelect().selectByVisibleText(projectName);

        //inherit everything
        WebDriverWait wait = new WebDriverWait(driver,5);
        WebElement cb = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#cbSelectAllInheritance")));
        cb.click();

        //okay, now inherit nothing
        cb.click();

        //check some of the fields to see if we populated the page with project information
        find("#viewCoordinatesCheckbox").click();
        Thread.sleep(1000); //wait for coordinates to appear.
        assertTrue("geo bounds should be set", StringUtils.isNotBlank(find("#d_maxy").val()));
        assertTrue("other keywords should be set", StringUtils.isNotBlank(find("#metadataForm_otherKeywords_0_").val()));
    }


}
