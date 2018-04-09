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
public class OntologyNodeServiceImpl extends ServiceInterface.TypedDaoBase<OntologyNode, OntologyNodeDao> implements OntologyNodeService {

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyNodeService#getAllChildren(org.tdar.core.bean.resource.OntologyNode)
     */
    @Override
    public List<OntologyNode> getAllChildren(OntologyNode ontologyNode) {
        return getDao().getAllChildren(ontologyNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyNodeService#getHierarchyMap(java.util.List)
     */
    @Override
    public Map<OntologyNode, List<OntologyNode>> getHierarchyMap(List<OntologyNode> selectedOntologyNodes) {
        HashMap<OntologyNode, List<OntologyNode>> hierarchyMap = new HashMap<OntologyNode, List<OntologyNode>>();
        for (OntologyNode node : selectedOntologyNodes) {
            hierarchyMap.put(node, getAllChildren(node));
        }
        return hierarchyMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyNodeService#getAllChildren(java.util.List)
     */
    // FIXME: may want to aggregate / batch for efficiency
    @Override
    public Set<OntologyNode> getAllChildren(List<OntologyNode> selectedOntologyNodes) {
        return getDao().getAllChildren(selectedOntologyNodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyNodeService#listDatasetsWithMappingsToNode(org.tdar.core.bean.resource.OntologyNode)
     */
    @Override
    public List<Dataset> listDatasetsWithMappingsToNode(OntologyNode node) {
        return getDao().findDatasetsUsingNode(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyNodeService#getParent(org.tdar.core.bean.resource.OntologyNode)
     */
    @Override
    public OntologyNode getParent(OntologyNode node) {
        return getDao().getParentNode(node);
    }
}
