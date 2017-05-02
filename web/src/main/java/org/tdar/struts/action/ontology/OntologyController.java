package org.tdar.struts.action.ontology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts.action.resource.AbstractSupportingInformationResourceController;

/**
 * $Id$
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
    private List<CodingSheet> codingSheetsWithMappings = new ArrayList<CodingSheet>();

    @Autowired
    private transient CodingSheetService codingSheetService;
    @Autowired
    private transient OntologyService ontologyService;

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws IOException {
        String filename = getPersistable().getTitle() + ".owl";
        // convert text input to OWL XML text and use that as our archival version
        String owlXml = ontologyService.toOwlXml(getPersistable().getId(), fileTextInput);
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
        // ontologyService.saveOrUpdate(ontology);
        handleUploadedFiles();
        return SUCCESS;
    }

    public Ontology getOntology() {
        return getPersistable();
    }

    @Override
    protected void loadCustomMetadata() throws TdarActionException {
        super.loadCustomMetadata();
        getCodingSheetsWithMappings().addAll(codingSheetService.findAllUsingOntology(getOntology()));
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return getAnalyzer().getExtensionsForType(ResourceType.ONTOLOGY);
    }

    public List<OntologyNode> getRootElements() {
        return ontologyService.getRootElements(getPersistable().getOntologyNodes());
    }

    public void setOntology(Ontology ontology) {
        setPersistable(ontology);
    }

    @Override
    public Class<Ontology> getPersistableClass() {
        return Ontology.class;
    }

    public List<CodingSheet> getCodingSheetsWithMappings() {
        return codingSheetsWithMappings;
    }

    public void setCodingSheetsWithMappings(List<CodingSheet> codingSheetsWithMappings) {
        this.codingSheetsWithMappings = codingSheetsWithMappings;
    }

}
