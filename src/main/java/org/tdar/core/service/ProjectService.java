package org.tdar.core.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.resource.FullUserDao;
import org.tdar.core.dao.resource.ProjectDao;
import org.tdar.core.dao.resource.ReadUserDao;

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
    private FullUserDao fullUserDao;
    @Autowired
    private ReadUserDao readUserDao;

    @Autowired
    public void setDao(ProjectDao dao) {
        super.setDao(dao);
    }

    @Transactional(readOnly = true)
    public Project find(Long id) {
        Project project = getDao().find(id);
        if (project == null) {
            return Project.NULL;
        }
        return project;
    }

    @Transactional(readOnly = false)
    public void deleteAllKeywords(Resource resource) {
        delete(resource.getSiteNameKeywords());
        delete(resource.getCultureKeywords());
        delete(resource.getOtherKeywords());
        // delete(resource.getMaterialKeywords());
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
    public List<Project> findAllSorted() {
        return getDao().findAllSorted();
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
    public List<Project> findViewableProjects(Person person) {
        return getDao().findViewableProjects(person);
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
    public List<Project> findSparseTitleIdProjectListByPerson(Person person) {
        return fullUserDao.findSparseTitleIdProjectListByPerson(person);
    }

    @Transactional(readOnly = true)
    public List<Project> findReadUserProjects(Person person) {
        return readUserDao.findProjectsByPerson(person);
    }

    public Boolean containsIntegratableDatasets(Project project) {
        return getDao().containsIntegratableDatasets(project);
    }

    public Boolean containsIntegratableDatasets(List<Long> projectIds) {
        return getDao().containsIntegratableDatasets(projectIds);
    }

    public void setFullUserDao(FullUserDao fullUserDao) {
        this.fullUserDao = fullUserDao;
    }

    public void setReadUserDao(ReadUserDao readUserDao) {
        this.readUserDao = readUserDao;
    }

}
