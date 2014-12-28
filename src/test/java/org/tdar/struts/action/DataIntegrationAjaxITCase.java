package org.tdar.struts.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationDataTableSearchResult;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.search.DatasetSearchFilter;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.api.integration.IntegrationColumnDetailsAction;
import org.tdar.struts.action.api.integration.NodeParticipationByColumnAction;
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

    @Autowired
    GenericService genericService;


    @Test
    public void testNodeParticipation() throws IOException {
        NodeParticipationByColumnAction action;
        //get all the mapped dataTableColumns
        action = generateNewController(NodeParticipationByColumnAction.class);
        List<DataTableColumn> dataTableColumns = new ArrayList<>(genericService.findAll(DataTableColumn.class));
        List<Long> dtcIds = new ArrayList<>();
        for(DataTableColumn dtc : dataTableColumns) {
            if(dtc.getDefaultOntology() != null) {
                dtcIds.add(dtc.getId());
            }
        }

        action.getDataTableColumnIds().addAll(dtcIds);
        action.prepare();
        action.execute();
        logger.debug("results:{}", action.getNodeIdsByColumnId());

        //we expect to have at least one node value present
        int nodesPresent = 0;
        for(List<OntologyNode> nodes : action.getNodesByColumn().values()) {
            nodesPresent += nodes.size();
        }
        MatcherAssert.assertThat(nodesPresent, Matchers.greaterThan(0));


        nodesPresent = 0;
        for(List<Long> nodeids : action.getNodeIdsByColumnId().values()) {
            nodesPresent += nodeids.size();
        }
        MatcherAssert.assertThat(nodesPresent, Matchers.greaterThan(0));
    }

}
