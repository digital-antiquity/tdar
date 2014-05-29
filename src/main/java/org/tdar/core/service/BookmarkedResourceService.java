package org.tdar.core.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.BookmarkedResourceDao;

/**
 * Helps create and manage Bookmarks for users. Bookmarks are for keeping track of items, and identifying items for DataIntegration
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Transactional
@Service
public class BookmarkedResourceService extends ServiceInterface.TypedDaoBase<BookmarkedResource, BookmarkedResourceDao> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional(readOnly = true)
    public <R extends Resource> void applyTransientBookmarked(Collection<R> resources, TdarUser person) {
        for (Resource resource : resources) {
            if (isAlreadyBookmarked(resource, person)) {
                resource.setBookmarked(true);
            }
        }
    }
    
    @Transactional(readOnly = true)
    public boolean isAlreadyBookmarked(Resource resource, TdarUser person) {
        return getDao().isAlreadyBookmarked(resource, person);
    }

    /**
     * Returns true if this a new @link BookmarkedResource for a @link Resource and @link Person was created as a result of this call, false otherwise.
     * 
     * @param resource
     * @param person
     * @return
     */
    @Transactional(readOnly=false)
    public boolean bookmarkResource(Resource resource, TdarUser person) {
        getDao().markWritableOnExistingSession(person);
        if (isAlreadyBookmarked(resource, person)) {
            logger.trace(String.format("person %s already bookmarked resource %s", person, resource.getId()));
            return false;
        }
        logger.trace("Creating bookmark for :" + person + " of " + resource.getId() + "[" + resource.getResourceType() + "]");
        BookmarkedResource bookmark = new BookmarkedResource();
        bookmark.setResource(resource);
        // FIXME: names should be editable by the user, and have a better default.
        // not sure why names are constructed this way, was in previous bookmarking code.
        bookmark.setName("Bookmark for " + TdarConfiguration.getInstance().getSiteAcronym() + " resource:" + resource.getId());
        bookmark.setTimestamp(new Date());
        bookmark.setPerson(person);
        getDao().save(bookmark);
        return true;
    }

    /**
     * Returns true if a @link BookmarkedResouce exists for a @link Person and @link Resource and it was removed successfully.
     * 
     * @param resource
     * @param person
     * @return
     */
    @Transactional(readOnly=false)
    public boolean removeBookmark(Resource resource, TdarUser person) {
        BookmarkedResource bookmark = getDao().findBookmark(resource, person);
        getDao().markWritableOnExistingSession(bookmark);
        getDao().markWritableOnExistingSession(person);
       if (bookmark == null) {
            return false;
        }
        person.getBookmarkedResources().remove(bookmark);
        getDao().delete(bookmark);
        getDao().saveOrUpdate(person);
        return true;
    }

    /**
     * Find all @link Resource entries that are referred to by a @link BookmarkedResource and a @link Person with a specified set of @link Status entries.
     * 
     * @param person
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findBookmarkedResourcesByPerson(TdarUser person, List<Status> statuses) {
        return getDao().findBookmarkedResourcesByPerson(person, statuses);
    }
}
