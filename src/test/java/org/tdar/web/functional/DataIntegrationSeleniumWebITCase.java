/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.functional;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.ResourceType;

import edu.emory.mathcs.backport.java.util.Arrays;

public class DataIntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String FAUNA_ELEMENT = "Fauna Element";
    private static final String FAUNA_TAXON = "Fauna Taxon";
    private static final String ALEXANDRIA_DB_NAME = "qrybonecatalogueeditedkk.xls";
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";
    public static final String FAUNA_ELEMENT_NAME = "fauna-element-updated---default-ontology-draft.owl";
    public static final String FAUNA_TAXON_NAME = "fauna-taxon---tag-uk-updated---default-ontology-draft.owl";

    @Test
    public void testDataIntegration() {
        Long spitalId = uploadSparseResource("Spitalfields", "Spitalfields Description", ResourceType.DATASET, "1923", -1, new File(
                TestConstants.TEST_DATA_INTEGRATION_DIR + SPITAL_DB_NAME));

        Long faunaId = uploadSparseResource(FAUNA_ELEMENT, "Fauna Element Description", ResourceType.ONTOLOGY, "1920", -1, new File(
                TestConstants.TEST_DATA_INTEGRATION_DIR + FAUNA_ELEMENT_NAME));

        Long taxonId = uploadSparseResource(FAUNA_TAXON, "Fauna Taxon Description", ResourceType.ONTOLOGY, "1920", -1, new File(
                TestConstants.TEST_DATA_INTEGRATION_DIR + FAUNA_TAXON_NAME));

        assertTrue(getCurrentUrl().contains("columns"));
        WebElementSelection option = null;
        for (WebElement option_ : find(By.id("table_select")).find(By.tagName("option"))) {
            if (option_.getText().contains("Main table")) {
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
        mapColumnToOntology("Bone Common name", FAUNA_ELEMENT);
        submitForm();

        find(By.linkText("Generated identity coding sheet for Species Common name")).click();
        find(By.linkText("map ontology")).click();
        
        
        Long alexId = uploadSparseResource("Alexandria", "Alexandria Description", ResourceType.DATASET, "1924", -1, new File(
                TestConstants.TEST_DATA_INTEGRATION_DIR + ALEXANDRIA_DB_NAME));
        find(By.linkText("2")).click();
        mapColumnToOntology("Taxon", FAUNA_TAXON);
        mapColumnToOntology("BELEMENT", FAUNA_ELEMENT);
        submitForm();

    }

    private void mapColumnToOntology(String columnName, String ontologyName) {
        WebElementSelection column = null;
        for (WebElement columnDiv_ : find(By.className("datatablecolumn"))) {
            WebElementSelection columnDiv = new WebElementSelection(columnDiv_, driver);
            if (!columnDiv.find(By.className("displayName")).first().getText().contains(columnName)) {
                continue;
            }
            column = columnDiv;
        }

        WebElement ontologyField = column.find(By.className("ontologyfield")).first();
        if (!selectAutocompleteValue(ontologyField, ontologyName, ontologyName)) {
            String fmt = "Failed to add ontology %s because selenium failed to select a user from the autocomplete " +
                    "dialog.  Either the autocomplete failed to appear or an appropriate value was not in the " +
                    "menu.";
            fail(String.format(fmt, ontologyName));
        }
    }

    private Long uploadSparseResource(String title, String description, ResourceType resourceType, String date, int projectId, File file) {
        gotoPage(String.format("/%s/add", resourceType.getUrlNamespace()));
        setFieldByName("dataset.title", title);
        setFieldByName("dataset.description", description);
        setFieldByName("dataset.date", date);
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
