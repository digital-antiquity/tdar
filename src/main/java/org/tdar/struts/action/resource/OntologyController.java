package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.exception.StatusCode;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.interceptor.HttpOnlyIfUnauthenticated;

/**
 * $Id$
 * <p>
 * Manages CRUD requests for OntologyS.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/ontology")
@Component
@Scope("prototype")
public class OntologyController extends AbstractSupportingInformationResourceController<Ontology> {

    private static final long serialVersionUID = 4320412741803278996L;
    private String iri;
    private List<Dataset> datasetsWithMappingsToNode;
    private OntologyNode node;
    private OntologyNode parentNode;
    private List<OntologyNode> children;

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        String filename = getPersistable().getTitle() + ".owl";
        // convert text input to OWL XML text and use that as our archival version
        String owlXml = getOntologyService().toOwlXml(getPersistable().getId(), fileTextInput);
        getLogger().info("owl xml is: \n{}", owlXml);
        return new FileProxy(filename, FileProxy.createTempFileFromString(owlXml), VersionType.UPLOADED);
    }

    /**
     * Sets the various pieces of metadata on this ontology and then saves it.
     * 
     * @param ontology
     * @throws TdarActionException 
     */
    @Override
    protected String save(Ontology ontology) throws TdarActionException {
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();

        saveCategories();
        // getOntologyService().saveOrUpdate(ontology);
        handleUploadedFiles();
        return SUCCESS;
    }

    public Ontology getOntology() {
        return getPersistable();
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.ONTOLOGY);
    }

    public List<OntologyNode> getRootElements() {
        return getOntologyService().getRootElements(getPersistable().getOntologyNodes());
    }

    public List<OntologyNode> getChildElements(OntologyNode node) {
        logger.trace("get children:" + node);
        return getOntologyService().getChildren(getPersistable().getOntologyNodes(), node);
    }

    @SkipValidation
    @HttpOnlyIfUnauthenticated
    @Action(value = "node",
            interceptorRefs = { @InterceptorRef("unauthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, location = "view-node.ftl")
            })
    public String node() throws TdarActionException {
        setNode(getOntology().getNodeByIri(getIri()));
        if (node == null) {
            throw new TdarActionException(StatusCode.NOT_FOUND, "Ontology Node: " + getIri() + " does not exist");
        }
        setChildren(getChildElements(node));
        setParentNode(getOntologyNodeService().getParent(node));
        
        setDatasetsWithMappingsToNode(getOntologyNodeService().listDatasetsWithMappingsToNode(getNode()));
        return SUCCESS;
    }

    public List<OntologyNode> getChildElements(String index) {
        logger.trace("get children:" + index);
        for (OntologyNode node : getPersistable().getOntologyNodes()) {
            if (node.getIndex().equals(index))
                return getOntologyService().getChildren(getPersistable().getOntologyNodes(), node);
        }
        return null;
    }

    public void setOntology(Ontology ontology) {
        setPersistable(ontology);
    }

    public Class<Ontology> getPersistableClass() {
        return Ontology.class;
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

}
