package org.tdar.struts.action;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.dao.integration.DatasetSearchFilter;
import org.tdar.core.dao.integration.OntologySearchFilter;
import org.tdar.core.dao.resource.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.resource.integration.IntegrationOntologySearchResult;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.utils.json.JsonIntegrationFilter;

public class DataIntegrationAjaxITCase extends AbstractControllerITCase {

    @Autowired
    DataTableService dataTableService;

    @Autowired
    OntologyService ontologyService;

    @Autowired
    private SerializationService serializationService;

    @Test
    public void testDatasetService() throws IOException {
        DatasetSearchFilter filter = new DatasetSearchFilter(100, 0);
        filter.setAuthorizedUser(getUser());
        IntegrationDataTableSearchResult findDataTables = dataTableService.findDataTables(filter);
        logger.debug(serializationService.convertToFilteredJson(findDataTables, JsonIntegrationFilter.class));
    }

    @Test
    public void testOntologyService() throws IOException {
        OntologySearchFilter filter = new OntologySearchFilter(100, 0);
        filter.setAuthorizedUser(getUser());
        IntegrationOntologySearchResult findOntologies = ontologyService.findOntologies(filter);
        logger.debug(serializationService.convertToFilteredJson(findOntologies, JsonIntegrationFilter.class));
    }

}
