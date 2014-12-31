package org.tdar.web.functional;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.tdar.web.functional.util.ByLabelText;
import org.tdar.web.functional.util.WebElementSelection;


public class IntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String SPITAL_DT_ID = "3091";
    private static final String ALEX_DT_ID = "3104";
    private static final String SPITALFIELD_CHECKBOX = "cbResult" + SPITAL_DT_ID;
    private static final String ALEXANDRIA_CHECKBOX = "cbResult" + ALEX_DT_ID;

    By aves = By.id("cbont_64870");
    By rabbit = By.id("cbont_63000");
    By sheep = By.id("cbont_62580");

    @Before
    public void setupIntegration() {
        login();
        find(By.partialLinkText("Integrate")).click();
        Assert.assertTrue(getText().contains("Data Integration"));
        Assert.assertTrue(getCurrentUrl().contains("/workspace/list"));
        find(By.linkText("Start Now")).click();
        Assert.assertTrue(getCurrentUrl().contains("/workspace/integrate"));
    }
    
    @Test
    public void testInvalidIntegrate() throws InterruptedException {
        // add three datasets that don't work, remove one, assert that we get back to an integratable state
        
        setFieldByName("integration.title", "test integration");
        waitFor(ExpectedConditions.elementToBeClickable(By.id("btnSave")));
        // assert save enabled
        openDatasetsModal();
        // uncheck
        find(By.name("searchFilter.integrationCompatible")).click();
        // note that IDs are dataTable ids
        findAndClickDataset("Knowth", "cbResult3089");
        // re-check
        find(By.name("searchFilter.integrationCompatible")).click();
        // note that IDs are dataTable ids
        findAndClickDataset("spitalf", SPITALFIELD_CHECKBOX);
        // note that IDs are dataTable ids
        findAndClickDataset("alexandria", ALEXANDRIA_CHECKBOX);
        // add selected items
        find(By.className("btn-primary")).click();
        waitFor(4);
        Assert.assertEquals(0, find(By.className("sharedOntologies")).size());
        
        removeDatasetByPartialName("Knowth");
        Assert.assertEquals(2, find(By.className("sharedOntologies")).size());
        assertFalse(getText().contains("Knowth"));
        waitFor(ExpectedConditions.elementToBeClickable(By.linkText("Add Integration Column")));
    }

    
    @Test
    public void testValidIntegrate() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integratable state
        
        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(By.className("sharedOntologies")).size());

        find(By.id("btnAddDisplayColumn")).click();
        By tab1 = By.id("tab0");
        waitFor(tab1).isDisplayed();
        // dt_ + tabid + _ + data_table_id
        By spital_select = By.id("dt_0_" + SPITAL_DT_ID);
        chooseSelectByName("Site Code", spital_select);
        By alex_select = By.id("dt_0_" + ALEX_DT_ID);
        chooseSelectByName("LOCATION", alex_select);
        takeScreenshot();
        find(By.linkText("Add Integration Column")).click();
        find(By.linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(By.id("tabtab1")).isDisplayed();
        // wait for tab contents is visible
        waitFor(By.id("tab1")).isDisplayed();
        logger.debug(getText());
        find(ByLabelText.byLabelText("Aves")).click();// this is really slow, so do it once
        assertTrue(ExpectedConditions.elementSelectionStateToBe(By.id("cbont_64870"), true).apply(getDriver()).booleanValue());

        find(rabbit).click();
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        find(sheep).click();

        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());
        find(By.id("btnIntegrate")).click();
        waitForPageload();
        takeScreenshot();
        clearPageCache();
        logger.debug(getText());
        assertTrue(getText().contains("Summary of Integration Results"));
        assertTrue(getText().contains("Fauna Taxon"));
        assertTrue(getText().contains("Aves"));
        assertTrue(getText().toLowerCase().contains("sheep"));
        assertTrue(getText().toLowerCase().contains("rabbit"));
        assertTrue(getText().contains("Spitalfields"));
        assertTrue(getText().contains("Alexandria"));
    }

    
    @Test
    public void testIntegrateRetainCheckboxOnClearAll() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integratable state
        
        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(By.className("sharedOntologies")).size());

        // add integration column with a few check boxes
        takeScreenshot();
        find(By.linkText("Add Integration Column")).click();
        find(By.linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(By.id("tabtab0")).isDisplayed();
        // wait for tab contents is visible
        waitFor(By.id("tab0")).isDisplayed();
        find(aves).click();
        find(rabbit).click();
        find(sheep).click();

        assertTrue(ExpectedConditions.elementSelectionStateToBe(aves, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());

        // remove the datasets
        removeDatasetByPartialName("Spital");
        removeDatasetByPartialName("Alexandria");
        
//        assertFalse(find(By.id("btnIntegrate")).isEnabled());

        // add one back
        openDatasetsModal();
        findAndClickDataset("spitalf", SPITALFIELD_CHECKBOX);
        find(By.id("btnModalAdd")).click();
        waitFor(4);

        // make sure the checkboxes are still there
        assertTrue(ExpectedConditions.elementSelectionStateToBe(aves, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());
    }

    private void setupSpitalfieldsAlexandriaForTest() throws InterruptedException {
        setFieldByName("integration.title", "test integration");
        waitFor(ExpectedConditions.elementToBeClickable(By.id("btnSave")));
        // assert save enabled
        openDatasetsModal();
        findAndClickDataset("spitalf", SPITALFIELD_CHECKBOX);
        // note that IDs are dataTable ids
        findAndClickDataset("alexandria", ALEXANDRIA_CHECKBOX);
        // add selected items
        find(By.className("btn-primary")).click();
        waitFor(4);
    }

    private void removeDatasetByPartialName(String name) {
        By selector = By.id("selDatasets");
        chooseSelectByName(name, selector);
        find(By.id("rmDatasetBtn")).click();
    }

    private void chooseSelectByName(String name, By selector) {
        List<WebElement> findElements = find(selector).first().findElements(By.tagName("option"));
        for (WebElement el : findElements) {
            if (el.getText().contains(name)) {
                el.click();
                break;
            }
        }
    }

    private void findAndClickDataset(String text, String cbid) {
        WebElementSelection textEl = find(By.name("searchFilter.title"));
        textEl.val("");
        textEl.sendKeys(text);
        // wait for response ... would be nice to not use this, but we could already have the checkbox, and have issues when the ajax cycles back
        waitFor(2);
        // note that IDs are dataTable ids
        By checkbox = By.id(cbid);
        waitFor(ExpectedConditions.elementToBeClickable(checkbox));
        find(checkbox).click();
    }


    private void openDatasetsModal() throws InterruptedException {
        // wait for modal to load
        find(By.id("btnAddDataset")).click();
        waitFor(DEFAULT_WAITFOR_TIMEOUT / 2);
        // wait for results table
        waitFor(By.className("table-striped"));
    }
}
