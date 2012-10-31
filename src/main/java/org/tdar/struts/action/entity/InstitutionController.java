package org.tdar.struts.action.entity;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.struts.action.AbstractPersistableController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/institution")
public class InstitutionController extends AbstractPersistableController<Institution> {

    public static final String ERROR_INSTITUTION_NAME_BLANK = "Institution name cannot be blank.";
    private static final long serialVersionUID = 2051510910128780834L;

    @Override
    protected String save(Institution persistable) {
        if (hasActionErrors())
            return INPUT;

        if (Persistable.Base.isNullOrTransient(persistable)) {
            getGenericService().save(persistable);
        } else {
            getGenericService().update(persistable);
        }
        return SUCCESS;
    }

    // FIXME: USE INTERFACE INSTEAD
    // @Override
    // public void validate() {
    // if(StringUtils.isBlank(getPersistable().getName())) {
    // addActionError(ERROR_INSTITUTION_NAME_BLANK);
    // }
    // }

    @Override
    public void prepare() {
        super.prepare();
        // during a save request, struts will have potentially modified the fields of the persistable. This has the potential cause hibernate
        // to throw exceptions (e.g. uniqueconstraint) even if we don't ultimately save the modified record. So we detach the persistable here,
        // before struts modifies any fields, so that hibernate won't freak out at us.
        getGenericService().detachFromSession(getPersistable());
    }

    @Override
    protected void delete(Institution persistable) {
    }

    @Override
    public Class<Institution> getPersistableClass() {
        return Institution.class;
    }

    @Override
    public String loadMetadata() {
        return SUCCESS;
    }

    public Institution getInstitution() {
        return getPersistable();
    }

    public void setInstitution(Institution inst) {
        setPersistable(inst);
    }

    @Override
    public String getSaveSuccessPath() {
        // instead of a custom view page we will co-opt the browse/creator page.
        String path = "/browse/creators";
        getLogger().debug("{}?id={}", path, getId());
        return path;
    }

    @Override
    public boolean isEditable() {
        if (!isAuthenticated())
            return false;
        return getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_INSTITUTIONAL_ENTITES, getAuthenticatedUser());
    }

}
