package org.tdar.struts.action.workspace.integration;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.integration.DataIntegrationWorkflow;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/workspace/settings")
@Result(name="workspace", location="/workspace/list",type=TdarActionSupport.REDIRECT)
public class IntegrationSettingsController extends AbstractPersistableController<DataIntegrationWorkflow> {

    private static final long serialVersionUID = -2663378965534285107L;
    public static final String SUCCESS_WORKSPACE = "workspace";
    private List<TdarUser> authorizedMembers = new ArrayList<>();

    @Autowired
    private transient AuthorizationService authorizationService;

    @Override
    public boolean authorize() {
        if (PersistableUtils.isNullOrTransient(getPersistable())) {
            return true;
        }
        return authorizationService.canEditWorkflow(getPersistable(), getAuthenticatedUser());
    }

    @Override
    protected String save(DataIntegrationWorkflow persistable) throws TdarActionException {
        List<TdarUser> members = getGenericService().loadFromSparseEntities(getAuthorizedMembers(), TdarUser.class);
        authorizationService.updateAuthorizedMembers(getPersistable(), members);
        return SUCCESS_WORKSPACE;
    }

    @Override
    public String loadEditMetadata() throws TdarActionException {
        // TODO Auto-generated method stub
        return SUCCESS;
    }

    @Override
    public Class<DataIntegrationWorkflow> getPersistableClass() {
        return DataIntegrationWorkflow.class;
    }

    public List<TdarUser> getAuthorizedMembers() {
        return authorizedMembers;
    }

    public void setAuthorizedMembers(List<TdarUser> authorizedMembers) {
        this.authorizedMembers = authorizedMembers;
    }

    @Override
    public void prepare() throws TdarActionException {
        super.prepare();
        for (TdarUser user : getPersistable().getAuthorizedMembers()) {
            getAuthorizedUsersFullNames().add(user.getProperName());
        }

    }

    public Person getBlankPerson() {
        return new Person();
    }
}
