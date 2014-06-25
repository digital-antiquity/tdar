package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * 
 * @version $Revision$
 * @latest $Id$
 */
@Component
public class OntologyDao extends ResourceDao<Ontology> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OntologyDao() {
        super(Ontology.class);
    }

    public int getNumberOfMappedDataValues(Ontology ontology) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY);
        query.setLong("ontologyId", ontology.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN);
        query.setParameter("ontology", dataTableColumn.getDefaultOntology());
        query.setParameter("codingSheet", dataTableColumn.getDefaultCodingSheet());
        return ((Long) query.uniqueResult()).intValue();
    }

    public void removeReferencesToOntologyNodes(List<OntologyNode> incoming) {
        List<OntologyNode> toDelete = new ArrayList<OntologyNode>();
        for (OntologyNode node : incoming) {
            if (Persistable.Base.isNullOrTransient(node)) {
                continue;
            }
            toDelete.add(node);
        }
        logger.debug("removing coding rule references to {} ", toDelete);
        if (CollectionUtils.isEmpty(toDelete)) {
            return;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES);
        query.setParameterList("ontologyNodes", toDelete);
        query.executeUpdate();

    }

}
