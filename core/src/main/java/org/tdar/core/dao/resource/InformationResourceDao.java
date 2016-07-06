package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.cache.BrowseDecadeCountCache;
import org.tdar.core.cache.BrowseYearCountCache;

/**
 * $Id$
 * 
 * Generic InformationResource DAO for finding generic InformationResources.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component("informationResourceDao")
public class InformationResourceDao extends ResourceDao<InformationResource> {

    public InformationResourceDao() {
        super(InformationResource.class);
    }

    public InformationResourceFile findFileByFilename(InformationResource resource, String filename) {
        Query query = getCurrentSession().getNamedQuery(QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME);
        query.setString("filename", filename).setEntity("resource", resource);
        return (InformationResourceFile) query.uniqueResult();
    }

    public <E> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, null, null, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, null, project, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, List<ResourceCollection> collections, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, collections, null, maxResults);
    }

    @SuppressWarnings("unchecked")
    public List<BrowseDecadeCountCache> findResourcesByDecade(Status ... statuses) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCES_BY_DECADE);
        query.setParameterList("statuses", statuses);
        return query.list();
    }

    public List<BrowseYearCountCache> findResourcesByYear(Status ... statuses) {
        Query query = getCurrentSession().createSQLQuery(QUERY_SQL_RESOURCES_BY_YEAR);
        List<BrowseYearCountCache> result = new ArrayList<BrowseYearCountCache>();
        for (Object obj : query.list()) {
            Object[] row = (Object[]) obj;
            result.add(new BrowseYearCountCache(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));
        }
        return result;
    }

    public InformationResource findByDoi(String doi) {
        Query query = getCurrentSession().getNamedQuery(QUERY_BY_DOI);
        query.setParameter("doi", doi);
        return (InformationResource) query.uniqueResult();
    }

}
