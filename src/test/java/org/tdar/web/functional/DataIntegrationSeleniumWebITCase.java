/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.functional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.DataIntegrationITCase;

//@Ignore
public class DataIntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String BONE_COMMON_NAME = "Bone Common name";
    private static final String MAIN_TABLE = "Main table";
    private static final String SPITALFIELDS_DATASET_NAME = "Spitalfields Dataset";
    private static final String ALEXANDRIA_DATASET_NAME = "Alexandria Dataset";
    private static final String FAUNA_ELEMENT = "Fauna Element";
    private static final String FAUNA_TAXON = "Fauna Taxon";
    private static final String ALEXANDRIA_DB_NAME = "qrybonecatalogueeditedkk.xls";
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";
    public static final String FAUNA_ELEMENT_NAME = "fauna-element-updated---default-ontology-draft.owl";
    public static final String FAUNA_TAXON_NAME = "fauna-taxon---tag-uk-updated---default-ontology-draft.owl";
    private static final String GENERATED = "Generated identity coding sheet for ";
    String[] selectedNodeNames = new String[] { "Felis catus (Cat)", "Canis familiaris (Dog)", "Ovis aries (Sheep)", "Atlas", "Axis", "Carpal", "Tooth",
            "Ulna" };

    @Test
    public void testDataIntegration() {
        boolean skip = false;
        if (!skip) {
            Long faunaId = uploadSparseResource(FAUNA_ELEMENT, "Fauna Element Description", ResourceType.ONTOLOGY, "1920", -1, new File(
                    TestConstants.TEST_DATA_INTEGRATION_DIR + FAUNA_ELEMENT_NAME));

            Long taxonId = uploadSparseResource(FAUNA_TAXON, "Fauna Taxon Description", ResourceType.ONTOLOGY, "1920", -1, new File(
                    TestConstants.TEST_DATA_INTEGRATION_DIR + FAUNA_TAXON_NAME));

            Long spitalId = uploadSparseResource(SPITALFIELDS_DATASET_NAME, "Spitalfields Description", ResourceType.DATASET, "1923", -1, new File(
                    TestConstants.TEST_DATA_INTEGRATION_DIR + SPITAL_DB_NAME));
            assertTrue(getCurrentUrl().contains("columns"));
            WebElementSelection option = null;
            for (WebElement option_ : find(By.id("table_select")).find(By.tagName("option"))) {
                if (option_.getText().contains(MAIN_TABLE)) {
                    option = new WebElementSelection(option_, driver);
                    break;
                }

            }
            if (option == null) {
                fail("Couldn't find spitalfields Main table");
            }
            option.click();
            assertTrue(getCurrentUrl().contains("dataTableId"));
            assertTrue(getText().contains("Table Main table"));

            mapColumnToOntology("Species Common name", FAUNA_TAXON);
            mapColumnToOntology(BONE_COMMON_NAME, FAUNA_ELEMENT);
            submitForm();
            find(By.className("bookmark-label")).click();
            String datasetViewUrl = getCurrentUrl();
            find(By.linkText(GENERATED + "Species Common name")).click();
            mapCodingSheetToOntology(DataIntegrationITCase.getTaxonValueMap());
            gotoPage(datasetViewUrl);
            find(By.linkText(GENERATED + BONE_COMMON_NAME)).click();
            mapCodingSheetToOntology(DataIntegrationITCase.getElementValueMap());

            Long alexId = uploadSparseResource(ALEXANDRIA_DATASET_NAME, "Alexandria Description", ResourceType.DATASET, "1924", -1, new File(
                    TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_DB_NAME));
            find(By.linkText("2")).click();
            mapColumnToOntology("Taxon", FAUNA_TAXON);
            mapColumnToOntology("BELEMENT", FAUNA_ELEMENT);
            submitForm();
            find(By.className("bookmark-label")).click();
            datasetViewUrl = getCurrentUrl();
            find(By.linkText(GENERATED + "Taxon")).click();
            mapCodingSheetToOntology(DataIntegrationITCase.getTaxonValueMap());
            gotoPage(datasetViewUrl);
            find(By.linkText(GENERATED + "BELEMENT")).click();
            mapCodingSheetToOntology(DataIntegrationITCase.getElementValueMap());
        }

        find(By.linkText("Integrate")).click();
        WebElementSelection datsets = find(By.className("datatableListItem"));
        for (WebElement dataset : datsets) {
            String text = dataset.getText();
            logger.info(text);
            if (StringUtils.containsIgnoreCase(text, "qrybone")
                    || StringUtils.containsIgnoreCase(text, MAIN_TABLE.toLowerCase())) {
                logger.info("FOUND + CLICKING: {} ", text);
                dataset.click();
            }
        }

        submitForm();
        find(By.id("addColumn")).click(); // 3 columns
        find(By.id("addColumn")).click();
        WebElementSelection columns = find(By.id("drplist")).find(By.tagName("td"));

        WebElement column = columns.get(0);
        WebElementSelection draggables = find(By.className("drg"));
        WebElement taxon = findMatchingElementBy(draggables, "Taxon", By.className("name")).first();
        WebElement scn = findMatchingElementBy(draggables, "Species Common name", By.className("name")).first();
        dragAndDrop(taxon, column);
        dragAndDrop(scn, column);

        column = columns.get(1);
        WebElement belement = findMatchingElementBy(draggables, "BELEMENT", By.className("name")).first();
        WebElement bcn = findMatchingElementBy(draggables, BONE_COMMON_NAME, By.className("name")).first();
        dragAndDrop(bcn, column);
        dragAndDrop(belement, column);

        column = columns.get(2);
        WebElement bclass = findMatchingElementBy(draggables, "BCLASS", By.className("name")).first();
        WebElement scode = findMatchingElementBy(draggables, "Site code", By.className("name")).first();
        dragAndDrop(bclass, column);
        dragAndDrop(scode, column);

        submitForm();

        WebElementSelection nodes = find(By.className("nodeLabel"));
        for (String nodeName : selectedNodeNames) {
            logger.info("node: {}  in : {}", nodeName, nodes);
            WebElementSelection node = findMatchingElementBy(nodes, nodeName, By.className("nodeName"));
            if (node != null) {
                logger.info("found: {}", nodeName);
                node.click();
            }
        }

        find(".submitButton").click();
        assertTrue(getText().contains("Summary of Integration Results"));
        // find(By.id("downloadLink")).click();
    }

    private void dragAndDrop(WebElement draggable, WebElement target) {
        // http://stackoverflow.com/questions/14210051/how-to-automate-drag-drop-functionality-using-selenium-web-driver
        Actions builder = new Actions(driver);
        Action dragAndDrop = builder.clickAndHold(draggable).moveToElement(target).release(target).build();
        dragAndDrop.perform();
    }

    private void mapCodingSheetToOntology(Map<String, String> map) {
        find(By.className("mappingLink")).find(By.tagName("a")).click();

        WebElementSelection nodePairs = find(By.className("mappingPair"));
        for (Entry<String, String> entry : map.entrySet()) {
            WebElementSelection match = findMatchingElementBy(nodePairs, entry.getKey(), By.className("codingSheetTerm"));
            if (match == null) {
                continue;
            }

            WebElement ontologyNode = match.find(By.className("ontologyValue")).first();
            String value = entry.getValue();
            if (value.indexOf("(") != -1) {
                value = value.substring(0, value.indexOf("("));
            }
            if (!selectAutocompleteValue(ontologyNode, value, value, null)) {
                String fmt = "Failed to map ontology %s because selenium failed to select a user from the autocomplete " +
                        "dialog.  Either the autocomplete failed to appear or an appropriate value was not in the " +
                        "menu.";
                fail(String.format(fmt, value));
            }
        }
        submitForm();
    }

    private WebElementSelection findMatchingElementBy(WebElementSelection parentElement, String matchingText, By selector) {
        logger.info("looking for {} in {} ({})", matchingText, selector.toString(), parentElement.size());
        for (WebElement element_ : parentElement) {
            WebElementSelection element = new WebElementSelection(element_, driver);
            WebElementSelection name = element.find(selector);
            logger.info("{} {} ({})", name.getText(), name.val(), name.toList().size());
            if (name.getText().equals(matchingText)) {
                return element;
            }
            if (StringUtils.equals(name.val(), matchingText)) {
                return element;
            }
        }
        return null;
    }

    private void mapColumnToOntology(String columnName, String ontologyName) {
        WebElementSelection column = findMatchingElementBy(find(By.className("datatablecolumn")), columnName, By.className("displayName"));
        if (column == null) {
            return;
        }

        WebElement ontologyField = column.find(By.className("ontologyfield")).first();
        if (!selectAutocompleteValue(ontologyField, ontologyName, ontologyName, null)) {
            String fmt = "Failed to add ontology %s because selenium failed to select a user from the autocomplete " +
                    "dialog.  Either the autocomplete failed to appear or an appropriate value was not in the " +
                    "menu.";
            fail(String.format(fmt, ontologyName));
        }
    }

    private Long uploadSparseResource(String title, String description, ResourceType resourceType, String date, int projectId, File file) {
        gotoPage(String.format("/%s/add", resourceType.getUrlNamespace()));
        setFieldByName(resourceType.getFieldName() + ".title", title);
        setFieldByName(resourceType.getFieldName() + ".description", description);
        setFieldByName(resourceType.getFieldName() + ".date", date);
        if (resourceType.isSupporting()) {
            uploadFile(FileAccessRestriction.PUBLIC, file);
        } else {
            uploadFileAsync(FileAccessRestriction.PUBLIC, file);
        }

        setFieldByName("projectId", Integer.toString(projectId));
        submitForm();
        String currentUrl = getCurrentUrl();
        String part = currentUrl.substring(currentUrl.indexOf(resourceType.getUrlNamespace()) + resourceType.getUrlNamespace().length() + 1);

        logger.info("part: {}", part);
        if (part.contains("/")) {
            part = part.substring(0, part.indexOf("/"));
        }
        try {
            return Long.parseLong(part);
        } catch (Exception e) {
            Assert.fail("tried to parse: " + part + " into long, but failed" + ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }
}
