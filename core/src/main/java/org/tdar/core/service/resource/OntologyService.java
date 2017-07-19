package org.tdar.core.service.resource;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.core.service.resource.ontology.OntologyNodeWrapper;

public interface OntologyService {

    /**
     * Find all ontologies, but return them with sparse objects (Title, Description only)
     * 
     * @return
     */
    List<Ontology> findSparseOntologyList();

    /**
     * Parses the OWL file associated with the given Ontology and indexes it into a representation of @link OntologyNodes the database.
     * 
     * @param ontology
     */
    void shred(Ontology ontology);

    /**
     * Takes an Ontology and finds the OWL @link InformationResourceFileVersion of the @link Ontology and returns the OntologyModel
     * 
     * @param ontology
     * @return
     * @throws FileNotFoundException
     */
    OntModel toOntModel(Ontology ontology) throws FileNotFoundException;

    /**
     * Filters a List of @link OntologyNode entries to to just the direct children of the @link OntologyNode.
     * 
     * @param allNodes
     * @param parent
     * @return
     */
    List<OntologyNode> getChildren(List<OntologyNode> allNodes, OntologyNode parent);

    /**
     * Returns @link OntologyNode entries that are at the root of their trees (i.e. first Branches; their parent is the root).
     * 
     * @param allNodes
     * @return
     */
    List<OntologyNode> getRootElements(List<OntologyNode> allNodes);

    /**
     * Find the number of @link CodingRule entries that refer to the @link DataTableColumn
     * 
     * @param dataTableColumn
     * @return
     */
    int getNumberOfMappedDataValues(DataTableColumn dataTableColumn);

    /**
     * Check if the @link DataTableColumn has any associations with an @link Ontology
     * 
     * @param dataTableColumn
     * @return
     */
    boolean isOntologyMapped(DataTableColumn dataTableColumn);

    /**
     * Converts a Text representation of an @link Ontology using TABs to an RDF/XML/OWL Ontology
     * 
     * @param id
     * @param fileTextInput
     * @return
     */
    String toOwlXml(Long id, String fileTextInput);

    IntegrationOntologySearchResult findOntologies(OntologySearchFilter searchFilter);

    OntologyNodeWrapper prepareOntologyJson(Ontology ontology);

}