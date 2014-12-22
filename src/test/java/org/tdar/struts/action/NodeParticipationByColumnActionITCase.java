package org.tdar.struts.action;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.api.integration.NodeParticipationByColumnAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by JAMES on 12/21/2014.
 */
public class NodeParticipationByColumnActionITCase extends AbstractDataIntegrationTestCase{

    @Autowired
    GenericService genericService;

    NodeParticipationByColumnAction action;

    public NodeParticipationByColumnActionITCase() {
    }

    @Test
    public void testNodeParticipation() throws IOException {
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
        assertThat(nodesPresent, greaterThan(0));


        nodesPresent = 0;
        for(List<Long> nodeids : action.getNodeIdsByColumnId().values()) {
            nodesPresent += nodeids.size();
        }
        assertThat(nodesPresent, greaterThan(0));
    }

}
