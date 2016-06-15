package org.tdar.struts.action.entity;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractDeleteAction;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity")
public class EntityDeleteAction extends AbstractDeleteAction<Creator> implements Preparable {

    private static final long serialVersionUID = -6793209214179204672L;
    @Autowired
    private transient EntityService entityService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    protected Creator loadPersistable() {
        return entityService.find(getId());
    }

    @Override
    protected void delete(Creator collection) {
        entityService.deleteForController(collection, getDeletionReason(), getAuthenticatedUser());
    }

    @Override
    protected DeleteIssue getDeletionIssues() {
        return entityService.getDeletionIssues(this, getPersistable());
    }

    @Override
    protected boolean canDelete() {
        if (authorizationService.can(InternalTdarRights.DELETE_ANYTHING, getAuthenticatedUser())) {
            return true;
        }

        return authorizationService.canEdit(getAuthenticatedUser(), getPersistable());
    }

}
