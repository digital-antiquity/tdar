package org.tdar.core.dao.resource;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
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

}
