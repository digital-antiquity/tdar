package org.tdar.struts.action.api.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.dao.integration.OntologyProxy;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.utils.json.JsonIntegrationDetailsFilter;
import org.tdar.utils.json.JsonIntegrationFilter;

@Namespace("/api/integration")
@ParentPackage("secured")
@Component
@Scope("prototype")
public class OntologyDetailsAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = -6992574286555315265L;

    private List<Long> ontologyIds = new ArrayList<>();

    @Autowired
    DataIntegrationService integrationService;

    @Action(value = "ontology-details")
    public String ontologyDetails() throws IOException {
        List<OntologyProxy> results = new ArrayList<>();
        for (Long id : ontologyIds) {
            OntologyProxy proxy = new OntologyProxy(getGenericService().find(Ontology.class, id));
            results.add(proxy);
        }
        setJsonObject(results, getJsonView());
        return SUCCESS;
    }

    public Class<?> getJsonView() {
        return JsonIntegrationDetailsFilter.class;
    }

    public List<Long> getOntologyIds() {
        return ontologyIds;
    }

    public void setOntologyIds(List<Long> dataTableIds) {
        this.ontologyIds = dataTableIds;
    }

}
