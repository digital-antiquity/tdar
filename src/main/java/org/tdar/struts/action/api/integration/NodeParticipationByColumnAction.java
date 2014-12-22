package org.tdar.struts.action.api.integration;

import com.opensymphony.xwork2.Preparable;
import org.apache.avro.generic.GenericData;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.DataIntegrationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This action uses the provided list of dataTableColumnIds and creates a map of ontologyNode lists by dataTableColumn
 * ID.  The list items indicate the node values that occur in each dataTableColumn.  The action
 * serializes the resulting map to a JSON ByteInputStream (for use in InputStreamResults)
 */
@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class NodeParticipationByColumnAction extends AbstractIntegrationAction implements Preparable{

    private List<Long> dataTableColumnIds = new ArrayList<>();
    private Map<Long, List<Long>> nodeIdsByColumnId = new HashMap<>();
    private Map<DataTableColumn, List<OntologyNode>> nodesByColumn;

    @Autowired
    DataIntegrationService integrationService;

    public List<Long> getDataTableColumnIds() {
        return dataTableColumnIds;
    }

    public void setDataTableColumnIds(List<Long> dataTableColumnIds) {
        this.dataTableColumnIds = dataTableColumnIds;
    }

    public Map<Long, List<Long>> getNodeIdsByColumnId() {
        return nodeIdsByColumnId;
    }

    public Map<DataTableColumn, List<OntologyNode>> getNodesByColumn() {
        return nodesByColumn;
    }


    public void prepare()  {
        nodesByColumn = integrationService.getNodeParticipationByColumn(dataTableColumnIds);

        for (Map.Entry<DataTableColumn, List<OntologyNode>> entry : nodesByColumn.entrySet()) {
            Long dtcId = entry.getKey().getId();
            nodeIdsByColumnId.put(dtcId, new ArrayList<Long>());

            for(OntologyNode ontologyNode : entry.getValue()) {
                nodeIdsByColumnId.get(dtcId).add(ontologyNode.getId());
            }
        }
    }

    @Action("node-participation")
    public String execute() throws IOException {
        setJsonObject(nodeIdsByColumnId);
        return "success";
    }
}
