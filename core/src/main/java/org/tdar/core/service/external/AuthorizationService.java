package org.tdar.core.service.external;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.HasUsers;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.RightsResolver;

public interface AuthorizationService {

    List<Resource> findEditableResources(TdarUser person, boolean isAdmin, List<ResourceType> resourceTypes);

    /**
     * Group Permissions tend to be hierarchical, hence, you may want to know if a user is a member of any of the nested hierarchy. Eg. EDITOR is a subset of
     * ADMIN
     */
    boolean isMemberOfAny(TdarUser person, TdarGroup... groups);

    /**
     * TdarGroups are represented in the external auth systems, but enable global permissions in tDAR; Admins, Billing Administrators, etc.
     */
    boolean isMember(TdarUser person, TdarGroup group);

    boolean isAdministrator(TdarUser person);

    boolean isBillingManager(TdarUser person);

    boolean isEditor(TdarUser person);

    /*
     * @return all of the resource statuses that a user is allowed to view in a search. Different users have different search permissions in different contexts.
     * A user
     * should be able to see their own DRAFTs, but never DELETED statuss unless they're an admin, for example
     */
    Set<Status> getAllowedSearchStatuses(TdarUser person);

    /**
     * Checks whether a @link Person has the rights to view a given resource. First, checking whether the person's @link TdarGroup permissions grant them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.VIEW_ALL or greater
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has view privileges
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has read permissions on resource according to the above policies, false otherwise.
     */
    boolean canViewResource(TdarUser person, Resource resource);

    /**
     * Checks whether a @link Person has the rights to edit a given resource. First, checking whether the person's @link TdarGroup permissions grant them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.MODIFY_METADATA or greater
     * <ol>
     * <li>the person and resource parameters are not null
     * <li>resource.submitter is the same as the person parameter
     * <li>the person has edit privileges
     * </ol>
     * 
     * @param person
     * @param resource
     * @return true if person has write permissions on resource according to the above policies, false otherwise.
     */
    boolean canEditResource(TdarUser person, Resource resource, GeneralPermissions basePermission);

    /**
     * Avoid using in general, but this allows us to ask whether a User has the "inherited" rights to do something as opposed to being granted direct rights to
     * it.
     * 
     * @param person
     * @param permission
     * @param ids
     * @return
     */
    boolean isAllowedToEditInherited(TdarUser person, Resource resource);

    /**
     * Checks whether a @link Person has the rights to edit a @link ResourceCollection. First, checking whether the person's @link TdarGroup permissions grant
     * them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.ADMINISTER_GROUP or greater
     * 
     * @param authenticatedUser
     * @param persistable
     * @return
     */
    boolean canEditCollection(TdarUser authenticatedUser, ResourceCollection persistable);

    /**
     * Checks whether a @link Person has the rights to administer a @link ResourceCollection. First, checking whether the person's @link TdarGroup permissions
     * grant
     * them
     * additional rights, for example if ADMIN; or if their @link ResourceCollection permissions include GeneralPermission.ADMINISTER_GROUP or greater
     * 
     * @param authenticatedUser
     * @param persistable
     * @return
     */
    boolean canAdministerCollection(TdarUser authenticatedUser, ResourceCollection persistable);

    /**
     * This method checks whether a person's group membership allows them to perform an associated right.
     * 
     * The @link InternalTdarRights enum associates global permissions with a @link TdarGroup or set of Groups. These global permissions allow
     * us to simplify permissions management by associating explicit rights with actions in the code, and managing permissions mappings in the enum(s).
     */
    boolean can(InternalTdarRights rights, TdarUser person);

    /**
     * This method checks whether a person's group membership denies them to perform an associated right @see #can
     */
    boolean cannot(InternalTdarRights rights, TdarUser person);

    /**
     * Evaluates an <E> (likely resource) in a list and removes it if the @link Person does not have the specified @link InternalTdarRights to perform the
     * action.
     */
    <E> void removeIfNotAllowed(Collection<E> list, E item, InternalTdarRights permission, TdarUser person);

    /**
     * Checks whether the @link Person can perform the specified edit action.
     * 
     * Part of the contract of @link AbstractPersistableController is to checks whether a @link Person (user) can View, Edit, Delete a @link Persistable prior
     * to rendering the page.
     * 
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.Accessible#canEdit(org.tdar.core.bean.entity.Person, org.tdar.core.bean.Persistable)
     */
    boolean canEdit(TdarUser authenticatedUser, Persistable item);

