package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.utils.PersistableUtils;

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

    @SuppressWarnings("unchecked")
    public List<CodingSheet> findAllUsingOntology(Ontology ontology, List<Status> statuses) {
        if (PersistableUtils.isNullOrTransient(ontology)) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_CODING_SHEETS_USING_ONTOLOGY);
        query.setParameter("ontologyId", ontology.getId());
        query.setParameter("statuses", statuses);
        return query.getResultList();
    }

}
