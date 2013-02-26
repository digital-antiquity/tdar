package org.tdar.core.service.resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.service.ServiceInterface;

/**
 * $Id$
 * 
 * Service API for manipulating Project-S and their related entities.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class ProjectService extends ServiceInterface.TypedDaoBase<Project, ProjectDao> {

    @Autowired
    private AuthorizedUserDao authorizedUserDao;

    @Transactional(readOnly = true)
    public Project find(Long id) {
        Project project = getDao().find(id);
        if (project == null) {
            return Project.NULL;
        }
        return project;
    }

    @Transactional(readOnly = true)
    public List<Project> findBySubmitter(Person submitter) {
        if (submitter == null) {
            getLogger().warn("Trying to find projects for a null Person submitter, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findBySubmitter(submitter);
    }

    @Transactional(readOnly = true)
    public List<Project> findByTitle(final String title) {
        if (StringUtils.isBlank(title)) {
            getLogger().warn("Trying to find projects with an empty title, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findByTitle(title);
    }

    @Transactional(readOnly = true)
    public List<Project> findAllSparseEditableProjects(Person person) {
        return getDao().findAllEditableProjects(person);
    }

    @Transactional(readOnly = true)
    public List<Project> findAllSparse() {
        return getDao().findAllSparse();
    }

    @Transactional(readOnly = true)
    public List<Project> findAllOtherProjects(Person person) {
        return getDao().findAllOtherProjects(person);
    }

    @Transactional(readOnly = true)
    public Set<InformationResource> findAllResourcesInProject(Project p, Status... statuses) {
        p.setCachedInformationResources(new HashSet<InformationResource>());
        Set<InformationResource> informationResources = getDao().findAllResourcesInProject(p, statuses);
        return informationResources;
    }

    @Transactional(readOnly = true)
    public List<Resource> findRecentlyEditedResources(Person updater, int maxResults) {
        return getDao().findSparseRecentlyEditedResources(updater, maxResults);
    }

    @Transactional(readOnly = true)
    public List<Project> findEmptyProjects(Person updater) {
        return getDao().findEmptyProjects(updater);
    }

    @Transactional(readOnly = true)
    public Set<Resource> findSparseTitleIdProjectListByPerson(Person person, boolean isAdmin) {
        return authorizedUserDao.findSparseTitleIdProjectListByPerson(person, isAdmin);
    }

    public Boolean containsIntegratableDatasets(Project project) {
        return getDao().containsIntegratableDatasets(project);
    }

    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        return getDao().containsIntegratableDatasets(projectIds);
    }
}
