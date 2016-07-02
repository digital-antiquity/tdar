package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * $Id$
 * 
 * DAO access for OntologyNodeS.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class OntologyNodeDao extends Dao.HibernateBase<OntologyNode> {

    public OntologyNodeDao() {
        super(OntologyNode.class);
    }

    @SuppressWarnings("unchecked")
    public List<OntologyNode> getAllChildren(OntologyNode ontologyNode) {
        Query query = getCurrentSession().getNamedQuery(QUERY_ONTOLOGYNODE_ALL_CHILDREN);
        query.setParameter("ontologyId", ontologyNode.getOntology().getId());
        query.setParameter("intervalStart", ontologyNode.getIntervalStart());
        query.setParameter("intervalEnd", ontologyNode.getIntervalEnd());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<OntologyNode> getAllChildrenWithIndexWildcard(OntologyNode ontologyNode) {
        Query query = getCurrentSession().getNamedQuery(QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD);
        query.setParameter("ontologyId", ontologyNode.getOntology().getId());
        String indexWildcardString = ontologyNode.getIndex() + ".%";
        query.setParameter("indexWildcardString", indexWildcardString);
        return query.getResultList();
    }

    public Set<OntologyNode> getAllChildren(List<OntologyNode> selectedOntologyNodes) {
        HashSet<OntologyNode> allChildren = new HashSet<OntologyNode>();
        for (OntologyNode node : selectedOntologyNodes) {
            allChildren.addAll(getAllChildren(node));
        }
        allChildren.addAll(selectedOntologyNodes);
        return allChildren;
    }

    public List<Dataset> findDatasetsUsingNode(OntologyNode node) {
        List<Long> ids = new ArrayList<>();
        Query query = getCurrentSession().createNativeQuery(String.format(TdarNamedQueries.DATASETS_USING_NODES, node.getId()));
        for (Object obj : query.getResultList()) {
            ids.add(((Number) obj).longValue());
        }
        return findAll(Dataset.class, ids);
    }

    public OntologyNode getParentNode(OntologyNode node) {
        Query query = getCurrentSession().getNamedQuery(QUERY_ONTOLOGYNODE_PARENT);
        query.setParameter("ontologyId", node.getOntology().getId());
        if (node.getIndex().indexOf(".") != -1) {
            String index = node.getIndex().substring(0, node.getIndex().lastIndexOf("."));
            query.setParameter("index", index);
            return (OntologyNode) query.uniqueResult();
        }
        return null;
    }
}
