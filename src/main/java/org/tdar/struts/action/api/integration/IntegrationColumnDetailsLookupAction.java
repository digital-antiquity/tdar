package org.tdar.struts.action.api.integration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.dao.integration.IntegrationColumnProxy;
import org.tdar.core.service.DataIntegrationService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;

public class IntegrationColumnDetailsLookupAction extends AbstractIntegrationAction {

    private static final long serialVersionUID = 6114433438074971363L;

    private IntegrationColumn integrationColumn;
    @Autowired
    private transient DataIntegrationService integrationService;

    @Autowired
    private transient SerializationService serializationService;

    @Action(value = "integration-column-details")
    public String integrationColumnDetails() throws IOException {
        integrationService.getColumnDetails(integrationColumn);
        Ontology sharedOntology = integrationColumn.getSharedOntology();
        IntegrationColumnProxy proxy = new IntegrationColumnProxy();
        proxy.setSharedOntology(sharedOntology);
        proxy.getFlattenedNodes().addAll(integrationColumn.getFlattenedOntologyNodeList());
        setJsonInputStream(new ByteArrayInputStream(serializationService.convertToFilteredJson(proxy, JsonIntegrationDetailsFilter.class).getBytes()));

        return SUCCESS;
    }

}
