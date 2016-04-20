package org.tdar.functional;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Project;

public class InheritanceSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // FIXME: make this more generic (it was generated from selenium IDE)
    private void fillOUtProjectForm(WebDriver driver, Project project) {
        driver.findElement(By.id("resourceRegistrationTitle")).clear();
        driver.findElement(By.id("resourceRegistrationTitle")).sendKeys(project.getTitle());
        driver.findElement(By.id("resourceDescription")).clear();
        driver.findElement(By.id("resourceDescription")).sendKeys(project.getDescription());
        driver.findElement(By.id("metadataForm_resourceAnnotations_0__resourceAnnotationKey_key")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_0__resourceAnnotationKey_key")).sendKeys("annokey1");
        driver.findElement(By.id("metadataForm_resourceAnnotations_0__value")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_0__value")).sendKeys("annoval1");
        driver.findElement(By.id("divIdentifiersAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_resourceAnnotations_1__resourceAnnotationKey_key")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_1__resourceAnnotationKey_key")).sendKeys("annokey2");
        driver.findElement(By.id("metadataForm_resourceAnnotations_1__value")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_1__value")).sendKeys("annoval2");
        driver.findElement(By.id("divIdentifiersAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_resourceAnnotations_2__resourceAnnotationKey_key")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_2__resourceAnnotationKey_key")).sendKeys("annokey3");
        driver.findElement(By.id("metadataForm_resourceAnnotations_2__value")).clear();
        driver.findElement(By.id("metadataForm_resourceAnnotations_2__value")).sendKeys("annoval3");
        driver.findElement(By.id("metadataForm_geographicKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_geographicKeywords_0_")).sendKeys("geoterm1");
        driver.findElement(By.id("geographicKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_geographicKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_geographicKeywords_1_")).sendKeys("geoterm2");
        driver.findElement(By.id("geographicKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_geographicKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_geographicKeywords_2_")).sendKeys("geoterm3");
        driver.findElement(By.id("viewCoordinatesCheckbox")).click();
        driver.findElement(By.id("d_minx")).clear();
        driver.findElement(By.id("d_minx")).sendKeys("109°12′10″W");
        driver.findElement(By.id("d_maxy")).click();
        driver.findElement(By.id("d_maxy")).clear();
        driver.findElement(By.id("d_maxy")).sendKeys("44°26′00″N");
        driver.findElement(By.id("d_maxx")).click();
        driver.findElement(By.id("d_maxx")).clear();
        driver.findElement(By.id("d_maxx")).sendKeys("081°46′50″W");
        driver.findElement(By.id("d_miny")).clear();
        driver.findElement(By.id("d_miny")).sendKeys("30°42′40″N");
        driver.findElement(By.id("metadataForm_temporalKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_temporalKeywords_0_")).sendKeys("temporal1");
        driver.findElement(By.id("temporalKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_temporalKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_temporalKeywords_1_")).sendKeys("temporal2");
        driver.findElement(By.id("temporalKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_temporalKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_temporalKeywords_2_")).sendKeys("temporal3");
        new Select(driver.findElement(By.id("metadataForm_coverageDates_0__dateType"))).selectByVisibleText("Calendar Date");
        driver.findElement(By.id("metadataForm_coverageDates_0__startDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_0__startDate")).sendKeys("1900");
        driver.findElement(By.id("metadataForm_coverageDates_0__endDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_0__endDate")).sendKeys("1950");
        driver.findElement(By.id("metadataForm_coverageDates_0__description")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_0__description")).sendKeys("coverage date 1 - calendar");
        driver.findElement(By.id("coverageDateRepeatableAddAnotherButton")).click();
        new Select(driver.findElement(By.id("metadataForm_coverageDates_1__dateType"))).selectByVisibleText("Radiocarbon Date");
        driver.findElement(By.id("metadataForm_coverageDates_1__startDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_1__startDate")).sendKeys("1000");
        driver.findElement(By.id("metadataForm_coverageDates_1__endDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_1__endDate")).sendKeys("500");
        driver.findElement(By.id("metadataForm_coverageDates_1__description")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_1__description")).sendKeys("coverage date 2 - radiocarbon");
        driver.findElement(By.id("coverageDateRepeatableAddAnotherButton")).click();
        new Select(driver.findElement(By.id("metadataForm_coverageDates_2__dateType"))).selectByVisibleText("Calendar Date");
        driver.findElement(By.id("metadataForm_coverageDates_2__startDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_2__startDate")).sendKeys("1974");
        driver.findElement(By.id("metadataForm_coverageDates_2__endDate")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_2__endDate")).sendKeys("2001");
        driver.findElement(By.id("metadataForm_coverageDates_2__description")).clear();
        driver.findElement(By.id("metadataForm_coverageDates_2__description")).sendKeys("coverage date 3 - calendar");
        driver.findElement(By.id("metadataForm_investigationTypeIds1")).click();
        driver.findElement(By.id("metadataForm_investigationTypeIds9")).click();
        driver.findElement(By.id("metadataForm_investigationTypeIds20")).click();
        driver.findElement(By.id("metadataForm_approvedMaterialKeywordIds1")).click();
        driver.findElement(By.id("metadataForm_approvedMaterialKeywordIds8")).click();
        driver.findElement(By.id("metadataForm_approvedMaterialKeywordIds15")).click();
        driver.findElement(By.id("approvedCultureKeywordIds-1")).click();
        driver.findElement(By.id("approvedCultureKeywordIds-16")).click();
        driver.findElement(By.id("approvedCultureKeywordIds-22")).click();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_0_")).sendKeys("cultkeyword1");
        driver.findElement(By.id("uncontrolledCultureKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_1_")).sendKeys("cultkeyword2");
        driver.findElement(By.id("uncontrolledCultureKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledCultureKeywords_2_")).sendKeys("cultkeyword3");
        driver.findElement(By.id("metadataForm_siteNameKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_siteNameKeywords_0_")).sendKeys("sitename1");
        driver.findElement(By.id("siteNameKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_siteNameKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_siteNameKeywords_1_")).sendKeys("sitename2");
        driver.findElement(By.id("siteNameKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_siteNameKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_siteNameKeywords_2_")).sendKeys("sitename3");
        driver.findElement(By.id("approvedSiteTypeKeywordIds-1")).click();
        driver.findElement(By.id("approvedSiteTypeKeywordIds-59")).click();
        driver.findElement(By.id("approvedSiteTypeKeywordIds-115")).click();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_0_")).sendKeys("sitetype1");
        driver.findElement(By.id("uncontrolledSiteTypeKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_1_")).sendKeys("sitetype2");
        driver.findElement(By.id("uncontrolledSiteTypeKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_uncontrolledSiteTypeKeywords_2_")).sendKeys("sitetype3");
        driver.findElement(By.id("metadataForm_otherKeywords_0_")).click();
        driver.findElement(By.id("metadataForm_otherKeywords_0_")).clear();
        driver.findElement(By.id("metadataForm_otherKeywords_0_")).sendKeys("otherkeyword1");
        driver.findElement(By.id("otherKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_otherKeywords_1_")).clear();
        driver.findElement(By.id("metadataForm_otherKeywords_1_")).sendKeys("otherkeyword2");
        driver.findElement(By.id("otherKeywordsRepeatableAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_otherKeywords_2_")).clear();
        driver.findElement(By.id("metadataForm_otherKeywords_2_")).sendKeys("otherkeyword3");
        driver.findElement(By.cssSelector("#resourceNoteSectionGlide > h2")).click();
        new Select(driver.findElement(By.id("metadataForm_resourceNotes_0__type"))).selectByVisibleText("Redaction Note");
        new Select(driver.findElement(By.id("metadataForm_resourceNotes_0__type"))).selectByVisibleText("General Note");
        driver.findElement(By.id("metadataForm_resourceNotes_0__note")).clear();
        driver.findElement(By.id("metadataForm_resourceNotes_0__note")).sendKeys("note 1 - general");
        driver.findElement(By.id("resourceNoteSectionAddAnotherButton")).click();
        new Select(driver.findElement(By.id("metadataForm_resourceNotes_1__type"))).selectByVisibleText("Redaction Note");
        driver.findElement(By.id("metadataForm_resourceNotes_1__note")).clear();
        driver.findElement(By.id("metadataForm_resourceNotes_1__note")).sendKeys("note 2 - redaction");
        driver.findElement(By.id("resourceNoteSectionAddAnotherButton")).click();
        new Select(driver.findElement(By.id("metadataForm_resourceNotes_2__type"))).selectByVisibleText("Rights & Attribution");
        driver.findElement(By.id("metadataForm_resourceNotes_2__note")).clear();
        driver.findElement(By.id("metadataForm_resourceNotes_2__note")).sendKeys("note 3 rights");
        driver.findElement(By.id("resourceNoteSectionAddAnotherButton")).click();
        new Select(driver.findElement(By.id("metadataForm_resourceNotes_3__type"))).selectByVisibleText("Administration Note");
        driver.findElement(By.id("metadataForm_resourceNotes_3__note")).clear();
        driver.findElement(By.id("metadataForm_resourceNotes_3__note")).sendKeys("note 4 administration");
        driver.findElement(By.id("metadataForm_sourceCollections_0__text")).clear();
        driver.findElement(By.id("metadataForm_sourceCollections_0__text")).sendKeys("sourcecol1");
        driver.findElement(By.id("divSourceCollectionControlAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_sourceCollections_1__text")).clear();
        driver.findElement(By.id("metadataForm_sourceCollections_1__text")).sendKeys("sourcecol2");
        driver.findElement(By.id("divSourceCollectionControlAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_sourceCollections_2__text")).clear();
        driver.findElement(By.id("metadataForm_sourceCollections_2__text")).sendKeys("sourcecol3");
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_0__text")).clear();
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_0__text")).sendKeys("relcoll1");
        driver.findElement(By.id("divRelatedComparativeCitationControlAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_1__text")).clear();
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_1__text")).sendKeys("relcoll2");
        driver.findElement(By.id("divRelatedComparativeCitationControlAddAnotherButton")).click();
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_2__text")).clear();
        driver.findElement(By.id("metadataForm_relatedComparativeCollections_2__text")).sendKeys("relcoll3");
    }

    @Test
    // create a project, fill out a couple inheritable sections, then inherit
    public void testBasicInheritance() throws InterruptedException {
        // ignore misc javascript errors (gmaps, et. al), our asserts will break if relevant javascript had problems
        setIgnoreJavascriptErrors(true);
        gotoPage("/project/add");

        Project project = new Project();
        project.setTitle("project abc");
        project.setDescription("project abc description");
        gotoPage("/project/add");
        fillOUtProjectForm(getDriver(), project);

        submitForm();

        // now create a document and inherit everything.
        gotoPage("/document/add");
        logger.debug("expecting to be on document add page: {}", getDriver().getCurrentUrl());
        find("#projectId").toSelect().selectByVisibleText(project.getTitle());

        // inherit everything
        WebDriverWait wait = new WebDriverWait(getDriver(), 5);
        WebElement cb = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#cbSelectAllInheritance")));
        cb.click();

        // okay, now inherit nothing
        cb.click();

        // check some of the fields to see if we populated the page with project information
        find("#viewCoordinatesCheckbox").click();
        Thread.sleep(1000); // wait for coordinates to appear.
        assertTrue("geo bounds should be set", StringUtils.isNotBlank(find("#d_maxy").val()));
        takeScreenshot("before checking other keyword");
        assertTrue("other keywords should be set", StringUtils.isNotBlank(find("#metadataForm_otherKeywords_0_").val()));
    }

    @Test
    public void testInheritCreditSection() throws InterruptedException {
        gotoPage("/document/add");
        find("#resourceRegistrationTitle").val("my fancy document");
        find("#resourceDescription").val("this test took me 8 hours to write. a lot of trial and error was involved.");
        find("#dateCreated").val("2012");
        find("#projectId").val("3805");
        find("#cbInheritingCreditRoles").click();
        // this project should have about four contributors.
        waitFor("#creditTable > :nth-child(4)");
        find("#submitButton").click();
    }

    @After
    public void turnIgnoresOff() {
        // jtd: I don't think this is necessary - you get a new test class instance for each test
        setIgnoreJavascriptErrors(false);
    }

    @Override
    public void login() {
        setScreenshotsAllowed(false);
        // reindexOnce();
        loginAdmin();
        setIgnoreModals(false);
        setScreenshotsAllowed(true);
    }
}
