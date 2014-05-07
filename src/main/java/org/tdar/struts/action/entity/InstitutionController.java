package org.tdar.struts.action.entity;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.statistics.CreatorViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.AbstractPersistableController;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/institution")
public class InstitutionController extends AbstractCreatorController<Institution> {

    private static final long serialVersionUID = 2051510910128780834L;

    @Autowired
    private transient EntityService entityService;

    private String name;

    @Override
    protected String save(Institution persistable) {
        if (hasActionErrors()) {
            return INPUT;
        }

        // name has a unique key; so we need to be careful with it
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
            Institution findInstitutionByName = entityService.findInstitutionByName(name);
            if (findInstitutionByName != null) {
                addActionError(getText("institutionController.cannot_rename", Arrays.asList(name)));
            }
        }
    }

    @Override
    protected void delete(Institution persistable) {
    }

    @Override
    public Class<Institution> getPersistableClass() {
        return Institution.class;
    }

    @Override
    public String loadViewMetadata() {
        if (!isEditor()) {
            CreatorViewStatistic cvs = new CreatorViewStatistic(new Date(), getPersistable());
            getGenericService().saveOrUpdate(cvs);
        }
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
        if (!isAuthenticated()) {
            return false;
        }
        return getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_INSTITUTIONAL_ENTITES, getAuthenticatedUser());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
