/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web.functional;

import java.io.File;

import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.ResourceType;

public class DataIntegrationSeleniumWebITCase extends AbstractBasicSeleniumWebITCase {

    private static final String ALEXANDRIA_DB_NAME = "qrybonecatalogueeditedkk.xls";
    public static final String SPITAL_DB_NAME = "Spital Abone database.mdb";
    public static final String FAUNA_ELEMENT_NAME = "fauna-element-updated---default-ontology-draft.owl";
    public static final String FAUNA_TAXON_NAME = "fauna-taxon---tag-uk-updated---default-ontology-draft.owl";

    @Test
    public void testDataIntegration() {
        Long spitalId = uploadSparseResource("Spitalfields", "Spitalfields Description", ResourceType.DATASET, "1923", -1, new File(TestConstants.TEST_DATA_INTEGRATION_DIR
                + SPITAL_DB_NAME));
        Long alexId = uploadSparseResource("Alexandria", "Alexandria Description", ResourceType.DATASET, "1924", -1, new File(TestConstants.TEST_DATA_INTEGRATION_DIR
                + ALEXANDRIA_DB_NAME));

        Long faunaId = uploadSparseResource("Fauna Element", "Fauna Element Description", ResourceType.ONTOLOGY, "1920", -1, new File(TestConstants.TEST_DATA_INTEGRATION_DIR
                + FAUNA_ELEMENT_NAME));
        Long taxonId = uploadSparseResource("Fauna Taxon", "Fauna Taxon Description", ResourceType.ONTOLOGY, "1920", -1, new File(TestConstants.TEST_DATA_INTEGRATION_DIR
                + FAUNA_TAXON_NAME));
    }

    private Long uploadSparseResource(String title, String description, ResourceType resourceType, String date, int projectId, File file) {
        gotoPage(String.format("/%s/add", resourceType.getUrlNamespace()));
        setFieldByName("dataset.title", title);
        setFieldByName("dataset.description", description);
        setFieldByName("dataset.date", date);
        uploadFile(FileAccessRestriction.PUBLIC, file);
        setFieldByName("projectId", Integer.toString(projectId));
        submitForm();
        String currentUrl = getCurrentUrl();
        String part = currentUrl.substring(currentUrl.indexOf(resourceType.getUrlNamespace())+ resourceType.getUrlNamespace().length() + 1);
        logger.info("part: {}",part);
        if (part.contains("/")) {
            part = part.substring(0,part.indexOf("/"));
        }
        try {
            return Long.parseLong(part);
        } catch (Exception e) {
            Assert.fail("tried to parse: " + part + " into long, but failed" + ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }
}
