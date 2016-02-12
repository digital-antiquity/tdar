package org.tdar.struts.action.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.data.OntologyNodeWrapper;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

import edu.emory.mathcs.backport.java.util.Collections;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/ontology")
@Results(value = {
        @Result(name = OntologyViewController.REDIRECT_IRI, type = TdarActionSupport.REDIRECT, location = "${redirectIri}", params = { "ignoreParams",
                "id,slug" })
})
public class OntologyViewController extends AbstractOntologyViewAction {

    public static final String REDIRECT_IRI = "redirect_iri";
    private static final long serialVersionUID = -826507251116794622L;

    @Autowired
    private SerializationService serializationService;

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        // prepare calls handleSlug
        // handle slug will check if there's an IRI that's in a slug (fallback from older behavior)
        getLogger().trace("{} -- {}", getNode(), getRedirectIri());

        if (getRedirectIri() != null && getNode() == null) {
            abort(StatusCode.NOT_FOUND, getText("ontologyController.node_not_found", Arrays.asList(getIri())));
        }
    }

    @Override
    @HttpOnlyIfUnauthenticated
    @Actions(value = {
            @Action(value = "{id}/{slug}"),
            @Action(value = "{id}/"),
            @Action(value = "{id}")
    })
    public String view() throws TdarActionException {
        getLogger().trace("redirect iri: {}", getRedirectIri());
        // redirect if we don't have an IRI and have a bad slug
        if (isRedirectBadSlug() && StringUtils.isBlank(getRedirectIri())) {
            return BAD_SLUG;
        }

        // redirect if we have a IRI
        if (getRedirectIri() != null) {
            return REDIRECT_IRI;
        }
        buildJson();
        return super.view();
    }

    private String json = "";

    private void buildJson() {
        List<OntologyNode> nodes = getOntology().getSortedOntologyNodes();
        Collections.reverse(nodes);
        Map<Long, OntologyNodeWrapper> tree = new HashMap<>();
        OntologyNodeWrapper root = null;
        Set<OntologyNodeWrapper> roots = new HashSet<>();
        for (OntologyNode node : nodes) {
            OntologyNodeWrapper value = new OntologyNodeWrapper(node);
            if (!node.getIndex().contains(".")) {
                root = value;
                roots.add(value);
            }
            tree.put(node.getId(), value);
        }
        for (OntologyNode node : nodes) {
            for (OntologyNode c : nodes) {
                if (c == node) {
                    continue;
                }

                if (c.isChildOf(node)) {
                    OntologyNodeWrapper e = tree.get(c.getId());
                    if (c.getParentNode() != null) {
                        int cIndex = StringUtils.countMatches(c.getParentNode().getIndex(), ".");
                        int index_ = StringUtils.countMatches(node.getIndex(), ".");
                        if (cIndex > index_) {
                            continue;
                        } else {
                            OntologyNodeWrapper wrap = tree.get(c.getParentNode().getId());
                            wrap.getChildren().remove(e);
                            if (wrap.getChildren().isEmpty()) {
                                wrap.setChildren(null);
                            }
                        }
                    }
                    c.setParentNode(node);
                    OntologyNodeWrapper wrapper = tree.get(node.getId());
                    if (wrapper.getChildren() == null) {
                        wrapper.setChildren(new ArrayList<>());
                    }
                    wrapper.getChildren().add(e);
                }
            }
        }

        if (roots.size() > 1) {
            OntologyNodeWrapper wrapper = new OntologyNodeWrapper();
            wrapper.setId(-1L);
            wrapper.setDisplayName(getOntology().getName());
            wrapper.getChildren().addAll(roots);
            root = wrapper;
        }

        try {
            setJson(serializationService.convertToJson(root));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * History -- the following URLs need to be managed:
     * http://localhost:8080/ontology/3656 --> redirect to slug (or just work)
     * http://localhost:8080/ontology/3656/taxon --> just work
     * http://localhost:8080/ontology/3656/Kangaroo_Rat_or_Dipodomys_sp
     * http://localhost:8080/ontology/3656/node/Kangaroo_Rat_or_Dipodomys_sp
     */
    @Override
    protected void handleSlug() {
        // if slug != actual
        if (!Objects.equals(getSlug(), getPersistable().getSlug())) {
            getLogger().trace("processing slug request: {}", getSlug());
            // if completely blank, then redirect the slug
            if (StringUtils.isBlank(getSlug())) {
                getLogger().trace("passing upward: {}", getSlug());
                super.handleSlug();
                return;
            }

            // otherwise, try to see if the slug is an IRI
            String normalizeIri = OntologyNode.normalizeIri(getSlug());
            getLogger().trace("iri:{} --> {}", getIri(), normalizeIri);
            OntologyNode node_ = getOntology().getNodeByIri(normalizeIri);
            // handle struts differences /\\'\\,\\./ ...
            if (node_ == null) {
                node_ = fallbackCheckForIri(normalizeIri);
            }

            // redirect if we have an actual node match to the new home
            if (node_ != null) {
                setIri(node_.getIri());
                getLogger().trace("redirecting by iri: {}", node_.getIri());
                setNode(node_);
                setRedirectIri(String.format("/ontology/%s/node/%s", getId(), node_.getIri()));
                return;
            }
        }
        // handle slug
        super.handleSlug();
    }

    @Override
    public Class<Ontology> getPersistableClass() {
        return Ontology.class;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
