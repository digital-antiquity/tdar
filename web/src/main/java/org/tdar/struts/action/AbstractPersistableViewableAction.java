package org.tdar.struts.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.Sortable;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.service.FileSystemResourceService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.auth.AntiSpamHelper;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.search.query.facet.Facet;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Provides basic metadata support for controllers that manage subtypes of
 * Resource.
 * 
 * Don't extend this class unless you need this metadata to be set.
 * 
 * 
 * @author Adam Brin, <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, location = "view.ftl"),
        @Result(name = TdarActionSupport.BAD_SLUG, type = TdarActionSupport.TDAR_REDIRECT,
                location = "${id}/${persistable.slug}${slugSuffix}", params = { "ignoreParams", "id,keywordPath,slug" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "404" }),
        @Result(name = TdarActionSupport.DRAFT, location = "/WEB-INF/content/errors/resource-in-draft.ftl")
})
public abstract class AbstractPersistableViewableAction<P extends Persistable> extends AbstractAuthenticatableAction implements Preparable, ViewableAction<P>,
        PersistableLoadingAction<P> {

    private static final long serialVersionUID = -5126488373034823160L;

    private AntiSpamHelper h = new AntiSpamHelper();
    private Long startTime = -1L;
    private P persistable;
    private Long id;
    private Status status;
    @SuppressWarnings("unused")
    private Class<P> persistableClass;
    public final static String REDIRECT_HOME = "REDIRECT_HOME";
    public final static String REDIRECT_PROJECT_LIST = "PROJECT_LIST";
    private List<AuthorizedUser> authorizedUsers;
    private List<String> authorizedUsersFullNames = new ArrayList<String>();

    private ResourceSpaceUsageStatistic totalResourceAccessStatistic;
    private ResourceSpaceUsageStatistic uploadedResourceAccessStatistic;

    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient FileSystemResourceService fileSystemResourceService;
    
    private boolean redirectBadSlug;
    private String slug;
    private String slugSuffix;

    public static String formatTime(long millis) {
        Date dt = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        // SimpleDateFormat sdf = new SimpleDateFormat("H'h, 'm'm, 's's, 'S'ms'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // no offset
        return sdf.format(dt);
    }

    protected P loadFromId(final Long id) {
        return getGenericService().find(getPersistableClass(), id);
    }

    @HttpOnlyIfUnauthenticated
    @Actions(value = {
            @Action(value = "{id}/{slug}"),
            @Action(value = "{id}/"),
            @Action(value = "{id}")
    })
    @SkipValidation
    public String view() throws TdarActionException {
        if (isRedirectBadSlug()) {
            return BAD_SLUG;
        }
        String resultName = SUCCESS;

        resultName = loadViewMetadata();
        loadExtraViewMetadata();
        return resultName;
    }

    /*
     * override this to load extra metadata for the "view"
     */
    public void loadExtraViewMetadata() {

    }

    /**
     * The 'contributor' property only affects which menu items we show (for now). Let non-contributors perform
     * CRUD actions, but send them a reminder about the 'contributor' option in the prefs page
     * 
     * FIXME: this needs to be centralized, as it's not going to be caught in all of the location it exists in ... eg: editColumnMetadata ...
     */
    protected void checkForNonContributorCrud() {
        if (!isContributor()) {
            // FIXME: The html here could benefit from link to the prefs page. Devise a way to hint to the view-layer that certain messages can be decorated
            // and/or replaced.
            addActionMessage(getText("abstractPersistableController.change_profile"));
        }
    }

    /**
     * Generic method enabling override for whether a record is viewable
     * 
     * @return boolean whether the user can VIEW this resource
     * @throws TdarActionException
     */
    @Override
    public boolean authorize() throws TdarActionException {
        return true;
    }

    @Override
    public P getPersistable() {
        return persistable;
    }

    public void setPersistable(P persistable) {
        this.persistable = persistable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method is invoked when the paramsPrepareParamsInterceptor stack is
     * applied. It allows us to fetch an entity from the database based on the
     * incoming resourceId param, and then re-apply params on that resource.
     * 
     * @throws TdarActionException
     * 
     * @see <a href="http://blog.mattsch.com/2011/04/14/things-discovered-in-struts-2/">Things discovered in Struts 2</a>
     */
    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, RequestType.VIEW);
        checkValidRequest(this);
        handleSlug();
    }

    protected void handleSlug() {
        if (!handleSlugRedirect(persistable, this)) {
            setRedirectBadSlug(true);
        }
    }

    protected boolean isPersistableIdSet() {
        return PersistableUtils.isNotNullOrTransient(getPersistable());
    }

    public void setPersistableClass(Class<P> persistableClass) {
        this.persistableClass = persistableClass;
    }

    public abstract String loadViewMetadata() throws TdarActionException;

    protected boolean isNullOrNew() {
        return !isPersistableIdSet();
    }

    /**
     * @param authorizedUsers
     *            the authorizedUsers to set
     */
    public void setAuthorizedUsers(List<AuthorizedUser> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    /**
     * @return the authorizedUsers
     */
    public List<AuthorizedUser> getAuthorizedUsers() {
        if (authorizedUsers == null) {
            authorizedUsers = new ArrayList<AuthorizedUser>();
        }
        return authorizedUsers;
    }

    public AuthorizedUser getBlankAuthorizedUser() {
        AuthorizedUser user = new AuthorizedUser();
        user.setUser(new TdarUser());
        return user;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = GeneralPermissions.getAvailablePermissionsFor(getPersistableClass());
        return permissions;
    }

    /**
     * @return the startTime
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * @return the startTime
     */
    public Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * @param startTime
     *            the startTime to set
     */
    public void setStartTime(Long startTime) {
        getLogger().trace("set start time: " + startTime);
        this.startTime = startTime;
    }

    public ResourceSpaceUsageStatistic getUploadedResourceAccessStatistic() {
        return uploadedResourceAccessStatistic;
    }

    public void setUploadedResourceAccessStatistic(ResourceSpaceUsageStatistic uploadedResourceAccessStatistic) {
        this.uploadedResourceAccessStatistic = uploadedResourceAccessStatistic;
    }

    public ResourceSpaceUsageStatistic getTotalResourceAccessStatistic() {
        return totalResourceAccessStatistic;
    }

    public void setTotalResourceAccessStatistic(ResourceSpaceUsageStatistic totalResourceAccessStatistic) {
        this.totalResourceAccessStatistic = totalResourceAccessStatistic;
    }

    public Status getStatus() {
        if (status != null) {
            return status;
        }
        if (getPersistable() instanceof HasStatus) {
            return ((HasStatus) getPersistable()).getStatus();
        }
        return null;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Status> getStatuses() {
        return new ArrayList<Status>(authorizationService.getAllowedSearchStatuses(getAuthenticatedUser()));
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public List<String> getAuthorizedUsersFullNames() {
        return authorizedUsersFullNames;
    }

    public void setAuthorizedUsersFullNames(List<String> authorizedUsersFullNames) {
        this.authorizedUsersFullNames = authorizedUsersFullNames;
    }

    public String getSlugSuffix() {
        return slugSuffix;
    }

    public void setSlugSuffix(String slugSuffix) {
        this.slugSuffix = slugSuffix;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isRedirectBadSlug() {
        return redirectBadSlug;
    }

    public void setRedirectBadSlug(boolean redirectBadSlug) {
        this.redirectBadSlug = redirectBadSlug;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.VIEW_ANYTHING;
    }

    protected void reSortFacets(ResourceFacetedAction handler, Sortable persistable) {
        // sort facets A-Z unless sortOption explicitly otherwise
        if (PersistableUtils.isNotNullOrTransient(getPersistable()) && CollectionUtils.isNotEmpty(handler.getResourceTypeFacets())) {
            final boolean reversed = persistable.getSortBy() == SortOption.RESOURCE_TYPE_REVERSE;
            Collections.sort(handler.getResourceTypeFacets(), new Comparator<Facet>() {
                @Override
                public int compare(Facet o1, Facet o2) {
                    if (reversed) {
                        return o2.getRaw().compareTo(o1.getRaw());
                    } else {
                        return o1.getRaw().compareTo(o2.getRaw());
                    }
                }
            });
        }
    }

    /**
     * Is the specified public file available for the current resource
     * 
     * @param filename
     * @return
     */
    protected boolean checkHostedFileAvailable(String filename, Long id) {
        return fileSystemResourceService.checkHostedFileAvailable(filename, FilestoreObjectType.COLLECTION, id);
    }

}
