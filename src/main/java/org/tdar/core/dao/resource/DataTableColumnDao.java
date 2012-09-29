package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.dao.Dao;

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

    @SuppressWarnings("unchecked")
    public List<DataTableColumn> findOntologyMappedColumns(Dataset dataset) {
        if (dataset == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_DATATABLECOLUMN_WITH_DEFAULT_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return (List<DataTableColumn>) query.list();
    }
}
