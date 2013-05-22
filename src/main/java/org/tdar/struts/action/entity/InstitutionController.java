package org.tdar.struts.action.entity;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.struts.action.AbstractPersistableController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/institution")
public class InstitutionController extends AbstractPersistableController<Institution> {

    public static final String ERROR_INSTITUTION_NAME_BLANK = "Institution name cannot be blank.";
    private static final long serialVersionUID = 2051510910128780834L;

    private String name;

    @Override
    protected String save(Institution persistable) {
        if (hasActionErrors())
            return INPUT;

        //name has a unique key; so we need to be careful with it
        persistable.setName(getName());
        if (Persistable.Base.isNullOrTransient(persistable)) {
            getGenericService().save(persistable);
        } else {
            getGenericService().update(persistable);
        }
        return SUCCESS;
    }

     @Override
     public void validate() {
         if (!StringUtils.equalsIgnoreCase(name, getInstitution().getName())) {
             Institution findInstitutionByName = getEntityService().findInstitutionByName(name);
             if (findInstitutionByName != null) {
                 throw new TdarValidationException(String.format("Cannot rename institution to %s because it already exists", name));
             }
         }
     }
//
//    @Override
//    public void prepare() {
//        super.prepare();
//        // during a save request, struts will have potentially modified the fields of the persistable. This has the potential cause hibernate
//        // to throw exceptions (e.g. uniqueconstraint) even if we don't ultimately save the modified record. So we detach the persistable here,
//        // before struts modifies any fields, so that hibernate won't freak out at us.
//        // getGenericService().detachFromSession(getPersistable());
//    }

    @Override
    protected void delete(Institution persistable) {
    }

    @Override
    public Class<Institution> getPersistableClass() {
        return Institution.class;
    }

    @Override
    public String loadViewMetadata() {
        return SUCCESS;
    }

    @Override
    public String loadEditMetadata() {
        if (Persistable.Base.isNotNullOrTransient(getPersistable())) {
            setName(getPersistable().getName());
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
