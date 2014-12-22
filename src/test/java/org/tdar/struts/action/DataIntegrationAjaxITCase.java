package org.tdar.struts.action;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.dao.integration.DatasetSearchFilter;
import org.tdar.core.dao.integration.OntologySearchFilter;
import org.tdar.core.dao.resource.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.resource.integration.IntegrationOntologySearchResult;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationColumn.ColumnType;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.api.integration.IntegrationColumnDetailsAction;
import org.tdar.struts.action.api.integration.TableDetailsAction;
import org.tdar.utils.json.JsonIntegrationFilter;

public class DataIntegrationAjaxITCase extends AbstractControllerITCase {

    @Autowired
    DataTableService dataTableService;

    @Autowired
    OntologyService ontologyService;

    @Autowired
    private SerializationService serializationService;

    public static final Long SPITAL_MAIN_ID = 3091L;
    public static final Long TAXON_ID = 42940L;
    
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

    @Test
    public void testDataTableDetailsAction() throws IOException {
        TableDetailsAction detailsAction = generateNewInitializedController(TableDetailsAction.class,getAdminUser());
        detailsAction.setDataTableIds(Arrays.asList(SPITAL_MAIN_ID));
        detailsAction.dataTableDetails();
        logger.debug(IOUtils.toString(detailsAction.getJsonInputStream()));
    }

    @Test
    public void testIntegrationColumnDetailsAction() throws IOException {
        IntegrationColumnDetailsAction detailsAction = generateNewInitializedController(IntegrationColumnDetailsAction.class,getAdminUser());
        IntegrationColumn column = new IntegrationColumn();
        column.setColumnType(ColumnType.INTEGRATION);
        column.getColumns().add(dataTableService.find(SPITAL_MAIN_ID).getColumnByName("species_common_name"));
        column.setSharedOntology(ontologyService.find(TAXON_ID));
        detailsAction.setIntegrationColumn(column);
        detailsAction.integrationColumnDetails();
        logger.debug(IOUtils.toString(detailsAction.getJsonInputStream()));
    }

}
