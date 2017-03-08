package org.tdar.struts.action.entity.user;

import com.opensymphony.xwork2.Preparable;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.ViewableAction;

import java.util.ArrayList;
import java.util.List;

import static org.geotools.kml.v22.KML.value;

/**
 * List the UserInvites created by the current User.
 *
 * <em><strong>Editor's Note:</strong>First, let's address the "elephant in the room":  this class name is terrible.  However, it's consistent
 * with the naming for List[ThingThatInviteAssociatesFrom]UserInviteAction.  Please feel free to rename.</em>
 */
@ParentPackage("secured")
@Namespace("/entity/user")
@Component
@Scope("prototype")
public class ListUserUserInvitesAction extends AbstractAuthenticatableAction implements Preparable, ViewableAction<TdarUser>, PersistableLoadingAction<TdarUser> {

    private TdarUser user;
    private Long id;
    private List<UserInvite> userInvites = new ArrayList<>();

    @Autowired
    private ResourceCollectionService resourceCollectionService;



    @Override
    public void prepare() throws TdarActionException {
        prepareAndLoad(this, AbstractPersistableController.RequestType.VIEW);
        userInvites.addAll(resourceCollectionService.findUserInvites(getPersistable()));
    }


    @Override
    @Action(value="list-invites", results={
            @Result(name="success", location="list-invites.ftl")
    })
    public String execute() {
        return "success";
    }


    /**
     * Generic method enabling override for whether a record is viewable
     *
     * @return boolean whether the user can VIEW this resource
     * @throws TdarActionException
     */
    @Override
    public boolean authorize() throws TdarActionException {
        //Fixme: what are the appropriate rights for a user  to *view* a list of invites created by a user?
        return getAuthorizationService().canEditCreator(getAuthenticatedUser(), getPersistable());
    }

    @Override
    public TdarUser getPersistable() {
        return user;
    }

    @Override
    public Class<TdarUser> getPersistableClass() {
        return TdarUser.class;
    }

    @Override
    public void setPersistable(TdarUser user) {
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<UserInvite> getUserInvites() {
        return userInvites;
    }

}
