package org.tdar.core.service.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
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

//    /**
//     * Find Project by Id
//     */
//    @Transactional(readOnly = true)
//    public Project find(Long id) {
//        Project project = getDao().find(id);
//        if (project == null) {
//            return Project.NULL;
//        }
//        return project;
//    }

    /**
     * Find @link Project resources by their submitter (@link Person).
     * @param submitter
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findBySubmitter(Person submitter) {
        if (submitter == null) {
            getLogger().warn("Trying to find projects for a null Person submitter, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findBySubmitter(submitter);
    }

    /**
     * Find @link Project resources by their matching title.
     * @param title
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findByTitle(final String title) {
        if (StringUtils.isBlank(title)) {
            getLogger().warn("Trying to find projects with an empty title, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findByTitle(title);
    }

    /**
     * Find all @link Project resources by the submitter, but return only sparse (title, description) objects.
     * @param person
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findAllSparseEditableProjects(Person person) {
        return getDao().findAllEditableProjects(person);
    }

    /**
     * Find all @link Project resources that were submitted by the person, or if admin, all. Return sparse entities (title, description).
     * @param person
     * @param isAdmin
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findSparseTitleIdProjectListByPerson(Person person, boolean isAdmin) {
        if (Persistable.Base.isNullOrTransient(person)) {
            return Collections.emptyList();
        }
        return authorizedUserDao.findSpaseEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin, true);
    }


    /**
     * Find all @link Project resources, but only return sparse objects (title, description)
     */
    @Transactional(readOnly = true)
    public List<Project> findAllSparse() {
        return getDao().findAllSparse();
    }


    /**
     * Finds all @link Resource entries that are part of the specified @link Project. These entries are maintained transiently on the Project entity, and must
     * be dynamically loaded. This was done for performance tuning for large projects.
     * 
     * @param p
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    public Set<InformationResource> findAllResourcesInProject(Project p, Status... statuses) {
        p.setCachedInformationResources(new HashSet<InformationResource>());
        Set<InformationResource> informationResources = getDao().findAllResourcesInProject(p, statuses);
        return informationResources;
    }

    /**
     * Find Projects that were edited recently by the specified user, and return sparse objects (title, description)
     * @param updater
     * @param maxResults
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findRecentlyEditedResources(Person updater, int maxResults) {
        return getDao().findSparseRecentlyEditedResources(updater, maxResults);
    }

    /**
     * Find projects with no resources.
     * @param updater
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findEmptyProjects(Person updater) {
        return getDao().findEmptyProjects(updater);
    }

    /**
     * Check if specified @link Project contains a @link Dataset entity that has mapped @link CodingSheet entries and @link Ontology entities.
     * @param project
     * @return
     */
    public Boolean containsIntegratableDatasets(Project project) {
        return getDao().containsIntegratableDatasets(project);
    }

    /**
     * Find out if any @link Project specified by id has @link Dataset entities that have mapped @link CodingSheet entries and @link Ontology entities.
     * 
     * @param project
     * @return
     */
    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        return getDao().containsIntegratableDatasets(projectIds);
    }
}
