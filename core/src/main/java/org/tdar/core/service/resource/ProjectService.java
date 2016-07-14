package org.tdar.core.service.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.utils.ImmutableScrollableCollection;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonProjectLookupFilter;

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

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private SerializationService serializationService;

    /**
     * Find @link Project resources by their submitter (@link Person).
     * 
     * @param submitter
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findBySubmitter(TdarUser submitter) {
        if (submitter == null) {
            getLogger().warn("Trying to find projects for a null Person submitter, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findBySubmitter(submitter);
    }

    /**
     * Find @link Project resources by their matching title.
     * 
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
     * 
     * @param person
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findAllSparseEditableProjects(Person person) {
        return getDao().findAllEditableProjects(person);
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
        ScrollableResults informationResources = getDao().findAllResourcesInProject(p, statuses);
        Set<InformationResource> results = new HashSet<>();
        for (InformationResource ir : new ImmutableScrollableCollection<InformationResource>(informationResources)) {
            results.add(ir);
        }

        return results;
    }

    /**
     * Find Projects that were edited recently by the specified user, and return sparse objects (title, description)
     * 
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
     * 
     * @param updater
     * @return
     */
    @Transactional(readOnly = true)
    public List<Project> findEmptyProjects(Person updater) {
        return getDao().findEmptyProjects(updater);
    }

    // @Transactional(readOnly = true)
    // public List<Resource> findSparseTitleIdProjectListByPersonOld(Person person, boolean isAdmin) {
    // return authorizedUserDao.findEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin, true);
    // }

    // FIXME: isAdmin should not be an argument. It's not the controller's job to know what the user can/can't see.
    @Transactional(readOnly = true)
    public List<Resource> findSparseTitleIdProjectListByPerson(TdarUser person, boolean isAdmin) {
        // get all of the collections (direct/inherited) that bestow modify-metadata rights to the specified user
        Set<SharedCollection> collections = resourceCollectionDao.findFlattendCollections(person, GeneralPermissions.MODIFY_METADATA, SharedCollection.class);

        // find all of the editable projects for the user (either directly assigned or via the specified collections)
        List<Long> collectionIds = PersistableUtils.extractIds(collections);
        List<Resource> editableResources = authorizedUserDao.findEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin, true, collectionIds);

        return editableResources;
    }

    /**
     * Check if specified @link Project contains a @link Dataset entity that has mapped @link CodingSheet entries and @link Ontology entities.
     * 
     * @param project
     * @return
     */
    @Transactional(readOnly=true)
    public Boolean containsIntegratableDatasets(Project project) {
        return getDao().containsIntegratableDatasets(project);
    }

    /**
     * Find out if any @link Project specified by id has @link Dataset entities that have mapped @link CodingSheet entries and @link Ontology entities.
     * 
     * @param project
     * @return
     */
    @Transactional(readOnly=true)
    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        return getDao().containsIntegratableDatasets(projectIds);
    }

    @Transactional(readOnly=true)
    public String getProjectAsJson(Project project, TdarUser user, String callback) {
        getLogger().trace("getprojectasjson called");
        Object result = new HashMap<String, Object>();

        try {
            if (PersistableUtils.isNotNullOrTransient(project)) {
            	getDao().markReadOnly(project);
            	List<ResourceCreator> rc = new ArrayList<>(project.getResourceCreators());
            	project.getResourceCreators().clear();
            	Collections.sort(rc);
            	project.getResourceCreators().addAll(rc);
                getLogger().trace("Trying to convert blank or null project to json: " + project);
                // obfuscationService.obfuscate(project, user);
                result = project;
            } else {
                result = Project.NULL;
            }
        } catch (Exception ex) {
            throw new TdarRecoverableRuntimeException("projectController.project_json_invalid", ex);
        }
        return serializationService.convertFilteredJsonForStream(result, JsonProjectLookupFilter.class, callback);
    }
}
