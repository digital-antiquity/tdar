package org.tdar.core.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.BookmarkedResourceDao;

/**
 * Helps create and manage Bookmarks for users. Bookmarks are for keeping track of items, and identifying items for DataIntegration
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class BookmarkedResourceServiceImpl  extends ServiceInterface.TypedDaoBase<BookmarkedResource, BookmarkedResourceDao> implements BookmarkedResourceService {

    /* (non-Javadoc)
     * @see org.tdar.core.service.BookmarkedResourceService#applyTransientBookmarked(java.util.Collection, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public <R> void applyTransientBookmarked(Collection<R> resources, TdarUser person) {
        for (R resource_ : resources) {
            if (resource_ instanceof Resource) {
                Resource resource = (Resource)resource_;
                if (isAlreadyBookmarked(resource, person)) {
                    resource.setBookmarked(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.BookmarkedResourceService#isAlreadyBookmarked(org.tdar.core.bean.resource.Resource, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyBookmarked(Resource resource, TdarUser person) {
        return getDao().isAlreadyBookmarked(resource, person);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.BookmarkedResourceService#bookmarkResource(org.tdar.core.bean.resource.Resource, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public boolean bookmarkResource(Resource resource, TdarUser person) {
        getDao().markWritableOnExistingSession(person);
        if (getDao().isAlreadyBookmarked(resource, person)) {
            getLogger().trace("person {} already bookmarked resource {}s", person, resource.getId());
            return false;
        }
        getLogger().trace("{} creating bookmark for {}", person, resource);
        BookmarkedResource bookmark = new BookmarkedResource();
        bookmark.setResource(resource);
        bookmark.setTimestamp(new Date());
        bookmark.setPerson(person);
        try {
            getDao().save(bookmark);
            return true;
        } catch (ConstraintViolationException exception) {
            getLogger().error("Didn't save duplicate bookmark {}", bookmark, exception);
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.BookmarkedResourceService#removeBookmark(org.tdar.core.bean.resource.Resource, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public boolean removeBookmark(Resource resource, TdarUser person) {
        BookmarkedResource bookmark = getDao().findBookmark(resource, person);
        getDao().markWritableOnExistingSession(bookmark);
        getDao().markWritableOnExistingSession(person);
        if (bookmark == null) {
            return false;
        }
        resource.getBookmarkedResources().remove(bookmark);
        getDao().delete(bookmark);
        getDao().saveOrUpdate(person);
        return true;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.BookmarkedResourceService#findBookmarkedResourcesByPerson(org.tdar.core.bean.entity.TdarUser, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findBookmarkedResourcesByPerson(TdarUser person, List<Status> statuses) {
        return getDao().findBookmarkedResourcesByPerson(person, statuses);
    }
}
