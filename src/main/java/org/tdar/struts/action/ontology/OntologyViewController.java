package org.tdar.struts.action.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractResourceViewAction;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/ontology")
@Results(value = {
        @Result(name = "redirect_iri", location = "${redirectIri}")
})
public class OntologyViewController extends AbstractResourceViewAction<Ontology> {

    private static final long serialVersionUID = -826507251116794622L;
    @Autowired
    private transient OntologyNodeService ontologyNodeService;
    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient CodingSheetService codingSheetService;

    private List<CodingSheet> codingSheetsWithMappings = new ArrayList<CodingSheet>();
    private OntologyNode node;
    private OntologyNode parentNode;
    private List<OntologyNode> children;
    private String iri;
    private List<Dataset> datasetsWithMappingsToNode;
    private String redirectIri;

    @HttpOnlyIfUnauthenticated
    @Action(value = "{id}/node/{iri}",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = "view-node.ftl")
            })
    public String node() throws TdarActionException {
        getCodingSheetsWithMappings().addAll(codingSheetService.findAllUsingOntology(getOntology()));

        setNode(getOntology().getNodeByIri(getIri()));
        if (node == null) {
            throw new TdarActionException(StatusCode.NOT_FOUND, getText("ontologyController.node_not_found", getIri()));
        }
        setChildren(getChildElements(node));
        setParentNode(ontologyNodeService.getParent(node));

        setDatasetsWithMappingsToNode(ontologyNodeService.listDatasetsWithMappingsToNode(getNode()));
        return SUCCESS;
    }

    @Override
    @HttpOnlyIfUnauthenticated
    @Actions(value = {
            @Action(value = "{id}/{slug}"),
            @Action(value = "{id}")
    })
    public String view() throws TdarActionException {
        if (redirectIri != null) {
            return "redirect_iri";
        }
        return super.view();
    }

    public OntologyNode getNode() {
        return node;
    }

    public void setNode(OntologyNode node) {
        this.node = node;
    }

    public List<OntologyNode> getChildren() {
        return children;
    }

    public void setChildren(List<OntologyNode> children) {
        this.children = children;
    }

    public OntologyNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(OntologyNode parentNode) {
        this.parentNode = parentNode;
    }

    public List<OntologyNode> getChildElements(OntologyNode node) {
        getLogger().trace("get children:" + node);
        return ontologyService.getChildren(getOntology().getOntologyNodes(), node);
    }

    public List<OntologyNode> getChildElements(String index) {
        getLogger().trace("get children: {}", index);
        for (OntologyNode node : getOntology().getOntologyNodes()) {
            if (node.getIndex().equals(index)) {
                return ontologyService.getChildren(getOntology().getOntologyNodes(), node);
            }
        }
        return null;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public List<Dataset> getDatasetsWithMappingsToNode() {
        return datasetsWithMappingsToNode;
    }

    public void setDatasetsWithMappingsToNode(List<Dataset> datasetsWithMappingsToNode) {
        this.datasetsWithMappingsToNode = datasetsWithMappingsToNode;
    }

    public Ontology getOntology() {
        return (Ontology) getResource();
    }

    @Override
    protected void handleSlug() {
        if (!Objects.equals(getSlug(), getPersistable().getSlug())) {
            OntologyNode node_ = getOntology().getNodeByIri(getIri());
            if (node_ != null) {
                redirectIri = String.format("/ontology/%s/node/%s", getId(), getIri());
            }
        } else {
            super.handleSlug();
        }
    }

    public List<CodingSheet> getCodingSheetsWithMappings() {
        return codingSheetsWithMappings;
    }

    public void setCodingSheetsWithMappings(List<CodingSheet> codingSheetsWithMappings) {
        this.codingSheetsWithMappings = codingSheetsWithMappings;
    }
}
