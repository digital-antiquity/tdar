package org.tdar.struts.action.entity.user.rights;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/user/rights")
public class UserRightsAction extends AbstractAuthenticatableAction implements Preparable, Validateable {

    private static final long serialVersionUID = -967084931743446966L;
    private Long id;
    private TdarUser user;
    
    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private GenericService genericService;
    private List<Resource> findResourcesSharedWith;
    private List<SharedCollection> findCollectionsSharedWith;

    @Override
    public void prepare() throws Exception {
        this.user = genericService.find(TdarUser.class, id);
        setFindResourcesSharedWith(resourceCollectionService.findResourcesSharedWith(getAuthenticatedUser(), user));
        setFindCollectionsSharedWith(resourceCollectionService.findCollectionsSharedWith(getAuthenticatedUser(), getUser(), SharedCollection.class));
    }
    
    @Override
    @Action(value = "{id}", results = {
            @Result(name = SUCCESS, type = FREEMARKER, location = "rights.ftl"),
    })
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Override
    public void validate() {
        if (user == null) {
            addActionError("error.object_does_not_exist");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TdarUser getUser() {
        return user;
    }

    public void setUser(TdarUser user) {
        this.user = user;
    }

    public List<Resource> getFindResourcesSharedWith() {
        return findResourcesSharedWith;
    }

    public void setFindResourcesSharedWith(List<Resource> findResourcesSharedWith) {
        this.findResourcesSharedWith = findResourcesSharedWith;
    }

    public List<SharedCollection> getFindCollectionsSharedWith() {
        return findCollectionsSharedWith;
    }

    public void setFindCollectionsSharedWith(List<SharedCollection> findCollectionsSharedWith) {
        this.findCollectionsSharedWith = findCollectionsSharedWith;
    }
    
}
