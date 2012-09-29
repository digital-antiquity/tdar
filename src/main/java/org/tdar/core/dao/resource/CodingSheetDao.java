/**
 * 
 */
package org.tdar.core.dao.resource;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 * @latest $Id$
 */
@Component
public class CodingSheetDao extends ResourceDao<CodingSheet> {

    public CodingSheetDao() {
        super(CodingSheet.class);
    }

    /**
     * Returns a CodingSheet with the given filename.
     * 
     * @param filename
     * @return
     */
    public CodingSheet findByFilename(final String filename) {
        return (CodingSheet) getCriteria().add(Restrictions.eq("filename", filename)).uniqueResult();
    }

    public int getNumberOfMappedDataTableColumns(CodingSheet codingSheet) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_IS_CODING_SHEET_MAPPED);
        query.setLong("codingId", codingSheet.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

}
