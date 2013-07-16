package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * $Id$
 * 
 * @author Digital Anqituity 
 * @version $Revision$
 */
@Component
public class CodingSheetDao extends ResourceDao<CodingSheet> {

    public CodingSheetDao() {
        super(CodingSheet.class);
    }

    public int updateDataTableColumnOntologies(CodingSheet codingSheet, Ontology ontology) {
        if (codingSheet == null) {
            getLogger().warn("trying to update data table column default ontology references for a null coding sheet");
            return 0;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.UPDATE_DATATABLECOLUMN_ONTOLOGIES);
        query.setParameter("codingSheet", codingSheet);
        query.setParameter("ontology", ontology);
        return query.executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<CodingSheet> findAllUsingOntology(Ontology ontology, List<Status> statuses) {
        if (Persistable.Base.isNullOrTransient(ontology)) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY);
        query.setParameter("ontologyId", ontology.getId());
        query.setParameterList("statuses", statuses);
        return query.list();
    }

}
