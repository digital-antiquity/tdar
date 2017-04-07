package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Namespaces;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespaces({
        @Namespace("/document/rights"),
        @Namespace("/image/rights"),
        @Namespace("/dataset/rights"),
        @Namespace("/image/rights"),
        @Namespace("/resource/rights"),
        @Namespace("/geospatial/rights"),
        @Namespace("/sensory-data/rights"),
        @Namespace("/coding-sheet/rights"),
        @Namespace("/ontology/rights")
})
public class ResourceRightsController extends AbstractAuthenticatableAction implements
        Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 8551222659351457637L;

    private static final String RIGHTS = "{id}";

    private static final String SUCCESS_INVITE = "invite";
    private static final String INVITE = "invite.ftl";

    private Resource resource;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;
    @Autowired
    private transient EntityService entityService;

    private List<UserRightsProxy> proxies = new ArrayList<>();

    private Long id;

    @Override
    public boolean authorize() {
        return authorizationService.canEdit(getAuthenticatedUser(), getPersistable());
    }

    public UserRightsProxy getBlankProxy() {
        return new UserRightsProxy();
    }

    public List<UserRightsProxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<UserRightsProxy> proxies) {
        this.proxies = proxies;
    }

    @Override
    public Resource getPersistable() {
        return getResource();
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public void setPersistable(Resource persistable) {
        this.setResource(persistable);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }

    @SkipValidation
    @Action(value = RIGHTS, results = {
            @Result(name = SUCCESS, location = "../rights.ftl")
    })
    public String edit() throws TdarActionException {
        setupEdit();
        return SUCCESS;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        List<GeneralPermissions> permissions = GeneralPermissions.getAvailablePermissionsFor(getPersistableClass());
        return permissions;
    }

    @SkipValidation
    @Action(value = SAVE, results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${resource.detailUrl}"),
            @Result(name= SUCCESS_INVITE, location = INVITE),
            @Result(name = INPUT, location = RIGHTS)
    })
    @PostOnly
    public String save() {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        List<UserInvite> invites = new ArrayList<>();
        for (UserRightsProxy proxy : proxies) {
            if (proxy == null || proxy.isEmpty()) {

            } else if (proxy.getEmail() != null) {
                invites.add(toInvite(proxy));
            } else if (proxy.getId() != null) {
                authorizedUsers.add(toAuthorizedUser(proxy));
            }
        }
        try {
            resourceCollectionService.saveAuthorizedUsersForResource(getResource(), authorizedUsers, true, getAuthenticatedUser());
        } catch (Exception e) {
            getLogger().error("issue saving", e);
            return INPUT;
        }
        return SUCCESS;
    }

    private UserInvite toInvite(UserRightsProxy proxy) {
        UserInvite invite = new UserInvite();
        invite.setAuthorizer(getAuthenticatedUser());
        invite.setDateExpires(proxy.getUntilDate());
        invite.setId(proxy.getInviteId());
        invite.setPermissions(proxy.getPermission());
        Person person = new Person(proxy.getFirstName(), proxy.getLastName(), proxy.getEmail());
        person = entityService.findOrSaveCreator(person);
        invite.setPerson(person);
        return invite;
    }

    private AuthorizedUser toAuthorizedUser(UserRightsProxy proxy) {
        getLogger().debug("{}", proxy.getUntil());
        AuthorizedUser au = new AuthorizedUser();
        au.setUser(getGenericService().find(TdarUser.class, proxy.getId()));
        au.setGeneralPermission(proxy.getPermission());
        au.setDateExpires(proxy.getUntilDate());
        getLogger().debug("{}", au);
        return au;
    }

    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
    }

    private void setupEdit() {
        InternalCollection internal = getResource().getInternalResourceCollection();

        getLogger().debug("internal:{}", internal);
        if (internal != null) {
            internal.getAuthorizedUsers().forEach(au -> {
                UserRightsProxy proxy = new UserRightsProxy();
                proxy.setDisplayName(au.getUser().getProperName());
                proxy.setId(au.getUser().getId());
                proxy.setPermission(au.getGeneralPermission());
                proxy.setUntilDate(au.getDateExpires());
                proxies.add(proxy);
                getLogger().debug("{}", au);
            });
        }

        List<UserInvite> invites = resourceCollectionService.findUserInvites(getPersistable());
        invites.forEach(invite -> {
            UserRightsProxy proxy = new UserRightsProxy();
            Person user = invite.getUser();
            proxy.setEmail(user.getEmail());
            proxy.setFirstName(user.getFirstName());
            proxy.setLastName(user.getLastName());
            proxy.setDisplayName(user.getProperName());
            proxy.setInviteId(invite.getId());
            proxy.setPermission(invite.getPermissions());
            proxy.setUntilDate(invite.getDateExpires());
            proxies.add(proxy);
        });
        getLogger().debug("proxies:{}", proxies);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
