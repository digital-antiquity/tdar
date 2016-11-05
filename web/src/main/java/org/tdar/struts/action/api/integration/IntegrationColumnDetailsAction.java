package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.integration.IntegrationColumnPartProxy;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.utils.json.JsonIdNameFilter;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class IntegrationColumnDetailsAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = 6114433438074971363L;

    private IntegrationColumn integrationColumn;

    @Autowired
    private transient DataIntegrationService integrationService;

    @Action(value = "integration-column-details")
    public String integrationColumnDetails() throws IOException {
        Set<OntologyNode> sharedNodes = integrationService.getFilteredOntologyNodes(getIntegrationColumn());
        Ontology sharedOntology = getIntegrationColumn().getSharedOntology();
        IntegrationColumnPartProxy proxy = new IntegrationColumnPartProxy();
        proxy.setSharedOntology(sharedOntology);
        proxy.setFlattenedNodes(sharedNodes);
        setJsonObject(proxy, JsonIdNameFilter.class);
        return SUCCESS;
    }

    public IntegrationColumn getIntegrationColumn() {
        return integrationColumn;
    }

    public void setIntegrationColumn(IntegrationColumn integrationColumn) {
        this.integrationColumn = integrationColumn;
    }

}
