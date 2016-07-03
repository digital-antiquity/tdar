package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.OntologyProxy;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.utils.PersistableUtils;

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
        query.setParameter("ontologyId", ontology.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN);
        query.setParameter("ontology", dataTableColumn.getMappedOntology());
        query.setParameter("codingSheet", dataTableColumn.getDefaultCodingSheet());
        return ((Long) query.uniqueResult()).intValue();
    }

    public void removeReferencesToOntologyNodes(List<OntologyNode> incoming) {
        List<OntologyNode> toDelete = new ArrayList<OntologyNode>();
        for (OntologyNode node : incoming) {
            if (PersistableUtils.isNullOrTransient(node)) {
                continue;
            }
            toDelete.add(node);
        }
        logger.debug("removing coding rule references to {} ", toDelete);
        if (CollectionUtils.isEmpty(toDelete)) {
            return;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES);
        query.setParameter("ontologyNodes", toDelete);
        query.executeUpdate();

    }

    @SuppressWarnings("unchecked")
    public IntegrationOntologySearchResult findOntologies(OntologySearchFilter searchFilter) {
        Query query = getCurrentSession().getNamedQuery(QUERY_INTEGRATION_ONTOLOGY);
        query.setProperties(searchFilter);
        query.setFirstResult(searchFilter.getStartRecord());
        query.setMaxResults(searchFilter.getRecordsPerPage());
        IntegrationOntologySearchResult result = new IntegrationOntologySearchResult();
        for (Ontology ontology : (List<Ontology>) query.getResultList()) {
            result.getOntologies().add(new OntologyProxy(ontology));
        }
        return result;
    }

}
