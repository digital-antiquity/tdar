package org.tdar.struts.action.ontology;

import java.util.Objects;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.OntologyNodeService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/ontology")
@Results(value = {
        @Result(name = "redirect_iri", location = "${redirectIri}")
})
public class OntologyViewController extends AbstractOntologyViewAction {

    private static final long serialVersionUID = -826507251116794622L;
    @Autowired
    private transient OntologyNodeService ontologyNodeService;
    @Autowired
    private transient OntologyService ontologyService;
    @Autowired
    private transient CodingSheetService codingSheetService;

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        if (getRedirectIri() != null) {
            setNode(getNodeByIri());
            getLogger().debug("NODE: {}", getNode());
            if (getNode() == null) {
                abort(StatusCode.NOT_FOUND, getText("ontologyController.node_not_found", getIri()));
            }
        }
        getLogger().debug("slug:{} , node:{}, iri: {}", getSlug(), getNode(), getIri());
    }

    @Override
    @HttpOnlyIfUnauthenticated
    @Actions(value = {
            @Action(value = "{id}/{slug}"),
            @Action(value = "{id}")
    })
    public String view() throws TdarActionException {
        if (getRedirectIri() != null) {
            return "redirect_iri";
        }
        return super.view();
    }

    // public List<OntologyNode> getChildren() {
    // return children;
    // }
    //
    // public void setChildren(List<OntologyNode> children) {
    // this.children = children;
    // }

    // public List<OntologyNode> getChildElements(OntologyNode node_) {
    // getLogger().trace("get children:" + node_);
    // return ontologyService.getChildren(getOntology().getOntologyNodes(), node_);
    // }

    // public List<OntologyNode> getChildElements(String index) {
    // getLogger().trace("get children: {}", index);
    // for (OntologyNode node : getOntology().getOntologyNodes()) {
    // if (node.getIndex().equals(index)) {
    // return ontologyService.getChildren(getOntology().getOntologyNodes(), node);
    // }
    // }
    // return null;
    // }

    @Override
    protected void handleSlug() {
        if (!Objects.equals(getSlug(), getPersistable().getSlug())) {
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
        } else {
            super.handleSlug();
        }
    }
}
