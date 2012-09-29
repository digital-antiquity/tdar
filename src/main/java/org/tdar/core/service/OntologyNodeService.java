package org.tdar.core.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.resource.OntologyNodeDao;

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
     * @param selectedOntologyNodes a list of ontology nodes selected in the filter-data-values data integration step.
     * @return a Map whose keys represent selectedOntologyNodes and whose values are lists of the given selectedOntologyNode's children.
     */
    public Map<OntologyNode, List<OntologyNode>> getHierarchyMap(List<OntologyNode> selectedOntologyNodes) {
        HashMap<OntologyNode, List<OntologyNode>> hierarchyMap = new HashMap<OntologyNode, List<OntologyNode>>();
        for (OntologyNode node: selectedOntologyNodes) {
            hierarchyMap.put(node, getAllChildren(node));
        }
        return hierarchyMap;
    }
    
    // FIXME: may want to aggregate / batch for efficiency
    public Set<OntologyNode> getAllChildren(List<OntologyNode> selectedOntologyNodes) {
        HashSet<OntologyNode> allChildren = new HashSet<OntologyNode>();
        for (OntologyNode node: selectedOntologyNodes) {
            allChildren.addAll(getAllChildren(node));
        }
        allChildren.addAll(selectedOntologyNodes);
        return allChildren;
    }
}
