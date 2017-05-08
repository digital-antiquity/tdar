package org.tdar.struts.action.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
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
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/ontology_node")
@Results(value = {
        @Result(name = "redirect_iri", location = "${redirectIri}")
})
public class OntologyNodeViewAction extends AbstractOntologyViewAction {

    private static final long serialVersionUID = -172190399789767787L;
    @Autowired
    private transient OntologyNodeService ontologyNodeService;
    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient CodingSheetService codingSheetService;

    private List<CodingSheet> codingSheetsWithMappings = new ArrayList<CodingSheet>();
    private OntologyNode parentNode;
    private List<OntologyNode> children;
    private String iri;
    private List<Dataset> datasetsWithMappingsToNode;

    @HttpOnlyIfUnauthenticated
    @Action(value = "{id}/node/{iri}",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = "../ontology/view-node.ftl")
            })
    public String node() throws TdarActionException {
        try {
            getCodingSheetsWithMappings().addAll(codingSheetService.findAllUsingOntology(getOntology()));
            setParentNode(ontologyNodeService.getParent(getNode()));

            setDatasetsWithMappingsToNode(ontologyNodeService.listDatasetsWithMappingsToNode(getNode()));
            setChildren(getChildElements(getNode()));
        } catch (Exception e) {
            getLogger().warn("{}", e, e);
        }
        return SUCCESS;
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        setNode(getNodeBySlug());
//        getLogger().debug("{}", getNode());
        if (getNode() == null) {
            setNode(getNodeByIri());
        }
  //      getLogger().debug("{}", getNode());
        if (getNode() == null) {
            abort(StatusCode.NOT_FOUND, getText("ontologyController.node_not_found", Arrays.asList(getIri())));
        }
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
        String normalizeIri = OntologyNode.normalizeIri(getIri());
        getLogger().trace("iri:{} --> {}", getIri(), normalizeIri);
        OntologyNode node_ = getOntology().getNodeByIri(normalizeIri);
        getLogger().trace("node:{}", node_);
        if (node_ == null) {
            node_ = fallbackCheckForIri(normalizeIri);
        }

        if (node_ != null) {
            setRedirectIri(String.format("/ontology/%s/node/%s", getId(), normalizeIri));
        }
    }

    public List<CodingSheet> getCodingSheetsWithMappings() {
        return codingSheetsWithMappings;
    }

    public void setCodingSheetsWithMappings(List<CodingSheet> codingSheetsWithMappings) {
        this.codingSheetsWithMappings = codingSheetsWithMappings;
    }

    @Override
    public Class<Ontology> getPersistableClass() {
        return Ontology.class;
    }
}
