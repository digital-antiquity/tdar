package org.tdar.core.dao.resource;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Dataset;

/**
 * $Id$ Provides DAO access to
 * persistent Datasets.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Component
public class DatasetDao extends ResourceDao<Dataset> {

    public DatasetDao() {
        super(Dataset.class);
    }

    public boolean canLinkDataToOntology(Dataset dataset) {
        if (dataset == null)
            return false;
        Query query = getCurrentSession().getNamedQuery(QUERY_DATASET_CAN_LINK_TO_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return !query.list().isEmpty();
    }

    public Long countResourcesForUserAccess(Person user) {
        if (user == null)
            return 0l;
        Query query = getCurrentSession().getNamedQuery(QUERY_USER_GET_ALL_RESOURCES_COUNT);
        query.setLong("userId", user.getId());
        return (Long) query.iterate().next();
    }

}
