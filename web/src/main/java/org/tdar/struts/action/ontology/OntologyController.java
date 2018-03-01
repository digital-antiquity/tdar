package org.tdar.struts.action.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.service.resource.CodingSheetService;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.action.resource.AbstractSupportingInformationResourceController;
import org.tdar.struts_base.action.TdarActionException;

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

    public void saveCustomMetadata() {
        super.saveCategories();
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
