package org.tdar.web.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.tdar.web.functional.util.ByLabelText;
import org.tdar.web.functional.util.WebElementSelection;

import static org.openqa.selenium.By.partialLinkText;
import static org.openqa.selenium.By.linkText;
import static org.openqa.selenium.By.id;



public class IntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String TEST_INTEGRATION = "test integration";
    private static final String SPITAL_DT_ID = "3091";
    private static final String ALEX_DT_ID = "3104";
    private static final String SPITALFIELD_CHECKBOX = "cbResult" + SPITAL_DT_ID;
    private static final String ALEXANDRIA_CHECKBOX = "cbResult" + ALEX_DT_ID;

    private static final By saveButton = By.id("btnSave");
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
        
        setFieldByName("integration.title", TEST_INTEGRATION);
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
        // add two datasets that work, assert that we get back to an integrate-able state
        
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
        find(ByLabelText.byLabelText("Aves")).click();// this is really slow, so do it once
        assertTrue(ExpectedConditions.elementSelectionStateToBe(By.id("cbont_64870"), true).apply(getDriver()).booleanValue());

        find(rabbit).click();
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        find(sheep).click();
        waitFor(ExpectedConditions.elementToBeClickable(saveButton));

        assertTrue("Perca flavescens node should be visible for spitalfields", find(By.id("cbx-32450-60600")).isDisplayed());
        assertTrue("Coding error should be visible for alexandria", find(By.id("cbx-31710-56490")).isDisplayed());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());

        //click the integrate button and wait for results
        find(By.id("btnIntegrate")).click();
        waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("divResultContainer")));
        takeScreenshot();
        clearPageCache();
        String pivotTableText = find("#divResultContainer tbody").first().getText();
        find(By.linkText("Preview")).click();

        //Capture the contents of the preview table body.
        String previewTableText = find("#divResultContainer tbody").last().getText();
        logger.debug("previewTableText: {}", previewTableText);
        logger.debug("pivotTableText: {}", pivotTableText);

        // test ontology present
        //todo:  jtd: I don't think we show the names of the ontologies in the new integration preview (are we supposed to?). commenting out for now.
        //assertTrue(pivotTableText.contains("Fauna Taxon"));

        // test nodes present
        assertThat(pivotTableText, containsString("Aves"));
        assertThat(pivotTableText.toLowerCase(), containsString("sheep"));
        assertThat(pivotTableText.toLowerCase(), containsString("rabbit"));

        // test databases present
        assertThat(previewTableText, containsString("Spitalfields"));
        assertThat(previewTableText, containsString("Alexandria"));
    }

    @Test
    public void testSaveAndReload() throws InterruptedException {
        // add two datasets that work, assert that we get back to an integratable state
        
        setupSpitalfieldsAlexandriaForTest();
        Assert.assertEquals(2, find(By.className("sharedOntologies")).size());

        find(id("btnAddDisplayColumn")).click();
        By tab1 = id("tab0");
        waitFor(tab1).isDisplayed();
        // dt_ + tabid + _ + data_table_id
        By spital_select = id("dt_0_" + SPITAL_DT_ID);
        chooseSelectByName("Site Code", spital_select);
        By alex_select = id("dt_0_" + ALEX_DT_ID);
        chooseSelectByName("LOCATION", alex_select);

        takeScreenshot();
        find(linkText("Add Integration Column")).click();
        find(linkText("Fauna Taxon Ontology")).click();
        // wait for tab visible
        waitFor(id("tabtab1")).isDisplayed();
        // wait for tab contents is visible
        waitFor(id("tab1")).isDisplayed();
        logger.debug(getText());

        find(aves).click();
        assertTrue(ExpectedConditions.elementSelectionStateToBe(id("cbont_64870"), true).apply(getDriver()).booleanValue());

        find(rabbit).click();
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        find(sheep).click();
        waitFor(ExpectedConditions.elementToBeClickable(saveButton));

        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());
        waitFor(saveButton).click();
        waitFor(4);
        gotoPage("/workspace/list");
        logger.debug(getText());
        clearPageCache();
        find(partialLinkText(TEST_INTEGRATION)).first().click();
        waitForPageload();
        
        waitFor(partialLinkText("Fauna Taxon Ontology")).click();
        waitFor(".nodechild1");
        logger.debug(getText());
        takeScreenshot("expecting populated fauna taxon pane");

        assertEquals(find(By.name("integration.title")).val(),TEST_INTEGRATION);
        String ontologyPaneText = getText().toLowerCase();
        
        assertTrue(ontologyPaneText.contains("aves"));
        assertTrue(ontologyPaneText.contains("rabbit"));
        assertTrue(ontologyPaneText.contains("taxon"));
        assertTrue(ontologyPaneText.contains("element"));
        assertTrue(ExpectedConditions.elementSelectionStateToBe(aves, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());
        assertTrue(ontologyPaneText.contains("spitalfield"));
        assertTrue(ontologyPaneText.contains("alexandria"));
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

        //fixme: don't look for ID,  look for label with child text node containing desired ontologyNode name(xpath is your best bet, i think), then click on that text node
        find(aves).click();
        find(rabbit).click();
        find(sheep).click();

        //fixme: using variables for  locators impacts readibility (in this call below you can't immediately discern value of the argument, nor whether it is a css selector, id, or elementLocator)
        //  Consider saving the result instead, e.g.:
        //      WebElement rabbitCheckbox = find("#cbont_1234").click().first();
        //      assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbitCheckbox, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(aves, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(rabbit, true).apply(getDriver()).booleanValue());
        assertTrue(ExpectedConditions.elementSelectionStateToBe(sheep, true).apply(getDriver()).booleanValue());
        waitFor(ExpectedConditions.elementToBeClickable(saveButton));

        // remove the datasets
        removeDatasetByPartialName("Spital");
        removeDatasetByPartialName("Alexandria");
        
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
        setFieldByName("integration.title", TEST_INTEGRATION);
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
