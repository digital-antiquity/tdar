package org.tdar.core.dao.resource;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * 
 * @version $Revision$
 * @latest $Id$
 */
@Component
public class OntologyDao extends ResourceDao<Ontology> {

    public OntologyDao() {
        super(Ontology.class);
    }

    public int getNumberOfMappedDataValues(Ontology ontology) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_IS_ONTOLOGY_MAPPED);
        query.setLong("ontologyId", ontology.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

    public int getNumberOfMappedDataValuesToDataTableColumn(DataTableColumn dataTableColumn) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_IS_ONTOLOGY_MAPPED_TO_COLUMN);
        query.setLong("ontologyId", dataTableColumn.getDefaultOntology().getId());
        query.setLong("dataTableColumnId", dataTableColumn.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

}
