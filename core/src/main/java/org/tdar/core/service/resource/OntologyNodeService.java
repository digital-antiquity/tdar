package org.tdar.core.service.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.OntologyNode;

public interface OntologyNodeService {

    List<OntologyNode> getAllChildren(OntologyNode ontologyNode);

    /**
     * Returns a mapping between OntologyNodeS and a List of all their children (recursive).
     * 
     * FIXME: performance?
     * 
     * @param selectedOntologyNodes
     *            a list of ontology nodes selected in the filter-data-values data integration step.
     * @return a Map whose keys represent selectedOntologyNodes and whose values are lists of the given selectedOntologyNode's children.
     */
    Map<OntologyNode, List<OntologyNode>> getHierarchyMap(List<OntologyNode> selectedOntologyNodes);

    /**
     * Find all child nodes of the specified @link OntologyNode entries.
     * 
     * @param selectedOntologyNodes
     * @return
     */
    // FIXME: may want to aggregate / batch for efficiency
    Set<OntologyNode> getAllChildren(List<OntologyNode> selectedOntologyNodes);

    /**
     * Find all @link Dataset Resources that are mapped to the specified @link OntologyNode
     * 
     * @param node
     * @return
     */
    List<Dataset> listDatasetsWithMappingsToNode(OntologyNode node);

    /**
     * Find the parent @link OntologyNode of the specified node
     * 
     * @param node
     * @return
     */
    OntologyNode getParent(OntologyNode node);

}