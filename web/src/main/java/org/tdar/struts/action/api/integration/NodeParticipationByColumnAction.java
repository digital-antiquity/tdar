package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.integration.IntegrationColumnPartProxy;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.json.JacksonView;
import org.tdar.utils.json.JsonIdNameFilter;

import com.opensymphony.xwork2.Preparable;

/**
 * This action uses the provided list of dataTableColumnIds and creates a map of ontologyNode lists by dataTableColumn
 * ID. The list items indicate the node values that occur in each dataTableColumn. The action
 * serializes the resulting map to a JSON ByteInputStream (for use in InputStreamResults)
 */
@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class NodeParticipationByColumnAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = 499550761252167428L;
    private List<Long> dataTableColumnIds = new ArrayList<>();
    private List<IntegrationColumnPartProxy> integrationColumnPartProxies;
    private Map<Long, List<Long>> nodeIdsByColumnId = new HashMap<>();
    private boolean verbose = true;

    @Autowired
    DataIntegrationService integrationService;

    public List<Long> getDataTableColumnIds() {
        return dataTableColumnIds;
    }

    public void setDataTableColumnIds(List<Long> dataTableColumnIds) {
        this.dataTableColumnIds = dataTableColumnIds;
    }

    public List<IntegrationColumnPartProxy> getIntegrationColumnPartProxies() {
        return integrationColumnPartProxies;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean debug) {
        this.verbose = debug;
    }

    public void prepare() {
        integrationColumnPartProxies = integrationService.getNodeParticipationByColumn(dataTableColumnIds);

        for (Long dataTableColumnId : dataTableColumnIds) {
            nodeIdsByColumnId.put(dataTableColumnId, new ArrayList<Long>());
        }

        for (IntegrationColumnPartProxy columnPart : integrationColumnPartProxies) {
            for (OntologyNode ontologyNode : columnPart.getFlattenedNodes()) {
                nodeIdsByColumnId.get(columnPart.getDataTableColumn().getId()).add(ontologyNode.getId());
            }
        }
    }

    public Class<? extends JacksonView> getJsonView() {
        return JsonIdNameFilter.class;
    }
    
    @Action("node-participation")
    @PostOnly
    public String execute() throws IOException {
        if (verbose) {
            setJsonObject(integrationColumnPartProxies, JsonIdNameFilter.class);
        } else {
            setJsonObject(nodeIdsByColumnId);
        }

        return SUCCESS;
    }

}
