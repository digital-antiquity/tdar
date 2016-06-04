package org.tdar.struts.action.entity;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/institution")
public class InstitutionController extends AbstractCreatorController<Institution> {

    private static final long serialVersionUID = 2051510910128780834L;

    @Autowired
    private transient EntityService entityService;
    @Autowired
    private transient AuthorizationService authorizationService;

    private String name;
    private String email;


    @Override
    protected String save(Institution persistable) {
        if (hasActionErrors()) {
            return INPUT;
        }
        entityService.saveInstitutionForController(persistable, name, email, generateFileProxy(getFileFileName(), getFile()));
        return SUCCESS;
    }

    @Override
    public void validate() {

        //if name changed, make sure it's not already taken
        if (!StringUtils.equalsIgnoreCase(name, getInstitution().getName())) {
            Institution findInstitutionByName = entityService.findInstitutionByName(name);
            if (findInstitutionByName != null) {
                addActionError(getText("institutionController.cannot_rename", Arrays.asList(name)));
            }
        }

        //if email changed, make sure it's not already taken
        if(StringUtils.isNotBlank(getEmail()) && !StringUtils.equalsIgnoreCase(email, getInstitution().getEmail())) {
            Institution inst = getGenericService().findByProperty(Institution.class, "email", email);
            if(inst != null) {
                addActionError("institutionController.email_taken");
            }
        }
    }

    @Override
    public Class<Institution> getPersistableClass() {
        return Institution.class;
    }

    @Override
    public String loadEditMetadata() {
        if (PersistableUtils.isNotNullOrTransient(getPersistable())) {
            setName(getPersistable().getName());
            setEmail(getPersistable().getEmail());
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
        String path = "browse/creators";
        getLogger().debug("{}?id={}", path, getId());
        return path;
    }

    @Override
    public boolean authorize() {
        if (!isAuthenticated()) {
            return false;
        }
        return authorizationService.canEdit(getAuthenticatedUser(), getInstitution());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
