package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * 
 * @version $Revision$
 * @latest $Id$
 */
@Component
public class OntologyDao extends ResourceDao<Ontology> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OntologyDao() {
        super(Ontology.class);
    }

    public int getNumberOfMappedDataValues(Ontology ontology) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_ONTOLOGY);
        query.setLong("ontologyId", ontology.getId());
        return ((Long) query.uniqueResult()).intValue();
    }

    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_NUMBER_OF_MAPPED_DATA_VALUES_FOR_COLUMN);
        query.setParameter("ontology", dataTableColumn.getDefaultOntology());
        query.setParameter("codingSheet", dataTableColumn.getDefaultCodingSheet());
        return ((Long) query.uniqueResult()).intValue();
    }

    public void removeReferencesToOntologyNodes(List<OntologyNode> incoming) {
        List<OntologyNode> toDelete = new ArrayList<OntologyNode>();
        for (OntologyNode node : incoming) {
            if (Persistable.Base.isNullOrTransient(node)) {
                continue;
            }
            toDelete.add(node);
        }
        logger.debug("removing coding rule references to {} ", toDelete);
        if (CollectionUtils.isEmpty(toDelete)) {
            return;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_CLEAR_REFERENCED_ONTOLOGYNODE_RULES);
        query.setParameterList("ontologyNodes", toDelete);
        query.executeUpdate();

    }

    @SuppressWarnings("unchecked")
    public List<Ontology> findOntologies(Project project, ResourceCollection collection, TdarUser authorizedUser, CategoryVariable category, Collection<DataTable> dataTables, boolean bookmarked, int firstResult, int maxResults) {
        Long projectId = -1L;
        Long collectionId = -1L;
        Long categoryVariableId = -1L;
        boolean hasDatasets = false;
        List<Long>dataTableIds = Arrays.asList(-1L);
        Long submitterId = -1L;
        if (Persistable.Base.isNotNullOrTransient(authorizedUser)) {
            submitterId = authorizedUser.getId();
        }
        
        if (Persistable.Base.isNotNullOrTransient(category)) {
            categoryVariableId = category.getId();
        }

        if (CollectionUtils.isNotEmpty(dataTables)) {
            dataTableIds = Persistable.Base.extractIds(dataTables);
            hasDatasets = true;
        }
        if (Persistable.Base.isNotNullOrTransient(collection)) {
            collectionId = collection.getId();
        }

        if (Persistable.Base.isNotNullOrTransient(project)) {
            projectId = project.getId();
        }
        Query query = getCurrentSession().getNamedQuery(QUERY_INTEGRATION_ONTOLOGY);
        query.setParameter("projectId", projectId, LongType.INSTANCE);
        query.setParameter("collectionId", collectionId, LongType.INSTANCE);
        query.setParameter("bookmarked", bookmarked);
        query.setParameter("categoryId", categoryVariableId, LongType.INSTANCE);
        query.setParameter("hasDatasets", hasDatasets);
        query.setParameterList("dataTableIds", dataTableIds);
        query.setParameter("submitterId", submitterId, LongType.INSTANCE);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.list();
    }
}
