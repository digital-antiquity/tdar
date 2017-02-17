package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.VisibleCollection;
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
        Query<InformationResourceFile> query = getCurrentSession().createNamedQuery(QUERY_INFORMATIONRESOURCE_FIND_BY_FILENAME,InformationResourceFile.class);
        query.setParameter("filename", filename).setParameter("resource", resource);
        return (InformationResourceFile) query.getSingleResult();
    }

    public <E> List<E> findRandomFeaturedResource(boolean restrictToFiles, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, null, null, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInProject(boolean restrictToFiles, Project project, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, null, project, maxResults);
    }

    public <E> List<E> findRandomFeaturedResourceInCollection(boolean restrictToFiles, List<VisibleCollection> collections, int maxResults) {
        return findRandomFeaturedResource(restrictToFiles, collections, null, maxResults);
    }

    public List<BrowseDecadeCountCache> findResourcesByDecade(Status ... statuses) {
        Query<BrowseDecadeCountCache> query = getCurrentSession().createNamedQuery(QUERY_RESOURCES_BY_DECADE,BrowseDecadeCountCache.class);
        query.setParameter("statuses", Arrays.asList(statuses));
        return query.getResultList();
    }

    public List<BrowseYearCountCache> findResourcesByYear(Status ... statuses) {
        Query query = getCurrentSession().createNativeQuery(QUERY_SQL_RESOURCES_BY_YEAR);
        List<BrowseYearCountCache> result = new ArrayList<BrowseYearCountCache>();
        for (Object obj : query.getResultList()) {
            Object[] row = (Object[]) obj;
            result.add(new BrowseYearCountCache(((Number) row[0]).intValue(), ((Number) row[1]).longValue()));
        }
        return result;
    }

    public InformationResource findByDoi(String doi) {
        Query<InformationResource> query = getCurrentSession().createNamedQuery(QUERY_BY_DOI, InformationResource.class);
        query.setParameter("doi", doi);
        return (InformationResource) query.getSingleResult();
    }

}
