package org.tdar.struts.action.resource;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractPersistableViewableAction;
import org.tdar.struts_base.action.TdarActionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jimdevos on 3/7/17.
 */
@ParentPackage("secured")
@Namespace("/resource")
@Component
@Scope("prototype")
public class ListResourceUserInvitesAction extends AbstractPersistableViewableAction<Resource> {

    private List<UserInvite> userInvites = new ArrayList<>();

    @Autowired
    private ResourceCollectionService resourceCollectionService;

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    @Override
    public String loadViewMetadata() throws TdarActionException {
        // baggage from APVA - shoudn't ever be called
        return "error";
    }


    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        userInvites.addAll(resourceCollectionService.findUserInvites(getPersistable()));
    }

    @Override
    @Action(value="list-invites", results={
            @Result(name="success", location="list-invites.ftl")
    })
    public String execute() {
        return "success";
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANY_RESOURCE;
    }

    @Override
    public boolean authorize() {
        return getAuthorizationService().canEdit(getAuthenticatedUser(), getPersistable());
    }

    public List<UserInvite> getUserInvites() {
        return userInvites;
    }
}
