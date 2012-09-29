package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * $Id$
 * 
 * DAO access for DataTableColumnS.
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component
public class DataTableColumnDao extends Dao.HibernateBase<DataTableColumn> {

    public DataTableColumnDao() {
        super(DataTableColumn.class);
    }
    
    public List<CodingRule> findMappedCodingRules(DataTableColumn column, List<String> valuesToMatch) {
        if (column == null || CollectionUtils.isEmpty(valuesToMatch)) {
            getLogger().debug("No mapped coding rules available for column {} and values {}", column, valuesToMatch);
            return Collections.emptyList();
        }
        return findMappedCodingRules(column.getDefaultCodingSheet(), valuesToMatch);
    }

    @SuppressWarnings("unchecked")
    public List<CodingRule> findMappedCodingRules(CodingSheet sheet, List<String> valuesToMatch) {
        if (sheet == null || CollectionUtils.isEmpty(valuesToMatch)) {
            getLogger().debug("no mapped coding rules available for sheet {} and values {}", sheet, valuesToMatch);
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_MAPPED_CODING_RULES);
        query.setParameter("codingSheetId", sheet.getId());
        query.setParameterList("valuesToMatch", valuesToMatch);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        if (dataset == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return query.list();
    }

}
