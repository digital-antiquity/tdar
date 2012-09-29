package org.tdar.core.dao.resource;

import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.dao.Dao;

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
	    query.setLong("ontologyId", ontologyNode.getOntology().getId());
	    query.setLong("intervalStart", ontologyNode.getIntervalStart());
	    query.setLong("intervalEnd", ontologyNode.getIntervalEnd());
	    return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<OntologyNode> getAllChildrenWithIndexWildcard(OntologyNode ontologyNode) {
	    Query query = getCurrentSession().getNamedQuery(QUERY_ONTOLOGYNODE_ALL_CHILDREN_WITH_WILDCARD);
	    query.setLong("ontologyId", ontologyNode.getOntology().getId());
	    String indexWildcardString = ontologyNode.getIndex() + ".%";
	    query.setString("indexWildcardString", indexWildcardString);
	    return query.list();
	}
}
