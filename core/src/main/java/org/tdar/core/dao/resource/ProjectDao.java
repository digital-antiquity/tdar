package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.ScrollableResults;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

/**
 * $Id$
 * 
 * Hibernate dao implementation for TDAR Projects.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component("projectDao")
@SuppressWarnings("unchecked")
public class ProjectDao extends ResourceDao<Project> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ProjectDao() {
        super(Project.class);
    }

    public List<Project> findAllEditableProjects(final Person person) {
        Query<Project> query = getNamedQuery(QUERY_READ_ONLY_EDITABLE_PROJECTS, Project.class);// QUERY_PROJECT_EDITABLE
        query.setParameter("submitterId", person.getId());
        return query.getResultList();
    }

    public Boolean containsIntegratableDatasets(Project project) {
        Query<Number> query = getNamedQuery(QUERY_PROJECT_COUNT_INTEGRATABLE_DATASETS, Number.class);
        query.setParameter("projectId", project.getId());
        return query.getSingleResult().longValue() > 0;
    }

    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Boolean.FALSE;
        }
        Query<Number> query = getNamedQuery(QUERY_PROJECTS_COUNT_INTEGRATABLE_DATASETS, Number.class);
        logger.debug("setting parameter list");
        query.setParameter("projectIdList", projectIds);
        return query.getSingleResult().longValue() > 0;
    }

    // TODO:maxResults ignored for now. You can have as many results as you'd like, so long as it's 5
    public List<Resource> findSparseRecentlyEditedResources(Person updater, int maxResults) {
        Query<Resource> query = getNamedQuery(QUERY_SPARSE_RECENT_EDITS,Resource.class);
        query.setParameter("personId", updater.getId());
        query.setMaxResults(maxResults);
        return query.getResultList();
    }

    /**
     * @param updater
     * @return
     */
    public List<Project> findEmptyProjects(Person updater) {
        Query<Project> query = getNamedQuery(QUERY_SPARSE_EMPTY_PROJECTS, Project.class);
        query.setParameter("submitter", updater.getId());
        return query.getResultList();
    }

    /**
     * @return
     */
    public List<Project> findAllSparse() {
        Query<Project> query = getNamedQuery(QUERY_SPARSE_PROJECTS, Project.class);
        return query.getResultList();
    }

    public ScrollableResults findAllResourcesInProject(Project project, Status... statuses) {
        Query<Resource> query = getNamedQuery(QUERY_RESOURCES_IN_PROJECT, Resource.class);
        if (ArrayUtils.isNotEmpty(statuses)) {
            query = getNamedQuery(QUERY_RESOURCES_IN_PROJECT_WITH_STATUS);
            query.setParameter("statuses", Arrays.asList(statuses));
        }
        query.setParameter("projectId", project.getId());
        return query.scroll();
    }

}
