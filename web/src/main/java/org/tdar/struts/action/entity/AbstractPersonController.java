package org.tdar.struts.action.entity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.AuthorizationService;
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
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient EntityService entityService;

    @Override
    public String loadEditMetadata() throws TdarActionException {
        email = getPersistable().getEmail();
        return SUCCESS;
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
        if (hasErrors()) {
            getLogger().info("errors present, returning INPUT");
            getLogger().info("actionErrors:{}", getActionErrors());
            getLogger().info("fieldErrors:{}", getFieldErrors());
            return INPUT;
        }
        try {
            entityService.savePersonforController(person, getEmail(), getInstitutionName(), generateFileProxy(getFileFileName(), getFile()));
        } catch (Exception e) {
            addActionError(e.getLocalizedMessage());
            return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public boolean authorize() {
        return authorizationService.canEditCreator(getPersistable(), getAuthenticatedUser());
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
        String path = "browse/creators";
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
