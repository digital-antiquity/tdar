package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.integration.IntegrationSearchFilter;

/**
 * $Id$
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Component
public class DataTableDao extends Dao.HibernateBase<DataTable> {

    public DataTableDao() {
        super(DataTable.class);
    }

    @SuppressWarnings("unchecked")
    public List<DataTable> findDataTablesUsingResource(Resource resource) {
        if (resource == null) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_DATATABLE_RELATED_ID);
        getLogger().trace("Searching for linked resources to {}", resource.getId());
        query.setLong("relatedId", resource.getId());
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<DataTable> findDataTables(IntegrationSearchFilter filter) {
        Query query = getCurrentSession().getNamedQuery(QUERY_INTEGRATION_DATA_TABLE);
        query.setProperties(filter);
        query.setMaxResults(filter.getMaxResults());
        query.setFirstResult(filter.getFirstResult());
        return Collections.checkedList(query.list(), DataTable.class);
    }

}
