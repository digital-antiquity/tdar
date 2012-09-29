package org.tdar.core.dao.resource;

import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.dao.Dao;

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
	public List<DataTable> findAllFromIdList(List<Long> dataTableIds) {
	    Query query = getCurrentSession().getNamedQuery(QUERY_DATATABLE_IDLIST);
	    query.setParameterList("dataTableIds", dataTableIds);
	    return (List<DataTable>) query.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<DataTable> findDataTablesUsingResource(Resource resource) {
		if (resource == null) {
			return Collections.emptyList();
		}
		Query query = getCurrentSession().getNamedQuery(QUERY_DATATABLE_RELATED_ID);
		getLogger().debug("Searching for linked resources to {}", resource.getId());
		query.setLong("relatedId",resource.getId());
        return (List<DataTable>) query.list();
	}

}
