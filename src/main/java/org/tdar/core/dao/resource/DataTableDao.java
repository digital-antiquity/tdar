package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
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


    public List<DataTable> findDataTables(IntegrationSearchFilter filter) {
        Query query = getCurrentSession().getNamedQuery(QUERY_INTEGRATION_DATA_TABLE)
                .setParameter("projectId", filter.getProjectId(), LongType.INSTANCE)
                .setParameter("collectionId", filter.getCollectionId(), LongType.INSTANCE)
                .setParameter("hasOntologies", !filter.getOntologyIds().isEmpty())
                .setParameterList("ontologyIds", paddedList(filter.getOntologyIds()), LongType.INSTANCE)
                .setParameter("bookmarked", filter.isBookmarked())
                .setParameter("submitterId", filter.getAuthorizedUser().getId(), LongType.INSTANCE)
                .setMaxResults(filter.getMaxResults())
                .setFirstResult(filter.getFirstResult());
        return Collections.checkedList(query.list(), DataTable.class);
    }

}
