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
    public List<DataTable> findDataTables(Project project, ResourceCollection collection, List<Ontology> ontologies, boolean bookmarked, TdarUser authorizedUser, int firstResult, int maxResults) {
        Query query = getCurrentSession().getNamedQuery(QUERY_INTEGRATION_DATA_TABLE);
        Long projectId = -1L;
        Long collectionId = -1L;
        Long submitterId = -1L;
        boolean hasOntologies = false;
        List<Long> ontologyIds = Arrays.asList(-1L);
        if (Persistable.Base.isNotNullOrTransient(project)) {
            projectId = project.getId();
        }

        if (Persistable.Base.isNotNullOrTransient(authorizedUser)) {
            submitterId = authorizedUser.getId();
        }
        
        if (Persistable.Base.isNotNullOrTransient(collection)) {
            collectionId = collection.getId();
        }
        if (CollectionUtils.isNotEmpty(ontologies)) {
            ontologyIds = Persistable.Base.extractIds(ontologies);
        }

        // not really sure why, but the -1L seems to need a type hint
        query.setParameter("projectId", projectId, LongType.INSTANCE);
        query.setParameter("collectionId", collectionId, LongType.INSTANCE);
        query.setParameter("hasOntologies", hasOntologies);
        query.setParameterList("ontologyIds", ontologyIds, LongType.INSTANCE);
        query.setParameter("bookmarked", bookmarked);
        query.setParameter("submitterId", submitterId, LongType.INSTANCE);
        query.setMaxResults(maxResults);
        query.setFirstResult(firstResult);
        return query.list();
    }

}
