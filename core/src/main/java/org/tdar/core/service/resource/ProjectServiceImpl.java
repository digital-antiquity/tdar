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
public class ProjectServiceImpl  extends ServiceInterface.TypedDaoBase<Project, ProjectDao> implements ProjectService {

    @Autowired
    private AuthorizedUserDao authorizedUserDao;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private SerializationService serializationService;

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findBySubmitter(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> findBySubmitter(TdarUser submitter) {
        if (submitter == null) {
            getLogger().warn("Trying to find projects for a null Person submitter, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findBySubmitter(submitter);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findByTitle(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> findByTitle(final String title) {
        if (StringUtils.isBlank(title)) {
            getLogger().warn("Trying to find projects with an empty title, ignoring.");
            return Collections.emptyList();
        }
        return getDao().findByTitle(title);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findAllSparseEditableProjects(org.tdar.core.bean.entity.Person)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> findAllSparseEditableProjects(Person person) {
        if (PersistableUtils.isNullOrTransient(person)) {
            return new ArrayList<>();
        }
        return getDao().findAllEditableProjects(person);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findAllSparse()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> findAllSparse() {
        return getDao().findAllSparse();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findAllResourcesInProject(org.tdar.core.bean.resource.Project, org.tdar.core.bean.resource.Status)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<InformationResource> findAllResourcesInProject(Project p, Status... statuses) {
        ScrollableResults informationResources = getDao().findAllResourcesInProject(p, statuses);
        Set<InformationResource> results = new HashSet<>();
        for (InformationResource ir : new ImmutableScrollableCollection<InformationResource>(informationResources)) {
            results.add(ir);
        }

        return results;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findRecentlyEditedResources(org.tdar.core.bean.entity.Person, int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findRecentlyEditedResources(Person updater, int maxResults) {
        return getDao().findSparseRecentlyEditedResources(updater, maxResults);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findEmptyProjects(org.tdar.core.bean.entity.Person)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Project> findEmptyProjects(Person updater) {
        return getDao().findEmptyProjects(updater);
    }


    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#findSparseTitleIdProjectListByPerson(org.tdar.core.bean.entity.TdarUser, boolean)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findSparseTitleIdProjectListByPerson(TdarUser person, boolean isAdmin) {
        // get all of the collections (direct/inherited) that bestow modify-metadata rights to the specified user
        Set<SharedCollection> collections = resourceCollectionDao.findFlattendCollections(person, GeneralPermissions.MODIFY_METADATA, SharedCollection.class);

        // find all of the editable projects for the user (either directly assigned or via the specified collections)
        List<Long> collectionIds = PersistableUtils.extractIds(collections);
        List<Resource> editableResources = authorizedUserDao.findEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin, true, collectionIds);

        return editableResources;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#containsIntegratableDatasets(org.tdar.core.bean.resource.Project)
     */
    @Override
    @Transactional(readOnly=true)
    public Boolean containsIntegratableDatasets(Project project) {
        return getDao().containsIntegratableDatasets(project);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#containsIntegratableDatasets(java.util.List)
     */
    @Override
    @Transactional(readOnly=true)
    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        return getDao().containsIntegratableDatasets(projectIds);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.resource.ProjectService#getProjectAsJson(org.tdar.core.bean.resource.Project, org.tdar.core.bean.entity.TdarUser, java.lang.String)
     */
    @Override
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
