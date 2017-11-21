package org.tdar.core.service.resource;

import java.util.List;
import java.util.Set;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;

public interface ProjectService {

    /**
     * Find @link Project resources by their submitter (@link Person).
     * 
     * @param submitter
     * @return
     */
    List<Project> findBySubmitter(TdarUser submitter);

    /**
     * Find @link Project resources by their matching title.
     * 
     * @param title
     * @return
     */
    List<Project> findByTitle(String title);

    /**
     * Find all @link Project resources by the submitter, but return only sparse (title, description) objects.
     * 
     * @param person
     * @return
     */
    List<Project> findAllSparseEditableProjects(Person person);

    /**
     * Find all @link Project resources, but only return sparse objects (title, description)
     */
    List<Project> findAllSparse();

    /**
     * Finds all @link Resource entries that are part of the specified @link Project. These entries are maintained transiently on the Project entity, and must
     * be dynamically loaded. This was done for performance tuning for large projects.
     * 
     * @param p
     * @param statuses
     * @return
     */
    Set<InformationResource> findAllResourcesInProject(Project p, Status... statuses);

    /**
     * Find Projects that were edited recently by the specified user, and return sparse objects (title, description)
     * 
     * @param updater
     * @param maxResults
     * @return
     */
    List<Resource> findRecentlyEditedResources(Person updater, int maxResults);

    /**
     * Find projects with no resources.
     * 
     * @param updater
     * @return
     */
    List<Project> findEmptyProjects(Person updater);

    List<Resource> findSparseTitleIdProjectListByPerson(TdarUser person, boolean isAdmin);

    /**
     * Check if specified @link Project contains a @link Dataset entity that has mapped @link CodingSheet entries and @link Ontology entities.
     * 
     * @param project
     * @return
     */
    Boolean containsIntegratableDatasets(Project project);

    /**
     * Find out if any @link Project specified by id has @link Dataset entities that have mapped @link CodingSheet entries and @link Ontology entities.
     * 
     * @param project
     * @return
     */
    Boolean containsIntegratableDatasets(List<Long> projectIds);

    Object getProjectAsJson(Project project, TdarUser user, String callback);

    Project find(long l);

}