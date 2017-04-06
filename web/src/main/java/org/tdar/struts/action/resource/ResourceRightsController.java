package org.tdar.struts.action.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
public class ResourceRightsController extends AbstractAuthenticatableAction implements
        Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 8551222659351457637L;

    private static final String RIGHTS = "rights";

    private Resource resource;
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

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
        return resource;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public void setPersistable(Resource persistable) {
        this.resource = persistable;
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
            @Result(name = SUCCESS, location = "../dashboard/manage.ftl")
    })
    public String edit() throws TdarActionException {
        return SUCCESS;
    }
    
    @Override
    public void prepare() throws Exception {
        prepareAndLoad(this, RequestType.EDIT);
        InternalCollection internal = resource.getInternalResourceCollection();

        getLogger().debug("internal:{}", internal);
        if (internal != null) {
            internal.getAuthorizedUsers().forEach(au -> {
                UserRightsProxy proxy = new UserRightsProxy();
                proxy.setDisplayName(au.getUser().getProperName());
                proxy.setId(au.getUser().getId());
                proxy.setPermission(au.getGeneralPermission());
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
            proxies.add(proxy);
        });
        getLogger().debug("proxies:{}", proxies);
    }

    public void setId(Long id) {
        this.id = id;
    }

}
