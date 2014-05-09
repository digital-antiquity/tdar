package org.tdar.core.service.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.service.ServiceInterface;

/**
 * Transactional service providing persistence access to OntologyNodeS.
 * 
 * @author Allen Lee
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class OntologyNodeService extends ServiceInterface.TypedDaoBase<OntologyNode, OntologyNodeDao> {

    public List<OntologyNode> getAllChildren(OntologyNode ontologyNode) {
        return getDao().getAllChildren(ontologyNode);
    }

    /**
     * Returns a mapping between OntologyNodeS and a List of all their children (recursive).
     * 
     * FIXME: performance?
     * 
     * @param selectedOntologyNodes
     *            a list of ontology nodes selected in the filter-data-values data integration step.
     * @return a Map whose keys represent selectedOntologyNodes and whose values are lists of the given selectedOntologyNode's children.
     */
    public Map<OntologyNode, List<OntologyNode>> getHierarchyMap(List<OntologyNode> selectedOntologyNodes) {
        HashMap<OntologyNode, List<OntologyNode>> hierarchyMap = new HashMap<OntologyNode, List<OntologyNode>>();
        for (OntologyNode node : selectedOntologyNodes) {
            hierarchyMap.put(node, getAllChildren(node));
        }
        return hierarchyMap;
    }

    /**
     * Find all child nodes of the specified @link OntologyNode entries.
     * 
     * @param selectedOntologyNodes
     * @return
     */
    // FIXME: may want to aggregate / batch for efficiency
    public Set<OntologyNode> getAllChildren(List<OntologyNode> selectedOntologyNodes) {
        return getDao().getAllChildren(selectedOntologyNodes);
    }

    /**
     * Find all @link Dataset Resources that are mapped to the specified @link OntologyNode
     * 
     * @param node
     * @return
     */
    public List<Dataset> listDatasetsWithMappingsToNode(OntologyNode node) {
        return getDao().findDatasetsUsingNode(node);
    }

    /**
     * Find the parent @link OntologyNode of the specified node
     * 
     * @param node
     * @return
     */
    public OntologyNode getParent(OntologyNode node) {
        return getDao().getParentNode(node);
    }
}
