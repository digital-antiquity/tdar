package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.struts.data.FileProxy;

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

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        String filename = getPersistable().getTitle() + ".owl";
        // convert text input to OWL XML text and use that as our archival version
        String owlXml = getOntologyService().toOwlXml(getPersistable().getId(), fileTextInput);
        getLogger().trace("owl xml is: \n{}", owlXml);
        return new FileProxy(filename, FileProxy.createTempFileFromString(owlXml), VersionType.UPLOADED);
    }

    /**
     * Sets the various pieces of metadata on this ontology and then saves it.
     * 
     * 
     * @param ontology
     */
    @Override
    protected String save(Ontology ontology) {
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();

        saveCategories();
        getOntologyService().saveOrUpdate(ontology);
        handleUploadedFiles();
        return SUCCESS;
    }

    /**
     * @return all ontologies submitted by this user
     *         public List<Ontology> getAllSubmittedOntologies() {
     *         if (allSubmittedOntologies == null) {
     *         allSubmittedOntologies = getOntologyService().findBySubmitter(getAuthenticatedUser());
     *         }
     *         return allSubmittedOntologies;
     *         }
     */

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

}
