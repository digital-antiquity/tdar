package org.tdar.struts.action.entity;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.statistics.CreatorViewStatistic;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@Component
public abstract class AbstractPersonController<P extends Person> extends AbstractCreatorController<P> {

    private static final long serialVersionUID = -5771859332133394964L;

    private String institutionName;

    private String email;

    @Autowired
    private transient XmlService xmlService;

    @Autowired
    private transient EntityService entityService;

    @Override
    public String loadEditMetadata() throws TdarActionException {
        String ret = super.loadEditMetadata();
        email = getPersistable().getEmail();
        return ret;
    }


    public void validateUniqueEmail() {
        if (StringUtils.isBlank(getPersistable().getEmail())) {
            return;
        }

        // person2 should be null or same person being edited. Anything else means email address is not unique.
        Person person2 = entityService.findByEmail(email);
        if (person2 != null) {
            if (!person2.equals(getPersistable())) {
                addFieldError("email", getText("userAccountController.username_not_available"));
            }
        }

    }

    @Override
    public void validate() {
        validateUniqueEmail();
    }

    @Override
    @Validations(
            emails = { @EmailValidator(type = ValidatorType.SIMPLE, fieldName = "email", key = "userAccountController.email_invalid") },
            stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.SIMPLE, fieldName = "contributorReason",
                    key = "userAccountController.contributorReason_invalid", maxLength = "512") }
            )
    public String save(Person person) {
        return savePersonInfo(person);
    }

    protected String savePersonInfo(Person person) {
        if (!StringUtils.equals(email, getPersistable().getEmail())) {
            getPersistable().setEmail(email);
        }
        if (hasErrors()) {
            getLogger().info("errors present, returning INPUT");
            getLogger().info("actionErrors:{}", getActionErrors());
            getLogger().info("fieldErrors:{}", getFieldErrors());
            return INPUT;
        }
        getLogger().debug("saving person: {} with institution {} ", person, institutionName);
        if (StringUtils.isBlank(institutionName)) {
            person.setInstitution(null);
        }
        else {
            // if the user changed the person's institution, find or create it
            Institution persistentInstitution = entityService.findOrSaveCreator(new Institution(institutionName));
            getLogger().debug("setting institution to persistent: " + persistentInstitution);
            person.setInstitution(persistentInstitution);
        }

        getGenericService().saveOrUpdate(person);
        xmlService.logRecordXmlToFilestore(getPersistable());

        // If the user is editing their own profile, refresh the session object if needed
        return SUCCESS;
    }

    @Override
    public boolean isViewable() throws org.tdar.struts.action.TdarActionException {
        if (!isEditable()) {
            throw new TdarActionException(StatusCode.UNAUTHORIZED, getText("abstactPersistableController.unable_to_view_edit"));
        }
        return true;
    };


    @Override
    public boolean isEditable() {
        return getAuthenticatedUser().equals(getPersistable())
                || getAuthenticationAndAuthorizationService().can(InternalTdarRights.EDIT_PERSONAL_ENTITES, getAuthenticatedUser());
    }

    @Override
    protected void delete(Person persistable) {
        xmlService.logRecordXmlToFilestore(getPersistable());

        // the actual delete is being done by persistableController. We don't delete any relations since we want the operation to fail if any exist.
    }


    @Override
    public String loadViewMetadata() {
        // nothing to do here, the person record was already loaded by prepare()

        if (!isEditor() && !Persistable.Base.isEqual(getPersistable(), getAuthenticatedUser())) {
            CreatorViewStatistic cvs = new CreatorViewStatistic(new Date(), getPersistable());
            getGenericService().saveOrUpdate(cvs);
        }

        return SUCCESS;
    }


    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    // return true if the persistable==authUser
    public boolean isEditingSelf() {
        return getAuthenticatedUser().equals(getPersistable());
    }


    @Override
    public String getSaveSuccessPath() {
        // instead of a custom view page we will co-opt the browse/creator page.
        String path = "/browse/creators";
        getLogger().debug("{}?id={}", path, getId());
        return path;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String personEmail) {
        this.email = personEmail;
    }

}