    /**
     * Checks whether the @link Person can perform the specified view action.
     * 
     * Part of the contract of @link AbstractPersistableController is to checks whether a @link Person (user) can View, Edit, Delete a @link Persistable prior
     * to rendering the page.
     * 
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.Accessible#canView(org.tdar.core.bean.entity.Person, org.tdar.core.bean.Persistable)
     */
    boolean canView(TdarUser authenticatedUser, Persistable item);

    /**
     * Returns true if the person is privileged, the resource is not restricted in access, or the person is granted @link GeneralPermissions.VIEW_ALL
     * 
     * @param person
     * @return
     */
    boolean canViewConfidentialInformation(TdarUser person, Resource resource);

    /**
     * Confirms that a @link Person has the rights to edit a @link InformationResource and upload files (@link GeneralPermissions.MODIFY_RECORD (as opposed to
     * 
     * @link GeneralPermissions.MODIFY_METADATA ))
     */
    boolean canUploadFiles(TdarUser person, Resource resource);

    /**
     * Pairs the @link InternalTdarRights permission with a @link ResourceCollection's @link GeneralPermission to check whether a user
     * can perform an action. Many of the other checks within this class are reflected as canDo checks or could be refactored as such
     * (a) checks if inputs are NULL
     * (b) checks if user is privileged (admin, etc.)
     * (c) checks if user is allowed to perform action based on @link AuthorizedUser / @link ResourceCollection permissions
     * (d) check's iuf user was submitter
     */
    boolean canDo(TdarUser person, HasAuthorizedUsers resource, InternalTdarRights equivalentAdminRight, GeneralPermissions permission);

    /*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFileVersion
     */
    boolean canDownload(TdarUser person, InformationResourceFileVersion irFileVersion);

    /*
     * Checks whether a @link Person has rights to download a given @link InformationResourceFile
     */
    boolean canDownload(TdarUser person, InformationResourceFile irFile);

    /*
     * Checks whether a person has the rights to view a collection based on their @link GeneralPermission on the @link ResourceCollection; filters by Shared
     * Visible collections
     */
    boolean canViewCollection(TdarUser person, VisibleCollection collection);

    /*
     * For the view-layer, we add a hint via the @link Viewable interface about whether a @link Resource @link Person or other @link Indexable is viewable or
     * not by
     * the @link Person performing the action.
     * 
     * (a) if the item has a @link Status, and it's Active, ok
     * (b) if not active, then check whether the @link Person can view that status
     * (c) if it's a collection, make sure it's public and shared
     * (d) otherwise, it's probably not
     */
    void applyTransientViewableFlag(Indexable p, TdarUser authenticatedUser);

    /*
     * checks that the specified @link Person can assign an @link Invoice to an @link Account; 1/2 of the check whether the person has the rights to do anything
     * with the
     * Invoive itself
     */
    boolean canAssignInvoice(Invoice invoice, TdarUser authenticatedUser);

    <R extends Resource> boolean isResourceViewable(TdarUser authenticatedUser, R resource);

    boolean isResourceEditable(TdarUser authenticatedUser, Resource resource);

    void applyTransientViewableFlag(InformationResourceFileVersion informationResourceFileVersion, TdarUser authenticatedUser);

    /**
     * Takes the request, and compares the referrer to the known list of referrers in the DB. If one matches, then we're okay
     * 
     * @param informationResourceFileVersion
     * @param apiKey
     * @param referrer
     * @return List of errors in the form of localization messages to be expanded e.g. from a TextProvider. An empty
     *         list indicates success.
     */
    List<String> checkValidUnauthenticatedDownload(InformationResourceFileVersion informationResourceFileVersion, String apiKey, String referrer_);

    boolean canEditWorkflow(TdarUser authenticatedUser, DataIntegrationWorkflow workflow);

    boolean canViewWorkflow(TdarUser authenticatedUser, DataIntegrationWorkflow workflow);

    boolean canEditAccount(TdarUser authenticatedUser, BillingAccount account);

    boolean canAdministerAccount(TdarUser authenticatedUser, BillingAccount account);

    void applyTransientViewableFlag(Resource r_, TdarUser authenticatedUser, Collection<Long> collectionIds);

    boolean canAdminiserUsersOn(HasAuthorizedUsers source, TdarUser actor);

    void updateAuthorizedMembers(HasUsers entity, List<TdarUser> members);

    boolean canViewBillingAccount(TdarUser authenticatedUser, BillingAccount account);

    boolean canEditCreator(TdarUser tdarUser, Creator<?> persistable);

    boolean canAddToCollection(TdarUser user, ResourceCollection collectionToAdd);

    <R extends ResourceCollection> boolean canRemoveFromCollection(R collection, TdarUser user);

    RightsResolver getRightsResolverFor(HasAuthorizedUsers resource, TdarUser actor, InternalTdarRights rights);

    
}